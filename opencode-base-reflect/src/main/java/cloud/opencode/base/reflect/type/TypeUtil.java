package cloud.opencode.base.reflect.type;

import java.lang.reflect.*;
import java.util.*;

/**
 * Type Utility Class
 * 类型工具类
 *
 * <p>Utility class providing common type operations including type resolution,
 * primitive/wrapper conversion, and type string formatting.</p>
 * <p>提供常用类型操作的工具类，包括类型解析、原始/包装类型转换和类型字符串格式化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Raw type extraction - 原始类型提取</li>
 *   <li>Primitive/wrapper conversion - 原始/包装类型转换</li>
 *   <li>Type assignability checking - 类型可赋值检查</li>
 *   <li>Type string formatting - 类型字符串格式化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Class<?> raw = TypeUtil.getRawType(parameterizedType);
 * boolean assignable = TypeUtil.isAssignable(targetType, sourceType);
 * String formatted = TypeUtil.toString(genericType);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class, immutable maps) - 线程安全: 是（无状态工具类，不可变映射）</li>
 *   <li>Null-safe: No (caller must ensure non-null type arguments) - 空值安全: 否（调用方须确保非空类型参数）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for type lookups using immutable maps; toString() O(n) where n is the number of type arguments - 时间复杂度: 使用不可变映射的类型查找为 O(1)；toString() 为 O(n)，n为类型参数数量</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class TypeUtil {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER;
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE;

    static {
        Map<Class<?>, Class<?>> p2w = new HashMap<>();
        p2w.put(boolean.class, Boolean.class);
        p2w.put(byte.class, Byte.class);
        p2w.put(char.class, Character.class);
        p2w.put(short.class, Short.class);
        p2w.put(int.class, Integer.class);
        p2w.put(long.class, Long.class);
        p2w.put(float.class, Float.class);
        p2w.put(double.class, Double.class);
        p2w.put(void.class, Void.class);
        PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(p2w);

        Map<Class<?>, Class<?>> w2p = new HashMap<>();
        for (Map.Entry<Class<?>, Class<?>> entry : p2w.entrySet()) {
            w2p.put(entry.getValue(), entry.getKey());
        }
        WRAPPER_TO_PRIMITIVE = Collections.unmodifiableMap(w2p);
    }

    private TypeUtil() {
    }

    // ==================== Raw Type | 原始类型 ====================

    /**
     * Gets the raw type from a Type
     * 从Type获取原始类型
     *
     * @param type the type | 类型
     * @return the raw class | 原始类
     */
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getRawType();
        }
        if (type instanceof GenericArrayType gat) {
            Type componentType = gat.getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof TypeVariable<?>) {
            return Object.class;
        }
        if (type instanceof WildcardType wt) {
            Type[] upperBounds = wt.getUpperBounds();
            if (upperBounds.length > 0) {
                return getRawType(upperBounds[0]);
            }
            return Object.class;
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }

    // ==================== Primitive/Wrapper | 原始/包装类型 ====================

    /**
     * Checks if type is primitive
     * 检查是否为原始类型
     *
     * @param type the type | 类型
     * @return true if primitive | 如果是原始类型返回true
     */
    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive();
    }

    /**
     * Checks if type is wrapper
     * 检查是否为包装类型
     *
     * @param type the type | 类型
     * @return true if wrapper | 如果是包装类型返回true
     */
    public static boolean isWrapper(Class<?> type) {
        return WRAPPER_TO_PRIMITIVE.containsKey(type);
    }

    /**
     * Converts primitive to wrapper
     * 将原始类型转换为包装类型
     *
     * @param type the primitive type | 原始类型
     * @return the wrapper type | 包装类型
     */
    public static Class<?> wrap(Class<?> type) {
        return PRIMITIVE_TO_WRAPPER.getOrDefault(type, type);
    }

    /**
     * Converts wrapper to primitive
     * 将包装类型转换为原始类型
     *
     * @param type the wrapper type | 包装类型
     * @return the primitive type or original if not wrapper | 原始类型，如果不是包装类型则返回原类型
     */
    public static Class<?> unwrap(Class<?> type) {
        return WRAPPER_TO_PRIMITIVE.getOrDefault(type, type);
    }

    // ==================== Assignability | 可赋值性 ====================

    /**
     * Checks if target type is assignable from source type
     * 检查目标类型是否可从源类型赋值
     *
     * @param target the target type | 目标类型
     * @param source the source type | 源类型
     * @return true if assignable | 如果可赋值返回true
     */
    public static boolean isAssignableFrom(Type target, Type source) {
        if (target.equals(source)) {
            return true;
        }

        Class<?> targetRaw = getRawType(target);
        Class<?> sourceRaw = getRawType(source);

        if (!targetRaw.isAssignableFrom(sourceRaw)) {
            return false;
        }

        if (!(target instanceof ParameterizedType targetPt)) {
            return true;
        }

        if (!(source instanceof ParameterizedType sourcePt)) {
            return true;
        }

        Type[] targetArgs = targetPt.getActualTypeArguments();
        Type[] sourceArgs = sourcePt.getActualTypeArguments();

        if (targetArgs.length != sourceArgs.length) {
            return false;
        }

        for (int i = 0; i < targetArgs.length; i++) {
            if (!isTypeArgumentAssignable(targetArgs[i], sourceArgs[i])) {
                return false;
            }
        }

        return true;
    }

    private static boolean isTypeArgumentAssignable(Type target, Type source) {
        if (target.equals(source)) {
            return true;
        }

        if (target instanceof WildcardType wt) {
            Type[] upperBounds = wt.getUpperBounds();
            Type[] lowerBounds = wt.getLowerBounds();

            for (Type upper : upperBounds) {
                if (!isAssignableFrom(upper, source)) {
                    return false;
                }
            }

            for (Type lower : lowerBounds) {
                if (!isAssignableFrom(source, lower)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    // ==================== Type String | 类型字符串 ====================

    /**
     * Converts Type to readable string
     * 将Type转换为可读字符串
     *
     * @param type the type | 类型
     * @return string representation | 字符串表示
     */
    public static String toString(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz.getSimpleName();
        }
        if (type instanceof ParameterizedType pt) {
            StringBuilder sb = new StringBuilder();
            sb.append(toString(pt.getRawType()));
            sb.append("<");
            Type[] args = pt.getActualTypeArguments();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(toString(args[i]));
            }
            sb.append(">");
            return sb.toString();
        }
        if (type instanceof GenericArrayType gat) {
            return toString(gat.getGenericComponentType()) + "[]";
        }
        if (type instanceof WildcardType wt) {
            Type[] upperBounds = wt.getUpperBounds();
            Type[] lowerBounds = wt.getLowerBounds();
            if (lowerBounds.length > 0) {
                return "? super " + toString(lowerBounds[0]);
            }
            if (upperBounds.length > 0 && !upperBounds[0].equals(Object.class)) {
                return "? extends " + toString(upperBounds[0]);
            }
            return "?";
        }
        if (type instanceof TypeVariable<?> tv) {
            return tv.getName();
        }
        return type.toString();
    }

    /**
     * Gets type parameters from a class
     * 从类获取类型参数
     *
     * @param clazz the class | 类
     * @return array of type variables | 类型变量数组
     */
    public static TypeVariable<?>[] getTypeParameters(Class<?> clazz) {
        return clazz.getTypeParameters();
    }

    /**
     * Gets actual type arguments from a parameterized type
     * 从参数化类型获取实际类型参数
     *
     * @param type the type | 类型
     * @return array of type arguments or empty array | 类型参数数组或空数组
     */
    public static Type[] getActualTypeArguments(Type type) {
        if (type instanceof ParameterizedType pt) {
            return pt.getActualTypeArguments();
        }
        return new Type[0];
    }

    /**
     * Checks if two types are equal considering type parameters
     * 检查两个类型是否相等（考虑类型参数）
     *
     * @param type1 the first type | 第一个类型
     * @param type2 the second type | 第二个类型
     * @return true if equal | 如果相等返回true
     */
    public static boolean equals(Type type1, Type type2) {
        if (type1 == type2) {
            return true;
        }
        if (type1 == null || type2 == null) {
            return false;
        }
        if (type1 instanceof Class && type2 instanceof Class) {
            return type1.equals(type2);
        }
        if (type1 instanceof ParameterizedType pt1 && type2 instanceof ParameterizedType pt2) {
            if (!equals(pt1.getRawType(), pt2.getRawType())) {
                return false;
            }
            Type[] args1 = pt1.getActualTypeArguments();
            Type[] args2 = pt2.getActualTypeArguments();
            if (args1.length != args2.length) {
                return false;
            }
            for (int i = 0; i < args1.length; i++) {
                if (!equals(args1[i], args2[i])) {
                    return false;
                }
            }
            return true;
        }
        return type1.equals(type2);
    }
}
