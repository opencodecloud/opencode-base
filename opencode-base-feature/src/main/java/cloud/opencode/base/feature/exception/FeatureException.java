package cloud.opencode.base.feature.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * Feature Exception Base Class
 * 功能异常基类
 *
 * <p>Base exception class for all feature-related errors.</p>
 * <p>所有功能相关错误的基类异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Error code support - 错误码支持</li>
 *   <li>Feature key context - 功能键上下文</li>
 *   <li>Extends OpenException for unified exception handling - 扩展OpenException以统一异常处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     OpenFeature.getInstance().get("unknown");
 * } catch (FeatureException e) {
 *     log.error("Feature error: code={}, message={}",
 *         e.getFeatureErrorCode().getCode(), e.getMessage());
 * }
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
public class FeatureException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String featureKey;
    private final FeatureErrorCode errorCode;

    /**
     * Create exception with message
     * 使用消息创建异常
     *
     * @param message the error message | 错误消息
     */
    public FeatureException(String message) {
        this(message, null, null, FeatureErrorCode.UNKNOWN);
    }

    /**
     * Create exception with message and cause
     * 使用消息和原因创建异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public FeatureException(String message, Throwable cause) {
        this(message, cause, null, FeatureErrorCode.UNKNOWN);
    }

    /**
     * Create exception with message and error code
     * 使用消息和错误码创建异常
     *
     * @param message   the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public FeatureException(String message, FeatureErrorCode errorCode) {
        this(message, null, null, errorCode);
    }

    /**
     * Create exception with all parameters
     * 使用所有参数创建异常
     *
     * @param message    the error message | 错误消息
     * @param cause      the cause | 原因
     * @param featureKey the feature key | 功能键
     * @param errorCode  the error code | 错误码
     */
    public FeatureException(String message, Throwable cause, String featureKey, FeatureErrorCode errorCode) {
        super("feature", errorCode != null ? String.valueOf(errorCode.getCode()) : null, message, cause);
        this.featureKey = featureKey;
        this.errorCode = errorCode != null ? errorCode : FeatureErrorCode.UNKNOWN;
    }

    /**
     * Get the feature key
     * 获取功能键
     *
     * @return feature key or null | 功能键或null
     */
    public String getFeatureKey() {
        return featureKey;
    }

    /**
     * Get the feature error code
     * 获取功能错误码
     *
     * @return error code | 错误码
     */
    public FeatureErrorCode getFeatureErrorCode() {
        return errorCode;
    }
}
