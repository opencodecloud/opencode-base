package cloud.opencode.base.rules.trace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ExecutionTrace Tests
 * ExecutionTrace 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
@DisplayName("ExecutionTrace Tests | ExecutionTrace 测试")
class ExecutionTraceTest {

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() computes counts correctly | of()正确计算计数")
        void testOfComputesCounts() {
            List<RuleTrace> traces = List.of(
                    RuleTrace.fired("r1", Duration.ofMillis(10)),
                    RuleTrace.fired("r2", Duration.ofMillis(20)),
                    RuleTrace.notMatched("r3", Duration.ofMillis(5)),
                    RuleTrace.skipped("r4"),
                    RuleTrace.failed("r5", Duration.ofMillis(15), new RuntimeException("err"))
            );

            ExecutionTrace exec = ExecutionTrace.of(traces, Duration.ofMillis(100));

            assertThat(exec.ruleTraces()).hasSize(5);
            assertThat(exec.totalDuration()).isEqualTo(Duration.ofMillis(100));
            assertThat(exec.firedCount()).isEqualTo(2);
            assertThat(exec.skippedCount()).isEqualTo(1);
            assertThat(exec.failedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("of() with empty traces | of()使用空轨迹列表")
        void testOfEmpty() {
            ExecutionTrace exec = ExecutionTrace.of(List.of(), Duration.ofMillis(1));

            assertThat(exec.ruleTraces()).isEmpty();
            assertThat(exec.firedCount()).isZero();
            assertThat(exec.skippedCount()).isZero();
            assertThat(exec.failedCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Query Method Tests | 查询方法测试")
    class QueryMethodTests {

        private final ExecutionTrace trace = ExecutionTrace.of(List.of(
                RuleTrace.fired("fired-1", Duration.ofMillis(10)),
                RuleTrace.notMatched("miss-1", Duration.ofMillis(5)),
                RuleTrace.failed("fail-1", Duration.ofMillis(15), new RuntimeException("err")),
                RuleTrace.skipped("skip-1")
        ), Duration.ofMillis(50));

        @Test
        @DisplayName("firedRules() returns only fired rules | firedRules()只返回已触发的规则")
        void testFiredRules() {
            List<RuleTrace> fired = trace.firedRules();
            assertThat(fired).hasSize(1);
            assertThat(fired.getFirst().ruleName()).isEqualTo("fired-1");
        }

        @Test
        @DisplayName("failedRules() returns only failed rules | failedRules()只返回已失败的规则")
        void testFailedRules() {
            List<RuleTrace> failed = trace.failedRules();
            assertThat(failed).hasSize(1);
            assertThat(failed.getFirst().ruleName()).isEqualTo("fail-1");
        }

        @Test
        @DisplayName("getTrace() finds by name | getTrace()按名称查找")
        void testGetTraceByName() {
            assertThat(trace.getTrace("fired-1")).isPresent()
                    .get().extracting(RuleTrace::hasFired).isEqualTo(true);
            assertThat(trace.getTrace("miss-1")).isPresent()
                    .get().extracting(RuleTrace::hasFired).isEqualTo(false);
            assertThat(trace.getTrace("nonexistent")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation Tests | 验证测试")
    class ValidationTests {

        @Test
        @DisplayName("null ruleTraces throws NullPointerException | null ruleTraces抛出NullPointerException")
        void testNullRuleTraces() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ExecutionTrace.of(null, Duration.ZERO));
        }

        @Test
        @DisplayName("null totalDuration throws NullPointerException | null totalDuration抛出NullPointerException")
        void testNullTotalDuration() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ExecutionTrace.of(List.of(), null));
        }

        @Test
        @DisplayName("ruleTraces is immutable | ruleTraces是不可变的")
        void testImmutableTraces() {
            ExecutionTrace exec = ExecutionTrace.of(
                    List.of(RuleTrace.fired("r1", Duration.ZERO)), Duration.ZERO);
            assertThatThrownBy(() -> exec.ruleTraces().add(RuleTrace.skipped("x")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
