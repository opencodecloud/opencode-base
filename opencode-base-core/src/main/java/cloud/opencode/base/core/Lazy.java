package cloud.opencode.base.core;

import cloud.opencode.base.core.annotation.Experimental;
import cloud.opencode.base.core.func.CheckedSupplier;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Lazy - Virtual-thread-safe lazy evaluation container using VarHandle CAS
 * Lazy - 使用 VarHandle CAS 的虚拟线程安全惰性求值容器
 *
 * <p>A container that delays computation until the value is actually needed.
 * The computation is performed at most once, and the result is cached.
 * Uses VarHandle-based CAS spinning instead of synchronized/ReentrantLock,
 * making it safe for virtual threads (no pinning).</p>
 * <p>延迟计算直到实际需要值的容器。计算最多执行一次，结果会被缓存。
 * 使用基于 VarHandle 的 CAS 自旋代替 synchronized/ReentrantLock，
 * 对虚拟线程安全（不会导致线程固定）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deferred computation - 延迟计算</li>
 *   <li>Single evaluation (memoization) - 单次求值（记忆化）</li>
 *   <li>Virtual-thread safe (VarHandle CAS, no locking) - 虚拟线程安全（VarHandle CAS，无锁）</li>
 *   <li>Monadic operations (map, flatMap, filter) - Monad 操作</li>
 *   <li>Spin wait with backoff (onSpinWait + parkNanos) - 带退避的自旋等待</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Deferred computation
 * Lazy<ExpensiveObject> lazy = Lazy.of(() -> createExpensiveObject());
 *
 * // Value not computed yet
 * assertFalse(lazy.isEvaluated());
 *
 * // Computed on first access
 * ExpensiveObject obj = lazy.get();
 * assertTrue(lazy.isEvaluated());
 *
 * // Cached on subsequent access
 * ExpensiveObject same = lazy.get();  // No recomputation
 *
 * // Chained lazy operations
 * Lazy<String> result = Lazy.of(() -> fetchData())
 *     .map(this::process)
 *     .map(this::format);
 * // Nothing computed until result.get()
 *
 * // With checked supplier
 * Lazy<String> fromFile = Lazy.of((CheckedSupplier<String>) () -> Files.readString(path));
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>First get(): O(computation time) - 首次 get(): O(计算时间)</li>
 *   <li>Subsequent get(): O(1) volatile read - 后续 get(): O(1) volatile 读取</li>
 *   <li>Memory: Supplier reference is kept for {@link #reset()} support; Lazy itself should be discarded post-evaluation if memory reclamation is needed - 内存: supplier 引用保留以支持 reset()；如需回收内存请在求值后丢弃 Lazy 实例</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (VarHandle CAS) - 线程安全: 是 (VarHandle CAS)</li>
 *   <li>Null-safe: Allows null results - 空值安全: 允许 null 结果</li>
 * </ul>
 *
 * @param <T> value type - 值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Suppliers
 * @since JDK 25, opencode-base-core V1.0.3
 */
public final class Lazy<T> implements Supplier<T> {

    /**
     * Sentinel object indicating that the value has not been computed yet.
     * 表示值尚未计算的哨兵对象。
     */
    private static final Object UNINITIALIZED = new Object();

    /**
     * Wrapper for memoized supplier exceptions. When the supplier throws,
     * the exception is stored so subsequent callers get a consistent failure
     * instead of re-invoking the supplier.
     * 供应商异常的包装器。当供应商抛出时，存储异常以便后续调用者得到一致的失败结果。
     */
    private record FailedComputation(RuntimeException exception) {}

    /**
     * Spin limit before falling back to parkNanos.
     * 回退到 parkNanos 之前的自旋限制。
     */
    private static final int SPIN_LIMIT = 64;

    /**
     * Park duration in nanoseconds for backoff.
     * 退避的 park 持续时间（纳秒）。
     */
    private static final long PARK_NANOS = 1000L;

    // VarHandles for lock-free CAS operations
    private static final VarHandle VALUE;
    private static final VarHandle LOCK;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            VALUE = lookup.findVarHandle(Lazy.class, "value", Object.class);
            LOCK = lookup.findVarHandle(Lazy.class, "lock", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unused") // accessed via VarHandle
    private volatile Object value;

    @SuppressWarnings("unused") // accessed via VarHandle
    private volatile int lock;

    private Supplier<T> supplier;
    private final boolean preEvaluated;

    @SuppressWarnings("unchecked")
    private Lazy(Supplier<T> supplier, Object initialValue, boolean preEvaluated) {
        this.supplier = supplier;
        this.value = initialValue;
        this.lock = 0;
        this.preEvaluated = preEvaluated;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a Lazy from a Supplier
     * 从 Supplier 创建 Lazy
     *
     * @param supplier value supplier (evaluated lazily) - 值供应商（惰性求值）
     * @param <T>      value type - 值类型
     * @return Lazy container
     * @throws NullPointerException if supplier is null
     */
    public static <T> Lazy<T> of(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return new Lazy<>(supplier, UNINITIALIZED, false);
    }

    /**
     * Create a Lazy from a CheckedSupplier. Checked exceptions are wrapped in OpenException.
     * 从 CheckedSupplier 创建 Lazy。受检异常包装为 OpenException。
     *
     * @param supplier checked value supplier (evaluated lazily) - 受检值供应商（惰性求值）
     * @param <T>      value type - 值类型
     * @return Lazy container
     * @throws NullPointerException if supplier is null
     */
    public static <T> Lazy<T> of(CheckedSupplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return new Lazy<>(supplier.unchecked(), UNINITIALIZED, false);
    }

    /**
     * Create an already-evaluated Lazy with a pre-computed value
     * 创建已求值的 Lazy，包含预计算的值
     *
     * @param value the pre-computed value - 预计算的值
     * @param <T>   value type - 值类型
     * @return already-evaluated Lazy
     */
    public static <T> Lazy<T> value(T value) {
        return new Lazy<>(null, value, true);
    }

    // ==================== Value Access | 值访问 ====================

    /**
     * Get the value, computing if necessary. Thread-safe via VarHandle CAS with spin-wait backoff.
     * If the supplier throws, the exception is memoized and re-thrown on subsequent calls.
     * 获取值，必要时进行计算。通过 VarHandle CAS 和自旋等待退避保证线程安全。
     * 如果供应商抛出异常，异常会被缓存并在后续调用时重新抛出。
     *
     * @return the computed value - 计算后的值
     * @throws RuntimeException if the supplier threw (memoized) - 如果供应商抛出异常（已缓存）
     */
    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        // Fast path: volatile read
        Object v = VALUE.getVolatile(this);
        if (v != UNINITIALIZED) {
            if (v instanceof FailedComputation fc) {
                throw fc.exception();
            }
            return (T) v;
        }

        // Slow path: CAS acquire lock
        if (LOCK.compareAndSet(this, 0, 1)) {
            try {
                // Double-check after acquiring lock
                v = VALUE.getVolatile(this);
                if (v != UNINITIALIZED) {
                    if (v instanceof FailedComputation fc) {
                        throw fc.exception();
                    }
                    return (T) v;
                }
                T computed = supplier.get();
                VALUE.setRelease(this, computed);
                return computed;
            } catch (RuntimeException e) {
                VALUE.setRelease(this, new FailedComputation(e));
                throw e;
            } finally {
                LOCK.setRelease(this, 0);
            }
        }

        // Another thread is computing: spin wait with backoff
        for (long i = 0; ; i++) {
            v = VALUE.getVolatile(this);
            if (v != UNINITIALIZED) {
                if (v instanceof FailedComputation fc) {
                    throw fc.exception();
                }
                return (T) v;
            }
            if (i < SPIN_LIMIT) {
                Thread.onSpinWait();
            } else {
                LockSupport.parkNanos(PARK_NANOS);
            }
        }
    }

    /**
     * Check if the value has been evaluated
     * 检查值是否已被求值
     *
     * @return true if evaluated - 如果已求值返回 true
     */
    public boolean isEvaluated() {
        Object v = VALUE.getVolatile(this);
        return v != UNINITIALIZED && !(v instanceof FailedComputation);
    }

    // ==================== Transformations | 转换 ====================

    /**
     * Transform the value lazily. The mapper is not applied until get() is called on the result.
     * 惰性转换值。映射函数在结果的 get() 被调用前不会执行。
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return lazy transformed value
     */
    public <U> Lazy<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        Supplier<U> s = () -> mapper.apply(get());
        return Lazy.of(s);
    }

    /**
     * Transform to another Lazy lazily
     * 惰性转换为另一个 Lazy
     *
     * @param mapper transformation function returning Lazy - 返回 Lazy 的转换函数
     * @param <U>    result type - 结果类型
     * @return lazy result
     */
    public <U> Lazy<U> flatMap(Function<? super T, Lazy<U>> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        Supplier<U> s = () -> mapper.apply(get()).get();
        return Lazy.of(s);
    }

    /**
     * Filter the value lazily. If the predicate is not satisfied, get() throws NoSuchElementException.
     * 惰性过滤值。如果谓词不满足，get() 抛出 NoSuchElementException。
     *
     * @param predicate filter condition - 过滤条件
     * @return filtered Lazy
     */
    public Lazy<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        Supplier<T> s = () -> {
            T val = get();
            if (predicate.test(val)) {
                return val;
            }
            throw new NoSuchElementException("Lazy.filter: predicate not satisfied");
        };
        return Lazy.of(s);
    }

    /**
     * Get value or default if evaluation throws
     * 获取值或默认值（如果求值抛出异常）
     *
     * @param defaultValue default value on error - 错误时的默认值
     * @return value or default
     */
    public T getOrElse(T defaultValue) {
        try {
            return get();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get value or compute default if evaluation throws
     * 获取值或计算默认值（如果求值抛出异常）
     *
     * @param supplier default value supplier - 默认值供应商
     * @return value or computed default
     */
    public T getOrElse(Supplier<? extends T> supplier) {
        try {
            return get();
        } catch (Exception e) {
            return supplier.get();
        }
    }

    /**
     * Convert to Optional. Returns Optional.of(value) if evaluated and non-null,
     * Optional.empty() if evaluation throws or returns null.
     * 转换为 Optional。如果已求值且非 null 返回 Optional.of(value)，
     * 如果求值抛出异常或返回 null 则返回 Optional.empty()。
     *
     * @return Optional containing the value
     */
    public Optional<T> toOptional() {
        try {
            return Optional.ofNullable(get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ==================== Experimental | 实验性 ====================

    /**
     * Reset this Lazy to unevaluated state, allowing re-computation on next get() call.
     * 将此 Lazy 重置为未求值状态，允许在下次 get() 调用时重新计算。
     *
     * <p><strong>Warning:</strong> This method is experimental. Using reset() while concurrent
     * get() calls are in flight may cause the supplier to be invoked more than once.
     * The supplier reference must still be available (not a pre-evaluated Lazy created via value()).</p>
     * <p><strong>警告：</strong>此方法为实验性。在并发 get() 调用进行中使用 reset() 可能导致
     * supplier 被调用多次。supplier 引用必须仍然可用（不是通过 value() 创建的预求值 Lazy）。</p>
     *
     * @throws IllegalStateException if this Lazy was created via value() (supplier is null)
     */
    @Experimental(since = "1.0.3", reason = "Reset semantics under concurrency need further validation")
    public void reset() {
        if (preEvaluated) {
            throw new IllegalStateException("Cannot reset a pre-evaluated Lazy created via Lazy.value()");
        }
        VALUE.setVolatile(this, UNINITIALIZED);
    }

    @Override
    public String toString() {
        Object v = VALUE.getOpaque(this);
        if (v instanceof FailedComputation fc) {
            return "Lazy[FAILED: " + fc.exception().getClass().getSimpleName() + "]";
        }
        if (v != UNINITIALIZED) {
            return "Lazy[" + v + "]";
        }
        return "Lazy[?]";
    }
}
