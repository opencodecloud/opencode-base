package cloud.opencode.base.image.feature;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.KernelOp;
import cloud.opencode.base.image.kernel.PixelOp;
import cloud.opencode.base.image.kernel.SeparableKernelOp;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Shi-Tomasi Corner Detector (Good Features To Track)
 * Shi-Tomasi 角点检测器（良好跟踪特征）
 *
 * <p>Implements the Shi-Tomasi corner detection algorithm, which improves upon Harris
 * by using the minimum eigenvalue of the structure tensor as the corner response.
 * This provides more stable and intuitive corner detection.</p>
 * <p>实现 Shi-Tomasi 角点检测算法，通过使用结构张量的最小特征值作为角点响应来改进 Harris 算法。
 * 这提供了更稳定和直观的角点检测。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Shi-Tomasi corner detection using minimum eigenvalue - 使用最小特征值的 Shi-Tomasi 角点检测</li>
 *   <li>Quality level filtering relative to strongest corner - 相对于最强角点的质量级别过滤</li>
 *   <li>Top-N corner selection with non-maximum suppression - 带非极大值抑制的 Top-N 角点选择</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Detect with custom parameters
 * List<double[]> corners = ShiTomasiOp.detect(image, 50, 0.01);
 * for (double[] corner : corners) {
 *     System.out.printf("Corner at (%d, %d) quality=%.4f%n",
 *         (int) corner[0], (int) corner[1], corner[2]);
 * }
 *
 * // Detect with defaults (maxCorners=100, qualityLevel=0.01)
 * List<double[]> corners = ShiTomasiOp.detect(image);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(w * h * k) where k is kernel size - 时间复杂度: O(w * h * k)，k 为卷积核大小</li>
 *   <li>Space complexity: O(w * h) for gradient and eigenvalue arrays - 空间复杂度: O(w * h) 用于梯度和特征值数组</li>
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
public final class ShiTomasiOp {

    private ShiTomasiOp() {
        throw new AssertionError("No ShiTomasiOp instances");
    }

    /** Default maximum number of corners. */
    private static final int DEFAULT_MAX_CORNERS = 100;

    /** Default quality level. */
    private static final double DEFAULT_QUALITY_LEVEL = 0.01;

    /** Sobel horizontal kernel (separable): [-1, 0, 1]. */
    private static final float[] SOBEL_H = {-1f, 0f, 1f};

    /** Sobel smoothing kernel (separable): [1, 2, 1]. */
    private static final float[] SOBEL_SMOOTH = {1f, 2f, 1f};

    /** Non-maximum suppression window half-size. */
    private static final int NMS_RADIUS = 1;

    /**
     * Detect Shi-Tomasi corners (Good Features To Track) with custom parameters.
     * 使用自定义参数检测 Shi-Tomasi 角点（良好跟踪特征）。
     *
     * <p>The quality level is relative to the maximum corner response. Only corners
     * with response >= qualityLevel * maxResponse are retained.</p>
     * <p>质量级别相对于最大角点响应。只保留响应值 >= qualityLevel * maxResponse 的角点。</p>
     *
     * @param image        the source image | 源图像
     * @param maxCorners   maximum number of corners to return | 返回的最大角点数
     * @param qualityLevel minimum quality level (0.0-1.0), relative to strongest corner |
     *                     最小质量级别（0.0-1.0），相对于最强角点
     * @return list of [x, y, minEigenvalue] for each detected corner, sorted by response descending |
     *         每个检测到的角点的 [x, y, minEigenvalue] 列表，按响应值降序排列
     * @throws ImageOperationException if image is null, maxCorners not positive,
     *                                  or qualityLevel not in [0, 1] |
     *                                  当图像为 null、maxCorners 不为正数或 qualityLevel 不在 [0, 1] 范围内时抛出
     */
    public static List<double[]> detect(BufferedImage image, int maxCorners, double qualityLevel) {
        Objects.requireNonNull(image, "image must not be null");
        if (maxCorners <= 0) {
            throw new ImageOperationException("maxCorners must be positive, got: " + maxCorners);
        }
        if (qualityLevel < 0.0 || qualityLevel > 1.0) {
            throw new ImageOperationException(
                    "qualityLevel must be in [0.0, 1.0], got: " + qualityLevel);
        }

        int w = image.getWidth();
        int h = image.getHeight();
        double[] minEigenvalues = computeMinEigenvalues(image, w, h);

        // Find maximum eigenvalue for quality threshold
        double maxEigen = 0;
        for (double e : minEigenvalues) {
            if (e > maxEigen) {
                maxEigen = e;
            }
        }

        double threshold = qualityLevel * maxEigen;

        // Collect candidates passing threshold and NMS
        List<double[]> candidates = new ArrayList<>();
        for (int y = NMS_RADIUS; y < h - NMS_RADIUS; y++) {
            for (int x = NMS_RADIUS; x < w - NMS_RADIUS; x++) {
                double e = minEigenvalues[y * w + x];
                if (e > threshold && isLocalMaximum(minEigenvalues, w, h, x, y)) {
                    candidates.add(new double[]{x, y, e});
                }
            }
        }

        candidates.sort(Comparator.comparingDouble((double[] c) -> c[2]).reversed());

        if (candidates.size() > maxCorners) {
            return new ArrayList<>(candidates.subList(0, maxCorners));
        }
        return candidates;
    }

    /**
     * Detect Shi-Tomasi corners with default parameters (maxCorners=100, qualityLevel=0.01).
     * 使用默认参数检测 Shi-Tomasi 角点（maxCorners=100，qualityLevel=0.01）。
     *
     * @param image the source image | 源图像
     * @return list of [x, y, minEigenvalue] for each detected corner | 每个检测到的角点的 [x, y, minEigenvalue] 列表
     * @throws ImageOperationException if image is null | 当图像为 null 时抛出
     */
    public static List<double[]> detect(BufferedImage image) {
        return detect(image, DEFAULT_MAX_CORNERS, DEFAULT_QUALITY_LEVEL);
    }

    /**
     * Compute minimum eigenvalue of the structure tensor for every pixel.
     * Shi-Tomasi uses min(lambda1, lambda2) where:
     *   lambda = (a+c)/2 +/- sqrt(((a-c)/2)^2 + b^2)
     *   a = sum(Ix^2), b = sum(IxIy), c = sum(Iy^2)
     *   min eigenvalue = (a+c)/2 - sqrt(((a-c)/2)^2 + b^2)
     */
    static double[] computeMinEigenvalues(BufferedImage image, int w, int h) {
        // Step 1: Convert to grayscale
        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] pixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(pixels);

        // Step 2: Compute Sobel gradients
        int[] dx = SeparableKernelOp.convolveGray(gray, w, h, SOBEL_H, SOBEL_SMOOTH, KernelOp.BorderMode.CLAMP);
        int[] dy = SeparableKernelOp.convolveGray(gray, w, h, SOBEL_SMOOTH, SOBEL_H, KernelOp.BorderMode.CLAMP);

        // Step 3: Compute gradient products (centered at 128 to handle signed values)
        int size = w * h;
        int[] ix2 = new int[size];
        int[] iy2 = new int[size];
        int[] ixiy = new int[size];

        for (int i = 0; i < size; i++) {
            int gx = dx[i] - 128;
            int gy = dy[i] - 128;
            ix2[i] = PixelOp.clamp((gx * gx) / 4 + 128);
            iy2[i] = PixelOp.clamp((gy * gy) / 4 + 128);
            ixiy[i] = PixelOp.clamp((gx * gy) / 4 + 128);
        }

        // Step 4: Gaussian smooth
        float[] gaussKernel = HarrisCornerOp.gaussianKernel1D(5, 1.5f);
        int[] sIx2 = SeparableKernelOp.convolveGray(ix2, w, h, gaussKernel, gaussKernel, KernelOp.BorderMode.CLAMP);
        int[] sIy2 = SeparableKernelOp.convolveGray(iy2, w, h, gaussKernel, gaussKernel, KernelOp.BorderMode.CLAMP);
        int[] sIxIy = SeparableKernelOp.convolveGray(ixiy, w, h, gaussKernel, gaussKernel, KernelOp.BorderMode.CLAMP);

        // Step 5: Compute minimum eigenvalue
        double[] minEigen = new double[size];
        for (int i = 0; i < size; i++) {
            double a = sIx2[i] - 128.0;
            double c = sIy2[i] - 128.0;
            double b = sIxIy[i] - 128.0;

            double halfTrace = (a + c) / 2.0;
            double halfDiff = (a - c) / 2.0;
            double discriminant = halfDiff * halfDiff + b * b;
            double sqrtDisc = Math.sqrt(Math.max(0.0, discriminant));
            minEigen[i] = halfTrace - sqrtDisc;
        }

        return minEigen;
    }

    /**
     * Check if the value at (x, y) is a local maximum in a 3x3 window.
     */
    private static boolean isLocalMaximum(double[] values, int w, int h, int x, int y) {
        double center = values[y * w + x];
        for (int dy = -NMS_RADIUS; dy <= NMS_RADIUS; dy++) {
            for (int dx = -NMS_RADIUS; dx <= NMS_RADIUS; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                    if (values[ny * w + nx] >= center) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
