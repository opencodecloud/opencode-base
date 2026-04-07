package cloud.opencode.base.image.histogram;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Contrast Limited Adaptive Histogram Equalization (CLAHE)
 * 对比度限制自适应直方图均衡化 (CLAHE)
 *
 * <p>CLAHE divides the image into tiles, performs contrast-limited histogram equalization
 * on each tile, and uses bilinear interpolation between tiles to eliminate boundary artifacts.</p>
 * <p>CLAHE 将图像分割为多个块，对每个块执行对比度限制的直方图均衡化，
 * 并在块之间使用双线性插值消除边界伪影。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Adaptive local contrast enhancement - 自适应局部对比度增强</li>
 *   <li>Clip limit prevents noise amplification - 裁剪限制防止噪声放大</li>
 *   <li>Bilinear interpolation for smooth transitions - 双线性插值实现平滑过渡</li>
 *   <li>Configurable tile grid size and clip limit - 可配置的块网格大小和裁剪限制</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default parameters (clipLimit=2.0, tileGridSize=8)
 * BufferedImage result = ClaheOp.apply(image);
 *
 * // Custom parameters
 * BufferedImage result2 = ClaheOp.apply(image, 3.0, 16);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(w * h) - 时间复杂度: O(w * h)</li>
 *   <li>Space complexity: O(w * h + tileGridSize^2 * 256) - 空间复杂度: O(w * h + tileGridSize^2 * 256)</li>
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
public final class ClaheOp {

    private static final double DEFAULT_CLIP_LIMIT = 2.0;
    private static final int DEFAULT_TILE_GRID_SIZE = 8;

    private ClaheOp() {
        throw new AssertionError("No ClaheOp instances");
    }

    /**
     * Apply CLAHE with default parameters (clipLimit=2.0, tileGridSize=8).
     * 使用默认参数（clipLimit=2.0, tileGridSize=8）应用 CLAHE。
     *
     * @param image the source image | 源图像
     * @return the CLAHE-enhanced image | CLAHE 增强后的图像
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static BufferedImage apply(BufferedImage image) {
        return apply(image, DEFAULT_CLIP_LIMIT, DEFAULT_TILE_GRID_SIZE);
    }

    /**
     * Apply CLAHE with specified parameters.
     * 使用指定参数应用 CLAHE。
     *
     * @param image        the source image | 源图像
     * @param clipLimit    the contrast clip limit (must be positive) | 对比度裁剪限制（必须为正数）
     * @param tileGridSize the number of tiles in each dimension (must be positive) | 每个维度的块数（必须为正数）
     * @return the CLAHE-enhanced image | CLAHE 增强后的图像
     * @throws NullPointerException    if image is null | 当图像为 null 时抛出
     * @throws ImageOperationException if clipLimit or tileGridSize is invalid | 当参数无效时抛出
     */
    public static BufferedImage apply(BufferedImage image, double clipLimit, int tileGridSize) {
        Objects.requireNonNull(image, "image must not be null");
        if (clipLimit <= 0 || Double.isNaN(clipLimit) || Double.isInfinite(clipLimit)) {
            throw new ImageOperationException(
                    "clipLimit must be a positive finite number, got: " + clipLimit,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (tileGridSize <= 0) {
            throw new ImageOperationException(
                    "tileGridSize must be positive, got: " + tileGridSize,
                    ImageErrorCode.INVALID_PARAMETERS);
        }

        // Convert to grayscale
        BufferedImage argb = PixelOp.ensureArgb(image);
        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] pixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(pixels);

        // Compute tile dimensions
        int tileW = (w + tileGridSize - 1) / tileGridSize;
        int tileH = (h + tileGridSize - 1) / tileGridSize;

        // Compute per-tile equalization LUTs
        int[][] tileLuts = new int[tileGridSize * tileGridSize][256];

        for (int ty = 0; ty < tileGridSize; ty++) {
            for (int tx = 0; tx < tileGridSize; tx++) {
                int x0 = tx * tileW;
                int y0 = ty * tileH;
                int x1 = Math.min(x0 + tileW, w);
                int y1 = Math.min(y0 + tileH, h);
                int tilePixelCount = (x1 - x0) * (y1 - y0);

                if (tilePixelCount == 0) {
                    // Identity LUT for empty tiles
                    for (int i = 0; i < 256; i++) {
                        tileLuts[ty * tileGridSize + tx][i] = i;
                    }
                    continue;
                }

                // Compute tile histogram
                int[] hist = new int[256];
                for (int y = y0; y < y1; y++) {
                    for (int x = x0; x < x1; x++) {
                        hist[gray[y * w + x]]++;
                    }
                }

                // Clip histogram and redistribute excess
                int clipCount = (int) (clipLimit * tilePixelCount / 256);
                clipCount = Math.max(clipCount, 1);
                int excess = 0;
                for (int i = 0; i < 256; i++) {
                    if (hist[i] > clipCount) {
                        excess += hist[i] - clipCount;
                        hist[i] = clipCount;
                    }
                }

                // Distribute excess evenly
                int avgInc = excess / 256;
                int remainder = excess - avgInc * 256;
                for (int i = 0; i < 256; i++) {
                    hist[i] += avgInc;
                    if (i < remainder) {
                        hist[i]++;
                    }
                }

                // Build CDF and equalization LUT
                int[] cdf = new int[256];
                cdf[0] = hist[0];
                for (int i = 1; i < 256; i++) {
                    cdf[i] = cdf[i - 1] + hist[i];
                }

                int cdfMin = 0;
                for (int i = 0; i < 256; i++) {
                    if (cdf[i] != 0) {
                        cdfMin = cdf[i];
                        break;
                    }
                }

                int tileIdx = ty * tileGridSize + tx;
                int cdfTotal = cdf[255];
                int denom = cdfTotal - cdfMin;
                if (denom > 0) {
                    for (int i = 0; i < 256; i++) {
                        if (cdf[i] == 0) {
                            tileLuts[tileIdx][i] = 0;
                        } else {
                            tileLuts[tileIdx][i] = PixelOp.clamp(
                                    (int) Math.round((double) (cdf[i] - cdfMin) / denom * 255.0));
                        }
                    }
                } else {
                    // denom == 0: all pixels have the same value — use identity mapping
                    for (int i = 0; i < 256; i++) {
                        tileLuts[tileIdx][i] = i;
                    }
                }
            }
        }

        // Bilinear interpolation between tile LUTs
        int[] result = new int[gray.length];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = gray[y * w + x];

                // Determine which tile center this pixel is relative to
                double txf = ((double) x / tileW) - 0.5;
                double tyf = ((double) y / tileH) - 0.5;

                int tx0 = Math.max(0, (int) Math.floor(txf));
                int ty0 = Math.max(0, (int) Math.floor(tyf));
                int tx1 = Math.min(tileGridSize - 1, tx0 + 1);
                int ty1 = Math.min(tileGridSize - 1, ty0 + 1);

                double fx = txf - tx0;
                double fy = tyf - ty0;
                fx = Math.clamp(fx, 0.0, 1.0);
                fy = Math.clamp(fy, 0.0, 1.0);

                // Bilinear interpolation of equalized values
                double v00 = tileLuts[ty0 * tileGridSize + tx0][v];
                double v10 = tileLuts[ty0 * tileGridSize + tx1][v];
                double v01 = tileLuts[ty1 * tileGridSize + tx0][v];
                double v11 = tileLuts[ty1 * tileGridSize + tx1][v];

                double top = v00 * (1.0 - fx) + v10 * fx;
                double bottom = v01 * (1.0 - fx) + v11 * fx;
                double interpolated = top * (1.0 - fy) + bottom * fy;

                result[y * w + x] = PixelOp.clamp((int) Math.round(interpolated));
            }
        }

        // Convert back to ARGB image
        BufferedImage output = PixelOp.createArgb(w, h);
        int[] dst = PixelOp.getPixels(output);
        int[] argbResult = ChannelOp.grayToArgb(result);
        System.arraycopy(argbResult, 0, dst, 0, argbResult.length);
        return output;
    }
}
