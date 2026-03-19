package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * File Writer Utility
 * 文件写入器
 *
 * <p>Fluent API for writing files with various options.
 * Provides convenient methods for different writing scenarios.</p>
 * <p>用于写入文件的流式API。
 * 提供适用于不同写入场景的便捷方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API - 流式API</li>
 *   <li>Charset configuration - 字符集配置</li>
 *   <li>Append mode support - 追加模式支持</li>
 *   <li>Create parent directories - 创建父目录</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Write string
 * FileWriter.of(path).write("Hello World");
 *
 * // Append content
 * FileWriter.of(path).append().write("More content");
 *
 * // Write with charset
 * FileWriter.of(path).charset(StandardCharsets.ISO_8859_1).write(content);
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
public final class FileWriter {

    private final Path path;
    private Charset charset = StandardCharsets.UTF_8;
    private boolean append = false;
    private boolean createParents = true;

    private FileWriter(Path path) {
        this.path = path;
    }

    /**
     * Creates a file writer for the given path
     * 为给定路径创建文件写入器
     *
     * @param path the file path | 文件路径
     * @return file writer | 文件写入器
     */
    public static FileWriter of(Path path) {
        return new FileWriter(path);
    }

    /**
     * Creates a file writer for the given path string
     * 为给定路径字符串创建文件写入器
     *
     * @param path the file path string | 文件路径字符串
     * @return file writer | 文件写入器
     */
    public static FileWriter of(String path) {
        return new FileWriter(Path.of(path));
    }

    /**
     * Sets the charset
     * 设置字符集
     *
     * @param charset the charset | 字符集
     * @return this | 当前对象
     */
    public FileWriter charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Enables append mode
     * 启用追加模式
     *
     * @return this | 当前对象
     */
    public FileWriter append() {
        this.append = true;
        return this;
    }

    /**
     * Sets append mode
     * 设置追加模式
     *
     * @param append whether to append | 是否追加
     * @return this | 当前对象
     */
    public FileWriter append(boolean append) {
        this.append = append;
        return this;
    }

    /**
     * Disables automatic parent directory creation
     * 禁用自动创建父目录
     *
     * @return this | 当前对象
     */
    public FileWriter noCreateParents() {
        this.createParents = false;
        return this;
    }

    /**
     * Writes byte array to file
     * 写入字节数组到文件
     *
     * @param bytes the content | 内容
     */
    public void write(byte[] bytes) {
        ensureParentExists();
        try {
            Files.write(path, bytes, getOpenOptions());
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(path, e);
        }
    }

    /**
     * Writes string to file
     * 写入字符串到文件
     *
     * @param content the content | 内容
     */
    public void write(CharSequence content) {
        ensureParentExists();
        try {
            Files.writeString(path, content, charset, getOpenOptions());
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(path, e);
        }
    }

    /**
     * Writes lines to file
     * 写入行到文件
     *
     * @param lines the lines | 行
     */
    public void writeLines(Iterable<? extends CharSequence> lines) {
        ensureParentExists();
        try {
            Files.write(path, lines, charset, getOpenOptions());
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(path, e);
        }
    }

    /**
     * Gets a buffered writer
     * 获取缓冲写入器
     *
     * @return buffered writer | 缓冲写入器
     */
    public BufferedWriter asBufferedWriter() {
        ensureParentExists();
        try {
            return Files.newBufferedWriter(path, charset, getOpenOptions());
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(path, e);
        }
    }

    /**
     * Gets an output stream
     * 获取输出流
     *
     * @return output stream | 输出流
     */
    public OutputStream asOutputStream() {
        ensureParentExists();
        try {
            return Files.newOutputStream(path, getOpenOptions());
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(path, e);
        }
    }

    private OpenOption[] getOpenOptions() {
        if (append) {
            return new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND};
        }
        return new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
    }

    private void ensureParentExists() {
        if (createParents) {
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    throw OpenIOOperationException.createDirectoryFailed(parent, e);
                }
            }
        }
    }

    /**
     * Gets the path
     * 获取路径
     *
     * @return path | 路径
     */
    public Path getPath() {
        return path;
    }
}
