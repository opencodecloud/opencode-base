package cloud.opencode.base.image.kernel;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;

import java.util.Objects;

/**
 * Channel Splitting and Merging Operations
 * 通道分离与合并操作工具类
 *
 * <p>Provides operations for splitting ARGB pixels into individual channels,
 * merging channels back, and converting between ARGB and grayscale.</p>
 * <p>提供将 ARGB 像素分离为各通道、合并通道以及 ARGB 与灰度之间转换的操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Split ARGB pixels into 4 separate channel arrays - 将 ARGB 像素分离为 4 个独立通道数组</li>
 *   <li>Merge 4 channel arrays back into ARGB - 将 4 个通道数组合并为 ARGB</li>
 *   <li>RGB to grayscale conversion using ITU-R BT.601 coefficients - 使用 ITU-R BT.601 系数进行 RGB 到灰度转换</li>
 *   <li>Grayscale to ARGB conversion - 灰度到 ARGB 转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * int[] argbPixels = PixelOp.getPixels(image);
 * int[][] channels = ChannelOp.split(argbPixels);
 * // channels[0]=alpha, channels[1]=red, channels[2]=green, channels[3]=blue
 * int[] merged = ChannelOp.merge(channels[0], channels[1], channels[2], channels[3]);
 *
 * int[] gray = ChannelOp.toGray(argbPixels);
 * int[] backToArgb = ChannelOp.grayToArgb(gray);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for all operations where n = pixel count - 时间复杂度: 所有操作为 O(n)，n 为像素数量</li>
 *   <li>Space complexity: O(n) - allocates new arrays - 空间复杂度: O(n) - 分配新数组</li>
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
public final class ChannelOp {

    private ChannelOp() {
        throw new AssertionError("No ChannelOp instances");
    }

    /**
     * Split ARGB pixel array into 4 separate channel arrays [alpha, red, green, blue].
     * 将 ARGB 像素数组分离为 4 个独立通道数组 [alpha, red, green, blue]。
     *
     * @param argbPixels the ARGB pixel array | ARGB 像素数组
     * @return a 2D array [4][n]: [0]=alpha, [1]=red, [2]=green, [3]=blue | 二维数组 [4][n]: [0]=alpha, [1]=red, [2]=green, [3]=blue
     * @throws ImageOperationException if argbPixels is null or empty | 当 argbPixels 为 null 或空时抛出
     */
    public static int[][] split(int[] argbPixels) {
        Objects.requireNonNull(argbPixels, "argbPixels must not be null");
        if (argbPixels.length == 0) {
            throw new ImageOperationException("argbPixels must not be empty", ImageErrorCode.INVALID_PARAMETERS);
        }
        int len = argbPixels.length;
        int[] a = new int[len];
        int[] r = new int[len];
        int[] g = new int[len];
        int[] b = new int[len];
        for (int i = 0; i < len; i++) {
            int px = argbPixels[i];
            a[i] = (px >> 24) & 0xFF;
            r[i] = (px >> 16) & 0xFF;
            g[i] = (px >> 8) & 0xFF;
            b[i] = px & 0xFF;
        }
        return new int[][]{a, r, g, b};
    }

    /**
     * Merge 4 separate channel arrays into a single ARGB pixel array.
     * 将 4 个独立通道数组合并为单个 ARGB 像素数组。
     *
     * @param a alpha channel array | Alpha 通道数组
     * @param r red channel array | 红色通道数组
     * @param g green channel array | 绿色通道数组
     * @param b blue channel array | 蓝色通道数组
     * @return the merged ARGB pixel array | 合并后的 ARGB 像素数组
     * @throws ImageOperationException if any array is null or lengths differ | 当任一数组为 null 或长度不一致时抛出
     */
    public static int[] merge(int[] a, int[] r, int[] g, int[] b) {
        Objects.requireNonNull(a, "alpha array must not be null");
        Objects.requireNonNull(r, "red array must not be null");
        Objects.requireNonNull(g, "green array must not be null");
        Objects.requireNonNull(b, "blue array must not be null");
        if (a.length != r.length || r.length != g.length || g.length != b.length) {
            throw new ImageOperationException(
                    "Channel arrays must have equal length, got: a=" + a.length
                            + " r=" + r.length + " g=" + g.length + " b=" + b.length,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        int len = a.length;
        int[] result = new int[len];
        for (int i = 0; i < len; i++) {
            result[i] = (a[i] << 24) | (r[i] << 16) | (g[i] << 8) | b[i];
        }
        return result;
    }

    /**
     * Convert ARGB pixel array to grayscale using ITU-R BT.601 coefficients (0.299R + 0.587G + 0.114B).
     * 使用 ITU-R BT.601 系数 (0.299R + 0.587G + 0.114B) 将 ARGB 像素数组转换为灰度。
     *
     * @param argbPixels the ARGB pixel array | ARGB 像素数组
     * @return grayscale values array [0, 255] | 灰度值数组 [0, 255]
     * @throws ImageOperationException if argbPixels is null or empty | 当 argbPixels 为 null 或空时抛出
     */
    public static int[] toGray(int[] argbPixels) {
        Objects.requireNonNull(argbPixels, "argbPixels must not be null");
        if (argbPixels.length == 0) {
            throw new ImageOperationException("argbPixels must not be empty", ImageErrorCode.INVALID_PARAMETERS);
        }
        int len = argbPixels.length;
        int[] gray = new int[len];
        for (int i = 0; i < len; i++) {
            int px = argbPixels[i];
            int r = (px >> 16) & 0xFF;
            int g = (px >> 8) & 0xFF;
            int b = px & 0xFF;
            // ITU-R BT.601: 0.299R + 0.587G + 0.114B
            // Using fixed-point: (299*R + 587*G + 114*B + 500) / 1000
            gray[i] = (299 * r + 587 * g + 114 * b + 500) / 1000;
        }
        return gray;
    }

    /**
     * Convert grayscale values to ARGB pixel array (gray copied to R/G/B, A=255).
     * 将灰度值转换为 ARGB 像素数组（灰度值复制到 R/G/B，A=255）。
     *
     * @param grayPixels the grayscale values array [0, 255] | 灰度值数组 [0, 255]
     * @return ARGB pixel array | ARGB 像素数组
     * @throws ImageOperationException if grayPixels is null or empty | 当 grayPixels 为 null 或空时抛出
     */
    public static int[] grayToArgb(int[] grayPixels) {
        Objects.requireNonNull(grayPixels, "grayPixels must not be null");
        if (grayPixels.length == 0) {
            throw new ImageOperationException("grayPixels must not be empty", ImageErrorCode.INVALID_PARAMETERS);
        }
        int len = grayPixels.length;
        int[] argb = new int[len];
        for (int i = 0; i < len; i++) {
            int v = grayPixels[i];
            argb[i] = (0xFF << 24) | (v << 16) | (v << 8) | v;
        }
        return argb;
    }
}
