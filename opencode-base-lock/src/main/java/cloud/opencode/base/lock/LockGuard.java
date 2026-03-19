package cloud.opencode.base.lock;

/**
 * Lock Guard for Automatic Resource Release
 * 锁守卫 - 用于自动资源释放
 *
 * <p>A record that holds a lock and its token, implementing AutoCloseable
 * for use with try-with-resources pattern to ensure automatic lock release.</p>
 * <p>持有锁及其令牌的记录，实现AutoCloseable接口，
 * 配合try-with-resources模式确保自动释放锁。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic lock release - 自动释放锁</li>
 *   <li>Token access for lock identification - 令牌访问用于锁标识</li>
 *   <li>Exception-safe release - 异常安全释放</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Automatic release with try-with-resources | 使用try-with-resources自动释放
 * try (var guard = lock.lock()) {
 *     // Critical section | 临界区
 *     Long token = guard.token(); // Access token if needed | 如需要可访问令牌
 * } // Lock automatically released here | 锁在此自动释放
 *
 * // Even on exception | 即使发生异常
 * try (var guard = lock.lock()) {
 *     throw new RuntimeException("Error");
 * } // Lock still released | 锁仍然会释放
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Exception-safe: Yes - 异常安全: 是</li>
 * </ul>
 *
 * @param lock  the lock being held | 被持有的锁
 * @param token the lock token | 锁令牌
 * @param <T>   the type of lock token | 锁令牌类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Lock
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public record LockGuard<T>(Lock<T> lock, T token) implements AutoCloseable {

    /**
     * Releases the lock when the guard is closed
     * 当守卫关闭时释放锁
     *
     * <p>Called automatically when using try-with-resources.</p>
     * <p>使用try-with-resources时自动调用。</p>
     */
    @Override
    public void close() {
        lock.unlock();
    }
}
