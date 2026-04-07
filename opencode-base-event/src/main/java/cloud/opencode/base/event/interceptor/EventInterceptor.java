package cloud.opencode.base.event.interceptor;

import cloud.opencode.base.event.Event;

/**
 * Event Interceptor - Pre/post processing hook for event publishing
 * 事件拦截器 - 事件发布的前/后处理钩子
 *
 * <p>Interceptors are invoked in registration order before and after event dispatching.
 * They enable cross-cutting concerns such as logging, metrics, security checks,
 * and event transformation.</p>
 * <p>拦截器按注册顺序在事件分发前后被调用。
 * 它们支持横切关注点，如日志、指标、安全检查和事件转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pre-publish filtering (block events) - 发布前过滤（阻止事件）</li>
 *   <li>Post-publish notification - 发布后通知</li>
 *   <li>Cross-cutting concern support - 横切关注点支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EventInterceptor loggingInterceptor = new EventInterceptor() {
 *     @Override
 *     public boolean beforePublish(Event event) {
 *         log.info("Publishing: {}", event.getClass().getSimpleName());
 *         return true; // allow publishing
 *     }
 *
 *     @Override
 *     public void afterPublish(Event event, boolean dispatched) {
 *         log.info("Published: {}, dispatched={}", event.getClass().getSimpleName(), dispatched);
 *     }
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
public interface EventInterceptor {

    /**
     * Called before an event is dispatched to listeners
     * 在事件分发给监听器之前调用
     *
     * @param event the event about to be published | 即将发布的事件
     * @return true to allow publishing, false to block | true 允许发布，false 阻止
     */
    boolean beforePublish(Event event);

    /**
     * Called after an event has been dispatched to listeners
     * 在事件分发给监听器之后调用
     *
     * @param event      the event that was published | 已发布的事件
     * @param dispatched true if the event was dispatched to at least one listener | 是否已分发给至少一个监听器
     */
    default void afterPublish(Event event, boolean dispatched) {
        // Default no-op
    }
}
