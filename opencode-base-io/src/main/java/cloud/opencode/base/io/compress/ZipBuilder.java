package cloud.opencode.base.io.compress;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Fluent Builder for Creating Zip Archives
 * 用于创建Zip归档的流式构建器
 *
 * <p>Provides a fluent API to assemble a Zip archive from files, directories,
 * byte arrays, and strings, then write it to a target path.</p>
 * <p>提供流式API从文件、目录、字节数组和字符串组装Zip归档，然后写入目标路径。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Add files with optional custom entry names - 添加文件并可自定义条目名</li>
 *   <li>Add directories recursively - 递归添加目录</li>
 *   <li>Add raw bytes and strings - 添加原始字节和字符串</li>
 *   <li>Configurable compression level and comment - 可配置压缩级别和注释</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ZipUtil.builder()
 *     .addFile(path1)
 *     .addFile(path2, "custom/name.txt")
 *     .addDirectory(dirPath)
 *     .addString("readme.txt", "Hello World")
 *     .compressionLevel(6)
 *     .comment("My archive")
 *     .writeTo(outputPath);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not intended for concurrent use) - 线程安全: 否（不用于并发使用）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
public final class ZipBuilder {

    /**
     * Buffer size for stream operations (8KB)
     * 流操作的缓冲区大小（8KB）
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Entries to be written
     * 要写入的条目
     */
    private final List<EntrySource> entries = new ArrayList<>();

    /**
     * Archive comment
     * 归档注释
     */
    private String archiveComment;

    /**
     * Compression level (0-9), default Deflater.DEFAULT_COMPRESSION
     * 压缩级别（0-9），默认Deflater.DEFAULT_COMPRESSION
     */
    private int level = Deflater.DEFAULT_COMPRESSION;

    /**
     * Package-private constructor, use {@link ZipUtil#builder()}
     * 包级私有构造方法，使用{@link ZipUtil#builder()}
     */
    ZipBuilder() {
    }

    // ==================== Add Methods | 添加方法 ====================

    /**
     * Adds a file using its file name as entry name
     * 使用文件名作为条目名添加文件
     *
     * @param file the file to add | 要添加的文件
     * @return this builder | 此构建器
     * @throws NullPointerException if file is null | 当file为null时抛出
     */
    public ZipBuilder addFile(Path file) {
        Objects.requireNonNull(file, "file must not be null");
        String entryName = file.getFileName().toString();
        entries.add(new FileEntrySource(file, entryName));
        return this;
    }

    /**
     * Adds a file with a custom entry name
     * 使用自定义条目名添加文件
     *
     * @param file      the file to add | 要添加的文件
     * @param entryName the entry name in the archive | 归档中的条目名
     * @return this builder | 此构建器
     * @throws NullPointerException if file or entryName is null | 当file或entryName为null时抛出
     */
    public ZipBuilder addFile(Path file, String entryName) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(entryName, "entryName must not be null");
        validateEntryName(entryName);
        entries.add(new FileEntrySource(file, entryName));
        return this;
    }

    /**
     * Adds a directory recursively
     * 递归添加目录
     *
     * @param dir the directory to add | 要添加的目录
     * @return this builder | 此构建器
     * @throws NullPointerException if dir is null | 当dir为null时抛出
     */
    public ZipBuilder addDirectory(Path dir) {
        Objects.requireNonNull(dir, "dir must not be null");
        entries.add(new DirectoryEntrySource(dir));
        return this;
    }

    /**
     * Adds raw bytes as an entry
     * 添加原始字节作为条目
     *
     * @param entryName the entry name | 条目名
     * @param data      the byte data | 字节数据
     * @return this builder | 此构建器
     * @throws NullPointerException if entryName or data is null | 当entryName或data为null时抛出
     */
    public ZipBuilder addBytes(String entryName, byte[] data) {
        Objects.requireNonNull(entryName, "entryName must not be null");
        Objects.requireNonNull(data, "data must not be null");
        validateEntryName(entryName);
        entries.add(new BytesEntrySource(entryName, data.clone()));
        return this;
    }

    /**
     * Adds a string as an entry using UTF-8 encoding
     * 使用UTF-8编码添加字符串作为条目
     *
     * @param entryName the entry name | 条目名
     * @param content   the string content | 字符串内容
     * @return this builder | 此构建器
     * @throws NullPointerException if entryName or content is null | 当entryName或content为null时抛出
     */
    public ZipBuilder addString(String entryName, String content) {
        return addString(entryName, content, StandardCharsets.UTF_8);
    }

    /**
     * Adds a string as an entry with the specified charset
     * 使用指定字符集添加字符串作为条目
     *
     * @param entryName the entry name | 条目名
     * @param content   the string content | 字符串内容
     * @param charset   the charset for encoding | 编码用的字符集
     * @return this builder | 此构建器
     * @throws NullPointerException if any argument is null | 当任何参数为null时抛出
     */
    public ZipBuilder addString(String entryName, String content, Charset charset) {
        Objects.requireNonNull(entryName, "entryName must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        validateEntryName(entryName);
        entries.add(new BytesEntrySource(entryName, content.getBytes(charset)));
        return this;
    }

    // ==================== Configuration | 配置 ====================

    /**
     * Sets the archive comment
     * 设置归档注释
     *
     * @param comment the archive comment | 归档注释
     * @return this builder | 此构建器
     */
    public ZipBuilder comment(String comment) {
        this.archiveComment = comment;
        return this;
    }

    /**
     * Sets the compression level (0-9)
     * 设置压缩级别（0-9）
     *
     * @param level the compression level (0 = no compression, 9 = best compression) | 压缩级别（0=不压缩，9=最佳压缩）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if level is out of range | 当级别超出范围时抛出
     */
    public ZipBuilder compressionLevel(int level) {
        if (level < 0 || level > 9) {
            throw new IllegalArgumentException("Compression level must be between 0 and 9, got: " + level);
        }
        this.level = level;
        return this;
    }

    // ==================== Build | 构建 ====================

    /**
     * Writes the assembled archive to the target path
     * 将组装好的归档写入目标路径
     *
     * @param target the output file path | 输出文件路径
     * @throws NullPointerException      if target is null | 当target为null时抛出
     * @throws OpenIOOperationException  if writing fails | 当写入失败时抛出
     */
    public void writeTo(Path target) {
        Objects.requireNonNull(target, "target must not be null");
        try (OutputStream fos = Files.newOutputStream(target);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zos.setLevel(level);
            if (archiveComment != null) {
                zos.setComment(archiveComment);
            }
            for (EntrySource entry : entries) {
                entry.writeTo(zos);
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("zip", target.toString(),
                    String.format("Failed to write zip archive: %s", target), e);
        }
    }

    // ==================== Validation | 校验 ====================

    /**
     * Validates a zip entry name for path traversal attacks
     * 校验zip条目名是否存在路径穿越攻击
     *
     * @param entryName the entry name to validate | 要校验的条目名
     * @throws IllegalArgumentException if the entry name is unsafe | 当条目名不安全时抛出
     */
    private static void validateEntryName(String entryName) {
        if (entryName.isEmpty()) {
            throw new IllegalArgumentException("Entry name must not be empty");
        }
        if (entryName.startsWith("/") || entryName.startsWith("\\")) {
            throw new IllegalArgumentException(
                    "Entry name must not be absolute: " + entryName);
        }
        if (entryName.contains("\0")) {
            throw new IllegalArgumentException(
                    "Entry name must not contain null bytes: " + entryName);
        }
        // Normalize and check for path traversal
        String normalized = entryName.replace('\\', '/');
        for (String segment : normalized.split("/")) {
            if ("..".equals(segment)) {
                throw new IllegalArgumentException(
                        "Entry name must not contain path traversal (..): " + entryName);
            }
        }
    }

    // ==================== Internal Entry Sources | 内部条目源 ====================

    /**
     * Abstract source for a zip entry
     * Zip条目的抽象源
     */
    private sealed interface EntrySource permits FileEntrySource, DirectoryEntrySource, BytesEntrySource {
        /**
         * Writes this entry to the ZipOutputStream
         * 将此条目写入ZipOutputStream
         */
        void writeTo(ZipOutputStream zos) throws IOException;
    }

    /**
     * Single file entry source
     * 单文件条目源
     */
    private record FileEntrySource(Path file, String entryName) implements EntrySource {
        @Override
        public void writeTo(ZipOutputStream zos) throws IOException {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            try (var is = Files.newInputStream(file)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesRead);
                }
            }
            zos.closeEntry();
        }
    }

    /**
     * Directory entry source (recursive)
     * 目录条目源（递归）
     */
    private record DirectoryEntrySource(Path dir) implements EntrySource {
        @Override
        public void writeTo(ZipOutputStream zos) throws IOException {
            Path baseName = dir.getFileName();
            if (baseName == null) {
                throw new IOException("Cannot determine directory name for: " + dir);
            }
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dirEntry, BasicFileAttributes attrs)
                        throws IOException {
                    String name = baseName + "/" + dir.relativize(dirEntry);
                    if (!name.endsWith("/")) {
                        name += "/";
                    }
                    zos.putNextEntry(new ZipEntry(name));
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    String name = baseName + "/" + dir.relativize(file);
                    zos.putNextEntry(new ZipEntry(name));
                    try (var is = Files.newInputStream(file)) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                    }
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * Byte array entry source
     * 字节数组条目源
     */
    private record BytesEntrySource(String entryName, byte[] data) implements EntrySource {
        @Override
        public void writeTo(ZipOutputStream zos) throws IOException {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
        }
    }
}
