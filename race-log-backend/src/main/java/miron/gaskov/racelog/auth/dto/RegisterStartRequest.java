package miron.gaskov.racelog.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterStartRequest(
        @NotBlank(message = "Телефон обязателен")
        @Pattern(regexp = "\\+7\\d{10}", message = "Телефон должен быть в формате +7XXXXXXXXXX")
        String phone,

        @NotBlank(message = "Логин обязателен")
        @Size(min = 3, max = 64, message = "Логин должен быть от 3 до 64 символов")
        String login,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 8, max = 64, message = "Пароль должен быть от 8 до 64 символов")
        String password,

        @NotBlank(message = "Роль обязательна")
        String role
) {
}
