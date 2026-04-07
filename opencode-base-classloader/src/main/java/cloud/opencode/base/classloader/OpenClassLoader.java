package cloud.opencode.base.classloader;

import cloud.opencode.base.classloader.diagnostic.ClassLoadTrace;
import cloud.opencode.base.classloader.diagnostic.ClassLoaderDiagnostics;
import cloud.opencode.base.classloader.diagnostic.DuplicateClassReport;
import cloud.opencode.base.classloader.diagnostic.PackageSplitReport;
import cloud.opencode.base.classloader.graalvm.NativeImageSupport;
import cloud.opencode.base.classloader.loader.*;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/**
 * OpenClassLoader - ClassLoader Facade
 * OpenClassLoader - 类加载器门面
 *
 * <p>Unified entry point for class loader operations.</p>
 * <p>类加载器操作的统一入口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Get default/context classloader - 获取默认/上下文类加载器</li>
 *   <li>Create isolated classloader - 创建隔离类加载器</li>
 *   <li>Create hot-swap classloader - 创建热替换类加载器</li>
 *   <li>ClassLoader utilities - 类加载器工具</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get default classloader
 * ClassLoader cl = OpenClassLoader.getDefaultClassLoader();
 *
 * // Create isolated classloader
 * IsoClassLoader iso = OpenClassLoader.isolatedLoader()
 *     .fromJar("/path/to/plugin.jar")
 *     .addIsolatedPackage("com.plugin")
 *     .build();
 *
 * // Execute with specific classloader
 * String result = OpenClassLoader.withClassLoader(customLoader, () -> {
 *     // Code runs with customLoader as context classloader
 *     return "result";
 * });
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
public final class OpenClassLoader {

    private OpenClassLoader() {
        // Utility class
    }

    // ==================== ClassLoader Getters | 类加载器获取 ====================

    /**
     * Get default classloader
     * 获取默认类加载器
     *
     * <p>Priority: Context ClassLoader > Class ClassLoader > System ClassLoader</p>
     * <p>优先级: 上下文类加载器 > 类的类加载器 > 系统类加载器</p>
     *
     * @return default classloader | 默认类加载器
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = OpenClassLoader.class.getClassLoader();
        }
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl;
    }

    /**
     * Get context classloader
     * 获取上下文类加载器
     *
     * @return context classloader | 上下文类加载器
     */
    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Get classloader for specified class
     * 获取指定类的类加载器
     *
     * @param clazz class | 类
     * @return classloader | 类加载器
     */
    public static ClassLoader getClassLoader(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        ClassLoader cl = clazz.getClassLoader();
        return cl != null ? cl : ClassLoader.getSystemClassLoader();
    }

    // ==================== ClassLoader Creation | 类加载器创建 ====================

    /**
     * Create isolated classloader builder
     * 创建隔离类加载器构建器
     *
     * @return builder | 构建器
     */
    public static IsoClassLoader.Builder isolatedLoader() {
        return new IsoClassLoader.Builder();
    }

    /**
     * Create hot-swap classloader
     * 创建热替换类加载器
     *
     * @return hot-swap classloader | 热替换类加载器
     */
    public static HotSwapClassLoader hotSwapLoader() {
        return HotSwapClassLoader.create();
    }

    /**
     * Create hot-swap classloader with parent
     * 使用父加载器创建热替换类加载器
     *
     * @param parent parent classloader | 父类加载器
     * @return hot-swap classloader | 热替换类加载器
     */
    public static HotSwapClassLoader hotSwapLoader(ClassLoader parent) {
        return HotSwapClassLoader.create(parent);
    }

    /**
     * Create hot-swap classloader with version history support
     * 创建带版本历史支持的热替换类加载器
     *
     * @param maxHistoryVersions max history versions to keep per class | 每个类保留的最大历史版本数
     * @return hot-swap classloader | 热替换类加载器
     */
    public static HotSwapClassLoader hotSwapLoader(int maxHistoryVersions) {
        return HotSwapClassLoader.create(maxHistoryVersions);
    }

    /**
     * Create hot-swap classloader with parent and version history support
     * 使用父加载器创建带版本历史支持的热替换类加载器
     *
     * @param parent             parent classloader | 父类加载器
     * @param maxHistoryVersions max history versions to keep per class | 每个类保留的最大历史版本数
     * @return hot-swap classloader | 热替换类加载器
     */
    public static HotSwapClassLoader hotSwapLoader(ClassLoader parent, int maxHistoryVersions) {
        return HotSwapClassLoader.create(parent, maxHistoryVersions);
    }

    /**
     * Create resource classloader
     * 创建资源类加载器
     *
     * @param resourcePaths resource paths | 资源路径
     * @return resource classloader | 资源类加载器
     */
    public static ResourceClassLoader resourceLoader(Path... resourcePaths) {
        return ResourceClassLoader.create(resourcePaths);
    }

    // ==================== Utilities | 工具方法 ====================

    /**
     * Execute operation with specified classloader as context classloader
     * 使用指定类加载器作为上下文类加载器执行操作
     *
     * @param classLoader classloader to use | 要使用的类加载器
     * @param supplier    operation to execute | 要执行的操作
     * @param <T>         return type | 返回类型
     * @return operation result | 操作结果
     */
    public static <T> T withClassLoader(ClassLoader classLoader, Supplier<T> supplier) {
        Objects.requireNonNull(classLoader, "ClassLoader must not be null");
        Objects.requireNonNull(supplier, "Supplier must not be null");

        Thread currentThread = Thread.currentThread();
        ClassLoader original = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(classLoader);
            return supplier.get();
        } finally {
            currentThread.setContextClassLoader(original);
        }
    }

    /**
     * Execute operation with specified classloader as context classloader
     * 使用指定类加载器作为上下文类加载器执行操作
     *
     * @param classLoader classloader to use | 要使用的类加载器
     * @param runnable    operation to execute | 要执行的操作
     */
    public static void withClassLoader(ClassLoader classLoader, Runnable runnable) {
        Objects.requireNonNull(classLoader, "ClassLoader must not be null");
        Objects.requireNonNull(runnable, "Runnable must not be null");

        Thread currentThread = Thread.currentThread();
        ClassLoader original = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(classLoader);
            runnable.run();
        } finally {
            currentThread.setContextClassLoader(original);
        }
    }

    /**
     * Check if class is visible (loadable) by classloader
     * 检查类是否对类加载器可见（可加载）
     *
     * @param clazz       class to check | 要检查的类
     * @param classLoader classloader | 类加载器
     * @return true if visible | 可见返回 true
     */
    public static boolean isVisible(Class<?> clazz, ClassLoader classLoader) {
        Objects.requireNonNull(clazz, "Class must not be null");
        if (classLoader == null) {
            return true; // Bootstrap classloader can see all
        }
        try {
            return classLoader.loadClass(clazz.getName()) == clazz;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Get classloader hierarchy
     * 获取类加载器层次
     *
     * @param classLoader starting classloader | 起始类加载器
     * @return list of classloaders from child to parent | 从子到父的类加载器列表
     */
    public static List<ClassLoader> getClassLoaderHierarchy(ClassLoader classLoader) {
        List<ClassLoader> hierarchy = new ArrayList<>();
        ClassLoader current = classLoader;
        while (current != null) {
            hierarchy.add(current);
            current = current.getParent();
        }
        return hierarchy;
    }

    /**
     * Find classloader that defines the class
     * 查找定义类的类加载器
     *
     * @param className class name | 类名
     * @return optional classloader | 可选的类加载器
     */
    public static Optional<ClassLoader> findDefiningClassLoader(String className) {
        Objects.requireNonNull(className, "Class name must not be null");

        ClassLoader current = getDefaultClassLoader();
        while (current != null) {
            try {
                Class<?> clazz = current.loadClass(className);
                return Optional.ofNullable(clazz.getClassLoader());
            } catch (ClassNotFoundException e) {
                // Continue to parent
            }
            current = current.getParent();
        }

        // Try system classloader
        try {
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
            return Optional.ofNullable(clazz.getClassLoader());
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Load class safely (returns Optional)
     * 安全加载类（返回 Optional）
     *
     * @param className class name | 类名
     * @return optional class | 可选的类
     */
    public static Optional<Class<?>> loadClass(String className) {
        return loadClass(className, getDefaultClassLoader());
    }

    /**
     * Load class safely with specified classloader
     * 使用指定类加载器安全加载类
     *
     * @param className   class name | 类名
     * @param classLoader classloader | 类加载器
     * @return optional class | 可选的类
     */
    public static Optional<Class<?>> loadClass(String className, ClassLoader classLoader) {
        Objects.requireNonNull(className, "Class name must not be null");
        try {
            return Optional.of(Class.forName(className, false, classLoader));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    // ==================== Diagnostics | 诊断工具 ====================

    /**
     * Find duplicate classes across the specified ClassLoaders
     * 在指定的 ClassLoader 中查找重复类
     *
     * @param classLoaders classloaders to scan | 要扫描的类加载器
     * @return list of duplicate class reports | 重复类报告列表
     */
    public static List<DuplicateClassReport> findDuplicateClasses(ClassLoader... classLoaders) {
        return ClassLoaderDiagnostics.findDuplicateClasses(classLoaders);
    }

    /**
     * Detect package splits across the specified ClassLoaders
     * 检测指定 ClassLoader 中的包拆分
     *
     * @param classLoaders classloaders to scan | 要扫描的类加载器
     * @return list of package split reports | 包拆分报告列表
     */
    public static List<PackageSplitReport> detectPackageSplits(ClassLoader... classLoaders) {
        return ClassLoaderDiagnostics.detectPackageSplits(classLoaders);
    }

    /**
     * Trace the class loading delegation chain for a class
     * 跟踪类的类加载委托链
     *
     * @param className   fully-qualified class name | 完全限定类名
     * @param classLoader starting classloader | 起始类加载器
     * @return class load trace | 类加载跟踪
     */
    public static ClassLoadTrace traceClassLoading(String className, ClassLoader classLoader) {
        return ClassLoaderDiagnostics.traceClassLoading(className, classLoader);
    }

    // ==================== Native Image Support | 原生镜像支持 ====================

    /**
     * Check if running in GraalVM native image
     * 检查是否在 GraalVM 原生镜像中运行
     *
     * @return true if in native image | 在原生镜像中返回 true
     */
    public static boolean isNativeImage() {
        return NativeImageSupport.isNativeImage();
    }
}
