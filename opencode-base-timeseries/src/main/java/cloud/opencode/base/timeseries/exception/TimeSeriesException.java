package cloud.opencode.base.timeseries.exception;

/**
 * Time Series Exception
 * 时间序列异常
 *
 * <p>Base exception for time series operations.</p>
 * <p>时间序列操作的基础异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Structured error codes (data, query, computation, capacity) - 结构化错误码</li>
 *   <li>Detail message and cause chaining - 详细消息和原因链</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     validator.validate(dataPoint);
 * } catch (TimeSeriesException e) {
 *     System.err.println(e.errorCode().code() + ": " + e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是</li>
 *   <li>Null-safe: No (errorCode must not be null) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public class TimeSeriesException extends RuntimeException {

    private final TimeSeriesErrorCode errorCode;

    /**
     * Create exception with error code
     * 使用错误码创建异常
     *
     * @param errorCode the error code | 错误码
     */
    public TimeSeriesException(TimeSeriesErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    /**
     * Create exception with error code and detail
     * 使用错误码和详情创建异常
     *
     * @param errorCode the error code | 错误码
     * @param detail the detail message | 详细消息
     */
    public TimeSeriesException(TimeSeriesErrorCode errorCode, String detail) {
        super(errorCode.message() + ": " + detail);
        this.errorCode = errorCode;
    }

    /**
     * Create exception with error code and cause
     * 使用错误码和原因创建异常
     *
     * @param errorCode the error code | 错误码
     * @param cause the cause | 原因
     */
    public TimeSeriesException(TimeSeriesErrorCode errorCode, Throwable cause) {
        super(errorCode.message(), cause);
        this.errorCode = errorCode;
    }

    /**
     * Create exception with error code, detail and cause
     * 使用错误码、详情和原因创建异常
     *
     * @param errorCode the error code | 错误码
     * @param detail the detail message | 详细消息
     * @param cause the cause | 原因
     */
    public TimeSeriesException(TimeSeriesErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode.message() + ": " + detail, cause);
        this.errorCode = errorCode;
    }

    /**
     * Get error code
     * 获取错误码
     *
     * @return the error code | 错误码
     */
    public TimeSeriesErrorCode errorCode() {
        return errorCode;
    }

    /**
     * Get error code string
     * 获取错误码字符串
     *
     * @return the error code string | 错误码字符串
     */
    public String getErrorCodeString() {
        return errorCode.code();
    }
}
