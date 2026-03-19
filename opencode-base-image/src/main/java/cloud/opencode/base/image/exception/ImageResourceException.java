package cloud.opencode.base.image.exception;

/**
 * Image Resource Exception
 * 图片资源异常
 *
 * <p>Exception thrown when image processing resources are exhausted.</p>
 * <p>当图片处理资源耗尽时抛出的异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for image resource exhaustion - 图片资源耗尽的异常</li>
 *   <li>Carries ImageErrorCode for programmatic error handling - 携带 ImageErrorCode 用于编程式错误处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     OpenImage.read(path);
 * } catch (ImageResourceException e) {
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
public class ImageResourceException extends ImageException {

    /**
     * Create an image resource exception
     * 创建图片资源异常
     *
     * @param message the error message | 错误消息
     */
    public ImageResourceException(String message) {
        super(message, ImageErrorCode.RESOURCE_UNAVAILABLE);
    }

    /**
     * Create an image resource exception with cause
     * 创建带原因的图片资源异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public ImageResourceException(String message, Throwable cause) {
        super(message, cause, ImageErrorCode.RESOURCE_UNAVAILABLE);
    }

    /**
     * Create an image resource exception with error code
     * 创建带错误码的图片资源异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public ImageResourceException(String message, ImageErrorCode errorCode) {
        super(message, errorCode);
    }

    /**
     * Create exception for too many requests
     * 创建请求过多的异常
     *
     * @return the exception | 异常
     */
    public static ImageResourceException tooManyRequests() {
        return new ImageResourceException("Too many concurrent requests", ImageErrorCode.TOO_MANY_REQUESTS);
    }

    /**
     * Create exception for out of memory
     * 创建内存不足的异常
     *
     * @return the exception | 异常
     */
    public static ImageResourceException outOfMemory() {
        return new ImageResourceException("Out of memory during image processing", ImageErrorCode.OUT_OF_MEMORY);
    }
}
