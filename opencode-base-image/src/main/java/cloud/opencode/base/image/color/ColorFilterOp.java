package cloud.opencode.base.image.color;

import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Color Filter Operations
 * 颜色滤镜操作工具类
 *
 * <p>Provides artistic color filter transformations including sepia tone
 * and color inversion.</p>
 * <p>提供艺术色彩滤镜变换，包括怀旧棕褐色调和反色。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sepia (nostalgic brown tone) filter - 怀旧棕褐色滤镜</li>
 *   <li>Color inversion filter (preserving alpha) - 反色滤镜（保留 Alpha 通道）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BufferedImage sepiaImage = ColorFilterOp.sepia(image);
 * BufferedImage invertedImage = ColorFilterOp.invert(image);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(n) where n = pixel count - 时间: O(n)，n 为像素数量</li>
 *   <li>Space: O(n) for output image - 空间: O(n) 用于输出图像</li>
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
 * @since JDK 25, opencode-base-image V1.0.3
 */
public final class ColorFilterOp {

    private ColorFilterOp() {
        throw new AssertionError("No ColorFilterOp instances");
    }

    /**
     * Apply sepia (nostalgic brown tone) filter to an image.
     * 对图像应用怀旧棕褐色滤镜。
     *
     * <p>Uses the standard sepia tone matrix:</p>
     * <pre>
     * newR = clamp(0.393*R + 0.769*G + 0.189*B)
     * newG = clamp(0.349*R + 0.686*G + 0.168*B)
     * newB = clamp(0.272*R + 0.534*G + 0.131*B)
     * </pre>
     * <p>Alpha channel is preserved.</p>
     * <p>Alpha 通道保持不变。</p>
     *
     * @param image the source image | 源图像
     * @return the sepia-filtered image | 应用棕褐色滤镜后的图像
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage sepia(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] src = PixelOp.getPixels(argb);

        BufferedImage output = PixelOp.createCompatible(argb);
        int[] dst = PixelOp.getPixels(output);

        for (int i = 0; i < src.length; i++) {
            int px = src[i];
            int a = PixelOp.alpha(px);
            int r = PixelOp.red(px);
            int g = PixelOp.green(px);
            int b = PixelOp.blue(px);

            int newR = PixelOp.clamp((int) Math.round(0.393 * r + 0.769 * g + 0.189 * b));
            int newG = PixelOp.clamp((int) Math.round(0.349 * r + 0.686 * g + 0.168 * b));
            int newB = PixelOp.clamp((int) Math.round(0.272 * r + 0.534 * g + 0.131 * b));

            dst[i] = PixelOp.argb(a, newR, newG, newB);
        }
        return output;
    }

    /**
     * Apply color inversion filter to an image.
     * 对图像应用反色滤镜。
     *
     * <p>Each RGB component is inverted: newVal = 255 - oldVal.
     * Alpha channel is preserved.</p>
     * <p>每个 RGB 分量被反转: newVal = 255 - oldVal。
     * Alpha 通道保持不变。</p>
     *
     * @param image the source image | 源图像
     * @return the color-inverted image | 反色后的图像
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage invert(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] src = PixelOp.getPixels(argb);

        BufferedImage output = PixelOp.createCompatible(argb);
        int[] dst = PixelOp.getPixels(output);

        for (int i = 0; i < src.length; i++) {
            int px = src[i];
            int a = PixelOp.alpha(px);
            int r = 255 - PixelOp.red(px);
            int g = 255 - PixelOp.green(px);
            int b = 255 - PixelOp.blue(px);
            dst[i] = PixelOp.argb(a, r, g, b);
        }
        return output;
    }
}
