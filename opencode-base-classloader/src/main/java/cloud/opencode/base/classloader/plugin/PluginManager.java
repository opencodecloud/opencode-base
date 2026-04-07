package cloud.opencode.base.classloader.plugin;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;
import cloud.opencode.base.classloader.leak.LeakDetection;
import cloud.opencode.base.classloader.loader.IsoClassLoader;
import cloud.opencode.base.classloader.security.ClassLoadingPolicy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Plugin lifecycle manager - Manages plugin discovery, loading, starting, stopping and unloading
 * 插件生命周期管理器 - 管理插件的发现、加载、启动、停止和卸载
 *
 * <p>Provides a complete plugin lifecycle management framework built on top of
 * {@link IsoClassLoader} for class isolation. Plugins are discovered from JAR
 * files containing {@code META-INF/opencode/plugin.properties} descriptors.</p>
 *
 * <p>基于 {@link IsoClassLoader} 提供完整的插件生命周期管理框架，实现类隔离。
 * 通过包含 {@code META-INF/opencode/plugin.properties} 描述符的 JAR 文件发现插件。</p>
 *
 * <p><strong>Lifecycle | 生命周期:</strong></p>
 * <pre>
 * discover &rarr; load &rarr; start &rarr; stop &rarr; unload
 *                                  \               /
 *                                   &larr; reload &larr;
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (PluginManager manager = PluginManager.builder()
 *         .pluginDir(Path.of("/plugins"))
 *         .leakDetection(LeakDetection.SIMPLE)
 *         .build()) {
 *
 *     List<PluginDescriptor> descriptors = manager.discoverPlugins();
 *     for (PluginDescriptor desc : descriptors) {
 *         manager.load(desc.id());
 *         manager.start(desc.id());
 *     }
 *     // ... use plugins ...
 * } // auto-closes all plugins
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap + synchronized transitions) -
 *       线程安全: 是 (ConcurrentHashMap + 同步状态转换)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public class PluginManager implements AutoCloseable {

    private static final String DESCRIPTOR_PATH = "META-INF/opencode/plugin.properties";

    private final Path pluginDir;
    private final IsoClassLoader.LoadingStrategy loadingStrategy;
    private final LeakDetection leakDetection;
    private final ClassLoadingPolicy policy;

    private final ConcurrentHashMap<String, PluginHandle> plugins = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PluginDescriptor> discovered = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private PluginManager(Builder builder) {
        this.pluginDir = builder.pluginDir;
        this.loadingStrategy = builder.loadingStrategy;
        this.leakDetection = builder.leakDetection;
        this.policy = builder.policy;
    }

    /**
     * Create a new Builder for PluginManager
     * 创建 PluginManager 的新 Builder
     *
     * @return a new builder instance | 新的构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Discovery | 发现 ====================

    /**
     * Discover plugins by scanning the plugin directory for JARs containing
     * {@code META-INF/opencode/plugin.properties}
     * 通过扫描插件目录中包含 {@code META-INF/opencode/plugin.properties} 的 JAR 来发现插件
     *
     * @return list of discovered plugin descriptors | 发现的插件描述符列表
     * @throws OpenClassLoaderException if the plugin directory cannot be read |
     *                                  当无法读取插件目录时
     * @throws IllegalStateException    if this manager is closed | 当管理器已关闭时
     */
    public List<PluginDescriptor> discoverPlugins() {
        checkNotClosed();

        if (!Files.isDirectory(pluginDir)) {
            throw new OpenClassLoaderException("Plugin directory does not exist: " + pluginDir);
        }

        List<PluginDescriptor> result = new ArrayList<>();
        // Use case-insensitive filter to match .jar/.JAR/.Jar on all platforms
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pluginDir,
                p -> p.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".jar"))) {
            for (Path jarPath : stream) {
                PluginDescriptor descriptor = readDescriptor(jarPath);
                if (descriptor != null) {
                    discovered.put(descriptor.id(), descriptor);
                    result.add(descriptor);
                }
            }
        } catch (IOException e) {
            throw new OpenClassLoaderException("Failed to scan plugin directory: " + pluginDir, e);
        }
        return Collections.unmodifiableList(result);
    }

    // ==================== Lifecycle | 生命周期 ====================

    /**
     * Load a discovered plugin: create an isolated ClassLoader and instantiate the Plugin
     * 加载已发现的插件：创建隔离的 ClassLoader 并实例化 Plugin
     *
     * <p><strong>Concurrency note:</strong> This method uses atomic {@code compute()} on the
     * internal plugin map, which may briefly block concurrent {@code load()} calls for the
     * <em>same</em> pluginId while the ClassLoader is being created. Calls for different
     * pluginIds are not affected.</p>
     * <p><strong>并发说明：</strong>此方法对内部插件 map 使用原子 {@code compute()} 操作，
     * 在创建 ClassLoader 期间可能短暂阻塞对<em>同一</em> pluginId 的并发 {@code load()} 调用。
     * 不同 pluginId 的调用不受影响。</p>
     *
     * @param pluginId the plugin identifier | 插件标识符
     * @return the plugin handle | 插件句柄
     * @throws OpenClassLoaderException if the plugin is not discovered, already loaded,
     *                                  or loading fails | 当插件未发现、已加载或加载失败时
     * @throws IllegalStateException    if this manager is closed | 当管理器已关闭时
     */
    public PluginHandle load(String pluginId) {
        checkNotClosed();
        Objects.requireNonNull(pluginId, "pluginId must not be null");

        PluginDescriptor descriptor = discovered.get(pluginId);
        if (descriptor == null) {
            throw new OpenClassLoaderException("Plugin not discovered: " + pluginId);
        }

        // Use compute() for atomic check-and-load to eliminate race conditions
        // between concurrent load() calls on the same pluginId.
        // Holder array to propagate exceptions out of the compute lambda.
        RuntimeException[] error = {null};

        PluginHandle result = plugins.compute(pluginId, (id, existing) -> {
            if (existing != null && existing.getState() != PluginState.UNLOADED
                    && existing.getState() != PluginState.FAILED) {
                error[0] = new OpenClassLoaderException("Plugin already loaded: " + id);
                return existing;
            }

            IsoClassLoader classLoader = null;
            try {
                IsoClassLoader.Builder clBuilder = IsoClassLoader.fromJar(descriptor.jarPath())
                        .name("plugin-" + id)
                        .loadingStrategy(loadingStrategy)
                        .leakDetection(leakDetection);
                if (policy != null) {
                    clBuilder.policy(policy);
                }
                classLoader = clBuilder.build();

                Class<?> mainClass = classLoader.loadClass(descriptor.mainClass());
                if (!Plugin.class.isAssignableFrom(mainClass)) {
                    throw new OpenClassLoaderException(
                            "Main class does not implement Plugin: " + descriptor.mainClass());
                }

                @SuppressWarnings("unchecked")
                Plugin plugin = (Plugin) mainClass.getDeclaredConstructor().newInstance();

                return new PluginHandle(id, descriptor, classLoader, plugin, PluginState.LOADED);
            } catch (OpenClassLoaderException e) {
                closeClassLoaderQuietly(classLoader);
                error[0] = e;
                return existing; // keep previous entry unchanged
            } catch (Exception e) {
                closeClassLoaderQuietly(classLoader);
                error[0] = new OpenClassLoaderException("Failed to load plugin: " + id, e);
                return existing; // keep previous entry unchanged
            }
        });

        if (error[0] != null) {
            throw error[0];
        }
        return result;
    }

    /**
     * Start a loaded plugin by calling {@link Plugin#onStart(PluginContext)}
     * 通过调用 {@link Plugin#onStart(PluginContext)} 启动已加载的插件
     *
     * @param pluginId the plugin identifier | 插件标识符
     * @throws OpenClassLoaderException if the plugin is not loaded or start fails |
     *                                  当插件未加载或启动失败时
     * @throws IllegalStateException    if this manager is closed | 当管理器已关闭时
     */
    public void start(String pluginId) {
        checkNotClosed();
        Objects.requireNonNull(pluginId, "pluginId must not be null");

        PluginHandle handle = requireHandle(pluginId);

        // Synchronize on handle to prevent concurrent start/stop on the same plugin
        synchronized (handle) {
            if (handle.getState() != PluginState.LOADED && handle.getState() != PluginState.STOPPED) {
                throw new OpenClassLoaderException(
                        "Plugin cannot be started in state " + handle.getState() + ": " + pluginId);
            }

            try {
                PluginContext context = new PluginContext(pluginId, handle.getDescriptor());
                handle.getPlugin().onStart(context);
                handle.setState(PluginState.STARTED);
            } catch (Exception e) {
                handle.setState(PluginState.FAILED);
                throw new OpenClassLoaderException("Plugin onStart failed: " + pluginId, e);
            }
        }
    }

    /**
     * Stop a running plugin by calling {@link Plugin#onStop()}
     * 通过调用 {@link Plugin#onStop()} 停止运行中的插件
     *
     * @param pluginId the plugin identifier | 插件标识符
     * @throws OpenClassLoaderException if the plugin is not started or stop fails |
     *                                  当插件未启动或停止失败时
     * @throws IllegalStateException    if this manager is closed | 当管理器已关闭时
     */
    public void stop(String pluginId) {
        checkNotClosed();
        Objects.requireNonNull(pluginId, "pluginId must not be null");

        PluginHandle handle = requireHandle(pluginId);

        // Synchronize on handle to prevent concurrent start/stop on the same plugin
        synchronized (handle) {
            if (handle.getState() != PluginState.STARTED) {
                throw new OpenClassLoaderException(
                        "Plugin cannot be stopped in state " + handle.getState() + ": " + pluginId);
            }

            try {
                handle.getPlugin().onStop();
                handle.setState(PluginState.STOPPED);
            } catch (Exception e) {
                handle.setState(PluginState.FAILED);
                throw new OpenClassLoaderException("Plugin onStop failed: " + pluginId, e);
            }
        }
    }

    /**
     * Unload a plugin: stop if running, then close its ClassLoader
     * 卸载插件：如果正在运行则停止，然后关闭其 ClassLoader
     *
     * @param pluginId the plugin identifier | 插件标识符
     * @throws OpenClassLoaderException if the plugin is not found |
     *                                  当未找到插件时
     * @throws IllegalStateException    if this manager is closed | 当管理器已关闭时
     */
    public void unload(String pluginId) {
        checkNotClosed();
        Objects.requireNonNull(pluginId, "pluginId must not be null");

        PluginHandle handle = requireHandle(pluginId);

        try {
            if (handle.getState() == PluginState.STARTED) {
                try {
                    handle.getPlugin().onStop();
                } catch (Exception e) {
                    // Best effort stop before unload
                }
            }
        } finally {
            try {
                handle.getClassLoader().close();
            } catch (Exception e) {
                // Best effort ClassLoader close
            }
            handle.setState(PluginState.UNLOADED);
            plugins.remove(pluginId);
        }
    }

    /**
     * Reload a plugin: unload, then load and start
     * 重新加载插件：卸载，然后加载并启动
     *
     * @param pluginId the plugin identifier | 插件标识符
     * @return the new plugin handle | 新的插件句柄
     * @throws OpenClassLoaderException if reload fails | 当重新加载失败时
     * @throws IllegalStateException    if this manager is closed | 当管理器已关闭时
     */
    public PluginHandle reload(String pluginId) {
        checkNotClosed();
        Objects.requireNonNull(pluginId, "pluginId must not be null");

        // Unload if currently loaded
        if (plugins.containsKey(pluginId)) {
            unload(pluginId);
        }

        PluginHandle handle = load(pluginId);
        start(pluginId);
        return handle;
    }

    // ==================== Query | 查询 ====================

    /**
     * Get a plugin handle by id
     * 通过 ID 获取插件句柄
     *
     * @param pluginId the plugin identifier | 插件标识符
     * @return optional plugin handle | 可选的插件句柄
     */
    public Optional<PluginHandle> getPlugin(String pluginId) {
        Objects.requireNonNull(pluginId, "pluginId must not be null");
        return Optional.ofNullable(plugins.get(pluginId));
    }

    /**
     * Get all loaded plugin handles
     * 获取所有已加载的插件句柄
     *
     * @return unmodifiable map of plugin id to handle | 不可修改的插件 ID 到句柄的映射
     */
    public Map<String, PluginHandle> getPlugins() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(plugins));
    }

    // ==================== AutoCloseable | 自动关闭 ====================

    /**
     * Close this manager, unloading all plugins
     * 关闭此管理器，卸载所有插件
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        for (String pluginId : List.copyOf(plugins.keySet())) {
            try {
                PluginHandle handle = plugins.get(pluginId);
                if (handle == null) {
                    continue;
                }
                try {
                    if (handle.getState() == PluginState.STARTED) {
                        handle.getPlugin().onStop();
                    }
                } catch (Exception e) {
                    // Best effort stop
                }
                try {
                    handle.getClassLoader().close();
                } catch (Exception e) {
                    // Best effort ClassLoader close
                }
                handle.setState(PluginState.UNLOADED);
            } catch (Exception e) {
                // Continue closing remaining plugins
            }
        }
        plugins.clear();
    }

    // ==================== Private Methods | 私有方法 ====================

    /**
     * Read plugin descriptor from a JAR file
     * 从 JAR 文件中读取插件描述符
     */
    private PluginDescriptor readDescriptor(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            ZipEntry entry = jarFile.getEntry(DESCRIPTOR_PATH);
            if (entry == null) {
                return null;
            }
            Properties props = new Properties();
            try (InputStream is = jarFile.getInputStream(entry)) {
                props.load(is);
            }

            String id = props.getProperty("plugin.id");
            String name = props.getProperty("plugin.name");
            String version = props.getProperty("plugin.version");
            String mainClass = props.getProperty("plugin.mainClass");

            if (id == null || name == null || version == null || mainClass == null) {
                return null;
            }

            return new PluginDescriptor(id, name, version, mainClass, jarPath);
        } catch (IOException e) {
            System.getLogger(PluginManager.class.getName())
                    .log(System.Logger.Level.WARNING, "Failed to read descriptor from JAR: " + jarPath, e);
            return null;
        }
    }

    private PluginHandle requireHandle(String pluginId) {
        PluginHandle handle = plugins.get(pluginId);
        if (handle == null) {
            throw new OpenClassLoaderException("Plugin not loaded: " + pluginId);
        }
        return handle;
    }

    private void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("PluginManager is closed");
        }
    }

    private static void closeClassLoaderQuietly(IsoClassLoader classLoader) {
        if (classLoader != null) {
            try {
                classLoader.close();
            } catch (Exception e) {
                // Quiet close
            }
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for PluginManager
     * PluginManager 构建器
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-classloader V1.0.3
     */
    public static class Builder {

        private Path pluginDir;
        private IsoClassLoader.LoadingStrategy loadingStrategy = IsoClassLoader.LoadingStrategy.CHILD_FIRST;
        private LeakDetection leakDetection = LeakDetection.DISABLED;
        private ClassLoadingPolicy policy;

        private Builder() {
        }

        /**
         * Set the plugin directory to scan for JAR files
         * 设置用于扫描 JAR 文件的插件目录
         *
         * @param pluginDir plugin directory path | 插件目录路径
         * @return this builder | 此构建器
         * @throws NullPointerException if pluginDir is null | 当 pluginDir 为 null 时
         */
        public Builder pluginDir(Path pluginDir) {
            this.pluginDir = Objects.requireNonNull(pluginDir, "pluginDir must not be null");
            return this;
        }

        /**
         * Set the class loading strategy for plugin ClassLoaders
         * 设置插件 ClassLoader 的类加载策略
         *
         * @param loadingStrategy the loading strategy | 加载策略
         * @return this builder | 此构建器
         * @throws NullPointerException if loadingStrategy is null | 当 loadingStrategy 为 null 时
         */
        public Builder loadingStrategy(IsoClassLoader.LoadingStrategy loadingStrategy) {
            this.loadingStrategy = Objects.requireNonNull(loadingStrategy,
                    "loadingStrategy must not be null");
            return this;
        }

        /**
         * Set the leak detection level for plugin ClassLoaders
         * 设置插件 ClassLoader 的泄漏检测级别
         *
         * @param leakDetection the leak detection level | 泄漏检测级别
         * @return this builder | 此构建器
         * @throws NullPointerException if leakDetection is null | 当 leakDetection 为 null 时
         */
        public Builder leakDetection(LeakDetection leakDetection) {
            this.leakDetection = Objects.requireNonNull(leakDetection,
                    "leakDetection must not be null");
            return this;
        }

        /**
         * Set the class loading policy for plugin ClassLoaders
         * 设置插件 ClassLoader 的类加载策略
         *
         * @param policy the class loading policy | 类加载策略
         * @return this builder | 此构建器
         * @throws NullPointerException if policy is null | 当 policy 为 null 时
         */
        public Builder policy(ClassLoadingPolicy policy) {
            this.policy = Objects.requireNonNull(policy, "policy must not be null");
            return this;
        }

        /**
         * Build the PluginManager
         * 构建 PluginManager
         *
         * @return a new PluginManager instance | 新的 PluginManager 实例
         * @throws OpenClassLoaderException if pluginDir is not set | 当 pluginDir 未设置时
         */
        public PluginManager build() {
            if (pluginDir == null) {
                throw new OpenClassLoaderException("pluginDir must be set");
            }
            // Normalize to prevent path traversal via symlinks or ..
            pluginDir = pluginDir.toAbsolutePath().normalize();
            return new PluginManager(this);
        }
    }
}
