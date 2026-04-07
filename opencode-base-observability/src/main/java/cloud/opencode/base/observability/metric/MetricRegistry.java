package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * MetricRegistry - Central registry for creating and managing metrics
 * MetricRegistry - 创建和管理指标的中央注册表
 *
 * <p>Provides factory methods for counters, gauges, timers, and histograms.
 * All registrations are idempotent — registering the same MetricId returns the existing metric.
 * Registering a different type for an existing MetricId throws an exception.</p>
 * <p>提供计数器、仪表盘、计时器和直方图的工厂方法。
 * 所有注册都是幂等的 — 注册相同的 MetricId 返回已有指标。
 * 为已有 MetricId 注册不同类型将抛出异常。</p>
 *
 * <p><strong>Thread safety | 线程安全:</strong> All operations are thread-safe via ConcurrentHashMap.</p>
 * <p>所有操作通过 ConcurrentHashMap 实现线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public final class MetricRegistry {

    private static final int DEFAULT_MAX_METRICS = 10_000;

    private final ConcurrentHashMap<MetricId, Object> metrics = new ConcurrentHashMap<>();
    private final int maxMetrics;

    private MetricRegistry(int maxMetrics) {
        if (maxMetrics <= 0) {
            throw new ObservabilityException("INVALID_CONFIG", "maxMetrics must be positive");
        }
        this.maxMetrics = maxMetrics;
    }

    /**
     * Creates a registry with the default max metrics limit (10,000).
     * 使用默认最大指标限制（10,000）创建注册表。
     *
     * @return a new MetricRegistry | 新的 MetricRegistry
     */
    public static MetricRegistry create() {
        return new MetricRegistry(DEFAULT_MAX_METRICS);
    }

    /**
     * Creates a registry with the specified max metrics limit.
     * 使用指定的最大指标限制创建注册表。
     *
     * @param maxMetrics the maximum number of metrics | 最大指标数量
     * @return a new MetricRegistry | 新的 MetricRegistry
     * @throws ObservabilityException if maxMetrics is not positive | 如果 maxMetrics 不为正
     */
    public static MetricRegistry create(int maxMetrics) {
        return new MetricRegistry(maxMetrics);
    }

    /**
     * Registers or returns an existing counter.
     * 注册或返回已有的计数器。
     *
     * @param name the metric name | 指标名称
     * @param tags the optional tags | 可选标签
     * @return the Counter instance | 计数器实例
     * @throws ObservabilityException if a different metric type is already registered, or limit exceeded
     *                                如果已注册了不同的指标类型，或超出限制
     */
    public Counter counter(String name, Tag... tags) {
        MetricId id = MetricId.of(name, tags);
        return getOrRegister(id, Counter.class, () -> new DefaultCounter(id));
    }

    /**
     * Registers or returns an existing gauge.
     * 注册或返回已有的仪表盘。
     *
     * @param name     the metric name | 指标名称
     * @param supplier the value supplier, must not be null | 值供应者，不能为 null
     * @param tags     the optional tags | 可选标签
     * @return the Gauge instance | 仪表盘实例
     * @throws ObservabilityException if supplier is null, a different type is registered, or limit exceeded
     *                                如果 supplier 为 null、已注册不同类型或超出限制
     */
    public Gauge gauge(String name, Supplier<Double> supplier, Tag... tags) {
        if (supplier == null) {
            throw new ObservabilityException("INVALID_METRIC", "Gauge supplier must not be null");
        }
        MetricId id = MetricId.of(name, tags);
        return getOrRegister(id, Gauge.class, () -> new DefaultGauge(id, supplier));
    }

    /**
     * Registers or returns an existing timer.
     * 注册或返回已有的计时器。
     *
     * @param name the metric name | 指标名称
     * @param tags the optional tags | 可选标签
     * @return the Timer instance | 计时器实例
     * @throws ObservabilityException if a different metric type is already registered, or limit exceeded
     *                                如果已注册了不同的指标类型，或超出限制
     */
    public Timer timer(String name, Tag... tags) {
        MetricId id = MetricId.of(name, tags);
        return getOrRegister(id, Timer.class, () -> new DefaultTimer(id));
    }

    /**
     * Registers or returns an existing histogram.
     * 注册或返回已有的直方图。
     *
     * @param name the metric name | 指标名称
     * @param tags the optional tags | 可选标签
     * @return the Histogram instance | 直方图实例
     * @throws ObservabilityException if a different metric type is already registered, or limit exceeded
     *                                如果已注册了不同的指标类型，或超出限制
     */
    public Histogram histogram(String name, Tag... tags) {
        MetricId id = MetricId.of(name, tags);
        return getOrRegister(id, Histogram.class, () -> new DefaultHistogram(id));
    }

    /**
     * Finds the first metric matching the given name.
     * 查找第一个匹配给定名称的指标。
     *
     * @param name the metric name to search for | 要搜索的指标名称
     * @return an Optional containing the metric, or empty | 包含指标的 Optional，或空
     */
    public Optional<?> find(String name) {
        for (var entry : metrics.entrySet()) {
            if (entry.getKey().name().equals(name)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * Returns a snapshot of all registered metrics.
     * 返回所有已注册指标的快照。
     *
     * @return a list of MetricSnapshot | MetricSnapshot 列表
     */
    public List<MetricSnapshot> snapshot() {
        List<MetricSnapshot> result = new ArrayList<>();
        for (var entry : metrics.entrySet()) {
            MetricId id = entry.getKey();
            Object metric = entry.getValue();
            try {
                result.add(toSnapshot(id, metric));
            } catch (Exception e) {
                result.add(new MetricSnapshot(id, "error",
                        Map.of("error", e.getMessage() != null ? e.getMessage() : e.getClass().getName())));
            }
        }
        return List.copyOf(result);
    }

    /**
     * Removes the metric with the given id.
     * 移除给定 ID 的指标。
     *
     * @param id the MetricId to remove | 要移除的 MetricId
     * @return true if the metric was removed | 如果指标被移除则返回 true
     */
    public boolean remove(MetricId id) {
        return metrics.remove(id) != null;
    }

    /**
     * Removes all registered metrics.
     * 移除所有已注册的指标。
     */
    public void clear() {
        metrics.clear();
    }

    /**
     * Returns the number of registered metrics.
     * 返回已注册指标的数量。
     *
     * @return the count | 数量
     */
    public int size() {
        return metrics.size();
    }

    // ==================== Internal ====================

    @SuppressWarnings("unchecked")
    private <T> T getOrRegister(MetricId id, Class<T> type, Supplier<T> factory) {
        // Fast path: lock-free get() for already-registered metrics (the 99.9% steady-state case).
        // 快速路径：对已注册指标的无锁 get()（稳态下 99.9% 的场景）。
        Object fastHit = metrics.get(id);
        if (fastHit != null) {
            if (!type.isInstance(fastHit)) {
                throw new ObservabilityException("METRIC_TYPE_CONFLICT",
                        "MetricId '" + id.name() + "' is already registered as "
                                + fastHit.getClass().getSimpleName() + ", cannot register as " + type.getSimpleName());
            }
            return (T) fastHit;
        }
        // Slow path: compute() with bin-level lock for first registration.
        // 慢路径：首次注册时使用 compute() 的 bin 级锁。
        Object[] result = new Object[1];
        metrics.compute(id, (key, existing) -> {
            if (existing != null) {
                if (!type.isInstance(existing)) {
                    throw new ObservabilityException("METRIC_TYPE_CONFLICT",
                            "MetricId '" + id.name() + "' is already registered as "
                                    + existing.getClass().getSimpleName() + ", cannot register as " + type.getSimpleName());
                }
                result[0] = existing;
                return existing;
            }
            // Atomic capacity check within compute lock stripe
            if (metrics.size() >= maxMetrics) {
                throw new ObservabilityException("REGISTRY_FULL",
                        "MetricRegistry is full (max=" + maxMetrics + ")");
            }
            T created = factory.get();
            result[0] = created;
            return created;
        });
        return (T) result[0];
    }

    private MetricSnapshot toSnapshot(MetricId id, Object metric) {
        Map<String, Object> values = new LinkedHashMap<>();
        String type;

        switch (metric) {
            case Counter c -> {
                type = "counter";
                values.put("count", c.count());
            }
            case Gauge g -> {
                type = "gauge";
                values.put("value", g.value());
            }
            case Timer t -> {
                type = "timer";
                values.put("count", t.count());
                values.put("totalTime", t.totalTime());
                values.put("max", t.max());
                values.put("mean", t.mean());
            }
            case Histogram h -> {
                type = "histogram";
                values.put("count", h.count());
                values.put("totalAmount", h.totalAmount());
                values.put("max", h.max());
                values.put("mean", h.mean());
            }
            default -> {
                type = "unknown";
            }
        }
        return new MetricSnapshot(id, type, values);
    }
}
