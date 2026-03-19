package cloud.opencode.base.pool.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;
import java.time.Duration;

/**
 * OpenPoolException - Pool Exception
 * OpenPoolException - 对象池异常
 *
 * <p>Exception class for object pool operations, supporting error types
 * and factory methods for common error scenarios.</p>
 * <p>对象池操作异常类，支持错误类型和常见错误场景的工厂方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Error type classification - 错误类型分类</li>
 *   <li>Pool name tracking - 池名称追踪</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
 *   <li>Extends OpenException for unified handling - 继承OpenException统一处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new OpenPoolException("Pool operation failed");
 * throw OpenPoolException.exhausted("connection-pool");
 * throw OpenPoolException.timeout("db-pool", Duration.ofSeconds(5));
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
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public class OpenPoolException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "POOL";

    private final String poolName;
    private final PoolErrorType errorType;

    /**
     * Pool error type enumeration.
     * 池错误类型枚举。
     */
    public enum PoolErrorType {
        /** General error - 一般错误 */
        GENERAL,
        /** Pool exhausted - 池耗尽 */
        EXHAUSTED,
        /** Timeout waiting - 超时 */
        TIMEOUT,
        /** Validation failed - 验证失败 */
        VALIDATION,
        /** Pool closed - 池已关闭 */
        CLOSED,
        /** Object creation failed - 创建对象失败 */
        CREATE,
        /** Object destruction failed - 销毁对象失败 */
        DESTROY,
        /** Object activation failed - 激活对象失败 */
        ACTIVATE,
        /** Object passivation failed - 钝化对象失败 */
        PASSIVATE,
        /** Invalid state - 非法状态 */
        INVALID_STATE
    }

    // ==================== Constructors ====================

    /**
     * Creates exception with message.
     * 创建带消息的异常。
     *
     * @param message the message - 异常消息
     */
    public OpenPoolException(String message) {
        super(COMPONENT, null, message);
        this.poolName = null;
        this.errorType = PoolErrorType.GENERAL;
    }

    /**
     * Creates exception with message and cause.
     * 创建带消息和原因的异常。
     *
     * @param message the message - 异常消息
     * @param cause   the cause - 原始异常
     */
    public OpenPoolException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.poolName = null;
        this.errorType = PoolErrorType.GENERAL;
    }

    /**
     * Creates exception with full details.
     * 创建带完整详情的异常。
     *
     * @param message   the message - 异常消息
     * @param poolName  the pool name - 池名称
     * @param errorType the error type - 错误类型
     */
    public OpenPoolException(String message, String poolName, PoolErrorType errorType) {
        super(COMPONENT, errorType.name(), message);
        this.poolName = poolName;
        this.errorType = errorType;
    }

    /**
     * Creates exception with full details and cause.
     * 创建带完整详情和原因的异常。
     *
     * @param message   the message - 异常消息
     * @param poolName  the pool name - 池名称
     * @param errorType the error type - 错误类型
     * @param cause     the cause - 原始异常
     */
    public OpenPoolException(String message, String poolName, PoolErrorType errorType, Throwable cause) {
        super(COMPONENT, errorType.name(), message, cause);
        this.poolName = poolName;
        this.errorType = errorType;
    }

    // ==================== Getters ====================

    /**
     * Gets the pool name.
     * 获取池名称。
     *
     * @return the pool name - 池名称
     */
    public String poolName() {
        return poolName;
    }

    /**
     * Gets the error type.
     * 获取错误类型。
     *
     * @return the error type - 错误类型
     */
    public PoolErrorType errorType() {
        return errorType;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates pool exhausted exception.
     * 创建池耗尽异常。
     *
     * @param poolName the pool name - 池名称
     * @return the exception - 异常
     */
    public static OpenPoolException exhausted(String poolName) {
        return new OpenPoolException(
                "Pool exhausted: " + poolName,
                poolName, PoolErrorType.EXHAUSTED
        );
    }

    /**
     * Creates timeout exception.
     * 创建超时异常。
     *
     * @param poolName the pool name - 池名称
     * @param timeout  the timeout duration - 超时时长
     * @return the exception - 异常
     */
    public static OpenPoolException timeout(String poolName, Duration timeout) {
        return new OpenPoolException(
                "Timeout waiting for object after " + timeout.toMillis() + "ms: " + poolName,
                poolName, PoolErrorType.TIMEOUT
        );
    }

    /**
     * Creates validation failed exception.
     * 创建验证失败异常。
     *
     * @param poolName the pool name - 池名称
     * @return the exception - 异常
     */
    public static OpenPoolException validationFailed(String poolName) {
        return new OpenPoolException(
                "Object validation failed: " + poolName,
                poolName, PoolErrorType.VALIDATION
        );
    }

    /**
     * Creates pool closed exception.
     * 创建池已关闭异常。
     *
     * @param poolName the pool name - 池名称
     * @return the exception - 异常
     */
    public static OpenPoolException closed(String poolName) {
        return new OpenPoolException(
                "Pool is closed: " + poolName,
                poolName, PoolErrorType.CLOSED
        );
    }

    /**
     * Creates object creation failed exception.
     * 创建对象创建失败异常。
     *
     * @param poolName the pool name - 池名称
     * @param cause    the cause - 原始异常
     * @return the exception - 异常
     */
    public static OpenPoolException createFailed(String poolName, Throwable cause) {
        return new OpenPoolException(
                "Failed to create object: " + poolName,
                poolName, PoolErrorType.CREATE, cause
        );
    }

    /**
     * Creates object destruction failed exception.
     * 创建对象销毁失败异常。
     *
     * @param poolName the pool name - 池名称
     * @param cause    the cause - 原始异常
     * @return the exception - 异常
     */
    public static OpenPoolException destroyFailed(String poolName, Throwable cause) {
        return new OpenPoolException(
                "Failed to destroy object: " + poolName,
                poolName, PoolErrorType.DESTROY, cause
        );
    }

    /**
     * Creates object activation failed exception.
     * 创建对象激活失败异常。
     *
     * @param poolName the pool name - 池名称
     * @param cause    the cause - 原始异常
     * @return the exception - 异常
     */
    public static OpenPoolException activateFailed(String poolName, Throwable cause) {
        return new OpenPoolException(
                "Failed to activate object: " + poolName,
                poolName, PoolErrorType.ACTIVATE, cause
        );
    }

    /**
     * Creates object passivation failed exception.
     * 创建对象钝化失败异常。
     *
     * @param poolName the pool name - 池名称
     * @param cause    the cause - 原始异常
     * @return the exception - 异常
     */
    public static OpenPoolException passivateFailed(String poolName, Throwable cause) {
        return new OpenPoolException(
                "Failed to passivate object: " + poolName,
                poolName, PoolErrorType.PASSIVATE, cause
        );
    }

    /**
     * Creates invalid state exception.
     * 创建非法状态异常。
     *
     * @param message the message - 消息
     * @return the exception - 异常
     */
    public static OpenPoolException invalidState(String message) {
        return new OpenPoolException(
                message,
                null, PoolErrorType.INVALID_STATE
        );
    }
}
