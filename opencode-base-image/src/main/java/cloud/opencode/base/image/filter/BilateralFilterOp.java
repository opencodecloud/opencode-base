package cloud.opencode.base.image.filter;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Bilateral Filter
 * 双边滤波器
 *
 * <p>Edge-preserving smoothing filter that averages nearby pixels weighted by both
 * spatial proximity and intensity similarity. Flat regions are smoothed while
 * edges are preserved.</p>
 * <p>保边平滑滤波器，通过空间距离和强度相似度的联合加权来平均邻近像素。
 * 平坦区域被平滑，而边缘被保留。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Edge-preserving smoothing via bilateral weighting - 通过双边加权实现保边平滑</li>
 *   <li>Configurable spatial and range sigma - 可配置的空间和范围 sigma</li>
 *   <li>Independent R, G, B channel processing - 独立处理 R、G、B 通道</li>
 *   <li>Alpha channel preservation - Alpha 通道保持不变</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default bilateral filter (kernelSize=9, sigmaColor=75, sigmaSpace=75)
 * BufferedImage smoothed = BilateralFilterOp.apply(image);
 *
 * // Custom parameters
 * BufferedImage smoothed = BilateralFilterOp.apply(image, 5, 50.0, 50.0);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(W * H * K^2) where K is kernel size - 时间: O(W * H * K^2) 其中 K 为核大小</li>
 *   <li>Space: O(W * H) - 空间: O(W * H)</li>
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
public final class BilateralFilterOp {

    private static final int DEFAULT_KERNEL_SIZE = 9;
    private static final double DEFAULT_SIGMA_COLOR = 75.0;
    private static final double DEFAULT_SIGMA_SPACE = 75.0;

    private BilateralFilterOp() {
        throw new AssertionError("No BilateralFilterOp instances");
    }

    /**
     * Apply bilateral filter with default parameters (kernelSize=9, sigmaColor=75, sigmaSpace=75).
     * 使用默认参数（kernelSize=9, sigmaColor=75, sigmaSpace=75）应用双边滤波。
     *
     * @param image the source image | 源图像
     * @return the filtered image | 滤波后的图像
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage apply(BufferedImage image) {
        return apply(image, DEFAULT_KERNEL_SIZE, DEFAULT_SIGMA_COLOR, DEFAULT_SIGMA_SPACE);
    }

    /**
     * Apply bilateral filter with custom parameters.
     * 使用自定义参数应用双边滤波。
     *
     * <p>For each pixel, computes a weighted average of its neighbors within the kernel window.
     * The weight combines a spatial Gaussian (based on pixel distance) and a range Gaussian
     * (based on intensity difference). R, G, B channels are processed independently.</p>
     * <p>对每个像素，计算核窗口内邻近像素的加权平均值。
     * 权重结合了空间高斯（基于像素距离）和范围高斯（基于强度差异）。
     * R、G、B 通道独立处理。</p>
     *
     * @param image      the source image | 源图像
     * @param kernelSize the kernel size (must be odd and &gt;= 3) | 核大小（必须为奇数且 &gt;= 3）
     * @param sigmaColor the range sigma controlling color similarity weighting (must be &gt; 0) |
     *                   范围 sigma，控制颜色相似度权重（必须大于 0）
     * @param sigmaSpace the spatial sigma controlling distance weighting (must be &gt; 0) |
     *                   空间 sigma，控制距离权重（必须大于 0）
     * @return the filtered image | 滤波后的图像
     * @throws NullPointerException      if image is null | 当图像为 null 时抛出
     * @throws ImageOperationException   if parameters are invalid | 当参数无效时抛出
     */
    public static BufferedImage apply(BufferedImage image, int kernelSize,
                                      double sigmaColor, double sigmaSpace) {
        Objects.requireNonNull(image, "image must not be null");
        validateKernelSize(kernelSize);
        validateSigma(sigmaColor, "sigmaColor");
        validateSigma(sigmaSpace, "sigmaSpace");

        BufferedImage src = PixelOp.ensureArgb(image);
        int width = src.getWidth();
        int height = src.getHeight();
        int[] srcPixels = PixelOp.getPixels(src);

        BufferedImage result = PixelOp.createArgb(width, height);
        int[] dstPixels = PixelOp.getPixels(result);

        int half = kernelSize / 2;
        double twoSigmaColorSq = 2.0 * sigmaColor * sigmaColor;
        double twoSigmaSpaceSq = 2.0 * sigmaSpace * sigmaSpace;

        // Precompute spatial weights
        double[] spatialWeights = new double[kernelSize * kernelSize];
        for (int dy = -half; dy <= half; dy++) {
            for (int dx = -half; dx <= half; dx++) {
                double distSq = (double) dx * dx + (double) dy * dy;
                spatialWeights[(dy + half) * kernelSize + (dx + half)] =
                        Math.exp(-distSq / twoSigmaSpaceSq);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int centerIdx = y * width + x;
                int centerPixel = srcPixels[centerIdx];
                int ca = PixelOp.alpha(centerPixel);
                int cr = PixelOp.red(centerPixel);
                int cg = PixelOp.green(centerPixel);
                int cb = PixelOp.blue(centerPixel);

                double sumR = 0.0, sumG = 0.0, sumB = 0.0;
                double sumWeight = 0.0;

                for (int dy = -half; dy <= half; dy++) {
                    int ny = clampCoord(y + dy, height);
                    for (int dx = -half; dx <= half; dx++) {
                        int nx = clampCoord(x + dx, width);
                        int neighborPixel = srcPixels[ny * width + nx];

                        int nr = PixelOp.red(neighborPixel);
                        int ng = PixelOp.green(neighborPixel);
                        int nb = PixelOp.blue(neighborPixel);

                        // Range difference: Euclidean distance in RGB space
                        double dr = cr - nr;
                        double dg = cg - ng;
                        double db = cb - nb;
                        double colorDistSq = dr * dr + dg * dg + db * db;

                        double spatialW = spatialWeights[(dy + half) * kernelSize + (dx + half)];
                        double rangeW = Math.exp(-colorDistSq / twoSigmaColorSq);
                        double weight = spatialW * rangeW;

                        sumR += weight * nr;
                        sumG += weight * ng;
                        sumB += weight * nb;
                        sumWeight += weight;
                    }
                }

                int outR = PixelOp.clamp((int) Math.round(sumR / sumWeight));
                int outG = PixelOp.clamp((int) Math.round(sumG / sumWeight));
                int outB = PixelOp.clamp((int) Math.round(sumB / sumWeight));

                dstPixels[centerIdx] = PixelOp.argb(ca, outR, outG, outB);
            }
        }

        return result;
    }

    /**
     * Clamp a coordinate to valid range [0, max-1].
     * 将坐标钳制到有效范围 [0, max-1]。
     */
    private static int clampCoord(int val, int max) {
        return Math.max(0, Math.min(val, max - 1));
    }

    private static void validateKernelSize(int kernelSize) {
        if (kernelSize < 3) {
            throw new ImageOperationException(
                    "kernelSize must be >= 3, got: " + kernelSize,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (kernelSize % 2 == 0) {
            throw new ImageOperationException(
                    "kernelSize must be odd, got: " + kernelSize,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }

    private static void validateSigma(double sigma, String name) {
        if (sigma <= 0 || Double.isNaN(sigma) || Double.isInfinite(sigma)) {
            throw new ImageOperationException(
                    name + " must be a positive finite number, got: " + sigma,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }
}
