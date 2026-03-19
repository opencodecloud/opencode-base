package cloud.opencode.base.geo.security;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.geohash.GeoHashUtil;

import java.security.SecureRandom;

/**
 * Coordinate Masker
 * 坐标模糊化工具
 *
 * <p>Utility class for masking/obfuscating coordinates to protect user privacy.</p>
 * <p>用于模糊化坐标以保护用户隐私的工具类。</p>
 *
 * <p><strong>Masking Methods | 模糊化方法:</strong></p>
 * <ul>
 *   <li>{@link #mask(Coordinate, double)} - Random offset within radius | 半径内随机偏移</li>
 *   <li>{@link #reducePrecision(Coordinate, int)} - Reduce decimal precision | 降低小数精度</li>
 *   <li>{@link #maskByGeoHash(Coordinate, int)} - Align to GeoHash grid | 对齐到GeoHash网格</li>
 * </ul>
 *
 * <p><strong>Precision Reference | 精度参考:</strong></p>
 * <table>
 *   <tr><th>Decimal Places | 小数位</th><th>Precision | 精度</th></tr>
 *   <tr><td>3</td><td>~111 meters</td></tr>
 *   <tr><td>4</td><td>~11 meters</td></tr>
 *   <tr><td>5</td><td>~1.1 meters</td></tr>
 * </table>
 *
 * <p><strong>GeoHash Precision | GeoHash精度:</strong></p>
 * <table>
 *   <tr><th>Length | 长度</th><th>Cell Size | 单元格大小</th></tr>
 *   <tr><td>4</td><td>~39km x 20km</td></tr>
 *   <tr><td>5</td><td>~5km x 5km</td></tr>
 *   <tr><td>6</td><td>~1.2km x 600m</td></tr>
 *   <tr><td>7</td><td>~150m x 150m</td></tr>
 * </table>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>All methods are thread-safe and stateless.</p>
 * <p>所有方法都是线程安全和无状态的。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Coordinate location = Coordinate.wgs84(116.4074, 39.9042);
 *
 * // Random offset within 500 meters
 * Coordinate masked = CoordinateMasker.mask(location, 500);
 *
 * // Reduce to ~100m precision
 * Coordinate reduced = CoordinateMasker.reducePrecision(location, 3);
 *
 * // Align to GeoHash grid (~1.2km)
 * Coordinate gridAligned = CoordinateMasker.maskByGeoHash(location, 6);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core functionality - 核心功能</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public final class CoordinateMasker {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Earth's radius in meters
     * 地球半径（米）
     */
    private static final double EARTH_RADIUS = 6371000.0;

    private CoordinateMasker() {
        // Utility class
    }

    /**
     * Mask coordinate with random offset within specified radius
     * 在指定半径内对坐标添加随机偏移
     *
     * @param coordinate original coordinate | 原始坐标
     * @param maxOffsetMeters maximum offset distance in meters | 最大偏移距离（米）
     * @return masked coordinate | 模糊化后的坐标
     * @throws IllegalArgumentException if coordinate is null or offset is negative | 如果坐标为null或偏移为负则抛出异常
     */
    public static Coordinate mask(Coordinate coordinate, double maxOffsetMeters) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate cannot be null");
        }
        if (maxOffsetMeters < 0) {
            throw new IllegalArgumentException("Max offset cannot be negative");
        }
        if (maxOffsetMeters == 0) {
            return coordinate;
        }

        // Random distance and bearing
        double distance = RANDOM.nextDouble() * maxOffsetMeters;
        double bearing = RANDOM.nextDouble() * 360;

        return destination(coordinate, distance, bearing);
    }

    /**
     * Reduce coordinate precision by rounding to specified decimal places
     * 通过四舍五入到指定小数位来降低坐标精度
     *
     * @param coordinate original coordinate | 原始坐标
     * @param decimalPlaces number of decimal places to keep (3 = ~111m, 4 = ~11m, 5 = ~1.1m) | 保留的小数位数
     * @return reduced precision coordinate | 降低精度后的坐标
     * @throws IllegalArgumentException if coordinate is null or decimalPlaces is negative | 如果坐标为null或小数位数为负则抛出异常
     */
    public static Coordinate reducePrecision(Coordinate coordinate, int decimalPlaces) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate cannot be null");
        }
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("Decimal places cannot be negative");
        }

        double factor = Math.pow(10, decimalPlaces);
        double lng = Math.round(coordinate.longitude() * factor) / factor;
        double lat = Math.round(coordinate.latitude() * factor) / factor;

        return new Coordinate(lng, lat, coordinate.system());
    }

    /**
     * Mask coordinate by aligning to GeoHash grid center
     * 通过对齐到GeoHash网格中心来模糊化坐标
     *
     * @param coordinate original coordinate | 原始坐标
     * @param precision GeoHash precision (4=39km, 5=5km, 6=1.2km, 7=153m) | GeoHash精度
     * @return grid-aligned coordinate | 对齐到网格的坐标
     * @throws IllegalArgumentException if coordinate is null or precision is invalid | 如果坐标为null或精度无效则抛出异常
     */
    public static Coordinate maskByGeoHash(Coordinate coordinate, int precision) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate cannot be null");
        }
        if (precision < 1 || precision > 12) {
            throw new IllegalArgumentException("GeoHash precision must be between 1 and 12");
        }

        // Encode to GeoHash and decode back to get grid center
        String hash = GeoHashUtil.encode(coordinate.latitude(), coordinate.longitude(), precision);
        double[] decoded = GeoHashUtil.decode(hash);

        return new Coordinate(decoded[1], decoded[0], coordinate.system());
    }

    /**
     * Apply multiple masking techniques for enhanced privacy
     * 应用多种模糊化技术以增强隐私保护
     *
     * @param coordinate original coordinate | 原始坐标
     * @param decimalPlaces decimal places for precision reduction | 用于精度降低的小数位数
     * @param maxOffsetMeters maximum random offset in meters | 最大随机偏移（米）
     * @return masked coordinate | 模糊化后的坐标
     */
    public static Coordinate maskEnhanced(Coordinate coordinate, int decimalPlaces, double maxOffsetMeters) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate cannot be null");
        }

        // First reduce precision, then add random offset
        Coordinate reduced = reducePrecision(coordinate, decimalPlaces);
        return mask(reduced, maxOffsetMeters);
    }

    /**
     * Mask coordinate to city-level precision (~5km)
     * 将坐标模糊化到城市级精度（约5公里）
     *
     * @param coordinate original coordinate | 原始坐标
     * @return city-level masked coordinate | 城市级模糊化坐标
     */
    public static Coordinate maskToCity(Coordinate coordinate) {
        return maskByGeoHash(coordinate, 5);
    }

    /**
     * Mask coordinate to neighborhood-level precision (~1km)
     * 将坐标模糊化到街区级精度（约1公里）
     *
     * @param coordinate original coordinate | 原始坐标
     * @return neighborhood-level masked coordinate | 街区级模糊化坐标
     */
    public static Coordinate maskToNeighborhood(Coordinate coordinate) {
        return maskByGeoHash(coordinate, 6);
    }

    /**
     * Mask coordinate to block-level precision (~150m)
     * 将坐标模糊化到街区块级精度（约150米）
     *
     * @param coordinate original coordinate | 原始坐标
     * @return block-level masked coordinate | 街区块级模糊化坐标
     */
    public static Coordinate maskToBlock(Coordinate coordinate) {
        return maskByGeoHash(coordinate, 7);
    }

    /**
     * Calculate destination point given distance and bearing from start
     * 根据起点、距离和方位角计算终点
     *
     * @param start starting coordinate | 起始坐标
     * @param distance distance in meters | 距离（米）
     * @param bearing bearing in degrees (0=north, 90=east) | 方位角（度，0=北，90=东）
     * @return destination coordinate | 终点坐标
     */
    private static Coordinate destination(Coordinate start, double distance, double bearing) {
        double d = distance / EARTH_RADIUS;
        double brng = Math.toRadians(bearing);

        double lat1 = Math.toRadians(start.latitude());
        double lng1 = Math.toRadians(start.longitude());

        double lat2 = Math.asin(
            Math.sin(lat1) * Math.cos(d)
                + Math.cos(lat1) * Math.sin(d) * Math.cos(brng)
        );

        double lng2 = lng1 + Math.atan2(
            Math.sin(brng) * Math.sin(d) * Math.cos(lat1),
            Math.cos(d) - Math.sin(lat1) * Math.sin(lat2)
        );

        return new Coordinate(
            Math.toDegrees(lng2),
            Math.toDegrees(lat2),
            start.system()
        );
    }
}
