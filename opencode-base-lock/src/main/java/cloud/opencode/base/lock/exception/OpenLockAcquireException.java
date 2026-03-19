package cloud.opencode.base.lock.exception;

/**
 * Lock Acquisition Exception for Non-Timeout Failures
 * 非超时失败的锁获取异常
 *
 * <p>Thrown when lock acquisition fails for reasons other than timeout,
 * such as interruption or internal errors.</p>
 * <p>当锁获取因超时以外的原因（如中断或内部错误）而失败时抛出。</p>
 *
 * <p><strong>Common Causes | 常见原因:</strong></p>
 * <ul>
 *   <li>Thread interrupted during wait - 等待期间线程被中断</li>
 *   <li>Lock in invalid state - 锁处于无效状态</li>
 *   <li>Internal lock errors - 内部锁错误</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core functionality for OpenLockAcquireException - OpenLockAcquireException的核心功能</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Handle lock acquisition failure
 * // 处理锁获取失败
 * try {
 *     lock.tryLock(timeout);
 * } catch (OpenLockAcquireException e) {
 *     log.warn("Could not acquire lock: {}", e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable exception) - 线程安全: 是（不可变异常）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see OpenLockException
 * @see OpenLockTimeoutException
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public class OpenLockAcquireException extends OpenLockException {

    /**
     * Constructs a new acquire exception with message
     * 使用消息构造新的获取异常
     *
     * @param message the error message | 错误消息
     */
    public OpenLockAcquireException(String message) {
        super(message, null, LockErrorType.ACQUIRE);
    }

    /**
     * Constructs a new acquire exception with message and cause
     * 使用消息和原因构造新的获取异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public OpenLockAcquireException(String message, Throwable cause) {
        super(message, null, LockErrorType.ACQUIRE, cause);
    }

    /**
     * Constructs a new acquire exception with lock name and cause
     * 使用锁名称和原因构造新的获取异常
     *
     * @param message  the error message | 错误消息
     * @param lockName the lock name | 锁名称
     * @param cause    the cause | 原因
     */
    public OpenLockAcquireException(String message, String lockName, Throwable cause) {
        super(message, lockName, LockErrorType.ACQUIRE, cause);
    }
}
