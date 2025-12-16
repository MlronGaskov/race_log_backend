package miron.gaskov.user.dto;

public record UserMeResponse(
        Long id,
        String phone,
        String login,
        String role
) {}
