package cloud.opencode.base.geo;

import cloud.opencode.base.geo.geohash.GeoHashUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Bounding Box - Axis-aligned geographic bounding box
 * 边界框 - 轴对齐地理边界框
 *
 * <p>An immutable record representing a geographic bounding box defined by minimum and maximum
 * longitude/latitude coordinates. Supports date line crossing where minLng &gt; maxLng.</p>
 * <p>表示由最小和最大经纬度坐标定义的地理边界框的不可变记录。支持日期变更线穿越（minLng &gt; maxLng）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Point and box containment checks - 点和框包含检查</li>
 *   <li>Intersection and union operations - 相交和合并操作</li>
 *   <li>Expansion by distance (meters) - 按距离（米）扩展</li>
 *   <li>GeoHash coverage computation - GeoHash 覆盖计算</li>
 *   <li>Date line crossing support - 支持日期变更线穿越</li>
 *   <li>Factory methods from center+radius and coordinate collections - 从中心+半径及坐标集合创建的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from explicit bounds
 * BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
 *
 * // Create from center and radius
 * Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
 * BoundingBox box = BoundingBox.fromCenter(center, 5000);
 *
 * // Check containment
 * boolean inside = box.contains(Coordinate.wgs84(116.5, 39.5));
 *
 * // Get GeoHash coverage
 * Set<String> hashes = box.toGeoHashes(6);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (coordinates must not be null) - 空值安全: 否（坐标不能为null）</li>
 * </ul>
 *
 * @param minLng the minimum longitude (western boundary) | 最小经度（西边界）
 * @param minLat the minimum latitude (southern boundary) | 最小纬度（南边界）
 * @param maxLng the maximum longitude (eastern boundary) | 最大经度（东边界）
 * @param maxLat the maximum latitude (northern boundary) | 最大纬度（北边界）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.3
 */
public record BoundingBox(double minLng, double minLat, double maxLng, double maxLat) {

    /**
     * Earth radius in meters (WGS84 mean)
     * 地球半径（米，WGS84 平均值）
     */
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    /**
     * Compact constructor - validates that no coordinate is NaN or Infinity
     * 紧凑构造函数 - 验证坐标不为 NaN 或 Infinity
     *
     * @throws IllegalArgumentException if any coordinate is NaN or Infinity | 如果任何坐标为 NaN 或 Infinity
     */
    public BoundingBox {
        if (Double.isNaN(minLng) || Double.isInfinite(minLng)) {
            throw new IllegalArgumentException("minLng must not be NaN or Infinity | minLng 不能为 NaN 或 Infinity");
        }
        if (Double.isNaN(minLat) || Double.isInfinite(minLat)) {
            throw new IllegalArgumentException("minLat must not be NaN or Infinity | minLat 不能为 NaN 或 Infinity");
        }
        if (Double.isNaN(maxLng) || Double.isInfinite(maxLng)) {
            throw new IllegalArgumentException("maxLng must not be NaN or Infinity | maxLng 不能为 NaN 或 Infinity");
        }
        if (Double.isNaN(maxLat) || Double.isInfinite(maxLat)) {
            throw new IllegalArgumentException("maxLat must not be NaN or Infinity | maxLat 不能为 NaN 或 Infinity");
        }
        if (minLat > maxLat) {
            throw new IllegalArgumentException(
                "minLat (" + minLat + ") must not be greater than maxLat (" + maxLat + ") | minLat 不能大于 maxLat");
        }
    }

    /**
     * Create a bounding box from explicit bounds
     * 使用显式边界创建边界框
     *
     * @param minLng the minimum longitude | 最小经度
     * @param minLat the minimum latitude | 最小纬度
     * @param maxLng the maximum longitude | 最大经度
     * @param maxLat the maximum latitude | 最大纬度
     * @return the bounding box | 边界框
     * @throws IllegalArgumentException if any coordinate is NaN/Infinity or minLat &gt; maxLat | 如果坐标为 NaN/Infinity 或 minLat &gt; maxLat
     */
    public static BoundingBox of(double minLng, double minLat, double maxLng, double maxLat) {
        return new BoundingBox(minLng, minLat, maxLng, maxLat);
    }

    /**
     * Create a bounding box from a center coordinate and radius in meters
     * 使用中心坐标和半径（米）创建边界框
     *
     * <p>Calculates the bounding box by offsetting the center coordinate by the given radius
     * in all four cardinal directions using WGS84 spherical approximation.</p>
     * <p>通过在四个基本方向上以给定半径偏移中心坐标来计算边界框（WGS84 球面近似）。</p>
     *
     * @param center the center coordinate (WGS84) | 中心坐标（WGS84）
     * @param radiusMeters the radius in meters | 半径（米）
     * @return the bounding box | 边界框
     * @throws NullPointerException if center is null | 如果 center 为 null
     * @throws IllegalArgumentException if radiusMeters is negative or NaN | 如果 radiusMeters 为负数或 NaN
     */
    public static BoundingBox fromCenter(Coordinate center, double radiusMeters) {
        Objects.requireNonNull(center, "center must not be null | center 不能为 null");
        if (radiusMeters < 0 || Double.isNaN(radiusMeters) || Double.isInfinite(radiusMeters)) {
            throw new IllegalArgumentException(
                "radiusMeters must be non-negative and finite | radiusMeters 必须为非负有限值: " + radiusMeters);
        }

        double lat = center.latitude();
        double lng = center.longitude();

        // Latitude offset: 1 degree latitude ~ 111,320 meters
        double latOffset = Math.toDegrees(radiusMeters / EARTH_RADIUS_METERS);

        // Longitude offset: depends on latitude
        double cosLat = Math.cos(Math.toRadians(lat));
        double lngOffset;
        if (cosLat < 1e-10) {
            // Near the poles, span full longitude
            lngOffset = 180.0;
        } else {
            lngOffset = Math.toDegrees(radiusMeters / (EARTH_RADIUS_METERS * cosLat));
        }

        double minLat = Math.max(-90.0, lat - latOffset);
        double maxLat = Math.min(90.0, lat + latOffset);
        double minLng = lng - lngOffset;
        double maxLng = lng + lngOffset;

        // Normalize longitudes to [-180, 180]
        minLng = normalizeLng(minLng);
        maxLng = normalizeLng(maxLng);

        return new BoundingBox(minLng, minLat, maxLng, maxLat);
    }

    /**
     * Compute the minimum bounding box that contains all given coordinates
     * 计算包含所有给定坐标的最小边界框
     *
     * @param coordinates the coordinates to enclose | 要包含的坐标
     * @return the bounding box | 边界框
     * @throws NullPointerException if coordinates is null | 如果 coordinates 为 null
     * @throws IllegalArgumentException if coordinates is empty | 如果 coordinates 为空
     */
    public static BoundingBox fromCoordinates(Collection<Coordinate> coordinates) {
        Objects.requireNonNull(coordinates, "coordinates must not be null | coordinates 不能为 null");
        if (coordinates.isEmpty()) {
            throw new IllegalArgumentException("coordinates must not be empty | coordinates 不能为空");
        }

        double minLng = Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;

        for (Coordinate c : coordinates) {
            Objects.requireNonNull(c, "coordinate element must not be null | 坐标元素不能为 null");
            if (c.longitude() < minLng) minLng = c.longitude();
            if (c.longitude() > maxLng) maxLng = c.longitude();
            if (c.latitude() < minLat) minLat = c.latitude();
            if (c.latitude() > maxLat) maxLat = c.latitude();
        }

        return new BoundingBox(minLng, minLat, maxLng, maxLat);
    }

    /**
     * Check if this bounding box contains the given coordinate
     * 检查此边界框是否包含给定坐标
     *
     * <p>Handles date line crossing where minLng &gt; maxLng.</p>
     * <p>处理日期变更线穿越（minLng &gt; maxLng）的情况。</p>
     *
     * @param c the coordinate to check | 要检查的坐标
     * @return true if the coordinate is inside | 如果坐标在内部返回 true
     * @throws NullPointerException if c is null | 如果 c 为 null
     */
    public boolean contains(Coordinate c) {
        Objects.requireNonNull(c, "coordinate must not be null | coordinate 不能为 null");

        double lat = c.latitude();
        double lng = c.longitude();

        if (lat < minLat || lat > maxLat) {
            return false;
        }

        if (crossesDateLine()) {
            // Wraps around: point is inside if lng >= minLng OR lng <= maxLng
            return lng >= minLng || lng <= maxLng;
        } else {
            return lng >= minLng && lng <= maxLng;
        }
    }

    /**
     * Check if this bounding box fully contains another bounding box
     * 检查此边界框是否完全包含另一个边界框
     *
     * @param other the other bounding box | 另一个边界框
     * @return true if other is fully contained | 如果另一个边界框完全在内部返回 true
     * @throws NullPointerException if other is null | 如果 other 为 null
     */
    public boolean contains(BoundingBox other) {
        Objects.requireNonNull(other, "other must not be null | other 不能为 null");

        if (other.minLat < this.minLat || other.maxLat > this.maxLat) {
            return false;
        }

        if (crossesDateLine()) {
            if (other.crossesDateLine()) {
                return other.minLng >= this.minLng && other.maxLng <= this.maxLng;
            } else {
                return other.minLng >= this.minLng || other.maxLng <= this.maxLng;
            }
        } else {
            if (other.crossesDateLine()) {
                return false; // A non-crossing box cannot contain a crossing box
            }
            return other.minLng >= this.minLng && other.maxLng <= this.maxLng;
        }
    }

    /**
     * Check if this bounding box intersects with another
     * 检查此边界框是否与另一个相交
     *
     * @param other the other bounding box | 另一个边界框
     * @return true if the boxes overlap | 如果边界框重叠返回 true
     * @throws NullPointerException if other is null | 如果 other 为 null
     */
    public boolean intersects(BoundingBox other) {
        Objects.requireNonNull(other, "other must not be null | other 不能为 null");

        // Check latitude overlap first
        if (this.maxLat < other.minLat || this.minLat > other.maxLat) {
            return false;
        }

        // Check longitude overlap considering date line
        boolean thisWraps = crossesDateLine();
        boolean otherWraps = other.crossesDateLine();

        if (!thisWraps && !otherWraps) {
            return this.maxLng >= other.minLng && this.minLng <= other.maxLng;
        }
        if (thisWraps && otherWraps) {
            return true; // Two wrapping boxes always overlap in longitude
        }
        // One wraps, one doesn't
        BoundingBox wrapping = thisWraps ? this : other;
        BoundingBox normal = thisWraps ? other : this;
        return normal.maxLng >= wrapping.minLng || normal.minLng <= wrapping.maxLng;
    }

    /**
     * Compute the union of this bounding box with another
     * 计算此边界框与另一个边界框的合并
     *
     * @param other the other bounding box | 另一个边界框
     * @return the union bounding box | 合并后的边界框
     * @throws NullPointerException if other is null | 如果 other 为 null
     */
    public BoundingBox union(BoundingBox other) {
        Objects.requireNonNull(other, "other must not be null | other 不能为 null");

        double uMinLat = Math.min(this.minLat, other.minLat);
        double uMaxLat = Math.max(this.maxLat, other.maxLat);
        double uMinLng = Math.min(this.minLng, other.minLng);
        double uMaxLng = Math.max(this.maxLng, other.maxLng);

        return new BoundingBox(uMinLng, uMinLat, uMaxLng, uMaxLat);
    }

    /**
     * Expand this bounding box by the given distance in meters in all directions
     * 在所有方向上按给定距离（米）扩展此边界框
     *
     * @param meters the distance to expand in meters | 扩展距离（米）
     * @return the expanded bounding box | 扩展后的边界框
     * @throws IllegalArgumentException if meters is negative or NaN | 如果 meters 为负数或 NaN
     */
    public BoundingBox expand(double meters) {
        if (meters < 0 || Double.isNaN(meters) || Double.isInfinite(meters)) {
            throw new IllegalArgumentException(
                "meters must be non-negative and finite | meters 必须为非负有限值: " + meters);
        }

        double latOffset = Math.toDegrees(meters / EARTH_RADIUS_METERS);
        double centerLat = (minLat + maxLat) / 2.0;
        double cosLat = Math.cos(Math.toRadians(centerLat));
        double lngOffset;
        if (cosLat < 1e-10) {
            lngOffset = 180.0;
        } else {
            lngOffset = Math.toDegrees(meters / (EARTH_RADIUS_METERS * cosLat));
        }

        double newMinLat = Math.max(-90.0, minLat - latOffset);
        double newMaxLat = Math.min(90.0, maxLat + latOffset);
        double newMinLng = normalizeLng(minLng - lngOffset);
        double newMaxLng = normalizeLng(maxLng + lngOffset);

        return new BoundingBox(newMinLng, newMinLat, newMaxLng, newMaxLat);
    }

    /**
     * Get the center coordinate of this bounding box (WGS84)
     * 获取此边界框的中心坐标（WGS84）
     *
     * @return the center coordinate | 中心坐标
     */
    public Coordinate center() {
        double centerLat = (minLat + maxLat) / 2.0;
        double centerLng;
        if (crossesDateLine()) {
            // Crossing date line: average with wrap-around
            double span = (360.0 - minLng + maxLng);
            centerLng = minLng + span / 2.0;
            centerLng = normalizeLng(centerLng);
        } else {
            centerLng = (minLng + maxLng) / 2.0;
        }
        return Coordinate.wgs84(centerLng, centerLat);
    }

    /**
     * Get the width of this bounding box in degrees of longitude
     * 获取此边界框的宽度（经度度数）
     *
     * @return the width in degrees | 宽度（度）
     */
    public double width() {
        if (crossesDateLine()) {
            return 360.0 - minLng + maxLng;
        }
        return maxLng - minLng;
    }

    /**
     * Get the height of this bounding box in degrees of latitude
     * 获取此边界框的高度（纬度度数）
     *
     * @return the height in degrees | 高度（度）
     */
    public double height() {
        return maxLat - minLat;
    }

    /**
     * Check if this bounding box crosses the International Date Line
     * 检查此边界框是否穿越国际日期变更线
     *
     * @return true if minLng &gt; maxLng (crosses date line) | 如果 minLng &gt; maxLng（穿越日期变更线）返回 true
     */
    public boolean crossesDateLine() {
        return minLng > maxLng;
    }

    /**
     * Compute a set of GeoHash strings that cover this bounding box at the given precision
     * 计算在给定精度下覆盖此边界框的 GeoHash 字符串集合
     *
     * <p>Iterates over the bounding box area using GeoHash grid cells of the given precision
     * and returns all GeoHash strings that overlap with this box.</p>
     * <p>使用给定精度的 GeoHash 网格单元遍历边界框区域，返回与此框重叠的所有 GeoHash 字符串。</p>
     *
     * @param precision the GeoHash precision (1-12) | GeoHash 精度（1-12）
     * @return set of GeoHash strings covering this box | 覆盖此框的 GeoHash 字符串集合
     * @throws IllegalArgumentException if precision is not between 1 and 12 | 如果精度不在 1-12 之间
     */
    public Set<String> toGeoHashes(int precision) {
        if (precision < 1 || precision > 12) {
            throw new IllegalArgumentException(
                "Precision must be between 1 and 12 | 精度必须在 1-12 之间: " + precision);
        }

        int maxHashes = 10_000;
        Set<String> hashes = new HashSet<>();

        // Approximate step sizes for the given precision in degrees
        // Latitude bits: precision * 5 / 2 (rounded), longitude bits: precision * 5 - lat bits
        int totalBits = precision * 5;
        int latBits = totalBits / 2;
        int lngBits = totalBits - latBits;
        double latStep = 180.0 / (1 << latBits);
        double lngStep = 360.0 / (1 << lngBits);

        // Use half-step for iteration to ensure no gaps
        double halfLatStep = latStep / 2.0;
        double halfLngStep = lngStep / 2.0;

        if (crossesDateLine()) {
            // Two longitude ranges: [minLng, 180] and [-180, maxLng]
            addHashes(hashes, minLng, 180.0, minLat, maxLat, halfLatStep, halfLngStep, precision, maxHashes);
            addHashes(hashes, -180.0, maxLng, minLat, maxLat, halfLatStep, halfLngStep, precision, maxHashes);
        } else {
            addHashes(hashes, minLng, maxLng, minLat, maxLat, halfLatStep, halfLngStep, precision, maxHashes);
        }

        return hashes;
    }

    /**
     * Add GeoHash strings for the given longitude/latitude range
     * 为给定的经纬度范围添加 GeoHash 字符串
     */
    private static void addHashes(Set<String> hashes, double lngStart, double lngEnd,
                                  double latStart, double latEnd,
                                  double latStep, double lngStep, int precision,
                                  int maxHashes) {
        for (double lat = latStart; lat <= latEnd; lat += latStep) {
            for (double lng = lngStart; lng <= lngEnd; lng += lngStep) {
                double clampedLat = Math.max(-90.0, Math.min(90.0, lat));
                double clampedLng = Math.max(-180.0, Math.min(180.0, lng));
                hashes.add(GeoHashUtil.encode(clampedLat, clampedLng, precision));
                if (hashes.size() > maxHashes) {
                    throw new IllegalArgumentException(
                        "GeoHash coverage exceeds maximum of " + maxHashes
                        + " cells. Use a coarser precision. | GeoHash 覆盖超过最大 " + maxHashes + " 个格子，请使用更粗的精度");
                }
            }
        }
    }

    /**
     * Normalize longitude to the range [-180, 180]
     * 将经度归一化到 [-180, 180] 范围
     */
    private static double normalizeLng(double lng) {
        // Normalize to [-180, 180]; -180 and 180 are equivalent, keep -180 for consistency
        return ((lng + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }
}
