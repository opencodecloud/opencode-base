package cloud.opencode.base.xml.sax;

import cloud.opencode.base.xml.exception.XmlParseException;

import java.io.Serial;

/**
 * SAX Parse Exception - Exception thrown during SAX parsing
 * SAX 解析异常 - SAX 解析期间抛出的异常
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SAX-specific parse exception with line/column info - 带行/列信息的 SAX 特定解析异常</li>
 *   <li>Wraps org.xml.sax.SAXParseException - 包装 org.xml.sax.SAXParseException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch SAX parse exception
 * try {
 *     SaxParser.createSecure().parse(xml);
 * } catch (SaxParseException e) {
 *     System.err.println("Error at line " + e.getLine());
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
public class SaxParseException extends XmlParseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SaxParseException(String message, int line, int column) {
        super(message, line, column);
    }

    public SaxParseException(String message, int line, int column, Throwable cause) {
        super(message, line, column, cause);
    }

    public SaxParseException(org.xml.sax.SAXParseException e) {
        super(e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e);
    }
}
