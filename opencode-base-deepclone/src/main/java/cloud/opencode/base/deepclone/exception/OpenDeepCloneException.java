package cloud.opencode.base.deepclone.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * Exception for deep clone operations
 * 深度克隆操作异常
 *
 * <p>This exception is thrown when errors occur during object cloning,
 * such as instantiation failures, field access errors, or circular reference issues.</p>
 * <p>当对象克隆过程中发生错误时抛出此异常，如实例化失败、字段访问错误或循环引用问题。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Target type tracking - 目标类型跟踪</li>
 *   <li>Clone path tracking - 克隆路径跟踪</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw OpenDeepCloneException.instantiationFailed(MyClass.class, cause);
 * throw OpenDeepCloneException.maxDepthExceeded(100, "a.b.c.d");
 * throw OpenDeepCloneException.unsupportedType(SomeClass.class);
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public class OpenDeepCloneException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "deepclone";

    /**
     * The type being cloned when the error occurred
     * 发生错误时正在克隆的类型
     */
    private final Class<?> targetType;

    /**
     * The clone path where the error occurred
     * 发生错误的克隆路径
     */
    private final String path;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Creates an exception with a message
     * 使用消息创建异常
     *
     * @param message the error message | 错误消息
     */
    public OpenDeepCloneException(String message) {
        super(COMPONENT, null, message);
        this.targetType = null;
        this.path = null;
    }

    /**
     * Creates an exception with a message and cause
     * 使用消息和原因创建异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public OpenDeepCloneException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.targetType = null;
        this.path = null;
    }

    /**
     * Creates an exception with target type, path, and message
     * 使用目标类型、路径和消息创建异常
     *
     * @param targetType the type being cloned | 正在克隆的类型
     * @param path       the clone path | 克隆路径
     * @param message    the error message | 错误消息
     */
    public OpenDeepCloneException(Class<?> targetType, String path, String message) {
        super(COMPONENT, null, message);
        this.targetType = targetType;
        this.path = path;
    }

    /**
     * Creates an exception with target type, path, message, and cause
     * 使用目标类型、路径、消息和原因创建异常
     *
     * @param targetType the type being cloned | 正在克隆的类型
     * @param path       the clone path | 克隆路径
     * @param message    the error message | 错误消息
     * @param cause      the cause | 原因
     */
    public OpenDeepCloneException(Class<?> targetType, String path, String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.targetType = targetType;
        this.path = path;
    }

    // ==================== Getters | 访问方法 ====================

    /**
     * Gets the target type being cloned
     * 获取正在克隆的目标类型
     *
     * @return the target type, may be null | 目标类型，可能为null
     */
    public Class<?> getTargetType() {
        return targetType;
    }

    /**
     * Gets the clone path where the error occurred
     * 获取发生错误的克隆路径
     *
     * @return the path, may be null | 路径，可能为null
     */
    public String getPath() {
        return path;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates an exception for exceeding maximum clone depth
     * 创建超过最大克隆深度的异常
     *
     * @param depth the depth reached | 达到的深度
     * @param path  the clone path | 克隆路径
     * @return the exception | 异常
     */
    public static OpenDeepCloneException maxDepthExceeded(int depth, String path) {
        return new OpenDeepCloneException(null, path,
                String.format("Maximum clone depth exceeded: %d at path '%s'", depth, path));
    }

    /**
     * Creates an exception for unsupported type
     * 创建不支持类型的异常
     *
     * @param type the unsupported type | 不支持的类型
     * @return the exception | 异常
     */
    public static OpenDeepCloneException unsupportedType(Class<?> type) {
        return new OpenDeepCloneException(type, null,
                String.format("Unsupported type for cloning: %s", type.getName()));
    }

    /**
     * Creates an exception for instantiation failure
     * 创建实例化失败的异常
     *
     * @param type  the type that failed to instantiate | 无法实例化的类型
     * @param cause the cause | 原因
     * @return the exception | 异常
     */
    public static OpenDeepCloneException instantiationFailed(Class<?> type, Throwable cause) {
        return new OpenDeepCloneException(type, null,
                String.format("Failed to instantiate type: %s", type.getName()), cause);
    }

    /**
     * Creates an exception for field access failure
     * 创建字段访问失败的异常
     *
     * @param field the field name | 字段名
     * @param type  the declaring type | 声明类型
     * @param cause the cause | 原因
     * @return the exception | 异常
     */
    public static OpenDeepCloneException fieldAccessFailed(String field, Class<?> type, Throwable cause) {
        return new OpenDeepCloneException(type, field,
                String.format("Failed to access field '%s' in type %s", field, type.getName()), cause);
    }

    /**
     * Creates an exception for serialization failure
     * 创建序列化失败的异常
     *
     * @param type  the type that failed to serialize | 无法序列化的类型
     * @param cause the cause | 原因
     * @return the exception | 异常
     */
    public static OpenDeepCloneException serializationFailed(Class<?> type, Throwable cause) {
        return new OpenDeepCloneException(type, null,
                String.format("Serialization failed for type: %s", type.getName()), cause);
    }

    /**
     * Creates an exception for circular reference detection
     * 创建循环引用检测的异常
     *
     * @param type the type with circular reference | 存在循环引用的类型
     * @param path the clone path | 克隆路径
     * @return the exception | 异常
     */
    public static OpenDeepCloneException circularReference(Class<?> type, String path) {
        return new OpenDeepCloneException(type, path,
                String.format("Circular reference detected at path '%s' for type %s", path, type.getName()));
    }
}
