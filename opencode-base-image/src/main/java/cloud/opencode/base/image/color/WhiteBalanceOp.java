package cloud.opencode.base.image.color;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Gray World White Balance Operation
 * 灰度世界白平衡操作工具类
 *
 * <p>Implements the Gray World assumption for automatic white balance correction.
 * Each color channel is scaled so that its mean equals the overall gray mean.</p>
 * <p>实现灰度世界假设的自动白平衡校正。
 * 将每个颜色通道进行缩放，使其均值等于总体灰度均值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Gray World automatic white balance - 灰度世界自动白平衡</li>
 *   <li>Per-channel mean normalization - 逐通道均值归一化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BufferedImage balanced = WhiteBalanceOp.apply(image);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(n) where n = pixel count (two passes) - 时间: O(n)，n 为像素数量（两次遍历）</li>
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
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class WhiteBalanceOp {

    private WhiteBalanceOp() {
        throw new AssertionError("No WhiteBalanceOp instances");
    }

    /**
     * Apply Gray World white balance correction to an image.
     * 对图像应用灰度世界白平衡校正。
     *
     * <p>Computes the mean of each RGB channel, then scales each channel
     * so that its mean equals the overall gray mean (average of the three means).</p>
     * <p>计算每个 RGB 通道的均值，然后缩放每个通道使其均值等于总体灰度均值（三个均值的平均值）。</p>
     *
     * @param image the source image | 源图像
     * @return the white-balanced image | 白平衡校正后的图像
     * @throws ImageOperationException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage apply(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] src = PixelOp.getPixels(argb);
        int len = src.length;

        // First pass: compute channel means
        long sumR = 0, sumG = 0, sumB = 0;
        for (int px : src) {
            sumR += (px >> 16) & 0xFF;
            sumG += (px >> 8) & 0xFF;
            sumB += px & 0xFF;
        }

        double meanR = (double) sumR / len;
        double meanG = (double) sumG / len;
        double meanB = (double) sumB / len;
        double grayMean = (meanR + meanG + meanB) / 3.0;

        // Compute scale factors (guard against division by zero for fully black channels)
        double scaleR = (meanR > 0.0) ? (grayMean / meanR) : 1.0;
        double scaleG = (meanG > 0.0) ? (grayMean / meanG) : 1.0;
        double scaleB = (meanB > 0.0) ? (grayMean / meanB) : 1.0;

        // Second pass: apply scaling
        BufferedImage output = PixelOp.createArgb(w, h);
        int[] dst = PixelOp.getPixels(output);

        for (int i = 0; i < len; i++) {
            int px = src[i];
            int a = (px >> 24) & 0xFF;
            int r = PixelOp.clamp((int) Math.round(((px >> 16) & 0xFF) * scaleR));
            int g = PixelOp.clamp((int) Math.round(((px >> 8) & 0xFF) * scaleG));
            int b = PixelOp.clamp((int) Math.round((px & 0xFF) * scaleB));
            dst[i] = PixelOp.argb(a, r, g, b);
        }
        return output;
    }
}
