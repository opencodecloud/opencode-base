package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;

import java.util.Objects;

/**
 * Bitwise Operation Node
 * 位运算节点
 *
 * <p>Represents bitwise operations: AND, OR, XOR, NOT, left shift, right shift.</p>
 * <p>表示位运算：与、或、异或、取反、左移、右移。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Binary bitwise: {@code &}, {@code |}, {@code ^}, {@code <<}, {@code >>} - 二元位运算</li>
 *   <li>Unary bitwise NOT: {@code ~} - 一元位取反</li>
 *   <li>Operands converted to long for computation - 操作数转换为 long 进行计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // 0xFF & 0x0F
 * Node and = BitwiseOpNode.of(leftNode, "&", rightNode);
 * Object result = and.evaluate(ctx);  // 15L
 *
 * // ~value
 * Node not = BitwiseOpNode.ofNot(operandNode);
 * Object result2 = not.evaluate(ctx);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: No, operator and left required non-null; right nullable only for ~ - 空值安全: 否，运算符和左操作数要求非空；右操作数仅在 ~ 时可为空</li>
 *   <li>Type-safe: Throws exception for non-integer types - 类型安全：非整数类型抛出异常</li>
 * </ul>
 *
 * @param operator the bitwise operator | 位运算符
 * @param left the left operand (or sole operand for ~) | 左操作数（~ 运算的唯一操作数）
 * @param right the right operand (null for ~) | 右操作数（~ 运算时为 null）
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public record BitwiseOpNode(String operator, Node left, Node right) implements Node {

    public BitwiseOpNode {
        Objects.requireNonNull(operator, "operator cannot be null");
        Objects.requireNonNull(left, "left operand cannot be null");
        // right is nullable for unary ~ operator
    }

    /**
     * Create binary bitwise operation node
     * 创建二元位运算节点
     *
     * @param left the left operand | 左操作数
     * @param operator the operator string ({@code &}, {@code |}, {@code ^}, {@code <<}, {@code >>}) | 运算符字符串
     * @param right the right operand | 右操作数
     * @return the bitwise operation node | 位运算节点
     */
    public static BitwiseOpNode of(Node left, String operator, Node right) {
        Objects.requireNonNull(right, "right operand cannot be null for binary bitwise operation");
        return new BitwiseOpNode(operator, left, right);
    }

    /**
     * Create unary bitwise NOT node
     * 创建一元位取反节点
     *
     * @param operand the operand | 操作数
     * @return the bitwise NOT node | 位取反节点
     */
    public static BitwiseOpNode ofNot(Node operand) {
        return new BitwiseOpNode("~", operand, null);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        Object leftVal = left.evaluate(context);
        long l = toLong(leftVal);

        if ("~".equals(operator)) {
            return ~l;
        }

        if (right == null) {
            throw OpenExpressionException.evaluationError(
                    "Right operand required for binary bitwise operator: " + operator);
        }

        Object rightVal = right.evaluate(context);
        long r = toLong(rightVal);

        return switch (operator) {
            case "&" -> l & r;
            case "|" -> l | r;
            case "^" -> l ^ r;
            case "<<" -> { validateShift(r); yield l << r; }
            case ">>" -> { validateShift(r); yield l >> r; }
            default -> throw OpenExpressionException.evaluationError(
                    "Unknown bitwise operator: " + operator);
        };
    }

    private static void validateShift(long amount) {
        if (amount < 0 || amount > 63) {
            throw OpenExpressionException.evaluationError(
                    "Shift amount must be between 0 and 63, got: " + amount);
        }
    }

    private long toLong(Object value) {
        if (value instanceof Number n) {
            if (n instanceof Double || n instanceof Float) {
                throw OpenExpressionException.typeError("integer (byte, short, int, or long)", value);
            }
            return n.longValue();
        }
        throw OpenExpressionException.typeError("integer (byte, short, int, or long)", value);
    }

    @Override
    public String toExpressionString() {
        if ("~".equals(operator)) {
            return "(~" + left.toExpressionString() + ")";
        }
        return "(" + left.toExpressionString() + " " + operator + " " + right.toExpressionString() + ")";
    }
}
