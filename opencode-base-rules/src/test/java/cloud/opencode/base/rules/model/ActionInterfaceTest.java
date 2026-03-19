package cloud.opencode.base.rules.model;

import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Action Interface Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("Action Interface Tests")
class ActionInterfaceTest {

    @Nested
    @DisplayName("execute() Tests")
    class ExecuteTests {

        @Test
        @DisplayName("execute() should modify context")
        void executeShouldModifyContext() {
            Action action = ctx -> ctx.put("result", "executed");
            RuleContext context = RuleContext.create();

            action.execute(context);

            assertThat(context.<String>get("result")).isEqualTo("executed");
        }
    }

    @Nested
    @DisplayName("andThen() Tests")
    class AndThenTests {

        @Test
        @DisplayName("andThen() should execute both actions in order")
        void andThenShouldExecuteBothActionsInOrder() {
            List<String> order = new ArrayList<>();
            Action first = ctx -> order.add("first");
            Action second = ctx -> order.add("second");

            Action combined = first.andThen(second);
            combined.execute(RuleContext.create());

            assertThat(order).containsExactly("first", "second");
        }

        @Test
        @DisplayName("andThen() should chain multiple actions")
        void andThenShouldChainMultipleActions() {
            List<String> order = new ArrayList<>();
            Action a1 = ctx -> order.add("1");
            Action a2 = ctx -> order.add("2");
            Action a3 = ctx -> order.add("3");

            Action combined = a1.andThen(a2).andThen(a3);
            combined.execute(RuleContext.create());

            assertThat(order).containsExactly("1", "2", "3");
        }

        @Test
        @DisplayName("andThen() should share context between actions")
        void andThenShouldShareContext() {
            Action first = ctx -> ctx.put("value", 10);
            Action second = ctx -> {
                int value = ctx.get("value");
                ctx.put("value", value * 2);
            };

            RuleContext context = RuleContext.create();
            Action combined = first.andThen(second);
            combined.execute(context);

            assertThat(context.<Integer>get("value")).isEqualTo(20);
        }

        @Test
        @DisplayName("andThen() should propagate exceptions from first")
        void andThenShouldPropagateExceptionFromFirst() {
            Action first = ctx -> { throw new RuntimeException("First failed"); };
            Action second = ctx -> ctx.put("value", "second");

            Action combined = first.andThen(second);

            assertThatThrownBy(() -> combined.execute(RuleContext.create()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("First failed");
        }

        @Test
        @DisplayName("andThen() should propagate exceptions from second")
        void andThenShouldPropagateExceptionFromSecond() {
            Action first = ctx -> ctx.put("value", "first");
            Action second = ctx -> { throw new RuntimeException("Second failed"); };

            Action combined = first.andThen(second);

            assertThatThrownBy(() -> combined.execute(RuleContext.create()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Second failed");
        }
    }

    @Nested
    @DisplayName("noOp() Tests")
    class NoOpTests {

        @Test
        @DisplayName("noOp() should do nothing")
        void noOpShouldDoNothing() {
            Action noOp = Action.noOp();
            RuleContext context = RuleContext.of("key", "original");

            noOp.execute(context);

            assertThat(context.<String>get("key")).isEqualTo("original");
        }

        @Test
        @DisplayName("noOp() should be chainable")
        void noOpShouldBeChainable() {
            List<String> order = new ArrayList<>();
            Action action = ctx -> order.add("action");

            Action combined = Action.noOp().andThen(action).andThen(Action.noOp());
            combined.execute(RuleContext.create());

            assertThat(order).containsExactly("action");
        }
    }

    @Nested
    @DisplayName("Lambda Implementation Tests")
    class LambdaImplementationTests {

        @Test
        @DisplayName("Lambda should be usable as Action")
        void lambdaShouldBeUsableAsAction() {
            Action action = ctx -> ctx.put("discount", 0.15);
            RuleContext context = RuleContext.create();

            action.execute(context);

            assertThat(context.<Double>get("discount")).isEqualTo(0.15);
        }

        @Test
        @DisplayName("Method reference should be usable as Action")
        void methodReferenceShouldBeUsableAsAction() {
            Action action = this::incrementCounter;
            RuleContext context = RuleContext.of("counter", 0);

            action.execute(context);

            assertThat(context.<Integer>get("counter")).isEqualTo(1);
        }

        private void incrementCounter(RuleContext context) {
            int current = context.get("counter");
            context.put("counter", current + 1);
        }
    }
}
