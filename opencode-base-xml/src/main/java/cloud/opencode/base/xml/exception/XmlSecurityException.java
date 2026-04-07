package cloud.opencode.base.xml.exception;

import java.io.Serial;

/**
 * XML Security Exception - Thrown when a security violation is detected
 * XML 安全异常 - 当检测到安全违规时抛出
 *
 * <p>This exception is thrown when the parser detects potential security threats,
 * such as XXE (XML External Entity) attacks or entity expansion attacks.</p>
 * <p>当解析器检测到潜在的安全威胁（如 XXE 攻击或实体扩展攻击）时抛出此异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for XML security violations (XXE, entity expansion, etc.) - XML 安全违规异常（XXE、实体扩展等）</li>
 *   <li>Carries SecurityViolationType for programmatic handling - 携带 SecurityViolationType 用于编程式处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch security exception
 * try {
 *     DomParser.parse(untrustedXml);
 * } catch (XmlSecurityException e) {
 *     System.err.println("Security violation: " + e.getType());
 * }
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public class XmlSecurityException extends OpenXmlException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final SecurityViolationType type;

    /**
     * Constructs a security exception with violation type and message.
     * 构造带违规类型和消息的安全异常。
     *
     * @param type    the security violation type | 安全违规类型
     * @param message the detail message | 详细消息
     */
    public XmlSecurityException(SecurityViolationType type, String message) {
        super("XML Security violation [" + type + "]: " + message);
        this.type = type;
    }

    /**
     * Constructs a security exception with violation type, message and cause.
     * 构造带违规类型、消息和原因的安全异常。
     *
     * @param type    the security violation type | 安全违规类型
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public XmlSecurityException(SecurityViolationType type, String message, Throwable cause) {
        super("XML Security violation [" + type + "]: " + message, cause);
        this.type = type;
    }

    /**
     * Returns the type of security violation.
     * 返回安全违规类型。
     *
     * @return the security violation type | 安全违规类型
     */
    public SecurityViolationType getType() {
        return type;
    }

    /**
     * Security Violation Type Enumeration
     * 安全违规类型枚举
     */
    public enum SecurityViolationType {
        /**
         * External entity reference detected (XXE attack)
         * 检测到外部实体引用（XXE 攻击）
         */
        XXE_DETECTED,

        /**
         * Entity expansion limit exceeded (Billion Laughs attack)
         * 实体扩展超限（十亿笑声攻击）
         */
        ENTITY_EXPANSION_LIMIT,

        /**
         * DTD processing is prohibited
         * DTD 处理被禁止
         */
        DTD_PROHIBITED,

        /**
         * External parameter entity reference detected
         * 检测到外部参数实体引用
         */
        EXTERNAL_PARAMETER_ENTITY
    }
}
