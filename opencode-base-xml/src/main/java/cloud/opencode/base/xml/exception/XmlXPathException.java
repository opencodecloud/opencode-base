package cloud.opencode.base.xml.exception;

import java.io.Serial;

/**
 * XPath Exception - Thrown when XPath evaluation fails
 * XPath 异常 - 当 XPath 求值失败时抛出
 *
 * <p>This exception is thrown when an XPath expression is invalid
 * or cannot be evaluated against the given XML document.</p>
 * <p>当 XPath 表达式无效或无法针对给定的 XML 文档求值时抛出此异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for XPath evaluation failures - XPath 求值失败的异常</li>
 *   <li>Extends OpenXmlException with specific context - 继承 OpenXmlException，带特定上下文</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     // XML operation
 * } catch (XmlXPathException e) {
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
public class XmlXPathException extends OpenXmlException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String xpath;

    /**
     * Constructs an XPath exception with expression and cause.
     * 构造带表达式和原因的 XPath 异常。
     *
     * @param xpath the XPath expression | XPath 表达式
     * @param cause the cause | 原因
     */
    public XmlXPathException(String xpath, Throwable cause) {
        super("Invalid XPath expression: " + xpath, cause);
        this.xpath = xpath;
    }

    /**
     * Constructs an XPath exception with expression and message.
     * 构造带表达式和消息的 XPath 异常。
     *
     * @param xpath   the XPath expression | XPath 表达式
     * @param message the detail message | 详细消息
     */
    public XmlXPathException(String xpath, String message) {
        super("XPath error [" + xpath + "]: " + message);
        this.xpath = xpath;
    }

    /**
     * Returns the XPath expression that caused the error.
     * 返回导致错误的 XPath 表达式。
     *
     * @return the XPath expression | XPath 表达式
     */
    public String getXPath() {
        return xpath;
    }
}
