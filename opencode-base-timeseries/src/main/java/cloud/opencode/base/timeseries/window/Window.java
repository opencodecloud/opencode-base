package cloud.opencode.base.timeseries.window;

import cloud.opencode.base.timeseries.DataPoint;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Window
 * 窗口
 *
 * <p>Interface for windowing strategies.</p>
 * <p>窗口策略的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Assign data points to time windows - 将数据点分配到时间窗口</li>
 *   <li>Window start/end time and containment check - 窗口开始/结束时间和包含检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Window window = TumblingWindow.of(Duration.ofMinutes(5));
 * List<Long> keys = window.assignWindows(dataPoint);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public interface Window {

    /**
     * Assign data point to windows
     * 将数据点分配到窗口
     *
     * @param point the data point | 数据点
     * @return the window keys | 窗口键
     */
    List<Long> assignWindows(DataPoint point);

    /**
     * Get window start time from key
     * 从键获取窗口开始时间
     *
     * @param windowKey the window key | 窗口键
     * @return the window start time | 窗口开始时间
     */
    Instant getWindowStart(long windowKey);

    /**
     * Get window end time from key
     * 从键获取窗口结束时间
     *
     * @param windowKey the window key | 窗口键
     * @return the window end time | 窗口结束时间
     */
    Instant getWindowEnd(long windowKey);

    /**
     * Get window size
     * 获取窗口大小
     *
     * @return the window size | 窗口大小
     */
    Duration getSize();

    /**
     * Check if timestamp is in window
     * 检查时间戳是否在窗口内
     *
     * @param windowKey the window key | 窗口键
     * @param timestamp the timestamp | 时间戳
     * @return true if in window | 如果在窗口内返回true
     */
    default boolean isInWindow(long windowKey, Instant timestamp) {
        Instant start = getWindowStart(windowKey);
        Instant end = getWindowEnd(windowKey);
        return !timestamp.isBefore(start) && timestamp.isBefore(end);
    }

    /**
     * Get window key for timestamp
     * 获取时间戳的窗口键
     *
     * @param timestamp the timestamp | 时间戳
     * @return the window key | 窗口键
     */
    default long getWindowKey(Instant timestamp) {
        long sizeMillis = getSize().toMillis();
        return (timestamp.toEpochMilli() / sizeMillis) * sizeMillis;
    }
}
