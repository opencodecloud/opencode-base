package cloud.opencode.base.geo.exception;

/**
 * GeoHash Encode Exception
 * GeoHash编码异常
 *
 * <p>Exception thrown when GeoHash encoding fails.</p>
 * <p>当GeoHash编码失败时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>GeoHash encoding failure reporting - GeoHash编码失败报告</li>
 *   <li>Cause chain support - 异常链支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new GeoHashEncodeException("Failed to encode coordinate to GeoHash");
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
public class GeoHashEncodeException extends GeoHashException {

    /**
     * Create GeoHash encode exception with message
     * 使用消息创建GeoHash编码异常
     *
     * @param message the error message | 错误消息
     */
    public GeoHashEncodeException(String message) {
        super(message);
    }

    /**
     * Create GeoHash encode exception with message and cause
     * 使用消息和原因创建GeoHash编码异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public GeoHashEncodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
