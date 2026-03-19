package cloud.opencode.base.reflect;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Modifier Facade Entry Class
 * 修饰符门面入口类
 *
 * <p>Provides common modifier operations API.</p>
 * <p>提供常用修饰符操作API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Modifier checking - 修饰符检查</li>
 *   <li>Modifier parsing - 修饰符解析</li>
 *   <li>Modifier string conversion - 修饰符字符串转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check member modifiers
 * boolean isPublic = OpenModifier.isPublic(method);
 * boolean isStatic = OpenModifier.isStatic(field);
 *
 * // Get access level
 * OpenModifier.AccessLevel level = OpenModifier.getAccessLevel(method);
 *
 * // Check if method can be overridden
 * boolean overridable = OpenModifier.isOverridable(method);
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
public final class OpenModifier {

    private OpenModifier() {
    }

    // ==================== Access Modifiers | 访问修饰符 ====================

    /**
     * Checks if public
     * 检查是否public
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if public | 如果是public返回true
     */
    public static boolean isPublic(int modifiers) {
        return Modifier.isPublic(modifiers);
    }

    /**
     * Checks if member is public
     * 检查成员是否public
     *
     * @param member the member | 成员
     * @return true if public | 如果是public返回true
     */
    public static boolean isPublic(Member member) {
        return Modifier.isPublic(member.getModifiers());
    }

    /**
     * Checks if class is public
     * 检查类是否public
     *
     * @param clazz the class | 类
     * @return true if public | 如果是public返回true
     */
    public static boolean isPublic(Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }

    /**
     * Checks if private
     * 检查是否private
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if private | 如果是private返回true
     */
    public static boolean isPrivate(int modifiers) {
        return Modifier.isPrivate(modifiers);
    }

    /**
     * Checks if member is private
     * 检查成员是否private
     *
     * @param member the member | 成员
     * @return true if private | 如果是private返回true
     */
    public static boolean isPrivate(Member member) {
        return Modifier.isPrivate(member.getModifiers());
    }

    /**
     * Checks if class is private
     * 检查类是否private
     *
     * @param clazz the class | 类
     * @return true if private | 如果是private返回true
     */
    public static boolean isPrivate(Class<?> clazz) {
        return Modifier.isPrivate(clazz.getModifiers());
    }

    /**
     * Checks if protected
     * 检查是否protected
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if protected | 如果是protected返回true
     */
    public static boolean isProtected(int modifiers) {
        return Modifier.isProtected(modifiers);
    }

    /**
     * Checks if member is protected
     * 检查成员是否protected
     *
     * @param member the member | 成员
     * @return true if protected | 如果是protected返回true
     */
    public static boolean isProtected(Member member) {
        return Modifier.isProtected(member.getModifiers());
    }

    /**
     * Checks if class is protected
     * 检查类是否protected
     *
     * @param clazz the class | 类
     * @return true if protected | 如果是protected返回true
     */
    public static boolean isProtected(Class<?> clazz) {
        return Modifier.isProtected(clazz.getModifiers());
    }

    /**
     * Checks if package-private (default access)
     * 检查是否包私有（默认访问）
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if package-private | 如果是包私有返回true
     */
    public static boolean isPackagePrivate(int modifiers) {
        return !isPublic(modifiers) && !isPrivate(modifiers) && !isProtected(modifiers);
    }

    /**
     * Checks if member is package-private
     * 检查成员是否包私有
     *
     * @param member the member | 成员
     * @return true if package-private | 如果是包私有返回true
     */
    public static boolean isPackagePrivate(Member member) {
        return isPackagePrivate(member.getModifiers());
    }

    /**
     * Checks if class is package-private
     * 检查类是否包私有
     *
     * @param clazz the class | 类
     * @return true if package-private | 如果是包私有返回true
     */
    public static boolean isPackagePrivate(Class<?> clazz) {
        return isPackagePrivate(clazz.getModifiers());
    }

    // ==================== Other Modifiers | 其他修饰符 ====================

    /**
     * Checks if static
     * 检查是否static
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if static | 如果是static返回true
     */
    public static boolean isStatic(int modifiers) {
        return Modifier.isStatic(modifiers);
    }

    /**
     * Checks if member is static
     * 检查成员是否static
     *
     * @param member the member | 成员
     * @return true if static | 如果是static返回true
     */
    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    /**
     * Checks if final
     * 检查是否final
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if final | 如果是final返回true
     */
    public static boolean isFinal(int modifiers) {
        return Modifier.isFinal(modifiers);
    }

    /**
     * Checks if member is final
     * 检查成员是否final
     *
     * @param member the member | 成员
     * @return true if final | 如果是final返回true
     */
    public static boolean isFinal(Member member) {
        return Modifier.isFinal(member.getModifiers());
    }

    /**
     * Checks if class is final
     * 检查类是否final
     *
     * @param clazz the class | 类
     * @return true if final | 如果是final返回true
     */
    public static boolean isFinal(Class<?> clazz) {
        return Modifier.isFinal(clazz.getModifiers());
    }

    /**
     * Checks if abstract
     * 检查是否abstract
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if abstract | 如果是abstract返回true
     */
    public static boolean isAbstract(int modifiers) {
        return Modifier.isAbstract(modifiers);
    }

    /**
     * Checks if class is abstract
     * 检查类是否abstract
     *
     * @param clazz the class | 类
     * @return true if abstract | 如果是abstract返回true
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Checks if method is abstract
     * 检查方法是否abstract
     *
     * @param method the method | 方法
     * @return true if abstract | 如果是abstract返回true
     */
    public static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    /**
     * Checks if synchronized
     * 检查是否synchronized
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if synchronized | 如果是synchronized返回true
     */
    public static boolean isSynchronized(int modifiers) {
        return Modifier.isSynchronized(modifiers);
    }

    /**
     * Checks if method is synchronized
     * 检查方法是否synchronized
     *
     * @param method the method | 方法
     * @return true if synchronized | 如果是synchronized返回true
     */
    public static boolean isSynchronized(Method method) {
        return Modifier.isSynchronized(method.getModifiers());
    }

    /**
     * Checks if volatile
     * 检查是否volatile
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if volatile | 如果是volatile返回true
     */
    public static boolean isVolatile(int modifiers) {
        return Modifier.isVolatile(modifiers);
    }

    /**
     * Checks if field is volatile
     * 检查字段是否volatile
     *
     * @param field the field | 字段
     * @return true if volatile | 如果是volatile返回true
     */
    public static boolean isVolatile(Field field) {
        return Modifier.isVolatile(field.getModifiers());
    }

    /**
     * Checks if transient
     * 检查是否transient
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if transient | 如果是transient返回true
     */
    public static boolean isTransient(int modifiers) {
        return Modifier.isTransient(modifiers);
    }

    /**
     * Checks if field is transient
     * 检查字段是否transient
     *
     * @param field the field | 字段
     * @return true if transient | 如果是transient返回true
     */
    public static boolean isTransient(Field field) {
        return Modifier.isTransient(field.getModifiers());
    }

    /**
     * Checks if native
     * 检查是否native
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if native | 如果是native返回true
     */
    public static boolean isNative(int modifiers) {
        return Modifier.isNative(modifiers);
    }

    /**
     * Checks if method is native
     * 检查方法是否native
     *
     * @param method the method | 方法
     * @return true if native | 如果是native返回true
     */
    public static boolean isNative(Method method) {
        return Modifier.isNative(method.getModifiers());
    }

    /**
     * Checks if interface
     * 检查是否interface
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if interface | 如果是interface返回true
     */
    public static boolean isInterface(int modifiers) {
        return Modifier.isInterface(modifiers);
    }

    /**
     * Checks if strict (strictfp)
     * 检查是否strictfp
     *
     * @param modifiers the modifiers | 修饰符
     * @return true if strict | 如果是strictfp返回true
     */
    public static boolean isStrict(int modifiers) {
        return Modifier.isStrict(modifiers);
    }

    // ==================== Synthetic Check | 合成检查 ====================

    /**
     * Checks if method is synthetic
     * 检查方法是否合成
     *
     * @param method the method | 方法
     * @return true if synthetic | 如果是合成返回true
     */
    public static boolean isSynthetic(Method method) {
        return method.isSynthetic();
    }

    /**
     * Checks if field is synthetic
     * 检查字段是否合成
     *
     * @param field the field | 字段
     * @return true if synthetic | 如果是合成返回true
     */
    public static boolean isSynthetic(Field field) {
        return field.isSynthetic();
    }

    /**
     * Checks if class is synthetic
     * 检查类是否合成
     *
     * @param clazz the class | 类
     * @return true if synthetic | 如果是合成返回true
     */
    public static boolean isSynthetic(Class<?> clazz) {
        return clazz.isSynthetic();
    }

    // ==================== Modifier String | 修饰符字符串 ====================

    /**
     * Converts modifiers to string
     * 修饰符转字符串
     *
     * @param modifiers the modifiers | 修饰符
     * @return string representation | 字符串表示
     */
    public static String toString(int modifiers) {
        return Modifier.toString(modifiers);
    }

    /**
     * Gets modifier names as list
     * 获取修饰符名称列表
     *
     * @param modifiers the modifiers | 修饰符
     * @return list of modifier names | 修饰符名称列表
     */
    public static List<String> toList(int modifiers) {
        List<String> result = new ArrayList<>();
        if (isPublic(modifiers)) result.add("public");
        if (isPrivate(modifiers)) result.add("private");
        if (isProtected(modifiers)) result.add("protected");
        if (isStatic(modifiers)) result.add("static");
        if (isFinal(modifiers)) result.add("final");
        if (isAbstract(modifiers)) result.add("abstract");
        if (isSynchronized(modifiers)) result.add("synchronized");
        if (isVolatile(modifiers)) result.add("volatile");
        if (isTransient(modifiers)) result.add("transient");
        if (isNative(modifiers)) result.add("native");
        if (isInterface(modifiers)) result.add("interface");
        if (isStrict(modifiers)) result.add("strictfp");
        return result;
    }

    // ==================== Access Level | 访问级别 ====================

    /**
     * Access level enumeration
     * 访问级别枚举
     */
    public enum AccessLevel {
        /**
         * Public access
         * 公共访问
         */
        PUBLIC,
        /**
         * Protected access
         * 保护访问
         */
        PROTECTED,
        /**
         * Package-private access
         * 包私有访问
         */
        PACKAGE_PRIVATE,
        /**
         * Private access
         * 私有访问
         */
        PRIVATE
    }

    /**
     * Gets access level from modifiers
     * 从修饰符获取访问级别
     *
     * @param modifiers the modifiers | 修饰符
     * @return the access level | 访问级别
     */
    public static AccessLevel getAccessLevel(int modifiers) {
        if (isPublic(modifiers)) return AccessLevel.PUBLIC;
        if (isProtected(modifiers)) return AccessLevel.PROTECTED;
        if (isPrivate(modifiers)) return AccessLevel.PRIVATE;
        return AccessLevel.PACKAGE_PRIVATE;
    }

    /**
     * Gets access level from member
     * 从成员获取访问级别
     *
     * @param member the member | 成员
     * @return the access level | 访问级别
     */
    public static AccessLevel getAccessLevel(Member member) {
        return getAccessLevel(member.getModifiers());
    }

    /**
     * Gets access level from class
     * 从类获取访问级别
     *
     * @param clazz the class | 类
     * @return the access level | 访问级别
     */
    public static AccessLevel getAccessLevel(Class<?> clazz) {
        return getAccessLevel(clazz.getModifiers());
    }

    // ==================== Modifier Comparison | 修饰符比较 ====================

    /**
     * Checks if access is at least the specified level
     * 检查访问级别是否至少为指定级别
     *
     * @param modifiers the modifiers | 修饰符
     * @param level     the minimum level | 最低级别
     * @return true if access is at least the level | 如果访问级别至少为指定级别返回true
     */
    public static boolean isAccessAtLeast(int modifiers, AccessLevel level) {
        AccessLevel actual = getAccessLevel(modifiers);
        return actual.ordinal() <= level.ordinal();
    }

    /**
     * Checks if method can be overridden
     * 检查方法是否可被重写
     *
     * @param method the method | 方法
     * @return true if can be overridden | 如果可被重写返回true
     */
    public static boolean isOverridable(Method method) {
        int modifiers = method.getModifiers();
        return !isStatic(modifiers) && !isFinal(modifiers) && !isPrivate(modifiers);
    }

    /**
     * Checks if class can be extended
     * 检查类是否可被继承
     *
     * @param clazz the class | 类
     * @return true if can be extended | 如果可被继承返回true
     */
    public static boolean isExtendable(Class<?> clazz) {
        return !isFinal(clazz) && !clazz.isInterface() && !clazz.isArray() && !clazz.isPrimitive();
    }
}
