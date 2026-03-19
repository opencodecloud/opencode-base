package cloud.opencode.base.geo.validation;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.exception.CoordinateOutOfRangeException;
import cloud.opencode.base.geo.exception.InvalidCoordinateException;
import cloud.opencode.base.geo.exception.InvalidGeoHashException;

/**
 * Coordinate Validator
 * 坐标验证器
 *
 * <p>Utility class for validating geographic coordinates and GeoHash strings.</p>
 * <p>用于验证地理坐标和GeoHash字符串的工具类。</p>
 *
 * <p><strong>Validation Rules | 验证规则:</strong></p>
 * <ul>
 *   <li>Longitude: -180 to 180 degrees | 经度：-180到180度</li>
 *   <li>Latitude: -90 to 90 degrees | 纬度：-90到90度</li>
 *   <li>Cannot be NaN or Infinite | 不能为NaN或无穷大</li>
 *   <li>GeoHash: Base32 characters only (0-9, b-h, j, k, m, n, p-z) | GeoHash：仅Base32字符</li>
 * </ul>
 *
 * <p><strong>China Bounds | 中国边界:</strong></p>
 * <ul>
 *   <li>Longitude: 72.004 to 137.8347</li>
 *   <li>Latitude: 0.8293 to 55.8271</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Validate coordinates
 * CoordinateValidator.validate(116.4074, 39.9042);  // OK
 * CoordinateValidator.validate(200, 39.9042);  // throws InvalidCoordinateException
 *
 * // Validate in China
 * CoordinateValidator.validateInChina(116.4074, 39.9042);  // OK
 * CoordinateValidator.validateInChina(139.6917, 35.6895);  // throws (Tokyo not in China)
 *
 * // Validate GeoHash
 * CoordinateValidator.validateGeoHash("wx4g0bce");  // OK
 * CoordinateValidator.validateGeoHash("invalid!");  // throws InvalidGeoHashException
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core functionality - 核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public final class CoordinateValidator {

    /**
     * Minimum valid longitude
     * 最小有效经度
     */
    public static final double MIN_LONGITUDE = -180.0;

    /**
     * Maximum valid longitude
     * 最大有效经度
     */
    public static final double MAX_LONGITUDE = 180.0;

    /**
     * Minimum valid latitude
     * 最小有效纬度
     */
    public static final double MIN_LATITUDE = -90.0;

    /**
     * Maximum valid latitude
     * 最大有效纬度
     */
    public static final double MAX_LATITUDE = 90.0;

    /**
     * China longitude boundary (west)
     * 中国经度边界（西）
     */
    public static final double CHINA_MIN_LONGITUDE = 72.004;

    /**
     * China longitude boundary (east)
     * 中国经度边界（东）
     */
    public static final double CHINA_MAX_LONGITUDE = 137.8347;

    /**
     * China latitude boundary (south)
     * 中国纬度边界（南）
     */
    public static final double CHINA_MIN_LATITUDE = 0.8293;

    /**
     * China latitude boundary (north)
     * 中国纬度边界（北）
     */
    public static final double CHINA_MAX_LATITUDE = 55.8271;

    /**
     * Valid GeoHash characters (Base32 without a, i, l, o)
     * 有效的GeoHash字符（不含a, i, l, o的Base32）
     */
    private static final String GEOHASH_CHARS = "0123456789bcdefghjkmnpqrstuvwxyz";

    /**
     * Maximum GeoHash length
     * GeoHash最大长度
     */
    public static final int MAX_GEOHASH_LENGTH = 12;

    private CoordinateValidator() {
        // Utility class
    }

    /**
     * Validate coordinate values
     * 验证坐标值
     *
     * @param longitude the longitude value | 经度值
     * @param latitude the latitude value | 纬度值
     * @throws InvalidCoordinateException if coordinates are invalid | 如果坐标无效则抛出异常
     */
    public static void validate(double longitude, double latitude) {
        if (Double.isNaN(longitude) || Double.isNaN(latitude)) {
            throw new InvalidCoordinateException("Coordinate cannot be NaN");
        }
        if (Double.isInfinite(longitude) || Double.isInfinite(latitude)) {
            throw new InvalidCoordinateException("Coordinate cannot be Infinite");
        }
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new InvalidCoordinateException(
                "Longitude must be between " + MIN_LONGITUDE + " and " + MAX_LONGITUDE + ": " + longitude);
        }
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new InvalidCoordinateException(
                "Latitude must be between " + MIN_LATITUDE + " and " + MAX_LATITUDE + ": " + latitude);
        }
    }

    /**
     * Validate a Coordinate object
     * 验证坐标对象
     *
     * @param coordinate the coordinate to validate | 要验证的坐标
     * @throws InvalidCoordinateException if coordinate is invalid | 如果坐标无效则抛出异常
     */
    public static void validate(Coordinate coordinate) {
        if (coordinate == null) {
            throw new InvalidCoordinateException("Coordinate cannot be null");
        }
        validate(coordinate.longitude(), coordinate.latitude());
    }

    /**
     * Check if coordinates are valid without throwing exception
     * 检查坐标是否有效（不抛出异常）
     *
     * @param longitude the longitude value | 经度值
     * @param latitude the latitude value | 纬度值
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(double longitude, double latitude) {
        if (Double.isNaN(longitude) || Double.isNaN(latitude)) {
            return false;
        }
        if (Double.isInfinite(longitude) || Double.isInfinite(latitude)) {
            return false;
        }
        return longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE
            && latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE;
    }

    /**
     * Check if a Coordinate object is valid
     * 检查坐标对象是否有效
     *
     * @param coordinate the coordinate to check | 要检查的坐标
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(Coordinate coordinate) {
        return coordinate != null && isValid(coordinate.longitude(), coordinate.latitude());
    }

    /**
     * Validate that coordinates are within China bounds
     * 验证坐标在中国境内
     *
     * @param longitude the longitude value | 经度值
     * @param latitude the latitude value | 纬度值
     * @throws InvalidCoordinateException if coordinates are invalid | 如果坐标无效则抛出异常
     * @throws CoordinateOutOfRangeException if coordinates are outside China | 如果坐标不在中国境内则抛出异常
     */
    public static void validateInChina(double longitude, double latitude) {
        validate(longitude, latitude);
        if (!isInChina(longitude, latitude)) {
            throw new CoordinateOutOfRangeException(
                "Coordinate is not in China: " + longitude + ", " + latitude);
        }
    }

    /**
     * Validate that a Coordinate is within China bounds
     * 验证坐标对象在中国境内
     *
     * @param coordinate the coordinate to validate | 要验证的坐标
     * @throws InvalidCoordinateException if coordinate is invalid | 如果坐标无效则抛出异常
     * @throws CoordinateOutOfRangeException if coordinate is outside China | 如果坐标不在中国境内则抛出异常
     */
    public static void validateInChina(Coordinate coordinate) {
        validate(coordinate);
        validateInChina(coordinate.longitude(), coordinate.latitude());
    }

    /**
     * Check if coordinates are within China bounds
     * 检查坐标是否在中国境内
     *
     * @param longitude the longitude value | 经度值
     * @param latitude the latitude value | 纬度值
     * @return true if in China | 如果在中国境内返回true
     */
    public static boolean isInChina(double longitude, double latitude) {
        return longitude >= CHINA_MIN_LONGITUDE && longitude <= CHINA_MAX_LONGITUDE
            && latitude >= CHINA_MIN_LATITUDE && latitude <= CHINA_MAX_LATITUDE;
    }

    /**
     * Check if a Coordinate is within China bounds
     * 检查坐标对象是否在中国境内
     *
     * @param coordinate the coordinate to check | 要检查的坐标
     * @return true if in China | 如果在中国境内返回true
     */
    public static boolean isInChina(Coordinate coordinate) {
        return coordinate != null && isInChina(coordinate.longitude(), coordinate.latitude());
    }

    /**
     * Validate a GeoHash string
     * 验证GeoHash字符串
     *
     * @param hash the GeoHash string | GeoHash字符串
     * @throws InvalidGeoHashException if GeoHash is invalid | 如果GeoHash无效则抛出异常
     */
    public static void validateGeoHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            throw new InvalidGeoHashException("GeoHash cannot be null or empty");
        }
        if (hash.length() > MAX_GEOHASH_LENGTH) {
            throw new InvalidGeoHashException("GeoHash length cannot exceed " + MAX_GEOHASH_LENGTH + ": " + hash);
        }
        String lowerHash = hash.toLowerCase();
        for (char c : lowerHash.toCharArray()) {
            if (GEOHASH_CHARS.indexOf(c) < 0) {
                throw new InvalidGeoHashException("Invalid GeoHash character '" + c + "' in: " + hash);
            }
        }
    }

    /**
     * Check if a GeoHash string is valid without throwing exception
     * 检查GeoHash字符串是否有效（不抛出异常）
     *
     * @param hash the GeoHash string | GeoHash字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidGeoHash(String hash) {
        if (hash == null || hash.isEmpty() || hash.length() > MAX_GEOHASH_LENGTH) {
            return false;
        }
        String lowerHash = hash.toLowerCase();
        for (char c : lowerHash.toCharArray()) {
            if (GEOHASH_CHARS.indexOf(c) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clamp longitude to valid range
     * 将经度限制在有效范围内
     *
     * @param longitude the longitude value | 经度值
     * @return clamped longitude | 限制后的经度
     */
    public static double clampLongitude(double longitude) {
        return Math.max(MIN_LONGITUDE, Math.min(MAX_LONGITUDE, longitude));
    }

    /**
     * Clamp latitude to valid range
     * 将纬度限制在有效范围内
     *
     * @param latitude the latitude value | 纬度值
     * @return clamped latitude | 限制后的纬度
     */
    public static double clampLatitude(double latitude) {
        return Math.max(MIN_LATITUDE, Math.min(MAX_LATITUDE, latitude));
    }

    /**
     * Normalize longitude to -180 to 180 range
     * 将经度标准化到-180到180范围
     *
     * @param longitude the longitude value | 经度值
     * @return normalized longitude | 标准化后的经度
     */
    public static double normalizeLongitude(double longitude) {
        if (Double.isNaN(longitude) || Double.isInfinite(longitude)) {
            return 0;
        }
        // Use modulo for O(1) normalization instead of O(n) loop
        longitude = longitude % 360.0;
        if (longitude > 180) {
            longitude -= 360;
        }
        if (longitude < -180) {
            longitude += 360;
        }
        return longitude;
    }
}
