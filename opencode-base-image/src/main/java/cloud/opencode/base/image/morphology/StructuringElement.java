package cloud.opencode.base.image.morphology;

import cloud.opencode.base.image.exception.ImageErrorCode;
import cloud.opencode.base.image.exception.ImageOperationException;

/**
 * Structuring Element for Morphological Operations
 * 形态学运算的结构元素
 *
 * <p>Defines a binary mask used as the kernel for morphological operations such as
 * erosion, dilation, opening, and closing. Provides factory methods for common shapes.</p>
 * <p>定义用于形态学操作（如腐蚀、膨胀、开运算、闭运算）内核的二值掩码，
 * 提供常用形状的工厂方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Rectangular structuring element - 矩形结构元素</li>
 *   <li>Elliptical structuring element - 椭圆形结构元素</li>
 *   <li>Cross (plus) structuring element - 十字形结构元素</li>
 *   <li>Custom anchor point support - 自定义锚点支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * StructuringElement rect = StructuringElement.rect(3, 3);
 * StructuringElement ellipse = StructuringElement.ellipse(5, 5);
 * StructuringElement cross = StructuringElement.cross(3);
 * BufferedImage eroded = MorphologyOp.erode(image, rect);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 *
 * @param mask    the binary mask array of length width*height | 长度为 width*height 的二值掩码数组
 * @param width   the width of the structuring element (must be positive and odd) | 结构元素宽度（必须为正奇数）
 * @param height  the height of the structuring element (must be positive and odd) | 结构元素高度（必须为正奇数）
 * @param anchorX the x-coordinate of the anchor point | 锚点 x 坐标
 * @param anchorY the y-coordinate of the anchor point | 锚点 y 坐标
 */
public record StructuringElement(boolean[] mask, int width, int height, int anchorX, int anchorY) {

    /**
     * Compact constructor with validation.
     * 带验证的紧凑构造器。
     */
    public StructuringElement {
        if (mask == null) {
            throw new ImageOperationException("mask must not be null",
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (width <= 0 || width % 2 == 0) {
            throw new ImageOperationException(
                    "width must be a positive odd number, got: " + width,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (height <= 0 || height % 2 == 0) {
            throw new ImageOperationException(
                    "height must be a positive odd number, got: " + height,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (mask.length != width * height) {
            throw new ImageOperationException(
                    "mask length must equal width*height (" + (width * height) + "), got: " + mask.length,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (anchorX < 0 || anchorX >= width) {
            throw new ImageOperationException(
                    "anchorX must be in [0, " + (width - 1) + "], got: " + anchorX,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (anchorY < 0 || anchorY >= height) {
            throw new ImageOperationException(
                    "anchorY must be in [0, " + (height - 1) + "], got: " + anchorY,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        // Defensive copy
        mask = mask.clone();
    }

    /**
     * Return a defensive copy of the mask.
     * 返回掩码的防御性副本。
     *
     * @return a copy of the mask array | 掩码数组的副本
     */
    @Override
    public boolean[] mask() {
        return mask.clone();
    }

    /**
     * Get the raw mask value at the given position (no copy).
     * 获取给定位置的原始掩码值（不复制）。
     *
     * @param x the x-coordinate | x 坐标
     * @param y the y-coordinate | y 坐标
     * @return true if the mask is set at (x, y) | 如果掩码在 (x, y) 处被设置则返回 true
     */
    boolean maskAt(int x, int y) {
        return mask[y * width + x];
    }

    /**
     * Create a rectangular structuring element with all mask values set to true.
     * 创建所有掩码值为 true 的矩形结构元素。
     *
     * @param width  the width (must be positive and odd) | 宽度（必须为正奇数）
     * @param height the height (must be positive and odd) | 高度（必须为正奇数）
     * @return a rectangular structuring element | 矩形结构元素
     * @throws ImageOperationException if dimensions are invalid | 当尺寸无效时抛出
     */
    public static StructuringElement rect(int width, int height) {
        if (width <= 0 || width % 2 == 0) {
            throw new ImageOperationException(
                    "width must be a positive odd number, got: " + width,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (height <= 0 || height % 2 == 0) {
            throw new ImageOperationException(
                    "height must be a positive odd number, got: " + height,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        boolean[] mask = new boolean[width * height];
        java.util.Arrays.fill(mask, true);
        return new StructuringElement(mask, width, height, width / 2, height / 2);
    }

    /**
     * Create an elliptical structuring element.
     * 创建椭圆形结构元素。
     *
     * <p>Pixels inside the ellipse inscribed in the width x height rectangle are set to true.</p>
     * <p>在 width x height 矩形内接椭圆内的像素设置为 true。</p>
     *
     * @param width  the width (must be positive and odd) | 宽度（必须为正奇数）
     * @param height the height (must be positive and odd) | 高度（必须为正奇数）
     * @return an elliptical structuring element | 椭圆形结构元素
     * @throws ImageOperationException if dimensions are invalid | 当尺寸无效时抛出
     */
    public static StructuringElement ellipse(int width, int height) {
        if (width <= 0 || width % 2 == 0) {
            throw new ImageOperationException(
                    "Width must be a positive odd number, got: " + width,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        if (height <= 0 || height % 2 == 0) {
            throw new ImageOperationException(
                    "Height must be a positive odd number, got: " + height,
                    ImageErrorCode.INVALID_PARAMETERS);
        }
        boolean[] mask = new boolean[width * height];
        double cx = (width - 1) / 2.0;
        double cy = (height - 1) / 2.0;
        double rx = width / 2.0;
        double ry = height / 2.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double dx = (x - cx) / rx;
                double dy = (y - cy) / ry;
                mask[y * width + x] = (dx * dx + dy * dy) <= 1.0;
            }
        }
        return new StructuringElement(mask, width, height, width / 2, height / 2);
    }

    /**
     * Create a cross (plus) shaped structuring element.
     * 创建十字形结构元素。
     *
     * <p>The cross is formed by the center row and center column of a size x size grid.</p>
     * <p>十字由 size x size 网格的中心行和中心列构成。</p>
     *
     * @param size the width and height (must be positive and odd) | 宽度和高度（必须为正奇数）
     * @return a cross-shaped structuring element | 十字形结构元素
     * @throws ImageOperationException if size is invalid | 当尺寸无效时抛出
     */
    public static StructuringElement cross(int size) {
        boolean[] mask = new boolean[size * size];
        int center = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                mask[y * size + x] = (x == center || y == center);
            }
        }
        return new StructuringElement(mask, size, size, center, center);
    }
}
