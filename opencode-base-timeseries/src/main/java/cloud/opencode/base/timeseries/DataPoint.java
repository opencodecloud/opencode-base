package cloud.opencode.base.timeseries;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Data Point
 * 数据点
 *
 * <p>A single data point in a time series.</p>
 * <p>时间序列中的单个数据点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record with timestamp, value, and optional tags - 不可变记录，包含时间戳、值和可选标签</li>
 *   <li>Factory methods: of, now, from epoch millis - 工厂方法：of、now、从纪元毫秒</li>
 *   <li>Comparable by timestamp for natural ordering - 按时间戳的自然排序</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DataPoint p1 = DataPoint.of(Instant.now(), 42.0);
 * DataPoint p2 = DataPoint.now(99.9);
 * DataPoint p3 = p1.withTag("host", "server1");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record, tags are defensively copied) - 线程安全: 是（不可变记录，标签被防御性复制）</li>
 *   <li>Null-safe: No (null timestamp throws NullPointerException) - 空值安全: 否（空时间戳抛出异常）</li>
 * </ul>
 *
 * @param timestamp the timestamp | 时间戳
 * @param value the value | 值
 * @param tags optional tags | 可选标签
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public record DataPoint(Instant timestamp, double value, Map<String, String> tags) implements Comparable<DataPoint> {

    public DataPoint {
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        tags = tags != null ? Map.copyOf(tags) : Map.of();
    }

    /**
     * Create data point with value only
     * 仅用值创建数据点
     *
     * @param timestamp the timestamp | 时间戳
     * @param value the value | 值
     * @return the data point | 数据点
     */
    public static DataPoint of(Instant timestamp, double value) {
        return new DataPoint(timestamp, value, null);
    }

    /**
     * Create data point with current time
     * 用当前时间创建数据点
     *
     * @param value the value | 值
     * @return the data point | 数据点
     */
    public static DataPoint now(double value) {
        return new DataPoint(Instant.now(), value, null);
    }

    /**
     * Create data point from epoch milliseconds
     * 从纪元毫秒创建数据点
     *
     * @param epochMillis the epoch milliseconds | 纪元毫秒
     * @param value the value | 值
     * @return the data point | 数据点
     */
    public static DataPoint of(long epochMillis, double value) {
        return new DataPoint(Instant.ofEpochMilli(epochMillis), value, null);
    }

    /**
     * Create data point with tags
     * 用标签创建数据点
     *
     * @param timestamp the timestamp | 时间戳
     * @param value the value | 值
     * @param tags the tags | 标签
     * @return the data point | 数据点
     */
    public static DataPoint of(Instant timestamp, double value, Map<String, String> tags) {
        return new DataPoint(timestamp, value, tags);
    }

    /**
     * Get epoch millis
     * 获取纪元毫秒
     *
     * @return the epoch millis | 纪元毫秒
     */
    public long epochMillis() {
        return timestamp.toEpochMilli();
    }

    /**
     * Check if value is NaN
     * 检查值是否为NaN
     *
     * @return true if NaN | 如果是NaN返回true
     */
    public boolean isNaN() {
        return Double.isNaN(value);
    }

    /**
     * Get tag value
     * 获取标签值
     *
     * @param key the tag key | 标签键
     * @return the value or null | 值或null
     */
    public String getTag(String key) {
        return tags.get(key);
    }

    /**
     * Create new data point with additional tag
     * 创建带有额外标签的新数据点
     *
     * @param key the tag key | 标签键
     * @param value the tag value | 标签值
     * @return new data point with the tag | 带标签的新数据点
     */
    public DataPoint withTag(String key, String value) {
        Map<String, String> newTags = new java.util.HashMap<>(tags);
        newTags.put(key, value);
        return new DataPoint(timestamp, this.value, newTags);
    }

    @Override
    public int compareTo(DataPoint other) {
        return this.timestamp.compareTo(other.timestamp);
    }
}
