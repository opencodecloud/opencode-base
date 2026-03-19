package cloud.opencode.base.image.exception;

/**
 * Image Too Large Exception
 * 图片过大异常
 *
 * <p>Exception thrown when image dimensions or file size exceed limits.</p>
 * <p>当图片尺寸或文件大小超出限制时抛出的异常。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for images exceeding size limits - 图片超出大小限制的异常</li>
 *   <li>Carries ImageErrorCode for programmatic error handling - 携带 ImageErrorCode 用于编程式错误处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch specific exception
 * try {
 *     OpenImage.read(path);
 * } catch (ImageTooLargeException e) {
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
public class ImageTooLargeException extends ImageValidationException {

    private final int width;
    private final int height;
    private final int maxWidth;
    private final int maxHeight;
    private final long fileSize;
    private final long maxFileSize;

    /**
     * Create an image too large exception for dimensions
     * 创建尺寸过大的图片异常
     *
     * @param width actual width | 实际宽度
     * @param height actual height | 实际高度
     * @param maxWidth maximum width | 最大宽度
     * @param maxHeight maximum height | 最大高度
     */
    public ImageTooLargeException(int width, int height, int maxWidth, int maxHeight) {
        super(String.format("Image too large: %dx%d (max: %dx%d)",
            width, height, maxWidth, maxHeight), ImageErrorCode.IMAGE_TOO_LARGE);
        this.width = width;
        this.height = height;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.fileSize = 0;
        this.maxFileSize = 0;
    }

    /**
     * Create an image too large exception for file size
     * 创建文件过大的图片异常
     *
     * @param fileSize actual file size | 实际文件大小
     * @param maxFileSize maximum file size | 最大文件大小
     */
    public ImageTooLargeException(long fileSize, long maxFileSize) {
        super(String.format("File too large: %d bytes (max: %d bytes)",
            fileSize, maxFileSize), ImageErrorCode.FILE_TOO_LARGE);
        this.width = 0;
        this.height = 0;
        this.maxWidth = 0;
        this.maxHeight = 0;
        this.fileSize = fileSize;
        this.maxFileSize = maxFileSize;
    }

    /**
     * Create an image too large exception with message
     * 创建带消息的图片过大异常
     *
     * @param message the error message | 错误消息
     */
    public ImageTooLargeException(String message) {
        super(message, ImageErrorCode.IMAGE_TOO_LARGE);
        this.width = 0;
        this.height = 0;
        this.maxWidth = 0;
        this.maxHeight = 0;
        this.fileSize = 0;
        this.maxFileSize = 0;
    }

    /**
     * Get actual width
     * 获取实际宽度
     *
     * @return the width | 宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get actual height
     * 获取实际高度
     *
     * @return the height | 高度
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get maximum width
     * 获取最大宽度
     *
     * @return the max width | 最大宽度
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * Get maximum height
     * 获取最大高度
     *
     * @return the max height | 最大高度
     */
    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * Get actual file size
     * 获取实际文件大小
     *
     * @return the file size | 文件大小
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Get maximum file size
     * 获取最大文件大小
     *
     * @return the max file size | 最大文件大小
     */
    public long getMaxFileSize() {
        return maxFileSize;
    }
}
