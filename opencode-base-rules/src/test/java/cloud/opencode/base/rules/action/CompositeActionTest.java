package cloud.opencode.base.rules.action;

import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.model.Action;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CompositeAction Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("CompositeAction Tests")
class CompositeActionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should create action from list")
        void constructorShouldCreateActionFromList() {
            Action action1 = ctx -> {};
            Action action2 = ctx -> {};
            CompositeAction composite = new CompositeAction(List.of(action1, action2));

            assertThat(composite).isNotNull();
            assertThat(composite.getActions()).hasSize(2);
        }

        @Test
        @DisplayName("Constructor should copy the list")
        void constructorShouldCopyTheList() {
            List<Action> actions = new ArrayList<>();
            actions.add(ctx -> {});
            CompositeAction composite = new CompositeAction(actions);

            actions.add(ctx -> {}); // Modify original list
            assertThat(composite.getActions()).hasSize(1); // Should not be affected
        }
    }

    @Nested
    @DisplayName("execute() Tests")
    class ExecuteTests {

        @Test
        @DisplayName("execute() should invoke all actions in order")
        void executeShouldInvokeAllActionsInOrder() {
            List<String> executionOrder = new ArrayList<>();

            Action action1 = ctx -> executionOrder.add("first");
            Action action2 = ctx -> executionOrder.add("second");
            Action action3 = ctx -> executionOrder.add("third");

            CompositeAction composite = new CompositeAction(List.of(action1, action2, action3));
            RuleContext context = RuleContext.create();

            composite.execute(context);

            assertThat(executionOrder).containsExactly("first", "second", "third");
        }

        @Test
        @DisplayName("execute() should pass same context to all actions")
        void executeShouldPassSameContextToAllActions() {
            Action action1 = ctx -> ctx.setResult("step1", "done");
            Action action2 = ctx -> ctx.setResult("step2", "done");
            Action action3 = ctx -> ctx.setResult("step3", "done");

            CompositeAction composite = new CompositeAction(List.of(action1, action2, action3));
            RuleContext context = RuleContext.create();

            composite.execute(context);

            assertThat(context.<String>getResult("step1")).isEqualTo("done");
            assertThat(context.<String>getResult("step2")).isEqualTo("done");
            assertThat(context.<String>getResult("step3")).isEqualTo("done");
        }

        @Test
        @DisplayName("execute() should work with empty list")
        void executeShouldWorkWithEmptyList() {
            CompositeAction composite = new CompositeAction(List.of());
            RuleContext context = RuleContext.create();

            assertThatCode(() -> composite.execute(context)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("execute() should allow actions to access previous actions' results")
        void executeShouldAllowActionsToAccessPreviousResults() {
            Action action1 = ctx -> ctx.put("value", 10);
            Action action2 = ctx -> {
                int value = ctx.get("value");
                ctx.put("doubled", value * 2);
            };
            Action action3 = ctx -> {
                int doubled = ctx.get("doubled");
                ctx.setResult("final", doubled + 5);
            };

            CompositeAction composite = new CompositeAction(List.of(action1, action2, action3));
            RuleContext context = RuleContext.create();

            composite.execute(context);

            assertThat(context.<Integer>getResult("final")).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("getActions() Tests")
    class GetActionsTests {

        @Test
        @DisplayName("getActions() should return immutable list")
        void getActionsShouldReturnImmutableList() {
            Action action = ctx -> {};
            CompositeAction composite = new CompositeAction(List.of(action));

            assertThatThrownBy(() -> composite.getActions().add(ctx -> {}))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getActions() should return all actions")
        void getActionsShouldReturnAllActions() {
            Action action1 = ctx -> {};
            Action action2 = ctx -> {};
            Action action3 = ctx -> {};

            CompositeAction composite = new CompositeAction(List.of(action1, action2, action3));
            assertThat(composite.getActions()).containsExactly(action1, action2, action3);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() should create composite from varargs")
        void ofShouldCreateCompositeFromVarargs() {
            List<String> executionOrder = new ArrayList<>();

            Action action1 = ctx -> executionOrder.add("first");
            Action action2 = ctx -> executionOrder.add("second");

            CompositeAction composite = CompositeAction.of(action1, action2);
            RuleContext context = RuleContext.create();

            composite.execute(context);

            assertThat(executionOrder).containsExactly("first", "second");
        }

        @Test
        @DisplayName("of() should work with single action")
        void ofShouldWorkWithSingleAction() {
            Action action = ctx -> ctx.setResult("executed", true);
            CompositeAction composite = CompositeAction.of(action);
            RuleContext context = RuleContext.create();

            composite.execute(context);

            assertThat(context.<Boolean>getResult("executed")).isTrue();
        }
    }

    @Nested
    @DisplayName("Nested Composite Tests")
    class NestedCompositeTests {

        @Test
        @DisplayName("Should support nested composites")
        void shouldSupportNestedComposites() {
            List<String> executionOrder = new ArrayList<>();

            Action action1 = ctx -> executionOrder.add("1");
            Action action2 = ctx -> executionOrder.add("2");
            Action action3 = ctx -> executionOrder.add("3");
            Action action4 = ctx -> executionOrder.add("4");

            CompositeAction inner = CompositeAction.of(action2, action3);
            CompositeAction outer = CompositeAction.of(action1, inner, action4);

            RuleContext context = RuleContext.create();
            outer.execute(context);

            assertThat(executionOrder).containsExactly("1", "2", "3", "4");
        }
    }
}
