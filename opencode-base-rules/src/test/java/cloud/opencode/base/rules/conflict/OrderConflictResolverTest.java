package cloud.opencode.base.rules.conflict;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OrderConflictResolver Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("OrderConflictResolver Tests")
class OrderConflictResolverTest {

    @Nested
    @DisplayName("Singleton Tests")
    class SingletonTests {

        @Test
        @DisplayName("INSTANCE should be same instance")
        void instanceShouldBeSameInstance() {
            OrderConflictResolver instance1 = OrderConflictResolver.INSTANCE;
            OrderConflictResolver instance2 = OrderConflictResolver.INSTANCE;

            assertThat(instance1).isSameAs(instance2);
        }
    }

    @Nested
    @DisplayName("resolve() Tests")
    class ResolveTests {

        @Test
        @DisplayName("resolve() should preserve registration order")
        void resolveShouldPreserveRegistrationOrder() {
            Rule rule1 = createRule("rule1", 100);
            Rule rule2 = createRule("rule2", 1);
            Rule rule3 = createRule("rule3", 50);

            List<Rule> rules = List.of(rule1, rule2, rule3);
            List<Rule> resolved = OrderConflictResolver.INSTANCE.resolve(rules);

            assertThat(resolved).containsExactly(rule1, rule2, rule3);
        }

        @Test
        @DisplayName("resolve() should return same list reference")
        void resolveShouldReturnSameListReference() {
            Rule rule = createRule("rule", 50);
            List<Rule> rules = List.of(rule);

            List<Rule> resolved = OrderConflictResolver.INSTANCE.resolve(rules);
            assertThat(resolved).isSameAs(rules);
        }

        @Test
        @DisplayName("resolve() should handle empty list")
        void resolveShouldHandleEmptyList() {
            List<Rule> resolved = OrderConflictResolver.INSTANCE.resolve(List.of());
            assertThat(resolved).isEmpty();
        }

        @Test
        @DisplayName("resolve() should handle single rule")
        void resolveShouldHandleSingleRule() {
            Rule rule = createRule("single", 50);
            List<Rule> rules = List.of(rule);

            List<Rule> resolved = OrderConflictResolver.INSTANCE.resolve(rules);
            assertThat(resolved).containsExactly(rule);
        }

        @Test
        @DisplayName("resolve() should ignore priorities")
        void resolveShouldIgnorePriorities() {
            Rule highPriority = createRule("high", 1);
            Rule lowPriority = createRule("low", 1000);

            List<Rule> rules = List.of(lowPriority, highPriority);
            List<Rule> resolved = OrderConflictResolver.INSTANCE.resolve(rules);

            // Order should be preserved regardless of priority
            assertThat(resolved).containsExactly(lowPriority, highPriority);
        }
    }

    private Rule createRule(String name, int priority) {
        return new Rule() {
            @Override
            public String getName() { return name; }
            @Override
            public String getDescription() { return null; }
            @Override
            public int getPriority() { return priority; }
            @Override
            public String getGroup() { return null; }
            @Override
            public boolean isEnabled() { return true; }
            @Override
            public boolean evaluate(RuleContext context) { return true; }
            @Override
            public void execute(RuleContext context) {}
        };
    }
}
