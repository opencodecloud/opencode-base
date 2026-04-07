package cloud.opencode.base.captcha.exception;

import cloud.opencode.base.core.exception.OpenException;

/**
 * Captcha Exception - Base exception for CAPTCHA operations
 * 验证码异常 - 验证码操作的基础异常
 *
 * <p>This is the base exception class for all CAPTCHA-related exceptions.
 * It extends {@link OpenException} to integrate with the unified OpenCode
 * exception hierarchy, providing error code and component name support.</p>
 * <p>这是所有验证码相关异常的基础异常类。
 * 继承 {@link OpenException} 以融入 OpenCode 统一异常体系，支持错误码和组件名称。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base exception for CAPTCHA exception hierarchy - 验证码异常层次的基础异常</li>
 *   <li>Extends OpenException with component="Captcha" - 继承 OpenException，组件名="Captcha"</li>
 *   <li>Supports message, cause, errorCode, and combined constructors - 支持消息、原因、错误码和组合构造器</li>
 *   <li>Formatted getMessage(): [Captcha] (errorCode) message - 格式化消息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new CaptchaException("CAPTCHA operation failed");
 * throw new CaptchaException("CAPTCHA operation failed", cause);
 *
 * // Catchable as OpenException
 * try { ... } catch (OpenException e) { ... }   // catches all OpenCode exceptions
 * try { ... } catch (CaptchaException e) { ... } // catches only captcha exceptions
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
public class CaptchaException extends OpenException {

    private static final String COMPONENT = "Captcha";

    /**
     * Constructs a new exception with the specified message.
     * 使用指定消息构造新异常。
     *
     * @param message the detail message | 详细消息
     */
    public CaptchaException(String message) {
        super(COMPONENT, null, message);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     * 使用指定消息和原因构造新异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public CaptchaException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     * 使用指定原因构造新异常。
     *
     * @param cause the cause | 原因
     */
    public CaptchaException(Throwable cause) {
        super(COMPONENT, null, cause != null ? cause.getMessage() : null, cause);
    }

    /**
     * Constructs a new exception with message and error code.
     * 使用消息和错误码构造新异常。
     *
     * @param errorCode the error code | 错误码
     * @param message   the detail message | 详细消息
     */
    public CaptchaException(String errorCode, String message) {
        super(COMPONENT, errorCode, message);
    }

    /**
     * Constructs a new exception with message, error code, and cause.
     * 使用消息、错误码和原因构造新异常。
     *
     * @param errorCode the error code | 错误码
     * @param message   the detail message | 详细消息
     * @param cause     the cause | 原因
     */
    public CaptchaException(String errorCode, String message, Throwable cause) {
        super(COMPONENT, errorCode, message, cause);
    }
}
