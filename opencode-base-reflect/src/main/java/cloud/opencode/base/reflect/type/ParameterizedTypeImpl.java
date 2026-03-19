package cloud.opencode.base.reflect.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * Parameterized Type Implementation
 * 参数化类型实现
 *
 * <p>Implementation of ParameterizedType for runtime type construction.</p>
 * <p>用于运行时类型构造的ParameterizedType实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ParameterizedType runtime construction - ParameterizedType运行时构造</li>
 *   <li>Proper equals/hashCode/toString - 正确的equals/hashCode/toString</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Represents List<String>
 * ParameterizedType type = new ParameterizedTypeImpl(List.class, null, String.class);
 *
 * // Represents Map<String, Integer>
 * ParameterizedType mapType = new ParameterizedTypeImpl(Map.class, null, String.class, Integer.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No (raw type must be non-null) - 空值安全: 否（原始类型须非空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class ParameterizedTypeImpl implements ParameterizedType {

    private final Class<?> rawType;
    private final Type ownerType;
    private final Type[] actualTypeArguments;

    /**
     * Creates a parameterized type
     * 创建参数化类型
     *
     * @param rawType             the raw type | 原始类型
     * @param ownerType           the owner type | 所有者类型
     * @param actualTypeArguments the actual type arguments | 实际类型参数
     */
    public ParameterizedTypeImpl(Class<?> rawType, Type ownerType, Type... actualTypeArguments) {
        this.rawType = Objects.requireNonNull(rawType, "rawType must not be null");
        this.ownerType = ownerType;
        this.actualTypeArguments = actualTypeArguments != null ?
                actualTypeArguments.clone() : new Type[0];
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments.clone();
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ParameterizedType other)) return false;

        return Objects.equals(rawType, other.getRawType())
                && Objects.equals(ownerType, other.getOwnerType())
                && Arrays.equals(actualTypeArguments, other.getActualTypeArguments());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(actualTypeArguments)
                ^ Objects.hashCode(ownerType)
                ^ Objects.hashCode(rawType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (ownerType != null) {
            sb.append(TypeUtil.toString(ownerType)).append(".");
        }
        sb.append(rawType.getSimpleName());
        if (actualTypeArguments.length > 0) {
            sb.append("<");
            for (int i = 0; i < actualTypeArguments.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(TypeUtil.toString(actualTypeArguments[i]));
            }
            sb.append(">");
        }
        return sb.toString();
    }
}
