package cloud.opencode.base.feature.exception;

/**
 * Feature Security Exception
 * 功能安全异常
 *
 * <p>Exception thrown for feature security errors.</p>
 * <p>功能安全错误时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Unauthorized operation - 未授权操作</li>
 *   <li>Audit logging failed - 审计日志失败</li>
 *   <li>Security violation - 安全违规</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Security violation reporting for feature access control - 功能访问控制的安全违规报告</li>
 *   <li>Supports error codes for categorized security errors - 支持分类安全错误的错误代码</li>
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
public class FeatureSecurityException extends FeatureException {

    /**
     * Create security exception with message
     * 使用消息创建安全异常
     *
     * @param message the error message | 错误消息
     */
    public FeatureSecurityException(String message) {
        super(message, FeatureErrorCode.UNAUTHORIZED);
    }

    /**
     * Create security exception with message and error code
     * 使用消息和错误码创建安全异常
     *
     * @param message   the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public FeatureSecurityException(String message, FeatureErrorCode errorCode) {
        super(message, null, null, errorCode);
    }

    /**
     * Create security exception with message and cause
     * 使用消息和原因创建安全异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public FeatureSecurityException(String message, Throwable cause) {
        super(message, cause, null, FeatureErrorCode.UNAUTHORIZED);
    }
}
