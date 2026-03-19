package cloud.opencode.base.feature.exception;

/**
 * Feature Config Exception
 * 功能配置异常
 *
 * <p>Exception thrown for feature configuration errors.</p>
 * <p>功能配置错误时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Invalid strategy configuration - 无效的策略配置</li>
 *   <li>Invalid context - 无效的上下文</li>
 *   <li>Missing required fields - 缺少必需字段</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configuration error reporting for feature management - 功能管理的配置错误报告</li>
 *   <li>Extends FeatureException with error codes - 使用错误代码扩展FeatureException</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class FeatureConfigException extends FeatureException {

    /**
     * Create config exception with message
     * 使用消息创建配置异常
     *
     * @param message the error message | 错误消息
     */
    public FeatureConfigException(String message) {
        super(message, FeatureErrorCode.INVALID_CONFIG);
    }

    /**
     * Create config exception with message and error code
     * 使用消息和错误码创建配置异常
     *
     * @param message   the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public FeatureConfigException(String message, FeatureErrorCode errorCode) {
        super(message, null, null, errorCode);
    }

    /**
     * Create config exception with message and cause
     * 使用消息和原因创建配置异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public FeatureConfigException(String message, Throwable cause) {
        super(message, cause, null, FeatureErrorCode.INVALID_CONFIG);
    }
}
