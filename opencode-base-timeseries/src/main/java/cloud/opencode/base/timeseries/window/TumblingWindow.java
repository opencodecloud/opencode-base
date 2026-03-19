package cloud.opencode.base.timeseries.window;

import cloud.opencode.base.timeseries.DataPoint;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Tumbling Window
 * 滚动窗口
 *
 * <p>Non-overlapping fixed-size windows.</p>
 * <p>不重叠的固定大小窗口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Non-overlapping fixed-size time windows - 不重叠的固定大小时间窗口</li>
 *   <li>Factory methods for hourly, daily, and minute-based windows - 小时、天和分钟窗口的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TumblingWindow tw = TumblingWindow.hourly();
 * List<Long> keys = tw.assignWindows(dataPoint);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是</li>
 *   <li>Null-safe: No (null size throws NullPointerException) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class TumblingWindow implements Window {

    private final Duration size;
    private final long sizeMillis;

    /**
     * Create tumbling window
     * 创建滚动窗口
     *
     * @param size the window size | 窗口大小
     */
    public TumblingWindow(Duration size) {
        Objects.requireNonNull(size, "size cannot be null");
        if (size.isZero() || size.isNegative()) {
            throw new IllegalArgumentException("Window size must be positive");
        }
        this.size = size;
        this.sizeMillis = size.toMillis();
    }

    /**
     * Create tumbling window with duration
     * 使用时长创建滚动窗口
     *
     * @param size the window size | 窗口大小
     * @return the window | 窗口
     */
    public static TumblingWindow of(Duration size) {
        return new TumblingWindow(size);
    }

    /**
     * Create hourly tumbling window
     * 创建每小时滚动窗口
     *
     * @return the window | 窗口
     */
    public static TumblingWindow hourly() {
        return new TumblingWindow(Duration.ofHours(1));
    }

    /**
     * Create daily tumbling window
     * 创建每天滚动窗口
     *
     * @return the window | 窗口
     */
    public static TumblingWindow daily() {
        return new TumblingWindow(Duration.ofDays(1));
    }

    /**
     * Create minute tumbling window
     * 创建每分钟滚动窗口
     *
     * @param minutes the minutes | 分钟数
     * @return the window | 窗口
     */
    public static TumblingWindow minutes(int minutes) {
        return new TumblingWindow(Duration.ofMinutes(minutes));
    }

    @Override
    public List<Long> assignWindows(DataPoint point) {
        long windowKey = (point.epochMillis() / sizeMillis) * sizeMillis;
        return List.of(windowKey);
    }

    @Override
    public Instant getWindowStart(long windowKey) {
        return Instant.ofEpochMilli(windowKey);
    }

    @Override
    public Instant getWindowEnd(long windowKey) {
        return Instant.ofEpochMilli(windowKey + sizeMillis);
    }

    @Override
    public Duration getSize() {
        return size;
    }

    @Override
    public String toString() {
        return String.format("TumblingWindow[size=%s]", size);
    }
}
