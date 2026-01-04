package pt.psoft.g1.psoftg1;

public record CreateUserCommand(
    String username,
    String password,
    String fullName,
    String role
) {}
