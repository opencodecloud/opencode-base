package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import cloud.opencode.base.reflect.invokable.Invokable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Constructor Facade Entry Class
 * 构造器门面入口类
 *
 * <p>Provides common constructor operations API.
 * Similar to Commons Lang ConstructorUtils.</p>
 * <p>提供常用构造器操作API。
 * 对标Commons Lang ConstructorUtils。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Constructor retrieval - 构造器获取</li>
 *   <li>Instance creation - 实例创建</li>
 *   <li>Factory method discovery - 工厂方法发现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create instance with default constructor
 * MyClass obj = OpenConstructor.newInstance(MyClass.class);
 *
 * // Create instance with arguments
 * MyClass obj = OpenConstructor.newInstance(MyClass.class, "name", 25);
 *
 * // Find factory method
 * Optional<Method> factory = OpenConstructor.findFactoryMethod(MyClass.class, "of");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class OpenConstructor {

    private OpenConstructor() {
    }

    // ==================== Constructor Retrieval | 构造器获取 ====================

    /**
     * Gets a constructor (exact parameter types)
     * 获取构造器（精确参数类型）
     *
     * @param clazz          the class | 类
     * @param parameterTypes the parameter types | 参数类型
     * @param <T>            the type | 类型
     * @return the constructor | 构造器
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        return getConstructor(clazz, true, parameterTypes);
    }

    /**
     * Gets a constructor with optional force access
     * 获取构造器（可选强制访问）
     *
     * @param clazz          the class | 类
     * @param forceAccess    whether to force access | 是否强制访问
     * @param parameterTypes the parameter types | 参数类型
     * @param <T>            the type | 类型
     * @return the constructor | 构造器
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, boolean forceAccess, Class<?>... parameterTypes) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor(parameterTypes);
            if (forceAccess) {
                ctor.setAccessible(true);
            }
            return ctor;
        } catch (NoSuchMethodException e) {
            throw OpenReflectException.constructorNotFound(clazz, parameterTypes);
        }
    }

    /**
     * Gets a matching constructor (compatible parameter types)
     * 获取匹配构造器（兼容参数类型）
     *
     * @param clazz          the class | 类
     * @param parameterTypes the parameter types | 参数类型
     * @param <T>            the type | 类型
     * @return the constructor | 构造器
     * @throws OpenReflectException if not found | 如果未找到
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getMatchingConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        // First try exact match
        try {
            return getConstructor(clazz, true, parameterTypes);
        } catch (OpenReflectException ignored) {
        }

        // Try to find compatible constructor
        for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            if (isAssignable(ctor.getParameterTypes(), parameterTypes)) {
                ctor.setAccessible(true);
                return (Constructor<T>) ctor;
            }
        }

        throw OpenReflectException.constructorNotFound(clazz, parameterTypes);
    }

    /**
     * Gets the default (no-arg) constructor
     * 获取无参构造器
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return the constructor | 构造器
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static <T> Constructor<T> getDefaultConstructor(Class<T> clazz) {
        return getConstructor(clazz);
    }

    /**
     * Gets all constructors
     * 获取所有构造器
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return list of constructors | 构造器列表
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Constructor<T>> getConstructors(Class<T> clazz) {
        Constructor<?>[] ctors = clazz.getDeclaredConstructors();
        List<Constructor<T>> result = new ArrayList<>(ctors.length);
        for (Constructor<?> ctor : ctors) {
            result.add((Constructor<T>) ctor);
        }
        return result;
    }

    /**
     * Gets constructors with specific annotation
     * 按注解获取构造器
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @param <T>             the type | 类型
     * @return list of constructors | 构造器列表
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Constructor<T>> getConstructorsWithAnnotation(Class<T> clazz,
                                                                          Class<? extends Annotation> annotationClass) {
        List<Constructor<T>> result = new ArrayList<>();
        for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            if (ctor.isAnnotationPresent(annotationClass)) {
                result.add((Constructor<T>) ctor);
            }
        }
        return result;
    }

    // ==================== Instance Creation | 实例创建 ====================

    /**
     * Creates an instance using default constructor
     * 创建实例（使用无参构造器）
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return the instance | 实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> ctor = getDefaultConstructor(clazz);
            return ctor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw OpenReflectException.instantiationFailed(clazz, e);
        }
    }

    /**
     * Creates an instance with auto-matching constructor
     * 创建实例（自动匹配构造器）
     *
     * @param clazz the class | 类
     * @param args  the arguments | 参数
     * @param <T>   the type | 类型
     * @return the instance | 实例
     */
    public static <T> T newInstance(Class<T> clazz, Object... args) {
        Class<?>[] paramTypes = getTypesFromArgs(args);
        Constructor<T> ctor = getMatchingConstructor(clazz, paramTypes);
        return newInstance(ctor, args);
    }

    /**
     * Creates an instance with specified parameter types
     * 创建实例（指定参数类型）
     *
     * @param clazz          the class | 类
     * @param parameterTypes the parameter types | 参数类型
     * @param args           the arguments | 参数
     * @param <T>            the type | 类型
     * @return the instance | 实例
     */
    public static <T> T newInstance(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
        Constructor<T> ctor = getConstructor(clazz, true, parameterTypes);
        return newInstance(ctor, args);
    }

    /**
     * Creates an instance using Constructor object
     * 创建实例（Constructor对象）
     *
     * @param constructor the constructor | 构造器
     * @param args        the arguments | 参数
     * @param <T>         the type | 类型
     * @return the instance | 实例
     */
    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw OpenReflectException.instantiationFailed(constructor.getDeclaringClass(), e);
        }
    }

    /**
     * Forces creation of an instance (bypasses private constructors)
     * 强制创建实例（绕过私有构造器）
     *
     * @param clazz the class | 类
     * @param args  the arguments | 参数
     * @param <T>   the type | 类型
     * @return the instance | 实例
     */
    public static <T> T newInstanceForced(Class<T> clazz, Object... args) {
        Class<?>[] paramTypes = getTypesFromArgs(args);
        Constructor<T> ctor = getMatchingConstructor(clazz, paramTypes);
        ctor.setAccessible(true);
        return newInstance(ctor, args);
    }

    // ==================== Constructor Information | 构造器信息 ====================

    /**
     * Checks if class has default constructor
     * 检查是否有无参构造器
     *
     * @param clazz the class | 类
     * @return true if has default constructor | 如果有无参构造器返回true
     */
    public static boolean hasDefaultConstructor(Class<?> clazz) {
        try {
            clazz.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Checks if class has constructor with specified parameters
     * 检查是否有指定参数构造器
     *
     * @param clazz          the class | 类
     * @param parameterTypes the parameter types | 参数类型
     * @return true if has constructor | 如果有构造器返回true
     */
    public static boolean hasConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            clazz.getDeclaredConstructor(parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Gets constructor parameter names
     * 获取构造器参数名称
     *
     * @param constructor the constructor | 构造器
     * @return list of parameter names | 参数名列表
     */
    public static List<String> getParameterNames(Constructor<?> constructor) {
        java.lang.reflect.Parameter[] params = constructor.getParameters();
        List<String> names = new ArrayList<>(params.length);
        for (java.lang.reflect.Parameter param : params) {
            names.add(param.getName());
        }
        return names;
    }

    /**
     * Gets constructor parameter types
     * 获取构造器参数类型
     *
     * @param constructor the constructor | 构造器
     * @return array of parameter types | 参数类型数组
     */
    public static Class<?>[] getParameterTypes(Constructor<?> constructor) {
        return constructor.getParameterTypes();
    }

    // ==================== Factory Method Discovery | 工厂方法发现 ====================

    /**
     * Finds a static factory method
     * 查找静态工厂方法
     *
     * @param clazz      the class | 类
     * @param methodName the method name | 方法名
     * @param <T>        the type | 类型
     * @return Optional of method | 方法的Optional
     */
    public static <T> Optional<Method> findFactoryMethod(Class<T> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())
                    && method.getName().equals(methodName)
                    && clazz.isAssignableFrom(method.getReturnType())) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all static factory methods
     * 查找所有静态工厂方法
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return list of methods | 方法列表
     */
    public static <T> List<Method> findFactoryMethods(Class<T> clazz) {
        List<Method> result = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())
                    && clazz.isAssignableFrom(method.getReturnType())) {
                result.add(method);
            }
        }
        return result;
    }

    /**
     * Creates an instance via factory method
     * 通过工厂方法创建实例
     *
     * @param clazz         the class | 类
     * @param factoryMethod the factory method name | 工厂方法名
     * @param args          the arguments | 参数
     * @param <T>           the type | 类型
     * @return the instance | 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstanceByFactory(Class<T> clazz, String factoryMethod, Object... args) {
        Method method = findFactoryMethod(clazz, factoryMethod)
                .orElseThrow(() -> OpenReflectException.methodNotFound(clazz, factoryMethod, getTypesFromArgs(args)));
        try {
            method.setAccessible(true);
            return (T) method.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw OpenReflectException.methodInvokeFailed(clazz, factoryMethod, e);
        }
    }

    // ==================== Invokable Conversion | Invokable转换 ====================

    /**
     * Converts to Invokable
     * 转为Invokable
     *
     * @param constructor the constructor | 构造器
     * @param <T>         the type | 类型
     * @return Invokable | Invokable
     */
    public static <T> Invokable<T, T> toInvokable(Constructor<T> constructor) {
        return Invokable.from(constructor);
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private static Class<?>[] getTypesFromArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return new Class<?>[0];
        }
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i] != null ? args[i].getClass() : Object.class;
        }
        return types;
    }

    private static boolean isAssignable(Class<?>[] paramTypes, Class<?>[] argTypes) {
        if (paramTypes.length != argTypes.length) {
            return false;
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (!paramTypes[i].isAssignableFrom(argTypes[i])
                    && !isPrimitiveCompatible(paramTypes[i], argTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPrimitiveCompatible(Class<?> paramType, Class<?> argType) {
        if (paramType == int.class) return argType == Integer.class;
        if (paramType == long.class) return argType == Long.class;
        if (paramType == double.class) return argType == Double.class;
        if (paramType == float.class) return argType == Float.class;
        if (paramType == boolean.class) return argType == Boolean.class;
        if (paramType == byte.class) return argType == Byte.class;
        if (paramType == char.class) return argType == Character.class;
        if (paramType == short.class) return argType == Short.class;
        return false;
    }
}
