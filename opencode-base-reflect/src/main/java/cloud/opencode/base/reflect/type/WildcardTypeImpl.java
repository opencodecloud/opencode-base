package cloud.opencode.base.reflect.type;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * Wildcard Type Implementation
 * 通配符类型实现
 *
 * <p>Implementation of WildcardType for runtime type construction.</p>
 * <p>用于运行时类型构造的WildcardType实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>WildcardType runtime construction - WildcardType运行时构造</li>
 *   <li>Upper and lower bounds support - 上界和下界支持</li>
 *   <li>Proper equals/hashCode/toString - 正确的equals/hashCode/toString</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // ? extends Number
 * WildcardType upper = new WildcardTypeImpl(new Type[]{Number.class}, new Type[0]);
 *
 * // ? super Integer
 * WildcardType lower = new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{Integer.class});
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No (bounds arrays must be non-null) - 空值安全: 否（边界数组须非空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class WildcardTypeImpl implements WildcardType {

    private final Type[] upperBounds;
    private final Type[] lowerBounds;

    /**
     * Creates a wildcard type
     * 创建通配符类型
     *
     * @param upperBounds the upper bounds | 上界
     * @param lowerBounds the lower bounds | 下界
     */
    public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
        this.upperBounds = upperBounds != null && upperBounds.length > 0
                ? upperBounds.clone() : new Type[]{Object.class};
        this.lowerBounds = lowerBounds != null ? lowerBounds.clone() : new Type[0];
    }

    /**
     * Creates a wildcard with upper bound (? extends T)
     * 创建带上界的通配符（? extends T）
     *
     * @param upperBound the upper bound | 上界
     * @return wildcard type | 通配符类型
     */
    public static WildcardTypeImpl extendsOf(Type upperBound) {
        return new WildcardTypeImpl(new Type[]{upperBound}, null);
    }

    /**
     * Creates a wildcard with lower bound (? super T)
     * 创建带下界的通配符（? super T）
     *
     * @param lowerBound the lower bound | 下界
     * @return wildcard type | 通配符类型
     */
    public static WildcardTypeImpl superOf(Type lowerBound) {
        return new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{lowerBound});
    }

    /**
     * Creates an unbounded wildcard (?)
     * 创建无界通配符（?）
     *
     * @return wildcard type | 通配符类型
     */
    public static WildcardTypeImpl unbounded() {
        return new WildcardTypeImpl(null, null);
    }

    @Override
    public Type[] getUpperBounds() {
        return upperBounds.clone();
    }

    @Override
    public Type[] getLowerBounds() {
        return lowerBounds.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WildcardType other)) return false;
        return Arrays.equals(upperBounds, other.getUpperBounds())
                && Arrays.equals(lowerBounds, other.getLowerBounds());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(upperBounds) ^ Arrays.hashCode(lowerBounds);
    }

    @Override
    public String toString() {
        if (lowerBounds.length > 0) {
            return "? super " + TypeUtil.toString(lowerBounds[0]);
        }
        if (upperBounds.length > 0 && !upperBounds[0].equals(Object.class)) {
            return "? extends " + TypeUtil.toString(upperBounds[0]);
        }
        return "?";
    }
}
