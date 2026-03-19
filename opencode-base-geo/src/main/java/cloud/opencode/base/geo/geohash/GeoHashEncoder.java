package cloud.opencode.base.geo.geohash;

import cloud.opencode.base.geo.exception.InvalidGeoHashException;

import java.util.ArrayList;
import java.util.List;

/**
 * GeoHash Encoder Implementation
 * GeoHash编码器实现
 *
 * <p>Implementation of GeoHash encoding and decoding using Base32.</p>
 * <p>使用Base32实现GeoHash编码和解码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base32 encoding - Base32编码</li>
 *   <li>Precision control - 精度控制</li>
 *   <li>Neighbor calculation - 相邻计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String hash = GeoHashEncoder.encode(39.9042, 116.4074, 8);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
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
public class GeoHashEncoder implements GeoHash {

    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int[] BITS = {16, 8, 4, 2, 1};

    /**
     * Singleton instance
     * 单例实例
     */
    public static final GeoHashEncoder INSTANCE = new GeoHashEncoder();

    @Override
    public String encode(double latitude, double longitude, int precision) {
        if (precision < 1 || precision > 12) {
            throw new IllegalArgumentException(
                    "GeoHash precision must be between 1 and 12, got: " + precision);
        }

        double[] latRange = {-90.0, 90.0};
        double[] lngRange = {-180.0, 180.0};

        StringBuilder hash = new StringBuilder();
        boolean isEven = true;
        int bit = 0;
        int ch = 0;

        while (hash.length() < precision) {
            double mid;
            if (isEven) {
                mid = (lngRange[0] + lngRange[1]) / 2;
                if (longitude >= mid) {
                    ch |= BITS[bit];
                    lngRange[0] = mid;
                } else {
                    lngRange[1] = mid;
                }
            } else {
                mid = (latRange[0] + latRange[1]) / 2;
                if (latitude >= mid) {
                    ch |= BITS[bit];
                    latRange[0] = mid;
                } else {
                    latRange[1] = mid;
                }
            }

            isEven = !isEven;
            if (bit < 4) {
                bit++;
            } else {
                hash.append(BASE32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }

        return hash.toString();
    }

    /**
     * Decoded latitude and longitude ranges from a GeoHash.
     * 从GeoHash解码的纬度和经度范围。
     *
     * @param latRange latitude range [min, max] | 纬度范围 [最小, 最大]
     * @param lngRange longitude range [min, max] | 经度范围 [最小, 最大]
     */
    private record DecodedRanges(double[] latRange, double[] lngRange) {}

    /**
     * Decodes a GeoHash string into latitude and longitude ranges.
     * 将GeoHash字符串解码为纬度和经度范围。
     *
     * <p>Shared logic used by both {@link #decode(String)} and {@link #getBoundingBox(String)}.</p>
     *
     * @param geoHash the GeoHash string | GeoHash字符串
     * @return decoded ranges | 解码的范围
     */
    private DecodedRanges decodeRanges(String geoHash) {
        if (geoHash == null || geoHash.isEmpty()) {
            throw new InvalidGeoHashException("GeoHash cannot be empty");
        }

        double[] latRange = {-90.0, 90.0};
        double[] lngRange = {-180.0, 180.0};
        boolean isEven = true;

        for (int i = 0; i < geoHash.length(); i++) {
            char c = geoHash.charAt(i);
            int cd = BASE32.indexOf(c);
            if (cd < 0) {
                throw new InvalidGeoHashException("Invalid GeoHash character: " + c, geoHash);
            }

            for (int mask : BITS) {
                if (isEven) {
                    double mid = (lngRange[0] + lngRange[1]) / 2;
                    if ((cd & mask) != 0) {
                        lngRange[0] = mid;
                    } else {
                        lngRange[1] = mid;
                    }
                } else {
                    double mid = (latRange[0] + latRange[1]) / 2;
                    if ((cd & mask) != 0) {
                        latRange[0] = mid;
                    } else {
                        latRange[1] = mid;
                    }
                }
                isEven = !isEven;
            }
        }

        return new DecodedRanges(latRange, lngRange);
    }

    @Override
    public double[] decode(String geoHash) {
        DecodedRanges ranges = decodeRanges(geoHash);
        double lat = (ranges.latRange()[0] + ranges.latRange()[1]) / 2;
        double lng = (ranges.lngRange()[0] + ranges.lngRange()[1]) / 2;
        return new double[]{lat, lng};
    }

    @Override
    public List<String> neighbors(String geoHash) {
        if (geoHash == null || geoHash.isEmpty()) {
            throw new InvalidGeoHashException("GeoHash cannot be empty");
        }

        // Decode ranges once and derive both center and bounding box
        DecodedRanges ranges = decodeRanges(geoHash);
        double centerLat = (ranges.latRange()[0] + ranges.latRange()[1]) / 2;
        double centerLng = (ranges.lngRange()[0] + ranges.lngRange()[1]) / 2;
        double latDiff = ranges.latRange()[1] - ranges.latRange()[0];
        double lngDiff = ranges.lngRange()[1] - ranges.lngRange()[0];

        List<String> result = new ArrayList<>(8);
        int precision = geoHash.length();

        // N, NE, E, SE, S, SW, W, NW
        double[][] offsets = {
            {latDiff, 0},           // N
            {latDiff, lngDiff},     // NE
            {0, lngDiff},           // E
            {-latDiff, lngDiff},    // SE
            {-latDiff, 0},          // S
            {-latDiff, -lngDiff},   // SW
            {0, -lngDiff},          // W
            {latDiff, -lngDiff}     // NW
        };

        for (double[] offset : offsets) {
            double newLat = centerLat + offset[0];
            double newLng = centerLng + offset[1];

            // Handle wrap-around for longitude
            if (newLng > 180) newLng -= 360;
            if (newLng < -180) newLng += 360;

            // Clamp latitude
            if (newLat > 90) newLat = 90;
            if (newLat < -90) newLat = -90;

            result.add(encode(newLat, newLng, precision));
        }

        return result;
    }

    @Override
    public double[] getBoundingBox(String geoHash) {
        DecodedRanges ranges = decodeRanges(geoHash);
        return new double[]{ranges.latRange()[0], ranges.lngRange()[0], ranges.latRange()[1], ranges.lngRange()[1]};
    }
}
