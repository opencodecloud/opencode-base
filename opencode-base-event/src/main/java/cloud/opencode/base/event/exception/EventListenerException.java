package cloud.opencode.base.event.exception;

import cloud.opencode.base.event.Event;

/**
 * Event Listener Exception
 * 事件监听器异常
 *
 * <p>Exception thrown when event listener operations fail.</p>
 * <p>事件监听器操作失败时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Listener invocation failed - 监听器调用失败</li>
 *   <li>Invalid listener method signature - 无效的监听器方法签名</li>
 *   <li>Listener registration failed - 监听器注册失败</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Listener-specific error handling - 监听器特定错误处理</li>
 *   <li>Error code categorization - 错误码分类</li>
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
public class EventListenerException extends EventException {

    /**
     * Create listener exception with message
     * 使用消息创建监听器异常
     *
     * @param message the error message | 错误消息
     */
    public EventListenerException(String message) {
        super(message, EventErrorCode.LISTENER_ERROR);
    }

    /**
     * Create listener exception with message and cause
     * 使用消息和原因创建监听器异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public EventListenerException(String message, Throwable cause) {
        super(message, cause, null, EventErrorCode.LISTENER_ERROR);
    }

    /**
     * Create listener exception with all parameters
     * 使用所有参数创建监听器异常
     *
     * @param message   the error message | 错误消息
     * @param cause     the cause | 原因
     * @param event     the related event | 相关事件
     * @param errorCode the error code | 错误码
     */
    public EventListenerException(String message, Throwable cause, Event event, EventErrorCode errorCode) {
        super(message, cause, event, errorCode);
    }
}
