package cloud.opencode.base.reflect.type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Generic Array Type Implementation
 * 泛型数组类型实现
 *
 * <p>Implementation of GenericArrayType for runtime type construction.</p>
 * <p>用于运行时类型构造的GenericArrayType实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>GenericArrayType runtime construction - GenericArrayType运行时构造</li>
 *   <li>Proper equals/hashCode/toString - 正确的equals/hashCode/toString</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Type listOfString = new ParameterizedTypeImpl(List.class, null, String.class);
 * GenericArrayType arrayType = new GenericArrayTypeImpl(listOfString);
 * // Represents List<String>[]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No (component type must be non-null) - 空值安全: 否（组件类型须非空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class GenericArrayTypeImpl implements GenericArrayType {

    private final Type genericComponentType;

    /**
     * Creates a generic array type
     * 创建泛型数组类型
     *
     * @param genericComponentType the component type | 组件类型
     */
    public GenericArrayTypeImpl(Type genericComponentType) {
        this.genericComponentType = Objects.requireNonNull(genericComponentType,
                "genericComponentType must not be null");
    }

    @Override
    public Type getGenericComponentType() {
        return genericComponentType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GenericArrayType other)) return false;
        return genericComponentType.equals(other.getGenericComponentType());
    }

    @Override
    public int hashCode() {
        return genericComponentType.hashCode();
    }

    @Override
    public String toString() {
        return TypeUtil.toString(genericComponentType) + "[]";
    }
}
