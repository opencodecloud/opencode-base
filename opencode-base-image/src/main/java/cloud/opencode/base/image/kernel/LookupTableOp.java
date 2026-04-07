package cloud.opencode.base.image.kernel;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Lookup Table (LUT) Operations
 * 查找表 (LUT) 操作工具类
 *
 * <p>Provides efficient pixel-level transformations using pre-computed lookup tables.
 * Includes factory methods for common LUTs: gamma, contrast, brightness, threshold, invert.</p>
 * <p>使用预计算的查找表提供高效的像素级变换。
 * 包含常用 LUT 的工厂方法：伽马、对比度、亮度、阈值、反转。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Apply single LUT to all RGB channels - 将单个 LUT 应用于所有 RGB 通道</li>
 *   <li>Apply separate LUTs per R/G/B channel - 为 R/G/B 通道分别应用不同 LUT</li>
 *   <li>Pre-built LUTs: gamma, contrast, brightness, threshold, invert, identity - 预置 LUT：伽马、对比度、亮度、阈值、反转、恒等</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Gamma correction
 * int[] lut = LookupTableOp.gammaLut(2.2);
 * BufferedImage corrected = LookupTableOp.apply(image, lut);
 *
 * // Invert image
 * BufferedImage inverted = LookupTableOp.apply(image, LookupTableOp.invertLut());
 *
 * // Binary threshold
 * BufferedImage binary = LookupTableOp.apply(image, LookupTableOp.thresholdLut(128));
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>LUT creation: O(256) - LUT 创建: O(256)</li>
 *   <li>LUT application: O(w * h) - LUT 应用: O(w * h)</li>
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
public final class LookupTableOp {

    private static final int LUT_SIZE = 256;

    private LookupTableOp() {
        throw new AssertionError("No LookupTableOp instances");
    }

    /**
     * Apply a single lookup table to all RGB channels of an image (alpha preserved).
     * 将单个查找表应用于图像的所有 RGB 通道（Alpha 通道保持不变）。
     *
     * @param image the source image | 源图像
     * @param lut   the lookup table (length must be 256) | 查找表（长度必须为 256）
     * @return the transformed image | 变换后的图像
     * @throws ImageOperationException if parameters are invalid | 当参数无效时抛出
     */
    public static BufferedImage apply(BufferedImage image, int[] lut) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(lut, "lut must not be null");
        validateLut(lut, "lut");
        return apply(image, lut, lut, lut);
    }

    /**
     * Apply separate lookup tables to R, G, B channels of an image (alpha preserved).
     * 为图像的 R、G、B 通道分别应用不同的查找表（Alpha 通道保持不变）。
     *
     * @param image the source image | 源图像
     * @param lutR  the red channel lookup table (length must be 256) | 红色通道查找表（长度必须为 256）
     * @param lutG  the green channel lookup table (length must be 256) | 绿色通道查找表（长度必须为 256）
     * @param lutB  the blue channel lookup table (length must be 256) | 蓝色通道查找表（长度必须为 256）
     * @return the transformed image | 变换后的图像
     * @throws ImageOperationException if parameters are invalid | 当参数无效时抛出
     */
    public static BufferedImage apply(BufferedImage image, int[] lutR, int[] lutG, int[] lutB) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(lutR, "lutR must not be null");
        Objects.requireNonNull(lutG, "lutG must not be null");
        Objects.requireNonNull(lutB, "lutB must not be null");
        validateLut(lutR, "lutR");
        validateLut(lutG, "lutG");
        validateLut(lutB, "lutB");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] src = PixelOp.getPixels(argb);

        BufferedImage output = PixelOp.createArgb(w, h);
        int[] dst = PixelOp.getPixels(output);

        for (int i = 0; i < src.length; i++) {
            int px = src[i];
            int a = (px >> 24) & 0xFF;
            int r = lutR[(px >> 16) & 0xFF];
            int g = lutG[(px >> 8) & 0xFF];
            int b = lutB[px & 0xFF];
            dst[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        return output;
    }

    /**
     * Create a gamma correction lookup table.
     * 创建伽马校正查找表。
     *
     * <p>Formula: out = 255 * (in / 255) ^ (1 / gamma)</p>
     *
     * @param gamma the gamma value (must be positive) | 伽马值（必须为正数）
     * @return the gamma correction LUT | 伽马校正查找表
     * @throws ImageOperationException if gamma is not positive | 当伽马值不为正数时抛出
     */
    public static int[] gammaLut(double gamma) {
        if (gamma <= 0 || Double.isNaN(gamma) || Double.isInfinite(gamma)) {
            throw new ImageOperationException(
                    "Gamma must be a positive finite number, got: " + gamma,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        int[] lut = new int[LUT_SIZE];
        double invGamma = 1.0 / gamma;
        for (int i = 0; i < LUT_SIZE; i++) {
            lut[i] = PixelOp.clamp((int) Math.round(255.0 * Math.pow(i / 255.0, invGamma)));
        }
        return lut;
    }

    /**
     * Create a contrast adjustment lookup table.
     * 创建对比度调整查找表。
     *
     * <p>Formula: out = clamp(factor * (in - 128) + 128)</p>
     *
     * @param factor the contrast factor (1.0 = no change, &gt;1 increases, &lt;1 decreases) | 对比度因子（1.0 不变，&gt;1 增大，&lt;1 减小）
     * @return the contrast LUT | 对比度查找表
     * @throws ImageOperationException if factor is NaN or infinite | 当因子为 NaN 或无穷时抛出
     */
    public static int[] contrastLut(double factor) {
        if (Double.isNaN(factor) || Double.isInfinite(factor)) {
            throw new ImageOperationException(
                    "Contrast factor must be a finite number, got: " + factor,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        int[] lut = new int[LUT_SIZE];
        for (int i = 0; i < LUT_SIZE; i++) {
            lut[i] = PixelOp.clamp((int) Math.round(factor * (i - 128) + 128));
        }
        return lut;
    }

    /**
     * Create a brightness adjustment lookup table.
     * 创建亮度调整查找表。
     *
     * <p>Formula: out = clamp(in + offset)</p>
     *
     * @param offset the brightness offset (positive = brighter, negative = darker) | 亮度偏移量（正值更亮，负值更暗）
     * @return the brightness LUT | 亮度查找表
     * @throws ImageOperationException if offset is NaN or infinite | 当偏移量为 NaN 或无穷时抛出
     */
    public static int[] brightnessLut(double offset) {
        if (Double.isNaN(offset) || Double.isInfinite(offset)) {
            throw new ImageOperationException(
                    "Brightness offset must be a finite number, got: " + offset,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        int[] lut = new int[LUT_SIZE];
        for (int i = 0; i < LUT_SIZE; i++) {
            lut[i] = PixelOp.clamp((int) Math.round(i + offset));
        }
        return lut;
    }

    /**
     * Create a binary threshold lookup table.
     * 创建二值阈值查找表。
     *
     * <p>Values below threshold map to 0, values at or above threshold map to 255.</p>
     * <p>低于阈值的映射为 0，大于等于阈值的映射为 255。</p>
     *
     * @param threshold the threshold value [0, 255] | 阈值 [0, 255]
     * @return the threshold LUT | 阈值查找表
     * @throws ImageOperationException if threshold is out of [0, 255] | 当阈值超出 [0, 255] 范围时抛出
     */
    public static int[] thresholdLut(int threshold) {
        if (threshold < 0 || threshold > 255) {
            throw new ImageOperationException(
                    "Threshold must be in [0, 255], got: " + threshold,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        int[] lut = new int[LUT_SIZE];
        for (int i = 0; i < LUT_SIZE; i++) {
            lut[i] = (i >= threshold) ? 255 : 0;
        }
        return lut;
    }

    /**
     * Create an inversion lookup table (255 - value).
     * 创建反转查找表 (255 - value)。
     *
     * @return the inversion LUT | 反转查找表
     */
    public static int[] invertLut() {
        int[] lut = new int[LUT_SIZE];
        for (int i = 0; i < LUT_SIZE; i++) {
            lut[i] = 255 - i;
        }
        return lut;
    }

    /**
     * Create an identity lookup table (output = input).
     * 创建恒等查找表（输出 = 输入）。
     *
     * @return the identity LUT | 恒等查找表
     */
    public static int[] identityLut() {
        int[] lut = new int[LUT_SIZE];
        for (int i = 0; i < LUT_SIZE; i++) {
            lut[i] = i;
        }
        return lut;
    }

    private static void validateLut(int[] lut, String name) {
        if (lut.length != LUT_SIZE) {
            throw new ImageOperationException(
                    name + " length must be 256, got: " + lut.length,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }
}
