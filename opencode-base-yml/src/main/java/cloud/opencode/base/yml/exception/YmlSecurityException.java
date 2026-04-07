package cloud.opencode.base.yml.exception;

import java.io.Serial;

/**
 * YAML Security Exception - Thrown when security violation is detected
 * YAML 安全异常 - 当检测到安全违规时抛出
 *
 * <p>This exception is thrown when YAML content violates security constraints.</p>
 * <p>当 YAML 内容违反安全约束时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized violation types (alias, depth, size, type, recursion) - 分类违规类型（别名、深度、大小、类型、递归）</li>
 *   <li>Factory methods for common security violations - 常见安全违规的工厂方法</li>
 *   <li>Error code YML_SECURITY_001 - 错误码 YML_SECURITY_001</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     YmlSecurity.validate(yamlContent);
 * } catch (YmlSecurityException e) {
 *     System.err.println("Violation: " + e.getType());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (type must not be null) - 空值安全: 否（类型不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public class YmlSecurityException extends OpenYmlException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Default error code for security exceptions.
     * 安全异常的默认错误码。
     */
    private static final String ERROR_CODE = "YML_SECURITY_001";

    private final SecurityViolationType type;

    /**
     * Constructs a security exception with type and message.
     * 构造带类型和消息的安全异常。
     *
     * @param type    the violation type | 违规类型
     * @param message the detail message | 详细消息
     */
    public YmlSecurityException(SecurityViolationType type, String message) {
        super(ERROR_CODE, "YAML Security violation [" + type + "]: " + message);
        this.type = type;
    }

    /**
     * Constructs a security exception with type, message and cause.
     * 构造带类型、消息和原因的安全异常。
     *
     * @param type    the violation type | 违规类型
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public YmlSecurityException(SecurityViolationType type, String message, Throwable cause) {
        super(ERROR_CODE, "YAML Security violation [" + type + "]: " + message, cause);
        this.type = type;
    }

    /**
     * Gets the security violation type.
     * 获取安全违规类型。
     *
     * @return the violation type | 违规类型
     */
    public SecurityViolationType getType() {
        return type;
    }

    /**
     * Security Violation Type - Types of security violations
     * 安全违规类型 - 安全违规的类型
     */
    public enum SecurityViolationType {
        /**
         * Alias limit exceeded (YAML bomb prevention)
         * 别名数量超限（YAML 炸弹防护）
         */
        ALIAS_LIMIT_EXCEEDED,

        /**
         * Nesting depth exceeded
         * 嵌套深度超限
         */
        NESTING_DEPTH_EXCEEDED,

        /**
         * Document size exceeded
         * 文档大小超限
         */
        DOCUMENT_SIZE_EXCEEDED,

        /**
         * Forbidden type deserialization
         * 禁止的类型反序列化
         */
        FORBIDDEN_TYPE,

        /**
         * Recursive reference detected
         * 检测到循环引用
         */
        RECURSIVE_REFERENCE
    }

    /**
     * Creates an exception for alias limit exceeded.
     * 为别名超限创建异常。
     *
     * @param count the alias count | 别名数量
     * @param limit the alias limit | 别名限制
     * @return the exception | 异常
     */
    public static YmlSecurityException aliasLimitExceeded(int count, int limit) {
        return new YmlSecurityException(SecurityViolationType.ALIAS_LIMIT_EXCEEDED,
            String.format("Alias count %d exceeds limit %d (possible YAML bomb attack)", count, limit));
    }

    /**
     * Creates an exception for nesting depth exceeded.
     * 为嵌套深度超限创建异常。
     *
     * @param depth the nesting depth | 嵌套深度
     * @param limit the depth limit | 深度限制
     * @return the exception | 异常
     */
    public static YmlSecurityException nestingDepthExceeded(int depth, int limit) {
        return new YmlSecurityException(SecurityViolationType.NESTING_DEPTH_EXCEEDED,
            String.format("Nesting depth %d exceeds limit %d", depth, limit));
    }

    /**
     * Creates an exception for document size exceeded.
     * 为文档大小超限创建异常。
     *
     * @param size  the document size | 文档大小
     * @param limit the size limit | 大小限制
     * @return the exception | 异常
     */
    public static YmlSecurityException documentSizeExceeded(long size, long limit) {
        return new YmlSecurityException(SecurityViolationType.DOCUMENT_SIZE_EXCEEDED,
            String.format("Document size %d bytes exceeds limit %d bytes", size, limit));
    }

    /**
     * Creates an exception for forbidden type.
     * 为禁止类型创建异常。
     *
     * @param typeName the forbidden type name | 禁止的类型名称
     * @return the exception | 异常
     */
    public static YmlSecurityException forbiddenType(String typeName) {
        return new YmlSecurityException(SecurityViolationType.FORBIDDEN_TYPE,
            "Deserialization of type '" + typeName + "' is not allowed");
    }

    /**
     * Creates an exception for unsafe type.
     * 为不安全类型创建异常。
     *
     * @param typeName the unsafe type name | 不安全的类型名称
     * @return the exception | 异常
     */
    public static YmlSecurityException unsafeType(String typeName) {
        return new YmlSecurityException(SecurityViolationType.FORBIDDEN_TYPE,
            "Type '" + typeName + "' is not allowed for safe YAML construction");
    }
}
