package cloud.opencode.base.image.kernel;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * General-purpose 2D Convolution Engine
 * 通用二维卷积引擎
 *
 * <p>Performs 2D convolution on ARGB images or grayscale pixel arrays
 * with configurable border handling modes.</p>
 * <p>对 ARGB 图像或灰度像素数组执行二维卷积，支持可配置的边界处理模式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>General 2D convolution for ARGB images (R/G/B convolved separately) - ARGB 图像通用二维卷积（R/G/B 分别卷积）</li>
 *   <li>General 2D convolution for grayscale arrays - 灰度数组通用二维卷积</li>
 *   <li>Four border handling modes: ZERO, CLAMP, MIRROR, WRAP - 四种边界处理模式：补零、截断、镜像、环绕</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Identity kernel (3x3)
 * float[] identity = {0,0,0, 0,1,0, 0,0,0};
 * BufferedImage result = KernelOp.convolve(image, identity, 3, 3, BorderMode.CLAMP);
 *
 * // Box blur (3x3)
 * float v = 1.0f / 9;
 * float[] box = {v,v,v, v,v,v, v,v,v};
 * BufferedImage blurred = KernelOp.convolve(image, box, 3, 3, BorderMode.CLAMP);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(w * h * kw * kh) - 时间复杂度: O(w * h * kw * kh)</li>
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
public final class KernelOp {

    private KernelOp() {
        throw new AssertionError("No KernelOp instances");
    }

    /**
     * Border handling mode for convolution operations.
     * 卷积操作的边界处理模式。
     */
    public enum BorderMode {
        /** Pad with zeros | 补零 */
        ZERO,
        /** Clamp to edge pixel | 截断到边缘像素 */
        CLAMP,
        /** Mirror at border | 镜像 */
        MIRROR,
        /** Wrap around | 环绕 */
        WRAP
    }

    /**
     * Perform 2D convolution on an ARGB image (R/G/B channels convolved separately, alpha preserved).
     * 对 ARGB 图像执行二维卷积（R/G/B 通道分别卷积，Alpha 通道保持不变）。
     *
     * @param image        the source ARGB image | 源 ARGB 图像
     * @param kernel       the convolution kernel (row-major) | 卷积核（行优先）
     * @param kernelWidth  the kernel width (must be odd) | 卷积核宽度（必须为奇数）
     * @param kernelHeight the kernel height (must be odd) | 卷积核高度（必须为奇数）
     * @param border       the border handling mode | 边界处理模式
     * @return the convolved image | 卷积后的图像
     * @throws ImageOperationException if parameters are invalid | 当参数无效时抛出
     */
    public static BufferedImage convolve(BufferedImage image, float[] kernel,
                                         int kernelWidth, int kernelHeight, BorderMode border) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(kernel, "kernel must not be null");
        Objects.requireNonNull(border, "border must not be null");
        validateKernel(kernel, kernelWidth, kernelHeight);

        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] src = PixelOp.getPixels(argb);

        int[][] channels = ChannelOp.split(src);
        int[] alphaChannel = channels[0];
        int[] redChannel = channels[1];
        int[] greenChannel = channels[2];
        int[] blueChannel = channels[3];

        int[] outR = convolveChannel(redChannel, w, h, kernel, kernelWidth, kernelHeight, border);
        int[] outG = convolveChannel(greenChannel, w, h, kernel, kernelWidth, kernelHeight, border);
        int[] outB = convolveChannel(blueChannel, w, h, kernel, kernelWidth, kernelHeight, border);

        int[] result = ChannelOp.merge(alphaChannel, outR, outG, outB);
        BufferedImage output = PixelOp.createCompatible(argb);
        int[] outPixels = PixelOp.getPixels(output);
        System.arraycopy(result, 0, outPixels, 0, result.length);
        return output;
    }

    /**
     * Perform 2D convolution on a grayscale pixel array.
     * 对灰度像素数组执行二维卷积。
     *
     * @param pixels the grayscale pixel array | 灰度像素数组
     * @param width  the image width | 图像宽度
     * @param height the image height | 图像高度
     * @param kernel the convolution kernel (row-major) | 卷积核（行优先）
     * @param kw     the kernel width (must be odd) | 卷积核宽度（必须为奇数）
     * @param kh     the kernel height (must be odd) | 卷积核高度（必须为奇数）
     * @param border the border handling mode | 边界处理模式
     * @return the convolved grayscale array | 卷积后的灰度数组
     * @throws ImageOperationException if parameters are invalid | 当参数无效时抛出
     */
    public static int[] convolveGray(int[] pixels, int width, int height,
                                     float[] kernel, int kw, int kh, BorderMode border) {
        Objects.requireNonNull(pixels, "pixels must not be null");
        Objects.requireNonNull(kernel, "kernel must not be null");
        Objects.requireNonNull(border, "border must not be null");
        validateDimensions(width, height, pixels.length);
        validateKernel(kernel, kw, kh);
        return convolveChannel(pixels, width, height, kernel, kw, kh, border);
    }

    /**
     * Convolve a single channel.
     */
    static int[] convolveChannel(int[] channel, int w, int h,
                                 float[] kernel, int kw, int kh, BorderMode border) {
        int halfW = kw / 2;
        int halfH = kh / 2;
        int[] out = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float sum = 0;
                for (int ky = 0; ky < kh; ky++) {
                    for (int kx = 0; kx < kw; kx++) {
                        int srcY = y + ky - halfH;
                        int srcX = x + kx - halfW;
                        int pixel = samplePixel(channel, w, h, srcX, srcY, border);
                        sum += pixel * kernel[ky * kw + kx];
                    }
                }
                out[y * w + x] = PixelOp.clamp(Math.round(sum));
            }
        }
        return out;
    }

    /**
     * Sample a pixel with border handling.
     */
    static int samplePixel(int[] pixels, int w, int h, int x, int y, BorderMode border) {
        if (x >= 0 && x < w && y >= 0 && y < h) {
            return pixels[y * w + x];
        }
        return switch (border) {
            case ZERO -> 0;
            case CLAMP -> {
                int cx = Math.clamp(x, 0, w - 1);
                int cy = Math.clamp(y, 0, h - 1);
                yield pixels[cy * w + cx];
            }
            case MIRROR -> {
                int mx = mirrorIndex(x, w);
                int my = mirrorIndex(y, h);
                yield pixels[my * w + mx];
            }
            case WRAP -> {
                int wx = Math.floorMod(x, w);
                int wy = Math.floorMod(y, h);
                yield pixels[wy * w + wx];
            }
        };
    }

    /**
     * Mirror an index within [0, size).
     */
    static int mirrorIndex(int i, int size) {
        if (i < 0) {
            i = -i - 1;
        }
        if (size == 1) {
            return 0;
        }
        // Period is 2*(size-1)
        int period = 2 * (size - 1);
        i = Math.floorMod(i, period);
        if (i >= size) {
            i = period - i;
        }
        return i;
    }

    private static void validateKernel(float[] kernel, int kw, int kh) {
        if (kw <= 0 || kh <= 0) {
            throw new ImageOperationException(
                    "Kernel dimensions must be positive, got: " + kw + "x" + kh,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (kw % 2 == 0 || kh % 2 == 0) {
            throw new ImageOperationException(
                    "Kernel dimensions must be odd, got: " + kw + "x" + kh,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (kernel.length != kw * kh) {
            throw new ImageOperationException(
                    "Kernel array length must equal kw*kh, got: " + kernel.length + " != " + (kw * kh),
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
