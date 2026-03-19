package cloud.opencode.base.rules.spi;

import cloud.opencode.base.rules.model.Action;

import java.util.Map;
import java.util.Optional;

/**
 * Action Provider SPI - Service Provider Interface for Action Discovery
 * 动作提供者SPI - 动作发现的服务提供者接口
 *
 * <p>Implementations of this interface are discovered via ServiceLoader
 * and can provide named actions for use in rule definitions.</p>
 * <p>此接口的实现通过ServiceLoader发现，可以提供命名动作用于规则定义。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ServiceLoader-based discovery - 基于ServiceLoader的发现</li>
 *   <li>Named action registry - 命名动作注册表</li>
 *   <li>Priority-based ordering - 基于优先级排序</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implementation example
 * public class NotificationActionProvider implements ActionProvider {
 *     @Override
 *     public Map<String, Action> getActions() {
 *         return Map.of(
 *             "sendEmail", ctx -> sendEmail(ctx),
 *             "sendSms", ctx -> sendSms(ctx),
 *             "logEvent", ctx -> logEvent(ctx)
 *         );
 *     }
 *
 *     @Override
 *     public String getName() {
 *         return "notification-actions";
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (action names must not be null) - 空值安全: 否（动作名称不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public interface ActionProvider {

    /**
     * Gets all actions provided by this provider
     * 获取此提供者提供的所有动作
     *
     * @return map of action name to action | 动作名称到动作的映射
     */
    Map<String, Action> getActions();

    /**
     * Gets an action by name
     * 按名称获取动作
     *
     * @param name the action name | 动作名称
     * @return the action if found | 如果找到则返回动作
     */
    default Optional<Action> getAction(String name) {
        return Optional.ofNullable(getActions().get(name));
    }

    /**
     * Gets the provider name
     * 获取提供者名称
     *
     * @return the provider name | 提供者名称
     */
    String getName();

    /**
     * Gets the provider priority (lower = higher priority)
     * 获取提供者优先级（数字越小 = 优先级越高）
     *
     * @return the priority | 优先级
     */
    default int getPriority() {
        return 1000;
    }

    /**
     * Checks if this provider is enabled
     * 检查此提供者是否启用
     *
     * @return true if enabled | 如果启用返回true
     */
    default boolean isEnabled() {
        return true;
    }
}
