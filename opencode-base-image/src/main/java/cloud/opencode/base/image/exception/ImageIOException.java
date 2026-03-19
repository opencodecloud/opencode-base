package cloud.opencode.base.image.exception;

/**
 * Image IO Exception
 * 图片IO异常
 *
 * <p>Exception thrown when image IO operations fail.</p>
 * <p>当图片IO操作失败时抛出的异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for image read/write IO failures - 图片读写IO失败的异常</li>
 *   <li>Carries ImageErrorCode for programmatic error handling - 携带 ImageErrorCode 用于编程式错误处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     OpenImage.read(path);
 * } catch (ImageIOException e) {
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
public class ImageIOException extends ImageException {

    /**
     * Create an image IO exception
     * 创建图片IO异常
     *
     * @param message the error message | 错误消息
     */
    public ImageIOException(String message) {
        super(message, ImageErrorCode.IO_ERROR);
    }

    /**
     * Create an image IO exception with cause
     * 创建带原因的图片IO异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public ImageIOException(String message, Throwable cause) {
        super(message, cause, ImageErrorCode.IO_ERROR);
    }

    /**
     * Create an image IO exception with error code
     * 创建带错误码的图片IO异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public ImageIOException(String message, ImageErrorCode errorCode) {
        super(message, errorCode);
    }

    /**
     * Create an image IO exception with cause and error code
     * 创建带原因和错误码的图片IO异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     * @param errorCode the error code | 错误码
     */
    public ImageIOException(String message, Throwable cause, ImageErrorCode errorCode) {
        super(message, cause, errorCode);
    }
}
