package cloud.opencode.base.yml.exception;

/**
 * YAML Parse Exception - Thrown when YAML parsing fails
 * YAML 解析异常 - 当 YAML 解析失败时抛出
 *
 * <p>This exception is thrown when the YAML content has syntax errors or format issues.</p>
 * <p>当 YAML 内容存在语法错误或格式问题时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Inherits line/column location from OpenYmlException - 从 OpenYmlException 继承行/列位置</li>
 *   <li>Wraps underlying parser exceptions - 包装底层解析器异常</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     OpenYml.parse(invalidYaml);
 * } catch (YmlParseException e) {
 *     System.err.println("Parse error: " + e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (message may be null) - 空值安全: 否（消息可能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public class YmlParseException extends OpenYmlException {

    /**
     * Constructs a parse exception with message.
     * 构造带消息的解析异常。
     *
     * @param message the detail message | 详细消息
     */
    public YmlParseException(String message) {
        super(message);
    }

    /**
     * Constructs a parse exception with message and location.
     * 构造带消息和位置的解析异常。
     *
     * @param message the detail message | 详细消息
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     */
    public YmlParseException(String message, int line, int column) {
        super(message, line, column);
    }

    /**
     * Constructs a parse exception with message and cause.
     * 构造带消息和原因的解析异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public YmlParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a parse exception with message, cause and location.
     * 构造带消息、原因和位置的解析异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     */
    public YmlParseException(String message, Throwable cause, int line, int column) {
        super(message, cause, line, column);
    }
}
