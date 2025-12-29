package miron.gaskov.racelog.auth.dto;

public record RegisterStartResponse(
        String phone,
        String code
) {
}
