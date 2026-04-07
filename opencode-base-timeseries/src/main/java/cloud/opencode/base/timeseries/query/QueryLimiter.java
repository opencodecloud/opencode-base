package cloud.opencode.base.timeseries.query;

import cloud.opencode.base.timeseries.exception.TimeSeriesErrorCode;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Query Limiter
 * 查询限制器
 *
 * <p>Limits query range and result size to prevent resource exhaustion.</p>
 * <p>限制查询范围和结果大小以防止资源耗尽。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable max query range and max result size - 可配置最大查询范围和结果大小</li>
 *   <li>Automatic result truncation - 自动结果截断</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * QueryLimiter.setMaxRangeDays(30);
 * QueryLimiter.validateRange(range);
 * List<DataPoint> limited = QueryLimiter.limitResult(points);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses AtomicInteger for configuration) - 线程安全: 是（使用AtomicInteger）</li>
 *   <li>Null-safe: No (null range throws NullPointerException) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class QueryLimiter {

    private static final int DEFAULT_MAX_RANGE_DAYS = 365;
    private static final int DEFAULT_MAX_RESULT_SIZE = 100_000;

    private static final AtomicInteger maxRangeDays = new AtomicInteger(DEFAULT_MAX_RANGE_DAYS);
    private static final AtomicInteger maxResultSize = new AtomicInteger(DEFAULT_MAX_RESULT_SIZE);

    private QueryLimiter() {
        // Utility class
    }

    /**
     * Validate query time range
     * 验证查询时间范围
     *
     * @param from the start time | 开始时间
     * @param to the end time | 结束时间
     * @throws TimeSeriesException if range is invalid | 如果范围无效抛出异常
     */
    public static void validateRange(Instant from, Instant to) {
        java.util.Objects.requireNonNull(from, "from must not be null");
        java.util.Objects.requireNonNull(to, "to must not be null");
        Duration range = Duration.between(from, to);

        if (range.isNegative()) {
            throw new TimeSeriesException(
                TimeSeriesErrorCode.INVALID_TIME_RANGE,
                "Start time must be before end time"
            );
        }

        int currentMaxDays = maxRangeDays.get();
        if (range.toDays() > currentMaxDays) {
            throw new TimeSeriesException(
                TimeSeriesErrorCode.QUERY_RANGE_TOO_LARGE,
                "Query range exceeds " + currentMaxDays + " days"
            );
        }
    }

    /**
     * Validate time range record
     * 验证时间范围记录
     *
     * @param range the time range | 时间范围
     * @throws TimeSeriesException if range is invalid | 如果范围无效抛出异常
     */
    public static void validateRange(TimeRange range) {
        java.util.Objects.requireNonNull(range, "range must not be null");
        validateRange(range.from(), range.to());
    }

    /**
     * Limit result size
     * 限制结果大小
     *
     * @param result the result list | 结果列表
     * @param <T> the element type | 元素类型
     * @return the limited result | 限制后的结果
     */
    public static <T> List<T> limitResult(List<T> result) {
        int currentMaxSize = maxResultSize.get();
        if (result.size() > currentMaxSize) {
            return result.subList(0, currentMaxSize);
        }
        return result;
    }

    /**
     * Check if result size exceeds limit
     * 检查结果大小是否超过限制
     *
     * @param size the result size | 结果大小
     * @return true if exceeds limit | 如果超过限制返回true
     */
    public static boolean exceedsLimit(int size) {
        return size > maxResultSize.get();
    }

    /**
     * Set max range days
     * 设置最大范围天数
     *
     * @param days the max days | 最大天数
     */
    public static void setMaxRangeDays(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Max range days must be positive");
        }
        maxRangeDays.set(days);
    }

    /**
     * Set max result size
     * 设置最大结果大小
     *
     * @param size the max size | 最大大小
     */
    public static void setMaxResultSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Max result size must be positive");
        }
        maxResultSize.set(size);
    }

    /**
     * Get max range days
     * 获取最大范围天数
     *
     * @return the max range days | 最大范围天数
     */
    public static int getMaxRangeDays() {
        return maxRangeDays.get();
    }

    /**
     * Get max result size
     * 获取最大结果大小
     *
     * @return the max result size | 最大结果大小
     */
    public static int getMaxResultSize() {
        return maxResultSize.get();
    }

    /**
     * Reset to default limits
     * 重置为默认限制
     */
    public static void resetDefaults() {
        maxRangeDays.set(DEFAULT_MAX_RANGE_DAYS);
        maxResultSize.set(DEFAULT_MAX_RESULT_SIZE);
    }
}
