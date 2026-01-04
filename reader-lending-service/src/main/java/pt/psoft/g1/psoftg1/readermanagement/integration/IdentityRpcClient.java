package pt.psoft.g1.psoftg1.readermanagement.integration;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import pt.psoft.g1.psoftg1.CreateUserCommand;
import pt.psoft.g1.psoftg1.CreateUserReply;
import pt.psoft.g1.psoftg1.DeleteUserCommand;

@Component
public class IdentityRpcClient {

  private final RabbitTemplate rabbitTemplate;
  private final String exchange;
  private final String createRoutingKey;
  private final String deleteRoutingKey;

  public IdentityRpcClient(RabbitTemplate rabbitTemplate,
      @Value("${identity.exchange}") String exchange,
      @Value("${identity.create-user.routing-key}") String createRoutingKey,
      @Value("${identity.delete-user.routing-key}") String deleteRoutingKey) {
    this.rabbitTemplate = rabbitTemplate;
    this.exchange = exchange;
    this.createRoutingKey = createRoutingKey;
    this.deleteRoutingKey = deleteRoutingKey;
  }

  public CreateUserReply createUser(CreateUserCommand cmd) {
    Object raw = rabbitTemplate.convertSendAndReceive(exchange, createRoutingKey, cmd);
    if (raw == null) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Identity RPC timeout");
    }
    return (CreateUserReply) raw;
  }

  public void deleteUser(DeleteUserCommand cmd) {
    rabbitTemplate.convertAndSend(exchange, deleteRoutingKey, cmd);
  }
}
