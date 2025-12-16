package miron.gaskov.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miron.gaskov.auth.dto.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register/start")
    public RegisterStartResponse registerStart(@Valid @RequestBody RegisterStartRequest request) {
        String code = authService.startRegistration(request);
        return new RegisterStartResponse(request.phone(), code);
    }

    @PostMapping("/register/confirm")
    public LoginResponse registerConfirm(
            @Valid @RequestBody RegisterConfirmRequest confirm,
            @RequestParam String login,
            @RequestParam String password,
            @RequestParam String role
    ) {
        RegisterStartRequest original = new RegisterStartRequest(
                confirm.phone(), login, password, role
        );
        return authService.confirmRegistration(confirm, original);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public String refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.refresh(request);
    }
}
