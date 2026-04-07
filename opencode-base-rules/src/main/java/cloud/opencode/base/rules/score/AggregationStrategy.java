package cloud.opencode.base.rules.score;

/**
 * Aggregation Strategy - Defines How Individual Rule Scores Are Combined
 * 聚合策略 - 定义如何组合各规则的分数
 *
 * <p>Determines the mathematical operation used to reduce multiple per-rule scores
 * into a single total score.</p>
 * <p>确定用于将多个规则分数归约为单个总分的数学运算。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ScoringEngine
 * @see ScoreResult
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public enum AggregationStrategy {

    /** Sum all individual scores | 所有分数求和 */
    SUM,

    /** Sum of (score * weight) for each rule | 每条规则的 (分数 * 权重) 之和 */
    WEIGHTED_SUM,

    /** Maximum score among all rules | 所有规则中的最高分 */
    MAX,

    /** Average of all scores (0 if none) | 所有分数的平均值（如果没有则为0） */
    AVERAGE
}
