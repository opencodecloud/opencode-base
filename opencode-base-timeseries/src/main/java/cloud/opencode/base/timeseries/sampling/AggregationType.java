package cloud.opencode.base.timeseries.sampling;

/**
 * Aggregation Type
 * 聚合类型
 *
 * <p>Enumeration of aggregation types for downsampling.</p>
 * <p>降采样的聚合类型枚举。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SUM, AVG, MIN, MAX, FIRST, LAST, COUNT aggregation types - 多种聚合类型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeries ds = SamplerUtil.downsample(series, interval, AggregationType.AVG);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum constants are immutable) - 线程安全: 是（枚举常量不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public enum AggregationType {

    /**
     * Sum of values | 值的总和
     */
    SUM,

    /**
     * Average of values | 值的平均值
     */
    AVG,

    /**
     * Minimum value | 最小值
     */
    MIN,

    /**
     * Maximum value | 最大值
     */
    MAX,

    /**
     * First value in window | 窗口中的第一个值
     */
    FIRST,

    /**
     * Last value in window | 窗口中的最后一个值
     */
    LAST,

    /**
     * Count of values | 值的数量
     */
    COUNT
}
