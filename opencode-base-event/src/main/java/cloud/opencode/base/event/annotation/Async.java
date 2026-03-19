package cloud.opencode.base.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Async Processing Annotation
 * 异步处理注解
 *
 * <p>Marks an event handler method for asynchronous execution.</p>
 * <p>标记事件处理方法为异步执行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Non-blocking event handling - 非阻塞事件处理</li>
 *   <li>Virtual thread execution (JDK 25+) - 虚拟线程执行</li>
 *   <li>Suitable for IO-bound operations - 适合IO密集型操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @Subscribe
 * @Async
 * public void onOrderCreated(OrderCreatedEvent event) {
 *     // This runs in a virtual thread
 *     sendNotificationEmail(event);
 *     updateAnalytics(event);
 * }
 * }</pre>
 *
 * <p><strong>Note | 注意:</strong></p>
 * <p>Async handlers run independently and exceptions won't affect other handlers.</p>
 * <p>异步处理器独立运行，异常不会影响其他处理器。</p>
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
public @interface Async {
}
