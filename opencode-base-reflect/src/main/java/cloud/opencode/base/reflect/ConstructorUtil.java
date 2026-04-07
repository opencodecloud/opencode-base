package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Constructor Utility Class
 * 构造器工具类
 *
 * <p>Provides low-level constructor operation utilities with caching.</p>
 * <p>提供带缓存的底层构造器操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Constructor discovery with caching - 带缓存的构造器发现</li>
 *   <li>Parameter type matching - 参数类型匹配</li>
 *   <li>Constructor filtering by annotation - 按注解过滤构造器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Constructor<?> ctor = ConstructorUtil.getConstructor(User.class, String.class, int.class);
 * boolean hasDefault = ConstructorUtil.hasDefaultConstructor(User.class);
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
 *   <li>Time complexity: O(1) for cached lookups; O(c) for first access where c is the number of constructors - 时间复杂度: 缓存命中时 O(1)；首次访问为 O(c)，c为构造器数量</li>
 *   <li>Space complexity: O(c) for the cached constructors per class - 空间复杂度: O(c)，每类缓存构造器</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class ConstructorUtil {

    private static final Map<ConstructorKey, Constructor<?>> CONSTRUCTOR_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    private ConstructorUtil() {
    }

    // ==================== Constructor Discovery | 构造器发现 ====================

    /**
     * Gets all declared constructors
     * 获取所有声明的构造器
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return array of constructors | 构造器数组
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T>[] getDeclaredConstructors(Class<T> clazz) {
        return (Constructor<T>[]) clazz.getDeclaredConstructors();
    }

    /**
     * Gets constructor by parameter types (cached)
     * 按参数类型获取构造器（缓存）
     *
     * @param clazz          the class | 类
     * @param parameterTypes the parameter types | 参数类型
     * @param <T>            the type | 类型
     * @return the constructor or null | 构造器或null
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        ConstructorKey key = new ConstructorKey(clazz, parameterTypes);
        return (Constructor<T>) CONSTRUCTOR_CACHE.computeIfAbsent(key,
                k -> findConstructor(k.clazz(), k.parameterTypes()));
    }

    private static Constructor<?> findConstructor(Class<?> clazz, Class<?>[] parameterTypes) {
        try {
            return clazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Gets constructor or throws exception
     * 获取构造器或抛出异常
     *
     * @param clazz          the class | 类
     * @param parameterTypes the parameter types | 参数类型
     * @param <T>            the type | 类型
     * @return the constructor | 构造器
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static <T> Constructor<T> getConstructorOrThrow(Class<T> clazz, Class<?>... parameterTypes) {
        Constructor<T> constructor = getConstructor(clazz, parameterTypes);
        if (constructor == null) {
            throw OpenReflectException.constructorNotFound(clazz, parameterTypes);
        }
        return constructor;
    }

    /**
     * Gets default (no-arg) constructor
     * 获取默认（无参）构造器
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return the constructor or null | 构造器或null
     */
    public static <T> Constructor<T> getDefaultConstructor(Class<T> clazz) {
        return getConstructor(clazz);
    }

    /**
     * Finds best matching constructor for arguments
     * 查找最佳匹配的构造器
     *
     * @param clazz the class | 类
     * @param args  the arguments | 参数
     * @param <T>   the type | 类型
     * @return the constructor or null | 构造器或null
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findMatchingConstructor(Class<T> clazz, Object... args) {
        Class<?>[] argTypes = getArgTypes(args);

        // Try exact match first
        Constructor<T> exact = getConstructor(clazz, argTypes);
        if (exact != null) {
            return exact;
        }

        // Try compatible match
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (isAssignable(constructor.getParameterTypes(), args)) {
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }

    private static Class<?>[] getArgTypes(Object[] args) {
        if (args == null || args.length == 0) {
            return new Class<?>[0];
        }
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i] != null ? args[i].getClass() : Object.class;
        }
        return types;
    }

    private static boolean isAssignable(Class<?>[] paramTypes, Object[] args) {
        if (paramTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (args[i] == null) {
                if (paramTypes[i].isPrimitive()) {
                    return false;
                }
            } else if (!ReflectUtil.isAssignable(paramTypes[i], args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    // ==================== Constructor Filtering | 构造器过滤 ====================

    /**
     * Gets constructors matching predicate
     * 获取匹配条件的构造器
     *
     * @param clazz     the class | 类
     * @param predicate the predicate | 谓词
     * @param <T>       the type | 类型
     * @return list of matching constructors | 匹配的构造器列表
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Constructor<T>> getConstructors(Class<T> clazz, Predicate<Constructor<T>> predicate) {
        List<Constructor<T>> result = new ArrayList<>();
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            Constructor<T> typed = (Constructor<T>) constructor;
            if (predicate.test(typed)) {
                result.add(typed);
            }
        }
        return result;
    }

    /**
     * Gets constructors with annotation
     * 获取带注解的构造器
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @param <T>             the type | 类型
     * @return list of constructors | 构造器列表
     */
    public static <T> List<Constructor<T>> getConstructorsWithAnnotation(Class<T> clazz,
            Class<? extends Annotation> annotationClass) {
        return getConstructors(clazz, c -> c.isAnnotationPresent(annotationClass));
    }

    /**
     * Gets public constructors
     * 获取公共构造器
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return list of constructors | 构造器列表
     */
    public static <T> List<Constructor<T>> getPublicConstructors(Class<T> clazz) {
        return getConstructors(clazz, c -> Modifier.isPublic(c.getModifiers()));
    }

    // ==================== Constructor Invocation | 构造器调用 ====================

    /**
     * Creates new instance using constructor
     * 使用构造器创建新实例
     *
     * @param constructor the constructor | 构造器
     * @param args        the arguments | 参数
     * @param <T>         the type | 类型
     * @return the instance | 实例
     */
    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            ReflectUtil.setAccessible(constructor);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw OpenReflectException.instantiationFailed(constructor.getDeclaringClass(),
                    ReflectUtil.unwrapInvocationTargetException(e));
        }
    }

    /**
     * Creates new instance using default constructor
     * 使用默认构造器创建新实例
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return the instance | 实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        Constructor<T> constructor = getDefaultConstructor(clazz);
        if (constructor == null) {
            throw OpenReflectException.constructorNotFound(clazz, new Class<?>[0]);
        }
        return newInstance(constructor);
    }

    /**
     * Creates new instance with arguments (auto-matching)
     * 使用参数创建新实例（自动匹配）
     *
     * @param clazz the class | 类
     * @param args  the arguments | 参数
     * @param <T>   the type | 类型
     * @return the instance | 实例
     */
    public static <T> T newInstance(Class<T> clazz, Object... args) {
        if (args == null || args.length == 0) {
            return newInstance(clazz);
        }
        Constructor<T> constructor = findMatchingConstructor(clazz, args);
        if (constructor == null) {
            throw OpenReflectException.constructorNotFound(clazz, getArgTypes(args));
        }
        return newInstance(constructor, args);
    }

    /**
     * Creates new instance safely
     * 安全创建新实例
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return Optional of instance | 实例的Optional
     */
    public static <T> Optional<T> newInstanceSafe(Class<T> clazz) {
        try {
            return Optional.of(newInstance(clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ==================== Constructor Information | 构造器信息 ====================

    /**
     * Gets constructor parameter types
     * 获取构造器参数类型
     *
     * @param constructor the constructor | 构造器
     * @return the parameter types | 参数类型
     */
    public static Class<?>[] getParameterTypes(Constructor<?> constructor) {
        return constructor.getParameterTypes();
    }

    /**
     * Gets constructor parameter count
     * 获取构造器参数数量
     *
     * @param constructor the constructor | 构造器
     * @return the parameter count | 参数数量
     */
    public static int getParameterCount(Constructor<?> constructor) {
        return constructor.getParameterCount();
    }

    /**
     * Checks if constructor is public
     * 检查构造器是否为公共
     *
     * @param constructor the constructor | 构造器
     * @return true if public | 如果是公共返回true
     */
    public static boolean isPublic(Constructor<?> constructor) {
        return Modifier.isPublic(constructor.getModifiers());
    }

    /**
     * Checks if constructor is private
     * 检查构造器是否为私有
     *
     * @param constructor the constructor | 构造器
     * @return true if private | 如果是私有返回true
     */
    public static boolean isPrivate(Constructor<?> constructor) {
        return Modifier.isPrivate(constructor.getModifiers());
    }

    /**
     * Checks if class has default constructor
     * 检查类是否有默认构造器
     *
     * @param clazz the class | 类
     * @return true if has default constructor | 如果有默认构造器返回true
     */
    public static boolean hasDefaultConstructor(Class<?> clazz) {
        return getDefaultConstructor(clazz) != null;
    }

    /**
     * Checks if class is instantiable
     * 检查类是否可实例化
     *
     * @param clazz the class | 类
     * @return true if instantiable | 如果可实例化返回true
     */
    public static boolean isInstantiable(Class<?> clazz) {
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        if (clazz.isPrimitive() || clazz.isArray()) {
            return false;
        }
        return clazz.getDeclaredConstructors().length > 0;
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Clears constructor cache
     * 清除构造器缓存
     */
    public static void clearCache() {
        CONSTRUCTOR_CACHE.clear();
    }

    /**
     * Clears cache for specific class
     * 清除特定类的缓存
     *
     * @param clazz the class | 类
     */
    public static void clearCache(Class<?> clazz) {
        CONSTRUCTOR_CACHE.keySet().removeIf(key -> key.clazz() == clazz);
    }

    // ==================== Internal | 内部 ====================

    private record ConstructorKey(Class<?> clazz, Class<?>[] parameterTypes) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConstructorKey that)) return false;
            return clazz == that.clazz && Arrays.equals(parameterTypes, that.parameterTypes);
        }

        @Override
        public int hashCode() {
            return 31 * clazz.hashCode() + Arrays.hashCode(parameterTypes);
        }
    }
}
