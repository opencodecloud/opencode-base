package cloud.opencode.base.functional.function;

import cloud.opencode.base.functional.exception.OpenFunctionalException;
import cloud.opencode.base.functional.monad.Option;
import cloud.opencode.base.functional.monad.Try;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * FunctionUtil - Function composition, currying, and memoization utilities
 * FunctionUtil - 函数组合、柯里化和记忆化工具类
 *
 * <p>Provides advanced functional programming operations including composition,
 * currying, partial application, and memoization.</p>
 * <p>提供高级函数式编程操作，包括组合、柯里化、部分应用和记忆化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>compose() - Function composition - 函数组合</li>
 *   <li>curry() - Function currying - 函数柯里化</li>
 *   <li>uncurry() - Reverse currying - 反柯里化</li>
 *   <li>partial() - Partial application - 部分应用</li>
 *   <li>flip() - Flip arguments - 翻转参数</li>
 *   <li>memoize() - Result caching - 结果缓存</li>
 *   <li>identity() - Identity function - 恒等函数</li>
 *   <li>constant() - Constant function - 常量函数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Composition: g(f(x))
 * var addOne = FunctionUtil.compose(x -> x + 1, x -> x * 2);
 * addOne.apply(5);  // (5 + 1) * 2 = 12
 *
 * // Currying
 * BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
 * Function<Integer, Integer> add5 = FunctionUtil.curry(add).apply(5);
 * add5.apply(3);  // 8
 *
 * // Memoization
 * Function<Integer, Integer> fib = FunctionUtil.memoize(n ->
 *     n <= 1 ? n : fib.apply(n - 1) + fib.apply(n - 2));
 *
 * // Partial application
 * BiFunction<String, String, String> concat = (a, b) -> a + b;
 * Function<String, String> hello = FunctionUtil.partial(concat, "Hello, ");
 * hello.apply("World");  // "Hello, World"
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>compose(): O(1) - 常量时间</li>
 *   <li>curry(): O(1) - 常量时间</li>
 *   <li>memoize(): O(1) average for cache hits - 缓存命中平均 O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (all methods are stateless) - 线程安全: 是</li>
 *   <li>memoize() uses ConcurrentHashMap - memoize() 使用 ConcurrentHashMap</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class FunctionUtil {

    private FunctionUtil() {
        // Utility class
    }

    // ==================== Composition | 组合 ====================

    /**
     * Compose two functions: g(f(x))
     * 组合两个函数：g(f(x))
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * compose(x -> x + 1, x -> x * 2).apply(5) = 12  // (5+1)*2
     * </pre>
     *
     * @param f   first function (applied first) - 第一个函数（先应用）
     * @param g   second function (applied to result of f) - 第二个函数（应用于 f 的结果）
     * @param <A> input type - 输入类型
     * @param <B> intermediate type - 中间类型
     * @param <C> output type - 输出类型
     * @return composed function - 组合后的函数
     */
    public static <A, B, C> Function<A, C> compose(
            Function<A, B> f,
            Function<B, C> g) {
        return f.andThen(g);
    }

    /**
     * Compose three functions: h(g(f(x)))
     * 组合三个函数：h(g(f(x)))
     *
     * @param f   first function - 第一个函数
     * @param g   second function - 第二个函数
     * @param h   third function - 第三个函数
     * @param <A> input type - 输入类型
     * @param <B> first intermediate type - 第一个中间类型
     * @param <C> second intermediate type - 第二个中间类型
     * @param <D> output type - 输出类型
     * @return composed function - 组合后的函数
     */
    public static <A, B, C, D> Function<A, D> compose(
            Function<A, B> f,
            Function<B, C> g,
            Function<C, D> h) {
        return f.andThen(g).andThen(h);
    }

    // ==================== Currying | 柯里化 ====================

    /**
     * Curry a BiFunction to a curried function
     * 将 BiFunction 柯里化
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * BiFunction&lt;Integer, Integer, Integer&gt; add = (a, b) -&gt; a + b;
     * Function&lt;Integer, Function&lt;Integer, Integer&gt;&gt; curried = curry(add);
     * curried.apply(5).apply(3) = 8
     * </pre>
     *
     * @param f   function to curry - 要柯里化的函数
     * @param <A> first input type - 第一个输入类型
     * @param <B> second input type - 第二个输入类型
     * @param <R> result type - 结果类型
     * @return curried function - 柯里化后的函数
     */
    public static <A, B, R> Function<A, Function<B, R>> curry(BiFunction<A, B, R> f) {
        return a -> b -> f.apply(a, b);
    }

    /**
     * Curry a TriFunction to a curried function
     * 将 TriFunction 柯里化
     *
     * @param f      function to curry - 要柯里化的函数
     * @param <T1>   first input type - 第一个输入类型
     * @param <T2>   second input type - 第二个输入类型
     * @param <T3>   third input type - 第三个输入类型
     * @param <R>    result type - 结果类型
     * @return curried function - 柯里化后的函数
     */
    public static <T1, T2, T3, R> Function<T1, Function<T2, Function<T3, R>>> curry(
            TriFunction<T1, T2, T3, R> f) {
        return t1 -> t2 -> t3 -> f.apply(t1, t2, t3);
    }

    /**
     * Uncurry a curried function back to BiFunction
     * 将柯里化函数还原为 BiFunction
     *
     * @param f   curried function - 柯里化函数
     * @param <A> first input type - 第一个输入类型
     * @param <B> second input type - 第二个输入类型
     * @param <R> result type - 结果类型
     * @return uncurried BiFunction - 反柯里化后的 BiFunction
     */
    public static <A, B, R> BiFunction<A, B, R> uncurry(Function<A, Function<B, R>> f) {
        return (a, b) -> f.apply(a).apply(b);
    }

    // ==================== Partial Application | 部分应用 ====================

    /**
     * Partial application - fix the first argument
     * 部分应用 - 固定第一个参数
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * BiFunction&lt;String, String, String&gt; concat = (a, b) -&gt; a + b;
     * Function&lt;String, String&gt; hello = partial(concat, "Hello, ");
     * hello.apply("World") = "Hello, World"
     * </pre>
     *
     * @param f   function - 函数
     * @param a   first argument (fixed) - 第一个参数（固定）
     * @param <A> first input type - 第一个输入类型
     * @param <B> second input type - 第二个输入类型
     * @param <R> result type - 结果类型
     * @return partially applied function - 部分应用后的函数
     */
    public static <A, B, R> Function<B, R> partial(BiFunction<A, B, R> f, A a) {
        return b -> f.apply(a, b);
    }

    /**
     * Partial application - fix the second argument
     * 部分应用 - 固定第二个参数
     *
     * @param f   function - 函数
     * @param b   second argument (fixed) - 第二个参数（固定）
     * @param <A> first input type - 第一个输入类型
     * @param <B> second input type - 第二个输入类型
     * @param <R> result type - 结果类型
     * @return partially applied function - 部分应用后的函数
     */
    public static <A, B, R> Function<A, R> partialRight(BiFunction<A, B, R> f, B b) {
        return a -> f.apply(a, b);
    }

    // ==================== Flip | 翻转参数 ====================

    /**
     * Flip the arguments of a BiFunction
     * 翻转 BiFunction 的参数
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * BiFunction&lt;String, Integer, String&gt; repeat = (s, n) -&gt; s.repeat(n);
     * BiFunction&lt;Integer, String, String&gt; flipped = flip(repeat);
     * flipped.apply(3, "ab") = "ababab"
     * </pre>
     *
     * @param f   function - 函数
     * @param <A> first input type - 第一个输入类型
     * @param <B> second input type - 第二个输入类型
     * @param <R> result type - 结果类型
     * @return function with flipped arguments - 参数翻转后的函数
     */
    public static <A, B, R> BiFunction<B, A, R> flip(BiFunction<A, B, R> f) {
        return (b, a) -> f.apply(a, b);
    }

    // ==================== Memoization | 记忆化 ====================

    /**
     * Default maximum cache size for memoization
     * 记忆化的默认最大缓存大小
     */
    private static final int DEFAULT_MEMOIZE_MAX_SIZE = 1000;

    /**
     * Memoize a function - cache results for repeated calls
     * 记忆化函数 - 缓存重复调用的结果
     *
     * <p>Uses ConcurrentHashMap for thread-safe caching with a default
     * maximum size of 1000 entries (LRU eviction).</p>
     * <p>使用 ConcurrentHashMap 实现线程安全缓存，默认最大 1000 条记录（LRU 淘汰）。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * Function&lt;Integer, Integer&gt; fib = memoize(n -&gt;
     *     n &lt;= 1 ? n : fib.apply(n-1) + fib.apply(n-2));
     * fib.apply(10);  // Computed
     * fib.apply(10);  // Cached result
     * </pre>
     *
     * @param f   function to memoize - 要记忆化的函数
     * @param <T> input type - 输入类型
     * @param <R> result type - 结果类型
     * @return memoized function - 记忆化后的函数
     */
    public static <T, R> Function<T, R> memoize(Function<T, R> f) {
        return memoize(f, DEFAULT_MEMOIZE_MAX_SIZE);
    }

    /**
     * Memoize a function with specified maximum cache size
     * 使用指定最大缓存大小记忆化函数
     *
     * <p>Uses LRU (Least Recently Used) eviction when cache is full.
     * Thread-safe: Computation runs outside the lock to avoid blocking other keys.
     * Note: For the same uncached key, concurrent callers may compute simultaneously;
     * only the first result is cached (subsequent results are discarded).</p>
     * <p>缓存满时使用 LRU（最近最少使用）淘汰策略。
     * 线程安全：计算在锁外执行以避免阻塞其他 key。
     * 注意：对于同一个未缓存的 key，并发调用者可能同时计算；
     * 仅第一个结果被缓存（后续结果被丢弃）。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * // Cache only last 100 results
     * Function&lt;String, Data&gt; fetch = memoize(api::fetchData, 100);
     * </pre>
     *
     * @param f       function to memoize - 要记忆化的函数
     * @param maxSize maximum cache size (must be positive) - 最大缓存大小（必须为正数）
     * @param <T>     input type - 输入类型
     * @param <R>     result type - 结果类型
     * @return memoized function - 记忆化后的函数
     * @throws IllegalArgumentException if maxSize is not positive
     */
    public static <T, R> Function<T, R> memoize(Function<T, R> f, int maxSize) {
        java.util.Objects.requireNonNull(f, "function must not be null");
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive: " + maxSize);
        }

        // Sentinel to distinguish "not cached" from "cached null"
        @SuppressWarnings("unchecked")
        final R sentinel = (R) new Object();

        // Use LinkedHashMap with access-order for LRU eviction
        // ReentrantLock required for thread-safe access to non-concurrent LinkedHashMap
        final Map<T, R> cache = new java.util.LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<T, R> eldest) {
                return size() > maxSize;
            }
        };
        final ReentrantLock cacheLock = new ReentrantLock();

        return t -> {
            // Fast path: check cache under lock (short hold)
            cacheLock.lock();
            try {
                R cached = cache.getOrDefault(t, sentinel);
                if (cached != sentinel) {
                    return cached;
                }
            } finally {
                cacheLock.unlock();
            }

            // Compute outside lock to avoid blocking other keys
            R result = f.apply(t);

            // Store result under lock
            cacheLock.lock();
            try {
                cache.putIfAbsent(t, result);
                return cache.get(t);
            } finally {
                cacheLock.unlock();
            }
        };
    }

    /**
     * Memoize a Supplier - cache the result of first call
     * 记忆化 Supplier - 缓存首次调用的结果
     *
     * @param supplier supplier to memoize - 要记忆化的 Supplier
     * @param <T>      result type - 结果类型
     * @return memoized supplier - 记忆化后的 Supplier
     */
    public static <T> Supplier<T> memoize(Supplier<T> supplier) {
        java.util.Objects.requireNonNull(supplier, "supplier must not be null");
        return new Supplier<>() {
            private final ReentrantLock lock = new ReentrantLock();
            private volatile T value;
            private volatile boolean computed = false;

            @Override
            public T get() {
                if (!computed) {
                    lock.lock();
                    try {
                        if (!computed) {
                            value = supplier.get();
                            computed = true;
                        }
                    } finally {
                        lock.unlock();
                    }
                }
                return value;
            }
        };
    }

    // ==================== Unchecked Conversion | 非受检转换 ====================

    /**
     * Convert CheckedFunction to standard Function (wrapping exceptions)
     * 将 CheckedFunction 转换为标准 Function（包装异常）
     *
     * @param f   checked function - 受检函数
     * @param <T> input type - 输入类型
     * @param <R> result type - 结果类型
     * @return unchecked function - 非受检函数
     */
    public static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenFunctionalException("Checked function threw exception", e);
            }
        };
    }

    /**
     * Convert CheckedBiFunction to standard BiFunction (wrapping exceptions)
     * 将 CheckedBiFunction 转换为标准 BiFunction（包装异常）
     *
     * @param f   checked function - 受检函数
     * @param <T> first input type - 第一个输入类型
     * @param <U> second input type - 第二个输入类型
     * @param <R> result type - 结果类型
     * @return unchecked function - 非受检函数
     */
    public static <T, U, R> BiFunction<T, U, R> unchecked(CheckedBiFunction<T, U, R> f) {
        return (t, u) -> {
            try {
                return f.apply(t, u);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenFunctionalException("Checked function threw exception", e);
            }
        };
    }

    // ==================== Common Functions | 常用函数 ====================

    /**
     * Identity function - returns its input unchanged
     * 恒等函数 - 原样返回输入
     *
     * @param <T> type - 类型
     * @return identity function - 恒等函数
     */
    public static <T> Function<T, T> identity() {
        return t -> t;
    }

    /**
     * Constant function - always returns the same value
     * 常量函数 - 始终返回相同的值
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * Function&lt;Object, String&gt; always = constant("default");
     * always.apply(anything) = "default"
     * </pre>
     *
     * @param value constant value to return - 要返回的常量值
     * @param <T>   input type (ignored) - 输入类型（被忽略）
     * @param <R>   result type - 结果类型
     * @return constant function - 常量函数
     */
    public static <T, R> Function<T, R> constant(R value) {
        return t -> value;
    }

    /**
     * Predicate negation
     * 谓词取反
     *
     * @param predicate predicate to negate - 要取反的谓词
     * @param <T>       input type - 输入类型
     * @return negated predicate - 取反后的谓词
     */
    public static <T> java.util.function.Predicate<T> not(
            java.util.function.Predicate<T> predicate) {
        return predicate.negate();
    }

    // ==================== Lift | 提升 ====================

    /**
     * Lift a checked function to return Option instead of throwing
     * 将受检函数提升为返回 Option 而非抛出异常
     *
     * <p>On success, returns Option.some(result). On exception, returns Option.none().</p>
     * <p>成功时返回 Option.some(result)。异常时返回 Option.none()。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * Function&lt;String, Option&lt;Integer&gt;&gt; safeParse = FunctionUtil.lift(Integer::parseInt);
     * safeParse.apply("123");  // Option.some(123)
     * safeParse.apply("abc");  // Option.none()
     * </pre>
     *
     * @param f   checked function to lift - 要提升的受检函数
     * @param <T> input type - 输入类型
     * @param <R> result type - 结果类型
     * @return function returning Option - 返回 Option 的函数
     * @since JDK 25, opencode-base-functional V1.0.3
     */
    public static <T, R> Function<T, Option<R>> lift(CheckedFunction<T, R> f) {
        return t -> {
            try {
                return Option.of(f.apply(t));
            } catch (Exception e) {
                return Option.none();
            }
        };
    }

    /**
     * Lift a checked function to return Try instead of throwing
     * 将受检函数提升为返回 Try 而非抛出异常
     *
     * <p>On success, returns Try.success(result). On exception, returns Try.failure(exception).</p>
     * <p>成功时返回 Try.success(result)。异常时返回 Try.failure(exception)。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * Function&lt;String, Try&lt;Integer&gt;&gt; safeParse = FunctionUtil.liftTry(Integer::parseInt);
     * safeParse.apply("123");  // Try.success(123)
     * safeParse.apply("abc");  // Try.failure(NumberFormatException)
     * </pre>
     *
     * @param f   checked function to lift - 要提升的受检函数
     * @param <T> input type - 输入类型
     * @param <R> result type - 结果类型
     * @return function returning Try - 返回 Try 的函数
     * @since JDK 25, opencode-base-functional V1.0.3
     */
    public static <T, R> Function<T, Try<R>> liftTry(CheckedFunction<T, R> f) {
        return t -> {
            try {
                return Try.success(f.apply(t));
            } catch (Exception e) {
                return Try.failure(e);
            }
        };
    }

    /**
     * Lift a checked bi-function to return Option instead of throwing
     * 将受检双参函数提升为返回 Option 而非抛出异常
     *
     * <p>On success, returns Option.some(result). On exception, returns Option.none().</p>
     * <p>成功时返回 Option.some(result)。异常时返回 Option.none()。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * BiFunction&lt;String, Integer, Option&lt;String&gt;&gt; safeSub =
     *     FunctionUtil.liftBi((s, len) -&gt; s.substring(0, len));
     * safeSub.apply("hello", 3);   // Option.some("hel")
     * safeSub.apply("hello", 100); // Option.none()
     * </pre>
     *
     * @param f   checked bi-function to lift - 要提升的受检双参函数
     * @param <T> first input type - 第一个输入类型
     * @param <U> second input type - 第二个输入类型
     * @param <R> result type - 结果类型
     * @return bi-function returning Option - 返回 Option 的双参函数
     * @since JDK 25, opencode-base-functional V1.0.3
     */
    public static <T, U, R> BiFunction<T, U, Option<R>> liftBi(CheckedBiFunction<T, U, R> f) {
        return (t, u) -> {
            try {
                return Option.of(f.apply(t, u));
            } catch (Exception e) {
                return Option.none();
            }
        };
    }

    /**
     * Lift a checked bi-function to return Try instead of throwing
     * 将受检双参函数提升为返回 Try 而非抛出异常
     *
     * <p>On success, returns Try.success(result). On exception, returns Try.failure(exception).</p>
     * <p>成功时返回 Try.success(result)。异常时返回 Try.failure(exception)。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * BiFunction&lt;String, Integer, Try&lt;String&gt;&gt; safeSub =
     *     FunctionUtil.liftBiTry((s, len) -&gt; s.substring(0, len));
     * safeSub.apply("hello", 3);   // Try.success("hel")
     * safeSub.apply("hello", 100); // Try.failure(StringIndexOutOfBoundsException)
     * </pre>
     *
     * @param f   checked bi-function to lift - 要提升的受检双参函数
     * @param <T> first input type - 第一个输入类型
     * @param <U> second input type - 第二个输入类型
     * @param <R> result type - 结果类型
     * @return bi-function returning Try - 返回 Try 的双参函数
     * @since JDK 25, opencode-base-functional V1.0.3
     */
    public static <T, U, R> BiFunction<T, U, Try<R>> liftBiTry(CheckedBiFunction<T, U, R> f) {
        return (t, u) -> {
            try {
                return Try.success(f.apply(t, u));
            } catch (Exception e) {
                return Try.failure(e);
            }
        };
    }
}
