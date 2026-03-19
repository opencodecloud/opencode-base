package cloud.opencode.base.rules.listener;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LoggingRuleListener Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("LoggingRuleListener Tests")
class LoggingRuleListenerTest {

    private LoggingRuleListener listener;
    private RuleContext context;
    private Rule mockRule;

    @BeforeEach
    void setUp() {
        listener = new LoggingRuleListener();
        context = RuleContext.create();
        mockRule = new Rule() {
            @Override
            public String getName() { return "test-rule"; }
            @Override
            public String getDescription() { return "Test description"; }
            @Override
            public int getPriority() { return 100; }
            @Override
            public String getGroup() { return "test-group"; }
            @Override
            public boolean isEnabled() { return true; }
            @Override
            public boolean evaluate(RuleContext context) { return true; }
            @Override
            public void execute(RuleContext context) {}
        };
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should create listener")
        void defaultConstructorShouldCreateListener() {
            LoggingRuleListener listener = new LoggingRuleListener();
            assertThat(listener).isNotNull();
        }
    }

    @Nested
    @DisplayName("Lifecycle Method Tests")
    class LifecycleMethodTests {

        @Test
        @DisplayName("onStart() should not throw")
        void onStartShouldNotThrow() {
            assertThatCode(() -> listener.onStart(context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onFinish() should not throw")
        void onFinishShouldNotThrow() {
            assertThatCode(() -> listener.onFinish(context, 5, 100))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onFinish() should handle zero fired count")
        void onFinishShouldHandleZeroFiredCount() {
            assertThatCode(() -> listener.onFinish(context, 0, 50))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Evaluation Event Tests")
    class EvaluationEventTests {

        @Test
        @DisplayName("beforeEvaluate() should not throw")
        void beforeEvaluateShouldNotThrow() {
            assertThatCode(() -> listener.beforeEvaluate(mockRule, context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("afterEvaluate() with true result should not throw")
        void afterEvaluateWithTrueResultShouldNotThrow() {
            assertThatCode(() -> listener.afterEvaluate(mockRule, context, true))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("afterEvaluate() with false result should not throw")
        void afterEvaluateWithFalseResultShouldNotThrow() {
            assertThatCode(() -> listener.afterEvaluate(mockRule, context, false))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Execution Event Tests")
    class ExecutionEventTests {

        @Test
        @DisplayName("beforeExecute() should not throw")
        void beforeExecuteShouldNotThrow() {
            assertThatCode(() -> listener.beforeExecute(mockRule, context))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("afterExecute() should not throw")
        void afterExecuteShouldNotThrow() {
            assertThatCode(() -> listener.afterExecute(mockRule, context))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Failure Event Tests")
    class FailureEventTests {

        @Test
        @DisplayName("onFailure() should not throw")
        void onFailureShouldNotThrow() {
            Exception testException = new RuntimeException("Test error");
            assertThatCode(() -> listener.onFailure(mockRule, context, testException))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onFailure() should handle null exception")
        void onFailureShouldHandleNullException() {
            assertThatCode(() -> listener.onFailure(mockRule, context, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("onFailure() should handle nested exceptions")
        void onFailureShouldHandleNestedExceptions() {
            Exception nested = new RuntimeException("Nested");
            Exception outer = new RuntimeException("Outer", nested);
            assertThatCode(() -> listener.onFailure(mockRule, context, outer))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("All methods should work with null rule name")
        void allMethodsShouldWorkWithNullRuleName() {
            Rule ruleWithNullName = new Rule() {
                @Override
                public String getName() { return null; }
                @Override
                public String getDescription() { return null; }
                @Override
                public int getPriority() { return 100; }
                @Override
                public String getGroup() { return null; }
                @Override
                public boolean isEnabled() { return true; }
                @Override
                public boolean evaluate(RuleContext context) { return true; }
                @Override
                public void execute(RuleContext context) {}
            };

            assertThatCode(() -> {
                listener.beforeEvaluate(ruleWithNullName, context);
                listener.afterEvaluate(ruleWithNullName, context, true);
                listener.beforeExecute(ruleWithNullName, context);
                listener.afterExecute(ruleWithNullName, context);
                listener.onFailure(ruleWithNullName, context, new Exception());
            }).doesNotThrowAnyException();
        }
    }
}
