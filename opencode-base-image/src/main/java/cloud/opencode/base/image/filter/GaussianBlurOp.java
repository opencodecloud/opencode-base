package cloud.opencode.base.image.filter;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.KernelOp;
import cloud.opencode.base.image.kernel.PixelOp;
import cloud.opencode.base.image.kernel.SeparableKernelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Gaussian Blur Filter
 * 高斯模糊滤波器
 *
 * <p>Applies Gaussian blur to images using a separable 1D kernel convolution,
 * which is mathematically equivalent to a 2D Gaussian kernel but significantly faster.</p>
 * <p>使用可分离一维核卷积对图像进行高斯模糊处理，
 * 数学上等效于二维高斯核但速度显著更快。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Gaussian blur with automatic kernel size from sigma - 根据 sigma 自动计算核大小的高斯模糊</li>
 *   <li>Gaussian blur with explicit kernel size - 显式指定核大小的高斯模糊</li>
 *   <li>Separable convolution for O(w*h*k) performance - 可分离卷积实现 O(w*h*k) 性能</li>
 *   <li>Alpha channel preservation - Alpha 通道保持不变</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Auto kernel size from sigma
 * BufferedImage blurred = GaussianBlurOp.apply(image, 2.0);
 *
 * // Explicit kernel size
 * BufferedImage blurred = GaussianBlurOp.apply(image, 1.5, 7);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(w * h * k) via separable convolution - 时间: O(w * h * k) 通过可分离卷积</li>
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
public final class GaussianBlurOp {

    private GaussianBlurOp() {
        throw new AssertionError("No GaussianBlurOp instances");
    }

    /**
     * Apply Gaussian blur with automatic kernel size computed from sigma.
     * 使用根据 sigma 自动计算的核大小应用高斯模糊。
     *
     * <p>The kernel size is computed as {@code ceil(3 * sigma) * 2 + 1},
     * ensuring the kernel captures at least 3 standard deviations.</p>
     * <p>核大小计算公式为 {@code ceil(3 * sigma) * 2 + 1}，
     * 确保核至少覆盖 3 个标准差。</p>
     *
     * @param image the source image | 源图像
     * @param sigma the Gaussian standard deviation (must be &gt; 0) | 高斯标准差（必须大于 0）
     * @return the blurred image | 模糊后的图像
     * @throws ImageOperationException if image is null or sigma is not positive | 当图像为 null 或 sigma 不为正数时抛出
     */
    public static BufferedImage apply(BufferedImage image, double sigma) {
        Objects.requireNonNull(image, "image must not be null");
        validateSigma(sigma);
        int kernelSize = computeKernelSize(sigma);
        return applyInternal(image, sigma, kernelSize);
    }

    /**
     * Apply Gaussian blur with explicit kernel size.
     * 使用显式指定的核大小应用高斯模糊。
     *
     * @param image      the source image | 源图像
     * @param sigma      the Gaussian standard deviation (must be &gt; 0) | 高斯标准差（必须大于 0）
     * @param kernelSize the kernel size (must be odd and &gt;= 1) | 核大小（必须为奇数且 &gt;= 1）
     * @return the blurred image | 模糊后的图像
     * @throws ImageOperationException if parameters are invalid | 当参数无效时抛出
     */
    public static BufferedImage apply(BufferedImage image, double sigma, int kernelSize) {
        Objects.requireNonNull(image, "image must not be null");
        validateSigma(sigma);
        validateKernelSize(kernelSize);
        return applyInternal(image, sigma, kernelSize);
    }

    /**
     * Generate a 1D Gaussian kernel.
     * 生成一维高斯核。
     *
     * <p>The kernel is normalized so that all values sum to 1.0.</p>
     * <p>核经过归一化处理，所有值之和为 1.0。</p>
     *
     * @param sigma the Gaussian standard deviation | 高斯标准差
     * @param size  the kernel size (must be odd) | 核大小（必须为奇数）
     * @return the normalized 1D Gaussian kernel | 归一化的一维高斯核
     */
    static float[] generateKernel(double sigma, int size) {
        float[] kernel = new float[size];
        int half = size / 2;
        double twoSigmaSq = 2.0 * sigma * sigma;
        double sum = 0.0;

        for (int i = 0; i < size; i++) {
            double x = i - half;
            double value = Math.exp(-(x * x) / twoSigmaSq);
            kernel[i] = (float) value;
            sum += value;
        }

        // Normalize so kernel sums to 1.0
        for (int i = 0; i < size; i++) {
            kernel[i] /= (float) sum;
        }
        return kernel;
    }

    /**
     * Compute the automatic kernel size for a given sigma.
     * 根据给定的 sigma 计算自动核大小。
     *
     * @param sigma the Gaussian standard deviation | 高斯标准差
     * @return the kernel size (always odd, &gt;= 1) | 核大小（始终为奇数，&gt;= 1）
     */
    static int computeKernelSize(double sigma) {
        return (int) (Math.ceil(3.0 * sigma)) * 2 + 1;
    }

    private static BufferedImage applyInternal(BufferedImage image, double sigma, int kernelSize) {
        BufferedImage argb = PixelOp.ensureArgb(image);
        float[] kernel = generateKernel(sigma, kernelSize);
        return SeparableKernelOp.convolve(argb, kernel, kernel, KernelOp.BorderMode.CLAMP);
    }

    private static void validateSigma(double sigma) {
        if (sigma <= 0 || Double.isNaN(sigma) || Double.isInfinite(sigma)) {
            throw new ImageOperationException(
                    "sigma must be a positive finite number, got: " + sigma,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }

    private static void validateKernelSize(int kernelSize) {
        if (kernelSize < 1) {
            throw new ImageOperationException(
                    "kernelSize must be >= 1, got: " + kernelSize,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (kernelSize % 2 == 0) {
            throw new ImageOperationException(
                    "kernelSize must be odd, got: " + kernelSize,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }
}
