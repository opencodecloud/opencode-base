package cloud.opencode.base.rules.score;

import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link ScoringEngine}.
 * {@link ScoringEngine} 的测试。
 */
@DisplayName("ScoringEngine - 评分引擎")
class ScoringEngineTest {

    private static ScoringRule createRule(String name, boolean matches, double score, double weight) {
        return new ScoringRule() {
            @Override public String getName() { return name; }
            @Override public String getDescription() { return name; }
            @Override public int getPriority() { return 1000; }
            @Override public boolean evaluate(RuleContext context) { return matches; }
            @Override public void execute(RuleContext context) {}
            @Override public double score(RuleContext context) { return score; }
            @Override public double weight() { return weight; }
        };
    }

    private static ScoringRule createRule(String name, boolean matches, double score) {
        return createRule(name, matches, score, 1.0);
    }

    @Nested
    @DisplayName("SUM strategy - SUM策略")
    class SumStrategy {

        @Test
        @DisplayName("Should sum all matching scores - 应对所有匹配分数求和")
        void shouldSumScores() {
            ScoringRule r1 = createRule("a", true, 10.0);
            ScoringRule r2 = createRule("b", true, 20.0);
            ScoringRule r3 = createRule("c", false, 100.0);

            ScoreResult result = ScoringEngine.score(
                    RuleContext.create(), List.of(r1, r2, r3), AggregationStrategy.SUM);

            assertThat(result.totalScore()).isCloseTo(30.0, within(0.001));
            assertThat(result.matchCount()).isEqualTo(2);
            assertThat(result.ruleScores()).containsEntry("a", 10.0);
            assertThat(result.ruleScores()).containsEntry("b", 20.0);
            assertThat(result.ruleScores()).doesNotContainKey("c");
        }
    }

    @Nested
    @DisplayName("WEIGHTED_SUM strategy - WEIGHTED_SUM策略")
    class WeightedSumStrategy {

        @Test
        @DisplayName("Should apply weights to scores - 应对分数应用权重")
        void shouldApplyWeights() {
            ScoringRule r1 = createRule("a", true, 10.0, 2.0);
            ScoringRule r2 = createRule("b", true, 5.0, 3.0);

            ScoreResult result = ScoringEngine.score(
                    RuleContext.create(), List.of(r1, r2), AggregationStrategy.WEIGHTED_SUM);

            // 10*2 + 5*3 = 35
            assertThat(result.totalScore()).isCloseTo(35.0, within(0.001));
        }

        @Test
        @DisplayName("Should use default weight of 1.0 - 应使用默认权重1.0")
        void shouldUseDefaultWeight() {
            ScoringRule r1 = createRule("a", true, 10.0);

            ScoreResult result = ScoringEngine.score(
                    RuleContext.create(), List.of(r1), AggregationStrategy.WEIGHTED_SUM);

            assertThat(result.totalScore()).isCloseTo(10.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("MAX strategy - MAX策略")
    class MaxStrategy {

        @Test
        @DisplayName("Should return maximum score - 应返回最高分")
        void shouldReturnMax() {
            ScoringRule r1 = createRule("a", true, 5.0);
            ScoringRule r2 = createRule("b", true, 25.0);
            ScoringRule r3 = createRule("c", true, 15.0);

            ScoreResult result = ScoringEngine.score(
                    RuleContext.create(), List.of(r1, r2, r3), AggregationStrategy.MAX);

            assertThat(result.totalScore()).isCloseTo(25.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("AVERAGE strategy - AVERAGE策略")
    class AverageStrategy {

        @Test
        @DisplayName("Should return average of matching scores - 应返回匹配分数的平均值")
        void shouldReturnAverage() {
            ScoringRule r1 = createRule("a", true, 10.0);
            ScoringRule r2 = createRule("b", true, 20.0);
            ScoringRule r3 = createRule("c", true, 30.0);

            ScoreResult result = ScoringEngine.score(
                    RuleContext.create(), List.of(r1, r2, r3), AggregationStrategy.AVERAGE);

            assertThat(result.totalScore()).isCloseTo(20.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("Empty and no-match cases - 空和无匹配情况")
    class EdgeCases {

        @Test
        @DisplayName("Should return 0 for empty rules - 空规则集应返回0")
        void shouldReturnZeroForEmpty() {
            ScoreResult result = ScoringEngine.score(
                    RuleContext.create(), List.of(), AggregationStrategy.SUM);

            assertThat(result.totalScore()).isEqualTo(0.0);
            assertThat(result.matchCount()).isZero();
        }

        @Test
        @DisplayName("Should return 0 when no rules match - 无规则匹配时应返回0")
        void shouldReturnZeroWhenNoneMatch() {
            ScoringRule r1 = createRule("a", false, 100.0);
            ScoringRule r2 = createRule("b", false, 200.0);

            ScoreResult result = ScoringEngine.score(
                    RuleContext.create(), List.of(r1, r2), AggregationStrategy.SUM);

            assertThat(result.totalScore()).isEqualTo(0.0);
            assertThat(result.matchCount()).isZero();
        }

        @Test
        @DisplayName("Should return 0 for AVERAGE with no matches - 无匹配时AVERAGE应返回0")
        void shouldReturnZeroForAverageNoMatches() {
            ScoreResult result = ScoringEngine.score(
                    RuleContext.create(), List.of(), AggregationStrategy.AVERAGE);

            assertThat(result.totalScore()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Strategy in result - 结果中的策略")
    class StrategyInResult {

        @Test
        @DisplayName("Should include strategy in result - 结果应包含策略")
        void shouldIncludeStrategy() {
            ScoreResult result = ScoringEngine.score(
                    RuleContext.create(), List.of(), AggregationStrategy.MAX);

            assertThat(result.strategy()).isEqualTo(AggregationStrategy.MAX);
        }
    }
}
