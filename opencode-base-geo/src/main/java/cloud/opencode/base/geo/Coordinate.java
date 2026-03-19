package cloud.opencode.base.geo;

import cloud.opencode.base.geo.geohash.GeoHashUtil;

/**
 * Coordinate Point
 * 坐标点
 *
 * <p>Immutable record representing a geographic coordinate point.</p>
 * <p>表示地理坐标点的不可变记录。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Coordinate system support - 坐标系支持</li>
 *   <li>Coordinate transformation - 坐标转换</li>
 *   <li>Distance calculation - 距离计算</li>
 *   <li>GeoHash encoding - GeoHash编码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create coordinates
 * Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);
 * Coordinate gcj02 = Coordinate.gcj02(116.4074, 39.9042);
 * Coordinate bd09 = Coordinate.bd09(116.4074, 39.9042);
 *
 * // Transform coordinates
 * Coordinate converted = wgs84.to(CoordinateSystem.GCJ02);
 *
 * // Calculate distance
 * double distance = wgs84.distanceTo(otherCoord);
 *
 * // Get GeoHash
 * String hash = wgs84.toGeoHash(8);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (coordinate system must not be null) - 空值安全: 否（坐标系不能为null）</li>
 * </ul>
 *
 * @param longitude the longitude (经度)
 * @param latitude the latitude (纬度)
 * @param system the coordinate system (坐标系)
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public record Coordinate(
    double longitude,
    double latitude,
    CoordinateSystem system
) {

    /**
     * Create WGS84 coordinate
     * 创建WGS84坐标
     *
     * @param longitude the longitude | 经度
     * @param latitude the latitude | 纬度
     * @return WGS84 coordinate | WGS84坐标
     */
    public static Coordinate wgs84(double longitude, double latitude) {
        return new Coordinate(longitude, latitude, CoordinateSystem.WGS84);
    }

    /**
     * Create GCJ02 coordinate
     * 创建GCJ02坐标
     *
     * @param longitude the longitude | 经度
     * @param latitude the latitude | 纬度
     * @return GCJ02 coordinate | GCJ02坐标
     */
    public static Coordinate gcj02(double longitude, double latitude) {
        return new Coordinate(longitude, latitude, CoordinateSystem.GCJ02);
    }

    /**
     * Create BD09 coordinate
     * 创建BD09坐标
     *
     * @param longitude the longitude | 经度
     * @param latitude the latitude | 纬度
     * @return BD09 coordinate | BD09坐标
     */
    public static Coordinate bd09(double longitude, double latitude) {
        return new Coordinate(longitude, latitude, CoordinateSystem.BD09);
    }

    /**
     * Transform to target coordinate system
     * 转换到目标坐标系
     *
     * @param target the target coordinate system | 目标坐标系
     * @return transformed coordinate | 转换后的坐标
     */
    public Coordinate to(CoordinateSystem target) {
        return CoordinateUtil.transform(this, target);
    }

    /**
     * Calculate distance to another coordinate (meters)
     * 计算到另一个坐标的距离（米）
     *
     * @param other the other coordinate | 另一个坐标
     * @return distance in meters | 距离（米）
     */
    public double distanceTo(Coordinate other) {
        return OpenGeo.distance(this, other);
    }

    /**
     * Convert to GeoHash
     * 转换为GeoHash
     *
     * @param precision the precision (1-12) | 精度（1-12）
     * @return GeoHash string | GeoHash字符串
     */
    public String toGeoHash(int precision) {
        return GeoHashUtil.encode(latitude, longitude, precision);
    }

    /**
     * Check if coordinate is valid
     * 检查坐标是否有效
     *
     * @return true if valid | 如果有效返回true
     */
    public boolean isValid() {
        return longitude >= -180 && longitude <= 180
            && latitude >= -90 && latitude <= 90
            && !Double.isNaN(longitude) && !Double.isNaN(latitude)
            && !Double.isInfinite(longitude) && !Double.isInfinite(latitude);
    }

    /**
     * Check if coordinate is in China
     * 检查坐标是否在中国境内
     *
     * @return true if in China | 如果在中国境内返回true
     */
    public boolean isInChina() {
        return OpenGeo.isInChina(longitude, latitude);
    }
}
