package cloud.opencode.base.image.filter;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Median Blur Filter
 * 中值模糊滤波器
 *
 * <p>Applies median filtering to images using the Huang sliding histogram algorithm,
 * achieving O(W*H*K) time complexity instead of the naive O(W*H*K^2).</p>
 * <p>使用 Huang 滑动直方图算法对图像进行中值滤波，
 * 时间复杂度为 O(W*H*K)，优于朴素算法的 O(W*H*K^2)。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Effective salt-and-pepper noise removal - 有效去除椒盐噪声</li>
 *   <li>Edge-preserving smoothing - 保边平滑</li>
 *   <li>Huang sliding histogram for efficient computation - Huang 滑动直方图高效计算</li>
 *   <li>Independent R/G/B channel processing with alpha preservation - 独立 R/G/B 通道处理，Alpha 通道保持不变</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Remove salt-and-pepper noise with 3x3 median filter
 * BufferedImage filtered = MedianBlurOp.apply(noisyImage, 3);
 *
 * // Stronger filtering with 5x5 kernel
 * BufferedImage filtered = MedianBlurOp.apply(noisyImage, 5);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(W * H * K) via Huang sliding histogram - 时间: O(W * H * K) 通过 Huang 滑动直方图</li>
 *   <li>Space: O(W * H) + O(256) histogram - 空间: O(W * H) + O(256) 直方图</li>
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
public final class MedianBlurOp {

    private MedianBlurOp() {
        throw new AssertionError("No MedianBlurOp instances");
    }

    /**
     * Apply median filter to an image.
     * 对图像应用中值滤波。
     *
     * @param image      the source image | 源图像
     * @param kernelSize the kernel size (must be odd and &gt;= 3) | 核大小（必须为奇数且 &gt;= 3）
     * @return the filtered image | 滤波后的图像
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

        int[] outR = medianFilterChannel(channels[1], w, h, kernelSize);
        int[] outG = medianFilterChannel(channels[2], w, h, kernelSize);
        int[] outB = medianFilterChannel(channels[3], w, h, kernelSize);

        int[] result = ChannelOp.merge(alpha, outR, outG, outB);
        BufferedImage output = PixelOp.createCompatible(argb);
        int[] outPixels = PixelOp.getPixels(output);
        System.arraycopy(result, 0, outPixels, 0, result.length);
        return output;
    }

    /**
     * Apply Huang sliding histogram median filter to a single channel.
     * 对单个通道应用 Huang 滑动直方图中值滤波。
     *
     * <p>Algorithm: For each row, initialize the histogram with the first window.
     * Then slide the window right: add the new column, remove the leftmost column,
     * and find the median by scanning the histogram from bin 0 until the cumulative
     * count reaches the median position.</p>
     */
    private static int[] medianFilterChannel(int[] channel, int w, int h, int kernelSize) {
        int[] out = new int[w * h];
        int half = kernelSize / 2;
        int medianPos = (kernelSize * kernelSize) / 2;

        int[] histogram = new int[256];
        for (int y = 0; y < h; y++) {
            java.util.Arrays.fill(histogram, 0);
            int count = 0;

            // Initialize histogram for the first window centered at (0, y)
            for (int ky = -half; ky <= half; ky++) {
                int sy = clampIndex(y + ky, h);
                for (int kx = -half; kx <= half; kx++) {
                    int sx = clampIndex(kx, w);
                    histogram[channel[sy * w + sx]]++;
                    count++;
                }
            }

            // Find median for position (0, y)
            out[y * w] = findMedian(histogram, medianPos);

            // Slide window right across the row
            for (int x = 1; x < w; x++) {
                // Remove leftmost column (x - half - 1) and add rightmost column (x + half)
                int removeX = x - half - 1;
                int addX = x + half;
                int clampedRemoveX = clampIndex(removeX, w);
                int clampedAddX = clampIndex(addX, w);

                for (int ky = -half; ky <= half; ky++) {
                    int sy = clampIndex(y + ky, h);
                    histogram[channel[sy * w + clampedRemoveX]]--;
                    histogram[channel[sy * w + clampedAddX]]++;
                }

                out[y * w + x] = findMedian(histogram, medianPos);
            }
        }
        return out;
    }

    /**
     * Find the median value from a histogram.
     * Scans from bin 0 upward until the cumulative count exceeds medianPos.
     */
    private static int findMedian(int[] histogram, int medianPos) {
        int cumulative = 0;
        for (int i = 0; i < 256; i++) {
            cumulative += histogram[i];
            if (cumulative > medianPos) {
                return i;
            }
        }
        // Should not reach here with valid data
        return 255;
    }

    /**
     * Clamp an index to the range [0, size - 1].
     */
    private static int clampIndex(int index, int size) {
        if (index < 0) {
            return 0;
        }
        if (index >= size) {
            return size - 1;
        }
        return index;
    }

    private static void validateKernelSize(int kernelSize) {
        if (kernelSize < 3) {
            throw new ImageOperationException(
                    "kernelSize must be >= 3, got: " + kernelSize,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (kernelSize % 2 == 0) {
            throw new ImageOperationException(
                    "kernelSize must be odd, got: " + kernelSize,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }
}
