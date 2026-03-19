package cloud.opencode.base.core.exception;

import java.io.IOException;
import java.io.Serial;
import java.nio.file.Path;

/**
 * IO Exception Wrapper - Wrap checked IOException as unchecked
 * IO 异常包装 - 将受检 IOException 包装为非受检异常
 *
 * <p>Wraps checked {@link IOException} as unchecked exception to simplify exception handling.</p>
 * <p>将受检的 {@link IOException} 包装为非受检异常，简化异常处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Wrap IOException (wrap) - 包装 IOException</li>
 *   <li>Read failure factory (readFailed) - 读取失败工厂</li>
 *   <li>Write failure factory (writeFailed) - 写入失败工厂</li>
 *   <li>File not found factory (fileNotFound) - 文件未找到工厂</li>
 *   <li>Close failure factory (closeFailed) - 关闭失败工厂</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     Files.readString(path);
 * } catch (IOException e) {
 *     throw new OpenIOException("Failed to read file", e);
 * }
 *
 * // Static factory methods - 静态工厂方法
 * throw OpenIOException.readFailed(path, e);
 * throw OpenIOException.writeFailed(path, e);
 * throw OpenIOException.fileNotFound(path);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Serializable: Yes - 可序列化: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class OpenIOException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "Core";
    private static final String ERROR_CODE = "IO_ERROR";

    /**
     * Creates
     * 创建 IO 异常
     *
     * @param message the value | 异常消息
     */
    public OpenIOException(String message) {
        super(COMPONENT, ERROR_CODE, message);
    }

    /**
     * Creates
     * 创建 IO 异常（包装原始 IOException）
     *
     * @param message the value | 异常消息
     * @param cause the value | 原始异常
     */
    public OpenIOException(String message, Throwable cause) {
        super(COMPONENT, ERROR_CODE, message, cause);
    }

    /**
     * Creates
     * 创建 IO 异常（指定错误码）
     *
     * @param errorCode the value | 错误码
     * @param message the value | 异常消息
     * @param cause the value | 原始异常
     */
    public OpenIOException(String errorCode, String message, Throwable cause) {
        super(COMPONENT, errorCode, message, cause);
    }

    // ==================== 静态工厂方法 ====================

    /**
     * Wraps
     * 包装 IOException
     *
     * @param cause the value | 原始 IOException
     * @return the result | 异常实例
     */
    public static OpenIOException wrap(IOException cause) {
        return new OpenIOException(cause.getMessage(), cause);
    }

    /**
     * Creates
     * 创建"读取失败"异常
     *
     * @param path the value | 文件路径
     * @param cause the value | 原始异常
     * @return the result | 异常实例
     */
    public static OpenIOException readFailed(Path path, Throwable cause) {
        return new OpenIOException("IO_READ_FAILED",
                "Failed to read from: " + path, cause);
    }

    /**
     * Creates
     * 创建"读取失败"异常
     *
     * @param resource the value | 资源名
     * @param cause the value | 原始异常
     * @return the result | 异常实例
     */
    public static OpenIOException readFailed(String resource, Throwable cause) {
        return new OpenIOException("IO_READ_FAILED",
                "Failed to read from: " + resource, cause);
    }

    /**
     * Creates
     * 创建"写入失败"异常
     *
     * @param path the value | 文件路径
     * @param cause the value | 原始异常
     * @return the result | 异常实例
     */
    public static OpenIOException writeFailed(Path path, Throwable cause) {
        return new OpenIOException("IO_WRITE_FAILED",
                "Failed to write to: " + path, cause);
    }

    /**
     * Creates
     * 创建"写入失败"异常
     *
     * @param resource the value | 资源名
     * @param cause the value | 原始异常
     * @return the result | 异常实例
     */
    public static OpenIOException writeFailed(String resource, Throwable cause) {
        return new OpenIOException("IO_WRITE_FAILED",
                "Failed to write to: " + resource, cause);
    }

    /**
     * Creates
     * 创建"文件未找到"异常
     *
     * @param path the value | 文件路径
     * @return the result | 异常实例
     */
    public static OpenIOException fileNotFound(Path path) {
        return new OpenIOException("IO_FILE_NOT_FOUND",
                "File not found: " + path, null);
    }

    /**
     * Creates
     * 创建"文件未找到"异常
     *
     * @param resource the value | 资源名
     * @return the result | 异常实例
     */
    public static OpenIOException fileNotFound(String resource) {
        return new OpenIOException("IO_FILE_NOT_FOUND",
                "File not found: " + resource, null);
    }

    /**
     * Creates
     * 创建"关闭资源失败"异常
     *
     * @param resource the value | 资源名
     * @param cause the value | 原始异常
     * @return the result | 异常实例
     */
    public static OpenIOException closeFailed(String resource, Throwable cause) {
        return new OpenIOException("IO_CLOSE_FAILED",
                "Failed to close: " + resource, cause);
    }
}
