package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.eval.TypeCoercion;

import java.util.Objects;

/**
 * Ternary Operation Node
 * 三元运算节点
 *
 * <p>Represents ternary conditional: condition ? trueValue : falseValue</p>
 * <p>表示三元条件运算：条件 ? 真值 : 假值</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Conditional evaluation with lazy branch evaluation - 条件求值，分支惰性求值</li>
 *   <li>Boolean coercion for condition values - 条件值的布尔强制转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // age >= 18 ? 'adult' : 'minor'
 * Node ternary = TernaryOpNode.of(conditionNode, trueNode, falseNode);
 * Object result = ternary.evaluate(ctx);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: No, condition/trueValue/falseValue required non-null - 空值安全: 否，条件/真值/假值要求非空</li>
 * </ul>
 *
 * @param condition the condition | 条件
 * @param trueValue the value if true | 真值
 * @param falseValue the value if false | 假值
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record TernaryOpNode(Node condition, Node trueValue, Node falseValue) implements Node {

    public TernaryOpNode {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(trueValue, "trueValue cannot be null");
        Objects.requireNonNull(falseValue, "falseValue cannot be null");
    }

    /**
     * Create ternary operation node
     * 创建三元运算节点
     *
     * @param condition the condition | 条件
     * @param trueValue the value if true | 真值
     * @param falseValue the value if false | 假值
     * @return the ternary operation node | 三元运算节点
     */
    public static TernaryOpNode of(Node condition, Node trueValue, Node falseValue) {
        return new TernaryOpNode(condition, trueValue, falseValue);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        Object conditionResult = condition.evaluate(context);
        boolean isTrue = TypeCoercion.toBoolean(conditionResult);
        return isTrue ? trueValue.evaluate(context) : falseValue.evaluate(context);
    }

    @Override
    public String toExpressionString() {
        return "(" + condition.toExpressionString() + " ? " +
               trueValue.toExpressionString() + " : " +
               falseValue.toExpressionString() + ")";
    }
}
