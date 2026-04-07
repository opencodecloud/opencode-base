package cloud.opencode.base.geo;

import cloud.opencode.base.geo.distance.DistanceCalculator;
import cloud.opencode.base.geo.distance.DistanceCalculatorFactory;
import cloud.opencode.base.geo.distance.DistanceCalculatorFactory.DistanceAccuracy;
import cloud.opencode.base.geo.distance.HaversineCalculator;
import cloud.opencode.base.geo.distance.VincentyCalculator;
import cloud.opencode.base.geo.fence.GeoFence;
import cloud.opencode.base.geo.fence.PolygonFence;
import cloud.opencode.base.geo.fence.RectangleFence;
import cloud.opencode.base.geo.geohash.GeoHashPrecision;
import cloud.opencode.base.geo.geohash.GeoHashSearch;
import cloud.opencode.base.geo.geohash.GeoHashUtil;
import cloud.opencode.base.geo.polyline.PolylineCodec;
import cloud.opencode.base.geo.polyline.TrackSimplifier;

import java.util.List;
import java.util.Set;

/**
 * OpenGeo - Facade Entry Point
 * OpenGeo - 门面入口类
 *
 * <p>Main entry point for geographic operations including distance calculation,
 * coordinate transformation, geo-fence checking, and GeoHash encoding.</p>
 * <p>地理操作的主入口点，包括距离计算、坐标转换、地理围栏检查和GeoHash编码。</p>
 *
 * <p><strong>Core Features | 核心功能:</strong></p>
 * <ul>
 *   <li>Distance calculation (Haversine/Vincenty) | 距离计算</li>
 *   <li>Coordinate transformation (WGS84/GCJ02/BD09) | 坐标转换</li>
 *   <li>Geo-fence operations (circle/polygon/rectangle) | 地理围栏操作</li>
 *   <li>GeoHash encoding/decoding | GeoHash编解码</li>
 *   <li>Bearing and destination calculation | 方位角和目标点计算</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is thread-safe. All methods are stateless static methods.</p>
 * <p>此类是线程安全的。所有方法都是无状态的静态方法。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Distance calculation
 * Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
 * Coordinate shanghai = Coordinate.wgs84(121.4737, 31.2304);
 * double distance = OpenGeo.distance(beijing, shanghai);
 * System.out.println(OpenGeo.formatDistance(distance));  // "1068.0公里"
 *
 * // Coordinate transformation
 * Coordinate gcj02 = OpenGeo.wgs84ToGcj02(116.4074, 39.9042);
 *
 * // Geo-fence check
 * boolean inCircle = OpenGeo.inCircle(point, center, 1000);
 * boolean inPolygon = OpenGeo.inPolygon(point, polygonVertices);
 *
 * // GeoHash
 * String hash = OpenGeo.geoHash(116.4074, 39.9042, 8);
 * Coordinate decoded = OpenGeo.fromGeoHash(hash);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless static methods) - 线程安全: 是（无状态静态方法）</li>
 *   <li>Null-safe: Partial (returns 0 or null for null inputs) - 空值安全: 部分（对null输入返回0或null）</li>
 * </ul>
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
public final class OpenGeo {

    /**
     * Default distance calculator (Haversine - fast with ~0.5% accuracy)
     * 默认距离计算器（Haversine - 快速，精度约0.5%）
     */
    private static final DistanceCalculator CALCULATOR = HaversineCalculator.INSTANCE;

    /**
     * Earth's mean radius in meters
     * 地球平均半径（米）
     */
    public static final double EARTH_RADIUS = 6371000.0;

    private OpenGeo() {
        // Utility class - no instantiation
    }

    // ============ Distance Calculation | 距离计算 ============

    /**
     * Calculate distance between two coordinates in meters
     * 计算两个坐标之间的距离（米）
     *
     * <p>Both coordinates are automatically converted to WGS84 before calculation.</p>
     * <p>两个坐标在计算前会自动转换为WGS84。</p>
     *
     * @param c1 first coordinate | 第一个坐标
     * @param c2 second coordinate | 第二个坐标
     * @return distance in meters | 距离（米）
     */
    public static double distance(Coordinate c1, Coordinate c2) {
        if (c1 == null || c2 == null) {
            return 0;
        }
        // Ensure both coordinates are in WGS84
        Coordinate p1 = c1.to(CoordinateSystem.WGS84);
        Coordinate p2 = c2.to(CoordinateSystem.WGS84);
        return CALCULATOR.calculate(p1, p2);
    }

    /**
     * Calculate distance between two points by coordinates in meters
     * 计算两点之间的距离（米）
     *
     * @param lng1 first point longitude | 第一点经度
     * @param lat1 first point latitude | 第一点纬度
     * @param lng2 second point longitude | 第二点经度
     * @param lat2 second point latitude | 第二点纬度
     * @return distance in meters | 距离（米）
     */
    public static double distance(double lng1, double lat1, double lng2, double lat2) {
        return CALCULATOR.calculate(
            Coordinate.wgs84(lng1, lat1),
            Coordinate.wgs84(lng2, lat2)
        );
    }

    /**
     * Calculate distance between two coordinates using specified accuracy level
     * 使用指定精度级别计算两个坐标之间的距离
     *
     * <p><strong>Accuracy Levels | 精度级别:</strong></p>
     * <ul>
     *   <li>{@code FAST} - Haversine algorithm, ~0.5% error, fast - Haversine算法，约0.5%误差，快速</li>
     *   <li>{@code PRECISE} - Vincenty algorithm, ~0.5mm error, slower - Vincenty算法，约0.5mm误差，较慢</li>
     * </ul>
     *
     * <p><strong>Usage Examples | 使用示例:</strong></p>
     * <pre>{@code
     * // Fast calculation (default behavior)
     * double fast = OpenGeo.distance(c1, c2, DistanceAccuracy.FAST);
     *
     * // Precise calculation for surveying
     * double precise = OpenGeo.distance(c1, c2, DistanceAccuracy.PRECISE);
     * }</pre>
     *
     * @param c1 first coordinate | 第一个坐标
     * @param c2 second coordinate | 第二个坐标
     * @param accuracy the accuracy level | 精度级别
     * @return distance in meters | 距离（米）
     */
    public static double distance(Coordinate c1, Coordinate c2, DistanceAccuracy accuracy) {
        if (c1 == null || c2 == null) {
            return 0;
        }
        Coordinate p1 = c1.to(CoordinateSystem.WGS84);
        Coordinate p2 = c2.to(CoordinateSystem.WGS84);
        return DistanceCalculatorFactory.create(accuracy).calculate(p1, p2);
    }

    /**
     * Calculate precise distance between two coordinates using Vincenty algorithm
     * 使用Vincenty算法计算两个坐标之间的精确距离
     *
     * <p>Vincenty algorithm provides ~0.5mm accuracy, suitable for surveying applications.</p>
     * <p>Vincenty算法提供约0.5mm的精度，适合测绘应用。</p>
     *
     * @param c1 first coordinate | 第一个坐标
     * @param c2 second coordinate | 第二个坐标
     * @return distance in meters | 距离（米）
     */
    public static double distancePrecise(Coordinate c1, Coordinate c2) {
        if (c1 == null || c2 == null) {
            return 0;
        }
        Coordinate p1 = c1.to(CoordinateSystem.WGS84);
        Coordinate p2 = c2.to(CoordinateSystem.WGS84);
        return VincentyCalculator.INSTANCE.calculate(p1, p2);
    }

    /**
     * Format distance for display
     * 格式化距离用于显示
     *
     * @param meters distance in meters | 距离（米）
     * @return formatted string (e.g., "500米" or "1.5公里") | 格式化字符串
     */
    public static String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format("%.0f米", meters);
        } else {
            return String.format("%.1f公里", meters / 1000);
        }
    }

    // ============ Coordinate Transformation | 坐标转换 ============

    /**
     * Transform WGS84 to GCJ02
     * WGS84转GCJ02
     *
     * @param lng longitude | 经度
     * @param lat latitude | 纬度
     * @return GCJ02 coordinate | GCJ02坐标
     */
    public static Coordinate wgs84ToGcj02(double lng, double lat) {
        return Coordinate.wgs84(lng, lat).to(CoordinateSystem.GCJ02);
    }

    /**
     * Transform GCJ02 to BD09
     * GCJ02转BD09
     *
     * @param lng longitude | 经度
     * @param lat latitude | 纬度
     * @return BD09 coordinate | BD09坐标
     */
    public static Coordinate gcj02ToBd09(double lng, double lat) {
        return Coordinate.gcj02(lng, lat).to(CoordinateSystem.BD09);
    }

    /**
     * Transform BD09 to GCJ02
     * BD09转GCJ02
     *
     * @param lng longitude | 经度
     * @param lat latitude | 纬度
     * @return GCJ02 coordinate | GCJ02坐标
     */
    public static Coordinate bd09ToGcj02(double lng, double lat) {
        return Coordinate.bd09(lng, lat).to(CoordinateSystem.GCJ02);
    }

    /**
     * Transform WGS84 to BD09
     * WGS84转BD09
     *
     * @param lng longitude | 经度
     * @param lat latitude | 纬度
     * @return BD09 coordinate | BD09坐标
     */
    public static Coordinate wgs84ToBd09(double lng, double lat) {
        return Coordinate.wgs84(lng, lat).to(CoordinateSystem.BD09);
    }

    /**
     * Transform BD09 to WGS84
     * BD09转WGS84
     *
     * @param lng longitude | 经度
     * @param lat latitude | 纬度
     * @return WGS84 coordinate | WGS84坐标
     */
    public static Coordinate bd09ToWgs84(double lng, double lat) {
        return Coordinate.bd09(lng, lat).to(CoordinateSystem.WGS84);
    }

    /**
     * Transform GCJ02 to WGS84
     * GCJ02转WGS84
     *
     * @param lng longitude | 经度
     * @param lat latitude | 纬度
     * @return WGS84 coordinate | WGS84坐标
     */
    public static Coordinate gcj02ToWgs84(double lng, double lat) {
        return Coordinate.gcj02(lng, lat).to(CoordinateSystem.WGS84);
    }

    // ============ Geo-Fence | 地理围栏 ============

    /**
     * Check if point is inside circular fence
     * 检查点是否在圆形围栏内
     *
     * @param point the point to check | 要检查的点
     * @param center center of circle | 圆心
     * @param radius radius in meters | 半径（米）
     * @return true if inside | 如果在内部返回true
     */
    public static boolean inCircle(Coordinate point, Coordinate center, double radius) {
        return distance(point, center) <= radius;
    }

    /**
     * Check if point is inside polygon
     * 检查点是否在多边形内
     *
     * @param point the point to check | 要检查的点
     * @param polygon list of polygon vertices | 多边形顶点列表
     * @return true if inside | 如果在内部返回true
     */
    public static boolean inPolygon(Coordinate point, List<Coordinate> polygon) {
        if (point == null || polygon == null || polygon.size() < 3) {
            return false;
        }
        GeoFence fence = new PolygonFence(polygon);
        return fence.contains(point);
    }

    /**
     * Check if point is inside rectangle
     * 检查点是否在矩形内
     *
     * @param point the point to check | 要检查的点
     * @param southwest southwest corner | 西南角
     * @param northeast northeast corner | 东北角
     * @return true if inside | 如果在内部返回true
     */
    public static boolean inRectangle(Coordinate point, Coordinate southwest, Coordinate northeast) {
        if (point == null || southwest == null || northeast == null) {
            return false;
        }
        GeoFence fence = new RectangleFence(southwest, northeast);
        return fence.contains(point);
    }

    // ============ GeoHash | GeoHash编码 ============

    /**
     * Encode coordinate to GeoHash
     * 将坐标编码为GeoHash
     *
     * @param lng longitude | 经度
     * @param lat latitude | 纬度
     * @param precision precision (1-12) | 精度（1-12）
     * @return GeoHash string | GeoHash字符串
     */
    public static String geoHash(double lng, double lat, int precision) {
        return GeoHashUtil.encode(lat, lng, precision);
    }

    /**
     * Decode GeoHash to coordinate
     * 将GeoHash解码为坐标
     *
     * @param hash GeoHash string | GeoHash字符串
     * @return decoded coordinate (WGS84) | 解码后的坐标（WGS84）
     */
    public static Coordinate fromGeoHash(String hash) {
        double[] coords = GeoHashUtil.decode(hash);
        return Coordinate.wgs84(coords[1], coords[0]);  // [lat, lng] -> [lng, lat]
    }

    /**
     * Get neighboring GeoHashes
     * 获取相邻的GeoHash
     *
     * @param hash center GeoHash | 中心GeoHash
     * @return list of 8 neighboring GeoHashes | 8个相邻GeoHash的列表
     */
    public static List<String> geoHashNeighbors(String hash) {
        return GeoHashUtil.neighbors(hash);
    }

    /**
     * Get GeoHash bounding box
     * 获取GeoHash边界框
     *
     * @param hash GeoHash string | GeoHash字符串
     * @return bounding box [minLat, minLng, maxLat, maxLng] | 边界框
     */
    public static double[] geoHashBoundingBox(String hash) {
        return GeoHashUtil.getBoundingBox(hash);
    }

    // ============ Utility Methods | 辅助方法 ============

    /**
     * Check if coordinate is within China bounds
     * 检查坐标是否在中国境内
     *
     * @param lng longitude | 经度
     * @param lat latitude | 纬度
     * @return true if in China | 如果在中国境内返回true
     */
    public static boolean isInChina(double lng, double lat) {
        return lng >= 72.004 && lng <= 137.8347
            && lat >= 0.8293 && lat <= 55.8271;
    }

    /**
     * Calculate bearing from one point to another in degrees
     * 计算从一点到另一点的方位角（度）
     *
     * @param from starting point | 起点
     * @param to ending point | 终点
     * @return bearing in degrees (0-360, 0=North, 90=East) | 方位角（0-360度，0=北，90=东）
     */
    public static double bearing(Coordinate from, Coordinate to) {
        if (from == null || to == null) {
            return 0;
        }

        double lng1 = Math.toRadians(from.longitude());
        double lat1 = Math.toRadians(from.latitude());
        double lng2 = Math.toRadians(to.longitude());
        double lat2 = Math.toRadians(to.latitude());

        double dLng = lng2 - lng1;
        double x = Math.cos(lat2) * Math.sin(dLng);
        double y = Math.cos(lat1) * Math.sin(lat2)
            - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng);

        double bearing = Math.toDegrees(Math.atan2(x, y));
        return (bearing + 360) % 360;
    }

    /**
     * Calculate destination point given start, distance, and bearing
     * 根据起点、距离和方位角计算终点
     *
     * @param start starting point | 起点
     * @param distance distance in meters | 距离（米）
     * @param bearing bearing in degrees (0=North, 90=East) | 方位角（度，0=北，90=东）
     * @return destination coordinate (same coordinate system as start) | 终点坐标（与起点相同坐标系）
     */
    public static Coordinate destination(Coordinate start, double distance, double bearing) {
        if (start == null) {
            return null;
        }

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

    /**
     * Calculate the midpoint between two coordinates
     * 计算两个坐标之间的中点
     *
     * @param c1 first coordinate | 第一个坐标
     * @param c2 second coordinate | 第二个坐标
     * @return midpoint coordinate (WGS84) | 中点坐标（WGS84）
     */
    public static Coordinate midpoint(Coordinate c1, Coordinate c2) {
        if (c1 == null || c2 == null) {
            return null;
        }

        Coordinate p1 = c1.to(CoordinateSystem.WGS84);
        Coordinate p2 = c2.to(CoordinateSystem.WGS84);

        double lat1 = Math.toRadians(p1.latitude());
        double lng1 = Math.toRadians(p1.longitude());
        double lat2 = Math.toRadians(p2.latitude());
        double lng2 = Math.toRadians(p2.longitude());

        double bx = Math.cos(lat2) * Math.cos(lng2 - lng1);
        double by = Math.cos(lat2) * Math.sin(lng2 - lng1);

        double lat3 = Math.atan2(
            Math.sin(lat1) + Math.sin(lat2),
            Math.sqrt((Math.cos(lat1) + bx) * (Math.cos(lat1) + bx) + by * by)
        );

        double lng3 = lng1 + Math.atan2(by, Math.cos(lat1) + bx);

        return Coordinate.wgs84(Math.toDegrees(lng3), Math.toDegrees(lat3));
    }

    /**
     * Check if coordinate values are valid
     * 检查坐标值是否有效
     *
     * @param lng longitude | 经度
     * @param lat latitude | 纬度
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidCoordinate(double lng, double lat) {
        return lng >= -180 && lng <= 180
            && lat >= -90 && lat <= 90
            && !Double.isNaN(lng) && !Double.isNaN(lat)
            && !Double.isInfinite(lng) && !Double.isInfinite(lat);
    }

    // ============ BoundingBox | 边界框 ============

    /**
     * Create a bounding box from coordinates
     * 从坐标集合创建边界框
     *
     * @param coordinates list of coordinates | 坐标列表
     * @return bounding box | 边界框
     */
    public static BoundingBox boundingBox(java.util.Collection<Coordinate> coordinates) {
        return BoundingBox.fromCoordinates(coordinates);
    }

    /**
     * Create a bounding box from center and radius
     * 从中心点和半径创建边界框
     *
     * @param center center coordinate | 中心坐标
     * @param radiusMeters radius in meters | 半径（米）
     * @return bounding box | 边界框
     */
    public static BoundingBox boundingBox(Coordinate center, double radiusMeters) {
        return BoundingBox.fromCenter(center, radiusMeters);
    }

    // ============ GeoHash Search | GeoHash搜索 ============

    /**
     * Search GeoHash cells covering a circular area
     * 搜索覆盖圆形区域的GeoHash格子
     *
     * @param lat latitude | 纬度
     * @param lng longitude | 经度
     * @param radiusMeters search radius in meters | 搜索半径（米）
     * @return set of GeoHash strings | GeoHash字符串集合
     */
    public static Set<String> geoHashSearch(double lat, double lng, double radiusMeters) {
        return GeoHashSearch.searchHashes(lat, lng, radiusMeters);
    }

    /**
     * Search GeoHash cells covering a circular area with specified precision
     * 使用指定精度搜索覆盖圆形区域的GeoHash格子
     *
     * @param lat latitude | 纬度
     * @param lng longitude | 经度
     * @param radiusMeters search radius in meters | 搜索半径（米）
     * @param precision GeoHash precision | GeoHash精度
     * @return set of GeoHash strings | GeoHash字符串集合
     */
    public static Set<String> geoHashSearch(double lat, double lng, double radiusMeters, int precision) {
        return GeoHashSearch.searchHashes(lat, lng, radiusMeters, precision);
    }

    // ============ Polyline | 折线编解码 ============

    /**
     * Encode coordinates to Google Encoded Polyline format
     * 将坐标编码为Google Encoded Polyline格式
     *
     * @param coordinates list of coordinates | 坐标列表
     * @return encoded polyline string | 编码后的折线字符串
     */
    public static String encodePolyline(List<Coordinate> coordinates) {
        return PolylineCodec.encode(coordinates);
    }

    /**
     * Decode Google Encoded Polyline to coordinates
     * 将Google Encoded Polyline解码为坐标
     *
     * @param encoded encoded polyline string | 编码后的折线字符串
     * @return list of coordinates | 坐标列表
     */
    public static List<Coordinate> decodePolyline(String encoded) {
        return PolylineCodec.decode(encoded);
    }

    // ============ Track Operations | 轨迹操作 ============

    /**
     * Simplify a GPS track using Ramer-Douglas-Peucker algorithm
     * 使用Ramer-Douglas-Peucker算法简化GPS轨迹
     *
     * @param track list of track coordinates | 轨迹坐标列表
     * @param toleranceMeters simplification tolerance in meters | 简化容差（米）
     * @return simplified track | 简化后的轨迹
     */
    public static List<Coordinate> simplifyTrack(List<Coordinate> track, double toleranceMeters) {
        return TrackSimplifier.simplify(track, toleranceMeters);
    }

    /**
     * Calculate total distance of a track in meters
     * 计算轨迹总距离（米）
     *
     * @param track list of track coordinates | 轨迹坐标列表
     * @return total distance in meters | 总距离（米）
     */
    public static double trackDistance(List<Coordinate> track) {
        return GeoUtil.totalDistance(track);
    }

    // ============ Geometry | 几何计算 ============

    /**
     * Calculate centroid of a set of coordinates
     * 计算坐标集合的质心
     *
     * @param coordinates list of coordinates | 坐标列表
     * @return centroid coordinate (WGS84) | 质心坐标（WGS84）
     */
    public static Coordinate centroid(List<Coordinate> coordinates) {
        return GeoUtil.centroid(coordinates);
    }

    /**
     * Interpolate a point along the great-circle path between two coordinates
     * 沿两个坐标之间的大圆路径插值
     *
     * @param c1 start coordinate | 起始坐标
     * @param c2 end coordinate | 终止坐标
     * @param fraction fraction along the path (0.0-1.0) | 路径分数（0.0-1.0）
     * @return interpolated coordinate (WGS84) | 插值坐标（WGS84）
     */
    public static Coordinate interpolate(Coordinate c1, Coordinate c2, double fraction) {
        return GeoUtil.interpolate(c1, c2, fraction);
    }

    /**
     * Get compass direction from bearing
     * 获取方位角对应的罗盘方向
     *
     * @param bearing bearing in degrees (0-360) | 方位角（0-360度）
     * @return compass direction (e.g., "N", "NE", "SSW") | 罗盘方向
     */
    public static String compassDirection(double bearing) {
        return GeoUtil.compassDirection(bearing);
    }

    /**
     * Calculate distance from a point to a line segment in meters
     * 计算点到线段的距离（米）
     *
     * @param point the point | 点
     * @param start segment start | 线段起点
     * @param end segment end | 线段终点
     * @return distance in meters | 距离（米）
     */
    public static double distanceToSegment(Coordinate point, Coordinate start, Coordinate end) {
        return GeoUtil.distanceToSegment(point, start, end);
    }

    /**
     * Calculate distance from a point to a polyline in meters
     * 计算点到折线的距离（米）
     *
     * @param point the point | 点
     * @param polyline the polyline vertices | 折线顶点
     * @return distance in meters | 距离（米）
     */
    public static double distanceToPolyline(Coordinate point, List<Coordinate> polyline) {
        return GeoUtil.distanceToPolyline(point, polyline);
    }
}
