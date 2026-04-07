package cloud.opencode.base.image.filter;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Unsharp Mask Sharpening Filter
 * 非锐化掩模锐化滤波器
 *
 * <p>Enhances image edges using unsharp masking: {@code result = src + amount * (src - blur)}.
 * The blur is computed via {@link GaussianBlurOp} with a configurable sigma.</p>
 * <p>使用非锐化掩模增强图像边缘：{@code result = src + amount * (src - blur)}。
 * 模糊通过 {@link GaussianBlurOp} 以可配置的 sigma 计算。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unsharp mask sharpening with configurable amount and sigma - 可配置强度和 sigma 的非锐化掩模锐化</li>
 *   <li>Default parameters: amount=1.0, sigma=1.0 - 默认参数：amount=1.0, sigma=1.0</li>
 *   <li>Per-channel processing with clamping to [0, 255] - 逐通道处理并钳制到 [0, 255]</li>
 *   <li>Alpha channel preservation - Alpha 通道保持不变</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default sharpening (amount=1.0, sigma=1.0)
 * BufferedImage sharpened = SharpenOp.apply(image);
 *
 * // Custom amount
 * BufferedImage sharpened = SharpenOp.apply(image, 1.5);
 *
 * // Custom amount and sigma
 * BufferedImage sharpened = SharpenOp.apply(image, 2.0, 2.0);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(w * h * k) dominated by Gaussian blur - 时间: O(w * h * k) 由高斯模糊主导</li>
 *   <li>Space: O(w * h) - 空间: O(w * h)</li>
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
public final class SharpenOp {

    private static final double DEFAULT_AMOUNT = 1.0;
    private static final double DEFAULT_SIGMA = 1.0;

    private SharpenOp() {
        throw new AssertionError("No SharpenOp instances");
    }

    /**
     * Apply unsharp mask sharpening with default parameters (amount=1.0, sigma=1.0).
     * 使用默认参数（amount=1.0, sigma=1.0）应用非锐化掩模锐化。
     *
     * @param image the source image | 源图像
     * @return the sharpened image | 锐化后的图像
     * @throws NullPointerException    if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage apply(BufferedImage image) {
        return apply(image, DEFAULT_AMOUNT, DEFAULT_SIGMA);
    }

    /**
     * Apply unsharp mask sharpening with custom amount and default sigma=1.0.
     * 使用自定义强度和默认 sigma=1.0 应用非锐化掩模锐化。
     *
     * @param image  the source image | 源图像
     * @param amount the sharpening amount (must be &gt;= 0) | 锐化强度（必须 &gt;= 0）
     * @return the sharpened image | 锐化后的图像
     * @throws NullPointerException      if image is null | 当图像为 null 时抛出
     * @throws ImageOperationException   if amount is negative, NaN, or infinite | 当 amount 为负数、NaN 或无穷时抛出
     */
    public static BufferedImage apply(BufferedImage image, double amount) {
        return apply(image, amount, DEFAULT_SIGMA);
    }

    /**
     * Apply unsharp mask sharpening with custom amount and sigma.
     * 使用自定义强度和 sigma 应用非锐化掩模锐化。
     *
     * <p>The algorithm computes: {@code result = clamp(src + amount * (src - blur))},
     * where {@code blur = GaussianBlurOp.apply(image, sigma)}.</p>
     * <p>算法计算：{@code result = clamp(src + amount * (src - blur))}，
     * 其中 {@code blur = GaussianBlurOp.apply(image, sigma)}。</p>
     *
     * @param image  the source image | 源图像
     * @param amount the sharpening amount (must be &gt;= 0) | 锐化强度（必须 &gt;= 0）
     * @param sigma  the Gaussian blur sigma (must be &gt; 0) | 高斯模糊 sigma（必须大于 0）
     * @return the sharpened image | 锐化后的图像
     * @throws NullPointerException      if image is null | 当图像为 null 时抛出
     * @throws ImageOperationException   if amount is negative or sigma is not positive | 当 amount 为负数或 sigma 不为正数时抛出
     */
    public static BufferedImage apply(BufferedImage image, double amount, double sigma) {
        Objects.requireNonNull(image, "image must not be null");
        validateAmount(amount);
        validateSigma(sigma);

        BufferedImage src = PixelOp.ensureArgb(image);
        BufferedImage blurred = GaussianBlurOp.apply(src, sigma);

        int[] srcPixels = PixelOp.getPixels(src);
        int[] blurPixels = PixelOp.getPixels(blurred);

        BufferedImage result = PixelOp.createArgb(src.getWidth(), src.getHeight());
        int[] dstPixels = PixelOp.getPixels(result);

        for (int i = 0; i < srcPixels.length; i++) {
            int a = PixelOp.alpha(srcPixels[i]);
            int sr = PixelOp.red(srcPixels[i]);
            int sg = PixelOp.green(srcPixels[i]);
            int sb = PixelOp.blue(srcPixels[i]);

            int br = PixelOp.red(blurPixels[i]);
            int bg = PixelOp.green(blurPixels[i]);
            int bb = PixelOp.blue(blurPixels[i]);

            int r = PixelOp.clamp((int) Math.round(sr + amount * (sr - br)));
            int g = PixelOp.clamp((int) Math.round(sg + amount * (sg - bg)));
            int b = PixelOp.clamp((int) Math.round(sb + amount * (sb - bb)));

            dstPixels[i] = PixelOp.argb(a, r, g, b);
        }

        return result;
    }

    private static void validateAmount(double amount) {
        if (amount < 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new ImageOperationException(
                    "amount must be a non-negative finite number, got: " + amount,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }

    private static void validateSigma(double sigma) {
        if (sigma <= 0 || Double.isNaN(sigma) || Double.isInfinite(sigma)) {
            throw new ImageOperationException(
                    "sigma must be a positive finite number, got: " + sigma,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }
}
