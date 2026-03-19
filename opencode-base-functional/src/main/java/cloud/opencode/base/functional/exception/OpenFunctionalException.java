package cloud.opencode.base.functional.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * OpenFunctionalException - Base exception for functional operations
 * OpenFunctionalException - 函数式操作的基础异常
 *
 * <p>Base exception for all functional programming related errors including
 * monad failures, computation errors, and pipeline exceptions.</p>
 * <p>所有函数式编程相关错误的基础异常，包括 Monad 失败、计算错误和管道异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Component identification - 组件标识</li>
 *   <li>Error code support - 错误码支持</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Direct creation
 * throw new OpenFunctionalException("Computation failed");
 *
 * // With cause
 * throw new OpenFunctionalException("Mapping failed", cause);
 *
 * // Factory methods
 * throw OpenFunctionalException.computationFailed("Division by zero");
 * throw OpenFunctionalException.noValue("Option is empty");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Serializable: Yes - 可序列化: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public class OpenFunctionalException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Component name for functional module
     * 函数式模块的组件名称
     */
    protected static final String COMPONENT = "FUNCTIONAL";

    // ==================== Constructors | 构造方法 ====================

    /**
     * Create exception with message
     * 创建带消息的异常
     *
     * @param message error message | 错误消息
     */
    public OpenFunctionalException(String message) {
        super(COMPONENT, null, message);
    }

    /**
     * Create exception with message and cause
     * 创建带消息和原因的异常
     *
     * @param message error message | 错误消息
     * @param cause   original exception | 原始异常
     */
    public OpenFunctionalException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
    }

    /**
     * Create exception with error code and message
     * 创建带错误码和消息的异常
     *
     * @param errorCode error code | 错误码
     * @param message   error message | 错误消息
     */
    public OpenFunctionalException(String errorCode, String message) {
        super(COMPONENT, errorCode, message);
    }

    /**
     * Create exception with error code, message and cause
     * 创建带错误码、消息和原因的异常
     *
     * @param errorCode error code | 错误码
     * @param message   error message | 错误消息
     * @param cause     original exception | 原始异常
     */
    public OpenFunctionalException(String errorCode, String message, Throwable cause) {
        super(COMPONENT, errorCode, message, cause);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create exception for computation failure
     * 为计算失败创建异常
     *
     * @param message error message | 错误消息
     * @return exception instance | 异常实例
     */
    public static OpenFunctionalException computationFailed(String message) {
        return new OpenFunctionalException("FUNC_001", "Computation failed: " + message);
    }

    /**
     * Create exception for computation failure with cause
     * 为带原因的计算失败创建异常
     *
     * @param message error message | 错误消息
     * @param cause   original exception | 原始异常
     * @return exception instance | 异常实例
     */
    public static OpenFunctionalException computationFailed(String message, Throwable cause) {
        return new OpenFunctionalException("FUNC_001", "Computation failed: " + message, cause);
    }

    /**
     * Create exception for missing value
     * 为缺少值创建异常
     *
     * @param message error message | 错误消息
     * @return exception instance | 异常实例
     */
    public static OpenFunctionalException noValue(String message) {
        return new OpenFunctionalException("FUNC_002", "No value present: " + message);
    }

    /**
     * Create exception for invalid state
     * 为无效状态创建异常
     *
     * @param message error message | 错误消息
     * @return exception instance | 异常实例
     */
    public static OpenFunctionalException invalidState(String message) {
        return new OpenFunctionalException("FUNC_003", "Invalid state: " + message);
    }

    /**
     * Create exception for mapping failure
     * 为映射失败创建异常
     *
     * @param cause original exception | 原始异常
     * @return exception instance | 异常实例
     */
    public static OpenFunctionalException mappingFailed(Throwable cause) {
        return new OpenFunctionalException("FUNC_004", "Mapping operation failed", cause);
    }

    /**
     * Create exception for filter failure
     * 为过滤失败创建异常
     *
     * @param message error message | 错误消息
     * @return exception instance | 异常实例
     */
    public static OpenFunctionalException filterFailed(String message) {
        return new OpenFunctionalException("FUNC_005", "Filter predicate not satisfied: " + message);
    }
}
