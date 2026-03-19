package cloud.opencode.base.event.exception;

import cloud.opencode.base.event.Event;

/**
 * Event Publish Exception
 * 事件发布异常
 *
 * <p>Exception thrown when event publishing fails.</p>
 * <p>事件发布失败时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Event cancelled during processing - 处理过程中事件被取消</li>
 *   <li>Publish timeout - 发布超时</li>
 *   <li>No listeners available - 没有可用监听器</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Publish-specific error handling - 发布特定错误处理</li>
 *   <li>Timeout and cancellation errors - 超时和取消错误</li>
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
public class EventPublishException extends EventException {

    /**
     * Create publish exception with message
     * 使用消息创建发布异常
     *
     * @param message the error message | 错误消息
     */
    public EventPublishException(String message) {
        super(message, EventErrorCode.PUBLISH_FAILED);
    }

    /**
     * Create publish exception with message and cause
     * 使用消息和原因创建发布异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public EventPublishException(String message, Throwable cause) {
        super(message, cause, null, EventErrorCode.PUBLISH_FAILED);
    }

    /**
     * Create publish exception with all parameters
     * 使用所有参数创建发布异常
     *
     * @param message   the error message | 错误消息
     * @param cause     the cause | 原因
     * @param event     the related event | 相关事件
     * @param errorCode the error code | 错误码
     */
    public EventPublishException(String message, Throwable cause, Event event, EventErrorCode errorCode) {
        super(message, cause, event, errorCode);
    }
}
