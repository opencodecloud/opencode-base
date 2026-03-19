package cloud.opencode.base.functional.monad;

import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Lazy - Lazy evaluation container
 * Lazy - 惰性求值容器
 *
 * <p>A container that delays computation until the value is actually needed.
 * The computation is performed at most once, and the result is cached.</p>
 * <p>延迟计算直到实际需要值的容器。计算最多执行一次，结果会被缓存。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deferred computation - 延迟计算</li>
 *   <li>Single evaluation (memoization) - 单次求值（记忆化）</li>
 *   <li>Thread-safe (double-checked locking) - 线程安全（双重检查锁）</li>
 *   <li>Monadic operations (map, flatMap, filter) - Monad 操作</li>
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
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>First get(): O(computation time) - 首次 get(): O(计算时间)</li>
 *   <li>Subsequent get(): O(1) - 后续 get(): O(1)</li>
 *   <li>Memory: Holds supplier until evaluated - 内存: 保持 supplier 直到求值</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (DCL) - 线程安全: 是 (双重检查锁)</li>
 *   <li>Null-safe: Allows null results - 空值安全: 允许 null 结果</li>
 * </ul>
 *
 * @param <T> value type - 值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class Lazy<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile T value;
    private volatile boolean evaluated = false;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a Lazy from supplier
     * 从供应商创建 Lazy
     *
     * @param supplier value supplier (evaluated lazily) - 值供应商（惰性求值）
     * @param <T>      value type - 值类型
     * @return Lazy container
     */
    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Create an already-evaluated Lazy with value
     * 创建已求值的 Lazy
     *
     * @param value the value - 值
     * @param <T>   value type - 值类型
     * @return already-evaluated Lazy
     */
    public static <T> Lazy<T> value(T value) {
        Lazy<T> lazy = new Lazy<>(() -> value);
        lazy.value = value;
        lazy.evaluated = true;
        return lazy;
    }

    // ==================== Value Access | 值访问 ====================

    /**
     * Get the value, computing if necessary
     * 获取值，必要时进行计算
     *
     * <p>Thread-safe with double-checked locking.</p>
     * <p>使用双重检查锁保证线程安全。</p>
     *
     * @return the computed value - 计算后的值
     */
    @Override
    public T get() {
        if (!evaluated) {
            lock.lock();
            try {
                if (!evaluated) {
                    value = supplier.get();
                    evaluated = true;
                }
            } finally {
                lock.unlock();
            }
        }
        return value;
    }

    /**
     * Check if the value has been evaluated
     * 检查值是否已被求值
     *
     * @return true if evaluated - 如果已求值返回 true
     */
    public boolean isEvaluated() {
        return evaluated;
    }

    // ==================== Transformations | 转换 ====================

    /**
     * Transform the value lazily
     * 惰性转换值
     *
     * <p>The mapper is not applied until the result's get() is called.</p>
     * <p>映射函数在结果的 get() 被调用前不会执行。</p>
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return lazy transformed value
     */
    public <U> Lazy<U> map(Function<? super T, ? extends U> mapper) {
        return Lazy.of(() -> mapper.apply(get()));
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
        return Lazy.of(() -> mapper.apply(get()).get());
    }

    /**
     * Filter the value lazily
     * 惰性过滤值
     *
     * <p>If the predicate is not satisfied, get() throws NoSuchElementException.</p>
     * <p>如果谓词不满足，get() 抛出 NoSuchElementException。</p>
     *
     * @param predicate filter condition - 过滤条件
     * @return filtered Lazy
     */
    public Lazy<T> filter(Predicate<? super T> predicate) {
        return Lazy.of(() -> {
            T val = get();
            if (predicate.test(val)) {
                return val;
            }
            throw new NoSuchElementException("Lazy.filter: predicate not satisfied");
        });
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
     * Convert to Try
     * 转换为 Try
     *
     * @return Try containing the result or exception
     */
    public Try<T> toTry() {
        return Try.of(this::get);
    }

    /**
     * Convert to Option
     * 转换为 Option
     *
     * @return Option (None if evaluation throws or returns null)
     */
    public Option<T> toOption() {
        try {
            return Option.of(get());
        } catch (Exception e) {
            return Option.none();
        }
    }

    @Override
    public String toString() {
        if (evaluated) {
            return "Lazy[" + value + "]";
        }
        return "Lazy[?]";
    }
}
