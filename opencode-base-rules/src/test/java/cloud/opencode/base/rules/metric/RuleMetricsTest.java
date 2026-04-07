package cloud.opencode.base.rules.metric;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RuleMetrics}.
 * {@link RuleMetrics} 的测试。
 */
@DisplayName("RuleMetrics - 规则指标")
class RuleMetricsTest {

    @Nested
    @DisplayName("Recording evaluations - 记录评估")
    class RecordEvaluations {

        @Test
        @DisplayName("Should track evaluation count and fire count - 应跟踪评估次数和触发次数")
        void shouldTrackEvaluationAndFire() {
            RuleMetrics metrics = new RuleMetrics();

            metrics.recordEvaluation("rule-a", 1000L, true);
            metrics.recordEvaluation("rule-a", 2000L, false);
            metrics.recordEvaluation("rule-a", 3000L, true);

            MetricsSnapshot snapshot = metrics.getSnapshot("rule-a");
            assertThat(snapshot).isNotNull();
            assertThat(snapshot.evaluationCount()).isEqualTo(3);
            assertThat(snapshot.fireCount()).isEqualTo(2);
            assertThat(snapshot.totalDurationNanos()).isEqualTo(6000L);
        }

        @Test
        @DisplayName("Should compute average duration - 应计算平均持续时间")
        void shouldComputeAvgDuration() {
            RuleMetrics metrics = new RuleMetrics();

            metrics.recordEvaluation("rule-b", 100L, false);
            metrics.recordEvaluation("rule-b", 200L, false);

            MetricsSnapshot snapshot = metrics.getSnapshot("rule-b");
            assertThat(snapshot.avgDurationNanos()).isEqualTo(150.0);
        }

        @Test
        @DisplayName("Should compute fire rate - 应计算触发率")
        void shouldComputeFireRate() {
            RuleMetrics metrics = new RuleMetrics();

            metrics.recordEvaluation("rule-c", 10L, true);
            metrics.recordEvaluation("rule-c", 10L, true);
            metrics.recordEvaluation("rule-c", 10L, false);
            metrics.recordEvaluation("rule-c", 10L, false);

            MetricsSnapshot snapshot = metrics.getSnapshot("rule-c");
            assertThat(snapshot.fireRate()).isEqualTo(0.5);
        }
    }

    @Nested
    @DisplayName("Recording failures - 记录失败")
    class RecordFailures {

        @Test
        @DisplayName("Should track fail count - 应跟踪失败次数")
        void shouldTrackFailCount() {
            RuleMetrics metrics = new RuleMetrics();

            metrics.recordFailure("rule-d");
            metrics.recordFailure("rule-d");

            MetricsSnapshot snapshot = metrics.getSnapshot("rule-d");
            assertThat(snapshot).isNotNull();
            assertThat(snapshot.failCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Snapshot for unknown rule - 未知规则的快照")
    class UnknownRule {

        @Test
        @DisplayName("Should return null for unknown rule - 应对未知规则返回null")
        void shouldReturnNullForUnknown() {
            RuleMetrics metrics = new RuleMetrics();
            assertThat(metrics.getSnapshot("nonexistent")).isNull();
        }
    }

    @Nested
    @DisplayName("Get all snapshots - 获取所有快照")
    class AllSnapshots {

        @Test
        @DisplayName("Should return snapshots for all rules - 应返回所有规则的快照")
        void shouldReturnAllSnapshots() {
            RuleMetrics metrics = new RuleMetrics();
            metrics.recordEvaluation("a", 10L, true);
            metrics.recordEvaluation("b", 20L, false);

            Map<String, MetricsSnapshot> all = metrics.getAllSnapshots();

            assertThat(all).hasSize(2);
            assertThat(all).containsKeys("a", "b");
            assertThat(all.get("a").fireCount()).isEqualTo(1);
            assertThat(all.get("b").fireCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Reset - 重置")
    class ResetMetrics {

        @Test
        @DisplayName("Should clear all metrics on reset - 重置应清除所有指标")
        void shouldClearAllOnReset() {
            RuleMetrics metrics = new RuleMetrics();
            metrics.recordEvaluation("rule-x", 10L, true);
            metrics.recordEvaluation("rule-y", 20L, false);

            metrics.reset();

            assertThat(metrics.getSnapshot("rule-x")).isNull();
            assertThat(metrics.getSnapshot("rule-y")).isNull();
            assertThat(metrics.getAllSnapshots()).isEmpty();
        }

        @Test
        @DisplayName("Should clear single rule on reset(name) - reset(name)应清除单个规则")
        void shouldClearSingleRule() {
            RuleMetrics metrics = new RuleMetrics();
            metrics.recordEvaluation("rule-x", 10L, true);
            metrics.recordEvaluation("rule-y", 20L, false);

            metrics.reset("rule-x");

            assertThat(metrics.getSnapshot("rule-x")).isNull();
            assertThat(metrics.getSnapshot("rule-y")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Zero-evaluation edge cases - 零评估边缘情况")
    class ZeroEvaluation {

        @Test
        @DisplayName("avgDurationNanos returns 0 when no evaluations - 没有评估时avgDurationNanos返回0")
        void avgDurationZero() {
            MetricsSnapshot snapshot = new MetricsSnapshot("rule", 0, 0, 0, 0);
            assertThat(snapshot.avgDurationNanos()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("fireRate returns 0 when no evaluations - 没有评估时fireRate返回0")
        void fireRateZero() {
            MetricsSnapshot snapshot = new MetricsSnapshot("rule", 0, 0, 0, 0);
            assertThat(snapshot.fireRate()).isEqualTo(0.0);
        }
    }
}
