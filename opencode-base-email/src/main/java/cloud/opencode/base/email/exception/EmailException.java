package cloud.opencode.base.email.exception;

import cloud.opencode.base.core.exception.OpenException;
import cloud.opencode.base.email.Email;

import java.io.Serial;

/**
 * Email Exception Base Class
 * 邮件异常基类
 *
 * <p>Base exception class for all email-related errors.</p>
 * <p>所有邮件相关错误的基类异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Error code support - 错误码支持</li>
 *   <li>Email context preservation - 邮件上下文保留</li>
 *   <li>Retryable flag - 可重试标志</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     OpenEmail.send(email);
 * } catch (EmailException e) {
 *     if (e.isRetryable()) {
 *         // Schedule for retry
 *     }
 *     log.error("Error code: {}", e.getEmailErrorCode().getCode());
 * }
 * }</pre>
 *
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
public class EmailException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "Email";

    private final EmailErrorCode emailErrorCode;
    private final Email email;

    /**
     * Create exception with message only
     * 仅使用消息创建异常
     *
     * @param message the error message | 错误消息
     */
    public EmailException(String message) {
        this(message, null, null, EmailErrorCode.UNKNOWN);
    }

    /**
     * Create exception with message and cause
     * 使用消息和原因创建异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public EmailException(String message, Throwable cause) {
        this(message, cause, null, EmailErrorCode.fromException(cause));
    }

    /**
     * Create exception with message and error code
     * 使用消息和错误码创建异常
     *
     * @param message   the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public EmailException(String message, EmailErrorCode errorCode) {
        this(message, null, null, errorCode);
    }

    /**
     * Create exception with all parameters
     * 使用所有参数创建异常
     *
     * @param message   the error message | 错误消息
     * @param cause     the cause | 原因
     * @param email     the related email | 相关邮件
     * @param errorCode the error code | 错误码
     */
    public EmailException(String message, Throwable cause, Email email, EmailErrorCode errorCode) {
        super(COMPONENT, errorCode != null ? String.valueOf(errorCode.getCode()) : "0", message, cause);
        this.emailErrorCode = errorCode != null ? errorCode : EmailErrorCode.UNKNOWN;
        this.email = email;
    }

    /**
     * Get the email error code
     * 获取邮件错误码
     *
     * @return the email error code | 邮件错误码
     */
    public EmailErrorCode getEmailErrorCode() {
        return emailErrorCode;
    }

    /**
     * Get the related email (if available)
     * 获取相关邮件（如果可用）
     *
     * @return the email or null | 邮件或null
     */
    public Email getEmail() {
        return email;
    }

    /**
     * Check if error is retryable
     * 检查错误是否可重试
     *
     * @return true if retryable | 可重试返回true
     */
    public boolean isRetryable() {
        return emailErrorCode.isRetryable();
    }
}
