package cloud.opencode.base.xml.exception;

import java.io.Serial;

/**
 * XML Parse Exception - Thrown when XML parsing fails
 * XML 解析异常 - 当 XML 解析失败时抛出
 *
 * <p>This exception is thrown when the XML parser encounters malformed XML,
 * encoding issues, or other parsing errors.</p>
 * <p>当 XML 解析器遇到格式错误的 XML、编码问题或其他解析错误时抛出此异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for XML parsing failures with line/column info - 带行/列信息的 XML 解析失败异常</li>
 *   <li>Extends OpenXmlException with specific context - 继承 OpenXmlException，带特定上下文</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     // XML operation
 * } catch (XmlParseException e) {
 *     System.err.println(e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable exception) - 线程安全: 是（不可变异常）</li>
 *   <li>Null-safe: No (message must not be null) - 空值安全: 否（消息不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public class XmlParseException extends OpenXmlException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a parse exception with message and location.
     * 构造带消息和位置的解析异常。
     *
     * @param message the detail message | 详细消息
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     */
    public XmlParseException(String message, int line, int column) {
        super(message, line, column);
    }

    /**
     * Constructs a parse exception with message, location and cause.
     * 构造带消息、位置和原因的解析异常。
     *
     * @param message the detail message | 详细消息
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     * @param cause   the cause | 原因
     */
    public XmlParseException(String message, int line, int column, Throwable cause) {
        super(message, line, column, cause);
    }

    /**
     * Constructs a parse exception with message only.
     * 构造仅带消息的解析异常。
     *
     * @param message the detail message | 详细消息
     */
    public XmlParseException(String message) {
        super(message);
    }

    /**
     * Constructs a parse exception with message and cause.
     * 构造带消息和原因的解析异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public XmlParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
