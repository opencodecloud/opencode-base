package cloud.opencode.base.observability.metric;

/**
 * Gauge - A metric that reports a current value from a supplier
 * Gauge - 从供应者报告当前值的指标
 *
 * <p>Gauges represent a point-in-time value, such as queue size,
 * memory usage, or active thread count.</p>
 * <p>仪表盘表示某一时刻的值，如队列大小、内存使用量或活跃线程数。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public interface Gauge {

    /**
     * Returns the current gauge value.
     * 返回当前仪表盘值。
     *
     * @return the current value | 当前值
     */
    double value();

    /**
     * Returns the metric identifier.
     * 返回指标标识符。
     *
     * @return the MetricId | 指标 ID
     */
    MetricId id();
}
