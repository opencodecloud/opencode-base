package cloud.opencode.base.image.color;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Brightness and Contrast Adjustment Operations
 * 亮度与对比度调整操作工具类
 *
 * <p>Provides pixel-level brightness and contrast adjustments using
 * multiplicative factor-based transformations.</p>
 * <p>提供基于乘法因子的像素级亮度和对比度调整。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Brightness adjustment via multiplicative factor - 基于乘法因子的亮度调整</li>
 *   <li>Contrast adjustment centered at 128 - 以 128 为中心的对比度调整</li>
 *   <li>Preserves alpha channel - 保留 Alpha 通道</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Brighten image by 50%
 * BufferedImage bright = BrightnessContrastOp.brightness(image, 1.5);
 *
 * // Increase contrast
 * BufferedImage highContrast = BrightnessContrastOp.contrast(image, 2.0);
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
public final class BrightnessContrastOp {

    private BrightnessContrastOp() {
        throw new AssertionError("No BrightnessContrastOp instances");
    }

    /**
     * Adjust brightness of an image by multiplying each RGB component by the given factor.
     * 通过将每个 RGB 分量乘以给定因子来调整图像亮度。
     *
     * <p>A factor &gt; 1 brightens, &lt; 1 darkens, and exactly 1 leaves unchanged.
     * Factor = 0 produces a fully black image (alpha preserved).</p>
     * <p>因子 &gt; 1 变亮，&lt; 1 变暗，等于 1 不变。
     * 因子 = 0 生成全黑图像（Alpha 通道保持不变）。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * brightness(image, 1.0)  // no change | 不变
     * brightness(image, 2.0)  // twice as bright | 亮度翻倍
     * brightness(image, 0.5)  // half brightness | 亮度减半
     * brightness(image, 0.0)  // all black | 全黑
     * </pre>
     *
     * @param image  the source image | 源图像
     * @param factor the brightness factor (must be &gt;= 0) | 亮度因子（必须 &gt;= 0）
     * @return the brightness-adjusted image | 亮度调整后的图像
     * @throws NullPointerException     if image is null | 当图像为 null 时抛出
     * @throws ImageOperationException  if factor is negative, NaN, or infinite | 当因子为负数、NaN 或无穷时抛出
     */
    public static BufferedImage brightness(BufferedImage image, double factor) {
        Objects.requireNonNull(image, "image must not be null");
        validateFactor(factor, "Brightness");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] src = PixelOp.getPixels(argb);

        BufferedImage output = PixelOp.createCompatible(argb);
        int[] dst = PixelOp.getPixels(output);

        for (int i = 0; i < src.length; i++) {
            int px = src[i];
            int a = PixelOp.alpha(px);
            int r = PixelOp.clamp((int) Math.round(PixelOp.red(px) * factor));
            int g = PixelOp.clamp((int) Math.round(PixelOp.green(px) * factor));
            int b = PixelOp.clamp((int) Math.round(PixelOp.blue(px) * factor));
            dst[i] = PixelOp.argb(a, r, g, b);
        }
        return output;
    }

    /**
     * Adjust contrast of an image by scaling RGB components around the midpoint 128.
     * 通过围绕中间值 128 缩放 RGB 分量来调整图像对比度。
     *
     * <p>Formula: newVal = clamp((oldVal - 128) * factor + 128, 0, 255)</p>
     * <p>A factor &gt; 1 increases contrast, &lt; 1 decreases it, and exactly 1 leaves unchanged.</p>
     * <p>因子 &gt; 1 增加对比度，&lt; 1 降低对比度，等于 1 不变。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * contrast(image, 1.0)  // no change | 不变
     * contrast(image, 2.0)  // high contrast | 高对比度
     * contrast(image, 0.5)  // low contrast | 低对比度
     * contrast(image, 0.0)  // flat gray (128) | 纯灰色 (128)
     * </pre>
     *
     * @param image  the source image | 源图像
     * @param factor the contrast factor (must be &gt;= 0) | 对比度因子（必须 &gt;= 0）
     * @return the contrast-adjusted image | 对比度调整后的图像
     * @throws NullPointerException     if image is null | 当图像为 null 时抛出
     * @throws ImageOperationException  if factor is negative, NaN, or infinite | 当因子为负数、NaN 或无穷时抛出
     */
    public static BufferedImage contrast(BufferedImage image, double factor) {
        Objects.requireNonNull(image, "image must not be null");
        validateFactor(factor, "Contrast");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] src = PixelOp.getPixels(argb);

        BufferedImage output = PixelOp.createCompatible(argb);
        int[] dst = PixelOp.getPixels(output);

        for (int i = 0; i < src.length; i++) {
            int px = src[i];
            int a = PixelOp.alpha(px);
            int r = PixelOp.clamp((int) Math.round((PixelOp.red(px) - 128) * factor + 128));
            int g = PixelOp.clamp((int) Math.round((PixelOp.green(px) - 128) * factor + 128));
            int b = PixelOp.clamp((int) Math.round((PixelOp.blue(px) - 128) * factor + 128));
            dst[i] = PixelOp.argb(a, r, g, b);
        }
        return output;
    }

    /**
     * Validate factor parameter.
     * 验证因子参数。
     *
     * @param factor the factor to validate | 要验证的因子
     * @param name   the parameter name for error messages | 用于错误消息的参数名称
     * @throws ImageOperationException if factor is negative, NaN, or infinite | 当因子为负数、NaN 或无穷时抛出
     */
    private static void validateFactor(double factor, String name) {
        if (factor < 0 || Double.isNaN(factor) || Double.isInfinite(factor)) {
            throw new ImageOperationException(
                    name + " factor must be a non-negative finite number, got: " + factor,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }
}
