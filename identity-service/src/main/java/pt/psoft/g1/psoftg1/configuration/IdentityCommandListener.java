package pt.psoft.g1.psoftg1.configuration;

import jakarta.validation.ValidationException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.CreateUserCommand;
import pt.psoft.g1.psoftg1.CreateUserReply;
import pt.psoft.g1.psoftg1.DeleteUserCommand;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.CreateUserRequest;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

@Component
public class IdentityCommandListener {

  private final UserService userService;

  public IdentityCommandListener(UserService userService) {
    this.userService = userService;
  }

  @RabbitListener(queues = "${identity.create-user.queue}")
  public CreateUserReply handleCreateUser(CreateUserCommand cmd) {
    try {
      CreateUserRequest request = new CreateUserRequest(cmd.username(), cmd.fullName(), cmd.password());
      request.setRole(cmd.role());
      // call your existing create-user logic (same validations)
      User user = userService.create(request);

      return new CreateUserReply(true, user.getId(), null, null);

    } catch (ConflictException e) {
      return new CreateUserReply(false, null, "USERNAME_EXISTS", e.getMessage());

    } catch (ValidationException e) {
      return new CreateUserReply(false, null, "VALIDATION", e.getMessage());

    } catch (AccessDeniedException e) {
      return new CreateUserReply(false, null, "FORBIDDEN", e.getMessage());

    } catch (Exception e) {
      return new CreateUserReply(false, null, "INTERNAL", "Failed to create user");
    }
  }

  @RabbitListener(queues = "${identity.delete-user.queue}")
  public void handleDeleteUser(DeleteUserCommand cmd) {
    if (cmd.userId() != null) {
      userService.delete(cmd.userId());
    }
  }
}
