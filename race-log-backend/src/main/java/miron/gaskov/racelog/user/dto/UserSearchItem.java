package miron.gaskov.racelog.user.dto;

public record UserSearchItem(
        Long id,
        String login,
        String club
) {}
