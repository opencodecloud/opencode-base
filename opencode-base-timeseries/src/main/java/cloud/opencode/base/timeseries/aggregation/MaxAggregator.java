package cloud.opencode.base.timeseries.aggregation;

import cloud.opencode.base.timeseries.DataPoint;

import java.util.List;

/**
 * Max Aggregator
 * 最大值聚合器
 *
 * <p>Aggregates data points by finding maximum value.</p>
 * <p>通过找最大值来聚合数据点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Singleton maximum value aggregator - 单例最大值聚合器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * double max = MaxAggregator.getInstance().aggregate(points);
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
public final class MaxAggregator implements Aggregator {

    private static final MaxAggregator INSTANCE = new MaxAggregator();

    private MaxAggregator() {
        // Singleton
    }

    /**
     * Get singleton instance
     * 获取单例实例
     *
     * @return the instance | 实例
     */
    public static MaxAggregator getInstance() {
        return INSTANCE;
    }

    @Override
    public double aggregate(List<DataPoint> points) {
        return points.stream()
            .mapToDouble(DataPoint::value)
            .max()
            .orElse(Double.MIN_VALUE);
    }

    @Override
    public String name() {
        return "MAX";
    }
}
