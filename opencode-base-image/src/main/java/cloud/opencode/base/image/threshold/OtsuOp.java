package cloud.opencode.base.image.threshold;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Otsu's Automatic Thresholding
 * 大津法自动阈值选取
 *
 * <p>Implements Otsu's method for computing the optimal global threshold that
 * maximizes inter-class variance between foreground and background pixels.</p>
 * <p>实现大津法，计算使前景和背景像素之间类间方差最大化的最优全局阈值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic optimal threshold computation - 自动最优阈值计算</li>
 *   <li>Histogram-based inter-class variance maximization - 基于直方图的类间方差最大化</li>
 *   <li>Combined compute + apply convenience method - 计算与应用一体化的便捷方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Automatic thresholding
 * BufferedImage binary = OtsuOp.apply(image);
 *
 * // Compute threshold from histogram
 * int[] histogram = new int[256];
 * // ... populate histogram ...
 * int threshold = OtsuOp.computeThreshold(histogram);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(n + 256) where n = pixel count - 时间: O(n + 256)，n 为像素数量</li>
 *   <li>Space: O(n) for output image - 空间: O(n) 用于输出图像</li>
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
public final class OtsuOp {

    private OtsuOp() {
        throw new AssertionError("No OtsuOp instances");
    }

    /**
     * Compute the optimal Otsu threshold from a 256-bin histogram.
     * 从 256 个 bin 的直方图中计算最优大津阈值。
     *
     * <p>Iterates over all possible thresholds [0, 255] and selects the one that
     * maximizes the inter-class variance: sigma_b^2 = w0 * w1 * (mu0 - mu1)^2.</p>
     * <p>遍历所有可能的阈值 [0, 255]，选取使类间方差最大化的阈值：
     * sigma_b^2 = w0 * w1 * (mu0 - mu1)^2。</p>
     *
     * @param histogram the 256-bin histogram (must have length 256) | 256 个 bin 的直方图（长度必须为 256）
     * @return the optimal threshold [0, 255] | 最优阈值 [0, 255]
     * @throws ImageOperationException if histogram is null or length is not 256 |
     *                                 当直方图为 null 或长度不为 256 时抛出
     */
    public static int computeThreshold(int[] histogram) {
        Objects.requireNonNull(histogram, "histogram must not be null");
        if (histogram.length != 256) {
            throw new ImageOperationException(
                    "Histogram length must be 256, got: " + histogram.length,
                    ImageErrorCode.INVALID_PARAMETERS);
        }

        // Total pixel count
        long total = 0;
        for (int count : histogram) {
            total += count;
        }
        if (total == 0) {
            return 0;
        }

        // Total weighted sum
        long totalSum = 0;
        for (int i = 0; i < 256; i++) {
            totalSum += (long) i * histogram[i];
        }

        long w0 = 0;         // cumulative count for class 0
        long sum0 = 0;       // cumulative weighted sum for class 0
        double maxVariance = -1.0;
        int bestThreshold = 0;

        for (int t = 0; t < 256; t++) {
            w0 += histogram[t];
            if (w0 == 0) {
                continue;
            }
            long w1 = total - w0;
            if (w1 == 0) {
                break;
            }

            sum0 += (long) t * histogram[t];
            double mu0 = (double) sum0 / w0;
            double mu1 = (double) (totalSum - sum0) / w1;
            double diff = mu0 - mu1;
            double variance = (double) w0 * w1 * diff * diff;

            if (variance > maxVariance) {
                maxVariance = variance;
                bestThreshold = t;
            }
        }

        return bestThreshold;
    }

    /**
     * Apply Otsu's automatic thresholding to an image.
     * 对图像应用大津法自动阈值处理。
     *
     * <p>Computes the grayscale histogram, finds the optimal Otsu threshold,
     * and applies binary thresholding using {@link ThresholdOp}.</p>
     * <p>计算灰度直方图，找到最优大津阈值，然后使用 {@link ThresholdOp} 应用二值化。</p>
     *
     * @param image the source image | 源图像
     * @return the binary thresholded image | 二值化后的图像
     * @throws ImageOperationException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage apply(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");

        // Convert to grayscale
        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] pixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(pixels);

        // Build histogram
        int[] histogram = new int[256];
        for (int v : gray) {
            histogram[v]++;
        }

        // Compute threshold and apply
        int threshold = computeThreshold(histogram);
        return ThresholdOp.apply(image, threshold, ThresholdOp.Mode.BINARY);
    }
}
