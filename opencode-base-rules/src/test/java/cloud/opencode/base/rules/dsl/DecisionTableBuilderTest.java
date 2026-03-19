package cloud.opencode.base.rules.dsl;

import cloud.opencode.base.rules.decision.DecisionResult;
import cloud.opencode.base.rules.decision.DecisionTable;
import cloud.opencode.base.rules.decision.HitPolicy;
import cloud.opencode.base.rules.exception.OpenRulesException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * DecisionTableBuilder Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("DecisionTableBuilder Tests")
class DecisionTableBuilderTest {

    @Nested
    @DisplayName("name() Tests")
    class NameTests {

        @Test
        @DisplayName("name() should set table name")
        void nameShouldSetTableName() {
            DecisionTable table = new DecisionTableBuilder()
                    .name("my-table")
                    .input("col1")
                    .output("out1")
                    .row(new Object[]{"value"}, new Object[]{"result"})
                    .build();

            assertThat(table.getName()).isEqualTo("my-table");
        }

        @Test
        @DisplayName("default name should be 'decision-table'")
        void defaultNameShouldBeDecisionTable() {
            DecisionTable table = new DecisionTableBuilder()
                    .input("col1")
                    .output("out1")
                    .row(new Object[]{"value"}, new Object[]{"result"})
                    .build();

            assertThat(table.getName()).isEqualTo("decision-table");
        }

        @Test
        @DisplayName("name() should reject null")
        void nameShouldRejectNull() {
            assertThatThrownBy(() -> new DecisionTableBuilder().name(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("hitPolicy() Tests")
    class HitPolicyTests {

        @Test
        @DisplayName("hitPolicy() should set policy")
        void hitPolicyShouldSetPolicy() {
            DecisionTable table = new DecisionTableBuilder()
                    .hitPolicy(HitPolicy.COLLECT)
                    .input("col1")
                    .output("out1")
                    .row(new Object[]{"value"}, new Object[]{"result"})
                    .build();

            assertThat(table.getHitPolicy()).isEqualTo(HitPolicy.COLLECT);
        }

        @Test
        @DisplayName("default hit policy should be FIRST")
        void defaultHitPolicyShouldBeFirst() {
            DecisionTable table = new DecisionTableBuilder()
                    .input("col1")
                    .output("out1")
                    .row(new Object[]{"value"}, new Object[]{"result"})
                    .build();

            assertThat(table.getHitPolicy()).isEqualTo(HitPolicy.FIRST);
        }

        @Test
        @DisplayName("hitPolicy() should reject null")
        void hitPolicyShouldRejectNull() {
            assertThatThrownBy(() -> new DecisionTableBuilder().hitPolicy(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("input() Tests")
    class InputTests {

        @Test
        @DisplayName("input(name, type) should add input column")
        void inputWithTypeShouldAddInputColumn() {
            DecisionTable table = new DecisionTableBuilder()
                    .input("amount", Double.class)
                    .output("discount")
                    .row(new Object[]{100.0}, new Object[]{0.1})
                    .build();

            assertThat(table.getInputColumns()).containsExactly("amount");
        }

        @Test
        @DisplayName("input(name) should add input column with Object type")
        void inputWithoutTypeShouldAddInputColumn() {
            DecisionTable table = new DecisionTableBuilder()
                    .input("col1")
                    .output("out1")
                    .row(new Object[]{"value"}, new Object[]{"result"})
                    .build();

            assertThat(table.getInputColumns()).containsExactly("col1");
        }

        @Test
        @DisplayName("inputs() should add multiple columns")
        void inputsShouldAddMultipleColumns() {
            DecisionTable table = new DecisionTableBuilder()
                    .inputs("col1", "col2", "col3")
                    .output("out1")
                    .row(new Object[]{"a", "b", "c"}, new Object[]{"result"})
                    .build();

            assertThat(table.getInputColumns()).containsExactly("col1", "col2", "col3");
        }

        @Test
        @DisplayName("input() should reject null name")
        void inputShouldRejectNullName() {
            assertThatThrownBy(() -> new DecisionTableBuilder().input(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("input(name, type) should reject null type")
        void inputShouldRejectNullType() {
            assertThatThrownBy(() -> new DecisionTableBuilder().input("name", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("output() Tests")
    class OutputTests {

        @Test
        @DisplayName("output(name, type) should add output column")
        void outputWithTypeShouldAddOutputColumn() {
            DecisionTable table = new DecisionTableBuilder()
                    .input("input")
                    .output("discount", Double.class)
                    .row(new Object[]{"value"}, new Object[]{0.1})
                    .build();

            assertThat(table.getOutputColumns()).containsExactly("discount");
        }

        @Test
        @DisplayName("output(name) should add output column with Object type")
        void outputWithoutTypeShouldAddOutputColumn() {
            DecisionTable table = new DecisionTableBuilder()
                    .input("input")
                    .output("out1")
                    .row(new Object[]{"value"}, new Object[]{"result"})
                    .build();

            assertThat(table.getOutputColumns()).containsExactly("out1");
        }

        @Test
        @DisplayName("outputs() should add multiple columns")
        void outputsShouldAddMultipleColumns() {
            DecisionTable table = new DecisionTableBuilder()
                    .input("input")
                    .outputs("out1", "out2", "out3")
                    .row(new Object[]{"value"}, new Object[]{"a", "b", "c"})
                    .build();

            assertThat(table.getOutputColumns()).containsExactly("out1", "out2", "out3");
        }
    }

    @Nested
    @DisplayName("row() Tests")
    class RowTests {

        @Test
        @DisplayName("row() should add row to table")
        void rowShouldAddRowToTable() {
            DecisionTable table = new DecisionTableBuilder()
                    .input("input")
                    .output("output")
                    .row(new Object[]{"value1"}, new Object[]{"result1"})
                    .row(new Object[]{"value2"}, new Object[]{"result2"})
                    .build();

            assertThat(table.getRowCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("row() should throw when conditions don't match input count")
        void rowShouldThrowWhenConditionsDontMatchInputCount() {
            DecisionTableBuilder builder = new DecisionTableBuilder()
                    .input("col1")
                    .input("col2")
                    .output("out1");

            assertThatThrownBy(() -> builder.row(new Object[]{"single"}, new Object[]{"result"}))
                    .isInstanceOf(OpenRulesException.class)
                    .hasMessageContaining("Conditions length");
        }

        @Test
        @DisplayName("row() should throw when values don't match output count")
        void rowShouldThrowWhenValuesDontMatchOutputCount() {
            DecisionTableBuilder builder = new DecisionTableBuilder()
                    .input("col1")
                    .output("out1")
                    .output("out2");

            assertThatThrownBy(() -> builder.row(new Object[]{"value"}, new Object[]{"single"}))
                    .isInstanceOf(OpenRulesException.class)
                    .hasMessageContaining("Values length");
        }
    }

    @Nested
    @DisplayName("addRow() Tests")
    class AddRowTests {

        @Test
        @DisplayName("addRow() should parse conditions and values")
        void addRowShouldParseConditionsAndValues() {
            DecisionTable table = new DecisionTableBuilder()
                    .input("input1")
                    .input("input2")
                    .output("output1")
                    .addRow("a", "b", "result")
                    .build();

            DecisionResult result = table.evaluate(Map.of("input1", "a", "input2", "b"));
            assertThat(result.hasMatch()).isTrue();
            assertThat(result.<String>get("output1")).isEqualTo("result");
        }

        @Test
        @DisplayName("addRow() should throw without columns defined")
        void addRowShouldThrowWithoutColumnsDefined() {
            DecisionTableBuilder builder = new DecisionTableBuilder();

            assertThatThrownBy(() -> builder.addRow("a", "b"))
                    .isInstanceOf(OpenRulesException.class)
                    .hasMessageContaining("columns");
        }

        @Test
        @DisplayName("addRow() should throw with wrong argument count")
        void addRowShouldThrowWithWrongArgumentCount() {
            DecisionTableBuilder builder = new DecisionTableBuilder()
                    .input("input1")
                    .output("output1");

            assertThatThrownBy(() -> builder.addRow("a", "b", "c"))
                    .isInstanceOf(OpenRulesException.class)
                    .hasMessageContaining("arguments");
        }
    }

    @Nested
    @DisplayName("build() Validation Tests")
    class BuildValidationTests {

        @Test
        @DisplayName("build() should throw when no input columns")
        void buildShouldThrowWhenNoInputColumns() {
            DecisionTableBuilder builder = new DecisionTableBuilder()
                    .output("out1")
                    .row(new Object[]{}, new Object[]{"result"});

            assertThatThrownBy(builder::build)
                    .isInstanceOf(OpenRulesException.class)
                    .hasMessageContaining("input");
        }

        @Test
        @DisplayName("build() should throw when no output columns")
        void buildShouldThrowWhenNoOutputColumns() {
            DecisionTableBuilder builder = new DecisionTableBuilder()
                    .input("input")
                    .row(new Object[]{"value"}, new Object[]{});

            assertThatThrownBy(builder::build)
                    .isInstanceOf(OpenRulesException.class)
                    .hasMessageContaining("output");
        }

        @Test
        @DisplayName("build() should throw when no rows")
        void buildShouldThrowWhenNoRows() {
            DecisionTableBuilder builder = new DecisionTableBuilder()
                    .input("input")
                    .output("output");

            assertThatThrownBy(builder::build)
                    .isInstanceOf(OpenRulesException.class)
                    .hasMessageContaining("row");
        }
    }

    @Nested
    @DisplayName("Count Methods Tests")
    class CountMethodsTests {

        @Test
        @DisplayName("getRowCount() should return row count")
        void getRowCountShouldReturnRowCount() {
            DecisionTableBuilder builder = new DecisionTableBuilder()
                    .input("input")
                    .output("output")
                    .row(new Object[]{"a"}, new Object[]{"1"})
                    .row(new Object[]{"b"}, new Object[]{"2"});

            assertThat(builder.getRowCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("getInputColumnCount() should return input column count")
        void getInputColumnCountShouldReturnInputColumnCount() {
            DecisionTableBuilder builder = new DecisionTableBuilder()
                    .inputs("col1", "col2", "col3");

            assertThat(builder.getInputColumnCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("getOutputColumnCount() should return output column count")
        void getOutputColumnCountShouldReturnOutputColumnCount() {
            DecisionTableBuilder builder = new DecisionTableBuilder()
                    .outputs("out1", "out2");

            assertThat(builder.getOutputColumnCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Fluent API Tests")
    class FluentApiTests {

        @Test
        @DisplayName("All methods should be chainable")
        void allMethodsShouldBeChainable() {
            DecisionTable table = new DecisionTableBuilder()
                    .name("test-table")
                    .hitPolicy(HitPolicy.FIRST)
                    .input("type", String.class)
                    .input("amount", Double.class)
                    .output("discount", Double.class)
                    .row(new Object[]{"VIP", ">= 1000"}, new Object[]{0.15})
                    .row(new Object[]{"VIP", "-"}, new Object[]{0.10})
                    .row(new Object[]{"-", "-"}, new Object[]{0.0})
                    .build();

            assertThat(table).isNotNull();
            assertThat(table.getName()).isEqualTo("test-table");
            assertThat(table.getHitPolicy()).isEqualTo(HitPolicy.FIRST);
            assertThat(table.getRowCount()).isEqualTo(3);
        }
    }
}
