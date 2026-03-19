package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * File Reader Utility
 * 文件读取器
 *
 * <p>Fluent API for reading files with various options.
 * Provides convenient methods for different reading scenarios.</p>
 * <p>用于读取文件的流式API。
 * 提供适用于不同读取场景的便捷方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API - 流式API</li>
 *   <li>Charset configuration - 字符集配置</li>
 *   <li>Multiple read modes - 多种读取模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Read as string
 * String content = FileReader.of(path).asString();
 *
 * // Read lines
 * List<String> lines = FileReader.of(path).asLines();
 *
 * // Read with charset
 * String content = FileReader.of(path).charset(StandardCharsets.ISO_8859_1).asString();
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
public final class FileReader {

    private final Path path;
    private Charset charset = StandardCharsets.UTF_8;

    private FileReader(Path path) {
        this.path = path;
    }

    /**
     * Creates a file reader for the given path
     * 为给定路径创建文件读取器
     *
     * @param path the file path | 文件路径
     * @return file reader | 文件读取器
     */
    public static FileReader of(Path path) {
        return new FileReader(path);
    }

    /**
     * Creates a file reader for the given path string
     * 为给定路径字符串创建文件读取器
     *
     * @param path the file path string | 文件路径字符串
     * @return file reader | 文件读取器
     */
    public static FileReader of(String path) {
        return new FileReader(Path.of(path));
    }

    /**
     * Sets the charset
     * 设置字符集
     *
     * @param charset the charset | 字符集
     * @return this | 当前对象
     */
    public FileReader charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Reads file as byte array
     * 读取文件为字节数组
     *
     * @return byte array | 字节数组
     */
    public byte[] asBytes() {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Reads file as string
     * 读取文件为字符串
     *
     * @return content string | 内容字符串
     */
    public String asString() {
        try {
            return Files.readString(path, charset);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Reads file as list of lines
     * 读取文件为行列表
     *
     * @return list of lines | 行列表
     */
    public List<String> asLines() {
        try {
            return Files.readAllLines(path, charset);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Reads file as stream of lines
     * 读取文件为行流
     *
     * @return stream of lines | 行流
     */
    public Stream<String> asLineStream() {
        try {
            return Files.lines(path, charset);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Gets a buffered reader
     * 获取缓冲读取器
     *
     * @return buffered reader | 缓冲读取器
     */
    public BufferedReader asBufferedReader() {
        try {
            return Files.newBufferedReader(path, charset);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Gets an input stream
     * 获取输入流
     *
     * @return input stream | 输入流
     */
    public InputStream asInputStream() {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Reads first line of file
     * 读取文件首行
     *
     * @return first line or null | 首行或null
     */
    public String firstLine() {
        try (BufferedReader reader = asBufferedReader()) {
            return reader.readLine();
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Reads last line of file
     * 读取文件末行
     *
     * @return last line or null | 末行或null
     */
    public String lastLine() {
        try (Stream<String> lines = asLineStream()) {
            return lines.reduce((first, second) -> second).orElse(null);
        }
    }

    /**
     * Checks if file exists
     * 检查文件是否存在
     *
     * @return true if exists | 如果存在返回true
     */
    public boolean exists() {
        return Files.exists(path);
    }

    /**
     * Gets file size
     * 获取文件大小
     *
     * @return size in bytes | 字节大小
     */
    public long size() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return -1;
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
