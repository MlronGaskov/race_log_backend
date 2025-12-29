package miron.gaskov.racelog.auth;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import miron.gaskov.racelog.auth.dto.*;
import miron.gaskov.racelog.auth.jwt.JwtService;
import miron.gaskov.racelog.auth.sms.SmsCode;
import miron.gaskov.racelog.auth.sms.SmsCodeRepository;
import miron.gaskov.racelog.auth.sms.SmsService;
import miron.gaskov.racelog.common.ForbiddenException;
import miron.gaskov.racelog.common.NotFoundException;
import miron.gaskov.racelog.user.User;
import miron.gaskov.racelog.user.UserRepository;
import miron.gaskov.racelog.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final SmsCodeRepository smsCodeRepository;
    private final SmsService smsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public String startRegistration(RegisterStartRequest request) {
        if (userRepository.findByPhone(request.phone()).isPresent()) {
            throw new ForbiddenException("Пользователь с таким телефоном уже существует");
        }

        String code = "%04d".formatted(random.nextInt(10_000));

        SmsCode smsCode = SmsCode.builder()
                .phone(request.phone())
                .code(code)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .used(false)
                .build();

        smsCodeRepository.save(smsCode);
        smsService.sendCode(request.phone(), code);

        return code;
    }

    @Transactional
    public LoginResponse confirmRegistration(RegisterConfirmRequest confirm, RegisterStartRequest original) {
        SmsCode smsCode = smsCodeRepository
                .findTopByPhoneAndCodeAndUsedIsFalseAndExpiresAtAfterOrderByIdDesc(
                        confirm.phone(), confirm.code(), Instant.now()
                )
                .orElseThrow(() -> new ForbiddenException("Неверный или просроченный код"));

        smsCode.setUsed(true);

        User user = User.builder()
                .phone(original.phone())
                .login(original.login())
                .passwordHash(passwordEncoder.encode(original.password()))
                .role(UserRole.valueOf(original.role()))
                .build();

        userRepository.save(user);

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        return new LoginResponse(access, refresh, user.getId(), user.getLogin(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLogin(request.login())
                .or(() -> userRepository.findByPhone(request.login()))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ForbiddenException("Неверный пароль");
        }

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return new LoginResponse(access, refresh, user.getId(), user.getLogin(), user.getRole().name());
    }

    public String refresh(TokenRefreshRequest request) {
        Claims claims = jwtService.parseToken(request.refreshToken());
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new ForbiddenException("Некорректный refresh-токен");
        }
        Long userId = Long.parseLong(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return jwtService.generateAccessToken(user);
    }
}
