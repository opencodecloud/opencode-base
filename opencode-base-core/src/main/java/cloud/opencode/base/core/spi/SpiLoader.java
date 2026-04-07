package cloud.opencode.base.core.spi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Objects;

/**
 * SPI Loader - Service Provider Interface loading utility
 * SPI 加载器 - 服务提供者接口加载工具
 *
 * <p>Provides cached SPI service loading using Java ServiceLoader mechanism.</p>
 * <p>使用 Java ServiceLoader 机制提供带缓存的 SPI 服务加载。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load all implementations (load) - 加载所有实现</li>
 *   <li>Load first implementation (loadFirst) - 加载第一个实现</li>
 *   <li>Load with default (loadFirstOrDefault) - 带默认值加载</li>
 *   <li>Filter by type (loadByType) - 按类型过滤</li>
 *   <li>Cached loading for performance - 缓存加载提升性能</li>
 *   <li>Reload support (reload) - 重新加载支持</li>
 *   <li>Safe loading with error isolation (loadSafe) - 带错误隔离的安全加载</li>
 *   <li>Priority-ordered loading (loadOrdered) - 按优先级排序加载</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<MyService> services = SpiLoader.load(MyService.class);
 * Optional<MyService> first = SpiLoader.loadFirst(MyService.class);
 * MyService service = SpiLoader.loadFirstOrDefault(MyService.class, defaultImpl);
 *
 * if (SpiLoader.hasService(MyService.class)) {
 *     int count = SpiLoader.count(MyService.class);
 * }
 *
 * // Error-isolated loading | 带错误隔离的加载
 * List<MyService> safe = SpiLoader.loadSafe(MyService.class);  // skips broken providers
 *
 * // Priority-ordered loading | 按优先级排序加载
 * List<MyService> ordered = SpiLoader.loadOrdered(MyService.class);
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
public final class SpiLoader {

    private static final Map<Class<?>, List<?>> CACHE = new ConcurrentHashMap<>();

    private SpiLoader() {
    }

    /**
     * Loads SPI service implementations
     * 加载 SPI 服务实现
     */
    public static <T> List<T> load(Class<T> serviceClass) {
        return load(serviceClass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Loads SPI service implementations (with specified ClassLoader)
     * 加载 SPI 服务实现（指定类加载器）
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> load(Class<T> serviceClass, ClassLoader classLoader) {
        return Collections.unmodifiableList((List<T>) CACHE.computeIfAbsent(serviceClass, k -> {
            List<T> services = new ArrayList<>();
            ServiceLoader<T> loader = ServiceLoader.load(serviceClass, classLoader);
            for (T service : loader) {
                services.add(service);
            }
            return services;
        }));
    }

    /**
     * Loads the first SPI service implementation
     * 加载第一个 SPI 服务实现
     */
    public static <T> Optional<T> loadFirst(Class<T> serviceClass) {
        List<T> services = load(serviceClass);
        return services.isEmpty() ? Optional.empty() : Optional.of(services.get(0));
    }

    /**
     * Loads the first SPI service implementation, or uses the default
     * 加载第一个 SPI 服务实现，不存在则使用默认值
     */
    public static <T> T loadFirstOrDefault(Class<T> serviceClass, T defaultValue) {
        return loadFirst(serviceClass).orElse(defaultValue);
    }

    /**
     * Forces reload of SPI services using the context ClassLoader.
     * 使用上下文类加载器强制重新加载 SPI 服务。
     */
    public static <T> List<T> reload(Class<T> serviceClass) {
        return reload(serviceClass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Forces reload of SPI services atomically using compute to prevent concurrent reload races.
     * 使用 compute 原子地强制重新加载 SPI 服务，防止并发 reload 竞争。
     *
     * @param <T> the service type - 服务类型
     * @param serviceClass the service class - 服务类
     * @param classLoader the class loader to use - 使用的类加载器
     * @return the reloaded list of service implementations - 重新加载的服务实现列表
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> reload(Class<T> serviceClass, ClassLoader classLoader) {
        return Collections.unmodifiableList((List<T>) CACHE.compute(serviceClass, (k, oldValue) -> {
            List<T> services = new ArrayList<>();
            ServiceLoader<T> loader = ServiceLoader.load(serviceClass, classLoader);
            for (T service : loader) {
                services.add(service);
            }
            return services;
        }));
    }

    /**
     * Checks if the SPI service exists
     * 检查 SPI 服务是否存在
     */
    public static <T> boolean hasService(Class<T> serviceClass) {
        return !load(serviceClass).isEmpty();
    }

    /**
     * Gets the SPI service count
     * 获取 SPI 服务数量
     */
    public static <T> int count(Class<T> serviceClass) {
        return load(serviceClass).size();
    }

    /**
     * Filters SPI services by type
     * 按类型过滤 SPI 服务
     */
    public static <T, S extends T> List<S> loadByType(Class<T> serviceClass, Class<S> targetType) {
        List<T> services = load(serviceClass);
        List<S> result = new ArrayList<>();
        for (T service : services) {
            if (targetType.isInstance(service)) {
                result.add(targetType.cast(service));
            }
        }
        return result;
    }

    /**
     * Lazily loads SPI service implementations as a Stream without eagerly materializing.
     * 延迟加载 SPI 服务实现为 Stream，不会立即实例化所有实现。
     *
     * @param <T> the service type - 服务类型
     * @param serviceClass the service class - 服务类
     * @return a stream of service implementations - 服务实现的流
     */
    public static <T> Stream<T> loadStream(Class<T> serviceClass) {
        return loadStream(serviceClass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Lazily loads SPI service implementations as a Stream with a specified ClassLoader.
     * 使用指定类加载器延迟加载 SPI 服务实现为 Stream。
     *
     * @param <T> the service type - 服务类型
     * @param serviceClass the service class - 服务类
     * @param classLoader the class loader - 类加载器
     * @return a stream of service implementations - 服务实现的流
     */
    public static <T> Stream<T> loadStream(Class<T> serviceClass, ClassLoader classLoader) {
        ServiceLoader<T> loader = ServiceLoader.load(serviceClass, classLoader);
        return loader.stream().map(ServiceLoader.Provider::get);
    }

    // ==================== Safe Loading | 安全加载 ====================

    /**
     * Loads SPI service implementations, skipping any that throw during instantiation.
     * 加载 SPI 服务实现，跳过实例化时抛出异常的实现。
     *
     * <p>Unlike {@link #load(Class)}, this method does not cache results and does not
     * throw if a provider fails to instantiate. Failed providers are silently skipped.</p>
     * <p>与 {@link #load(Class)} 不同，此方法不缓存结果，且不因提供者实例化失败而抛出异常。
     * 失败的提供者被静默跳过。</p>
     *
     * @param serviceClass the service interface class | 服务接口类
     * @param <T>          the service type | 服务类型
     * @return unmodifiable list of successfully loaded services | 成功加载的服务的不可修改列表
     */
    public static <T> List<T> loadSafe(Class<T> serviceClass) {
        return loadSafe(serviceClass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Loads SPI service implementations with error isolation and a specified ClassLoader.
     * 使用指定类加载器加载 SPI 服务实现，带错误隔离。
     *
     * @param serviceClass the service interface class | 服务接口类
     * @param classLoader  the class loader to use | 使用的类加载器
     * @param <T>          the service type | 服务类型
     * @return unmodifiable list of successfully loaded services | 成功加载的服务的不可修改列表
     */
    public static <T> List<T> loadSafe(Class<T> serviceClass, ClassLoader classLoader) {
        Objects.requireNonNull(serviceClass, "serviceClass must not be null");
        ServiceLoader<T> loader = ServiceLoader.load(serviceClass, classLoader);
        List<T> services = new ArrayList<>();
        for (ServiceLoader.Provider<T> provider : loader.stream().toList()) {
            try {
                services.add(provider.get());
            } catch (Exception | ServiceConfigurationError ignored) {
                // Skip providers that fail to instantiate
            }
        }
        return Collections.unmodifiableList(services);
    }

    // ==================== Ordered Loading | 排序加载 ====================

    /**
     * Loads SPI service implementations sorted by priority.
     * 按优先级排序加载 SPI 服务实现。
     *
     * <p>Services with a {@code getPriority()} or {@code getOrder()} method
     * returning an integer are sorted by that value (lower = higher priority).
     * Services without priority information retain their original loading order.</p>
     * <p>拥有返回整数的 {@code getPriority()} 或 {@code getOrder()} 方法的服务按该值排序
     * （值越小优先级越高）。没有优先级信息的服务保持原始加载顺序。</p>
     *
     * @param serviceClass the service interface class | 服务接口类
     * @param <T>          the service type | 服务类型
     * @return unmodifiable ordered list of services | 排序后的服务不可修改列表
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> List<T> loadOrdered(Class<T> serviceClass) {
        List<T> services = new ArrayList<>(load(serviceClass));
        // Pre-extract priorities in O(n) to avoid O(n log n) reflection during sort
        java.util.IdentityHashMap<T, Integer> priorities = new java.util.IdentityHashMap<>(services.size());
        for (T service : services) {
            priorities.put(service, getPriority(service));
        }
        services.sort(java.util.Comparator.comparingInt(priorities::get));
        return Collections.unmodifiableList(services);
    }

    /**
     * Extracts priority value from a service instance.
     * 从服务实例中提取优先级值。
     *
     * <p>Checks in order: Comparable natural order proxy, getPriority() method, getOrder() method.
     * Returns Integer.MAX_VALUE if no priority information is found.</p>
     */
    private static int getPriority(Object service) {
        // Check for getPriority() method (common pattern: jakarta.annotation.Priority, etc.)
        try {
            java.lang.reflect.Method m = service.getClass().getMethod("getPriority");
            if (m.getReturnType() == int.class || m.getReturnType() == Integer.class) {
                return (int) m.invoke(service);
            }
        } catch (Exception ignored) {
        }
        // Check for getOrder() method (common in Spring-like frameworks)
        try {
            java.lang.reflect.Method m = service.getClass().getMethod("getOrder");
            if (m.getReturnType() == int.class || m.getReturnType() == Integer.class) {
                return (int) m.invoke(service);
            }
        } catch (Exception ignored) {
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Clears the cache
     * 清除缓存
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Clears the cache for the specified class
     * 清除指定类的缓存
     */
    public static void clearCache(Class<?> serviceClass) {
        CACHE.remove(serviceClass);
    }
}
