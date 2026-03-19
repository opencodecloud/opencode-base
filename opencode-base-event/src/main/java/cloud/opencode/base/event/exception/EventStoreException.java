package cloud.opencode.base.event.exception;

import cloud.opencode.base.event.Event;

/**
 * Event Store Exception
 * 事件存储异常
 *
 * <p>Exception thrown when event store operations fail.</p>
 * <p>事件存储操作失败时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Event persistence failed - 事件持久化失败</li>
 *   <li>Event replay failed - 事件重放失败</li>
 *   <li>Store connection error - 存储连接错误</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Store operation error handling - 存储操作错误处理</li>
 *   <li>Persistence and replay errors - 持久化和重放错误</li>
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
public class EventStoreException extends EventException {

    /**
     * Create store exception with message
     * 使用消息创建存储异常
     *
     * @param message the error message | 错误消息
     */
    public EventStoreException(String message) {
        super(message, EventErrorCode.STORE_ERROR);
    }

    /**
     * Create store exception with message and cause
     * 使用消息和原因创建存储异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public EventStoreException(String message, Throwable cause) {
        super(message, cause, null, EventErrorCode.STORE_ERROR);
    }

    /**
     * Create store exception with all parameters
     * 使用所有参数创建存储异常
     *
     * @param message   the error message | 错误消息
     * @param cause     the cause | 原因
     * @param event     the related event | 相关事件
     * @param errorCode the error code | 错误码
     */
    public EventStoreException(String message, Throwable cause, Event event, EventErrorCode errorCode) {
        super(message, cause, event, errorCode);
    }
}
