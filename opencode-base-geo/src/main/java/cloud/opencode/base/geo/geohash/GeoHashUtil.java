package cloud.opencode.base.geo.geohash;

import java.util.List;
import java.util.regex.Pattern;

/**
 * GeoHash Utility Class
 * GeoHash工具类
 *
 * <p>Static utility methods for GeoHash operations.</p>
 * <p>GeoHash操作的静态工具方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Encode coordinates to GeoHash - 将坐标编码为GeoHash</li>
 *   <li>Decode GeoHash to coordinates - 将GeoHash解码为坐标</li>
 *   <li>Get neighbors - 获取相邻格子</li>
 *   <li>Precision utilities - 精度工具</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String hash = GeoHashUtil.encode(39.9042, 116.4074, 8);
 * double[] coords = GeoHashUtil.decode(hash);
 * List<String> neighbors = GeoHashUtil.neighbors(hash);
 * }</pre>
 *
 * <p><strong>Precision Reference | 精度参考:</strong></p>
 * <pre>
 * Precision | Grid Size
 * 1         | ~5000km
 * 2         | ~1250km
 * 3         | ~156km
 * 4         | ~39km
 * 5         | ~5km
 * 6         | ~1.2km
 * 7         | ~153m
 * 8         | ~38m
 * 9         | ~5m
 * </pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public final class GeoHashUtil {

    private static final GeoHashEncoder ENCODER = GeoHashEncoder.INSTANCE;
    private static final Pattern GEOHASH_PATTERN = Pattern.compile("^[0-9b-hjkmnp-z]+$");

    private GeoHashUtil() {
    }

    /**
     * Encode latitude and longitude to GeoHash
     * 将经纬度编码为GeoHash
     *
     * @param latitude the latitude | 纬度
     * @param longitude the longitude | 经度
     * @param precision the precision (1-12) | 精度（1-12）
     * @return GeoHash string | GeoHash字符串
     */
    public static String encode(double latitude, double longitude, int precision) {
        return ENCODER.encode(latitude, longitude, precision);
    }

    /**
     * Encode latitude and longitude to GeoHash with default precision (8)
     * 使用默认精度（8）将经纬度编码为GeoHash
     *
     * @param latitude the latitude | 纬度
     * @param longitude the longitude | 经度
     * @return GeoHash string | GeoHash字符串
     */
    public static String encode(double latitude, double longitude) {
        return encode(latitude, longitude, 8);
    }

    /**
     * Decode GeoHash to coordinates
     * 将GeoHash解码为坐标
     *
     * @param geoHash the GeoHash string | GeoHash字符串
     * @return array of [latitude, longitude] | [纬度, 经度]数组
     */
    public static double[] decode(String geoHash) {
        return ENCODER.decode(geoHash);
    }

    /**
     * Get all 8 neighbors of a GeoHash
     * 获取GeoHash的所有8个相邻格子
     *
     * @param geoHash the GeoHash string | GeoHash字符串
     * @return list of 8 neighbor GeoHashes | 8个相邻GeoHash的列表
     */
    public static List<String> neighbors(String geoHash) {
        return ENCODER.neighbors(geoHash);
    }

    /**
     * Get bounding box of a GeoHash
     * 获取GeoHash的边界框
     *
     * @param geoHash the GeoHash string | GeoHash字符串
     * @return array of [minLat, minLng, maxLat, maxLng] | [最小纬度, 最小经度, 最大纬度, 最大经度]数组
     */
    public static double[] getBoundingBox(String geoHash) {
        return ENCODER.getBoundingBox(geoHash);
    }

    /**
     * Get recommended precision for search radius
     * 获取搜索半径的推荐精度
     *
     * @param radiusKm the search radius in kilometers | 搜索半径（公里）
     * @return recommended precision | 推荐精度
     */
    public static int getPrecisionForRadius(double radiusKm) {
        if (radiusKm >= 100) return 3;
        if (radiusKm >= 20) return 4;
        if (radiusKm >= 5) return 5;
        if (radiusKm >= 1) return 6;
        if (radiusKm >= 0.1) return 7;
        return 8;
    }

    /**
     * Check if a GeoHash is valid
     * 检查GeoHash是否有效
     *
     * @param geoHash the GeoHash string | GeoHash字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String geoHash) {
        if (geoHash == null || geoHash.isEmpty() || geoHash.length() > 12) {
            return false;
        }
        return GEOHASH_PATTERN.matcher(geoHash).matches();
    }
}
