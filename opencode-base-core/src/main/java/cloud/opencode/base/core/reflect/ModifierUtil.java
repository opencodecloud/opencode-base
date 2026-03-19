package cloud.opencode.base.core.reflect;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Modifier Utility Class - Java modifier checking operations
 * 修饰符工具类 - Java 修饰符检查操作
 *
 * <p>Provides utilities for checking Java modifiers on classes, methods, and fields.</p>
 * <p>提供类、方法和字段的 Java 修饰符检查工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Access modifiers (public, private, protected, package) - 访问修饰符</li>
 *   <li>Class modifiers (abstract, final, interface) - 类修饰符</li>
 *   <li>Member modifiers (static, final, volatile, transient) - 成员修饰符</li>
 *   <li>Method modifiers (synchronized, native, strict) - 方法修饰符</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check access modifier - 检查访问修饰符
 * boolean isPub = ModifierUtil.isPublic(field);
 *
 * // Check class modifier - 检查类修饰符
 * boolean isAbstract = ModifierUtil.isAbstract(MyClass.class);
 *
 * // Get modifier string - 获取修饰符字符串
 * String modStr = ModifierUtil.toString(method);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per check - 每次检查 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class ModifierUtil {

    private ModifierUtil() {
    }

    /**
     * Checks if the modifier is public
     * 检查是否为 public
     */
    public static boolean isPublic(int modifiers) {
        return Modifier.isPublic(modifiers);
    }

    /**
     * Checks if the modifier is public
     * 检查是否为 public
     */
    public static boolean isPublic(Member member) {
        return isPublic(member.getModifiers());
    }

    /**
     * Checks if the modifier is public
     * 检查是否为 public
     */
    public static boolean isPublic(Class<?> clazz) {
        return isPublic(clazz.getModifiers());
    }

    /**
     * Checks if the modifier is private
     * 检查是否为 private
     */
    public static boolean isPrivate(int modifiers) {
        return Modifier.isPrivate(modifiers);
    }

    /**
     * Checks if the modifier is private
     * 检查是否为 private
     */
    public static boolean isPrivate(Member member) {
        return isPrivate(member.getModifiers());
    }

    /**
     * Checks if the modifier is protected
     * 检查是否为 protected
     */
    public static boolean isProtected(int modifiers) {
        return Modifier.isProtected(modifiers);
    }

    /**
     * Checks if the modifier is protected
     * 检查是否为 protected
     */
    public static boolean isProtected(Member member) {
        return isProtected(member.getModifiers());
    }

    /**
     * Checks if the modifier is static
     * 检查是否为 static
     */
    public static boolean isStatic(int modifiers) {
        return Modifier.isStatic(modifiers);
    }

    /**
     * Checks if the modifier is static
     * 检查是否为 static
     */
    public static boolean isStatic(Member member) {
        return isStatic(member.getModifiers());
    }

    /**
     * Checks if the modifier is final
     * 检查是否为 final
     */
    public static boolean isFinal(int modifiers) {
        return Modifier.isFinal(modifiers);
    }

    /**
     * Checks if the modifier is final
     * 检查是否为 final
     */
    public static boolean isFinal(Member member) {
        return isFinal(member.getModifiers());
    }

    /**
     * Checks if the modifier is final
     * 检查是否为 final
     */
    public static boolean isFinal(Class<?> clazz) {
        return isFinal(clazz.getModifiers());
    }

    /**
     * Checks if the modifier is synchronized
     * 检查是否为 synchronized
     */
    public static boolean isSynchronized(int modifiers) {
        return Modifier.isSynchronized(modifiers);
    }

    /**
     * Checks if the modifier is volatile
     * 检查是否为 volatile
     */
    public static boolean isVolatile(int modifiers) {
        return Modifier.isVolatile(modifiers);
    }

    /**
     * Checks if the modifier is transient
     * 检查是否为 transient
     */
    public static boolean isTransient(int modifiers) {
        return Modifier.isTransient(modifiers);
    }

    /**
     * Checks if the modifier is native
     * 检查是否为 native
     */
    public static boolean isNative(int modifiers) {
        return Modifier.isNative(modifiers);
    }

    /**
     * Checks if the modifier is interface
     * 检查是否为 interface
     */
    public static boolean isInterface(int modifiers) {
        return Modifier.isInterface(modifiers);
    }

    /**
     * Checks if the modifier is interface
     * 检查是否为 interface
     */
    public static boolean isInterface(Class<?> clazz) {
        return clazz.isInterface();
    }

    /**
     * Checks if the modifier is abstract
     * 检查是否为 abstract
     */
    public static boolean isAbstract(int modifiers) {
        return Modifier.isAbstract(modifiers);
    }

    /**
     * Checks if the modifier is abstract
     * 检查是否为 abstract
     */
    public static boolean isAbstract(Class<?> clazz) {
        return isAbstract(clazz.getModifiers());
    }

    /**
     * Checks if the modifier is strict (strictfp)
     * 检查是否为 strict (strictfp)
     */
    public static boolean isStrict(int modifiers) {
        return Modifier.isStrict(modifiers);
    }

    /**
     * Checks if the modifier is package-private (no access modifier)
     * 检查是否为包可见（无访问修饰符）
     */
    public static boolean isPackagePrivate(int modifiers) {
        return !isPublic(modifiers) && !isProtected(modifiers) && !isPrivate(modifiers);
    }

    /**
     * Checks if the modifier is package-private
     * 检查是否为包可见
     */
    public static boolean isPackagePrivate(Member member) {
        return isPackagePrivate(member.getModifiers());
    }

    /**
     * Gets the string representation of the modifiers
     * 获取修饰符字符串表示
     */
    public static String toString(int modifiers) {
        return Modifier.toString(modifiers);
    }

    /**
     * Gets the modifier string of a class
     * 获取类的修饰符字符串
     */
    public static String toString(Class<?> clazz) {
        return toString(clazz.getModifiers());
    }

    /**
     * Gets the modifier string of a member
     * 获取成员的修饰符字符串
     */
    public static String toString(Member member) {
        return toString(member.getModifiers());
    }
}
