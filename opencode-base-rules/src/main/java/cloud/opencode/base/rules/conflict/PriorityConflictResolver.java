package cloud.opencode.base.rules.conflict;

import cloud.opencode.base.rules.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Priority Conflict Resolver - Orders Rules by Priority
 * 优先级冲突解决器 - 按优先级排序规则
 *
 * <p>Orders rules by their priority value (lower value = higher priority).</p>
 * <p>按规则的优先级值排序（值越小优先级越高）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Priority-based sorting - 基于优先级排序</li>
 *   <li>Singleton pattern - 单例模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * engine.setConflictResolver(PriorityConflictResolver.INSTANCE);
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
public class PriorityConflictResolver implements ConflictResolver {

    /** Singleton instance | 单例实例 */
    public static final PriorityConflictResolver INSTANCE = new PriorityConflictResolver();

    private PriorityConflictResolver() {}

    @Override
    public List<Rule> resolve(List<Rule> rules) {
        List<Rule> sorted = new ArrayList<>(rules);
        Collections.sort(sorted);
        return sorted;
    }
}
