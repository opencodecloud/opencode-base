package cloud.opencode.base.rules.score;

import cloud.opencode.base.rules.RuleContext;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Scoring Engine - Evaluates Scoring Rules and Aggregates Results
 * 评分引擎 - 评估评分规则并聚合结果
 *
 * <p>Evaluates a collection of {@link ScoringRule}s against a context, collects
 * individual scores from rules whose conditions are satisfied, and aggregates
 * them according to the specified {@link AggregationStrategy}.</p>
 * <p>针对上下文评估一组{@link ScoringRule}，收集条件满足的规则的单独分数，
 * 并根据指定的{@link AggregationStrategy}进行聚合。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ScoringRule
 * @see ScoreResult
 * @see AggregationStrategy
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public final class ScoringEngine {

    private ScoringEngine() {
        // utility class
    }

    /**
     * Scores a collection of rules against a context using the given strategy
     * 使用给定策略针对上下文对一组规则进行评分
     *
     * @param context  the rule context | 规则上下文
     * @param rules    the scoring rules | 评分规则
     * @param strategy the aggregation strategy | 聚合策略
     * @return the score result | 评分结果
     * @throws NullPointerException if any argument is null | 如果任何参数为null则抛出
     */
    public static ScoreResult score(RuleContext context, Collection<ScoringRule> rules,
                                    AggregationStrategy strategy) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        if (rules == null) {
            throw new NullPointerException("rules must not be null");
        }
        if (strategy == null) {
            throw new NullPointerException("strategy must not be null");
        }

        Map<String, Double> ruleScores = new LinkedHashMap<>();

        for (ScoringRule rule : rules) {
            if (rule.evaluate(context)) {
                double ruleScore = rule.score(context);
                ruleScores.put(rule.getName(), ruleScore);
            }
        }

        double totalScore = aggregate(ruleScores, rules, strategy);
        return new ScoreResult(totalScore, ruleScores, strategy);
    }

    private static double aggregate(Map<String, Double> ruleScores,
                                    Collection<ScoringRule> rules,
                                    AggregationStrategy strategy) {
        if (ruleScores.isEmpty()) {
            return 0.0;
        }

        return switch (strategy) {
            case SUM -> ruleScores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            case WEIGHTED_SUM -> {
                // Build a name-to-weight lookup from the original rules
                Map<String, Double> weights = new LinkedHashMap<>();
                for (ScoringRule rule : rules) {
                    weights.put(rule.getName(), rule.weight());
                }
                double sum = 0.0;
                for (Map.Entry<String, Double> entry : ruleScores.entrySet()) {
                    double weight = weights.getOrDefault(entry.getKey(), 1.0);
                    sum += entry.getValue() * weight;
                }
                yield sum;
            }

            case MAX -> ruleScores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);

            case AVERAGE -> ruleScores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
        };
    }
}
