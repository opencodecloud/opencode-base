package cloud.opencode.base.pool.impl;

import cloud.opencode.base.pool.ObjectPool;
import cloud.opencode.base.pool.PoolConfig;
import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.PooledObjectFactory;
import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.DefaultPooledObject;
import cloud.opencode.base.pool.factory.PooledObjectState;
import cloud.opencode.base.pool.metrics.DefaultPoolMetrics;
import cloud.opencode.base.pool.metrics.PoolMetrics;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SoftReferencePool - Soft Reference Object Pool
 * SoftReferencePool - 软引用对象池
 *
 * <p>Object pool using soft references for idle objects. Objects can be
 * garbage collected when memory is low, providing automatic memory management.</p>
 * <p>使用软引用存储空闲对象的对象池。当内存不足时对象可被垃圾回收，提供自动内存管理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Soft reference for idle objects - 空闲对象使用软引用</li>
 *   <li>Automatic memory reclaim - 自动内存回收</li>
 *   <li>GC-friendly pooling - GC友好的池化</li>
 *   <li>No hard limit on idle objects - 空闲对象无硬限制</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SoftReferencePool<ExpensiveObject> pool = new SoftReferencePool<>(factory, config);
 *
 * ExpensiveObject obj = pool.borrowObject();
 * try {
 *     // use object
 * } finally {
 *     pool.returnObject(obj);
 * }
 * // Object may be GC'd if memory is low
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Lower memory footprint under pressure - 内存压力下占用更低</li>
 *   <li>May recreate objects after GC - GC后可能需要重建对象</li>
 * </ul>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentLinkedQueue, AtomicInteger) - 线程安全: 是（ConcurrentLinkedQueue，AtomicInteger）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @param <T> the type of object being pooled - 池化对象类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public class SoftReferencePool<T> implements ObjectPool<T> {

    private final PooledObjectFactory<T> factory;
    private final PoolConfig config;
    private final ConcurrentLinkedQueue<SoftReference<PooledObject<T>>> idleObjects;
    private final ReferenceQueue<PooledObject<T>> refQueue;
    private final ConcurrentHashMap<IdentityWrapper<T>, PooledObject<T>> allObjects;
    private final AtomicInteger numActive;
    private final AtomicInteger numIdle;
    private final DefaultPoolMetrics metrics;
    private volatile boolean closed;

    /**
     * Creates a soft reference pool with default configuration.
     * 使用默认配置创建软引用池。
     *
     * @param factory the object factory - 对象工厂
     */
    public SoftReferencePool(PooledObjectFactory<T> factory) {
        this(factory, PoolConfig.defaults());
    }

    /**
     * Creates a soft reference pool with custom configuration.
     * 使用自定义配置创建软引用池。
     *
     * @param factory the object factory - 对象工厂
     * @param config  the pool configuration - 池配置
     */
    public SoftReferencePool(PooledObjectFactory<T> factory, PoolConfig config) {
        this.factory = factory;
        this.config = config;
        this.idleObjects = new ConcurrentLinkedQueue<>();
        this.refQueue = new ReferenceQueue<>();
        this.allObjects = new ConcurrentHashMap<>();
        this.numActive = new AtomicInteger(0);
        this.numIdle = new AtomicInteger(0);
        this.metrics = new DefaultPoolMetrics();
        this.metrics.setActiveSupplier(this::getNumActive);
        this.metrics.setIdleSupplier(this::getNumIdle);
    }

    @Override
    public T borrowObject() throws OpenPoolException {
        return borrowObject(config.maxWait());
    }

    @Override
    public T borrowObject(Duration timeout) throws OpenPoolException {
        checkClosed();
        cleanupGarbageCollected();

        // Try to get from idle queue
        SoftReference<PooledObject<T>> ref;
        while ((ref = idleObjects.poll()) != null) {
            PooledObject<T> pooledObject = ref.get();
            if (pooledObject == null) {
                numIdle.decrementAndGet();
                continue;
            }
            numIdle.decrementAndGet();
            if (pooledObject.compareAndSetState(
                    PooledObjectState.IDLE, PooledObjectState.ALLOCATED)) {
                try {
                    if (config.testOnBorrow() && !factory.validateObject(pooledObject)) {
                        destroyPooledObject(pooledObject);
                        continue;
                    }
                    factory.activateObject(pooledObject);
                    ((DefaultPooledObject<T>) pooledObject).markBorrowed();
                    numActive.incrementAndGet();
                    metrics.recordBorrow();
                    T obj = pooledObject.getObject();
                    allObjects.put(new IdentityWrapper<>(obj), pooledObject);
                    return obj;
                } catch (Exception e) {
                    destroyPooledObject(pooledObject);
                }
            }
        }

        // Create new object
        PooledObject<T> pooledObject = factory.makeObject();
        if (config.testOnCreate() && !factory.validateObject(pooledObject)) {
            throw new OpenPoolException("Validation failed on create");
        }
        pooledObject.compareAndSetState(PooledObjectState.IDLE, PooledObjectState.ALLOCATED);
        ((DefaultPooledObject<T>) pooledObject).markBorrowed();
        numActive.incrementAndGet();
        metrics.recordCreate();
        metrics.recordBorrow();
        T obj = pooledObject.getObject();
        allObjects.put(new IdentityWrapper<>(obj), pooledObject);
        return obj;
    }

    @Override
    public void returnObject(T obj) {
        if (obj == null) return;

        PooledObject<T> pooledObject = allObjects.remove(new IdentityWrapper<>(obj));
        if (pooledObject == null) {
            throw new OpenPoolException("Object not tracked by this pool");
        }

        try {
            if (config.testOnReturn() && !factory.validateObject(pooledObject)) {
                destroyPooledObject(pooledObject);
                return;
            }

            factory.passivateObject(pooledObject);
            pooledObject.compareAndSetState(PooledObjectState.ALLOCATED, PooledObjectState.IDLE);
            ((DefaultPooledObject<T>) pooledObject).markReturned();

            // Wrap in soft reference and add to queue
            idleObjects.offer(new SoftReference<>(pooledObject, refQueue));
            numIdle.incrementAndGet();

        } catch (Exception e) {
            destroyPooledObject(pooledObject);
        } finally {
            numActive.decrementAndGet();
            metrics.recordReturn();
        }
    }

    @Override
    public void invalidateObject(T obj) {
        if (obj == null) return;
        PooledObject<T> pooledObject = allObjects.remove(new IdentityWrapper<>(obj));
        if (pooledObject != null) {
            try {
                factory.destroyObject(pooledObject);
            } catch (Exception e) {
                // Ignore destroy errors to ensure pool state is updated
            }
            numActive.decrementAndGet();
            metrics.recordDestroy();
        }
    }

    @Override
    public void addObject() throws OpenPoolException {
        checkClosed();

        PooledObject<T> pooledObject = factory.makeObject();
        if (config.testOnCreate() && !factory.validateObject(pooledObject)) {
            throw new OpenPoolException("Validation failed on create");
        }
        ((DefaultPooledObject<T>) pooledObject).markReturned();
        idleObjects.offer(new SoftReference<>(pooledObject, refQueue));
        numIdle.incrementAndGet();
        metrics.recordCreate();
    }

    @Override
    public int getNumIdle() {
        cleanupGarbageCollected();
        return Math.max(0, numIdle.get());
    }

    @Override
    public int getNumActive() {
        return numActive.get();
    }

    @Override
    public void clear() {
        SoftReference<PooledObject<T>> ref;
        while ((ref = idleObjects.poll()) != null) {
            numIdle.decrementAndGet();
            PooledObject<T> pooledObject = ref.get();
            if (pooledObject != null) {
                destroyPooledObject(pooledObject);
            }
        }
    }

    @Override
    public PoolMetrics getMetrics() {
        return metrics;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        clear();
    }

    // ==================== Internal Methods ====================

    private void checkClosed() throws OpenPoolException {
        if (closed) {
            throw new OpenPoolException("Pool is closed");
        }
    }

    @SuppressWarnings("unchecked")
    private void cleanupGarbageCollected() {
        java.lang.ref.Reference<? extends PooledObject<T>> ref;
        while ((ref = refQueue.poll()) != null) {
            if (idleObjects.remove(ref)) {
                numIdle.decrementAndGet();
            }
        }
    }

    private void destroyPooledObject(PooledObject<T> pooledObject) {
        try {
            factory.destroyObject(pooledObject);
        } catch (Exception e) {
            // Ignore
        }
        metrics.recordDestroy();
    }

    private record IdentityWrapper<T>(T instance) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IdentityWrapper<?> that)) return false;
            return instance == that.instance;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(instance);
        }
    }
}
