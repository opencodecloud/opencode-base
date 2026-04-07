package cloud.opencode.base.image.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * Image Exception - Base exception for all image processing exceptions
 * 图片异常基类 - 所有图片处理异常的基类
 *
 * <p>Base exception class for all image processing exceptions.
 * Extends {@link OpenException} to integrate with the unified OpenCode exception hierarchy.</p>
 * <p>所有图片处理异常的基类。继承 {@link OpenException} 以集成到统一的 OpenCode 异常体系中。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base exception for all image processing errors - 所有图片处理错误的基础异常</li>
 *   <li>Carries ImageErrorCode for programmatic error handling - 携带 ImageErrorCode 用于编程式错误处理</li>
 *   <li>Integrates with OpenException (component="Image") - 集成 OpenException（组件="Image"）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     OpenImage.read(path);
 * } catch (ImageException e) {
 *     System.err.println(e.getRawMessage());
 *     ImageErrorCode code = e.getImageErrorCode();
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
 * @since JDK 25, opencode-base-image V1.0.3
 */
public class ImageException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Image error code enum
     * 图片错误码枚举
     */
    private final ImageErrorCode imageErrorCode;

    /**
     * Create an image exception with message and error code
     * 创建带消息和错误码的图片异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public ImageException(String message, ImageErrorCode errorCode) {
        super("Image", errorCode.name(), message, null);
        this.imageErrorCode = errorCode;
    }

    /**
     * Create an image exception with message, cause and error code
     * 创建带消息、原因和错误码的图片异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     * @param errorCode the error code | 错误码
     */
    public ImageException(String message, Throwable cause, ImageErrorCode errorCode) {
        super("Image", errorCode.name(), message, cause);
        this.imageErrorCode = errorCode;
    }

    /**
     * Create an image exception with message
     * 创建带消息的图片异常
     *
     * @param message the error message | 错误消息
     */
    public ImageException(String message) {
        this(message, ImageErrorCode.UNKNOWN);
    }

    /**
     * Create an image exception with message and cause
     * 创建带消息和原因的图片异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public ImageException(String message, Throwable cause) {
        this(message, cause, ImageErrorCode.UNKNOWN);
    }

    /**
     * Get the image error code enum
     * 获取图片错误码枚举
     *
     * <p>Returns the typed {@link ImageErrorCode} enum. For the String error code
     * inherited from {@link OpenException}, use {@link #getErrorCode()}.</p>
     * <p>返回类型化的 {@link ImageErrorCode} 枚举。若需要从 {@link OpenException}
     * 继承的 String 错误码，请使用 {@link #getErrorCode()}。</p>
     *
     * @return the image error code enum | 图片错误码枚举
     */
    public ImageErrorCode getImageErrorCode() {
        return imageErrorCode;
    }
}
