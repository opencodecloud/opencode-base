package cloud.opencode.base.timeseries.aggregation;

import java.time.Instant;

/**
 * Aggregation Result
 * 聚合结果
 *
 * <p>Sealed interface for aggregation results.</p>
 * <p>聚合结果的密封接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed interface: Success, Empty, Error variants - 密封接口：成功、空、错误变体</li>
 *   <li>Factory methods and default value support - 工厂方法和默认值支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AggregationResult result = aggregator.aggregateToResult(points);
 * double value = result.getValueOrDefault(0.0);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable records) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (getValueOrDefault handles non-success) - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public sealed interface AggregationResult
    permits AggregationResult.Success, AggregationResult.Empty, AggregationResult.Error {

    /**
     * Check if result is successful
     * 检查结果是否成功
     *
     * @return true if successful | 如果成功返回true
     */
    boolean isSuccess();

    /**
     * Get value or default
     * 获取值或默认值
     *
     * @param defaultValue the default value | 默认值
     * @return the value or default | 值或默认值
     */
    default double getValueOrDefault(double defaultValue) {
        return isSuccess() ? ((Success) this).value() : defaultValue;
    }

    /**
     * Success result
     * 成功结果
     *
     * @param value the aggregated value | 聚合值
     * @param count the data point count | 数据点数量
     * @param from the start time | 开始时间
     * @param to the end time | 结束时间
     */
    record Success(double value, long count, Instant from, Instant to) implements AggregationResult {
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    /**
     * Empty result (no data)
     * 空结果（无数据）
     *
     * @param seriesName the series name | 序列名称
     */
    record Empty(String seriesName) implements AggregationResult {
        @Override
        public boolean isSuccess() {
            return false;
        }
    }

    /**
     * Error result
     * 错误结果
     *
     * @param message the error message | 错误消息
     */
    record Error(String message) implements AggregationResult {
        @Override
        public boolean isSuccess() {
            return false;
        }
    }

    /**
     * Create success result
     * 创建成功结果
     *
     * @param value the value | 值
     * @param count the count | 数量
     * @param from the start time | 开始时间
     * @param to the end time | 结束时间
     * @return the result | 结果
     */
    static AggregationResult success(double value, long count, Instant from, Instant to) {
        return new Success(value, count, from, to);
    }

    /**
     * Create empty result
     * 创建空结果
     *
     * @param seriesName the series name | 序列名称
     * @return the result | 结果
     */
    static AggregationResult empty(String seriesName) {
        return new Empty(seriesName);
    }

    /**
     * Create error result
     * 创建错误结果
     *
     * @param message the error message | 错误消息
     * @return the result | 结果
     */
    static AggregationResult error(String message) {
        return new Error(message);
    }
}
