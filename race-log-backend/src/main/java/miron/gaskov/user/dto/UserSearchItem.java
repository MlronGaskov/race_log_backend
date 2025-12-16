package miron.gaskov.user.dto;

public record UserSearchItem(
        Long id,
        String login,
        String club
) {}
