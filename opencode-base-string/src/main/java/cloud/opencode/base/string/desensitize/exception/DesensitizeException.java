package cloud.opencode.base.string.desensitize.exception;

import cloud.opencode.base.string.exception.OpenStringException;

/**
 * Desensitization Exception
 * 脱敏异常
 *
 * <p>Exception thrown during data desensitization operations.</p>
 * <p>数据脱敏操作期间抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Desensitization errors - 脱敏错误</li>
 *   <li>Strategy application failures - 策略应用失败</li>
 *   <li>Annotation processing errors - 注解处理错误</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Strategy not found
 * throw new DesensitizeException("Desensitize strategy not found: " + type);
 *
 * // Invalid configuration
 * throw new DesensitizeException("Invalid keep length: " + keep);
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
 * @since JDK 25, opencode-base-string V1.0.0
 */
public class DesensitizeException extends OpenStringException {

    /**
     * Constructs a new desensitize exception with the specified detail message.
     * 使用指定的详细消息构造新脱敏异常。
     *
     * @param message the detail message | 详细消息
     */
    public DesensitizeException(String message) {
        super(message);
    }

    /**
     * Constructs a new desensitize exception with the specified detail message and cause.
     * 使用指定的详细消息和原因构造新脱敏异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public DesensitizeException(String message, Throwable cause) {
        super(message, cause);
    }
}
