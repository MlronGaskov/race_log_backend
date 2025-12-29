package miron.gaskov.racelog.result;

import lombok.RequiredArgsConstructor;
import miron.gaskov.racelog.common.ForbiddenException;
import miron.gaskov.racelog.common.NotFoundException;
import miron.gaskov.racelog.discipline.Discipline;
import miron.gaskov.racelog.discipline.DisciplineRepository;
import miron.gaskov.racelog.result.dto.ResultDtos;
import miron.gaskov.racelog.user.User;
import miron.gaskov.racelog.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final DisciplineRepository disciplineRepository;

    @Transactional(readOnly = true)
    public List<ResultDtos.ResultDto> getResults(Long athleteId, Integer disciplineId) {
        List<Result> list = (disciplineId == null)
                ? resultRepository.findByAthleteIdOrderByDateDesc(athleteId)
                : resultRepository.findByAthleteIdAndDisciplineIdOrderByDateDesc(athleteId, disciplineId);

        return list.stream()
                .map(r -> new ResultDtos.ResultDto(
                        r.getId(),
                        r.getDiscipline().getId(),
                        r.getDiscipline().getName(),
                        r.getResultValue(),
                        r.getCompetitionName(),
                        r.getPlace(),
                        r.getDate().toString(),
                        r.getInfo()
                ))
                .toList();
    }

    @Transactional
    public ResultDtos.ResultCreatedDto addResult(User current, Long athleteId, ResultDtos.ResultCreateRequest request) {
        if (current.getRole() != UserRole.ATHLETE) {
            throw new ForbiddenException("Только спортсмен может добавлять результаты");
        }
        if (!current.getId().equals(athleteId)) {
            throw new ForbiddenException("Нельзя добавлять результат за другого спортсмена");
        }

        Discipline discipline = disciplineRepository.findById(request.disciplineId())
                .orElseThrow(() -> new NotFoundException("Дисциплина не найдена"));

        Double numeric = ResultTimeParser.parseToSeconds(request.resultValue());
        if (numeric == null) {
            throw new IllegalArgumentException(
                    "Неверный формат результата. Примеры: 59.87, 4:12.35, 1:02:15.3"
            );
        }

        Result result = Result.builder()
                .athlete(current)
                .discipline(discipline)
                .resultValue(request.resultValue())
                .resultNumeric(numeric)
                .competitionName(request.competitionName())
                .place(request.place())
                .date(LocalDate.parse(request.date()))
                .info(request.info())
                .build();

        Result saved = resultRepository.save(result);
        return new ResultDtos.ResultCreatedDto(saved.getId(), discipline.getId(), saved.getDate().toString());
    }
}
