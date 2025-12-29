package miron.gaskov.racelog.user.dto;

public record UserMeResponse(
        Long id,
        String phone,
        String login,
        String role
) {}
