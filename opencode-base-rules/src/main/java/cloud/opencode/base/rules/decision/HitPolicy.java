package cloud.opencode.base.rules.decision;

/**
 * Hit Policy - Determines How Decision Table Matches Are Handled
 * 命中策略 - 确定如何处理决策表匹配
 *
 * <p>Defines the behavior when one or more rows in a decision table match the input.</p>
 * <p>定义当决策表中一行或多行匹配输入时的行为。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Seven hit policies (UNIQUE, FIRST, PRIORITY, ANY, COLLECT, RULE_ORDER, OUTPUT_ORDER) - 七种命中策略</li>
 *   <li>DMN standard compliance - 符合DMN标准</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DecisionTable table = OpenRules.decisionTable()
 *     .hitPolicy(HitPolicy.FIRST)
 *     .input("status", String.class)
 *     .output("action", String.class)
 *     .row(new Object[]{"active"}, new Object[]{"process"})
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public enum HitPolicy {

    /**
     * Unique - Only one row can match (error if multiple match)
     * 唯一 - 只能有一行匹配（如果多行匹配则报错）
     */
    UNIQUE,

    /**
     * First - Return the first matching row
     * 首个 - 返回第一个匹配的行
     */
    FIRST,

    /**
     * Priority - Return the row with highest priority
     * 优先级 - 返回优先级最高的行
     */
    PRIORITY,

    /**
     * Any - Return any matching row (all matches must produce same output)
     * 任意 - 返回任意匹配的行（所有匹配必须产生相同输出）
     */
    ANY,

    /**
     * Collect - Return all matching rows
     * 收集 - 返回所有匹配的行
     */
    COLLECT,

    /**
     * Rule Order - Return all matches in rule definition order
     * 规则顺序 - 按规则定义顺序返回所有匹配
     */
    RULE_ORDER,

    /**
     * Output Order - Return all matches ordered by output values
     * 输出顺序 - 按输出值排序返回所有匹配
     */
    OUTPUT_ORDER
}
