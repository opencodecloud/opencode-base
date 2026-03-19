package cloud.opencode.base.timeseries.aggregation;

import cloud.opencode.base.timeseries.DataPoint;

import java.util.List;

/**
 * Aggregator
 * 聚合器
 *
 * <p>Interface for aggregating data points.</p>
 * <p>聚合数据点的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface for custom aggregation - 自定义聚合的函数式接口</li>
 *   <li>Default aggregateToResult with error handling - 带错误处理的默认结果聚合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Aggregator agg = SumAggregator.getInstance();
 * double result = agg.aggregate(dataPoints);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes (aggregateToResult handles null/empty) - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@FunctionalInterface
public interface Aggregator {

    /**
     * Aggregate data points
     * 聚合数据点
     *
     * @param points the data points | 数据点
     * @return the aggregated value | 聚合值
     */
    double aggregate(List<DataPoint> points);

    /**
     * Get aggregator name
     * 获取聚合器名称
     *
     * @return the name | 名称
     */
    default String name() {
        return getClass().getSimpleName();
    }

    /**
     * Aggregate and wrap result
     * 聚合并包装结果
     *
     * @param points the data points | 数据点
     * @return the aggregation result | 聚合结果
     */
    default AggregationResult aggregateToResult(List<DataPoint> points) {
        if (points == null || points.isEmpty()) {
            return AggregationResult.empty(name());
        }

        try {
            double value = aggregate(points);
            return AggregationResult.success(
                value,
                points.size(),
                points.getFirst().timestamp(),
                points.getLast().timestamp()
            );
        } catch (Exception e) {
            return AggregationResult.error(e.getMessage());
        }
    }
}
