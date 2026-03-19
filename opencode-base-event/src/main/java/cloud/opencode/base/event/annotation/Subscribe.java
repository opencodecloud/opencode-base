package cloud.opencode.base.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Subscribe Annotation
 * 订阅注解
 *
 * <p>Marks a method as an event subscriber.</p>
 * <p>标记方法为事件订阅者。</p>
 *
 * <p><strong>Requirements | 要求:</strong></p>
 * <ul>
 *   <li>Method must have exactly one parameter - 方法必须只有一个参数</li>
 *   <li>Parameter type must be Event or subclass - 参数类型必须是Event或其子类</li>
 *   <li>Method can be any visibility - 方法可以是任意可见性</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class UserEventHandler {
 *     @Subscribe
 *     public void onUserRegistered(UserRegisteredEvent event) {
 *         // Handle user registration
 *     }
 *
 *     @Subscribe
 *     @Async
 *     public void onUserRegisteredAsync(UserRegisteredEvent event) {
 *         // Handle asynchronously
 *     }
 *
 *     @Subscribe
 *     @Priority(100)
 *     public void onUserRegisteredFirst(UserRegisteredEvent event) {
 *         // Handle with high priority
 *     }
 * }
 *
 * // Register the handler
 * OpenEvent.getDefault().register(new UserEventHandler());
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core functionality - 核心功能</li>
 * </ul>
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
public @interface Subscribe {
}
