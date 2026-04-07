package cloud.opencode.base.core;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Suppliers - Utility methods for working with Supplier instances
 * Supplier 工具类 - 提供 Supplier 实例的实用方法
 *
 * <p>This class provides useful operations on Supplier instances, including memoization
 * (caching) and time-based expiration.</p>
 * <p>该类提供对 Supplier 实例的实用操作，包括记忆化（缓存）和基于时间的过期。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Memoization (lazy initialization with caching) - 记忆化（惰性初始化并缓存）</li>
 *   <li>Time-based expiration - 基于时间的过期</li>
 *   <li>Thread-safe implementations - 线程安全实现</li>
 *   <li>Compose and synchronize suppliers - 组合和同步 Supplier</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic memoization | 基本记忆化
 * Supplier<ExpensiveObject> supplier = Suppliers.memoize(() -> {
 *     return new ExpensiveObject(); // Only called once
 * });
 * ExpensiveObject obj1 = supplier.get(); // Computes value
 * ExpensiveObject obj2 = supplier.get(); // Returns cached value (same instance)
 *
 * // Memoization with expiration | 带过期的记忆化
 * Supplier<Config> configSupplier = Suppliers.memoizeWithExpiration(
 *     () -> loadConfigFromFile(),
 *     5, TimeUnit.MINUTES  // Cache for 5 minutes
 * );
 *
 * // Using Duration | 使用 Duration
 * Supplier<Data> dataSupplier = Suppliers.memoizeWithExpiration(
 *     () -> fetchData(),
 *     Duration.ofHours(1)
 * );
 *
 * // Compose with function | 与函数组合
 * Supplier<String> stringSupplier = Suppliers.compose(
 *     Object::toString,
 *     () -> someObject
 * );
 *
 * // Synchronized supplier | 同步的 Supplier
 * Supplier<Counter> syncSupplier = Suppliers.synchronizedSupplier(unsafeSupplier);
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>All memoized suppliers returned by this class are thread-safe. The memoization uses
 * double-checked locking for optimal performance.</p>
 * <p>此类返回的所有记忆化 Supplier 都是线程安全的。记忆化使用双重检查锁定以获得最佳性能。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class Suppliers {

    private Suppliers() {
        // Utility class, not instantiable
    }

    // ==================== Memoization | 记忆化 ====================

    /**
     * Returns a supplier that caches the instance supplied by the delegate and returns
     * that value on subsequent calls to get(). The delegate is only invoked once.
     * 返回一个 Supplier，它缓存委托提供的实例，并在后续调用 get() 时返回该值。委托只调用一次。
     *
     * <p>The returned supplier is thread-safe. The delegate's get() method will be invoked
     * at most once, even if the returned supplier's get() method is called multiple times
     * concurrently.</p>
     * <p>返回的 Supplier 是线程安全的。即使并发多次调用返回的 Supplier 的 get() 方法，
     * 委托的 get() 方法也最多只会被调用一次。</p>
     *
     * <p>If the delegate is already a memoized supplier (created by this method), it is
     * returned directly.</p>
     * <p>如果委托已经是记忆化的 Supplier（由此方法创建），则直接返回。</p>
     *
     * @param delegate the underlying supplier to memoize
     * @param <T>      the type of value supplied
     * @return a memoizing supplier
     * @throws NullPointerException if delegate is null
     * @deprecated Use {@link Lazy#of(java.util.function.Supplier)} instead, which is virtual-thread safe.
     */
    @Deprecated(since = "1.0.3")
    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        Objects.requireNonNull(delegate, "delegate must not be null");

        // If already memoized, return as-is
        if (delegate instanceof MemoizingSupplier) {
            return delegate;
        }

        return new MemoizingSupplier<>(delegate);
    }

    /**
     * Returns a supplier that caches the instance supplied by the delegate and returns
     * that value on subsequent calls to get(). The value expires after the specified duration.
     * 返回一个 Supplier，它缓存委托提供的实例，并在后续调用 get() 时返回该值。值在指定时间后过期。
     *
     * <p>After the specified duration has passed, the next call to get() will trigger a new
     * computation by calling the delegate again.</p>
     * <p>指定时间过后，下次调用 get() 将再次调用委托进行新的计算。</p>
     *
     * @param delegate the underlying supplier
     * @param duration the duration after which the cached value expires
     * @param unit     the time unit of the duration
     * @param <T>      the type of value supplied
     * @return a memoizing supplier with expiration
     * @throws NullPointerException     if delegate or unit is null
     * @throws IllegalArgumentException if duration is not positive
     * @deprecated Use {@link Lazy#of(java.util.function.Supplier)} instead, which is virtual-thread safe.
     */
    @Deprecated(since = "1.0.3")
    public static <T> Supplier<T> memoizeWithExpiration(Supplier<T> delegate, long duration, TimeUnit unit) {
        Objects.requireNonNull(delegate, "delegate must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be positive: " + duration);
        }

        long durationNanos = unit.toNanos(duration);
        return new ExpiringMemoizingSupplier<>(delegate, durationNanos);
    }

    /**
     * Returns a supplier that caches the instance supplied by the delegate and returns
     * that value on subsequent calls to get(). The value expires after the specified duration.
     * 返回一个 Supplier，它缓存委托提供的实例，并在后续调用 get() 时返回该值。值在指定时间后过期。
     *
     * @param delegate the underlying supplier
     * @param duration the duration after which the cached value expires
     * @param <T>      the type of value supplied
     * @return a memoizing supplier with expiration
     * @throws NullPointerException     if delegate or duration is null
     * @throws IllegalArgumentException if duration is not positive
     * @deprecated Use {@link Lazy#of(java.util.function.Supplier)} instead, which is virtual-thread safe.
     */
    @Deprecated(since = "1.0.3")
    public static <T> Supplier<T> memoizeWithExpiration(Supplier<T> delegate, Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        return memoizeWithExpiration(delegate, duration.toNanos(), TimeUnit.NANOSECONDS);
    }

    // ==================== Composition | 组合 ====================

    /**
     * Returns a new supplier that applies the given function to the value from the delegate.
     * 返回一个新的 Supplier，它将给定函数应用于委托的值。
     *
     * <p>The returned supplier evaluates the delegate lazily each time get() is called.
     * For cached behavior, wrap the result with memoize().</p>
     * <p>返回的 Supplier 每次调用 get() 时惰性计算委托。如需缓存行为，请用 memoize() 包装结果。</p>
     *
     * @param function the function to apply to the delegate's value
     * @param delegate the underlying supplier
     * @param <F>      the type produced by the delegate
     * @param <T>      the type produced by the resulting supplier
     * @return a composed supplier
     * @throws NullPointerException if function or delegate is null
     */
    public static <F, T> Supplier<T> compose(Function<? super F, T> function, Supplier<F> delegate) {
        Objects.requireNonNull(function, "function must not be null");
        Objects.requireNonNull(delegate, "delegate must not be null");

        return () -> function.apply(delegate.get());
    }

    // ==================== Synchronization | 同步 ====================

    /**
     * Returns a supplier that synchronizes on itself before calling the delegate's get() method.
     * 返回一个 Supplier，它在调用委托的 get() 方法之前对自身进行同步。
     *
     * <p>This is useful for making a non-thread-safe supplier thread-safe.</p>
     * <p>这对于使非线程安全的 Supplier 变为线程安全很有用。</p>
     *
     * @param delegate the underlying supplier
     * @param <T>      the type of value supplied
     * @return a synchronized supplier
     * @throws NullPointerException if delegate is null
     */
    public static <T> Supplier<T> synchronizedSupplier(Supplier<T> delegate) {
        Objects.requireNonNull(delegate, "delegate must not be null");
        return new SynchronizedSupplier<>(delegate);
    }

    // ==================== Utility Methods | 实用方法 ====================

    /**
     * Returns a supplier that always returns the same instance.
     * 返回一个始终返回相同实例的 Supplier。
     *
     * @param instance the instance to return
     * @param <T>      the type of the instance
     * @return a supplier that always returns the given instance
     */
    public static <T> Supplier<T> ofInstance(T instance) {
        return () -> instance;
    }

    // ==================== Internal Implementations | 内部实现 ====================

    /**
     * Thread-safe memoizing supplier using double-checked locking.
     * 使用双重检查锁定的线程安全记忆化 Supplier。
     */
    private static final class MemoizingSupplier<T> implements Supplier<T>, Serializable {

        private static final long serialVersionUID = 1L;

        private transient volatile boolean initialized = false;
        private transient T value;
        private final Supplier<T> delegate;

        MemoizingSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            // Double-checked locking for performance
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        value = delegate.get();
                        initialized = true;
                    }
                }
            }
            return value;
        }

        @Override
        public String toString() {
            return "Suppliers.memoize(" +
                    (initialized ? "<computed: " + value + ">" : delegate) +
                    ")";
        }
    }

    /**
     * Thread-safe memoizing supplier with expiration.
     * 带过期的线程安全记忆化 Supplier。
     */
    private static final class ExpiringMemoizingSupplier<T> implements Supplier<T>, Serializable {

        private static final long serialVersionUID = 1L;

        private final Supplier<T> delegate;
        private final long durationNanos;

        private transient volatile T value;
        private transient volatile long expirationNanos;

        ExpiringMemoizingSupplier(Supplier<T> delegate, long durationNanos) {
            this.delegate = delegate;
            this.durationNanos = durationNanos;
        }

        @Override
        public T get() {
            long nanos = expirationNanos;
            long now = System.nanoTime();

            // Check if expired or not initialized
            if (nanos == 0 || now - nanos >= 0) {
                synchronized (this) {
                    // Double-check within sync block - re-read both volatile field and current time
                    now = System.nanoTime();
                    if (expirationNanos == 0 || now - expirationNanos >= 0) {
                        T t = delegate.get();
                        value = t;
                        // Calculate expiration time, saturating on overflow
                        nanos = now + durationNanos;
                        if (((now ^ nanos) & (durationNanos ^ nanos)) < 0) {
                            // Overflow detected — saturate to Long.MAX_VALUE
                            nanos = Long.MAX_VALUE;
                        }
                        expirationNanos = (nanos == 0) ? 1 : nanos;
                        return t;
                    }
                }
            }
            return value;
        }

        @Override
        public String toString() {
            return "Suppliers.memoizeWithExpiration(" + delegate + ", " +
                    durationNanos + " ns)";
        }
    }

    /**
     * Synchronized supplier wrapper.
     * 同步的 Supplier 包装器。
     */
    private static final class SynchronizedSupplier<T> implements Supplier<T>, Serializable {

        private static final long serialVersionUID = 1L;

        private final Supplier<T> delegate;

        SynchronizedSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public synchronized T get() {
            return delegate.get();
        }

        @Override
        public String toString() {
            return "Suppliers.synchronizedSupplier(" + delegate + ")";
        }
    }
}
