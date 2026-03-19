package cloud.opencode.base.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Rule Interface Tests
 * Rule 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("Rule Interface Tests | Rule 接口测试")
class RuleTest {

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("getGroup returns null by default | getGroup 默认返回 null")
        void testGetGroupDefault() {
            Rule rule = createMinimalRule("test-rule", 100);
            assertThat(rule.getGroup()).isNull();
        }

        @Test
        @DisplayName("isEnabled returns true by default | isEnabled 默认返回 true")
        void testIsEnabledDefault() {
            Rule rule = createMinimalRule("test-rule", 100);
            assertThat(rule.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("compareTo compares by priority | compareTo 按优先级比较")
        void testCompareTo() {
            Rule highPriority = createMinimalRule("high", 10);
            Rule lowPriority = createMinimalRule("low", 100);

            assertThat(highPriority.compareTo(lowPriority)).isLessThan(0);
            assertThat(lowPriority.compareTo(highPriority)).isGreaterThan(0);
        }

        @Test
        @DisplayName("compareTo returns 0 for same priority | compareTo 对相同优先级返回 0")
        void testCompareToSamePriority() {
            Rule rule1 = createMinimalRule("rule1", 50);
            Rule rule2 = createMinimalRule("rule2", 50);

            assertThat(rule1.compareTo(rule2)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("DEFAULT_PRIORITY Tests | DEFAULT_PRIORITY 测试")
    class DefaultPriorityTests {

        @Test
        @DisplayName("DEFAULT_PRIORITY is 1000 | DEFAULT_PRIORITY 是 1000")
        void testDefaultPriorityValue() {
            assertThat(Rule.DEFAULT_PRIORITY).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("Basic Rule Tests | 基本规则测试")
    class BasicRuleTests {

        @Test
        @DisplayName("getName returns rule name | getName 返回规则名称")
        void testGetName() {
            Rule rule = createMinimalRule("my-rule", 100);
            assertThat(rule.getName()).isEqualTo("my-rule");
        }

        @Test
        @DisplayName("getDescription returns description | getDescription 返回描述")
        void testGetDescription() {
            Rule rule = new Rule() {
                @Override
                public String getName() { return "test"; }
                @Override
                public String getDescription() { return "Test description"; }
                @Override
                public int getPriority() { return 100; }
                @Override
                public boolean evaluate(RuleContext context) { return true; }
                @Override
                public void execute(RuleContext context) {}
            };
            assertThat(rule.getDescription()).isEqualTo("Test description");
        }

        @Test
        @DisplayName("getPriority returns priority | getPriority 返回优先级")
        void testGetPriority() {
            Rule rule = createMinimalRule("test", 50);
            assertThat(rule.getPriority()).isEqualTo(50);
        }

        @Test
        @DisplayName("evaluate returns condition result | evaluate 返回条件结果")
        void testEvaluate() {
            Rule alwaysTrue = createConditionalRule("always-true", ctx -> true);
            Rule alwaysFalse = createConditionalRule("always-false", ctx -> false);

            RuleContext context = OpenRules.context();
            assertThat(alwaysTrue.evaluate(context)).isTrue();
            assertThat(alwaysFalse.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("execute runs action | execute 运行动作")
        void testExecute() {
            final boolean[] executed = {false};
            Rule rule = new Rule() {
                @Override
                public String getName() { return "test"; }
                @Override
                public String getDescription() { return null; }
                @Override
                public int getPriority() { return 100; }
                @Override
                public boolean evaluate(RuleContext context) { return true; }
                @Override
                public void execute(RuleContext context) { executed[0] = true; }
            };

            RuleContext context = OpenRules.context();
            rule.execute(context);
            assertThat(executed[0]).isTrue();
        }
    }

    @Nested
    @DisplayName("Custom Rule Tests | 自定义规则测试")
    class CustomRuleTests {

        @Test
        @DisplayName("rule with custom group | 带自定义分组的规则")
        void testRuleWithGroup() {
            Rule rule = new Rule() {
                @Override
                public String getName() { return "grouped-rule"; }
                @Override
                public String getDescription() { return null; }
                @Override
                public int getPriority() { return 100; }
                @Override
                public boolean evaluate(RuleContext context) { return true; }
                @Override
                public void execute(RuleContext context) {}
                @Override
                public String getGroup() { return "my-group"; }
            };

            assertThat(rule.getGroup()).isEqualTo("my-group");
        }

        @Test
        @DisplayName("disabled rule returns false for isEnabled | 禁用的规则 isEnabled 返回 false")
        void testDisabledRule() {
            Rule rule = new Rule() {
                @Override
                public String getName() { return "disabled-rule"; }
                @Override
                public String getDescription() { return null; }
                @Override
                public int getPriority() { return 100; }
                @Override
                public boolean evaluate(RuleContext context) { return true; }
                @Override
                public void execute(RuleContext context) {}
                @Override
                public boolean isEnabled() { return false; }
            };

            assertThat(rule.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("OpenRules Builder Integration Tests | OpenRules 构建器集成测试")
    class OpenRulesBuilderTests {

        @Test
        @DisplayName("build rule with builder | 使用构建器构建规则")
        void testBuildRule() {
            Rule rule = OpenRules.rule("discount-rule")
                    .description("Apply discount for VIP")
                    .priority(10)
                    .when((java.util.function.Predicate<RuleContext>) ctx -> ctx.get("isVip", false))
                    .then((java.util.function.Consumer<RuleContext>) ctx -> ctx.setResult("discount", 0.1))
                    .build();

            assertThat(rule.getName()).isEqualTo("discount-rule");
            assertThat(rule.getDescription()).isEqualTo("Apply discount for VIP");
            assertThat(rule.getPriority()).isEqualTo(10);
        }

        @Test
        @DisplayName("rule evaluates condition correctly | 规则正确评估条件")
        void testRuleEvaluatesCondition() {
            Rule rule = OpenRules.rule("age-check")
                    .when((java.util.function.Predicate<RuleContext>) ctx -> {
                        Integer age = ctx.get("age", 0);
                        return age >= 18;
                    })
                    .then((java.util.function.Consumer<RuleContext>) ctx -> ctx.setResult("allowed", true))
                    .build();

            RuleContext context = OpenRules.context();
            context.put("age", 20);
            assertThat(rule.evaluate(context)).isTrue();

            context.put("age", 15);
            assertThat(rule.evaluate(context)).isFalse();
        }
    }

    // Helper methods

    private Rule createMinimalRule(String name, int priority) {
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

    private Rule createConditionalRule(String name, java.util.function.Predicate<RuleContext> condition) {
        return new Rule() {
            @Override
            public String getName() { return name; }
            @Override
            public String getDescription() { return null; }
            @Override
            public int getPriority() { return 100; }
            @Override
            public boolean evaluate(RuleContext context) { return condition.test(context); }
            @Override
            public void execute(RuleContext context) {}
        };
    }
}
