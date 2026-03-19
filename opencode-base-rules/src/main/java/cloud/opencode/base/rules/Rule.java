package cloud.opencode.base.rules;

/**
 * Rule Interface - Core Abstraction of Business Rule
 * 规则接口 - 业务规则的核心抽象
 *
 * <p>Defines the contract for a business rule with condition evaluation and action execution.</p>
 * <p>定义业务规则的契约，包括条件评估和动作执行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Condition evaluation - 条件评估</li>
 *   <li>Action execution - 动作执行</li>
 *   <li>Priority-based ordering - 基于优先级排序</li>
 *   <li>Group support - 分组支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Rule rule = OpenRules.rule("discount-rule")
 *     .when(ctx -> ctx.get("amount") > 1000)
 *     .then(ctx -> ctx.setResult("discount", 0.1))
 *     .build();
 *
 * if (rule.evaluate(context)) {
 *     rule.execute(context);
 * }
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
 * @see RuleEngine
 * @see RuleContext
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public interface Rule extends Comparable<Rule> {

    /** Default priority value | 默认优先级值 */
    int DEFAULT_PRIORITY = 1000;

    /**
     * Gets the unique name of this rule
     * 获取此规则的唯一名称
     *
     * @return the rule name | 规则名称
     */
    String getName();

    /**
     * Gets the description of this rule
     * 获取此规则的描述
     *
     * @return the rule description, may be null | 规则描述，可能为null
     */
    String getDescription();

    /**
     * Gets the priority of this rule (lower value = higher priority)
     * 获取此规则的优先级（值越小优先级越高）
     *
     * @return the priority value | 优先级值
     */
    int getPriority();

    /**
     * Evaluates the rule condition against the given context
     * 根据给定上下文评估规则条件
     *
     * @param context the rule context containing facts | 包含事实的规则上下文
     * @return true if the condition is satisfied | 如果条件满足返回true
     */
    boolean evaluate(RuleContext context);

    /**
     * Executes the rule action with the given context
     * 使用给定上下文执行规则动作
     *
     * @param context the rule context | 规则上下文
     */
    void execute(RuleContext context);

    /**
     * Gets the group this rule belongs to
     * 获取此规则所属的分组
     *
     * @return the group name, or null if not grouped | 分组名称，如果未分组则为null
     */
    default String getGroup() {
        return null;
    }

    /**
     * Checks if this rule is enabled
     * 检查此规则是否启用
     *
     * @return true if the rule is enabled | 如果规则启用返回true
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Compares rules by priority (lower value = higher priority)
     * 按优先级比较规则（值越小优先级越高）
     *
     * @param other the other rule to compare | 要比较的另一个规则
     * @return comparison result | 比较结果
     */
    @Override
    default int compareTo(Rule other) {
        return Integer.compare(this.getPriority(), other.getPriority());
    }
}
