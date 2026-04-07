package cloud.opencode.base.image.color;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.LookupTableOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Gamma Correction Operation
 * 伽马校正操作工具类
 *
 * <p>Applies gamma correction to an image using a pre-computed lookup table
 * for efficient per-pixel transformation.</p>
 * <p>使用预计算查找表对图像进行伽马校正，实现高效的逐像素变换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Gamma correction via LUT for O(n) performance - 基于 LUT 的伽马校正，O(n) 性能</li>
 *   <li>Supports arbitrary positive gamma values - 支持任意正伽马值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BufferedImage corrected = GammaOp.apply(image, 2.2);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(n) where n = pixel count - 时间: O(n)，n 为像素数量</li>
 *   <li>LUT creation: O(256) - LUT 创建: O(256)</li>
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
public final class GammaOp {

    private GammaOp() {
        throw new AssertionError("No GammaOp instances");
    }

    /**
     * Apply gamma correction to an image.
     * 对图像应用伽马校正。
     *
     * <p>Gamma &gt; 1.0 darkens the image, gamma &lt; 1.0 brightens it,
     * and gamma = 1.0 leaves the image unchanged.</p>
     * <p>伽马值 &gt; 1.0 使图像变暗，&lt; 1.0 使图像变亮，
     * 等于 1.0 时图像不变。</p>
     *
     * @param image the source image | 源图像
     * @param gamma the gamma value (must be positive) | 伽马值（必须为正数）
     * @return the gamma-corrected image | 伽马校正后的图像
     * @throws ImageOperationException if image is null or gamma is not positive | 当图像为 null 或伽马值不为正数时抛出
     */
    public static BufferedImage apply(BufferedImage image, double gamma) {
        Objects.requireNonNull(image, "image must not be null");
        if (gamma <= 0 || Double.isNaN(gamma) || Double.isInfinite(gamma)) {
            throw new ImageOperationException(
                    "Gamma must be a positive finite number, got: " + gamma,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        int[] lut = LookupTableOp.gammaLut(gamma);
        return LookupTableOp.apply(image, lut);
    }
}
