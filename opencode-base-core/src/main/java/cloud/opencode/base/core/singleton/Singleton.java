package cloud.opencode.base.core.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Singleton Container - Global singleton instance registry
 * 单例容器 - 全局单例实例注册表
 *
 * <p>Provides a thread-safe container for managing singleton instances by type or name.</p>
 * <p>提供线程安全的容器，用于按类型或名称管理单例实例。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-based registry (get, register by Class) - 按类型注册</li>
 *   <li>Name-based registry (get, register by String) - 按名称注册</li>
 *   <li>Lazy initialization (get with Supplier) - 延迟初始化</li>
 *   <li>Thread-safe operations (ConcurrentHashMap) - 线程安全操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Type-based - 按类型
 * Singleton.register(DataSource.class, dataSource);
 * DataSource ds = Singleton.get(DataSource.class);
 *
 * // Lazy initialization - 延迟初始化
 * Config config = Singleton.get(Config.class, () -> new Config());
 *
 * // Name-based - 按名称
 * Singleton.register("myService", service);
 * MyService svc = Singleton.get("myService");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap) - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class Singleton {

    private static final Map<Class<?>, Object> INSTANCES = new ConcurrentHashMap<>();
    private static final Map<String, Object> NAMED_INSTANCES = new ConcurrentHashMap<>();

    private Singleton() {
    }

    /**
     * Gets a singleton instance (by type)
     * 获取单例实例（按类型）
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        return (T) INSTANCES.get(clazz);
    }

    /**
     * Gets a singleton instance, creates if absent
     * 获取单例实例，不存在时创建
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, Supplier<T> supplier) {
        return (T) INSTANCES.computeIfAbsent(clazz, k -> supplier.get());
    }

    /**
     * Registers a singleton instance (by type)
     * 注册单例实例（按类型）
     */
    public static <T> void register(Class<T> clazz, T instance) {
        INSTANCES.put(clazz, instance);
    }

    /**
     * Registers a singleton instance (if absent)
     * 注册单例实例（如果不存在）
     */
    @SuppressWarnings("unchecked")
    public static <T> T registerIfAbsent(Class<T> clazz, T instance) {
        return (T) INSTANCES.putIfAbsent(clazz, instance);
    }

    /**
     * Removes a singleton instance (by type)
     * 移除单例实例（按类型）
     */
    public static void remove(Class<?> clazz) {
        INSTANCES.remove(clazz);
    }

    /**
     * Checks if a singleton exists (by type)
     * 检查单例是否存在（按类型）
     */
    public static boolean contains(Class<?> clazz) {
        return INSTANCES.containsKey(clazz);
    }

    /**
     * Gets a singleton instance (by name)
     * 获取单例实例（按名称）
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String name) {
        return (T) NAMED_INSTANCES.get(name);
    }

    /**
     * Gets a singleton instance, creates if absent (by name)
     * 获取单例实例，不存在时创建（按名称）
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String name, Supplier<T> supplier) {
        return (T) NAMED_INSTANCES.computeIfAbsent(name, k -> supplier.get());
    }

    /**
     * Registers a singleton instance (by name)
     * 注册单例实例（按名称）
     */
    public static void register(String name, Object instance) {
        NAMED_INSTANCES.put(name, instance);
    }

    /**
     * Registers a singleton instance (if absent, by name)
     * 注册单例实例（如果不存在，按名称）
     */
    @SuppressWarnings("unchecked")
    public static <T> T registerIfAbsent(String name, T instance) {
        return (T) NAMED_INSTANCES.putIfAbsent(name, instance);
    }

    /**
     * Removes a singleton instance (by name)
     * 移除单例实例（按名称）
     */
    public static void remove(String name) {
        NAMED_INSTANCES.remove(name);
    }

    /**
     * Checks if a singleton exists (by name)
     * 检查单例是否存在（按名称）
     */
    public static boolean contains(String name) {
        return NAMED_INSTANCES.containsKey(name);
    }

    /**
     * Clears all singletons
     * 清除所有单例
     */
    public static void clear() {
        INSTANCES.clear();
        NAMED_INSTANCES.clear();
    }

    /**
     * Gets the count of all type-based singletons
     * 获取所有类型单例数量
     */
    public static int size() {
        return INSTANCES.size();
    }

    /**
     * Gets the count of all named singletons
     * 获取所有命名单例数量
     */
    public static int namedSize() {
        return NAMED_INSTANCES.size();
    }
}
