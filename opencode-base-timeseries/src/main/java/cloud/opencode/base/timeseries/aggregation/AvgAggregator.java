package cloud.opencode.base.timeseries.aggregation;

import cloud.opencode.base.timeseries.DataPoint;

import java.util.List;

/**
 * Avg Aggregator
 * 平均值聚合器
 *
 * <p>Aggregates data points by averaging values.</p>
 * <p>通过平均值来聚合数据点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Singleton average aggregator - 单例平均值聚合器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * double avg = AvgAggregator.getInstance().aggregate(points);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless singleton) - 线程安全: 是（无状态单例）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class AvgAggregator implements Aggregator {

    private static final AvgAggregator INSTANCE = new AvgAggregator();

    private AvgAggregator() {
        // Singleton
    }

    /**
     * Get singleton instance
     * 获取单例实例
     *
     * @return the instance | 实例
     */
    public static AvgAggregator getInstance() {
        return INSTANCE;
    }

    @Override
    public double aggregate(List<DataPoint> points) {
        return points.stream()
            .mapToDouble(DataPoint::value)
            .average()
            .orElse(0);
    }

    @Override
    public String name() {
        return "AVG";
    }
}
