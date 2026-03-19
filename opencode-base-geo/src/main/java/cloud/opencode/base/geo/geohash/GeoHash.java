package cloud.opencode.base.geo.geohash;

import java.util.List;

/**
 * GeoHash Interface
 * GeoHash接口
 *
 * <p>Interface for GeoHash encoding and decoding operations.</p>
 * <p>GeoHash编码和解码操作的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Encode coordinates to GeoHash - 将坐标编码为GeoHash</li>
 *   <li>Decode GeoHash to coordinates - 将GeoHash解码为坐标</li>
 *   <li>Get neighbors - 获取相邻格子</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * GeoHash hash = GeoHash.encode(39.9042, 116.4074, 8);
 * String hashStr = hash.toBase32();
 * List<GeoHash> neighbors = hash.getNeighbors();
 * }</pre>
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
public interface GeoHash {

    /**
     * Encode latitude and longitude to GeoHash
     * 将经纬度编码为GeoHash
     *
     * @param latitude the latitude | 纬度
     * @param longitude the longitude | 经度
     * @param precision the precision (1-12) | 精度（1-12）
     * @return GeoHash string | GeoHash字符串
     */
    String encode(double latitude, double longitude, int precision);

    /**
     * Decode GeoHash to coordinates
     * 将GeoHash解码为坐标
     *
     * @param geoHash the GeoHash string | GeoHash字符串
     * @return array of [latitude, longitude] | [纬度, 经度]数组
     */
    double[] decode(String geoHash);

    /**
     * Get all 8 neighbors of a GeoHash
     * 获取GeoHash的所有8个相邻格子
     *
     * @param geoHash the GeoHash string | GeoHash字符串
     * @return list of 8 neighbor GeoHashes | 8个相邻GeoHash的列表
     */
    List<String> neighbors(String geoHash);

    /**
     * Get bounding box of a GeoHash
     * 获取GeoHash的边界框
     *
     * @param geoHash the GeoHash string | GeoHash字符串
     * @return array of [minLat, minLng, maxLat, maxLng] | [最小纬度, 最小经度, 最大纬度, 最大经度]数组
     */
    double[] getBoundingBox(String geoHash);
}
