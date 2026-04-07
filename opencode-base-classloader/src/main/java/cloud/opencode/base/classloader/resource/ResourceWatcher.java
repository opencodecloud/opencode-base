package cloud.opencode.base.classloader.resource;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Resource Watcher - Watches file resources for changes using WatchService
 * 资源监听器 - 使用 WatchService 监听文件资源变更
 *
 * <p>Monitors file system paths for create, modify, and delete events.
 * Uses a virtual thread daemon for the poll loop and provides debouncing
 * to coalesce rapid successive events on the same file.</p>
 * <p>监视文件系统路径的创建、修改和删除事件。使用虚拟线程守护进程进行轮询循环，
 * 并提供去抖动功能以合并同一文件上快速连续的事件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Watch individual files - 监听单个文件</li>
 *   <li>Watch directories with glob patterns - 使用 glob 模式监听目录</li>
 *   <li>Debouncing (100ms window) - 去抖动（100ms 窗口）</li>
 *   <li>Virtual thread poll loop - 虚拟线程轮询循环</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (ResourceWatcher watcher = new ResourceWatcher()) {
 *     ResourceWatchHandle handle = watcher.watch(Path.of("config.yml"), event -> {
 *         System.out.println(event.type() + ": " + event.resource().getFilename());
 *     });
 *     // ... later
 *     handle.close(); // stop watching
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public class ResourceWatcher implements AutoCloseable {

    private static final long DEBOUNCE_MILLIS = 100L;

    private final WatchService watchService;
    private final ConcurrentHashMap<Path, List<WatchEntry>> watchEntries = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Path, Long> lastEventTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Path, WatchKey> registeredDirs = new ConcurrentHashMap<>();
    private volatile boolean running = true;
    private final Thread pollThread;

    /**
     * Internal watch entry holding callback and optional pre-compiled glob matcher
     * 内部监听条目，保存回调和可选的预编译 glob 匹配器
     */
    private record WatchEntry(
            Path targetPath,
            PathMatcher compiledMatcher,
            Consumer<ResourceEvent> callback
    ) {
    }

    /**
     * Create a new ResourceWatcher
     * 创建新的 ResourceWatcher
     *
     * @throws IOException if cannot create WatchService | 无法创建 WatchService 时抛出
     */
    public ResourceWatcher() throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.pollThread = Thread.ofVirtual().name("resource-watcher-poll").start(this::pollLoop);
    }

    /**
     * Watch a file path for changes
     * 监听文件路径的变更
     *
     * @param filePath path to watch | 要监听的文件路径
     * @param callback event callback | 事件回调
     * @return closeable handle | 可关闭的句柄
     * @throws NullPointerException if filePath or callback is null | 如果 filePath 或 callback 为 null 则抛出
     */
    public ResourceWatchHandle watch(Path filePath, Consumer<ResourceEvent> callback) {
        Objects.requireNonNull(filePath, "File path must not be null");
        Objects.requireNonNull(callback, "Callback must not be null");

        Path absolutePath = filePath.toAbsolutePath().normalize();
        Path directory = absolutePath.getParent();
        if (directory == null) {
            throw new IllegalArgumentException("Cannot watch root path: " + filePath);
        }

        registerDirectory(directory);

        WatchEntry entry = new WatchEntry(absolutePath, null, callback);  // no glob matcher for file watch
        watchEntries.computeIfAbsent(directory, k -> new CopyOnWriteArrayList<>()).add(entry);

        return new ResourceWatchHandle(() -> {
            List<WatchEntry> entries = watchEntries.get(directory);
            if (entries != null) {
                entries.remove(entry);
            }
        });
    }

    /**
     * Watch a directory for changes matching a glob pattern
     * 监听目录中匹配 glob 模式的变更
     *
     * @param directory   directory to watch | 要监听的目录
     * @param globPattern glob pattern (e.g. "*.yml") | glob 模式（如 "*.yml"）
     * @param callback    event callback | 事件回调
     * @return closeable handle | 可关闭的句柄
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出
     */
    public ResourceWatchHandle watchPattern(Path directory, String globPattern, Consumer<ResourceEvent> callback) {
        Objects.requireNonNull(directory, "Directory must not be null");
        Objects.requireNonNull(globPattern, "Glob pattern must not be null");
        Objects.requireNonNull(callback, "Callback must not be null");

        Path absoluteDir = directory.toAbsolutePath().normalize();
        registerDirectory(absoluteDir);

        PathMatcher compiledMatcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
        WatchEntry entry = new WatchEntry(absoluteDir, compiledMatcher, callback);
        watchEntries.computeIfAbsent(absoluteDir, k -> new CopyOnWriteArrayList<>()).add(entry);

        return new ResourceWatchHandle(() -> {
            List<WatchEntry> entries = watchEntries.get(absoluteDir);
            if (entries != null) {
                entries.remove(entry);
            }
        });
    }

    /**
     * Get number of active watches
     * 获取活跃监听数量
     *
     * @return number of active watches | 活跃监听数量
     */
    public int getWatchCount() {
        return watchEntries.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Close the watcher, stopping the poll loop and releasing all resources
     * 关闭监听器，停止轮询循环并释放所有资源
     *
     * @throws IOException if closing fails | 关闭失败时抛出
     */
    @Override
    public void close() throws IOException {
        running = false;
        pollThread.interrupt();
        watchService.close();
        watchEntries.clear();
        registeredDirs.clear();
        lastEventTimes.clear();
    }

    /**
     * Register a directory with the watch service if not already registered
     * 如果尚未注册，则向监视服务注册目录
     */
    private void registerDirectory(Path directory) {
        registeredDirs.computeIfAbsent(directory, dir -> {
            try {
                return dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            } catch (IOException e) {
                throw new java.io.UncheckedIOException("Failed to register directory: " + dir, e);
            }
        });
    }

    /**
     * Poll loop running in a virtual daemon thread
     * 在虚拟守护线程中运行的轮询循环
     */
    private void pollLoop() {
        while (running) {
            try {
                WatchKey key = watchService.poll(500, TimeUnit.MILLISECONDS);
                if (key == null) {
                    continue;
                }

                Path directory = (Path) key.watchable();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path changedFile = directory.resolve(pathEvent.context()).toAbsolutePath().normalize();

                    // Debounce: skip if event for same file within debounce window
                    long now = System.currentTimeMillis();
                    Long lastTime = lastEventTimes.get(changedFile);
                    if (lastTime != null && (now - lastTime) < DEBOUNCE_MILLIS) {
                        continue;
                    }
                    // Clean up debounce entry for deleted files to prevent unbounded map growth;
                    // for other events, record timestamp for debouncing
                    if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        lastEventTimes.remove(changedFile);
                    } else {
                        lastEventTimes.put(changedFile, now);
                    }

                    ResourceEvent.Type eventType = mapEventType(kind);
                    if (eventType == null) {
                        continue;
                    }

                    FileResource resource = new FileResource(changedFile);
                    ResourceEvent resourceEvent = new ResourceEvent(eventType, resource, now);

                    // Notify matching watchers
                    List<WatchEntry> entries = watchEntries.get(directory);
                    if (entries != null) {
                        for (WatchEntry entry : entries) {
                            if (matchesEntry(entry, changedFile, directory)) {
                                try {
                                    entry.callback().accept(resourceEvent);
                                } catch (Exception e) {
                                    // Isolate listener exceptions — do not crash poll loop
                                }
                            }
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    registeredDirs.values().remove(key);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                break;
            }
        }
    }

    /**
     * Check if a watch entry matches the changed file
     * 检查监听条目是否匹配变更的文件
     */
    private boolean matchesEntry(WatchEntry entry, Path changedFile, Path directory) {
        if (entry.compiledMatcher() != null) {
            // Pattern-based watch: match against pre-compiled glob
            Path fileName = changedFile.getFileName();
            return fileName != null && entry.compiledMatcher().matches(fileName);
        } else {
            // File-specific watch: exact match
            return changedFile.equals(entry.targetPath());
        }
    }

    /**
     * Map WatchEvent kind to ResourceEvent type
     * 将 WatchEvent 类型映射到 ResourceEvent 类型
     */
    private static ResourceEvent.Type mapEventType(WatchEvent.Kind<?> kind) {
        if (kind == ENTRY_CREATE) {
            return ResourceEvent.Type.CREATED;
        } else if (kind == ENTRY_MODIFY) {
            return ResourceEvent.Type.MODIFIED;
        } else if (kind == ENTRY_DELETE) {
            return ResourceEvent.Type.DELETED;
        }
        return null;
    }
}
