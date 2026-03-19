package cloud.opencode.base.classloader.scanner;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Package Scanner - Scans package structure
 * 包扫描器 - 扫描包结构
 *
 * <p>Scans package structure and finds sub-packages and classes.</p>
 * <p>扫描包结构并查找子包和类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Find sub-packages - 查找子包</li>
 *   <li>Find classes in package - 查找包中的类</li>
 *   <li>Recursive scanning - 递归扫描</li>
 *   <li>Virtual thread support - 虚拟线程支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PackageScanner scanner = PackageScanner.of("com.example");
 * Set<String> subPackages = scanner.findSubPackages();
 * List<Class<?>> classes = scanner.findClasses();
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
public class PackageScanner {

    private static final System.Logger LOGGER = System.getLogger(PackageScanner.class.getName());

    private final String basePackage;
    private ClassLoader classLoader;
    private boolean useVirtualThreads = false;

    private PackageScanner(String basePackage) {
        this.basePackage = Objects.requireNonNull(basePackage, "Base package must not be null");
        this.classLoader = Thread.currentThread().getContextClassLoader();
        if (this.classLoader == null) {
            this.classLoader = PackageScanner.class.getClassLoader();
        }
    }

    /**
     * Create package scanner
     * 创建包扫描器
     *
     * @param basePackage base package | 基础包
     * @return scanner | 扫描器
     */
    public static PackageScanner of(String basePackage) {
        return new PackageScanner(basePackage);
    }

    /**
     * Set class loader
     * 设置类加载器
     *
     * @param classLoader class loader | 类加载器
     * @return this scanner | 此扫描器
     */
    public PackageScanner classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        return this;
    }

    /**
     * Enable virtual threads for parallel scanning
     * 启用虚拟线程进行并行扫描
     *
     * @param useVirtualThreads use virtual threads | 使用虚拟线程
     * @return this scanner | 此扫描器
     */
    public PackageScanner useVirtualThreads(boolean useVirtualThreads) {
        this.useVirtualThreads = useVirtualThreads;
        return this;
    }

    /**
     * Find all sub-packages
     * 查找所有子包
     *
     * @return set of sub-package names | 子包名集合
     */
    public Set<String> findSubPackages() {
        Set<String> subPackages = new HashSet<>();
        String packagePath = basePackage.replace('.', '/');

        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    findSubPackagesInDirectory(new File(resource.toURI()), basePackage, subPackages);
                } else if ("jar".equals(protocol)) {
                    findSubPackagesInJar(resource, packagePath, subPackages);
                }
            }
        } catch (Exception e) {
            throw OpenClassLoaderException.scanFailed(basePackage, e);
        }

        return subPackages;
    }

    /**
     * Find all classes in package (non-recursive)
     * 查找包中的所有类（非递归）
     *
     * @return list of classes | 类列表
     */
    public List<Class<?>> findClasses() {
        return findClasses(false);
    }

    /**
     * Find all classes in package
     * 查找包中的所有类
     *
     * @param recursive include sub-packages | 包含子包
     * @return list of classes | 类列表
     */
    public List<Class<?>> findClasses(boolean recursive) {
        List<String> classNames = findClassNames(recursive);

        if (useVirtualThreads) {
            return loadClassesWithVirtualThreads(classNames);
        } else {
            return classNames.stream()
                    .map(this::loadClass)
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    /**
     * Find all class names in package
     * 查找包中的所有类名
     *
     * @param recursive include sub-packages | 包含子包
     * @return list of class names | 类名列表
     */
    public List<String> findClassNames(boolean recursive) {
        List<String> classNames = new ArrayList<>();
        String packagePath = basePackage.replace('.', '/');

        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    findClassNamesInDirectory(new File(resource.toURI()), basePackage, classNames, recursive);
                } else if ("jar".equals(protocol)) {
                    findClassNamesInJar(resource, packagePath, classNames, recursive);
                }
            }
        } catch (Exception e) {
            throw OpenClassLoaderException.scanFailed(basePackage, e);
        }

        return classNames;
    }

    /**
     * Check if package exists
     * 检查包是否存在
     *
     * @return true if exists | 存在返回 true
     */
    public boolean exists() {
        String packagePath = basePackage.replace('.', '/');
        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            return resources.hasMoreElements();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get base package name
     * 获取基础包名
     *
     * @return base package name | 基础包名
     */
    public String getBasePackage() {
        return basePackage;
    }

    // ==================== Private Methods | 私有方法 ====================

    private void findSubPackagesInDirectory(File directory, String packageName, Set<String> result) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                String subPackage = packageName + "." + file.getName();
                result.add(subPackage);
                findSubPackagesInDirectory(file, subPackage, result);
            }
        }
    }

    private void findSubPackagesInJar(URL jarUrl, String packagePath, Set<String> result) {
        try {
            JarURLConnection connection = (JarURLConnection) jarUrl.openConnection();
            try (JarFile jarFile = connection.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                Set<String> seen = new HashSet<>();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    if (entryName.startsWith(packagePath + "/") && entryName.endsWith("/")) {
                        String subPath = entryName.substring(0, entryName.length() - 1);
                        String subPackage = subPath.replace('/', '.');
                        if (!seen.contains(subPackage) && !subPackage.equals(basePackage)) {
                            result.add(subPackage);
                            seen.add(subPackage);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.DEBUG, "Failed to scan JAR for sub-packages: " + jarUrl, e);
        }
    }

    private void findClassNamesInDirectory(File directory, String packageName,
                                           List<String> result, boolean recursive) {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory() && recursive) {
                findClassNamesInDirectory(file, packageName + "." + file.getName(), result, true);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                result.add(className);
            }
        }
    }

    private void findClassNamesInJar(URL jarUrl, String packagePath,
                                     List<String> result, boolean recursive) {
        try {
            JarURLConnection connection = (JarURLConnection) jarUrl.openConnection();
            try (JarFile jarFile = connection.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    if (entryName.endsWith(".class") && entryName.startsWith(packagePath + "/")) {
                        String relativePath = entryName.substring(packagePath.length() + 1);
                        // Check if it's directly in the package (no sub-directory)
                        if (recursive || !relativePath.contains("/")) {
                            String className = entryName.replace('/', '.').replace(".class", "");
                            result.add(className);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.DEBUG, "Failed to scan JAR for class names: " + jarUrl, e);
        }
    }

    private List<Class<?>> loadClassesWithVirtualThreads(List<String> classNames) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Class<?>> results = new ArrayList<>();
            List<Future<Class<?>>> futures = new ArrayList<>();

            for (String name : classNames) {
                futures.add(executor.submit(() -> loadClass(name)));
            }

            for (Future<Class<?>> future : futures) {
                try {
                    Class<?> clazz = future.get();
                    if (clazz != null) {
                        results.add(clazz);
                    }
                } catch (Exception ignored) {
                }
            }
            return results;
        }
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }
}
