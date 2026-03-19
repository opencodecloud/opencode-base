package cloud.opencode.base.geo.exception;

import cloud.opencode.base.geo.Coordinate;

/**
 * Invalid Coordinate Exception
 * 无效坐标异常
 *
 * <p>Exception thrown when a coordinate is invalid.</p>
 * <p>当坐标无效时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Invalid coordinate error reporting - 无效坐标错误报告</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new InvalidCoordinateException("Latitude out of range: " + lat);
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
public class InvalidCoordinateException extends GeoException {

    /**
     * Create invalid coordinate exception
     * 创建无效坐标异常
     *
     * @param message the error message | 错误消息
     */
    public InvalidCoordinateException(String message) {
        super(message, GeoErrorCode.INVALID_COORDINATE);
    }

    /**
     * Create invalid coordinate exception with coordinate
     * 使用坐标创建无效坐标异常
     *
     * @param message the error message | 错误消息
     * @param coordinate the invalid coordinate | 无效坐标
     */
    public InvalidCoordinateException(String message, Coordinate coordinate) {
        super(message, null, GeoErrorCode.INVALID_COORDINATE, coordinate);
    }

    /**
     * Create invalid coordinate exception with longitude and latitude
     * 使用经纬度创建无效坐标异常
     *
     * @param longitude the longitude | 经度
     * @param latitude the latitude | 纬度
     */
    public InvalidCoordinateException(double longitude, double latitude) {
        super("Invalid coordinate: longitude=" + longitude + ", latitude=" + latitude,
            GeoErrorCode.INVALID_COORDINATE);
    }
}
