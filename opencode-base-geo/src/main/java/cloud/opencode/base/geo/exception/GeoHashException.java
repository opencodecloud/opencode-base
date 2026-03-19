package cloud.opencode.base.geo.exception;

/**
 * GeoHash Exception
 * GeoHash异常
 *
 * <p>Parent exception class for all GeoHash-related exceptions.</p>
 * <p>所有GeoHash相关异常的父类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core functionality - 核心功能</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use for
 * for instance = ...;
 * }</pre>
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
public class GeoHashException extends GeoException {

    /**
     * Create GeoHash exception with message
     * 使用消息创建GeoHash异常
     *
     * @param message the error message | 错误消息
     */
    public GeoHashException(String message) {
        super(message, GeoErrorCode.INVALID_GEOHASH);
    }

    /**
     * Create GeoHash exception with message and cause
     * 使用消息和原因创建GeoHash异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public GeoHashException(String message, Throwable cause) {
        super(message, cause, GeoErrorCode.INVALID_GEOHASH);
    }
}
