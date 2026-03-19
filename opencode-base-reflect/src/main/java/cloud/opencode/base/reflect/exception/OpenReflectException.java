package cloud.opencode.base.reflect.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Reflection Operation Exception (Unchecked)
 * 反射操作异常（非受检）
 *
 * <p>Unchecked exception for reflection operations that wraps checked exceptions.
 * Simplifies caller code by eliminating mandatory try-catch blocks.</p>
 * <p>用于反射操作的非受检异常，封装受检异常。
 * 通过消除强制性的try-catch块来简化调用方代码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unchecked exception wrapper - 非受检异常包装</li>
 *   <li>Target type and member tracking - 目标类型和成员追踪</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create with factory method
 * throw OpenReflectException.fieldNotFound(User.class, "name");
 *
 * // Create with details
 * throw new OpenReflectException(User.class, "getName", "invoke", "Method not accessible");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes (null target type is allowed) - 空值安全: 是（允许null目标类型）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class OpenReflectException extends OpenException {

    private static final String COMPONENT = "reflect";

    /**
     * Target type
     * 目标类型
     */
    private final Class<?> targetType;

    /**
     * Member name (field/method/constructor)
     * 成员名称（字段/方法/构造器）
     */
    private final String memberName;

    /**
     * Operation type
     * 操作类型
     */
    private final String operation;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Creates an exception with message
     * 创建带消息的异常
     *
     * @param message the error message | 错误消息
     */
    public OpenReflectException(String message) {
        super(COMPONENT, null, message);
        this.targetType = null;
        this.memberName = null;
        this.operation = null;
    }

    /**
     * Creates an exception with message and cause
     * 创建带消息和原因的异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public OpenReflectException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.targetType = null;
        this.memberName = null;
        this.operation = null;
    }

    /**
     * Creates an exception with target, member, operation and message
     * 创建带目标、成员、操作和消息的异常
     *
     * @param targetType the target type | 目标类型
     * @param memberName the member name | 成员名称
     * @param operation  the operation type | 操作类型
     * @param message    the error message | 错误消息
     */
    public OpenReflectException(Class<?> targetType, String memberName, String operation, String message) {
        super(COMPONENT, null, message);
        this.targetType = targetType;
        this.memberName = memberName;
        this.operation = operation;
    }

    /**
     * Creates an exception with target, member, operation, message and cause
     * 创建带目标、成员、操作、消息和原因的异常
     *
     * @param targetType the target type | 目标类型
     * @param memberName the member name | 成员名称
     * @param operation  the operation type | 操作类型
     * @param message    the error message | 错误消息
     * @param cause      the cause | 原因
     */
    public OpenReflectException(Class<?> targetType, String memberName, String operation, String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.targetType = targetType;
        this.memberName = memberName;
        this.operation = operation;
    }

    // ==================== Accessors | 访问方法 ====================

    /**
     * Gets the target type
     * 获取目标类型
     *
     * @return target type | 目标类型
     */
    public Class<?> targetType() {
        return targetType;
    }

    /**
     * Gets the member name
     * 获取成员名称
     *
     * @return member name | 成员名称
     */
    public String memberName() {
        return memberName;
    }

    /**
     * Gets the operation type
     * 获取操作类型
     *
     * @return operation type | 操作类型
     */
    public String operation() {
        return operation;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a field not found exception
     * 创建字段未找到异常
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @return exception instance | 异常实例
     */
    public static OpenReflectException fieldNotFound(Class<?> clazz, String fieldName) {
        return new OpenReflectException(clazz, fieldName, "getField",
                String.format("Field '%s' not found in class %s", fieldName, clazz.getName()));
    }

    /**
     * Creates a field access failed exception
     * 创建字段访问失败异常
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @param cause     the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenReflectException fieldAccessFailed(Class<?> clazz, String fieldName, Throwable cause) {
        return new OpenReflectException(clazz, fieldName, "accessField",
                String.format("Failed to access field '%s' in class %s", fieldName, clazz.getName()), cause);
    }

    /**
     * Creates a method not found exception
     * 创建方法未找到异常
     *
     * @param clazz      the class | 类
     * @param methodName the method name | 方法名
     * @param paramTypes the parameter types | 参数类型
     * @return exception instance | 异常实例
     */
    public static OpenReflectException methodNotFound(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        String params = paramTypes == null ? "" : Arrays.stream(paramTypes)
                .map(Class::getSimpleName).collect(Collectors.joining(", "));
        return new OpenReflectException(clazz, methodName, "getMethod",
                String.format("Method '%s(%s)' not found in class %s", methodName, params, clazz.getName()));
    }

    /**
     * Creates a method invoke failed exception
     * 创建方法调用失败异常
     *
     * @param clazz      the class | 类
     * @param methodName the method name | 方法名
     * @param cause      the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenReflectException methodInvokeFailed(Class<?> clazz, String methodName, Throwable cause) {
        return new OpenReflectException(clazz, methodName, "invokeMethod",
                String.format("Failed to invoke method '%s' in class %s", methodName, clazz.getName()), cause);
    }

    /**
     * Creates a constructor not found exception
     * 创建构造器未找到异常
     *
     * @param clazz      the class | 类
     * @param paramTypes the parameter types | 参数类型
     * @return exception instance | 异常实例
     */
    public static OpenReflectException constructorNotFound(Class<?> clazz, Class<?>[] paramTypes) {
        String params = paramTypes == null ? "" : Arrays.stream(paramTypes)
                .map(Class::getSimpleName).collect(Collectors.joining(", "));
        return new OpenReflectException(clazz, "<init>", "getConstructor",
                String.format("Constructor(%s) not found in class %s", params, clazz.getName()));
    }

    /**
     * Creates an instantiation failed exception
     * 创建实例化失败异常
     *
     * @param clazz the class | 类
     * @param cause the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenReflectException instantiationFailed(Class<?> clazz, Throwable cause) {
        return new OpenReflectException(clazz, "<init>", "newInstance",
                String.format("Failed to instantiate class %s", clazz.getName()), cause);
    }

    /**
     * Creates a class not found exception
     * 创建类未找到异常
     *
     * @param className the class name | 类名
     * @return exception instance | 异常实例
     */
    public static OpenReflectException classNotFound(String className) {
        return new OpenReflectException(null, null, "forName",
                String.format("Class not found: %s", className));
    }

    /**
     * Creates a class load failed exception
     * 创建类加载失败异常
     *
     * @param className the class name | 类名
     * @param cause     the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenReflectException classLoadFailed(String className, Throwable cause) {
        return new OpenReflectException(null, null, "forName",
                String.format("Failed to load class: %s", className), cause);
    }

    /**
     * Creates a type cast failed exception
     * 创建类型转换失败异常
     *
     * @param targetType the target type | 目标类型
     * @param value      the value | 值
     * @return exception instance | 异常实例
     */
    public static OpenReflectException typeCastFailed(Class<?> targetType, Object value) {
        String valueType = value == null ? "null" : value.getClass().getName();
        return new OpenReflectException(targetType, null, "cast",
                String.format("Cannot cast %s to %s", valueType, targetType.getName()));
    }

    /**
     * Creates a copy failed exception
     * 创建复制失败异常
     *
     * @param sourceType the source type | 源类型
     * @param targetType the target type | 目标类型
     * @param cause      the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenReflectException copyFailed(Class<?> sourceType, Class<?> targetType, Throwable cause) {
        return new OpenReflectException(targetType, null, "copy",
                String.format("Failed to copy from %s to %s", sourceType.getName(), targetType.getName()), cause);
    }

    /**
     * Creates a proxy creation failed exception
     * 创建代理创建失败异常
     *
     * @param interfaceType the interface type | 接口类型
     * @param cause         the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenReflectException proxyCreationFailed(Class<?> interfaceType, Throwable cause) {
        return new OpenReflectException(interfaceType, null, "createProxy",
                String.format("Failed to create proxy for %s", interfaceType.getName()), cause);
    }

    /**
     * Creates an annotation not found exception
     * 创建注解未找到异常
     *
     * @param clazz          the class | 类
     * @param annotationType the annotation type | 注解类型
     * @return exception instance | 异常实例
     */
    public static OpenReflectException annotationNotFound(Class<?> clazz, Class<?> annotationType) {
        return new OpenReflectException(clazz, annotationType.getSimpleName(), "getAnnotation",
                String.format("Annotation @%s not found on %s", annotationType.getSimpleName(), clazz.getName()));
    }

    /**
     * Creates a lambda parse failed exception
     * 创建Lambda解析失败异常
     *
     * @param reason the reason | 原因
     * @return exception instance | 异常实例
     */
    public static OpenReflectException lambdaParseFailed(String reason) {
        return new OpenReflectException(null, null, "parseLambda",
                String.format("Failed to parse lambda: %s", reason));
    }

    /**
     * Creates a record operation failed exception
     * 创建Record操作失败异常
     *
     * @param recordClass the record class | Record类
     * @param operation   the operation | 操作
     * @param cause       the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenReflectException recordOperationFailed(Class<?> recordClass, String operation, Throwable cause) {
        return new OpenReflectException(recordClass, null, operation,
                String.format("Record operation '%s' failed for %s", operation, recordClass.getName()), cause);
    }

    /**
     * Creates an illegal access exception
     * 创建非法访问异常
     *
     * @param clazz      the class | 类
     * @param memberName the member name | 成员名
     * @param cause      the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenReflectException illegalAccess(Class<?> clazz, String memberName, Throwable cause) {
        return new OpenReflectException(clazz, memberName, "access",
                String.format("Illegal access to '%s' in class %s", memberName, clazz.getName()), cause);
    }
}
