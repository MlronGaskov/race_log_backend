package miron.gaskov.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @NotBlank(message = "Пустой refreshToken") String refreshToken
) {}
