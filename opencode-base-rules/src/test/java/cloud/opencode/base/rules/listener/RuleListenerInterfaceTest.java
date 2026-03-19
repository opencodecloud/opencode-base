package cloud.opencode.base.rules.listener;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleListener Interface Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("RuleListener Interface Tests")
class RuleListenerInterfaceTest {

    @Nested
    @DisplayName("Default Method Tests")
    class DefaultMethodTests {

        @Test
        @DisplayName("beforeEvaluate() default should do nothing")
        void beforeEvaluateDefaultShouldDoNothing() {
            RuleListener listener = new RuleListener() {};
            Rule rule = createRule("test");
            RuleContext context = RuleContext.create();

            // Should not throw
            assertThatCode(() -> listener.beforeEvaluate(rule, context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("afterEvaluate() default should do nothing")
        void afterEvaluateDefaultShouldDoNothing() {
            RuleListener listener = new RuleListener() {};
            Rule rule = createRule("test");
            RuleContext context = RuleContext.create();

            assertThatCode(() -> listener.afterEvaluate(rule, context, true))
                    .doesNotThrowAnyException();
            assertThatCode(() -> listener.afterEvaluate(rule, context, false))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("beforeExecute() default should do nothing")
        void beforeExecuteDefaultShouldDoNothing() {
            RuleListener listener = new RuleListener() {};
            Rule rule = createRule("test");
            RuleContext context = RuleContext.create();

            assertThatCode(() -> listener.beforeExecute(rule, context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("afterExecute() default should do nothing")
        void afterExecuteDefaultShouldDoNothing() {
            RuleListener listener = new RuleListener() {};
            Rule rule = createRule("test");
            RuleContext context = RuleContext.create();

            assertThatCode(() -> listener.afterExecute(rule, context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onFailure() default should do nothing")
        void onFailureDefaultShouldDoNothing() {
            RuleListener listener = new RuleListener() {};
            Rule rule = createRule("test");
            RuleContext context = RuleContext.create();
            Exception exception = new RuntimeException("Test failure");

            assertThatCode(() -> listener.onFailure(rule, context, exception))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onStart() default should do nothing")
        void onStartDefaultShouldDoNothing() {
            RuleListener listener = new RuleListener() {};
            RuleContext context = RuleContext.create();

            assertThatCode(() -> listener.onStart(context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onFinish() default should do nothing")
        void onFinishDefaultShouldDoNothing() {
            RuleListener listener = new RuleListener() {};
            RuleContext context = RuleContext.create();

            assertThatCode(() -> listener.onFinish(context, 5, 100L))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Selective Override Tests")
    class SelectiveOverrideTests {

        @Test
        @DisplayName("Can override only specific methods")
        void canOverrideOnlySpecificMethods() {
            final boolean[] called = {false};

            RuleListener listener = new RuleListener() {
                @Override
                public void onStart(RuleContext context) {
                    called[0] = true;
                }
            };

            listener.onStart(RuleContext.create());
            assertThat(called[0]).isTrue();

            // Other methods should still work
            assertThatCode(() -> listener.beforeEvaluate(createRule("test"), RuleContext.create()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Can override all methods")
        void canOverrideAllMethods() {
            final int[] callCount = {0};

            RuleListener listener = new RuleListener() {
                @Override
                public void beforeEvaluate(Rule rule, RuleContext context) {
                    callCount[0]++;
                }
                @Override
                public void afterEvaluate(Rule rule, RuleContext context, boolean satisfied) {
                    callCount[0]++;
                }
                @Override
                public void beforeExecute(Rule rule, RuleContext context) {
                    callCount[0]++;
                }
                @Override
                public void afterExecute(Rule rule, RuleContext context) {
                    callCount[0]++;
                }
                @Override
                public void onFailure(Rule rule, RuleContext context, Exception exception) {
                    callCount[0]++;
                }
                @Override
                public void onStart(RuleContext context) {
                    callCount[0]++;
                }
                @Override
                public void onFinish(RuleContext context, int firedCount, long elapsedMillis) {
                    callCount[0]++;
                }
            };

            Rule rule = createRule("test");
            RuleContext context = RuleContext.create();

            listener.beforeEvaluate(rule, context);
            listener.afterEvaluate(rule, context, true);
            listener.beforeExecute(rule, context);
            listener.afterExecute(rule, context);
            listener.onFailure(rule, context, new RuntimeException());
            listener.onStart(context);
            listener.onFinish(context, 1, 10L);

            assertThat(callCount[0]).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("Lambda/Anonymous Class Tests")
    class LambdaAnonymousClassTests {

        @Test
        @DisplayName("Empty listener implementation should work")
        void emptyListenerImplementationShouldWork() {
            RuleListener listener = new RuleListener() {};
            assertThat(listener).isNotNull();
        }
    }

    private Rule createRule(String name) {
        return new Rule() {
            @Override
            public String getName() { return name; }
            @Override
            public String getDescription() { return null; }
            @Override
            public int getPriority() { return Rule.DEFAULT_PRIORITY; }
            @Override
            public boolean evaluate(RuleContext context) { return true; }
            @Override
            public void execute(RuleContext context) {}
        };
    }
}
