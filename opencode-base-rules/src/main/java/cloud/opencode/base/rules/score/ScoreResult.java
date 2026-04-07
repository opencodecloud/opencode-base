package cloud.opencode.base.rules.score;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * Score Result - Aggregated Scoring Outcome from a Scoring Engine Run
 * 评分结果 - 评分引擎运行的聚合评分结果
 *
 * <p>Contains the total aggregated score, individual per-rule scores, and
 * the aggregation strategy used.</p>
 * <p>包含总聚合分数、各规则的单独分数以及使用的聚合策略。</p>
 *
 * @param totalScore the aggregated total score | 聚合总分
 * @param ruleScores per-rule scores (rule name to score) | 各规则分数（规则名称到分数）
 * @param strategy   the aggregation strategy used | 使用的聚合策略
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ScoringEngine
 * @see AggregationStrategy
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public record ScoreResult(double totalScore, Map<String, Double> ruleScores, AggregationStrategy strategy) {

    /**
     * Creates a ScoreResult with a defensive copy of the rule scores map
     * 使用规则分数映射的防御性副本创建ScoreResult
     *
     * @param totalScore the total score | 总分
     * @param ruleScores the per-rule scores | 各规则分数
     * @param strategy   the aggregation strategy | 聚合策略
     */
    public ScoreResult {
        ruleScores = Map.copyOf(ruleScores);
    }

    /**
     * Returns the number of rules that matched (scored)
     * 返回匹配（评分）的规则数量
     *
     * @return match count | 匹配数量
     */
    public int matchCount() {
        return ruleScores.size();
    }

    /**
     * Returns the name of the rule with the highest score
     * 返回得分最高的规则名称
     *
     * @return optional containing the top-scoring rule name, empty if no rules scored |
     *         包含最高分规则名称的Optional，如果没有规则评分则为空
     */
    public Optional<String> topScoringRule() {
        return ruleScores.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey);
    }
}
