package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.EvaluationContext;

import java.util.List;
import java.util.Objects;

/**
 * String Interpolation Node
 * 字符串插值节点
 *
 * <p>Represents string interpolation like {@code "text ${expr} text"}.
 * Parts consist of LiteralNode (string segments) and expression nodes
 * (interpolated segments) that are evaluated and concatenated at runtime.</p>
 * <p>表示字符串插值，如 {@code "text ${expr} text"}。
 * 部分由 LiteralNode（字符串段）和表达式节点（插值段）组成，在运行时求值并拼接。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Inline string interpolation with expression evaluation - 内联字符串插值与表达式求值</li>
 *   <li>Mixed literal text and dynamic expressions - 混合字面文本和动态表达式</li>
 *   <li>Null values converted to "null" string - null 值转换为 "null" 字符串</li>
 *   <li>Supports any expression type in interpolated segments - 插值段支持任意表达式类型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node interpolation = StringInterpolationNode.of(List.of(
 *     LiteralNode.ofString("Hello, "),
 *     IdentifierNode.of("name"),
 *     LiteralNode.ofString("! You are "),
 *     IdentifierNode.of("age"),
 *     LiteralNode.ofString(" years old.")
 * ));
 * // With name="John", age=30:
 * String result = (String) interpolation.evaluate(ctx);
 * // "Hello, John! You are 30 years old."
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record with defensive copy - 线程安全: 是，不可变记录，防御性拷贝</li>
 *   <li>Null-safe: No, null parts list rejected - 空值安全: 否，null 部分列表被拒绝</li>
 * </ul>
 *
 * @param parts the interpolation parts (literal strings and expressions) | 插值部分（字面字符串和表达式）
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public record StringInterpolationNode(List<Node> parts) implements Node {

    public StringInterpolationNode {
        Objects.requireNonNull(parts, "parts cannot be null");
        parts = List.copyOf(parts);
    }

    /**
     * Create string interpolation node
     * 创建字符串插值节点
     *
     * @param parts the interpolation parts | 插值部分
     * @return the string interpolation node | 字符串插值节点
     */
    public static StringInterpolationNode of(List<Node> parts) {
        return new StringInterpolationNode(parts);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        StringBuilder sb = new StringBuilder();
        for (Node part : parts) {
            Object value = part.evaluate(context);
            sb.append(value == null ? "null" : value.toString());
        }
        return sb.toString();
    }

    @Override
    public String toExpressionString() {
        StringBuilder sb = new StringBuilder();
        for (Node part : parts) {
            sb.append(part.toExpressionString());
        }
        return sb.toString();
    }
}
