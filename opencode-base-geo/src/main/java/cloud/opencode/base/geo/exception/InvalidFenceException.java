package cloud.opencode.base.geo.exception;

/**
 * Invalid Fence Exception
 * 无效围栏异常
 *
 * <p>Exception thrown when a geo fence definition is invalid.</p>
 * <p>当地理围栏定义无效时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Invalid geofence error reporting - 无效地理围栏错误报告</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new InvalidFenceException("Polygon must have >= 3 vertices");
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
public class InvalidFenceException extends FenceException {

    /**
     * Create invalid fence exception with message
     * 使用消息创建无效围栏异常
     *
     * @param message the error message | 错误消息
     */
    public InvalidFenceException(String message) {
        super(message);
    }

    /**
     * Create invalid fence exception with message and cause
     * 使用消息和原因创建无效围栏异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public InvalidFenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
