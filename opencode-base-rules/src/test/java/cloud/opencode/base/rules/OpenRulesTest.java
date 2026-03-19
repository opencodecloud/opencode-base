package cloud.opencode.base.rules;

import cloud.opencode.base.rules.decision.DecisionResult;
import cloud.opencode.base.rules.decision.DecisionTable;
import cloud.opencode.base.rules.decision.HitPolicy;
import cloud.opencode.base.rules.model.Action;
import cloud.opencode.base.rules.model.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenRules Tests
 * OpenRules测试
 *
 * @author Leon Soo
 * @since JDK 25, OpenCode-Base-Rules V1.0.0
 */
@DisplayName("OpenRules Tests")
class OpenRulesTest {

    @Nested
    @DisplayName("Rule Builder Tests")
    class RuleBuilderTests {

        @Test
        @DisplayName("Should create rule with fluent API")
        void shouldCreateRule() {
            // Given & When
            Rule rule = OpenRules.rule("discount-rule")
                    .description("Apply VIP discount")
                    .priority(100)
                    .when((Condition) ctx -> "VIP".equals(ctx.get("customerType")))
                    .then((Action) ctx -> ctx.setResult("discount", 0.15))
                    .build();

            // Then
            assertThat(rule.getName()).isEqualTo("discount-rule");
            assertThat(rule.getDescription()).isEqualTo("Apply VIP discount");
            assertThat(rule.getPriority()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should evaluate rule condition")
        void shouldEvaluateCondition() {
            // Given
            Rule rule = OpenRules.rule("test-rule")
                    .when((Condition) ctx -> ctx.<Integer>get("amount") > 100)
                    .then((Action) ctx -> ctx.setResult("eligible", true))
                    .build();

            RuleContext context = OpenRules.contextOf("amount", 150);

            // When
            boolean result = rule.evaluate(context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should execute rule action")
        void shouldExecuteAction() {
            // Given
            Rule rule = OpenRules.rule("action-rule")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> ctx.setResult("executed", true))
                    .build();

            RuleContext context = OpenRules.context();

            // When
            rule.execute(context);

            // Then
            assertThat(context.<Boolean>getResult("executed")).isTrue();
        }
    }

    @Nested
    @DisplayName("Rule Engine Tests")
    class RuleEngineTests {

        @Test
        @DisplayName("Should fire rules and collect results")
        void shouldFireRules() {
            // Given
            Rule vipRule = OpenRules.rule("vip-discount")
                    .priority(1)
                    .when((Condition) ctx -> "VIP".equals(ctx.get("customerType")))
                    .then((Action) ctx -> ctx.setResult("discount", 0.20))
                    .build();

            Rule regularRule = OpenRules.rule("regular-discount")
                    .priority(2)
                    .when((Condition) ctx -> "REGULAR".equals(ctx.get("customerType")))
                    .then((Action) ctx -> ctx.setResult("discount", 0.05))
                    .build();

            RuleEngine engine = OpenRules.engineWith(vipRule, regularRule);
            RuleContext context = OpenRules.contextOf("customerType", "VIP");

            // When
            RuleResult result = engine.fire(context);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.wasFired("vip-discount")).isTrue();
            assertThat(result.wasSkipped("regular-discount")).isTrue();
            assertThat(context.<Double>getResult("discount")).isEqualTo(0.20);
        }

        @Test
        @DisplayName("Should fire first matching rule only")
        void shouldFireFirstOnly() {
            // Given
            Rule rule1 = OpenRules.rule("rule1")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> ctx.setResult("fired", "rule1"))
                    .build();

            Rule rule2 = OpenRules.rule("rule2")
                    .when((Condition) ctx -> true)
                    .then((Action) ctx -> ctx.setResult("fired", "rule2"))
                    .build();

            RuleEngine engine = OpenRules.engineWith(rule1, rule2);
            RuleContext context = OpenRules.context();

            // When
            RuleResult result = engine.fireFirst(context);

            // Then
            assertThat(result.firedCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Decision Table Tests")
    class DecisionTableTests {

        @Test
        @DisplayName("Should evaluate decision table with FIRST hit policy")
        void shouldEvaluateDecisionTable() {
            // Given
            DecisionTable table = OpenRules.decisionTable("pricing")
                    .hitPolicy(HitPolicy.FIRST)
                    .input("customerType", String.class)
                    .input("amount", Double.class)
                    .output("discount", Double.class)
                    .output("freeShipping", Boolean.class)
                    .row(new Object[]{"VIP", ">= 1000"}, new Object[]{0.15, true})
                    .row(new Object[]{"VIP", "-"}, new Object[]{0.10, false})
                    .row(new Object[]{"REGULAR", ">= 500"}, new Object[]{0.05, false})
                    .row(new Object[]{"-", "-"}, new Object[]{0.0, false})
                    .build();

            // When
            DecisionResult result = table.evaluate(Map.of(
                    "customerType", "VIP",
                    "amount", 1500.0
            ));

            // Then
            assertThat(result.hasMatch()).isTrue();
            assertThat(result.<Double>get("discount")).isEqualTo(0.15);
            assertThat(result.<Boolean>get("freeShipping")).isTrue();
        }

        @Test
        @DisplayName("Should return no match when conditions not met")
        void shouldReturnNoMatch() {
            // Given
            DecisionTable table = OpenRules.decisionTable("test")
                    .input("value")
                    .output("result")
                    .row(new Object[]{"A"}, new Object[]{"matched-A"})
                    .build();

            // When
            DecisionResult result = table.evaluate(Map.of("value", "B"));

            // Then
            assertThat(result.hasMatch()).isFalse();
        }

        @Test
        @DisplayName("Should handle comparison operators")
        void shouldHandleComparisonOperators() {
            // Given
            DecisionTable table = OpenRules.decisionTable("score")
                    .input("score")
                    .output("grade")
                    .row(new Object[]{">= 90"}, new Object[]{"A"})
                    .row(new Object[]{">= 80"}, new Object[]{"B"})
                    .row(new Object[]{">= 70"}, new Object[]{"C"})
                    .row(new Object[]{"-"}, new Object[]{"F"})
                    .build();

            // When & Then
            assertThat(table.evaluate(Map.of("score", 95)).<String>get("grade")).isEqualTo("A");
            assertThat(table.evaluate(Map.of("score", 85)).<String>get("grade")).isEqualTo("B");
            assertThat(table.evaluate(Map.of("score", 75)).<String>get("grade")).isEqualTo("C");
            assertThat(table.evaluate(Map.of("score", 60)).<String>get("grade")).isEqualTo("F");
        }
    }

    @Nested
    @DisplayName("Context Tests")
    class ContextTests {

        @Test
        @DisplayName("Should create context with initial facts")
        void shouldCreateContextWithFacts() {
            // Given & When
            RuleContext context = OpenRules.contextOf(
                    "name", "John",
                    "age", 30,
                    "active", true
            );

            // Then
            assertThat(context.<String>get("name")).isEqualTo("John");
            assertThat(context.<Integer>get("age")).isEqualTo(30);
            assertThat(context.<Boolean>get("active")).isTrue();
        }

        @Test
        @DisplayName("Should store and retrieve results")
        void shouldStoreAndRetrieveResults() {
            // Given
            RuleContext context = OpenRules.context();

            // When
            context.setResult("discount", 0.15);
            context.setResult("freeShipping", true);

            // Then
            assertThat(context.<Double>getResult("discount")).isEqualTo(0.15);
            assertThat(context.<Boolean>getResult("freeShipping")).isTrue();
        }
    }

    @Nested
    @DisplayName("Utility Tests")
    class UtilityTests {

        @Test
        @DisplayName("Should return version info")
        void shouldReturnVersion() {
            assertThat(OpenRules.version()).isNotBlank();
            assertThat(OpenRules.info()).containsIgnoringCase("opencode-base-rules");
        }

        @Test
        @DisplayName("Should provide conflict resolvers")
        void shouldProvideConflictResolvers() {
            assertThat(OpenRules.priorityResolver()).isNotNull();
            assertThat(OpenRules.orderResolver()).isNotNull();
        }

        @Test
        @DisplayName("Should provide logging listener")
        void shouldProvideLoggingListener() {
            assertThat(OpenRules.loggingListener()).isNotNull();
        }
    }
}
