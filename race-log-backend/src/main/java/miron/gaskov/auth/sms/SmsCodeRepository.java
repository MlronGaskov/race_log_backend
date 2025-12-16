package miron.gaskov.auth.sms;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface SmsCodeRepository extends JpaRepository<SmsCode, Long> {
    Optional<SmsCode> findTopByPhoneAndCodeAndUsedIsFalseAndExpiresAtAfterOrderByIdDesc(
            String phone, String code, Instant now);
}
