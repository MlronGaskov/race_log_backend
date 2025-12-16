package miron.gaskov.user.dto;

public record UserProfileResponse(
        Long id,
        String login,
        String role,
        String info
) {}
