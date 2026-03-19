package cloud.opencode.base.image.exception;

/**
 * Image Format Exception
 * 图片格式异常
 *
 * <p>Exception thrown when image format is invalid or unsupported.</p>
 * <p>当图片格式无效或不支持时抛出的异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for invalid or unsupported image formats - 无效或不支持的图片格式的异常</li>
 *   <li>Carries ImageErrorCode for programmatic error handling - 携带 ImageErrorCode 用于编程式错误处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     OpenImage.read(path);
 * } catch (ImageFormatException e) {
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
public class ImageFormatException extends ImageException {

    private final String format;

    /**
     * Create an image format exception
     * 创建图片格式异常
     *
     * @param message the error message | 错误消息
     */
    public ImageFormatException(String message) {
        super(message, ImageErrorCode.UNSUPPORTED_FORMAT);
        this.format = null;
    }

    /**
     * Create an image format exception with format
     * 创建带格式的图片格式异常
     *
     * @param message the error message | 错误消息
     * @param format the image format | 图片格式
     */
    public ImageFormatException(String message, String format) {
        super(message, ImageErrorCode.UNSUPPORTED_FORMAT);
        this.format = format;
    }

    /**
     * Create an image format exception with error code
     * 创建带错误码的图片格式异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public ImageFormatException(String message, ImageErrorCode errorCode) {
        super(message, errorCode);
        this.format = null;
    }

    /**
     * Create an image format exception with cause
     * 创建带原因的图片格式异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public ImageFormatException(String message, Throwable cause) {
        super(message, cause, ImageErrorCode.UNSUPPORTED_FORMAT);
        this.format = null;
    }

    /**
     * Create exception for unsupported format
     * 创建不支持格式的异常
     *
     * @param format the unsupported format | 不支持的格式
     * @return the exception | 异常
     */
    public static ImageFormatException unsupported(String format) {
        return new ImageFormatException("Unsupported image format: " + format, format);
    }

    /**
     * Get the format
     * 获取格式
     *
     * @return the format or null | 格式或null
     */
    public String getFormat() {
        return format;
    }
}
