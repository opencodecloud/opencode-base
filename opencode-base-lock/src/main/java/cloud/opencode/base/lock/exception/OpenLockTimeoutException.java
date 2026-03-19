package cloud.opencode.base.lock.exception;

import java.time.Duration;

/**
 * Lock Timeout Exception for Acquisition Timeout
 * 获取超时的锁超时异常
 *
 * <p>Thrown when lock acquisition times out before the lock could be obtained,
 * providing information about the wait duration.</p>
 * <p>当锁获取在获得锁之前超时时抛出，提供有关等待时长的信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Wait time tracking - 等待时间跟踪</li>
 *   <li>Lock name identification - 锁名称标识</li>
 *   <li>Timeout-specific handling - 特定超时处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     lock.lock(Duration.ofSeconds(5));
 * } catch (OpenLockTimeoutException e) {
 *     Duration waited = e.waitTime();
 *     // Handle timeout | 处理超时
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
 * @see OpenLockAcquireException
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public class OpenLockTimeoutException extends OpenLockException {

    private final Duration waitTime;

    /**
     * Constructs a new timeout exception with message
     * 使用消息构造新的超时异常
     *
     * @param message the error message | 错误消息
     */
    public OpenLockTimeoutException(String message) {
        super(message, null, LockErrorType.TIMEOUT);
        this.waitTime = null;
    }

    /**
     * Constructs a new timeout exception with wait time
     * 使用等待时间构造新的超时异常
     *
     * @param message  the error message | 错误消息
     * @param waitTime the time spent waiting | 等待时间
     */
    public OpenLockTimeoutException(String message, Duration waitTime) {
        super(message, null, LockErrorType.TIMEOUT);
        this.waitTime = waitTime;
    }

    /**
     * Constructs a new timeout exception with lock name and wait time
     * 使用锁名称和等待时间构造新的超时异常
     *
     * @param message  the error message | 错误消息
     * @param lockName the lock name | 锁名称
     * @param waitTime the time spent waiting | 等待时间
     */
    public OpenLockTimeoutException(String message, String lockName, Duration waitTime) {
        super(message, lockName, LockErrorType.TIMEOUT);
        this.waitTime = waitTime;
    }

    /**
     * Gets the time spent waiting before the timeout occurred
     * 获取超时发生前的等待时间
     *
     * @return the time spent waiting, or null if not tracked | 等待时间，如果未跟踪则为null
     */
    public Duration waitTime() {
        return waitTime;
    }
}
