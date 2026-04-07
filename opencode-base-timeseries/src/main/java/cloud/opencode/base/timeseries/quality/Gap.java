package cloud.opencode.base.timeseries.quality;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Gap in Time Series Data
 * 时间序列数据中的间隙
 *
 * <p>Represents a gap (missing data interval) between two consecutive data points
 * in a time series.</p>
 * <p>表示时间序列中两个连续数据点之间的间隙（缺失数据区间）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record with start and end timestamps - 不可变记录，包含开始和结束时间戳</li>
 *   <li>Derived gap length calculation - 派生间隙长度计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Gap gap = new Gap(Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T01:00:00Z"));
 * Duration length = gap.length(); // PT1H
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (null timestamps throw NullPointerException) - 空值安全: 否（空时间戳抛出空指针异常）</li>
 * </ul>
 *
 * @param start the start of the gap (timestamp of the point before the gap) | 间隙开始（间隙前数据点的时间戳）
 * @param end   the end of the gap (timestamp of the point after the gap) | 间隙结束（间隙后数据点的时间戳）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
public record Gap(Instant start, Instant end) {

    public Gap {
        Objects.requireNonNull(start, "Gap start must not be null");
        Objects.requireNonNull(end, "Gap end must not be null");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException(
                    "Gap end must not be before start: start=" + start + ", end=" + end);
        }
    }

    /**
     * Get the length (duration) of this gap.
     * 获取此间隙的长度（时长）。
     *
     * @return the duration between start and end | 开始和结束之间的时长
     */
    public Duration length() {
        return Duration.between(start, end);
    }
}
