package cloud.opencode.base.pool.metrics;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Observability Metrics Exporter with Optional Observability Module Delegation
 * 支持可选 Observability 模块委托的指标导出器
 *
 * <p>Exports pool metrics to OpenMetrics when the Observability module is available.
 * Registers gauges for pool statistics that are automatically updated.</p>
 * <p>当 Observability 模块可用时，将池指标导出到 OpenMetrics。
 * 注册自动更新的池统计仪表。</p>
 *
 * <p><strong>Exported Metrics | 导出的指标:</strong></p>
 * <ul>
 *   <li>{@code pool.borrow.total} - Total borrow count | 总借用次数</li>
 *   <li>{@code pool.return.total} - Total return count | 总归还次数</li>
 *   <li>{@code pool.created.total} - Total objects created | 总创建对象数</li>
 *   <li>{@code pool.destroyed.total} - Total objects destroyed | 总销毁对象数</li>
 *   <li>{@code pool.wait.avg.ms} - Average wait duration | 平均等待时长</li>
 *   <li>{@code pool.borrow.avg.ms} - Average borrow duration | 平均借用时长</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Optional observability module integration - 可选的 Observability 模块集成</li>
 *   <li>Automatic gauge registration for pool statistics - 自动注册池统计仪表</li>
 *   <li>Tag-based metric grouping - 基于标签的指标分组</li>
 *   <li>Graceful degradation when module unavailable - 模块不可用时优雅降级</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Export pool metrics to Observability
 * ObjectPool<Connection> pool = OpenPool.builder(...).build();
 * ObservabilityMetricsExporter.export("db-pool", pool.getMetrics());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (utility class with static methods) - 线程安全: 是（静态方法的工具类）</li>
 *   <li>Null-safe: Yes (null metrics ignored) - 空值安全: 是（空指标被忽略）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public final class ObservabilityMetricsExporter {

    private static final MethodHandle GAUGE_HANDLE;
    private static final MethodHandle GAUGE_WITH_TAGS_HANDLE;

    static {
        GAUGE_HANDLE = initGaugeHandle();
        GAUGE_WITH_TAGS_HANDLE = initGaugeWithTagsHandle();
    }

    private ObservabilityMetricsExporter() {
    }

    private static MethodHandle initGaugeHandle() {
        try {
            Class<?> openMetricsClass = Class.forName("cloud.opencode.base.observability.OpenMetrics");
            Class<?> gaugeClass = Class.forName("cloud.opencode.base.observability.metrics.GaugeMetric");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findStatic(openMetricsClass, "gauge",
                    MethodType.methodType(gaugeClass, String.class, Supplier.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    private static MethodHandle initGaugeWithTagsHandle() {
        try {
            Class<?> openMetricsClass = Class.forName("cloud.opencode.base.observability.OpenMetrics");
            Class<?> gaugeClass = Class.forName("cloud.opencode.base.observability.metrics.GaugeMetric");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findStatic(openMetricsClass, "gauge",
                    MethodType.methodType(gaugeClass, String.class, Supplier.class, Map.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Checks if the Observability module is available.
     * 检查 Observability 模块是否可用
     *
     * @return true if Observability module is available | 如果 Observability 模块可用返回 true
     */
    public static boolean isObservabilityModuleAvailable() {
        return GAUGE_HANDLE != null;
    }

    /**
     * Exports pool metrics to Observability.
     * 将池指标导出到 Observability
     *
     * @param poolName the pool name for tagging | 用于标记的池名称
     * @param metrics the pool metrics to export | 要导出的池指标
     */
    public static void export(String poolName, PoolMetrics metrics) {
        if (!isObservabilityModuleAvailable() || metrics == null) {
            return;
        }

        Map<String, String> tags = Map.of("pool", poolName);

        registerGauge("pool.borrow.total", () -> metrics.getBorrowCount(), tags);
        registerGauge("pool.return.total", () -> metrics.getReturnCount(), tags);
        registerGauge("pool.created.total", () -> metrics.getCreatedCount(), tags);
        registerGauge("pool.destroyed.total", () -> metrics.getDestroyedCount(), tags);
        registerGauge("pool.wait.avg.ms", () -> metrics.getAverageWaitDuration().toMillis(), tags);
        registerGauge("pool.borrow.avg.ms", () -> metrics.getAverageBorrowDuration().toMillis(), tags);
        registerGauge("pool.borrow.max.ms", () -> metrics.getMaxBorrowDuration().toMillis(), tags);
    }

    /**
     * Exports pool metrics with custom prefix.
     * 使用自定义前缀导出池指标
     *
     * @param prefix the metric name prefix | 指标名称前缀
     * @param poolName the pool name for tagging | 用于标记的池名称
     * @param metrics the pool metrics to export | 要导出的池指标
     */
    public static void export(String prefix, String poolName, PoolMetrics metrics) {
        if (!isObservabilityModuleAvailable() || metrics == null) {
            return;
        }

        Map<String, String> tags = Map.of("pool", poolName);

        registerGauge(prefix + ".borrow.total", () -> metrics.getBorrowCount(), tags);
        registerGauge(prefix + ".return.total", () -> metrics.getReturnCount(), tags);
        registerGauge(prefix + ".created.total", () -> metrics.getCreatedCount(), tags);
        registerGauge(prefix + ".destroyed.total", () -> metrics.getDestroyedCount(), tags);
        registerGauge(prefix + ".wait.avg.ms", () -> metrics.getAverageWaitDuration().toMillis(), tags);
        registerGauge(prefix + ".borrow.avg.ms", () -> metrics.getAverageBorrowDuration().toMillis(), tags);
    }

    private static void registerGauge(String name, Supplier<Number> supplier, Map<String, String> tags) {
        try {
            GAUGE_WITH_TAGS_HANDLE.invokeWithArguments(name, supplier, tags);
        } catch (Throwable e) {
            // Ignore metrics registration errors
        }
    }
}
