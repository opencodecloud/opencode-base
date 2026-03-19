package cloud.opencode.base.reflect.type;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

/**
 * Generic Type Token (Similar to Guava TypeToken)
 * 泛型类型令牌（对标 Guava TypeToken）
 *
 * <p>Captures and preserves generic type information at runtime,
 * solving Java's type erasure problem.</p>
 * <p>在运行时捕获和保留泛型类型信息，解决Java的类型擦除问题。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Runtime generic type capture - 运行时泛型类型捕获</li>
 *   <li>Type parameter extraction - 类型参数提取</li>
 *   <li>Type relationship checking - 类型关系检查</li>
 *   <li>Field/Method type resolution - 字段/方法类型解析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Capture generic type via anonymous subclass
 * TypeToken<List<String>> listType = new TypeToken<List<String>>() {};
 *
 * // Get raw type
 * Class<?> rawType = listType.getRawType(); // List.class
 *
 * // Get type parameter
 * TypeToken<?> elementType = listType.getTypeParameter(0); // String
 *
 * // Convenience methods
 * TypeToken<List<Integer>> intList = TypeToken.listOf(Integer.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param <T> the type to capture | 要捕获的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public abstract class TypeToken<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Type type;
    private final Class<? super T> rawType;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Creates a TypeToken by capturing generic type from anonymous subclass
     * 通过匿名子类捕获泛型类型创建TypeToken
     */
    @SuppressWarnings("unchecked")
    protected TypeToken() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType pt) {
            this.type = pt.getActualTypeArguments()[0];
        } else {
            throw new OpenReflectException("TypeToken must be created with a type parameter");
        }
        this.rawType = (Class<? super T>) TypeUtil.getRawType(this.type);
    }

    @SuppressWarnings("unchecked")
    private TypeToken(Type type) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.rawType = (Class<? super T>) TypeUtil.getRawType(type);
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a TypeToken from Class
     * 从Class创建TypeToken
     *
     * @param type the class | 类
     * @param <T>  the type | 类型
     * @return TypeToken instance | TypeToken实例
     */
    public static <T> TypeToken<T> of(Class<T> type) {
        return new SimpleTypeToken<>(type);
    }

    /**
     * Creates a TypeToken from Type
     * 从Type创建TypeToken
     *
     * @param type the type | 类型
     * @return TypeToken instance | TypeToken实例
     */
    public static TypeToken<?> of(Type type) {
        return new SimpleTypeToken<>(type);
    }

    // ==================== Getters | 获取方法 ====================

    /**
     * Gets the underlying Type
     * 获取底层Type
     *
     * @return the Type | 类型
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the raw type (without generic parameters)
     * 获取原始类型（无泛型参数）
     *
     * @return the raw type | 原始类型
     */
    public Class<? super T> getRawType() {
        return rawType;
    }

    /**
     * Gets the type parameter at the specified index
     * 获取指定索引的类型参数
     *
     * @param index the index | 索引
     * @return the type parameter | 类型参数
     */
    public TypeToken<?> getTypeParameter(int index) {
        if (type instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (index >= 0 && index < args.length) {
                return of(args[index]);
            }
        }
        throw new OpenReflectException(String.format(
                "No type parameter at index %d for type %s", index, type));
    }

    /**
     * Gets all type parameters
     * 获取所有类型参数
     *
     * @return list of type parameters | 类型参数列表
     */
    public List<TypeToken<?>> getTypeParameters() {
        if (type instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            List<TypeToken<?>> result = new ArrayList<>(args.length);
            for (Type arg : args) {
                result.add(of(arg));
            }
            return Collections.unmodifiableList(result);
        }
        return Collections.emptyList();
    }

    /**
     * Gets the component type (for arrays)
     * 获取组件类型（用于数组）
     *
     * @return the component type or null | 组件类型或null
     */
    public TypeToken<?> getComponentType() {
        if (type instanceof GenericArrayType gat) {
            return of(gat.getGenericComponentType());
        }
        if (rawType.isArray()) {
            return of(rawType.getComponentType());
        }
        return null;
    }

    // ==================== Type Checking | 类型判断 ====================

    /**
     * Checks if this is a primitive type
     * 检查是否为原始类型
     *
     * @return true if primitive | 如果是原始类型返回true
     */
    public boolean isPrimitive() {
        return rawType.isPrimitive();
    }

    /**
     * Checks if this is an array type
     * 检查是否为数组类型
     *
     * @return true if array | 如果是数组返回true
     */
    public boolean isArray() {
        return rawType.isArray() || type instanceof GenericArrayType;
    }

    /**
     * Checks if this is a parameterized type
     * 检查是否为参数化类型
     *
     * @return true if parameterized | 如果是参数化类型返回true
     */
    public boolean isParameterized() {
        return type instanceof ParameterizedType;
    }

    /**
     * Checks if this is a wildcard type
     * 检查是否为通配符类型
     *
     * @return true if wildcard | 如果是通配符返回true
     */
    public boolean isWildcard() {
        return type instanceof WildcardType;
    }

    /**
     * Checks if this is a type variable
     * 检查是否为类型变量
     *
     * @return true if type variable | 如果是类型变量返回true
     */
    public boolean isTypeVariable() {
        return type instanceof TypeVariable;
    }

    /**
     * Checks if this type is a supertype of the specified type
     * 检查此类型是否为指定类型的父类型
     *
     * @param other the other type | 另一类型
     * @return true if supertype | 如果是父类型返回true
     */
    public boolean isSupertypeOf(TypeToken<?> other) {
        return TypeUtil.isAssignableFrom(this.type, other.type);
    }

    /**
     * Checks if this type is a subtype of the specified type
     * 检查此类型是否为指定类型的子类型
     *
     * @param other the other type | 另一类型
     * @return true if subtype | 如果是子类型返回true
     */
    public boolean isSubtypeOf(TypeToken<?> other) {
        return TypeUtil.isAssignableFrom(other.type, this.type);
    }

    /**
     * Checks if this type is assignable from the specified type
     * 检查此类型是否可从指定类型赋值
     *
     * @param other the other type | 另一类型
     * @return true if assignable | 如果可赋值返回true
     */
    public boolean isAssignableFrom(TypeToken<?> other) {
        return isSupertypeOf(other);
    }

    // ==================== Type Operations | 类型操作 ====================

    /**
     * Resolves a type in the context of this type
     * 在此类型上下文中解析类型
     *
     * @param toResolve the type to resolve | 要解析的类型
     * @return resolved TypeToken | 解析后的TypeToken
     */
    public TypeToken<?> resolveType(Type toResolve) {
        return of(TypeResolver.resolveType(type, toResolve));
    }

    /**
     * Gets the wrapper type (for primitives)
     * 获取包装类型（对于原始类型）
     *
     * @return wrapper TypeToken | 包装类型TypeToken
     */
    @SuppressWarnings("unchecked")
    public TypeToken<T> wrap() {
        if (isPrimitive()) {
            return (TypeToken<T>) of(TypeUtil.wrap(rawType));
        }
        return this;
    }

    /**
     * Gets the primitive type (for wrappers)
     * 获取原始类型（对于包装类型）
     *
     * @return primitive TypeToken | 原始类型TypeToken
     */
    @SuppressWarnings("unchecked")
    public TypeToken<T> unwrap() {
        Class<?> unwrapped = TypeUtil.unwrap(rawType);
        if (unwrapped != rawType) {
            return (TypeToken<T>) of(unwrapped);
        }
        return this;
    }

    // ==================== Field/Method Type Resolution | 字段/方法类型解析 ====================

    /**
     * Resolves field type in context of this type
     * 在此类型上下文中解析字段类型
     *
     * @param field the field | 字段
     * @return resolved TypeToken | 解析后的TypeToken
     */
    public TypeToken<?> resolveFieldType(Field field) {
        return resolveType(field.getGenericType());
    }

    /**
     * Resolves method return type in context of this type
     * 在此类型上下文中解析方法返回类型
     *
     * @param method the method | 方法
     * @return resolved TypeToken | 解析后的TypeToken
     */
    public TypeToken<?> resolveReturnType(Method method) {
        return resolveType(method.getGenericReturnType());
    }

    /**
     * Resolves method parameter types in context of this type
     * 在此类型上下文中解析方法参数类型
     *
     * @param method the method | 方法
     * @return list of resolved TypeTokens | 解析后的TypeToken列表
     */
    public List<TypeToken<?>> resolveParameterTypes(Method method) {
        Type[] paramTypes = method.getGenericParameterTypes();
        List<TypeToken<?>> result = new ArrayList<>(paramTypes.length);
        for (Type paramType : paramTypes) {
            result.add(resolveType(paramType));
        }
        return Collections.unmodifiableList(result);
    }

    // ==================== Convenience Factory Methods | 便捷工厂方法 ====================

    /**
     * Creates a List&lt;E&gt; TypeToken
     * 创建 List&lt;E&gt; TypeToken
     *
     * @param elementType the element type | 元素类型
     * @param <E>         the element type | 元素类型
     * @return TypeToken for List&lt;E&gt; | List&lt;E&gt;的TypeToken
     */
    public static <E> TypeToken<List<E>> listOf(TypeToken<E> elementType) {
        return new SimpleTypeToken<>(new ParameterizedTypeImpl(List.class, null, elementType.getType()));
    }

    /**
     * Creates a List&lt;E&gt; TypeToken
     * 创建 List&lt;E&gt; TypeToken
     *
     * @param elementType the element class | 元素类
     * @param <E>         the element type | 元素类型
     * @return TypeToken for List&lt;E&gt; | List&lt;E&gt;的TypeToken
     */
    public static <E> TypeToken<List<E>> listOf(Class<E> elementType) {
        return listOf(of(elementType));
    }

    /**
     * Creates a Set&lt;E&gt; TypeToken
     * 创建 Set&lt;E&gt; TypeToken
     *
     * @param elementType the element type | 元素类型
     * @param <E>         the element type | 元素类型
     * @return TypeToken for Set&lt;E&gt; | Set&lt;E&gt;的TypeToken
     */
    public static <E> TypeToken<Set<E>> setOf(TypeToken<E> elementType) {
        return new SimpleTypeToken<>(new ParameterizedTypeImpl(Set.class, null, elementType.getType()));
    }

    /**
     * Creates a Set&lt;E&gt; TypeToken
     * 创建 Set&lt;E&gt; TypeToken
     *
     * @param elementType the element class | 元素类
     * @param <E>         the element type | 元素类型
     * @return TypeToken for Set&lt;E&gt; | Set&lt;E&gt;的TypeToken
     */
    public static <E> TypeToken<Set<E>> setOf(Class<E> elementType) {
        return setOf(of(elementType));
    }

    /**
     * Creates a Map&lt;K,V&gt; TypeToken
     * 创建 Map&lt;K,V&gt; TypeToken
     *
     * @param keyType   the key type | 键类型
     * @param valueType the value type | 值类型
     * @param <K>       the key type | 键类型
     * @param <V>       the value type | 值类型
     * @return TypeToken for Map&lt;K,V&gt; | Map&lt;K,V&gt;的TypeToken
     */
    public static <K, V> TypeToken<Map<K, V>> mapOf(TypeToken<K> keyType, TypeToken<V> valueType) {
        return new SimpleTypeToken<>(new ParameterizedTypeImpl(Map.class, null,
                keyType.getType(), valueType.getType()));
    }

    /**
     * Creates a Map&lt;K,V&gt; TypeToken
     * 创建 Map&lt;K,V&gt; TypeToken
     *
     * @param keyType   the key class | 键类
     * @param valueType the value class | 值类
     * @param <K>       the key type | 键类型
     * @param <V>       the value type | 值类型
     * @return TypeToken for Map&lt;K,V&gt; | Map&lt;K,V&gt;的TypeToken
     */
    public static <K, V> TypeToken<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType) {
        return mapOf(of(keyType), of(valueType));
    }

    /**
     * Creates an Optional&lt;T&gt; TypeToken
     * 创建 Optional&lt;T&gt; TypeToken
     *
     * @param valueType the value type | 值类型
     * @param <T>       the value type | 值类型
     * @return TypeToken for Optional&lt;T&gt; | Optional&lt;T&gt;的TypeToken
     */
    public static <T> TypeToken<Optional<T>> optionalOf(TypeToken<T> valueType) {
        return new SimpleTypeToken<>(new ParameterizedTypeImpl(Optional.class, null, valueType.getType()));
    }

    /**
     * Creates an Optional&lt;T&gt; TypeToken
     * 创建 Optional&lt;T&gt; TypeToken
     *
     * @param valueType the value class | 值类
     * @param <T>       the value type | 值类型
     * @return TypeToken for Optional&lt;T&gt; | Optional&lt;T&gt;的TypeToken
     */
    public static <T> TypeToken<Optional<T>> optionalOf(Class<T> valueType) {
        return optionalOf(of(valueType));
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TypeToken<?> other)) return false;
        return type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return TypeUtil.toString(type);
    }

    // ==================== Inner Class | 内部类 ====================

    private static final class SimpleTypeToken<T> extends TypeToken<T> {
        private static final long serialVersionUID = 1L;

        SimpleTypeToken(Type type) {
            super(type);
        }
    }
}
