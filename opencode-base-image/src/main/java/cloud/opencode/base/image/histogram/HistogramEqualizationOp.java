package cloud.opencode.base.image.histogram;

import cloud.opencode.base.image.color.ColorSpaceOp;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.LookupTableOp;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Histogram Equalization Operations
 * 直方图均衡化操作工具类
 *
 * <p>Provides histogram equalization for grayscale and color images.
 * Grayscale equalization uses a CDF-based lookup table; color equalization
 * converts to HSV and equalizes only the V (value/brightness) channel.</p>
 * <p>提供灰度图像和彩色图像的直方图均衡化。
 * 灰度均衡化使用基于 CDF 的查找表；彩色均衡化转换为 HSV 并仅均衡 V（亮度）通道。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Grayscale histogram equalization via LUT - 通过查找表实现灰度直方图均衡化</li>
 *   <li>Color histogram equalization via HSV V-channel - 通过 HSV V 通道实现彩色直方图均衡化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BufferedImage equalized = HistogramEqualizationOp.apply(grayImage);
 * BufferedImage colorEqualized = HistogramEqualizationOp.applyColor(colorImage);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(w * h) - 时间复杂度: O(w * h)</li>
 *   <li>Space complexity: O(w * h) for output image - 空间复杂度: O(w * h) 输出图像</li>
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
public final class HistogramEqualizationOp {

    private HistogramEqualizationOp() {
        throw new AssertionError("No HistogramEqualizationOp instances");
    }

    /**
     * Apply histogram equalization to a grayscale image.
     * 对灰度图像应用直方图均衡化。
     *
     * <p>Algorithm: compute histogram, build CDF, create LUT where
     * LUT[i] = round(CDF[i] * 255 / totalPixels), then apply via LookupTableOp.</p>
     * <p>算法：计算直方图，构建 CDF，创建查找表
     * LUT[i] = round(CDF[i] * 255 / totalPixels)，然后通过 LookupTableOp 应用。</p>
     *
     * @param image the source image | 源图像
     * @return the equalized image | 均衡化后的图像
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage apply(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");

        // Convert to grayscale
        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] pixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(pixels);

        // Compute histogram
        int[] histogram = new int[256];
        for (int v : gray) {
            histogram[v]++;
        }

        // Build CDF
        int totalPixels = gray.length;
        int[] cdf = new int[256];
        cdf[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + histogram[i];
        }

        // Find minimum non-zero CDF value
        int cdfMin = 0;
        for (int i = 0; i < 256; i++) {
            if (cdf[i] != 0) {
                cdfMin = cdf[i];
                break;
            }
        }

        // Build LUT: equalized[i] = round((cdf[i] - cdfMin) / (totalPixels - cdfMin) * 255)
        int[] lut = new int[256];
        int denominator = totalPixels - cdfMin;
        if (denominator > 0) {
            for (int i = 0; i < 256; i++) {
                if (cdf[i] == 0) {
                    lut[i] = 0;
                } else {
                    lut[i] = PixelOp.clamp((int) Math.round(
                            (double) (cdf[i] - cdfMin) / denominator * 255.0));
                }
            }
        }
        // If denominator == 0, all pixels are same value — LUT stays at 0

        // Apply LUT to the grayscale image (not the original color image)
        BufferedImage grayImage = PixelOp.createArgb(argb.getWidth(), argb.getHeight());
        int[] grayArgb = ChannelOp.grayToArgb(gray);
        System.arraycopy(grayArgb, 0, PixelOp.getPixels(grayImage), 0, grayArgb.length);
        return LookupTableOp.apply(grayImage, lut);
    }

    /**
     * Apply histogram equalization to a color image via HSV V-channel.
     * 通过 HSV V 通道对彩色图像应用直方图均衡化。
     *
     * <p>Algorithm: convert RGB to HSV, equalize the V channel histogram,
     * convert HSV back to RGB. Hue and Saturation are preserved.</p>
     * <p>算法：将 RGB 转换为 HSV，均衡化 V 通道直方图，
     * 将 HSV 转换回 RGB。色相和饱和度保持不变。</p>
     *
     * @param image the source color image | 源彩色图像
     * @return the equalized color image | 均衡化后的彩色图像
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage applyColor(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] pixels = PixelOp.getPixels(argb);
        int totalPixels = pixels.length;

        // RGB -> HSV
        float[][] hsv = ColorSpaceOp.toHsv(pixels);
        float[] vChannel = hsv[2];

        // Compute histogram of V channel (quantized to 0..255)
        int[] histogram = new int[256];
        for (float v : vChannel) {
            int bin = PixelOp.clamp(Math.round(v * 255.0f));
            histogram[bin]++;
        }

        // Build CDF
        int[] cdf = new int[256];
        cdf[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + histogram[i];
        }

        // Find minimum non-zero CDF
        int cdfMin = 0;
        for (int i = 0; i < 256; i++) {
            if (cdf[i] != 0) {
                cdfMin = cdf[i];
                break;
            }
        }

        // Build equalization LUT for V channel
        float[] lutV = new float[256];
        int denominator = totalPixels - cdfMin;
        if (denominator > 0) {
            for (int i = 0; i < 256; i++) {
                if (cdf[i] == 0) {
                    lutV[i] = 0.0f;
                } else {
                    lutV[i] = (float) ((double) (cdf[i] - cdfMin) / denominator);
                }
            }
        }

        // Apply equalized V
        for (int i = 0; i < vChannel.length; i++) {
            int bin = PixelOp.clamp(Math.round(vChannel[i] * 255.0f));
            vChannel[i] = lutV[bin];
        }

        // HSV -> RGB
        int[] equalizedPixels = ColorSpaceOp.fromHsv(hsv);

        // Write to output image
        BufferedImage output = PixelOp.createArgb(w, h);
        int[] dst = PixelOp.getPixels(output);
        System.arraycopy(equalizedPixels, 0, dst, 0, equalizedPixels.length);
        return output;
    }
}
