package cloud.opencode.base.classloader.scanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
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

    /**
     * Pre-filter by class name before loading the class (performance optimization)
     * 在加载类之前按类名预过滤（性能优化）
     *
     * <p>If this method returns false, the class will not be loaded via Class.forName,
     * saving expensive class loading operations. Default returns true (no pre-filtering).</p>
     * <p>如果此方法返回 false，则不会通过 Class.forName 加载该类，
     * 从而节省昂贵的类加载操作。默认返回 true（不预过滤）。</p>
     *
     * @param className fully qualified class name | 全限定类名
     * @return true if class should proceed to loading | 需要继续加载返回 true
     */
    default boolean preTest(String className) {
        return true;
    }

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
        ScanFilter self = this;
        return new ScanFilter() {
            @Override public boolean test(Class<?> clazz) { return self.test(clazz) && other.test(clazz); }
            @Override public boolean preTest(String className) { return self.preTest(className) && other.preTest(className); }
        };
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
        ScanFilter self = this;
        return new ScanFilter() {
            @Override public boolean test(Class<?> clazz) { return self.test(clazz) || other.test(clazz); }
            @Override public boolean preTest(String className) { return self.preTest(className) || other.preTest(className); }
        };
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
        ScanFilter[] copy = filters.clone();
        return new ScanFilter() {
            @Override public boolean test(Class<?> clazz) {
                for (ScanFilter f : copy) {
                    if (!f.test(clazz)) return false;
                }
                return true;
            }
            @Override public boolean preTest(String className) {
                for (ScanFilter f : copy) {
                    if (!f.preTest(className)) return false;
                }
                return true;
            }
        };
    }

    /**
     * OR combination of multiple filters
     * 多个过滤器的 OR 组合
     *
     * @param filters filters to combine | 要组合的过滤器
     * @return combined filter | 组合后的过滤器
     */
    static ScanFilter or(ScanFilter... filters) {
        ScanFilter[] copy = filters.clone();
        return new ScanFilter() {
            @Override public boolean test(Class<?> clazz) {
                for (ScanFilter f : copy) {
                    if (f.test(clazz)) return true;
                }
                return false;
            }
            @Override public boolean preTest(String className) {
                // For OR, if ANY sub-filter's preTest passes, we must load the class
                for (ScanFilter f : copy) {
                    if (f.preTest(className)) return true;
                }
                return false;
            }
        };
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
        Class<? extends Annotation>[] copy = annotations.clone();
        return clazz -> {
            for (Class<? extends Annotation> a : copy) {
                if (clazz.isAnnotationPresent(a)) return true;
            }
            return false;
        };
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
        Class<? extends Annotation>[] copy = annotations.clone();
        return clazz -> {
            for (Class<? extends Annotation> a : copy) {
                if (!clazz.isAnnotationPresent(a)) return false;
            }
            return true;
        };
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
        return new ScanFilter() {
            @Override public boolean test(Class<?> clazz) { return clazz.getName().startsWith(prefix); }
            @Override public boolean preTest(String className) { return className.startsWith(prefix); }
        };
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
        return new ScanFilter() {
            @Override public boolean test(Class<?> clazz) { return clazz.getName().endsWith(suffix); }
            @Override public boolean preTest(String className) { return className.endsWith(suffix); }
        };
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
        return new ScanFilter() {
            @Override public boolean test(Class<?> clazz) { return pattern.matcher(clazz.getName()).matches(); }
            @Override public boolean preTest(String className) { return pattern.matcher(className).matches(); }
        };
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
        return new ScanFilter() {
            @Override public boolean test(Class<?> clazz) {
                Package pkg = clazz.getPackage();
                return pkg != null && pkg.getName().startsWith(packageName);
            }
            @Override public boolean preTest(String className) {
                return className.startsWith(packageName);
            }
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

    // ==================== Sealed Filters | 密封类过滤器 ====================

    /**
     * Filter for sealed classes
     * 过滤密封类
     *
     * @return filter | 过滤器
     */
    static ScanFilter isSealed() {
        return clazz -> clazz.isSealed();
    }

    /**
     * Filter for sealed classes that have permitted subclasses
     * 过滤有许可子类的密封类
     *
     * @return filter | 过滤器
     */
    static ScanFilter hasPermittedSubclass() {
        return clazz -> clazz.isSealed() && clazz.getPermittedSubclasses() != null
                && clazz.getPermittedSubclasses().length > 0;
    }

    // ==================== Member Filters | 成员过滤器 ====================

    /**
     * Filter for classes that have a constructor with the specified parameter count
     * 过滤具有指定参数数量构造器的类
     *
     * @param count parameter count | 参数数量
     * @return filter | 过滤器
     */
    static ScanFilter hasConstructorWithParameterCount(int count) {
        return clazz -> {
            for (var constructor : clazz.getDeclaredConstructors()) {
                if (constructor.getParameterCount() == count) return true;
            }
            return false;
        };
    }

    /**
     * Filter for classes that have a method with the specified return type
     * 过滤具有指定返回类型方法的类
     *
     * @param returnType return type | 返回类型
     * @return filter | 过滤器
     */
    static ScanFilter hasMethodWithReturnType(Class<?> returnType) {
        Objects.requireNonNull(returnType, "Return type must not be null");
        return clazz -> {
            for (var method : clazz.getDeclaredMethods()) {
                if (method.getReturnType() == returnType) return true;
            }
            return false;
        };
    }

    /**
     * Filter for classes that have a method with the specified parameter count
     * 过滤具有指定参数数量方法的类
     *
     * @param count parameter count | 参数数量
     * @return filter | 过滤器
     */
    static ScanFilter hasMethodWithParameterCount(int count) {
        return clazz -> {
            for (var method : clazz.getDeclaredMethods()) {
                if (method.getParameterCount() == count) return true;
            }
            return false;
        };
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
        return new ScanFilter() {
            @Override public boolean test(Class<?> clazz) { return !clazz.getName().contains("$"); }
            @Override public boolean preTest(String className) { return !className.contains("$"); }
        };
    }

    /**
     * Filter for inner classes
     * 过滤内部类
     *
     * @return filter | 过滤器
     */
    static ScanFilter isInnerClass() {
        return new ScanFilter() {
            @Override public boolean test(Class<?> clazz) { return clazz.getName().contains("$"); }
            @Override public boolean preTest(String className) { return className.contains("$"); }
        };
    }
}
