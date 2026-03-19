package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.EvaluationContext;

/**
 * Literal Node
 * 字面量节点
 *
 * <p>Represents literal values: numbers, strings, booleans, null.</p>
 * <p>表示字面量值：数字、字符串、布尔值、null。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Typed factory methods for int, long, double, string, boolean, null - 为int、long、double、string、boolean、null提供类型化工厂方法</li>
 *   <li>Constant folding optimization support - 支持常量折叠优化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node num = LiteralNode.ofInt(42);
 * Node str = LiteralNode.ofString("hello");
 * Node nil = LiteralNode.ofNull();
 * Object result = num.evaluate(ctx);  // 42
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: Yes, null value is a valid literal - 空值安全: 是，null值是合法字面量</li>
 * </ul>
 *
 * @param value the literal value | 字面量值
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record LiteralNode(Object value) implements Node {

    /**
     * Create literal node with any value
     * 创建任意值的字面量节点
     *
     * @param value the literal value | 字面量值
     * @return the literal node | 字面量节点
     */
    public static LiteralNode of(Object value) {
        return new LiteralNode(value);
    }

    /**
     * Create null literal
     * 创建 null 字面量
     *
     * @return the null literal node | null 字面量节点
     */
    public static LiteralNode ofNull() {
        return new LiteralNode(null);
    }

    /**
     * Create boolean literal
     * 创建布尔字面量
     *
     * @param value the boolean value | 布尔值
     * @return the boolean literal node | 布尔字面量节点
     */
    public static LiteralNode ofBoolean(boolean value) {
        return new LiteralNode(value);
    }

    /**
     * Create integer literal
     * 创建整数字面量
     *
     * @param value the integer value | 整数值
     * @return the integer literal node | 整数字面量节点
     */
    public static LiteralNode ofInt(int value) {
        return new LiteralNode(value);
    }

    /**
     * Create long literal
     * 创建长整数字面量
     *
     * @param value the long value | 长整数值
     * @return the long literal node | 长整数字面量节点
     */
    public static LiteralNode ofLong(long value) {
        return new LiteralNode(value);
    }

    /**
     * Create double literal
     * 创建双精度字面量
     *
     * @param value the double value | 双精度值
     * @return the double literal node | 双精度字面量节点
     */
    public static LiteralNode ofDouble(double value) {
        return new LiteralNode(value);
    }

    /**
     * Create string literal
     * 创建字符串字面量
     *
     * @param value the string value | 字符串值
     * @return the string literal node | 字符串字面量节点
     */
    public static LiteralNode ofString(String value) {
        return new LiteralNode(value);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        return value;
    }

    @Override
    public String toExpressionString() {
        if (value == null) {
            return "null";
        }
        if (value instanceof String s) {
            return "'" + s.replace("'", "\\'") + "'";
        }
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        return value.toString();
    }
}
