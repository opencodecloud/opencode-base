package cloud.opencode.base.sms.exception;

import cloud.opencode.base.sms.validation.PhoneValidator;

import java.time.Duration;

/**
 * SMS Rate Limit Exception
 * 短信频率限制异常
 *
 * <p>Exception thrown when SMS rate limit is exceeded.</p>
 * <p>超出短信频率限制时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Phone number and retry-after duration tracking - 手机号和重试等待时间跟踪</li>
 *   <li>Masked phone number for safe logging - 脱敏手机号用于安全日志</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new SmsRateLimitException("13800138000", Duration.ofMinutes(1));
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
public class SmsRateLimitException extends SmsException {

    private final String phone;
    private final Duration retryAfter;

    /**
     * Create rate limit exception
     * 创建频率限制异常
     *
     * @param phone the phone number | 手机号
     * @param retryAfter the time to retry after | 重试等待时间
     */
    public SmsRateLimitException(String phone, Duration retryAfter) {
        super(SmsErrorCode.SEND_RATE_LIMITED,
            "Rate limit exceeded for: " + PhoneValidator.mask(phone));
        this.phone = phone;
        this.retryAfter = retryAfter;
    }

    /**
     * Get phone number
     * 获取手机号
     *
     * @return the phone number | 手机号
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Get masked phone number
     * 获取脱敏后的手机号
     *
     * @return the masked phone | 脱敏后的手机号
     */
    public String getMaskedPhone() {
        return PhoneValidator.mask(phone);
    }

    /**
     * Get retry after duration
     * 获取重试等待时间
     *
     * @return the duration | 时长
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }

    /**
     * Get retry after in seconds
     * 获取重试等待秒数
     *
     * @return the seconds | 秒数
     */
    public long getRetryAfterSeconds() {
        return retryAfter.toSeconds();
    }
}
