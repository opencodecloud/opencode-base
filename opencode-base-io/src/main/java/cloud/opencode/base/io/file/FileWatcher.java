package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * File Watcher
 * 文件监听器
 *
 * <p>Watches a directory for file changes using Java NIO WatchService.
 * Supports monitoring create, modify, and delete events.</p>
 * <p>使用Java NIO WatchService监听目录的文件变化。
 * 支持监控创建、修改和删除事件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API - 流式API</li>
 *   <li>Event filtering - 事件过滤</li>
 *   <li>Pattern matching - 模式匹配</li>
 *   <li>Virtual thread support - 虚拟线程支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FileWatcher watcher = FileWatcher.watch(Path.of("/var/log"))
 *     .filter("*.log")
 *     .onModify(path -> System.out.println("Modified: " + path))
 *     .onCreate(path -> System.out.println("Created: " + path));
 *
 * watcher.start();
 * // ... do work ...
 * watcher.stop();
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
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class FileWatcher implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(FileWatcher.class.getName());

    private final Path watchPath;
    private final WatchService watchService;
    private PathMatcher pathMatcher;
    private Consumer<Path> onCreateHandler;
    private Consumer<Path> onModifyHandler;
    private Consumer<Path> onDeleteHandler;
    private Consumer<FileEvent> onAnyHandler;
    private Thread watchThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private FileWatcher(Path path) {
        this.watchPath = path;
        try {
            this.watchService = path.getFileSystem().newWatchService();
            path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
        } catch (IOException e) {
            throw OpenIOOperationException.watchFailed(path, e);
        }
    }

    /**
     * Creates a file watcher for the given path
     * 为给定路径创建文件监听器
     *
     * @param path the directory path to watch | 要监听的目录路径
     * @return file watcher | 文件监听器
     */
    public static FileWatcher watch(Path path) {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory: " + path);
        }
        return new FileWatcher(path);
    }

    /**
     * Creates a file watcher for the given path string
     * 为给定路径字符串创建文件监听器
     *
     * @param path the directory path string | 目录路径字符串
     * @return file watcher | 文件监听器
     */
    public static FileWatcher watch(String path) {
        return watch(Path.of(path));
    }

    /**
     * Sets file name filter pattern
     * 设置文件名过滤模式
     *
     * @param pattern glob pattern (e.g., "*.log") | glob模式
     * @return this | 当前对象
     */
    public FileWatcher filter(String pattern) {
        this.pathMatcher = watchPath.getFileSystem().getPathMatcher("glob:" + pattern);
        return this;
    }

    /**
     * Sets handler for create events
     * 设置创建事件处理器
     *
     * @param handler the handler | 处理器
     * @return this | 当前对象
     */
    public FileWatcher onCreate(Consumer<Path> handler) {
        this.onCreateHandler = handler;
        return this;
    }

    /**
     * Sets handler for modify events
     * 设置修改事件处理器
     *
     * @param handler the handler | 处理器
     * @return this | 当前对象
     */
    public FileWatcher onModify(Consumer<Path> handler) {
        this.onModifyHandler = handler;
        return this;
    }

    /**
     * Sets handler for delete events
     * 设置删除事件处理器
     *
     * @param handler the handler | 处理器
     * @return this | 当前对象
     */
    public FileWatcher onDelete(Consumer<Path> handler) {
        this.onDeleteHandler = handler;
        return this;
    }

    /**
     * Sets handler for any event
     * 设置任意事件处理器
     *
     * @param handler the handler | 处理器
     * @return this | 当前对象
     */
    public FileWatcher onAny(Consumer<FileEvent> handler) {
        this.onAnyHandler = handler;
        return this;
    }

    /**
     * Starts watching
     * 开始监听
     */
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        watchThread = Thread.ofVirtual().name("FileWatcher-" + watchPath).start(this::processEvents);
    }

    /**
     * Stops watching and releases OS file watch handles (inotify/kqueue).
     * 停止监听并释放操作系统文件监视句柄（inotify/kqueue）。
     */
    public void stop() {
        running.set(false);
        if (watchThread != null) {
            watchThread.interrupt();
            watchThread = null;
        }
        try {
            watchService.close();
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to close watch service for: " + watchPath, e);
        }
    }

    @Override
    public void close() {
        stop();
    }

    private void processEvents() {
        while (running.get()) {
            try {
                WatchKey key = watchService.poll(500, TimeUnit.MILLISECONDS);
                if (key == null) {
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path fileName = pathEvent.context();
                    Path fullPath = watchPath.resolve(fileName);

                    // Apply filter
                    if (pathMatcher != null && !pathMatcher.matches(fileName)) {
                        continue;
                    }

                    // Dispatch to handlers
                    dispatch(fullPath, kind);
                }

                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void dispatch(Path path, WatchEvent.Kind<?> kind) {
        // Specific handlers
        if (kind == StandardWatchEventKinds.ENTRY_CREATE && onCreateHandler != null) {
            onCreateHandler.accept(path);
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY && onModifyHandler != null) {
            onModifyHandler.accept(path);
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE && onDeleteHandler != null) {
            onDeleteHandler.accept(path);
        }

        // Any handler
        if (onAnyHandler != null) {
            onAnyHandler.accept(new FileEvent(path, kind));
        }
    }

    /**
     * Checks if watcher is running
     * 检查监听器是否正在运行
     *
     * @return true if running | 如果正在运行返回true
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Gets the watch path
     * 获取监听路径
     *
     * @return watch path | 监听路径
     */
    public Path getWatchPath() {
        return watchPath;
    }

    /**
     * File Event
     * 文件事件
     *
     * @param path the file path | 文件路径
     * @param kind the event kind | 事件类型
     */
    public record FileEvent(Path path, WatchEvent.Kind<?> kind) {

        /**
         * Checks if this is a create event
         * 检查是否为创建事件
         *
         * @return true if create | 如果是创建事件返回true
         */
        public boolean isCreate() {
            return kind == StandardWatchEventKinds.ENTRY_CREATE;
        }

        /**
         * Checks if this is a modify event
         * 检查是否为修改事件
         *
         * @return true if modify | 如果是修改事件返回true
         */
        public boolean isModify() {
            return kind == StandardWatchEventKinds.ENTRY_MODIFY;
        }

        /**
         * Checks if this is a delete event
         * 检查是否为删除事件
         *
         * @return true if delete | 如果是删除事件返回true
         */
        public boolean isDelete() {
            return kind == StandardWatchEventKinds.ENTRY_DELETE;
        }
    }
}
