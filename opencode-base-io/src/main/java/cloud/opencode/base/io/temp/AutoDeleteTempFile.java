package cloud.opencode.base.io.temp;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Auto Delete Temp File
 * 自动删除临时文件
 *
 * <p>A temporary file that automatically deletes itself when closed.
 * Implements Closeable for use with try-with-resources.</p>
 * <p>关闭时自动删除自身的临时文件。
 * 实现Closeable以便与try-with-resources一起使用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Auto delete on close - 关闭时自动删除</li>
 *   <li>Convenient read/write methods - 便捷的读写方法</li>
 *   <li>Try-with-resources support - 支持try-with-resources</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile("data", ".json")) {
 *     temp.write("{\"key\": \"value\"}");
 *     processFile(temp.getPath());
 * } // File is automatically deleted
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public class AutoDeleteTempFile implements Closeable {

    private final Path path;
    private volatile boolean deleted;

    /**
     * Creates an auto delete temp file
     * 创建自动删除临时文件
     *
     * @param path the temp file path | 临时文件路径
     */
    AutoDeleteTempFile(Path path) {
        this.path = path;
        this.deleted = false;
    }

    /**
     * Gets the file path
     * 获取文件路径
     *
     * @return path | 路径
     */
    public Path getPath() {
        return path;
    }

    /**
     * Gets the file as File object
     * 获取File对象
     *
     * @return File object | File对象
     */
    public File toFile() {
        return path.toFile();
    }

    /**
     * Writes byte content to the file
     * 写入字节内容到文件
     *
     * @param content the content | 内容
     */
    public void write(byte[] content) {
        try {
            Files.write(path, content);
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(path, e);
        }
    }

    /**
     * Writes string content to the file (UTF-8)
     * 写入字符串内容到文件（UTF-8）
     *
     * @param content the content | 内容
     */
    public void write(String content) {
        write(content, StandardCharsets.UTF_8);
    }

    /**
     * Writes string content to the file with charset
     * 使用指定字符集写入字符串内容到文件
     *
     * @param content the content | 内容
     * @param charset the charset | 字符集
     */
    public void write(String content, Charset charset) {
        try {
            Files.writeString(path, content, charset);
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(path, e);
        }
    }

    /**
     * Reads file content as bytes
     * 读取文件内容为字节
     *
     * @return byte array | 字节数组
     */
    public byte[] read() {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Reads file content as string (UTF-8)
     * 读取文件内容为字符串（UTF-8）
     *
     * @return content | 内容
     */
    public String readString() {
        return readString(StandardCharsets.UTF_8);
    }

    /**
     * Reads file content as string with charset
     * 使用指定字符集读取文件内容为字符串
     *
     * @param charset the charset | 字符集
     * @return content | 内容
     */
    public String readString(Charset charset) {
        try {
            return Files.readString(path, charset);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Checks if the file exists
     * 检查文件是否存在
     *
     * @return true if exists | 如果存在返回true
     */
    public boolean exists() {
        return Files.exists(path) && !deleted;
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
     * Deletes the file immediately
     * 立即删除文件
     *
     * @return true if deleted | 如果删除成功返回true
     */
    public boolean delete() {
        if (deleted) {
            return true;
        }
        try {
            boolean result = Files.deleteIfExists(path);
            deleted = true;
            return result;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void close() {
        delete();
    }

    @Override
    public String toString() {
        return "AutoDeleteTempFile[" + path + "]";
    }
}
