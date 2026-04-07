package cloud.opencode.base.image.threshold;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.IntegralImage;
import cloud.opencode.base.image.kernel.KernelOp;
import cloud.opencode.base.image.kernel.PixelOp;
import cloud.opencode.base.image.kernel.SeparableKernelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Adaptive Thresholding Operations
 * 自适应阈值操作工具类
 *
 * <p>Applies locally adaptive thresholding where the threshold for each pixel is
 * computed from its local neighborhood, making it robust to uneven illumination.</p>
 * <p>应用局部自适应阈值处理，每个像素的阈值由其局部邻域计算得到，
 * 对不均匀光照具有鲁棒性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>MEAN method: local mean via integral image (O(1) per pixel) - MEAN 方法：通过积分图计算局部均值（每像素 O(1)）</li>
 *   <li>GAUSSIAN method: Gaussian-weighted local mean via separable convolution - GAUSSIAN 方法：通过可分离卷积计算高斯加权局部均值</li>
 *   <li>Configurable block size and constant offset - 可配置的块大小和常数偏移</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Adaptive mean thresholding
 * BufferedImage binary = AdaptiveThresholdOp.apply(image, 15, 10.0);
 *
 * // Adaptive Gaussian thresholding
 * BufferedImage gaussian = AdaptiveThresholdOp.apply(image, 15, 10.0, AdaptiveThresholdOp.Method.GAUSSIAN);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>MEAN method: O(n) using integral image - MEAN 方法：使用积分图 O(n)</li>
 *   <li>GAUSSIAN method: O(n * k) using separable convolution - GAUSSIAN 方法：使用可分离卷积 O(n * k)</li>
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
public final class AdaptiveThresholdOp {

    private AdaptiveThresholdOp() {
        throw new AssertionError("No AdaptiveThresholdOp instances");
    }

    /**
     * Adaptive threshold method enumeration.
     * 自适应阈值方法枚举。
     */
    public enum Method {
        /** Local mean computed via integral image | 通过积分图计算局部均值 */
        MEAN,
        /** Gaussian-weighted local mean via separable convolution | 通过可分离卷积计算高斯加权局部均值 */
        GAUSSIAN
    }

    /**
     * Apply adaptive threshold with the specified method.
     * 使用指定方法应用自适应阈值。
     *
     * <p>For each pixel, a local threshold is computed from a blockSize x blockSize
     * neighborhood. The pixel is set to 255 if its value exceeds (local_mean - c),
     * otherwise it is set to 0.</p>
     * <p>对每个像素，从 blockSize x blockSize 邻域计算局部阈值。
     * 若像素值大于 (局部均值 - c) 则置 255，否则置 0。</p>
     *
     * @param image     the source image | 源图像
     * @param blockSize the neighborhood block size (must be odd and >= 3) | 邻域块大小（必须为奇数且 >= 3）
     * @param c         the constant subtracted from the local mean | 从局部均值中减去的常数
     * @param method    the adaptive method | 自适应方法
     * @return the binary thresholded image | 二值化后的图像
     * @throws ImageOperationException if parameters are invalid | 当参数无效时抛出
     */
    public static BufferedImage apply(BufferedImage image, int blockSize, double c, Method method) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(method, "method must not be null");
        validateBlockSize(blockSize);
        if (Double.isNaN(c) || Double.isInfinite(c)) {
            throw new ImageOperationException(
                    "Constant c must be a finite number, got: " + c,
                    ImageErrorCode.INVALID_PARAMETERS);
        }

        // Convert to grayscale
        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] srcPixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(srcPixels);

        // Compute local means
        int[] localMeans = switch (method) {
            case MEAN -> computeMeanLocalMeans(gray, w, h, blockSize);
            case GAUSSIAN -> computeGaussianLocalMeans(gray, w, h, blockSize);
        };

        // Apply adaptive threshold: pixel > mean - c ? 255 : 0
        int[] result = new int[w * h];
        for (int i = 0; i < gray.length; i++) {
            result[i] = (gray[i] > localMeans[i] - c) ? 255 : 0;
        }

        // Convert to ARGB output
        int[] outputArgb = ChannelOp.grayToArgb(result);
        BufferedImage output = PixelOp.createArgb(w, h);
        int[] outPixels = PixelOp.getPixels(output);
        System.arraycopy(outputArgb, 0, outPixels, 0, outputArgb.length);
        return output;
    }

    /**
     * Apply adaptive threshold with MEAN method (convenience method).
     * 使用 MEAN 方法应用自适应阈值（便捷方法）。
     *
     * <p>Equivalent to {@code apply(image, blockSize, c, Method.MEAN)}.</p>
     * <p>等价于 {@code apply(image, blockSize, c, Method.MEAN)}。</p>
     *
     * @param image     the source image | 源图像
     * @param blockSize the neighborhood block size (must be odd and >= 3) | 邻域块大小（必须为奇数且 >= 3）
     * @param c         the constant subtracted from the local mean | 从局部均值中减去的常数
     * @return the binary thresholded image | 二值化后的图像
     * @throws ImageOperationException if parameters are invalid | 当参数无效时抛出
     */
    public static BufferedImage apply(BufferedImage image, int blockSize, double c) {
        return apply(image, blockSize, c, Method.MEAN);
    }

    /**
     * Compute local means using integral image (MEAN method).
     */
    private static int[] computeMeanLocalMeans(int[] gray, int w, int h, int blockSize) {
        IntegralImage integral = new IntegralImage(gray, w, h);
        int half = blockSize / 2;
        int[] means = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int x1 = Math.max(0, x - half);
                int y1 = Math.max(0, y - half);
                int x2 = Math.min(w - 1, x + half);
                int y2 = Math.min(h - 1, y + half);
                double mean = integral.regionMean(x1, y1, x2, y2);
                means[y * w + x] = (int) Math.round(mean);
            }
        }
        return means;
    }

    /**
     * Compute local means using Gaussian-weighted separable convolution (GAUSSIAN method).
     */
    private static int[] computeGaussianLocalMeans(int[] gray, int w, int h, int blockSize) {
        float[] kernel = createGaussianKernel(blockSize);
        return SeparableKernelOp.convolveGray(gray, w, h, kernel, kernel, KernelOp.BorderMode.CLAMP);
    }

    /**
     * Create a normalized 1D Gaussian kernel of the given size.
     */
    private static float[] createGaussianKernel(int size) {
        float[] kernel = new float[size];
        // sigma = 0.3 * ((size - 1) * 0.5 - 1) + 0.8 (OpenCV formula)
        double sigma = 0.3 * ((size - 1) * 0.5 - 1) + 0.8;
        int half = size / 2;
        double sum = 0;

        for (int i = 0; i < size; i++) {
            double x = i - half;
            kernel[i] = (float) Math.exp(-(x * x) / (2.0 * sigma * sigma));
            sum += kernel[i];
        }

        // Normalize
        for (int i = 0; i < size; i++) {
            kernel[i] /= (float) sum;
        }
        return kernel;
    }

    private static void validateBlockSize(int blockSize) {
        if (blockSize < 3) {
            throw new ImageOperationException(
                    "Block size must be >= 3, got: " + blockSize,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (blockSize % 2 == 0) {
            throw new ImageOperationException(
                    "Block size must be odd, got: " + blockSize,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }
}
