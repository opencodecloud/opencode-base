package cloud.opencode.base.i18n.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Resource bundle with automatic reloading support
 * 支持自动重载的资源包
 *
 * <p>Extends resource bundle functionality with file watching and
 * automatic reloading capabilities.</p>
 * <p>扩展资源包功能，支持文件监听和自动重载能力。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>File watching - 文件监听</li>
 *   <li>Automatic reload - 自动重载</li>
 *   <li>Change notification - 变更通知</li>
 *   <li>Thread-safe access - 线程安全访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ReloadableResourceBundle bundle = new ReloadableResourceBundle(
 *     Path.of("/app/i18n/messages.properties"),
 *     Locale.CHINESE
 * );
 * bundle.startWatching(Duration.ofSeconds(30));
 * bundle.addReloadListener(b -> System.out.println("Bundle reloaded!"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public class ReloadableResourceBundle extends ResourceBundle implements AutoCloseable {

    private final Path filePath;
    private final Locale locale;
    private volatile Properties properties;
    private final AtomicBoolean watching = new AtomicBoolean(false);
    private final AtomicLong lastModified = new AtomicLong(0);
    private final List<Consumer<ReloadableResourceBundle>> reloadListeners = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService scheduler;

    /**
     * Creates a reloadable bundle from a file path
     * 从文件路径创建可重载资源包
     *
     * @param filePath the properties file path | 属性文件路径
     * @param locale   the locale | 地区
     */
    public ReloadableResourceBundle(Path filePath, Locale locale) {
        this.filePath = filePath;
        this.locale = locale;
        this.properties = new Properties();
        reload();
    }

    /**
     * Creates a reloadable bundle from classpath resource
     * 从类路径资源创建可重载资源包
     *
     * @param baseName the base name | 基础名称
     * @param locale   the locale | 地区
     * @return reloadable bundle | 可重载资源包
     */
    public static ReloadableResourceBundle fromClasspath(String baseName, Locale locale) {
        String resourceName = toResourceName(baseName, locale);
        var url = ReloadableResourceBundle.class.getClassLoader().getResource(resourceName);
        if (url != null) {
            try {
                Path path = Path.of(url.toURI());
                return new ReloadableResourceBundle(path, locale);
            } catch (Exception e) {
                throw new MissingResourceException(
                        "Cannot load resource: " + resourceName,
                        ReloadableResourceBundle.class.getName(),
                        baseName
                );
            }
        }
        throw new MissingResourceException(
                "Resource not found: " + resourceName,
                ReloadableResourceBundle.class.getName(),
                baseName
        );
    }

    @Override
    protected Object handleGetObject(String key) {
        return properties.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        Set<String> keys = new HashSet<>();
        for (Object key : properties.keySet()) {
            keys.add((String) key);
        }
        if (parent != null) {
            Enumeration<String> parentKeys = parent.getKeys();
            while (parentKeys.hasMoreElements()) {
                keys.add(parentKeys.nextElement());
            }
        }
        return Collections.enumeration(keys);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Reloads the properties from file
     * 从文件重载属性
     *
     * @return true if reloaded successfully | 如果重载成功返回true
     */
    public boolean reload() {
        if (!Files.exists(filePath)) {
            return false;
        }

        try {
            long currentModified = Files.getLastModifiedTime(filePath).toMillis();
            if (currentModified > lastModified.get()) {
                Properties newProps = new Properties();
                try (InputStream is = Files.newInputStream(filePath)) {
                    newProps.load(is);
                }
                properties = newProps;
                lastModified.set(currentModified);

                // Notify listeners
                for (Consumer<ReloadableResourceBundle> listener : reloadListeners) {
                    try {
                        listener.accept(this);
                    } catch (Exception e) {
                        // Ignore listener exceptions
                    }
                }
                return true;
            }
        } catch (IOException e) {
            // Ignore reload failures
        }
        return false;
    }

    /**
     * Starts watching for file changes
     * 开始监听文件变更
     *
     * @param checkInterval the check interval | 检查间隔
     */
    public void startWatching(Duration checkInterval) {
        if (watching.compareAndSet(false, true)) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "reloadable-bundle-watcher");
                t.setDaemon(true);
                return t;
            });

            scheduler.scheduleWithFixedDelay(
                    this::reload,
                    checkInterval.toMillis(),
                    checkInterval.toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * Stops watching for file changes
     * 停止监听文件变更
     */
    public void stopWatching() {
        if (watching.compareAndSet(true, false)) {
            if (scheduler != null) {
                scheduler.shutdown();
                scheduler = null;
            }
        }
    }

    /**
     * Adds a reload listener
     * 添加重载监听器
     *
     * @param listener the listener | 监听器
     */
    public void addReloadListener(Consumer<ReloadableResourceBundle> listener) {
        reloadListeners.add(listener);
    }

    /**
     * Removes a reload listener
     * 移除重载监听器
     *
     * @param listener the listener | 监听器
     */
    public void removeReloadListener(Consumer<ReloadableResourceBundle> listener) {
        reloadListeners.remove(listener);
    }

    /**
     * Closes this bundle and stops watching for file changes.
     * 关闭此资源包并停止监听文件变更。
     *
     * <p>Delegates to {@link #stopWatching()}. Enables try-with-resources usage.</p>
     * <p>委托给 {@link #stopWatching()}。支持 try-with-resources 使用。</p>
     */
    @Override
    public void close() {
        stopWatching();
    }

    /**
     * Checks if watching for changes
     * 检查是否正在监听变更
     *
     * @return true if watching | 如果正在监听返回true
     */
    public boolean isWatching() {
        return watching.get();
    }

    /**
     * Gets the file path
     * 获取文件路径
     *
     * @return file path | 文件路径
     */
    public Path getFilePath() {
        return filePath;
    }

    /**
     * Gets the last modified time
     * 获取最后修改时间
     *
     * @return last modified timestamp | 最后修改时间戳
     */
    public long getLastModified() {
        return lastModified.get();
    }

    private static String toResourceName(String baseName, Locale locale) {
        StringBuilder sb = new StringBuilder(baseName.replace('.', '/'));
        if (!locale.equals(Locale.ROOT)) {
            sb.append('_').append(locale.getLanguage());
            if (!locale.getCountry().isEmpty()) {
                sb.append('_').append(locale.getCountry());
            }
            if (!locale.getVariant().isEmpty()) {
                sb.append('_').append(locale.getVariant());
            }
        }
        sb.append(".properties");
        return sb.toString();
    }
}
