package cloud.opencode.base.rules.decision;

import cloud.opencode.base.rules.RuleContext;

import java.util.Map;

/**
 * Decision Table Interface - Table-Based Rule Definition
 * 决策表接口 - 基于表格的规则定义
 *
 * <p>Provides a tabular way to define business rules with input conditions and output actions.</p>
 * <p>提供一种以表格方式定义业务规则的方法，包含输入条件和输出动作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Tabular rule definition - 表格式规则定义</li>
 *   <li>Multiple hit policies - 多种命中策略</li>
 *   <li>Context-based evaluation - 基于上下文的评估</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DecisionTable table = OpenRules.decisionTable()
 *     .name("pricing")
 *     .hitPolicy(HitPolicy.FIRST)
 *     .input("customerType", String.class)
 *     .input("amount", Double.class)
 *     .output("discount", Double.class)
 *     .row(new Object[]{"VIP", ">= 1000"}, new Object[]{0.15})
 *     .row(new Object[]{"VIP", "-"}, new Object[]{0.10})
 *     .row(new Object[]{"-", "-"}, new Object[]{0.0})
 *     .build();
 *
 * DecisionResult result = table.evaluate(context);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (context/inputs must not be null) - 空值安全: 否（上下文/输入不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public interface DecisionTable {

    /**
     * Gets the decision table name
     * 获取决策表名称
     *
     * @return the name | 名称
     */
    String getName();

    /**
     * Gets the hit policy
     * 获取命中策略
     *
     * @return the hit policy | 命中策略
     */
    HitPolicy getHitPolicy();

    /**
     * Evaluates the decision table against the given context
     * 针对给定上下文评估决策表
     *
     * @param context the rule context | 规则上下文
     * @return the decision result | 决策结果
     */
    DecisionResult evaluate(RuleContext context);

    /**
     * Evaluates the decision table against the given inputs
     * 针对给定输入评估决策表
     *
     * @param inputs the input values | 输入值
     * @return the decision result | 决策结果
     */
    DecisionResult evaluate(Map<String, Object> inputs);

    /**
     * Gets the number of rows in the table
     * 获取表中的行数
     *
     * @return row count | 行数
     */
    int getRowCount();

    /**
     * Gets the input column names
     * 获取输入列名
     *
     * @return input column names | 输入列名
     */
    java.util.List<String> getInputColumns();

    /**
     * Gets the output column names
     * 获取输出列名
     *
     * @return output column names | 输出列名
     */
    java.util.List<String> getOutputColumns();
}
