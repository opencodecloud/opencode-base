package cloud.opencode.base.core.convert;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;

/**
 * Type Utility Class - Type checking, conversion and metadata operations
 * 类型工具类 - 类型检查、转换和元数据操作
 *
 * <p>Provides utilities for type checking, primitive/wrapper conversion and type classification.</p>
 * <p>提供类型检查、原始/包装类型转换和类型分类功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type checking (isPrimitive, isWrapper, isNumber, isDate) - 类型检查</li>
 *   <li>Primitive/Wrapper conversion (wrap, unwrap) - 原始/包装类型转换</li>
 *   <li>Generic type extraction (getTypeArgument) - 泛型类型提取</li>
 *   <li>Type compatibility checking (isAssignable) - 类型兼容性检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Type checking - 类型检查
 * boolean isPrim = TypeUtil.isPrimitive(int.class);     // true
 * boolean isNum = TypeUtil.isNumber(Integer.class);     // true
 *
 * // Wrapper conversion - 包装类型转换
 * Class<?> wrapper = TypeUtil.wrap(int.class);          // Integer.class
 * Class<?> prim = TypeUtil.unwrap(Integer.class);       // int.class
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable constants) - 线程安全: 是 (不可变常量)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for type resolution - 类型解析 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class TypeUtil {

    private static final Set<Class<?>> PRIMITIVE_TYPES = Set.of(
            boolean.class, byte.class, char.class, short.class,
            int.class, long.class, float.class, double.class, void.class
    );

    private static final Set<Class<?>> WRAPPER_TYPES = Set.of(
            Boolean.class, Byte.class, Character.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, Void.class
    );

    private static final Set<Class<?>> NUMBER_TYPES = Set.of(
            byte.class, Byte.class, short.class, Short.class,
            int.class, Integer.class, long.class, Long.class,
            float.class, Float.class, double.class, Double.class,
            BigInteger.class, BigDecimal.class
    );

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class
    );

    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = Map.of(
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            Character.class, char.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class,
            Void.class, void.class
    );

    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Map.of(
            boolean.class, false,
            byte.class, (byte) 0,
            char.class, '\0',
            short.class, (short) 0,
            int.class, 0,
            long.class, 0L,
            float.class, 0.0f,
            double.class, 0.0d
    );

    private TypeUtil() {
    }

    /**
     * Checks if the type is a primitive
     * 检查是否为原始类型
     */
    public static boolean isPrimitive(Class<?> clazz) {
        return clazz != null && PRIMITIVE_TYPES.contains(clazz);
    }

    /**
     * Checks if the type is a wrapper
     * 检查是否为包装类型
     */
    public static boolean isWrapper(Class<?> clazz) {
        return clazz != null && WRAPPER_TYPES.contains(clazz);
    }

    /**
     * Checks if the type is a primitive or wrapper
     * 检查是否为原始类型或包装类型
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return isPrimitive(clazz) || isWrapper(clazz);
    }

    /**
     * Checks if the type is a number
     * 检查是否为数字类型
     */
    public static boolean isNumber(Class<?> clazz) {
        return clazz != null && (NUMBER_TYPES.contains(clazz) || Number.class.isAssignableFrom(clazz));
    }

    /**
     * Checks if the type is a collection
     * 检查是否为集合类型
     */
    public static boolean isCollection(Class<?> clazz) {
        return clazz != null && Collection.class.isAssignableFrom(clazz);
    }

    /**
     * 检查是否为 Map 类型
     */
    public static boolean isMap(Class<?> clazz) {
        return clazz != null && Map.class.isAssignableFrom(clazz);
    }

    /**
     * Checks if the type is an array
     * 检查是否为数组类型
     */
    public static boolean isArray(Class<?> clazz) {
        return clazz != null && clazz.isArray();
    }

    /**
     * Checks if the type is a String
     * 检查是否为字符串类型
     */
    public static boolean isString(Class<?> clazz) {
        return clazz == String.class || clazz == CharSequence.class;
    }

    /**
     * Checks if the type is a date/time type
     * 检查是否为日期时间类型
     */
    public static boolean isDateTime(Class<?> clazz) {
        return clazz != null && (
                clazz == LocalDate.class ||
                clazz == LocalDateTime.class ||
                clazz == LocalTime.class ||
                clazz == Instant.class ||
                clazz == ZonedDateTime.class ||
                clazz == OffsetDateTime.class ||
                clazz == Date.class
        );
    }

    /**
     * Gets the wrapper class
     * 获取包装类型
     */
    public static Class<?> getWrapperClass(Class<?> primitiveType) {
        return PRIMITIVE_TO_WRAPPER.get(primitiveType);
    }

    /**
     * Gets the primitive class
     * 获取原始类型
     */
    public static Class<?> getPrimitiveClass(Class<?> wrapperType) {
        return WRAPPER_TO_PRIMITIVE.get(wrapperType);
    }

    /**
     * Gets the default value for the type
     * 获取类型的默认值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValue(Class<T> clazz) {
        if (clazz == null) return null;
        Object defaultValue = PRIMITIVE_DEFAULTS.get(clazz);
        return defaultValue != null ? (T) defaultValue : null;
    }

    /**
     * Converts the type
     * 类型转换
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object value, Class<T> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return (T) value;

        Converter<T> converter = ConverterRegistry.getConverter(targetType);
        if (converter != null) {
            return converter.convert(value);
        }

        return null;
    }

    /**
     * 安全类型转换，返回 Optional
     */
    public static <T> Optional<T> convertOptional(Object value, Class<T> targetType) {
        return Optional.ofNullable(convert(value, targetType));
    }

    /**
     * Gets the generic type arguments
     * 获取泛型类型参数
     */
    public static Type[] getGenericTypes(Type type) {
        if (type instanceof ParameterizedType pt) {
            return pt.getActualTypeArguments();
        }
        return new Type[0];
    }

    /**
     * Gets the raw type of a generic type
     * 获取泛型的原始类型
     */
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?> c) {
            return c;
        }
        if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getRawType();
        }
        if (type instanceof GenericArrayType gat) {
            Class<?> componentType = getRawType(gat.getGenericComponentType());
            return Array.newInstance(componentType, 0).getClass();
        }
        if (type instanceof TypeVariable<?> || type instanceof WildcardType) {
            return Object.class;
        }
        return null;
    }

    /**
     * Gets the generic type of a field
     * 获取字段的泛型类型
     */
    public static Type getFieldGenericType(Field field) {
        return field.getGenericType();
    }

    /**
     * Gets the generic return type of a method
     * 获取方法返回值的泛型类型
     */
    public static Type getMethodReturnGenericType(Method method) {
        return method.getGenericReturnType();
    }

    /**
     * Gets the generic type arguments of the superclass
     * 获取父类的泛型参数
     */
    public static Type[] getSuperclassGenericTypes(Class<?> clazz) {
        Type superclass = clazz.getGenericSuperclass();
        if (superclass instanceof ParameterizedType pt) {
            return pt.getActualTypeArguments();
        }
        return new Type[0];
    }

    /**
     * Gets the generic type arguments of an interface
     * 获取接口的泛型参数
     */
    public static Type[] getInterfaceGenericTypes(Class<?> clazz, Class<?> interfaceClass) {
        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType pt) {
                if (pt.getRawType() == interfaceClass) {
                    return pt.getActualTypeArguments();
                }
            }
        }
        return new Type[0];
    }

    /**
     * Checks if the type is assignable
     * 检查类型是否可赋值
     */
    public static boolean isAssignable(Class<?> superType, Class<?> subType) {
        if (superType == null || subType == null) return false;
        if (superType.isAssignableFrom(subType)) return true;

        // 处理原始类型和包装类型
        if (superType.isPrimitive()) {
            Class<?> wrapper = getWrapperClass(superType);
            return wrapper != null && wrapper.isAssignableFrom(subType);
        }
        if (subType.isPrimitive()) {
            Class<?> wrapper = getWrapperClass(subType);
            return wrapper != null && superType.isAssignableFrom(wrapper);
        }

        return false;
    }
}
