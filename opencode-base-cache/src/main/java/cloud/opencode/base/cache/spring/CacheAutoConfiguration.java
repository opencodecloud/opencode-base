package cloud.opencode.base.cache.spring;

import cloud.opencode.base.cache.CacheManager;
import cloud.opencode.base.cache.config.CacheConfig;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Cache Auto-Configuration for Spring Boot
 * 缓存自动配置类 - 用于 Spring Boot 自动配置
 *
 * <p>This class provides Spring Boot auto-configuration support without requiring
 * Spring as a compile-time dependency. It uses reflection to interact with Spring.</p>
 * <p>此类提供 Spring Boot 自动配置支持，无需 Spring 作为编译时依赖。它使用反射与 Spring 交互。</p>
 *
 * <p><strong>Configuration Properties | 配置属性:</strong></p>
 * <pre>{@code
 * # application.yml
 * opencode:
 *   cache:
 *     enabled: true
 *     allow-null-values: true
 *     default:
 *       maximum-size: 10000
 *       expire-after-write: 30m
 *       expire-after-access: 10m
 *       record-stats: true
 *     caches:
 *       users:
 *         maximum-size: 5000
 *         expire-after-write: 1h
 *       products:
 *         maximum-size: 50000
 *         expire-after-write: 24h
 * }</pre>
 *
 * <p><strong>Usage with Spring Boot | Spring Boot 使用方式:</strong></p>
 * <pre>{@code
 * // 1. Add dependency in pom.xml
 * <dependency>
 *     <groupId>cloud.opencode.base</groupId>
 *     <artifactId>opencode-base-cache</artifactId>
 * </dependency>
 *
 * // 2. Enable caching in configuration class
 * @Configuration
 * @EnableCaching
 * public class CacheConfiguration {
 *     // Auto-configuration will provide CacheManager bean
 * }
 *
 * // 3. Use Spring Cache annotations
 * @Service
 * public class UserService {
 *     @Cacheable("users")
 *     public User findById(Long id) {
 *         return userRepository.findById(id);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Auto-detection of Spring Boot environment - 自动检测 Spring Boot 环境</li>
 *   <li>YAML/properties configuration binding - YAML/属性配置绑定</li>
 *   <li>Per-cache and default configuration - 每缓存和默认配置</li>
 *   <li>Conditional bean registration - 条件化 Bean 注册</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Auto-configured by Spring Boot when on classpath
 * // No explicit usage needed - Spring detects and configures automatically
 * }</pre>
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
public final class CacheAutoConfiguration {

    private CacheAutoConfiguration() {
    }

    /**
     * Create OpenCodeCacheManager from CacheProperties
     * 从 CacheProperties 创建 OpenCodeCacheManager
     *
     * @param properties the cache properties | 缓存属性
     * @return configured cache manager | 配置的缓存管理器
     */
    public static OpenCodeCacheManager createCacheManager(CacheProperties properties) {
        return createCacheManager(properties, true);
    }

    /**
     * Create OpenCodeCacheManager from CacheProperties with optional validation
     * 从 CacheProperties 创建 OpenCodeCacheManager，可选验证
     *
     * @param properties the cache properties | 缓存属性
     * @param validate   whether to validate configuration | 是否验证配置
     * @return configured cache manager | 配置的缓存管理器
     * @throws IllegalArgumentException if validation is enabled and configuration is invalid
     * @since V2.0.1
     */
    public static OpenCodeCacheManager createCacheManager(CacheProperties properties, boolean validate) {
        if (validate) {
            properties.validate();
        }

        if (!properties.isEnabled()) {
            return OpenCodeCacheManager.create();
        }

        OpenCodeCacheManager.Builder builder = OpenCodeCacheManager.builder()
                .allowNullValues(properties.isAllowNullValues());

        // Apply default configuration
        CacheProperties.CacheSpec defaultSpec = properties.getDefaultSpec();
        if (defaultSpec != null) {
            builder.defaultConfig(configBuilder -> applySpec(configBuilder, defaultSpec));
        }

        // Apply named cache configurations
        for (Map.Entry<String, CacheProperties.CacheSpec> entry : properties.getCaches().entrySet()) {
            String cacheName = entry.getKey();
            CacheProperties.CacheSpec spec = entry.getValue();
            builder.cache(cacheName, configBuilder -> applySpec(configBuilder, spec));
        }

        return builder.build();
    }

    /**
     * Apply CacheSpec to CacheConfig.Builder
     */
    private static void applySpec(CacheConfig.Builder<Object, Object> builder, CacheProperties.CacheSpec spec) {
        builder.maximumSize(spec.getMaximumSize());

        if (spec.getMaximumWeight() > 0) {
            builder.maximumWeight(spec.getMaximumWeight());
        }

        if (spec.getExpireAfterWrite() != null) {
            builder.expireAfterWrite(spec.getExpireAfterWrite());
        }

        if (spec.getExpireAfterAccess() != null) {
            builder.expireAfterAccess(spec.getExpireAfterAccess());
        }

        if (spec.getRefreshAfterWrite() != null) {
            builder.refreshAfterWrite(spec.getRefreshAfterWrite());
        }

        builder.initialCapacity(spec.getInitialCapacity());

        if (spec.isRecordStats()) {
            builder.recordStats();
        }

        // Apply eviction policy
        String evictionPolicy = spec.getEvictionPolicy();
        if (evictionPolicy != null) {
            switch (evictionPolicy.toLowerCase()) {
                case "lru" -> builder.evictionPolicy(cloud.opencode.base.cache.spi.EvictionPolicy.lru());
                case "lfu" -> builder.evictionPolicy(cloud.opencode.base.cache.spi.EvictionPolicy.lfu());
                case "fifo" -> builder.evictionPolicy(cloud.opencode.base.cache.spi.EvictionPolicy.fifo());
                case "wtinylfu", "w-tinylfu" -> builder.evictionPolicy(cloud.opencode.base.cache.spi.EvictionPolicy.wTinyLfu());
                default -> {
                    // Default to LRU
                }
            }
        }
    }

    /**
     * Configuration factory for manual Spring integration
     * 用于手动 Spring 集成的配置工厂
     *
     * <p>Example usage in Spring configuration:</p>
     * <pre>{@code
     * @Configuration
     * @EnableCaching
     * public class CacheConfiguration {
     *
     *     @Bean
     *     @ConfigurationProperties(prefix = "opencode.cache")
     *     public CacheProperties cacheProperties() {
     *         return new CacheProperties();
     *     }
     *
     *     @Bean
     *     public CacheManager cacheManager(CacheProperties properties) {
     *         return CacheAutoConfiguration.createCacheManager(properties);
     *     }
     * }
     * }</pre>
     */
    public static class ConfigurationFactory {

        /**
         * Create cache manager with programmatic configuration
         * 使用编程配置创建缓存管理器
         *
         * @param configurer configuration consumer | 配置消费者
         * @return cache manager | 缓存管理器
         */
        public static OpenCodeCacheManager create(Consumer<CacheProperties> configurer) {
            CacheProperties properties = new CacheProperties();
            configurer.accept(properties);
            return createCacheManager(properties);
        }

        /**
         * Create simple cache manager with basic settings
         * 使用基本设置创建简单缓存管理器
         *
         * @param maximumSize     maximum entries per cache | 每个缓存的最大条目数
         * @param expireAfterWrite expire after write duration | 写入后过期时间
         * @return cache manager | 缓存管理器
         */
        public static OpenCodeCacheManager createSimple(long maximumSize, Duration expireAfterWrite) {
            return create(props -> {
                CacheProperties.CacheSpec spec = props.getDefaultSpec();
                spec.setMaximumSize(maximumSize);
                spec.setExpireAfterWrite(expireAfterWrite);
            });
        }

        /**
         * Create cache manager with named caches
         * 创建带命名缓存的缓存管理器
         *
         * @param cacheNames cache names to pre-create | 要预创建的缓存名称
         * @return cache manager | 缓存管理器
         */
        public static OpenCodeCacheManager createWithCaches(String... cacheNames) {
            OpenCodeCacheManager.Builder builder = OpenCodeCacheManager.builder();
            for (String name : cacheNames) {
                builder.cache(name, config -> {
                });
            }
            return builder.build();
        }
    }

    // ==================== Spring META-INF Configuration ====================

    /**
     * Get the auto-configuration class name for META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
     * 获取自动配置类名，用于 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
     *
     * <p>Create this file in resources:</p>
     * <pre>
     * # META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
     * cloud.opencode.base.cache.spring.CacheAutoConfiguration
     * </pre>
     *
     * @return fully qualified class name | 完全限定类名
     */
    public static String getAutoConfigurationClassName() {
        return CacheAutoConfiguration.class.getName();
    }

    /**
     * Get the properties class name for @ConfigurationProperties
     * 获取属性类名，用于 @ConfigurationProperties
     *
     * @return fully qualified class name | 完全限定类名
     */
    public static String getPropertiesClassName() {
        return CacheProperties.class.getName();
    }

    /**
     * Get the cache manager class name for bean registration
     * 获取缓存管理器类名，用于 bean 注册
     *
     * @return fully qualified class name | 完全限定类名
     */
    public static String getCacheManagerClassName() {
        return OpenCodeCacheManager.class.getName();
    }
}
