package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;

import java.util.Objects;

/**
 * Range Test Node
 * 范围测试节点
 *
 * <p>Represents the {@code between} operator which tests whether a value falls
 * within a range (inclusive on both ends).</p>
 * <p>表示 {@code between} 运算符，用于测试一个值是否在范围内（两端包含）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Inclusive range test: lower &lt;= value &lt;= upper - 包含范围测试：下界 &lt;= 值 &lt;= 上界</li>
 *   <li>Numeric comparison via doubleValue for Number types - 数值类型通过 doubleValue 比较</li>
 *   <li>Comparable comparison for other types - 其他类型通过 Comparable 比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // age between 18 and 65
 * Node between = BetweenNode.of(ageNode, lowerNode, upperNode);
 * Object result = between.evaluate(ctx);  // true or false
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: No, value/lower/upper required non-null - 空值安全: 否，值/下界/上界要求非空</li>
 * </ul>
 *
 * @param value the value to test | 要测试的值
 * @param lower the lower bound (inclusive) | 下界（包含）
 * @param upper the upper bound (inclusive) | 上界（包含）
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public record BetweenNode(Node value, Node lower, Node upper) implements Node {

    public BetweenNode {
        Objects.requireNonNull(value, "value cannot be null");
        Objects.requireNonNull(lower, "lower cannot be null");
        Objects.requireNonNull(upper, "upper cannot be null");
    }

    /**
     * Create range test node
     * 创建范围测试节点
     *
     * @param value the value to test | 要测试的值
     * @param lower the lower bound (inclusive) | 下界（包含）
     * @param upper the upper bound (inclusive) | 上界（包含）
     * @return the range test node | 范围测试节点
     */
    public static BetweenNode of(Node value, Node lower, Node upper) {
        return new BetweenNode(value, lower, upper);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        Object val = value.evaluate(context);
        Object lo = lower.evaluate(context);
        Object hi = upper.evaluate(context);

        if (val == null || lo == null || hi == null) {
            return false;
        }

        if (val instanceof Number nVal && lo instanceof Number nLo && hi instanceof Number nHi) {
            if (isIntegral(nVal) && isIntegral(nLo) && isIntegral(nHi)) {
                long v = nVal.longValue();
                return v >= nLo.longValue() && v <= nHi.longValue();
            }
            double v = nVal.doubleValue();
            return v >= nLo.doubleValue() && v <= nHi.doubleValue();
        }

        return compareComparable(val, lo, hi);
    }

    @SuppressWarnings("unchecked")
    private boolean compareComparable(Object val, Object lo, Object hi) {
        if (val instanceof Comparable<?> cVal
                && val.getClass().isInstance(lo) && val.getClass().isInstance(hi)) {
            Comparable<Object> comparable = (Comparable<Object>) cVal;
            return comparable.compareTo(lo) >= 0 && comparable.compareTo(hi) <= 0;
        }
        throw OpenExpressionException.typeError("comparable", val);
    }

    private static boolean isIntegral(Number n) {
        return n instanceof Integer || n instanceof Long || n instanceof Short || n instanceof Byte;
    }

    @Override
    public String toExpressionString() {
        return "(" + value.toExpressionString() + " between " +
               lower.toExpressionString() + " and " +
               upper.toExpressionString() + ")";
    }
}
