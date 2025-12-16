package miron.gaskov.user;

import lombok.RequiredArgsConstructor;
import miron.gaskov.common.NotFoundException;
import miron.gaskov.user.dto.UserMeResponse;
import miron.gaskov.user.dto.UserProfileResponse;
import miron.gaskov.user.dto.UserSearchItem;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserMeResponse me(@AuthenticationPrincipal User current) {
        return new UserMeResponse(
                current.getId(),
                current.getPhone(),
                current.getLogin(),
                current.getRole().name()
        );
    }

    @GetMapping("/{id}")
    public UserProfileResponse getById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return new UserProfileResponse(
                user.getId(),
                user.getLogin(),
                user.getRole().name(),
                user.getInfo()
        );
    }

    @GetMapping("/search")
    public List<UserSearchItem> search(@RequestParam String query) {
        return userRepository
                .findByRoleAndLoginContainingIgnoreCase(UserRole.ATHLETE, query)
                .stream()
                .map(u -> new UserSearchItem(u.getId(), u.getLogin(), u.getInfo()))
                .toList();
    }
}
