package cloud.opencode.base.classloader.diagnostic;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * ClassLoader diagnostics utility for detecting classpath issues
 * ClassLoader 诊断工具，用于检测类路径问题
 *
 * <p>Provides static utility methods for diagnosing common ClassLoader-related issues
 * such as duplicate classes on the classpath, split packages across ClassLoaders, and
 * tracing the delegation chain for class loading. All methods use resource-based scanning
 * ({@code getResources()}) rather than {@code loadClass()} to avoid side effects.</p>
 *
 * <p>提供静态工具方法，用于诊断常见的 ClassLoader 相关问题，
 * 例如类路径上的重复类、跨 ClassLoader 拆分的包，以及跟踪类加载的委托链。
 * 所有方法使用基于资源的扫描（{@code getResources()}）而非 {@code loadClass()}，
 * 以避免副作用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Find duplicate classes across ClassLoaders - 在多个 ClassLoader 中查找重复类</li>
 *   <li>Detect split packages - 检测拆分包</li>
 *   <li>Trace class loading delegation chain - 跟踪类加载委托链</li>
 *   <li>Locate class resources across loaders - 跨加载器定位类资源</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Find duplicate classes between two loaders
 * List<DuplicateClassReport> duplicates =
 *     ClassLoaderDiagnostics.findDuplicateClasses(loader1, loader2);
 *
 * // Trace a class loading path
 * ClassLoadTrace trace =
 *     ClassLoaderDiagnostics.traceClassLoading("com.example.Foo", loader1);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是 (无状态工具类)</li>
 *   <li>No strong ClassLoader references in reports - 报告中不持有 ClassLoader 的强引用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public final class ClassLoaderDiagnostics {

    private static final System.Logger LOGGER = System.getLogger(ClassLoaderDiagnostics.class.getName());

    private static final String BOOTSTRAP_NAME = "bootstrap";

    private ClassLoaderDiagnostics() {
        throw new AssertionError("No instances");
    }

    /**
     * Find classes that exist in two or more of the given ClassLoaders
     * 查找在两个或多个给定 ClassLoader 中存在的类
     *
     * <p>Scans each ClassLoader using {@code getResources()} for {@code .class} files.
     * Returns a report for every class name found in at least two loaders.</p>
     *
     * <p>使用 {@code getResources()} 扫描每个 ClassLoader 的 {@code .class} 文件。
     * 返回至少在两个加载器中找到的每个类名的报告。</p>
     *
     * @param classLoaders the ClassLoaders to scan | 要扫描的 ClassLoader
     * @return list of duplicate class reports, empty if no duplicates found |
     *         重复类报告列表，如果没有发现重复则为空
     * @throws NullPointerException if classLoaders array or any element is null |
     *                              当 classLoaders 数组或任何元素为 null 时
     */
    public static List<DuplicateClassReport> findDuplicateClasses(ClassLoader... classLoaders) {
        Objects.requireNonNull(classLoaders, "classLoaders must not be null");
        for (ClassLoader cl : classLoaders) {
            Objects.requireNonNull(cl, "classLoader element must not be null");
        }
        if (classLoaders.length < 2) {
            return List.of();
        }

        // className -> { loaderName -> set of locations }
        Map<String, Map<String, Set<String>>> classMap = new HashMap<>();

        for (ClassLoader cl : classLoaders) {
            String loaderName = classLoaderName(cl);
            scanClassLoaderResources(cl, loaderName, classMap);
        }

        List<DuplicateClassReport> results = new ArrayList<>();
        for (Map.Entry<String, Map<String, Set<String>>> entry : classMap.entrySet()) {
            Map<String, Set<String>> loaderLocations = entry.getValue();
            if (loaderLocations.size() >= 2) {
                List<String> loaderNames = new ArrayList<>(loaderLocations.keySet());
                List<String> allLocations = new ArrayList<>();
                for (Set<String> locs : loaderLocations.values()) {
                    allLocations.addAll(locs);
                }
                results.add(new DuplicateClassReport(entry.getKey(), loaderNames, allLocations));
            }
        }
        return Collections.unmodifiableList(results);
    }

    /**
     * Detect packages that are split across multiple ClassLoaders
     * 检测跨多个 ClassLoader 拆分的包
     *
     * <p>Scans each ClassLoader for class resources and identifies packages whose classes
     * appear in two or more loaders. Split packages can cause issues with the Java module
     * system and may lead to runtime access errors.</p>
     *
     * <p>扫描每个 ClassLoader 的类资源，识别其类出现在两个或多个加载器中的包。
     * 拆分包可能导致 Java 模块系统问题，并可能导致运行时访问错误。</p>
     *
     * @param classLoaders the ClassLoaders to scan | 要扫描的 ClassLoader
     * @return list of package split reports | 包拆分报告列表
     * @throws NullPointerException if classLoaders array or any element is null |
     *                              当 classLoaders 数组或任何元素为 null 时
     */
    public static List<PackageSplitReport> detectPackageSplits(ClassLoader... classLoaders) {
        Objects.requireNonNull(classLoaders, "classLoaders must not be null");
        for (ClassLoader cl : classLoaders) {
            Objects.requireNonNull(cl, "classLoader element must not be null");
        }
        if (classLoaders.length < 2) {
            return List.of();
        }

        // packageName -> set of loaderNames
        Map<String, Set<String>> packageMap = new LinkedHashMap<>();

        for (ClassLoader cl : classLoaders) {
            String loaderName = classLoaderName(cl);
            Set<String> packages = discoverPackages(cl);
            for (String pkg : packages) {
                packageMap.computeIfAbsent(pkg, _ -> new LinkedHashSet<>()).add(loaderName);
            }
        }

        List<PackageSplitReport> results = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : packageMap.entrySet()) {
            if (entry.getValue().size() >= 2) {
                results.add(new PackageSplitReport(entry.getKey(), new ArrayList<>(entry.getValue())));
            }
        }
        return Collections.unmodifiableList(results);
    }

    /**
     * Trace the class loading delegation chain for a given class name
     * 跟踪给定类名的类加载委托链
     *
     * <p>Walks the parent chain from the given ClassLoader, trying {@code getResource()} at
     * each level. Returns a {@link ClassLoadTrace} showing every loader consulted and which
     * one ultimately provides the class resource.</p>
     *
     * <p>从给定的 ClassLoader 向上遍历父链，在每个级别尝试 {@code getResource()}。
     * 返回 {@link ClassLoadTrace}，显示咨询的每个加载器以及最终提供类资源的加载器。</p>
     *
     * @param className   the fully qualified class name to trace | 要跟踪的完全限定类名
     * @param classLoader the starting ClassLoader | 起始 ClassLoader
     * @return the class load trace | 类加载跟踪记录
     * @throws NullPointerException if className or classLoader is null |
     *                              当 className 或 classLoader 为 null 时
     */
    public static ClassLoadTrace traceClassLoading(String className, ClassLoader classLoader) {
        Objects.requireNonNull(className, "className must not be null");
        Objects.requireNonNull(classLoader, "classLoader must not be null");

        String resourcePath = classNameToResourcePath(className);

        // Build delegation chain (child → parent → ... → bootstrap)
        List<String> delegationChain = new ArrayList<>();
        List<ClassLoader> loaderChain = new ArrayList<>();
        ClassLoader current = classLoader;
        while (current != null) {
            delegationChain.add(classLoaderName(current));
            loaderChain.add(current);
            current = current.getParent();
        }
        delegationChain.add(BOOTSTRAP_NAME);
        loaderChain.add(null); // null represents bootstrap

        // Use getResource() on the starting classloader — it follows parent-first delegation
        // internally, so the result reflects the actual defining loader.
        // Then walk the chain to identify which loader owns that URL.
        String definingLoader = BOOTSTRAP_NAME;
        String location = null;

        URL resolvedUrl = classLoader.getResource(resourcePath);
        if (resolvedUrl != null) {
            location = resolvedUrl.toExternalForm();
            // Walk from parent (bootstrap) toward child to find the most-parent loader
            // that directly provides this resource
            for (int i = loaderChain.size() - 1; i >= 0; i--) {
                ClassLoader loader = loaderChain.get(i);
                URL url;
                if (loader == null) {
                    url = ClassLoader.getSystemResource(resourcePath);
                } else {
                    url = loader.getResource(resourcePath);
                }
                if (url != null && url.equals(resolvedUrl)) {
                    definingLoader = delegationChain.get(i);
                    break;
                }
            }
        }

        return new ClassLoadTrace(className, delegationChain, definingLoader, location);
    }

    /**
     * Find all resource locations for a class across the given ClassLoaders
     * 在给定的 ClassLoader 中查找某个类的所有资源位置
     *
     * <p>Converts the class name to a resource path and calls {@code getResources()} on
     * each ClassLoader, collecting all distinct URL strings.</p>
     *
     * <p>将类名转换为资源路径，并在每个 ClassLoader 上调用 {@code getResources()}，
     * 收集所有不同的 URL 字符串。</p>
     *
     * @param className    the fully qualified class name | 完全限定类名
     * @param classLoaders the ClassLoaders to search | 要搜索的 ClassLoader
     * @return list of URL strings where the class resource was found |
     *         找到类资源的 URL 字符串列表
     * @throws NullPointerException if className or classLoaders array or any element is null |
     *                              当 className 或 classLoaders 数组或任何元素为 null 时
     */
    public static List<String> findClassLocations(String className, ClassLoader... classLoaders) {
        Objects.requireNonNull(className, "className must not be null");
        Objects.requireNonNull(classLoaders, "classLoaders must not be null");
        for (ClassLoader cl : classLoaders) {
            Objects.requireNonNull(cl, "classLoader element must not be null");
        }

        String resourcePath = classNameToResourcePath(className);
        Set<String> seen = new LinkedHashSet<>();

        for (ClassLoader cl : classLoaders) {
            try {
                Enumeration<URL> urls = cl.getResources(resourcePath);
                while (urls.hasMoreElements()) {
                    seen.add(urls.nextElement().toExternalForm());
                }
            } catch (IOException e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Failed to get resources for {0} from {1}: {2}",
                        className, classLoaderName(cl), e.getMessage());
            }
        }
        return List.copyOf(seen);
    }

    /**
     * Convert a fully qualified class name to a resource path
     * 将完全限定类名转换为资源路径
     *
     * @param className the class name (e.g. "com.example.Foo") | 类名（例如 "com.example.Foo"）
     * @return the resource path (e.g. "com/example/Foo.class") | 资源路径（例如 "com/example/Foo.class"）
     */
    static String classNameToResourcePath(String className) {
        return className.replace('.', '/') + ".class";
    }

    /**
     * Get a safe string name for a ClassLoader, never holding a strong reference
     * 获取 ClassLoader 的安全字符串名称，不持有强引用
     *
     * @param classLoader the ClassLoader, may be null for bootstrap | ClassLoader，bootstrap 时可能为 null
     * @return a descriptive name string | 描述性名称字符串
     */
    static String classLoaderName(ClassLoader classLoader) {
        if (classLoader == null) {
            return BOOTSTRAP_NAME;
        }
        String name = classLoader.getName();
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return classLoader.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(classLoader));
    }

    /**
     * Scan a ClassLoader's resources to build the class map by enumerating classpath roots
     * 通过枚举类路径根扫描 ClassLoader 的资源以构建类映射
     *
     * <p>Uses {@code getResources("")} to discover classpath roots, then recursively scans
     * directories and JAR files for .class entries.</p>
     * <p>使用 {@code getResources("")} 发现类路径根，然后递归扫描目录和 JAR 文件中的 .class 条目。</p>
     */
    private static void scanClassLoaderResources(ClassLoader cl, String loaderName,
                                                  Map<String, Map<String, Set<String>>> classMap) {
        try {
            Enumeration<URL> roots = cl.getResources("");
            while (roots.hasMoreElements()) {
                URL root = roots.nextElement();
                String protocol = root.getProtocol();
                if ("file".equals(protocol)) {
                    scanDirectory(root, loaderName, classMap);
                } else if ("jar".equals(protocol)) {
                    scanJarUrl(root, loaderName, classMap);
                }
            }
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to scan resources for loader {0}: {1}",
                    loaderName, e.getMessage());
        }
    }

    /**
     * Recursively scan a file-system directory for .class files
     * 递归扫描文件系统目录中的 .class 文件
     */
    private static void scanDirectory(URL root, String loaderName,
                                       Map<String, Map<String, Set<String>>> classMap) {
        Path rootPath;
        try {
            rootPath = Path.of(root.toURI());
        } catch (URISyntaxException e) {
            return;
        }
        if (!Files.isDirectory(rootPath)) {
            return;
        }
        scanDirectoryRecursive(rootPath, rootPath, loaderName, root.toExternalForm(), classMap, 0);
    }

    private static final int MAX_SCAN_DEPTH = 128;

    private static void scanDirectoryRecursive(Path base, Path dir, String loaderName,
                                                String rootUrl,
                                                Map<String, Map<String, Set<String>>> classMap,
                                                int depth) {
        if (depth > MAX_SCAN_DEPTH) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    scanDirectoryRecursive(base, entry, loaderName, rootUrl, classMap, depth + 1);
                } else if (entry.getFileName().toString().endsWith(".class")) {
                    String relative = base.relativize(entry).toString();
                    if (relative.startsWith("META-INF") || relative.contains("module-info")) {
                        continue;
                    }
                    String className = relative
                            .replace(File.separatorChar, '.')
                            .replace('/', '.');
                    className = className.substring(0, className.length() - ".class".length());
                    classMap.computeIfAbsent(className, _ -> new LinkedHashMap<>())
                            .computeIfAbsent(loaderName, _ -> new LinkedHashSet<>())
                            .add(rootUrl + relative);
                }
            }
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Failed to scan directory {0}: {1}", dir, e.getMessage());
        }
    }

    /**
     * Scan a jar: URL for .class entries
     * 扫描 jar: URL 中的 .class 条目
     */
    private static void scanJarUrl(URL root, String loaderName,
                                    Map<String, Map<String, Set<String>>> classMap) {
        try {
            JarURLConnection conn = (JarURLConnection) root.openConnection();
            conn.setUseCaches(false);
            try (JarFile jarFile = conn.getJarFile()) {
                String rootUrl = "jar:" + conn.getJarFileURL().toExternalForm() + "!/";
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (entry.isDirectory() || !name.endsWith(".class")) {
                        continue;
                    }
                    if (name.startsWith("META-INF/") || name.contains("module-info")) {
                        continue;
                    }
                    String className = name.substring(0, name.length() - ".class".length())
                            .replace('/', '.');
                    classMap.computeIfAbsent(className, _ -> new LinkedHashMap<>())
                            .computeIfAbsent(loaderName, _ -> new LinkedHashSet<>())
                            .add(rootUrl + name);
                }
            }
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Failed to scan JAR URL {0}: {1}", root, e.getMessage());
        }
    }

    /**
     * Discover packages visible to a ClassLoader by scanning root resources
     * 通过扫描根资源发现 ClassLoader 可见的包
     */
    private static Set<String> discoverPackages(ClassLoader cl) {
        Set<String> packages = new HashSet<>();
        // Use the ClassLoader's defined packages if available
        Package[] definedPkgs = cl.getDefinedPackages();
        for (Package pkg : definedPkgs) {
            packages.add(pkg.getName());
        }
        return packages;
    }

}
