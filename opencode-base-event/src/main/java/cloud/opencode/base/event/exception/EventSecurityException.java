package cloud.opencode.base.event.exception;

import cloud.opencode.base.event.Event;

/**
 * Event Security Exception
 * 事件安全异常
 *
 * <p>Exception thrown for security-related event errors.</p>
 * <p>安全相关事件错误时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Event signature verification failed - 事件签名验证失败</li>
 *   <li>Rate limit exceeded - 频率限制超出</li>
 *   <li>Unauthorized listener registration - 未授权的监听器注册</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Security violation reporting - 安全违规报告</li>
 *   <li>Rate limit and verification errors - 频率限制和验证错误</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public class EventSecurityException extends EventException {

    /**
     * Create security exception with message
     * 使用消息创建安全异常
     *
     * @param message the error message | 错误消息
     */
    public EventSecurityException(String message) {
        super(message, EventErrorCode.SECURITY_VIOLATION);
    }

    /**
     * Create security exception with message and error code
     * 使用消息和错误码创建安全异常
     *
     * @param message   the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public EventSecurityException(String message, EventErrorCode errorCode) {
        super(message, errorCode);
    }

    /**
     * Create security exception with message and cause
     * 使用消息和原因创建安全异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public EventSecurityException(String message, Throwable cause) {
        super(message, cause, null, EventErrorCode.SECURITY_VIOLATION);
    }

    /**
     * Create security exception with all parameters
     * 使用所有参数创建安全异常
     *
     * @param message   the error message | 错误消息
     * @param cause     the cause | 原因
     * @param event     the related event | 相关事件
     * @param errorCode the error code | 错误码
     */
    public EventSecurityException(String message, Throwable cause, Event event, EventErrorCode errorCode) {
        super(message, cause, event, errorCode);
    }
}
