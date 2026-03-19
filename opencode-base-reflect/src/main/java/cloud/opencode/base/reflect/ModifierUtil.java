package cloud.opencode.base.reflect;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Modifier Utility Class
 * 修饰符工具类
 *
 * <p>Provides utilities for checking and manipulating Java modifiers.</p>
 * <p>提供检查和操作Java修饰符的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Modifier checking utilities - 修饰符检查工具</li>
 *   <li>Access level determination - 访问级别判断</li>
 *   <li>Modifier string conversion - 修饰符字符串转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean isPublic = ModifierUtil.isPublic(field.getModifiers());
 * boolean isStatic = ModifierUtil.isStatic(method.getModifiers());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (works with int modifiers) - 空值安全: 是（使用int修饰符）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for all modifier checks (bitwise operations) - 时间复杂度: 所有修饰符检查均为 O(1)（位运算）</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class ModifierUtil {

    private ModifierUtil() {
    }

    // ==================== Access Modifiers | 访问修饰符 ====================

    /**
     * Checks if public
     * 检查是否为public
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if public | 如果是public返回true
     */
    public static boolean isPublic(int modifiers) {
        return Modifier.isPublic(modifiers);
    }

    /**
     * Checks if member is public
     * 检查成员是否为public
     *
     * @param member the member | 成员
     * @return true if public | 如果是public返回true
     */
    public static boolean isPublic(Member member) {
        return isPublic(member.getModifiers());
    }

    /**
     * Checks if class is public
     * 检查类是否为public
     *
     * @param clazz the class | 类
     * @return true if public | 如果是public返回true
     */
    public static boolean isPublic(Class<?> clazz) {
        return isPublic(clazz.getModifiers());
    }

    /**
     * Checks if private
     * 检查是否为private
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if private | 如果是private返回true
     */
    public static boolean isPrivate(int modifiers) {
        return Modifier.isPrivate(modifiers);
    }

    /**
     * Checks if member is private
     * 检查成员是否为private
     *
     * @param member the member | 成员
     * @return true if private | 如果是private返回true
     */
    public static boolean isPrivate(Member member) {
        return isPrivate(member.getModifiers());
    }

    /**
     * Checks if protected
     * 检查是否为protected
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if protected | 如果是protected返回true
     */
    public static boolean isProtected(int modifiers) {
        return Modifier.isProtected(modifiers);
    }

    /**
     * Checks if member is protected
     * 检查成员是否为protected
     *
     * @param member the member | 成员
     * @return true if protected | 如果是protected返回true
     */
    public static boolean isProtected(Member member) {
        return isProtected(member.getModifiers());
    }

    /**
     * Checks if package-private (default access)
     * 检查是否为包私有（默认访问）
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if package-private | 如果是包私有返回true
     */
    public static boolean isPackagePrivate(int modifiers) {
        return !isPublic(modifiers) && !isPrivate(modifiers) && !isProtected(modifiers);
    }

    /**
     * Checks if member is package-private
     * 检查成员是否为包私有
     *
     * @param member the member | 成员
     * @return true if package-private | 如果是包私有返回true
     */
    public static boolean isPackagePrivate(Member member) {
        return isPackagePrivate(member.getModifiers());
    }

    // ==================== Non-Access Modifiers | 非访问修饰符 ====================

    /**
     * Checks if static
     * 检查是否为static
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if static | 如果是static返回true
     */
    public static boolean isStatic(int modifiers) {
        return Modifier.isStatic(modifiers);
    }

    /**
     * Checks if member is static
     * 检查成员是否为static
     *
     * @param member the member | 成员
     * @return true if static | 如果是static返回true
     */
    public static boolean isStatic(Member member) {
        return isStatic(member.getModifiers());
    }

    /**
     * Checks if final
     * 检查是否为final
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if final | 如果是final返回true
     */
    public static boolean isFinal(int modifiers) {
        return Modifier.isFinal(modifiers);
    }

    /**
     * Checks if member is final
     * 检查成员是否为final
     *
     * @param member the member | 成员
     * @return true if final | 如果是final返回true
     */
    public static boolean isFinal(Member member) {
        return isFinal(member.getModifiers());
    }

    /**
     * Checks if class is final
     * 检查类是否为final
     *
     * @param clazz the class | 类
     * @return true if final | 如果是final返回true
     */
    public static boolean isFinal(Class<?> clazz) {
        return isFinal(clazz.getModifiers());
    }

    /**
     * Checks if abstract
     * 检查是否为abstract
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if abstract | 如果是abstract返回true
     */
    public static boolean isAbstract(int modifiers) {
        return Modifier.isAbstract(modifiers);
    }

    /**
     * Checks if class is abstract
     * 检查类是否为abstract
     *
     * @param clazz the class | 类
     * @return true if abstract | 如果是abstract返回true
     */
    public static boolean isAbstract(Class<?> clazz) {
        return isAbstract(clazz.getModifiers());
    }

    /**
     * Checks if transient
     * 检查是否为transient
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if transient | 如果是transient返回true
     */
    public static boolean isTransient(int modifiers) {
        return Modifier.isTransient(modifiers);
    }

    /**
     * Checks if volatile
     * 检查是否为volatile
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if volatile | 如果是volatile返回true
     */
    public static boolean isVolatile(int modifiers) {
        return Modifier.isVolatile(modifiers);
    }

    /**
     * Checks if synchronized
     * 检查是否为synchronized
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if synchronized | 如果是synchronized返回true
     */
    public static boolean isSynchronized(int modifiers) {
        return Modifier.isSynchronized(modifiers);
    }

    /**
     * Checks if native
     * 检查是否为native
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if native | 如果是native返回true
     */
    public static boolean isNative(int modifiers) {
        return Modifier.isNative(modifiers);
    }

    /**
     * Checks if strictfp
     * 检查是否为strictfp
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if strictfp | 如果是strictfp返回true
     */
    public static boolean isStrict(int modifiers) {
        return Modifier.isStrict(modifiers);
    }

    // ==================== Modifier Composition | 修饰符组合 ====================

    /**
     * Checks if has all modifiers
     * 检查是否有所有修饰符
     *
     * @param modifiers the modifiers | 修饰符
     * @param required  the required modifiers | 需要的修饰符
     * @return true if has all | 如果有所有返回true
     */
    public static boolean hasAll(int modifiers, int required) {
        return (modifiers & required) == required;
    }

    /**
     * Checks if has any modifier
     * 检查是否有任一修饰符
     *
     * @param modifiers the modifiers | 修饰符
     * @param any       the any modifiers | 任一修饰符
     * @return true if has any | 如果有任一返回true
     */
    public static boolean hasAny(int modifiers, int any) {
        return (modifiers & any) != 0;
    }

    /**
     * Checks if has none of modifiers
     * 检查是否没有任何修饰符
     *
     * @param modifiers the modifiers | 修饰符
     * @param excluded  the excluded modifiers | 排除的修饰符
     * @return true if has none | 如果没有返回true
     */
    public static boolean hasNone(int modifiers, int excluded) {
        return (modifiers & excluded) == 0;
    }

    // ==================== Modifier String | 修饰符字符串 ====================

    /**
     * Converts modifiers to string
     * 修饰符转字符串
     *
     * @param modifiers the modifiers | 修饰符
     * @return the string representation | 字符串表示
     */
    public static String toString(int modifiers) {
        return Modifier.toString(modifiers);
    }

    /**
     * Gets access level name
     * 获取访问级别名称
     *
     * @param modifiers the modifiers | 修饰符
     * @return the access level name | 访问级别名称
     */
    public static String getAccessLevelName(int modifiers) {
        if (isPublic(modifiers)) return "public";
        if (isPrivate(modifiers)) return "private";
        if (isProtected(modifiers)) return "protected";
        return "package-private";
    }

    // ==================== Modifier Constants | 修饰符常量 ====================

    /**
     * Gets public modifier
     * 获取public修饰符
     *
     * @return the public modifier | public修饰符
     */
    public static int publicModifier() {
        return Modifier.PUBLIC;
    }

    /**
     * Gets private modifier
     * 获取private修饰符
     *
     * @return the private modifier | private修饰符
     */
    public static int privateModifier() {
        return Modifier.PRIVATE;
    }

    /**
     * Gets protected modifier
     * 获取protected修饰符
     *
     * @return the protected modifier | protected修饰符
     */
    public static int protectedModifier() {
        return Modifier.PROTECTED;
    }

    /**
     * Gets static modifier
     * 获取static修饰符
     *
     * @return the static modifier | static修饰符
     */
    public static int staticModifier() {
        return Modifier.STATIC;
    }

    /**
     * Gets final modifier
     * 获取final修饰符
     *
     * @return the final modifier | final修饰符
     */
    public static int finalModifier() {
        return Modifier.FINAL;
    }

    /**
     * Gets abstract modifier
     * 获取abstract修饰符
     *
     * @return the abstract modifier | abstract修饰符
     */
    public static int abstractModifier() {
        return Modifier.ABSTRACT;
    }

    /**
     * Gets transient modifier
     * 获取transient修饰符
     *
     * @return the transient modifier | transient修饰符
     */
    public static int transientModifier() {
        return Modifier.TRANSIENT;
    }

    /**
     * Gets volatile modifier
     * 获取volatile修饰符
     *
     * @return the volatile modifier | volatile修饰符
     */
    public static int volatileModifier() {
        return Modifier.VOLATILE;
    }

    /**
     * Gets synchronized modifier
     * 获取synchronized修饰符
     *
     * @return the synchronized modifier | synchronized修饰符
     */
    public static int synchronizedModifier() {
        return Modifier.SYNCHRONIZED;
    }

    /**
     * Gets native modifier
     * 获取native修饰符
     *
     * @return the native modifier | native修饰符
     */
    public static int nativeModifier() {
        return Modifier.NATIVE;
    }
}
