package miron.gaskov.racelog.auth.sms;

public interface SmsService {
    void sendCode(String phone, String code);
}
