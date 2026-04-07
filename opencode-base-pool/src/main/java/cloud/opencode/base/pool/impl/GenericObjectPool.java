package cloud.opencode.base.pool.impl;

import cloud.opencode.base.pool.ObjectPool;
import cloud.opencode.base.pool.PoolConfig;
import cloud.opencode.base.pool.PoolEventListener;
import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.PooledObjectFactory;
import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.DefaultPooledObject;
import cloud.opencode.base.pool.factory.PooledObjectState;
import cloud.opencode.base.pool.metrics.DefaultPoolMetrics;
import cloud.opencode.base.pool.metrics.PoolMetrics;
import cloud.opencode.base.pool.policy.WaitPolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GenericObjectPool - Generic Object Pool Implementation
 * GenericObjectPool - 通用对象池实现
 *
 * <p>High-performance general-purpose object pool with configurable size,
 * eviction, validation, and Virtual Thread support.</p>
 * <p>高性能通用对象池，支持可配置的大小、驱逐、验证和虚拟线程。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Semaphore-based concurrency control - 基于信号量的并发控制</li>
 *   <li>ConcurrentLinkedDeque for idle objects - ConcurrentLinkedDeque存储空闲对象</li>
 *   <li>Scheduled eviction with Virtual Threads - 使用虚拟线程的计划驱逐</li>
 *   <li>Lock-free metrics collection - 无锁指标收集</li>
 *   <li>Configurable validation points - 可配置的验证点</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PooledObjectFactory<Connection> factory = new BasePooledObjectFactory<>() {
 *     @Override
 *     protected Connection create() {
 *         return DriverManager.getConnection(url);
 *     }
 * };
 *
 * GenericObjectPool<Connection> pool = new GenericObjectPool<>(factory,
 *     PoolConfig.builder()
 *         .maxTotal(20)
 *         .testOnBorrow(true)
 *         .build());
 *
 * Connection conn = pool.borrowObject();
 * try {
 *     // use connection
 * } finally {
 *     pool.returnObject(conn);
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Borrow: O(1) when idle available - 借用: O(1) 当有空闲时</li>
 *   <li>Return: O(1) - 归还: O(1)</li>
 *   <li>Virtual Thread friendly (Semaphore) - 虚拟线程友好(信号量)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @param <T> the type of object being pooled - 池化对象类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public class GenericObjectPool<T> implements ObjectPool<T> {

    private static final System.Logger logger = System.getLogger(GenericObjectPool.class.getName());

    private final PooledObjectFactory<T> factory;
    private final PoolConfig config;
    private final Deque<PooledObject<T>> idleObjects;
    private final Map<IdentityWrapper<T>, PooledObject<T>> allObjects;
    private final AtomicInteger numActive;
    private final AtomicInteger numIdle;
    private final Semaphore permits;
    private final DefaultPoolMetrics metrics;
    private final ScheduledExecutorService evictionExecutor;
    private final PoolEventListener<T> eventListener;
    private final java.util.concurrent.atomic.AtomicBoolean closed = new java.util.concurrent.atomic.AtomicBoolean(false);

    /**
     * Creates a pool with default configuration.
     * 使用默认配置创建池。
     *
     * @param factory the object factory - 对象工厂
     */
    public GenericObjectPool(PooledObjectFactory<T> factory) {
        this(factory, PoolConfig.defaults());
    }

    /**
     * Creates a pool with custom configuration.
     * 使用自定义配置创建池。
     *
     * @param factory the object factory - 对象工厂
     * @param config  the pool configuration - 池配置
     */
    @SuppressWarnings("unchecked")
    public GenericObjectPool(PooledObjectFactory<T> factory, PoolConfig config) {
        this.factory = java.util.Objects.requireNonNull(factory, "factory cannot be null");
        this.config = java.util.Objects.requireNonNull(config, "config cannot be null");
        this.idleObjects = new ConcurrentLinkedDeque<>();
        this.allObjects = new ConcurrentHashMap<>();
        this.numActive = new AtomicInteger(0);
        this.numIdle = new AtomicInteger(0);
        this.permits = new Semaphore(config.maxTotal(), true);
        this.metrics = new DefaultPoolMetrics();
        this.metrics.setActiveSupplier(this::getNumActive);
        this.metrics.setIdleSupplier(this::getNumIdle);
        this.eventListener = (PoolEventListener<T>) config.eventListener();

        // Start eviction task if configured
        ScheduledExecutorService evictionExec = null;
        if (config.isEvictionEnabled()) {
            evictionExec = Executors.newSingleThreadScheduledExecutor(
                    Thread.ofVirtual().name("pool-evictor").factory()
            );
        }

        try {
            if (evictionExec != null) {
                this.evictionExecutor = evictionExec;
                scheduleEviction();
            } else {
                this.evictionExecutor = null;
            }

            // Pre-create minimum idle objects | 预创建最小空闲对象
            for (int i = 0; i < config.minIdle(); i++) {
                try {
                    addObject();
                } catch (OpenPoolException e) {
                    break;
                }
            }
        } catch (RuntimeException | Error ex) {
            // Shut down the eviction executor to prevent leak | 关闭驱逐执行器以防止泄漏
            if (evictionExec != null) {
                evictionExec.shutdownNow();
            }
            throw ex;
        }
    }

    @Override
    public T borrowObject() throws OpenPoolException {
        return borrowObject(config.maxWait());
    }

    @Override
    public T borrowObject(Duration timeout) throws OpenPoolException {
        checkClosed();

        long startNanos = System.nanoTime();
        long waitNanos = timeout.toNanos();

        // Acquire permit
        if (!tryAcquirePermit(timeout)) {
            fireOnExhausted();
            // Only fire onTimeout when blocking wait was attempted (not FAIL/instant rejection)
            if (config.blockWhenExhausted()) {
                Duration waited = Duration.ofNanos(System.nanoTime() - startNanos);
                fireOnTimeout(waited);
            }
            throw new OpenPoolException("Timeout waiting for available object");
        }

        try {
            // Loop to retry validation failures without recursive call (avoids semaphore leak)
            while (true) {
                // Try to get from idle queue
                PooledObject<T> pooledObject = pollIdle();

                // Check max object lifetime for idle objects
                if (pooledObject != null && isExpired(pooledObject)) {
                    invalidateInternal(pooledObject);
                    pooledObject = null;
                }

                if (pooledObject == null) {
                    // Create new object
                    pooledObject = createObject();
                }

                // Validate and activate
                if (validateAndActivate(pooledObject)) {
                    long borrowNanos = System.nanoTime();
                    ((DefaultPooledObject<T>) pooledObject).markBorrowed(borrowNanos);
                    numActive.incrementAndGet();
                    metrics.recordBorrow();
                    metrics.recordWaitNanos(borrowNanos - startNanos);
                    T obj = pooledObject.getObject();
                    fireOnBorrow(obj);
                    return obj;
                }

                // Validation failed, check remaining time
                long elapsed = System.nanoTime() - startNanos;
                long remaining = waitNanos - elapsed;
                if (remaining <= 0) {
                    throw new OpenPoolException("Failed to validate object");
                }
                // Continue loop to retry with same permit
            }

        } catch (Exception e) {
            permits.release();
            throw e instanceof OpenPoolException pe ? pe :
                    new OpenPoolException("Failed to borrow object", e);
        }
    }

    @Override
    public void returnObject(T obj) {
        if (obj == null) return;

        PooledObject<T> pooledObject = allObjects.get(new IdentityWrapper<>(obj));
        if (pooledObject == null) {
            throw OpenPoolException.invalidState("Object not from this pool");
        }

        // Set state to returning
        if (!pooledObject.compareAndSetState(
                PooledObjectState.ALLOCATED, PooledObjectState.RETURNING)) {
            // CAS failed: object not in ALLOCATED state, still release permit
            permits.release();
            return;
        }

        numActive.decrementAndGet();

        boolean returned = false;
        try {
            // Validate on return
            if (config.testOnReturn() && !factory.validateObject(pooledObject)) {
                invalidateInternal(pooledObject);
                return;
            }

            // Passivate
            factory.passivateObject(pooledObject);

            // Return to idle queue
            if (!pooledObject.compareAndSetState(
                    PooledObjectState.RETURNING, PooledObjectState.IDLE)) {
                invalidateInternal(pooledObject);
                return;
            }

            ((DefaultPooledObject<T>) pooledObject).markReturned();

            // Check max idle
            if (numIdle.get() >= config.maxIdle()) {
                invalidateInternal(pooledObject);
            } else {
                if (config.lifo()) {
                    idleObjects.addFirst(pooledObject);
                } else {
                    idleObjects.addLast(pooledObject);
                }
                numIdle.incrementAndGet();
                returned = true;
            }

        } catch (Exception e) {
            invalidateInternal(pooledObject);
        } finally {
            permits.release();
            metrics.recordReturn();
            if (returned) {
                fireOnReturn(obj);
            }
        }
    }

    @Override
    public void invalidateObject(T obj) {
        PooledObject<T> pooledObject = allObjects.get(new IdentityWrapper<>(obj));
        if (pooledObject != null) {
            boolean wasActive = pooledObject.compareAndSetState(
                    PooledObjectState.ALLOCATED, PooledObjectState.INVALID);
            invalidateInternal(pooledObject);
            if (wasActive) {
                numActive.decrementAndGet();
                permits.release();
            }
        }
    }

    @Override
    public void addObject() throws OpenPoolException {
        checkClosed();

        // Check total object count without acquiring a permit.
        // Permits track active (borrowed) objects only; idle objects do not hold permits.
        // Acquiring a permit here would cause double-counting when the idle object
        // is later borrowed (borrowObject also acquires a permit), prematurely
        // exhausting pool capacity.
        if (allObjects.size() >= config.maxTotal()) {
            throw new OpenPoolException("Pool exhausted");
        }

        try {
            PooledObject<T> pooledObject = createObject();
            pooledObject.compareAndSetState(
                    PooledObjectState.ALLOCATED, PooledObjectState.IDLE);
            ((DefaultPooledObject<T>) pooledObject).markReturned();
            idleObjects.addLast(pooledObject);
            numIdle.incrementAndGet();
        } catch (Exception e) {
            throw e instanceof OpenPoolException pe ? pe :
                    new OpenPoolException("Failed to add object", e);
        }
    }

    @Override
    public int getNumIdle() {
        return Math.max(0, numIdle.get());
    }

    @Override
    public int getNumActive() {
        return numActive.get();
    }

    @Override
    public void clear() {
        PooledObject<T> pooledObject;
        while ((pooledObject = idleObjects.poll()) != null) {
            numIdle.decrementAndGet();
            invalidateInternal(pooledObject);
            // No permit release needed: idle objects do not hold permits.
            // Permits track only active (borrowed) objects.
        }
    }

    @Override
    public PoolMetrics getMetrics() {
        return metrics;
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;

        if (evictionExecutor != null) {
            evictionExecutor.shutdown();
            try {
                if (!evictionExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    evictionExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                evictionExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        clear();

        // Destroy all objects | 销毁所有对象
        for (PooledObject<T> obj : allObjects.values()) {
            try {
                factory.destroyObject(obj);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING, "Failed to destroy pooled object during pool close", e);
            }
        }
        allObjects.clear();
    }

    // ==================== Internal Methods ====================

    private void checkClosed() throws OpenPoolException {
        if (closed.get()) {
            throw new OpenPoolException("Pool is closed");
        }
    }

    private boolean tryAcquirePermit(Duration timeout) {
        try {
            if (config.blockWhenExhausted()) {
                return permits.tryAcquire(timeout.toNanos(), TimeUnit.NANOSECONDS);
            } else if (config.waitPolicy() == WaitPolicy.GROW) {
                return true; // Allow growth beyond max
            } else {
                return permits.tryAcquire();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private PooledObject<T> pollIdle() {
        PooledObject<T> obj = config.lifo() ? idleObjects.pollFirst() : idleObjects.pollLast();
        if (obj != null) {
            numIdle.decrementAndGet();
        }
        return obj;
    }

    private PooledObject<T> createObject() throws OpenPoolException {
        PooledObject<T> pooledObject = factory.makeObject();
        if (config.testOnCreate() && !factory.validateObject(pooledObject)) {
            throw new OpenPoolException("Validation failed on create");
        }
        allObjects.put(new IdentityWrapper<>(pooledObject.getObject()), pooledObject);
        metrics.recordCreate();
        fireOnCreate(pooledObject.getObject());
        return pooledObject;
    }

    private boolean validateAndActivate(PooledObject<T> pooledObject)
            throws OpenPoolException {
        if (!pooledObject.compareAndSetState(
                PooledObjectState.IDLE, PooledObjectState.ALLOCATED)) {
            return false;
        }

        try {
            if (config.testOnBorrow() && !factory.validateObject(pooledObject)) {
                invalidateInternal(pooledObject);
                return false;
            }

            factory.activateObject(pooledObject);
            return true;

        } catch (Exception e) {
            invalidateInternal(pooledObject);
            throw e instanceof OpenPoolException pe ? pe :
                    new OpenPoolException("Failed to activate object", e);
        }
    }

    private void invalidateInternal(PooledObject<T> pooledObject) {
        pooledObject.compareAndSetState(pooledObject.getState(), PooledObjectState.INVALID);
        fireOnDestroy(pooledObject.getObject());
        try {
            factory.destroyObject(pooledObject);
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING, "Failed to destroy pooled object during invalidation", e);
        }
        allObjects.remove(new IdentityWrapper<>(pooledObject.getObject()));
        metrics.recordDestroy();
    }

    private void scheduleEviction() {
        evictionExecutor.scheduleWithFixedDelay(
                this::evict,
                config.timeBetweenEvictionRuns().toMillis(),
                config.timeBetweenEvictionRuns().toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    private void evict() {
        if (closed.get()) return;

        int numTests = Math.min(config.numTestsPerEvictionRun(), numIdle.get());

        for (int i = 0; i < numTests; i++) {
            PooledObject<T> pooledObject = idleObjects.pollFirst();
            if (pooledObject == null) break;
            numIdle.decrementAndGet();

            boolean evict = false;

            // Check idle time
            if (pooledObject.getIdleDuration().compareTo(
                    config.minEvictableIdleTime()) > 0) {
                evict = true;
            }

            // Check validation - catch exceptions to prevent crashing the eviction thread
            if (!evict && config.testWhileIdle()) {
                try {
                    if (!factory.validateObject(pooledObject)) {
                        evict = true;
                    }
                } catch (Exception e) {
                    logger.log(System.Logger.Level.WARNING, "Exception during eviction validation, evicting object", e);
                    evict = true;
                }
            }

            // Check max object lifetime
            if (!evict && isExpired(pooledObject)) {
                evict = true;
            }

            if (evict) {
                fireOnEvict(pooledObject.getObject());
                invalidateInternal(pooledObject);
                // No permit release needed: idle objects do not hold permits.
                // Permits track only active (borrowed) objects.
            } else {
                // Return to queue tail
                idleObjects.addLast(pooledObject);
                numIdle.incrementAndGet();
            }
        }

        // Ensure minimum idle
        while (numIdle.get() < config.minIdle() && !closed.get()) {
            try {
                addObject();
            } catch (OpenPoolException e) {
                logger.log(System.Logger.Level.WARNING, "Failed to add object during eviction to maintain minIdle", e);
                break;
            }
        }
    }

    // ==================== Lifetime Check ====================

    private boolean isExpired(PooledObject<T> pooledObject) {
        if (!config.isLifetimeEnabled()) {
            return false;
        }
        // Avoid Instant/Duration allocation: compare epoch millis directly
        long ageMillis = System.currentTimeMillis() - pooledObject.getCreateInstant().toEpochMilli();
        return ageMillis > config.maxObjectLifetime().toMillis();
    }

    // ==================== Event Listener Helpers ====================

    private void fireOnBorrow(T object) {
        if (eventListener != null) {
            try {
                eventListener.onBorrow(object);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING, "Exception in pool event listener onBorrow", e);
            }
        }
    }

    private void fireOnReturn(T object) {
        if (eventListener != null) {
            try {
                eventListener.onReturn(object);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING, "Exception in pool event listener onReturn", e);
            }
        }
    }

    private void fireOnCreate(T object) {
        if (eventListener != null) {
            try {
                eventListener.onCreate(object);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING, "Exception in pool event listener onCreate", e);
            }
        }
    }

    private void fireOnDestroy(T object) {
        if (eventListener != null) {
            try {
                eventListener.onDestroy(object);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING, "Exception in pool event listener onDestroy", e);
            }
        }
    }

    private void fireOnEvict(T object) {
        if (eventListener != null) {
            try {
                eventListener.onEvict(object);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING, "Exception in pool event listener onEvict", e);
            }
        }
    }

    private void fireOnExhausted() {
        if (eventListener != null) {
            try {
                eventListener.onExhausted();
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING, "Exception in pool event listener onExhausted", e);
            }
        }
    }

    private void fireOnTimeout(Duration waitDuration) {
        if (eventListener != null) {
            try {
                eventListener.onTimeout(waitDuration);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING, "Exception in pool event listener onTimeout", e);
            }
        }
    }
}
