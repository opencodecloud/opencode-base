package cloud.opencode.base.cache.metrics;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheManager;
import cloud.opencode.base.cache.CacheMetrics;
import cloud.opencode.base.cache.CacheStats;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Prometheus Metrics Exporter - Export cache metrics in Prometheus format
 * Prometheus 指标导出器 - 以 Prometheus 格式导出缓存指标
 *
 * <p>Exports cache metrics in Prometheus text-based exposition format,
 * suitable for scraping by Prometheus server.</p>
 * <p>以 Prometheus 文本格式导出缓存指标，适合 Prometheus 服务器抓取。</p>
 *
 * <p><strong>Exported Metrics | 导出的指标:</strong></p>
 * <ul>
 *   <li>cache_hits_total - Total cache hits | 总缓存命中数</li>
 *   <li>cache_misses_total - Total cache misses | 总缓存未命中数</li>
 *   <li>cache_requests_total - Total requests | 总请求数</li>
 *   <li>cache_evictions_total - Total evictions | 总淘汰数</li>
 *   <li>cache_size - Current cache size | 当前缓存大小</li>
 *   <li>cache_hit_ratio - Cache hit ratio | 缓存命中率</li>
 *   <li>cache_load_duration_seconds - Load latency histogram | 加载延迟直方图</li>
 *   <li>cache_get_duration_seconds - Get latency percentiles | 获取延迟百分位数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create exporter - 创建导出器
 * PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();
 *
 * // Register caches - 注册缓存
 * exporter.register("users", userCache);
 * exporter.register("products", productCache);
 *
 * // Or auto-register all managed caches - 或自动注册所有托管缓存
 * exporter.registerAll(CacheManager.getInstance());
 *
 * // Export metrics - 导出指标
 * String metrics = exporter.export();
 *
 * // Use in HTTP endpoint - 在 HTTP 端点中使用
 * // GET /metrics -> returns Prometheus format
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Prometheus text exposition format - Prometheus 文本导出格式</li>
 *   <li>Auto-register all managed caches - 自动注册所有托管缓存</li>
 *   <li>Latency histogram export - 延迟直方图导出</li>
 *   <li>Custom label support - 自定义标签支持</li>
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
public final class PrometheusMetricsExporter {

    private static final String METRIC_PREFIX = "cache_";
    private static final String HELP_PREFIX = "# HELP ";
    private static final String TYPE_PREFIX = "# TYPE ";

    private final Map<String, Supplier<CacheStats>> statsSuppliers = new ConcurrentHashMap<>();
    private final Map<String, Supplier<CacheMetrics>> metricsSuppliers = new ConcurrentHashMap<>();
    private final Map<String, Supplier<Long>> sizeSuppliers = new ConcurrentHashMap<>();

    private final String namespace;
    private final Map<String, String> commonLabels;

    private PrometheusMetricsExporter(String namespace, Map<String, String> commonLabels) {
        this.namespace = namespace;
        this.commonLabels = commonLabels;
    }

    /**
     * Create exporter with default settings
     * 使用默认设置创建导出器
     *
     * @return exporter | 导出器
     */
    public static PrometheusMetricsExporter create() {
        return new PrometheusMetricsExporter("", Map.of());
    }

    /**
     * Create exporter with namespace
     * 使用命名空间创建导出器
     *
     * @param namespace metric namespace prefix | 指标命名空间前缀
     * @return exporter | 导出器
     */
    public static PrometheusMetricsExporter create(String namespace) {
        return new PrometheusMetricsExporter(namespace, Map.of());
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

    // ==================== Registration | 注册 ====================

    /**
     * Register a cache for metrics export
     * 注册缓存以导出指标
     *
     * @param name  cache name | 缓存名称
     * @param cache the cache | 缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return this exporter | 此导出器
     */
    public <K, V> PrometheusMetricsExporter register(String name, Cache<K, V> cache) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(cache, "cache must not be null");

        statsSuppliers.put(name, cache::stats);
        sizeSuppliers.put(name, cache::estimatedSize);

        if (cache.metrics() != null) {
            metricsSuppliers.put(name, cache::metrics);
        }

        return this;
    }

    /**
     * Register all caches from CacheManager
     * 从 CacheManager 注册所有缓存
     *
     * @param manager cache manager | 缓存管理器
     * @return this exporter | 此导出器
     */
    public PrometheusMetricsExporter registerAll(CacheManager manager) {
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
    public PrometheusMetricsExporter unregister(String name) {
        statsSuppliers.remove(name);
        metricsSuppliers.remove(name);
        sizeSuppliers.remove(name);
        return this;
    }

    // ==================== Export | 导出 ====================

    /**
     * Export all metrics in Prometheus format
     * 以 Prometheus 格式导出所有指标
     *
     * @return Prometheus format metrics | Prometheus 格式指标
     */
    public String export() {
        StringWriter writer = new StringWriter();
        try {
            export(writer);
        } catch (IOException e) {
            // StringWriter doesn't throw IOException
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    /**
     * Export all metrics to writer
     * 将所有指标导出到写入器
     *
     * @param writer output writer | 输出写入器
     * @throws IOException on write error | 写入错误时抛出
     */
    public void export(Writer writer) throws IOException {
        // Export basic stats metrics
        exportCounter(writer, "hits_total", "Total number of cache hits",
                name -> statsSuppliers.get(name).get().hitCount());
        exportCounter(writer, "misses_total", "Total number of cache misses",
                name -> statsSuppliers.get(name).get().missCount());
        exportCounter(writer, "requests_total", "Total number of cache requests",
                name -> statsSuppliers.get(name).get().requestCount());
        exportCounter(writer, "evictions_total", "Total number of cache evictions",
                name -> statsSuppliers.get(name).get().evictionCount());
        exportCounter(writer, "load_success_total", "Total number of successful cache loads",
                name -> statsSuppliers.get(name).get().loadSuccessCount());
        exportCounter(writer, "load_failure_total", "Total number of failed cache loads",
                name -> statsSuppliers.get(name).get().loadFailureCount());

        // Export gauges
        exportGauge(writer, "size", "Current number of entries in cache",
                name -> sizeSuppliers.get(name).get().doubleValue());
        exportGauge(writer, "hit_ratio", "Cache hit ratio",
                name -> statsSuppliers.get(name).get().hitRate());
        exportGauge(writer, "miss_ratio", "Cache miss ratio",
                name -> statsSuppliers.get(name).get().missRate());

        // Export load time
        exportGauge(writer, "load_duration_seconds_total", "Total time spent loading cache entries",
                name -> statsSuppliers.get(name).get().totalLoadTime() / 1_000_000_000.0);
        exportGauge(writer, "load_duration_seconds_avg", "Average load duration in seconds",
                name -> statsSuppliers.get(name).get().averageLoadPenalty() / 1_000_000_000.0);

        // Export detailed metrics (percentiles) if available
        if (!metricsSuppliers.isEmpty()) {
            exportPercentiles(writer, "get_duration_seconds", "Cache get operation duration",
                    name -> {
                        CacheMetrics m = metricsSuppliers.get(name).get();
                        return new double[]{
                                m.getGetLatencyP50() / 1_000_000_000.0,
                                m.getGetLatencyP95() / 1_000_000_000.0,
                                m.getGetLatencyP99() / 1_000_000_000.0
                        };
                    });
            exportPercentiles(writer, "put_duration_seconds", "Cache put operation duration",
                    name -> {
                        CacheMetrics m = metricsSuppliers.get(name).get();
                        return new double[]{
                                m.getPutLatencyP50() / 1_000_000_000.0,
                                m.getPutLatencyP95() / 1_000_000_000.0,
                                m.getPutLatencyP99() / 1_000_000_000.0
                        };
                    });

            // Export throughput
            exportGauge(writer, "get_throughput", "Cache get operations per second",
                    name -> metricsSuppliers.get(name).get().getGetThroughput());
            exportGauge(writer, "put_throughput", "Cache put operations per second",
                    name -> metricsSuppliers.get(name).get().getPutThroughput());
        }
    }

    // ==================== Private Methods | 私有方法 ====================

    private void exportCounter(Writer writer, String name, String help,
                               java.util.function.Function<String, Long> valueExtractor) throws IOException {
        String metricName = formatMetricName(name);

        writer.write(HELP_PREFIX);
        writer.write(metricName);
        writer.write(" ");
        writer.write(help);
        writer.write("\n");

        writer.write(TYPE_PREFIX);
        writer.write(metricName);
        writer.write(" counter\n");

        for (String cacheName : statsSuppliers.keySet()) {
            try {
                long value = valueExtractor.apply(cacheName);
                writer.write(metricName);
                writer.write(formatLabels(cacheName));
                writer.write(" ");
                writer.write(Long.toString(value));
                writer.write("\n");
            } catch (Exception e) {
                // Skip this cache if error
            }
        }
        writer.write("\n");
    }

    private void exportGauge(Writer writer, String name, String help,
                             java.util.function.Function<String, Double> valueExtractor) throws IOException {
        String metricName = formatMetricName(name);

        writer.write(HELP_PREFIX);
        writer.write(metricName);
        writer.write(" ");
        writer.write(help);
        writer.write("\n");

        writer.write(TYPE_PREFIX);
        writer.write(metricName);
        writer.write(" gauge\n");

        for (String cacheName : statsSuppliers.keySet()) {
            try {
                double value = valueExtractor.apply(cacheName);
                writer.write(metricName);
                writer.write(formatLabels(cacheName));
                writer.write(" ");
                writer.write(formatDouble(value));
                writer.write("\n");
            } catch (Exception e) {
                // Skip this cache if error
            }
        }
        writer.write("\n");
    }

    private void exportPercentiles(Writer writer, String name, String help,
                                   java.util.function.Function<String, double[]> valueExtractor) throws IOException {
        String metricName = formatMetricName(name);

        writer.write(HELP_PREFIX);
        writer.write(metricName);
        writer.write(" ");
        writer.write(help);
        writer.write("\n");

        writer.write(TYPE_PREFIX);
        writer.write(metricName);
        writer.write(" summary\n");

        String[] quantiles = {"0.5", "0.95", "0.99"};

        for (String cacheName : metricsSuppliers.keySet()) {
            try {
                double[] values = valueExtractor.apply(cacheName);
                for (int i = 0; i < quantiles.length && i < values.length; i++) {
                    writer.write(metricName);
                    writer.write(formatLabelsWithQuantile(cacheName, quantiles[i]));
                    writer.write(" ");
                    writer.write(formatDouble(values[i]));
                    writer.write("\n");
                }
            } catch (Exception e) {
                // Skip this cache if error
            }
        }
        writer.write("\n");
    }

    private String formatMetricName(String name) {
        StringBuilder sb = new StringBuilder();
        if (!namespace.isEmpty()) {
            sb.append(namespace).append("_");
        }
        sb.append(METRIC_PREFIX).append(name);
        return sb.toString();
    }

    private String formatLabels(String cacheName) {
        StringBuilder sb = new StringBuilder();
        sb.append("{cache=\"").append(escapeLabelValue(cacheName)).append("\"");
        for (Map.Entry<String, String> label : commonLabels.entrySet()) {
            sb.append(",").append(label.getKey()).append("=\"")
                    .append(escapeLabelValue(label.getValue())).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private String formatLabelsWithQuantile(String cacheName, String quantile) {
        StringBuilder sb = new StringBuilder();
        sb.append("{cache=\"").append(escapeLabelValue(cacheName)).append("\"");
        sb.append(",quantile=\"").append(quantile).append("\"");
        for (Map.Entry<String, String> label : commonLabels.entrySet()) {
            sb.append(",").append(label.getKey()).append("=\"")
                    .append(escapeLabelValue(label.getValue())).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeLabelValue(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private String formatDouble(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "+Inf" : "-Inf";
        }
        return String.format("%.6g", value);
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for PrometheusMetricsExporter
     * PrometheusMetricsExporter 构建器
     */
    public static class Builder {

        /** Creates a new Builder instance | 创建新的 Builder 实例 */
        public Builder() {}
        private String namespace = "";
        private final Map<String, String> labels = new ConcurrentHashMap<>();

        /**
         * Set metric namespace prefix
         * 设置指标命名空间前缀
         *
         * @param namespace namespace | 命名空间
         * @return this builder | 此构建器
         */
        public Builder namespace(String namespace) {
            this.namespace = namespace != null ? namespace : "";
            return this;
        }

        /**
         * Add common label to all metrics
         * 为所有指标添加公共标签
         *
         * @param name  label name | 标签名
         * @param value label value | 标签值
         * @return this builder | 此构建器
         */
        public Builder label(String name, String value) {
            labels.put(name, value);
            return this;
        }

        /**
         * Build the exporter
         * 构建导出器
         *
         * @return exporter | 导出器
         */
        public PrometheusMetricsExporter build() {
            return new PrometheusMetricsExporter(namespace, Map.copyOf(labels));
        }
    }
}
