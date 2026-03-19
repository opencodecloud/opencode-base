package cloud.opencode.base.timeseries.sampling;

/**
 * Fill Strategy
 * 填充策略
 *
 * <p>Strategy for filling missing data points.</p>
 * <p>填充缺失数据点的策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ZERO, PREVIOUS, NEXT, LINEAR interpolation, AVERAGE, NAN strategies - 零值、前值、后值、线性插值、平均、NaN 策略</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeries filled = SamplerUtil.fillGaps(series, interval, FillStrategy.LINEAR);
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
public enum FillStrategy {

    /**
     * Fill with zero | 用零填充
     */
    ZERO,

    /**
     * Fill with previous value | 用前一个值填充
     */
    PREVIOUS,

    /**
     * Linear interpolation | 线性插值
     */
    LINEAR,

    /**
     * Fill with NaN (no fill) | 用NaN填充（不填充）
     */
    NAN,

    /**
     * Fill with next value | 用下一个值填充
     */
    NEXT,

    /**
     * Fill with average of neighbors | 用邻居的平均值填充
     */
    AVERAGE
}
