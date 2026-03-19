package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;

import java.util.Arrays;
import java.util.List;

/**
 * Composite Strategy
 * 组合策略
 *
 * <p>Strategy that combines multiple strategies with AND/OR logic.</p>
 * <p>使用AND/OR逻辑组合多个策略的策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>AND logic (allOf) - AND逻辑（全部满足）</li>
 *   <li>OR logic (anyOf) - OR逻辑（任一满足）</li>
 *   <li>Complex conditions - 复杂条件</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // AND: must be VIP user AND within date range
 * EnableStrategy strategy = CompositeStrategy.allOf(
 *     new UserListStrategy(Set.of("vip1", "vip2")),
 *     new DateRangeStrategy(start, end)
 * );
 *
 * // OR: VIP user OR 50% rollout
 * EnableStrategy strategy = CompositeStrategy.anyOf(
 *     new UserListStrategy(Set.of("vip1")),
 *     new PercentageStrategy(50)
 * );
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class CompositeStrategy implements EnableStrategy {

    private final List<EnableStrategy> strategies;
    private final boolean requireAll;

    /**
     * Create composite strategy
     * 创建组合策略
     *
     * @param strategies the list of strategies | 策略列表
     * @param requireAll true for AND logic, false for OR | AND逻辑为true，OR为false
     */
    public CompositeStrategy(List<EnableStrategy> strategies, boolean requireAll) {
        this.strategies = strategies != null ? List.copyOf(strategies) : List.of();
        this.requireAll = requireAll;
    }

    /**
     * Create AND composite (all strategies must pass)
     * 创建AND组合（所有策略必须通过）
     *
     * @param strategies the strategies | 策略
     * @return composite strategy | 组合策略
     */
    public static CompositeStrategy allOf(EnableStrategy... strategies) {
        return new CompositeStrategy(Arrays.asList(strategies), true);
    }

    /**
     * Create AND composite from list
     * 从列表创建AND组合
     *
     * @param strategies the strategies | 策略
     * @return composite strategy | 组合策略
     */
    public static CompositeStrategy allOf(List<EnableStrategy> strategies) {
        return new CompositeStrategy(strategies, true);
    }

    /**
     * Create OR composite (any strategy must pass)
     * 创建OR组合（任一策略通过即可）
     *
     * @param strategies the strategies | 策略
     * @return composite strategy | 组合策略
     */
    public static CompositeStrategy anyOf(EnableStrategy... strategies) {
        return new CompositeStrategy(Arrays.asList(strategies), false);
    }

    /**
     * Create OR composite from list
     * 从列表创建OR组合
     *
     * @param strategies the strategies | 策略
     * @return composite strategy | 组合策略
     */
    public static CompositeStrategy anyOf(List<EnableStrategy> strategies) {
        return new CompositeStrategy(strategies, false);
    }

    /**
     * Evaluate all strategies with AND/OR logic
     * 使用AND/OR逻辑评估所有策略
     *
     * @param feature the feature | 功能
     * @param context the context | 上下文
     * @return true if conditions met | 如果条件满足返回true
     */
    @Override
    public boolean isEnabled(Feature feature, FeatureContext context) {
        if (strategies.isEmpty()) {
            return false;
        }

        if (requireAll) {
            // AND logic: all must pass
            return strategies.stream()
                .allMatch(s -> s.isEnabled(feature, context));
        } else {
            // OR logic: any must pass
            return strategies.stream()
                .anyMatch(s -> s.isEnabled(feature, context));
        }
    }

    /**
     * Get the strategies
     * 获取策略列表
     *
     * @return strategies | 策略列表
     */
    public List<EnableStrategy> getStrategies() {
        return strategies;
    }

    /**
     * Check if using AND logic
     * 检查是否使用AND逻辑
     *
     * @return true if AND | 如果是AND返回true
     */
    public boolean isRequireAll() {
        return requireAll;
    }

    @Override
    public String toString() {
        String logic = requireAll ? "AND" : "OR";
        return "CompositeStrategy{" + logic + ", strategies=" + strategies.size() + "}";
    }
}
