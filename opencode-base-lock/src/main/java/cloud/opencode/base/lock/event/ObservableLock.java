package cloud.opencode.base.lock.event;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.System.Logger.Level.WARNING;

/**
 * Observable Lock Decorator with Event Notification
 * 带事件通知的可观察锁装饰器
 *
 * <p>A decorator that wraps any {@link Lock} implementation and fires
 * {@link LockEvent} notifications to registered {@link LockListener}s
 * on lock lifecycle transitions.</p>
 * <p>一个装饰器，包装任意 {@link Lock} 实现，并在锁生命周期转换时
 * 向注册的 {@link LockListener} 发送 {@link LockEvent} 通知。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Transparent decorator pattern - 透明装饰器模式</li>
 *   <li>Thread-safe listener management - 线程安全的监听器管理</li>
 *   <li>Listener exception isolation - 监听器异常隔离</li>
 *   <li>Wait-time tracking for acquisition events - 获取事件的等待时间跟踪</li>
 *   <li>Fluent API for listener registration - 流式API用于监听器注册</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Wrap an existing lock | 包装现有锁
 * Lock<Long> baseLock = new LocalLock();
 * ObservableLock<Long> lock = new ObservableLock<>(baseLock, "order-lock");
 *
 * // Add listeners | 添加监听器
 * lock.addListener(event -> log.info("{}: {}", event.type(), event.lockName()));
 * lock.addListener(event -> metrics.record(event));
 *
 * // Use like a normal lock | 像普通锁一样使用
 * try (var guard = lock.lock()) {
 *     // Critical section | 临界区
 * }
 * // ACQUIRED and RELEASED events are fired automatically
 * // ACQUIRED和RELEASED事件自动触发
 *
 * // With initial listeners | 带初始监听器
 * ObservableLock<Long> lock2 = new ObservableLock<>(baseLock, "stock-lock",
 *     auditListener, metricsListener);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (CopyOnWriteArrayList for listeners) -
 *       线程安全: 是（监听器使用CopyOnWriteArrayList）</li>
 *   <li>Exception isolation: Listener exceptions are caught and suppressed -
 *       异常隔离: 监听器异常被捕获并抑制</li>
 *   <li>Virtual Thread friendly: Yes - 虚拟线程友好: 是</li>
 * </ul>
 *
 * @param <T> the type of lock token | 锁令牌类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Lock
 * @see LockEvent
 * @see LockListener
 * @since JDK 25, opencode-base-lock V1.0.3
 */
public class ObservableLock<T> implements Lock<T> {

    private static final System.Logger LOG = System.getLogger(ObservableLock.class.getName());

    private final Lock<T> delegate;
    private final String lockName;
    private final List<LockListener> listeners;

    /**
     * Creates an observable lock wrapping the given delegate
     * 创建包装给定委托的可观察锁
     *
     * @param delegate the underlying lock to decorate | 要装饰的底层锁
     * @param lockName the name for this lock (used in events) | 此锁的名称（用于事件）
     * @throws NullPointerException if delegate or lockName is null |
     *                              如果delegate或lockName为null则抛出
     */
    public ObservableLock(Lock<T> delegate, String lockName) {
        this.delegate = Objects.requireNonNull(delegate,
                "Delegate lock must not be null | 委托锁不能为 null");
        this.lockName = Objects.requireNonNull(lockName,
                "Lock name must not be null | 锁名称不能为 null");
        this.listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Creates an observable lock with initial listeners
     * 创建带初始监听器的可观察锁
     *
     * @param delegate  the underlying lock to decorate | 要装饰的底层锁
     * @param lockName  the name for this lock (used in events) | 此锁的名称（用于事件）
     * @param listeners the initial listeners to register | 要注册的初始监听器
     * @throws NullPointerException if delegate or lockName is null |
     *                              如果delegate或lockName为null则抛出
     */
    public ObservableLock(Lock<T> delegate, String lockName, LockListener... listeners) {
        this(delegate, lockName);
        if (listeners != null) {
            for (LockListener listener : listeners) {
                if (listener != null) {
                    this.listeners.add(listener);
                }
            }
        }
    }

    /**
     * Adds a listener to receive lock events
     * 添加监听器以接收锁事件
     *
     * @param listener the listener to add | 要添加的监听器
     * @return this observable lock for fluent chaining | 此可观察锁用于链式调用
     * @throws NullPointerException if listener is null | 如果listener为null则抛出
     */
    public ObservableLock<T> addListener(LockListener listener) {
        Objects.requireNonNull(listener, "Listener must not be null | 监听器不能为 null");
        listeners.add(listener);
        return this;
    }

    /**
     * Removes a listener from receiving lock events
     * 移除监听器使其不再接收锁事件
     *
     * @param listener the listener to remove | 要移除的监听器
     * @return this observable lock for fluent chaining | 此可观察锁用于链式调用
     */
    public ObservableLock<T> removeListener(LockListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
        return this;
    }

    /**
     * Gets the lock name used in events
     * 获取事件中使用的锁名称
     *
     * @return the lock name | 锁名称
     */
    public String getLockName() {
        return lockName;
    }

    @Override
    public LockGuard<T> lock() {
        if (listeners.isEmpty()) {
            LockGuard<T> guard = delegate.lock();
            return new LockGuard<>(this, guard.token());
        }
        long startNanos = System.nanoTime();
        LockGuard<T> guard = delegate.lock();
        Duration waitTime = Duration.ofNanos(System.nanoTime() - startNanos);
        fireEvent(LockEvent.acquired(lockName, waitTime));
        return new LockGuard<>(this, guard.token());
    }

    @Override
    public LockGuard<T> lock(Duration timeout) {
        if (listeners.isEmpty()) {
            LockGuard<T> guard = delegate.lock(timeout);
            return new LockGuard<>(this, guard.token());
        }
        long startNanos = System.nanoTime();
        try {
            LockGuard<T> guard = delegate.lock(timeout);
            Duration waitTime = Duration.ofNanos(System.nanoTime() - startNanos);
            fireEvent(LockEvent.acquired(lockName, waitTime));
            return new LockGuard<>(this, guard.token());
        } catch (OpenLockTimeoutException e) {
            Duration waitTime = Duration.ofNanos(System.nanoTime() - startNanos);
            fireEvent(LockEvent.timeout(lockName, waitTime));
            throw e;
        } catch (RuntimeException e) {
            fireEvent(LockEvent.error(lockName));
            throw e;
        }
    }

    @Override
    public boolean tryLock() {
        boolean acquired = delegate.tryLock();
        if (acquired && !listeners.isEmpty()) {
            fireEvent(LockEvent.acquired(lockName, Duration.ZERO));
        }
        return acquired;
    }

    @Override
    public boolean tryLock(Duration timeout) {
        if (listeners.isEmpty()) {
            return delegate.tryLock(timeout);
        }
        long startNanos = System.nanoTime();
        boolean acquired = delegate.tryLock(timeout);
        Duration waitTime = Duration.ofNanos(System.nanoTime() - startNanos);
        if (acquired) {
            fireEvent(LockEvent.acquired(lockName, waitTime));
        } else {
            fireEvent(LockEvent.timeout(lockName, waitTime));
        }
        return acquired;
    }

    @Override
    public LockGuard<T> lockInterruptibly() throws InterruptedException {
        if (listeners.isEmpty()) {
            LockGuard<T> guard = delegate.lockInterruptibly();
            return new LockGuard<>(this, guard.token());
        }
        long startNanos = System.nanoTime();
        LockGuard<T> guard = delegate.lockInterruptibly();
        Duration waitTime = Duration.ofNanos(System.nanoTime() - startNanos);
        fireEvent(LockEvent.acquired(lockName, waitTime));
        return new LockGuard<>(this, guard.token());
    }

    @Override
    public void unlock() {
        try {
            delegate.unlock();
        } finally {
            if (!listeners.isEmpty()) {
                fireEvent(LockEvent.released(lockName));
            }
        }
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return delegate.isHeldByCurrentThread();
    }

    @Override
    public Optional<T> getToken() {
        return delegate.getToken();
    }

    /**
     * Fires an event to all registered listeners
     * 向所有注册的监听器触发事件
     *
     * <p>Exceptions thrown by listeners are caught and suppressed to
     * prevent listener failures from affecting lock operations.</p>
     * <p>监听器抛出的异常被捕获并抑制，以防止监听器故障影响锁操作。</p>
     *
     * @param event the event to fire | 要触发的事件
     */
    private void fireEvent(LockEvent event) {
        for (LockListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                // Suppress listener exceptions to not affect lock operations
                // 抑制监听器异常，不影响锁操作
                LOG.log(WARNING, "LockListener threw exception for event "
                        + event.type() + " on lock '" + lockName + "'", e);
            }
        }
    }
}
