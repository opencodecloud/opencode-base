package cloud.opencode.base.rules.decision;

import cloud.opencode.base.rules.OpenRules;
import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * DecisionTable Interface Tests
 * DecisionTable 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("DecisionTable Interface Tests | DecisionTable 接口测试")
class DecisionTableTest {

    @Nested
    @DisplayName("Basic Operations Tests | 基本操作测试")
    class BasicOperationsTests {

        @Test
        @DisplayName("getName returns table name | getName 返回表名")
        void testGetName() {
            DecisionTable table = createSimpleTable("pricing-table");
            assertThat(table.getName()).isEqualTo("pricing-table");
        }

        @Test
        @DisplayName("getHitPolicy returns policy | getHitPolicy 返回策略")
        void testGetHitPolicy() {
            DecisionTable table = createTableWithPolicy(HitPolicy.FIRST);
            assertThat(table.getHitPolicy()).isEqualTo(HitPolicy.FIRST);
        }

        @Test
        @DisplayName("getRowCount returns correct count | getRowCount 返回正确数量")
        void testGetRowCount() {
            DecisionTable table = createTableWithRows(3);
            assertThat(table.getRowCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("getInputColumns returns input columns | getInputColumns 返回输入列")
        void testGetInputColumns() {
            DecisionTable table = createTableWithColumns(
                    List.of("amount", "customerType"),
                    List.of("discount")
            );
            assertThat(table.getInputColumns()).containsExactly("amount", "customerType");
        }

        @Test
        @DisplayName("getOutputColumns returns output columns | getOutputColumns 返回输出列")
        void testGetOutputColumns() {
            DecisionTable table = createTableWithColumns(
                    List.of("amount"),
                    List.of("discount", "bonus")
            );
            assertThat(table.getOutputColumns()).containsExactly("discount", "bonus");
        }
    }

    @Nested
    @DisplayName("Evaluate with Context Tests | 使用上下文评估测试")
    class EvaluateWithContextTests {

        @Test
        @DisplayName("evaluate returns result | evaluate 返回结果")
        void testEvaluate() {
            DecisionTable table = createEvaluableTable();
            RuleContext context = OpenRules.context();
            context.put("amount", 1500);
            context.put("customerType", "VIP");

            DecisionResult result = table.evaluate(context);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Evaluate with Map Tests | 使用 Map 评估测试")
    class EvaluateWithMapTests {

        @Test
        @DisplayName("evaluate with map returns result | evaluate 带 Map 返回结果")
        void testEvaluateWithMap() {
            DecisionTable table = createEvaluableTable();
            Map<String, Object> inputs = Map.of(
                    "amount", 1500,
                    "customerType", "VIP"
            );

            DecisionResult result = table.evaluate(inputs);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Hit Policy Tests | 命中策略测试")
    class HitPolicyTests {

        @Test
        @DisplayName("HitPolicy FIRST returns first match | HitPolicy FIRST 返回第一个匹配")
        void testHitPolicyFirst() {
            DecisionTable table = createTableWithPolicy(HitPolicy.FIRST);
            assertThat(table.getHitPolicy()).isEqualTo(HitPolicy.FIRST);
        }

        @Test
        @DisplayName("HitPolicy UNIQUE requires single match | HitPolicy UNIQUE 要求单一匹配")
        void testHitPolicyUnique() {
            DecisionTable table = createTableWithPolicy(HitPolicy.UNIQUE);
            assertThat(table.getHitPolicy()).isEqualTo(HitPolicy.UNIQUE);
        }

        @Test
        @DisplayName("HitPolicy ANY allows any match | HitPolicy ANY 允许任何匹配")
        void testHitPolicyAny() {
            DecisionTable table = createTableWithPolicy(HitPolicy.ANY);
            assertThat(table.getHitPolicy()).isEqualTo(HitPolicy.ANY);
        }

        @Test
        @DisplayName("HitPolicy COLLECT returns all matches | HitPolicy COLLECT 返回所有匹配")
        void testHitPolicyCollect() {
            DecisionTable table = createTableWithPolicy(HitPolicy.COLLECT);
            assertThat(table.getHitPolicy()).isEqualTo(HitPolicy.COLLECT);
        }
    }

    // Helper methods to create test tables

    private DecisionTable createSimpleTable(String name) {
        return new DecisionTable() {
            @Override
            public String getName() { return name; }
            @Override
            public HitPolicy getHitPolicy() { return HitPolicy.FIRST; }
            @Override
            public DecisionResult evaluate(RuleContext context) { return DecisionResult.noMatch(); }
            @Override
            public DecisionResult evaluate(Map<String, Object> inputs) { return DecisionResult.noMatch(); }
            @Override
            public int getRowCount() { return 0; }
            @Override
            public List<String> getInputColumns() { return List.of(); }
            @Override
            public List<String> getOutputColumns() { return List.of(); }
        };
    }

    private DecisionTable createTableWithPolicy(HitPolicy policy) {
        return new DecisionTable() {
            @Override
            public String getName() { return "test"; }
            @Override
            public HitPolicy getHitPolicy() { return policy; }
            @Override
            public DecisionResult evaluate(RuleContext context) { return DecisionResult.noMatch(); }
            @Override
            public DecisionResult evaluate(Map<String, Object> inputs) { return DecisionResult.noMatch(); }
            @Override
            public int getRowCount() { return 0; }
            @Override
            public List<String> getInputColumns() { return List.of(); }
            @Override
            public List<String> getOutputColumns() { return List.of(); }
        };
    }

    private DecisionTable createTableWithRows(int rowCount) {
        return new DecisionTable() {
            @Override
            public String getName() { return "test"; }
            @Override
            public HitPolicy getHitPolicy() { return HitPolicy.FIRST; }
            @Override
            public DecisionResult evaluate(RuleContext context) { return DecisionResult.noMatch(); }
            @Override
            public DecisionResult evaluate(Map<String, Object> inputs) { return DecisionResult.noMatch(); }
            @Override
            public int getRowCount() { return rowCount; }
            @Override
            public List<String> getInputColumns() { return List.of(); }
            @Override
            public List<String> getOutputColumns() { return List.of(); }
        };
    }

    private DecisionTable createTableWithColumns(List<String> inputCols, List<String> outputCols) {
        return new DecisionTable() {
            @Override
            public String getName() { return "test"; }
            @Override
            public HitPolicy getHitPolicy() { return HitPolicy.FIRST; }
            @Override
            public DecisionResult evaluate(RuleContext context) { return DecisionResult.noMatch(); }
            @Override
            public DecisionResult evaluate(Map<String, Object> inputs) { return DecisionResult.noMatch(); }
            @Override
            public int getRowCount() { return 0; }
            @Override
            public List<String> getInputColumns() { return inputCols; }
            @Override
            public List<String> getOutputColumns() { return outputCols; }
        };
    }

    private DecisionTable createEvaluableTable() {
        return new DecisionTable() {
            @Override
            public String getName() { return "pricing"; }
            @Override
            public HitPolicy getHitPolicy() { return HitPolicy.FIRST; }
            @Override
            public DecisionResult evaluate(RuleContext context) {
                return DecisionResult.singleMatch(0, Map.of("discount", 0.15));
            }
            @Override
            public DecisionResult evaluate(Map<String, Object> inputs) {
                return DecisionResult.singleMatch(0, Map.of("discount", 0.15));
            }
            @Override
            public int getRowCount() { return 1; }
            @Override
            public List<String> getInputColumns() { return List.of("amount", "customerType"); }
            @Override
            public List<String> getOutputColumns() { return List.of("discount"); }
        };
    }
}
