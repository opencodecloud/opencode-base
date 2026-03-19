package cloud.opencode.base.pool;

import java.lang.ScopedValue.CallableOp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pool Context - ScopedValue-based Pool Context
 * 池上下文 - 基于 ScopedValue 的池上下文
 *
 * <p>Uses JDK 25 ScopedValue for efficient context propagation in Virtual Threads.</p>
 * <p>使用 JDK 25 ScopedValue 实现虚拟线程中高效的上下文传播。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ScopedValue-based context - 基于 ScopedValue 的上下文</li>
 *   <li>Virtual Thread friendly - 虚拟线程友好</li>
 *   <li>Borrow tracking - 借用追踪</li>
 *   <li>Context attributes - 上下文属性</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PoolContext context = PoolContext.create("connectionPool");
 * PoolContext.run(context, () -> {
 *     // Access current context
 *     PoolContext current = PoolContext.current().orElseThrow();
 *     System.out.println("Pool: " + current.poolName());
 *
 *     // Do work with pooled objects
 *     return result;
 * });
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Partially (ConcurrentHashMap attributes, volatile fields) - 线程安全: 部分（ConcurrentHashMap属性，volatile字段）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public final class PoolContext {

    /**
     * Current pool context (ScopedValue)
     * 当前池上下文 (ScopedValue)
     */
    public static final ScopedValue<PoolContext> CURRENT = ScopedValue.newInstance();

    private final String poolName;
    private final Instant createdAt;
    private final Thread thread;
    private final Map<String, Object> attributes;
    private volatile Instant borrowTime;
    private volatile Instant returnTime;
    private volatile Object borrowedObject;

    private PoolContext(String poolName) {
        this.poolName = poolName;
        this.createdAt = Instant.now();
        this.thread = Thread.currentThread();
        this.attributes = new ConcurrentHashMap<>();
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a new pool context
     * 创建新的池上下文
     *
     * @param poolName the pool name | 池名称
     * @return new context | 新上下文
     */
    public static PoolContext create(String poolName) {
        return new PoolContext(poolName);
    }

    /**
     * Creates a new pool context with default name
     * 使用默认名称创建新的池上下文
     *
     * @return new context | 新上下文
     */
    public static PoolContext create() {
        return new PoolContext("default");
    }

    // ==================== Context Execution | 上下文执行 ====================

    /**
     * Runs a callable within this context
     * 在此上下文中运行可调用对象
     *
     * @param context the pool context | 池上下文
     * @param task    the task to run | 要运行的任务
     * @param <T>     return type | 返回类型
     * @param <X>     exception type | 异常类型
     * @return task result | 任务结果
     * @throws X if task throws | 如果任务抛出异常
     */
    public static <T, X extends Throwable> T run(PoolContext context, CallableOp<T, X> task) throws X {
        return ScopedValue.where(CURRENT, context).call(task);
    }

    /**
     * Runs a runnable within this context
     * 在此上下文中运行可运行对象
     *
     * @param context the pool context | 池上下文
     * @param task    the task to run | 要运行的任务
     */
    public static void run(PoolContext context, Runnable task) {
        ScopedValue.where(CURRENT, context).run(task);
    }

    // ==================== Current Context | 当前上下文 ====================

    /**
     * Gets the current pool context
     * 获取当前池上下文
     *
     * @return current context or empty | 当前上下文或空
     */
    public static Optional<PoolContext> current() {
        return CURRENT.isBound() ? Optional.of(CURRENT.get()) : Optional.empty();
    }

    /**
     * Gets the current context or creates a new one
     * 获取当前上下文或创建新的
     *
     * @return current or new context | 当前或新的上下文
     */
    public static PoolContext currentOrCreate() {
        return current().orElseGet(PoolContext::create);
    }

    // ==================== Borrow Tracking | 借用追踪 ====================

    /**
     * Records object borrow
     * 记录对象借用
     *
     * @param object the borrowed object | 借用的对象
     */
    public void recordBorrow(Object object) {
        this.borrowTime = Instant.now();
        this.borrowedObject = object;
        this.returnTime = null;
    }

    /**
     * Records object return
     * 记录对象归还
     */
    public void recordReturn() {
        this.returnTime = Instant.now();
        this.borrowedObject = null;
    }

    /**
     * Checks if an object is currently borrowed
     * 检查是否有对象当前被借用
     *
     * @return true if borrowed | 如果已借用返回 true
     */
    public boolean hasBorrowedObject() {
        return borrowedObject != null;
    }

    // ==================== Attributes | 属性 ====================

    /**
     * Sets an attribute
     * 设置属性
     *
     * @param key   attribute key | 属性键
     * @param value attribute value | 属性值
     * @return this context | 此上下文
     */
    public PoolContext setAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    /**
     * Gets an attribute
     * 获取属性
     *
     * @param key attribute key | 属性键
     * @param <T> value type | 值类型
     * @return attribute value or empty | 属性值或空
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAttribute(String key) {
        return Optional.ofNullable((T) attributes.get(key));
    }

    /**
     * Checks if attribute exists
     * 检查属性是否存在
     *
     * @param key attribute key | 属性键
     * @return true if exists | 如果存在返回 true
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    // ==================== Getters ====================

    /**
     * Gets the pool name
     * 获取池名称
     *
     * @return pool name | 池名称
     */
    public String poolName() {
        return poolName;
    }

    /**
     * Gets creation time
     * 获取创建时间
     *
     * @return creation time | 创建时间
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Gets the thread
     * 获取线程
     *
     * @return thread | 线程
     */
    public Thread thread() {
        return thread;
    }

    /**
     * Gets borrow time
     * 获取借用时间
     *
     * @return borrow time or empty | 借用时间或空
     */
    public Optional<Instant> borrowTime() {
        return Optional.ofNullable(borrowTime);
    }

    /**
     * Gets return time
     * 获取归还时间
     *
     * @return return time or empty | 归还时间或空
     */
    public Optional<Instant> returnTime() {
        return Optional.ofNullable(returnTime);
    }

    /**
     * Checks if on virtual thread
     * 检查是否在虚拟线程上
     *
     * @return true if virtual thread | 如果是虚拟线程返回 true
     */
    public boolean isVirtualThread() {
        return thread.isVirtual();
    }

    @Override
    public String toString() {
        return "PoolContext[poolName=" + poolName + ", thread=" + thread.getName() +
                ", virtual=" + thread.isVirtual() + ", hasBorrowed=" + hasBorrowedObject() + "]";
    }
}
