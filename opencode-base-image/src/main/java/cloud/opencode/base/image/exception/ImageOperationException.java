package cloud.opencode.base.image.exception;

/**
 * Image Operation Exception
 * 图片操作异常
 *
 * <p>Exception thrown when image operations (resize, crop, rotate, etc.) fail.</p>
 * <p>当图片操作（缩放、裁剪、旋转等）失败时抛出的异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for image operation failures (resize, crop, etc.) - 图片操作失败的异常（缩放、裁剪等）</li>
 *   <li>Carries ImageErrorCode for programmatic error handling - 携带 ImageErrorCode 用于编程式错误处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     OpenImage.read(path);
 * } catch (ImageOperationException e) {
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
public class ImageOperationException extends ImageException {

    private final String operation;

    /**
     * Create an image operation exception
     * 创建图片操作异常
     *
     * @param message the error message | 错误消息
     */
    public ImageOperationException(String message) {
        super(message, ImageErrorCode.INVALID_PARAMETERS);
        this.operation = null;
    }

    /**
     * Create an image operation exception with operation name
     * 创建带操作名称的图片操作异常
     *
     * @param message the error message | 错误消息
     * @param operation the operation name | 操作名称
     */
    public ImageOperationException(String message, String operation) {
        super(message, ImageErrorCode.INVALID_PARAMETERS);
        this.operation = operation;
    }

    /**
     * Create an image operation exception with error code
     * 创建带错误码的图片操作异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public ImageOperationException(String message, ImageErrorCode errorCode) {
        super(message, errorCode);
        this.operation = null;
    }

    /**
     * Create an image operation exception with cause
     * 创建带原因的图片操作异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public ImageOperationException(String message, Throwable cause) {
        super(message, cause, ImageErrorCode.INVALID_PARAMETERS);
        this.operation = null;
    }

    /**
     * Create an image operation exception with cause and error code
     * 创建带原因和错误码的图片操作异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     * @param errorCode the error code | 错误码
     */
    public ImageOperationException(String message, Throwable cause, ImageErrorCode errorCode) {
        super(message, cause, errorCode);
        this.operation = null;
    }

    /**
     * Get the operation name
     * 获取操作名称
     *
     * @return the operation name or null | 操作名称或null
     */
    public String getOperation() {
        return operation;
    }
}
