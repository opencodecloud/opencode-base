package cloud.opencode.base.sms.exception;

/**
 * SMS Send Exception
 * 短信发送异常
 *
 * <p>Exception thrown when SMS sending fails.</p>
 * <p>短信发送失败时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Phone number and provider code tracking - 手机号和服务商错误码跟踪</li>
 *   <li>Factory methods for common send failures - 常见发送失败的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw SmsSendException.failed("13800138000", cause);
 * throw SmsSendException.timeout("13800138000");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public class SmsSendException extends SmsException {

    private final String phoneNumber;
    private final String providerCode;

    public SmsSendException(SmsErrorCode errorCode, String phoneNumber) {
        super(errorCode);
        this.phoneNumber = phoneNumber;
        this.providerCode = null;
    }

    public SmsSendException(SmsErrorCode errorCode, String phoneNumber, String providerCode) {
        super(errorCode);
        this.phoneNumber = phoneNumber;
        this.providerCode = providerCode;
    }

    public SmsSendException(SmsErrorCode errorCode, String phoneNumber, Throwable cause) {
        super(errorCode, cause);
        this.phoneNumber = phoneNumber;
        this.providerCode = null;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public static SmsSendException failed(String phoneNumber, Throwable cause) {
        return new SmsSendException(SmsErrorCode.SEND_FAILED, phoneNumber, cause);
    }

    public static SmsSendException timeout(String phoneNumber) {
        return new SmsSendException(SmsErrorCode.SEND_TIMEOUT, phoneNumber);
    }

    public static SmsSendException rateLimited(String phoneNumber) {
        return new SmsSendException(SmsErrorCode.SEND_RATE_LIMITED, phoneNumber);
    }
}
