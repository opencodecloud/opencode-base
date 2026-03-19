package cloud.opencode.base.pool.impl;

import cloud.opencode.base.pool.ObjectPool;
import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.PooledObjectFactory;
import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.DefaultPooledObject;
import cloud.opencode.base.pool.metrics.DefaultPoolMetrics;
import cloud.opencode.base.pool.metrics.PoolMetrics;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadLocalPool - Thread Local Object Pool
 * ThreadLocalPool - 线程本地对象池
 *
 * <p>Object pool that maintains one object per thread using ThreadLocal.
 * Provides zero-contention access for single-object-per-thread scenarios.</p>
 * <p>使用ThreadLocal为每个线程维护一个对象的对象池。为单线程单对象场景提供零竞争访问。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>One object per thread - 每线程一个对象</li>
 *   <li>Zero contention access - 零竞争访问</li>
 *   <li>Automatic lazy initialization - 自动延迟初始化</li>
 *   <li>Thread-safe without locks - 无锁的线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ThreadLocalPool<StringBuilder> pool = new ThreadLocalPool<>(
 *     new BasePooledObjectFactory<>() {
 *         @Override
 *         protected StringBuilder create() {
 *             return new StringBuilder(1024);
 *         }
 *
 *         @Override
 *         public void passivateObject(PooledObject<StringBuilder> obj) {
 *             obj.getObject().setLength(0);
 *         }
 *     });
 *
 * StringBuilder sb = pool.borrowObject();
 * try {
 *     sb.append("Hello");
 * } finally {
 *     pool.returnObject(sb);  // Resets the StringBuilder
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Borrow/return: O(1) with no contention - 借用/归还: O(1) 无竞争</li>
 *   <li>Best for thread-affine workloads - 最适合线程亲和的工作负载</li>
 * </ul>
 *
 * <p><strong>Caution | 注意:</strong></p>
 * <ul>
 *   <li>Not suitable for Virtual Threads with many instances - 不适合大量虚拟线程实例</li>
 *   <li>Memory proportional to thread count - 内存与线程数成正比</li>
 *   <li><strong>Thread cleanup limitation:</strong> Due to JDK ThreadLocal design, {@link #close()} and
 *       {@link #clear()} can only destroy the pooled object belonging to the calling thread. Objects held
 *       by other threads cannot be removed from their ThreadLocal slots and will only be garbage collected
 *       when those threads terminate. The pool tracks all threads that have borrowed objects (via a
 *       ConcurrentHashMap-backed set) and marks the pool as closed so that subsequent borrow attempts from
 *       any thread will fail fast with an exception.</li>
 *   <li><strong>线程清理限制:</strong> 由于JDK ThreadLocal的设计，{@link #close()} 和 {@link #clear()} 只能销毁
 *       调用线程拥有的池化对象。其他线程持有的对象无法从其ThreadLocal槽中移除，只有在这些线程终止时才会被垃圾回收。
 *       池会跟踪所有借用过对象的线程（通过ConcurrentHashMap支持的集合），并将池标记为已关闭，
 *       使得任何线程后续的借用尝试都会立即抛出异常。</li>
 * </ul>
 *
 * @param <T> the type of object being pooled - 池化对象类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public class ThreadLocalPool<T> implements ObjectPool<T> {

    private final ThreadLocal<PooledObject<T>> localObject;
    private final PooledObjectFactory<T> factory;
    private final DefaultPoolMetrics metrics;
    private final AtomicInteger activeCount;
    private final Set<Thread> trackedThreads = ConcurrentHashMap.newKeySet();
    private final java.util.concurrent.atomic.AtomicLong borrowCount = new java.util.concurrent.atomic.AtomicLong(0);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Creates a thread-local pool.
     * 创建线程本地池。
     *
     * @param factory the object factory - 对象工厂
     */
    public ThreadLocalPool(PooledObjectFactory<T> factory) {
        this.factory = factory;
        this.metrics = new DefaultPoolMetrics();
        this.activeCount = new AtomicInteger(0);
        this.localObject = ThreadLocal.withInitial(() -> {
            try {
                PooledObject<T> obj = factory.makeObject();
                metrics.recordCreate();
                return obj;
            } catch (OpenPoolException e) {
                throw new RuntimeException(e);
            }
        });
        this.metrics.setActiveSupplier(this::getNumActive);
        this.metrics.setIdleSupplier(this::getNumIdle);
    }

    @Override
    public T borrowObject() throws OpenPoolException {
        checkClosed();
        trackedThreads.add(Thread.currentThread());
        // Periodically purge dead threads to prevent unbounded growth with virtual threads
        if (borrowCount.incrementAndGet() % 1024 == 0) {
            trackedThreads.removeIf(t -> !t.isAlive());
        }

        PooledObject<T> obj = localObject.get();
        try {
            factory.activateObject(obj);
            ((DefaultPooledObject<T>) obj).markBorrowed();
            activeCount.incrementAndGet();
            metrics.recordBorrow();
            return obj.getObject();
        } catch (Exception e) {
            // Object may be invalid, recreate
            obj = factory.makeObject();
            localObject.set(obj);
            try {
                factory.activateObject(obj);
                ((DefaultPooledObject<T>) obj).markBorrowed();
                activeCount.incrementAndGet();
                metrics.recordCreate();
                metrics.recordBorrow();
                return obj.getObject();
            } catch (Exception ex) {
                // Activation failed on new object, clean up
                try {
                    factory.destroyObject(obj);
                } catch (Exception ignored) {
                }
                localObject.remove();
                throw ex instanceof OpenPoolException ope ? ope :
                        new OpenPoolException("Failed to activate object", ex);
            }
        }
    }

    @Override
    public T borrowObject(Duration timeout) throws OpenPoolException {
        // Timeout not applicable for thread-local pool
        return borrowObject();
    }

    @Override
    public void returnObject(T obj) {
        if (obj == null) return;

        PooledObject<T> pooledObject = localObject.get();
        if (pooledObject != null && pooledObject.getObject() == obj) {
            try {
                factory.passivateObject(pooledObject);
                ((DefaultPooledObject<T>) pooledObject).markReturned();
            } catch (Exception e) {
                // Try to recreate on next borrow
            } finally {
                activeCount.decrementAndGet();
                metrics.recordReturn();
            }
        }
    }

    @Override
    public void invalidateObject(T obj) {
        if (obj == null) return;

        PooledObject<T> pooledObject = localObject.get();
        if (pooledObject != null && pooledObject.getObject() == obj) {
            try {
                factory.destroyObject(pooledObject);
            } catch (Exception e) {
                // Ignore
            } finally {
                localObject.remove();
                metrics.recordDestroy();
                activeCount.decrementAndGet();
            }
        }
    }

    @Override
    public void addObject() throws OpenPoolException {
        // Pre-initialize for current thread
        trackedThreads.add(Thread.currentThread());
        localObject.get();
    }

    @Override
    public int getNumIdle() {
        // Thread-local pool doesn't track idle across threads
        return 0;
    }

    @Override
    public int getNumActive() {
        return activeCount.get();
    }

    @Override
    public void clear() {
        // Due to JDK ThreadLocal design, we can only remove the ThreadLocal value for the
        // current thread. Other threads' pooled objects will be cleaned up when those threads
        // terminate or when they next attempt to borrow (and get an OpenPoolException because
        // the pool is closed). See the class-level Javadoc for the full explanation.
        if (trackedThreads.contains(Thread.currentThread())) {
            PooledObject<T> obj = localObject.get();
            if (obj != null) {
                try {
                    factory.destroyObject(obj);
                } catch (Exception e) {
                    // Ignore
                }
                localObject.remove();
            }
            trackedThreads.remove(Thread.currentThread());
        }
    }

    @Override
    public PoolMetrics getMetrics() {
        return metrics;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            clear();
        }
    }

    // ==================== Internal Methods ====================

    private void checkClosed() throws OpenPoolException {
        if (closed.get()) {
            throw new OpenPoolException("Pool is closed");
        }
    }
}
