package cloud.opencode.base.config.internal;

import cloud.opencode.base.config.ConfigChangeEvent;
import cloud.opencode.base.config.ConfigListener;
import cloud.opencode.base.config.source.CompositeConfigSource;
import cloud.opencode.base.config.source.ConfigSource;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Configuration File Watcher
 * 配置文件监视器
 *
 * <p>Monitors configuration sources for changes and notifies listeners using virtual threads.</p>
 * <p>监视配置源的变更并使用虚拟线程通知监听器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Periodic change detection - 定期变更检测</li>
 *   <li>Virtual thread execution - 虚拟线程执行</li>
 *   <li>Automatic reload on change - 变更时自动重载</li>
 *   <li>Thread-safe listener notification - 线程安全的监听器通知</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ConfigWatcher watcher = new ConfigWatcher(Duration.ofSeconds(5));
 *
 * watcher.addListener(event -> {
 *     System.out.println("Config changed: " + event.key());
 * });
 *
 * watcher.watch(configSource);
 * watcher.start();
 *
 * // Later...
 * watcher.close();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Check interval configurable - 检查间隔可配置</li>
 *   <li>Virtual threads for low overhead - 虚拟线程降低开销</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Auto-closeable - 自动关闭</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class ConfigWatcher implements AutoCloseable {

    private final Duration checkInterval;
    private final CopyOnWriteArrayList<ConfigListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<ConfigSource, Map<String, String>> lastSnapshots = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;
    private ExecutorService notificationExecutor;
    private CompositeConfigSource watchedSource;

    /**
     * Create config watcher with check interval
     * 创建带检查间隔的配置监视器
     *
     * @param checkInterval check interval | 检查间隔
     */
    public ConfigWatcher(Duration checkInterval) {
        this.checkInterval = checkInterval;
    }

    /**
     * Watch configuration source
     * 监视配置源
     *
     * @param source configuration source | 配置源
     */
    public void watch(CompositeConfigSource source) {
        this.watchedSource = source;
        for (ConfigSource s : source.getSources()) {
            if (s.supportsReload()) {
                lastSnapshots.put(s, new HashMap<>(s.getProperties()));
            }
        }
    }

    /**
     * Add change listener
     * 添加变更监听器
     *
     * @param listener configuration listener | 配置监听器
     */
    public void addListener(ConfigListener listener) {
        listeners.add(listener);
    }

    /**
     * Start watching
     * 开始监视
     */
    public void start() {
        if (this.scheduler != null) {
            return;
        }
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = Thread.ofVirtual().unstarted(r);
            t.setName("config-watcher");
            return t;
        });

        // Single-threaded executor for sequential notification delivery
        this.notificationExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = Thread.ofVirtual().unstarted(r);
            t.setName("config-notification");
            return t;
        });

        scheduler.scheduleAtFixedRate(
            this::checkChanges,
            checkInterval.toMillis(),
            checkInterval.toMillis(),
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Check for configuration changes
     * 检查配置变更
     */
    private void checkChanges() {
        for (var entry : lastSnapshots.entrySet()) {
            ConfigSource source = entry.getKey();
            Map<String, String> lastSnapshot = entry.getValue();

            try {
                // Reload source
                source.reload();

                // Get current properties
                Map<String, String> currentProps = source.getProperties();

                // Detect changes
                var events = detectChanges(lastSnapshot, currentProps);

                // Notify listeners
                events.forEach(this::notifyListeners);

                // Update snapshot
                lastSnapshots.put(source, new HashMap<>(currentProps));

            } catch (Exception e) {
                System.getLogger(ConfigWatcher.class.getName())
                        .log(System.Logger.Level.WARNING,
                                "Failed to check config changes for source: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Detect changes between snapshots
     * 检测快照之间的变更
     */
    private java.util.List<ConfigChangeEvent> detectChanges(
            Map<String, String> oldProps,
            Map<String, String> newProps) {

        java.util.List<ConfigChangeEvent> events = new java.util.ArrayList<>();

        // Check for added or modified
        for (var entry : newProps.entrySet()) {
            String key = entry.getKey();
            String newValue = entry.getValue();
            String oldValue = oldProps.get(key);

            if (oldValue == null) {
                events.add(ConfigChangeEvent.added(key, newValue));
            } else if (!Objects.equals(oldValue, newValue)) {
                events.add(ConfigChangeEvent.modified(key, oldValue, newValue));
            }
        }

        // Check for removed
        for (String key : oldProps.keySet()) {
            if (!newProps.containsKey(key)) {
                events.add(ConfigChangeEvent.removed(key, oldProps.get(key)));
            }
        }

        return events;
    }

    /**
     * Notify listeners of change sequentially
     * 顺序通知监听器变更
     */
    private void notifyListeners(ConfigChangeEvent event) {
        // Submit to single-threaded executor for sequential delivery
        if (notificationExecutor != null && !notificationExecutor.isShutdown()) {
            notificationExecutor.execute(() -> {
                for (ConfigListener listener : listeners) {
                    try {
                        listener.onConfigChange(event);
                    } catch (Exception e) {
                        System.getLogger(ConfigWatcher.class.getName())
                                .log(System.Logger.Level.WARNING, "Config change listener threw exception", e);
                    }
                }
            });
        }
    }

    @Override
    public void close() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        if (notificationExecutor != null) {
            notificationExecutor.shutdown();
            try {
                if (!notificationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    notificationExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                notificationExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
