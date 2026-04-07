package cloud.opencode.base.rules.trace;

import cloud.opencode.base.rules.OpenRules;
import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.RuleEngine;
import cloud.opencode.base.rules.dsl.RuleBuilder;
import cloud.opencode.base.rules.engine.DefaultRuleEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * TracingRuleListener Tests
 * TracingRuleListener 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
@DisplayName("TracingRuleListener Tests | TracingRuleListener 测试")
class TracingRuleListenerTest {

    // Typed lambdas to avoid ambiguity
    private static final Predicate<RuleContext> ALWAYS_TRUE = ctx -> true;
    private static final Predicate<RuleContext> ALWAYS_FALSE = ctx -> false;
    private static final Consumer<RuleContext> NO_OP = ctx -> {};

    @Nested
    @DisplayName("Integration with DefaultRuleEngine | 与DefaultRuleEngine集成测试")
    class IntegrationTests {

        private static final Predicate<RuleContext> ALWAYS_TRUE = ctx -> true;
        private static final Predicate<RuleContext> ALWAYS_FALSE = ctx -> false;
        private static final Consumer<RuleContext> NO_OP = ctx -> {};

        @Test
        @DisplayName("traces fired, not-matched, and skipped rules | 追踪已触发、未匹配和已跳过的规则")
        void testTraceFiredNotMatchedSkipped() {
            Rule firedRule = RuleBuilder.rule("fires")
                    .when(ALWAYS_TRUE)
                    .then(NO_OP)
                    .priority(1)
                    .build();

            Rule notMatchedRule = RuleBuilder.rule("no-match")
                    .when(ALWAYS_FALSE)
                    .then(NO_OP)
                    .priority(2)
                    .build();

            Rule disabledRule = RuleBuilder.rule("disabled")
                    .when(ALWAYS_TRUE)
                    .then(NO_OP)
                    .enabled(false)
                    .priority(3)
                    .build();

            DefaultRuleEngine engine = new DefaultRuleEngine();
            engine.register(firedRule, notMatchedRule, disabledRule);

            TracingRuleListener tracer = new TracingRuleListener();
            engine.addListener(tracer);

            RuleContext context = RuleContext.create();
            engine.fire(context);

            ExecutionTrace trace = tracer.getTrace();

            assertThat(trace.ruleTraces()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(trace.totalDuration()).isNotNull();
            assertThat(trace.firedCount()).isGreaterThanOrEqualTo(1);

            // Check that the fired rule was traced
            assertThat(trace.getTrace("fires")).isPresent();
            assertThat(trace.getTrace("fires").get().hasFired()).isTrue();

            // Check not-matched rule
            assertThat(trace.getTrace("no-match")).isPresent();
            assertThat(trace.getTrace("no-match").get().hasFired()).isFalse();
            assertThat(trace.getTrace("no-match").get().conditionResult()).isFalse();
        }

        @Test
        @DisplayName("traces rule execution errors | 追踪规则执行错误")
        void testTraceRuleError() {
            Consumer<RuleContext> throwingAction = ctx -> {
                throw new RuntimeException("test error");
            };

            Rule failingRule = RuleBuilder.rule("failing")
                    .when(ALWAYS_TRUE)
                    .then(throwingAction)
                    .build();

            DefaultRuleEngine engine = new DefaultRuleEngine();
            engine.register(failingRule);

            TracingRuleListener tracer = new TracingRuleListener();
            engine.addListener(tracer);

            engine.fire(RuleContext.create());

            ExecutionTrace trace = tracer.getTrace();
            assertThat(trace.failedCount()).isEqualTo(1);
            assertThat(trace.failedRules()).hasSize(1);
            assertThat(trace.getTrace("failing")).isPresent();
            assertThat(trace.getTrace("failing").get().hasFailed()).isTrue();
            assertThat(trace.getTrace("failing").get().error().getMessage()).isEqualTo("test error");
        }
    }

    @Nested
    @DisplayName("fireAndTrace Tests | fireAndTrace 测试")
    class FireAndTraceTests {

        private static final Predicate<RuleContext> ALWAYS_TRUE = ctx -> true;
        private static final Consumer<RuleContext> NO_OP = ctx -> {};

        @Test
        @DisplayName("fireAndTrace returns valid trace | fireAndTrace返回有效的轨迹")
        void testFireAndTrace() {
            Rule rule = RuleBuilder.rule("trace-rule")
                    .when(ALWAYS_TRUE)
                    .then(NO_OP)
                    .build();

            RuleEngine engine = new DefaultRuleEngine();
            engine.register(rule);

            ExecutionTrace trace = engine.fireAndTrace(RuleContext.create());

            assertThat(trace).isNotNull();
            assertThat(trace.firedCount()).isEqualTo(1);
            assertThat(trace.getTrace("trace-rule")).isPresent();
            assertThat(trace.getTrace("trace-rule").get().hasFired()).isTrue();
        }
    }

    @Nested
    @DisplayName("Reset Tests | 重置测试")
    class ResetTests {

        private static final Predicate<RuleContext> ALWAYS_TRUE = ctx -> true;
        private static final Consumer<RuleContext> NO_OP = ctx -> {};

        @Test
        @DisplayName("reset clears state for reuse | reset清除状态以便重用")
        void testReset() {
            Rule rule = RuleBuilder.rule("r1")
                    .when(ALWAYS_TRUE)
                    .then(NO_OP)
                    .build();

            DefaultRuleEngine engine = new DefaultRuleEngine();
            engine.register(rule);

            TracingRuleListener tracer = new TracingRuleListener();
            engine.addListener(tracer);

            // First fire
            engine.fire(RuleContext.create());
            ExecutionTrace trace1 = tracer.getTrace();
            assertThat(trace1.firedCount()).isEqualTo(1);

            // Reset and fire again
            tracer.reset();
            tracer.onStart(RuleContext.create());
            engine.fire(RuleContext.create());
            ExecutionTrace trace2 = tracer.getTrace();
            assertThat(trace2.firedCount()).isEqualTo(1);
        }
    }
}
