package cloud.opencode.base.image.color;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Saturation Adjustment Operation
 * 饱和度调整操作工具类
 *
 * <p>Adjusts image saturation by converting to HSV color space, scaling the saturation
 * channel, and converting back to RGB.</p>
 * <p>通过转换为 HSV 颜色空间、缩放饱和度通道并转回 RGB 来调整图像饱和度。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Saturation adjustment via HSV color space - 通过 HSV 颜色空间调整饱和度</li>
 *   <li>Factor 0.0 produces grayscale, 1.0 is unchanged, 2.0 doubles saturation - 因子 0.0 产生灰度图，1.0 不变，2.0 双倍饱和度</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BufferedImage vivid = SaturationOp.apply(image, 1.5);
 * BufferedImage gray = SaturationOp.apply(image, 0.0);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(n) where n = pixel count - 时间: O(n)，n 为像素数量</li>
 *   <li>Space: O(n) for intermediate HSV arrays - 空间: O(n) 用于中间 HSV 数组</li>
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
public final class SaturationOp {

    private SaturationOp() {
        throw new AssertionError("No SaturationOp instances");
    }

    /**
     * Adjust the saturation of an image.
     * 调整图像的饱和度。
     *
     * <p>A factor of 0.0 produces a grayscale image, 1.0 leaves the image unchanged,
     * and values greater than 1.0 increase saturation.</p>
     * <p>因子为 0.0 时产生灰度图像，1.0 时图像不变，大于 1.0 时增加饱和度。</p>
     *
     * @param image  the source image | 源图像
     * @param factor the saturation factor (must be &gt;= 0) | 饱和度因子（必须 &gt;= 0）
     * @return the saturation-adjusted image | 饱和度调整后的图像
     * @throws ImageOperationException if image is null or factor is negative | 当图像为 null 或因子为负时抛出
     */
    public static BufferedImage apply(BufferedImage image, double factor) {
        Objects.requireNonNull(image, "image must not be null");
        if (factor < 0 || Double.isNaN(factor) || Double.isInfinite(factor)) {
            throw new ImageOperationException(
                    "Saturation factor must be a non-negative finite number, got: " + factor,
                    ImageErrorCode.INVALID_PARAMETERS);
        }

        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] src = PixelOp.getPixels(argb);

        // Convert to HSV
        float[][] hsv = ColorSpaceOp.toHsv(src);

        // Scale saturation
        float[] sArr = hsv[1];
        float fFactor = (float) factor;
        for (int i = 0; i < sArr.length; i++) {
            sArr[i] = Math.clamp(sArr[i] * fFactor, 0.0f, 1.0f);
        }

        // Convert back to RGB
        int[] resultPixels = ColorSpaceOp.fromHsv(hsv);

        // Preserve original alpha
        BufferedImage output = PixelOp.createArgb(w, h);
        int[] dst = PixelOp.getPixels(output);
        for (int i = 0; i < src.length; i++) {
            int alpha = (src[i] >> 24) & 0xFF;
            dst[i] = (alpha << 24) | (resultPixels[i] & 0x00FFFFFF);
        }
        return output;
    }
}
