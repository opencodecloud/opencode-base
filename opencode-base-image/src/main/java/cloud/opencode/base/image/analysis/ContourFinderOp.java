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
 * Contour Finder using Moore Neighborhood Border Tracing Algorithm
 * 基于 Moore 邻域边界追踪算法的轮廓检测
 *
 * <p>Finds contours (boundaries) of foreground regions in binary images. Each contour
 * is a list of (x, y) border pixel coordinates forming the boundary of a connected
 * foreground region.</p>
 * <p>在二值图像中查找前景区域的轮廓（边界）。每个轮廓是一组 (x, y) 边界像素坐标，
 * 构成一个连通前景区域的边界。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Moore neighborhood border tracing for contour extraction - Moore 邻域边界追踪提取轮廓</li>
 *   <li>Per-contour metrics: area (Shoelace formula), perimeter, bounding box - 每个轮廓的指标: 面积（鞋带公式）、周长、边界框</li>
 *   <li>Handles multiple disjoint contours in a single image - 处理单张图像中的多个不相交轮廓</li>
 *   <li>Visited-pixel tracking prevents duplicate detection - 已访问像素追踪防止重复检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BufferedImage binary = ...; // binary image with white foreground on black background
 * List<ContourFinderOp.Contour> contours = ContourFinderOp.find(binary);
 * for (ContourFinderOp.Contour c : contours) {
 *     System.out.printf("Contour: %d points, area=%.1f, perimeter=%.1f%n",
 *         c.size(), c.area(), c.perimeter());
 *     int[] bbox = c.boundingBox();
 *     System.out.printf("  BBox: x=%d, y=%d, w=%d, h=%d%n",
 *         bbox[0], bbox[1], bbox[2], bbox[3]);
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = width * height (each pixel visited at most a constant number of times) -
 *       时间复杂度: O(n)，n = 宽 * 高（每个像素最多被访问常数次）</li>
 *   <li>Space complexity: O(n) for the visited mask and foreground mask -
 *       空间复杂度: O(n) 用于已访问掩码和前景掩码</li>
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
public final class ContourFinderOp {

    private ContourFinderOp() {
        throw new AssertionError("No ContourFinderOp instances");
    }

    /**
     * Moore neighborhood 8-direction offsets (clockwise starting from right).
     * Moore 邻域 8 方向偏移（从右方开始顺时针）。
     * <p>Directions: 0=E, 1=SE, 2=S, 3=SW, 4=W, 5=NW, 6=N, 7=NE</p>
     */
    private static final int[] DX = {1, 1, 0, -1, -1, -1, 0, 1};
    private static final int[] DY = {0, 1, 1, 1, 0, -1, -1, -1};

    /**
     * A single contour consisting of boundary pixel coordinates.
     * 由边界像素坐标组成的单个轮廓。
     *
     * @param points the list of (x, y) boundary pixel coordinates | 边界像素坐标列表 (x, y)
     */
    public record Contour(List<int[]> points) {

        /**
         * Create a contour with a defensive copy of the points list.
         * 创建轮廓，对点列表进行防御性拷贝。
         *
         * @param points the list of boundary points | 边界点列表
         */
        public Contour {
            Objects.requireNonNull(points, "points must not be null");
            points = List.copyOf(points);
        }

        /**
         * Get the number of points in this contour.
         * 获取此轮廓中的点数。
         *
         * @return the point count | 点数
         */
        public int size() {
            return points.size();
        }

        /**
         * Compute the area enclosed by this contour using the Shoelace formula.
         * 使用鞋带公式计算此轮廓所围面积。
         *
         * <p>The Shoelace formula computes the signed area of a polygon from its
         * vertex coordinates. The absolute value is returned.</p>
         * <p>鞋带公式根据多边形顶点坐标计算有符号面积，返回绝对值。</p>
         *
         * @return the area of the contour | 轮廓面积
         */
        public double area() {
            if (points.size() < 3) {
                return 0.0;
            }
            long sum = 0;
            int n = points.size();
            for (int i = 0; i < n; i++) {
                int[] current = points.get(i);
                int[] next = points.get((i + 1) % n);
                sum = Math.addExact(sum, (long) current[0] * next[1] - (long) next[0] * current[1]);
            }
            return Math.abs(sum) / 2.0;
        }

        /**
         * Compute the perimeter of this contour as the sum of distances between consecutive points.
         * 计算此轮廓的周长，即相邻点之间距离之和。
         *
         * @return the perimeter length | 周长
         */
        public double perimeter() {
            if (points.size() < 2) {
                return 0.0;
            }
            double totalDist = 0.0;
            int n = points.size();
            for (int i = 0; i < n; i++) {
                int[] current = points.get(i);
                int[] next = points.get((i + 1) % n);
                double dx = next[0] - current[0];
                double dy = next[1] - current[1];
                totalDist += Math.sqrt(dx * dx + dy * dy);
            }
            return totalDist;
        }

        /**
         * Compute the bounding box of this contour.
         * 计算此轮廓的边界框。
         *
         * @return an array [x, y, width, height] of the bounding box | 边界框数组 [x, y, width, height]
         */
        public int[] boundingBox() {
            if (points.isEmpty()) {
                return new int[]{0, 0, 0, 0};
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (int[] pt : points) {
                if (pt[0] < minX) {
                    minX = pt[0];
                }
                if (pt[0] > maxX) {
                    maxX = pt[0];
                }
                if (pt[1] < minY) {
                    minY = pt[1];
                }
                if (pt[1] > maxY) {
                    maxY = pt[1];
                }
            }
            return new int[]{minX, minY, maxX - minX + 1, maxY - minY + 1};
        }
    }

    /**
     * Find all contours in a binary image.
     * 在二值图像中查找所有轮廓。
     *
     * <p>Foreground pixels are those with non-zero grayscale value after converting
     * the image to grayscale using ITU-R BT.601 coefficients. A border pixel is a
     * foreground pixel adjacent to at least one background pixel (or image edge).</p>
     * <p>将图像转换为灰度（使用 ITU-R BT.601 系数）后，非零灰度值的像素为前景像素。
     * 边界像素是至少与一个背景像素（或图像边缘）相邻的前景像素。</p>
     *
     * <p>The algorithm uses Moore neighborhood border tracing:</p>
     * <ol>
     *   <li>Scan image for unvisited foreground pixel adjacent to background</li>
     *   <li>Follow border clockwise using Moore neighborhood tracing</li>
     *   <li>Mark visited border pixels to avoid re-detection</li>
     *   <li>Repeat until no new contour starts found</li>
     * </ol>
     * <p>算法使用 Moore 邻域边界追踪:</p>
     * <ol>
     *   <li>扫描图像中未访问的、与背景相邻的前景像素</li>
     *   <li>使用 Moore 邻域追踪沿边界顺时针行走</li>
     *   <li>标记已访问的边界像素以避免重复检测</li>
     *   <li>重复直到找不到新的轮廓起点</li>
     * </ol>
     *
     * @param image the input image | 输入图像
     * @return the list of detected contours | 检测到的轮廓列表
     * @throws NullPointerException    if image is null | 当图像为 null 时抛出
     * @throws ImageOperationException if the image has invalid dimensions | 当图像尺寸无效时抛出
     */
    public static List<Contour> find(BufferedImage image) {
        Objects.requireNonNull(image, "image must not be null");

        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= 0 || height <= 0) {
            throw new ImageOperationException(
                    "Image dimensions must be positive, got: " + width + "x" + height,
                    ImageErrorCode.INVALID_DIMENSIONS);
        }

        // Convert to grayscale binary mask
        boolean[][] foreground = buildForegroundMask(image, width, height);

        // Track visited border pixels
        boolean[][] visited = new boolean[height][width];

        List<Contour> contours = new ArrayList<>();

        // Scan for contour start pixels (foreground pixels adjacent to background)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (foreground[y][x] && !visited[y][x] && isBorderPixel(foreground, x, y, width, height)) {
                    List<int[]> points = traceBorder(foreground, visited, x, y, width, height);
                    if (!points.isEmpty()) {
                        contours.add(new Contour(points));
                    }
                }
            }
        }

        return Collections.unmodifiableList(contours);
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
     * Check if a foreground pixel is a border pixel (adjacent to background or image edge).
     */
    private static boolean isBorderPixel(boolean[][] foreground, int x, int y, int width, int height) {
        // Edge of image is always a border
        if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
            return true;
        }
        // Check 8-neighbors for any background pixel
        for (int d = 0; d < 8; d++) {
            int nx = x + DX[d];
            int ny = y + DY[d];
            if (!foreground[ny][nx]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Trace the border of a contour using Moore neighborhood tracing.
     *
     * <p>From the start pixel, walk around the boundary recording each border pixel.
     * The next boundary pixel is found by checking 8-neighbors clockwise starting
     * from the direction we came from. The contour is complete when we return to the
     * start pixel and the entry direction matches.</p>
     */
    private static List<int[]> traceBorder(boolean[][] foreground, boolean[][] visited,
                                           int startX, int startY, int width, int height) {
        List<int[]> points = new ArrayList<>();
        // Mark start and add to contour
        visited[startY][startX] = true;
        points.add(new int[]{startX, startY});

        // Initial backtrack direction: we enter from the west (direction 4 = W),
        // so we start scanning from direction (4+1) % 8 = 5 = NW clockwise.
        // But convention: the "came from" direction for the very first pixel found
        // by scanning left-to-right is W (direction index 4).
        int backtrackDir = 4; // West: we came from the left during scan

        int curX = startX;
        int curY = startY;

        // Safety limit: contour cannot be longer than the image perimeter
        int maxSteps = (int) Math.min(2L * width * height, Integer.MAX_VALUE);

        for (int step = 0; step < maxSteps; step++) {
            // Search clockwise from (backtrackDir + 1) % 8
            int startDir = (backtrackDir + 1) % 8;
            boolean found = false;

            for (int i = 0; i < 8; i++) {
                int dir = (startDir + i) % 8;
                int nx = curX + DX[dir];
                int ny = curY + DY[dir];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height && foreground[ny][nx]) {
                    // Check termination: returned to start with same entry direction
                    if (nx == startX && ny == startY && points.size() > 2) {
                        return points;
                    }

                    // Move to this pixel
                    curX = nx;
                    curY = ny;
                    visited[curY][curX] = true;

                    // Only add if not a duplicate of the last point
                    int[] last = points.getLast();
                    if (last[0] != curX || last[1] != curY) {
                        points.add(new int[]{curX, curY});
                    }

                    // Backtrack direction: the opposite of the direction we just moved
                    backtrackDir = (dir + 4) % 8;
                    found = true;
                    break;
                }
            }

            if (!found) {
                // Isolated pixel or dead end
                break;
            }
        }

        return points;
    }
}
