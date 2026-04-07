package cloud.opencode.base.image.compare;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Image Compare — pixel-level diff, SSIM, MSE, and PSNR metrics
 * 图像比较 — 像素级差异、SSIM、MSE 和 PSNR 指标
 *
 * <p>Provides structural and statistical comparison between two images, useful for
 * quality assessment, regression testing, and visual diff generation.</p>
 * <p>提供两幅图像之间的结构和统计比较，适用于质量评估、回归测试和视觉差异生成。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pixel-level diff with highlighted difference image - 像素级差异及高亮差异图</li>
 *   <li>SSIM (Structural Similarity Index) with 11x11 window - SSIM 结构相似性指数</li>
 *   <li>MSE (Mean Squared Error) - MSE 均方误差</li>
 *   <li>PSNR (Peak Signal-to-Noise Ratio) - PSNR 峰值信噪比</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DiffResult result = ImageCompare.diff(imageA, imageB);
 * double similarity = ImageCompare.ssim(imageA, imageB);
 * double error = ImageCompare.mse(imageA, imageB);
 * double psnrDb = ImageCompare.psnr(imageA, imageB);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>diff/mse/psnr: O(W*H) per-pixel scan - 逐像素扫描</li>
 *   <li>SSIM: O(W*H*K^2) with K=11 window — consider downscale for large images - 滑动窗口</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class ImageCompare {

    /** Channel difference threshold for counting a pixel as "different" in diff(). */
    private static final int DIFF_THRESHOLD = 10;

    /** SSIM window size. */
    private static final int SSIM_WINDOW = 11;

    /** SSIM constant C1 = (0.01 * 255)^2. */
    private static final double C1 = Math.pow(0.01 * 255, 2);

    /** SSIM constant C2 = (0.03 * 255)^2. */
    private static final double C2 = Math.pow(0.03 * 255, 2);

    private ImageCompare() {
    }

    /**
     * Result of a pixel-level image diff.
     * 像素级图像差异的结果。
     *
     * @param diffImage the difference image highlighting changed pixels | 高亮变化像素的差异图
     * @param diffPercent percentage of pixels that differ (0.0–1.0) | 不同像素的百分比 (0.0–1.0)
     */
    public record DiffResult(BufferedImage diffImage, double diffPercent) {
    }

    /**
     * Compute pixel-level diff between two images.
     * 计算两幅图像之间的像素级差异。
     *
     * <p>If b has different dimensions, it is resized to match a. The diff image highlights
     * pixels where any channel difference exceeds 10. diffPercent is the ratio of such pixels.</p>
     * <p>如果 b 的尺寸不同，会缩放至与 a 相同。差异图高亮任一通道差值超过 10 的像素。
     * diffPercent 为此类像素的比例。</p>
     *
     * @param a first image (reference) | 第一幅图像（参考）
     * @param b second image (comparison) | 第二幅图像（比较）
     * @return diff result containing difference image and percentage | 包含差异图和百分比的结果
     * @throws NullPointerException if either image is null | 任一图像为 null 时抛出
     * @throws ImageOperationException if either image has zero dimensions | 图像尺寸为零时抛出
     */
    public static DiffResult diff(BufferedImage a, BufferedImage b) {
        Objects.requireNonNull(a, "image a must not be null");
        Objects.requireNonNull(b, "image b must not be null");
        validateDimensions(a);
        validateDimensions(b);

        int width = a.getWidth();
        int height = a.getHeight();
        BufferedImage resizedB = resizeTo(b, width, height);

        BufferedImage srcA = PixelOp.ensureArgb(a);
        BufferedImage srcB = PixelOp.ensureArgb(resizedB);
        int[] pixelsA = PixelOp.getPixels(srcA);
        int[] pixelsB = PixelOp.getPixels(srcB);

        BufferedImage diffImage = PixelOp.createArgb(width, height);
        int[] diffPixels = PixelOp.getPixels(diffImage);
        int diffCount = 0;

        for (int i = 0; i < pixelsA.length; i++) {
            int rgbA = pixelsA[i];
            int rgbB = pixelsB[i];

            int rDiff = Math.abs(((rgbA >> 16) & 0xFF) - ((rgbB >> 16) & 0xFF));
            int gDiff = Math.abs(((rgbA >> 8) & 0xFF) - ((rgbB >> 8) & 0xFF));
            int bDiff = Math.abs((rgbA & 0xFF) - (rgbB & 0xFF));

            boolean isDifferent = rDiff > DIFF_THRESHOLD || gDiff > DIFF_THRESHOLD || bDiff > DIFF_THRESHOLD;

            if (isDifferent) {
                diffCount++;
                diffPixels[i] = 0xFFFF0000; // ARGB red
            } else {
                int gray = (int) (0.299 * ((rgbA >> 16) & 0xFF) + 0.587 * ((rgbA >> 8) & 0xFF)
                        + 0.114 * (rgbA & 0xFF));
                int dimmed = gray / 3;
                diffPixels[i] = 0xFF000000 | (dimmed << 16) | (dimmed << 8) | dimmed;
            }
        }

        double diffPercent = (double) diffCount / ((long) width * height);
        return new DiffResult(diffImage, diffPercent);
    }

    /**
     * Compute Structural Similarity Index (SSIM) between two images.
     * 计算两幅图像之间的结构相似性指数 (SSIM)。
     *
     * <p>Uses an 11x11 sliding window with box averaging. Both images are converted to
     * grayscale first. If b has different dimensions, it is resized to match a.</p>
     * <p>使用 11x11 滑动窗口的简单均值。两幅图像首先转换为灰度图。
     * 如果 b 的尺寸不同，会缩放至与 a 相同。</p>
     *
     * @param a first image (reference) | 第一幅图像（参考）
     * @param b second image (comparison) | 第二幅图像（比较）
     * @return SSIM value in range [0.0, 1.0] | SSIM 值，范围 [0.0, 1.0]
     * @throws NullPointerException if either image is null | 任一图像为 null 时抛出
     * @throws ImageOperationException if either image has zero dimensions | 图像尺寸为零时抛出
     */
    public static double ssim(BufferedImage a, BufferedImage b) {
        Objects.requireNonNull(a, "image a must not be null");
        Objects.requireNonNull(b, "image b must not be null");
        validateDimensions(a);
        validateDimensions(b);

        int width = a.getWidth();
        int height = a.getHeight();
        BufferedImage resizedB = resizeTo(b, width, height);

        double[][] grayA = toGrayscale(a);
        double[][] grayB = toGrayscale(resizedB);

        int halfWin = SSIM_WINDOW / 2;
        double ssimSum = 0.0;
        int count = 0;

        for (int y = halfWin; y < height - halfWin; y++) {
            for (int x = halfWin; x < width - halfWin; x++) {
                double muA = 0, muB = 0;
                int winPixels = 0;

                for (int wy = -halfWin; wy <= halfWin; wy++) {
                    for (int wx = -halfWin; wx <= halfWin; wx++) {
                        muA += grayA[y + wy][x + wx];
                        muB += grayB[y + wy][x + wx];
                        winPixels++;
                    }
                }
                muA /= winPixels;
                muB /= winPixels;

                double sigmaA2 = 0, sigmaB2 = 0, sigmaAB = 0;
                for (int wy = -halfWin; wy <= halfWin; wy++) {
                    for (int wx = -halfWin; wx <= halfWin; wx++) {
                        double da = grayA[y + wy][x + wx] - muA;
                        double db = grayB[y + wy][x + wx] - muB;
                        sigmaA2 += da * da;
                        sigmaB2 += db * db;
                        sigmaAB += da * db;
                    }
                }
                sigmaA2 /= winPixels;
                sigmaB2 /= winPixels;
                sigmaAB /= winPixels;

                double numerator = (2 * muA * muB + C1) * (2 * sigmaAB + C2);
                double denominator = (muA * muA + muB * muB + C1) * (sigmaA2 + sigmaB2 + C2);
                ssimSum += numerator / denominator;
                count++;
            }
        }

        if (count == 0) {
            // Image too small for window — fall back to pixel equality check
            return pixelEqual(grayA, grayB, width, height) ? 1.0 : 0.0;
        }

        return ssimSum / count;
    }

    /**
     * Compute Mean Squared Error (MSE) between two images.
     * 计算两幅图像之间的均方误差 (MSE)。
     *
     * <p>Computes MSE across all RGB channels. If b has different dimensions,
     * it is resized to match a.</p>
     * <p>在所有 RGB 通道上计算 MSE。如果 b 的尺寸不同，会缩放至与 a 相同。</p>
     *
     * @param a first image (reference) | 第一幅图像（参考）
     * @param b second image (comparison) | 第二幅图像（比较）
     * @return MSE value (0.0 for identical images) | MSE 值（相同图像为 0.0）
     * @throws NullPointerException if either image is null | 任一图像为 null 时抛出
     * @throws ImageOperationException if either image has zero dimensions | 图像尺寸为零时抛出
     */
    public static double mse(BufferedImage a, BufferedImage b) {
        Objects.requireNonNull(a, "image a must not be null");
        Objects.requireNonNull(b, "image b must not be null");
        validateDimensions(a);
        validateDimensions(b);

        int width = a.getWidth();
        int height = a.getHeight();
        BufferedImage resizedB = resizeTo(b, width, height);

        BufferedImage srcA = PixelOp.ensureArgb(a);
        BufferedImage srcB = PixelOp.ensureArgb(resizedB);
        int[] pixelsA = PixelOp.getPixels(srcA);
        int[] pixelsB = PixelOp.getPixels(srcB);

        double sum = 0.0;
        long totalSamples = (long) width * height * 3; // 3 channels

        for (int i = 0; i < pixelsA.length; i++) {
            int rgbA = pixelsA[i];
            int rgbB = pixelsB[i];

            double dr = ((rgbA >> 16) & 0xFF) - ((rgbB >> 16) & 0xFF);
            double dg = ((rgbA >> 8) & 0xFF) - ((rgbB >> 8) & 0xFF);
            double db = (rgbA & 0xFF) - (rgbB & 0xFF);

            sum += dr * dr + dg * dg + db * db;
        }

        return sum / totalSamples;
    }

    /**
     * Compute Peak Signal-to-Noise Ratio (PSNR) between two images.
     * 计算两幅图像之间的峰值信噪比 (PSNR)。
     *
     * <p>PSNR = 10 * log10(255^2 / MSE). Returns {@link Double#POSITIVE_INFINITY}
     * when the images are identical (MSE = 0).</p>
     * <p>PSNR = 10 * log10(255^2 / MSE)。当图像相同时（MSE = 0）返回正无穷。</p>
     *
     * @param a first image (reference) | 第一幅图像（参考）
     * @param b second image (comparison) | 第二幅图像（比较）
     * @return PSNR in dB, or POSITIVE_INFINITY for identical images | PSNR 分贝值，相同图像返回正无穷
     * @throws NullPointerException if either image is null | 任一图像为 null 时抛出
     * @throws ImageOperationException if either image has zero dimensions | 图像尺寸为零时抛出
     */
    public static double psnr(BufferedImage a, BufferedImage b) {
        Objects.requireNonNull(a, "image a must not be null");
        Objects.requireNonNull(b, "image b must not be null");

        double mseVal = mse(a, b);
        if (mseVal == 0.0) {
            return Double.POSITIVE_INFINITY;
        }
        return 10.0 * Math.log10(255.0 * 255.0 / mseVal);
    }

    /**
     * Resize image to target dimensions using bilinear interpolation.
     * 使用双线性插值将图像缩放至目标尺寸。
     */
    private static BufferedImage resizeTo(BufferedImage image, int width, int height) {
        if (image.getWidth() == width && image.getHeight() == height) {
            return image;
        }
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return resized;
    }

    /**
     * Convert image to grayscale pixel matrix.
     * 将图像转换为灰度像素矩阵。
     */
    private static double[][] toGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage src = PixelOp.ensureArgb(image);
        int[] pixels = PixelOp.getPixels(src);

        double[][] gray = new double[height][width];
        for (int y = 0; y < height; y++) {
            int rowOffset = y * width;
            for (int x = 0; x < width; x++) {
                int rgb = pixels[rowOffset + x];
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                gray[y][x] = 0.299 * r + 0.587 * g + 0.114 * b;
            }
        }
        return gray;
    }

    /**
     * Check if two grayscale matrices are equal.
     * 检查两个灰度矩阵是否相等。
     */
    private static boolean pixelEqual(double[][] a, double[][] b, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (Double.compare(a[y][x], b[y][x]) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validate that the image has non-zero dimensions.
     * 校验图像的尺寸不为零。
     */
    private static void validateDimensions(BufferedImage image) {
        if (image.getWidth() <= 0 || image.getHeight() <= 0) {
            throw new ImageOperationException("Image must have positive dimensions");
        }
    }
}
