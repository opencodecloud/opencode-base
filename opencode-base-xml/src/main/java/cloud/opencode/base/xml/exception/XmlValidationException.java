package cloud.opencode.base.xml.exception;

import java.util.List;

/**
 * XML Validation Exception - Thrown when XML validation fails
 * XML 验证异常 - 当 XML 验证失败时抛出
 *
 * <p>This exception is thrown when XML fails Schema or DTD validation.
 * It contains the list of validation errors encountered.</p>
 * <p>当 XML 未通过 Schema 或 DTD 验证时抛出此异常。它包含遇到的验证错误列表。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for XML schema/DTD validation failures - XML Schema/DTD 验证失败的异常</li>
 *   <li>Extends OpenXmlException with specific context - 继承 OpenXmlException，带特定上下文</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     // XML operation
 * } catch (XmlValidationException e) {
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
public class XmlValidationException extends OpenXmlException {

    private final List<String> errors;

    /**
     * Constructs a validation exception with message.
     * 构造带消息的验证异常。
     *
     * @param message the detail message | 详细消息
     */
    public XmlValidationException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    /**
     * Constructs a validation exception with error list.
     * 构造带错误列表的验证异常。
     *
     * @param errors the validation errors | 验证错误列表
     */
    public XmlValidationException(List<String> errors) {
        super("XML validation failed with " + errors.size() + " error(s): " +
              (errors.isEmpty() ? "" : errors.getFirst()));
        this.errors = List.copyOf(errors);
    }

    /**
     * Constructs a validation exception with message and error list.
     * 构造带消息和错误列表的验证异常。
     *
     * @param message the detail message | 详细消息
     * @param errors  the validation errors | 验证错误列表
     */
    public XmlValidationException(String message, List<String> errors) {
        super(message + ": " + errors.size() + " error(s)");
        this.errors = List.copyOf(errors);
    }

    /**
     * Constructs a validation exception with message and cause.
     * 构造带消息和原因的验证异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public XmlValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errors = List.of(message);
    }

    /**
     * Returns the list of validation errors.
     * 返回验证错误列表。
     *
     * @return the validation errors | 验证错误列表
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Returns whether there are any errors.
     * 返回是否有任何错误。
     *
     * @return true if there are errors | 如果有错误则返回 true
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
