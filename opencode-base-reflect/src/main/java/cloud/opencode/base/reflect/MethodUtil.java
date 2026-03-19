package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Method Utility Class
 * 方法工具类
 *
 * <p>Provides low-level method operation utilities with caching.</p>
 * <p>提供带缓存的底层方法操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Method discovery with caching - 带缓存的方法发现</li>
 *   <li>Inherited method resolution - 继承方法解析</li>
 *   <li>Method filtering by name, annotation, modifier - 按名称、注解、修饰符过滤方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<Method> methods = MethodUtil.getAllMethods(User.class);
 * Method method = MethodUtil.getMethod(User.class, "getName");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap for caching) - 线程安全: 是（使用ConcurrentHashMap缓存）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for cached lookups; O(m) for first access where m is the number of methods (including inherited) - 时间复杂度: 缓存命中时 O(1)；首次访问为 O(m)，m为方法数量（含继承）</li>
 *   <li>Space complexity: O(m) for the cached methods per class - 空间复杂度: O(m)，每类缓存方法</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class MethodUtil {

    private static final Map<Class<?>, List<Method>> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<MethodKey, Method> SINGLE_METHOD_CACHE = new ConcurrentHashMap<>();

    private MethodUtil() {
    }

    // ==================== Method Discovery | 方法发现 ====================

    /**
     * Gets all declared methods (no inheritance)
     * 获取所有声明的方法（不含继承）
     *
     * @param clazz the class | 类
     * @return array of methods | 方法数组
     */
    public static Method[] getDeclaredMethods(Class<?> clazz) {
        return clazz.getDeclaredMethods();
    }

    /**
     * Gets all methods including inherited (cached)
     * 获取所有方法包含继承（缓存）
     *
     * @param clazz the class | 类
     * @return list of methods | 方法列表
     */
    public static List<Method> getAllMethods(Class<?> clazz) {
        return METHOD_CACHE.computeIfAbsent(clazz, MethodUtil::collectAllMethods);
    }

    private static List<Method> collectAllMethods(Class<?> clazz) {
        Map<String, Method> methodMap = new LinkedHashMap<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                String sig = getMethodSignature(method);
                methodMap.putIfAbsent(sig, method);
            }
            current = current.getSuperclass();
        }
        // Include interface default methods
        for (Class<?> iface : getAllInterfaces(clazz)) {
            for (Method method : iface.getDeclaredMethods()) {
                if (method.isDefault()) {
                    String sig = getMethodSignature(method);
                    methodMap.putIfAbsent(sig, method);
                }
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(methodMap.values()));
    }

    private static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        collectInterfaces(clazz, interfaces);
        return new ArrayList<>(interfaces);
    }

    private static void collectInterfaces(Class<?> clazz, Set<Class<?>> interfaces) {
        if (clazz == null) {
            return;
        }
        for (Class<?> iface : clazz.getInterfaces()) {
            if (interfaces.add(iface)) {
                collectInterfaces(iface, interfaces);
            }
        }
        collectInterfaces(clazz.getSuperclass(), interfaces);
    }

    /**
     * Gets method by name and parameter types (cached)
     * 按名称和参数类型获取方法（缓存）
     *
     * @param clazz          the class | 类
     * @param methodName     the method name | 方法名
     * @param parameterTypes the parameter types | 参数类型
     * @return the method or null | 方法或null
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        MethodKey key = new MethodKey(clazz, methodName, parameterTypes);
        return SINGLE_METHOD_CACHE.computeIfAbsent(key, k -> findMethod(k.clazz(), k.methodName(), k.parameterTypes()));
    }

    private static Method findMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        // Check interfaces
        for (Class<?> iface : getAllInterfaces(clazz)) {
            try {
                return iface.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    /**
     * Gets method or throws exception
     * 获取方法或抛出异常
     *
     * @param clazz          the class | 类
     * @param methodName     the method name | 方法名
     * @param parameterTypes the parameter types | 参数类型
     * @return the method | 方法
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Method getMethodOrThrow(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Method method = getMethod(clazz, methodName, parameterTypes);
        if (method == null) {
            throw OpenReflectException.methodNotFound(clazz, methodName, parameterTypes);
        }
        return method;
    }

    /**
     * Finds method by name (any parameters)
     * 按名称查找方法（任意参数）
     *
     * @param clazz      the class | 类
     * @param methodName the method name | 方法名
     * @return list of methods | 方法列表
     */
    public static List<Method> getMethodsByName(Class<?> clazz, String methodName) {
        return getAllMethods(clazz).stream()
                .filter(m -> m.getName().equals(methodName))
                .toList();
    }

    // ==================== Method Filtering | 方法过滤 ====================

    /**
     * Gets methods matching predicate
     * 获取匹配条件的方法
     *
     * @param clazz     the class | 类
     * @param predicate the predicate | 谓词
     * @return list of matching methods | 匹配的方法列表
     */
    public static List<Method> getMethods(Class<?> clazz, Predicate<Method> predicate) {
        return getAllMethods(clazz).stream().filter(predicate).toList();
    }

    /**
     * Gets methods with annotation
     * 获取带注解的方法
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @return list of methods | 方法列表
     */
    public static List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return getMethods(clazz, m -> m.isAnnotationPresent(annotationClass));
    }

    /**
     * Gets methods with specific return type
     * 获取特定返回类型的方法
     *
     * @param clazz      the class | 类
     * @param returnType the return type | 返回类型
     * @return list of methods | 方法列表
     */
    public static List<Method> getMethodsWithReturnType(Class<?> clazz, Class<?> returnType) {
        return getMethods(clazz, m -> returnType.isAssignableFrom(m.getReturnType()));
    }

    /**
     * Gets getter methods
     * 获取getter方法
     *
     * @param clazz the class | 类
     * @return list of methods | 方法列表
     */
    public static List<Method> getGetters(Class<?> clazz) {
        return getMethods(clazz, MethodUtil::isGetter);
    }

    /**
     * Gets setter methods
     * 获取setter方法
     *
     * @param clazz the class | 类
     * @return list of methods | 方法列表
     */
    public static List<Method> getSetters(Class<?> clazz) {
        return getMethods(clazz, MethodUtil::isSetter);
    }

    // ==================== Method Invocation | 方法调用 ====================

    /**
     * Invokes method
     * 调用方法
     *
     * @param method the method | 方法
     * @param target the target object | 目标对象
     * @param args   the arguments | 参数
     * @return the result | 结果
     */
    public static Object invoke(Method method, Object target, Object... args) {
        try {
            ReflectUtil.setAccessible(method, target);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw OpenReflectException.methodInvokeFailed(
                    method.getDeclaringClass(), method.getName(),
                    ReflectUtil.unwrapInvocationTargetException(e));
        }
    }

    /**
     * Invokes method with type cast
     * 调用方法并转型
     *
     * @param method     the method | 方法
     * @param target     the target object | 目标对象
     * @param returnType the return type | 返回类型
     * @param args       the arguments | 参数
     * @param <T>        the type | 类型
     * @return the result | 结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(Method method, Object target, Class<T> returnType, Object... args) {
        Object result = invoke(method, target, args);
        if (result == null) {
            return null;
        }
        if (!returnType.isInstance(result)) {
            throw OpenReflectException.typeCastFailed(returnType, result);
        }
        return (T) result;
    }

    /**
     * Invokes static method
     * 调用静态方法
     *
     * @param method the method | 方法
     * @param args   the arguments | 参数
     * @return the result | 结果
     */
    public static Object invokeStatic(Method method, Object... args) {
        return invoke(method, null, args);
    }

    // ==================== Method Information | 方法信息 ====================

    /**
     * Gets method return type
     * 获取方法返回类型
     *
     * @param method the method | 方法
     * @return the return type | 返回类型
     */
    public static Class<?> getReturnType(Method method) {
        return method.getReturnType();
    }

    /**
     * Gets method generic return type
     * 获取方法泛型返回类型
     *
     * @param method the method | 方法
     * @return the generic return type | 泛型返回类型
     */
    public static Type getGenericReturnType(Method method) {
        return method.getGenericReturnType();
    }

    /**
     * Gets method parameter types
     * 获取方法参数类型
     *
     * @param method the method | 方法
     * @return the parameter types | 参数类型
     */
    public static Class<?>[] getParameterTypes(Method method) {
        return method.getParameterTypes();
    }

    /**
     * Gets method parameter count
     * 获取方法参数数量
     *
     * @param method the method | 方法
     * @return the parameter count | 参数数量
     */
    public static int getParameterCount(Method method) {
        return method.getParameterCount();
    }

    /**
     * Gets method signature
     * 获取方法签名
     *
     * @param method the method | 方法
     * @return the signature | 签名
     */
    public static String getMethodSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName()).append('(');
        Class<?>[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(params[i].getName());
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Checks if method is getter
     * 检查是否为getter方法
     *
     * @param method the method | 方法
     * @return true if getter | 如果是getter返回true
     */
    public static boolean isGetter(Method method) {
        if (method.getParameterCount() != 0) {
            return false;
        }
        if (method.getReturnType() == void.class) {
            return false;
        }
        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            return Character.isUpperCase(name.charAt(3));
        }
        if (name.startsWith("is") && name.length() > 2) {
            return Character.isUpperCase(name.charAt(2)) &&
                   (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class);
        }
        return false;
    }

    /**
     * Checks if method is setter
     * 检查是否为setter方法
     *
     * @param method the method | 方法
     * @return true if setter | 如果是setter返回true
     */
    public static boolean isSetter(Method method) {
        if (method.getParameterCount() != 1) {
            return false;
        }
        String name = method.getName();
        return name.startsWith("set") && name.length() > 3 && Character.isUpperCase(name.charAt(3));
    }

    /**
     * Gets property name from getter/setter
     * 从getter/setter获取属性名
     *
     * @param method the method | 方法
     * @return the property name or null | 属性名或null
     */
    public static String getPropertyName(Method method) {
        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        if (name.startsWith("is") && name.length() > 2) {
            return Character.toLowerCase(name.charAt(2)) + name.substring(3);
        }
        if (name.startsWith("set") && name.length() > 3) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        return null;
    }

    /**
     * Checks if method is static
     * 检查方法是否为静态
     *
     * @param method the method | 方法
     * @return true if static | 如果是静态返回true
     */
    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * Checks if method is abstract
     * 检查方法是否为抽象
     *
     * @param method the method | 方法
     * @return true if abstract | 如果是抽象返回true
     */
    public static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    /**
     * Checks if method is default interface method
     * 检查是否为接口默认方法
     *
     * @param method the method | 方法
     * @return true if default | 如果是默认方法返回true
     */
    public static boolean isDefault(Method method) {
        return method.isDefault();
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Clears method cache
     * 清除方法缓存
     */
    public static void clearCache() {
        METHOD_CACHE.clear();
        SINGLE_METHOD_CACHE.clear();
    }

    /**
     * Clears cache for specific class
     * 清除特定类的缓存
     *
     * @param clazz the class | 类
     */
    public static void clearCache(Class<?> clazz) {
        METHOD_CACHE.remove(clazz);
        SINGLE_METHOD_CACHE.keySet().removeIf(key -> key.clazz() == clazz);
    }

    // ==================== Internal | 内部 ====================

    private record MethodKey(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MethodKey that)) return false;
            return clazz == that.clazz &&
                   methodName.equals(that.methodName) &&
                   Arrays.equals(parameterTypes, that.parameterTypes);
        }

        @Override
        public int hashCode() {
            int result = clazz.hashCode();
            result = 31 * result + methodName.hashCode();
            result = 31 * result + Arrays.hashCode(parameterTypes);
            return result;
        }
    }
}
