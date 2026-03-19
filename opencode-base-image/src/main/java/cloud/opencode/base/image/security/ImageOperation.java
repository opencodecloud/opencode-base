package cloud.opencode.base.image.security;

import java.awt.image.BufferedImage;

/**
 * Image Operation
 * 图片操作函数接口
 *
 * <p>Functional interface for image operations with exception handling.</p>
 * <p>带异常处理的图片操作函数接口。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ImageOperation op = image -> ResizeOp.resize(image, 800, 600);
 * BufferedImage result = op.apply(sourceImage);
 *
 * // Chain operations
 * ImageOperation chain = op1.andThen(op2).andThen(op3);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface for image transformations - 图片变换的函数接口</li>
 *   <li>Chainable via andThen composition - 通过 andThen 组合可链式调用</li>
 *   <li>Exception-aware apply method - 支持异常的 apply 方法</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless functional interface) - 线程安全: 是（无状态函数接口）</li>
 *   <li>Null-safe: No (throws on null image) - 空值安全: 否（null 图片抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@FunctionalInterface
public interface ImageOperation {

    /**
     * Apply operation to image
     * 应用操作到图片
     *
     * @param image the input image | 输入图片
     * @return the processed image | 处理后的图片
     * @throws Exception if operation fails | 如果操作失败
     */
    BufferedImage apply(BufferedImage image) throws Exception;

    /**
     * Chain with another operation
     * 与另一个操作链接
     *
     * @param after the next operation | 下一个操作
     * @return the combined operation | 组合后的操作
     */
    default ImageOperation andThen(ImageOperation after) {
        return image -> after.apply(this.apply(image));
    }

    /**
     * Compose with another operation
     * 与另一个操作组合
     *
     * @param before the previous operation | 前一个操作
     * @return the combined operation | 组合后的操作
     */
    default ImageOperation compose(ImageOperation before) {
        return image -> this.apply(before.apply(image));
    }

    /**
     * Create identity operation (returns input unchanged)
     * 创建恒等操作（返回不变的输入）
     *
     * @return the identity operation | 恒等操作
     */
    static ImageOperation identity() {
        return image -> image;
    }

    /**
     * Wrap operation with timeout handling
     * 使用超时处理包装操作
     *
     * @param operation the operation | 操作
     * @param timeoutMs timeout in milliseconds | 超时时间（毫秒）
     * @return the wrapped operation | 包装后的操作
     */
    static ImageOperation withTimeout(ImageOperation operation, long timeoutMs) {
        return image -> {
            // Simple timeout wrapper - could be enhanced with Future
            long startTime = System.currentTimeMillis();
            BufferedImage result = operation.apply(image);
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > timeoutMs) {
                throw new RuntimeException("Operation timed out after " + elapsed + "ms");
            }
            return result;
        };
    }
}
