package cloud.opencode.base.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Priority Annotation
 * 优先级注解
 *
 * <p>Specifies the execution priority of an event handler.</p>
 * <p>指定事件处理器的执行优先级。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Higher value = higher priority - 值越大优先级越高</li>
 *   <li>Default priority is 0 - 默认优先级为0</li>
 *   <li>Handlers execute in priority order - 处理器按优先级顺序执行</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @Subscribe
 * @Priority(100)  // Executes first
 * public void validateOrder(OrderCreatedEvent event) {
 *     if (!isValid(event)) {
 *         event.cancel();  // Cancel to stop lower priority handlers
 *     }
 * }
 *
 * @Subscribe
 * @Priority(50)   // Executes second
 * public void logOrder(OrderCreatedEvent event) {
 *     log.info("Order created: {}", event.getOrderId());
 * }
 *
 * @Subscribe      // Default priority 0, executes last
 * public void processOrder(OrderCreatedEvent event) {
 *     orderService.process(event);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {

    /**
     * The priority value (higher = earlier execution)
     * 优先级值（越高越先执行）
     *
     * @return the priority value | 优先级值
     */
    int value() default 0;
}
