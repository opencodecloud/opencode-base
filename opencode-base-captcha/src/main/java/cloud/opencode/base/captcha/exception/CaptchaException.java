package cloud.opencode.base.captcha.exception;

/**
 * Captcha Exception - Base exception for CAPTCHA operations
 * 验证码异常 - 验证码操作的基础异常
 *
 * <p>This is the base exception class for all CAPTCHA-related exceptions.</p>
 * <p>这是所有验证码相关异常的基础异常类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base exception for CAPTCHA exception hierarchy - 验证码异常层次的基础异常</li>
 *   <li>Extends RuntimeException for unchecked usage - 继承 RuntimeException 用于非受检使用</li>
 *   <li>Supports message, cause, and combined constructors - 支持消息、原因和组合构造器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new CaptchaException("CAPTCHA operation failed");
 * throw new CaptchaException("CAPTCHA operation failed", cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (message may be null) - 空值安全: 否（消息可能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public class CaptchaException extends RuntimeException {

    /**
     * Constructs a new exception with the specified message.
     * 使用指定消息构造新异常。
     *
     * @param message the detail message | 详细消息
     */
    public CaptchaException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     * 使用指定消息和原因构造新异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public CaptchaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     * 使用指定原因构造新异常。
     *
     * @param cause the cause | 原因
     */
    public CaptchaException(Throwable cause) {
        super(cause);
    }
}
