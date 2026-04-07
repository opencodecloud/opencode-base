package cloud.opencode.base.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

/**
 * Image Stitching Utility
 * 图片拼接工具类
 *
 * <p>Provides static methods for stitching multiple images together
 * horizontally, vertically, or in a grid layout.</p>
 * <p>提供静态方法用于将多张图片水平拼接、垂直拼接或按网格排列。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Horizontal stitching with auto vertical centering - 水平拼接，自动垂直居中</li>
 *   <li>Vertical stitching with auto horizontal centering - 垂直拼接，自动水平居中</li>
 *   <li>Grid layout stitching by column count - 按列数网格拼接</li>
 *   <li>Gap and background color support - 支持间距和背景色</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Horizontal stitch
 * BufferedImage result = ImageStitch.horizontal(img1, img2, img3);
 *
 * // Vertical stitch with gap
 * BufferedImage result = ImageStitch.vertical(10, Color.WHITE, img1, img2);
 *
 * // Grid layout
 * BufferedImage result = ImageStitch.grid(List.of(img1, img2, img3, img4), 2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (throws on null input) - 空值安全: 否（null 输入抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
public final class ImageStitch {

    private ImageStitch() {
        // Utility class
    }

    /**
     * Stitch images horizontally; height is the maximum, smaller images are vertically centered
     * 水平拼接图片；高度取最大值，较小的图片垂直居中
     *
     * @param images the images to stitch | 要拼接的图片
     * @return the stitched image | 拼接后的图片
     * @throws IllegalArgumentException if images is null or empty | 如果图片数组为 null 或空
     */
    public static BufferedImage horizontal(BufferedImage... images) {
        return horizontal(0, null, images);
    }

    /**
     * Stitch images horizontally with gap and background color
     * 水平拼接图片，带间距和背景色
     *
     * @param gap the gap between images in pixels | 图片之间的间距（像素）
     * @param bgColor the background color for gaps and padding (null for transparent) | 间距和边距的背景色（null 表示透明）
     * @param images the images to stitch | 要拼接的图片
     * @return the stitched image | 拼接后的图片
     * @throws IllegalArgumentException if images is null or empty, or gap is negative | 如果图片为 null/空或间距为负
     */
    public static BufferedImage horizontal(int gap, Color bgColor, BufferedImage... images) {
        validateImages(images);
        if (gap < 0) {
            throw new IllegalArgumentException("Gap must be >= 0, got: " + gap);
        }

        long totalWidthL = 0L;
        int maxHeight = 0;
        for (BufferedImage img : images) {
            totalWidthL += img.getWidth();
            maxHeight = Math.max(maxHeight, img.getHeight());
        }
        totalWidthL += (long) gap * (images.length - 1);
        if (totalWidthL > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Resulting image width exceeds maximum: " + totalWidthL);
        }
        int totalWidth = (int) totalWidthL;

        BufferedImage result = new BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        try {
            if (bgColor != null) {
                g.setColor(bgColor);
                g.fillRect(0, 0, totalWidth, maxHeight);
            }

            int x = 0;
            for (BufferedImage img : images) {
                int y = (maxHeight - img.getHeight()) / 2;
                g.drawImage(img, x, y, null);
                x += img.getWidth() + gap;
            }
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Stitch images vertically; width is the maximum, smaller images are horizontally centered
     * 垂直拼接图片；宽度取最大值，较小的图片水平居中
     *
     * @param images the images to stitch | 要拼接的图片
     * @return the stitched image | 拼接后的图片
     * @throws IllegalArgumentException if images is null or empty | 如果图片数组为 null 或空
     */
    public static BufferedImage vertical(BufferedImage... images) {
        return vertical(0, null, images);
    }

    /**
     * Stitch images vertically with gap and background color
     * 垂直拼接图片，带间距和背景色
     *
     * @param gap the gap between images in pixels | 图片之间的间距（像素）
     * @param bgColor the background color for gaps and padding (null for transparent) | 间距和边距的背景色（null 表示透明）
     * @param images the images to stitch | 要拼接的图片
     * @return the stitched image | 拼接后的图片
     * @throws IllegalArgumentException if images is null or empty, or gap is negative | 如果图片为 null/空或间距为负
     */
    public static BufferedImage vertical(int gap, Color bgColor, BufferedImage... images) {
        validateImages(images);
        if (gap < 0) {
            throw new IllegalArgumentException("Gap must be >= 0, got: " + gap);
        }

        int maxWidth = 0;
        long totalHeightL = 0L;
        for (BufferedImage img : images) {
            maxWidth = Math.max(maxWidth, img.getWidth());
            totalHeightL += img.getHeight();
        }
        totalHeightL += (long) gap * (images.length - 1);
        if (totalHeightL > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Resulting image height exceeds maximum: " + totalHeightL);
        }
        int totalHeight = (int) totalHeightL;

        BufferedImage result = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        try {
            if (bgColor != null) {
                g.setColor(bgColor);
                g.fillRect(0, 0, maxWidth, totalHeight);
            }

            int y = 0;
            for (BufferedImage img : images) {
                int x = (maxWidth - img.getWidth()) / 2;
                g.drawImage(img, x, y, null);
                y += img.getHeight() + gap;
            }
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Stitch images in a grid layout with the specified number of columns
     * 按指定列数网格拼接图片，自动计算行数
     *
     * <p>Cell size is determined by the maximum width and height across all images.
     * Each image is centered within its cell.</p>
     * <p>单元格尺寸由所有图片中的最大宽度和最大高度决定。每张图片在其单元格内居中。</p>
     *
     * @param images the images to stitch | 要拼接的图片
     * @param columns the number of columns | 列数
     * @return the stitched image | 拼接后的图片
     * @throws IllegalArgumentException if images is null or empty, or columns &lt; 1 | 如果图片为 null/空或列数小于 1
     */
    public static BufferedImage grid(List<BufferedImage> images, int columns) {
        Objects.requireNonNull(images, "Images list must not be null");
        if (images.isEmpty()) {
            throw new IllegalArgumentException("Images list must not be empty");
        }
        if (columns < 1) {
            throw new IllegalArgumentException("Columns must be >= 1, got: " + columns);
        }
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i) == null) {
                throw new IllegalArgumentException("Image at index " + i + " must not be null");
            }
        }

        int maxCellWidth = 0;
        int maxCellHeight = 0;
        for (BufferedImage img : images) {
            maxCellWidth = Math.max(maxCellWidth, img.getWidth());
            maxCellHeight = Math.max(maxCellHeight, img.getHeight());
        }

        int rows = (images.size() + columns - 1) / columns;
        long totalWidthL = (long) maxCellWidth * columns;
        long totalHeightL = (long) maxCellHeight * rows;
        if (totalWidthL > Integer.MAX_VALUE || totalHeightL > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Resulting grid dimensions exceed maximum: " + totalWidthL + "x" + totalHeightL);
        }
        int totalWidth = (int) totalWidthL;
        int totalHeight = (int) totalHeightL;

        BufferedImage result = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        try {
            for (int i = 0; i < images.size(); i++) {
                BufferedImage img = images.get(i);
                int col = i % columns;
                int row = i / columns;
                int x = col * maxCellWidth + (maxCellWidth - img.getWidth()) / 2;
                int y = row * maxCellHeight + (maxCellHeight - img.getHeight()) / 2;
                g.drawImage(img, x, y, null);
            }
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Validate images array is not null and not empty
     * 验证图片数组非 null 且非空
     *
     * @param images the images array | 图片数组
     * @throws IllegalArgumentException if validation fails | 如果验证失败
     */
    private static void validateImages(BufferedImage[] images) {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("Images array must not be null or empty");
        }
        for (int i = 0; i < images.length; i++) {
            if (images[i] == null) {
                throw new IllegalArgumentException("Image at index " + i + " must not be null");
            }
        }
    }
}
