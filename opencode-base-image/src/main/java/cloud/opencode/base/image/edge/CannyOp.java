package cloud.opencode.base.image.edge;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.KernelOp;
import cloud.opencode.base.image.kernel.PixelOp;
import cloud.opencode.base.image.kernel.SeparableKernelOp;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Canny Edge Detection Operator
 * Canny 边缘检测算子
 *
 * <p>Implements the full Canny edge detection pipeline: Gaussian blur,
 * Sobel gradient computation, non-maximum suppression, double thresholding,
 * and hysteresis edge tracking. Produces a binary edge map (0 or 255).</p>
 * <p>实现完整的 Canny 边缘检测流程：高斯模糊、Sobel 梯度计算、
 * 非极大值抑制、双阈值处理和滞后边缘跟踪。输出二值边缘图（0 或 255）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Full Canny pipeline with configurable thresholds - 完整 Canny 流程支持可配置阈值</li>
 *   <li>Gaussian smoothing via separable convolution (sigma=1.4) - 通过可分离卷积实现高斯平滑 (sigma=1.4)</li>
 *   <li>Non-maximum suppression for thin edges - 非极大值抑制实现细化边缘</li>
 *   <li>Hysteresis tracking for edge continuity - 滞后跟踪确保边缘连续性</li>
 *   <li>Binary output: only 0 or 255 pixel values - 二值输出：仅 0 或 255 像素值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Canny with custom thresholds
 * BufferedImage edges = CannyOp.apply(image, 50, 150);
 *
 * // Canny with default thresholds (50, 150) and sigma=1.4
 * BufferedImage edges = CannyOp.apply(image);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(w * h) for each pipeline stage - 时间复杂度: 每个流水线阶段 O(w * h)</li>
 *   <li>Space complexity: O(w * h) for intermediate buffers - 空间复杂度: 中间缓冲区 O(w * h)</li>
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
public final class CannyOp {

    private CannyOp() {
        throw new AssertionError("No CannyOp instances");
    }

    /** Default low threshold. */
    private static final double DEFAULT_LOW_THRESHOLD = 50.0;

    /** Default high threshold. */
    private static final double DEFAULT_HIGH_THRESHOLD = 150.0;

    /** Default Gaussian sigma. */
    private static final double DEFAULT_SIGMA = 1.4;

    /** Pixel marker: strong edge. */
    private static final int STRONG = 255;

    /** Pixel marker: weak edge. */
    private static final int WEAK = 128;

    /** Sobel horizontal separable kernels: [-1,0,1] x [1,2,1]^T */
    private static final float[] SOBEL_H_ROW = {-1f, 0f, 1f};
    private static final float[] SOBEL_H_COL = {1f, 2f, 1f};

    /** Sobel vertical separable kernels: [1,2,1] x [-1,0,1]^T */
    private static final float[] SOBEL_V_ROW = {1f, 2f, 1f};
    private static final float[] SOBEL_V_COL = {-1f, 0f, 1f};

    /**
     * Apply Canny edge detection with default thresholds (50, 150) and sigma=1.4.
     * 使用默认阈值 (50, 150) 和 sigma=1.4 应用 Canny 边缘检测。
     *
     * @param image the source image | 源图像
     * @return the binary edge image (0 or 255) | 二值边缘图像（0 或 255）
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage apply(BufferedImage image) {
        return apply(image, DEFAULT_LOW_THRESHOLD, DEFAULT_HIGH_THRESHOLD);
    }

    /**
     * Apply Canny edge detection with specified thresholds and sigma=1.4.
     * 使用指定阈值和 sigma=1.4 应用 Canny 边缘检测。
     *
     * @param image         the source image | 源图像
     * @param lowThreshold  the low threshold for hysteresis (must be >= 0 and <= highThreshold) | 滞后低阈值
     * @param highThreshold the high threshold for hysteresis (must be >= lowThreshold) | 滞后高阈值
     * @return the binary edge image (0 or 255) | 二值边缘图像（0 或 255）
     * @throws NullPointerException    if image is null | 当图像为 null 时抛出
     * @throws ImageOperationException if lowThreshold > highThreshold or thresholds are negative | 当低阈值大于高阈值或阈值为负时抛出
     */
    public static BufferedImage apply(BufferedImage image, double lowThreshold, double highThreshold) {
        Objects.requireNonNull(image, "image must not be null");
        if (lowThreshold < 0) {
            throw new ImageOperationException(
                    "lowThreshold must be >= 0, got: " + lowThreshold,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (highThreshold < lowThreshold) {
            throw new ImageOperationException(
                    "lowThreshold must be <= highThreshold, got: low=" + lowThreshold + " high=" + highThreshold,
                    ImageErrorCode.INVALID_PARAMETERS);
        }

        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] pixels = PixelOp.getPixels(argb);

        // Step 1: Convert to grayscale
        int[] gray = ChannelOp.toGray(pixels);

        // Step 2: Gaussian blur via SeparableKernelOp (NOT filter/GaussianBlurOp)
        float[] gaussianKernel = buildGaussianKernel1D(DEFAULT_SIGMA);
        int[] blurred = SeparableKernelOp.convolveGray(gray, w, h,
                gaussianKernel, gaussianKernel, KernelOp.BorderMode.CLAMP);

        // Step 3: Sobel gradients (float precision for magnitude + direction)
        float[] gxFloat = convolveGrayFloat(blurred, w, h, SOBEL_H_ROW, SOBEL_H_COL);
        float[] gyFloat = convolveGrayFloat(blurred, w, h, SOBEL_V_ROW, SOBEL_V_COL);

        int len = w * h;
        float[] magnitude = new float[len];
        float[] direction = new float[len];
        for (int i = 0; i < len; i++) {
            magnitude[i] = (float) Math.sqrt(gxFloat[i] * gxFloat[i] + gyFloat[i] * gyFloat[i]);
            direction[i] = (float) Math.atan2(gyFloat[i], gxFloat[i]);
        }

        // Step 4: Non-maximum suppression
        float[] suppressed = nonMaximumSuppression(magnitude, direction, w, h);

        // Step 5: Double threshold
        int[] thresholded = doubleThreshold(suppressed, lowThreshold, highThreshold, w, h);

        // Step 6: Hysteresis edge tracking
        int[] edges = hysteresis(thresholded, w, h);

        int[] argbOut = ChannelOp.grayToArgb(edges);
        BufferedImage output = PixelOp.createArgb(w, h);
        int[] outPixels = PixelOp.getPixels(output);
        System.arraycopy(argbOut, 0, outPixels, 0, argbOut.length);
        return output;
    }

    /**
     * Build a 1D Gaussian kernel for the given sigma.
     * Kernel radius = ceil(3 * sigma), length = 2 * radius + 1.
     */
    private static float[] buildGaussianKernel1D(double sigma) {
        int radius = (int) Math.ceil(3.0 * sigma);
        int size = 2 * radius + 1;
        float[] kernel = new float[size];
        double twoSigmaSq = 2.0 * sigma * sigma;
        double sum = 0;
        for (int i = 0; i < size; i++) {
            double x = i - radius;
            kernel[i] = (float) Math.exp(-(x * x) / twoSigmaSq);
            sum += kernel[i];
        }
        // Normalize
        for (int i = 0; i < size; i++) {
            kernel[i] /= (float) sum;
        }
        return kernel;
    }

    /**
     * Perform separable convolution on gray pixels and return float results
     * (no clamping) for accurate gradient computation.
     */
    private static float[] convolveGrayFloat(int[] gray, int w, int h,
                                              float[] hKernel, float[] vKernel) {
        int halfH = hKernel.length / 2;
        int halfV = vKernel.length / 2;

        // Horizontal pass
        float[] temp = new float[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float sum = 0;
                for (int k = 0; k < hKernel.length; k++) {
                    int srcX = x + k - halfH;
                    int cx = Math.clamp(srcX, 0, w - 1);
                    sum += gray[y * w + cx] * hKernel[k];
                }
                temp[y * w + x] = sum;
            }
        }

        // Vertical pass
        float[] out = new float[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float sum = 0;
                for (int k = 0; k < vKernel.length; k++) {
                    int srcY = y + k - halfV;
                    int cy = Math.clamp(srcY, 0, h - 1);
                    sum += temp[cy * w + x] * vKernel[k];
                }
                out[y * w + x] = sum;
            }
        }
        return out;
    }

    /**
     * Non-maximum suppression: for each pixel, if magnitude is less than the
     * neighbor along the gradient direction, suppress it to 0.
     */
    private static float[] nonMaximumSuppression(float[] magnitude, float[] direction,
                                                   int w, int h) {
        float[] result = new float[w * h];
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int idx = y * w + x;
                float mag = magnitude[idx];
                // Quantize gradient direction to one of 4 directions
                float angle = direction[idx];
                // Normalize to [0, PI)
                if (angle < 0) {
                    angle += (float) Math.PI;
                }

                float neighbor1;
                float neighbor2;

                // 0 degrees (horizontal): compare East and West
                if (angle < Math.PI / 8 || angle >= 7 * Math.PI / 8) {
                    neighbor1 = magnitude[y * w + (x + 1)];
                    neighbor2 = magnitude[y * w + (x - 1)];
                }
                // 45 degrees (diagonal): compare NE and SW
                else if (angle < 3 * Math.PI / 8) {
                    neighbor1 = magnitude[(y - 1) * w + (x + 1)];
                    neighbor2 = magnitude[(y + 1) * w + (x - 1)];
                }
                // 90 degrees (vertical): compare North and South
                else if (angle < 5 * Math.PI / 8) {
                    neighbor1 = magnitude[(y - 1) * w + x];
                    neighbor2 = magnitude[(y + 1) * w + x];
                }
                // 135 degrees (diagonal): compare NW and SE
                else {
                    neighbor1 = magnitude[(y - 1) * w + (x - 1)];
                    neighbor2 = magnitude[(y + 1) * w + (x + 1)];
                }

                result[idx] = (mag >= neighbor1 && mag >= neighbor2) ? mag : 0;
            }
        }
        return result;
    }

    /**
     * Double threshold: classify pixels as STRONG, WEAK, or 0.
     */
    private static int[] doubleThreshold(float[] suppressed, double lowThreshold,
                                          double highThreshold, int w, int h) {
        int[] result = new int[w * h];
        for (int i = 0; i < w * h; i++) {
            float mag = suppressed[i];
            if (mag >= highThreshold) {
                result[i] = STRONG;
            } else if (mag >= lowThreshold) {
                result[i] = WEAK;
            } else {
                result[i] = 0;
            }
        }
        return result;
    }

    /**
     * Hysteresis edge tracking: WEAK pixels connected (8-connectivity)
     * to STRONG pixels become STRONG; isolated WEAK pixels become 0.
     */
    private static int[] hysteresis(int[] thresholded, int w, int h) {
        int[] result = new int[w * h];
        System.arraycopy(thresholded, 0, result, 0, w * h);

        // BFS: seed queue with all STRONG pixels, propagate to connected WEAK pixels
        Deque<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < result.length; i++) {
            if (result[i] == STRONG) {
                queue.add(i);
            }
        }
        while (!queue.isEmpty()) {
            int idx = queue.poll();
            int y = idx / w;
            int x = idx % w;
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }
                    int ny = y + dy;
                    int nx = x + dx;
                    if (ny >= 0 && ny < h && nx >= 0 && nx < w) {
                        int ni = ny * w + nx;
                        if (result[ni] == WEAK) {
                            result[ni] = STRONG;
                            queue.add(ni);
                        }
                    }
                }
            }
        }

        // Suppress remaining WEAK pixels to 0
        for (int i = 0; i < result.length; i++) {
            if (result[i] != STRONG) {
                result[i] = 0;
            }
        }
        return result;
    }
}
