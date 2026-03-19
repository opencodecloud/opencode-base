package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.eval.TypeCoercion;

import java.util.Objects;

/**
 * Unary Operation Node
 * 一元运算节点
 *
 * <p>Represents unary operations: negation, logical not.</p>
 * <p>表示一元运算：取负、逻辑非。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Numeric negation (-) - 数值取负</li>
 *   <li>Logical not (!) - 逻辑非</li>
 *   <li>Unary plus (+) no-op - 一元加号（无操作）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node neg = UnaryOpNode.of("-", LiteralNode.ofInt(5));
 * Object result = neg.evaluate(ctx);  // -5
 *
 * Node not = UnaryOpNode.of("!", LiteralNode.ofBoolean(true));
 * Object result2 = not.evaluate(ctx);  // false
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: No, operator and operand required non-null - 空值安全: 否，运算符和操作数要求非空</li>
 * </ul>
 *
 * @param operator the operator (-, !) | 运算符
 * @param operand the operand | 操作数
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record UnaryOpNode(String operator, Node operand) implements Node {

    public UnaryOpNode {
        Objects.requireNonNull(operator, "operator cannot be null");
        Objects.requireNonNull(operand, "operand cannot be null");
    }

    /**
     * Create unary operation node
     * 创建一元运算节点
     *
     * @param operator the operator string | 运算符字符串
     * @param operand the operand | 操作数
     * @return the unary operation node | 一元运算节点
     */
    public static UnaryOpNode of(String operator, Node operand) {
        return new UnaryOpNode(operator, operand);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        Object value = operand.evaluate(context);

        return switch (operator) {
            case "-" -> negate(value);
            case "!" -> !TypeCoercion.toBoolean(value);
            case "+" -> value; // Unary plus (no-op for numbers)
            default -> throw OpenExpressionException.evaluationError("Unknown unary operator: " + operator);
        };
    }

    private Object negate(Object value) {
        if (value instanceof Number n) {
            if (n instanceof Double d) {
                return -d;
            }
            if (n instanceof Long l) {
                return -l;
            }
            if (n instanceof Integer i) {
                return -i;
            }
            return -n.doubleValue();
        }
        throw OpenExpressionException.typeError("number", value);
    }

    @Override
    public String toExpressionString() {
        return operator + operand.toExpressionString();
    }
}
