package cloud.opencode.base.geo.geohash;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * GeoHash Proximity Search Utility - Solves boundary edge cases
 * GeoHash邻近搜索工具 - 解决边界边缘问题
 *
 * <p>Provides methods to find all GeoHash cells covering a circular search area
 * around a center point. This solves the most common GeoHash pain point: objects
 * near cell boundaries being missed in single-cell queries.</p>
 * <p>提供查找覆盖中心点周围圆形搜索区域的所有GeoHash单元格的方法。
 * 这解决了最常见的GeoHash痛点：在单单元格查询中遗漏靠近单元格边界的对象。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Auto-select precision based on search radius - 根据搜索半径自动选择精度</li>
 *   <li>Enumerate all GeoHash cells covering a circular area - 枚举覆盖圆形区域的所有GeoHash单元格</li>
 *   <li>Handles boundary edge cases correctly - 正确处理边界边缘情况</li>
 *   <li>Supports latitude/longitude wrapping - 支持经纬度环绕</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Auto-select precision, search within 500m of a point
 * Set<String> hashes = GeoHashSearch.searchHashes(39.9042, 116.4074, 500);
 *
 * // Use specific precision
 * Set<String> hashes2 = GeoHashSearch.searchHashes(39.9042, 116.4074, 500, 7);
 *
 * // Use GeoHashPrecision enum
 * Set<String> hashes3 = GeoHashSearch.searchHashes(
 *     39.9042, 116.4074, 500, GeoHashPrecision.STREET);
 * }</pre>
 *
 * <p><strong>Algorithm | 算法:</strong></p>
 * <ol>
 *   <li>Compute a bounding box from center point +/- radius (meters to degrees) - 从中心点 +/- 半径计算边界框</li>
 *   <li>Step through the bounding box by cell size to enumerate all GeoHash cells - 按单元格大小遍历边界框枚举所有GeoHash单元格</li>
 *   <li>Return the unique set of GeoHash strings - 返回唯一的GeoHash字符串集合</li>
 * </ol>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see GeoHashUtil
 * @see GeoHashPrecision
 * @since JDK 25, opencode-base-geo V1.0.3
 */
public final class GeoHashSearch {

    /**
     * Earth's mean radius in meters
     * 地球平均半径（米）
     */
    private static final double EARTH_RADIUS_M = 6_371_000.0;

    /**
     * Maximum number of cells to prevent DoS from extremely large searches
     * 最大单元格数量，防止超大搜索导致拒绝服务
     */
    private static final int MAX_CELLS = 10_000;

    private GeoHashSearch() {
    }

    /**
     * Search GeoHash cells covering a circular area with auto-selected precision
     * 使用自动选择精度搜索覆盖圆形区域的GeoHash单元格
     *
     * <p>Automatically selects the best GeoHash precision for the given radius.</p>
     * <p>根据给定半径自动选择最佳GeoHash精度。</p>
     *
     * @param lat center latitude in degrees | 中心纬度（度）
     * @param lng center longitude in degrees | 中心经度（度）
     * @param radiusMeters search radius in meters | 搜索半径（米）
     * @return set of GeoHash strings covering the search area | 覆盖搜索区域的GeoHash字符串集合
     * @throws IllegalArgumentException if coordinates are invalid or radius is not positive |
     *         当坐标无效或半径不为正数时抛出
     */
    public static Set<String> searchHashes(double lat, double lng, double radiusMeters) {
        validateInputs(lat, lng, radiusMeters);
        double radiusKm = radiusMeters / 1000.0;
        GeoHashPrecision precision = GeoHashPrecision.forRadius(radiusKm);
        return doSearch(lat, lng, radiusMeters, precision.getValue());
    }

    /**
     * Search GeoHash cells covering a circular area with specified integer precision
     * 使用指定整数精度搜索覆盖圆形区域的GeoHash单元格
     *
     * @param lat center latitude in degrees | 中心纬度（度）
     * @param lng center longitude in degrees | 中心经度（度）
     * @param radiusMeters search radius in meters | 搜索半径（米）
     * @param precision GeoHash precision (1-12) | GeoHash精度 (1-12)
     * @return set of GeoHash strings covering the search area | 覆盖搜索区域的GeoHash字符串集合
     * @throws IllegalArgumentException if coordinates are invalid, radius is not positive,
     *         or precision is out of range | 当坐标无效、半径不为正数或精度超出范围时抛出
     */
    public static Set<String> searchHashes(double lat, double lng,
                                           double radiusMeters, int precision) {
        validateInputs(lat, lng, radiusMeters);
        if (precision < 1 || precision > 12) {
            throw new IllegalArgumentException(
                    "GeoHash precision must be between 1 and 12, got: " + precision);
        }
        return doSearch(lat, lng, radiusMeters, precision);
    }

    /**
     * Search GeoHash cells covering a circular area with GeoHashPrecision enum
     * 使用GeoHashPrecision枚举搜索覆盖圆形区域的GeoHash单元格
     *
     * @param lat center latitude in degrees | 中心纬度（度）
     * @param lng center longitude in degrees | 中心经度（度）
     * @param radiusMeters search radius in meters | 搜索半径（米）
     * @param precision the GeoHashPrecision level | GeoHashPrecision精度级别
     * @return set of GeoHash strings covering the search area | 覆盖搜索区域的GeoHash字符串集合
     * @throws IllegalArgumentException if coordinates are invalid or radius is not positive |
     *         当坐标无效或半径不为正数时抛出
     * @throws NullPointerException if precision is null | 当precision为null时抛出
     */
    public static Set<String> searchHashes(double lat, double lng,
                                           double radiusMeters,
                                           GeoHashPrecision precision) {
        validateInputs(lat, lng, radiusMeters);
        if (precision == null) {
            throw new NullPointerException("GeoHashPrecision must not be null");
        }
        return doSearch(lat, lng, radiusMeters, precision.getValue());
    }

    /**
     * Core search implementation: enumerate GeoHash cells within bounding box
     * 核心搜索实现：枚举边界框内的GeoHash单元格
     */
    private static Set<String> doSearch(double lat, double lng,
                                        double radiusMeters, int precision) {
        // Compute bounding box in degrees
        double latDelta = metersToLatDegrees(radiusMeters);
        double lngDelta = metersToLngDegrees(radiusMeters, lat);

        double minLat = Math.max(-90.0, lat - latDelta);
        double maxLat = Math.min(90.0, lat + latDelta);
        double minLng = lng - lngDelta;
        double maxLng = lng + lngDelta;

        // Encode center to get cell dimensions
        String centerHash = GeoHashUtil.encode(lat, lng, precision);
        double[] bbox = GeoHashUtil.getBoundingBox(centerHash);
        // bbox = [minLat, minLng, maxLat, maxLng]
        double cellLatSize = bbox[2] - bbox[0];
        double cellLngSize = bbox[3] - bbox[1];

        // Guard against zero or near-zero cell size
        if (cellLatSize <= 0) cellLatSize = 1e-10;
        if (cellLngSize <= 0) cellLngSize = 1e-10;

        // Use half-cell steps to ensure we don't skip cells
        double latStep = cellLatSize * 0.5;
        double lngStep = cellLngSize * 0.5;

        // Estimate number of cells to prevent DoS
        double latRange = maxLat - minLat;
        double lngRange = maxLng - minLng;
        long latSteps = (long) Math.ceil(latRange / latStep + 1);
        long lngSteps = (long) Math.ceil(lngRange / lngStep + 1);
        // Guard against overflow: if either dimension is huge, reject early
        long estimatedSteps;
        if (latSteps > MAX_CELLS * 4L || lngSteps > MAX_CELLS * 4L) {
            estimatedSteps = Long.MAX_VALUE;
        } else {
            estimatedSteps = latSteps * lngSteps;
        }
        if (estimatedSteps > MAX_CELLS * 4L) {
            throw new IllegalArgumentException(
                    "Search area too large for precision " + precision
                            + ": would generate too many cells. "
                            + "Use a coarser precision or smaller radius.");
        }

        int estimatedCapacity = (int) Math.min(estimatedSteps, MAX_CELLS);
        Set<String> result = new LinkedHashSet<>(Math.max(16, estimatedCapacity * 4 / 3 + 1));

        for (double sampleLat = minLat; sampleLat <= maxLat; sampleLat += latStep) {
            double clampedLat = Math.min(Math.max(sampleLat, -90.0), 90.0);
            for (double sampleLng = minLng; sampleLng <= maxLng; sampleLng += lngStep) {
                double normalizedLng = normalizeLng(sampleLng);
                String hash = GeoHashUtil.encode(clampedLat, normalizedLng, precision);
                result.add(hash);
                if (result.size() > MAX_CELLS) {
                    throw new IllegalArgumentException(
                            "Search area too large for precision " + precision
                                    + ": exceeded " + MAX_CELLS + " cells. "
                                    + "Use a coarser precision or smaller radius.");
                }
            }
        }

        return result;
    }

    /**
     * Validate input parameters
     * 验证输入参数
     */
    private static void validateInputs(double lat, double lng, double radiusMeters) {
        if (Double.isNaN(lat) || Double.isInfinite(lat) || lat < -90 || lat > 90) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90, got: " + lat);
        }
        if (Double.isNaN(lng) || Double.isInfinite(lng) || lng < -180 || lng > 180) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180, got: " + lng);
        }
        if (Double.isNaN(radiusMeters) || Double.isInfinite(radiusMeters)
                || radiusMeters <= 0) {
            throw new IllegalArgumentException(
                    "Radius must be a positive finite number in meters, got: " + radiusMeters);
        }
    }

    /**
     * Convert meters to latitude degrees
     * 将米转换为纬度度数
     */
    private static double metersToLatDegrees(double meters) {
        return Math.toDegrees(meters / EARTH_RADIUS_M);
    }

    /**
     * Convert meters to longitude degrees at given latitude
     * 在给定纬度下将米转换为经度度数
     */
    private static double metersToLngDegrees(double meters, double latitude) {
        double latRad = Math.toRadians(latitude);
        double cosLat = Math.cos(latRad);
        if (cosLat < 1e-10) {
            // Near poles, longitude converges — return 360 to cover all longitudes
            return 360.0;
        }
        return Math.toDegrees(meters / (EARTH_RADIUS_M * cosLat));
    }

    /**
     * Normalize longitude to [-180, 180] range
     * 将经度归一化到[-180, 180]范围
     */
    private static double normalizeLng(double lng) {
        if (lng >= -180 && lng <= 180) {
            return lng;
        }
        double normalized = ((lng + 180) % 360 + 360) % 360 - 180;
        // Handle exact -180/180 boundary
        if (normalized < -180) normalized = -180;
        return normalized;
    }
}
