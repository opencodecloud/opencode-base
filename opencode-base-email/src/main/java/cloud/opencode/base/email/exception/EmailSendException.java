package cloud.opencode.base.email.exception;

import cloud.opencode.base.email.Email;

/**
 * Email Send Exception
 * 邮件发送异常
 *
 * <p>Exception thrown when email sending fails.</p>
 * <p>邮件发送失败时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Connection failed - 连接失败</li>
 *   <li>Authentication failed - 认证失败</li>
 *   <li>Recipient rejected - 收件人被拒绝</li>
 *   <li>Send timeout - 发送超时</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Email context preservation - 邮件上下文保留</li>
 *   <li>Error code support - 错误码支持</li>
 *   <li>Retryable flag - 可重试标志</li>
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
public class EmailSendException extends EmailException {

    /**
     * Create send exception with message
     * 使用消息创建发送异常
     *
     * @param message the error message | 错误消息
     */
    public EmailSendException(String message) {
        super(message, EmailErrorCode.UNKNOWN);
    }

    /**
     * Create send exception with message and cause
     * 使用消息和原因创建发送异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create send exception with all parameters
     * 使用所有参数创建发送异常
     *
     * @param message   the error message | 错误消息
     * @param cause     the cause | 原因
     * @param email     the related email | 相关邮件
     * @param errorCode the error code | 错误码
     */
    public EmailSendException(String message, Throwable cause, Email email, EmailErrorCode errorCode) {
        super(message, cause, email, errorCode);
    }

    /**
     * Create send exception with email and error code
     * 使用邮件和错误码创建发送异常
     *
     * @param message   the error message | 错误消息
     * @param email     the related email | 相关邮件
     * @param errorCode the error code | 错误码
     */
    public EmailSendException(String message, Email email, EmailErrorCode errorCode) {
        super(message, null, email, errorCode);
    }
}
