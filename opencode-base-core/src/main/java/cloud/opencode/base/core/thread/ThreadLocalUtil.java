package cloud.opencode.base.core.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * ThreadLocal Utility - Global named ThreadLocal management
 * ThreadLocal 工具类 - 全局命名的 ThreadLocal 管理
 *
 * <p>Provides global named ThreadLocal storage with context execution support.</p>
 * <p>提供全局命名的 ThreadLocal 存储和上下文执行支持。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Named ThreadLocal get/set/remove - 命名 ThreadLocal 获取/设置/移除</li>
 *   <li>Compute if absent (getOrCompute) - 不存在时计算</li>
 *   <li>Context execution (runWithContext, callWithContext) - 上下文执行</li>
 *   <li>InheritableThreadLocal creation - 可继承 ThreadLocal 创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Set/Get value - 设置/获取值
 * ThreadLocalUtil.set("userId", 123);
 * Integer userId = ThreadLocalUtil.get("userId");
 *
 * // Run with context - 上下文执行
 * ThreadLocalUtil.runWithContext("tenant", "A", () -> {
 *     // code runs with tenant=A
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap) - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per get/set - 每次获取/设置 O(1)</li>
 *   <li>Space complexity: O(1) per thread-local - 每个线程局部变量 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class ThreadLocalUtil {

    private static final Map<String, ThreadLocal<?>> THREAD_LOCALS = new ConcurrentHashMap<>();

    private ThreadLocalUtil() {
    }

    /**
     * Gets the ThreadLocal value
     * 获取 ThreadLocal 值
     *
     * @deprecated Use {@link ScopedValueUtil} instead for virtual-thread-safe context propagation.
     */
    @Deprecated(since = "1.0.3")
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        ThreadLocal<T> threadLocal = (ThreadLocal<T>) THREAD_LOCALS.get(key);
        return threadLocal != null ? threadLocal.get() : null;
    }

    /**
     * Gets the ThreadLocal value, returns default when absent
     * 获取 ThreadLocal 值，不存在时返回默认值
     *
     * @deprecated Use {@link ScopedValueUtil} instead for virtual-thread-safe context propagation.
     */
    @Deprecated(since = "1.0.3")
    public static <T> T get(String key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets the ThreadLocal value, computes via Supplier when absent
     * 获取 ThreadLocal 值，不存在时通过 Supplier 计算
     *
     * @deprecated Use {@link ScopedValueUtil} instead for virtual-thread-safe context propagation.
     */
    @Deprecated(since = "1.0.3")
    public static <T> T getOrCompute(String key, Supplier<T> supplier) {
        T value = get(key);
        if (value == null) {
            value = supplier.get();
            set(key, value);
        }
        return value;
    }

    /**
     * Sets the ThreadLocal value
     * 设置 ThreadLocal 值
     *
     * @deprecated Use {@link ScopedValueUtil} instead for virtual-thread-safe context propagation.
     */
    @Deprecated(since = "1.0.3")
    @SuppressWarnings("unchecked")
    public static <T> void set(String key, T value) {
        ThreadLocal<T> threadLocal = (ThreadLocal<T>) THREAD_LOCALS.computeIfAbsent(key, k -> new ThreadLocal<>());
        threadLocal.set(value);
    }

    /**
     * Removes the specified ThreadLocal value for the current thread
     * 移除当前线程的指定 ThreadLocal 值
     *
     * @deprecated Use {@link ScopedValueUtil} instead for virtual-thread-safe context propagation.
     */
    @Deprecated(since = "1.0.3")
    public static void remove(String key) {
        ThreadLocal<?> threadLocal = THREAD_LOCALS.get(key);
        if (threadLocal != null) {
            threadLocal.remove();
        }
    }

    /**
     * Unregisters a named ThreadLocal from the global registry and removes its value.
     * This allows the ThreadLocal instance to be garbage collected.
     * 从全局注册表中注销指定名称的 ThreadLocal 并移除其值，使 ThreadLocal 实例可被 GC 回收。
     *
     * @param name the ThreadLocal name to unregister - 要注销的 ThreadLocal 名称
     */
    public static void unregister(String name) {
        ThreadLocal<?> threadLocal = THREAD_LOCALS.remove(name);
        if (threadLocal != null) {
            threadLocal.remove();
        }
    }

    /**
     * Clears all ThreadLocal values for the current thread without affecting the global registry.
     * Other threads' values remain accessible and can still be cleaned up later.
     * 清除当前线程的所有 ThreadLocal 值，不影响全局注册表。
     * 其他线程的值仍可访问，后续仍可清理。
     *
     * @deprecated Use {@link ScopedValueUtil} instead for virtual-thread-safe context propagation.
     */
    @Deprecated(since = "1.0.3")
    public static void clear() {
        THREAD_LOCALS.values().forEach(ThreadLocal::remove);
    }

    /**
     * Unregisters all ThreadLocal entries: removes values for the current thread and clears the global registry.
     * 注销所有 ThreadLocal 条目：移除当前线程的值并清空全局注册表。
     *
     * <p><strong>WARNING:</strong> This is a global operation that affects ALL threads.
     * After this call, ThreadLocal values set by other threads become unreachable through this utility
     * and can no longer be cleaned up via {@link #clear()} or {@link #remove(String)}.
     * Typically only used during application shutdown.</p>
     *
     * <p><strong>警告：</strong>这是一个全局操作，会影响所有线程。
     * 调用后，其他线程通过本工具设置的 ThreadLocal 值将无法再通过 {@link #clear()} 或
     * {@link #remove(String)} 清理。通常仅在应用关闭时使用。</p>
     */
    public static void unregisterAll() {
        THREAD_LOCALS.values().forEach(ThreadLocal::remove);
        THREAD_LOCALS.clear();
    }

    /**
     * Checks if the ThreadLocal exists
     * 检查 ThreadLocal 是否存在
     */
    public static boolean contains(String key) {
        return THREAD_LOCALS.containsKey(key);
    }

    /**
     * Creates a ThreadLocal
     * 创建 ThreadLocal
     */
    public static <T> ThreadLocal<T> create() {
        return new ThreadLocal<>();
    }

    /**
     * Creates a ThreadLocal with initial value
     * 创建带初始值的 ThreadLocal
     */
    public static <T> ThreadLocal<T> createWithInitial(Supplier<T> supplier) {
        return ThreadLocal.withInitial(supplier);
    }

    /**
     * Creates an InheritableThreadLocal
     * 创建 InheritableThreadLocal
     */
    public static <T> InheritableThreadLocal<T> createInheritable() {
        return new InheritableThreadLocal<>();
    }

    /**
     * Creates an InheritableThreadLocal with initial value
     * 创建带初始值的 InheritableThreadLocal
     */
    public static <T> InheritableThreadLocal<T> createInheritableWithInitial(Supplier<T> supplier) {
        return new InheritableThreadLocal<>() {
            @Override
            protected T initialValue() {
                return supplier.get();
            }
        };
    }

    /**
     * Executes in the specified context
     * 在指定上下文中执行
     *
     * @deprecated Use {@link ScopedValueUtil} instead for virtual-thread-safe context propagation.
     */
    @Deprecated(since = "1.0.3")
    public static <T> void runWithContext(String key, T value, Runnable runnable) {
        T oldValue = get(key);
        try {
            set(key, value);
            runnable.run();
        } finally {
            if (oldValue == null) {
                remove(key);
            } else {
                set(key, oldValue);
            }
        }
    }

    /**
     * Executes in the specified context and returns a result
     * 在指定上下文中执行并返回结果
     *
     * @deprecated Use {@link ScopedValueUtil} instead for virtual-thread-safe context propagation.
     */
    @Deprecated(since = "1.0.3")
    public static <T, R> R callWithContext(String key, T value, Supplier<R> supplier) {
        T oldValue = get(key);
        try {
            set(key, value);
            return supplier.get();
        } finally {
            if (oldValue == null) {
                remove(key);
            } else {
                set(key, oldValue);
            }
        }
    }

    /**
     * Gets all ThreadLocal keys
     * 获取所有 ThreadLocal 的 key
     */
    public static java.util.Set<String> keys() {
        return java.util.Set.copyOf(THREAD_LOCALS.keySet());
    }
}
