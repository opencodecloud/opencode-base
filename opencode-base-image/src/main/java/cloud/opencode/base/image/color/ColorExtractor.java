package cloud.opencode.base.image.color;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Color Extraction Utility
 * 颜色提取工具类
 *
 * <p>Extracts dominant colors from images using a simplified K-Means
 * clustering algorithm on downsampled pixel data.</p>
 * <p>使用简化的 K-Means 聚类算法对降采样像素数据提取图像主色调。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extract multiple dominant colors via K-Means - 通过 K-Means 提取多个主色调</li>
 *   <li>Extract single dominant color - 提取单个主色调</li>
 *   <li>64x64 downsampling for performance - 64x64 降采样以提升性能</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extract top 5 dominant colors
 * List<Color> colors = ColorExtractor.dominantColors(image, 5);
 *
 * // Extract the single most dominant color
 * Color primary = ColorExtractor.dominantColor(image);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(k * iter * n) where n = 64*64 samples, k = cluster count, iter = max 20 - 时间: O(k * iter * n)</li>
 *   <li>Space: O(n + k) - 空间: O(n + k)</li>
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
 * @since JDK 25, opencode-base-image V1.0.3
 */
public final class ColorExtractor {

    private static final int SAMPLE_SIZE = 64;
    private static final int MAX_ITERATIONS = 20;
    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 20;

    private ColorExtractor() {
        throw new AssertionError("No ColorExtractor instances");
    }

    /**
     * Extract the dominant colors from an image using K-Means clustering.
     * 使用 K-Means 聚类从图像中提取主色调列表。
     *
     * <p>The image is first downsampled to 64x64 for performance, then K-Means
     * clustering is applied with the specified number of clusters. Results are
     * returned sorted by cluster size (most dominant first).</p>
     * <p>图像首先被降采样到 64x64 以提升性能，然后使用指定数量的聚类进行 K-Means
     * 聚类。结果按聚类大小降序排列返回（最主要的颜色排在前面）。</p>
     *
     * @param image the source image | 源图像
     * @param count the number of dominant colors to extract, must be in [1, 20] | 要提取的主色调数量，必须在 [1, 20] 范围
     * @return the list of dominant colors sorted by prevalence | 按主要程度排序的主色调列表
     * @throws NullPointerException    if image is null | 当图像为 null 时抛出
     * @throws ImageOperationException if count is out of [1, 20] range | 当 count 超出 [1, 20] 范围时抛出
     */
    public static List<Color> dominantColors(BufferedImage image, int count) {
        Objects.requireNonNull(image, "image must not be null");
        if (count < MIN_COUNT || count > MAX_COUNT) {
            throw new ImageOperationException(
                    "count must be in [" + MIN_COUNT + ", " + MAX_COUNT + "], got: " + count,
                    ImageErrorCode.INVALID_PARAMETERS);
        }

        // Step 1: Downsample to 64x64 using bilinear interpolation
        BufferedImage sampled = downsample(image);

        // Step 2: Extract RGB values from all pixels
        BufferedImage argb = PixelOp.ensureArgb(sampled);
        int[] pixels = PixelOp.getPixels(argb);

        int[][] rgbData = new int[pixels.length][3];
        for (int i = 0; i < pixels.length; i++) {
            rgbData[i][0] = PixelOp.red(pixels[i]);
            rgbData[i][1] = PixelOp.green(pixels[i]);
            rgbData[i][2] = PixelOp.blue(pixels[i]);
        }

        // Step 3: K-Means clustering
        return kMeans(rgbData, count);
    }

    /**
     * Extract the single most dominant color from an image.
     * 从图像中提取单个最主要的颜色。
     *
     * <p>Equivalent to calling {@code dominantColors(image, 1).get(0)}.</p>
     * <p>等效于调用 {@code dominantColors(image, 1).get(0)}。</p>
     *
     * @param image the source image | 源图像
     * @return the most dominant color | 最主要的颜色
     * @throws NullPointerException if image is null | 当图像为 null 时抛出
     */
    public static Color dominantColor(BufferedImage image) {
        return dominantColors(image, 1).getFirst();
    }

    /**
     * Downsample an image to SAMPLE_SIZE x SAMPLE_SIZE using bilinear interpolation.
     * 使用双线性插值将图像降采样到 SAMPLE_SIZE x SAMPLE_SIZE。
     *
     * @param image the source image | 源图像
     * @return the downsampled image | 降采样后的图像
     */
    private static BufferedImage downsample(BufferedImage image) {
        BufferedImage sampled = new BufferedImage(SAMPLE_SIZE, SAMPLE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sampled.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE, null);
        } finally {
            g.dispose();
        }
        return sampled;
    }

    /**
     * Perform K-Means clustering on RGB data.
     * 对 RGB 数据执行 K-Means 聚类。
     *
     * @param data  the RGB pixel data (Nx3 array) | RGB 像素数据（Nx3 数组）
     * @param k     the number of clusters | 聚类数量
     * @return the cluster centers sorted by cluster size descending | 按聚类大小降序排列的聚类中心
     */
    private static List<Color> kMeans(int[][] data, int k) {
        int n = data.length;
        // Clamp k to the number of data points
        int effectiveK = Math.min(k, n);

        // Initialize centroids by selecting k random distinct data points
        int[][] centroids = initCentroids(data, effectiveK);

        int[] assignments = new int[n];
        Arrays.fill(assignments, -1);

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            boolean changed = false;

            // Assign each point to nearest centroid
            for (int i = 0; i < n; i++) {
                int nearest = findNearest(data[i], centroids);
                if (nearest != assignments[i]) {
                    assignments[i] = nearest;
                    changed = true;
                }
            }

            if (!changed) {
                break;
            }

            // Recompute centroids
            long[][] sums = new long[effectiveK][3];
            int[] counts = new int[effectiveK];

            for (int i = 0; i < n; i++) {
                int c = assignments[i];
                sums[c][0] += data[i][0];
                sums[c][1] += data[i][1];
                sums[c][2] += data[i][2];
                counts[c]++;
            }

            for (int c = 0; c < effectiveK; c++) {
                if (counts[c] > 0) {
                    centroids[c][0] = (int) (sums[c][0] / counts[c]);
                    centroids[c][1] = (int) (sums[c][1] / counts[c]);
                    centroids[c][2] = (int) (sums[c][2] / counts[c]);
                }
            }
        }

        // Count final cluster sizes
        int[] counts = new int[effectiveK];
        for (int a : assignments) {
            counts[a]++;
        }

        // Build result list sorted by cluster size descending
        Integer[] indices = new Integer[effectiveK];
        for (int i = 0; i < effectiveK; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, Comparator.comparingInt((Integer i) -> counts[i]).reversed());

        List<Color> result = new ArrayList<>(effectiveK);
        for (int idx : indices) {
            result.add(new Color(
                    PixelOp.clamp(centroids[idx][0]),
                    PixelOp.clamp(centroids[idx][1]),
                    PixelOp.clamp(centroids[idx][2])));
        }
        return result;
    }

    /**
     * Initialize centroids by selecting k random distinct indices from data.
     * 通过从数据中选择 k 个随机不同的索引来初始化质心。
     *
     * @param data the pixel data | 像素数据
     * @param k    the number of centroids | 质心数量
     * @return the initial centroids | 初始质心
     */
    private static int[][] initCentroids(int[][] data, int k) {
        int[][] centroids = new int[k][3];
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        // Fisher-Yates partial shuffle to select k distinct indices
        int n = data.length;
        int[] indices = new int[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }
        for (int i = 0; i < k; i++) {
            int j = rng.nextInt(i, n);
            int tmp = indices[i];
            indices[i] = indices[j];
            indices[j] = tmp;
            centroids[i][0] = data[indices[i]][0];
            centroids[i][1] = data[indices[i]][1];
            centroids[i][2] = data[indices[i]][2];
        }
        return centroids;
    }

    /**
     * Find the nearest centroid to the given point using squared Euclidean distance.
     * 使用欧氏距离平方找到距给定点最近的质心。
     *
     * @param point     the RGB point | RGB 点
     * @param centroids the centroids array | 质心数组
     * @return the index of the nearest centroid | 最近质心的索引
     */
    private static int findNearest(int[] point, int[][] centroids) {
        int nearest = 0;
        long minDist = Long.MAX_VALUE;
        for (int c = 0; c < centroids.length; c++) {
            long dr = point[0] - centroids[c][0];
            long dg = point[1] - centroids[c][1];
            long db = point[2] - centroids[c][2];
            long dist = dr * dr + dg * dg + db * db;
            if (dist < minDist) {
                minDist = dist;
                nearest = c;
            }
        }
        return nearest;
    }
}
