package cloud.opencode.base.image.exception;

import java.nio.file.Path;

/**
 * Image Write Exception
 * 图片写入异常
 *
 * <p>Exception thrown when image writing fails.</p>
 * <p>当图片写入失败时抛出的异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for image writing failures - 图片写入失败的异常</li>
 *   <li>Carries ImageErrorCode for programmatic error handling - 携带 ImageErrorCode 用于编程式错误处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     OpenImage.read(path);
 * } catch (ImageWriteException e) {
 *     System.err.println(e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable exception) - 线程安全: 是（不可变异常）</li>
 *   <li>Null-safe: No (message must not be null) - 空值安全: 否（消息不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public class ImageWriteException extends ImageIOException {

    private final Path path;

    /**
     * Create an image write exception
     * 创建图片写入异常
     *
     * @param message the error message | 错误消息
     */
    public ImageWriteException(String message) {
        super(message, ImageErrorCode.WRITE_FAILED);
        this.path = null;
    }

    /**
     * Create an image write exception with path
     * 创建带路径的图片写入异常
     *
     * @param path the file path | 文件路径
     */
    public ImageWriteException(Path path) {
        super("Failed to write image: " + path, ImageErrorCode.WRITE_FAILED);
        this.path = path;
    }

    /**
     * Create an image write exception with path and cause
     * 创建带路径和原因的图片写入异常
     *
     * @param path the file path | 文件路径
     * @param cause the cause | 原因
     */
    public ImageWriteException(Path path, Throwable cause) {
        super("Failed to write image: " + path, cause, ImageErrorCode.WRITE_FAILED);
        this.path = path;
    }

    /**
     * Create an image write exception with message and cause
     * 创建带消息和原因的图片写入异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public ImageWriteException(String message, Throwable cause) {
        super(message, cause, ImageErrorCode.WRITE_FAILED);
        this.path = null;
    }

    /**
     * Get the file path
     * 获取文件路径
     *
     * @return the file path or null | 文件路径或null
     */
    public Path getPath() {
        return path;
    }
}
