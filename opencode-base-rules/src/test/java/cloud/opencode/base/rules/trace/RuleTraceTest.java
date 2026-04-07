package cloud.opencode.base.rules.trace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleTrace Tests
 * RuleTrace 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
@DisplayName("RuleTrace Tests | RuleTrace 测试")
class RuleTraceTest {

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("fired() creates a fired trace | fired()创建已触发的轨迹")
        void testFired() {
            Duration duration = Duration.ofMillis(50);
            RuleTrace trace = RuleTrace.fired("rule-1", duration);

            assertThat(trace.ruleName()).isEqualTo("rule-1");
            assertThat(trace.conditionResult()).isTrue();
            assertThat(trace.executed()).isTrue();
            assertThat(trace.duration()).isEqualTo(duration);
            assertThat(trace.error()).isNull();
            assertThat(trace.skipped()).isFalse();
            assertThat(trace.hasFired()).isTrue();
            assertThat(trace.hasFailed()).isFalse();
        }

        @Test
        @DisplayName("notMatched() creates a not-matched trace | notMatched()创建未匹配的轨迹")
        void testNotMatched() {
            Duration duration = Duration.ofMillis(10);
            RuleTrace trace = RuleTrace.notMatched("rule-2", duration);

            assertThat(trace.ruleName()).isEqualTo("rule-2");
            assertThat(trace.conditionResult()).isFalse();
            assertThat(trace.executed()).isFalse();
            assertThat(trace.duration()).isEqualTo(duration);
            assertThat(trace.error()).isNull();
            assertThat(trace.skipped()).isFalse();
            assertThat(trace.hasFired()).isFalse();
            assertThat(trace.hasFailed()).isFalse();
        }

        @Test
        @DisplayName("skipped() creates a skipped trace | skipped()创建已跳过的轨迹")
        void testSkipped() {
            RuleTrace trace = RuleTrace.skipped("rule-3");

            assertThat(trace.ruleName()).isEqualTo("rule-3");
            assertThat(trace.conditionResult()).isFalse();
            assertThat(trace.executed()).isFalse();
            assertThat(trace.duration()).isEqualTo(Duration.ZERO);
            assertThat(trace.error()).isNull();
            assertThat(trace.skipped()).isTrue();
            assertThat(trace.hasFired()).isFalse();
            assertThat(trace.hasFailed()).isFalse();
        }

        @Test
        @DisplayName("failed() creates a failed trace | failed()创建已失败的轨迹")
        void testFailed() {
            Duration duration = Duration.ofMillis(30);
            RuntimeException error = new RuntimeException("boom");
            RuleTrace trace = RuleTrace.failed("rule-4", duration, error);

            assertThat(trace.ruleName()).isEqualTo("rule-4");
            assertThat(trace.conditionResult()).isTrue();
            assertThat(trace.executed()).isFalse();
            assertThat(trace.duration()).isEqualTo(duration);
            assertThat(trace.error()).isSameAs(error);
            assertThat(trace.skipped()).isFalse();
            assertThat(trace.hasFired()).isFalse();
            assertThat(trace.hasFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Validation Tests | 验证测试")
    class ValidationTests {

        @Test
        @DisplayName("null ruleName throws NullPointerException | null ruleName抛出NullPointerException")
        void testNullRuleName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> RuleTrace.fired(null, Duration.ZERO))
                    .withMessageContaining("ruleName");
        }

        @Test
        @DisplayName("null duration throws NullPointerException | null duration抛出NullPointerException")
        void testNullDuration() {
            assertThatNullPointerException()
                    .isThrownBy(() -> RuleTrace.fired("rule", null))
                    .withMessageContaining("duration");
        }
    }

    @Nested
    @DisplayName("Query Method Tests | 查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("hasFired is true only when condition matched and executed | hasFired仅在条件匹配且已执行时为true")
        void testHasFired() {
            assertThat(RuleTrace.fired("r", Duration.ZERO).hasFired()).isTrue();
            assertThat(RuleTrace.notMatched("r", Duration.ZERO).hasFired()).isFalse();
            assertThat(RuleTrace.skipped("r").hasFired()).isFalse();
            assertThat(RuleTrace.failed("r", Duration.ZERO, new RuntimeException()).hasFired()).isFalse();
        }

        @Test
        @DisplayName("hasFailed is true only when error is present | hasFailed仅在存在错误时为true")
        void testHasFailed() {
            assertThat(RuleTrace.fired("r", Duration.ZERO).hasFailed()).isFalse();
            assertThat(RuleTrace.failed("r", Duration.ZERO, new RuntimeException()).hasFailed()).isTrue();
        }
    }
}
