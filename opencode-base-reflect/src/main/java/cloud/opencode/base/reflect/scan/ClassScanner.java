package cloud.opencode.base.reflect.scan;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Class Scanner
 * 类扫描器
 *
 * <p>Scans for classes matching various criteria.</p>
 * <p>扫描匹配各种条件的类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Package scanning - 包扫描</li>
 *   <li>Annotation filtering - 注解过滤</li>
 *   <li>Subtype filtering - 子类型过滤</li>
 *   <li>Custom predicate filtering - 自定义谓词过滤</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<Class<?>> classes = ClassScanner.from(classPath)
 *     .inPackage("com.example")
 *     .recursive()
 *     .withAnnotation(Component.class)
 *     .scan();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable builder state) - 线程安全: 否（可变构建器状态）</li>
 *   <li>Null-safe: No (class path must be non-null) - 空值安全: 否（类路径须非空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class ClassScanner {

    private final ClassPath classPath;
    private final Set<String> basePackages;
    private final List<Predicate<ClassInfo>> classInfoFilters;
    private final List<Predicate<Class<?>>> classFilters;
    private boolean recursive;
    private boolean includeInnerClasses;

    private ClassScanner(ClassPath classPath) {
        this.classPath = classPath;
        this.basePackages = new LinkedHashSet<>();
        this.classInfoFilters = new ArrayList<>();
        this.classFilters = new ArrayList<>();
        this.recursive = true;
        this.includeInnerClasses = false;
    }

    /**
     * Creates a scanner from ClassPath
     * 从ClassPath创建扫描器
     *
     * @param classPath the ClassPath | ClassPath
     * @return the scanner | 扫描器
     */
    public static ClassScanner from(ClassPath classPath) {
        return new ClassScanner(classPath);
    }

    /**
     * Creates a scanner from ClassLoader
     * 从ClassLoader创建扫描器
     *
     * @param classLoader the class loader | 类加载器
     * @return the scanner | 扫描器
     */
    public static ClassScanner from(ClassLoader classLoader) {
        return new ClassScanner(ClassPath.from(classLoader));
    }

    /**
     * Creates a scanner using current context ClassLoader
     * 使用当前上下文ClassLoader创建扫描器
     *
     * @return the scanner | 扫描器
     */
    public static ClassScanner create() {
        return from(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Adds a base package to scan
     * 添加要扫描的基础包
     *
     * @param packageName the package name | 包名
     * @return this scanner | 此扫描器
     */
    public ClassScanner inPackage(String packageName) {
        basePackages.add(packageName);
        return this;
    }

    /**
     * Adds multiple base packages to scan
     * 添加多个要扫描的基础包
     *
     * @param packageNames the package names | 包名
     * @return this scanner | 此扫描器
     */
    public ClassScanner inPackages(String... packageNames) {
        Collections.addAll(basePackages, packageNames);
        return this;
    }

    /**
     * Sets whether to scan recursively
     * 设置是否递归扫描
     *
     * @param recursive whether to scan recursively | 是否递归扫描
     * @return this scanner | 此扫描器
     */
    public ClassScanner recursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    /**
     * Sets whether to include inner classes
     * 设置是否包含内部类
     *
     * @param include whether to include | 是否包含
     * @return this scanner | 此扫描器
     */
    public ClassScanner includeInnerClasses(boolean include) {
        this.includeInnerClasses = include;
        return this;
    }

    /**
     * Filters by ClassInfo predicate (before loading)
     * 按ClassInfo谓词过滤（加载前）
     *
     * @param predicate the predicate | 谓词
     * @return this scanner | 此扫描器
     */
    public ClassScanner filterInfo(Predicate<ClassInfo> predicate) {
        classInfoFilters.add(predicate);
        return this;
    }

    /**
     * Filters by Class predicate (after loading)
     * 按Class谓词过滤（加载后）
     *
     * @param predicate the predicate | 谓词
     * @return this scanner | 此扫描器
     */
    public ClassScanner filter(Predicate<Class<?>> predicate) {
        classFilters.add(predicate);
        return this;
    }

    /**
     * Filters for classes with annotation
     * 过滤具有注解的类
     *
     * @param annotationClass the annotation class | 注解类
     * @return this scanner | 此扫描器
     */
    public ClassScanner withAnnotation(Class<? extends Annotation> annotationClass) {
        return filter(c -> c.isAnnotationPresent(annotationClass));
    }

    /**
     * Filters for subtypes of a class
     * 过滤某类的子类型
     *
     * @param superClass the super class | 父类
     * @return this scanner | 此扫描器
     */
    public ClassScanner subtypeOf(Class<?> superClass) {
        return filter(c -> superClass.isAssignableFrom(c) && !c.equals(superClass));
    }

    /**
     * Filters for implementations of an interface
     * 过滤接口的实现类
     *
     * @param interfaceClass the interface class | 接口类
     * @return this scanner | 此扫描器
     */
    public ClassScanner implementing(Class<?> interfaceClass) {
        return filter(c -> interfaceClass.isAssignableFrom(c)
                && !c.isInterface()
                && !c.equals(interfaceClass));
    }

    /**
     * Filters for interfaces only
     * 仅过滤接口
     *
     * @return this scanner | 此扫描器
     */
    public ClassScanner interfacesOnly() {
        return filter(Class::isInterface);
    }

    /**
     * Filters for concrete classes only
     * 仅过滤具体类
     *
     * @return this scanner | 此扫描器
     */
    public ClassScanner concreteOnly() {
        return filter(c -> !c.isInterface()
                && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()));
    }

    /**
     * Filters for record classes only
     * 仅过滤Record类
     *
     * @return this scanner | 此扫描器
     */
    public ClassScanner recordsOnly() {
        return filter(Class::isRecord);
    }

    /**
     * Filters for enum classes only
     * 仅过滤枚举类
     *
     * @return this scanner | 此扫描器
     */
    public ClassScanner enumsOnly() {
        return filter(Class::isEnum);
    }

    /**
     * Scans and returns ClassInfo without loading classes
     * 扫描并返回ClassInfo（不加载类）
     *
     * @return set of ClassInfo | ClassInfo集合
     */
    public Set<ClassInfo> scanInfo() {
        Set<ClassInfo> result = new LinkedHashSet<>();

        Set<ClassInfo> candidates = getCandidateClasses();

        for (ClassInfo classInfo : candidates) {
            if (matchesClassInfoFilters(classInfo)) {
                result.add(classInfo);
            }
        }

        return result;
    }

    /**
     * Scans and returns loaded classes
     * 扫描并返回已加载的类
     *
     * @return set of classes | 类集合
     */
    public Set<Class<?>> scan() {
        Set<Class<?>> result = new LinkedHashSet<>();

        for (ClassInfo classInfo : scanInfo()) {
            try {
                Class<?> clazz = classInfo.load();
                if (matchesClassFilters(clazz)) {
                    result.add(clazz);
                }
            } catch (Exception ignored) {
                // Skip classes that cannot be loaded
            }
        }

        return result;
    }

    /**
     * Scans and returns as stream
     * 扫描并返回为流
     *
     * @return stream of classes | 类流
     */
    public Stream<Class<?>> stream() {
        return scan().stream();
    }

    /**
     * Scans and returns as list
     * 扫描并返回为列表
     *
     * @return list of classes | 类列表
     */
    public List<Class<?>> toList() {
        return new ArrayList<>(scan());
    }

    private Set<ClassInfo> getCandidateClasses() {
        if (basePackages.isEmpty()) {
            if (includeInnerClasses) {
                return classPath.getAllClasses();
            } else {
                Set<ClassInfo> result = new LinkedHashSet<>();
                for (ClassInfo classInfo : classPath.getAllClasses()) {
                    if (!classInfo.isInnerClass()) {
                        result.add(classInfo);
                    }
                }
                return result;
            }
        }

        Set<ClassInfo> result = new LinkedHashSet<>();
        for (String packageName : basePackages) {
            Set<ClassInfo> classes;
            if (recursive) {
                classes = includeInnerClasses
                        ? classPath.getClassesRecursively(packageName)
                        : classPath.getTopLevelClassesRecursively(packageName);
            } else {
                classes = includeInnerClasses
                        ? classPath.getClassesInPackage(packageName)
                        : classPath.getTopLevelClassesInPackage(packageName);
            }
            result.addAll(classes);
        }
        return result;
    }

    private boolean matchesClassInfoFilters(ClassInfo classInfo) {
        for (Predicate<ClassInfo> filter : classInfoFilters) {
            if (!filter.test(classInfo)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesClassFilters(Class<?> clazz) {
        for (Predicate<Class<?>> filter : classFilters) {
            if (!filter.test(clazz)) {
                return false;
            }
        }
        return true;
    }
}
