package cloud.opencode.base.captcha.exception;

/**
 * Captcha Expired Exception - Thrown when CAPTCHA has expired
 * 验证码过期异常 - 当验证码过期时抛出
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries the expired CAPTCHA ID - 携带过期的验证码 ID</li>
 *   <li>Extends CaptchaException - 继承 CaptchaException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new CaptchaExpiredException(captchaId);
 * throw new CaptchaExpiredException("Custom message", captchaId);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (captchaId may be null) - 空值安全: 否（captchaId 可能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public class CaptchaExpiredException extends CaptchaException {

    private final String captchaId;

    /**
     * Constructs a new exception with the captcha ID.
     * 使用验证码 ID 构造新异常。
     *
     * @param captchaId the expired CAPTCHA ID | 过期的验证码 ID
     */
    public CaptchaExpiredException(String captchaId) {
        super("CAPTCHA has expired: " + captchaId);
        this.captchaId = captchaId;
    }

    /**
     * Constructs a new exception with message and captcha ID.
     * 使用消息和验证码 ID 构造新异常。
     *
     * @param message   the detail message | 详细消息
     * @param captchaId the expired CAPTCHA ID | 过期的验证码 ID
     */
    public CaptchaExpiredException(String message, String captchaId) {
        super(message);
        this.captchaId = captchaId;
    }

    /**
     * Gets the expired CAPTCHA ID.
     * 获取过期的验证码 ID。
     *
     * @return the captcha ID | 验证码 ID
     */
    public String getCaptchaId() {
        return captchaId;
    }
}
