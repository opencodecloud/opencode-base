package cloud.opencode.base.i18n.provider;

import cloud.opencode.base.i18n.spi.MessageProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Reloadable message provider with file watching
 * 带文件监听的可重载消息提供者
 *
 * <p>Watches for file changes and automatically reloads messages.</p>
 * <p>监听文件变更并自动重载消息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>File change detection - 文件变更检测</li>
 *   <li>Automatic reload - 自动重载</li>
 *   <li>Reload listeners - 重载监听器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ReloadableMessageProvider provider = new ReloadableMessageProvider(
 *     Path.of("/app/i18n"),
 *     Duration.ofSeconds(30)
 * );
 * provider.startAutoReload();
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
public class ReloadableMessageProvider implements MessageProvider, AutoCloseable {

    private static final System.Logger LOGGER =
            System.getLogger(ReloadableMessageProvider.class.getName());

    private final Path resourcePath;
    private final Duration checkInterval;
    private final Map<Locale, Properties> propertiesCache = new ConcurrentHashMap<>();
    private final List<Consumer<Set<Locale>>> reloadListeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean watching = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;
    private WatchService watchService;

    /**
     * Creates a reloadable provider
     * 创建可重载提供者
     *
     * @param path the resource directory path | 资源目录路径
     */
    public ReloadableMessageProvider(Path path) {
        this(path, Duration.ofSeconds(30));
    }

    /**
     * Creates a reloadable provider with custom interval
     * 使用自定义间隔创建可重载提供者
     *
     * @param path          the resource directory path | 资源目录路径
     * @param checkInterval the check interval | 检查间隔
     */
    public ReloadableMessageProvider(Path path, Duration checkInterval) {
        this.resourcePath = path;
        this.checkInterval = checkInterval;
        loadAllProperties();
    }

    @Override
    public Optional<String> getMessageTemplate(String key, Locale locale) {
        Properties props = propertiesCache.get(locale);
        if (props != null) {
            String value = props.getProperty(key);
            if (value != null) {
                return Optional.of(value);
            }
        }

        // Try language only
        if (!locale.getCountry().isEmpty()) {
            Locale languageOnly = Locale.of(locale.getLanguage());
            props = propertiesCache.get(languageOnly);
            if (props != null) {
                String value = props.getProperty(key);
                if (value != null) {
                    return Optional.of(value);
                }
            }
        }

        // Try root
        props = propertiesCache.get(Locale.ROOT);
        if (props != null) {
            String value = props.getProperty(key);
            if (value != null) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

    @Override
    public Set<String> getKeys(Locale locale) {
        Properties props = propertiesCache.get(locale);
        if (props != null) {
            Set<String> keys = new HashSet<>();
            for (Object key : props.keySet()) {
                keys.add((String) key);
            }
            return keys;
        }
        return Set.of();
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return new HashSet<>(propertiesCache.keySet());
    }

    @Override
    public void refresh() {
        propertiesCache.clear();
        loadAllProperties();
    }

    /**
     * Starts auto reload
     * 启动自动重载
     */
    public void startAutoReload() {
        if (watching.compareAndSet(false, true)) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "i18n-reloader");
                t.setDaemon(true);
                return t;
            });

            scheduler.scheduleWithFixedDelay(
                    this::checkAndReload,
                    checkInterval.toMillis(),
                    checkInterval.toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * Stops auto reload
     * 停止自动重载
     */
    public void stopAutoReload() {
        if (watching.compareAndSet(true, false)) {
            if (scheduler != null) {
                scheduler.shutdown();
                scheduler = null;
            }
            if (watchService != null) {
                try {
                    watchService.close();
                } catch (IOException ignored) {
                }
                watchService = null;
            }
        }
    }

    /**
     * Adds a reload listener
     * 添加重载监听器
     *
     * @param listener the listener | 监听器
     */
    public void addReloadListener(Consumer<Set<Locale>> listener) {
        reloadListeners.add(listener);
    }

    /**
     * Closes this provider and stops auto reload.
     * 关闭此提供者并停止自动重载。
     *
     * <p>Delegates to {@link #stopAutoReload()}. Enables try-with-resources usage.</p>
     * <p>委托给 {@link #stopAutoReload()}。支持 try-with-resources 使用。</p>
     */
    @Override
    public void close() {
        stopAutoReload();
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

    private void loadAllProperties() {
        if (!Files.isDirectory(resourcePath)) {
            return;
        }

        try (var stream = Files.list(resourcePath)) {
            stream.filter(p -> p.toString().endsWith(".properties"))
                    .forEach(this::loadPropertiesFile);
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to load properties from directory: " + resourcePath, e);
        }
    }

    private void loadPropertiesFile(Path file) {
        String filename = file.getFileName().toString();
        Locale locale = parseLocaleFromFilename(filename);

        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(file)) {
            props.load(is);
            propertiesCache.put(locale, props);
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to load properties file: " + file, e);
        }
    }

    private Locale parseLocaleFromFilename(String filename) {
        // messages_zh_CN.properties -> zh_CN
        // messages_en.properties -> en
        // messages.properties -> ROOT
        String name = filename.replace(".properties", "");
        int underscoreIdx = name.indexOf('_');
        if (underscoreIdx < 0) {
            return Locale.ROOT;
        }

        String localeStr = name.substring(underscoreIdx + 1);
        String[] parts = localeStr.split("_");
        if (parts.length == 1) {
            return Locale.of(parts[0]);
        } else if (parts.length == 2) {
            return Locale.of(parts[0], parts[1]);
        } else if (parts.length >= 3) {
            return Locale.of(parts[0], parts[1], parts[2]);
        }
        return Locale.ROOT;
    }

    private void checkAndReload() {
        Set<Locale> reloaded = new HashSet<>();
        try (var stream = Files.list(resourcePath)) {
            stream.filter(p -> p.toString().endsWith(".properties"))
                    .forEach(p -> {
                        Locale locale = parseLocaleFromFilename(p.getFileName().toString());
                        loadPropertiesFile(p);
                        reloaded.add(locale);
                    });
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to reload properties from directory: " + resourcePath, e);
        }

        if (!reloaded.isEmpty()) {
            for (Consumer<Set<Locale>> listener : reloadListeners) {
                try {
                    listener.accept(reloaded);
                } catch (Exception e) {
                    // Ignore listener exceptions
                }
            }
        }
    }
}
