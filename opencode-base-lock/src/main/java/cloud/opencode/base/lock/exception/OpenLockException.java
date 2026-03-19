package cloud.opencode.base.lock.exception;

import cloud.opencode.base.core.exception.OpenException;

/**
 * Base Lock Exception for All Lock-Related Errors
 * 所有锁相关错误的基础锁异常
 *
 * <p>Base exception class for all lock-related errors in the lock component,
 * providing detailed error information including lock name and error type.</p>
 * <p>锁组件中所有锁相关错误的基础异常类，提供包括锁名称和错误类型的详细错误信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lock name tracking - 锁名称跟踪</li>
 *   <li>Error type classification - 错误类型分类</li>
 *   <li>Cause chaining - 原因链</li>
 *   <li>Component identification - 组件标识</li>
 * </ul>
 *
 * <p><strong>Error Types | 错误类型:</strong></p>
 * <ul>
 *   <li>GENERAL - General lock errors | 一般锁错误</li>
 *   <li>TIMEOUT - Lock acquisition timeout | 锁获取超时</li>
 *   <li>ACQUIRE - Lock acquisition failure | 锁获取失败</li>
 *   <li>RELEASE - Lock release failure | 锁释放失败</li>
 *   <li>EXPIRED - Lock expired | 锁已过期</li>
 *   <li>NOT_HELD - Lock not held by thread | 线程未持有锁</li>
 *   <li>DEADLOCK - Deadlock detected | 检测到死锁</li>
 *   <li>INTERRUPTED - Thread interrupted | 线程被中断</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch lock operation failures
 * // 捕获锁操作失败
 * try {
 *     lock.lock();
 * } catch (OpenLockException e) {
 *     log.error("Lock failed: {}", e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see OpenLockTimeoutException
 * @see OpenLockAcquireException
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public class OpenLockException extends OpenException {

    private static final String COMPONENT = "LOCK";

    private final String lockName;
    private final LockErrorType errorType;

    /**
     * Constructs a new lock exception with message
     * 使用消息构造新的锁异常
     *
     * @param message the error message | 错误消息
     */
    public OpenLockException(String message) {
        super(COMPONENT, null, message);
        this.lockName = null;
        this.errorType = LockErrorType.GENERAL;
    }

    /**
     * Constructs a new lock exception with message and cause
     * 使用消息和原因构造新的锁异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public OpenLockException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.lockName = null;
        this.errorType = LockErrorType.GENERAL;
    }

    /**
     * Constructs a new lock exception with full details
     * 使用完整详情构造新的锁异常
     *
     * @param message   the error message | 错误消息
     * @param lockName  the lock name | 锁名称
     * @param errorType the error type | 错误类型
     */
    public OpenLockException(String message, String lockName, LockErrorType errorType) {
        super(COMPONENT, null, message);
        this.lockName = lockName;
        this.errorType = errorType;
    }

    /**
     * Constructs a new lock exception with full details and cause
     * 使用完整详情和原因构造新的锁异常
     *
     * @param message   the error message | 错误消息
     * @param lockName  the lock name | 锁名称
     * @param errorType the error type | 错误类型
     * @param cause     the cause | 原因
     */
    public OpenLockException(String message, String lockName, LockErrorType errorType, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.lockName = lockName;
        this.errorType = errorType;
    }

    /**
     * Gets the name of the lock that caused the exception
     * 获取导致异常的锁名称
     *
     * @return the lock name, or null if not specified | 锁名称，如果未指定则为null
     */
    public String lockName() {
        return lockName;
    }

    /**
     * Gets the type of lock error that occurred
     * 获取发生的锁错误类型
     *
     * @return the error type | 错误类型
     */
    public LockErrorType errorType() {
        return errorType;
    }

    /**
     * Lock Error Type Enumeration
     * 锁错误类型枚举
     *
     * <p>Classifies different types of lock errors for handling.</p>
     * <p>对不同类型的锁错误进行分类以便处理。</p>
     */
    public enum LockErrorType {
        /**
         * General error - 一般错误
         */
        GENERAL,

        /**
         * Timeout error - 超时
         */
        TIMEOUT,

        /**
         * Acquire failed - 获取失败
         */
        ACQUIRE,

        /**
         * Release failed - 释放失败
         */
        RELEASE,

        /**
         * Lock expired - 锁已过期
         */
        EXPIRED,

        /**
         * Lock not held - 未持有锁
         */
        NOT_HELD,

        /**
         * Deadlock detected - 死锁
         */
        DEADLOCK,

        /**
         * Interrupted - 被中断
         */
        INTERRUPTED
    }
}
