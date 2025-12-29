package miron.gaskov.racelog.result;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByAthleteIdOrderByDateDesc(Long athleteId);
    List<Result> findByAthleteIdAndDisciplineIdOrderByDateDesc(Long athleteId, Integer disciplineId);
    List<Result> findByAthleteIdInAndDisciplineIdOrderByDateDesc(List<Long> athleteIds, Integer disciplineId);
}