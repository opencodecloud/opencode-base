package cloud.opencode.base.rules.engine;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.model.Action;
import cloud.opencode.base.rules.model.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultRule Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("DefaultRule Tests")
class DefaultRuleTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should set all fields")
        void constructorShouldSetAllFields() {
            Condition condition = ctx -> true;
            Action action = ctx -> {};

            DefaultRule rule = new DefaultRule(
                    "test-rule",
                    "Test description",
                    100,
                    "test-group",
                    true,
                    condition,
                    action
            );

            assertThat(rule.getName()).isEqualTo("test-rule");
            assertThat(rule.getDescription()).isEqualTo("Test description");
            assertThat(rule.getPriority()).isEqualTo(100);
            assertThat(rule.getGroup()).isEqualTo("test-group");
            assertThat(rule.isEnabled()).isTrue();
            assertThat(rule.getCondition()).isSameAs(condition);
            assertThat(rule.getAction()).isSameAs(action);
        }

        @Test
        @DisplayName("Constructor should accept null description")
        void constructorShouldAcceptNullDescription() {
            DefaultRule rule = new DefaultRule(
                    "rule", null, 100, null, true, ctx -> true, ctx -> {}
            );

            assertThat(rule.getDescription()).isNull();
        }

        @Test
        @DisplayName("Constructor should accept null group")
        void constructorShouldAcceptNullGroup() {
            DefaultRule rule = new DefaultRule(
                    "rule", "desc", 100, null, true, ctx -> true, ctx -> {}
            );

            assertThat(rule.getGroup()).isNull();
        }
    }

    @Nested
    @DisplayName("Accessor Tests")
    class AccessorTests {

        @Test
        @DisplayName("getName() should return name")
        void getNameShouldReturnName() {
            DefaultRule rule = createRule("my-rule", 100, true);
            assertThat(rule.getName()).isEqualTo("my-rule");
        }

        @Test
        @DisplayName("getDescription() should return description")
        void getDescriptionShouldReturnDescription() {
            DefaultRule rule = new DefaultRule(
                    "rule", "My description", 100, null, true, ctx -> true, ctx -> {}
            );
            assertThat(rule.getDescription()).isEqualTo("My description");
        }

        @Test
        @DisplayName("getPriority() should return priority")
        void getPriorityShouldReturnPriority() {
            DefaultRule rule = createRule("rule", 50, true);
            assertThat(rule.getPriority()).isEqualTo(50);
        }

        @Test
        @DisplayName("getGroup() should return group")
        void getGroupShouldReturnGroup() {
            DefaultRule rule = new DefaultRule(
                    "rule", null, 100, "my-group", true, ctx -> true, ctx -> {}
            );
            assertThat(rule.getGroup()).isEqualTo("my-group");
        }

        @Test
        @DisplayName("isEnabled() should return enabled status")
        void isEnabledShouldReturnEnabledStatus() {
            DefaultRule enabled = createRule("enabled", 100, true);
            DefaultRule disabled = createRule("disabled", 100, false);

            assertThat(enabled.isEnabled()).isTrue();
            assertThat(disabled.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("getCondition() should return condition")
        void getConditionShouldReturnCondition() {
            Condition condition = ctx -> true;
            DefaultRule rule = new DefaultRule(
                    "rule", null, 100, null, true, condition, ctx -> {}
            );

            assertThat(rule.getCondition()).isSameAs(condition);
        }

        @Test
        @DisplayName("getAction() should return action")
        void getActionShouldReturnAction() {
            Action action = ctx -> {};
            DefaultRule rule = new DefaultRule(
                    "rule", null, 100, null, true, ctx -> true, action
            );

            assertThat(rule.getAction()).isSameAs(action);
        }
    }

    @Nested
    @DisplayName("evaluate() Tests")
    class EvaluateTests {

        @Test
        @DisplayName("evaluate() should delegate to condition")
        void evaluateShouldDelegateToCondition() {
            Condition trueCondition = ctx -> true;
            Condition falseCondition = ctx -> false;

            DefaultRule trueRule = new DefaultRule(
                    "true", null, 100, null, true, trueCondition, ctx -> {}
            );
            DefaultRule falseRule = new DefaultRule(
                    "false", null, 100, null, true, falseCondition, ctx -> {}
            );

            RuleContext context = RuleContext.create();
            assertThat(trueRule.evaluate(context)).isTrue();
            assertThat(falseRule.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("evaluate() should pass context to condition")
        void evaluateShouldPassContextToCondition() {
            Condition condition = ctx -> "VIP".equals(ctx.<String>get("type"));
            DefaultRule rule = new DefaultRule(
                    "rule", null, 100, null, true, condition, ctx -> {}
            );

            RuleContext vipContext = RuleContext.of("type", "VIP");
            RuleContext regularContext = RuleContext.of("type", "REGULAR");

            assertThat(rule.evaluate(vipContext)).isTrue();
            assertThat(rule.evaluate(regularContext)).isFalse();
        }
    }

    @Nested
    @DisplayName("execute() Tests")
    class ExecuteTests {

        @Test
        @DisplayName("execute() should delegate to action")
        void executeShouldDelegateToAction() {
            Action action = ctx -> ctx.setResult("executed", true);
            DefaultRule rule = new DefaultRule(
                    "rule", null, 100, null, true, ctx -> true, action
            );

            RuleContext context = RuleContext.create();
            rule.execute(context);

            assertThat(context.<Boolean>getResult("executed")).isTrue();
        }

        @Test
        @DisplayName("execute() should pass context to action")
        void executeShouldPassContextToAction() {
            Action action = ctx -> {
                int value = ctx.get("value");
                ctx.setResult("doubled", value * 2);
            };
            DefaultRule rule = new DefaultRule(
                    "rule", null, 100, null, true, ctx -> true, action
            );

            RuleContext context = RuleContext.of("value", 21);
            rule.execute(context);

            assertThat(context.<Integer>getResult("doubled")).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("Comparable Tests")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() should compare by priority")
        void compareToShouldCompareByPriority() {
            DefaultRule high = createRule("high", 1, true);
            DefaultRule low = createRule("low", 100, true);

            assertThat(high.compareTo(low)).isLessThan(0);
            assertThat(low.compareTo(high)).isGreaterThan(0);
        }

        @Test
        @DisplayName("compareTo() should return 0 for same priority")
        void compareToShouldReturnZeroForSamePriority() {
            DefaultRule rule1 = createRule("rule1", 50, true);
            DefaultRule rule2 = createRule("rule2", 50, true);

            assertThat(rule1.compareTo(rule2)).isZero();
        }
    }

    @Nested
    @DisplayName("toString() Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString() should contain name, priority, and group")
        void toStringShouldContainNamePriorityAndGroup() {
            DefaultRule rule = new DefaultRule(
                    "my-rule", null, 100, "my-group", true, ctx -> true, ctx -> {}
            );

            String str = rule.toString();
            assertThat(str).contains("my-rule");
            assertThat(str).contains("100");
            assertThat(str).contains("my-group");
        }
    }

    private DefaultRule createRule(String name, int priority, boolean enabled) {
        return new DefaultRule(
                name, null, priority, null, enabled, ctx -> true, ctx -> {}
        );
    }
}
