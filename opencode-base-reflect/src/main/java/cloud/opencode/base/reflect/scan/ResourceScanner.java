package cloud.opencode.base.reflect.scan;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Resource Scanner
 * 资源扫描器
 *
 * <p>Scans for resources in the classpath.</p>
 * <p>扫描类路径中的资源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extension filtering - 扩展名过滤</li>
 *   <li>Pattern matching - 模式匹配</li>
 *   <li>Package filtering - 包过滤</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<ResourceInfo> xmlFiles = ResourceScanner.from(classPath)
 *     .withExtension("xml")
 *     .inPackage("com.example")
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
public final class ResourceScanner {

    private final ClassPath classPath;
    private final List<Predicate<ResourceInfo>> filters;
    private final Set<String> extensions;
    private final Set<String> packages;
    private boolean excludeClasses;

    private ResourceScanner(ClassPath classPath) {
        this.classPath = classPath;
        this.filters = new ArrayList<>();
        this.extensions = new LinkedHashSet<>();
        this.packages = new LinkedHashSet<>();
        this.excludeClasses = true;
    }

    /**
     * Creates a ResourceScanner from ClassPath
     * 从ClassPath创建ResourceScanner
     *
     * @param classPath the ClassPath | ClassPath
     * @return the scanner | 扫描器
     */
    public static ResourceScanner from(ClassPath classPath) {
        return new ResourceScanner(classPath);
    }

    /**
     * Creates a ResourceScanner from ClassLoader
     * 从ClassLoader创建ResourceScanner
     *
     * @param classLoader the class loader | 类加载器
     * @return the scanner | 扫描器
     */
    public static ResourceScanner from(ClassLoader classLoader) {
        return new ResourceScanner(ClassPath.from(classLoader));
    }

    /**
     * Creates a ResourceScanner using context ClassLoader
     * 使用上下文ClassLoader创建ResourceScanner
     *
     * @return the scanner | 扫描器
     */
    public static ResourceScanner create() {
        return from(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Sets whether to exclude class files
     * 设置是否排除类文件
     *
     * @param exclude whether to exclude | 是否排除
     * @return this scanner | 此扫描器
     */
    public ResourceScanner excludeClasses(boolean exclude) {
        this.excludeClasses = exclude;
        return this;
    }

    /**
     * Includes class files in results
     * 在结果中包含类文件
     *
     * @return this scanner | 此扫描器
     */
    public ResourceScanner includeClasses() {
        return excludeClasses(false);
    }

    /**
     * Filters by extension
     * 按扩展名过滤
     *
     * @param extension the extension (without dot) | 扩展名（不含点）
     * @return this scanner | 此扫描器
     */
    public ResourceScanner withExtension(String extension) {
        extensions.add(extension.toLowerCase());
        return this;
    }

    /**
     * Filters by multiple extensions
     * 按多个扩展名过滤
     *
     * @param extensions the extensions | 扩展名
     * @return this scanner | 此扫描器
     */
    public ResourceScanner withExtensions(String... extensions) {
        for (String ext : extensions) {
            this.extensions.add(ext.toLowerCase());
        }
        return this;
    }

    /**
     * Filters by package
     * 按包过滤
     *
     * @param packageName the package name | 包名
     * @return this scanner | 此扫描器
     */
    public ResourceScanner inPackage(String packageName) {
        packages.add(packageName);
        return this;
    }

    /**
     * Filters by multiple packages
     * 按多个包过滤
     *
     * @param packageNames the package names | 包名
     * @return this scanner | 此扫描器
     */
    public ResourceScanner inPackages(String... packageNames) {
        Collections.addAll(packages, packageNames);
        return this;
    }

    /**
     * Filters by name pattern (regex)
     * 按名称模式过滤（正则表达式）
     *
     * @param pattern the regex pattern | 正则表达式模式
     * @return this scanner | 此扫描器
     */
    public ResourceScanner matching(String pattern) {
        Pattern p = Pattern.compile(pattern);
        return filter(r -> p.matcher(r.getResourceName()).matches());
    }

    /**
     * Filters by name containing
     * 按名称包含过滤
     *
     * @param substring the substring | 子字符串
     * @return this scanner | 此扫描器
     */
    public ResourceScanner nameContains(String substring) {
        return filter(r -> r.getResourceName().contains(substring));
    }

    /**
     * Filters by name prefix
     * 按名称前缀过滤
     *
     * @param prefix the prefix | 前缀
     * @return this scanner | 此扫描器
     */
    public ResourceScanner nameStartsWith(String prefix) {
        return filter(r -> r.getSimpleName().startsWith(prefix));
    }

    /**
     * Filters by name suffix
     * 按名称后缀过滤
     *
     * @param suffix the suffix | 后缀
     * @return this scanner | 此扫描器
     */
    public ResourceScanner nameEndsWith(String suffix) {
        return filter(r -> r.getSimpleName().endsWith(suffix));
    }

    /**
     * Adds a custom filter
     * 添加自定义过滤器
     *
     * @param predicate the predicate | 谓词
     * @return this scanner | 此扫描器
     */
    public ResourceScanner filter(Predicate<ResourceInfo> predicate) {
        filters.add(predicate);
        return this;
    }

    /**
     * Scans and returns matching resources
     * 扫描并返回匹配的资源
     *
     * @return set of ResourceInfo | ResourceInfo集合
     */
    public Set<ResourceInfo> scan() {
        Set<ResourceInfo> result = new LinkedHashSet<>();

        for (ResourceInfo resource : classPath.getResources()) {
            if (matches(resource)) {
                result.add(resource);
            }
        }

        return result;
    }

    /**
     * Scans and returns as stream
     * 扫描并返回为流
     *
     * @return stream of ResourceInfo | ResourceInfo流
     */
    public Stream<ResourceInfo> stream() {
        return scan().stream();
    }

    /**
     * Scans and returns as list
     * 扫描并返回为列表
     *
     * @return list of ResourceInfo | ResourceInfo列表
     */
    public List<ResourceInfo> toList() {
        return new ArrayList<>(scan());
    }

    /**
     * Finds properties files
     * 查找属性文件
     *
     * @return set of ResourceInfo | ResourceInfo集合
     */
    public Set<ResourceInfo> findProperties() {
        return withExtension("properties").scan();
    }

    /**
     * Finds XML files
     * 查找XML文件
     *
     * @return set of ResourceInfo | ResourceInfo集合
     */
    public Set<ResourceInfo> findXml() {
        return withExtension("xml").scan();
    }

    /**
     * Finds JSON files
     * 查找JSON文件
     *
     * @return set of ResourceInfo | ResourceInfo集合
     */
    public Set<ResourceInfo> findJson() {
        return withExtension("json").scan();
    }

    /**
     * Finds YAML files
     * 查找YAML文件
     *
     * @return set of ResourceInfo | ResourceInfo集合
     */
    public Set<ResourceInfo> findYaml() {
        return withExtensions("yaml", "yml").scan();
    }

    private boolean matches(ResourceInfo resource) {
        String name = resource.getResourceName();

        // Exclude classes if configured
        if (excludeClasses && name.endsWith(".class")) {
            return false;
        }

        // Check extension filter
        if (!extensions.isEmpty()) {
            String ext = resource.getExtension().toLowerCase();
            if (!extensions.contains(ext)) {
                return false;
            }
        }

        // Check package filter
        if (!packages.isEmpty()) {
            String pkgPath = resource.getPackageName();
            boolean inPackage = false;
            for (String pkg : packages) {
                if (pkgPath.equals(pkg) || pkgPath.startsWith(pkg + ".")) {
                    inPackage = true;
                    break;
                }
            }
            if (!inPackage) {
                return false;
            }
        }

        // Apply custom filters
        for (Predicate<ResourceInfo> filter : filters) {
            if (!filter.test(resource)) {
                return false;
            }
        }

        return true;
    }
}
