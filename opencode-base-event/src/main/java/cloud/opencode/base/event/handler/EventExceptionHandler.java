package cloud.opencode.base.event.handler;

import cloud.opencode.base.event.Event;

/**
 * Event Exception Handler Interface
 * 事件异常处理器接口
 *
 * <p>Interface for handling exceptions that occur during event processing.</p>
 * <p>用于处理事件处理过程中发生的异常的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception handling - 异常处理</li>
 *   <li>Error logging - 错误日志记录</li>
 *   <li>Retry support - 重试支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EventExceptionHandler handler = (event, exception, listenerName) -> {
 *     log.error("Event processing failed: {}", event.getId(), exception);
 * };
 *
 * OpenEvent eventBus = OpenEvent.builder()
 *     .exceptionHandler(handler)
 *     .build();
 * }</pre>
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
@FunctionalInterface
public interface EventExceptionHandler {

    /**
     * Handle an exception that occurred during event processing
     * 处理事件处理过程中发生的异常
     *
     * @param event        the event being processed | 正在处理的事件
     * @param exception    the exception that occurred | 发生的异常
     * @param listenerName the name of the listener that threw | 抛出异常的监听器名称
     */
    void handleException(Event event, Throwable exception, String listenerName);
}
