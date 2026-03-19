package cloud.opencode.base.rules.dsl;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.model.Action;
import cloud.opencode.base.rules.model.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleBuilder Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("RuleBuilder Tests")
class RuleBuilderTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with name should set name")
        void constructorWithNameShouldSetName() {
            Rule rule = new RuleBuilder("my-rule")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.getName()).isEqualTo("my-rule");
        }

        @Test
        @DisplayName("Default constructor should generate name")
        void defaultConstructorShouldGenerateName() {
            Rule rule = new RuleBuilder()
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.getName()).startsWith("rule-");
        }
    }

    @Nested
    @DisplayName("Static Factory Tests")
    class StaticFactoryTests {

        @Test
        @DisplayName("rule(name) should create builder with name")
        void ruleShouldCreateBuilderWithName() {
            Rule rule = RuleBuilder.rule("test-rule")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.getName()).isEqualTo("test-rule");
        }
    }

    @Nested
    @DisplayName("Description Tests")
    class DescriptionTests {

        @Test
        @DisplayName("description() should set description")
        void descriptionShouldSetDescription() {
            Rule rule = new RuleBuilder("rule")
                    .description("Test description")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.getDescription()).isEqualTo("Test description");
        }

        @Test
        @DisplayName("description should be null if not set")
        void descriptionShouldBeNullIfNotSet() {
            Rule rule = new RuleBuilder("rule")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("Priority Tests")
    class PriorityTests {

        @Test
        @DisplayName("priority() should set priority")
        void priorityShouldSetPriority() {
            Rule rule = new RuleBuilder("rule")
                    .priority(50)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.getPriority()).isEqualTo(50);
        }

        @Test
        @DisplayName("default priority should be Rule.DEFAULT_PRIORITY")
        void defaultPriorityShouldBeDefault() {
            Rule rule = new RuleBuilder("rule")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.getPriority()).isEqualTo(Rule.DEFAULT_PRIORITY);
        }
    }

    @Nested
    @DisplayName("Group Tests")
    class GroupTests {

        @Test
        @DisplayName("group() should set group")
        void groupShouldSetGroup() {
            Rule rule = new RuleBuilder("rule")
                    .group("my-group")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.getGroup()).isEqualTo("my-group");
        }

        @Test
        @DisplayName("group should be null if not set")
        void groupShouldBeNullIfNotSet() {
            Rule rule = new RuleBuilder("rule")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.getGroup()).isNull();
        }
    }

    @Nested
    @DisplayName("Enabled Tests")
    class EnabledTests {

        @Test
        @DisplayName("enabled(true) should enable rule")
        void enabledTrueShouldEnableRule() {
            Rule rule = new RuleBuilder("rule")
                    .enabled(true)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("enabled(false) should disable rule")
        void enabledFalseShouldDisableRule() {
            Rule rule = new RuleBuilder("rule")
                    .enabled(false)
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("default enabled should be true")
        void defaultEnabledShouldBeTrue() {
            Rule rule = new RuleBuilder("rule")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Condition Tests")
    class ConditionTests {

        @Test
        @DisplayName("when(Predicate) should add condition")
        void whenPredicateShouldAddCondition() {
            Rule rule = new RuleBuilder("rule")
                    .when((Condition) ctx -> "VIP".equals(ctx.<String>get("type")))
                    .then((Action) ctx -> {})
                    .build();

            RuleContext vipContext = RuleContext.of("type", "VIP");
            RuleContext regularContext = RuleContext.of("type", "REGULAR");

            assertThat(rule.evaluate(vipContext)).isTrue();
            assertThat(rule.evaluate(regularContext)).isFalse();
        }

        @Test
        @DisplayName("when(Condition) should add condition")
        void whenConditionShouldAddCondition() {
            Condition condition = ctx -> true;
            Rule rule = new RuleBuilder("rule")
                    .when(condition)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.evaluate(RuleContext.create())).isTrue();
        }

        @Test
        @DisplayName("and(Predicate) should combine conditions with AND")
        void andPredicateShouldCombineConditionsWithAnd() {
            Rule rule = new RuleBuilder("rule")
                    .when((Condition) ctx -> ctx.<Integer>get("age") >= 18)
                    .and((Condition) ctx -> ctx.<Double>get("income") >= 50000)
                    .then((Action) ctx -> {})
                    .build();

            RuleContext eligible = RuleContext.of("age", 25, "income", 75000.0);
            RuleContext tooYoung = RuleContext.of("age", 16, "income", 75000.0);
            RuleContext lowIncome = RuleContext.of("age", 25, "income", 30000.0);

            assertThat(rule.evaluate(eligible)).isTrue();
            assertThat(rule.evaluate(tooYoung)).isFalse();
            assertThat(rule.evaluate(lowIncome)).isFalse();
        }

        @Test
        @DisplayName("and(Condition) should combine conditions with AND")
        void andConditionShouldCombineConditionsWithAnd() {
            Condition condition1 = ctx -> true;
            Condition condition2 = ctx -> false;

            Rule rule = new RuleBuilder("rule")
                    .when(condition1)
                    .and(condition2)
                    .then((Action) ctx -> {})
                    .build();

            assertThat(rule.evaluate(RuleContext.create())).isFalse();
        }
    }

    @Nested
    @DisplayName("Action Tests")
    class ActionTests {

        @Test
        @DisplayName("then(Consumer) should add action")
        void thenConsumerShouldAddAction() {
            Rule rule = new RuleBuilder("rule")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> ctx.setResult("executed", true))
                    .build();

            RuleContext context = RuleContext.create();
            rule.execute(context);

            assertThat(context.<Boolean>getResult("executed")).isTrue();
        }

        @Test
        @DisplayName("then(Action) should add action")
        void thenActionShouldAddAction() {
            Action action = ctx -> ctx.setResult("executed", true);
            Rule rule = new RuleBuilder("rule")
                    .when((Condition) ctx -> true)
                    .then(action)
                    .build();

            RuleContext context = RuleContext.create();
            rule.execute(context);

            assertThat(context.<Boolean>getResult("executed")).isTrue();
        }

        @Test
        @DisplayName("andThen(Consumer) should chain actions")
        void andThenConsumerShouldChainActions() {
            Rule rule = new RuleBuilder("rule")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> ctx.setResult("step1", true))
                    .andThen((Action) ctx -> ctx.setResult("step2", true))
                    .build();

            RuleContext context = RuleContext.create();
            rule.execute(context);

            assertThat(context.<Boolean>getResult("step1")).isTrue();
            assertThat(context.<Boolean>getResult("step2")).isTrue();
        }

        @Test
        @DisplayName("andThen(Action) should chain actions")
        void andThenActionShouldChainActions() {
            Action action1 = ctx -> ctx.setResult("step1", true);
            Action action2 = ctx -> ctx.setResult("step2", true);

            Rule rule = new RuleBuilder("rule")
                    .when((Condition) ctx -> true)
                    .then(action1)
                    .andThen(action2)
                    .build();

            RuleContext context = RuleContext.create();
            rule.execute(context);

            assertThat(context.<Boolean>getResult("step1")).isTrue();
            assertThat(context.<Boolean>getResult("step2")).isTrue();
        }
    }

    @Nested
    @DisplayName("Build Validation Tests")
    class BuildValidationTests {

        @Test
        @DisplayName("build() should throw when no condition")
        void buildShouldThrowWhenNoCondition() {
            RuleBuilder builder = new RuleBuilder("rule")
                    .then((Action) ctx -> {});

            assertThatThrownBy(builder::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("condition");
        }

        @Test
        @DisplayName("build() should throw when no action")
        void buildShouldThrowWhenNoAction() {
            RuleBuilder builder = new RuleBuilder("rule")
                    .when((Condition) ctx -> true);

            assertThatThrownBy(builder::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("action");
        }
    }

    @Nested
    @DisplayName("Fluent API Tests")
    class FluentApiTests {

        @Test
        @DisplayName("Methods should be chainable")
        void methodsShouldBeChainable() {
            Rule rule = new RuleBuilder("rule")
                    .description("Test rule")
                    .priority(50)
                    .group("test-group")
                    .enabled(true)
                    .when((Condition) ctx -> ctx.<Integer>get("value") > 10)
                    .and((Condition) ctx -> ctx.<Integer>get("value") < 100)
                    .then((Action) ctx -> ctx.setResult("step1", true))
                    .andThen((Action) ctx -> ctx.setResult("step2", true))
                    .build();

            assertThat(rule.getName()).isEqualTo("rule");
            assertThat(rule.getDescription()).isEqualTo("Test rule");
            assertThat(rule.getPriority()).isEqualTo(50);
            assertThat(rule.getGroup()).isEqualTo("test-group");
            assertThat(rule.isEnabled()).isTrue();

            RuleContext context = RuleContext.of("value", 50);
            assertThat(rule.evaluate(context)).isTrue();

            rule.execute(context);
            assertThat(context.<Boolean>getResult("step1")).isTrue();
            assertThat(context.<Boolean>getResult("step2")).isTrue();
        }
    }
}
