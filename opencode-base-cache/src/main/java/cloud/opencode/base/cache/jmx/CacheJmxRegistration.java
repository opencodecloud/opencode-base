package cloud.opencode.base.cache.jmx;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheManager;
import cloud.opencode.base.cache.CacheMetrics;
import cloud.opencode.base.cache.CacheStats;
import cloud.opencode.base.cache.config.CacheConfig;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache JMX Registration - Register and manage cache MBeans
 * 缓存 JMX 注册 - 注册和管理缓存 MBean
 *
 * <p>Provides utilities for registering cache instances as JMX MBeans,
 * enabling monitoring and management via JMX clients.</p>
 * <p>提供将缓存实例注册为 JMX MBean 的工具，
 * 支持通过 JMX 客户端进行监控和管理。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register single cache - 注册单个缓存
 * CacheJmxRegistration.register(cache);
 *
 * // Register all caches from manager - 从管理器注册所有缓存
 * CacheJmxRegistration.registerAll(CacheManager.getInstance());
 *
 * // Custom domain - 自定义域
 * CacheJmxRegistration.register(cache, "com.example.cache");
 *
 * // Unregister - 取消注册
 * CacheJmxRegistration.unregister(cache);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JMX MBean registration - JMX MBean 注册</li>
 *   <li>Custom domain support - 自定义域支持</li>
 *   <li>Bulk registration from CacheManager - 从 CacheManager 批量注册</li>
 *   <li>Automatic unregistration - 自动取消注册</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class CacheJmxRegistration {

    private static final String DEFAULT_DOMAIN = "cloud.opencode.base.cache";
    private static final Map<String, ObjectName> registeredBeans = new ConcurrentHashMap<>();

    private CacheJmxRegistration() {
    }

    // ==================== Registration | 注册 ====================

    /**
     * Register cache with default domain
     * 使用默认域注册缓存
     *
     * @param cache the cache to register | 要注册的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return object name | 对象名称
     */
    public static <K, V> ObjectName register(Cache<K, V> cache) {
        return register(cache, DEFAULT_DOMAIN);
    }

    /**
     * Register cache with custom domain
     * 使用自定义域注册缓存
     *
     * @param cache  the cache to register | 要注册的缓存
     * @param domain JMX domain | JMX 域
     * @param <K>    key type | 键类型
     * @param <V>    value type | 值类型
     * @return object name | 对象名称
     */
    public static <K, V> ObjectName register(Cache<K, V> cache, String domain) {
        Objects.requireNonNull(cache, "cache must not be null");
        Objects.requireNonNull(domain, "domain must not be null");

        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = createObjectName(domain, cache.name());

            // Unregister if already registered
            if (mbs.isRegistered(objectName)) {
                mbs.unregisterMBean(objectName);
            }

            CacheMXBeanImpl<K, V> mbean = new CacheMXBeanImpl<>(cache);
            mbs.registerMBean(mbean, objectName);
            registeredBeans.put(cache.name(), objectName);

            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to register cache MBean: " + cache.name(), e);
        }
    }

    /**
     * Register all caches from CacheManager
     * 从 CacheManager 注册所有缓存
     *
     * @param manager cache manager | 缓存管理器
     */
    public static void registerAll(CacheManager manager) {
        registerAll(manager, DEFAULT_DOMAIN);
    }

    /**
     * Register all caches from CacheManager with custom domain
     * 使用自定义域从 CacheManager 注册所有缓存
     *
     * @param manager cache manager | 缓存管理器
     * @param domain  JMX domain | JMX 域
     */
    public static void registerAll(CacheManager manager, String domain) {
        for (String name : manager.getCacheNames()) {
            manager.<Object, Object>getCache(name).ifPresent(cache -> register(cache, domain));
        }
    }

    /**
     * Unregister cache
     * 取消注册缓存
     *
     * @param cache the cache to unregister | 要取消注册的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     */
    public static <K, V> void unregister(Cache<K, V> cache) {
        unregister(cache.name());
    }

    /**
     * Unregister cache by name
     * 按名称取消注册缓存
     *
     * @param cacheName cache name | 缓存名称
     */
    public static void unregister(String cacheName) {
        ObjectName objectName = registeredBeans.remove(cacheName);
        if (objectName != null) {
            try {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                if (mbs.isRegistered(objectName)) {
                    mbs.unregisterMBean(objectName);
                }
            } catch (Exception e) {
                // Ignore unregistration errors
            }
        }
    }

    /**
     * Unregister all caches
     * 取消注册所有缓存
     */
    public static void unregisterAll() {
        for (String name : registeredBeans.keySet()) {
            unregister(name);
        }
    }

    /**
     * Check if cache is registered
     * 检查缓存是否已注册
     *
     * @param cacheName cache name | 缓存名称
     * @return true if registered | 如果已注册返回 true
     */
    public static boolean isRegistered(String cacheName) {
        return registeredBeans.containsKey(cacheName);
    }

    private static ObjectName createObjectName(String domain, String cacheName) throws MalformedObjectNameException {
        return new ObjectName(domain + ":type=Cache,name=" + ObjectName.quote(cacheName));
    }

    // ==================== MBean Implementation | MBean 实现 ====================

    /**
     * CacheMXBean implementation
     * CacheMXBean 实现
     */
    private static class CacheMXBeanImpl<K, V> implements CacheMXBean {

        private final Cache<K, V> cache;

        CacheMXBeanImpl(Cache<K, V> cache) {
            this.cache = cache;
        }

        @Override
        public String getName() {
            return cache.name();
        }

        @Override
        public long getSize() {
            return cache.estimatedSize();
        }

        @Override
        public long getHitCount() {
            return cache.stats().hitCount();
        }

        @Override
        public long getMissCount() {
            return cache.stats().missCount();
        }

        @Override
        public long getRequestCount() {
            return cache.stats().requestCount();
        }

        @Override
        public double getHitRatio() {
            return cache.stats().hitRate();
        }

        @Override
        public double getMissRatio() {
            return cache.stats().missRate();
        }

        @Override
        public long getEvictionCount() {
            return cache.stats().evictionCount();
        }

        @Override
        public long getLoadSuccessCount() {
            return cache.stats().loadSuccessCount();
        }

        @Override
        public long getLoadFailureCount() {
            return cache.stats().loadFailureCount();
        }

        @Override
        public double getAverageLoadTimeMillis() {
            return cache.stats().averageLoadPenalty() / 1_000_000.0;
        }

        @Override
        public double getGetLatencyP50Micros() {
            CacheMetrics metrics = cache.metrics();
            return metrics != null ? metrics.getGetLatencyP50() / 1000.0 : 0;
        }

        @Override
        public double getGetLatencyP95Micros() {
            CacheMetrics metrics = cache.metrics();
            return metrics != null ? metrics.getGetLatencyP95() / 1000.0 : 0;
        }

        @Override
        public double getGetLatencyP99Micros() {
            CacheMetrics metrics = cache.metrics();
            return metrics != null ? metrics.getGetLatencyP99() / 1000.0 : 0;
        }

        @Override
        public void clear() {
            cache.invalidateAll();
        }

        @Override
        public void cleanup() {
            cache.cleanUp();
        }

        @Override
        public void resetStatistics() {
            CacheMetrics metrics = cache.metrics();
            if (metrics != null) {
                metrics.reset();
            }
        }

        @Override
        public long getMaximumSize() {
            // This would require access to config, return -1 if not available
            return -1;
        }

        @Override
        public boolean isStatisticsEnabled() {
            return cache.stats() != null && cache.stats() != CacheStats.empty();
        }

        @Override
        public String getExpirationConfig() {
            return "N/A";
        }
    }
}
