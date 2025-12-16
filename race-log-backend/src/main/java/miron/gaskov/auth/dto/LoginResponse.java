package miron.gaskov.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String login,
        String role
) {}
