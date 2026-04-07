package cloud.opencode.base.image.kernel;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Objects;

/**
 * Efficient Pixel Access Utilities
 * 高效像素访问工具类
 *
 * <p>Provides zero-copy pixel array access for TYPE_INT_ARGB images,
 * pixel channel extraction/composition, and image creation helpers.</p>
 * <p>提供 TYPE_INT_ARGB 图像的零拷贝像素数组访问、像素通道提取/合成以及图像创建辅助方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Zero-copy pixel array access via DataBufferInt - 通过 DataBufferInt 实现零拷贝像素访问</li>
 *   <li>ARGB channel extraction and composition - ARGB 通道提取与合成</li>
 *   <li>Image type conversion to TYPE_INT_ARGB - 图像类型转换为 TYPE_INT_ARGB</li>
 *   <li>Compatible image creation - 兼容图像创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BufferedImage img = PixelOp.ensureArgb(sourceImage);
 * int[] pixels = PixelOp.getPixels(img);
 * int r = PixelOp.red(pixels[0]);
 * int packed = PixelOp.argb(255, r, 0, 0);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for channel ops, O(w*h) for ensureArgb conversion - 时间复杂度: 通道操作 O(1)，ensureArgb 转换 O(w*h)</li>
 *   <li>Zero-copy pixel access when image is already TYPE_INT_ARGB - 当图像已是 TYPE_INT_ARGB 时零拷贝像素访问</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class PixelOp {

    private PixelOp() {
        throw new AssertionError("No PixelOp instances");
    }

    /**
     * Ensure the image is TYPE_INT_ARGB, converting if necessary.
     * 确保图像为 TYPE_INT_ARGB，必要时转换。
     *
     * @param image the source image | 源图像
     * @return a TYPE_INT_ARGB image (same instance if already correct type) | TYPE_INT_ARGB 图像（类型正确时返回原实例）
     * @throws ImageOperationException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage ensureArgb(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");
        if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            return image;
        }
        BufferedImage argb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = argb.createGraphics();
        try {
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        return argb;
    }

    /**
     * Get the backing int[] pixel array (zero-copy) for a TYPE_INT_ARGB image.
     * 获取 TYPE_INT_ARGB 图像的底层 int[] 像素数组（零拷贝）。
     *
     * @param image a TYPE_INT_ARGB image | TYPE_INT_ARGB 图像
     * @return the backing pixel array | 底层像素数组
     * @throws ImageOperationException if image is null or not TYPE_INT_ARGB | 当图像为 null 或不是 TYPE_INT_ARGB 时抛出
     */
    public static int[] getPixels(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            throw new ImageOperationException(
                    "Image must be TYPE_INT_ARGB, got type: " + image.getType(),
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        // Guard against sub-images where the raster has a non-zero offset
        if (image.getRaster().getSampleModelTranslateX() != 0
                || image.getRaster().getSampleModelTranslateY() != 0) {
            throw new ImageOperationException(
                    "Sub-images are not supported for zero-copy pixel access; "
                            + "call PixelOp.ensureArgb() first to obtain a standalone copy",
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    /**
     * Create a new TYPE_INT_ARGB image with the same dimensions as the source.
     * 创建与源图像尺寸相同的 TYPE_INT_ARGB 空图像。
     *
     * @param source the source image | 源图像
     * @return a new empty TYPE_INT_ARGB image | 新的空 TYPE_INT_ARGB 图像
     * @throws ImageOperationException if source is null | 当源图像为 null 时抛出
     */
    public static BufferedImage createCompatible(BufferedImage source) {
        Objects.requireNonNull(source, "source must not be null");
        return new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Create a new TYPE_INT_ARGB image with the specified dimensions.
     * 创建指定尺寸的 TYPE_INT_ARGB 空图像。
     *
     * @param width  the image width | 图像宽度
     * @param height the image height | 图像高度
     * @return a new empty TYPE_INT_ARGB image | 新的空 TYPE_INT_ARGB 图像
     * @throws ImageOperationException if width or height is not positive | 当宽度或高度不为正数时抛出
     */
    public static BufferedImage createArgb(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new ImageOperationException(
                    "Width and height must be positive, got: " + width + "x" + height,
                    ImageErrorCode.INVALID_DIMENSIONS);
        }
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Clamp a pixel value to the [0, 255] range.
     * 将像素值裁剪到 [0, 255] 范围。
     *
     * @param value the value to clamp | 要裁剪的值
     * @return the clamped value in [0, 255] | 裁剪后的 [0, 255] 范围值
     */
    public static int clamp(int value) {
        return Math.clamp(value, 0, 255);
    }

    /**
     * Extract the alpha component from an ARGB pixel.
     * 从 ARGB 像素中提取 Alpha 分量。
     *
     * @param argb the ARGB pixel value | ARGB 像素值
     * @return the alpha component [0, 255] | Alpha 分量 [0, 255]
     */
    public static int alpha(int argb) {
        return (argb >> 24) & 0xFF;
    }

    /**
     * Extract the red component from an ARGB pixel.
     * 从 ARGB 像素中提取红色分量。
     *
     * @param argb the ARGB pixel value | ARGB 像素值
     * @return the red component [0, 255] | 红色分量 [0, 255]
     */
    public static int red(int argb) {
        return (argb >> 16) & 0xFF;
    }

    /**
     * Extract the green component from an ARGB pixel.
     * 从 ARGB 像素中提取绿色分量。
     *
     * @param argb the ARGB pixel value | ARGB 像素值
     * @return the green component [0, 255] | 绿色分量 [0, 255]
     */
    public static int green(int argb) {
        return (argb >> 8) & 0xFF;
    }

    /**
     * Extract the blue component from an ARGB pixel.
     * 从 ARGB 像素中提取蓝色分量。
     *
     * @param argb the ARGB pixel value | ARGB 像素值
     * @return the blue component [0, 255] | 蓝色分量 [0, 255]
     */
    public static int blue(int argb) {
        return argb & 0xFF;
    }

    /**
     * Compose an ARGB pixel from individual components.
     * 从各分量合成 ARGB 像素值。
     *
     * @param a the alpha component [0, 255] | Alpha 分量 [0, 255]
     * @param r the red component [0, 255] | 红色分量 [0, 255]
     * @param g the green component [0, 255] | 绿色分量 [0, 255]
     * @param b the blue component [0, 255] | 蓝色分量 [0, 255]
     * @return the packed ARGB pixel value | 打包的 ARGB 像素值
     */
    public static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
