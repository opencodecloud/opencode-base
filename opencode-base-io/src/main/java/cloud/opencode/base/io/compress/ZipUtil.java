package cloud.opencode.base.io.compress;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Zip Archive Utility
 * Zip归档工具类
 *
 * <p>Provides static utility methods for creating, extracting, and inspecting
 * Zip archives. Includes security protections against zip bombs and path
 * traversal attacks.</p>
 * <p>提供创建、提取和检查Zip归档的静态工具方法。
 * 包含针对zip炸弹和路径穿越攻击的安全保护。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Zip files and directories - 压缩文件和目录</li>
 *   <li>Extract zip archives safely - 安全提取zip归档</li>
 *   <li>List and read individual entries - 列出和读取单个条目</li>
 *   <li>Fluent builder API for complex archives - 复杂归档的流式构建器API</li>
 *   <li>Path traversal protection - 路径穿越防护</li>
 *   <li>Zip bomb protection - Zip炸弹防护</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Zip a directory
 * ZipUtil.zip(sourceDir, targetZip);
 *
 * // Unzip safely
 * ZipUtil.unzip(zipFile, outputDir);
 *
 * // List entries
 * List<ZipEntryInfo> entries = ZipUtil.list(zipFile);
 *
 * // Builder API
 * ZipUtil.builder()
 *     .addFile(path1)
 *     .addString("note.txt", "hello")
 *     .writeTo(outputZip);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Path traversal: Blocked - 路径穿越: 已阻止</li>
 *   <li>Zip bomb: Size and entry count limits - Zip炸弹: 大小和条目数限制</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
public final class ZipUtil {

    /**
     * Maximum number of entries allowed in a zip archive
     * Zip归档中允许的最大条目数
     */
    public static final int MAX_ENTRIES = 65_535;

    /**
     * Maximum total uncompressed size allowed (4GB)
     * 允许的最大总未压缩大小（4GB）
     */
    public static final long MAX_UNCOMPRESSED_SIZE = 4L * 1024 * 1024 * 1024;

    /**
     * Maximum entry name length allowed
     * 允许的最大条目名长度
     */
    public static final int MAX_ENTRY_NAME_LENGTH = 512;

    /**
     * Buffer size for stream operations (8KB)
     * 流操作的缓冲区大小（8KB）
     */
    private static final int BUFFER_SIZE = 8192;

    private ZipUtil() {
        throw new AssertionError("No ZipUtil instances for you!");
    }

    // ==================== Zip | 压缩 ====================

    /**
     * Zips a single file or directory to the target zip file
     * 将单个文件或目录压缩到目标zip文件
     *
     * <p>If source is a directory, its contents are added recursively.</p>
     * <p>如果源是目录，则递归添加其内容。</p>
     *
     * @param source the source file or directory | 源文件或目录
     * @param target the target zip file path | 目标zip文件路径
     * @throws NullPointerException      if source or target is null | 当source或target为null时抛出
     * @throws OpenIOOperationException  if zipping fails | 当压缩失败时抛出
     */
    public static void zip(Path source, Path target) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        try (OutputStream fos = Files.newOutputStream(target);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            if (Files.isDirectory(source)) {
                addDirectory(zos, source, source);
            } else {
                addFileEntry(zos, source, source.getFileName().toString());
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("zip", source.toString(),
                    String.format("Failed to zip: %s", source), e);
        }
    }

    /**
     * Zips multiple files to the target zip file
     * 将多个文件压缩到目标zip文件
     *
     * @param sources the source files | 源文件集合
     * @param target  the target zip file path | 目标zip文件路径
     * @throws NullPointerException      if sources or target is null | 当sources或target为null时抛出
     * @throws OpenIOOperationException  if zipping fails | 当压缩失败时抛出
     */
    public static void zip(Collection<Path> sources, Path target) {
        Objects.requireNonNull(sources, "sources must not be null");
        Objects.requireNonNull(target, "target must not be null");
        try (OutputStream fos = Files.newOutputStream(target);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (Path source : sources) {
                Objects.requireNonNull(source, "source path must not be null");
                if (Files.isDirectory(source)) {
                    addDirectory(zos, source, source);
                } else {
                    addFileEntry(zos, source, source.getFileName().toString());
                }
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("zip", null,
                    "Failed to zip multiple files", e);
        }
    }

    // ==================== Unzip | 解压缩 ====================

    /**
     * Extracts a zip archive to the target directory with security checks
     * 使用安全检查将zip归档提取到目标目录
     *
     * <p>Security protections applied:</p>
     * <ul>
     *   <li>Path traversal detection (rejects entries containing "..")</li>
     *   <li>Resolved path must be within target directory</li>
     *   <li>Total uncompressed size limited to {@link #MAX_UNCOMPRESSED_SIZE}</li>
     *   <li>Entry count limited to {@link #MAX_ENTRIES}</li>
     *   <li>Entry name length limited to {@link #MAX_ENTRY_NAME_LENGTH}</li>
     * </ul>
     *
     * @param zipFile   the zip file to extract | 要提取的zip文件
     * @param targetDir the target directory | 目标目录
     * @throws NullPointerException      if zipFile or targetDir is null | 当zipFile或targetDir为null时抛出
     * @throws OpenIOOperationException  if extraction fails or security check fails | 当提取失败或安全检查失败时抛出
     */
    public static void unzip(Path zipFile, Path targetDir) {
        Objects.requireNonNull(zipFile, "zipFile must not be null");
        Objects.requireNonNull(targetDir, "targetDir must not be null");
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new OpenIOOperationException("unzip", targetDir.toString(),
                    String.format("Failed to create target directory: %s", targetDir), e);
        }

        Path normalizedTarget = targetDir.toAbsolutePath().normalize();

        try (ZipFile zf = new ZipFile(zipFile.toFile())) {
            int entryCount = 0;
            long totalSize = 0;

            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                // Security: entry count limit
                entryCount++;
                if (entryCount > MAX_ENTRIES) {
                    throw new OpenIOOperationException("unzip", zipFile.toString(),
                            String.format("Zip archive exceeds maximum entry count: %d", MAX_ENTRIES));
                }

                // Security: entry name length
                if (entry.getName().length() > MAX_ENTRY_NAME_LENGTH) {
                    throw new OpenIOOperationException("unzip", zipFile.toString(),
                            String.format("Entry name exceeds maximum length (%d): %s",
                                    MAX_ENTRY_NAME_LENGTH, entry.getName()));
                }

                // Security: path traversal check (canonical path verification)
                Path resolvedPath = normalizedTarget.resolve(entry.getName()).normalize();
                if (!resolvedPath.startsWith(normalizedTarget)) {
                    throw new OpenIOOperationException("unzip", zipFile.toString(),
                            String.format("Path traversal detected in entry: %s", entry.getName()));
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    // Ensure parent directory exists
                    Path parent = resolvedPath.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }

                    // Security: always count actual bytes written for zip bomb protection
                    try (InputStream is = zf.getInputStream(entry);
                         OutputStream os = Files.newOutputStream(resolvedPath)) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                            totalSize += bytesRead;
                            if (totalSize > MAX_UNCOMPRESSED_SIZE) {
                                throw new OpenIOOperationException("unzip", zipFile.toString(),
                                        String.format("Total uncompressed size exceeds limit: %d bytes",
                                                MAX_UNCOMPRESSED_SIZE));
                            }
                        }
                    }
                }
            }
        } catch (OpenIOOperationException e) {
            throw e;
        } catch (IOException e) {
            throw new OpenIOOperationException("unzip", zipFile.toString(),
                    String.format("Failed to unzip: %s", zipFile), e);
        }
    }

    // ==================== Inspection | 检查 ====================

    /**
     * Lists all entries in a zip archive
     * 列出zip归档中的所有条目
     *
     * @param zipFile the zip file path | zip文件路径
     * @return list of entry metadata | 条目元数据列表
     * @throws NullPointerException      if zipFile is null | 当zipFile为null时抛出
     * @throws OpenIOOperationException  if reading fails | 当读取失败时抛出
     */
    public static List<ZipEntryInfo> list(Path zipFile) {
        Objects.requireNonNull(zipFile, "zipFile must not be null");
        try (ZipFile zf = new ZipFile(zipFile.toFile())) {
            List<ZipEntryInfo> result = new ArrayList<>();
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Instant lastModified = entry.getLastModifiedTime() != null
                        ? entry.getLastModifiedTime().toInstant()
                        : null;
                result.add(new ZipEntryInfo(
                        entry.getName(),
                        entry.getSize(),
                        entry.getCompressedSize(),
                        entry.isDirectory(),
                        entry.getCrc(),
                        lastModified
                ));
            }
            return List.copyOf(result);
        } catch (IOException e) {
            throw new OpenIOOperationException("list", zipFile.toString(),
                    String.format("Failed to list zip entries: %s", zipFile), e);
        }
    }

    /**
     * Reads a single entry from a zip archive
     * 从zip归档中读取单个条目
     *
     * @param zipFile   the zip file path | zip文件路径
     * @param entryName the entry name to read | 要读取的条目名
     * @return the entry data | 条目数据
     * @throws NullPointerException      if zipFile or entryName is null | 当zipFile或entryName为null时抛出
     * @throws OpenIOOperationException  if entry not found or reading fails | 当条目未找到或读取失败时抛出
     */
    public static byte[] readEntry(Path zipFile, String entryName) {
        return readEntry(zipFile, entryName, MAX_UNCOMPRESSED_SIZE);
    }

    /**
     * Reads a single entry from a zip archive with a size limit
     * 从zip归档中读取单个条目（带大小限制）
     *
     * @param zipFile   the zip file path | zip文件路径
     * @param entryName the entry name to read | 要读取的条目名
     * @param maxSize   the maximum entry size in bytes | 最大条目大小（字节）
     * @return the entry data | 条目数据
     * @throws NullPointerException      if zipFile or entryName is null | 当zipFile或entryName为null时抛出
     * @throws IllegalArgumentException  if maxSize is not positive | 当maxSize非正数时抛出
     * @throws OpenIOOperationException  if entry not found, reading fails, or size limit exceeded |
     *                                   当条目未找到、读取失败或超出大小限制时抛出
     */
    public static byte[] readEntry(Path zipFile, String entryName, long maxSize) {
        Objects.requireNonNull(zipFile, "zipFile must not be null");
        Objects.requireNonNull(entryName, "entryName must not be null");
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive: " + maxSize);
        }
        try (ZipFile zf = new ZipFile(zipFile.toFile())) {
            ZipEntry entry = zf.getEntry(entryName);
            if (entry == null) {
                throw new OpenIOOperationException("readEntry", zipFile.toString(),
                        String.format("Entry not found: %s", entryName));
            }
            try (InputStream is = zf.getInputStream(entry)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                long totalRead = 0;
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    totalRead += bytesRead;
                    if (totalRead > maxSize) {
                        throw new OpenIOOperationException("readEntry", zipFile.toString(),
                                String.format("Entry '%s' exceeds maximum size limit: %d bytes",
                                        entryName, maxSize));
                    }
                    baos.write(buffer, 0, bytesRead);
                }
                return baos.toByteArray();
            }
        } catch (OpenIOOperationException e) {
            throw e;
        } catch (IOException e) {
            throw new OpenIOOperationException("readEntry", zipFile.toString(),
                    String.format("Failed to read entry '%s' from: %s", entryName, zipFile), e);
        }
    }

    /**
     * Checks whether a zip archive contains an entry with the given name
     * 检查zip归档中是否包含给定名称的条目
     *
     * @param zipFile   the zip file path | zip文件路径
     * @param entryName the entry name to check | 要检查的条目名
     * @return true if the entry exists | 如果条目存在返回true
     * @throws NullPointerException      if zipFile or entryName is null | 当zipFile或entryName为null时抛出
     * @throws OpenIOOperationException  if reading the zip fails | 当读取zip失败时抛出
     */
    public static boolean containsEntry(Path zipFile, String entryName) {
        Objects.requireNonNull(zipFile, "zipFile must not be null");
        Objects.requireNonNull(entryName, "entryName must not be null");
        try (ZipFile zf = new ZipFile(zipFile.toFile())) {
            return zf.getEntry(entryName) != null;
        } catch (IOException e) {
            throw new OpenIOOperationException("containsEntry", zipFile.toString(),
                    String.format("Failed to inspect zip: %s", zipFile), e);
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Creates a new fluent builder for assembling a zip archive
     * 创建一个新的流式构建器用于组装zip归档
     *
     * @return a new ZipBuilder | 新的ZipBuilder
     */
    public static ZipBuilder builder() {
        return new ZipBuilder();
    }

    // ==================== Internal | 内部方法 ====================

    /**
     * Adds a directory recursively to the ZipOutputStream
     * 递归将目录添加到ZipOutputStream
     */
    private static void addDirectory(ZipOutputStream zos, Path rootDir, Path sourceDir)
            throws IOException {
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                String entryName = rootDir.getParent() != null
                        ? rootDir.getParent().relativize(dir).toString()
                        : dir.getFileName().toString();
                if (!entryName.isEmpty()) {
                    if (!entryName.endsWith("/")) {
                        entryName += "/";
                    }
                    zos.putNextEntry(new ZipEntry(entryName));
                    zos.closeEntry();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                String entryName = rootDir.getParent() != null
                        ? rootDir.getParent().relativize(file).toString()
                        : rootDir.getFileName() + "/" + rootDir.relativize(file);
                addFileEntry(zos, file, entryName);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Adds a single file entry to the ZipOutputStream
     * 将单个文件条目添加到ZipOutputStream
     */
    private static void addFileEntry(ZipOutputStream zos, Path file, String entryName)
            throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
        }
        zos.closeEntry();
    }
}
