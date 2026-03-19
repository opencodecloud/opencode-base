package cloud.opencode.base.email.exception;

/**
 * Email Security Exception
 * 邮件安全异常
 *
 * <p>Exception thrown for security-related email errors.</p>
 * <p>安全相关邮件错误时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Header injection attack - 邮件头注入攻击</li>
 *   <li>Invalid attachment type - 无效的附件类型</li>
 *   <li>Attachment size exceeded - 附件大小超限</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Header injection detection - 邮件头注入检测</li>
 *   <li>Attachment security validation - 附件安全验证</li>
 *   <li>Error code support - 错误码支持</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public class EmailSecurityException extends EmailException {

    /**
     * Create security exception with message
     * 使用消息创建安全异常
     *
     * @param message the error message | 错误消息
     */
    public EmailSecurityException(String message) {
        super(message, EmailErrorCode.HEADER_INJECTION);
    }

    /**
     * Create security exception with message and error code
     * 使用消息和错误码创建安全异常
     *
     * @param message   the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public EmailSecurityException(String message, EmailErrorCode errorCode) {
        super(message, errorCode);
    }

    /**
     * Create security exception with message and cause
     * 使用消息和原因创建安全异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public EmailSecurityException(String message, Throwable cause) {
        super(message, cause, null, EmailErrorCode.HEADER_INJECTION);
    }
}
