package cloud.opencode.base.pool.impl;

import cloud.opencode.base.pool.ObjectPool;
import cloud.opencode.base.pool.PoolConfig;
import cloud.opencode.base.pool.PoolContext;
import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.PooledObjectFactory;
import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.DefaultPooledObject;
import cloud.opencode.base.pool.metrics.DefaultPoolMetrics;
import cloud.opencode.base.pool.metrics.PoolMetrics;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * VirtualThreadPool - Virtual Thread Friendly Object Pool
 * VirtualThreadPool - 虚拟线程友好的对象池
 *
 * <p>An object pool optimized for Virtual Threads with ScopedValue context propagation.</p>
 * <p>为虚拟线程优化的对象池，支持 ScopedValue 上下文传播。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Virtual Thread optimized - 虚拟线程优化</li>
 *   <li>ScopedValue context propagation - ScopedValue 上下文传播</li>
 *   <li>Async borrow operations - 异步借用操作</li>
 *   <li>StructuredTaskScope integration - StructuredTaskScope 集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * VirtualThreadPool<Connection> pool = VirtualThreadPool.create(factory, config);
 *
 * // Sync borrow
 * Connection conn = pool.borrowObject();
 * try {
 *     // use connection
 * } finally {
 *     pool.returnObject(conn);
 * }
 *
 * // Async borrow
 * CompletableFuture<Connection> future = pool.borrowAsync();
 * future.thenAccept(conn -> {
 *     // use connection
 *     pool.returnObject(conn);
 * });
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (Semaphore, ConcurrentHashMap) - 线程安全: 是（Semaphore，ConcurrentHashMap）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @param <T> the type of objects in the pool | 池中对象的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public class VirtualThreadPool<T> implements ObjectPool<T> {

    private final PooledObjectFactory<T> factory;
    private final PoolConfig config;
    private final BlockingQueue<PooledObject<T>> idleObjects;
    private final Map<IdentityWrapper<T>, PooledObject<T>> allObjects;
    private final Semaphore permits;
    private final AtomicInteger numActive;
    private final DefaultPoolMetrics metrics;
    private final ExecutorService virtualExecutor;
    private volatile boolean closed;

    /**
     * Creates a new VirtualThreadPool
     * 创建新的 VirtualThreadPool
     *
     * @param factory the object factory | 对象工厂
     * @param config  the pool config | 池配置
     */
    public VirtualThreadPool(PooledObjectFactory<T> factory, PoolConfig config) {
        this.factory = factory;
        this.config = config;
        this.idleObjects = new LinkedBlockingQueue<>(config.maxTotal());
        this.allObjects = new ConcurrentHashMap<>();
        this.permits = new Semaphore(config.maxTotal(), true);
        this.numActive = new AtomicInteger(0);
        this.metrics = new DefaultPoolMetrics();
        this.virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.closed = false;

        // Pre-create minimum objects
        initializePool();
    }

    /**
     * Creates a VirtualThreadPool with factory and config
     * 使用工厂和配置创建 VirtualThreadPool
     *
     * @param factory the object factory | 对象工厂
     * @param config  the pool config | 池配置
     * @param <T>     object type | 对象类型
     * @return new pool | 新池
     */
    public static <T> VirtualThreadPool<T> create(PooledObjectFactory<T> factory, PoolConfig config) {
        return new VirtualThreadPool<>(factory, config);
    }

    private void initializePool() {
        for (int i = 0; i < config.minIdle(); i++) {
            try {
                addObject();
            } catch (Exception e) {
                // Log but continue
                break;
            }
        }
    }

    // ==================== Borrow Operations | 借用操作 ====================

    @Override
    public T borrowObject() {
        return borrowObject(config.maxWait());
    }

    @Override
    public T borrowObject(Duration timeout) {
        checkOpen();
        long startTime = System.nanoTime();
        boolean permitAcquired = false;

        try {
            if (!permits.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw OpenPoolException.timeout("VirtualThreadPool", timeout);
            }
            permitAcquired = true;

            PooledObject<T> pooledObj = idleObjects.poll();
            if (pooledObj == null) {
                pooledObj = createObject();
            }

            if (config.testOnBorrow() && !factory.validateObject(pooledObj)) {
                destroyObject(pooledObj);
                pooledObj = createObject();
            }

            factory.activateObject(pooledObj);
            numActive.incrementAndGet();

            long elapsed = System.nanoTime() - startTime;
            metrics.recordBorrow();
            metrics.recordBorrowDuration(Duration.ofNanos(elapsed));

            final T result = pooledObj.getObject();

            // Record in context if available
            PoolContext.current().ifPresent(ctx -> ctx.recordBorrow(result));

            // Permit is now owned by the caller; released in returnObject/invalidateObject
            permitAcquired = false;
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenPoolException("Borrow interrupted", e);
        } catch (Exception e) {
            if (e instanceof OpenPoolException ope) {
                throw ope;
            }
            throw new OpenPoolException("Borrow failed", e);
        } finally {
            // Release permit only if we failed to hand it off to the caller
            if (permitAcquired) {
                permits.release();
            }
        }
    }

    /**
     * Borrows an object asynchronously
     * 异步借用对象
     *
     * @return future with borrowed object | 包含借用对象的 Future
     */
    public CompletableFuture<T> borrowAsync() {
        return CompletableFuture.supplyAsync(this::borrowObject, virtualExecutor);
    }

    /**
     * Borrows an object asynchronously with timeout
     * 带超时的异步借用对象
     *
     * @param timeout the timeout | 超时时间
     * @return future with borrowed object | 包含借用对象的 Future
     */
    public CompletableFuture<T> borrowAsync(Duration timeout) {
        return CompletableFuture.supplyAsync(() -> borrowObject(timeout), virtualExecutor);
    }

    private PooledObject<T> createObject() throws Exception {
        if (allObjects.size() >= config.maxTotal()) {
            throw OpenPoolException.exhausted("VirtualThreadPool");
        }
        PooledObject<T> pooledObj = factory.makeObject();
        allObjects.put(new IdentityWrapper<>(pooledObj.getObject()), pooledObj);
        metrics.recordCreate();
        return pooledObj;
    }

    // ==================== Return Operations | 归还操作 ====================

    @Override
    public void returnObject(T obj) {
        if (obj == null) {
            return;
        }

        PooledObject<T> pooledObj = allObjects.get(new IdentityWrapper<>(obj));
        if (pooledObj == null) {
            return;
        }

        try {
            numActive.decrementAndGet();

            if (closed || (config.testOnReturn() && !factory.validateObject(pooledObj))) {
                destroyObject(pooledObj);
                return;
            }

            factory.passivateObject(pooledObj);

            if (!idleObjects.offer(pooledObj)) {
                destroyObject(pooledObj);
            }

            // Record in context if available
            PoolContext.current().ifPresent(PoolContext::recordReturn);

            metrics.recordReturn();
        } finally {
            permits.release();
        }
    }

    @Override
    public void invalidateObject(T obj) {
        if (obj == null) {
            return;
        }

        PooledObject<T> pooledObj = allObjects.get(new IdentityWrapper<>(obj));
        if (pooledObj == null) {
            return;
        }

        try {
            numActive.decrementAndGet();
            destroyObject(pooledObj);
        } finally {
            permits.release();
        }
    }

    private void destroyObject(PooledObject<T> pooledObj) {
        try {
            allObjects.remove(new IdentityWrapper<>(pooledObj.getObject()));
            factory.destroyObject(pooledObj);
            metrics.recordDestroy();
        } catch (Exception ignored) {
        }
    }

    // ==================== Execute Pattern | 执行模式 ====================

    @Override
    public <R> R execute(Function<T, R> action) {
        T obj = borrowObject();
        try {
            return action.apply(obj);
        } finally {
            returnObject(obj);
        }
    }

    @Override
    public void execute(Consumer<T> action) {
        T obj = borrowObject();
        try {
            action.accept(obj);
        } finally {
            returnObject(obj);
        }
    }

    /**
     * Executes action asynchronously
     * 异步执行操作
     *
     * @param action the action | 操作
     * @param <R>    return type | 返回类型
     * @return future with result | 包含结果的 Future
     */
    public <R> CompletableFuture<R> executeAsync(Function<T, R> action) {
        return borrowAsync().thenApply(obj -> {
            try {
                return action.apply(obj);
            } finally {
                returnObject(obj);
            }
        });
    }

    // ==================== Pool Info | 池信息 ====================

    @Override
    public void addObject() {
        checkOpen();
        try {
            PooledObject<T> pooledObj = createObject();
            factory.activateObject(pooledObj);
            factory.passivateObject(pooledObj);
            if (!idleObjects.offer(pooledObj)) {
                destroyObject(pooledObj);
            }
        } catch (Exception e) {
            throw OpenPoolException.createFailed("VirtualThreadPool", e);
        }
    }

    @Override
    public int getNumIdle() {
        return idleObjects.size();
    }

    @Override
    public int getNumActive() {
        return numActive.get();
    }

    @Override
    public void clear() {
        PooledObject<T> pooledObj;
        while ((pooledObj = idleObjects.poll()) != null) {
            destroyObject(pooledObj);
        }
    }

    @Override
    public PoolMetrics getMetrics() {
        return metrics;
    }

    /**
     * Gets total created count
     * 获取创建的总数
     *
     * @return total count | 总数
     */
    public int size() {
        return allObjects.size();
    }

    /**
     * Gets available count
     * 获取可用数量
     *
     * @return available count | 可用数量
     */
    public int available() {
        return permits.availablePermits();
    }

    /**
     * Checks if running on virtual thread
     * 检查是否运行在虚拟线程上
     *
     * @return true if virtual thread | 如果是虚拟线程返回 true
     */
    public boolean isVirtualThread() {
        return Thread.currentThread().isVirtual();
    }

    // ==================== Lifecycle | 生命周期 ====================

    private void checkOpen() {
        if (closed) {
            throw OpenPoolException.closed("VirtualThreadPool");
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        virtualExecutor.shutdown();
        try {
            if (!virtualExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                virtualExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            virtualExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        clear();

        // Destroy all remaining objects (including any still marked as active)
        for (PooledObject<T> obj : allObjects.values()) {
            try {
                factory.destroyObject(obj);
            } catch (Exception ignored) {
                // Best effort destruction during shutdown
            }
        }
        allObjects.clear();
    }

}
