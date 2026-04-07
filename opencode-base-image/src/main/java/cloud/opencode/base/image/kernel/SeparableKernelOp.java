package cloud.opencode.base.image.kernel;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Separable Convolution Engine
 * 可分离卷积引擎
 *
 * <p>Performs separable 2D convolution by decomposing into horizontal + vertical
 * 1D passes, reducing complexity from O(w*h*k^2) to O(w*h*2k).</p>
 * <p>通过将卷积分解为水平和垂直两次一维遍历来执行可分离二维卷积，
 * 将复杂度从 O(w*h*k^2) 降低到 O(w*h*2k)。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Separable convolution for ARGB images - ARGB 图像的可分离卷积</li>
 *   <li>Separable convolution for grayscale arrays - 灰度数组的可分离卷积</li>
 *   <li>Automatic parallel processing for images larger than 1M pixels - 超过 100 万像素时自动并行处理</li>
 *   <li>Configurable border handling via BorderMode - 通过 BorderMode 配置边界处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Gaussian blur (separable 3-tap)
 * float[] hKernel = {0.25f, 0.5f, 0.25f};
 * float[] vKernel = {0.25f, 0.5f, 0.25f};
 * BufferedImage blurred = SeparableKernelOp.convolve(image, hKernel, vKernel, BorderMode.CLAMP);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(w * h * (kw + kh)) - 时间复杂度: O(w * h * (kw + kh))</li>
 *   <li>Parallel threshold: 1,000,000 pixels - 并行阈值: 100 万像素</li>
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
public final class SeparableKernelOp {

    private SeparableKernelOp() {
        throw new AssertionError("No SeparableKernelOp instances");
    }

    /**
     * Parallel processing threshold in pixels.
     * 并行处理的像素数阈值。
     */
    private static final int PARALLEL_THRESHOLD = 1_000_000;

    /**
     * Perform separable 2D convolution on an ARGB image.
     * 对 ARGB 图像执行可分离二维卷积。
     *
     * @param image            the source image | 源图像
     * @param horizontalKernel the horizontal 1D kernel (length must be odd) | 水平一维卷积核（长度必须为奇数）
     * @param verticalKernel   the vertical 1D kernel (length must be odd) | 垂直一维卷积核（长度必须为奇数）
     * @param border           the border handling mode | 边界处理模式
     * @return the convolved image | 卷积后的图像
     * @throws ImageOperationException if parameters are invalid | 当参数无效时抛出
     */
    public static BufferedImage convolve(BufferedImage image, float[] horizontalKernel,
                                         float[] verticalKernel, KernelOp.BorderMode border) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(horizontalKernel, "horizontalKernel must not be null");
        Objects.requireNonNull(verticalKernel, "verticalKernel must not be null");
        Objects.requireNonNull(border, "border must not be null");
        validateKernel1D(horizontalKernel, "horizontalKernel");
        validateKernel1D(verticalKernel, "verticalKernel");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] src = PixelOp.getPixels(argb);

        int[][] channels = ChannelOp.split(src);
        int[] alphaChannel = channels[0];

        int[] outR = convolveChannelSeparable(channels[1], w, h, horizontalKernel, verticalKernel, border);
        int[] outG = convolveChannelSeparable(channels[2], w, h, horizontalKernel, verticalKernel, border);
        int[] outB = convolveChannelSeparable(channels[3], w, h, horizontalKernel, verticalKernel, border);

        int[] result = ChannelOp.merge(alphaChannel, outR, outG, outB);
        BufferedImage output = PixelOp.createCompatible(argb);
        int[] outPixels = PixelOp.getPixels(output);
        System.arraycopy(result, 0, outPixels, 0, result.length);
        return output;
    }

    /**
     * Perform separable 2D convolution on a grayscale pixel array.
     * 对灰度像素数组执行可分离二维卷积。
     *
     * @param pixels  the grayscale pixel array | 灰度像素数组
     * @param width   the image width | 图像宽度
     * @param height  the image height | 图像高度
     * @param hKernel the horizontal 1D kernel (length must be odd) | 水平一维卷积核（长度必须为奇数）
     * @param vKernel the vertical 1D kernel (length must be odd) | 垂直一维卷积核（长度必须为奇数）
     * @param border  the border handling mode | 边界处理模式
     * @return the convolved grayscale array | 卷积后的灰度数组
     * @throws ImageOperationException if parameters are invalid | 当参数无效时抛出
     */
    public static int[] convolveGray(int[] pixels, int width, int height,
                                     float[] hKernel, float[] vKernel, KernelOp.BorderMode border) {
        Objects.requireNonNull(pixels, "pixels must not be null");
        Objects.requireNonNull(hKernel, "hKernel must not be null");
        Objects.requireNonNull(vKernel, "vKernel must not be null");
        Objects.requireNonNull(border, "border must not be null");
        validateDimensions(width, height, pixels.length);
        validateKernel1D(hKernel, "hKernel");
        validateKernel1D(vKernel, "vKernel");
        return convolveChannelSeparable(pixels, width, height, hKernel, vKernel, border);
    }

    /**
     * Perform separable convolution on a single channel: horizontal pass then vertical pass.
     */
    private static int[] convolveChannelSeparable(int[] channel, int w, int h,
                                                   float[] hKernel, float[] vKernel,
                                                   KernelOp.BorderMode border) {
        // Horizontal pass
        float[] temp = new float[w * h];
        int halfH = hKernel.length / 2;
        boolean parallel = (long) w * h >= PARALLEL_THRESHOLD;

        IntStream rowRange = IntStream.range(0, h);
        if (parallel) {
            rowRange = rowRange.parallel();
        }
        rowRange.forEach(y -> {
            for (int x = 0; x < w; x++) {
                float sum = 0;
                for (int k = 0; k < hKernel.length; k++) {
                    int srcX = x + k - halfH;
                    int pixel = KernelOp.samplePixel(channel, w, h, srcX, y, border);
                    sum += pixel * hKernel[k];
                }
                temp[y * w + x] = sum;
            }
        });

        // Vertical pass
        int[] out = new int[w * h];
        int halfV = vKernel.length / 2;

        IntStream colRowRange = IntStream.range(0, h);
        if (parallel) {
            colRowRange = colRowRange.parallel();
        }
        colRowRange.forEach(y -> {
            for (int x = 0; x < w; x++) {
                float sum = 0;
                for (int k = 0; k < vKernel.length; k++) {
                    int srcY = y + k - halfV;
                    float pixel = sampleFloat(temp, w, h, x, srcY, border);
                    sum += pixel * vKernel[k];
                }
                out[y * w + x] = PixelOp.clamp(Math.round(sum));
            }
        });

        return out;
    }

    /**
     * Sample a float pixel from the intermediate buffer with border handling.
     */
    private static float sampleFloat(float[] pixels, int w, int h, int x, int y, KernelOp.BorderMode border) {
        if (x >= 0 && x < w && y >= 0 && y < h) {
            return pixels[y * w + x];
        }
        return switch (border) {
            case ZERO -> 0f;
            case CLAMP -> {
                int cx = Math.clamp(x, 0, w - 1);
                int cy = Math.clamp(y, 0, h - 1);
                yield pixels[cy * w + cx];
            }
            case MIRROR -> {
                int mx = KernelOp.mirrorIndex(x, w);
                int my = KernelOp.mirrorIndex(y, h);
                yield pixels[my * w + mx];
            }
            case WRAP -> {
                int wx = Math.floorMod(x, w);
                int wy = Math.floorMod(y, h);
                yield pixels[wy * w + wx];
            }
        };
    }

    private static void validateKernel1D(float[] kernel, String name) {
        if (kernel.length == 0) {
            throw new ImageOperationException(
                    name + " must not be empty", ImageErrorCode.INVALID_PARAMETERS);
        }
        if (kernel.length % 2 == 0) {
            throw new ImageOperationException(
                    name + " length must be odd, got: " + kernel.length,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }

    private static void validateDimensions(int width, int height, int pixelCount) {
        if (width <= 0 || height <= 0) {
            throw new ImageOperationException(
                    "Image dimensions must be positive, got: " + width + "x" + height,
                    ImageErrorCode.INVALID_DIMENSIONS);
        }
        if ((long) width * height != pixelCount) {
            throw new ImageOperationException(
                    "Pixel array length must equal width*height, got: " + pixelCount + " != " + ((long) width * height),
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }
}
