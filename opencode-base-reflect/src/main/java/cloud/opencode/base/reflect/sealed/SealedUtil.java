package cloud.opencode.base.reflect.sealed;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sealed Class Utility Class
 * 密封类工具类
 *
 * <p>Provides low-level sealed class operation utilities with caching.</p>
 * <p>提供带缓存的底层密封类操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed class detection - 密封类检测</li>
 *   <li>Permitted subclasses discovery with caching - 带缓存的许可子类发现</li>
 *   <li>Recursive subclass resolution - 递归子类解析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean isSealed = SealedUtil.isSealed(Shape.class);
 * Class<?>[] permitted = SealedUtil.getPermittedSubclasses(Shape.class);
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
 *   <li>Time complexity: O(1) for cached lookups; O(p) for first access where p is the number of permitted subclasses - 时间复杂度: 缓存命中时 O(1)；首次访问为 O(p)，p为许可子类数量</li>
 *   <li>Space complexity: O(p) for the cached permitted subclasses per sealed class - 空间复杂度: O(p)，每个密封类缓存许可子类</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class SealedUtil {

    private static final Map<Class<?>, Class<?>[]> PERMITTED_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Set<Class<?>>> ALL_SUBCLASSES_CACHE = new ConcurrentHashMap<>();

    private SealedUtil() {
    }

    // ==================== Sealed Detection | 密封检测 ====================

    /**
     * Checks if a class is sealed
     * 检查类是否为密封类
     *
     * @param clazz the class | 类
     * @return true if sealed | 如果是密封类返回true
     */
    public static boolean isSealed(Class<?> clazz) {
        return clazz != null && clazz.isSealed();
    }

    /**
     * Checks if an object's class is sealed
     * 检查对象的类是否为密封类
     *
     * @param obj the object | 对象
     * @return true if sealed | 如果是密封类返回true
     */
    public static boolean isSealedClass(Object obj) {
        return obj != null && obj.getClass().isSealed();
    }

    /**
     * Requires class to be sealed
     * 要求类为密封类
     *
     * @param clazz the class | 类
     * @throws IllegalArgumentException if not sealed | 如果不是密封类
     */
    public static void requireSealed(Class<?> clazz) {
        if (!isSealed(clazz)) {
            throw new IllegalArgumentException("Class is not sealed: " + clazz);
        }
    }

    /**
     * Checks if a class is a non-sealed subclass
     * 检查类是否为非密封子类
     *
     * @param clazz the class | 类
     * @return true if non-sealed | 如果是非密封返回true
     */
    public static boolean isNonSealed(Class<?> clazz) {
        // A class is non-sealed if it's neither sealed nor final
        // and extends/implements a sealed type
        if (clazz == null || clazz.isSealed() || Modifier.isFinal(clazz.getModifiers())) {
            return false;
        }
        return hasSealedParent(clazz) || hasSealedInterface(clazz);
    }

    // ==================== Permitted Subclasses | 许可子类 ====================

    /**
     * Gets permitted subclasses (cached)
     * 获取许可子类（缓存）
     *
     * @param sealedClass the sealed class | 密封类
     * @return array of permitted subclasses | 许可子类数组
     */
    public static Class<?>[] getPermittedSubclasses(Class<?> sealedClass) {
        requireSealed(sealedClass);
        return PERMITTED_CACHE.computeIfAbsent(sealedClass, Class::getPermittedSubclasses);
    }

    /**
     * Gets permitted subclasses as list
     * 获取许可子类列表
     *
     * @param sealedClass the sealed class | 密封类
     * @return list of permitted subclasses | 许可子类列表
     */
    public static List<Class<?>> getPermittedSubclassList(Class<?> sealedClass) {
        return Arrays.asList(getPermittedSubclasses(sealedClass));
    }

    /**
     * Gets permitted subclasses count
     * 获取许可子类数量
     *
     * @param sealedClass the sealed class | 密封类
     * @return the count | 数量
     */
    public static int getPermittedSubclassCount(Class<?> sealedClass) {
        return getPermittedSubclasses(sealedClass).length;
    }

    /**
     * Checks if a class is a permitted subclass
     * 检查类是否为许可子类
     *
     * @param sealedClass the sealed class | 密封类
     * @param subclass    the potential subclass | 潜在子类
     * @return true if permitted | 如果是许可的返回true
     */
    public static boolean isPermittedSubclass(Class<?> sealedClass, Class<?> subclass) {
        if (!isSealed(sealedClass)) {
            return false;
        }
        for (Class<?> permitted : getPermittedSubclasses(sealedClass)) {
            if (permitted.equals(subclass)) {
                return true;
            }
        }
        return false;
    }

    // ==================== Hierarchy Navigation | 层次结构导航 ====================

    /**
     * Gets all subclasses recursively (cached)
     * 递归获取所有子类（缓存）
     *
     * @param sealedClass the sealed class | 密封类
     * @return set of all subclasses | 所有子类集合
     */
    public static Set<Class<?>> getAllSubclassesRecursive(Class<?> sealedClass) {
        return ALL_SUBCLASSES_CACHE.computeIfAbsent(sealedClass, SealedUtil::collectAllSubclasses);
    }

    private static Set<Class<?>> collectAllSubclasses(Class<?> sealedClass) {
        if (!isSealed(sealedClass)) {
            return Collections.emptySet();
        }

        Set<Class<?>> result = new LinkedHashSet<>();
        collectSubclassesRecursive(sealedClass, result);
        return Collections.unmodifiableSet(result);
    }

    private static void collectSubclassesRecursive(Class<?> clazz, Set<Class<?>> result) {
        if (!clazz.isSealed()) {
            return;
        }
        for (Class<?> subclass : clazz.getPermittedSubclasses()) {
            result.add(subclass);
            collectSubclassesRecursive(subclass, result);
        }
    }

    /**
     * Gets all leaf classes (final or non-sealed)
     * 获取所有叶类（final或非密封）
     *
     * @param sealedClass the sealed class | 密封类
     * @return list of leaf classes | 叶类列表
     */
    public static List<Class<?>> getLeafClasses(Class<?> sealedClass) {
        List<Class<?>> leaves = new ArrayList<>();
        collectLeafClasses(sealedClass, leaves);
        return leaves;
    }

    private static void collectLeafClasses(Class<?> clazz, List<Class<?>> result) {
        if (!clazz.isSealed()) {
            if (!Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
                result.add(clazz);
            }
            return;
        }
        for (Class<?> subclass : clazz.getPermittedSubclasses()) {
            collectLeafClasses(subclass, result);
        }
    }

    /**
     * Gets all concrete (instantiable) types
     * 获取所有具体（可实例化）类型
     *
     * @param sealedClass the sealed class | 密封类
     * @return list of concrete types | 具体类型列表
     */
    public static List<Class<?>> getConcreteTypes(Class<?> sealedClass) {
        Set<Class<?>> all = getAllSubclassesRecursive(sealedClass);
        List<Class<?>> concrete = new ArrayList<>();
        for (Class<?> clazz : all) {
            if (!Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
                concrete.add(clazz);
            }
        }
        return concrete;
    }

    // ==================== Parent Finding | 父类查找 ====================

    /**
     * Checks if class has a sealed parent
     * 检查类是否有密封父类
     *
     * @param clazz the class | 类
     * @return true if has sealed parent | 如果有密封父类返回true
     */
    public static boolean hasSealedParent(Class<?> clazz) {
        Class<?> parent = clazz.getSuperclass();
        while (parent != null && parent != Object.class) {
            if (parent.isSealed()) {
                return true;
            }
            parent = parent.getSuperclass();
        }
        return false;
    }

    /**
     * Gets the sealed parent class
     * 获取密封父类
     *
     * @param clazz the class | 类
     * @return Optional of sealed parent | 密封父类的Optional
     */
    public static Optional<Class<?>> getSealedParent(Class<?> clazz) {
        Class<?> parent = clazz.getSuperclass();
        while (parent != null && parent != Object.class) {
            if (parent.isSealed()) {
                return Optional.of(parent);
            }
            parent = parent.getSuperclass();
        }
        return Optional.empty();
    }

    /**
     * Checks if class implements a sealed interface
     * 检查类是否实现密封接口
     *
     * @param clazz the class | 类
     * @return true if implements sealed interface | 如果实现密封接口返回true
     */
    public static boolean hasSealedInterface(Class<?> clazz) {
        return !getSealedInterfaces(clazz).isEmpty();
    }

    /**
     * Gets all sealed interfaces implemented by a class
     * 获取类实现的所有密封接口
     *
     * @param clazz the class | 类
     * @return list of sealed interfaces | 密封接口列表
     */
    public static List<Class<?>> getSealedInterfaces(Class<?> clazz) {
        List<Class<?>> result = new ArrayList<>();
        collectSealedInterfaces(clazz, result, new HashSet<>());
        return result;
    }

    private static void collectSealedInterfaces(Class<?> clazz, List<Class<?>> result, Set<Class<?>> visited) {
        if (clazz == null || visited.contains(clazz)) {
            return;
        }
        visited.add(clazz);

        for (Class<?> iface : clazz.getInterfaces()) {
            if (iface.isSealed() && !result.contains(iface)) {
                result.add(iface);
            }
            collectSealedInterfaces(iface, result, visited);
        }

        collectSealedInterfaces(clazz.getSuperclass(), result, visited);
    }

    // ==================== Validation | 验证 ====================

    /**
     * Validates that all permitted subclasses extend the sealed class
     * 验证所有许可子类都继承密封类
     *
     * @param sealedClass the sealed class | 密封类
     * @return true if valid | 如果有效返回true
     */
    public static boolean validateHierarchy(Class<?> sealedClass) {
        if (!isSealed(sealedClass)) {
            return false;
        }

        for (Class<?> subclass : getPermittedSubclasses(sealedClass)) {
            if (!sealedClass.isAssignableFrom(subclass)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the sealed hierarchy is exhaustive
     * 检查密封层次结构是否穷尽
     *
     * @param sealedClass the sealed class | 密封类
     * @return true if exhaustive | 如果穷尽返回true
     */
    public static boolean isExhaustive(Class<?> sealedClass) {
        if (!isSealed(sealedClass)) {
            return false;
        }

        List<Class<?>> leaves = getLeafClasses(sealedClass);
        for (Class<?> leaf : leaves) {
            if (!Modifier.isFinal(leaf.getModifiers()) && !leaf.isRecord()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets non-exhaustive leaf classes
     * 获取非穷尽叶类
     *
     * @param sealedClass the sealed class | 密封类
     * @return list of non-exhaustive leaves | 非穷尽叶列表
     */
    public static List<Class<?>> getNonExhaustiveLeaves(Class<?> sealedClass) {
        List<Class<?>> result = new ArrayList<>();
        for (Class<?> leaf : getLeafClasses(sealedClass)) {
            if (!Modifier.isFinal(leaf.getModifiers()) && !leaf.isRecord()) {
                result.add(leaf);
            }
        }
        return result;
    }

    // ==================== Depth and Structure | 深度和结构 ====================

    /**
     * Gets the depth of the sealed hierarchy
     * 获取密封层次结构的深度
     *
     * @param sealedClass the sealed class | 密封类
     * @return the depth | 深度
     */
    public static int getHierarchyDepth(Class<?> sealedClass) {
        if (!isSealed(sealedClass)) {
            return 0;
        }
        int maxDepth = 0;
        for (Class<?> subclass : getPermittedSubclasses(sealedClass)) {
            int depth = 1 + getHierarchyDepth(subclass);
            maxDepth = Math.max(maxDepth, depth);
        }
        return maxDepth;
    }

    /**
     * Gets the total number of types in the hierarchy
     * 获取层次结构中的类型总数
     *
     * @param sealedClass the sealed class | 密封类
     * @return the total count | 总数
     */
    public static int getTotalTypeCount(Class<?> sealedClass) {
        return 1 + getAllSubclassesRecursive(sealedClass).size();
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Clears all caches
     * 清除所有缓存
     */
    public static void clearCache() {
        PERMITTED_CACHE.clear();
        ALL_SUBCLASSES_CACHE.clear();
    }

    /**
     * Clears cache for specific class
     * 清除特定类的缓存
     *
     * @param clazz the class | 类
     */
    public static void clearCache(Class<?> clazz) {
        PERMITTED_CACHE.remove(clazz);
        ALL_SUBCLASSES_CACHE.remove(clazz);
    }
}
