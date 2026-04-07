package cloud.opencode.base.classloader.scanner;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import cloud.opencode.base.classloader.resource.NestedJarHandler;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
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
 *   <li>Disk cache support for scan results - 扫描结果磁盘缓存支持</li>
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
    private boolean includeNestedJars = false;
    private boolean parallel = false;
    private final Set<String> excludedPackages = new HashSet<>();
    private Path cacheDir;
    private String cacheKey;

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
     * Set whether to include nested JARs (e.g. BOOT-INF/lib/ in Spring Boot fat JARs)
     * 设置是否包含嵌套 JAR（如 Spring Boot fat JAR 中的 BOOT-INF/lib/）
     *
     * <p>When enabled, the scanner will discover and scan nested JAR files inside
     * fat JARs (BOOT-INF/lib/, WEB-INF/lib/, lib/) using {@link NestedJarHandler}.</p>
     * <p>启用后，扫描器将使用 {@link NestedJarHandler} 发现并扫描 fat JAR 内的嵌套 JAR 文件。</p>
     *
     * @param include include nested JARs | 包含嵌套 JAR
     * @return this scanner | 此扫描器
     */
    public ClassScanner includeNestedJars(boolean include) {
        this.includeNestedJars = include;
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

    /**
     * Set cache directory for scan result caching
     * 设置扫描结果缓存目录
     *
     * <p>When both cacheDir and cacheKey are configured, scan results will be
     * cached to disk and reused on subsequent scans if the classpath hasn't changed.</p>
     * <p>当 cacheDir 和 cacheKey 都已配置时，扫描结果将被缓存到磁盘，
     * 如果类路径未变化，后续扫描将复用缓存。</p>
     *
     * @param cacheDir cache directory path | 缓存目录路径
     * @return this scanner | 此扫描器
     */
    public ClassScanner cacheDir(Path cacheDir) {
        this.cacheDir = cacheDir;
        return this;
    }

    /**
     * Set cache key (e.g. application version)
     * 设置缓存键（如应用版本号）
     *
     * <p>The cache key is used together with basePackage to form the cache file name.</p>
     * <p>缓存键与 basePackage 一起构成缓存文件名。</p>
     *
     * @param cacheKey cache key | 缓存键
     * @return this scanner | 此扫描器
     */
    public ClassScanner cacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
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

        Collection<String> classNames = loadClassNamesWithCache();

        if (parallel) {
            Set<Class<?>> result = ConcurrentHashMap.newKeySet();
            classNames.parallelStream()
                    .filter(filter::preTest)
                    .map(this::loadClass)
                    .filter(Objects::nonNull)
                    .filter(filter::test)
                    .forEach(result::add);
            return result;
        } else {
            Set<Class<?>> result = new HashSet<>();
            classNames.stream()
                    .filter(filter::preTest)
                    .map(this::loadClass)
                    .filter(Objects::nonNull)
                    .filter(filter::test)
                    .forEach(result::add);
            return result;
        }
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
     * <p>If caching is configured, cached class names will be used when available and valid.</p>
     * <p>如果配置了缓存，将在缓存可用且有效时使用缓存的类名。</p>
     *
     * @return stream of class names | 类名流
     */
    public Stream<String> classNameStream() {
        Collection<String> classNames = loadClassNamesWithCache();
        Stream<String> stream = classNames.stream();
        if (parallel) {
            stream = stream.parallel();
        }
        return stream;
    }

    // ==================== Private Methods | 私有方法 ====================

    /**
     * Load class names, using cache if configured and valid
     * 加载类名，如果配置了缓存且有效则使用缓存
     */
    private Collection<String> loadClassNamesWithCache() {
        if (!isCacheEnabled()) {
            return findAllClassNames();
        }

        String currentHash = computeClasspathHash();
        // Sanitize both basePackageName and cacheKey to prevent path traversal
        String safeBasePackageName = String.join("+", new TreeSet<>(basePackages))
                .replaceAll("[^a-zA-Z0-9._+-]", "_");
        String safeCacheKey = cacheKey.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path cacheFile = cacheDir.resolve(safeBasePackageName + "-" + safeCacheKey + ".json").normalize();
        // Guard against path traversal after resolve
        if (!cacheFile.startsWith(cacheDir.normalize())) {
            throw new IllegalArgumentException("Cache path traversal detected: " + cacheFile);
        }

        // Try to load from cache
        if (Files.exists(cacheFile)) {
            try {
                String json = Files.readString(cacheFile, java.nio.charset.StandardCharsets.UTF_8);
                CachedScanResult cached = CachedScanResult.fromJson(json);
                if (currentHash.equals(cached.classpathHash())) {
                    LOGGER.log(System.Logger.Level.DEBUG,
                            "Using cached scan result from: " + cacheFile);
                    return cached.classNames();
                }
                LOGGER.log(System.Logger.Level.DEBUG,
                        "Cache invalidated (classpath changed): " + cacheFile);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.DEBUG,
                        "Failed to read cache file, will rescan: " + cacheFile, e);
            }
        }

        // Cache miss or invalid — perform full scan
        List<String> classNames = findAllClassNames();

        // Write cache
        try {
            Files.createDirectories(cacheDir);
            CachedScanResult result = new CachedScanResult(
                    currentHash,
                    new LinkedHashSet<>(classNames),
                    Instant.now().toString()
            );
            Files.writeString(cacheFile, result.toJson(), java.nio.charset.StandardCharsets.UTF_8);
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Wrote scan cache: " + cacheFile);
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Failed to write cache file: " + cacheFile, e);
        }

        return classNames;
    }

    /**
     * Check if caching is enabled
     * 检查是否启用了缓存
     */
    private boolean isCacheEnabled() {
        return cacheDir != null && cacheKey != null;
    }

    /**
     * Find all class names across all base packages.
     * Closes the nested JAR handler after scanning is complete.
     * 查找所有基础包中的所有类名。扫描完成后关闭嵌套 JAR handler。
     */
    private List<String> findAllClassNames() {
        try {
            List<String> classNames = new ArrayList<>();
            for (String basePackage : basePackages) {
                classNames.addAll(findClassNames(basePackage));
            }
            return classNames;
        } finally {
            closeNestedJarHandler();
        }
    }

    /**
     * Short-term cache for classpath hash to avoid repeated filesystem I/O.
     * Uses a single volatile record to eliminate read-tearing between hash and timestamp.
     * 类路径哈希的短期缓存，避免重复的文件系统 I/O。
     * 使用单个 volatile record 消除 hash 和时间戳之间的读撕裂。
     */
    private record ClasspathHashCache(String hash, long timestampMs) {}
    private static volatile ClasspathHashCache cachedClasspathHash;
    private static final long CLASSPATH_HASH_TTL_MS = 5_000;

    /**
     * Compute a deterministic SHA-256 hash of classpath entries (with short-term cache).
     * Delegates to {@link cloud.opencode.base.classloader.index.ClassIndexWriter#computeClasspathHash()}
     * to avoid code duplication.
     * 计算类路径条目的确定性 SHA-256 哈希（带短期缓存）。
     * 委托给 ClassIndexWriter 以避免代码重复。
     */
    static String computeClasspathHash() {
        ClasspathHashCache cached = cachedClasspathHash;
        long now = System.currentTimeMillis();
        if (cached != null && (now - cached.timestampMs()) < CLASSPATH_HASH_TTL_MS) {
            return cached.hash();
        }
        String hash = cloud.opencode.base.classloader.index.ClassIndexWriter.computeClasspathHash();
        cachedClasspathHash = new ClasspathHashCache(hash, now);
        return hash;
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

    /** Maximum directory recursion depth to prevent symlink loops */
    private static final int MAX_DIRECTORY_DEPTH = 64;

    private List<String> findClassesInDirectory(File directory, String packageName) {
        return findClassesInDirectory(directory, packageName, new java.util.HashSet<>(), 0);
    }

    private List<String> findClassesInDirectory(File directory, String packageName,
                                                 java.util.Set<String> visitedPaths, int depth) {
        List<String> classNames = new ArrayList<>();
        if (!directory.exists() || depth > MAX_DIRECTORY_DEPTH) {
            return classNames;
        }

        // Prevent symlink cycles by tracking canonical paths
        String canonicalPath;
        try {
            canonicalPath = directory.getCanonicalPath();
        } catch (java.io.IOException e) {
            return classNames;
        }
        if (!visitedPaths.add(canonicalPath)) {
            return classNames; // already visited — symlink cycle
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classNames;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classNames.addAll(findClassesInDirectory(
                        file, packageName + "." + file.getName(), visitedPaths, depth + 1));
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
            Path jarFilePath = Path.of(connection.getJarFile().getName());
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

            // Scan nested JARs if enabled (e.g. BOOT-INF/lib/*.jar)
            if (includeNestedJars) {
                classNames.addAll(findClassesInNestedJars(jarFilePath, packagePath));
            }
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.DEBUG, "Failed to scan JAR for classes: " + jarUrl, e);
        }

        return classNames;
    }

    /**
     * Scan nested JARs inside a fat JAR for classes matching the package path.
     * Uses a lazily-initialized shared handler that is closed when the scan completes.
     * 扫描 fat JAR 内的嵌套 JAR 中匹配包路径的类。
     * 使用延迟初始化的共享 handler，扫描完成后关闭。
     */
    private List<String> findClassesInNestedJars(Path outerJarPath, String packagePath) {
        List<String> classNames = new ArrayList<>();
        NestedJarHandler handler = getOrCreateNestedJarHandler();
        try {
            List<String> nestedEntries = handler.findNestedJars(outerJarPath);
            for (String nestedEntry : nestedEntries) {
                Path extracted = handler.extractNestedJar(outerJarPath, nestedEntry);
                try (JarFile nestedJar = new JarFile(extracted.toFile())) {
                    Enumeration<JarEntry> entries = nestedJar.entries();
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
                } finally {
                    handler.release(outerJarPath, nestedEntry);
                }
            }
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Failed to scan nested JARs in: " + outerJarPath, e);
        }
        return classNames;
    }

    /** Lazily-initialized handler reused across multiple JAR scans within a single scan() call. */
    private NestedJarHandler nestedJarHandler;

    private NestedJarHandler getOrCreateNestedJarHandler() {
        if (nestedJarHandler == null || nestedJarHandler.isClosed()) {
            nestedJarHandler = NestedJarHandler.builder().build();
        }
        return nestedJarHandler;
    }

    /** Close the nested JAR handler if it was created during scanning. */
    private void closeNestedJarHandler() {
        if (nestedJarHandler != null) {
            nestedJarHandler.close();
            nestedJarHandler = null;
        }
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
