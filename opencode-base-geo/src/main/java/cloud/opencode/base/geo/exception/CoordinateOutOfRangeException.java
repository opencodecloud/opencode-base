package cloud.opencode.base.geo.exception;

import cloud.opencode.base.geo.Coordinate;

/**
 * Coordinate Out Of Range Exception
 * 坐标越界异常
 *
 * <p>Exception thrown when a coordinate is out of valid range.</p>
 * <p>当坐标超出有效范围时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Coordinate range validation exception - 坐标范围验证异常</li>
 *   <li>Factory methods for longitude/latitude - 经度/纬度的工厂方法</li>
 *   <li>Coordinate context support - 坐标上下文支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw CoordinateOutOfRangeException.forLongitude(200.0);
 * throw CoordinateOutOfRangeException.forLatitude(-100.0);
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
public class CoordinateOutOfRangeException extends GeoException {

    /**
     * Create coordinate out of range exception
     * 创建坐标越界异常
     *
     * @param message the error message | 错误消息
     */
    public CoordinateOutOfRangeException(String message) {
        super(message, GeoErrorCode.COORDINATE_OUT_OF_RANGE);
    }

    /**
     * Create coordinate out of range exception with coordinate
     * 使用坐标创建坐标越界异常
     *
     * @param message the error message | 错误消息
     * @param coordinate the out of range coordinate | 越界坐标
     */
    public CoordinateOutOfRangeException(String message, Coordinate coordinate) {
        super(message, null, GeoErrorCode.COORDINATE_OUT_OF_RANGE, coordinate);
    }

    /**
     * Create coordinate out of range exception for longitude
     * 为经度创建坐标越界异常
     *
     * @param longitude the out of range longitude | 越界经度
     * @return the exception | 异常
     */
    public static CoordinateOutOfRangeException forLongitude(double longitude) {
        return new CoordinateOutOfRangeException(
            "Longitude must be between -180 and 180: " + longitude);
    }

    /**
     * Create coordinate out of range exception for latitude
     * 为纬度创建坐标越界异常
     *
     * @param latitude the out of range latitude | 越界纬度
     * @return the exception | 异常
     */
    public static CoordinateOutOfRangeException forLatitude(double latitude) {
        return new CoordinateOutOfRangeException(
            "Latitude must be between -90 and 90: " + latitude);
    }
}
