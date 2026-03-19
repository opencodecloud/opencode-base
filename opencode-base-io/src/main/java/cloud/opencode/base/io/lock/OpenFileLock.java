package cloud.opencode.base.io.lock;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.*;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * File Locking Utility Class
 * 文件锁定工具类
 *
 * <p>Provides file locking utilities for cross-process synchronization
 * using NIO FileChannel locks.</p>
 * <p>提供基于 NIO FileChannel 锁的跨进程同步文件锁定工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exclusive and shared locks - 独占锁和共享锁</li>
 *   <li>Try-lock with timeout - 带超时的尝试锁定</li>
 *   <li>Lock file pattern for coordination - 锁文件模式用于协调</li>
 *   <li>AutoCloseable for try-with-resources - AutoCloseable 支持 try-with-resources</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Exclusive lock with try-with-resources
 * try (var lock = OpenFileLock.lock(path)) {
 *     // Exclusive access to file
 * }
 *
 * // Shared (read) lock
 * try (var lock = OpenFileLock.lockShared(path)) {
 *     // Shared read access
 * }
 *
 * // Try lock with timeout
 * Optional<FileLockHandle> lock = OpenFileLock.tryLock(path, Duration.ofSeconds(5));
 * if (lock.isPresent()) {
 *     try (var handle = lock.get()) {
 *         // Got the lock
 *     }
 * }
 *
 * // Execute with lock
 * String result = OpenFileLock.withLock(path, () -> {
 *     return readAndProcessFile(path);
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, uses NIO FileChannel locks for cross-process safety - 线程安全: 是，使用NIO FileChannel锁实现跨进程安全</li>
 *   <li>Null-safe: No, path must not be null - 空值安全: 否，路径不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class OpenFileLock {

    /**
     * Default lock file suffix
     * 默认锁文件后缀
     */
    public static final String LOCK_FILE_SUFFIX = ".lock";

    /**
     * Default retry interval for try-lock
     * 尝试锁定的默认重试间隔
     */
    private static final Duration DEFAULT_RETRY_INTERVAL = Duration.ofMillis(50);

    private OpenFileLock() {
    }

    // ==================== Exclusive Lock | 独占锁 ====================

    /**
     * Acquire exclusive lock on file
     * 获取文件的独占锁
     *
     * @param path the file path | 文件路径
     * @return lock handle | 锁句柄
     * @throws OpenIOOperationException if lock cannot be acquired | 如果无法获取锁
     */
    public static FileLockHandle lock(Path path) {
        return lock(path, false);
    }

    /**
     * Acquire lock on file
     * 获取文件锁
     *
     * @param path the file path | 文件路径
     * @param shared true for shared lock, false for exclusive | true 为共享锁，false 为独占锁
     * @return lock handle | 锁句柄
     * @throws OpenIOOperationException if lock cannot be acquired | 如果无法获取锁
     */
    public static FileLockHandle lock(Path path, boolean shared) {
        try {
            ensureFileExists(path);
            FileChannel channel = FileChannel.open(path,
                shared ? StandardOpenOption.READ : StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
            FileLock fileLock = channel.lock(0, Long.MAX_VALUE, shared);
            return new FileLockHandle(path, channel, fileLock, shared);
        } catch (IOException e) {
            throw new OpenIOOperationException("lock", path.toString(),
                "Failed to acquire " + (shared ? "shared" : "exclusive") + " lock on: " + path, e);
        }
    }

    /**
     * Acquire shared (read) lock on file
     * 获取文件的共享（读）锁
     *
     * @param path the file path | 文件路径
     * @return lock handle | 锁句柄
     * @throws OpenIOOperationException if lock cannot be acquired | 如果无法获取锁
     */
    public static FileLockHandle lockShared(Path path) {
        return lock(path, true);
    }

    // ==================== Try Lock | 尝试锁定 ====================

    /**
     * Try to acquire exclusive lock without blocking
     * 尝试获取独占锁（不阻塞）
     *
     * @param path the file path | 文件路径
     * @return lock handle if acquired, empty otherwise | 如果获取成功返回锁句柄，否则返回空
     */
    public static Optional<FileLockHandle> tryLock(Path path) {
        return tryLock(path, false);
    }

    /**
     * Try to acquire lock without blocking
     * 尝试获取锁（不阻塞）
     *
     * @param path the file path | 文件路径
     * @param shared true for shared lock | true 为共享锁
     * @return lock handle if acquired, empty otherwise | 如果获取成功返回锁句柄，否则返回空
     */
    public static Optional<FileLockHandle> tryLock(Path path, boolean shared) {
        try {
            ensureFileExists(path);
            FileChannel channel = FileChannel.open(path,
                shared ? StandardOpenOption.READ : StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
            FileLock fileLock = channel.tryLock(0, Long.MAX_VALUE, shared);
            if (fileLock != null) {
                return Optional.of(new FileLockHandle(path, channel, fileLock, shared));
            } else {
                channel.close();
                return Optional.empty();
            }
        } catch (OverlappingFileLockException e) {
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Try to acquire lock with timeout
     * 尝试在超时时间内获取锁
     *
     * @param path the file path | 文件路径
     * @param timeout maximum wait time | 最大等待时间
     * @return lock handle if acquired, empty otherwise | 如果获取成功返回锁句柄，否则返回空
     */
    public static Optional<FileLockHandle> tryLock(Path path, Duration timeout) {
        return tryLock(path, timeout, false);
    }

    /**
     * Try to acquire lock with timeout
     * 尝试在超时时间内获取锁
     *
     * @param path the file path | 文件路径
     * @param timeout maximum wait time | 最大等待时间
     * @param shared true for shared lock | true 为共享锁
     * @return lock handle if acquired, empty otherwise | 如果获取成功返回锁句柄，否则返回空
     */
    public static Optional<FileLockHandle> tryLock(Path path, Duration timeout, boolean shared) {
        long deadline = System.nanoTime() + timeout.toNanos();
        long retryNanos = DEFAULT_RETRY_INTERVAL.toNanos();

        while (System.nanoTime() < deadline) {
            Optional<FileLockHandle> lock = tryLock(path, shared);
            if (lock.isPresent()) {
                return lock;
            }
            try {
                TimeUnit.NANOSECONDS.sleep(Math.min(retryNanos, deadline - System.nanoTime()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    // ==================== Lock File Pattern | 锁文件模式 ====================

    /**
     * Create a lock file for coordination
     * 创建用于协调的锁文件
     *
     * @param path the resource path to protect | 要保护的资源路径
     * @return lock handle for the lock file | 锁文件的锁句柄
     */
    public static FileLockHandle lockFile(Path path) {
        Path lockPath = getLockFilePath(path);
        return lock(lockPath);
    }

    /**
     * Try to create a lock file with timeout
     * 尝试在超时时间内创建锁文件
     *
     * @param path the resource path to protect | 要保护的资源路径
     * @param timeout maximum wait time | 最大等待时间
     * @return lock handle if acquired, empty otherwise | 如果获取成功返回锁句柄，否则返回空
     */
    public static Optional<FileLockHandle> tryLockFile(Path path, Duration timeout) {
        Path lockPath = getLockFilePath(path);
        return tryLock(lockPath, timeout);
    }

    /**
     * Get lock file path for a resource
     * 获取资源的锁文件路径
     *
     * @param path the resource path | 资源路径
     * @return lock file path | 锁文件路径
     */
    public static Path getLockFilePath(Path path) {
        return path.resolveSibling(path.getFileName() + LOCK_FILE_SUFFIX);
    }

    // ==================== Execute with Lock | 带锁执行 ====================

    /**
     * Execute action with exclusive lock
     * 持有独占锁执行操作
     *
     * @param path the file path | 文件路径
     * @param action action to execute | 要执行的操作
     * @param <T> return type | 返回类型
     * @return action result | 操作结果
     */
    public static <T> T withLock(Path path, Supplier<T> action) {
        try (FileLockHandle lock = lock(path)) {
            return action.get();
        }
    }

    /**
     * Execute action with exclusive lock (no return value)
     * 持有独占锁执行操作（无返回值）
     *
     * @param path the file path | 文件路径
     * @param action action to execute | 要执行的操作
     */
    public static void withLock(Path path, Runnable action) {
        try (FileLockHandle lock = lock(path)) {
            action.run();
        }
    }

    /**
     * Execute action with shared lock
     * 持有共享锁执行操作
     *
     * @param path the file path | 文件路径
     * @param action action to execute | 要执行的操作
     * @param <T> return type | 返回类型
     * @return action result | 操作结果
     */
    public static <T> T withSharedLock(Path path, Supplier<T> action) {
        try (FileLockHandle lock = lockShared(path)) {
            return action.get();
        }
    }

    /**
     * Try to execute action with lock, with timeout
     * 尝试在超时时间内持有锁执行操作
     *
     * @param path the file path | 文件路径
     * @param timeout maximum wait time | 最大等待时间
     * @param action action to execute | 要执行的操作
     * @param <T> return type | 返回类型
     * @return action result if lock acquired, empty otherwise | 如果获取锁返回结果，否则返回空
     */
    public static <T> Optional<T> tryWithLock(Path path, Duration timeout, Supplier<T> action) {
        Optional<FileLockHandle> lock = tryLock(path, timeout);
        if (lock.isPresent()) {
            try (FileLockHandle handle = lock.get()) {
                return Optional.of(action.get());
            }
        }
        return Optional.empty();
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Check if file is locked
     * 检查文件是否被锁定
     *
     * @param path the file path | 文件路径
     * @return true if locked by another process | 如果被其他进程锁定返回 true
     */
    public static boolean isLocked(Path path) {
        return tryLock(path).map(lock -> {
            lock.close();
            return false;
        }).orElse(true);
    }

    /**
     * Check if file is locked with shared lock
     * 检查文件是否被共享锁定
     *
     * @param path the file path | 文件路径
     * @return true if exclusively locked | 如果被独占锁定返回 true
     */
    public static boolean isExclusivelyLocked(Path path) {
        return tryLock(path, true).map(lock -> {
            lock.close();
            return false;
        }).orElse(true);
    }

    private static void ensureFileExists(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try {
            Files.createFile(path);
        } catch (FileAlreadyExistsException ignored) {
            // File was created by another thread/process between our checks
        }
    }

    // ==================== File Lock Handle | 文件锁句柄 ====================

    /**
     * File Lock Handle
     * 文件锁句柄
     *
     * <p>AutoCloseable handle for file locks. Use with try-with-resources.</p>
     * <p>文件锁的 AutoCloseable 句柄，配合 try-with-resources 使用。</p>
     */
    public static class FileLockHandle implements Closeable {
        private final Path path;
        private final FileChannel channel;
        private final FileLock lock;
        private final boolean shared;
        private volatile boolean closed = false;

        FileLockHandle(Path path, FileChannel channel, FileLock lock, boolean shared) {
            this.path = path;
            this.channel = channel;
            this.lock = lock;
            this.shared = shared;
        }

        /**
         * Get the locked file path
         * 获取锁定的文件路径
         *
         * @return file path | 文件路径
         */
        public Path getPath() {
            return path;
        }

        /**
         * Check if this is a shared lock
         * 检查是否为共享锁
         *
         * @return true if shared lock | 如果是共享锁返回 true
         */
        public boolean isShared() {
            return shared;
        }

        /**
         * Check if lock is still valid
         * 检查锁是否仍然有效
         *
         * @return true if valid | 如果有效返回 true
         */
        public boolean isValid() {
            return !closed && lock.isValid();
        }

        /**
         * Get the underlying FileChannel
         * 获取底层 FileChannel
         *
         * @return file channel | 文件通道
         */
        public FileChannel getChannel() {
            return channel;
        }

        /**
         * Get the underlying FileLock
         * 获取底层 FileLock
         *
         * @return file lock | 文件锁
         */
        public FileLock getLock() {
            return lock;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            try {
                if (lock.isValid()) {
                    lock.release();
                }
            } catch (IOException ignored) {
            }
            try {
                channel.close();
            } catch (IOException ignored) {
            }
        }

        @Override
        public String toString() {
            return String.format("FileLockHandle[path=%s, shared=%s, valid=%s]",
                path, shared, isValid());
        }
    }
}
