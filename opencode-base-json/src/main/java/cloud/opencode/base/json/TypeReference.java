
package cloud.opencode.base.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Type Reference - Generic Type Token for JSON Deserialization
 * 类型引用 - 用于 JSON 反序列化的泛型类型令牌
 *
 * <p>This class captures generic type information at runtime by using
 * subclassing with an anonymous class. It solves Java's type erasure
 * problem for JSON deserialization.</p>
 * <p>此类通过使用匿名类子类化在运行时捕获泛型类型信息。
 * 它解决了 JSON 反序列化中 Java 类型擦除的问题。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Without TypeReference - loses generic type info
 * List<User> users = OpenJson.fromJson(json, List.class);  // Returns List<Object>
 *
 * // With TypeReference - preserves generic type info
 * List<User> users = OpenJson.fromJson(json, new TypeReference<List<User>>() {});
 *
 * // For Map types
 * Map<String, List<Order>> orders = OpenJson.fromJson(json,
 *     new TypeReference<Map<String, List<Order>>>() {});
 * }</pre>
 *
 * @param <T> the referenced type - 引用的类型
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Captures generic type information at runtime - 运行时捕获泛型类型信息</li>
 *   <li>Solves Java type erasure for JSON deserialization - 解决JSON反序列化中的Java类型擦除问题</li>
 *   <li>Factory methods for Class and Type creation - Class和Type创建的工厂方法</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
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
    private final Class<? super T> rawType;

    /**
     * Constructs a new type reference.
     * 构造新的类型引用。
     *
     * <p>This constructor must be called from a subclass (typically
     * an anonymous class) to capture the generic type parameter.</p>
     * <p>此构造函数必须从子类（通常是匿名类）调用以捕获泛型类型参数。</p>
     */
    @SuppressWarnings("unchecked")
    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (!(superClass instanceof ParameterizedType parameterizedType)) {
            throw new IllegalArgumentException(
                    "TypeReference must be constructed with actual type parameter. " +
                    "Use: new TypeReference<List<String>>() {}");
        }
        this.type = parameterizedType.getActualTypeArguments()[0];
        this.rawType = (Class<? super T>) extractRawType(type);
    }

    /**
     * Creates a TypeReference for a simple Class.
     * 为简单 Class 创建 TypeReference。
     *
     * @param clazz the class - 类
     * @param <T>   the type - 类型
     * @return the type reference - 类型引用
     */
    public static <T> TypeReference<T> of(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        return new SimpleTypeReference<>(clazz);
    }

    /**
     * Creates a TypeReference for a Type.
     * 为 Type 创建 TypeReference。
     *
     * @param type the type - 类型
     * @param <T>  the type - 类型
     * @return the type reference - 类型引用
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeReference<T> of(Type type) {
        Objects.requireNonNull(type, "Type must not be null");
        return new SimpleTypeReference<>((Class<T>) extractRawType(type), type);
    }

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
    public Class<? super T> getRawType() {
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

    /**
     * Extracts the raw class from a Type.
     * 从 Type 提取原始类。
     *
     * @param type the type - 类型
     * @return the raw class - 原始类
     */
    private static Class<?> extractRawType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        } else if (type instanceof ParameterizedType parameterizedType) {
            return (Class<?>) parameterizedType.getRawType();
        } else {
            return Object.class;
        }
    }

    /**
     * Simple TypeReference implementation for direct type creation.
     * 用于直接创建类型的简单 TypeReference 实现。
     */
    private static final class SimpleTypeReference<T> extends TypeReference<T> {
        private final Class<T> clazz;
        private final Type actualType;

        SimpleTypeReference(Class<T> clazz) {
            this.clazz = clazz;
            this.actualType = clazz;
        }

        SimpleTypeReference(Class<T> clazz, Type actualType) {
            this.clazz = clazz;
            this.actualType = actualType;
        }

        @Override
        public Type getType() {
            return actualType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<? super T> getRawType() {
            return clazz;
        }
    }
}
