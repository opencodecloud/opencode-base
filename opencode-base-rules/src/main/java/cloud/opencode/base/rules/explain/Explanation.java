package cloud.opencode.base.rules.explain;

import java.util.List;
import java.util.Objects;

/**
 * Explanation - Complete Explanation of a Rule Engine Execution
 * 解释 - 规则引擎执行的完整解释
 *
 * <p>Aggregates individual rule explanations with an overall summary.</p>
 * <p>聚合单个规则解释和总体摘要。</p>
 *
 * @param summary overall summary of the execution | 执行的总体摘要
 * @param details per-rule explanations | 每个规则的解释
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public record Explanation(
        String summary,
        List<RuleExplanation> details
) {

    /**
     * Canonical constructor with validation
     * 带验证的规范构造函数
     *
     * @param summary overall summary | 总体摘要
     * @param details per-rule explanations | 每个规则的解释
     */
    public Explanation {
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(details, "details must not be null");
        details = List.copyOf(details);
    }

    /**
     * Returns the count of rules that fired
     * 返回已触发规则的数量
     *
     * @return fired rule count | 已触发规则的数量
     */
    public int firedCount() {
        return (int) details.stream().filter(RuleExplanation::fired).count();
    }

    /**
     * Returns the total number of rules
     * 返回规则的总数
     *
     * @return total rule count | 规则总数
     */
    public int totalCount() {
        return details.size();
    }

    /**
     * Returns a formatted multi-line string representation
     * 返回格式化的多行字符串表示
     *
     * @return formatted explanation | 格式化的解释
     */
    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(summary).append('\n');
        for (RuleExplanation detail : details) {
            sb.append("  - ").append(detail.reason()).append('\n');
        }
        return sb.toString();
    }
}
