package cloud.opencode.base.xml.exception;

/**
 * XML Bind Exception - Thrown when XML-to-Bean binding fails
 * XML 绑定异常 - 当 XML 到 Bean 绑定失败时抛出
 *
 * <p>This exception is thrown when the binder cannot convert XML to a Java object
 * or vice versa, due to type mismatches, missing annotations, or other issues.</p>
 * <p>当绑定器由于类型不匹配、缺少注解或其他问题而无法将 XML 转换为 Java 对象（或反之）时抛出此异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for XML-to-Bean binding failures - XML 到 Bean 绑定失败的异常</li>
 *   <li>Extends OpenXmlException with specific context - 继承 OpenXmlException，带特定上下文</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     // XML operation
 * } catch (XmlBindException e) {
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
public class XmlBindException extends OpenXmlException {

    private final Class<?> targetType;

    /**
     * Constructs a bind exception with target type and message.
     * 构造带目标类型和消息的绑定异常。
     *
     * @param targetType the target type | 目标类型
     * @param message    the detail message | 详细消息
     */
    public XmlBindException(Class<?> targetType, String message) {
        super("Failed to bind XML to " + targetType.getName() + ": " + message);
        this.targetType = targetType;
    }

    /**
     * Constructs a bind exception with target type, message and cause.
     * 构造带目标类型、消息和原因的绑定异常。
     *
     * @param targetType the target type | 目标类型
     * @param message    the detail message | 详细消息
     * @param cause      the cause | 原因
     */
    public XmlBindException(Class<?> targetType, String message, Throwable cause) {
        super("Failed to bind XML to " + targetType.getName() + ": " + message, cause);
        this.targetType = targetType;
    }

    /**
     * Constructs a bind exception with message only.
     * 构造仅带消息的绑定异常。
     *
     * @param message the detail message | 详细消息
     */
    public XmlBindException(String message) {
        super(message);
        this.targetType = null;
    }

    /**
     * Constructs a bind exception with message and cause.
     * 构造带消息和原因的绑定异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public XmlBindException(String message, Throwable cause) {
        super(message, cause);
        this.targetType = null;
    }

    /**
     * Returns the target type that failed to bind.
     * 返回绑定失败的目标类型。
     *
     * @return the target type, or null if not applicable | 目标类型，如果不适用则返回 null
     */
    public Class<?> getTargetType() {
        return targetType;
    }
}
