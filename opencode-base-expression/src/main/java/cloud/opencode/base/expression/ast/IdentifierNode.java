package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;

/**
 * Identifier Node
 * 标识符节点
 *
 * <p>Represents variable references in expressions.</p>
 * <p>表示表达式中的变量引用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Variable lookup with # prefix support - 支持#前缀的变量查找</li>
 *   <li>Special identifiers: #root, #this - 特殊标识符: #root, #this</li>
 *   <li>Fallback to root object property access - 回退到根对象属性访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node id = IdentifierNode.of("name");
 * ctx.setVariable("name", "John");
 * Object result = id.evaluate(ctx);  // "John"
 *
 * Node root = IdentifierNode.of("#root");
 * Object rootObj = root.evaluate(ctx);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: No, null/blank name rejected at construction - 空值安全: 否，null/空名称在构造时被拒绝</li>
 * </ul>
 *
 * @param name the identifier name | 标识符名称
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record IdentifierNode(String name) implements Node {

    /**
     * Create identifier node
     * 创建标识符节点
     *
     * @param name the identifier name | 标识符名称
     */
    public IdentifierNode {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Identifier name cannot be null or blank");
        }
    }

    /**
     * Create identifier node
     * 创建标识符节点
     *
     * @param name the identifier name | 标识符名称
     * @return the identifier node | 标识符节点
     */
    public static IdentifierNode of(String name) {
        return new IdentifierNode(name);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        // Special identifiers
        if ("#root".equals(name) || "root".equals(name)) {
            return context.getRootObject();
        }
        if ("#this".equals(name) || "this".equals(name)) {
            Object thisObj = context.getVariable("#this");
            return thisObj != null ? thisObj : context.getRootObject();
        }

        // Variable lookup (with or without # prefix)
        String varName = name.startsWith("#") ? name : name;
        if (context.hasVariable(varName)) {
            return context.getVariable(varName);
        }

        // Try without # prefix
        String plainName = name.startsWith("#") ? name.substring(1) : name;
        if (context.hasVariable(plainName)) {
            return context.getVariable(plainName);
        }

        // Try root object property access
        Object root = context.getRootObject();
        if (root != null) {
            try {
                return PropertyAccessNode.getPropertyValue(root, plainName, context);
            } catch (OpenExpressionException e) {
                // Property not found, variable not found
            }
        }

        throw OpenExpressionException.evaluationError("Variable not found: " + name);
    }

    @Override
    public String toExpressionString() {
        return name;
    }
}
