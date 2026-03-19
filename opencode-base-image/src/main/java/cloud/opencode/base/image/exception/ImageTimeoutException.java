package cloud.opencode.base.image.exception;

import java.time.Duration;

/**
 * Image Timeout Exception
 * 图片处理超时异常
 *
 * <p>Exception thrown when image processing times out.</p>
 * <p>当图片处理超时时抛出的异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for image processing timeouts - 图片处理超时的异常</li>
 *   <li>Carries ImageErrorCode for programmatic error handling - 携带 ImageErrorCode 用于编程式错误处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     OpenImage.read(path);
 * } catch (ImageTimeoutException e) {
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
public class ImageTimeoutException extends ImageResourceException {

    private final Duration timeout;

    /**
     * Create an image timeout exception
     * 创建图片超时异常
     *
     * @param message the error message | 错误消息
     */
    public ImageTimeoutException(String message) {
        super(message, ImageErrorCode.TIMEOUT);
        this.timeout = null;
    }

    /**
     * Create an image timeout exception with timeout
     * 创建带超时时间的图片超时异常
     *
     * @param message the error message | 错误消息
     * @param timeout the timeout duration | 超时时间
     */
    public ImageTimeoutException(String message, Duration timeout) {
        super(message + " (timeout: " + timeout + ")", ImageErrorCode.TIMEOUT);
        this.timeout = timeout;
    }

    /**
     * Create an image timeout exception with cause
     * 创建带原因的图片超时异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public ImageTimeoutException(String message, Throwable cause) {
        super(message, cause);
        this.timeout = null;
    }

    /**
     * Get the timeout duration
     * 获取超时时间
     *
     * @return the timeout duration or null | 超时时间或null
     */
    public Duration getTimeout() {
        return timeout;
    }
}
