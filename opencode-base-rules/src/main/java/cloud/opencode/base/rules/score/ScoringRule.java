package cloud.opencode.base.rules.score;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;

/**
 * Scoring Rule - Rule That Produces a Numeric Score
 * 评分规则 - 产生数值分数的规则
 *
 * <p>Extends {@link Rule} to add scoring capability. When a rule's condition evaluates
 * to true, the {@link #score(RuleContext)} method computes a numeric score that can
 * be aggregated by a {@link ScoringEngine}.</p>
 * <p>扩展{@link Rule}以添加评分能力。当规则的条件评估为真时，
 * {@link #score(RuleContext)}方法计算一个可被{@link ScoringEngine}聚合的数值分数。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ScoringEngine
 * @see AggregationStrategy
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public interface ScoringRule extends Rule {

    /**
     * Computes the score for this rule in the given context
     * 在给定上下文中计算此规则的分数
     *
     * @param context the rule context | 规则上下文
     * @return the computed score | 计算的分数
     */
    double score(RuleContext context);

    /**
     * Returns the weight of this rule for weighted aggregation
     * 返回此规则用于加权聚合的权重
     *
     * @return the weight, defaults to 1.0 | 权重，默认为1.0
     */
    default double weight() {
        return 1.0;
    }
}
