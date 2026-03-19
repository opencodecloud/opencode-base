package cloud.opencode.base.reflect.sealed;

import java.util.*;

/**
 * Sealed Class Facade Entry Class
 * 密封类门面入口类
 *
 * <p>Provides common sealed class operations API.</p>
 * <p>提供常用密封类操作API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed class detection - 密封类检测</li>
 *   <li>Permitted subclasses access - 许可子类访问</li>
 *   <li>Hierarchy navigation - 层次结构导航</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean isSealed = OpenSealed.isSealed(Shape.class);
 * List<Class<?>> subclasses = OpenSealed.getPermittedSubclasses(Shape.class);
 * PermittedSubclasses permitted = OpenSealed.permitted(Shape.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Partially (null class returns false for isSealed) - 空值安全: 部分（null类在isSealed中返回false）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class OpenSealed {

    private OpenSealed() {
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
     * @param <T>   the type | 类型
     * @return the class | 类
     * @throws IllegalArgumentException if not sealed | 如果不是密封类
     */
    public static <T> Class<T> requireSealed(Class<T> clazz) {
        if (!isSealed(clazz)) {
            throw new IllegalArgumentException("Class is not sealed: " + clazz.getName());
        }
        return clazz;
    }

    // ==================== Permitted Subclasses | 许可子类 ====================

    /**
     * Gets permitted subclasses
     * 获取许可子类
     *
     * @param sealedClass the sealed class | 密封类
     * @return the PermittedSubclasses collection | PermittedSubclasses集合
     */
    public static PermittedSubclasses getPermittedSubclasses(Class<?> sealedClass) {
        return new PermittedSubclasses(sealedClass);
    }

    /**
     * Gets permitted subclasses as list
     * 获取许可子类列表
     *
     * @param sealedClass the sealed class | 密封类
     * @return list of permitted subclasses | 许可子类列表
     */
    public static List<Class<?>> getPermittedSubclassList(Class<?> sealedClass) {
        requireSealed(sealedClass);
        return Arrays.asList(sealedClass.getPermittedSubclasses());
    }

    /**
     * Gets the number of permitted subclasses
     * 获取许可子类数量
     *
     * @param sealedClass the sealed class | 密封类
     * @return the count | 数量
     */
    public static int getPermittedSubclassCount(Class<?> sealedClass) {
        requireSealed(sealedClass);
        return sealedClass.getPermittedSubclasses().length;
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
        for (Class<?> permitted : sealedClass.getPermittedSubclasses()) {
            if (permitted.equals(subclass)) {
                return true;
            }
        }
        return false;
    }

    // ==================== Hierarchy Navigation | 层次结构导航 ====================

    /**
     * Gets all subclasses recursively
     * 递归获取所有子类
     *
     * @param sealedClass the sealed class | 密封类
     * @return set of all subclasses | 所有子类集合
     */
    public static Set<Class<?>> getAllSubclassesRecursive(Class<?> sealedClass) {
        return getPermittedSubclasses(sealedClass).getAllRecursive();
    }

    /**
     * Gets all leaf (non-sealed, non-abstract) classes
     * 获取所有叶（非密封、非抽象）类
     *
     * @param sealedClass the sealed class | 密封类
     * @return list of leaf classes | 叶类列表
     */
    public static List<Class<?>> getLeafClasses(Class<?> sealedClass) {
        return getPermittedSubclasses(sealedClass).getHierarchy().getLeafClasses();
    }

    /**
     * Gets the hierarchy tree
     * 获取层次结构树
     *
     * @param sealedClass the sealed class | 密封类
     * @return the hierarchy node | 层次结构节点
     */
    public static PermittedSubclasses.HierarchyNode getHierarchy(Class<?> sealedClass) {
        return getPermittedSubclasses(sealedClass).getHierarchy();
    }

    /**
     * Gets the sealed parent of a class (if any)
     * 获取类的密封父类（如果有）
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

        for (Class<?> subclass : sealedClass.getPermittedSubclasses()) {
            if (!sealedClass.isAssignableFrom(subclass)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the sealed hierarchy is exhaustive (all cases handled)
     * 检查密封层次结构是否穷尽（所有情况都已处理）
     *
     * @param sealedClass the sealed class | 密封类
     * @return true if exhaustive | 如果穷尽返回true
     */
    public static boolean isExhaustive(Class<?> sealedClass) {
        if (!isSealed(sealedClass)) {
            return false;
        }

        // A hierarchy is exhaustive if all leaves are either:
        // - final classes
        // - records (implicitly final)
        // - non-sealed classes (can have any subclasses)
        List<Class<?>> leaves = getLeafClasses(sealedClass);
        for (Class<?> leaf : leaves) {
            if (!java.lang.reflect.Modifier.isFinal(leaf.getModifiers()) && !leaf.isRecord()) {
                return false;
            }
        }
        return true;
    }

    // ==================== Pattern Matching Support | 模式匹配支持 ====================

    /**
     * Gets all concrete types for pattern matching
     * 获取模式匹配的所有具体类型
     *
     * @param sealedClass the sealed class | 密封类
     * @return list of concrete types | 具体类型列表
     */
    public static List<Class<?>> getConcreteTypes(Class<?> sealedClass) {
        Set<Class<?>> result = new LinkedHashSet<>();
        collectConcreteTypes(sealedClass, result);
        return new ArrayList<>(result);
    }

    private static void collectConcreteTypes(Class<?> clazz, Set<Class<?>> result) {
        if (!clazz.isSealed()) {
            if (!java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
                result.add(clazz);
            }
            return;
        }

        for (Class<?> sub : clazz.getPermittedSubclasses()) {
            collectConcreteTypes(sub, result);
        }
    }

    /**
     * Generates a switch expression pattern matching template (as string)
     * 生成switch表达式模式匹配模板（作为字符串）
     *
     * @param sealedClass the sealed class | 密封类
     * @param variableName the variable name | 变量名
     * @return the template string | 模板字符串
     */
    public static String generateSwitchTemplate(Class<?> sealedClass, String variableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("switch (").append(variableName).append(") {\n");

        List<Class<?>> concreteTypes = getConcreteTypes(sealedClass);
        for (Class<?> type : concreteTypes) {
            String simple = type.getSimpleName();
            String varName = Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
            sb.append("    case ").append(simple).append(" ")
              .append(varName).append(" -> {\n");
            sb.append("        // Handle ").append(simple).append("\n");
            sb.append("    }\n");
        }

        sb.append("}");
        return sb.toString();
    }
}
