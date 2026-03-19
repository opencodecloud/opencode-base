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
        double deltaLng = meters / (METERS_PER_DEGREE * Math.cos(Math.toRadians(avgLat)));

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
}
