package cloud.opencode.base.geo.exception;

/**
 * Geo Error Code Enumeration
 * 地理错误码枚举
 *
 * <p>Defines error codes for geographic operations.</p>
 * <p>定义地理操作的错误码。</p>
 *
 * <p><strong>Error Code Ranges | 错误码范围:</strong></p>
 * <ul>
 *   <li>0 - Unknown error | 未知错误</li>
 *   <li>1xxx - Coordinate errors | 坐标错误</li>
 *   <li>2xxx - GeoHash errors | GeoHash错误</li>
 *   <li>3xxx - Fence errors | 围栏错误</li>
 *   <li>4xxx - Security errors | 安全错误</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Structured error codes for coordinate, GeoHash, fence, and security errors - 坐标、GeoHash、围栏和安全错误的结构化错误码</li>
 *   <li>Bilingual error messages (English and Chinese) - 双语错误消息（英文和中文）</li>
 *   <li>Numeric code ranges for error category identification - 用于错误类别识别的数字码范围</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * GeoErrorCode code = GeoErrorCode.INVALID_COORDINATE;
 * int numeric = code.getCode();      // 1001
 * String msg = code.getMessage();    // "Invalid coordinate"
 * String msgZh = code.getMessageZh(); // "无效坐标"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public enum GeoErrorCode {

    /** Unknown error | 未知错误 */
    UNKNOWN(0, "Unknown error", "未知错误"),

    // Coordinate errors 1xxx
    /** Invalid coordinate | 无效坐标 */
    INVALID_COORDINATE(1001, "Invalid coordinate", "无效坐标"),
    /** Coordinate out of range | 坐标越界 */
    COORDINATE_OUT_OF_RANGE(1002, "Coordinate out of range", "坐标越界"),
    /** Transform failed | 坐标转换失败 */
    TRANSFORM_FAILED(1003, "Coordinate transform failed", "坐标转换失败"),
    /** Not in China | 坐标不在中国境内 */
    NOT_IN_CHINA(1004, "Coordinate is not in China", "坐标不在中国境内"),

    // GeoHash errors 2xxx
    /** Invalid GeoHash | 无效GeoHash */
    INVALID_GEOHASH(2001, "Invalid GeoHash", "无效GeoHash"),
    /** GeoHash encode failed | GeoHash编码失败 */
    GEOHASH_ENCODE_FAILED(2002, "GeoHash encode failed", "GeoHash编码失败"),
    /** GeoHash decode failed | GeoHash解码失败 */
    GEOHASH_DECODE_FAILED(2003, "GeoHash decode failed", "GeoHash解码失败"),

    // Fence errors 3xxx
    /** Fence not found | 围栏不存在 */
    FENCE_NOT_FOUND(3001, "Fence not found", "围栏不存在"),
    /** Invalid fence | 无效围栏定义 */
    INVALID_FENCE(3002, "Invalid fence definition", "无效围栏定义"),
    /** Fence check failed | 围栏检查失败 */
    FENCE_CHECK_FAILED(3003, "Fence check failed", "围栏检查失败"),
    /** Insufficient vertices | 多边形顶点不足 */
    INSUFFICIENT_VERTICES(3004, "Insufficient polygon vertices", "多边形顶点不足"),

    // Security errors 4xxx
    /** Location spoofing | 位置欺骗检测 */
    LOCATION_SPOOFING(4001, "Location spoofing detected", "位置欺骗检测"),
    /** Invalid timestamp | 无效时间戳 */
    INVALID_TIMESTAMP(4002, "Invalid timestamp", "无效时间戳"),
    /** Impossible speed | 不可能的移动速度 */
    IMPOSSIBLE_SPEED(4003, "Impossible travel speed", "不可能的移动速度");

    private final int code;
    private final String message;
    private final String messageZh;

    GeoErrorCode(int code, String message, String messageZh) {
        this.code = code;
        this.message = message;
        this.messageZh = messageZh;
    }

    /**
     * Get the error code
     * 获取错误码
     *
     * @return the error code | 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the error message (English)
     * 获取错误消息（英文）
     *
     * @return the error message | 错误消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the error message (Chinese)
     * 获取错误消息（中文）
     *
     * @return the error message in Chinese | 中文错误消息
     */
    public String getMessageZh() {
        return messageZh;
    }
}
