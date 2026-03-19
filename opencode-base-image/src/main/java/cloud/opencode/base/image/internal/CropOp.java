package cloud.opencode.base.image.internal;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Crop Operation
 * 裁剪操作
 *
 * <p>Internal utility for image cropping operations.</p>
 * <p>图片裁剪操作的内部工具类。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Image region cropping - 图片区域裁剪</li>
 *   <li>Internal utility, not part of public API - 内部工具，非公共 API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Internal usage
 * BufferedImage cropped = CropOp.crop(image, 100, 100, 400, 300);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (throws on null image) - 空值安全: 否（null 图片抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public final class CropOp {

    private CropOp() {
        // Utility class
    }

    /**
     * Crop to specified region
     * 裁剪到指定区域
     *
     * @param image the source image | 源图片
     * @param x the x coordinate | X坐标
     * @param y the y coordinate | Y坐标
     * @param width the crop width | 裁剪宽度
     * @param height the crop height | 裁剪高度
     * @return the cropped image | 裁剪后的图片
     */
    public static BufferedImage crop(BufferedImage image, int x, int y, int width, int height) {
        // Validate and adjust bounds
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x >= imgWidth) x = imgWidth - 1;
        if (y >= imgHeight) y = imgHeight - 1;

        if (x + width > imgWidth) width = imgWidth - x;
        if (y + height > imgHeight) height = imgHeight - y;

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid crop region");
        }

        // Use getSubimage for simple crop
        BufferedImage cropped = image.getSubimage(x, y, width, height);

        // Create a copy to avoid reference to original
        BufferedImage result = new BufferedImage(width, height, getTargetType(image));
        Graphics2D g = result.createGraphics();
        try {
            g.drawImage(cropped, 0, 0, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Crop from center
     * 从中心裁剪
     *
     * @param image the source image | 源图片
     * @param width the crop width | 裁剪宽度
     * @param height the crop height | 裁剪高度
     * @return the cropped image | 裁剪后的图片
     */
    public static BufferedImage cropCenter(BufferedImage image, int width, int height) {
        int x = (image.getWidth() - width) / 2;
        int y = (image.getHeight() - height) / 2;
        return crop(image, x, y, width, height);
    }

    /**
     * Crop to square from center
     * 从中心裁剪为正方形
     *
     * @param image the source image | 源图片
     * @return the cropped square image | 裁剪后的正方形图片
     */
    public static BufferedImage cropSquare(BufferedImage image) {
        int size = Math.min(image.getWidth(), image.getHeight());
        return cropCenter(image, size, size);
    }

    /**
     * Crop to aspect ratio from center
     * 从中心裁剪到指定宽高比
     *
     * @param image the source image | 源图片
     * @param aspectWidth aspect ratio width | 宽高比宽度
     * @param aspectHeight aspect ratio height | 宽高比高度
     * @return the cropped image | 裁剪后的图片
     */
    public static BufferedImage cropToAspectRatio(BufferedImage image, int aspectWidth, int aspectHeight) {
        int srcWidth = image.getWidth();
        int srcHeight = image.getHeight();

        double targetRatio = (double) aspectWidth / aspectHeight;
        double srcRatio = (double) srcWidth / srcHeight;

        int cropWidth, cropHeight;

        if (srcRatio > targetRatio) {
            // Source is wider, crop horizontally
            cropHeight = srcHeight;
            cropWidth = (int) (srcHeight * targetRatio);
        } else {
            // Source is taller, crop vertically
            cropWidth = srcWidth;
            cropHeight = (int) (srcWidth / targetRatio);
        }

        return cropCenter(image, cropWidth, cropHeight);
    }

    /**
     * Crop top portion
     * 裁剪顶部部分
     *
     * @param image the source image | 源图片
     * @param height the crop height | 裁剪高度
     * @return the cropped image | 裁剪后的图片
     */
    public static BufferedImage cropTop(BufferedImage image, int height) {
        return crop(image, 0, 0, image.getWidth(), height);
    }

    /**
     * Crop bottom portion
     * 裁剪底部部分
     *
     * @param image the source image | 源图片
     * @param height the crop height | 裁剪高度
     * @return the cropped image | 裁剪后的图片
     */
    public static BufferedImage cropBottom(BufferedImage image, int height) {
        int y = image.getHeight() - height;
        return crop(image, 0, y, image.getWidth(), height);
    }

    /**
     * Crop left portion
     * 裁剪左侧部分
     *
     * @param image the source image | 源图片
     * @param width the crop width | 裁剪宽度
     * @return the cropped image | 裁剪后的图片
     */
    public static BufferedImage cropLeft(BufferedImage image, int width) {
        return crop(image, 0, 0, width, image.getHeight());
    }

    /**
     * Crop right portion
     * 裁剪右侧部分
     *
     * @param image the source image | 源图片
     * @param width the crop width | 裁剪宽度
     * @return the cropped image | 裁剪后的图片
     */
    public static BufferedImage cropRight(BufferedImage image, int width) {
        int x = image.getWidth() - width;
        return crop(image, x, 0, width, image.getHeight());
    }

    /**
     * Trim edges by amount
     * 按数量修剪边缘
     *
     * @param image the source image | 源图片
     * @param top top trim | 顶部修剪
     * @param right right trim | 右侧修剪
     * @param bottom bottom trim | 底部修剪
     * @param left left trim | 左侧修剪
     * @return the trimmed image | 修剪后的图片
     */
    public static BufferedImage trim(BufferedImage image, int top, int right, int bottom, int left) {
        int x = left;
        int y = top;
        int width = image.getWidth() - left - right;
        int height = image.getHeight() - top - bottom;
        return crop(image, x, y, width, height);
    }

    /**
     * Trim equal amount from all edges
     * 从所有边缘修剪相同数量
     *
     * @param image the source image | 源图片
     * @param amount the trim amount | 修剪数量
     * @return the trimmed image | 修剪后的图片
     */
    public static BufferedImage trim(BufferedImage image, int amount) {
        return trim(image, amount, amount, amount, amount);
    }

    /**
     * Get appropriate image type for output
     * 获取输出的适当图片类型
     */
    private static int getTargetType(BufferedImage source) {
        int type = source.getType();
        if (type == BufferedImage.TYPE_CUSTOM || type == 0) {
            return BufferedImage.TYPE_INT_ARGB;
        }
        return type;
    }
}
