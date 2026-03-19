package cloud.opencode.base.timeseries.validation;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.exception.TimeSeriesErrorCode;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;

import java.time.Instant;
import java.util.Objects;

/**
 * Data Point Validator
 * 数据点验证器
 *
 * <p>Validates data points for correctness.</p>
 * <p>验证数据点的正确性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Timestamp range validation (2000-2100) - 时间戳范围验证</li>
 *   <li>Value validation (rejects NaN and Infinity) - 值验证（拒绝 NaN 和 Infinity）</li>
 *   <li>Both throwing and boolean check variants - 抛出异常和布尔检查两种变体</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DataPointValidator.validate(dataPoint);
 * boolean valid = DataPointValidator.isValid(dataPoint);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (null returns false in boolean methods) - 空值安全: 是（布尔方法中空值返回 false）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class DataPointValidator {

    private static final Instant MIN_TIMESTAMP = Instant.parse("2000-01-01T00:00:00Z");
    private static final Instant MAX_TIMESTAMP = Instant.parse("2100-01-01T00:00:00Z");

    private DataPointValidator() {
        // Utility class
    }

    /**
     * Validate data point
     * 验证数据点
     *
     * @param point the data point | 数据点
     * @throws TimeSeriesException if validation fails | 如果验证失败抛出异常
     */
    public static void validate(DataPoint point) {
        Objects.requireNonNull(point, "DataPoint cannot be null");
        Objects.requireNonNull(point.timestamp(), "Timestamp cannot be null");

        validateTimestamp(point.timestamp());
        validateValue(point.value());
    }

    /**
     * Validate timestamp
     * 验证时间戳
     *
     * @param timestamp the timestamp | 时间戳
     * @throws TimeSeriesException if timestamp is invalid | 如果时间戳无效抛出异常
     */
    public static void validateTimestamp(Instant timestamp) {
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");

        if (timestamp.isBefore(MIN_TIMESTAMP) || timestamp.isAfter(MAX_TIMESTAMP)) {
            throw new TimeSeriesException(
                TimeSeriesErrorCode.INVALID_TIMESTAMP,
                "Timestamp out of valid range: " + timestamp
            );
        }
    }

    /**
     * Validate value
     * 验证值
     *
     * @param value the value | 值
     * @throws TimeSeriesException if value is invalid | 如果值无效抛出异常
     */
    public static void validateValue(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new TimeSeriesException(
                TimeSeriesErrorCode.INVALID_VALUE,
                "Value must be a finite number: " + value
            );
        }
    }

    /**
     * Check if value is valid (without throwing)
     * 检查值是否有效（不抛出异常）
     *
     * @param value the value | 值
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidValue(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /**
     * Check if timestamp is valid (without throwing)
     * 检查时间戳是否有效（不抛出异常）
     *
     * @param timestamp the timestamp | 时间戳
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidTimestamp(Instant timestamp) {
        if (timestamp == null) {
            return false;
        }
        return !timestamp.isBefore(MIN_TIMESTAMP) && !timestamp.isAfter(MAX_TIMESTAMP);
    }

    /**
     * Check if data point is valid (without throwing)
     * 检查数据点是否有效（不抛出异常）
     *
     * @param point the data point | 数据点
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(DataPoint point) {
        if (point == null) {
            return false;
        }
        return isValidTimestamp(point.timestamp()) && isValidValue(point.value());
    }

    /**
     * Get minimum valid timestamp
     * 获取最小有效时间戳
     *
     * @return the minimum timestamp | 最小时间戳
     */
    public static Instant getMinTimestamp() {
        return MIN_TIMESTAMP;
    }

    /**
     * Get maximum valid timestamp
     * 获取最大有效时间戳
     *
     * @return the maximum timestamp | 最大时间戳
     */
    public static Instant getMaxTimestamp() {
        return MAX_TIMESTAMP;
    }
}
