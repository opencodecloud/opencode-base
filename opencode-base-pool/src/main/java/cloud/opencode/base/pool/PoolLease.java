package cloud.opencode.base.pool;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * PoolLease - AutoCloseable Lease for Borrowed Pool Objects
 * PoolLease - 借用池对象的自动关闭租约
 *
 * <p>An AutoCloseable wrapper around a borrowed pool object that enables
 * the try-with-resources pattern. When the lease is closed, the object is
 * automatically returned to the pool (or destroyed if invalidated).</p>
 * <p>借用池对象的 AutoCloseable 包装器，支持 try-with-resources 模式。
 * 当租约关闭时，对象会自动归还到池中（如果已失效则销毁）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Try-with-resources support - 支持 try-with-resources</li>
 *   <li>Automatic return on close - 关闭时自动归还</li>
 *   <li>Invalidation support - 支持失效标记</li>
 *   <li>Idempotent close - 幂等关闭</li>
 *   <li>Thread-safe - 线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (PoolLease<Connection> lease = pool.borrowLease()) {
 *     Connection conn = lease.get();
 *     conn.executeQuery("SELECT ...");
 * } // automatically returned to pool
 *
 * // With invalidation
 * try (PoolLease<Connection> lease = pool.borrowLease()) {
 *     Connection conn = lease.get();
 *     try {
 *         conn.executeQuery("SELECT ...");
 *     } catch (SQLException e) {
 *         lease.invalidate(); // destroy instead of return
 *         throw e;
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (AtomicBoolean for idempotent close) - 线程安全: 是 (AtomicBoolean 保证幂等关闭)</li>
 *   <li>Null-safe: No (object and pool must not be null) - 空值安全: 否 (对象和池不能为空)</li>
 * </ul>
 *
 * @param <T> the type of object being leased - 租约对象类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.3
 */
public final class PoolLease<T> implements AutoCloseable {

    private final T object;
    private final ObjectPool<T> pool;
    private final AtomicBoolean returned = new AtomicBoolean(false);
    private final AtomicBoolean invalidated = new AtomicBoolean(false);

    /**
     * Creates a new pool lease.
     * 创建新的池租约。
     *
     * <p>Package-private constructor; instances are created by the pool.</p>
     * <p>包内可见构造函数；实例由池创建。</p>
     *
     * @param object the borrowed object - 借用的对象
     * @param pool   the source pool - 来源池
     */
    PoolLease(T object, ObjectPool<T> pool) {
        this.object = Objects.requireNonNull(object, "object must not be null");
        this.pool = Objects.requireNonNull(pool, "pool must not be null");
    }

    /**
     * Gets the borrowed object.
     * 获取借用的对象。
     *
     * @return the borrowed object - 借用的对象
     * @throws IllegalStateException if the lease is already closed - 如果租约已关闭
     */
    public T get() {
        if (returned.get()) {
            throw new IllegalStateException("PoolLease is already closed");
        }
        return object;
    }

    /**
     * Marks the object as invalid so that it will be destroyed instead of
     * returned to the pool when the lease is closed.
     * 将对象标记为无效，使其在租约关闭时被销毁而非归还到池中。
     *
     * <p>This method can be called multiple times safely.</p>
     * <p>此方法可以安全地多次调用。</p>
     */
    public void invalidate() {
        this.invalidated.set(true);
    }

    /**
     * Closes the lease and returns (or invalidates) the object.
     * 关闭租约并归还（或失效）对象。
     *
     * <p>If {@link #invalidate()} was called, the object is destroyed via
     * {@link ObjectPool#invalidateObject(Object)}. Otherwise, the object is
     * returned via {@link ObjectPool#returnObject(Object)}.</p>
     * <p>如果调用了 {@link #invalidate()}，对象将通过
     * {@link ObjectPool#invalidateObject(Object)} 销毁。否则，对象将通过
     * {@link ObjectPool#returnObject(Object)} 归还。</p>
     *
     * <p>This method is idempotent: only the first invocation has effect.</p>
     * <p>此方法是幂等的：只有第一次调用有效果。</p>
     */
    @Override
    public void close() {
        if (returned.compareAndSet(false, true)) {
            if (invalidated.get()) {
                pool.invalidateObject(object);
            } else {
                pool.returnObject(object);
            }
        }
    }

    /**
     * Checks whether the lease has been closed.
     * 检查租约是否已关闭。
     *
     * @return {@code true} if the lease is closed - 如果租约已关闭返回 {@code true}
     */
    public boolean isClosed() {
        return returned.get();
    }
}
