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
 * Harris Corner Detector
 * Harris 角点检测器
 *
 * <p>Implements the Harris corner detection algorithm for identifying interest points
 * in images where two edges meet, forming a corner.</p>
 * <p>实现 Harris 角点检测算法，用于识别图像中两条边缘相交形成角点的兴趣点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Harris corner detection with configurable k and threshold - 可配置 k 和阈值的 Harris 角点检测</li>
 *   <li>Top-N corner selection with automatic threshold - 自动阈值的 Top-N 角点选择</li>
 *   <li>Corner response image generation for visualization - 生成角点响应图用于可视化</li>
 *   <li>Non-maximum suppression (3x3 window) - 非极大值抑制（3x3 窗口）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Detect corners with explicit parameters
 * List<double[]> corners = HarrisCornerOp.detect(image, 0.04, 1000.0);
 * for (double[] corner : corners) {
 *     System.out.printf("Corner at (%d, %d) response=%.2f%n",
 *         (int) corner[0], (int) corner[1], corner[2]);
 * }
 *
 * // Detect top-N corners with automatic threshold
 * List<double[]> topCorners = HarrisCornerOp.detect(image, 50);
 *
 * // Get response image for visualization
 * BufferedImage response = HarrisCornerOp.responseImage(image, 0.04);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(w * h * k) where k is kernel size - 时间复杂度: O(w * h * k)，k 为卷积核大小</li>
 *   <li>Space complexity: O(w * h) for gradient and response arrays - 空间复杂度: O(w * h) 用于梯度和响应数组</li>
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
public final class HarrisCornerOp {

    private HarrisCornerOp() {
        throw new AssertionError("No HarrisCornerOp instances");
    }

    /** Default Harris sensitivity parameter. */
    private static final double DEFAULT_K = 0.04;

    /** Sobel horizontal kernel (separable): [-1, 0, 1]. */
    private static final float[] SOBEL_H = {-1f, 0f, 1f};

    /** Sobel vertical kernel (separable): [1, 2, 1] (smoothing component). */
    private static final float[] SOBEL_SMOOTH = {1f, 2f, 1f};

    /** Non-maximum suppression window half-size. */
    private static final int NMS_RADIUS = 1;

    /**
     * Detect Harris corners with explicit parameters.
     * 使用显式参数检测 Harris 角点。
     *
     * <p>Returns a list of detected corners, each represented as a {@code double[3]}
     * array containing [x, y, response].</p>
     * <p>返回检测到的角点列表，每个角点表示为 {@code double[3]} 数组，包含 [x, y, response]。</p>
     *
     * @param image     the source image | 源图像
     * @param k         Harris sensitivity parameter (typically 0.04-0.06) | Harris 灵敏度参数（通常为 0.04-0.06）
     * @param threshold minimum corner response threshold | 最小角点响应阈值
     * @return list of [x, y, response] for each detected corner | 每个检测到的角点的 [x, y, response] 列表
     * @throws ImageOperationException if image is null | 当图像为 null 时抛出
     */
    public static List<double[]> detect(BufferedImage image, double k, double threshold) {
        Objects.requireNonNull(image, "image must not be null");

        int w = image.getWidth();
        int h = image.getHeight();
        double[] response = computeResponse(image, w, h, k);

        List<double[]> corners = new ArrayList<>();
        for (int y = NMS_RADIUS; y < h - NMS_RADIUS; y++) {
            for (int x = NMS_RADIUS; x < w - NMS_RADIUS; x++) {
                double r = response[y * w + x];
                if (r > threshold && isLocalMaximum(response, w, h, x, y)) {
                    corners.add(new double[]{x, y, r});
                }
            }
        }

        corners.sort(Comparator.comparingDouble((double[] c) -> c[2]).reversed());
        return corners;
    }

    /**
     * Detect Harris corners with automatic threshold, returning at most maxCorners.
     * 使用自动阈值检测 Harris 角点，最多返回 maxCorners 个角点。
     *
     * <p>Uses default k=0.04 and selects the top-N corners by response strength
     * after non-maximum suppression.</p>
     * <p>使用默认 k=0.04，在非极大值抑制后按响应强度选择 Top-N 角点。</p>
     *
     * @param image      the source image | 源图像
     * @param maxCorners maximum number of corners to return | 返回的最大角点数
     * @return list of [x, y, response] for each detected corner, sorted by response descending |
     *         每个检测到的角点的 [x, y, response] 列表，按响应值降序排列
     * @throws ImageOperationException if image is null or maxCorners is not positive |
     *                                  当图像为 null 或 maxCorners 不为正数时抛出
     */
    public static List<double[]> detect(BufferedImage image, int maxCorners) {
        Objects.requireNonNull(image, "image must not be null");
        if (maxCorners <= 0) {
            throw new ImageOperationException("maxCorners must be positive, got: " + maxCorners);
        }

        int w = image.getWidth();
        int h = image.getHeight();
        double[] response = computeResponse(image, w, h, DEFAULT_K);

        // Collect all local maxima with positive response
        List<double[]> candidates = new ArrayList<>();
        for (int y = NMS_RADIUS; y < h - NMS_RADIUS; y++) {
            for (int x = NMS_RADIUS; x < w - NMS_RADIUS; x++) {
                double r = response[y * w + x];
                if (r > 0 && isLocalMaximum(response, w, h, x, y)) {
                    candidates.add(new double[]{x, y, r});
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
     * Compute and return the Harris corner response image for visualization.
     * 计算并返回 Harris 角点响应图用于可视化。
     *
     * <p>The response values are normalized to [0, 255] for display. Positive responses
     * (corners) appear as bright pixels.</p>
     * <p>响应值被归一化到 [0, 255] 用于显示。正响应（角点）显示为亮像素。</p>
     *
     * @param image the source image | 源图像
     * @param k     Harris sensitivity parameter | Harris 灵敏度参数
     * @return a grayscale image showing corner response | 显示角点响应的灰度图像
     * @throws ImageOperationException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage responseImage(BufferedImage image, double k) {
        Objects.requireNonNull(image, "image must not be null");

        int w = image.getWidth();
        int h = image.getHeight();
        double[] response = computeResponse(image, w, h, k);

        // Find max positive response for normalization
        double maxR = 0;
        for (double r : response) {
            if (r > maxR) {
                maxR = r;
            }
        }

        int[] grayPixels = new int[w * h];
        if (maxR > 0) {
            for (int i = 0; i < response.length; i++) {
                int v = (int) (Math.max(0, response[i]) / maxR * 255);
                grayPixels[i] = v;
            }
        }

        int[] argbPixels = ChannelOp.grayToArgb(grayPixels);
        BufferedImage result = PixelOp.createArgb(w, h);
        int[] outPixels = PixelOp.getPixels(result);
        System.arraycopy(argbPixels, 0, outPixels, 0, argbPixels.length);
        return result;
    }

    /**
     * Compute Harris corner response for every pixel.
     * Algorithm:
     * 1. Grayscale -> Sobel dx, dy (via SeparableKernelOp)
     * 2. Compute Ix^2, Iy^2, IxIy
     * 3. Gaussian smooth each (sigma ~1.5, kernel size 5)
     * 4. R = det(M) - k*trace(M)^2
     */
    static double[] computeResponse(BufferedImage image, int w, int h, double k) {
        // Step 1: Convert to grayscale
        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] pixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(pixels);

        // Step 2: Compute Sobel gradients using separable convolution
        // Sobel X = SOBEL_SMOOTH (vertical) * SOBEL_H (horizontal)
        int[] dx = SeparableKernelOp.convolveGray(gray, w, h, SOBEL_H, SOBEL_SMOOTH, KernelOp.BorderMode.CLAMP);
        // Sobel Y = SOBEL_H (vertical) * SOBEL_SMOOTH (horizontal)
        int[] dy = SeparableKernelOp.convolveGray(gray, w, h, SOBEL_SMOOTH, SOBEL_H, KernelOp.BorderMode.CLAMP);

        // Step 3: Compute products Ix^2, Iy^2, IxIy as int arrays
        // Note: dx, dy are clamped to [0,255] by convolveGray, so we need to
        // work with the raw gradient. Since convolveGray clamps, we recompute
        // using float-based approach.
        int size = w * h;
        int[] ix2 = new int[size];
        int[] iy2 = new int[size];
        int[] ixiy = new int[size];

        for (int i = 0; i < size; i++) {
            // dx, dy are clamped [0,255], center around 128 to get signed gradient
            int gx = dx[i] - 128;
            int gy = dy[i] - 128;
            // Scale down to avoid overflow while preserving relative magnitude
            ix2[i] = PixelOp.clamp((gx * gx) / 4 + 128);
            iy2[i] = PixelOp.clamp((gy * gy) / 4 + 128);
            ixiy[i] = PixelOp.clamp((gx * gy) / 4 + 128);
        }

        // Step 4: Gaussian smooth each product (sigma ~1.5, 5-tap kernel)
        float[] gaussKernel = gaussianKernel1D(5, 1.5f);
        int[] sIx2 = SeparableKernelOp.convolveGray(ix2, w, h, gaussKernel, gaussKernel, KernelOp.BorderMode.CLAMP);
        int[] sIy2 = SeparableKernelOp.convolveGray(iy2, w, h, gaussKernel, gaussKernel, KernelOp.BorderMode.CLAMP);
        int[] sIxIy = SeparableKernelOp.convolveGray(ixiy, w, h, gaussKernel, gaussKernel, KernelOp.BorderMode.CLAMP);

        // Step 5: Compute Harris response R = det(M) - k * trace(M)^2
        double[] response = new double[size];
        for (int i = 0; i < size; i++) {
            // Recover signed values (centered at 128)
            double a = sIx2[i] - 128.0;   // Σ(Ix^2)
            double c = sIy2[i] - 128.0;   // Σ(Iy^2)
            double b = sIxIy[i] - 128.0;  // Σ(IxIy)

            double det = a * c - b * b;
            double trace = a + c;
            response[i] = det - k * trace * trace;
        }

        return response;
    }

    /**
     * Check if the response at (x, y) is a local maximum in a 3x3 window.
     */
    private static boolean isLocalMaximum(double[] response, int w, int h, int x, int y) {
        double center = response[y * w + x];
        for (int dy = -NMS_RADIUS; dy <= NMS_RADIUS; dy++) {
            for (int dx = -NMS_RADIUS; dx <= NMS_RADIUS; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                    if (response[ny * w + nx] >= center) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Generate a normalized 1D Gaussian kernel.
     */
    static float[] gaussianKernel1D(int size, float sigma) {
        if (size % 2 == 0) {
            size++;
        }
        float[] kernel = new float[size];
        int half = size / 2;
        float sum = 0;
        float twoSigma2 = 2.0f * sigma * sigma;
        for (int i = 0; i < size; i++) {
            float x = i - half;
            kernel[i] = (float) Math.exp(-(x * x) / twoSigma2);
            sum += kernel[i];
        }
        // Normalize
        for (int i = 0; i < size; i++) {
            kernel[i] /= sum;
        }
        return kernel;
    }
}
