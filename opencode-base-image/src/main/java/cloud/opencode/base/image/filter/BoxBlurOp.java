package cloud.opencode.base.image.filter;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.IntegralImage;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Box Blur Filter
 * 均值模糊滤波器
 *
 * <p>Applies box blur (averaging filter) to images using integral images,
 * achieving O(1) per-pixel computation regardless of kernel size.</p>
 * <p>使用积分图对图像应用均值模糊（均值滤波器），
 * 无论核大小如何，每像素计算复杂度均为 O(1)。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>O(1) per-pixel box blur via integral image - 通过积分图实现 O(1) 每像素均值模糊</li>
 *   <li>Independent R/G/B channel processing - 独立 R/G/B 通道处理</li>
 *   <li>Alpha channel preservation - Alpha 通道保持不变</li>
 *   <li>Efficient for large kernel sizes - 对大核尺寸高效</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Mild blur with 3x3 kernel
 * BufferedImage blurred = BoxBlurOp.apply(image, 3);
 *
 * // Strong blur with 31x31 kernel
 * BufferedImage blurred = BoxBlurOp.apply(image, 31);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(W * H) regardless of kernel size - 时间: O(W * H) 与核大小无关</li>
 *   <li>Space: O(W * H) for integral image per channel - 空间: 每通道 O(W * H) 用于积分图</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class BoxBlurOp {

    private BoxBlurOp() {
        throw new AssertionError("No BoxBlurOp instances");
    }

    /**
     * Apply box blur to an image using integral images.
     * 使用积分图对图像应用均值模糊。
     *
     * @param image      the source image | 源图像
     * @param kernelSize the kernel size (must be odd and &gt;= 1) | 核大小（必须为奇数且 &gt;= 1）
     * @return the blurred image | 模糊后的图像
     * @throws ImageOperationException if image is null or kernelSize is invalid | 当图像为 null 或核大小无效时抛出
     */
    public static BufferedImage apply(BufferedImage image, int kernelSize) {
        Objects.requireNonNull(image, "image must not be null");
        validateKernelSize(kernelSize);

        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] src = PixelOp.getPixels(argb);

        int[][] channels = ChannelOp.split(src);
        int[] alpha = channels[0];

        int[] outR = boxBlurChannel(channels[1], w, h, kernelSize);
        int[] outG = boxBlurChannel(channels[2], w, h, kernelSize);
        int[] outB = boxBlurChannel(channels[3], w, h, kernelSize);

        int[] result = ChannelOp.merge(alpha, outR, outG, outB);
        BufferedImage output = PixelOp.createCompatible(argb);
        int[] outPixels = PixelOp.getPixels(output);
        System.arraycopy(result, 0, outPixels, 0, result.length);
        return output;
    }

    /**
     * Apply box blur to a single channel using an integral image.
     * 使用积分图对单个通道应用均值模糊。
     */
    private static int[] boxBlurChannel(int[] channel, int w, int h, int kernelSize) {
        IntegralImage integral = new IntegralImage(channel, w, h);
        int[] out = new int[w * h];
        int half = kernelSize / 2;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int x1 = Math.max(0, x - half);
                int y1 = Math.max(0, y - half);
                int x2 = Math.min(w - 1, x + half);
                int y2 = Math.min(h - 1, y + half);

                long sum = integral.regionSum(x1, y1, x2, y2);
                int area = (x2 - x1 + 1) * (y2 - y1 + 1);
                out[y * w + x] = PixelOp.clamp((int) ((sum + area / 2) / area));
            }
        }
        return out;
    }

    private static void validateKernelSize(int kernelSize) {
        if (kernelSize < 1) {
            throw new ImageOperationException(
                    "kernelSize must be >= 1, got: " + kernelSize,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (kernelSize % 2 == 0) {
            throw new ImageOperationException(
                    "kernelSize must be odd, got: " + kernelSize,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }
}
