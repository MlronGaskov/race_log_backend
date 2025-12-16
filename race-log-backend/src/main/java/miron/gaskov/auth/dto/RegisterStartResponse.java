package miron.gaskov.auth.dto;

public record RegisterStartResponse(
        String phone,
        String code
) {
}
