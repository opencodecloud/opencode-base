package cloud.opencode.base.classloader.loader;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;
import cloud.opencode.base.classloader.leak.LeakDetection;
import cloud.opencode.base.classloader.leak.LeakDetector;
import cloud.opencode.base.classloader.security.ClassLoadingPolicy;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Isolated ClassLoader - Supports independent class loading environment
 * 隔离类加载器 - 支持独立的类加载环境
 *
 * <p>ClassLoader that supports class isolation for plugin systems and hot deployment.</p>
 * <p>支持类隔离的类加载器，用于插件系统和热部署。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Isolated class loading - 隔离类加载</li>
 *   <li>Configurable loading strategy - 可配置的加载策略</li>
 *   <li>Package-level isolation control - 包级别隔离控制</li>
 *   <li>Resource access - 资源访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * IsoClassLoader loader = IsoClassLoader.fromJar("/path/to/plugin.jar")
 *     .addIsolatedPackage("com.plugin")
 *     .build();
 * Class<?> pluginClass = loader.loadClass("com.plugin.MyPlugin");
 * loader.close();
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
public class IsoClassLoader extends URLClassLoader implements AutoCloseable {

    private final Set<String> isolatedPackages;
    private final Set<String> sharedPackages;
    private final LoadingStrategy loadingStrategy;
    private final ConcurrentHashMap<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();
    private final LeakDetection leakDetection;
    private final ClassLoadingPolicy policy;
    private final String loaderName;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Class loading strategy
     * 类加载策略
     */
    public enum LoadingStrategy {
        /** Parent first (default JVM behavior) | 父优先（默认 JVM 行为） */
        PARENT_FIRST,
        /** Child first (isolation mode) | 子优先（隔离模式） */
        CHILD_FIRST,
        /** Parent only | 仅父加载器 */
        PARENT_ONLY,
        /** Child only | 仅本加载器 */
        CHILD_ONLY
    }

    private IsoClassLoader(URL[] urls, ClassLoader parent, Set<String> isolatedPackages,
                           Set<String> sharedPackages, LoadingStrategy loadingStrategy,
                           LeakDetection leakDetection, ClassLoadingPolicy policy, String loaderName) {
        super(urls, parent);
        this.isolatedPackages = Set.copyOf(isolatedPackages);
        this.sharedPackages = Set.copyOf(sharedPackages);
        this.loadingStrategy = loadingStrategy;
        this.leakDetection = leakDetection;
        this.policy = policy;
        this.loaderName = loaderName;

        // Register for leak detection
        if (leakDetection != LeakDetection.DISABLED) {
            LeakDetector.getInstance().track(this, loaderName, leakDetection);
        }
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create builder from JAR file path string
     * 从 JAR 文件路径字符串创建构建器
     *
     * @param jarPath JAR file path | JAR 文件路径
     * @return builder | 构建器
     */
    public static Builder fromJar(String jarPath) {
        return fromJar(Path.of(jarPath));
    }

    /**
     * Create builder from JAR file path
     * 从 JAR 文件路径创建构建器
     *
     * @param jarPath JAR file path | JAR 文件路径
     * @return builder | 构建器
     */
    public static Builder fromJar(Path jarPath) {
        try {
            return new Builder().addUrl(jarPath.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new OpenClassLoaderException("Invalid JAR path: " + jarPath, e);
        }
    }

    /**
     * Create builder from directory
     * 从目录创建构建器
     *
     * @param directory directory path | 目录路径
     * @return builder | 构建器
     */
    public static Builder fromDirectory(Path directory) {
        try {
            return new Builder().addUrl(directory.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new OpenClassLoaderException("Invalid directory path: " + directory, e);
        }
    }

    /**
     * Create builder from URLs
     * 从 URL 创建构建器
     *
     * @param urls URLs | URL 数组
     * @return builder | 构建器
     */
    public static Builder fromUrls(URL... urls) {
        Builder builder = new Builder();
        for (URL url : urls) {
            builder.addUrl(url);
        }
        return builder;
    }

    // ==================== Class Loading | 类加载 ====================

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        checkNotClosed();

        // Check if already loaded
        Class<?> loadedClass = loadedClasses.get(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        // Check parent first for system classes
        if (isSystemClass(name)) {
            return super.loadClass(name, resolve);
        }

        // Synchronize per-name to prevent: (a) duplicate defineClass LinkageError,
        // (b) concurrent bypass of maxLoadedClasses limit
        synchronized (getClassLoadingLock(name)) {
            // Double-check after acquiring lock
            loadedClass = loadedClasses.get(name);
            if (loadedClass != null) {
                return loadedClass;
            }

            // Apply security policy name check (bytecode not available yet)
            if (policy != null) {
                policy.checkNameAllowed(name, loadedClasses.size());
            }

            // Apply loading strategy
            Class<?> clazz = switch (loadingStrategy) {
                case PARENT_FIRST -> loadParentFirst(name);
                case CHILD_FIRST -> loadChildFirst(name);
                case PARENT_ONLY -> loadParentOnly(name);
                case CHILD_ONLY -> loadChildOnly(name);
            };

            if (resolve) {
                resolveClass(clazz);
            }

            loadedClasses.put(name, clazz);
            return clazz;
        }
    }

    /**
     * Find and define a class locally, enforcing bytecode policy checks before defineClass.
     * 本地查找并定义类，在 defineClass 之前执行字节码策略检查。
     *
     * <p>When a {@link ClassLoadingPolicy} is configured with {@code maxBytecodeSize} or a
     * {@link cloud.opencode.base.classloader.security.BytecodeVerifier}, this method reads
     * the raw bytecode, validates it against the policy, and then defines the class.
     * Without a policy (or without bytecode-level constraints), delegates to the parent
     * {@link URLClassLoader#findClass(String)}.</p>
     *
     * @param name the binary name of the class | 类的二进制名称
     * @return the resulting Class object | 结果 Class 对象
     * @throws ClassNotFoundException if the class could not be found | 类未找到时抛出
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (policy != null && (policy.maxBytecodeSize() > 0 || policy.bytecodeVerifier() != null)) {
            // Read bytecode manually so we can inspect it before defineClass
            String resourceName = name.replace('.', '/') + ".class";
            URL resource = findResource(resourceName);
            if (resource == null) {
                throw new ClassNotFoundException(name);
            }
            try (InputStream in = resource.openStream()) {
                // Use bounded read when maxBytecodeSize is set to prevent OOM on oversized classes
                int maxSize = policy.maxBytecodeSize();
                byte[] bytecode;
                if (maxSize > 0) {
                    bytecode = in.readNBytes(maxSize + 1);
                } else {
                    bytecode = in.readAllBytes();
                }
                policy.checkBytecodeAllowed(name, bytecode);
                return defineClass(name, bytecode, 0, bytecode.length);
            } catch (IOException e) {
                throw new ClassNotFoundException("Failed to read class bytecode: " + name, e);
            }
        }
        return super.findClass(name);
    }

    /**
     * Force load class locally (from this classloader)
     * 强制从本加载器加载类
     *
     * @param name class name | 类名
     * @return loaded class | 加载的类
     * @throws ClassNotFoundException if class not found | 类未找到时抛出
     */
    public Class<?> loadClassLocally(String name) throws ClassNotFoundException {
        checkNotClosed();
        return findClass(name);
    }

    /**
     * Check if class can be loaded
     * 检查类是否可加载
     *
     * @param className class name | 类名
     * @return true if can load | 可加载返回 true
     */
    public boolean canLoad(String className) {
        try {
            loadClass(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // ==================== Resource Access | 资源访问 ====================

    @Override
    public URL getResource(String name) {
        checkNotClosed();
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        checkNotClosed();
        return super.getResources(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        checkNotClosed();
        return super.getResourceAsStream(name);
    }

    // ==================== Lifecycle | 生命周期 ====================

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            // Unregister from leak detection before closing
            if (leakDetection != LeakDetection.DISABLED) {
                LeakDetector.getInstance().untrack(this);
            }
            try {
                super.close();
            } catch (IOException e) {
                throw new OpenClassLoaderException("Failed to close classloader", e);
            }
            loadedClasses.clear();
        }
    }

    /**
     * Check if classloader is closed
     * 检查类加载器是否已关闭
     *
     * @return true if closed | 已关闭返回 true
     */
    public boolean isClosed() {
        return closed.get();
    }

    // ==================== Private Methods | 私有方法 ====================

    private void checkNotClosed() {
        if (closed.get()) {
            throw OpenClassLoaderException.classLoaderClosed();
        }
    }

    private boolean isSystemClass(String name) {
        return name.startsWith("java.") || name.startsWith("javax.") ||
               name.startsWith("sun.") || name.startsWith("jdk.");
    }

    private boolean isIsolatedPackage(String className) {
        for (String pkg : isolatedPackages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSharedPackage(String className) {
        for (String pkg : sharedPackages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private Class<?> loadParentFirst(String name) throws ClassNotFoundException {
        // Isolated packages take precedence: use child-first, but without
        // re-checking shared status to avoid infinite recursion
        if (isIsolatedPackage(name)) {
            try {
                return findClass(name);
            } catch (ClassNotFoundException e) {
                return loadFromParent(name);
            }
        }
        try {
            return loadFromParent(name);
        } catch (ClassNotFoundException e) {
            return findClass(name);
        }
    }

    private Class<?> loadChildFirst(String name) throws ClassNotFoundException {
        // Shared packages take precedence: use parent-first, but without
        // re-checking isolated status to avoid infinite recursion
        if (isSharedPackage(name)) {
            try {
                return loadFromParent(name);
            } catch (ClassNotFoundException e) {
                return findClass(name);
            }
        }
        try {
            return findClass(name);
        } catch (ClassNotFoundException e) {
            return loadFromParent(name);
        }
    }

    private Class<?> loadParentOnly(String name) throws ClassNotFoundException {
        return loadFromParent(name);
    }

    /**
     * Load class from parent classloader with null-safety.
     * If parent is null (bootstrap classloader), uses Class.forName with null loader.
     * 从父类加载器加载类（null 安全）。若父加载器为 null（引导类加载器），使用 Class.forName。
     */
    private Class<?> loadFromParent(String name) throws ClassNotFoundException {
        ClassLoader parent = getParent();
        if (parent != null) {
            return parent.loadClass(name);
        }
        // Bootstrap classloader — use Class.forName with null loader
        return Class.forName(name, false, null);
    }

    private Class<?> loadChildOnly(String name) throws ClassNotFoundException {
        return findClass(name);
    }

    /**
     * Get loaded class names
     * 获取已加载的类名
     *
     * @return set of loaded class names | 已加载的类名集合
     */
    public Set<String> getLoadedClassNames() {
        return Set.copyOf(loadedClasses.keySet());
    }

    /**
     * Get loader name
     * 获取加载器名称
     *
     * @return loader name | 加载器名称
     */
    public String getLoaderName() {
        return loaderName;
    }

    /**
     * Get leak detection level
     * 获取泄漏检测级别
     *
     * @return leak detection level | 泄漏检测级别
     */
    public LeakDetection getLeakDetection() {
        return leakDetection;
    }

    /**
     * Get class loading policy
     * 获取类加载策略
     *
     * @return class loading policy, or null if not set | 类加载策略，未设置则为 null
     */
    public ClassLoadingPolicy getPolicy() {
        return policy;
    }

    // ==================== Builder ====================

    /**
     * Builder for IsoClassLoader
     * IsoClassLoader 构建器
     */
    public static class Builder {
        private final List<URL> urls = new ArrayList<>();
        private final Set<String> isolatedPackages = new HashSet<>();
        private final Set<String> sharedPackages = new HashSet<>();
        private ClassLoader parent;
        private LoadingStrategy loadingStrategy = LoadingStrategy.CHILD_FIRST;
        private LeakDetection leakDetection = LeakDetection.DISABLED;
        private ClassLoadingPolicy policy;
        private String loaderName = "IsoClassLoader";

        /**
         * Add URL to classpath
         * 添加 URL 到 classpath
         *
         * @param url URL to add | 要添加的 URL
         * @return this builder | 此构建器
         */
        public Builder addUrl(URL url) {
            urls.add(url);
            return this;
        }

        /**
         * Add path to classpath
         * 添加路径到 classpath
         *
         * @param path path to add | 要添加的路径
         * @return this builder | 此构建器
         */
        public Builder addPath(Path path) {
            try {
                urls.add(path.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new OpenClassLoaderException("Invalid path: " + path, e);
            }
            return this;
        }

        /**
         * Add isolated package (loaded from this classloader)
         * 添加隔离包（从本加载器加载）
         *
         * @param packageName package name | 包名
         * @return this builder | 此构建器
         */
        public Builder addIsolatedPackage(String packageName) {
            isolatedPackages.add(packageName);
            return this;
        }

        /**
         * Add isolated packages
         * 添加多个隔离包
         *
         * @param packageNames package names | 包名数组
         * @return this builder | 此构建器
         */
        public Builder addIsolatedPackages(String... packageNames) {
            isolatedPackages.addAll(Arrays.asList(packageNames));
            return this;
        }

        /**
         * Add shared package (loaded from parent classloader)
         * 添加共享包（从父加载器加载）
         *
         * @param packageName package name | 包名
         * @return this builder | 此构建器
         */
        public Builder addSharedPackage(String packageName) {
            sharedPackages.add(packageName);
            return this;
        }

        /**
         * Add shared packages
         * 添加多个共享包
         *
         * @param packageNames package names | 包名数组
         * @return this builder | 此构建器
         */
        public Builder addSharedPackages(String... packageNames) {
            sharedPackages.addAll(Arrays.asList(packageNames));
            return this;
        }

        /**
         * Set parent classloader
         * 设置父类加载器
         *
         * @param parent parent classloader | 父类加载器
         * @return this builder | 此构建器
         */
        public Builder parent(ClassLoader parent) {
            this.parent = parent;
            return this;
        }

        /**
         * Set loading strategy
         * 设置加载策略
         *
         * @param strategy loading strategy | 加载策略
         * @return this builder | 此构建器
         */
        public Builder loadingStrategy(LoadingStrategy strategy) {
            this.loadingStrategy = strategy;
            return this;
        }

        /**
         * Set leak detection level
         * 设置泄漏检测级别
         *
         * @param leakDetection leak detection level | 泄漏检测级别
         * @return this builder | 此构建器
         */
        public Builder leakDetection(LeakDetection leakDetection) {
            this.leakDetection = Objects.requireNonNull(leakDetection, "LeakDetection must not be null");
            return this;
        }

        /**
         * Set class loading policy
         * 设置类加载策略
         *
         * @param policy class loading policy | 类加载策略
         * @return this builder | 此构建器
         */
        public Builder policy(ClassLoadingPolicy policy) {
            this.policy = Objects.requireNonNull(policy, "ClassLoadingPolicy must not be null");
            return this;
        }

        /**
         * Set loader name (used in leak detection reports)
         * 设置加载器名称（用于泄漏检测报告）
         *
         * @param name loader name | 加载器名称
         * @return this builder | 此构建器
         */
        public Builder name(String name) {
            this.loaderName = Objects.requireNonNull(name, "Name must not be null");
            return this;
        }

        /**
         * Build the IsoClassLoader
         * 构建 IsoClassLoader
         *
         * @return IsoClassLoader instance | IsoClassLoader 实例
         */
        public IsoClassLoader build() {
            if (urls.isEmpty()) {
                throw new OpenClassLoaderException("No URLs specified for IsoClassLoader");
            }
            ClassLoader parentLoader = parent != null ? parent : getClass().getClassLoader();
            return new IsoClassLoader(
                    urls.toArray(new URL[0]),
                    parentLoader,
                    isolatedPackages,
                    sharedPackages,
                    loadingStrategy,
                    leakDetection,
                    policy,
                    loaderName
            );
        }
    }
}
