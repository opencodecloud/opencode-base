package cloud.opencode.base.rules.model;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleGroup Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("RuleGroup Tests")
class RuleGroupTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder() should create builder with name")
        void builderShouldCreateBuilderWithName() {
            RuleGroup group = RuleGroup.builder("test-group").build();
            assertThat(group.getName()).isEqualTo("test-group");
        }

        @Test
        @DisplayName("description() should set description")
        void descriptionShouldSetDescription() {
            RuleGroup group = RuleGroup.builder("group")
                    .description("Test description")
                    .build();

            assertThat(group.getDescription()).isEqualTo("Test description");
        }

        @Test
        @DisplayName("priority() should set priority")
        void priorityShouldSetPriority() {
            RuleGroup group = RuleGroup.builder("group")
                    .priority(100)
                    .build();

            assertThat(group.getPriority()).isEqualTo(100);
        }

        @Test
        @DisplayName("default priority should be Rule.DEFAULT_PRIORITY")
        void defaultPriorityShouldBeDefault() {
            RuleGroup group = RuleGroup.builder("group").build();
            assertThat(group.getPriority()).isEqualTo(Rule.DEFAULT_PRIORITY);
        }

        @Test
        @DisplayName("addRule() should add single rule")
        void addRuleShouldAddSingleRule() {
            Rule rule = createTestRule("rule1");
            RuleGroup group = RuleGroup.builder("group")
                    .addRule(rule)
                    .build();

            assertThat(group.getRules()).containsExactly(rule);
        }

        @Test
        @DisplayName("addRules(Rule...) should add multiple rules")
        void addRulesVarargsShouldAddMultipleRules() {
            Rule rule1 = createTestRule("rule1");
            Rule rule2 = createTestRule("rule2");
            RuleGroup group = RuleGroup.builder("group")
                    .addRules(rule1, rule2)
                    .build();

            assertThat(group.getRules()).containsExactly(rule1, rule2);
        }

        @Test
        @DisplayName("addRules(List) should add multiple rules")
        void addRulesListShouldAddMultipleRules() {
            Rule rule1 = createTestRule("rule1");
            Rule rule2 = createTestRule("rule2");
            RuleGroup group = RuleGroup.builder("group")
                    .addRules(List.of(rule1, rule2))
                    .build();

            assertThat(group.getRules()).containsExactly(rule1, rule2);
        }
    }

    @Nested
    @DisplayName("Accessor Tests")
    class AccessorTests {

        @Test
        @DisplayName("getName() should return name")
        void getNameShouldReturnName() {
            RuleGroup group = RuleGroup.builder("my-group").build();
            assertThat(group.getName()).isEqualTo("my-group");
        }

        @Test
        @DisplayName("getDescription() should return description")
        void getDescriptionShouldReturnDescription() {
            RuleGroup group = RuleGroup.builder("group")
                    .description("desc")
                    .build();
            assertThat(group.getDescription()).isEqualTo("desc");
        }

        @Test
        @DisplayName("getDescription() should return null when not set")
        void getDescriptionShouldReturnNullWhenNotSet() {
            RuleGroup group = RuleGroup.builder("group").build();
            assertThat(group.getDescription()).isNull();
        }

        @Test
        @DisplayName("getPriority() should return priority")
        void getPriorityShouldReturnPriority() {
            RuleGroup group = RuleGroup.builder("group")
                    .priority(50)
                    .build();
            assertThat(group.getPriority()).isEqualTo(50);
        }

        @Test
        @DisplayName("getRules() should return immutable list")
        void getRulesShouldReturnImmutableList() {
            Rule rule = createTestRule("rule1");
            RuleGroup group = RuleGroup.builder("group")
                    .addRule(rule)
                    .build();

            assertThatThrownBy(() -> group.getRules().add(createTestRule("rule2")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("size() should return rule count")
        void sizeShouldReturnRuleCount() {
            RuleGroup group = RuleGroup.builder("group")
                    .addRules(createTestRule("r1"), createTestRule("r2"), createTestRule("r3"))
                    .build();

            assertThat(group.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty() should return true for empty group")
        void isEmptyShouldReturnTrueForEmptyGroup() {
            RuleGroup group = RuleGroup.builder("group").build();
            assertThat(group.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty() should return false for non-empty group")
        void isEmptyShouldReturnFalseForNonEmptyGroup() {
            RuleGroup group = RuleGroup.builder("group")
                    .addRule(createTestRule("rule"))
                    .build();
            assertThat(group.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("Comparable Tests")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() should compare by priority")
        void compareToShouldCompareByPriority() {
            RuleGroup high = RuleGroup.builder("high").priority(1).build();
            RuleGroup low = RuleGroup.builder("low").priority(100).build();

            assertThat(high.compareTo(low)).isLessThan(0);
            assertThat(low.compareTo(high)).isGreaterThan(0);
        }

        @Test
        @DisplayName("compareTo() should return 0 for same priority")
        void compareToShouldReturnZeroForSamePriority() {
            RuleGroup group1 = RuleGroup.builder("g1").priority(50).build();
            RuleGroup group2 = RuleGroup.builder("g2").priority(50).build();

            assertThat(group1.compareTo(group2)).isZero();
        }
    }

    private Rule createTestRule(String name) {
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
