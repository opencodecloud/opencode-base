package cloud.opencode.base.rules.explain;

import cloud.opencode.base.rules.trace.ExecutionTrace;
import cloud.opencode.base.rules.trace.RuleTrace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleExplainer Tests
 * RuleExplainer 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
@DisplayName("RuleExplainer Tests | RuleExplainer 测试")
class RuleExplainerTest {

    @Nested
    @DisplayName("Explain Tests | 解释测试")
    class ExplainTests {

        @Test
        @DisplayName("explains fired rule | 解释已触发的规则")
        void testExplainFired() {
            ExecutionTrace trace = ExecutionTrace.of(
                    List.of(RuleTrace.fired("discount", Duration.ofMillis(42))),
                    Duration.ofMillis(50)
            );

            Explanation explanation = RuleExplainer.explain(trace);

            assertThat(explanation.details()).hasSize(1);
            assertThat(explanation.details().getFirst().fired()).isTrue();
            assertThat(explanation.details().getFirst().reason())
                    .contains("discount")
                    .contains("fired")
                    .contains("42ms");
            assertThat(explanation.firedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("explains not-matched rule | 解释未匹配的规则")
        void testExplainNotMatched() {
            ExecutionTrace trace = ExecutionTrace.of(
                    List.of(RuleTrace.notMatched("check", Duration.ofMillis(5))),
                    Duration.ofMillis(10)
            );

            Explanation explanation = RuleExplainer.explain(trace);

            assertThat(explanation.details().getFirst().fired()).isFalse();
            assertThat(explanation.details().getFirst().reason())
                    .contains("did not fire")
                    .contains("condition not met");
        }

        @Test
        @DisplayName("explains skipped rule | 解释已跳过的规则")
        void testExplainSkipped() {
            ExecutionTrace trace = ExecutionTrace.of(
                    List.of(RuleTrace.skipped("off")),
                    Duration.ofMillis(1)
            );

            Explanation explanation = RuleExplainer.explain(trace);

            assertThat(explanation.details().getFirst().fired()).isFalse();
            assertThat(explanation.details().getFirst().reason())
                    .contains("skipped")
                    .contains("disabled");
        }

        @Test
        @DisplayName("explains failed rule | 解释已失败的规则")
        void testExplainFailed() {
            ExecutionTrace trace = ExecutionTrace.of(
                    List.of(RuleTrace.failed("broken", Duration.ofMillis(20),
                            new RuntimeException("null pointer"))),
                    Duration.ofMillis(25)
            );

            Explanation explanation = RuleExplainer.explain(trace);

            assertThat(explanation.details().getFirst().fired()).isFalse();
            assertThat(explanation.details().getFirst().reason())
                    .contains("failed")
                    .contains("null pointer");
        }

        @Test
        @DisplayName("explains mixed rules with correct summary | 解释混合规则并生成正确摘要")
        void testExplainMixed() {
            ExecutionTrace trace = ExecutionTrace.of(List.of(
                    RuleTrace.fired("r1", Duration.ofMillis(10)),
                    RuleTrace.notMatched("r2", Duration.ofMillis(5)),
                    RuleTrace.skipped("r3"),
                    RuleTrace.failed("r4", Duration.ofMillis(15), new RuntimeException("err"))
            ), Duration.ofMillis(100));

            Explanation explanation = RuleExplainer.explain(trace);

            assertThat(explanation.summary())
                    .contains("4 rules")
                    .contains("1 fired")
                    .contains("1 skipped")
                    .contains("1 failed")
                    .contains("100ms");
            assertThat(explanation.details()).hasSize(4);
            assertThat(explanation.firedCount()).isEqualTo(1);
            assertThat(explanation.totalCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("null trace throws NullPointerException | null trace抛出NullPointerException")
        void testNullTrace() {
            assertThatNullPointerException()
                    .isThrownBy(() -> RuleExplainer.explain(null));
        }
    }
}
