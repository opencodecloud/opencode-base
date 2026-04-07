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
 *   <li>Zero-allocation nanoTime timestamps on hot path - 热路径上零分配的nanoTime时间戳</li>
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
 *   <li>markBorrowed/markReturned: zero allocation (nanoTime) - markBorrowed/markReturned: 零分配 (nanoTime)</li>
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
    private final long createNanoTime;
    private volatile long lastBorrowNanos;
    private volatile long lastReturnNanos;
    private volatile long lastUseNanos;
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
        // Capture nanoTime BEFORE Instant.now() so that nanoTime-derived instants
        // are never behind wall clock (avoids test ordering issues)
        this.createNanoTime = System.nanoTime();
        this.createInstant = Instant.now();
        this.lastBorrowNanos = createNanoTime;
        this.lastReturnNanos = createNanoTime;
        this.lastUseNanos = createNanoTime;
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
        return createInstant.plusNanos(lastBorrowNanos - createNanoTime);
    }

    @Override
    public Instant getLastReturnInstant() {
        return createInstant.plusNanos(lastReturnNanos - createNanoTime);
    }

    @Override
    public Instant getLastUseInstant() {
        return createInstant.plusNanos(lastUseNanos - createNanoTime);
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
        if (state.get() == PooledObjectState.ALLOCATED) {
            return Duration.ofNanos(System.nanoTime() - lastBorrowNanos);
        }
        return Duration.ofNanos(lastReturnNanos - lastBorrowNanos);
    }

    @Override
    public Duration getIdleDuration() {
        if (state.get() != PooledObjectState.IDLE) {
            return Duration.ZERO;
        }
        return Duration.ofNanos(System.nanoTime() - lastReturnNanos);
    }

    @Override
    public boolean compareAndSetState(PooledObjectState expect, PooledObjectState update) {
        return state.compareAndSet(expect, update);
    }

    /**
     * Marks the object as borrowed (zero-allocation).
     * 标记对象已被借用（零分配）。
     */
    public void markBorrowed() {
        long now = System.nanoTime();
        lastBorrowNanos = now;
        lastUseNanos = now;
        borrowCount.increment();
    }

    /**
     * Marks the object as borrowed with a pre-captured nanoTime (avoids extra System.nanoTime() call).
     * 使用预捕获的 nanoTime 标记对象已被借用（避免额外的 System.nanoTime() 调用）。
     *
     * @param nanoTime the pre-captured nanoTime value - 预捕获的 nanoTime 值
     */
    public void markBorrowed(long nanoTime) {
        lastBorrowNanos = nanoTime;
        lastUseNanos = nanoTime;
        borrowCount.increment();
    }

    /**
     * Marks the object as returned (zero-allocation).
     * 标记对象已归还（零分配）。
     */
    public void markReturned() {
        long now = System.nanoTime();
        lastReturnNanos = now;
        lastUseNanos = now;
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
