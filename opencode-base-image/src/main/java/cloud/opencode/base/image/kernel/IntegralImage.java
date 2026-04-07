package cloud.opencode.base.image.kernel;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;

import java.util.Objects;

/**
 * Integral Image (Summed-Area Table)
 * 积分图（求和面积表）
 *
 * <p>Computes an integral image from grayscale pixel data, enabling O(1) region
 * sum, mean, and variance queries for any axis-aligned rectangle.</p>
 * <p>从灰度像素数据计算积分图，支持对任意轴对齐矩形进行 O(1) 的区域求和、均值和方差查询。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>O(1) region sum query - O(1) 区域求和查询</li>
 *   <li>O(1) region mean query - O(1) 区域均值查询</li>
 *   <li>O(1) region variance query - O(1) 区域方差查询</li>
 *   <li>Uses long[] to prevent overflow - 使用 long[] 防止溢出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * int[] gray = ChannelOp.toGray(PixelOp.getPixels(image));
 * IntegralImage integral = new IntegralImage(gray, width, height);
 * long sum = integral.regionSum(10, 10, 50, 50);
 * double mean = integral.regionMean(10, 10, 50, 50);
 * double variance = integral.regionVariance(10, 10, 50, 50);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Construction: O(w * h) - 构建: O(w * h)</li>
 *   <li>Query: O(1) - 查询: O(1)</li>
 *   <li>Space: O(w * h) for sum and sumSq tables - 空间: 求和和平方和表各 O(w * h)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class IntegralImage {

    private final long[] sum;
    private final long[] sumSq;
    private final int width;
    private final int height;

    /**
     * Construct an integral image from grayscale pixel data.
     * 从灰度像素数据构建积分图。
     *
     * <p>The integral image uses 1-indexed tables of size (width+1)*(height+1)
     * so that region queries don't need special-case boundary checks.</p>
     * <p>积分图使用大小为 (width+1)*(height+1) 的 1 索引表，
     * 使区域查询不需要特殊边界检查。</p>
     *
     * @param grayPixels grayscale pixel array of length width*height | 长度为 width*height 的灰度像素数组
     * @param width      the image width | 图像宽度
     * @param height     the image height | 图像高度
     * @throws ImageOperationException if parameters are invalid | 当参数无效时抛出
     */
    public IntegralImage(int[] grayPixels, int width, int height) {
        Objects.requireNonNull(grayPixels, "grayPixels must not be null");
        if (width <= 0 || height <= 0) {
            throw new ImageOperationException(
                    "Dimensions must be positive, got: " + width + "x" + height,
                    ImageErrorCode.INVALID_DIMENSIONS);
        }
        if (grayPixels.length != (long) width * height) {
            throw new ImageOperationException(
                    "Pixel array length must equal width*height, got: " + grayPixels.length + " != " + ((long) width * height),
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        this.width = width;
        this.height = height;

        // 1-indexed table: (width+1) * (height+1)
        long tableSizeL = (long) (width + 1) * (height + 1);
        if (tableSizeL > Integer.MAX_VALUE) {
            throw new ImageOperationException(
                    "Image too large for integral image table: " + width + "x" + height,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        int tableSize = (int) tableSizeL;
        int tw = width + 1;
        int th = height + 1;
        this.sum = new long[tableSize];
        this.sumSq = new long[tableSize];

        for (int y = 1; y < th; y++) {
            for (int x = 1; x < tw; x++) {
                long val = grayPixels[(y - 1) * width + (x - 1)];
                sum[y * tw + x] = val
                        + sum[(y - 1) * tw + x]
                        + sum[y * tw + (x - 1)]
                        - sum[(y - 1) * tw + (x - 1)];
                sumSq[y * tw + x] = val * val
                        + sumSq[(y - 1) * tw + x]
                        + sumSq[y * tw + (x - 1)]
                        - sumSq[(y - 1) * tw + (x - 1)];
            }
        }
    }

    /**
     * Compute the sum of pixel values in a rectangular region [x1, y1] to [x2, y2] (inclusive).
     * 计算矩形区域 [x1, y1] 到 [x2, y2]（包含端点）内的像素值之和。
     *
     * @param x1 left column (inclusive, 0-based) | 左列（包含，从 0 开始）
     * @param y1 top row (inclusive, 0-based) | 上行（包含，从 0 开始）
     * @param x2 right column (inclusive, 0-based) | 右列（包含，从 0 开始）
     * @param y2 bottom row (inclusive, 0-based) | 下行（包含，从 0 开始）
     * @return the sum of pixel values in the region | 区域内像素值之和
     * @throws ImageOperationException if coordinates are out of bounds | 当坐标越界时抛出
     */
    public long regionSum(int x1, int y1, int x2, int y2) {
        validateRegion(x1, y1, x2, y2);
        int tw = width + 1;
        // Convert to 1-indexed: add 1 to x2, y2 for the inclusive right/bottom
        return sum[(y2 + 1) * tw + (x2 + 1)]
                - sum[y1 * tw + (x2 + 1)]
                - sum[(y2 + 1) * tw + x1]
                + sum[y1 * tw + x1];
    }

    /**
     * Compute the mean of pixel values in a rectangular region [x1, y1] to [x2, y2] (inclusive).
     * 计算矩形区域 [x1, y1] 到 [x2, y2]（包含端点）内的像素均值。
     *
     * @param x1 left column (inclusive, 0-based) | 左列（包含，从 0 开始）
     * @param y1 top row (inclusive, 0-based) | 上行（包含，从 0 开始）
     * @param x2 right column (inclusive, 0-based) | 右列（包含，从 0 开始）
     * @param y2 bottom row (inclusive, 0-based) | 下行（包含，从 0 开始）
     * @return the mean of pixel values in the region | 区域内像素均值
     * @throws ImageOperationException if coordinates are out of bounds | 当坐标越界时抛出
     */
    public double regionMean(int x1, int y1, int x2, int y2) {
        long s = regionSum(x1, y1, x2, y2);
        long count = (long) (x2 - x1 + 1) * (y2 - y1 + 1);
        return (double) s / count;
    }

    /**
     * Compute the variance of pixel values in a rectangular region [x1, y1] to [x2, y2] (inclusive).
     * 计算矩形区域 [x1, y1] 到 [x2, y2]（包含端点）内的像素方差。
     *
     * <p>Variance = E[X^2] - (E[X])^2</p>
     *
     * @param x1 left column (inclusive, 0-based) | 左列（包含，从 0 开始）
     * @param y1 top row (inclusive, 0-based) | 上行（包含，从 0 开始）
     * @param x2 right column (inclusive, 0-based) | 右列（包含，从 0 开始）
     * @param y2 bottom row (inclusive, 0-based) | 下行（包含，从 0 开始）
     * @return the variance of pixel values in the region | 区域内像素方差
     * @throws ImageOperationException if coordinates are out of bounds | 当坐标越界时抛出
     */
    public double regionVariance(int x1, int y1, int x2, int y2) {
        validateRegion(x1, y1, x2, y2);
        long count = (long) (x2 - x1 + 1) * (y2 - y1 + 1);
        long s = regionSum(x1, y1, x2, y2);
        int tw = width + 1;
        long sq = sumSq[(y2 + 1) * tw + (x2 + 1)]
                - sumSq[y1 * tw + (x2 + 1)]
                - sumSq[(y2 + 1) * tw + x1]
                + sumSq[y1 * tw + x1];
        double mean = (double) s / count;
        return (double) sq / count - mean * mean;
    }

    /**
     * Get the image width.
     * 获取图像宽度。
     *
     * @return the image width | 图像宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the image height.
     * 获取图像高度。
     *
     * @return the image height | 图像高度
     */
    public int getHeight() {
        return height;
    }

    private void validateRegion(int x1, int y1, int x2, int y2) {
        if (x1 < 0 || y1 < 0 || x2 >= width || y2 >= height || x1 > x2 || y1 > y2) {
            throw new ImageOperationException(
                    "Invalid region [" + x1 + "," + y1 + "]-[" + x2 + "," + y2
                            + "] for image " + width + "x" + height,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
    }
}
