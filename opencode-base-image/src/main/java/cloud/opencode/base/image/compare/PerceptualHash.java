package cloud.opencode.base.image.compare;

import cloud.opencode.base.image.exception.ImageOperationException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Perceptual Hash — image fingerprinting via aHash, dHash, pHash
 * 感知哈希 — 通过 aHash、dHash、pHash 实现图像指纹
 *
 * <p>Provides three perceptual hashing algorithms that produce compact 64-bit fingerprints
 * for images. Similar images yield similar hashes, enabling fast near-duplicate detection.</p>
 * <p>提供三种感知哈希算法，为图像生成紧凑的 64 位指纹。相似图像产生相似的哈希值，
 * 可用于快速近似重复检测。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>aHash — average hash based on 8x8 grayscale mean - 基于 8x8 灰度均值的平均哈希</li>
 *   <li>dHash — difference hash based on adjacent column gradients - 基于相邻列梯度的差异哈希</li>
 *   <li>pHash — perceptual hash based on DCT frequency domain - 基于 DCT 频域的感知哈希</li>
 *   <li>Hamming distance and similarity comparison - 汉明距离与相似度比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * long h1 = PerceptualHash.pHash(image1);
 * long h2 = PerceptualHash.pHash(image2);
 * int dist = PerceptualHash.hammingDistance(h1, h2);
 * double sim = PerceptualHash.similarity(h1, h2);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>aHash/dHash: O(1) — fixed 8x8/9x8 downscale - 固定缩放尺寸</li>
 *   <li>pHash: O(1) — fixed 32x32 DCT, only top-left 8x8 coefficients - 固定 32x32 DCT</li>
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
public final class PerceptualHash {

    private PerceptualHash() {
    }

    /**
     * Compute average hash (aHash) for an image.
     * 计算图像的平均哈希 (aHash)。
     *
     * <p>Scales to 8x8 grayscale, computes the mean pixel value, and produces a 64-bit hash
     * where each bit indicates whether the pixel is above the mean.</p>
     * <p>缩放至 8x8 灰度图，计算像素均值，产生 64 位哈希，每位表示像素是否高于均值。</p>
     *
     * @param image the source image | 源图像
     * @return 64-bit average hash | 64 位平均哈希值
     * @throws NullPointerException if image is null | 图像为 null 时抛出
     * @throws ImageOperationException if image has zero dimensions | 图像尺寸为零时抛出
     */
    public static long aHash(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");
        validateDimensions(image);

        double[][] gray = toGrayscale(image, 8, 8);
        double sum = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                sum += gray[y][x];
            }
        }
        double mean = sum / 64.0;

        long hash = 0L;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                hash <<= 1;
                if (gray[y][x] >= mean) {
                    hash |= 1L;
                }
            }
        }
        return hash;
    }

    /**
     * Compute difference hash (dHash) for an image.
     * 计算图像的差异哈希 (dHash)。
     *
     * <p>Scales to 9x8 grayscale and compares adjacent columns, producing a 64-bit hash
     * where each bit indicates whether the left pixel is brighter than the right.</p>
     * <p>缩放至 9x8 灰度图，比较相邻列像素，产生 64 位哈希，每位表示左像素是否比右像素亮。</p>
     *
     * @param image the source image | 源图像
     * @return 64-bit difference hash | 64 位差异哈希值
     * @throws NullPointerException if image is null | 图像为 null 时抛出
     * @throws ImageOperationException if image has zero dimensions | 图像尺寸为零时抛出
     */
    public static long dHash(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");
        validateDimensions(image);

        double[][] gray = toGrayscale(image, 9, 8);
        long hash = 0L;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                hash <<= 1;
                if (gray[y][x] > gray[y][x + 1]) {
                    hash |= 1L;
                }
            }
        }
        return hash;
    }

    /**
     * Compute perceptual hash (pHash) for an image.
     * 计算图像的感知哈希 (pHash)。
     *
     * <p>Scales to 32x32 grayscale, applies Type-II DCT, takes the top-left 8x8 coefficients
     * (excluding DC), and produces a 64-bit hash based on the median coefficient value.</p>
     * <p>缩放至 32x32 灰度图，应用 Type-II DCT，取左上 8x8 系数（排除 DC），
     * 根据中值系数产生 64 位哈希。</p>
     *
     * @param image the source image | 源图像
     * @return 64-bit perceptual hash | 64 位感知哈希值
     * @throws NullPointerException if image is null | 图像为 null 时抛出
     * @throws ImageOperationException if image has zero dimensions | 图像尺寸为零时抛出
     */
    public static long pHash(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");
        validateDimensions(image);

        int n = 32;
        double[][] gray = toGrayscale(image, n, n);

        // Precompute cosine table: cos[k][i] = cos(PI * (2*i + 1) * k / (2*n))
        double[][] cosTable = new double[8][n];
        for (int k = 0; k < 8; k++) {
            for (int i = 0; i < n; i++) {
                cosTable[k][i] = Math.cos(Math.PI * (2 * i + 1) * k / (2.0 * n));
            }
        }

        double cu0 = 1.0 / Math.sqrt(n);
        double cuK = Math.sqrt(2.0 / n);

        // Compute DCT top-left 8x8 coefficients using precomputed cosines
        double[][] dct = new double[8][8];
        for (int u = 0; u < 8; u++) {
            double cu = (u == 0) ? cu0 : cuK;
            for (int v = 0; v < 8; v++) {
                double cv = (v == 0) ? cu0 : cuK;
                double sum = 0.0;
                for (int y = 0; y < n; y++) {
                    double cosY = cosTable[u][y];
                    for (int x = 0; x < n; x++) {
                        sum += gray[y][x] * cosY * cosTable[v][x];
                    }
                }
                dct[u][v] = cu * cv * sum;
            }
        }

        // Collect 8x8 coefficients excluding DC (0,0) for median computation
        double[] coefficients = new double[63];
        int idx = 0;
        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
                if (u == 0 && v == 0) {
                    continue;
                }
                coefficients[idx++] = dct[u][v];
            }
        }

        double median = findMedian(coefficients);

        // Generate hash: bit 1 if coefficient >= median
        long hash = 0L;
        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
                hash <<= 1;
                if (u == 0 && v == 0) {
                    // DC coefficient: compare against median as well
                    if (dct[u][v] >= median) {
                        hash |= 1L;
                    }
                } else {
                    if (dct[u][v] >= median) {
                        hash |= 1L;
                    }
                }
            }
        }
        return hash;
    }

    /**
     * Compute the Hamming distance between two hashes.
     * 计算两个哈希值之间的汉明距离。
     *
     * @param hash1 first hash | 第一个哈希值
     * @param hash2 second hash | 第二个哈希值
     * @return number of differing bits (0–64) | 不同位的数量 (0–64)
     */
    public static int hammingDistance(long hash1, long hash2) {
        return Long.bitCount(hash1 ^ hash2);
    }

    /**
     * Compute the similarity between two hashes as a value between 0.0 and 1.0.
     * 计算两个哈希值之间的相似度，值域 0.0 到 1.0。
     *
     * @param hash1 first hash | 第一个哈希值
     * @param hash2 second hash | 第二个哈希值
     * @return similarity in range [0.0, 1.0] | 相似度，范围 [0.0, 1.0]
     */
    public static double similarity(long hash1, long hash2) {
        return 1.0 - hammingDistance(hash1, hash2) / 64.0;
    }

    /**
     * Scale image and convert to grayscale pixel matrix.
     * 缩放图像并转换为灰度像素矩阵。
     */
    private static double[][] toGrayscale(BufferedImage image, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }

        double[][] gray = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = scaled.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int gVal = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                gray[y][x] = 0.299 * r + 0.587 * gVal + 0.114 * b;
            }
        }
        return gray;
    }

    /**
     * Find the median value in an array (modifies the array order).
     * 查找数组中的中值（会修改数组顺序）。
     */
    private static double findMedian(double[] values) {
        java.util.Arrays.sort(values);
        int len = values.length;
        if (len % 2 == 0) {
            return (values[len / 2 - 1] + values[len / 2]) / 2.0;
        } else {
            return values[len / 2];
        }
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
