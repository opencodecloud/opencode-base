package cloud.opencode.base.event.exception;

import cloud.opencode.base.event.Event;

/**
 * Event Exception Base Class
 * 事件异常基类
 *
 * <p>Base exception class for all event-related errors.</p>
 * <p>所有事件相关错误的基类异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Error code support - 错误码支持</li>
 *   <li>Event context preservation - 事件上下文保留</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     OpenEvent.getDefault().publish(event);
 * } catch (EventException e) {
 *     log.error("Event error: code={}, message={}",
 *         e.getErrorCode().getCode(), e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public class EventException extends RuntimeException {

    private final Event event;
    private final EventErrorCode errorCode;

    /**
     * Create exception with message only
     * 仅使用消息创建异常
     *
     * @param message the error message | 错误消息
     */
    public EventException(String message) {
        this(message, null, null, EventErrorCode.UNKNOWN);
    }

    /**
     * Create exception with message and cause
     * 使用消息和原因创建异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public EventException(String message, Throwable cause) {
        this(message, cause, null, EventErrorCode.fromException(cause));
    }

    /**
     * Create exception with message and error code
     * 使用消息和错误码创建异常
     *
     * @param message   the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public EventException(String message, EventErrorCode errorCode) {
        this(message, null, null, errorCode);
    }

    /**
     * Create exception with all parameters
     * 使用所有参数创建异常
     *
     * @param message   the error message | 错误消息
     * @param cause     the cause | 原因
     * @param event     the related event | 相关事件
     * @param errorCode the error code | 错误码
     */
    public EventException(String message, Throwable cause, Event event, EventErrorCode errorCode) {
        super(message, cause);
        this.event = event;
        this.errorCode = errorCode != null ? errorCode : EventErrorCode.UNKNOWN;
    }

    /**
     * Get the error code
     * 获取错误码
     *
     * @return the error code | 错误码
     */
    public EventErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Get the related event (if available)
     * 获取相关事件（如果可用）
     *
     * @return the event or null | 事件或null
     */
    public Event getEvent() {
        return event;
    }
}
