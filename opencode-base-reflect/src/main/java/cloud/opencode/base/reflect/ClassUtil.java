package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class Utility Class
 * 类工具类
 *
 * <p>Provides low-level class operation utilities with caching.</p>
 * <p>提供带缓存的底层类操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Class loading with caching - 带缓存的类加载</li>
 *   <li>Class hierarchy resolution with caching - 带缓存的类层次解析</li>
 *   <li>Interface resolution with caching - 带缓存的接口解析</li>
 *   <li>Primitive/wrapper conversion - 原始/包装类型转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Class<?> clazz = ClassUtil.loadClass("com.example.User");
 * List<Class<?>> hierarchy = ClassUtil.getClassHierarchy(User.class);
 * Class<?> wrapper = ClassUtil.primitiveToWrapper(int.class);
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
 *   <li>Time complexity: O(1) for cached lookups; O(h) for first hierarchy resolution where h is the class hierarchy depth - 时间复杂度: 缓存命中时 O(1)；首次层次解析为 O(h)，h为类层次深度</li>
 *   <li>Space complexity: O(h) for the cached hierarchy per class - 空间复杂度: O(h)，每类缓存层次</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class ClassUtil {

    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Class<?>>> HIERARCHY_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Class<?>>> INTERFACES_CACHE = new ConcurrentHashMap<>();

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

    private static final Set<Class<?>> WRAPPER_TYPES = Set.of(
            Boolean.class, Byte.class, Character.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, Void.class
    );

    private ClassUtil() {
    }

    // ==================== Class Loading | 类加载 ====================

    /**
     * Loads class by name (cached)
     * 按名称加载类（缓存）
     *
     * @param className the class name | 类名
     * @return the class | 类
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Class<?> forName(String className) {
        return CLASS_CACHE.computeIfAbsent(className, ClassUtil::loadClass);
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw OpenReflectException.classLoadFailed(className, e);
        }
    }

    /**
     * Loads class by name safely
     * 安全按名称加载类
     *
     * @param className the class name | 类名
     * @return Optional of class | 类的Optional
     */
    public static Optional<Class<?>> forNameSafe(String className) {
        try {
            return Optional.of(forName(className));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Loads class by name with class loader
     * 使用类加载器按名称加载类
     *
     * @param className   the class name | 类名
     * @param classLoader the class loader | 类加载器
     * @return the class | 类
     */
    public static Class<?> forName(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw OpenReflectException.classLoadFailed(className, e);
        }
    }

    /**
     * Checks if class exists
     * 检查类是否存在
     *
     * @param className the class name | 类名
     * @return true if exists | 如果存在返回true
     */
    public static boolean exists(String className) {
        try {
            forName(className);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Class Hierarchy | 类层次 ====================

    /**
     * Gets all superclasses (cached)
     * 获取所有父类（缓存）
     *
     * @param clazz the class | 类
     * @return list of superclasses | 父类列表
     */
    public static List<Class<?>> getAllSuperclasses(Class<?> clazz) {
        List<Class<?>> result = new ArrayList<>();
        Class<?> current = clazz.getSuperclass();
        while (current != null && current != Object.class) {
            result.add(current);
            current = current.getSuperclass();
        }
        return result;
    }

    /**
     * Gets all interfaces including inherited (cached)
     * 获取所有接口包含继承（缓存）
     *
     * @param clazz the class | 类
     * @return list of interfaces | 接口列表
     */
    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        return INTERFACES_CACHE.computeIfAbsent(clazz, ClassUtil::collectInterfaces);
    }

    private static List<Class<?>> collectInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        collectInterfacesRecursive(clazz, interfaces);
        return Collections.unmodifiableList(new ArrayList<>(interfaces));
    }

    private static void collectInterfacesRecursive(Class<?> clazz, Set<Class<?>> interfaces) {
        if (clazz == null) {
            return;
        }
        for (Class<?> iface : clazz.getInterfaces()) {
            if (interfaces.add(iface)) {
                collectInterfacesRecursive(iface, interfaces);
            }
        }
        collectInterfacesRecursive(clazz.getSuperclass(), interfaces);
    }

    /**
     * Gets full class hierarchy (cached)
     * 获取完整类层次结构（缓存）
     *
     * @param clazz the class | 类
     * @return list of all classes in hierarchy | 层次结构中的所有类
     */
    public static List<Class<?>> getClassHierarchy(Class<?> clazz) {
        return HIERARCHY_CACHE.computeIfAbsent(clazz, ClassUtil::collectHierarchy);
    }

    private static List<Class<?>> collectHierarchy(Class<?> clazz) {
        List<Class<?>> hierarchy = new ArrayList<>();
        hierarchy.add(clazz);
        hierarchy.addAll(getAllSuperclasses(clazz));
        hierarchy.addAll(getAllInterfaces(clazz));
        return Collections.unmodifiableList(hierarchy);
    }

    // ==================== Type Checks | 类型检查 ====================

    /**
     * Checks if primitive type
     * 检查是否为原始类型
     *
     * @param clazz the class | 类
     * @return true if primitive | 如果是原始类型返回true
     */
    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive();
    }

    /**
     * Checks if wrapper type
     * 检查是否为包装类型
     *
     * @param clazz the class | 类
     * @return true if wrapper | 如果是包装类型返回true
     */
    public static boolean isWrapper(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    /**
     * Checks if primitive or wrapper
     * 检查是否为原始类型或包装类型
     *
     * @param clazz the class | 类
     * @return true if primitive or wrapper | 如果是原始或包装类型返回true
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return isPrimitive(clazz) || isWrapper(clazz);
    }

    /**
     * Checks if Record class
     * 检查是否为Record类
     *
     * @param clazz the class | 类
     * @return true if record | 如果是Record返回true
     */
    public static boolean isRecord(Class<?> clazz) {
        return clazz.isRecord();
    }

    /**
     * Checks if sealed class
     * 检查是否为密封类
     *
     * @param clazz the class | 类
     * @return true if sealed | 如果是密封类返回true
     */
    public static boolean isSealed(Class<?> clazz) {
        return clazz.isSealed();
    }

    /**
     * Checks if enum type
     * 检查是否为枚举类型
     *
     * @param clazz the class | 类
     * @return true if enum | 如果是枚举返回true
     */
    public static boolean isEnum(Class<?> clazz) {
        return clazz.isEnum();
    }

    /**
     * Checks if array type
     * 检查是否为数组类型
     *
     * @param clazz the class | 类
     * @return true if array | 如果是数组返回true
     */
    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    /**
     * Checks if interface
     * 检查是否为接口
     *
     * @param clazz the class | 类
     * @return true if interface | 如果是接口返回true
     */
    public static boolean isInterface(Class<?> clazz) {
        return clazz.isInterface();
    }

    /**
     * Checks if abstract class
     * 检查是否为抽象类
     *
     * @param clazz the class | 类
     * @return true if abstract | 如果是抽象类返回true
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Checks if final class
     * 检查是否为final类
     *
     * @param clazz the class | 类
     * @return true if final | 如果是final返回true
     */
    public static boolean isFinal(Class<?> clazz) {
        return Modifier.isFinal(clazz.getModifiers());
    }

    /**
     * Checks if anonymous class
     * 检查是否为匿名类
     *
     * @param clazz the class | 类
     * @return true if anonymous | 如果是匿名类返回true
     */
    public static boolean isAnonymous(Class<?> clazz) {
        return clazz.isAnonymousClass();
    }

    /**
     * Checks if inner class
     * 检查是否为内部类
     *
     * @param clazz the class | 类
     * @return true if inner | 如果是内部类返回true
     */
    public static boolean isInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() || clazz.isLocalClass() || clazz.isAnonymousClass();
    }

    /**
     * Checks if functional interface
     * 检查是否为函数式接口
     *
     * @param clazz the class | 类
     * @return true if functional interface | 如果是函数式接口返回true
     */
    public static boolean isFunctionalInterface(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return false;
        }
        return clazz.isAnnotationPresent(FunctionalInterface.class) ||
               countAbstractMethods(clazz) == 1;
    }

    private static int countAbstractMethods(Class<?> clazz) {
        int count = 0;
        for (java.lang.reflect.Method method : clazz.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers()) && !isObjectMethod(method)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isObjectMethod(java.lang.reflect.Method method) {
        String name = method.getName();
        Class<?>[] params = method.getParameterTypes();
        if ("equals".equals(name) && params.length == 1 && params[0] == Object.class) return true;
        if ("hashCode".equals(name) && params.length == 0) return true;
        if ("toString".equals(name) && params.length == 0) return true;
        return false;
    }

    // ==================== Type Conversion | 类型转换 ====================

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

    /**
     * Gets array component type
     * 获取数组组件类型
     *
     * @param arrayClass the array class | 数组类
     * @return the component type | 组件类型
     */
    public static Class<?> getComponentType(Class<?> arrayClass) {
        return arrayClass.getComponentType();
    }

    /**
     * Gets array class for component type
     * 获取组件类型的数组类
     *
     * @param componentType the component type | 组件类型
     * @return the array class | 数组类
     */
    public static Class<?> getArrayClass(Class<?> componentType) {
        return java.lang.reflect.Array.newInstance(componentType, 0).getClass();
    }

    // ==================== Class Name Operations | 类名操作 ====================

    /**
     * Gets simple name
     * 获取简单名称
     *
     * @param clazz the class | 类
     * @return the simple name | 简单名称
     */
    public static String getSimpleName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getSimpleName(clazz.getComponentType()) + "[]";
        }
        String simpleName = clazz.getSimpleName();
        if (simpleName.isEmpty()) {
            String name = clazz.getName();
            int lastDot = name.lastIndexOf('.');
            return lastDot >= 0 ? name.substring(lastDot + 1) : name;
        }
        return simpleName;
    }

    /**
     * Gets canonical name or name
     * 获取规范名称或名称
     *
     * @param clazz the class | 类
     * @return the canonical name or name | 规范名称或名称
     */
    public static String getCanonicalNameOrName(Class<?> clazz) {
        String canonical = clazz.getCanonicalName();
        return canonical != null ? canonical : clazz.getName();
    }

    /**
     * Gets package name
     * 获取包名
     *
     * @param clazz the class | 类
     * @return the package name | 包名
     */
    public static String getPackageName(Class<?> clazz) {
        return clazz.getPackageName();
    }

    /**
     * Checks if same package
     * 检查是否在同一包
     *
     * @param class1 the first class | 第一个类
     * @param class2 the second class | 第二个类
     * @return true if same package | 如果在同一包返回true
     */
    public static boolean isSamePackage(Class<?> class1, Class<?> class2) {
        return class1.getPackageName().equals(class2.getPackageName());
    }

    // ==================== Assignability | 可赋值性 ====================

    /**
     * Checks if assignable from
     * 检查是否可从另一类赋值
     *
     * @param target the target type | 目标类型
     * @param source the source type | 源类型
     * @return true if assignable | 如果可赋值返回true
     */
    public static boolean isAssignableFrom(Class<?> target, Class<?> source) {
        if (target.isAssignableFrom(source)) {
            return true;
        }
        // Handle primitive/wrapper compatibility
        if (target.isPrimitive() && isWrapper(source)) {
            return target == wrapperToPrimitive(source);
        }
        if (isWrapper(target) && source.isPrimitive()) {
            return wrapperToPrimitive(target) == source;
        }
        return false;
    }

    /**
     * Gets common superclass
     * 获取公共父类
     *
     * @param class1 the first class | 第一个类
     * @param class2 the second class | 第二个类
     * @return the common superclass | 公共父类
     */
    public static Class<?> getCommonSuperclass(Class<?> class1, Class<?> class2) {
        if (class1.isAssignableFrom(class2)) {
            return class1;
        }
        if (class2.isAssignableFrom(class1)) {
            return class2;
        }
        Class<?> current = class1;
        while (current != null) {
            if (current.isAssignableFrom(class2)) {
                return current;
            }
            current = current.getSuperclass();
        }
        return Object.class;
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Clears all caches
     * 清除所有缓存
     */
    public static void clearCache() {
        CLASS_CACHE.clear();
        HIERARCHY_CACHE.clear();
        INTERFACES_CACHE.clear();
    }

    /**
     * Clears cache for specific class
     * 清除特定类的缓存
     *
     * @param clazz the class | 类
     */
    public static void clearCache(Class<?> clazz) {
        CLASS_CACHE.values().removeIf(c -> c == clazz);
        HIERARCHY_CACHE.remove(clazz);
        INTERFACES_CACHE.remove(clazz);
    }
}
