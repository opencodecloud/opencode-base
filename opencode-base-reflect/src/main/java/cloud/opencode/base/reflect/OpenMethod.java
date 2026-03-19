package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import cloud.opencode.base.reflect.invokable.Invokable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Method Facade Entry Class
 * 方法门面入口类
 *
 * <p>Provides common method operations API, internally delegates to MethodUtil.
 * Similar to Commons Lang MethodUtils.</p>
 * <p>提供常用方法操作API，内部委托给MethodUtil。
 * 对标Commons Lang MethodUtils。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Method retrieval (with inheritance) - 方法获取（含继承）</li>
 *   <li>Method invocation - 方法调用</li>
 *   <li>Getter/Setter detection - Getter/Setter检测</li>
 *   <li>Annotation-based filtering - 基于注解的过滤</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Invoke a method
 * Object result = OpenMethod.invokeMethod(target, "getName");
 *
 * // Get all methods with annotation
 * List<Method> methods = OpenMethod.getMethodsWithAnnotation(MyClass.class, Override.class);
 *
 * // Check if method is getter
 * boolean isGetter = OpenMethod.isGetter(method);
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
public final class OpenMethod {

    private OpenMethod() {
    }

    // ==================== Method Retrieval | 方法获取 ====================

    /**
     * Gets a method (exact parameter types)
     * 获取方法（精确参数类型匹配）
     *
     * @param clazz          the class | 类
     * @param methodName     the method name | 方法名
     * @param parameterTypes the parameter types | 参数类型
     * @return the method | 方法
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return getMethod(clazz, methodName, true, parameterTypes);
    }

    /**
     * Gets a method with optional force access
     * 获取方法（可选强制访问）
     *
     * @param clazz          the class | 类
     * @param methodName     the method name | 方法名
     * @param forceAccess    whether to force access | 是否强制访问
     * @param parameterTypes the parameter types | 参数类型
     * @return the method | 方法
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Method getMethod(Class<?> clazz, String methodName, boolean forceAccess, Class<?>... parameterTypes) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        Objects.requireNonNull(methodName, "methodName must not be null");

        Class<?> current = clazz;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName, parameterTypes);
                if (forceAccess) {
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }

        // Try interfaces
        for (Class<?> iface : clazz.getInterfaces()) {
            try {
                return iface.getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ignored) {
            }
        }

        throw OpenReflectException.methodNotFound(clazz, methodName, parameterTypes);
    }

    /**
     * Gets a matching method (compatible parameter types)
     * 获取匹配方法（兼容参数类型）
     *
     * @param clazz          the class | 类
     * @param methodName     the method name | 方法名
     * @param parameterTypes the parameter types | 参数类型
     * @return the method | 方法
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Method getMatchingMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        // First try exact match
        try {
            return getMethod(clazz, methodName, true, parameterTypes);
        } catch (OpenReflectException ignored) {
        }

        // Try to find compatible method
        for (Method method : getAllMethods(clazz)) {
            if (method.getName().equals(methodName) && isAssignable(method.getParameterTypes(), parameterTypes)) {
                method.setAccessible(true);
                return method;
            }
        }

        throw OpenReflectException.methodNotFound(clazz, methodName, parameterTypes);
    }

    /**
     * Gets all methods (including inherited)
     * 获取所有方法（包含继承）
     *
     * @param clazz the class | 类
     * @return list of methods | 方法列表
     */
    public static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> result = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Collections.addAll(result, current.getDeclaredMethods());
            current = current.getSuperclass();
        }
        return result;
    }

    /**
     * Gets declared methods (not inherited)
     * 获取声明方法（不含继承）
     *
     * @param clazz the class | 类
     * @return list of methods | 方法列表
     */
    public static List<Method> getDeclaredMethods(Class<?> clazz) {
        return Arrays.asList(clazz.getDeclaredMethods());
    }

    /**
     * Gets methods with specific annotation
     * 按注解获取方法
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @return list of methods | 方法列表
     */
    public static List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return getAllMethods(clazz).stream()
                .filter(m -> m.isAnnotationPresent(annotationClass))
                .toList();
    }

    /**
     * Gets overloaded methods by name
     * 按名称获取所有重载方法
     *
     * @param clazz      the class | 类
     * @param methodName the method name | 方法名
     * @return list of methods | 方法列表
     */
    public static List<Method> getOverloadMethods(Class<?> clazz, String methodName) {
        return getAllMethods(clazz).stream()
                .filter(m -> m.getName().equals(methodName))
                .toList();
    }

    /**
     * Gets all getter methods
     * 获取所有Getter方法
     *
     * @param clazz the class | 类
     * @return list of getter methods | Getter方法列表
     */
    public static List<Method> getGetters(Class<?> clazz) {
        return getAllMethods(clazz).stream()
                .filter(OpenMethod::isGetter)
                .toList();
    }

    /**
     * Gets all setter methods
     * 获取所有Setter方法
     *
     * @param clazz the class | 类
     * @return list of setter methods | Setter方法列表
     */
    public static List<Method> getSetters(Class<?> clazz) {
        return getAllMethods(clazz).stream()
                .filter(OpenMethod::isSetter)
                .toList();
    }

    // ==================== Method Invocation | 方法调用 ====================

    /**
     * Invokes a method
     * 调用方法
     *
     * @param target     the target object | 目标对象
     * @param methodName the method name | 方法名
     * @param args       the arguments | 参数
     * @return the result | 结果
     */
    public static Object invokeMethod(Object target, String methodName, Object... args) {
        return invokeMethod(target, true, methodName, args);
    }

    /**
     * Invokes a method with optional force access
     * 调用方法（可选强制访问）
     *
     * @param target      the target object | 目标对象
     * @param forceAccess whether to force access | 是否强制访问
     * @param methodName  the method name | 方法名
     * @param args        the arguments | 参数
     * @return the result | 结果
     */
    public static Object invokeMethod(Object target, boolean forceAccess, String methodName, Object... args) {
        Class<?>[] paramTypes = getTypesFromArgs(args);
        Method method = getMatchingMethod(target.getClass(), methodName, paramTypes);
        if (forceAccess) {
            method.setAccessible(true);
        }
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw OpenReflectException.methodInvokeFailed(target.getClass(), methodName, e);
        }
    }

    /**
     * Invokes a method with type-safe return
     * 调用方法（泛型安全）
     *
     * @param target     the target object | 目标对象
     * @param methodName the method name | 方法名
     * @param returnType the return type | 返回类型
     * @param args       the arguments | 参数
     * @param <T>        the return type | 返回类型
     * @return the result | 结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object target, String methodName, Class<T> returnType, Object... args) {
        Object result = invokeMethod(target, methodName, args);
        if (result == null) {
            return null;
        }
        if (!returnType.isInstance(result)) {
            throw OpenReflectException.typeCastFailed(returnType, result);
        }
        return (T) result;
    }

    /**
     * Invokes a static method
     * 调用静态方法
     *
     * @param clazz      the class | 类
     * @param methodName the method name | 方法名
     * @param args       the arguments | 参数
     * @return the result | 结果
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Object... args) {
        Class<?>[] paramTypes = getTypesFromArgs(args);
        Method method = getMatchingMethod(clazz, methodName, paramTypes);
        method.setAccessible(true);
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw OpenReflectException.methodInvokeFailed(clazz, methodName, e);
        }
    }

    /**
     * Invokes a static method with type-safe return
     * 调用静态方法（泛型安全）
     *
     * @param clazz      the class | 类
     * @param methodName the method name | 方法名
     * @param returnType the return type | 返回类型
     * @param args       the arguments | 参数
     * @param <T>        the return type | 返回类型
     * @return the result | 结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeStaticMethod(Class<?> clazz, String methodName, Class<T> returnType, Object... args) {
        Object result = invokeStaticMethod(clazz, methodName, args);
        if (result == null) {
            return null;
        }
        if (!returnType.isInstance(result)) {
            throw OpenReflectException.typeCastFailed(returnType, result);
        }
        return (T) result;
    }

    // ==================== Method Information | 方法信息 ====================

    /**
     * Checks if a method exists
     * 检查方法是否存在
     *
     * @param clazz          the class | 类
     * @param methodName     the method name | 方法名
     * @param parameterTypes the parameter types | 参数类型
     * @return true if exists | 如果存在返回true
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            getMethod(clazz, methodName, true, parameterTypes);
            return true;
        } catch (OpenReflectException e) {
            return false;
        }
    }

    /**
     * Checks if a method is a getter
     * 检查是否为Getter方法
     *
     * @param method the method | 方法
     * @return true if getter | 如果是Getter返回true
     */
    public static boolean isGetter(Method method) {
        String name = method.getName();
        if (method.getParameterCount() != 0) {
            return false;
        }
        if (method.getReturnType() == void.class) {
            return false;
        }
        if (name.startsWith("get") && name.length() > 3) {
            return true;
        }
        if (name.startsWith("is") && name.length() > 2
                && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a method is a setter
     * 检查是否为Setter方法
     *
     * @param method the method | 方法
     * @return true if setter | 如果是Setter返回true
     */
    public static boolean isSetter(Method method) {
        String name = method.getName();
        return name.startsWith("set")
                && name.length() > 3
                && method.getParameterCount() == 1;
    }

    // ==================== Method Iteration | 方法遍历 ====================

    /**
     * Iterates all methods
     * 遍历所有方法
     *
     * @param clazz  the class | 类
     * @param action the action | 操作
     */
    public static void forEach(Class<?> clazz, Consumer<Method> action) {
        getAllMethods(clazz).forEach(action);
    }

    /**
     * Finds first matching method
     * 查找第一个匹配方法
     *
     * @param clazz     the class | 类
     * @param predicate the predicate | 谓词
     * @return Optional of method | 方法的Optional
     */
    public static Optional<Method> findFirst(Class<?> clazz, Predicate<Method> predicate) {
        return getAllMethods(clazz).stream().filter(predicate).findFirst();
    }

    /**
     * Creates a method stream
     * 创建方法流
     *
     * @param clazz the class | 类
     * @return stream of methods | 方法流
     */
    public static Stream<Method> stream(Class<?> clazz) {
        return getAllMethods(clazz).stream();
    }

    // ==================== Invokable Conversion | Invokable转换 ====================

    /**
     * Converts to Invokable
     * 转为Invokable
     *
     * @param method the method | 方法
     * @param <T>    the declaring class type | 声明类类型
     * @return Invokable | Invokable
     */
    public static <T> Invokable<T, Object> toInvokable(Method method) {
        return Invokable.from(method);
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
            if (!isAssignable(paramTypes[i], argTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAssignable(Class<?> paramType, Class<?> argType) {
        if (paramType.isAssignableFrom(argType)) {
            return true;
        }
        // Handle primitive/wrapper conversions
        if (paramType.isPrimitive()) {
            return primitiveToWrapper(paramType).isAssignableFrom(argType);
        }
        if (argType.isPrimitive()) {
            return paramType.isAssignableFrom(primitiveToWrapper(argType));
        }
        return false;
    }

    private static Class<?> primitiveToWrapper(Class<?> primitiveType) {
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == byte.class) return Byte.class;
        if (primitiveType == char.class) return Character.class;
        if (primitiveType == short.class) return Short.class;
        return primitiveType;
    }
}
