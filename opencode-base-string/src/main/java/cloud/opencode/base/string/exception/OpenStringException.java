package cloud.opencode.base.string.exception;

import cloud.opencode.base.core.exception.OpenException;

/**
 * String Component Exception
 * 字符串组件异常
 *
 * <p>Base exception for all string processing operations.</p>
 * <p>字符串处理操作的基础异常类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base exception for string operations - 字符串操作基础异常</li>
 *   <li>Template processing errors - 模板处理错误</li>
 *   <li>Format conversion errors - 格式转换错误</li>
 *   <li>Validation failures - 验证失败</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Template error
 * throw new OpenStringException("Invalid template syntax: " + template);
 *
 * // Format error
 * throw new OpenStringException("Cannot convert to snake_case: " + input);
 *
 * // With cause
 * throw new OpenStringException("Regex compilation failed", cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public class OpenStringException extends OpenException {

    /**
     * Constructs a new exception with the specified detail message.
     * 使用指定的详细消息构造新异常。
     *
     * @param message the detail message | 详细消息
     */
    public OpenStringException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 使用指定的详细消息和原因构造新异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public OpenStringException(String message, Throwable cause) {
        super(message, cause);
    }
}
