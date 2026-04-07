package cloud.opencode.base.image.analysis;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Connected Components Analysis using Two-Pass Union-Find Algorithm
 * 基于两遍扫描 Union-Find 算法的连通域分析
 *
 * <p>Analyzes connected components in binary images where foreground pixels (non-zero
 * grayscale value) are grouped into labeled regions. Supports both 4-connectivity
 * and 8-connectivity neighbor definitions.</p>
 * <p>分析二值图像中的连通域，前景像素（非零灰度值）被分组为标记区域。
 * 支持 4-连通和 8-连通两种邻域定义。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Two-Pass labeling with Union-Find (path compression + union by rank) - 两遍标记 + Union-Find（路径压缩 + 按秩合并）</li>
 *   <li>4-connectivity and 8-connectivity support - 支持 4-连通和 8-连通</li>
 *   <li>Per-component statistics: area, centroid, bounding box - 每个连通域统计: 面积、质心、边界框</li>
 *   <li>Label matrix output for downstream processing - 标签矩阵输出供下游处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BufferedImage binary = ...; // binary image with white foreground
 * ConnectedComponentsOp.Result result = ConnectedComponentsOp.analyze(binary);
 * System.out.println("Components: " + result.componentCount());
 * for (ConnectedComponentsOp.Component c : result.components()) {
 *     System.out.printf("Label %d: area=%d, centroid=(%d,%d)%n",
 *         c.label(), c.area(), c.centroidX(), c.centroidY());
 * }
 *
 * // Use 4-connectivity for stricter neighbor definition
 * ConnectedComponentsOp.Result result4 = ConnectedComponentsOp.analyze(binary,
 *     ConnectedComponentsOp.Connectivity.FOUR);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Two-Pass + Union-Find: O(n * alpha(n)) approximately O(n), where n = width * height -
 *       两遍扫描 + Union-Find: O(n * alpha(n)) 约等于 O(n)，n = 宽 * 高</li>
 *   <li>Space complexity: O(n) for label matrix and Union-Find structure -
 *       空间复杂度: O(n) 用于标签矩阵和 Union-Find 结构</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, all methods are pure functions) - 线程安全: 是（无状态，所有方法为纯函数）</li>
 *   <li>Null-safe: No (null image throws NullPointerException) - 空值安全: 否（null 图像抛出 NullPointerException）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class ConnectedComponentsOp {

    private ConnectedComponentsOp() {
        throw new AssertionError("No ConnectedComponentsOp instances");
    }

    /**
     * Connectivity type for neighbor pixel definition.
     * 邻域像素连接类型定义。
     */
    public enum Connectivity {
        /**
         * 4-connectivity: up, down, left, right neighbors only.
         * 4-连通: 仅上、下、左、右邻域。
         */
        FOUR,
        /**
         * 8-connectivity: includes diagonal neighbors.
         * 8-连通: 包含对角线邻域。
         */
        EIGHT
    }

    /**
     * A single connected component with its statistics.
     * 单个连通域及其统计信息。
     *
     * @param label        the unique label identifier | 唯一标签标识
     * @param area         the number of pixels in this component | 该连通域的像素数量
     * @param centroidX    the x coordinate of the centroid (mean x) | 质心 x 坐标（x 均值）
     * @param centroidY    the y coordinate of the centroid (mean y) | 质心 y 坐标（y 均值）
     * @param boundsX      the x coordinate of the bounding box top-left | 边界框左上角 x 坐标
     * @param boundsY      the y coordinate of the bounding box top-left | 边界框左上角 y 坐标
     * @param boundsWidth  the width of the bounding box | 边界框宽度
     * @param boundsHeight the height of the bounding box | 边界框高度
     */
    public record Component(int label, int area, int centroidX, int centroidY,
                            int boundsX, int boundsY, int boundsWidth, int boundsHeight) {
    }

    /**
     * The result of connected component analysis.
     * 连通域分析结果。
     *
     * @param labels     2D label matrix where labels[y][x] is the component label (0 = background) |
     *                   二维标签矩阵，labels[y][x] 为连通域标签（0 = 背景）
     * @param components the list of detected components sorted by label | 按标签排序的检测到的连通域列表
     */
    public record Result(int[][] labels, List<Component> components) {

        /**
         * Create a result with defensive copy of the component list.
         * 创建结果，对连通域列表进行防御性拷贝。
         *
         * @param labels     the label matrix | 标签矩阵
         * @param components the component list | 连通域列表
         */
        public Result {
            Objects.requireNonNull(labels, "labels must not be null");
            Objects.requireNonNull(components, "components must not be null");
            components = List.copyOf(components);
        }

        /**
         * Get the number of connected components found.
         * 获取找到的连通域数量。
         *
         * @return the component count | 连通域数量
         */
        public int componentCount() {
            return components.size();
        }
    }

    /**
     * Analyze connected components in a binary image using 8-connectivity.
     * 使用 8-连通分析二值图像中的连通域。
     *
     * <p>Foreground pixels are those with non-zero grayscale value after
     * converting the image to grayscale using ITU-R BT.601 coefficients.</p>
     * <p>将图像转换为灰度（使用 ITU-R BT.601 系数）后，非零灰度值的像素为前景像素。</p>
     *
     * @param image the input image | 输入图像
     * @return the analysis result containing labels and component statistics | 包含标签和连通域统计的分析结果
     * @throws NullPointerException    if image is null | 当图像为 null 时抛出
     * @throws ImageOperationException if the image has invalid dimensions | 当图像尺寸无效时抛出
     */
    public static Result analyze(BufferedImage image) {
        return analyze(image, Connectivity.EIGHT);
    }

    /**
     * Analyze connected components in a binary image with specified connectivity.
     * 使用指定连通性分析二值图像中的连通域。
     *
     * <p>The algorithm works in two passes:</p>
     * <ol>
     *   <li>First pass: scan top-left to bottom-right, assign provisional labels,
     *       record equivalences in a Union-Find structure.</li>
     *   <li>Second pass: replace all labels with their canonical root from Union-Find,
     *       then compute per-component statistics (area, centroid, bounding box).</li>
     * </ol>
     * <p>算法分两遍执行:</p>
     * <ol>
     *   <li>第一遍: 从左上到右下扫描，分配临时标签，在 Union-Find 结构中记录等价关系。</li>
     *   <li>第二遍: 将所有标签替换为 Union-Find 中的规范根标签，然后计算每个连通域的统计信息。</li>
     * </ol>
     *
     * @param image        the input image | 输入图像
     * @param connectivity the connectivity type (FOUR or EIGHT) | 连通性类型（FOUR 或 EIGHT）
     * @return the analysis result containing labels and component statistics | 包含标签和连通域统计的分析结果
     * @throws NullPointerException    if image or connectivity is null | 当图像或连通性为 null 时抛出
     * @throws ImageOperationException if the image has invalid dimensions | 当图像尺寸无效时抛出
     */
    public static Result analyze(BufferedImage image, Connectivity connectivity) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(connectivity, "connectivity must not be null");

        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= 0 || height <= 0) {
            throw new ImageOperationException(
                    "Image dimensions must be positive, got: " + width + "x" + height,
                    ImageErrorCode.INVALID_DIMENSIONS);
        }

        // Convert to grayscale binary: non-zero = foreground
        boolean[][] foreground = buildForegroundMask(image, width, height);

        // First pass: assign provisional labels with Union-Find
        int[][] labels = new int[height][width];
        UnionFind uf = firstPass(foreground, labels, width, height, connectivity);

        // Second pass: resolve labels to canonical roots
        secondPass(labels, uf, width, height);

        // Compute component statistics
        List<Component> components = computeStatistics(labels, width, height);

        return new Result(labels, components);
    }

    /**
     * Build a foreground mask from the image (non-zero grayscale = foreground).
     */
    private static boolean[][] buildForegroundMask(BufferedImage image, int width, int height) {
        BufferedImage argb = PixelOp.ensureArgb(image);
        int[] pixels = PixelOp.getPixels(argb);
        int[] gray = ChannelOp.toGray(pixels);

        boolean[][] foreground = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                foreground[y][x] = gray[offset + x] != 0;
            }
        }
        return foreground;
    }

    /**
     * First pass: scan pixels and assign provisional labels using Union-Find.
     */
    private static UnionFind firstPass(boolean[][] foreground, int[][] labels,
                                       int width, int height, Connectivity connectivity) {
        UnionFind uf = new UnionFind(width * height / 2 + 1);
        int nextLabel = 1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!foreground[y][x]) {
                    continue;
                }

                int minLabel = Integer.MAX_VALUE;

                // Check neighbors based on connectivity
                // For 4-conn: left, above
                // For 8-conn: left, above, upper-left, upper-right
                int neighborLabel;

                // Left neighbor
                if (x > 0 && (neighborLabel = labels[y][x - 1]) > 0) {
                    minLabel = Math.min(minLabel, uf.find(neighborLabel));
                }
                // Above neighbor
                if (y > 0 && (neighborLabel = labels[y - 1][x]) > 0) {
                    int root = uf.find(neighborLabel);
                    if (minLabel != Integer.MAX_VALUE && minLabel != root) {
                        uf.union(minLabel, root);
                        minLabel = uf.find(minLabel);
                    } else {
                        minLabel = Math.min(minLabel, root);
                    }
                }

                if (connectivity == Connectivity.EIGHT) {
                    // Upper-left diagonal
                    if (x > 0 && y > 0 && (neighborLabel = labels[y - 1][x - 1]) > 0) {
                        int root = uf.find(neighborLabel);
                        if (minLabel != Integer.MAX_VALUE && minLabel != root) {
                            uf.union(minLabel, root);
                            minLabel = uf.find(minLabel);
                        } else {
                            minLabel = Math.min(minLabel, root);
                        }
                    }
                    // Upper-right diagonal
                    if (x < width - 1 && y > 0 && (neighborLabel = labels[y - 1][x + 1]) > 0) {
                        int root = uf.find(neighborLabel);
                        if (minLabel != Integer.MAX_VALUE && minLabel != root) {
                            uf.union(minLabel, root);
                            minLabel = uf.find(minLabel);
                        } else {
                            minLabel = Math.min(minLabel, root);
                        }
                    }
                }

                if (minLabel == Integer.MAX_VALUE) {
                    // No labeled neighbor: assign new label
                    uf.ensureCapacity(nextLabel);
                    labels[y][x] = nextLabel;
                    nextLabel++;
                } else {
                    labels[y][x] = minLabel;
                }
            }
        }
        return uf;
    }

    /**
     * Second pass: replace all provisional labels with canonical Union-Find roots.
     */
    private static void secondPass(int[][] labels, UnionFind uf, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (labels[y][x] > 0) {
                    labels[y][x] = uf.find(labels[y][x]);
                }
            }
        }
    }

    /**
     * Compute per-component statistics from the resolved label matrix.
     */
    private static List<Component> computeStatistics(int[][] labels, int width, int height) {
        // Collect per-label accumulators: sumX, sumY, count, minX, minY, maxX, maxY
        // Use a simple approach with dynamic arrays since label count is unknown
        int maxLabel = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (labels[y][x] > maxLabel) {
                    maxLabel = labels[y][x];
                }
            }
        }

        if (maxLabel == 0) {
            return Collections.emptyList();
        }

        // Accumulators indexed by label (1-based)
        long[] sumX = new long[maxLabel + 1];
        long[] sumY = new long[maxLabel + 1];
        int[] count = new int[maxLabel + 1];
        int[] minX = new int[maxLabel + 1];
        int[] minY = new int[maxLabel + 1];
        int[] maxX = new int[maxLabel + 1];
        int[] maxY = new int[maxLabel + 1];

        // Initialize bounds
        for (int i = 1; i <= maxLabel; i++) {
            minX[i] = Integer.MAX_VALUE;
            minY[i] = Integer.MAX_VALUE;
            maxX[i] = Integer.MIN_VALUE;
            maxY[i] = Integer.MIN_VALUE;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int label = labels[y][x];
                if (label > 0) {
                    count[label]++;
                    sumX[label] += x;
                    sumY[label] += y;
                    if (x < minX[label]) {
                        minX[label] = x;
                    }
                    if (x > maxX[label]) {
                        maxX[label] = x;
                    }
                    if (y < minY[label]) {
                        minY[label] = y;
                    }
                    if (y > maxY[label]) {
                        maxY[label] = y;
                    }
                }
            }
        }

        List<Component> components = new ArrayList<>();
        for (int label = 1; label <= maxLabel; label++) {
            if (count[label] > 0) {
                int centroidXVal = (int) Math.round((double) sumX[label] / count[label]);
                int centroidYVal = (int) Math.round((double) sumY[label] / count[label]);
                int bw = maxX[label] - minX[label] + 1;
                int bh = maxY[label] - minY[label] + 1;
                components.add(new Component(label, count[label],
                        centroidXVal, centroidYVal,
                        minX[label], minY[label], bw, bh));
            }
        }

        return components;
    }

    /**
     * Union-Find (Disjoint Set Union) with path compression and union by rank.
     * 带路径压缩和按秩合并的并查集。
     */
    private static final class UnionFind {

        private int[] parent;
        private int[] rank;

        UnionFind(int initialCapacity) {
            int cap = Math.max(initialCapacity, 16);
            parent = new int[cap];
            rank = new int[cap];
            // Each element is its own parent initially
            for (int i = 0; i < cap; i++) {
                parent[i] = i;
            }
        }

        /**
         * Ensure the internal arrays can hold the given label.
         */
        void ensureCapacity(int label) {
            if (label >= parent.length) {
                int newCap = Math.max(parent.length * 2, label + 1);
                int[] newParent = new int[newCap];
                int[] newRank = new int[newCap];
                System.arraycopy(parent, 0, newParent, 0, parent.length);
                System.arraycopy(rank, 0, newRank, 0, rank.length);
                // Initialize new elements as their own parent
                for (int i = parent.length; i < newCap; i++) {
                    newParent[i] = i;
                }
                parent = newParent;
                rank = newRank;
            }
        }

        /**
         * Find the root of the set containing element x with path compression.
         */
        int find(int x) {
            if (x < 0 || x >= parent.length) {
                return x;
            }
            while (parent[x] != x) {
                parent[x] = parent[parent[x]]; // path halving
                x = parent[x];
            }
            return x;
        }

        /**
         * Union the sets containing elements a and b by rank.
         */
        void union(int a, int b) {
            int rootA = find(a);
            int rootB = find(b);
            if (rootA == rootB) {
                return;
            }
            if (rank[rootA] < rank[rootB]) {
                parent[rootA] = rootB;
            } else if (rank[rootA] > rank[rootB]) {
                parent[rootB] = rootA;
            } else {
                parent[rootB] = rootA;
                rank[rootA]++;
            }
        }
    }
}
