package cloud.opencode.base.core.exception;

import java.io.Serial;

/**
 * Unsupported Operation Exception - Operation not supported exception
 * 不支持的操作异常 - 操作不支持异常
 *
 * <p>Thrown when an unsupported operation is called. Replaces {@link UnsupportedOperationException}.</p>
 * <p>当调用不支持的操作时抛出此异常。替代 {@link UnsupportedOperationException}，提供统一的异常处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable object check (immutable) - 不可变对象检查</li>
 *   <li>Read-only check (readOnly) - 只读检查</li>
 *   <li>Not implemented check (notImplemented) - 未实现检查</li>
 *   <li>Unsupported type check (unsupportedType) - 不支持类型检查</li>
 *   <li>Generic unsupported (unsupported) - 通用不支持检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @Override
 * public void unsupportedMethod() {
 *     throw new OpenUnsupportedOperationException("This operation is not supported");
 * }
 *
 * // Static factory methods - 静态工厂方法
 * throw OpenUnsupportedOperationException.immutable();
 * throw OpenUnsupportedOperationException.readOnly();
 * throw OpenUnsupportedOperationException.notImplemented("save");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Serializable: Yes - 可序列化: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class OpenUnsupportedOperationException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "Core";
    private static final String ERROR_CODE = "UNSUPPORTED_OPERATION";

    /**
     * Creates
     * 创建不支持操作异常
     *
     * @param message the value | 异常消息
     */
    public OpenUnsupportedOperationException(String message) {
        super(COMPONENT, ERROR_CODE, message);
    }

    /**
     * Creates
     * 创建不支持操作异常（带原因）
     *
     * @param message the value | 异常消息
     * @param cause the value | 原始异常
     */
    public OpenUnsupportedOperationException(String message, Throwable cause) {
        super(COMPONENT, ERROR_CODE, message, cause);
    }

    // ==================== 静态工厂方法 ====================

    /**
     * Creates
     * 创建"不可变对象"异常
     *
     * @return the result | 异常实例
     */
    public static OpenUnsupportedOperationException immutable() {
        return new OpenUnsupportedOperationException("This object is immutable");
    }

    /**
     * Creates
     * 创建"只读"异常
     *
     * @return the result | 异常实例
     */
    public static OpenUnsupportedOperationException readOnly() {
        return new OpenUnsupportedOperationException("This object is read-only");
    }

    /**
     * Creates
     * 创建"未实现"异常
     *
     * @param methodName the value | 方法名
     * @return the result | 异常实例
     */
    public static OpenUnsupportedOperationException notImplemented(String methodName) {
        return new OpenUnsupportedOperationException("Method not implemented: " + methodName);
    }

    /**
     * Creates
     * 创建"不支持的类型"异常
     *
     * @param type the type | 类型
     * @return the result | 异常实例
     */
    public static OpenUnsupportedOperationException unsupportedType(Class<?> type) {
        return new OpenUnsupportedOperationException("Unsupported type: " + type.getName());
    }

    /**
     * Creates
     * 创建"不支持的操作"异常
     *
     * @param operation the value | 操作名
     * @return the result | 异常实例
     */
    public static OpenUnsupportedOperationException unsupported(String operation) {
        return new OpenUnsupportedOperationException("Unsupported operation: " + operation);
    }
}
