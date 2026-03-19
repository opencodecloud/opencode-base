package cloud.opencode.base.core.compare;

import java.util.Objects;

/**
 * Compare Utility - Generic comparison operations for Comparable types
 * 比较工具 - 对 Comparable 类型的通用比较操作
 *
 * <p>Provides generic comparison operator dispatch (EQ/NE/LT/LE/GT/GE)
 * for any Comparable types, with fallback to string comparison.</p>
 * <p>为任何 Comparable 类型提供通用比较运算符分派（EQ/NE/LT/LE/GT/GE），
 * 回退到字符串比较。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Generic comparison operator dispatch (EQ/NE/LT/LE/GT/GE) - 通用比较运算符分派</li>
 *   <li>Support for any Comparable types - 支持任何Comparable类型</li>
 *   <li>Fallback to string comparison - 回退到字符串比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean result = CompareUtil.compare(5, 3, CompareUtil.Operator.GT);  // true
 * boolean eq = CompareUtil.compare("a", "a", CompareUtil.Operator.EQ);  // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No, arguments must not be null - 空值安全: 否，参数不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class CompareUtil {

    private CompareUtil() {
    }

    /**
     * Comparison operators.
     * 比较运算符。
     */
    public enum Operator {
        LT("<") {
            @Override
            public boolean evaluate(int comparison) { return comparison < 0; }
        },
        LE("<=") {
            @Override
            public boolean evaluate(int comparison) { return comparison <= 0; }
        },
        EQ("==") {
            @Override
            public boolean evaluate(int comparison) { return comparison == 0; }
        },
        NE("!=") {
            @Override
            public boolean evaluate(int comparison) { return comparison != 0; }
        },
        GE(">=") {
            @Override
            public boolean evaluate(int comparison) { return comparison >= 0; }
        },
        GT(">") {
            @Override
            public boolean evaluate(int comparison) { return comparison > 0; }
        };

        private final String symbol;

        Operator(String symbol) { this.symbol = symbol; }

        /**
         * Get the symbol representation.
         * 获取符号表示。
         *
         * @return the symbol
         */
        public String symbol() { return symbol; }

        /**
         * Evaluate the comparison result against this operator.
         * 根据此运算符评估比较结果。
         *
         * @param comparison the result of compareTo (negative, 0, or positive)
         * @return true if the comparison satisfies this operator
         */
        public abstract boolean evaluate(int comparison);
    }

    /**
     * Compares two Comparable values. Falls back to string comparison for non-Comparable types.
     * 比较两个 Comparable 值。对非 Comparable 类型回退到字符串比较。
     *
     * @param first  the first value | 第一个值
     * @param second the second value | 第二个值
     * @return negative, 0, or positive | 负数、0 或正数
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static int compare(Object first, Object second) {
        if (first instanceof Comparable c) {
            return c.compareTo(second);
        }
        return String.valueOf(first).compareTo(String.valueOf(second));
    }

    /**
     * Checks if two objects are equal using {@link Objects#equals}.
     * 使用 Objects.equals 检查两个对象是否相等。
     *
     * @param first  the first value | 第一个值
     * @param second the second value | 第二个值
     * @return true if equal | 相等返回 true
     */
    public static boolean equals(Object first, Object second) {
        return Objects.equals(first, second);
    }
}
