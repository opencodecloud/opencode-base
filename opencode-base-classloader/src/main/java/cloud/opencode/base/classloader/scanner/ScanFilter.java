package cloud.opencode.base.classloader.scanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Scan Filter - Filter interface for class scanning
 * 扫描过滤器 - 类扫描的过滤器接口
 *
 * <p>Functional interface for filtering classes during scanning.</p>
 * <p>用于在扫描期间过滤类的函数式接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Predefined filters - 预定义过滤器</li>
 *   <li>Combinable with and/or/not - 可使用 and/or/not 组合</li>
 *   <li>Custom filter support - 自定义过滤器支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ScanFilter filter = ScanFilter.and(
 *     ScanFilter.isConcrete(),
 *     ScanFilter.hasAnnotation(Service.class),
 *     ScanFilter.not(ScanFilter.nameEndsWith("Test"))
 * );
 * Set<Class<?>> classes = scanner.scan(filter);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@FunctionalInterface
public interface ScanFilter {

    /**
     * Test if class matches filter
     * 测试类是否匹配过滤器
     *
     * @param clazz class to test | 要测试的类
     * @return true if matches | 匹配返回 true
     */
    boolean test(Class<?> clazz);

    // ==================== Combination Methods | 组合方法 ====================

    /**
     * AND combination
     * AND 组合
     *
     * @param other other filter | 其他过滤器
     * @return combined filter | 组合后的过滤器
     */
    default ScanFilter and(ScanFilter other) {
        Objects.requireNonNull(other);
        return clazz -> this.test(clazz) && other.test(clazz);
    }

    /**
     * OR combination
     * OR 组合
     *
     * @param other other filter | 其他过滤器
     * @return combined filter | 组合后的过滤器
     */
    default ScanFilter or(ScanFilter other) {
        Objects.requireNonNull(other);
        return clazz -> this.test(clazz) || other.test(clazz);
    }

    /**
     * Negate filter
     * 取反过滤器
     *
     * @return negated filter | 取反后的过滤器
     */
    default ScanFilter negate() {
        return clazz -> !this.test(clazz);
    }

    // ==================== Static Combination Methods | 静态组合方法 ====================

    /**
     * AND combination of multiple filters
     * 多个过滤器的 AND 组合
     *
     * @param filters filters to combine | 要组合的过滤器
     * @return combined filter | 组合后的过滤器
     */
    static ScanFilter and(ScanFilter... filters) {
        return clazz -> Arrays.stream(filters).allMatch(f -> f.test(clazz));
    }

    /**
     * OR combination of multiple filters
     * 多个过滤器的 OR 组合
     *
     * @param filters filters to combine | 要组合的过滤器
     * @return combined filter | 组合后的过滤器
     */
    static ScanFilter or(ScanFilter... filters) {
        return clazz -> Arrays.stream(filters).anyMatch(f -> f.test(clazz));
    }

    /**
     * Negate a filter
     * 对过滤器取反
     *
     * @param filter filter to negate | 要取反的过滤器
     * @return negated filter | 取反后的过滤器
     */
    static ScanFilter not(ScanFilter filter) {
        return filter.negate();
    }

    // ==================== Type Filters | 类型过滤器 ====================

    /**
     * Filter for concrete classes (not abstract, not interface)
     * 过滤具体类（非抽象、非接口）
     *
     * @return filter | 过滤器
     */
    static ScanFilter isConcrete() {
        return clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Filter for interfaces
     * 过滤接口
     *
     * @return filter | 过滤器
     */
    static ScanFilter isInterface() {
        return Class::isInterface;
    }

    /**
     * Filter for abstract classes
     * 过滤抽象类
     *
     * @return filter | 过滤器
     */
    static ScanFilter isAbstract() {
        return clazz -> Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface();
    }

    /**
     * Filter for enums
     * 过滤枚举
     *
     * @return filter | 过滤器
     */
    static ScanFilter isEnum() {
        return Class::isEnum;
    }

    /**
     * Filter for records
     * 过滤 Record
     *
     * @return filter | 过滤器
     */
    static ScanFilter isRecord() {
        return Class::isRecord;
    }

    /**
     * Filter for annotations
     * 过滤注解
     *
     * @return filter | 过滤器
     */
    static ScanFilter isAnnotation() {
        return Class::isAnnotation;
    }

    // ==================== Annotation Filters | 注解过滤器 ====================

    /**
     * Filter for classes with specified annotation
     * 过滤带指定注解的类
     *
     * @param annotation annotation class | 注解类
     * @return filter | 过滤器
     */
    static ScanFilter hasAnnotation(Class<? extends Annotation> annotation) {
        Objects.requireNonNull(annotation, "Annotation must not be null");
        return clazz -> clazz.isAnnotationPresent(annotation);
    }

    /**
     * Filter for classes with any of specified annotations
     * 过滤带任意指定注解的类
     *
     * @param annotations annotation classes | 注解类数组
     * @return filter | 过滤器
     */
    @SafeVarargs
    static ScanFilter hasAnyAnnotation(Class<? extends Annotation>... annotations) {
        return clazz -> Arrays.stream(annotations).anyMatch(clazz::isAnnotationPresent);
    }

    /**
     * Filter for classes with all specified annotations
     * 过滤带所有指定注解的类
     *
     * @param annotations annotation classes | 注解类数组
     * @return filter | 过滤器
     */
    @SafeVarargs
    static ScanFilter hasAllAnnotations(Class<? extends Annotation>... annotations) {
        return clazz -> Arrays.stream(annotations).allMatch(clazz::isAnnotationPresent);
    }

    // ==================== Inheritance Filters | 继承过滤器 ====================

    /**
     * Filter for subtypes of specified class
     * 过滤指定类的子类型
     *
     * @param superType super type | 父类型
     * @return filter | 过滤器
     */
    static ScanFilter isSubTypeOf(Class<?> superType) {
        Objects.requireNonNull(superType, "Super type must not be null");
        return clazz -> superType.isAssignableFrom(clazz) && !superType.equals(clazz);
    }

    /**
     * Filter for classes implementing specified interface
     * 过滤实现指定接口的类
     *
     * @param interfaceType interface type | 接口类型
     * @return filter | 过滤器
     */
    static ScanFilter implementsInterface(Class<?> interfaceType) {
        Objects.requireNonNull(interfaceType, "Interface type must not be null");
        if (!interfaceType.isInterface()) {
            throw new IllegalArgumentException("Not an interface: " + interfaceType);
        }
        return clazz -> interfaceType.isAssignableFrom(clazz) && !clazz.isInterface();
    }

    // ==================== Name Filters | 名称过滤器 ====================

    /**
     * Filter by name prefix
     * 按名称前缀过滤
     *
     * @param prefix name prefix | 名称前缀
     * @return filter | 过滤器
     */
    static ScanFilter nameStartsWith(String prefix) {
        Objects.requireNonNull(prefix, "Prefix must not be null");
        return clazz -> clazz.getName().startsWith(prefix);
    }

    /**
     * Filter by name suffix
     * 按名称后缀过滤
     *
     * @param suffix name suffix | 名称后缀
     * @return filter | 过滤器
     */
    static ScanFilter nameEndsWith(String suffix) {
        Objects.requireNonNull(suffix, "Suffix must not be null");
        return clazz -> clazz.getName().endsWith(suffix);
    }

    /**
     * Filter by simple name suffix
     * 按简单名称后缀过滤
     *
     * @param suffix simple name suffix | 简单名称后缀
     * @return filter | 过滤器
     */
    static ScanFilter simpleNameEndsWith(String suffix) {
        Objects.requireNonNull(suffix, "Suffix must not be null");
        return clazz -> clazz.getSimpleName().endsWith(suffix);
    }

    /**
     * Filter by name regex pattern
     * 按名称正则模式过滤
     *
     * @param regex regex pattern | 正则模式
     * @return filter | 过滤器
     */
    static ScanFilter nameMatches(String regex) {
        Objects.requireNonNull(regex, "Regex must not be null");
        Pattern pattern = Pattern.compile(regex);
        return clazz -> pattern.matcher(clazz.getName()).matches();
    }

    /**
     * Filter for classes in specified package
     * 过滤指定包中的类
     *
     * @param packageName package name | 包名
     * @return filter | 过滤器
     */
    static ScanFilter inPackage(String packageName) {
        Objects.requireNonNull(packageName, "Package name must not be null");
        return clazz -> {
            Package pkg = clazz.getPackage();
            return pkg != null && pkg.getName().startsWith(packageName);
        };
    }

    // ==================== Modifier Filters | 修饰符过滤器 ====================

    /**
     * Filter by modifier
     * 按修饰符过滤
     *
     * @param modifier modifier flag | 修饰符标志
     * @return filter | 过滤器
     */
    static ScanFilter hasModifier(int modifier) {
        return clazz -> (clazz.getModifiers() & modifier) != 0;
    }

    /**
     * Filter for public classes
     * 过滤 public 类
     *
     * @return filter | 过滤器
     */
    static ScanFilter isPublic() {
        return clazz -> Modifier.isPublic(clazz.getModifiers());
    }

    /**
     * Filter for final classes
     * 过滤 final 类
     *
     * @return filter | 过滤器
     */
    static ScanFilter isFinal() {
        return clazz -> Modifier.isFinal(clazz.getModifiers());
    }

    // ==================== Special Filters | 特殊过滤器 ====================

    /**
     * Filter that accepts all classes
     * 接受所有类的过滤器
     *
     * @return filter | 过滤器
     */
    static ScanFilter all() {
        return clazz -> true;
    }

    /**
     * Filter that rejects all classes
     * 拒绝所有类的过滤器
     *
     * @return filter | 过滤器
     */
    static ScanFilter none() {
        return clazz -> false;
    }

    /**
     * Filter for non-inner classes
     * 过滤非内部类
     *
     * @return filter | 过滤器
     */
    static ScanFilter isTopLevel() {
        return clazz -> !clazz.getName().contains("$");
    }

    /**
     * Filter for inner classes
     * 过滤内部类
     *
     * @return filter | 过滤器
     */
    static ScanFilter isInnerClass() {
        return clazz -> clazz.getName().contains("$");
    }
}
