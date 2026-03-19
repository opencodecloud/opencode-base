package cloud.opencode.base.rules.spi;

import cloud.opencode.base.rules.Rule;

import java.util.Collection;

/**
 * Rule Provider SPI - Service Provider Interface for Rule Discovery
 * 规则提供者SPI - 规则发现的服务提供者接口
 *
 * <p>Implementations of this interface are discovered via ServiceLoader
 * and can provide rules from various sources (files, databases, etc.).</p>
 * <p>此接口的实现通过ServiceLoader发现，可以从各种来源（文件、数据库等）提供规则。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ServiceLoader-based discovery - 基于ServiceLoader的发现</li>
 *   <li>Multiple source support (files, databases) - 多来源支持（文件、数据库）</li>
 *   <li>Hot reload via refresh - 通过refresh热重载</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implementation example
 * public class JsonRuleProvider implements RuleProvider {
 *     @Override
 *     public Collection<Rule> getRules() {
 *         // Load rules from JSON files
 *         return loadFromJson("rules.json");
 *     }
 *
 *     @Override
 *     public String getName() {
 *         return "json-rule-provider";
 *     }
 * }
 *
 * // In META-INF/services/cloud.opencode.base.rules.spi.RuleProvider:
 * // com.example.JsonRuleProvider
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (returned collection must not be null) - 空值安全: 否（返回的集合不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public interface RuleProvider {

    /**
     * Gets all rules provided by this provider
     * 获取此提供者提供的所有规则
     *
     * @return the collection of rules | 规则集合
     */
    Collection<Rule> getRules();

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
     * <p>Default priority is 1000.</p>
     * <p>默认优先级为1000。</p>
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

    /**
     * Refreshes the rules from the source
     * 从源刷新规则
     *
     * <p>Called when rules need to be reloaded.</p>
     * <p>当规则需要重新加载时调用。</p>
     */
    default void refresh() {
        // Default: no-op
    }

    /**
     * Closes resources used by this provider
     * 关闭此提供者使用的资源
     *
     * @throws Exception if closing fails
     */
    default void close() throws Exception {
        // Default: no-op
    }
}
