package cloud.opencode.base.rules.conflict;

import cloud.opencode.base.rules.Rule;

import java.util.List;

/**
 * Order Conflict Resolver - Preserves Registration Order
 * 顺序冲突解决器 - 保持注册顺序
 *
 * <p>Rules are executed in the order they were registered.</p>
 * <p>规则按注册顺序执行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Registration order preservation - 保持注册顺序</li>
 *   <li>Singleton pattern - 单例模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * engine.setConflictResolver(OrderConflictResolver.INSTANCE);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless singleton) - 线程安全: 是（无状态单例）</li>
 *   <li>Null-safe: No (rules list must not be null) - 空值安全: 否（规则列表不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public class OrderConflictResolver implements ConflictResolver {

    /** Singleton instance | 单例实例 */
    public static final OrderConflictResolver INSTANCE = new OrderConflictResolver();

    private OrderConflictResolver() {}

    @Override
    public List<Rule> resolve(List<Rule> rules) {
        // Return as-is, preserving registration order
        return rules;
    }
}
