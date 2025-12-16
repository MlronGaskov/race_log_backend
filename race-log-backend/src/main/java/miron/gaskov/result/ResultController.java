package miron.gaskov.result;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miron.gaskov.result.dto.ResultDtos;
import miron.gaskov.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService service;

    @GetMapping("/athletes/{athleteId}/results")
    public List<ResultDtos.ResultDto> getResults(
            @PathVariable Long athleteId,
            @RequestParam(required = false) Integer disciplineId
    ) {
        return service.getResults(athleteId, disciplineId);
    }

    @PostMapping("/athletes/{athleteId}/results")
    public ResultDtos.ResultCreatedDto addResult(
            @AuthenticationPrincipal User current,
            @PathVariable Long athleteId,
            @Valid @RequestBody ResultDtos.ResultCreateRequest request
    ) {
        return service.addResult(current, athleteId, request);
    }
}