package cloud.opencode.base.core.spi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
     * Forces reload of SPI services
     * 强制重新加载 SPI 服务
     */
    public static <T> List<T> reload(Class<T> serviceClass) {
        CACHE.remove(serviceClass);
        return load(serviceClass);
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
