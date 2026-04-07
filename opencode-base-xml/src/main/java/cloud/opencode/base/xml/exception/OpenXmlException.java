package cloud.opencode.base.xml.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * XML Exception Base Class - Base exception for all XML operations
 * XML 异常基类 - 所有 XML 操作的基础异常
 *
 * <p>This exception serves as the base class for all XML-related exceptions in the framework.
 * It provides location information (line and column) for parse errors.</p>
 * <p>此异常作为框架中所有 XML 相关异常的基类，为解析错误提供位置信息（行号和列号）。</p>
 *
 * <p><strong>Exception Hierarchy | 异常继承体系:</strong></p>
 * <pre>
 * OpenException (OpenCode统一异常基类)
 * └── OpenXmlException (XML异常基类)
 *     ├── XmlParseException      # 解析异常
 *     ├── XmlXPathException      # XPath异常
 *     ├── XmlBindException       # 绑定异常
 *     ├── XmlTransformException  # 转换异常
 *     ├── XmlValidationException # 验证异常
 *     └── XmlSecurityException   # 安全异常
 * </pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base exception for all XML operations - 所有 XML 操作的基础异常</li>
 *   <li>Line and column location info for parse errors - 解析错误的行号和列号位置信息</li>
 *   <li>Exception hierarchy root for XML module - XML 模块的异常继承体系根</li>
 *   <li>Inherits error code and component name from OpenException - 继承 OpenException 的错误码和组件名称</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch base XML exception
 * try {
 *     OpenXml.parse(xmlString);
 * } catch (OpenXmlException e) {
 *     System.err.println("XML error: " + e.getMessage());
 * }
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public class OpenXmlException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "XML";

    private final int line;
    private final int column;

    /**
     * Constructs an exception with a message.
     * 构造带消息的异常。
     *
     * @param message the detail message | 详细消息
     */
    public OpenXmlException(String message) {
        super(COMPONENT, null, message, null);
        this.line = -1;
        this.column = -1;
    }

    /**
     * Constructs an exception with a message and cause.
     * 构造带消息和原因的异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public OpenXmlException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.line = -1;
        this.column = -1;
    }

    /**
     * Constructs an exception with a cause.
     * 构造带原因的异常。
     *
     * @param cause the cause | 原因
     */
    public OpenXmlException(Throwable cause) {
        super(COMPONENT, null, cause != null ? cause.getMessage() : null, cause);
        this.line = -1;
        this.column = -1;
    }

    /**
     * Constructs an exception with location information.
     * 构造带位置信息的异常。
     *
     * @param message the detail message | 详细消息
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     */
    public OpenXmlException(String message, int line, int column) {
        super(COMPONENT, null, formatMessage(message, line, column), null);
        this.line = line;
        this.column = column;
    }

    /**
     * Constructs an exception with location information and cause.
     * 构造带位置信息和原因的异常。
     *
     * @param message the detail message | 详细消息
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     * @param cause   the cause | 原因
     */
    public OpenXmlException(String message, int line, int column, Throwable cause) {
        super(COMPONENT, null, formatMessage(message, line, column), cause);
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the line number where the error occurred.
     * 返回发生错误的行号。
     *
     * @return the line number, or -1 if not available | 行号，如果不可用则返回 -1
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the column number where the error occurred.
     * 返回发生错误的列号。
     *
     * @return the column number, or -1 if not available | 列号，如果不可用则返回 -1
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns whether location information is available.
     * 返回位置信息是否可用。
     *
     * @return true if location is available | 如果位置可用则返回 true
     */
    public boolean hasLocation() {
        return line >= 0 && column >= 0;
    }

    /**
     * Creates a parse error exception.
     * 创建解析错误异常。
     *
     * @param message the error message | 错误消息
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     * @return the exception | 异常
     */
    public static OpenXmlException parseError(String message, int line, int column) {
        return new XmlParseException(message, line, column);
    }

    /**
     * Creates an XPath error exception.
     * 创建 XPath 错误异常。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @param cause the cause | 原因
     * @return the exception | 异常
     */
    public static OpenXmlException xpathError(String xpath, Throwable cause) {
        return new XmlXPathException(xpath, cause);
    }

    /**
     * Creates a bind error exception.
     * 创建绑定错误异常。
     *
     * @param type  the target type | 目标类型
     * @param cause the cause | 原因
     * @return the exception | 异常
     */
    public static OpenXmlException bindError(Class<?> type, Throwable cause) {
        return new XmlBindException(type, cause.getMessage(), cause);
    }

    /**
     * Creates a validation error exception.
     * 创建验证错误异常。
     *
     * @param message the error message | 错误消息
     * @return the exception | 异常
     */
    public static OpenXmlException validationError(String message) {
        return new XmlValidationException(message);
    }

    /**
     * Creates a transform error exception.
     * 创建转换错误异常。
     *
     * @param cause the cause | 原因
     * @return the exception | 异常
     */
    public static OpenXmlException transformError(Throwable cause) {
        return new XmlTransformException(cause);
    }

    private static String formatMessage(String message, int line, int column) {
        if (line >= 0 && column >= 0) {
            return String.format("[Line %d, Column %d] %s", line, column, message);
        }
        return message;
    }
}
