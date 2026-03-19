package cloud.opencode.base.captcha.exception;

/**
 * Captcha Not Found Exception - Thrown when CAPTCHA is not found
 * 验证码未找到异常 - 当验证码未找到时抛出
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries the not-found CAPTCHA ID - 携带未找到的验证码 ID</li>
 *   <li>Extends CaptchaException - 继承 CaptchaException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new CaptchaNotFoundException(captchaId);
 * throw new CaptchaNotFoundException("Custom message", captchaId);
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
public class CaptchaNotFoundException extends CaptchaException {

    private final String captchaId;

    /**
     * Constructs a new exception with the captcha ID.
     * 使用验证码 ID 构造新异常。
     *
     * @param captchaId the not found CAPTCHA ID | 未找到的验证码 ID
     */
    public CaptchaNotFoundException(String captchaId) {
        super("CAPTCHA not found: " + captchaId);
        this.captchaId = captchaId;
    }

    /**
     * Constructs a new exception with message and captcha ID.
     * 使用消息和验证码 ID 构造新异常。
     *
     * @param message   the detail message | 详细消息
     * @param captchaId the not found CAPTCHA ID | 未找到的验证码 ID
     */
    public CaptchaNotFoundException(String message, String captchaId) {
        super(message);
        this.captchaId = captchaId;
    }

    /**
     * Gets the not found CAPTCHA ID.
     * 获取未找到的验证码 ID。
     *
     * @return the captcha ID | 验证码 ID
     */
    public String getCaptchaId() {
        return captchaId;
    }
}
