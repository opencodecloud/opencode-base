package cloud.opencode.base.geo.exception;

/**
 * Timestamp Exception
 * 时间戳异常
 *
 * <p>Exception thrown when a timestamp is invalid or expired.</p>
 * <p>当时间戳无效或过期时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Timestamp validation error reporting - 时间戳验证错误报告</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new TimestampException("Timestamp in future");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public class TimestampException extends GeoSecurityException {

    /**
     * Create timestamp exception with message
     * 使用消息创建时间戳异常
     *
     * @param message the error message | 错误消息
     */
    public TimestampException(String message) {
        super(message, GeoErrorCode.INVALID_TIMESTAMP);
    }

    /**
     * Create timestamp exception with message and cause
     * 使用消息和原因创建时间戳异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public TimestampException(String message, Throwable cause) {
        super(message, GeoErrorCode.INVALID_TIMESTAMP);
        initCause(cause);
    }
}
