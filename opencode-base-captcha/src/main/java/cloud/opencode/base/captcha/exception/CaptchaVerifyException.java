package cloud.opencode.base.captcha.exception;

/**
 * Captcha Verify Exception - Thrown when CAPTCHA verification fails
 * 验证码验证异常 - 当验证码验证失败时抛出
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries CAPTCHA ID and provided answer for diagnostics - 携带验证码 ID 和提供的答案用于诊断</li>
 *   <li>Extends CaptchaException - 继承 CaptchaException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new CaptchaVerifyException(captchaId);
 * throw new CaptchaVerifyException(captchaId, providedAnswer);
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
public class CaptchaVerifyException extends CaptchaException {

    private final String captchaId;
    private final String providedAnswer;

    /**
     * Constructs a new exception with the captcha ID.
     * 使用验证码 ID 构造新异常。
     *
     * @param captchaId the CAPTCHA ID | 验证码 ID
     */
    public CaptchaVerifyException(String captchaId) {
        super("CAPTCHA verification failed: " + captchaId);
        this.captchaId = captchaId;
        this.providedAnswer = null;
    }

    /**
     * Constructs a new exception with captcha ID and provided answer.
     * 使用验证码 ID 和提供的答案构造新异常。
     *
     * @param captchaId      the CAPTCHA ID | 验证码 ID
     * @param providedAnswer the provided answer | 提供的答案
     */
    public CaptchaVerifyException(String captchaId, String providedAnswer) {
        super("CAPTCHA verification failed for ID: " + captchaId);
        this.captchaId = captchaId;
        this.providedAnswer = providedAnswer;
    }

    /**
     * Constructs a new exception with message and captcha ID.
     * 使用消息和验证码 ID 构造新异常。
     *
     * @param message   the detail message | 详细消息
     * @param captchaId the CAPTCHA ID | 验证码 ID
     * @param cause     the cause | 原因
     */
    public CaptchaVerifyException(String message, String captchaId, Throwable cause) {
        super(message, cause);
        this.captchaId = captchaId;
        this.providedAnswer = null;
    }

    /**
     * Gets the CAPTCHA ID.
     * 获取验证码 ID。
     *
     * @return the captcha ID | 验证码 ID
     */
    public String getCaptchaId() {
        return captchaId;
    }

    /**
     * Gets the provided answer.
     * 获取提供的答案。
     *
     * @return the provided answer | 提供的答案
     */
    public String getProvidedAnswer() {
        return providedAnswer;
    }
}
