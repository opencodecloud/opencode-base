package cloud.opencode.base.pool.factory;

import cloud.opencode.base.pool.PooledObject;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * DefaultPooledObject - Default Pooled Object Implementation
 * DefaultPooledObject - 默认池化对象实现
 *
 * <p>Default implementation of PooledObject with thread-safe state
 * management and timing tracking.</p>
 * <p>PooledObject的默认实现，具有线程安全的状态管理和时间追踪。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe state management with AtomicReference - 使用AtomicReference的线程安全状态管理</li>
 *   <li>Lock-free borrow counting with LongAdder - 使用LongAdder的无锁借用计数</li>
 *   <li>Volatile timing fields for visibility - Volatile时间字段保证可见性</li>
 *   <li>CAS-based state transitions - 基于CAS的状态转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DefaultPooledObject<Connection> pooled = new DefaultPooledObject<>(connection);
 * Connection conn = pooled.getObject();
 * pooled.markBorrowed();
 * // use connection
 * pooled.markReturned();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>State transitions: O(1) CAS operation - 状态转换: O(1) CAS操作</li>
 *   <li>Counter updates: O(1) LongAdder - 计数更新: O(1) LongAdder</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Lock-free: Yes - 无锁: 是</li>
 * </ul>
 *
 * @param <T> the type of object being pooled - 池化对象类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public class DefaultPooledObject<T> implements PooledObject<T> {

    private final T object;
    private final Instant createInstant;
    private volatile Instant lastBorrowInstant;
    private volatile Instant lastReturnInstant;
    private volatile Instant lastUseInstant;
    private final AtomicReference<PooledObjectState> state;
    private final LongAdder borrowCount;

    /**
     * Creates a new pooled object wrapper.
     * 创建新的池化对象包装器。
     *
     * @param object the object to wrap - 要包装的对象
     */
    public DefaultPooledObject(T object) {
        this.object = object;
        this.createInstant = Instant.now();
        this.lastBorrowInstant = createInstant;
        this.lastReturnInstant = createInstant;
        this.lastUseInstant = createInstant;
        this.state = new AtomicReference<>(PooledObjectState.IDLE);
        this.borrowCount = new LongAdder();
    }

    @Override
    public T getObject() {
        return object;
    }

    @Override
    public Instant getCreateInstant() {
        return createInstant;
    }

    @Override
    public Instant getLastBorrowInstant() {
        return lastBorrowInstant;
    }

    @Override
    public Instant getLastReturnInstant() {
        return lastReturnInstant;
    }

    @Override
    public Instant getLastUseInstant() {
        return lastUseInstant;
    }

    @Override
    public PooledObjectState getState() {
        return state.get();
    }

    @Override
    public long getBorrowCount() {
        return borrowCount.sum();
    }

    @Override
    public Duration getActiveDuration() {
        Instant now = Instant.now();
        if (state.get() == PooledObjectState.ALLOCATED) {
            return Duration.between(lastBorrowInstant, now);
        }
        return Duration.between(lastBorrowInstant, lastReturnInstant);
    }

    @Override
    public Duration getIdleDuration() {
        if (state.get() != PooledObjectState.IDLE) {
            return Duration.ZERO;
        }
        return Duration.between(lastReturnInstant, Instant.now());
    }

    @Override
    public boolean compareAndSetState(PooledObjectState expect, PooledObjectState update) {
        return state.compareAndSet(expect, update);
    }

    /**
     * Marks the object as borrowed.
     * 标记对象已被借用。
     */
    public void markBorrowed() {
        lastBorrowInstant = Instant.now();
        lastUseInstant = lastBorrowInstant;
        borrowCount.increment();
    }

    /**
     * Marks the object as returned.
     * 标记对象已归还。
     */
    public void markReturned() {
        lastReturnInstant = Instant.now();
        lastUseInstant = lastReturnInstant;
    }

    /**
     * Sets the state directly (use with caution).
     * 直接设置状态（谨慎使用）。
     *
     * @param newState the new state - 新状态
     */
    public void setState(PooledObjectState newState) {
        state.set(newState);
    }

    @Override
    public String toString() {
        return "DefaultPooledObject{" +
                "object=" + object +
                ", state=" + state.get() +
                ", borrowCount=" + borrowCount.sum() +
                ", createInstant=" + createInstant +
                '}';
    }
}
