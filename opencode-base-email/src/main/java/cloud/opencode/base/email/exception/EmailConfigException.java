package cloud.opencode.base.email.exception;

/**
 * Email Configuration Exception
 * 邮件配置异常
 *
 * <p>Exception thrown when email configuration is invalid.</p>
 * <p>邮件配置无效时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Invalid SMTP host - 无效的SMTP主机</li>
 *   <li>Missing credentials - 缺少凭证</li>
 *   <li>Invalid port number - 无效的端口号</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configuration validation error handling - 配置验证错误处理</li>
 *   <li>Error code support - 错误码支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public class EmailConfigException extends EmailException {

    /**
     * Create configuration exception with message
     * 使用消息创建配置异常
     *
     * @param message the error message | 错误消息
     */
    public EmailConfigException(String message) {
        super(message, EmailErrorCode.CONFIG_INVALID);
    }

    /**
     * Create configuration exception with message and cause
     * 使用消息和原因创建配置异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public EmailConfigException(String message, Throwable cause) {
        super(message, cause, null, EmailErrorCode.CONFIG_INVALID);
    }
}
