package cloud.opencode.base.timeseries.exception;

/**
 * Time Series Error Code
 * 时序错误码
 *
 * <p>Error codes for time series operations (TS-1xxx ~ 6xxx).</p>
 * <p>时间序列操作的错误码（TS-1xxx ~ 6xxx）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized error codes: data (1xxx), query (2xxx), computation (3xxx), capacity (4xxx), alignment (5xxx), rate (6xxx) - 分类错误码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new TimeSeriesException(TimeSeriesErrorCode.INVALID_TIMESTAMP, "details");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum constants are immutable) - 线程安全: 是</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public enum TimeSeriesErrorCode {

    // 1xxx - Data errors | 数据错误
    INVALID_TIMESTAMP("TS-1001", "Invalid timestamp"),
    INVALID_VALUE("TS-1002", "Invalid value"),
    DUPLICATE_TIMESTAMP("TS-1003", "Duplicate timestamp"),
    EMPTY_SERIES("TS-1004", "Time series is empty"),

    // 2xxx - Query errors | 查询错误
    QUERY_RANGE_TOO_LARGE("TS-2001", "Query range too large"),
    INVALID_TIME_RANGE("TS-2002", "Invalid time range"),
    SERIES_NOT_FOUND("TS-2003", "Time series not found"),

    // 3xxx - Computation errors | 计算错误
    AGGREGATION_FAILED("TS-3001", "Aggregation failed"),
    WINDOW_SIZE_INVALID("TS-3002", "Invalid window size"),
    INSUFFICIENT_DATA("TS-3003", "Insufficient data"),
    INVALID_INTERVAL("TS-3004", "Invalid interval"),
    GAP_DETECTED("TS-3005", "Gap detected in time series"),

    // 4xxx - Capacity errors | 容量错误
    CAPACITY_EXCEEDED("TS-4001", "Capacity exceeded"),
    MEMORY_LIMIT_EXCEEDED("TS-4002", "Memory limit exceeded"),

    // 5xxx - Alignment/Interpolation errors | 对齐/插值错误
    ALIGNMENT_FAILED("TS-5001", "Alignment failed"),
    INTERPOLATION_FAILED("TS-5002", "Interpolation failed"),
    INSUFFICIENT_POINTS("TS-5003", "Insufficient data points for interpolation"),

    // 6xxx - Rate/Quality errors | 速率/质量错误
    COUNTER_RESET("TS-6001", "Counter reset detected");

    private final String code;
    private final String message;

    TimeSeriesErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Get error code
     * 获取错误码
     *
     * @return the error code | 错误码
     */
    public String code() {
        return code;
    }

    /**
     * Get error message
     * 获取错误消息
     *
     * @return the error message | 错误消息
     */
    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }
}
