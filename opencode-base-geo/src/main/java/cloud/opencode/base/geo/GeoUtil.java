package cloud.opencode.base.geo;

import cloud.opencode.base.geo.fence.CircleFence;
import cloud.opencode.base.geo.fence.GeoFence;
import cloud.opencode.base.geo.fence.PolygonFence;
import cloud.opencode.base.geo.fence.RectangleFence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * GeoUtil - Geographic Utility Class
 * GeoUtil - 地理工具类
 *
 * <p>Utility class providing additional geographic helper methods.</p>
 * <p>提供额外地理辅助方法的工具类。</p>
 *
 * <p><strong>Features | 功能:</strong></p>
 * <ul>
 *   <li>Fence creation helpers | 围栏创建辅助方法</li>
 *   <li>Bounding box calculation | 边界框计算</li>
 *   <li>Sorting by distance | 按距离排序</li>
 *   <li>Filtering by distance | 按距离过滤</li>
 *   <li>Area calculation | 面积计算</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is thread-safe. All methods are stateless static methods.</p>
 * <p>此类是线程安全的。所有方法都是无状态的静态方法。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create circle fence
 * GeoFence circle = GeoUtil.createCircleFence(116.4074, 39.9042, 1000);
 *
 * // Get bounding box for coordinates
 * double[] bbox = GeoUtil.getBoundingBox(coordinates);
 *
 * // Sort locations by distance
 * List<Location> sorted = GeoUtil.sortByDistance(locations, center, Location::getCoordinate);
 *
 * // Filter by distance
 * List<Location> nearby = GeoUtil.filterByDistance(locations, center, 5000, Location::getCoordinate);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless static methods) - 线程安全: 是（无状态静态方法）</li>
 *   <li>Null-safe: Partial (methods handle null inputs gracefully) - 空值安全: 部分（方法对null输入有容错处理）</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core functionality - 核心功能</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for batch operations - 时间复杂度: O(n)（批量操作）</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public final class GeoUtil {

    /**
     * Meters per degree at equator (approximately)
     * 赤道上每度的米数（约）
     */
    public static final double METERS_PER_DEGREE = 111319.9;

    private GeoUtil() {
        // Utility class - no instantiation
    }

    // ============ Fence Creation | 围栏创建 ============

    /**
     * Create a circle fence
     * 创建圆形围栏
     *
     * @param longitude center longitude | 中心经度
     * @param latitude center latitude | 中心纬度
     * @param radiusMeters radius in meters | 半径（米）
     * @return circle fence | 圆形围栏
     */
    public static GeoFence createCircleFence(double longitude, double latitude, double radiusMeters) {
        return new CircleFence(Coordinate.wgs84(longitude, latitude), radiusMeters);
    }

    /**
     * Create a circle fence around a coordinate
     * 创建以坐标为中心的圆形围栏
     *
     * @param center center coordinate | 中心坐标
     * @param radiusMeters radius in meters | 半径（米）
     * @return circle fence | 圆形围栏
     */
    public static GeoFence createCircleFence(Coordinate center, double radiusMeters) {
        return new CircleFence(center, radiusMeters);
    }

    /**
     * Create a rectangle fence
     * 创建矩形围栏
     *
     * @param minLng minimum longitude (west) | 最小经度（西）
     * @param minLat minimum latitude (south) | 最小纬度（南）
     * @param maxLng maximum longitude (east) | 最大经度（东）
     * @param maxLat maximum latitude (north) | 最大纬度（北）
     * @return rectangle fence | 矩形围栏
     */
    public static GeoFence createRectangleFence(double minLng, double minLat,
                                                 double maxLng, double maxLat) {
        return new RectangleFence(
            Coordinate.wgs84(minLng, minLat),
            Coordinate.wgs84(maxLng, maxLat)
        );
    }

    /**
     * Create a polygon fence from coordinates
     * 从坐标创建多边形围栏
     *
     * @param vertices list of vertex coordinates | 顶点坐标列表
     * @return polygon fence | 多边形围栏
     */
    public static GeoFence createPolygonFence(List<Coordinate> vertices) {
        return new PolygonFence(vertices);
    }

    /**
     * Create a polygon fence from longitude/latitude pairs
     * 从经纬度对创建多边形围栏
     *
     * @param lngLatPairs array of [lng, lat, lng, lat, ...] | 经纬度数组
     * @return polygon fence | 多边形围栏
     */
    public static GeoFence createPolygonFence(double... lngLatPairs) {
        if (lngLatPairs == null || lngLatPairs.length < 6 || lngLatPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Need at least 3 coordinate pairs (6 values)");
        }

        List<Coordinate> vertices = new ArrayList<>();
        for (int i = 0; i < lngLatPairs.length; i += 2) {
            vertices.add(Coordinate.wgs84(lngLatPairs[i], lngLatPairs[i + 1]));
        }
        return new PolygonFence(vertices);
    }

    // ============ Bounding Box | 边界框 ============

    /**
     * Calculate bounding box for a list of coordinates
     * 计算坐标列表的边界框
     *
     * @param coordinates list of coordinates | 坐标列表
     * @return bounding box [minLng, minLat, maxLng, maxLat] or null if empty | 边界框或null（如果为空）
     */
    public static double[] getBoundingBox(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return null;
        }

        double minLng = Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;

        for (Coordinate c : coordinates) {
            if (c == null) continue;
            if (c.longitude() < minLng) minLng = c.longitude();
            if (c.longitude() > maxLng) maxLng = c.longitude();
            if (c.latitude() < minLat) minLat = c.latitude();
            if (c.latitude() > maxLat) maxLat = c.latitude();
        }

        return new double[]{minLng, minLat, maxLng, maxLat};
    }

    /**
     * Calculate bounding box center
     * 计算边界框中心
     *
     * @param boundingBox bounding box [minLng, minLat, maxLng, maxLat] | 边界框
     * @return center coordinate | 中心坐标
     */
    public static Coordinate getBoundingBoxCenter(double[] boundingBox) {
        if (boundingBox == null || boundingBox.length < 4) {
            return null;
        }
        double centerLng = (boundingBox[0] + boundingBox[2]) / 2;
        double centerLat = (boundingBox[1] + boundingBox[3]) / 2;
        return Coordinate.wgs84(centerLng, centerLat);
    }

    /**
     * Expand bounding box by distance in meters
     * 按米数扩展边界框
     *
     * @param boundingBox original bounding box | 原始边界框
     * @param meters distance to expand in meters | 扩展的距离（米）
     * @return expanded bounding box | 扩展后的边界框
     */
    public static double[] expandBoundingBox(double[] boundingBox, double meters) {
        if (boundingBox == null || boundingBox.length < 4) {
            return null;
        }

        double deltaLat = meters / METERS_PER_DEGREE;
        double avgLat = (boundingBox[1] + boundingBox[3]) / 2;
        double cosLat = Math.cos(Math.toRadians(avgLat));
        double deltaLng;
        if (cosLat < 1e-10) {
            // Near poles, span full longitude
            deltaLng = 180.0;
        } else {
            deltaLng = meters / (METERS_PER_DEGREE * cosLat);
        }

        return new double[]{
            boundingBox[0] - deltaLng,
            boundingBox[1] - deltaLat,
            boundingBox[2] + deltaLng,
            boundingBox[3] + deltaLat
        };
    }

    // ============ Distance Operations | 距离操作 ============

    /**
     * Sort items by distance from a point
     * 按距离从某点排序
     *
     * @param <T> item type | 项目类型
     * @param items list of items | 项目列表
     * @param from reference point | 参考点
     * @param coordinateExtractor function to extract coordinate from item | 从项目提取坐标的函数
     * @return sorted list (closest first) | 排序后的列表（最近的在前）
     */
    public static <T> List<T> sortByDistance(List<T> items, Coordinate from,
                                              java.util.function.Function<T, Coordinate> coordinateExtractor) {
        if (items == null || items.isEmpty() || from == null) {
            return items;
        }

        return items.stream()
            .sorted(Comparator.comparingDouble(item -> {
                Coordinate coord = coordinateExtractor.apply(item);
                return coord != null ? OpenGeo.distance(from, coord) : Double.MAX_VALUE;
            }))
            .toList();
    }

    /**
     * Filter items within distance from a point
     * 过滤某点距离内的项目
     *
     * @param <T> item type | 项目类型
     * @param items list of items | 项目列表
     * @param from reference point | 参考点
     * @param maxDistanceMeters maximum distance in meters | 最大距离（米）
     * @param coordinateExtractor function to extract coordinate from item | 从项目提取坐标的函数
     * @return filtered list | 过滤后的列表
     */
    public static <T> List<T> filterByDistance(List<T> items, Coordinate from,
                                                double maxDistanceMeters,
                                                java.util.function.Function<T, Coordinate> coordinateExtractor) {
        if (items == null || items.isEmpty() || from == null) {
            return new ArrayList<>();
        }

        return items.stream()
            .filter(item -> {
                Coordinate coord = coordinateExtractor.apply(item);
                return coord != null && OpenGeo.distance(from, coord) <= maxDistanceMeters;
            })
            .toList();
    }

    /**
     * Find the nearest item to a point
     * 查找距离某点最近的项目
     *
     * @param <T> item type | 项目类型
     * @param items list of items | 项目列表
     * @param from reference point | 参考点
     * @param coordinateExtractor function to extract coordinate from item | 从项目提取坐标的函数
     * @return nearest item or null if list is empty | 最近的项目或null（如果列表为空）
     */
    public static <T> T findNearest(List<T> items, Coordinate from,
                                     java.util.function.Function<T, Coordinate> coordinateExtractor) {
        if (items == null || items.isEmpty() || from == null) {
            return null;
        }

        return items.stream()
            .min(Comparator.comparingDouble(item -> {
                Coordinate coord = coordinateExtractor.apply(item);
                return coord != null ? OpenGeo.distance(from, coord) : Double.MAX_VALUE;
            }))
            .orElse(null);
    }

    // ============ Area Calculation | 面积计算 ============

    /**
     * Calculate approximate area of a polygon in square meters
     * 计算多边形的近似面积（平方米）
     *
     * <p>Uses the Shoelace formula with spherical correction.</p>
     * <p>使用带球面校正的鞋带公式。</p>
     *
     * @param vertices polygon vertices | 多边形顶点
     * @return area in square meters | 面积（平方米）
     */
    public static double calculatePolygonArea(List<Coordinate> vertices) {
        if (vertices == null || vertices.size() < 3) {
            return 0;
        }

        int n = vertices.size();
        double area = 0;

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            Coordinate ci = vertices.get(i);
            Coordinate cj = vertices.get(j);

            area += Math.toRadians(cj.longitude() - ci.longitude())
                * (2 + Math.sin(Math.toRadians(ci.latitude()))
                + Math.sin(Math.toRadians(cj.latitude())));
        }

        area = Math.abs(area * OpenGeo.EARTH_RADIUS * OpenGeo.EARTH_RADIUS / 2.0);
        return area;
    }

    /**
     * Calculate circumference of a polygon in meters
     * 计算多边形的周长（米）
     *
     * @param vertices polygon vertices | 多边形顶点
     * @return circumference in meters | 周长（米）
     */
    public static double calculatePolygonCircumference(List<Coordinate> vertices) {
        if (vertices == null || vertices.size() < 2) {
            return 0;
        }

        double total = 0;
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            total += OpenGeo.distance(vertices.get(i), vertices.get(j));
        }

        return total;
    }

    // ============ Conversion | 转换 ============

    /**
     * Convert degrees to radians
     * 将度转换为弧度
     *
     * @param degrees angle in degrees | 角度（度）
     * @return angle in radians | 角度（弧度）
     */
    public static double toRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    /**
     * Convert radians to degrees
     * 将弧度转换为度
     *
     * @param radians angle in radians | 角度（弧度）
     * @return angle in degrees | 角度（度）
     */
    public static double toDegrees(double radians) {
        return Math.toDegrees(radians);
    }

    /**
     * Convert meters to degrees at given latitude
     * 在给定纬度将米转换为度
     *
     * @param meters distance in meters | 距离（米）
     * @param latitude the latitude for conversion | 用于转换的纬度
     * @return distance in degrees | 距离（度）
     */
    public static double metersToDegrees(double meters, double latitude) {
        return meters / (METERS_PER_DEGREE * Math.cos(Math.toRadians(latitude)));
    }

    /**
     * Convert degrees to meters at given latitude
     * 在给定纬度将度转换为米
     *
     * @param degrees distance in degrees | 距离（度）
     * @param latitude the latitude for conversion | 用于转换的纬度
     * @return distance in meters | 距离（米）
     */
    public static double degreesToMeters(double degrees, double latitude) {
        return degrees * METERS_PER_DEGREE * Math.cos(Math.toRadians(latitude));
    }

    // ============ Centroid & Path | 质心和路径 ============

    /**
     * Calculate the geographic centroid of a set of coordinates using vector averaging
     * 使用向量平均法计算一组坐标的地理质心
     *
     * <p>Converts each coordinate to Cartesian (x,y,z), averages, then converts back to lat/lng.
     * This provides accurate results even for coordinates spanning large areas.</p>
     * <p>将每个坐标转换为笛卡尔坐标（x,y,z），取平均值后转换回经纬度。
     * 即使坐标跨越大范围区域也能提供准确结果。</p>
     *
     * @param coordinates list of coordinates | 坐标列表
     * @return centroid coordinate (WGS84), or null if input is null/empty/all-null | 质心坐标（WGS84），输入为 null/空/全 null 时返回 null
     */
    public static Coordinate centroid(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return null;
        }
        double x = 0, y = 0, z = 0;
        int count = 0;
        for (Coordinate c : coordinates) {
            if (c == null) {
                continue;
            }
            Coordinate wgs = c.to(CoordinateSystem.WGS84);
            double latRad = Math.toRadians(wgs.latitude());
            double lngRad = Math.toRadians(wgs.longitude());
            x += Math.cos(latRad) * Math.cos(lngRad);
            y += Math.cos(latRad) * Math.sin(lngRad);
            z += Math.sin(latRad);
            count++;
        }
        if (count == 0) {
            return null;
        }
        x /= count;
        y /= count;
        z /= count;
        double lng = Math.toDegrees(Math.atan2(y, x));
        double hyp = Math.sqrt(x * x + y * y);
        double lat = Math.toDegrees(Math.atan2(z, hyp));
        return Coordinate.wgs84(lng, lat);
    }

    /**
     * Calculate total path distance in meters along a list of coordinates
     * 计算坐标列表的总路径距离（米）
     *
     * <p>Sums the Haversine distances between consecutive coordinate pairs.</p>
     * <p>对连续坐标对之间的 Haversine 距离求和。</p>
     *
     * @param coordinates list of coordinates forming a path | 构成路径的坐标列表
     * @return total distance in meters, 0 if fewer than 2 valid points | 总距离（米），少于2个有效点时返回0
     */
    public static double totalDistance(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            return 0;
        }
        double total = 0;
        Coordinate prev = null;
        for (Coordinate c : coordinates) {
            if (c == null) {
                continue;
            }
            if (prev != null) {
                total += OpenGeo.distance(prev, c);
            }
            prev = c;
        }
        return total;
    }

    // ============ Point-to-Segment Distance | 点到线段距离 ============

    /**
     * Calculate shortest distance from a point to a line segment in meters
     * 计算点到线段的最短距离（米）
     *
     * <p>Uses cross-track distance formula with along-track clamping to snap
     * to the nearest endpoint when the projection falls outside the segment.</p>
     * <p>使用跨轨距离公式，当投影落在线段外时夹紧到最近端点。</p>
     *
     * @param point the point | 目标点
     * @param start segment start | 线段起点
     * @param end segment end | 线段终点
     * @return shortest distance in meters | 最短距离（米）
     */
    public static double distanceToSegment(Coordinate point, Coordinate start, Coordinate end) {
        if (point == null || start == null || end == null) {
            return 0;
        }

        double d13 = OpenGeo.distance(start, point);
        double d12 = OpenGeo.distance(start, end);

        // Degenerate segment (start == end)
        if (d12 < 1e-10) {
            return d13;
        }

        double b12 = Math.toRadians(OpenGeo.bearing(start, end));
        double b13 = Math.toRadians(OpenGeo.bearing(start, point));

        double R = OpenGeo.EARTH_RADIUS;
        double angularD13 = d13 / R;

        // Cross-track distance
        double dxt = Math.asin(Math.sin(angularD13) * Math.sin(b13 - b12)) * R;

        // Along-track distance
        double cosAngularD13 = Math.cos(angularD13);
        double cosDxtR = Math.cos(dxt / R);
        // Clamp to avoid NaN from acos due to floating-point errors
        double ratio = cosAngularD13 / cosDxtR;
        ratio = Math.max(-1.0, Math.min(1.0, ratio));
        double dat = Math.acos(ratio) * R;

        // If along-track distance exceeds segment length, snap to nearest endpoint
        if (dat > d12) {
            double d23 = OpenGeo.distance(end, point);
            return Math.min(d13, d23);
        }

        return Math.abs(dxt);
    }

    /**
     * Calculate shortest distance from a point to a polyline in meters
     * 计算点到折线的最短距离（米）
     *
     * <p>Iterates all segments of the polyline and returns the minimum distance.</p>
     * <p>遍历折线的所有线段，返回最小距离。</p>
     *
     * @param point the point | 目标点
     * @param polyline list of coordinates forming the polyline | 构成折线的坐标列表
     * @return shortest distance in meters, 0 if inputs are invalid | 最短距离（米），输入无效时返回0
     */
    public static double distanceToPolyline(Coordinate point, List<Coordinate> polyline) {
        if (point == null || polyline == null || polyline.size() < 2) {
            return 0;
        }
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < polyline.size() - 1; i++) {
            Coordinate segStart = polyline.get(i);
            Coordinate segEnd = polyline.get(i + 1);
            if (segStart == null || segEnd == null) {
                continue;
            }
            double dist = distanceToSegment(point, segStart, segEnd);
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist == Double.MAX_VALUE ? 0 : minDist;
    }

    // ============ Interpolation | 插值 ============

    /**
     * Return the point at a given fraction along the great-circle path between two coordinates
     * 返回沿两个坐标之间大圆路径上给定比例处的点
     *
     * <p>Uses spherical interpolation (slerp) formula. Fraction 0.0 returns c1,
     * fraction 1.0 returns c2, and 0.5 returns the midpoint.</p>
     * <p>使用球面插值（slerp）公式。比例 0.0 返回 c1，1.0 返回 c2，0.5 返回中点。</p>
     *
     * @param c1 start coordinate | 起始坐标
     * @param c2 end coordinate | 结束坐标
     * @param fraction fraction along path (0.0-1.0) | 路径上的比例（0.0-1.0）
     * @return interpolated coordinate (WGS84), or null if either input is null | 插值坐标（WGS84），任一输入为 null 时返回 null
     * @throws IllegalArgumentException if fraction is outside [0.0, 1.0] | 当比例超出 [0.0, 1.0] 时抛出
     */
    public static Coordinate interpolate(Coordinate c1, Coordinate c2, double fraction) {
        if (c1 == null || c2 == null) {
            return null;
        }
        if (fraction < 0.0 || fraction > 1.0) {
            throw new IllegalArgumentException("Fraction must be between 0.0 and 1.0, got: " + fraction);
        }

        Coordinate p1 = c1.to(CoordinateSystem.WGS84);
        Coordinate p2 = c2.to(CoordinateSystem.WGS84);

        double lat1 = Math.toRadians(p1.latitude());
        double lng1 = Math.toRadians(p1.longitude());
        double lat2 = Math.toRadians(p2.latitude());
        double lng2 = Math.toRadians(p2.longitude());

        // Angular distance between the two points
        double dLat = lat2 - lat1;
        double dLng = lng2 - lng1;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        // Clamp to [0, 1] to avoid NaN from floating-point rounding near antipodal points
        a = Math.min(a, 1.0);
        double d = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // If the two points are (nearly) coincident, return the first point
        if (d < 1e-12) {
            return Coordinate.wgs84(p1.longitude(), p1.latitude());
        }

        double sinD = Math.sin(d);
        double A = Math.sin((1 - fraction) * d) / sinD;
        double B = Math.sin(fraction * d) / sinD;

        double x = A * Math.cos(lat1) * Math.cos(lng1) + B * Math.cos(lat2) * Math.cos(lng2);
        double y = A * Math.cos(lat1) * Math.sin(lng1) + B * Math.cos(lat2) * Math.sin(lng2);
        double z = A * Math.sin(lat1) + B * Math.sin(lat2);

        double lat = Math.toDegrees(Math.atan2(z, Math.sqrt(x * x + y * y)));
        double lng = Math.toDegrees(Math.atan2(y, x));

        return Coordinate.wgs84(lng, lat);
    }

    // ============ Compass Direction | 罗盘方向 ============

    /**
     * Convert a bearing (0-360 degrees) to a 16-point compass direction string
     * 将方位角（0-360度）转换为16方位罗盘方向字符串
     *
     * <p>Returns one of: N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW</p>
     * <p>返回以下之一: N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW</p>
     *
     * @param bearing bearing in degrees (0-360, 0=North, 90=East) | 方位角（0-360度，0=北，90=东）
     * @return compass direction string | 罗盘方向字符串
     */
    private static final String[] COMPASS_DIRECTIONS = {
        "N", "NNE", "NE", "ENE",
        "E", "ESE", "SE", "SSE",
        "S", "SSW", "SW", "WSW",
        "W", "WNW", "NW", "NNW"
    };

    public static String compassDirection(double bearing) {
        // Normalize bearing to [0, 360)
        double normalized = ((bearing % 360) + 360) % 360;
        int index = (int) Math.round(normalized / 22.5) % 16;
        return COMPASS_DIRECTIONS[index];
    }
}
