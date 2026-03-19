package cloud.opencode.base.rules.dsl;

import cloud.opencode.base.rules.decision.DecisionTable;
import cloud.opencode.base.rules.decision.HitPolicy;
import cloud.opencode.base.rules.decision.SimpleDecisionTable;
import cloud.opencode.base.rules.exception.OpenRulesException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Decision Table Builder - Fluent DSL for Decision Table Construction
 * 决策表构建器 - 决策表构建的流式DSL
 *
 * <p>Provides a fluent API for building decision tables with input columns,
 * output columns, and rows of conditions and values.</p>
 * <p>提供用于构建决策表的流式API，包含输入列、输出列和条件值行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API - 流式构建器API</li>
 *   <li>Input/output column definition - 输入/输出列定义</li>
 *   <li>Row-based and varargs row addition - 基于行和可变参数的行添加</li>
 *   <li>Validation on build - 构建时验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DecisionTable table = new DecisionTableBuilder()
 *     .name("pricing")
 *     .hitPolicy(HitPolicy.FIRST)
 *     .input("customerType", String.class)
 *     .input("amount", Double.class)
 *     .output("discount", Double.class)
 *     .output("freeShipping", Boolean.class)
 *     .row(new Object[]{"VIP", ">= 1000"}, new Object[]{0.15, true})
 *     .row(new Object[]{"VIP", "-"}, new Object[]{0.10, false})
 *     .row(new Object[]{"Regular", ">= 500"}, new Object[]{0.05, false})
 *     .row(new Object[]{"-", "-"}, new Object[]{0.0, false})
 *     .build();
 * }</pre>
 *
 * <p><strong>Condition Syntax | 条件语法:</strong></p>
 * <ul>
 *   <li>{@code "-"} - Wildcard, matches any value | 通配符，匹配任意值</li>
 *   <li>{@code ">= 100"} - Greater than or equal | 大于等于</li>
 *   <li>{@code "<= 100"} - Less than or equal | 小于等于</li>
 *   <li>{@code "> 100"} - Greater than | 大于</li>
 *   <li>{@code "< 100"} - Less than | 小于</li>
 *   <li>{@code "!= value"} - Not equal | 不等于</li>
 *   <li>{@code "value"} - Exact match | 精确匹配</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder pattern, single-threaded use) - 线程安全: 否（构建器模式，单线程使用）</li>
 *   <li>Null-safe: No (column names and values must not be null) - 空值安全: 否（列名和值不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per rule addition, O(r * c) for build where r = rules, c = conditions - 每次规则添加 O(1), 构建 O(r * c)</li>
 *   <li>Space complexity: O(r * c) for decision table - 决策表 O(r * c)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public final class DecisionTableBuilder {

    private String name = "decision-table";
    private HitPolicy hitPolicy = HitPolicy.FIRST;
    private final List<String> inputColumns = new ArrayList<>();
    private final List<Class<?>> inputTypes = new ArrayList<>();
    private final List<String> outputColumns = new ArrayList<>();
    private final List<Class<?>> outputTypes = new ArrayList<>();
    private final List<Object[]> inputConditions = new ArrayList<>();
    private final List<Object[]> outputValues = new ArrayList<>();

    /**
     * Creates a new decision table builder
     * 创建新的决策表构建器
     */
    public DecisionTableBuilder() {
    }

    /**
     * Sets the decision table name
     * 设置决策表名称
     *
     * @param name the table name | 表名称
     * @return this builder | 此构建器
     */
    public DecisionTableBuilder name(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        return this;
    }

    /**
     * Sets the hit policy
     * 设置命中策略
     *
     * @param hitPolicy the hit policy | 命中策略
     * @return this builder | 此构建器
     */
    public DecisionTableBuilder hitPolicy(HitPolicy hitPolicy) {
        this.hitPolicy = Objects.requireNonNull(hitPolicy, "Hit policy cannot be null");
        return this;
    }

    /**
     * Adds an input column
     * 添加输入列
     *
     * @param name the column name | 列名称
     * @param type the column type | 列类型
     * @return this builder | 此构建器
     */
    public DecisionTableBuilder input(String name, Class<?> type) {
        Objects.requireNonNull(name, "Column name cannot be null");
        Objects.requireNonNull(type, "Column type cannot be null");
        inputColumns.add(name);
        inputTypes.add(type);
        return this;
    }

    /**
     * Adds an input column (type defaults to Object)
     * 添加输入列（类型默认为Object）
     *
     * @param name the column name | 列名称
     * @return this builder | 此构建器
     */
    public DecisionTableBuilder input(String name) {
        return input(name, Object.class);
    }

    /**
     * Adds multiple input columns
     * 添加多个输入列
     *
     * @param names the column names | 列名称
     * @return this builder | 此构建器
     */
    public DecisionTableBuilder inputs(String... names) {
        for (String name : names) {
            input(name);
        }
        return this;
    }

    /**
     * Adds an output column
     * 添加输出列
     *
     * @param name the column name | 列名称
     * @param type the column type | 列类型
     * @return this builder | 此构建器
     */
    public DecisionTableBuilder output(String name, Class<?> type) {
        Objects.requireNonNull(name, "Column name cannot be null");
        Objects.requireNonNull(type, "Column type cannot be null");
        outputColumns.add(name);
        outputTypes.add(type);
        return this;
    }

    /**
     * Adds an output column (type defaults to Object)
     * 添加输出列（类型默认为Object）
     *
     * @param name the column name | 列名称
     * @return this builder | 此构建器
     */
    public DecisionTableBuilder output(String name) {
        return output(name, Object.class);
    }

    /**
     * Adds multiple output columns
     * 添加多个输出列
     *
     * @param names the column names | 列名称
     * @return this builder | 此构建器
     */
    public DecisionTableBuilder outputs(String... names) {
        for (String name : names) {
            output(name);
        }
        return this;
    }

    /**
     * Adds a row to the decision table
     * 向决策表添加行
     *
     * @param conditions the input conditions | 输入条件
     * @param values     the output values | 输出值
     * @return this builder | 此构建器
     * @throws OpenRulesException if conditions or values don't match column count
     */
    public DecisionTableBuilder row(Object[] conditions, Object[] values) {
        Objects.requireNonNull(conditions, "Conditions cannot be null");
        Objects.requireNonNull(values, "Values cannot be null");

        if (!inputColumns.isEmpty() && conditions.length != inputColumns.size()) {
            throw new OpenRulesException(
                    OpenRulesException.RuleErrorType.DECISION_TABLE,
                    "Conditions length (%d) must match input columns count (%d)"
                            .formatted(conditions.length, inputColumns.size())
            );
        }

        if (!outputColumns.isEmpty() && values.length != outputColumns.size()) {
            throw new OpenRulesException(
                    OpenRulesException.RuleErrorType.DECISION_TABLE,
                    "Values length (%d) must match output columns count (%d)"
                            .formatted(values.length, outputColumns.size())
            );
        }

        inputConditions.add(conditions.clone());
        outputValues.add(values.clone());
        return this;
    }

    /**
     * Adds a row using varargs syntax
     * 使用可变参数语法添加行
     *
     * <p>The first half of arguments are conditions, the second half are values.</p>
     * <p>参数的前半部分是条件，后半部分是值。</p>
     *
     * @param args conditions followed by values | 条件后跟值
     * @return this builder | 此构建器
     */
    public DecisionTableBuilder addRow(Object... args) {
        if (inputColumns.isEmpty() || outputColumns.isEmpty()) {
            throw new OpenRulesException(
                    OpenRulesException.RuleErrorType.DECISION_TABLE,
                    "Must define input and output columns before adding rows"
            );
        }

        int inputCount = inputColumns.size();
        int outputCount = outputColumns.size();
        int expectedArgs = inputCount + outputCount;

        if (args.length != expectedArgs) {
            throw new OpenRulesException(
                    OpenRulesException.RuleErrorType.DECISION_TABLE,
                    "Expected %d arguments (%d inputs + %d outputs), got %d"
                            .formatted(expectedArgs, inputCount, outputCount, args.length)
            );
        }

        Object[] conditions = new Object[inputCount];
        Object[] values = new Object[outputCount];

        System.arraycopy(args, 0, conditions, 0, inputCount);
        System.arraycopy(args, inputCount, values, 0, outputCount);

        return row(conditions, values);
    }

    /**
     * Builds the decision table
     * 构建决策表
     *
     * @return the decision table | 决策表
     * @throws OpenRulesException if validation fails
     */
    public DecisionTable build() {
        validate();
        return new SimpleDecisionTable(
                name,
                hitPolicy,
                inputColumns,
                outputColumns,
                inputConditions,
                outputValues
        );
    }

    private void validate() {
        if (inputColumns.isEmpty()) {
            throw new OpenRulesException(
                    OpenRulesException.RuleErrorType.DECISION_TABLE,
                    "Decision table must have at least one input column"
            );
        }

        if (outputColumns.isEmpty()) {
            throw new OpenRulesException(
                    OpenRulesException.RuleErrorType.DECISION_TABLE,
                    "Decision table must have at least one output column"
            );
        }

        if (inputConditions.isEmpty()) {
            throw new OpenRulesException(
                    OpenRulesException.RuleErrorType.DECISION_TABLE,
                    "Decision table must have at least one row"
            );
        }
    }

    /**
     * Gets the current row count
     * 获取当前行数
     *
     * @return the row count | 行数
     */
    public int getRowCount() {
        return inputConditions.size();
    }

    /**
     * Gets the input column count
     * 获取输入列数
     *
     * @return the input column count | 输入列数
     */
    public int getInputColumnCount() {
        return inputColumns.size();
    }

    /**
     * Gets the output column count
     * 获取输出列数
     *
     * @return the output column count | 输出列数
     */
    public int getOutputColumnCount() {
        return outputColumns.size();
    }
}
