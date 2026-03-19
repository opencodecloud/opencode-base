package cloud.opencode.base.timeseries.aggregation;

import cloud.opencode.base.timeseries.DataPoint;

import java.util.List;

/**
 * Min Aggregator
 * 最小值聚合器
 *
 * <p>Aggregates data points by finding minimum value.</p>
 * <p>通过找最小值来聚合数据点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Singleton minimum value aggregator - 单例最小值聚合器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * double min = MinAggregator.getInstance().aggregate(points);
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
public final class MinAggregator implements Aggregator {

    private static final MinAggregator INSTANCE = new MinAggregator();

    private MinAggregator() {
        // Singleton
    }

    /**
     * Get singleton instance
     * 获取单例实例
     *
     * @return the instance | 实例
     */
    public static MinAggregator getInstance() {
        return INSTANCE;
    }

    @Override
    public double aggregate(List<DataPoint> points) {
        return points.stream()
            .mapToDouble(DataPoint::value)
            .min()
            .orElse(Double.MAX_VALUE);
    }

    @Override
    public String name() {
        return "MIN";
    }
}
