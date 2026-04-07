package cloud.opencode.base.image.histogram;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Histogram Computation Operations
 * 直方图计算操作工具类
 *
 * <p>Computes histograms for grayscale and color images. Provides per-channel
 * statistics including counts, min, max, and mean values.</p>
 * <p>计算灰度图像和彩色图像的直方图。提供每个通道的统计信息，
 * 包括计数、最小值、最大值和均值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Grayscale histogram computation - 灰度直方图计算</li>
 *   <li>Per-channel (R, G, B) histogram computation - 逐通道（R、G、B）直方图计算</li>
 *   <li>Statistics: min, max, mean per histogram - 统计: 每个直方图的最小值、最大值、均值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BufferedImage image = ImageIO.read(new File("photo.png"));
 * HistogramOp.Histogram gray = HistogramOp.computeGray(image);
 * System.out.println("Mean: " + gray.mean());
 *
 * HistogramOp.Histogram[] rgb = HistogramOp.compute(image);
 * // rgb[0] = Red, rgb[1] = Green, rgb[2] = Blue
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(w * h) - 时间复杂度: O(w * h)</li>
 *   <li>Space complexity: O(256) per histogram - 空间复杂度: 每个直方图 O(256)</li>
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
public final class HistogramOp {

    private HistogramOp() {
        throw new AssertionError("No HistogramOp instances");
    }

    /**
     * Histogram data for a single channel.
     * 单通道直方图数据。
     *
     * @param counts  the count of pixels at each intensity level [0..255] | 每个灰度级 [0..255] 的像素计数
     * @param channel the channel index (0=gray, 1=R, 2=G, 3=B) | 通道索引（0=灰度, 1=R, 2=G, 3=B）
     * @param min     the minimum intensity value present | 存在的最小灰度值
     * @param max     the maximum intensity value present | 存在的最大灰度值
     * @param mean    the mean intensity value | 灰度均值
     */
    public record Histogram(int[] counts, int channel, int min, int max, double mean) {

        /**
         * Compact constructor with validation.
         * 带验证的紧凑构造器。
         */
        public Histogram {
            Objects.requireNonNull(counts, "counts must not be null");
            if (counts.length != 256) {
                throw new ImageOperationException(
                        "counts length must be 256, got: " + counts.length,
                        ImageErrorCode.INVALID_PARAMETERS);
            }
            // Defensive copy
            counts = counts.clone();
        }

        /**
         * Return a defensive copy of the counts array.
         * 返回计数数组的防御性副本。
         *
         * @return a copy of the counts array | 计数数组的副本
         */
        @Override
        public int[] counts() {
            return counts.clone();
        }
    }

    /**
     * Compute the grayscale histogram of an image.
     * 计算图像的灰度直方图。
     *
     * <p>The image is first converted to grayscale using ITU-R BT.601 coefficients.</p>
     * <p>图像首先使用 ITU-R BT.601 系数转换为灰度。</p>
     *
     * @param image the source image | 源图像
     * @return the grayscale histogram (channel=0) | 灰度直方图（channel=0）
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static Histogram computeGray(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] pixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(pixels);

        int[] counts = new int[256];
        long sum = 0;
        int min = 255;
        int max = 0;

        for (int v : gray) {
            counts[v]++;
            sum += v;
            if (v < min) {
                min = v;
            }
            if (v > max) {
                max = v;
            }
        }

        double mean = (double) sum / gray.length;
        return new Histogram(counts, 0, min, max, mean);
    }

    /**
     * Compute per-channel (R, G, B) histograms of an image.
     * 计算图像的逐通道（R、G、B）直方图。
     *
     * @param image the source image | 源图像
     * @return an array of 3 histograms: [0]=Red(channel=1), [1]=Green(channel=2), [2]=Blue(channel=3)
     *         | 3 个直方图数组：[0]=红色(channel=1), [1]=绿色(channel=2), [2]=蓝色(channel=3)
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static Histogram[] compute(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] pixels = PixelOp.getPixels(argb);

        int[][] counts = new int[3][256];
        long[] sums = new long[3];
        int[] mins = {255, 255, 255};
        int[] maxs = {0, 0, 0};

        for (int px : pixels) {
            int r = PixelOp.red(px);
            int g = PixelOp.green(px);
            int b = PixelOp.blue(px);

            counts[0][r]++;
            counts[1][g]++;
            counts[2][b]++;

            sums[0] += r;
            sums[1] += g;
            sums[2] += b;

            if (r < mins[0]) { mins[0] = r; }
            if (r > maxs[0]) { maxs[0] = r; }
            if (g < mins[1]) { mins[1] = g; }
            if (g > maxs[1]) { maxs[1] = g; }
            if (b < mins[2]) { mins[2] = b; }
            if (b > maxs[2]) { maxs[2] = b; }
        }

        int totalPixels = pixels.length;
        Histogram[] result = new Histogram[3];
        for (int c = 0; c < 3; c++) {
            double mean = (double) sums[c] / totalPixels;
            result[c] = new Histogram(counts[c], c + 1, mins[c], maxs[c], mean);
        }
        return result;
    }
}
