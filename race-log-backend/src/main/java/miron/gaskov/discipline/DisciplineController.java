package miron.gaskov.discipline;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DisciplineController {
    private final DisciplineRepository repo;

    @GetMapping("/api/v1/disciplines")
    public List<DisciplineDto> getAll() {
        return repo.findAll().stream()
                .map(d -> new DisciplineDto(d.getId(), d.getCode(), d.getName()))
                .toList();
    }
}
