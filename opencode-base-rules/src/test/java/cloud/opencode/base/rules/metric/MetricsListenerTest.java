package cloud.opencode.base.rules.metric;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.dsl.RuleBuilder;
import cloud.opencode.base.rules.engine.DefaultRuleEngine;
import cloud.opencode.base.rules.model.Action;
import cloud.opencode.base.rules.model.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MetricsListener}.
 * {@link MetricsListener} 的测试。
 */
@DisplayName("MetricsListener - 指标监听器")
class MetricsListenerTest {

    @Nested
    @DisplayName("Integration with DefaultRuleEngine - 与DefaultRuleEngine集成")
    class EngineIntegration {

        @Test
        @DisplayName("Should collect metrics for fired rules - 应收集已触发规则的指标")
        void shouldCollectMetricsForFiredRules() {
            MetricsListener listener = new MetricsListener();
            DefaultRuleEngine engine = new DefaultRuleEngine();
            engine.addListener(listener);

            Rule alwaysTrue = RuleBuilder.rule("always-true")
                    .when((Condition) _ -> true)
                    .then((Action) ctx -> ctx.setResult("fired", true))
                    .build();
            Rule alwaysFalse = RuleBuilder.rule("always-false")
                    .when((Condition) _ -> false)
                    .then((Action) _ -> {})
                    .build();

            engine.register(alwaysTrue, alwaysFalse);
            engine.fire(RuleContext.create());

            RuleMetrics metrics = listener.getMetrics();

            MetricsSnapshot trueSnap = metrics.getSnapshot("always-true");
            assertThat(trueSnap).isNotNull();
            assertThat(trueSnap.evaluationCount()).isEqualTo(1);
            assertThat(trueSnap.fireCount()).isEqualTo(1);
            assertThat(trueSnap.totalDurationNanos()).isGreaterThanOrEqualTo(0);

            MetricsSnapshot falseSnap = metrics.getSnapshot("always-false");
            assertThat(falseSnap).isNotNull();
            assertThat(falseSnap.evaluationCount()).isEqualTo(1);
            assertThat(falseSnap.fireCount()).isZero();
        }

        @Test
        @DisplayName("Should track multiple firings - 应跟踪多次触发")
        void shouldTrackMultipleFirings() {
            MetricsListener listener = new MetricsListener();
            DefaultRuleEngine engine = new DefaultRuleEngine();
            engine.addListener(listener);

            Rule rule = RuleBuilder.rule("counter")
                    .when((Condition) _ -> true)
                    .then((Action) _ -> {})
                    .build();

            engine.register(rule);
            engine.fire(RuleContext.create());
            engine.fire(RuleContext.create());
            engine.fire(RuleContext.create());

            MetricsSnapshot snapshot = listener.getMetrics().getSnapshot("counter");
            assertThat(snapshot.evaluationCount()).isEqualTo(3);
            assertThat(snapshot.fireCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should record failures - 应记录失败")
        void shouldRecordFailures() {
            MetricsListener listener = new MetricsListener();
            DefaultRuleEngine engine = new DefaultRuleEngine();
            engine.addListener(listener);

            Rule failingRule = RuleBuilder.rule("failing")
                    .when((Condition) _ -> true)
                    .then((Action) _ -> { throw new RuntimeException("boom"); })
                    .build();

            engine.register(failingRule);
            engine.fire(RuleContext.create());

            MetricsSnapshot snapshot = listener.getMetrics().getSnapshot("failing");
            assertThat(snapshot).isNotNull();
            assertThat(snapshot.evaluationCount()).isEqualTo(1);
            assertThat(snapshot.fireCount()).isEqualTo(1);
            assertThat(snapshot.failCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Constructor - 构造器")
    class Constructors {

        @Test
        @DisplayName("Default constructor creates internal metrics - 默认构造器创建内部指标")
        void defaultConstructorCreatesMetrics() {
            MetricsListener listener = new MetricsListener();
            assertThat(listener.getMetrics()).isNotNull();
        }

        @Test
        @DisplayName("Custom metrics constructor uses provided instance - 自定义指标构造器使用提供的实例")
        void customMetricsConstructor() {
            RuleMetrics metrics = new RuleMetrics();
            MetricsListener listener = new MetricsListener(metrics);
            assertThat(listener.getMetrics()).isSameAs(metrics);
        }
    }
}
