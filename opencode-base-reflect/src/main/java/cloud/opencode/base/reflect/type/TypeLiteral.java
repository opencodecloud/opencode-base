package cloud.opencode.base.reflect.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Type Literal (Alternative to TypeToken)
 * 类型字面量（TypeToken的替代）
 *
 * <p>A simpler alternative to TypeToken for capturing generic types.
 * Similar to Commons Lang TypeLiteral.</p>
 * <p>用于捕获泛型类型的更简单的TypeToken替代方案。
 * 类似于Commons Lang TypeLiteral。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TypeLiteral<List<String>> type = new TypeLiteral<List<String>>() {};
 * Type actualType = type.getType();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Generic type capture via anonymous subclass - 通过匿名子类捕获泛型类型</li>
 *   <li>Raw type extraction - 原始类型提取</li>
 *   <li>Type equality comparison - 类型相等比较</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No (must be created with type parameter) - 空值安全: 否（必须带类型参数创建）</li>
 * </ul>
 *
 * @param <T> the type to capture | 要捕获的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public abstract class TypeLiteral<T> {

    private final Type type;
    private final Class<? super T> rawType;

    /**
     * Creates a TypeLiteral by capturing generic type
     * 通过捕获泛型类型创建TypeLiteral
     */
    @SuppressWarnings("unchecked")
    protected TypeLiteral() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType pt) {
            this.type = pt.getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException("TypeLiteral must be created with a type parameter");
        }
        this.rawType = (Class<? super T>) TypeUtil.getRawType(this.type);
    }

    /**
     * Gets the captured Type
     * 获取捕获的Type
     *
     * @return the Type | 类型
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the raw type
     * 获取原始类型
     *
     * @return the raw class | 原始类
     */
    public Class<? super T> getRawType() {
        return rawType;
    }

    /**
     * Converts to TypeToken
     * 转换为TypeToken
     *
     * @return TypeToken | TypeToken
     */
    public TypeToken<T> toTypeToken() {
        return new TypeToken<T>() {
            @Override
            public Type getType() {
                return type;
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TypeLiteral<?> other)) return false;
        return type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return TypeUtil.toString(type);
    }
}
