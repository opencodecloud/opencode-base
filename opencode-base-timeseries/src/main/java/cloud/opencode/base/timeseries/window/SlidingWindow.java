package cloud.opencode.base.timeseries.window;

import cloud.opencode.base.timeseries.DataPoint;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Sliding Window
 * 滑动窗口
 *
 * <p>Overlapping windows with configurable size and slide.</p>
 * <p>具有可配置大小和滑动步长的重叠窗口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Overlapping windows with configurable size and slide - 可配置大小和步长的重叠窗口</li>
 *   <li>Overlap ratio calculation - 重叠比率计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SlidingWindow sw = SlidingWindow.of(Duration.ofMinutes(10), Duration.ofMinutes(5));
 * List<Long> keys = sw.assignWindows(dataPoint);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是</li>
 *   <li>Null-safe: No (null arguments throw NullPointerException) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class SlidingWindow implements Window {

    private final Duration size;
    private final Duration slide;
    private final long sizeMillis;
    private final long slideMillis;

    /**
     * Create sliding window
     * 创建滑动窗口
     *
     * @param size the window size | 窗口大小
     * @param slide the slide interval | 滑动间隔
     */
    public SlidingWindow(Duration size, Duration slide) {
        Objects.requireNonNull(size, "size cannot be null");
        Objects.requireNonNull(slide, "slide cannot be null");
        if (size.isZero() || size.isNegative()) {
            throw new IllegalArgumentException("Window size must be positive");
        }
        if (slide.isZero() || slide.isNegative()) {
            throw new IllegalArgumentException("Slide must be positive");
        }
        this.size = size;
        this.slide = slide;
        this.sizeMillis = size.toMillis();
        this.slideMillis = slide.toMillis();
    }

    /**
     * Create sliding window
     * 创建滑动窗口
     *
     * @param size the window size | 窗口大小
     * @param slide the slide interval | 滑动间隔
     * @return the window | 窗口
     */
    public static SlidingWindow of(Duration size, Duration slide) {
        return new SlidingWindow(size, slide);
    }

    /**
     * Get slide interval
     * 获取滑动间隔
     *
     * @return the slide interval | 滑动间隔
     */
    public Duration getSlide() {
        return slide;
    }

    @Override
    public List<Long> assignWindows(DataPoint point) {
        long timestamp = point.epochMillis();
        List<Long> windows = new ArrayList<>();

        // Calculate all windows that contain this point
        long firstWindowStart = ((timestamp - sizeMillis) / slideMillis + 1) * slideMillis;
        if (firstWindowStart < 0) {
            firstWindowStart = (timestamp / slideMillis) * slideMillis;
        }

        long windowStart = firstWindowStart;
        while (windowStart <= timestamp) {
            if (timestamp < windowStart + sizeMillis) {
                windows.add(windowStart);
            }
            windowStart += slideMillis;
        }

        return windows;
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

    /**
     * Get overlap ratio
     * 获取重叠比率
     *
     * @return the overlap ratio (0.0-1.0) | 重叠比率
     */
    public double getOverlapRatio() {
        if (slideMillis >= sizeMillis) {
            return 0.0;
        }
        return (double) (sizeMillis - slideMillis) / sizeMillis;
    }

    @Override
    public String toString() {
        return String.format("SlidingWindow[size=%s, slide=%s]", size, slide);
    }
}
