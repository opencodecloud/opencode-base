package cloud.opencode.base.captcha.exception;

import cloud.opencode.base.captcha.CaptchaType;

/**
 * Captcha Generation Exception - Thrown when CAPTCHA generation fails
 * 验证码生成异常 - 当验证码生成失败时抛出
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries the CAPTCHA type that failed to generate - 携带生成失败的验证码类型</li>
 *   <li>Extends CaptchaException - 继承 CaptchaException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new CaptchaGenerationException("Failed to render image");
 * throw new CaptchaGenerationException("Failed", CaptchaType.GIF, cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (type may be null) - 空值安全: 否（类型可能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public class CaptchaGenerationException extends CaptchaException {

    private final CaptchaType type;

    /**
     * Constructs a new exception with the specified message.
     * 使用指定消息构造新异常。
     *
     * @param message the detail message | 详细消息
     */
    public CaptchaGenerationException(String message) {
        super(message);
        this.type = null;
    }

    /**
     * Constructs a new exception with message and CAPTCHA type.
     * 使用消息和验证码类型构造新异常。
     *
     * @param message the detail message | 详细消息
     * @param type    the CAPTCHA type | 验证码类型
     */
    public CaptchaGenerationException(String message, CaptchaType type) {
        super(message + " (type: " + type + ")");
        this.type = type;
    }

    /**
     * Constructs a new exception with message and cause.
     * 使用消息和原因构造新异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public CaptchaGenerationException(String message, Throwable cause) {
        super(message, cause);
        this.type = null;
    }

    /**
     * Constructs a new exception with message, type and cause.
     * 使用消息、类型和原因构造新异常。
     *
     * @param message the detail message | 详细消息
     * @param type    the CAPTCHA type | 验证码类型
     * @param cause   the cause | 原因
     */
    public CaptchaGenerationException(String message, CaptchaType type, Throwable cause) {
        super(message + " (type: " + type + ")", cause);
        this.type = type;
    }

    /**
     * Gets the CAPTCHA type.
     * 获取验证码类型。
     *
     * @return the CAPTCHA type | 验证码类型
     */
    public CaptchaType getType() {
        return type;
    }
}
