package cloud.opencode.base.i18n.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * General i18n exception for the OpenCode i18n module
 * OpenCode 国际化模块通用异常
 *
 * <p>Thrown when internationalization operations fail, including message formatting
 * errors, pattern parsing failures, and plural rule evaluation issues.</p>
 * <p>当国际化操作失败时抛出，包括消息格式化错误、模式解析失败和复数规则评估问题。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Format error reporting - 格式化错误报告</li>
 *   <li>Parse error reporting - 解析错误报告</li>
 *   <li>Error code support - 错误码支持</li>
 *   <li>Component identification - 组件标识</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new OpenI18nException("Invalid plural pattern");
 * throw OpenI18nException.formatError("{0, plural, ...}", cause);
 * throw OpenI18nException.parseError("{bad pattern}", "Unclosed brace at position 5");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable exception) - 线程安全: 是（不可变异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public class OpenI18nException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "i18n";

    // ==================== 构造方法 ====================

    /**
     * Creates an exception with a message
     * 使用消息创建异常
     *
     * @param message the error message | 错误消息
     */
    public OpenI18nException(String message) {
        super(COMPONENT, null, message, null);
    }

    /**
     * Creates an exception with a message and cause
     * 使用消息和原因创建异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public OpenI18nException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
    }

    /**
     * Creates an exception with an error code and message
     * 使用错误码和消息创建异常
     *
     * @param errorCode the error code | 错误码
     * @param message   the error message | 错误消息
     */
    public OpenI18nException(String errorCode, String message) {
        super(COMPONENT, errorCode, message, null);
    }

    /**
     * Creates an exception with an error code, message and cause
     * 使用错误码、消息和原因创建异常
     *
     * @param errorCode the error code | 错误码
     * @param message   the error message | 错误消息
     * @param cause     the cause | 原因
     */
    public OpenI18nException(String errorCode, String message, Throwable cause) {
        super(COMPONENT, errorCode, message, cause);
    }

    // ==================== 工厂方法 ====================

    /**
     * Creates an exception for a formatting error
     * 为格式化错误创建异常
     *
     * @param template the message template that failed | 失败的消息模板
     * @param cause    the cause | 原因
     * @return the exception | 异常
     */
    public static OpenI18nException formatError(String template, Throwable cause) {
        return new OpenI18nException("FORMAT_ERROR",
                String.format("Failed to format message template: '%s'", template), cause);
    }

    /**
     * Creates an exception for a pattern parsing error
     * 为模式解析错误创建异常
     *
     * @param pattern the pattern that failed to parse | 解析失败的模式
     * @param detail  detail about the parse failure | 解析失败的详细信息
     * @return the exception | 异常
     */
    public static OpenI18nException parseError(String pattern, String detail) {
        return new OpenI18nException("PARSE_ERROR",
                String.format("Failed to parse pattern '%s': %s", pattern, detail));
    }
}
