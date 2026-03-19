package cloud.opencode.base.rules.listener;

import cloud.opencode.base.rules.OpenRules;
import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleListener Interface Tests
 * RuleListener 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("RuleListener Interface Tests | RuleListener 接口测试")
class RuleListenerTest {

    // Helper typed lambdas to avoid ambiguity between Predicate/Condition and Consumer/Action
    private static final Predicate<RuleContext> ALWAYS_TRUE = ctx -> true;
    private static final Consumer<RuleContext> NO_OP = ctx -> {};

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("all default methods do nothing | 所有默认方法什么都不做")
        void testDefaultMethodsDoNothing() {
            RuleListener listener = new RuleListener() {};

            Rule rule = OpenRules.rule("test").when(ALWAYS_TRUE).then(NO_OP).build();
            RuleContext context = OpenRules.context();

            // Should not throw
            listener.beforeEvaluate(rule, context);
            listener.afterEvaluate(rule, context, true);
            listener.beforeExecute(rule, context);
            listener.afterExecute(rule, context);
            listener.onFailure(rule, context, new RuntimeException("test"));
            listener.onStart(context);
            listener.onFinish(context, 1, 100L);
        }
    }

    @Nested
    @DisplayName("BeforeEvaluate Tests | beforeEvaluate 测试")
    class BeforeEvaluateTests {

        @Test
        @DisplayName("beforeEvaluate is called | beforeEvaluate 被调用")
        void testBeforeEvaluateCalled() {
            AtomicBoolean called = new AtomicBoolean(false);

            RuleListener listener = new RuleListener() {
                @Override
                public void beforeEvaluate(Rule rule, RuleContext context) {
                    called.set(true);
                }
            };

            Rule rule = OpenRules.rule("test").when(ALWAYS_TRUE).then(NO_OP).build();
            RuleContext context = OpenRules.context();

            listener.beforeEvaluate(rule, context);

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("beforeEvaluate receives rule | beforeEvaluate 接收规则")
        void testBeforeEvaluateReceivesRule() {
            final String[] receivedRuleName = {null};

            RuleListener listener = new RuleListener() {
                @Override
                public void beforeEvaluate(Rule rule, RuleContext context) {
                    receivedRuleName[0] = rule.getName();
                }
            };

            Rule rule = OpenRules.rule("my-rule").when(ALWAYS_TRUE).then(NO_OP).build();
            RuleContext context = OpenRules.context();

            listener.beforeEvaluate(rule, context);

            assertThat(receivedRuleName[0]).isEqualTo("my-rule");
        }
    }

    @Nested
    @DisplayName("AfterEvaluate Tests | afterEvaluate 测试")
    class AfterEvaluateTests {

        @Test
        @DisplayName("afterEvaluate receives satisfied result | afterEvaluate 接收满足结果")
        void testAfterEvaluateSatisfied() {
            final boolean[] receivedSatisfied = {false};

            RuleListener listener = new RuleListener() {
                @Override
                public void afterEvaluate(Rule rule, RuleContext context, boolean satisfied) {
                    receivedSatisfied[0] = satisfied;
                }
            };

            Rule rule = OpenRules.rule("test").when(ALWAYS_TRUE).then(NO_OP).build();
            RuleContext context = OpenRules.context();

            listener.afterEvaluate(rule, context, true);
            assertThat(receivedSatisfied[0]).isTrue();

            listener.afterEvaluate(rule, context, false);
            assertThat(receivedSatisfied[0]).isFalse();
        }
    }

    @Nested
    @DisplayName("Execution Callbacks Tests | 执行回调测试")
    class ExecutionCallbacksTests {

        @Test
        @DisplayName("beforeExecute and afterExecute are called in order | beforeExecute 和 afterExecute 按顺序调用")
        void testExecutionCallbacksOrder() {
            StringBuilder order = new StringBuilder();

            RuleListener listener = new RuleListener() {
                @Override
                public void beforeExecute(Rule rule, RuleContext context) {
                    order.append("before-");
                }

                @Override
                public void afterExecute(Rule rule, RuleContext context) {
                    order.append("after");
                }
            };

            Rule rule = OpenRules.rule("test").when(ALWAYS_TRUE).then(NO_OP).build();
            RuleContext context = OpenRules.context();

            listener.beforeExecute(rule, context);
            listener.afterExecute(rule, context);

            assertThat(order.toString()).isEqualTo("before-after");
        }
    }

    @Nested
    @DisplayName("OnFailure Tests | onFailure 测试")
    class OnFailureTests {

        @Test
        @DisplayName("onFailure receives exception | onFailure 接收异常")
        void testOnFailureReceivesException() {
            final Exception[] receivedException = {null};

            RuleListener listener = new RuleListener() {
                @Override
                public void onFailure(Rule rule, RuleContext context, Exception exception) {
                    receivedException[0] = exception;
                }
            };

            Rule rule = OpenRules.rule("test").when(ALWAYS_TRUE).then(NO_OP).build();
            RuleContext context = OpenRules.context();
            RuntimeException ex = new RuntimeException("Test error");

            listener.onFailure(rule, context, ex);

            assertThat(receivedException[0]).isSameAs(ex);
            assertThat(receivedException[0].getMessage()).isEqualTo("Test error");
        }
    }

    @Nested
    @DisplayName("Lifecycle Callbacks Tests | 生命周期回调测试")
    class LifecycleCallbacksTests {

        @Test
        @DisplayName("onStart is called | onStart 被调用")
        void testOnStart() {
            AtomicBoolean called = new AtomicBoolean(false);

            RuleListener listener = new RuleListener() {
                @Override
                public void onStart(RuleContext context) {
                    called.set(true);
                }
            };

            RuleContext context = OpenRules.context();
            listener.onStart(context);

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("onFinish receives statistics | onFinish 接收统计信息")
        void testOnFinishReceivesStats() {
            final int[] receivedFiredCount = {-1};
            final long[] receivedElapsed = {-1};

            RuleListener listener = new RuleListener() {
                @Override
                public void onFinish(RuleContext context, int firedCount, long elapsedMillis) {
                    receivedFiredCount[0] = firedCount;
                    receivedElapsed[0] = elapsedMillis;
                }
            };

            RuleContext context = OpenRules.context();
            listener.onFinish(context, 5, 150L);

            assertThat(receivedFiredCount[0]).isEqualTo(5);
            assertThat(receivedElapsed[0]).isEqualTo(150L);
        }
    }

    @Nested
    @DisplayName("Integration Tests | 集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("listener is called during engine fire | 引擎触发时调用监听器")
        void testListenerIntegration() {
            AtomicInteger beforeCount = new AtomicInteger(0);
            AtomicInteger afterCount = new AtomicInteger(0);

            RuleListener listener = new RuleListener() {
                @Override
                public void beforeExecute(Rule rule, RuleContext context) {
                    beforeCount.incrementAndGet();
                }

                @Override
                public void afterExecute(Rule rule, RuleContext context) {
                    afterCount.incrementAndGet();
                }
            };

            Rule rule = OpenRules.rule("test").when(ALWAYS_TRUE).then(NO_OP).build();

            OpenRules.engine()
                    .register(rule)
                    .addListener(listener)
                    .build()
                    .fire(OpenRules.context());

            assertThat(beforeCount.get()).isEqualTo(1);
            assertThat(afterCount.get()).isEqualTo(1);
        }
    }
}
