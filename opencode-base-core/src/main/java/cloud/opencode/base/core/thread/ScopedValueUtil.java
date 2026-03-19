package cloud.opencode.base.core.thread;

/**
 * ScopedValue Utility - JDK 25 Scoped Values support (JEP 506)
 * ScopedValue 工具类 - JDK 25 作用域值支持 (JEP 506)
 *
 * <p>Provides safer and more efficient thread-local value passing mechanism than ThreadLocal.</p>
 * <p>提供比 ThreadLocal 更安全、更高效的线程局部值传递机制。</p>
 *
 * <p><strong>JDK 25 Features | JDK 25 特性:</strong></p>
 * <ul>
 *   <li>Immutability - Values cannot be modified within scope - 不可变性：值在作用域内不可修改</li>
 *   <li>Auto cleanup - Automatically released when scope ends - 自动清理：作用域结束时自动释放</li>
 *   <li>Safe inheritance - Child threads safely inherit parent values - 继承安全：子线程可安全继承父线程的值</li>
 *   <li>Better performance - No manual cleanup, no memory leaks - 性能更优：无需手动清理，避免内存泄漏</li>
 * </ul>
 *
 * <p><strong>Comparison with ThreadLocal | 与 ThreadLocal 对比:</strong></p>
 * <table border="1">
 *   <caption>ScopedValue vs ThreadLocal comparison | ScopedValue 与 ThreadLocal 对比</caption>
 *   <tr><th>Feature</th><th>ScopedValue</th><th>ThreadLocal</th></tr>
 *   <tr><td>Mutability</td><td>Immutable</td><td>Mutable</td></tr>
 *   <tr><td>Cleanup</td><td>Automatic</td><td>Manual</td></tr>
 *   <tr><td>Inheritance</td><td>Safe</td><td>InheritableThreadLocal needed</td></tr>
 *   <tr><td>Memory Leak Risk</td><td>None</td><td>High if not cleaned</td></tr>
 * </table>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Define ScopedValue (typically static final)
 * private static final ScopedValue<String> USER_ID = ScopedValue.newInstance();
 * private static final ScopedValue<String> TRACE_ID = ScopedValue.newInstance();
 *
 * // Bind values and execute task
 * ScopedValueUtil.runWhere(USER_ID, "user123", () -> {
 *     String userId = USER_ID.get();  // Get bound value
 *     processRequest();
 * });
 *
 * // Multiple bindings
 * ScopedValueUtil.runWhere(USER_ID, "user123", TRACE_ID, "trace-abc", () -> {
 *     processRequest();
 * });
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong> Yes - ScopedValue is inherently thread-safe</p>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per get/set - 每次获取/设置 O(1)</li>
 *   <li>Space complexity: O(1) per scoped value - 每个作用域值 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ScopedValue
 * @see ThreadLocalUtil
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class ScopedValueUtil {

    private ScopedValueUtil() {
    }

    /**
     * Create a new ScopedValue instance
     * 创建新的 ScopedValue 实例
     *
     * @param <T> the type of the scoped value
     * @return a new ScopedValue instance
     */
    public static <T> ScopedValue<T> newScopedValue() {
        return ScopedValue.newInstance();
    }

    /**
     * Bind a value to a ScopedValue and execute a Runnable task
     * 将值绑定到 ScopedValue 并执行 Runnable 任务
     *
     * @param <T>         the type of the scoped value
     * @param scopedValue the ScopedValue to bind
     * @param value       the value to bind
     * @param task        the task to execute
     */
    public static <T> void runWhere(ScopedValue<T> scopedValue, T value, Runnable task) {
        ScopedValue.where(scopedValue, value).run(task);
    }

    /**
     * Bind a value to a ScopedValue and execute a CallableOp task
     * 将值绑定到 ScopedValue 并执行 CallableOp 任务
     *
     * @param <T>         the type of the scoped value
     * @param <R>         the return type of the callable
     * @param <X>         the exception type that may be thrown
     * @param scopedValue the ScopedValue to bind
     * @param value       the value to bind
     * @param task        the callable task to execute
     * @return the result of the callable
     * @throws X if the callable throws an exception
     */
    public static <T, R, X extends Throwable> R callWhere(ScopedValue<T> scopedValue, T value,
                                                          ScopedValue.CallableOp<R, X> task) throws X {
        return ScopedValue.where(scopedValue, value).call(task);
    }

    /**
     * Bind two values to two ScopedValues and execute a Runnable task
     * 将两个值绑定到两个 ScopedValue 并执行 Runnable 任务
     *
     * @param <T1>  the type of the first scoped value
     * @param <T2>  the type of the second scoped value
     * @param sv1   the first ScopedValue
     * @param v1    the first value to bind
     * @param sv2   the second ScopedValue
     * @param v2    the second value to bind
     * @param task  the task to execute
     */
    public static <T1, T2> void runWhere(ScopedValue<T1> sv1, T1 v1,
                                         ScopedValue<T2> sv2, T2 v2,
                                         Runnable task) {
        ScopedValue.where(sv1, v1).where(sv2, v2).run(task);
    }

    /**
     * Bind two values to two ScopedValues and execute a CallableOp task
     * 将两个值绑定到两个 ScopedValue 并执行 CallableOp 任务
     *
     * @param <T1>  the type of the first scoped value
     * @param <T2>  the type of the second scoped value
     * @param <R>   the return type of the callable
     * @param <X>   the exception type that may be thrown
     * @param sv1   the first ScopedValue
     * @param v1    the first value to bind
     * @param sv2   the second ScopedValue
     * @param v2    the second value to bind
     * @param task  the callable task to execute
     * @return the result of the callable
     * @throws X if the callable throws an exception
     */
    public static <T1, T2, R, X extends Throwable> R callWhere(ScopedValue<T1> sv1, T1 v1,
                                                               ScopedValue<T2> sv2, T2 v2,
                                                               ScopedValue.CallableOp<R, X> task) throws X {
        return ScopedValue.where(sv1, v1).where(sv2, v2).call(task);
    }

    /**
     * Bind three values to three ScopedValues and execute a Runnable task
     * 将三个值绑定到三个 ScopedValue 并执行 Runnable 任务
     *
     * @param <T1>  the type of the first scoped value
     * @param <T2>  the type of the second scoped value
     * @param <T3>  the type of the third scoped value
     * @param sv1   the first ScopedValue
     * @param v1    the first value to bind
     * @param sv2   the second ScopedValue
     * @param v2    the second value to bind
     * @param sv3   the third ScopedValue
     * @param v3    the third value to bind
     * @param task  the task to execute
     */
    public static <T1, T2, T3> void runWhere(ScopedValue<T1> sv1, T1 v1,
                                              ScopedValue<T2> sv2, T2 v2,
                                              ScopedValue<T3> sv3, T3 v3,
                                              Runnable task) {
        ScopedValue.where(sv1, v1).where(sv2, v2).where(sv3, v3).run(task);
    }

    /**
     * Check if a ScopedValue is bound in the current scope
     * 检查 ScopedValue 是否在当前作用域中已绑定
     *
     * @param <T>         the type of the scoped value
     * @param scopedValue the ScopedValue to check
     * @return true if the ScopedValue is bound, false otherwise
     */
    public static <T> boolean isBound(ScopedValue<T> scopedValue) {
        return scopedValue.isBound();
    }

    /**
     * Get the value of a ScopedValue, or return a default value if not bound
     * 获取 ScopedValue 的值，如果未绑定则返回默认值
     *
     * <p>Note: In JDK 25, the default value cannot be null.</p>
     *
     * @param <T>          the type of the scoped value
     * @param scopedValue  the ScopedValue to get
     * @param defaultValue the default value to return if not bound (cannot be null)
     * @return the bound value, or the default value if not bound
     */
    public static <T> T getOrDefault(ScopedValue<T> scopedValue, T defaultValue) {
        return scopedValue.orElse(defaultValue);
    }

    /**
     * Get the value of a ScopedValue
     * 获取 ScopedValue 的值
     *
     * @param <T>         the type of the scoped value
     * @param scopedValue the ScopedValue to get
     * @return the bound value
     * @throws java.util.NoSuchElementException if not bound
     */
    public static <T> T get(ScopedValue<T> scopedValue) {
        return scopedValue.get();
    }

    /**
     * Create a ScopedValue.Carrier with a single binding
     * 创建带有单个绑定的 ScopedValue.Carrier
     *
     * <p>Useful for building complex binding chains.</p>
     *
     * @param <T>         the type of the scoped value
     * @param scopedValue the ScopedValue to bind
     * @param value       the value to bind
     * @return a Carrier with the binding
     */
    public static <T> ScopedValue.Carrier where(ScopedValue<T> scopedValue, T value) {
        return ScopedValue.where(scopedValue, value);
    }
}
