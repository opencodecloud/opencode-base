package cloud.opencode.base.geo.exception;

/**
 * Invalid GeoHash Exception
 * 无效GeoHash异常
 *
 * <p>Exception thrown when a GeoHash is invalid.</p>
 * <p>当GeoHash无效时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Invalid GeoHash error reporting - 无效GeoHash错误报告</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new InvalidGeoHashException("Invalid hash: " + hash);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public class InvalidGeoHashException extends GeoException {

    private final String geoHash;

    /**
     * Create invalid GeoHash exception
     * 创建无效GeoHash异常
     *
     * @param message the error message | 错误消息
     */
    public InvalidGeoHashException(String message) {
        super(message, GeoErrorCode.INVALID_GEOHASH);
        this.geoHash = null;
    }

    /**
     * Create invalid GeoHash exception with GeoHash value
     * 使用GeoHash值创建无效GeoHash异常
     *
     * @param message the error message | 错误消息
     * @param geoHash the invalid GeoHash | 无效的GeoHash
     */
    public InvalidGeoHashException(String message, String geoHash) {
        super(message, GeoErrorCode.INVALID_GEOHASH);
        this.geoHash = geoHash;
    }

    /**
     * Get the invalid GeoHash
     * 获取无效的GeoHash
     *
     * @return the GeoHash or null | GeoHash或null
     */
    public String getGeoHash() {
        return geoHash;
    }
}
