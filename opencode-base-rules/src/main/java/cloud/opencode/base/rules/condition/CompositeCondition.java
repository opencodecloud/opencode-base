package cloud.opencode.base.rules.condition;

import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.model.Condition;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite Condition - Combines Multiple Conditions with Logical Operators
 * 组合条件 - 使用逻辑运算符组合多个条件
 *
 * <p>Allows combining multiple conditions using AND, OR, or NOT operators.</p>
 * <p>允许使用AND、OR或NOT运算符组合多个条件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>AND, OR, NOT logical operators - AND、OR、NOT逻辑运算符</li>
 *   <li>Short-circuit evaluation - 短路求值</li>
 *   <li>Factory methods for convenience - 便捷工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // AND combination
 * Condition andCondition = new CompositeCondition(
 *     Operator.AND, List.of(condition1, condition2)
 * );
 *
 * // OR combination
 * Condition orCondition = new CompositeCondition(
 *     Operator.OR, List.of(condition1, condition2)
 * );
 *
 * // NOT (single condition)
 * Condition notCondition = new CompositeCondition(
 *     Operator.NOT, List.of(condition1)
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not synchronized) - 线程安全: 否（未同步）</li>
 *   <li>Null-safe: No (conditions must not be null) - 空值安全: 否（条件不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public class CompositeCondition implements Condition {

    private final Operator operator;
    private final List<Condition> conditions;

    /**
     * Creates a composite condition
     * 创建组合条件
     *
     * @param operator   the logical operator | 逻辑运算符
     * @param conditions the conditions to combine | 要组合的条件
     */
    public CompositeCondition(Operator operator, List<Condition> conditions) {
        this.operator = operator;
        this.conditions = new ArrayList<>(conditions);
    }

    @Override
    public boolean evaluate(RuleContext context) {
        return switch (operator) {
            case AND -> evaluateAnd(context);
            case OR -> evaluateOr(context);
            case NOT -> evaluateNot(context);
        };
    }

    private boolean evaluateAnd(RuleContext context) {
        for (Condition condition : conditions) {
            if (!condition.evaluate(context)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateOr(RuleContext context) {
        for (Condition condition : conditions) {
            if (condition.evaluate(context)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateNot(RuleContext context) {
        if (conditions.isEmpty()) {
            return true;
        }
        return !conditions.getFirst().evaluate(context);
    }

    /**
     * Gets the operator
     * 获取运算符
     *
     * @return the operator | 运算符
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Gets the conditions
     * 获取条件列表
     *
     * @return the conditions | 条件列表
     */
    public List<Condition> getConditions() {
        return List.copyOf(conditions);
    }

    /**
     * Creates an AND composite condition
     * 创建AND组合条件
     *
     * @param conditions the conditions | 条件
     * @return the composite condition | 组合条件
     */
    public static CompositeCondition and(Condition... conditions) {
        return new CompositeCondition(Operator.AND, List.of(conditions));
    }

    /**
     * Creates an OR composite condition
     * 创建OR组合条件
     *
     * @param conditions the conditions | 条件
     * @return the composite condition | 组合条件
     */
    public static CompositeCondition or(Condition... conditions) {
        return new CompositeCondition(Operator.OR, List.of(conditions));
    }

    /**
     * Creates a NOT composite condition
     * 创建NOT组合条件
     *
     * @param condition the condition to negate | 要否定的条件
     * @return the composite condition | 组合条件
     */
    public static CompositeCondition not(Condition condition) {
        return new CompositeCondition(Operator.NOT, List.of(condition));
    }

    /**
     * Logical operator for composite conditions
     * 组合条件的逻辑运算符
     */
    public enum Operator {
        /** Logical AND - all conditions must be true | 逻辑AND - 所有条件必须为true */
        AND,
        /** Logical OR - at least one condition must be true | 逻辑OR - 至少一个条件必须为true */
        OR,
        /** Logical NOT - negates the first condition | 逻辑NOT - 否定第一个条件 */
        NOT
    }
}
