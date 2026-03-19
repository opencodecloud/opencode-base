package cloud.opencode.base.rules.conflict;

import cloud.opencode.base.rules.Rule;

import java.util.List;

/**
 * Conflict Resolver Interface - Determines Rule Execution Order
 * 冲突解决器接口 - 确定规则执行顺序
 *
 * <p>When multiple rules match, the conflict resolver determines the order
 * in which they should be executed.</p>
 * <p>当多个规则匹配时，冲突解决器确定它们应该执行的顺序。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Rule ordering - 规则排序</li>
 *   <li>Pluggable strategies - 可插拔策略</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ConflictResolver resolver = rules -> {
 *     List<Rule> sorted = new ArrayList<>(rules);
 *     sorted.sort(Comparator.comparingInt(Rule::getPriority));
 *     return sorted;
 * };
 * engine.setConflictResolver(resolver);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (rules list must not be null) - 空值安全: 否（规则列表不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@FunctionalInterface
public interface ConflictResolver {

    /**
     * Resolves conflicts by ordering the matching rules
     * 通过对匹配规则排序来解决冲突
     *
     * @param rules the matching rules | 匹配的规则
     * @return ordered list of rules | 排序后的规则列表
     */
    List<Rule> resolve(List<Rule> rules);
}
