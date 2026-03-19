package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.*;
import java.util.*;

/**
 * Reflection Utility Class
 * 反射工具类
 *
 * <p>Provides low-level reflection utility methods.</p>
 * <p>提供底层反射工具方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Accessibility management - 可访问性管理</li>
 *   <li>Method/field/constructor lookup - 方法/字段/构造器查找</li>
 *   <li>Reflection invocation wrappers - 反射调用包装</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ReflectUtil.makeAccessible(field);
 * Object value = ReflectUtil.getFieldValue(obj, field);
 * ReflectUtil.invokeMethod(obj, method, args);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per operation (accessibility set, field get/set, method invoke) - 时间复杂度: 每次操作 O(1)（可访问性设置、字段读写、方法调用）</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class ReflectUtil {

    private ReflectUtil() {
    }

    // ==================== Accessibility | 可访问性 ====================

    /**
     * Makes accessible object accessible
     * 设置可访问性
     *
     * @param accessible the accessible object | 可访问对象
     * @param <T>        the type | 类型
     * @return the accessible object | 可访问对象
     */
    public static <T extends AccessibleObject> T setAccessible(T accessible) {
        if (accessible != null && !accessible.canAccess(null)) {
            accessible.setAccessible(true);
        }
        return accessible;
    }

    /**
     * Makes accessible object accessible with target check
     * 设置可访问性（检查目标）
     *
     * @param accessible the accessible object | 可访问对象
     * @param target     the target object | 目标对象
     * @param <T>        the type | 类型
     * @return the accessible object | 可访问对象
     */
    public static <T extends AccessibleObject> T setAccessible(T accessible, Object target) {
        if (accessible != null && !accessible.canAccess(target)) {
            accessible.setAccessible(true);
        }
        return accessible;
    }

    /**
     * Checks if accessible
     * 检查是否可访问
     *
     * @param accessible the accessible object | 可访问对象
     * @param target     the target object | 目标对象
     * @return true if accessible | 如果可访问返回true
     */
    public static boolean isAccessible(AccessibleObject accessible, Object target) {
        return accessible.canAccess(target);
    }

    // ==================== Member Operations | 成员操作 ====================

    /**
     * Gets declaring class of member
     * 获取成员的声明类
     *
     * @param member the member | 成员
     * @return the declaring class | 声明类
     */
    public static Class<?> getDeclaringClass(Member member) {
        return member.getDeclaringClass();
    }

    /**
     * Gets member name
     * 获取成员名称
     *
     * @param member the member | 成员
     * @return the name | 名称
     */
    public static String getName(Member member) {
        return member.getName();
    }

    /**
     * Gets member modifiers
     * 获取成员修饰符
     *
     * @param member the member | 成员
     * @return the modifiers | 修饰符
     */
    public static int getModifiers(Member member) {
        return member.getModifiers();
    }

    // ==================== Type Resolution | 类型解析 ====================

    /**
     * Resolves generic return type of method
     * 解析方法的泛型返回类型
     *
     * @param method      the method | 方法
     * @param targetClass the target class | 目标类
     * @return the resolved type | 解析后的类型
     */
    public static Type resolveReturnType(Method method, Class<?> targetClass) {
        Type genericReturnType = method.getGenericReturnType();
        return resolveType(genericReturnType, targetClass);
    }

    /**
     * Resolves generic parameter types
     * 解析泛型参数类型
     *
     * @param executable  the executable | 可执行对象
     * @param targetClass the target class | 目标类
     * @return the resolved types | 解析后的类型数组
     */
    public static Type[] resolveParameterTypes(Executable executable, Class<?> targetClass) {
        Type[] genericTypes = executable.getGenericParameterTypes();
        Type[] resolvedTypes = new Type[genericTypes.length];
        for (int i = 0; i < genericTypes.length; i++) {
            resolvedTypes[i] = resolveType(genericTypes[i], targetClass);
        }
        return resolvedTypes;
    }

    /**
     * Resolves type variable in context of target class
     * 在目标类上下文中解析类型变量
     *
     * @param type        the type | 类型
     * @param targetClass the target class | 目标类
     * @return the resolved type | 解析后的类型
     */
    public static Type resolveType(Type type, Class<?> targetClass) {
        if (type instanceof TypeVariable<?> typeVar) {
            return resolveTypeVariable(typeVar, targetClass);
        }
        if (type instanceof ParameterizedType paramType) {
            return resolveParameterizedType(paramType, targetClass);
        }
        if (type instanceof GenericArrayType arrayType) {
            Type componentType = resolveType(arrayType.getGenericComponentType(), targetClass);
            return new GenericArrayType() {
                @Override
                public Type getGenericComponentType() {
                    return componentType;
                }
            };
        }
        return type;
    }

    private static Type resolveTypeVariable(TypeVariable<?> typeVar, Class<?> targetClass) {
        GenericDeclaration declaration = typeVar.getGenericDeclaration();
        if (declaration instanceof Class<?> declaredClass) {
            Type superType = findGenericSupertype(targetClass, declaredClass);
            if (superType instanceof ParameterizedType paramType) {
                TypeVariable<?>[] typeParams = declaredClass.getTypeParameters();
                Type[] actualArgs = paramType.getActualTypeArguments();
                for (int i = 0; i < typeParams.length; i++) {
                    if (typeParams[i].getName().equals(typeVar.getName())) {
                        return actualArgs[i];
                    }
                }
            }
        }
        return typeVar;
    }

    private static Type resolveParameterizedType(ParameterizedType paramType, Class<?> targetClass) {
        Type[] typeArgs = paramType.getActualTypeArguments();
        Type[] resolvedArgs = new Type[typeArgs.length];
        boolean changed = false;
        for (int i = 0; i < typeArgs.length; i++) {
            resolvedArgs[i] = resolveType(typeArgs[i], targetClass);
            if (resolvedArgs[i] != typeArgs[i]) {
                changed = true;
            }
        }
        if (!changed) {
            return paramType;
        }
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return resolvedArgs;
            }

            @Override
            public Type getRawType() {
                return paramType.getRawType();
            }

            @Override
            public Type getOwnerType() {
                return paramType.getOwnerType();
            }
        };
    }

    private static Type findGenericSupertype(Class<?> targetClass, Class<?> declaredClass) {
        if (targetClass == declaredClass) {
            return targetClass;
        }
        if (declaredClass.isInterface()) {
            for (Type iface : targetClass.getGenericInterfaces()) {
                if (iface instanceof ParameterizedType pt && pt.getRawType() == declaredClass) {
                    return pt;
                }
                if (iface instanceof Class<?> ifaceClass) {
                    Type found = findGenericSupertype(ifaceClass, declaredClass);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        Type superType = targetClass.getGenericSuperclass();
        if (superType != null) {
            if (superType instanceof ParameterizedType pt && pt.getRawType() == declaredClass) {
                return pt;
            }
            if (superType instanceof Class<?> superClass) {
                return findGenericSupertype(superClass, declaredClass);
            }
        }
        return null;
    }

    // ==================== Array Operations | 数组操作 ====================

    /**
     * Creates array instance
     * 创建数组实例
     *
     * @param componentType the component type | 组件类型
     * @param length        the length | 长度
     * @return the array | 数组
     */
    public static Object newArray(Class<?> componentType, int length) {
        return Array.newInstance(componentType, length);
    }

    /**
     * Creates multi-dimensional array
     * 创建多维数组
     *
     * @param componentType the component type | 组件类型
     * @param dimensions    the dimensions | 维度
     * @return the array | 数组
     */
    public static Object newArray(Class<?> componentType, int... dimensions) {
        return Array.newInstance(componentType, dimensions);
    }

    /**
     * Gets array length
     * 获取数组长度
     *
     * @param array the array | 数组
     * @return the length | 长度
     */
    public static int getArrayLength(Object array) {
        return Array.getLength(array);
    }

    /**
     * Gets array element
     * 获取数组元素
     *
     * @param array the array | 数组
     * @param index the index | 索引
     * @return the element | 元素
     */
    public static Object getArrayElement(Object array, int index) {
        return Array.get(array, index);
    }

    /**
     * Sets array element
     * 设置数组元素
     *
     * @param array the array | 数组
     * @param index the index | 索引
     * @param value the value | 值
     */
    public static void setArrayElement(Object array, int index, Object value) {
        Array.set(array, index, value);
    }

    // ==================== Exception Wrapping | 异常包装 ====================

    /**
     * Unwraps InvocationTargetException
     * 解包InvocationTargetException
     *
     * @param ex the exception | 异常
     * @return the cause or original | 原因或原始异常
     */
    public static Throwable unwrapInvocationTargetException(Throwable ex) {
        if (ex instanceof InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            return cause != null ? cause : ex;
        }
        return ex;
    }

    /**
     * Rethrows exception as unchecked
     * 将异常作为非受检异常重新抛出
     *
     * @param ex  the exception | 异常
     * @param <T> the return type | 返回类型
     * @return never returns | 永不返回
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException sneakyThrow(Throwable ex) throws T {
        throw (T) ex;
    }

    // ==================== Signature Matching | 签名匹配 ====================

    /**
     * Checks if parameter types match
     * 检查参数类型是否匹配
     *
     * @param parameterTypes the parameter types | 参数类型
     * @param args           the arguments | 参数
     * @return true if match | 如果匹配返回true
     */
    public static boolean isAssignable(Class<?>[] parameterTypes, Object[] args) {
        if (parameterTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] == null) {
                if (parameterTypes[i].isPrimitive()) {
                    return false;
                }
            } else if (!isAssignable(parameterTypes[i], args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if types are assignable (with primitive widening)
     * 检查类型是否可赋值（含原始类型拓宽）
     *
     * @param target the target type | 目标类型
     * @param source the source type | 源类型
     * @return true if assignable | 如果可赋值返回true
     */
    public static boolean isAssignable(Class<?> target, Class<?> source) {
        if (target.isAssignableFrom(source)) {
            return true;
        }
        if (target.isPrimitive()) {
            return isPrimitiveAssignable(target, source);
        }
        if (source.isPrimitive()) {
            Class<?> wrapped = primitiveToWrapper(source);
            return target.isAssignableFrom(wrapped);
        }
        return false;
    }

    private static boolean isPrimitiveAssignable(Class<?> target, Class<?> source) {
        if (!source.isPrimitive()) {
            source = wrapperToPrimitive(source);
            if (source == null) {
                return false;
            }
        }
        if (target == source) {
            return true;
        }
        // Primitive widening conversions
        if (target == double.class) {
            return source == float.class || source == long.class ||
                   source == int.class || source == short.class ||
                   source == char.class || source == byte.class;
        }
        if (target == float.class) {
            return source == long.class || source == int.class ||
                   source == short.class || source == char.class || source == byte.class;
        }
        if (target == long.class) {
            return source == int.class || source == short.class ||
                   source == char.class || source == byte.class;
        }
        if (target == int.class) {
            return source == short.class || source == char.class || source == byte.class;
        }
        if (target == short.class) {
            return source == byte.class;
        }
        return false;
    }

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

    /**
     * Converts primitive to wrapper
     * 原始类型转包装类型
     *
     * @param primitiveType the primitive type | 原始类型
     * @return the wrapper type | 包装类型
     */
    public static Class<?> primitiveToWrapper(Class<?> primitiveType) {
        return PRIMITIVE_TO_WRAPPER.getOrDefault(primitiveType, primitiveType);
    }

    /**
     * Converts wrapper to primitive
     * 包装类型转原始类型
     *
     * @param wrapperType the wrapper type | 包装类型
     * @return the primitive type or null | 原始类型或null
     */
    public static Class<?> wrapperToPrimitive(Class<?> wrapperType) {
        return WRAPPER_TO_PRIMITIVE.get(wrapperType);
    }

    // ==================== Default Values | 默认值 ====================

    /**
     * Gets default value for type
     * 获取类型的默认值
     *
     * @param type the type | 类型
     * @return the default value | 默认值
     */
    public static Object getDefaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        return null;
    }
}
