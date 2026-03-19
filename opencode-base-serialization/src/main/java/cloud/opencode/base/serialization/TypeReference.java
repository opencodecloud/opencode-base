
package cloud.opencode.base.serialization;

import java.lang.reflect.*;
import java.util.*;

/**
 * TypeReference - Generic Type Reference for Deserialization
 * 类型引用 - 用于反序列化的泛型类型引用
 *
 * <p>This class captures generic type information at runtime by using subclassing with an anonymous class.
 * It solves Java's type erasure problem for deserialization.</p>
 * <p>此类通过使用匿名类子类化在运行时捕获泛型类型信息。
 * 它解决了反序列化中 Java 类型擦除的问题。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Preserve generic type information - 保留泛型类型信息</li>
 *   <li>Factory methods for common types - 常用类型的工厂方法</li>
 *   <li>Support for nested generics - 支持嵌套泛型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Anonymous subclass pattern
 * TypeReference<List<User>> listType = new TypeReference<List<User>>() {};
 *
 * // Factory methods
 * TypeReference<List<String>> stringList = TypeReference.listOf(String.class);
 * TypeReference<Map<String, Integer>> map = TypeReference.mapOf(String.class, Integer.class);
 *
 * // Usage with deserialize
 * List<User> users = OpenSerializer.deserialize(data, new TypeReference<List<User>>() {});
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 * </ul>
 *
 * @param <T> the referenced type - 引用的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public abstract class TypeReference<T> {

    /**
     * The referenced type
     * 引用的类型
     */
    private final Type type;

    /**
     * The raw class of the referenced type
     * 引用类型的原始类
     */
    private final Class<?> rawType;

    /**
     * Constructs a new type reference.
     * 构造新的类型引用。
     *
     * <p>This constructor must be called from a subclass (typically an anonymous class)
     * to capture the generic type parameter.</p>
     * <p>此构造函数必须从子类（通常是匿名类）调用以捕获泛型类型参数。</p>
     */
    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (!(superClass instanceof ParameterizedType parameterizedType)) {
            throw new IllegalArgumentException(
                    "TypeReference must be parameterized. Use: new TypeReference<List<String>>() {}");
        }
        this.type = parameterizedType.getActualTypeArguments()[0];
        this.rawType = extractRawType(this.type);
    }

    /**
     * Internal constructor for factory methods.
     * 工厂方法的内部构造函数。
     */
    private TypeReference(Type type) {
        this.type = type;
        this.rawType = extractRawType(type);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a TypeReference from a Class.
     * 从 Class 创建 TypeReference。
     *
     * @param clazz the class - 类
     * @param <T>   the type - 类型
     * @return the type reference - 类型引用
     */
    public static <T> TypeReference<T> of(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        return new TypeReference<>(clazz) {};
    }

    /**
     * Creates a TypeReference from a Type.
     * 从 Type 创建 TypeReference。
     *
     * @param type the type - 类型
     * @param <T>  the type - 类型
     * @return the type reference - 类型引用
     */
    public static <T> TypeReference<T> of(Type type) {
        Objects.requireNonNull(type, "Type must not be null");
        return new TypeReference<>(type) {};
    }

    /**
     * Creates a TypeReference for List type.
     * 为 List 类型创建 TypeReference。
     *
     * @param elementType the element type - 元素类型
     * @param <T>         the element type - 元素类型
     * @return the type reference - 类型引用
     */
    public static <T> TypeReference<List<T>> listOf(Class<T> elementType) {
        Objects.requireNonNull(elementType, "Element type must not be null");
        return new TypeReference<>(
                new ParameterizedTypeImpl(List.class, new Type[]{elementType})
        ) {};
    }

    /**
     * Creates a TypeReference for Set type.
     * 为 Set 类型创建 TypeReference。
     *
     * @param elementType the element type - 元素类型
     * @param <T>         the element type - 元素类型
     * @return the type reference - 类型引用
     */
    public static <T> TypeReference<Set<T>> setOf(Class<T> elementType) {
        Objects.requireNonNull(elementType, "Element type must not be null");
        return new TypeReference<>(
                new ParameterizedTypeImpl(Set.class, new Type[]{elementType})
        ) {};
    }

    /**
     * Creates a TypeReference for Map type.
     * 为 Map 类型创建 TypeReference。
     *
     * @param keyType   the key type - 键类型
     * @param valueType the value type - 值类型
     * @param <K>       the key type - 键类型
     * @param <V>       the value type - 值类型
     * @return the type reference - 类型引用
     */
    public static <K, V> TypeReference<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType) {
        Objects.requireNonNull(keyType, "Key type must not be null");
        Objects.requireNonNull(valueType, "Value type must not be null");
        return new TypeReference<>(
                new ParameterizedTypeImpl(Map.class, new Type[]{keyType, valueType})
        ) {};
    }

    /**
     * Creates a TypeReference for Collection type.
     * 为 Collection 类型创建 TypeReference。
     *
     * @param elementType the element type - 元素类型
     * @param <T>         the element type - 元素类型
     * @return the type reference - 类型引用
     */
    public static <T> TypeReference<Collection<T>> collectionOf(Class<T> elementType) {
        Objects.requireNonNull(elementType, "Element type must not be null");
        return new TypeReference<>(
                new ParameterizedTypeImpl(Collection.class, new Type[]{elementType})
        ) {};
    }

    /**
     * Creates a TypeReference for Optional type.
     * 为 Optional 类型创建 TypeReference。
     *
     * @param elementType the element type - 元素类型
     * @param <T>         the element type - 元素类型
     * @return the type reference - 类型引用
     */
    public static <T> TypeReference<Optional<T>> optionalOf(Class<T> elementType) {
        Objects.requireNonNull(elementType, "Element type must not be null");
        return new TypeReference<>(
                new ParameterizedTypeImpl(Optional.class, new Type[]{elementType})
        ) {};
    }

    // ==================== Getter Methods | 获取方法 ====================

    /**
     * Returns the referenced type.
     * 返回引用的类型。
     *
     * @return the type - 类型
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the raw class of the referenced type.
     * 返回引用类型的原始类。
     *
     * @return the raw class - 原始类
     */
    public Class<?> getRawType() {
        return rawType;
    }

    /**
     * Returns whether this is a parameterized type.
     * 返回此是否为参数化类型。
     *
     * @return true if parameterized - 如果是参数化类型则返回 true
     */
    public boolean isParameterized() {
        return type instanceof ParameterizedType;
    }

    /**
     * Returns the type arguments if this is a parameterized type.
     * 如果是参数化类型则返回类型参数。
     *
     * @return the type arguments, or empty array - 类型参数，或空数组
     */
    public Type[] getTypeArguments() {
        if (type instanceof ParameterizedType parameterizedType) {
            return parameterizedType.getActualTypeArguments();
        }
        return new Type[0];
    }

    // ==================== Object Methods | 对象方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeReference<?> that)) return false;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return "TypeReference<" + type.getTypeName() + ">";
    }

    // ==================== Helper Methods | 辅助方法 ====================

    /**
     * Extracts the raw class from a Type.
     * 从 Type 提取原始类。
     */
    private static Class<?> extractRawType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        } else if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getRawType();
        } else if (type instanceof GenericArrayType gat) {
            Class<?> componentType = extractRawType(gat.getGenericComponentType());
            return Array.newInstance(componentType, 0).getClass();
        } else if (type instanceof TypeVariable<?>) {
            return Object.class;
        } else if (type instanceof WildcardType wt) {
            Type[] upperBounds = wt.getUpperBounds();
            if (upperBounds.length > 0) {
                return extractRawType(upperBounds[0]);
            }
            return Object.class;
        }
        return Object.class;
    }

    /**
     * ParameterizedType implementation for factory methods.
     * 用于工厂方法的 ParameterizedType 实现。
     */
    private record ParameterizedTypeImpl(
            Class<?> rawType,
            Type[] actualTypeArguments
    ) implements ParameterizedType {

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
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ParameterizedType other)) return false;
            if (!rawType.equals(other.getRawType())) return false;
            return Arrays.equals(actualTypeArguments, other.getActualTypeArguments());
        }

        @Override
        public int hashCode() {
            return Objects.hash(rawType, Arrays.hashCode(actualTypeArguments));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(rawType.getName());
            if (actualTypeArguments.length > 0) {
                sb.append('<');
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(actualTypeArguments[i].getTypeName());
                }
                sb.append('>');
            }
            return sb.toString();
        }
    }
}
