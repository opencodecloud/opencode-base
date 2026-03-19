package cloud.opencode.base.collections;

import java.util.Objects;

/**
 * ValueDifference - Represents a difference between two values
 * ValueDifference - 表示两个值之间的差异
 *
 * <p>Used in {@link MapDifference} to represent the difference between
 * values in two maps for the same key.</p>
 * <p>用于 {@link MapDifference} 中表示相同键在两个 Map 中值的差异。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Store left and right values - 存储左值和右值</li>
 *   <li>Factory method for creation - 工厂方法创建</li>
 *   <li>Equality and hash code support - 支持相等性和哈希码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ValueDifference<Integer> diff = ValueDifference.create(100, 200);
 * Integer left = diff.leftValue();   // 100
 * Integer right = diff.rightValue(); // 200
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>All operations: O(1) - 所有操作: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (allows null values) - 空值安全: 是（允许空值）</li>
 * </ul>
 *
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface ValueDifference<V> {

    /**
     * Get the value from the left map.
     * 获取左边 Map 的值。
     *
     * @return the left value | 左值
     */
    V leftValue();

    /**
     * Get the value from the right map.
     * 获取右边 Map 的值。
     *
     * @return the right value | 右值
     */
    V rightValue();

    /**
     * Create a new ValueDifference.
     * 创建新的 ValueDifference。
     *
     * @param <V>   value type | 值类型
     * @param left  the left value | 左值
     * @param right the right value | 右值
     * @return the value difference | 值差异
     */
    static <V> ValueDifference<V> create(V left, V right) {
        return new ValueDifferenceImpl<>(left, right);
    }

    /**
     * Default implementation of ValueDifference.
     * ValueDifference 的默认实现。
     *
     * @param <V> value type | 值类型
     */
    record ValueDifferenceImpl<V>(V leftValue, V rightValue) implements ValueDifference<V> {

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof ValueDifference<?> other) {
                return Objects.equals(leftValue, other.leftValue())
                        && Objects.equals(rightValue, other.rightValue());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(leftValue, rightValue);
        }

        @Override
        public String toString() {
            return "(" + leftValue + ", " + rightValue + ")";
        }
    }
}
