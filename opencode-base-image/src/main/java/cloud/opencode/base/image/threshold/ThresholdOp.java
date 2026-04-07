package cloud.opencode.base.image.threshold;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.LookupTableOp;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Fixed Threshold Operations
 * 固定阈值操作工具类
 *
 * <p>Applies fixed-value thresholding to grayscale images with multiple modes
 * including binary, inverse binary, truncate, to-zero and inverse to-zero.</p>
 * <p>对灰度图像应用固定值阈值处理，支持多种模式：
 * 二值化、反向二值化、截断、置零和反向置零。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Five threshold modes: BINARY, BINARY_INV, TRUNC, TOZERO, TOZERO_INV - 五种阈值模式</li>
 *   <li>LUT-based O(n) implementation - 基于查找表的 O(n) 实现</li>
 *   <li>Automatic grayscale conversion - 自动灰度转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Binary thresholding
 * BufferedImage binary = ThresholdOp.apply(image, 128);
 *
 * // Inverse binary thresholding
 * BufferedImage inv = ThresholdOp.apply(image, 128, ThresholdOp.Mode.BINARY_INV);
 *
 * // Truncate mode
 * BufferedImage trunc = ThresholdOp.apply(image, 200, ThresholdOp.Mode.TRUNC);
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
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class ThresholdOp {

    private ThresholdOp() {
        throw new AssertionError("No ThresholdOp instances");
    }

    /**
     * Threshold mode enumeration.
     * 阈值模式枚举。
     */
    public enum Mode {
        /** Below threshold to 0, at or above to 255 | 低于阈值置 0，大于等于阈值置 255 */
        BINARY,
        /** Below threshold to 255, at or above to 0 | 低于阈值置 255，大于等于阈值置 0 */
        BINARY_INV,
        /** Below threshold keep, at or above to threshold | 低于阈值保持，大于等于阈值置为阈值 */
        TRUNC,
        /** Below threshold to 0, at or above keep | 低于阈值置 0，大于等于阈值保持 */
        TOZERO,
        /** Below threshold keep, at or above to 0 | 低于阈值保持，大于等于阈值置 0 */
        TOZERO_INV
    }

    /**
     * Apply fixed threshold to a grayscale image with the specified mode.
     * 使用指定模式对灰度图像应用固定阈值。
     *
     * <p>The image is first converted to grayscale, then a lookup table is built
     * according to the mode and applied to all pixels.</p>
     * <p>图像先转换为灰度，然后根据模式构建查找表并应用于所有像素。</p>
     *
     * @param image     the source image | 源图像
     * @param threshold the threshold value [0, 255] | 阈值 [0, 255]
     * @param mode      the threshold mode | 阈值模式
     * @return the thresholded image | 阈值处理后的图像
     * @throws ImageOperationException if image is null, threshold out of range, or mode is null |
     *                                 当图像为 null、阈值越界或模式为 null 时抛出
     */
    public static BufferedImage apply(BufferedImage image, int threshold, Mode mode) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(mode, "mode must not be null");
        validateThreshold(threshold);

        // Convert to grayscale ARGB
        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] srcPixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(srcPixels);

        // Build grayscale image for LUT application
        int[] grayArgb = ChannelOp.grayToArgb(gray);
        BufferedImage grayImage = PixelOp.createArgb(w, h);
        int[] grayDst = PixelOp.getPixels(grayImage);
        System.arraycopy(grayArgb, 0, grayDst, 0, grayArgb.length);

        // Build LUT and apply
        int[] lut = buildLut(threshold, mode);
        return LookupTableOp.apply(grayImage, lut);
    }

    /**
     * Apply BINARY mode threshold (convenience method).
     * 应用 BINARY 模式阈值（便捷方法）。
     *
     * <p>Equivalent to {@code apply(image, threshold, Mode.BINARY)}.</p>
     * <p>等价于 {@code apply(image, threshold, Mode.BINARY)}。</p>
     *
     * @param image     the source image | 源图像
     * @param threshold the threshold value [0, 255] | 阈值 [0, 255]
     * @return the binary thresholded image | 二值化后的图像
     * @throws ImageOperationException if image is null or threshold out of range |
     *                                 当图像为 null 或阈值越界时抛出
     */
    public static BufferedImage apply(BufferedImage image, int threshold) {
        return apply(image, threshold, Mode.BINARY);
    }

    /**
     * Build a lookup table for the given threshold and mode.
     */
    private static int[] buildLut(int threshold, Mode mode) {
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            lut[i] = switch (mode) {
                case BINARY -> (i < threshold) ? 0 : 255;
                case BINARY_INV -> (i < threshold) ? 255 : 0;
                case TRUNC -> (i < threshold) ? i : threshold;
                case TOZERO -> (i < threshold) ? 0 : i;
                case TOZERO_INV -> (i < threshold) ? i : 0;
            };
        }
        return lut;
    }

    private static void validateThreshold(int threshold) {
        if (threshold < 0 || threshold > 255) {
            throw new ImageOperationException(
                    "Threshold must be in [0, 255], got: " + threshold,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }
}
