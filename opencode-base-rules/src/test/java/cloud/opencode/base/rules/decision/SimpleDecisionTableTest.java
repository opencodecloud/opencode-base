package cloud.opencode.base.rules.decision;

import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SimpleDecisionTable Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("SimpleDecisionTable Tests")
class SimpleDecisionTableTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should set all fields")
        void constructorShouldSetAllFields() {
            SimpleDecisionTable table = new SimpleDecisionTable(
                    "test-table",
                    HitPolicy.FIRST,
                    List.of("input1", "input2"),
                    List.of("output1"),
                    listOf(new Object[]{"a", "b"}),
                    listOf(new Object[]{"result"})
            );

            assertThat(table.getName()).isEqualTo("test-table");
            assertThat(table.getHitPolicy()).isEqualTo(HitPolicy.FIRST);
            assertThat(table.getInputColumns()).containsExactly("input1", "input2");
            assertThat(table.getOutputColumns()).containsExactly("output1");
            assertThat(table.getRowCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("evaluate(Map) Tests")
    class EvaluateMapTests {

        @Test
        @DisplayName("Should match exact values")
        void shouldMatchExactValues() {
            SimpleDecisionTable table = createTable(
                    List.of("type"),
                    List.of("discount"),
                    listOf(new Object[]{"VIP"}),
                    listOf(new Object[]{0.15})
            );

            DecisionResult result = table.evaluate(Map.of("type", "VIP"));
            assertThat(result.hasMatch()).isTrue();
            assertThat(result.<Double>get("discount")).isEqualTo(0.15);
        }

        @Test
        @DisplayName("Should return no match when value doesn't match")
        void shouldReturnNoMatchWhenValueDoesntMatch() {
            SimpleDecisionTable table = createTable(
                    List.of("type"),
                    List.of("discount"),
                    listOf(new Object[]{"VIP"}),
                    listOf(new Object[]{0.15})
            );

            DecisionResult result = table.evaluate(Map.of("type", "REGULAR"));
            assertThat(result.hasMatch()).isFalse();
        }

        @Test
        @DisplayName("Should match wildcard '-'")
        void shouldMatchWildcard() {
            SimpleDecisionTable table = createTable(
                    List.of("type"),
                    List.of("discount"),
                    listOf(new Object[]{"-"}),
                    listOf(new Object[]{0.05})
            );

            DecisionResult result1 = table.evaluate(Map.of("type", "VIP"));
            DecisionResult result2 = table.evaluate(Map.of("type", "REGULAR"));
            DecisionResult result3 = table.evaluate(Map.of("type", "anything"));

            assertThat(result1.hasMatch()).isTrue();
            assertThat(result2.hasMatch()).isTrue();
            assertThat(result3.hasMatch()).isTrue();
        }

        @Test
        @DisplayName("Should match null condition as wildcard")
        void shouldMatchNullConditionAsWildcard() {
            SimpleDecisionTable table = new SimpleDecisionTable(
                    "test-table",
                    HitPolicy.FIRST,
                    List.of("type"),
                    List.of("discount"),
                    listOf(new Object[]{null}),
                    listOf(new Object[]{0.05})
            );

            DecisionResult result = table.evaluate(Map.of("type", "anything"));
            assertThat(result.hasMatch()).isTrue();
        }
    }

    @Nested
    @DisplayName("Comparison Operator Tests")
    class ComparisonOperatorTests {

        @Test
        @DisplayName("'>=' should match greater than or equal")
        void greaterThanOrEqualShouldMatch() {
            SimpleDecisionTable table = createTable(
                    List.of("amount"),
                    List.of("discount"),
                    listOf(new Object[]{">= 1000"}),
                    listOf(new Object[]{0.15})
            );

            assertThat(table.evaluate(Map.of("amount", 1000)).hasMatch()).isTrue();
            assertThat(table.evaluate(Map.of("amount", 1500)).hasMatch()).isTrue();
            assertThat(table.evaluate(Map.of("amount", 999)).hasMatch()).isFalse();
        }

        @Test
        @DisplayName("'<=' should match less than or equal")
        void lessThanOrEqualShouldMatch() {
            SimpleDecisionTable table = createTable(
                    List.of("amount"),
                    List.of("category"),
                    listOf(new Object[]{"<= 100"}),
                    listOf(new Object[]{"small"})
            );

            assertThat(table.evaluate(Map.of("amount", 100)).hasMatch()).isTrue();
            assertThat(table.evaluate(Map.of("amount", 50)).hasMatch()).isTrue();
            assertThat(table.evaluate(Map.of("amount", 101)).hasMatch()).isFalse();
        }

        @Test
        @DisplayName("'>' should match greater than")
        void greaterThanShouldMatch() {
            SimpleDecisionTable table = createTable(
                    List.of("age"),
                    List.of("category"),
                    listOf(new Object[]{"> 18"}),
                    listOf(new Object[]{"adult"})
            );

            assertThat(table.evaluate(Map.of("age", 19)).hasMatch()).isTrue();
            assertThat(table.evaluate(Map.of("age", 18)).hasMatch()).isFalse();
        }

        @Test
        @DisplayName("'<' should match less than")
        void lessThanShouldMatch() {
            SimpleDecisionTable table = createTable(
                    List.of("age"),
                    List.of("category"),
                    listOf(new Object[]{"< 18"}),
                    listOf(new Object[]{"minor"})
            );

            assertThat(table.evaluate(Map.of("age", 17)).hasMatch()).isTrue();
            assertThat(table.evaluate(Map.of("age", 18)).hasMatch()).isFalse();
        }

        @Test
        @DisplayName("'!=' should match not equal")
        void notEqualShouldMatch() {
            SimpleDecisionTable table = createTable(
                    List.of("status"),
                    List.of("action"),
                    listOf(new Object[]{"!= cancelled"}),
                    listOf(new Object[]{"process"})
            );

            assertThat(table.evaluate(Map.of("status", "active")).hasMatch()).isTrue();
            assertThat(table.evaluate(Map.of("status", "pending")).hasMatch()).isTrue();
            assertThat(table.evaluate(Map.of("status", "cancelled")).hasMatch()).isFalse();
        }
    }

    @Nested
    @DisplayName("Hit Policy Tests")
    class HitPolicyTests {

        @Test
        @DisplayName("FIRST policy should return first match")
        void firstPolicyShouldReturnFirstMatch() {
            SimpleDecisionTable table = createTable(
                    HitPolicy.FIRST,
                    List.of("type"),
                    List.of("discount"),
                    listOf(new Object[]{"VIP"}, new Object[]{"-"}),
                    listOf(new Object[]{0.15}, new Object[]{0.05})
            );

            DecisionResult result = table.evaluate(Map.of("type", "VIP"));
            assertThat(result.matchCount()).isEqualTo(1);
            assertThat(result.<Double>get("discount")).isEqualTo(0.15);
        }

        @Test
        @DisplayName("UNIQUE policy should return single match when exactly one row matches")
        void uniquePolicyShouldReturnSingleMatch() {
            SimpleDecisionTable table = createTable(
                    HitPolicy.UNIQUE,
                    List.of("type"),
                    List.of("discount"),
                    listOf(new Object[]{"VIP"}, new Object[]{"REGULAR"}),
                    listOf(new Object[]{0.15}, new Object[]{0.05})
            );

            DecisionResult result = table.evaluate(Map.of("type", "VIP"));
            assertThat(result.matchCount()).isEqualTo(1);
            assertThat(result.<Double>get("discount")).isEqualTo(0.15);
        }

        @Test
        @DisplayName("UNIQUE policy should throw when multiple rows match")
        void uniquePolicyShouldThrowOnMultipleMatches() {
            SimpleDecisionTable table = createTable(
                    HitPolicy.UNIQUE,
                    List.of("type"),
                    List.of("discount"),
                    listOf(new Object[]{"VIP"}, new Object[]{"-"}),
                    listOf(new Object[]{0.15}, new Object[]{0.05})
            );

            assertThatThrownBy(() -> table.evaluate(Map.of("type", "VIP")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("UNIQUE hit policy violated");
        }

        @Test
        @DisplayName("COLLECT policy should return all matches")
        void collectPolicyShouldReturnAllMatches() {
            SimpleDecisionTable table = createTable(
                    HitPolicy.COLLECT,
                    List.of("type"),
                    List.of("tag"),
                    listOf(new Object[]{"-"}, new Object[]{"-"}),
                    listOf(new Object[]{"tag1"}, new Object[]{"tag2"})
            );

            DecisionResult result = table.evaluate(Map.of("type", "any"));
            assertThat(result.matchCount()).isEqualTo(2);
            assertThat(result.allOutputs()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("evaluate(RuleContext) Tests")
    class EvaluateContextTests {

        @Test
        @DisplayName("Should extract inputs from context")
        void shouldExtractInputsFromContext() {
            SimpleDecisionTable table = createTable(
                    List.of("customerType", "amount"),
                    List.of("discount"),
                    listOf(new Object[]{"VIP", ">= 1000"}),
                    listOf(new Object[]{0.15})
            );

            RuleContext context = RuleContext.of(
                    "customerType", "VIP",
                    "amount", 1500
            );

            DecisionResult result = table.evaluate(context);
            assertThat(result.hasMatch()).isTrue();
            assertThat(result.<Double>get("discount")).isEqualTo(0.15);
        }
    }

    @Nested
    @DisplayName("Multi-Column Tests")
    class MultiColumnTests {

        @Test
        @DisplayName("Should match all conditions in row")
        void shouldMatchAllConditionsInRow() {
            SimpleDecisionTable table = createTable(
                    List.of("type", "amount", "region"),
                    List.of("discount"),
                    listOf(new Object[]{"VIP", ">= 1000", "US"}),
                    listOf(new Object[]{0.20})
            );

            DecisionResult match = table.evaluate(Map.of(
                    "type", "VIP",
                    "amount", 1500,
                    "region", "US"
            ));

            DecisionResult noMatch = table.evaluate(Map.of(
                    "type", "VIP",
                    "amount", 1500,
                    "region", "EU"
            ));

            assertThat(match.hasMatch()).isTrue();
            assertThat(noMatch.hasMatch()).isFalse();
        }

        @Test
        @DisplayName("Should return multiple outputs")
        void shouldReturnMultipleOutputs() {
            SimpleDecisionTable table = new SimpleDecisionTable(
                    "multi-output",
                    HitPolicy.FIRST,
                    List.of("type"),
                    List.of("discount", "freeShipping", "message"),
                    listOf(new Object[]{"VIP"}),
                    listOf(new Object[]{0.15, true, "Welcome VIP!"})
            );

            DecisionResult result = table.evaluate(Map.of("type", "VIP"));
            assertThat(result.<Double>get("discount")).isEqualTo(0.15);
            assertThat(result.<Boolean>get("freeShipping")).isTrue();
            assertThat(result.<String>get("message")).isEqualTo("Welcome VIP!");
        }
    }

    @Nested
    @DisplayName("Number Conversion Tests")
    class NumberConversionTests {

        @Test
        @DisplayName("Should convert Number to double for comparison")
        void shouldConvertNumberToDoubleForComparison() {
            SimpleDecisionTable table = createTable(
                    List.of("value"),
                    List.of("category"),
                    listOf(new Object[]{">= 100"}),
                    listOf(new Object[]{"high"})
            );

            // Test with different number types
            assertThat(table.evaluate(Map.of("value", 150)).hasMatch()).isTrue();
            assertThat(table.evaluate(Map.of("value", 150L)).hasMatch()).isTrue();
            assertThat(table.evaluate(Map.of("value", 150.0)).hasMatch()).isTrue();
            assertThat(table.evaluate(Map.of("value", 150.0f)).hasMatch()).isTrue();
        }

        @Test
        @DisplayName("Should parse string to double for comparison")
        void shouldParseStringToDoubleForComparison() {
            SimpleDecisionTable table = createTable(
                    List.of("value"),
                    List.of("category"),
                    listOf(new Object[]{">= 100"}),
                    listOf(new Object[]{"high"})
            );

            assertThat(table.evaluate(Map.of("value", "150")).hasMatch()).isTrue();
        }
    }

    // Helper method to create List<Object[]> from varargs
    @SafeVarargs
    private static List<Object[]> listOf(Object[]... arrays) {
        List<Object[]> list = new ArrayList<>();
        for (Object[] array : arrays) {
            list.add(array);
        }
        return list;
    }

    private SimpleDecisionTable createTable(
            List<String> inputColumns,
            List<String> outputColumns,
            List<Object[]> inputConditions,
            List<Object[]> outputValues) {
        return new SimpleDecisionTable(
                "test-table",
                HitPolicy.FIRST,
                inputColumns,
                outputColumns,
                inputConditions,
                outputValues
        );
    }

    private SimpleDecisionTable createTable(
            HitPolicy hitPolicy,
            List<String> inputColumns,
            List<String> outputColumns,
            List<Object[]> inputConditions,
            List<Object[]> outputValues) {
        return new SimpleDecisionTable(
                "test-table",
                hitPolicy,
                inputColumns,
                outputColumns,
                inputConditions,
                outputValues
        );
    }
}
