package miron.gaskov.auth.sms;

public interface SmsService {
    void sendCode(String phone, String code);
}
