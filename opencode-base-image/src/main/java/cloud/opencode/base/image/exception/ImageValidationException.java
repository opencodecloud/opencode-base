package cloud.opencode.base.image.exception;

/**
 * Image Validation Exception
 * 图片验证异常
 *
 * <p>Exception thrown when image validation fails.</p>
 * <p>当图片验证失败时抛出的异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for image validation failures - 图片验证失败的异常</li>
 *   <li>Carries ImageErrorCode for programmatic error handling - 携带 ImageErrorCode 用于编程式错误处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     OpenImage.read(path);
 * } catch (ImageValidationException e) {
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
public class ImageValidationException extends ImageException {

    /**
     * Create an image validation exception
     * 创建图片验证异常
     *
     * @param message the error message | 错误消息
     */
    public ImageValidationException(String message) {
        super(message, ImageErrorCode.VALIDATION_FAILED);
    }

    /**
     * Create an image validation exception with cause
     * 创建带原因的图片验证异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public ImageValidationException(String message, Throwable cause) {
        super(message, cause, ImageErrorCode.VALIDATION_FAILED);
    }

    /**
     * Create an image validation exception with error code
     * 创建带错误码的图片验证异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public ImageValidationException(String message, ImageErrorCode errorCode) {
        super(message, errorCode);
    }
}
