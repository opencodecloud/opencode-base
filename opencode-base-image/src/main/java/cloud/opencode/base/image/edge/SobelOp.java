package cloud.opencode.base.image.edge;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.KernelOp;
import cloud.opencode.base.image.kernel.PixelOp;
import cloud.opencode.base.image.kernel.SeparableKernelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Sobel Edge Detection Operator
 * Sobel 边缘检测算子
 *
 * <p>Computes gradient magnitude using separable Sobel kernels. The horizontal
 * kernel detects vertical edges and the vertical kernel detects horizontal edges.
 * The combined gradient magnitude highlights all edges.</p>
 * <p>使用可分离 Sobel 卷积核计算梯度幅值。水平卷积核检测垂直边缘，
 * 垂直卷积核检测水平边缘。合成梯度幅值突显所有边缘。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Full gradient magnitude (sqrt(gx^2 + gy^2)) edge detection - 完整梯度幅值边缘检测</li>
 *   <li>Directional gradient (horizontal or vertical only) - 方向梯度（仅水平或垂直）</li>
 *   <li>Grayscale gradient magnitude on raw pixel arrays - 灰度像素数组上的梯度幅值计算</li>
 *   <li>Separable convolution for O(w*h*2k) performance - 可分离卷积实现 O(w*h*2k) 性能</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Full Sobel edge detection
 * BufferedImage edges = SobelOp.apply(image);
 *
 * // Horizontal gradient only (detects vertical edges)
 * BufferedImage horizontal = SobelOp.apply(image, 1, 0);
 *
 * // Vertical gradient only (detects horizontal edges)
 * BufferedImage vertical = SobelOp.apply(image, 0, 1);
 *
 * // Gradient magnitude on grayscale array
 * int[] magnitude = SobelOp.gradientMagnitude(grayPixels, width, height);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(w * h) using separable 3-tap kernels - 时间复杂度: 使用 3-tap 可分离卷积核 O(w * h)</li>
 *   <li>Space complexity: O(w * h) - 空间复杂度: O(w * h)</li>
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
public final class SobelOp {

    private SobelOp() {
        throw new AssertionError("No SobelOp instances");
    }

    /** Sobel horizontal separable kernels: [-1,0,1] x [1,2,1]^T */
    private static final float[] SOBEL_H_ROW = {-1f, 0f, 1f};
    private static final float[] SOBEL_H_COL = {1f, 2f, 1f};

    /** Sobel vertical separable kernels: [1,2,1] x [-1,0,1]^T */
    private static final float[] SOBEL_V_ROW = {1f, 2f, 1f};
    private static final float[] SOBEL_V_COL = {-1f, 0f, 1f};

    /**
     * Compute Sobel gradient magnitude image.
     * 计算 Sobel 梯度幅值图像。
     *
     * <p>Combines horizontal and vertical Sobel gradients using
     * magnitude = sqrt(gx^2 + gy^2), clamped to [0, 255].</p>
     * <p>使用 magnitude = sqrt(gx^2 + gy^2) 合成水平和垂直 Sobel 梯度，
     * 结果裁剪到 [0, 255]。</p>
     *
     * @param image the source image | 源图像
     * @return the gradient magnitude image | 梯度幅值图像
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage apply(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] pixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(pixels);

        int[] magnitude = gradientMagnitude(gray, w, h);

        int[] argbOut = ChannelOp.grayToArgb(magnitude);
        BufferedImage output = PixelOp.createArgb(w, h);
        int[] outPixels = PixelOp.getPixels(output);
        System.arraycopy(argbOut, 0, outPixels, 0, argbOut.length);
        return output;
    }

    /**
     * Compute directional Sobel gradient image.
     * 计算方向 Sobel 梯度图像。
     *
     * <p>Use dx=1, dy=0 for horizontal gradient (detects vertical edges),
     * or dx=0, dy=1 for vertical gradient (detects horizontal edges).</p>
     * <p>使用 dx=1, dy=0 计算水平梯度（检测垂直边缘），
     * 或 dx=0, dy=1 计算垂直梯度（检测水平边缘）。</p>
     *
     * @param image the source image | 源图像
     * @param dx    horizontal gradient flag (1 or 0) | 水平梯度标志（1 或 0）
     * @param dy    vertical gradient flag (1 or 0) | 垂直梯度标志（1 或 0）
     * @return the directional gradient image | 方向梯度图像
     * @throws NullPointerException      if image is null | 当图像为 null 时抛出
     * @throws ImageOperationException   if both dx and dy are 0 | 当 dx 和 dy 均为 0 时抛出
     */
    public static BufferedImage apply(BufferedImage image, int dx, int dy) {
        Objects.requireNonNull(image, "image must not be null");
        if (dx == 0 && dy == 0) {
            throw new ImageOperationException("At least one of dx or dy must be non-zero");
        }

        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] pixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(pixels);

        int[] result;
        if (dx != 0 && dy != 0) {
            result = gradientMagnitude(gray, w, h);
        } else if (dx != 0) {
            int[] gx = SeparableKernelOp.convolveGray(gray, w, h,
                    SOBEL_H_ROW, SOBEL_H_COL, KernelOp.BorderMode.CLAMP);
            result = absoluteClamp(gx);
        } else {
            int[] gy = SeparableKernelOp.convolveGray(gray, w, h,
                    SOBEL_V_ROW, SOBEL_V_COL, KernelOp.BorderMode.CLAMP);
            result = absoluteClamp(gy);
        }

        int[] argbOut = ChannelOp.grayToArgb(result);
        BufferedImage output = PixelOp.createArgb(w, h);
        int[] outPixels = PixelOp.getPixels(output);
        System.arraycopy(argbOut, 0, outPixels, 0, argbOut.length);
        return output;
    }

    /**
     * Compute gradient magnitude on a grayscale pixel array.
     * 对灰度像素数组计算梯度幅值。
     *
     * <p>Applies horizontal and vertical Sobel kernels and returns
     * magnitude = sqrt(gx^2 + gy^2) clamped to [0, 255].</p>
     * <p>应用水平和垂直 Sobel 卷积核，返回
     * magnitude = sqrt(gx^2 + gy^2) 裁剪到 [0, 255]。</p>
     *
     * @param grayPixels the grayscale pixel array [0, 255] | 灰度像素数组 [0, 255]
     * @param width      the image width | 图像宽度
     * @param height     the image height | 图像高度
     * @return the gradient magnitude array [0, 255] | 梯度幅值数组 [0, 255]
     * @throws NullPointerException    if grayPixels is null | 当 grayPixels 为 null 时抛出
     * @throws ImageOperationException if dimensions are invalid | 当尺寸无效时抛出
     */
    public static int[] gradientMagnitude(int[] grayPixels, int width, int height) {
        Objects.requireNonNull(grayPixels, "grayPixels must not be null");

        int[] gx = SeparableKernelOp.convolveGray(grayPixels, width, height,
                SOBEL_H_ROW, SOBEL_H_COL, KernelOp.BorderMode.CLAMP);
        int[] gy = SeparableKernelOp.convolveGray(grayPixels, width, height,
                SOBEL_V_ROW, SOBEL_V_COL, KernelOp.BorderMode.CLAMP);

        int len = grayPixels.length;
        int[] magnitude = new int[len];
        for (int i = 0; i < len; i++) {
            double mag = Math.sqrt((double) gx[i] * gx[i] + (double) gy[i] * gy[i]);
            magnitude[i] = PixelOp.clamp((int) Math.round(mag));
        }
        return magnitude;
    }

    /**
     * Take absolute value of each element and clamp to [0, 255].
     */
    private static int[] absoluteClamp(int[] values) {
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = PixelOp.clamp(Math.abs(values[i]));
        }
        return result;
    }
}
