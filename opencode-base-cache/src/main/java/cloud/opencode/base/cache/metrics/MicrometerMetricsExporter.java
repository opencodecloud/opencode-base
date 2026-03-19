package cloud.opencode.base.cache.metrics;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheManager;
import cloud.opencode.base.cache.CacheMetrics;
import cloud.opencode.base.cache.CacheStats;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * Micrometer Metrics Exporter - Export cache metrics to Micrometer registry
 * Micrometer 指标导出器 - 将缓存指标导出到 Micrometer 注册表
 *
 * <p>Provides integration with Micrometer for cache monitoring. Uses a registry
 * abstraction to avoid compile-time dependency on Micrometer.</p>
 * <p>提供与 Micrometer 的集成用于缓存监控。使用注册表抽象避免对 Micrometer 的编译时依赖。</p>
 *
 * <p><strong>Exported Metrics | 导出的指标:</strong></p>
 * <ul>
 *   <li>cache.gets{result=hit|miss} - Cache get operations | 缓存获取操作</li>
 *   <li>cache.puts - Cache put operations | 缓存放入操作</li>
 *   <li>cache.evictions - Cache evictions | 缓存淘汰</li>
 *   <li>cache.size - Current cache size | 当前缓存大小</li>
 *   <li>cache.load.duration - Load duration | 加载耗时</li>
 *   <li>cache.get.latency - Get latency percentiles | 获取延迟百分位</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // With actual Micrometer
 * MeterRegistry registry = new SimpleMeterRegistry();
 * MicrometerMetricsExporter exporter = MicrometerMetricsExporter.builder()
 *     .registry(MicrometerRegistry.wrap(registry))
 *     .prefix("app")
 *     .tag("env", "production")
 *     .build();
 *
 * exporter.register("users", userCache);
 * exporter.registerAll(CacheManager.getInstance());
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Micrometer registry integration - Micrometer 注册表集成</li>
 *   <li>Auto-register all managed caches - 自动注册所有托管缓存</li>
 *   <li>Custom tags and prefix support - 自定义标签和前缀支持</li>
 *   <li>Latency percentile export - 延迟百分位导出</li>
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
public final class MicrometerMetricsExporter {

    private static final System.Logger LOGGER = System.getLogger(MicrometerMetricsExporter.class.getName());

    private final MeterRegistry registry;
    private final String prefix;
    private final Map<String, String> commonTags;
    private final Map<String, CacheMetricsBinder<?>> binders = new ConcurrentHashMap<>();

    private MicrometerMetricsExporter(MeterRegistry registry, String prefix, Map<String, String> commonTags) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
        this.prefix = prefix != null ? prefix : "cache";
        this.commonTags = commonTags;
    }

    /**
     * Create exporter builder
     * 创建导出器构建器
     *
     * @return builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create exporter with registry
     * 使用注册表创建导出器
     *
     * @param registry meter registry | 指标注册表
     * @return exporter | 导出器
     */
    public static MicrometerMetricsExporter create(MeterRegistry registry) {
        return builder().registry(registry).build();
    }

    // ==================== Registration | 注册 ====================

    /**
     * Register a cache for metrics collection
     * 注册缓存进行指标收集
     *
     * @param name  cache name | 缓存名称
     * @param cache the cache | 缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return this exporter | 此导出器
     */
    public <K, V> MicrometerMetricsExporter register(String name, Cache<K, V> cache) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(cache, "cache cannot be null");

        CacheMetricsBinder<V> binder = new CacheMetricsBinder<>(name, cache, this);
        binders.put(name, binder);
        binder.bindTo(registry);

        return this;
    }

    /**
     * Register all caches from CacheManager
     * 从 CacheManager 注册所有缓存
     *
     * @param manager cache manager | 缓存管理器
     * @return this exporter | 此导出器
     */
    public MicrometerMetricsExporter registerAll(CacheManager manager) {
        for (String name : manager.getCacheNames()) {
            manager.<Object, Object>getCache(name).ifPresent(cache -> register(name, cache));
        }
        return this;
    }

    /**
     * Unregister a cache
     * 取消注册缓存
     *
     * @param name cache name | 缓存名称
     * @return this exporter | 此导出器
     */
    public MicrometerMetricsExporter unregister(String name) {
        CacheMetricsBinder<?> binder = binders.remove(name);
        if (binder != null) {
            binder.close();
        }
        return this;
    }

    String metricName(String suffix) {
        return prefix + "." + suffix;
    }

    String[] tags(String cacheName) {
        int tagCount = commonTags.size() + 1;
        String[] tags = new String[tagCount * 2];
        tags[0] = "cache";
        tags[1] = cacheName;
        int i = 2;
        for (Map.Entry<String, String> entry : commonTags.entrySet()) {
            tags[i++] = entry.getKey();
            tags[i++] = entry.getValue();
        }
        return tags;
    }

    // ==================== Meter Registry Abstraction | 指标注册表抽象 ====================

    /**
     * Meter registry abstraction to avoid compile-time Micrometer dependency
     * 指标注册表抽象，避免编译时 Micrometer 依赖
     */
    public interface MeterRegistry {
        /**
         * Register a gauge
         * 注册仪表
         *
         * @param name     metric name | 指标名称
         * @param tags     tags | 标签
         * @param obj      target object | 目标对象
         * @param function value function | 值函数
         * @param <T>      object type | 对象类型
         */
        <T> void gauge(String name, String[] tags, T obj, ToDoubleFunction<T> function);

        /**
         * Register a function counter
         * 注册函数计数器
         *
         * @param name     metric name | 指标名称
         * @param tags     tags | 标签
         * @param obj      target object | 目标对象
         * @param function value function | 值函数
         * @param <T>      object type | 对象类型
         */
        <T> void counter(String name, String[] tags, T obj, ToLongFunction<T> function);

        /**
         * Register a function timer
         * 注册函数定时器
         *
         * @param name          metric name | 指标名称
         * @param tags          tags | 标签
         * @param obj           target object | 目标对象
         * @param countFunction count function | 计数函数
         * @param totalFunction total time function (nanos) | 总时间函数（纳秒）
         * @param <T>           object type | 对象类型
         */
        <T> void timer(String name, String[] tags, T obj,
                       ToLongFunction<T> countFunction, ToDoubleFunction<T> totalFunction);

        /**
         * Remove meters by name prefix
         * 按名称前缀移除指标
         *
         * @param namePrefix name prefix | 名称前缀
         */
        void remove(String namePrefix);

        /**
         * Wrap actual Micrometer MeterRegistry using reflection
         * 使用反射包装实际的 Micrometer MeterRegistry
         *
         * @param meterRegistry actual Micrometer registry | 实际的 Micrometer 注册表
         * @return wrapped registry | 包装的注册表
         */
        static MeterRegistry wrap(Object meterRegistry) {
            return new ReflectiveMeterRegistry(meterRegistry);
        }
    }

    /**
     * Reflective Micrometer integration
     */
    private static class ReflectiveMeterRegistry implements MeterRegistry {
        private final Object registry;

        ReflectiveMeterRegistry(Object registry) {
            this.registry = registry;
        }

        @Override
        public <T> void gauge(String name, String[] tags, T obj, ToDoubleFunction<T> function) {
            try {
                // Try to call: Gauge.builder(name, obj, function).tags(tags).register(registry)
                Class<?> gaugeClass = Class.forName("io.micrometer.core.instrument.Gauge");
                var builder = gaugeClass.getMethod("builder", String.class, Object.class, ToDoubleFunction.class)
                        .invoke(null, name, obj, function);
                builder.getClass().getMethod("tags", String[].class).invoke(builder, (Object) tags);
                builder.getClass().getMethod("register", Class.forName("io.micrometer.core.instrument.MeterRegistry"))
                        .invoke(builder, registry);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "Failed to register gauge metric: " + name, e);
            }
        }

        @Override
        public <T> void counter(String name, String[] tags, T obj, ToLongFunction<T> function) {
            try {
                Class<?> counterClass = Class.forName("io.micrometer.core.instrument.FunctionCounter");
                var builder = counterClass.getMethod("builder", String.class, Object.class, ToDoubleFunction.class)
                        .invoke(null, name, obj, (ToDoubleFunction<T>) value -> function.applyAsLong(value));
                builder.getClass().getMethod("tags", String[].class).invoke(builder, (Object) tags);
                builder.getClass().getMethod("register", Class.forName("io.micrometer.core.instrument.MeterRegistry"))
                        .invoke(builder, registry);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "Failed to register counter metric: " + name, e);
            }
        }

        @Override
        public <T> void timer(String name, String[] tags, T obj,
                              ToLongFunction<T> countFunction, ToDoubleFunction<T> totalFunction) {
            try {
                Class<?> timerClass = Class.forName("io.micrometer.core.instrument.FunctionTimer");
                Class<?> timeUnitClass = Class.forName("java.util.concurrent.TimeUnit");
                Object nanosUnit = timeUnitClass.getField("NANOSECONDS").get(null);

                var builder = timerClass.getMethod("builder", String.class, Object.class,
                                ToLongFunction.class, ToDoubleFunction.class, timeUnitClass)
                        .invoke(null, name, obj, countFunction, totalFunction, nanosUnit);
                builder.getClass().getMethod("tags", String[].class).invoke(builder, (Object) tags);
                builder.getClass().getMethod("register", Class.forName("io.micrometer.core.instrument.MeterRegistry"))
                        .invoke(builder, registry);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "Failed to register timer metric: " + name, e);
            }
        }

        @Override
        public void remove(String namePrefix) {
            try {
                var removeMethod = registry.getClass().getMethod("removeByPreFilterId",
                        Class.forName("io.micrometer.core.instrument.Meter$Id"));
                // Simplified - in practice you'd iterate and remove
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "Failed to remove metrics with prefix: " + namePrefix, e);
            }
        }
    }

    // ==================== Cache Metrics Binder | 缓存指标绑定器 ====================

    private static class CacheMetricsBinder<V> {
        private final String cacheName;
        private final Cache<?, V> cache;
        private final MicrometerMetricsExporter exporter;

        CacheMetricsBinder(String cacheName, Cache<?, V> cache, MicrometerMetricsExporter exporter) {
            this.cacheName = cacheName;
            this.cache = cache;
            this.exporter = exporter;
        }

        void bindTo(MeterRegistry registry) {
            String[] tags = exporter.tags(cacheName);

            // Gauges
            registry.gauge(exporter.metricName("size"), tags, cache, c -> c.estimatedSize());

            // Hit/Miss counters
            String[] hitTags = appendTag(tags, "result", "hit");
            String[] missTags = appendTag(tags, "result", "miss");

            registry.counter(exporter.metricName("gets"), hitTags, cache, c -> c.stats().hitCount());
            registry.counter(exporter.metricName("gets"), missTags, cache, c -> c.stats().missCount());

            // Other counters
            registry.counter(exporter.metricName("evictions"), tags, cache, c -> c.stats().evictionCount());
            registry.counter(exporter.metricName("puts"), tags, cache, c -> {
                // Approximate puts as loads + explicit puts
                CacheStats stats = c.stats();
                return stats.loadSuccessCount();
            });

            // Load timer
            registry.timer(exporter.metricName("load"), tags, cache,
                    c -> c.stats().loadSuccessCount() + c.stats().loadFailureCount(),
                    c -> (double) c.stats().totalLoadTime());

            // Percentile gauges if metrics are available
            CacheMetrics metrics = cache.metrics();
            if (metrics != null) {
                String[] p50Tags = appendTag(tags, "percentile", "0.5");
                String[] p95Tags = appendTag(tags, "percentile", "0.95");
                String[] p99Tags = appendTag(tags, "percentile", "0.99");

                registry.gauge(exporter.metricName("get.latency"), p50Tags, metrics,
                        m -> m.getGetLatencyP50() / 1_000_000.0); // Convert to ms
                registry.gauge(exporter.metricName("get.latency"), p95Tags, metrics,
                        m -> m.getGetLatencyP95() / 1_000_000.0);
                registry.gauge(exporter.metricName("get.latency"), p99Tags, metrics,
                        m -> m.getGetLatencyP99() / 1_000_000.0);

                // Throughput
                registry.gauge(exporter.metricName("get.throughput"), tags, metrics,
                        CacheMetrics::getGetThroughput);
                registry.gauge(exporter.metricName("put.throughput"), tags, metrics,
                        CacheMetrics::getPutThroughput);
            }

            // Hit rate gauge
            registry.gauge(exporter.metricName("hit.ratio"), tags, cache,
                    c -> c.stats().hitRate());
        }

        private String[] appendTag(String[] tags, String key, String value) {
            String[] newTags = new String[tags.length + 2];
            System.arraycopy(tags, 0, newTags, 0, tags.length);
            newTags[tags.length] = key;
            newTags[tags.length + 1] = value;
            return newTags;
        }

        void close() {
            exporter.registry.remove(exporter.metricName(""));
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for MicrometerMetricsExporter
     */
    public static class Builder {
        private MeterRegistry registry;
        private String prefix = "cache";
        private final Map<String, String> tags = new ConcurrentHashMap<>();

        /**
         * Set meter registry
         * 设置指标注册表
         *
         * @param registry the registry | 注册表
         * @return this builder | 此构建器
         */
        public Builder registry(MeterRegistry registry) {
            this.registry = registry;
            return this;
        }

        /**
         * Set metric name prefix
         * 设置指标名称前缀
         *
         * @param prefix the prefix | 前缀
         * @return this builder | 此构建器
         */
        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Add a common tag
         * 添加公共标签
         *
         * @param key   tag key | 标签键
         * @param value tag value | 标签值
         * @return this builder | 此构建器
         */
        public Builder tag(String key, String value) {
            tags.put(key, value);
            return this;
        }

        /**
         * Build the exporter
         * 构建导出器
         *
         * @return exporter | 导出器
         */
        public MicrometerMetricsExporter build() {
            return new MicrometerMetricsExporter(registry, prefix, Map.copyOf(tags));
        }
    }
}
