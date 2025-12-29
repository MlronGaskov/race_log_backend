package miron.gaskov.racelog.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterConfirmRequest(
        @NotBlank(message = "Телефон обязателен")
        @Pattern(regexp = "\\+7\\d{10}", message = "Телефон должен быть в формате +7XXXXXXXXXX")
        String phone,

        @NotBlank(message = "Введите код")
        String code
) {
}
