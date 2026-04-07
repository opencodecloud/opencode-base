package cloud.opencode.base.geo.polyline;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.CoordinateSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Google Encoded Polyline Algorithm Codec
 * Google编码折线算法编解码器
 *
 * <p>Implements the Google Encoded Polyline Algorithm Format for encoding
 * and decoding sequences of coordinates into compact ASCII strings.</p>
 * <p>实现Google编码折线算法格式，用于将坐标序列编码为紧凑的ASCII字符串
 * 以及从字符串解码为坐标序列。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Encode coordinate list to polyline string - 将坐标列表编码为折线字符串</li>
 *   <li>Decode polyline string to coordinate list - 将折线字符串解码为坐标列表</li>
 *   <li>Configurable precision (default 1e-5) - 可配置精度（默认1e-5）</li>
 *   <li>DoS protection with max input length - 最大输入长度的DoS防护</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Encode coordinates
 * List<Coordinate> coords = List.of(
 *     Coordinate.wgs84(-120.2, 38.5),
 *     Coordinate.wgs84(-120.95, 40.7),
 *     Coordinate.wgs84(-126.453, 43.252)
 * );
 * String encoded = PolylineCodec.encode(coords);
 *
 * // Decode polyline
 * List<Coordinate> decoded = PolylineCodec.decode(encoded);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>DoS protection: Max 1,000,000 characters for decode input - DoS防护：解码输入最大1,000,000字符</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://developers.google.com/maps/documentation/utilities/polylinealgorithm">
 *      Google Encoded Polyline Algorithm</a>
 * @since JDK 25, opencode-base-geo V1.0.3
 */
public final class PolylineCodec {

    /**
     * Mutable decode state to avoid array allocation per decodeValue call
     * 可变解码状态，避免每次 decodeValue 调用分配数组
     */
    private static final class DecodeState {
        long value;
        int index;
    }

    /**
     * Default precision factor (1e-5, standard Google polyline precision)
     * 默认精度因子（1e-5，标准Google折线精度）
     */
    private static final int DEFAULT_PRECISION = 5;

    /**
     * Maximum encoded string length for decode (DoS protection)
     * 解码最大编码字符串长度（DoS防护）
     */
    private static final int MAX_DECODE_LENGTH = 1_000_000;

    /**
     * Pre-computed precision factors (10^0 to 10^10) to avoid Math.pow() in hot path
     * 预计算精度因子（10^0 到 10^10），避免热路径中调用 Math.pow()
     */
    private static final double[] PRECISION_FACTORS = {
        1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9, 1e10
    };

    private PolylineCodec() {
    }

    /**
     * Encode coordinates to Google Encoded Polyline with default precision (1e-5)
     * 使用默认精度（1e-5）将坐标编码为Google编码折线
     *
     * @param coordinates the coordinates to encode | 要编码的坐标
     * @return encoded polyline string | 编码的折线字符串
     */
    public static String encode(List<Coordinate> coordinates) {
        return encode(coordinates, DEFAULT_PRECISION);
    }

    /**
     * Encode coordinates to Google Encoded Polyline with specified precision
     * 使用指定精度将坐标编码为Google编码折线
     *
     * <p>The precision determines the multiplier: precision 5 means multiply by 1e5.</p>
     * <p>精度决定乘数：精度5表示乘以1e5。</p>
     *
     * @param coordinates the coordinates to encode | 要编码的坐标
     * @param precision the decimal precision (e.g., 5 for 1e-5) | 小数精度（例如5表示1e-5）
     * @return encoded polyline string | 编码的折线字符串
     * @throws IllegalArgumentException if precision is not between 1 and 10 | 当精度不在1-10之间时抛出
     */
    public static String encode(List<Coordinate> coordinates, int precision) {
        if (coordinates == null || coordinates.isEmpty()) {
            return "";
        }
        if (precision < 1 || precision > 10) {
            throw new IllegalArgumentException(
                    "Precision must be between 1 and 10, got: " + precision);
        }

        double factor = PRECISION_FACTORS[precision];
        StringBuilder result = new StringBuilder();

        long prevLat = 0;
        long prevLng = 0;

        for (Coordinate coord : coordinates) {
            if (coord == null) {
                continue;
            }
            long lat = Math.round(coord.latitude() * factor);
            long lng = Math.round(coord.longitude() * factor);

            encodeValue(lat - prevLat, result);
            encodeValue(lng - prevLng, result);

            prevLat = lat;
            prevLng = lng;
        }

        return result.toString();
    }

    /**
     * Decode Google Encoded Polyline to coordinates with default precision (1e-5)
     * 使用默认精度（1e-5）将Google编码折线解码为坐标
     *
     * @param encoded the encoded polyline string | 编码的折线字符串
     * @return list of decoded coordinates (WGS84) | 解码后的坐标列表（WGS84）
     */
    public static List<Coordinate> decode(String encoded) {
        return decode(encoded, DEFAULT_PRECISION);
    }

    /**
     * Decode Google Encoded Polyline to coordinates with specified precision
     * 使用指定精度将Google编码折线解码为坐标
     *
     * @param encoded the encoded polyline string | 编码的折线字符串
     * @param precision the decimal precision (e.g., 5 for 1e-5) | 小数精度（例如5表示1e-5）
     * @return list of decoded coordinates (WGS84) | 解码后的坐标列表（WGS84）
     * @throws IllegalArgumentException if encoded string exceeds max length or precision is invalid |
     *         当编码字符串超过最大长度或精度无效时抛出
     */
    public static List<Coordinate> decode(String encoded, int precision) {
        if (encoded == null || encoded.isEmpty()) {
            return Collections.emptyList();
        }
        if (encoded.length() > MAX_DECODE_LENGTH) {
            throw new IllegalArgumentException(
                    "Encoded polyline exceeds maximum length of " + MAX_DECODE_LENGTH
                            + " characters, got: " + encoded.length());
        }
        if (precision < 1 || precision > 10) {
            throw new IllegalArgumentException(
                    "Precision must be between 1 and 10, got: " + precision);
        }

        double factor = PRECISION_FACTORS[precision];
        List<Coordinate> coordinates = new ArrayList<>();

        long lat = 0;
        long lng = 0;
        DecodeState state = new DecodeState();
        state.index = 0;
        int len = encoded.length();

        while (state.index < len) {
            // Decode latitude
            decodeValue(encoded, state);
            lat += state.value;

            if (state.index >= len) {
                throw new IllegalArgumentException(
                        "Malformed encoded polyline: unexpected end of string at index " + state.index);
            }

            // Decode longitude
            decodeValue(encoded, state);
            lng += state.value;

            double latitude = lat / factor;
            double longitude = lng / factor;

            coordinates.add(new Coordinate(longitude, latitude, CoordinateSystem.WGS84));
        }

        return coordinates;
    }

    /**
     * Encode a single value using the polyline algorithm
     * 使用折线算法编码单个值
     */
    private static void encodeValue(long value, StringBuilder result) {
        // Left-shift and invert if negative
        long encoded = value < 0 ? ~(value << 1) : (value << 1);

        // Break into 5-bit chunks
        while (encoded >= 0x20) {
            result.append((char) ((int) ((encoded & 0x1F) | 0x20) + 63));
            encoded >>= 5;
        }
        result.append((char) ((int) (encoded & 0x1F) + 63));
    }

    /**
     * Decode a single value from the encoded string starting at the given index
     * 从编码字符串的给定索引处解码单个值
     *
     * Writes decoded value and updated index into the provided state object.
     * 将解码值和更新后的索引写入提供的状态对象。
     *
     * @param encoded the encoded string | 编码字符串
     * @param state mutable state holding current index (input) and decoded value + new index (output) |
     *        可变状态，持有当前索引（输入）和解码值 + 新索引（输出）
     */
    private static void decodeValue(String encoded, DecodeState state) {
        long result = 0;
        int shift = 0;
        int len = encoded.length();
        int index = state.index;
        int b;

        do {
            if (index >= len) {
                throw new IllegalArgumentException(
                        "Malformed encoded polyline: unexpected end at index " + index);
            }
            int ch = encoded.charAt(index++);
            if (ch < 63 || ch > 126) {
                throw new IllegalArgumentException(
                        "Malformed encoded polyline: invalid character '" + (char) ch
                        + "' (code " + ch + ") at index " + (index - 1));
            }
            b = ch - 63;
            result |= (long) (b & 0x1F) << shift;
            shift += 5;
            if (shift > 63) {
                throw new IllegalArgumentException(
                        "Malformed encoded polyline: value overflow at index " + index);
            }
        } while (b >= 0x20);

        // If the least significant bit is set, the value is negative
        state.value = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
        state.index = index;
    }
}
