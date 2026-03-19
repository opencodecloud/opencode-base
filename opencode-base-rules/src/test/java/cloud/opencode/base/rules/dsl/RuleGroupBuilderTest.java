package cloud.opencode.base.rules.dsl;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.model.RuleGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleGroupBuilder Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("RuleGroupBuilder Tests")
class RuleGroupBuilderTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should set name")
        void constructorShouldSetName() {
            RuleGroup group = new RuleGroupBuilder("my-group").build();
            assertThat(group.getName()).isEqualTo("my-group");
        }
    }

    @Nested
    @DisplayName("Static Factory Tests")
    class StaticFactoryTests {

        @Test
        @DisplayName("group() should create builder with name")
        void groupShouldCreateBuilderWithName() {
            RuleGroup group = RuleGroupBuilder.group("test-group").build();
            assertThat(group.getName()).isEqualTo("test-group");
        }
    }

    @Nested
    @DisplayName("Description Tests")
    class DescriptionTests {

        @Test
        @DisplayName("description() should set description")
        void descriptionShouldSetDescription() {
            RuleGroup group = new RuleGroupBuilder("group")
                    .description("Test description")
                    .build();

            assertThat(group.getDescription()).isEqualTo("Test description");
        }

        @Test
        @DisplayName("description should be null if not set")
        void descriptionShouldBeNullIfNotSet() {
            RuleGroup group = new RuleGroupBuilder("group").build();
            assertThat(group.getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("Priority Tests")
    class PriorityTests {

        @Test
        @DisplayName("priority() should set priority")
        void priorityShouldSetPriority() {
            RuleGroup group = new RuleGroupBuilder("group")
                    .priority(50)
                    .build();

            assertThat(group.getPriority()).isEqualTo(50);
        }

        @Test
        @DisplayName("default priority should be Rule.DEFAULT_PRIORITY")
        void defaultPriorityShouldBeDefault() {
            RuleGroup group = new RuleGroupBuilder("group").build();
            assertThat(group.getPriority()).isEqualTo(Rule.DEFAULT_PRIORITY);
        }
    }

    @Nested
    @DisplayName("Rule Addition Tests")
    class RuleAdditionTests {

        @Test
        @DisplayName("addRule() should add single rule")
        void addRuleShouldAddSingleRule() {
            Rule rule = createRule("rule1");
            RuleGroup group = new RuleGroupBuilder("group")
                    .addRule(rule)
                    .build();

            assertThat(group.getRules()).containsExactly(rule);
        }

        @Test
        @DisplayName("addRules(varargs) should add multiple rules")
        void addRulesVarargsShouldAddMultipleRules() {
            Rule rule1 = createRule("rule1");
            Rule rule2 = createRule("rule2");
            RuleGroup group = new RuleGroupBuilder("group")
                    .addRules(rule1, rule2)
                    .build();

            assertThat(group.getRules()).containsExactly(rule1, rule2);
        }

        @Test
        @DisplayName("addRules(List) should add multiple rules")
        void addRulesListShouldAddMultipleRules() {
            Rule rule1 = createRule("rule1");
            Rule rule2 = createRule("rule2");
            RuleGroup group = new RuleGroupBuilder("group")
                    .addRules(List.of(rule1, rule2))
                    .build();

            assertThat(group.getRules()).containsExactly(rule1, rule2);
        }

        @Test
        @DisplayName("Multiple addRule() calls should accumulate")
        void multipleAddRuleCallsShouldAccumulate() {
            Rule rule1 = createRule("rule1");
            Rule rule2 = createRule("rule2");
            Rule rule3 = createRule("rule3");

            RuleGroup group = new RuleGroupBuilder("group")
                    .addRule(rule1)
                    .addRule(rule2)
                    .addRule(rule3)
                    .build();

            assertThat(group.getRules()).containsExactly(rule1, rule2, rule3);
        }
    }

    @Nested
    @DisplayName("Fluent API Tests")
    class FluentApiTests {

        @Test
        @DisplayName("Methods should be chainable")
        void methodsShouldBeChainable() {
            Rule rule1 = createRule("rule1");
            Rule rule2 = createRule("rule2");

            RuleGroup group = new RuleGroupBuilder("group")
                    .description("Test group")
                    .priority(50)
                    .addRule(rule1)
                    .addRules(rule2)
                    .build();

            assertThat(group.getName()).isEqualTo("group");
            assertThat(group.getDescription()).isEqualTo("Test group");
            assertThat(group.getPriority()).isEqualTo(50);
            assertThat(group.getRules()).containsExactly(rule1, rule2);
        }

        @Test
        @DisplayName("Each method should return builder")
        void eachMethodShouldReturnBuilder() {
            RuleGroupBuilder builder = new RuleGroupBuilder("group");

            assertThat(builder.description("desc")).isSameAs(builder);
            assertThat(builder.priority(50)).isSameAs(builder);
            assertThat(builder.addRule(createRule("r1"))).isSameAs(builder);
            assertThat(builder.addRules(createRule("r2"))).isSameAs(builder);
            assertThat(builder.addRules(List.of(createRule("r3")))).isSameAs(builder);
        }
    }

    @Nested
    @DisplayName("Empty Group Tests")
    class EmptyGroupTests {

        @Test
        @DisplayName("build() should create empty group when no rules added")
        void buildShouldCreateEmptyGroupWhenNoRulesAdded() {
            RuleGroup group = new RuleGroupBuilder("empty").build();
            assertThat(group.isEmpty()).isTrue();
            assertThat(group.size()).isZero();
        }
    }

    private Rule createRule(String name) {
        return new Rule() {
            @Override
            public String getName() { return name; }
            @Override
            public String getDescription() { return null; }
            @Override
            public int getPriority() { return Rule.DEFAULT_PRIORITY; }
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
