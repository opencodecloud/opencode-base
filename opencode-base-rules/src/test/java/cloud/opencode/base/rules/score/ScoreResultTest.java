package cloud.opencode.base.rules.score;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link ScoreResult}.
 * {@link ScoreResult} 的测试。
 */
@DisplayName("ScoreResult - 评分结果")
class ScoreResultTest {

    @Nested
    @DisplayName("matchCount - 匹配数量")
    class MatchCount {

        @Test
        @DisplayName("Should return number of rule scores - 应返回规则分数数量")
        void shouldReturnRuleScoreCount() {
            ScoreResult result = new ScoreResult(30.0,
                    Map.of("a", 10.0, "b", 20.0), AggregationStrategy.SUM);

            assertThat(result.matchCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return 0 for empty scores - 空分数应返回0")
        void shouldReturnZeroForEmpty() {
            ScoreResult result = new ScoreResult(0.0,
                    Map.of(), AggregationStrategy.SUM);

            assertThat(result.matchCount()).isZero();
        }
    }

    @Nested
    @DisplayName("topScoringRule - 最高分规则")
    class TopScoringRule {

        @Test
        @DisplayName("Should return rule with highest score - 应返回最高分规则")
        void shouldReturnTopRule() {
            ScoreResult result = new ScoreResult(60.0,
                    Map.of("low", 10.0, "high", 50.0, "mid", 30.0),
                    AggregationStrategy.SUM);

            Optional<String> top = result.topScoringRule();
            assertThat(top).isPresent().hasValue("high");
        }

        @Test
        @DisplayName("Should return empty for no scores - 无分数应返回空")
        void shouldReturnEmptyForNoScores() {
            ScoreResult result = new ScoreResult(0.0,
                    Map.of(), AggregationStrategy.SUM);

            assertThat(result.topScoringRule()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Immutability - 不可变性")
    class Immutability {

        @Test
        @DisplayName("ruleScores map is immutable - 规则分数映射不可变")
        void ruleScoresMapIsImmutable() {
            ScoreResult result = new ScoreResult(10.0,
                    Map.of("a", 10.0), AggregationStrategy.SUM);

            assertThrows(UnsupportedOperationException.class,
                    () -> result.ruleScores().put("b", 20.0));
        }
    }

    @Nested
    @DisplayName("Record properties - 记录属性")
    class RecordProperties {

        @Test
        @DisplayName("Should expose all fields - 应暴露所有字段")
        void shouldExposeAllFields() {
            ScoreResult result = new ScoreResult(42.0,
                    Map.of("rule1", 42.0), AggregationStrategy.WEIGHTED_SUM);

            assertThat(result.totalScore()).isEqualTo(42.0);
            assertThat(result.ruleScores()).hasSize(1);
            assertThat(result.strategy()).isEqualTo(AggregationStrategy.WEIGHTED_SUM);
        }
    }
}
