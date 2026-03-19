package cloud.opencode.base.reflect.scan;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * ClassPath Scanner
 * 类路径扫描器
 *
 * <p>Scans the classpath for classes and resources.
 * Similar to Guava ClassPath.</p>
 * <p>扫描类路径中的类和资源。
 * 类似于Guava ClassPath。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Classpath scanning - 类路径扫描</li>
 *   <li>Package scanning - 包扫描</li>
 *   <li>Resource discovery - 资源发现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ClassPath classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
 * Set<ClassInfo> classes = classPath.getTopLevelClasses("com.example");
 * Set<ResourceInfo> resources = classPath.getResources();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (class loader must be non-null) - 空值安全: 否（类加载器须非空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class ClassPath {

    private final Set<ClassInfo> classes;
    private final Set<ResourceInfo> resources;
    private final ClassLoader classLoader;

    private ClassPath(ClassLoader classLoader, Set<ClassInfo> classes, Set<ResourceInfo> resources) {
        this.classLoader = classLoader;
        this.classes = Collections.unmodifiableSet(classes);
        this.resources = Collections.unmodifiableSet(resources);
    }

    /**
     * Creates a ClassPath from the given ClassLoader
     * 从给定ClassLoader创建ClassPath
     *
     * @param classLoader the class loader | 类加载器
     * @return the ClassPath | ClassPath
     */
    public static ClassPath from(ClassLoader classLoader) {
        Set<ClassInfo> classes = new LinkedHashSet<>();
        Set<ResourceInfo> resources = new LinkedHashSet<>();

        try {
            scanClassLoader(classLoader, classes, resources);
        } catch (IOException e) {
            throw new OpenReflectException("Failed to scan classpath", e);
        }

        return new ClassPath(classLoader, classes, resources);
    }

    /**
     * Creates a ClassPath from the system ClassLoader
     * 从系统ClassLoader创建ClassPath
     *
     * @return the ClassPath | ClassPath
     */
    public static ClassPath fromSystemClassLoader() {
        return from(ClassLoader.getSystemClassLoader());
    }

    /**
     * Creates a ClassPath from the thread context ClassLoader
     * 从线程上下文ClassLoader创建ClassPath
     *
     * @return the ClassPath | ClassPath
     */
    public static ClassPath fromContextClassLoader() {
        return from(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Gets all classes
     * 获取所有类
     *
     * @return set of ClassInfo | ClassInfo集合
     */
    public Set<ClassInfo> getAllClasses() {
        return classes;
    }

    /**
     * Gets all resources
     * 获取所有资源
     *
     * @return set of ResourceInfo | ResourceInfo集合
     */
    public Set<ResourceInfo> getResources() {
        return resources;
    }

    /**
     * Gets classes in the specified package (exact match)
     * 获取指定包中的类（精确匹配）
     *
     * @param packageName the package name | 包名
     * @return set of ClassInfo | ClassInfo集合
     */
    public Set<ClassInfo> getClassesInPackage(String packageName) {
        Set<ClassInfo> result = new LinkedHashSet<>();
        for (ClassInfo classInfo : classes) {
            if (classInfo.isInPackage(packageName)) {
                result.add(classInfo);
            }
        }
        return result;
    }

    /**
     * Gets top-level classes in the specified package
     * 获取指定包中的顶层类
     *
     * @param packageName the package name | 包名
     * @return set of ClassInfo | ClassInfo集合
     */
    public Set<ClassInfo> getTopLevelClassesInPackage(String packageName) {
        Set<ClassInfo> result = new LinkedHashSet<>();
        for (ClassInfo classInfo : classes) {
            if (classInfo.isInPackage(packageName) && !classInfo.isInnerClass()) {
                result.add(classInfo);
            }
        }
        return result;
    }

    /**
     * Gets classes in the specified package and subpackages
     * 获取指定包及子包中的类
     *
     * @param packageName the package name | 包名
     * @return set of ClassInfo | ClassInfo集合
     */
    public Set<ClassInfo> getClassesRecursively(String packageName) {
        Set<ClassInfo> result = new LinkedHashSet<>();
        for (ClassInfo classInfo : classes) {
            if (classInfo.isInPackageOrSubpackage(packageName)) {
                result.add(classInfo);
            }
        }
        return result;
    }

    /**
     * Gets top-level classes recursively
     * 递归获取顶层类
     *
     * @param packageName the package name | 包名
     * @return set of ClassInfo | ClassInfo集合
     */
    public Set<ClassInfo> getTopLevelClassesRecursively(String packageName) {
        Set<ClassInfo> result = new LinkedHashSet<>();
        for (ClassInfo classInfo : classes) {
            if (classInfo.isInPackageOrSubpackage(packageName) && !classInfo.isInnerClass()) {
                result.add(classInfo);
            }
        }
        return result;
    }

    /**
     * Gets resources in the specified package
     * 获取指定包中的资源
     *
     * @param packageName the package name | 包名
     * @return set of ResourceInfo | ResourceInfo集合
     */
    public Set<ResourceInfo> getResourcesInPackage(String packageName) {
        Set<ResourceInfo> result = new LinkedHashSet<>();
        String packagePath = packageName.replace('.', '/');
        for (ResourceInfo resource : resources) {
            if (resource.getResourceName().startsWith(packagePath)) {
                result.add(resource);
            }
        }
        return result;
    }

    /**
     * Gets resources with specific extension
     * 获取具有特定扩展名的资源
     *
     * @param extension the extension (without dot) | 扩展名（不含点）
     * @return set of ResourceInfo | ResourceInfo集合
     */
    public Set<ResourceInfo> getResourcesWithExtension(String extension) {
        Set<ResourceInfo> result = new LinkedHashSet<>();
        String suffix = "." + extension;
        for (ResourceInfo resource : resources) {
            if (resource.getResourceName().endsWith(suffix)) {
                result.add(resource);
            }
        }
        return result;
    }

    /**
     * Gets the class loader
     * 获取类加载器
     *
     * @return the class loader | 类加载器
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Streams all classes
     * 流式获取所有类
     *
     * @return stream of ClassInfo | ClassInfo流
     */
    public Stream<ClassInfo> streamClasses() {
        return classes.stream();
    }

    /**
     * Streams all resources
     * 流式获取所有资源
     *
     * @return stream of ResourceInfo | ResourceInfo流
     */
    public Stream<ResourceInfo> streamResources() {
        return resources.stream();
    }

    private static void scanClassLoader(ClassLoader classLoader, Set<ClassInfo> classes, Set<ResourceInfo> resources) throws IOException {
        Set<URI> scannedUris = new HashSet<>();

        // Get URLs from URLClassLoader
        if (classLoader instanceof URLClassLoader urlClassLoader) {
            for (URL url : urlClassLoader.getURLs()) {
                try {
                    URI uri = url.toURI();
                    if (scannedUris.add(uri)) {
                        scanUri(uri, classLoader, classes, resources);
                    }
                } catch (URISyntaxException ignored) {
                }
            }
        }

        // Scan class path from system property
        String classPath = System.getProperty("java.class.path");
        if (classPath != null) {
            for (String entry : classPath.split(File.pathSeparator)) {
                try {
                    URI uri = new File(entry).toURI();
                    if (scannedUris.add(uri)) {
                        scanUri(uri, classLoader, classes, resources);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void scanUri(URI uri, ClassLoader classLoader, Set<ClassInfo> classes, Set<ResourceInfo> resources) throws IOException {
        File file = new File(uri);
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            scanDirectory(file, "", classLoader, classes, resources);
        } else if (file.getName().endsWith(".jar")) {
            scanJar(file, classLoader, classes, resources);
        }
    }

    private static void scanDirectory(File directory, String packagePrefix, ClassLoader classLoader,
                                       Set<ClassInfo> classes, Set<ResourceInfo> resources) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String resourceName = packagePrefix.isEmpty() ? file.getName() : packagePrefix + "/" + file.getName();

            if (file.isDirectory()) {
                scanDirectory(file, resourceName, classLoader, classes, resources);
            } else if (file.getName().endsWith(".class")) {
                classes.add(new ClassInfo(resourceName, classLoader));
            } else {
                resources.add(new ResourceInfo(resourceName, classLoader));
            }
        }
    }

    private static void scanJar(File jarFile, ClassLoader classLoader,
                                 Set<ClassInfo> classes, Set<ResourceInfo> resources) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();
                if (name.endsWith(".class")) {
                    classes.add(new ClassInfo(name, classLoader));
                } else {
                    resources.add(new ResourceInfo(name, classLoader));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ClassPath[classes=" + classes.size() + ", resources=" + resources.size() + "]";
    }
}
