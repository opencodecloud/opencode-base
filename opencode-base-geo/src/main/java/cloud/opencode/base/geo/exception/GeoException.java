package cloud.opencode.base.geo.exception;

import cloud.opencode.base.core.exception.OpenException;
import cloud.opencode.base.geo.Coordinate;

import java.io.Serial;

/**
 * Geo Exception Base Class - Unified exception base for all geographic operations
 * 地理异常基类 - 所有地理操作的统一异常基类
 *
 * <p>Base exception class for all geographic operations, extending {@link OpenException}
 * to integrate with the OpenCode unified exception framework.</p>
 * <p>所有地理操作的基础异常类，继承 {@link OpenException} 以集成 OpenCode 统一异常框架。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Error code support via {@link GeoErrorCode} - 通过 {@link GeoErrorCode} 支持错误码</li>
 *   <li>Coordinate context - 坐标上下文</li>
 *   <li>Chained exceptions - 异常链</li>
 *   <li>OpenException integration with component "Geo" - 集成 OpenException，组件名 "Geo"</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new GeoException("Operation failed", GeoErrorCode.UNKNOWN);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Partial (errorCode defaults to UNKNOWN if null) - 空值安全: 部分（errorCode为null时默认UNKNOWN）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public class GeoException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final GeoErrorCode errorCode;
    private final Coordinate coordinate;

    /**
     * Create geo exception with message
     * 使用消息创建地理异常
     *
     * @param message the error message | 错误消息
     */
    public GeoException(String message) {
        this(message, null, GeoErrorCode.UNKNOWN, null);
    }

    /**
     * Create geo exception with message and error code
     * 使用消息和错误码创建地理异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public GeoException(String message, GeoErrorCode errorCode) {
        this(message, null, errorCode, null);
    }

    /**
     * Create geo exception with message, cause, and error code
     * 使用消息、原因和错误码创建地理异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     * @param errorCode the error code | 错误码
     */
    public GeoException(String message, Throwable cause, GeoErrorCode errorCode) {
        this(message, cause, errorCode, null);
    }

    /**
     * Create geo exception with all parameters
     * 使用所有参数创建地理异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     * @param errorCode the error code | 错误码
     * @param coordinate the related coordinate | 相关坐标
     */
    public GeoException(String message, Throwable cause, GeoErrorCode errorCode, Coordinate coordinate) {
        super("Geo",
            errorCode != null ? String.valueOf(errorCode.getCode()) : String.valueOf(GeoErrorCode.UNKNOWN.getCode()),
            message, cause);
        this.errorCode = errorCode != null ? errorCode : GeoErrorCode.UNKNOWN;
        this.coordinate = coordinate;
    }

    /**
     * Get the geo error code
     * 获取地理错误码
     *
     * @return the geo error code | 地理错误码
     */
    public GeoErrorCode getGeoErrorCode() {
        return errorCode;
    }

    /**
     * Get the related coordinate
     * 获取相关坐标
     *
     * @return the coordinate or null | 坐标或null
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }
}
