package cloud.opencode.base.yml.exception;

import java.io.Serial;

/**
 * YAML Placeholder Exception - Thrown when placeholder resolution fails
 * YAML 占位符异常 - 当占位符解析失败时抛出
 *
 * <p>This exception is thrown when a placeholder cannot be resolved.</p>
 * <p>当占位符无法解析时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Tracks the unresolved placeholder expression - 跟踪未解析的占位符表达式</li>
 *   <li>Circular reference detection - 循环引用检测</li>
 *   <li>Error code YML_PLACEHOLDER_001 - 错误码 YML_PLACEHOLDER_001</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     resolver.resolve("${MISSING_KEY}");
 * } catch (YmlPlaceholderException e) {
 *     System.err.println("Unresolved: " + e.getPlaceholder());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (placeholder may be null) - 空值安全: 否（占位符可能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public class YmlPlaceholderException extends OpenYmlException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Default error code for placeholder exceptions.
     * 占位符异常的默认错误码。
     */
    private static final String ERROR_CODE = "YML_PLACEHOLDER_001";

    private final String placeholder;

    /**
     * Constructs a placeholder exception for unresolved placeholder.
     * 为未解析的占位符构造异常。
     *
     * @param placeholder the unresolved placeholder | 未解析的占位符
     */
    public YmlPlaceholderException(String placeholder) {
        super(ERROR_CODE, "Cannot resolve placeholder: " + placeholder);
        this.placeholder = placeholder;
    }

    /**
     * Constructs a placeholder exception with message.
     * 构造带消息的占位符异常。
     *
     * @param placeholder the placeholder | 占位符
     * @param message     the detail message | 详细消息
     */
    public YmlPlaceholderException(String placeholder, String message) {
        super(ERROR_CODE, message);
        this.placeholder = placeholder;
    }

    /**
     * Constructs a placeholder exception with message and cause.
     * 构造带消息和原因的占位符异常。
     *
     * @param placeholder the placeholder | 占位符
     * @param message     the detail message | 详细消息
     * @param cause       the cause | 原因
     */
    public YmlPlaceholderException(String placeholder, String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
        this.placeholder = placeholder;
    }

    /**
     * Gets the placeholder that caused the exception.
     * 获取导致异常的占位符。
     *
     * @return the placeholder | 占位符
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * Creates an exception for circular reference.
     * 为循环引用创建异常。
     *
     * @param placeholder the placeholder | 占位符
     * @return the exception | 异常
     */
    public static YmlPlaceholderException circularReference(String placeholder) {
        return new YmlPlaceholderException(placeholder,
            "Circular reference detected in placeholder: " + placeholder);
    }
}
