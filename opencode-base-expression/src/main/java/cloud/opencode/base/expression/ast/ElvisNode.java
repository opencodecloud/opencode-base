package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.EvaluationContext;

import java.util.Objects;

/**
 * Elvis/Null-Coalescing Operation Node
 * Elvis/空值合并运算节点
 *
 * <p>Represents the elvis operator {@code ?:} which returns the left operand if non-null,
 * otherwise evaluates and returns the right operand (default value).</p>
 * <p>表示 elvis 运算符 {@code ?:}，如果左操作数非空则返回左操作数，
 * 否则求值并返回右操作数（默认值）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Null-coalescing with lazy default evaluation - 空值合并，默认值惰性求值</li>
 *   <li>Short-circuit: default not evaluated if value is non-null - 短路求值：值非空时不求值默认值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // name ?: 'unknown'
 * Node elvis = ElvisNode.of(nameNode, defaultNode);
 * Object result = elvis.evaluate(ctx);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: No, value and defaultValue required non-null - 空值安全: 否，值和默认值要求非空</li>
 * </ul>
 *
 * @param value the value expression | 值表达式
 * @param defaultValue the default value expression if value is null | 值为空时的默认值表达式
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public record ElvisNode(Node value, Node defaultValue) implements Node {

    public ElvisNode {
        Objects.requireNonNull(value, "value cannot be null");
        Objects.requireNonNull(defaultValue, "defaultValue cannot be null");
    }

    /**
     * Create elvis operation node
     * 创建 elvis 运算节点
     *
     * @param value the value expression | 值表达式
     * @param defaultValue the default value expression | 默认值表达式
     * @return the elvis operation node | elvis 运算节点
     */
    public static ElvisNode of(Node value, Node defaultValue) {
        return new ElvisNode(value, defaultValue);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        Object result = value.evaluate(context);
        return result != null ? result : defaultValue.evaluate(context);
    }

    @Override
    public String toExpressionString() {
        return "(" + value.toExpressionString() + " ?: " + defaultValue.toExpressionString() + ")";
    }
}
