package cloud.opencode.base.timeseries;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Bounded Time Series
 * 有界时间序列
 *
 * <p>Time series with capacity and age limits.</p>
 * <p>具有容量和时间限制的时间序列。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Maximum size limit with automatic eviction of oldest entries - 最大容量限制，自动驱逐最旧条目</li>
 *   <li>Maximum age limit with automatic expiry - 最大时间限制，自动过期</li>
 *   <li>Batch eviction for capacity overflow - 容量溢出时的批量驱逐</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BoundedTimeSeries bts = BoundedTimeSeries.of("metrics", 1000, Duration.ofHours(24));
 * bts.addNow(42.0);
 * boolean full = bts.isFull();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (inherits concurrent collections from TimeSeries) - 线程安全: 是</li>
 *   <li>Null-safe: No (null data points are rejected) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public class BoundedTimeSeries extends TimeSeries {

    private final int maxSize;
    private final Duration maxAge;

    /**
     * Create bounded time series
     * 创建有界时间序列
     *
     * @param name the series name | 序列名称
     * @param maxSize the maximum size | 最大容量
     * @param maxAge the maximum age | 最大时间
     */
    public BoundedTimeSeries(String name, int maxSize, Duration maxAge) {
        super(name);
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        this.maxSize = maxSize;
        this.maxAge = maxAge;
    }

    /**
     * Create bounded time series with size limit only
     * 仅使用容量限制创建有界时间序列
     *
     * @param name the series name | 序列名称
     * @param maxSize the maximum size | 最大容量
     */
    public BoundedTimeSeries(String name, int maxSize) {
        this(name, maxSize, Duration.ofDays(365));
    }

    /**
     * Create bounded time series
     * 创建有界时间序列
     *
     * @param name the series name | 序列名称
     * @param maxSize the maximum size | 最大容量
     * @param maxAge the maximum age | 最大时间
     * @return the bounded time series | 有界时间序列
     */
    public static BoundedTimeSeries of(String name, int maxSize, Duration maxAge) {
        return new BoundedTimeSeries(name, maxSize, maxAge);
    }

    /**
     * Create bounded time series with size limit only
     * 仅使用容量限制创建有界时间序列
     *
     * @param name the series name | 序列名称
     * @param maxSize the maximum size | 最大容量
     * @return the bounded time series | 有界时间序列
     */
    public static BoundedTimeSeries of(String name, int maxSize) {
        return new BoundedTimeSeries(name, maxSize);
    }

    @Override
    public TimeSeries add(DataPoint point) {
        // Batch evict excess entries by capacity (calculate once, remove in batch)
        int excess = size() - maxSize + 1; // +1 to make room for the new entry
        if (excess > 0) {
            evictOldest(excess);
        }
        // Check age
        evictExpired();
        return super.add(point);
    }

    @Override
    public TimeSeries addAll(Collection<DataPoint> points) {
        for (DataPoint point : points) {
            add(point);
        }
        return this;
    }

    /**
     * Get maximum size
     * 获取最大容量
     *
     * @return the maximum size | 最大容量
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Get maximum age
     * 获取最大时间
     *
     * @return the maximum age | 最大时间
     */
    public Duration getMaxAge() {
        return maxAge;
    }

    /**
     * Get remaining capacity
     * 获取剩余容量
     *
     * @return the remaining capacity | 剩余容量
     */
    public int remainingCapacity() {
        return Math.max(0, maxSize - size());
    }

    /**
     * Check if series is full
     * 检查序列是否已满
     *
     * @return true if full | 如果已满返回true
     */
    public boolean isFull() {
        return size() >= maxSize;
    }

    /**
     * Evict oldest data point
     * 驱逐最旧的数据点
     */
    protected void evictOldest() {
        getFirst().ifPresent(first -> remove(first.timestamp()));
    }

    /**
     * Evict multiple oldest data points in batch
     * 批量驱逐最旧的数据点
     *
     * @param count the number of entries to evict | 要驱逐的条目数
     */
    private void evictOldest(int count) {
        List<DataPoint> points = getPoints();
        int toRemove = Math.min(count, points.size());
        for (int i = 0; i < toRemove; i++) {
            remove(points.get(i).timestamp());
        }
    }

    /**
     * Evict expired data points
     * 驱逐过期的数据点
     */
    protected void evictExpired() {
        Instant cutoff = Instant.now().minus(maxAge);
        retain(maxAge);
    }

    /**
     * Remove data point by timestamp
     * 按时间戳移除数据点
     *
     * @param timestamp the timestamp | 时间戳
     */
    @Override
    public void remove(Instant timestamp) {
        super.remove(timestamp);
    }

    @Override
    public String toString() {
        return String.format("BoundedTimeSeries[%s, size=%d/%d, maxAge=%s]",
            getName(), size(), maxSize, maxAge);
    }
}
