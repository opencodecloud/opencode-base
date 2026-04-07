package cloud.opencode.base.rules.decision;

import cloud.opencode.base.rules.RuleContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Decision Table Implementation
 * 简单决策表实现
 *
 * <p>A basic implementation of DecisionTable for simple rule scenarios.</p>
 * <p>用于简单规则场景的DecisionTable基本实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Condition syntax support (wildcards, comparisons, equality) - 条件语法支持（通配符、比较、相等）</li>
 *   <li>Multiple hit policy support - 多种命中策略支持</li>
 *   <li>Context and map-based evaluation - 基于上下文和Map的评估</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SimpleDecisionTable table = new SimpleDecisionTable(
 *     "pricing", HitPolicy.FIRST,
 *     List.of("type"), List.of("discount"),
 *     List.of(new Object[]{"VIP"}),
 *     List.of(new Object[]{0.15})
 * );
 * DecisionResult result = table.evaluate(Map.of("type", "VIP"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable internal state) - 线程安全: 否（可变内部状态）</li>
 *   <li>Null-safe: No (inputs must not be null) - 空值安全: 否（输入不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public class SimpleDecisionTable implements DecisionTable {

    private final String name;
    private final HitPolicy hitPolicy;
    private final List<String> inputColumns;
    private final List<String> outputColumns;
    private final List<Object[]> inputConditions;
    private final List<Object[]> outputValues;

    /**
     * Creates a simple decision table
     * 创建简单决策表
     *
     * @param name            the table name | 表名
     * @param hitPolicy       the hit policy | 命中策略
     * @param inputColumns    the input column names | 输入列名
     * @param outputColumns   the output column names | 输出列名
     * @param inputConditions the input conditions for each row | 每行的输入条件
     * @param outputValues    the output values for each row | 每行的输出值
     */
    public SimpleDecisionTable(String name, HitPolicy hitPolicy,
                               List<String> inputColumns, List<String> outputColumns,
                               List<Object[]> inputConditions, List<Object[]> outputValues) {
        this.name = name;
        this.hitPolicy = hitPolicy;
        this.inputColumns = List.copyOf(inputColumns);
        this.outputColumns = List.copyOf(outputColumns);

        // Validate that each conditions row has enough columns
        for (int i = 0; i < inputConditions.size(); i++) {
            Object[] conditions = inputConditions.get(i);
            if (conditions.length < inputColumns.size()) {
                throw new IllegalArgumentException(
                        "Row " + i + " conditions array length (" + conditions.length
                        + ") is less than input columns size (" + inputColumns.size() + ")");
            }
        }
        // Validate that each output row has enough columns
        for (int i = 0; i < outputValues.size(); i++) {
            Object[] outputs = outputValues.get(i);
            if (outputs.length < outputColumns.size()) {
                throw new IllegalArgumentException(
                        "Row " + i + " output array length (" + outputs.length
                        + ") is less than output columns size (" + outputColumns.size() + ")");
            }
        }

        this.inputConditions = new ArrayList<>(inputConditions);
        this.outputValues = new ArrayList<>(outputValues);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public HitPolicy getHitPolicy() {
        return hitPolicy;
    }

    @Override
    public DecisionResult evaluate(RuleContext context) {
        Map<String, Object> inputs = new HashMap<>();
        for (String col : inputColumns) {
            inputs.put(col, context.get(col));
        }
        return evaluate(inputs);
    }

    @Override
    public DecisionResult evaluate(Map<String, Object> inputs) {
        List<Integer> matchedRows = new ArrayList<>();
        List<Map<String, Object>> allOutputs = new ArrayList<>();

        for (int i = 0; i < inputConditions.size(); i++) {
            if (matchesRow(i, inputs)) {
                matchedRows.add(i);
                allOutputs.add(getRowOutputs(i));

                // For FIRST policy, stop at first match
                if (hitPolicy == HitPolicy.FIRST) {
                    break;
                }
                // For UNIQUE policy, continue scanning to detect duplicates
                // but stop after finding a second match (we have enough to report violation)
                if (hitPolicy == HitPolicy.UNIQUE && matchedRows.size() > 1) {
                    throw new IllegalStateException(
                        "UNIQUE hit policy violated: multiple rows matched (rows " +
                        matchedRows + ") in decision table '" + name + "'");
                }
            }
        }

        if (matchedRows.isEmpty()) {
            return DecisionResult.noMatch();
        }

        if (matchedRows.size() == 1) {
            return DecisionResult.singleMatch(matchedRows.getFirst(), allOutputs.getFirst());
        }

        return DecisionResult.multipleMatches(matchedRows, allOutputs);
    }

    private boolean matchesRow(int rowIndex, Map<String, Object> inputs) {
        Object[] conditions = inputConditions.get(rowIndex);
        for (int i = 0; i < inputColumns.size(); i++) {
            String column = inputColumns.get(i);
            Object condition = conditions[i];
            Object inputValue = inputs.get(column);

            if (!matchesCondition(condition, inputValue)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesCondition(Object condition, Object inputValue) {
        // "-" means any value (wildcard)
        if ("-".equals(condition) || condition == null) {
            return true;
        }

        String condStr = String.valueOf(condition);

        // Handle comparison operators
        try {
            if (condStr.startsWith(">=")) {
                double threshold = Double.parseDouble(condStr.substring(2).trim());
                return toDouble(inputValue) >= threshold;
            }
            if (condStr.startsWith("<=")) {
                double threshold = Double.parseDouble(condStr.substring(2).trim());
                return toDouble(inputValue) <= threshold;
            }
            if (condStr.startsWith(">")) {
                double threshold = Double.parseDouble(condStr.substring(1).trim());
                return toDouble(inputValue) > threshold;
            }
            if (condStr.startsWith("<")) {
                double threshold = Double.parseDouble(condStr.substring(1).trim());
                return toDouble(inputValue) < threshold;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        if (condStr.startsWith("!=")) {
            String expected = condStr.substring(2).trim();
            return !expected.equals(String.valueOf(inputValue));
        }

        // Direct equality
        return condStr.equals(String.valueOf(inputValue));
    }

    private double toDouble(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private Map<String, Object> getRowOutputs(int rowIndex) {
        Map<String, Object> outputs = new HashMap<>();
        Object[] values = outputValues.get(rowIndex);
        for (int i = 0; i < outputColumns.size(); i++) {
            outputs.put(outputColumns.get(i), values[i]);
        }
        return outputs;
    }

    @Override
    public int getRowCount() {
        return inputConditions.size();
    }

    @Override
    public List<String> getInputColumns() {
        return inputColumns;
    }

    @Override
    public List<String> getOutputColumns() {
        return outputColumns;
    }
}
