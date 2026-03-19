package cloud.opencode.base.geo.exception;

/**
 * Coordinate Transform Exception
 * 坐标转换异常
 *
 * <p>Exception thrown when coordinate transformation fails.</p>
 * <p>当坐标转换失败时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Coordinate transformation failure reporting - 坐标转换失败报告</li>
 *   <li>Cause chain support - 异常链支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new CoordinateTransformException("Failed to transform WGS84 to GCJ02");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public class CoordinateTransformException extends CoordinateException {

    /**
     * Create coordinate transform exception with message
     * 使用消息创建坐标转换异常
     *
     * @param message the error message | 错误消息
     */
    public CoordinateTransformException(String message) {
        super(message);
    }

    /**
     * Create coordinate transform exception with message and cause
     * 使用消息和原因创建坐标转换异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public CoordinateTransformException(String message, Throwable cause) {
        super(message, cause);
    }
}
