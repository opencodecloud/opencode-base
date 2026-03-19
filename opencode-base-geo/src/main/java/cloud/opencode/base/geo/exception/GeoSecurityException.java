package cloud.opencode.base.geo.exception;

/**
 * Geo Security Exception
 * 地理安全异常
 *
 * <p>Exception thrown when a security violation is detected in geo operations.</p>
 * <p>当地理操作中检测到安全违规时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Geographic security violation reporting - 地理安全违规报告</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new GeoSecurityException("Coordinate access denied");
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
public class GeoSecurityException extends GeoException {

    /**
     * Create geo security exception
     * 创建地理安全异常
     *
     * @param message the error message | 错误消息
     */
    public GeoSecurityException(String message) {
        super(message, GeoErrorCode.LOCATION_SPOOFING);
    }

    /**
     * Create geo security exception with error code
     * 使用错误码创建地理安全异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public GeoSecurityException(String message, GeoErrorCode errorCode) {
        super(message, errorCode);
    }

    /**
     * Create location spoofing exception
     * 创建位置欺骗异常
     *
     * @return the exception | 异常
     */
    public static GeoSecurityException locationSpoofing() {
        return new GeoSecurityException("Location spoofing detected", GeoErrorCode.LOCATION_SPOOFING);
    }

    /**
     * Create invalid timestamp exception
     * 创建无效时间戳异常
     *
     * @return the exception | 异常
     */
    public static GeoSecurityException invalidTimestamp() {
        return new GeoSecurityException("Invalid or expired timestamp", GeoErrorCode.INVALID_TIMESTAMP);
    }

    /**
     * Create impossible speed exception
     * 创建不可能速度异常
     *
     * @param speedKmh the detected speed in km/h | 检测到的速度（公里/小时）
     * @return the exception | 异常
     */
    public static GeoSecurityException impossibleSpeed(double speedKmh) {
        return new GeoSecurityException(
            "Impossible travel speed detected: " + speedKmh + " km/h",
            GeoErrorCode.IMPOSSIBLE_SPEED);
    }
}
