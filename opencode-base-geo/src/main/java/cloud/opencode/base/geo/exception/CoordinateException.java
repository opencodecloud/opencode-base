package cloud.opencode.base.geo.exception;

/**
 * Coordinate Exception
 * 坐标异常
 *
 * <p>Parent exception class for all coordinate-related exceptions.</p>
 * <p>所有坐标相关异常的父类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base class for coordinate exceptions - 坐标异常的基类</li>
 *   <li>Automatic error code assignment (INVALID_COORDINATE) - 自动分配错误码（INVALID_COORDINATE）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new CoordinateException("Invalid coordinate value");
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
public class CoordinateException extends GeoException {

    /**
     * Create coordinate exception with message
     * 使用消息创建坐标异常
     *
     * @param message the error message | 错误消息
     */
    public CoordinateException(String message) {
        super(message, GeoErrorCode.INVALID_COORDINATE);
    }

    /**
     * Create coordinate exception with message and cause
     * 使用消息和原因创建坐标异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public CoordinateException(String message, Throwable cause) {
        super(message, cause, GeoErrorCode.INVALID_COORDINATE);
    }
}
