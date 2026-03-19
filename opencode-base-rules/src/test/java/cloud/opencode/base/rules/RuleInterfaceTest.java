package cloud.opencode.base.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Rule Interface Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("Rule Interface Tests")
class RuleInterfaceTest {

    @Nested
    @DisplayName("DEFAULT_PRIORITY Tests")
    class DefaultPriorityTests {

        @Test
        @DisplayName("DEFAULT_PRIORITY should be 1000")
        void defaultPriorityShouldBe1000() {
            assertThat(Rule.DEFAULT_PRIORITY).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("getGroup() Default Tests")
    class GetGroupDefaultTests {

        @Test
        @DisplayName("getGroup() default should return null")
        void getGroupDefaultShouldReturnNull() {
            Rule rule = createMinimalRule("test");

            assertThat(rule.getGroup()).isNull();
        }
    }

    @Nested
    @DisplayName("isEnabled() Default Tests")
    class IsEnabledDefaultTests {

        @Test
        @DisplayName("isEnabled() default should return true")
        void isEnabledDefaultShouldReturnTrue() {
            Rule rule = createMinimalRule("test");

            assertThat(rule.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("compareTo() Default Tests")
    class CompareToDefaultTests {

        @Test
        @DisplayName("compareTo() should compare by priority")
        void compareToShouldCompareByPriority() {
            Rule highPriority = createRuleWithPriority("high", 1);
            Rule lowPriority = createRuleWithPriority("low", 100);

            assertThat(highPriority.compareTo(lowPriority)).isNegative();
            assertThat(lowPriority.compareTo(highPriority)).isPositive();
        }

        @Test
        @DisplayName("compareTo() should return 0 for equal priorities")
        void compareToShouldReturnZeroForEqualPriorities() {
            Rule rule1 = createRuleWithPriority("rule1", 50);
            Rule rule2 = createRuleWithPriority("rule2", 50);

            assertThat(rule1.compareTo(rule2)).isZero();
        }

        @Test
        @DisplayName("compareTo() should work with default priority")
        void compareToShouldWorkWithDefaultPriority() {
            Rule rule1 = createMinimalRule("rule1");
            Rule rule2 = createMinimalRule("rule2");

            assertThat(rule1.compareTo(rule2)).isZero();
        }
    }

    @Nested
    @DisplayName("Abstract Method Requirements Tests")
    class AbstractMethodRequirementsTests {

        @Test
        @DisplayName("getName() must be implemented")
        void getNameMustBeImplemented() {
            Rule rule = createMinimalRule("test-rule");
            assertThat(rule.getName()).isEqualTo("test-rule");
        }

        @Test
        @DisplayName("getDescription() must be implemented")
        void getDescriptionMustBeImplemented() {
            Rule rule = createMinimalRule("test");
            assertThat(rule.getDescription()).isNull();
        }

        @Test
        @DisplayName("getPriority() must be implemented")
        void getPriorityMustBeImplemented() {
            Rule rule = createMinimalRule("test");
            assertThat(rule.getPriority()).isEqualTo(Rule.DEFAULT_PRIORITY);
        }

        @Test
        @DisplayName("evaluate() must be implemented")
        void evaluateMustBeImplemented() {
            Rule rule = createMinimalRule("test");
            assertThat(rule.evaluate(RuleContext.create())).isTrue();
        }

        @Test
        @DisplayName("execute() must be implemented")
        void executeMustBeImplemented() {
            Rule rule = createMinimalRule("test");
            rule.execute(RuleContext.create()); // Should not throw
        }
    }

    @Nested
    @DisplayName("Comparable Interface Tests")
    class ComparableInterfaceTests {

        @Test
        @DisplayName("Rule should implement Comparable<Rule>")
        void ruleShouldImplementComparable() {
            Rule rule = createMinimalRule("test");
            assertThat(rule).isInstanceOf(Comparable.class);
        }

        @Test
        @DisplayName("Rules should be sortable by priority")
        void rulesShouldBeSortableByPriority() {
            Rule r1 = createRuleWithPriority("r1", 100);
            Rule r2 = createRuleWithPriority("r2", 1);
            Rule r3 = createRuleWithPriority("r3", 50);

            java.util.List<Rule> rules = new java.util.ArrayList<>();
            rules.add(r1);
            rules.add(r2);
            rules.add(r3);
            java.util.Collections.sort(rules);

            assertThat(rules).extracting(Rule::getName)
                    .containsExactly("r2", "r3", "r1");
        }
    }

    private Rule createMinimalRule(String name) {
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

    private Rule createRuleWithPriority(String name, int priority) {
        return new Rule() {
            @Override
            public String getName() { return name; }
            @Override
            public String getDescription() { return null; }
            @Override
            public int getPriority() { return priority; }
            @Override
            public boolean evaluate(RuleContext context) { return true; }
            @Override
            public void execute(RuleContext context) {}
        };
    }
}
