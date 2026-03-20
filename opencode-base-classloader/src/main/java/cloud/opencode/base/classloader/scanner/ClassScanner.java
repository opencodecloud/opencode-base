package cloud.opencode.base.classloader.scanner;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Class Scanner - Scans classes in specified packages
 * 类扫描器 - 扫描指定包下的类
 *
 * <p>Scans and finds classes matching specified criteria.</p>
 * <p>扫描并查找匹配指定条件的类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Package scanning - 包扫描</li>
 *   <li>Annotation scanning - 注解扫描</li>
 *   <li>Subtype scanning - 子类型扫描</li>
 *   <li>Custom filter support - 自定义过滤器支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ClassScanner scanner = ClassScanner.of("com.example");
 * Set<Class<?>> services = scanner.scanWithAnnotation(Service.class);
 * Set<Class<?>> all = scanner.scan();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public class ClassScanner {

    private static final System.Logger LOGGER = System.getLogger(ClassScanner.class.getName());

    private final Set<String> basePackages;
    private ClassLoader classLoader;
    private boolean includeJars = true;
    private boolean includeInnerClasses = false;
    private boolean parallel = false;
    private final Set<String> excludedPackages = new HashSet<>();

    private ClassScanner(Set<String> basePackages) {
        this.basePackages = new HashSet<>(basePackages);
        this.classLoader = Thread.currentThread().getContextClassLoader();
        if (this.classLoader == null) {
            this.classLoader = ClassScanner.class.getClassLoader();
        }
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create scanner for single package
     * 为单个包创建扫描器
     *
     * @param basePackage base package | 基础包
     * @return scanner | 扫描器
     */
    public static ClassScanner of(String basePackage) {
        Objects.requireNonNull(basePackage, "Base package must not be null");
        return new ClassScanner(Set.of(basePackage));
    }

    /**
     * Create scanner for multiple packages
     * 为多个包创建扫描器
     *
     * @param basePackages base packages | 基础包数组
     * @return scanner | 扫描器
     */
    public static ClassScanner of(String... basePackages) {
        Objects.requireNonNull(basePackages, "Base packages must not be null");
        return new ClassScanner(Set.of(basePackages));
    }

    /**
     * Create scanner with classloader
     * 使用类加载器创建扫描器
     *
     * @param classLoader class loader | 类加载器
     * @param basePackage base package | 基础包
     * @return scanner | 扫描器
     */
    public static ClassScanner of(ClassLoader classLoader, String basePackage) {
        ClassScanner scanner = of(basePackage);
        scanner.classLoader = classLoader;
        return scanner;
    }

    // ==================== Configuration | 配置 ====================

    /**
     * Set whether to include JAR files
     * 设置是否包含 JAR 文件
     *
     * @param include include JARs | 包含 JAR
     * @return this scanner | 此扫描器
     */
    public ClassScanner includeJars(boolean include) {
        this.includeJars = include;
        return this;
    }

    /**
     * Set whether to include inner classes
     * 设置是否包含内部类
     *
     * @param include include inner classes | 包含内部类
     * @return this scanner | 此扫描器
     */
    public ClassScanner includeInnerClasses(boolean include) {
        this.includeInnerClasses = include;
        return this;
    }

    /**
     * Set parallel scanning
     * 设置并行扫描
     *
     * @param parallel enable parallel | 启用并行
     * @return this scanner | 此扫描器
     */
    public ClassScanner parallel(boolean parallel) {
        this.parallel = parallel;
        return this;
    }

    /**
     * Add excluded package
     * 添加排除包
     *
     * @param packageName package name | 包名
     * @return this scanner | 此扫描器
     */
    public ClassScanner excludePackage(String packageName) {
        this.excludedPackages.add(packageName);
        return this;
    }

    /**
     * Set class loader
     * 设置类加载器
     *
     * @param classLoader class loader | 类加载器
     * @return this scanner | 此扫描器
     */
    public ClassScanner classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        return this;
    }

    // ==================== Scan Methods | 扫描方法 ====================

    /**
     * Scan all classes
     * 扫描所有类
     *
     * @return set of classes | 类集合
     */
    public Set<Class<?>> scan() {
        return scan(ScanFilter.all());
    }

    /**
     * Scan with filter
     * 使用过滤器扫描
     *
     * @param filter scan filter | 扫描过滤器
     * @return set of matching classes | 匹配的类集合
     */
    public Set<Class<?>> scan(ScanFilter filter) {
        Objects.requireNonNull(filter, "Filter must not be null");
        Set<Class<?>> result = new HashSet<>();

        for (String basePackage : basePackages) {
            result.addAll(scanPackage(basePackage, filter));
        }

        return result;
    }

    /**
     * Scan for classes with annotation
     * 扫描带注解的类
     *
     * @param annotation annotation class | 注解类
     * @return set of annotated classes | 带注解的类集合
     */
    public Set<Class<?>> scanWithAnnotation(Class<? extends Annotation> annotation) {
        return scan(ScanFilter.hasAnnotation(annotation));
    }

    /**
     * Scan for subtypes
     * 扫描子类型
     *
     * @param superType super type | 父类型
     * @param <T>       type parameter | 类型参数
     * @return set of subtypes | 子类型集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<Class<? extends T>> scanSubTypes(Class<T> superType) {
        Set<Class<?>> classes = scan(ScanFilter.isSubTypeOf(superType));
        Set<Class<? extends T>> result = new HashSet<>();
        for (Class<?> clazz : classes) {
            result.add((Class<? extends T>) clazz);
        }
        return result;
    }

    /**
     * Scan for interface implementations
     * 扫描接口实现
     *
     * @param interfaceType interface type | 接口类型
     * @param <T>           type parameter | 类型参数
     * @return set of implementations | 实现类集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<Class<? extends T>> scanImplementations(Class<T> interfaceType) {
        Set<Class<?>> classes = scan(ScanFilter.and(
                ScanFilter.implementsInterface(interfaceType),
                ScanFilter.isConcrete()
        ));
        Set<Class<? extends T>> result = new HashSet<>();
        for (Class<?> clazz : classes) {
            result.add((Class<? extends T>) clazz);
        }
        return result;
    }

    // ==================== Stream API | 流式 API ====================

    /**
     * Return class stream (lazy loading)
     * 返回类流（懒加载）
     *
     * @return stream of classes | 类流
     */
    public Stream<Class<?>> stream() {
        return classNameStream()
                .map(this::loadClass)
                .filter(Objects::nonNull);
    }

    /**
     * Return class name stream (without loading)
     * 返回类名流（不加载类）
     *
     * @return stream of class names | 类名流
     */
    public Stream<String> classNameStream() {
        List<String> classNames = new ArrayList<>();
        for (String basePackage : basePackages) {
            classNames.addAll(findClassNames(basePackage));
        }
        Stream<String> stream = classNames.stream();
        if (parallel) {
            stream = stream.parallel();
        }
        return stream;
    }

    // ==================== Private Methods | 私有方法 ====================

    private Set<Class<?>> scanPackage(String basePackage, ScanFilter filter) {
        List<String> classNames = findClassNames(basePackage);

        if (parallel) {
            Set<Class<?>> result = ConcurrentHashMap.newKeySet();
            classNames.parallelStream()
                    .map(this::loadClass)
                    .filter(Objects::nonNull)
                    .filter(filter::test)
                    .forEach(result::add);
            return result;
        } else {
            Set<Class<?>> result = new HashSet<>();
            classNames.stream()
                    .map(this::loadClass)
                    .filter(Objects::nonNull)
                    .filter(filter::test)
                    .forEach(result::add);
            return result;
        }
    }

    private List<String> findClassNames(String basePackage) {
        List<String> classNames = new ArrayList<>();
        String packagePath = basePackage.replace('.', '/');

        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    classNames.addAll(findClassesInDirectory(new File(resource.toURI()), basePackage));
                } else if ("jar".equals(protocol) && includeJars) {
                    classNames.addAll(findClassesInJar(resource, packagePath));
                }
            }
        } catch (Exception e) {
            throw OpenClassLoaderException.scanFailed(basePackage, e);
        }

        return classNames;
    }

    private List<String> findClassesInDirectory(File directory, String packageName) {
        List<String> classNames = new ArrayList<>();
        if (!directory.exists()) {
            return classNames;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classNames;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classNames.addAll(findClassesInDirectory(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                if (shouldIncludeClass(className)) {
                    classNames.add(className);
                }
            }
        }

        return classNames;
    }

    private List<String> findClassesInJar(URL jarUrl, String packagePath) {
        List<String> classNames = new ArrayList<>();

        try {
            JarURLConnection connection = (JarURLConnection) jarUrl.openConnection();
            try (JarFile jarFile = connection.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                        String className = entryName.replace('/', '.').replace(".class", "");
                        if (shouldIncludeClass(className)) {
                            classNames.add(className);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.DEBUG, "Failed to scan JAR for classes: " + jarUrl, e);
        }

        return classNames;
    }

    private boolean shouldIncludeClass(String className) {
        // Check inner class
        if (!includeInnerClasses && className.contains("$")) {
            return false;
        }

        // Check excluded packages
        for (String excluded : excludedPackages) {
            if (className.startsWith(excluded)) {
                return false;
            }
        }

        return true;
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }
}
