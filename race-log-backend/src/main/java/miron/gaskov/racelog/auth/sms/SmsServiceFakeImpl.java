package miron.gaskov.racelog.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceFakeImpl implements SmsService {
    @Override
    public void sendCode(String phone, String code) {
        log.info("FAKE SMS to {}: code {}", phone, code);
    }
}
