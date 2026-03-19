package cloud.opencode.base.rules.action;

import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * ConsumerAction Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("ConsumerAction Tests")
class ConsumerActionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should create action from consumer")
        void constructorShouldCreateActionFromConsumer() {
            ConsumerAction action = new ConsumerAction(ctx -> {});
            assertThat(action).isNotNull();
        }
    }

    @Nested
    @DisplayName("execute() Tests")
    class ExecuteTests {

        @Test
        @DisplayName("execute() should invoke consumer")
        void executeShouldInvokeConsumer() {
            AtomicInteger counter = new AtomicInteger(0);
            ConsumerAction action = new ConsumerAction(ctx -> counter.incrementAndGet());
            RuleContext context = RuleContext.create();

            action.execute(context);
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("execute() should pass context to consumer")
        void executeShouldPassContextToConsumer() {
            ConsumerAction action = new ConsumerAction(ctx -> ctx.setResult("executed", true));
            RuleContext context = RuleContext.create();

            action.execute(context);
            assertThat(context.<Boolean>getResult("executed")).isTrue();
        }

        @Test
        @DisplayName("execute() should work with complex logic")
        void executeShouldWorkWithComplexLogic() {
            ConsumerAction action = new ConsumerAction(ctx -> {
                Double amount = ctx.get("amount");
                String type = ctx.get("type");
                double discount = "VIP".equals(type) ? 0.15 : 0.05;
                ctx.setResult("discount", amount * discount);
            });

            RuleContext context = RuleContext.of("amount", 1000.0, "type", "VIP");
            action.execute(context);

            assertThat(context.<Double>getResult("discount")).isEqualTo(150.0);
        }

        @Test
        @DisplayName("execute() should allow multiple executions")
        void executeShouldAllowMultipleExecutions() {
            AtomicInteger counter = new AtomicInteger(0);
            ConsumerAction action = new ConsumerAction(ctx -> counter.incrementAndGet());
            RuleContext context = RuleContext.create();

            action.execute(context);
            action.execute(context);
            action.execute(context);

            assertThat(counter.get()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() should create action from consumer")
        void ofShouldCreateActionFromConsumer() {
            ConsumerAction action = ConsumerAction.of(ctx -> ctx.setResult("key", "value"));
            RuleContext context = RuleContext.create();

            action.execute(context);
            assertThat(context.<String>getResult("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("of() should create same behavior as constructor")
        void ofShouldCreateSameBehaviorAsConstructor() {
            AtomicInteger counter1 = new AtomicInteger(0);
            AtomicInteger counter2 = new AtomicInteger(0);

            ConsumerAction fromConstructor = new ConsumerAction(ctx -> counter1.incrementAndGet());
            ConsumerAction fromFactory = ConsumerAction.of(ctx -> counter2.incrementAndGet());

            RuleContext context = RuleContext.create();
            fromConstructor.execute(context);
            fromFactory.execute(context);

            assertThat(counter1.get()).isEqualTo(counter2.get());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work with context modifications")
        void shouldWorkWithContextModifications() {
            ConsumerAction action = ConsumerAction.of(ctx -> {
                ctx.put("modified", true);
                ctx.setResult("result", "done");
                ctx.addFact("new-fact");
            });

            RuleContext context = RuleContext.create();
            action.execute(context);

            assertThat(context.<Boolean>get("modified")).isTrue();
            assertThat(context.<String>getResult("result")).isEqualTo("done");
            assertThat(context.getFact(String.class)).contains("new-fact");
        }
    }
}
