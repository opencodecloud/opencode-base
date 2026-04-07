package cloud.opencode.base.image.edge;

import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.KernelOp;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Laplacian Edge Detection Operator
 * Laplacian 边缘检测算子
 *
 * <p>Applies a 3x3 Laplacian kernel to detect edges by computing the second
 * derivative of image intensity. The kernel [0,1,0, 1,-4,1, 0,1,0] responds
 * strongly to rapid intensity changes in all directions.</p>
 * <p>应用 3x3 Laplacian 卷积核通过计算图像强度的二阶导数来检测边缘。
 * 卷积核 [0,1,0, 1,-4,1, 0,1,0] 对所有方向的快速强度变化都有强响应。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Second-derivative edge detection using 3x3 Laplacian - 使用 3x3 Laplacian 的二阶导数边缘检测</li>
 *   <li>Omnidirectional edge sensitivity - 全方向边缘敏感度</li>
 *   <li>Absolute value output clamped to [0, 255] - 取绝对值输出并裁剪到 [0, 255]</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BufferedImage edges = LaplacianOp.apply(image);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(w * h * 9) for 3x3 kernel - 时间复杂度: 3x3 卷积核 O(w * h * 9)</li>
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
public final class LaplacianOp {

    private LaplacianOp() {
        throw new AssertionError("No LaplacianOp instances");
    }

    /**
     * 3x3 Laplacian kernel: [0,1,0, 1,-4,1, 0,1,0].
     * 3x3 Laplacian 卷积核。
     */
    private static final float[] LAPLACIAN_KERNEL = {
            0f, 1f, 0f,
            1f, -4f, 1f,
            0f, 1f, 0f
    };

    /**
     * Apply Laplacian edge detection to an image.
     * 对图像应用 Laplacian 边缘检测。
     *
     * <p>Converts the image to grayscale, applies the 3x3 Laplacian kernel,
     * takes the absolute value of each pixel, and clamps to [0, 255].</p>
     * <p>将图像转换为灰度，应用 3x3 Laplacian 卷积核，
     * 对每个像素取绝对值并裁剪到 [0, 255]。</p>
     *
     * @param image the source image | 源图像
     * @return the edge-detected image | 边缘检测后的图像
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage apply(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");

        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] pixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(pixels);

        // Apply Laplacian convolution preserving negative values, then take absolute value
        int[] result = convolveAbsolute(gray, w, h);

        int[] argbOut = ChannelOp.grayToArgb(result);
        BufferedImage output = PixelOp.createArgb(w, h);
        int[] outPixels = PixelOp.getPixels(output);
        System.arraycopy(argbOut, 0, outPixels, 0, argbOut.length);
        return output;
    }

    /**
     * Apply Laplacian kernel and take absolute value, clamped to [0, 255].
     */
    private static int[] convolveAbsolute(int[] gray, int w, int h) {
        int[] out = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float sum = 0;
                for (int ky = 0; ky < 3; ky++) {
                    for (int kx = 0; kx < 3; kx++) {
                        int srcX = x + kx - 1;
                        int srcY = y + ky - 1;
                        int pixel = sampleClamp(gray, w, h, srcX, srcY);
                        sum += pixel * LAPLACIAN_KERNEL[ky * 3 + kx];
                    }
                }
                out[y * w + x] = PixelOp.clamp(Math.abs(Math.round(sum)));
            }
        }
        return out;
    }

    /**
     * Sample pixel with clamp border handling.
     */
    private static int sampleClamp(int[] pixels, int w, int h, int x, int y) {
        int cx = Math.clamp(x, 0, w - 1);
        int cy = Math.clamp(y, 0, h - 1);
        return pixels[cy * w + cx];
    }
}
