package cloud.opencode.base.yml.exception;

import java.io.Serial;

/**
 * YAML Bind Exception - Thrown when configuration binding fails
 * YAML 绑定异常 - 当配置绑定失败时抛出
 *
 * <p>This exception is thrown when YAML content cannot be bound to a Java object.</p>
 * <p>当 YAML 内容无法绑定到 Java 对象时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Tracks property path and target type for diagnostic info - 跟踪属性路径和目标类型以提供诊断信息</li>
 *   <li>Supports required field validation errors - 支持必填字段验证错误</li>
 *   <li>Error code YML_BIND_001 - 错误码 YML_BIND_001</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     YmlBinder.bind(document, ServerConfig.class);
 * } catch (YmlBindException e) {
 *     System.err.println("Path: " + e.getPath());
 *     System.err.println("Type: " + e.getTargetType());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: Yes (path and targetType may be null) - 空值安全: 是（路径和目标类型可为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public class YmlBindException extends OpenYmlException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Default error code for bind exceptions.
     * 绑定异常的默认错误码。
     */
    private static final String ERROR_CODE = "YML_BIND_001";

    private final String path;
    private final Class<?> targetType;

    /**
     * Constructs a bind exception with message.
     * 构造带消息的绑定异常。
     *
     * @param message the detail message | 详细消息
     */
    public YmlBindException(String message) {
        super(ERROR_CODE, message);
        this.path = null;
        this.targetType = null;
    }

    /**
     * Constructs a bind exception with path, target type and cause.
     * 构造带路径、目标类型和原因的绑定异常。
     *
     * @param path       the property path | 属性路径
     * @param targetType the target type | 目标类型
     * @param cause      the cause | 原因
     */
    public YmlBindException(String path, Class<?> targetType, Throwable cause) {
        super(ERROR_CODE, String.format("Failed to bind '%s' to %s", path, targetType.getName()), cause);
        this.path = path;
        this.targetType = targetType;
    }

    /**
     * Constructs a bind exception with message and cause.
     * 构造带消息和原因的绑定异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public YmlBindException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
        this.path = null;
        this.targetType = null;
    }

    /**
     * Constructs a bind exception for required field.
     * 为必填字段构造绑定异常。
     *
     * @param path       the property path | 属性路径
     * @param targetType the target type | 目标类型
     */
    public YmlBindException(String path, Class<?> targetType) {
        super(ERROR_CODE, String.format("Required property '%s' is missing for type %s", path, targetType.getName()));
        this.path = path;
        this.targetType = targetType;
    }

    /**
     * Gets the property path that failed to bind.
     * 获取绑定失败的属性路径。
     *
     * @return the property path | 属性路径
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the target type for binding.
     * 获取绑定的目标类型。
     *
     * @return the target type | 目标类型
     */
    public Class<?> getTargetType() {
        return targetType;
    }
}
