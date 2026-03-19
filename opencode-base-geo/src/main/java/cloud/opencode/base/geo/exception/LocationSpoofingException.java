package cloud.opencode.base.geo.exception;

/**
 * Location Spoofing Exception
 * 位置欺骗异常
 *
 * <p>Exception thrown when location spoofing is detected.</p>
 * <p>当检测到位置欺骗时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Location spoofing detection reporting - 位置欺骗检测报告</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new LocationSpoofingException("Suspicious jump detected");
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
public class LocationSpoofingException extends GeoSecurityException {

    /**
     * Create location spoofing exception with message
     * 使用消息创建位置欺骗异常
     *
     * @param message the error message | 错误消息
     */
    public LocationSpoofingException(String message) {
        super(message, GeoErrorCode.LOCATION_SPOOFING);
    }

    /**
     * Create location spoofing exception with message and cause
     * 使用消息和原因创建位置欺骗异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public LocationSpoofingException(String message, Throwable cause) {
        super(message, GeoErrorCode.LOCATION_SPOOFING);
        initCause(cause);
    }
}
