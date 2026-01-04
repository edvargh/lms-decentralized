package pt.psoft.g1.psoftg1;

public record CreateUserReply(
    boolean success,
    Long userId,
    String errorCode,
    String errorMessage
) {}