package cloud.opencode.base.geo.wkt;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.exception.GeoException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Lightweight WKT (Well-Known Text) Codec for Basic Geometry Types
 * 轻量级 WKT（Well-Known Text）基础几何类型编解码器
 *
 * <p>Provides parsing and serialization of basic WKT geometry types (POINT, LINESTRING, POLYGON)
 * without requiring heavy dependencies like JTS. Ideal for simple WKT interop with PostGIS.</p>
 * <p>提供基本 WKT 几何类型（POINT、LINESTRING、POLYGON）的解析和序列化，
 * 无需 JTS 等重量级依赖。适合与 PostGIS 进行简单的 WKT 交互。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse POINT, LINESTRING, POLYGON from WKT strings | 从 WKT 字符串解析 POINT、LINESTRING、POLYGON</li>
 *   <li>Serialize coordinates to WKT format | 将坐标序列化为 WKT 格式</li>
 *   <li>Input length and coordinate count security limits | 输入长度和坐标数量安全限制</li>
 *   <li>Case-insensitive type name parsing | 类型名称大小写不敏感解析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse POINT
 * Coordinate point = WktCodec.parsePoint("POINT(116.4074 39.9042)");
 *
 * // Parse LINESTRING
 * List<Coordinate> line = WktCodec.parseLineString("LINESTRING(0 0, 1 1, 2 2)");
 *
 * // Serialize to WKT
 * String wkt = WktCodec.toWkt(Coordinate.wgs84(116.4074, 39.9042));
 * // returns "POINT(116.4074 39.9042)"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless static methods) - 线程安全: 是（无状态静态方法）</li>
 *   <li>Max input length: 1,000,000 characters - 最大输入长度: 1,000,000 字符</li>
 *   <li>Max coordinate count: 100,000 points - 最大坐标数: 100,000 个点</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.3
 */
public final class WktCodec {

    /**
     * Maximum allowed input string length
     * 最大允许输入字符串长度
     */
    private static final int MAX_INPUT_LENGTH = 1_000_000;

    /**
     * Maximum allowed coordinate count
     * 最大允许坐标数
     */
    private static final int MAX_COORDINATE_COUNT = 100_000;

    /**
     * Pre-compiled patterns for formatNumber trailing zero removal
     * 预编译的 formatNumber 尾部零移除正则
     */
    private static final Pattern TRAILING_ZEROS = Pattern.compile("0+$");
    private static final Pattern TRAILING_DOT = Pattern.compile("\\.$");

    /**
     * Pre-compiled whitespace pattern for coordinate pair splitting
     * 预编译的坐标对分割空白正则
     */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private WktCodec() {
        // Utility class - no instantiation
    }

    /**
     * Parse a WKT POINT string to a Coordinate
     * 将 WKT POINT 字符串解析为坐标
     *
     * <p>Accepts formats: "POINT(lng lat)" or "POINT (lng lat)"</p>
     * <p>接受格式: "POINT(lng lat)" 或 "POINT (lng lat)"</p>
     *
     * @param wkt the WKT POINT string | WKT POINT 字符串
     * @return parsed WGS84 coordinate | 解析后的 WGS84 坐标
     * @throws GeoException if the input is null, too long, or has invalid format | 当输入为 null、过长或格式无效时抛出
     */
    public static Coordinate parsePoint(String wkt) {
        validateInput(wkt);
        String upper = wkt.strip().toUpperCase(Locale.ROOT);
        if (!upper.startsWith("POINT")) {
            throw new GeoException("Invalid WKT: expected POINT, got: " + truncate(wkt));
        }
        String body = extractBody(wkt.strip(), "POINT");
        String trimmed = body.strip();
        if (trimmed.isEmpty()) {
            throw new GeoException("Invalid WKT POINT: empty coordinate body");
        }
        double[] pair = parseCoordinatePair(trimmed);
        return Coordinate.wgs84(pair[0], pair[1]);
    }

    /**
     * Parse a WKT LINESTRING string to a list of Coordinates
     * 将 WKT LINESTRING 字符串解析为坐标列表
     *
     * <p>Accepts format: "LINESTRING(lng1 lat1, lng2 lat2, ...)"</p>
     * <p>接受格式: "LINESTRING(lng1 lat1, lng2 lat2, ...)"</p>
     *
     * @param wkt the WKT LINESTRING string | WKT LINESTRING 字符串
     * @return list of parsed WGS84 coordinates | 解析后的 WGS84 坐标列表
     * @throws GeoException if the input is null, too long, or has invalid format | 当输入为 null、过长或格式无效时抛出
     */
    public static List<Coordinate> parseLineString(String wkt) {
        validateInput(wkt);
        String upper = wkt.strip().toUpperCase(Locale.ROOT);
        if (!upper.startsWith("LINESTRING")) {
            throw new GeoException("Invalid WKT: expected LINESTRING, got: " + truncate(wkt));
        }
        String body = extractBody(wkt.strip(), "LINESTRING");
        return parseCoordinateList(body.strip());
    }

    /**
     * Parse a WKT POLYGON string to a list of rings (list of coordinates)
     * 将 WKT POLYGON 字符串解析为环列表（坐标列表的列表）
     *
     * <p>Accepts format: "POLYGON((lng1 lat1, lng2 lat2, ...), (lng1 lat1, ...))"</p>
     * <p>接受格式: "POLYGON((lng1 lat1, lng2 lat2, ...), (lng1 lat1, ...))"</p>
     *
     * <p>The first ring is the exterior ring; subsequent rings are holes.</p>
     * <p>第一个环是外环，后续环为孔洞。</p>
     *
     * @param wkt the WKT POLYGON string | WKT POLYGON 字符串
     * @return list of rings, each ring is a list of WGS84 coordinates | 环列表，每个环是 WGS84 坐标列表
     * @throws GeoException if the input is null, too long, or has invalid format | 当输入为 null、过长或格式无效时抛出
     */
    public static List<List<Coordinate>> parsePolygon(String wkt) {
        validateInput(wkt);
        String upper = wkt.strip().toUpperCase(Locale.ROOT);
        if (!upper.startsWith("POLYGON")) {
            throw new GeoException("Invalid WKT: expected POLYGON, got: " + truncate(wkt));
        }
        String body = extractBody(wkt.strip(), "POLYGON");
        return parseRings(body.strip());
    }

    /**
     * Serialize a Coordinate to WKT POINT format
     * 将坐标序列化为 WKT POINT 格式
     *
     * @param c the coordinate | 坐标
     * @return WKT POINT string, e.g. "POINT(116.4074 39.9042)" | WKT POINT 字符串
     * @throws GeoException if the coordinate is null | 当坐标为 null 时抛出
     */
    public static String toWkt(Coordinate c) {
        if (c == null) {
            throw new GeoException("Coordinate must not be null");
        }
        return "POINT(" + formatNumber(c.longitude()) + " " + formatNumber(c.latitude()) + ")";
    }

    /**
     * Serialize a list of Coordinates to WKT LINESTRING format
     * 将坐标列表序列化为 WKT LINESTRING 格式
     *
     * @param coords the coordinates | 坐标列表
     * @return WKT LINESTRING string | WKT LINESTRING 字符串
     * @throws GeoException if the coordinate list is null or empty | 当坐标列表为 null 或为空时抛出
     */
    public static String lineStringToWkt(List<Coordinate> coords) {
        if (coords == null || coords.isEmpty()) {
            throw new GeoException("Coordinate list must not be null or empty");
        }
        return "LINESTRING(" + formatCoordinateList(coords) + ")";
    }

    /**
     * Serialize an exterior ring to WKT POLYGON format (no holes)
     * 将外环序列化为 WKT POLYGON 格式（无孔洞）
     *
     * @param exteriorRing the exterior ring coordinates | 外环坐标
     * @return WKT POLYGON string | WKT POLYGON 字符串
     * @throws GeoException if the ring is null or empty | 当环为 null 或为空时抛出
     */
    public static String polygonToWkt(List<Coordinate> exteriorRing) {
        return polygonToWkt(exteriorRing, null);
    }

    /**
     * Serialize an exterior ring and holes to WKT POLYGON format
     * 将外环和孔洞序列化为 WKT POLYGON 格式
     *
     * @param exteriorRing the exterior ring coordinates | 外环坐标
     * @param holes the hole ring coordinates (may be null or empty) | 孔洞坐标（可为 null 或空）
     * @return WKT POLYGON string | WKT POLYGON 字符串
     * @throws GeoException if the exterior ring is null or empty | 当外环为 null 或为空时抛出
     */
    public static String polygonToWkt(List<Coordinate> exteriorRing, List<List<Coordinate>> holes) {
        if (exteriorRing == null || exteriorRing.isEmpty()) {
            throw new GeoException("Exterior ring must not be null or empty");
        }
        StringBuilder sb = new StringBuilder("POLYGON((");
        sb.append(formatCoordinateList(exteriorRing));
        sb.append(')');
        if (holes != null) {
            for (List<Coordinate> hole : holes) {
                if (hole == null || hole.isEmpty()) {
                    continue;
                }
                sb.append(", (");
                sb.append(formatCoordinateList(hole));
                sb.append(')');
            }
        }
        sb.append(')');
        return sb.toString();
    }

    // ============ Internal Methods | 内部方法 ============

    /**
     * Validate WKT input string
     */
    private static void validateInput(String wkt) {
        if (wkt == null) {
            throw new GeoException("WKT input must not be null");
        }
        if (wkt.length() > MAX_INPUT_LENGTH) {
            throw new GeoException("WKT input exceeds maximum length of " + MAX_INPUT_LENGTH + " characters");
        }
    }

    /**
     * Extract the content inside parentheses after the type keyword
     */
    private static String extractBody(String wkt, String keyword) {
        // Find the first '(' after the keyword
        int keyLen = keyword.length();
        String afterKeyword = wkt.substring(keyLen).strip();
        if (afterKeyword.isEmpty() || afterKeyword.charAt(0) != '(') {
            throw new GeoException("Invalid WKT: expected '(' after " + keyword);
        }
        if (afterKeyword.charAt(afterKeyword.length() - 1) != ')') {
            throw new GeoException("Invalid WKT: expected ')' at end of " + keyword);
        }
        // Remove outer parentheses
        return afterKeyword.substring(1, afterKeyword.length() - 1);
    }

    /**
     * Parse a single "lng lat" pair
     */
    private static double[] parseCoordinatePair(String pair) {
        String[] parts = WHITESPACE.split(pair.strip());
        if (parts.length < 2) {
            throw new GeoException("Invalid WKT coordinate pair: expected 'lng lat', got: " + truncate(pair));
        }
        try {
            double lng = Double.parseDouble(parts[0]);
            double lat = Double.parseDouble(parts[1]);
            return new double[]{lng, lat};
        } catch (NumberFormatException e) {
            throw new GeoException("Invalid WKT coordinate number: " + truncate(pair));
        }
    }

    /**
     * Parse a comma-separated list of coordinate pairs
     */
    private static List<Coordinate> parseCoordinateList(String body) {
        if (body == null || body.strip().isEmpty()) {
            throw new GeoException("Invalid WKT: empty coordinate list");
        }
        String[] pairs = body.split(",");
        if (pairs.length > MAX_COORDINATE_COUNT) {
            throw new GeoException("WKT coordinate count exceeds maximum of " + MAX_COORDINATE_COUNT);
        }
        List<Coordinate> result = new ArrayList<>(pairs.length);
        for (String pair : pairs) {
            double[] coord = parseCoordinatePair(pair);
            result.add(Coordinate.wgs84(coord[0], coord[1]));
        }
        return result;
    }

    /**
     * Parse polygon rings from the body inside POLYGON(...)
     */
    private static List<List<Coordinate>> parseRings(String body) {
        if (body == null || body.strip().isEmpty()) {
            throw new GeoException("Invalid WKT POLYGON: empty body");
        }
        List<List<Coordinate>> rings = new ArrayList<>();
        int totalCoords = 0;
        int i = 0;
        int len = body.length();

        while (i < len) {
            // Skip whitespace and commas between rings
            char ch = body.charAt(i);
            if (ch == ' ' || ch == ',' || ch == '\t' || ch == '\n' || ch == '\r') {
                i++;
                continue;
            }
            if (ch != '(') {
                throw new GeoException("Invalid WKT POLYGON: expected '(' for ring, got '" + ch + "'");
            }
            // Find matching closing paren
            int start = i + 1;
            int depth = 1;
            int j = start;
            while (j < len && depth > 0) {
                if (body.charAt(j) == '(') {
                    depth++;
                } else if (body.charAt(j) == ')') {
                    depth--;
                }
                j++;
            }
            if (depth != 0) {
                throw new GeoException("Invalid WKT POLYGON: unmatched parentheses");
            }
            String ringBody = body.substring(start, j - 1);
            List<Coordinate> ring = parseCoordinateList(ringBody);
            totalCoords += ring.size();
            if (totalCoords > MAX_COORDINATE_COUNT) {
                throw new GeoException("WKT coordinate count exceeds maximum of " + MAX_COORDINATE_COUNT);
            }
            rings.add(ring);
            i = j;
        }

        if (rings.isEmpty()) {
            throw new GeoException("Invalid WKT POLYGON: no rings found");
        }
        return rings;
    }

    /**
     * Format a list of coordinates as "lng1 lat1, lng2 lat2, ..."
     */
    private static String formatCoordinateList(List<Coordinate> coords) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < coords.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Coordinate c = coords.get(i);
            sb.append(formatNumber(c.longitude()));
            sb.append(' ');
            sb.append(formatNumber(c.latitude()));
        }
        return sb.toString();
    }

    /**
     * Format a double number, removing trailing zeros
     */
    private static String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)
                && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
            return String.valueOf((long) value);
        }
        String s = String.valueOf(value);
        // Remove trailing zeros after decimal point
        if (s.contains(".")) {
            s = TRAILING_ZEROS.matcher(s).replaceAll("");
            s = TRAILING_DOT.matcher(s).replaceAll("");
        }
        return s;
    }

    /**
     * Truncate a string for error messages
     */
    private static String truncate(String s) {
        if (s == null) {
            return "null";
        }
        return s.length() > 50 ? s.substring(0, 50) + "..." : s;
    }
}
