package cloud.opencode.base.classloader.resource;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Nested JAR Handler - Parses and manages inner JARs embedded in fat JARs
 * 嵌套 JAR 处理器 - 解析和管理嵌入在 fat JAR（如 Spring Boot）中的内层 JAR
 *
 * <p>Supports discovering and extracting nested JARs from common fat JAR layouts
 * including BOOT-INF/lib/, WEB-INF/lib/, and lib/ directories.</p>
 * <p>支持从常见的 fat JAR 布局中发现和解压嵌套 JAR，包括 BOOT-INF/lib/、WEB-INF/lib/ 和 lib/ 目录。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Discover nested JARs in fat JARs - 在 fat JAR 中发现嵌套 JAR</li>
 *   <li>Extract nested JARs to temporary files - 解压嵌套 JAR 到临时文件</li>
 *   <li>Reference counting for shared access - 共享访问的引用计数</li>
 *   <li>Automatic cleanup on close - 关闭时自动清理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (NestedJarHandler handler = NestedJarHandler.builder().build()) {
 *     List<String> nested = handler.findNestedJars(fatJarPath);
 *     Path extracted = handler.extractNestedJar(fatJarPath, nested.get(0));
 *     // use extracted JAR...
 *     handler.release(fatJarPath, nested.get(0));
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Path traversal protection via normalize + startsWith check - 通过 normalize + startsWith 检查防止路径穿越</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public class NestedJarHandler implements AutoCloseable {

    private final Path tempDirectory;
    private final ConcurrentHashMap<String, ExtractedEntry> extractedEntries = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // ==================== Constructor (private — use Builder) ====================

    private NestedJarHandler(Path tempDirectory) {
        this.tempDirectory = Objects.requireNonNull(tempDirectory, "tempDirectory must not be null");
    }

    // ==================== Builder ====================

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return builder instance | 构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for NestedJarHandler
     * NestedJarHandler 的构建器
     */
    public static final class Builder {

        private Path tempDirectory;

        private Builder() {
        }

        /**
         * Set the temporary directory for extracted JARs
         * 设置解压 JAR 的临时目录
         *
         * @param tempDirectory temporary directory path | 临时目录路径
         * @return this builder | 此构建器
         */
        public Builder tempDirectory(Path tempDirectory) {
            this.tempDirectory = tempDirectory;
            return this;
        }

        /**
         * Build the handler
         * 构建处理器
         *
         * @return new NestedJarHandler instance | 新的 NestedJarHandler 实例
         * @throws OpenClassLoaderException if temp directory creation fails | 临时目录创建失败时抛出
         */
        public NestedJarHandler build() {
            try {
                Path dir;
                if (tempDirectory != null) {
                    Files.createDirectories(tempDirectory);
                    dir = tempDirectory;
                } else {
                    dir = Files.createTempDirectory("opencode-nested-");
                }
                return new NestedJarHandler(dir);
            } catch (IOException e) {
                throw new OpenClassLoaderException("Failed to create temp directory for nested JAR extraction", e);
            }
        }
    }

    // ==================== Public API ====================

    /**
     * Discover nested JAR entry paths inside the given outer JAR
     * 发现给定外层 JAR 内的嵌套 JAR 条目路径
     *
     * <p>Scans BOOT-INF/lib/, WEB-INF/lib/, and lib/ for .jar entries.</p>
     * <p>扫描 BOOT-INF/lib/、WEB-INF/lib/ 和 lib/ 下的 .jar 条目。</p>
     *
     * @param jarPath path to the outer JAR | 外层 JAR 路径
     * @return list of nested JAR entry names | 嵌套 JAR 条目名列表
     * @throws OpenClassLoaderException if the JAR cannot be read | JAR 无法读取时抛出
     */
    public List<String> findNestedJars(Path jarPath) {
        Objects.requireNonNull(jarPath, "jarPath must not be null");
        ensureOpen();

        List<String> nestedJars = new ArrayList<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.endsWith(".jar") && isNestedJarPath(name)) {
                    nestedJars.add(name);
                }
            }
        } catch (IOException e) {
            throw new OpenClassLoaderException("Failed to read JAR: " + jarPath, e);
        }
        return List.copyOf(nestedJars);
    }

    /**
     * Extract a nested JAR entry to a temporary file
     * 将嵌套 JAR 条目解压到临时文件
     *
     * <p>If the same entry has already been extracted, the existing path is returned
     * and the reference count is incremented.</p>
     * <p>如果同一条目已被解压，则返回现有路径并递增引用计数。</p>
     *
     * @param outerJar    path to the outer JAR | 外层 JAR 路径
     * @param nestedEntry nested JAR entry name (e.g. "BOOT-INF/lib/foo.jar") | 嵌套 JAR 条目名
     * @return path to the extracted temporary JAR file | 解压后的临时 JAR 文件路径
     * @throws OpenClassLoaderException if extraction fails or entry not found | 解压失败或条目不存在时抛出
     */
    public Path extractNestedJar(Path outerJar, String nestedEntry) {
        Objects.requireNonNull(outerJar, "outerJar must not be null");
        Objects.requireNonNull(nestedEntry, "nestedEntry must not be null");
        ensureOpen();

        validateEntryPath(nestedEntry);

        String key = compositeKey(outerJar, nestedEntry);

        ExtractedEntry entry = extractedEntries.compute(key, (k, existing) -> {
            if (existing != null) {
                existing.refCount.incrementAndGet();
                return existing;
            }
            Path extracted = doExtract(outerJar, nestedEntry);
            return new ExtractedEntry(extracted);
        });

        return entry.path;
    }

    /**
     * Release a reference to a previously extracted nested JAR
     * 释放对先前解压的嵌套 JAR 的引用
     *
     * <p>When the reference count reaches zero the temporary file is deleted.</p>
     * <p>当引用计数降为零时，临时文件将被删除。</p>
     *
     * @param outerJar    path to the outer JAR | 外层 JAR 路径
     * @param nestedEntry nested JAR entry name | 嵌套 JAR 条目名
     */
    public void release(Path outerJar, String nestedEntry) {
        Objects.requireNonNull(outerJar, "outerJar must not be null");
        Objects.requireNonNull(nestedEntry, "nestedEntry must not be null");

        String key = compositeKey(outerJar, nestedEntry);

        extractedEntries.computeIfPresent(key, (k, existing) -> {
            if (existing.refCount.decrementAndGet() <= 0) {
                deleteQuietly(existing.path);
                return null; // remove from map
            }
            return existing;
        });
    }

    /**
     * Get the temporary directory used for extraction
     * 获取用于解压的临时目录
     *
     * @return temporary directory path | 临时目录路径
     */
    public Path getTempDirectory() {
        return tempDirectory;
    }

    /**
     * Check if the handler is closed
     * 检查处理器是否已关闭
     *
     * @return true if closed | 已关闭返回 true
     */
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        // Delete all extracted files
        for (ExtractedEntry entry : extractedEntries.values()) {
            deleteQuietly(entry.path);
        }
        extractedEntries.clear();

        // Try to delete the temp directory itself
        deleteQuietly(tempDirectory);
    }

    // ==================== Internal ====================

    private static boolean isNestedJarPath(String name) {
        return name.startsWith("BOOT-INF/lib/")
                || name.startsWith("WEB-INF/lib/")
                || name.startsWith("lib/");
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new OpenClassLoaderException("NestedJarHandler has been closed");
        }
    }

    /**
     * Validate that the entry path does not escape the JAR root (path traversal protection).
     */
    private void validateEntryPath(String entryPath) {
        String normalized = Path.of(entryPath).normalize().toString();
        if (normalized.startsWith("..") || normalized.startsWith("/")) {
            throw new OpenClassLoaderException(
                    "Path traversal detected in nested entry: " + entryPath);
        }
    }

    private Path doExtract(Path outerJar, String nestedEntry) {
        try (JarFile jar = new JarFile(outerJar.toFile())) {
            JarEntry entry = jar.getJarEntry(nestedEntry);
            if (entry == null) {
                throw new OpenClassLoaderException(
                        "Nested entry not found: " + nestedEntry + " in " + outerJar);
            }

            // Derive a safe filename
            String fileName = nestedEntry.replace('/', '_');
            Path target = tempDirectory.resolve(fileName).normalize();

            // Second path traversal guard: ensure the target stays within tempDirectory
            if (!target.startsWith(tempDirectory)) {
                throw new OpenClassLoaderException(
                        "Path traversal detected during extraction: " + nestedEntry);
            }

            // Create file with restrictive permissions (owner-only rw) on POSIX systems
            try {
                Files.createFile(target,
                        PosixFilePermissions.asFileAttribute(
                                Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)));
            } catch (UnsupportedOperationException e) {
                // Non-POSIX filesystem (e.g. Windows) — create with default permissions
                Files.createFile(target);
            }
            try (InputStream in = jar.getInputStream(entry);
                 OutputStream out = Files.newOutputStream(target)) {
                in.transferTo(out);
            }
            return target;
        } catch (OpenClassLoaderException e) {
            throw e;
        } catch (IOException e) {
            throw new OpenClassLoaderException(
                    "Failed to extract nested JAR: " + nestedEntry + " from " + outerJar, e);
        }
    }

    private static String compositeKey(Path outerJar, String nestedEntry) {
        return outerJar.toAbsolutePath().normalize() + "!/" + nestedEntry;
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }

    // ==================== Inner classes ====================

    /**
     * Tracks an extracted temporary file and its reference count.
     */
    private static final class ExtractedEntry {
        final Path path;
        final AtomicInteger refCount;

        ExtractedEntry(Path path) {
            this.path = path;
            this.refCount = new AtomicInteger(1);
        }
    }
}
