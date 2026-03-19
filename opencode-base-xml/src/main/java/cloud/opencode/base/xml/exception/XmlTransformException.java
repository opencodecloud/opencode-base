package cloud.opencode.base.xml.exception;

/**
 * XML Transform Exception - Thrown when XSLT transformation fails
 * XML 转换异常 - 当 XSLT 转换失败时抛出
 *
 * <p>This exception is thrown when an XSLT transformation encounters errors,
 * such as invalid XSLT stylesheets or transformation failures.</p>
 * <p>当 XSLT 转换遇到错误（如无效的 XSLT 样式表或转换失败）时抛出此异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for XSLT transformation failures - XSLT 转换失败的异常</li>
 *   <li>Extends OpenXmlException with specific context - 继承 OpenXmlException，带特定上下文</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     // XML operation
 * } catch (XmlTransformException e) {
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
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public class XmlTransformException extends OpenXmlException {

    /**
     * Constructs a transform exception with cause.
     * 构造带原因的转换异常。
     *
     * @param cause the cause | 原因
     */
    public XmlTransformException(Throwable cause) {
        super("XML transformation failed", cause);
    }

    /**
     * Constructs a transform exception with message.
     * 构造带消息的转换异常。
     *
     * @param message the detail message | 详细消息
     */
    public XmlTransformException(String message) {
        super(message);
    }

    /**
     * Constructs a transform exception with message and cause.
     * 构造带消息和原因的转换异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public XmlTransformException(String message, Throwable cause) {
        super(message, cause);
    }
}
