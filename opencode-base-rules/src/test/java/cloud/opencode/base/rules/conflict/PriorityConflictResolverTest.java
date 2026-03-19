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
 * PriorityConflictResolver Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("PriorityConflictResolver Tests")
class PriorityConflictResolverTest {

    @Nested
    @DisplayName("Singleton Tests")
    class SingletonTests {

        @Test
        @DisplayName("INSTANCE should be same instance")
        void instanceShouldBeSameInstance() {
            PriorityConflictResolver instance1 = PriorityConflictResolver.INSTANCE;
            PriorityConflictResolver instance2 = PriorityConflictResolver.INSTANCE;

            assertThat(instance1).isSameAs(instance2);
        }
    }

    @Nested
    @DisplayName("resolve() Tests")
    class ResolveTests {

        @Test
        @DisplayName("resolve() should sort by priority ascending")
        void resolveShouldSortByPriorityAscending() {
            Rule lowPriority = createRule("low", 100);
            Rule highPriority = createRule("high", 1);
            Rule mediumPriority = createRule("medium", 50);

            List<Rule> rules = new ArrayList<>(List.of(lowPriority, highPriority, mediumPriority));
            List<Rule> resolved = PriorityConflictResolver.INSTANCE.resolve(rules);

            assertThat(resolved).containsExactly(highPriority, mediumPriority, lowPriority);
        }

        @Test
        @DisplayName("resolve() should maintain order for same priority")
        void resolveShouldMaintainOrderForSamePriority() {
            Rule rule1 = createRule("rule1", 50);
            Rule rule2 = createRule("rule2", 50);
            Rule rule3 = createRule("rule3", 50);

            List<Rule> rules = new ArrayList<>(List.of(rule1, rule2, rule3));
            List<Rule> resolved = PriorityConflictResolver.INSTANCE.resolve(rules);

            assertThat(resolved).hasSize(3);
        }

        @Test
        @DisplayName("resolve() should handle empty list")
        void resolveShouldHandleEmptyList() {
            List<Rule> resolved = PriorityConflictResolver.INSTANCE.resolve(new ArrayList<>());
            assertThat(resolved).isEmpty();
        }

        @Test
        @DisplayName("resolve() should handle single rule")
        void resolveShouldHandleSingleRule() {
            Rule rule = createRule("single", 50);
            List<Rule> rules = new ArrayList<>(List.of(rule));

            List<Rule> resolved = PriorityConflictResolver.INSTANCE.resolve(rules);
            assertThat(resolved).containsExactly(rule);
        }

        @Test
        @DisplayName("resolve() should return new list")
        void resolveShouldReturnNewList() {
            Rule rule = createRule("rule", 50);
            List<Rule> rules = new ArrayList<>(List.of(rule));

            List<Rule> resolved = PriorityConflictResolver.INSTANCE.resolve(rules);
            assertThat(resolved).isNotSameAs(rules);
        }

        @Test
        @DisplayName("resolve() should handle negative priorities")
        void resolveShouldHandleNegativePriorities() {
            Rule negative = createRule("negative", -10);
            Rule zero = createRule("zero", 0);
            Rule positive = createRule("positive", 10);

            List<Rule> rules = new ArrayList<>(List.of(positive, negative, zero));
            List<Rule> resolved = PriorityConflictResolver.INSTANCE.resolve(rules);

            assertThat(resolved).containsExactly(negative, zero, positive);
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
