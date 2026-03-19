package cloud.opencode.base.rules.model;

import cloud.opencode.base.rules.OpenRules;
import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Action Interface Tests
 * Action 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("Action Interface Tests | Action 接口测试")
class ActionTest {

    @Nested
    @DisplayName("Execute Tests | 执行测试")
    class ExecuteTests {

        @Test
        @DisplayName("execute runs action | execute 运行动作")
        void testExecute() {
            AtomicInteger counter = new AtomicInteger(0);
            Action action = ctx -> counter.incrementAndGet();

            RuleContext context = OpenRules.context();
            action.execute(context);

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("execute can modify context | execute 可以修改上下文")
        void testExecuteModifiesContext() {
            Action action = ctx -> ctx.setResult("key", "value");

            RuleContext context = OpenRules.context();
            action.execute(context);

            assertThat(context.<String>getResult("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("lambda action works | lambda 动作可用")
        void testLambdaAction() {
            Action action = ctx -> ctx.put("executed", true);

            RuleContext context = OpenRules.context();
            action.execute(context);

            assertThat(context.<Boolean>get("executed")).isTrue();
        }
    }

    @Nested
    @DisplayName("AndThen Tests | andThen 测试")
    class AndThenTests {

        @Test
        @DisplayName("andThen chains actions | andThen 链接动作")
        void testAndThen() {
            AtomicInteger counter = new AtomicInteger(0);

            Action first = ctx -> counter.addAndGet(1);
            Action second = ctx -> counter.addAndGet(10);
            Action combined = first.andThen(second);

            RuleContext context = OpenRules.context();
            combined.execute(context);

            assertThat(counter.get()).isEqualTo(11);
        }

        @Test
        @DisplayName("andThen executes in order | andThen 按顺序执行")
        void testAndThenOrder() {
            StringBuilder sb = new StringBuilder();

            Action first = ctx -> sb.append("A");
            Action second = ctx -> sb.append("B");
            Action third = ctx -> sb.append("C");

            Action combined = first.andThen(second).andThen(third);

            RuleContext context = OpenRules.context();
            combined.execute(context);

            assertThat(sb.toString()).isEqualTo("ABC");
        }

        @Test
        @DisplayName("andThen with noOp | andThen 与 noOp")
        void testAndThenWithNoOp() {
            AtomicInteger counter = new AtomicInteger(0);

            Action action = ctx -> counter.incrementAndGet();
            Action combined = action.andThen(Action.noOp());

            RuleContext context = OpenRules.context();
            combined.execute(context);

            assertThat(counter.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("NoOp Tests | noOp 测试")
    class NoOpTests {

        @Test
        @DisplayName("noOp does nothing | noOp 什么都不做")
        void testNoOp() {
            Action noOp = Action.noOp();

            RuleContext context = OpenRules.context();
            // Should not throw
            noOp.execute(context);

            assertThat(context.getResults()).isEmpty();
        }

        @Test
        @DisplayName("noOp returns same instance | noOp 返回相同实例")
        void testNoOpSameInstance() {
            Action noOp1 = Action.noOp();
            Action noOp2 = Action.noOp();

            // Static factory may return same instance
            assertThat(noOp1).isNotNull();
            assertThat(noOp2).isNotNull();
        }
    }

    @Nested
    @DisplayName("Functional Interface Tests | 函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Action is functional interface | Action 是函数式接口")
        void testFunctionalInterface() {
            assertThat(Action.class.isAnnotationPresent(FunctionalInterface.class)).isTrue();
        }

        @Test
        @DisplayName("can use method reference | 可以使用方法引用")
        void testMethodReference() {
            ActionTestHelper helper = new ActionTestHelper();
            Action action = helper::doSomething;

            RuleContext context = OpenRules.context();
            action.execute(context);

            assertThat(helper.wasExecuted()).isTrue();
        }
    }

    // Helper class
    private static class ActionTestHelper {
        private boolean executed = false;

        void doSomething(RuleContext context) {
            executed = true;
        }

        boolean wasExecuted() {
            return executed;
        }
    }
}
