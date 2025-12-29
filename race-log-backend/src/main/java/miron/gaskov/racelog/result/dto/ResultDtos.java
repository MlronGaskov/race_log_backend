package miron.gaskov.racelog.result.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResultDtos {
    public record ResultCreateRequest(
            @NotNull(message = "Дисциплина обязательна")
            Integer disciplineId,

            @NotBlank(message = "Дата обязательна")
            String date,

            @NotBlank(message = "Название соревнования обязательно")
            String competitionName,

            String info,

            @NotBlank(message = "Результат обязателен")
            String resultValue,

            Integer place
    ) {}

    public record ResultDto(
            Long id,
            Integer disciplineId,
            String disciplineName,
            String resultValue,
            String competitionName,
            Integer place,
            String date,
            String info
    ) {}

    public record ResultCreatedDto(
            Long id,
            Integer disciplineId,
            String date
    ) {}
}
