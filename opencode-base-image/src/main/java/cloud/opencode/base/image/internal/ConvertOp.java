package cloud.opencode.base.image.internal;

import cloud.opencode.base.image.ImageFormat;
import cloud.opencode.base.image.kernel.PixelOp;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

/**
 * Convert Operation
 * 转换操作
 *
 * <p>Internal utility for image format and color conversion operations.</p>
 * <p>图片格式和颜色转换操作的内部工具类。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Image format and color space conversion - 图片格式和颜色空间转换</li>
 *   <li>Internal utility, not part of public API - 内部工具，非公共 API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Internal usage
 * BufferedImage converted = ConvertOp.convert(image, ImageFormat.PNG);
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
public final class ConvertOp {

    private ConvertOp() {
        // Utility class
    }

    /**
     * Convert image format
     * 转换图片格式
     *
     * @param image the source image | 源图片
     * @param sourceFormat the source format | 源格式
     * @param targetFormat the target format | 目标格式
     * @return the converted image | 转换后的图片
     */
    public static BufferedImage convert(BufferedImage image, ImageFormat sourceFormat, ImageFormat targetFormat) {
        // Handle transparency conversion
        if (!targetFormat.supportsTransparency() && hasTransparency(image)) {
            return removeAlpha(image);
        }

        if (targetFormat.supportsTransparency() && !hasTransparency(image)) {
            return addAlpha(image);
        }

        return copyImage(image);
    }

    /**
     * Convert to grayscale
     * 转换为灰度图
     *
     * @param image the source image | 源图片
     * @return the grayscale image | 灰度图片
     */
    public static BufferedImage grayscale(BufferedImage image) {
        ColorSpace graySpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(graySpace, null);

        BufferedImage result = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        op.filter(image, result);
        return result;
    }

    /**
     * Convert to sepia tone
     * 转换为棕褐色调
     *
     * @param image the source image | 源图片
     * @return the sepia image | 棕褐色图片
     */
    public static BufferedImage sepia(BufferedImage image) {
        BufferedImage src = PixelOp.ensureArgb(image);
        int[] srcPixels = PixelOp.getPixels(src);
        BufferedImage result = PixelOp.createCompatible(src);
        int[] dstPixels = PixelOp.getPixels(result);

        for (int i = 0; i < srcPixels.length; i++) {
            int px = srcPixels[i];
            int a = (px >> 24) & 0xFF;
            int r = (px >> 16) & 0xFF;
            int g = (px >> 8) & 0xFF;
            int b = px & 0xFF;

            int tr = Math.min(255, (int) (0.393 * r + 0.769 * g + 0.189 * b));
            int tg = Math.min(255, (int) (0.349 * r + 0.686 * g + 0.168 * b));
            int tb = Math.min(255, (int) (0.272 * r + 0.534 * g + 0.131 * b));

            dstPixels[i] = (a << 24) | (tr << 16) | (tg << 8) | tb;
        }

        return result;
    }

    /**
     * Invert colors (negative)
     * 反转颜色（负片）
     *
     * @param image the source image | 源图片
     * @return the inverted image | 反转后的图片
     */
    public static BufferedImage invert(BufferedImage image) {
        BufferedImage src = PixelOp.ensureArgb(image);
        int[] srcPixels = PixelOp.getPixels(src);
        BufferedImage result = PixelOp.createCompatible(src);
        int[] dstPixels = PixelOp.getPixels(result);

        for (int i = 0; i < srcPixels.length; i++) {
            int px = srcPixels[i];
            int a = (px >> 24) & 0xFF;
            int r = 255 - ((px >> 16) & 0xFF);
            int g = 255 - ((px >> 8) & 0xFF);
            int b = 255 - (px & 0xFF);
            dstPixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        return result;
    }

    /**
     * Adjust brightness
     * 调整亮度
     *
     * @param image the source image | 源图片
     * @param factor brightness factor (-1.0 to 1.0) | 亮度因子（-1.0到1.0）
     * @return the adjusted image | 调整后的图片
     */
    public static BufferedImage adjustBrightness(BufferedImage image, float factor) {
        int adjustment = (int) (factor * 255);

        BufferedImage src = PixelOp.ensureArgb(image);
        int[] srcPixels = PixelOp.getPixels(src);
        BufferedImage result = PixelOp.createCompatible(src);
        int[] dstPixels = PixelOp.getPixels(result);

        for (int i = 0; i < srcPixels.length; i++) {
            int px = srcPixels[i];
            int a = (px >> 24) & 0xFF;
            int r = clamp(((px >> 16) & 0xFF) + adjustment);
            int g = clamp(((px >> 8) & 0xFF) + adjustment);
            int b = clamp((px & 0xFF) + adjustment);
            dstPixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        return result;
    }

    /**
     * Adjust contrast
     * 调整对比度
     *
     * @param image the source image | 源图片
     * @param factor contrast factor (0.0 to 2.0, 1.0 is normal) | 对比度因子
     * @return the adjusted image | 调整后的图片
     */
    public static BufferedImage adjustContrast(BufferedImage image, float factor) {
        BufferedImage src = PixelOp.ensureArgb(image);
        int[] srcPixels = PixelOp.getPixels(src);
        BufferedImage result = PixelOp.createCompatible(src);
        int[] dstPixels = PixelOp.getPixels(result);

        for (int i = 0; i < srcPixels.length; i++) {
            int px = srcPixels[i];
            int a = (px >> 24) & 0xFF;
            int r = clamp((int) ((((px >> 16) & 0xFF) - 128) * factor + 128));
            int g = clamp((int) ((((px >> 8) & 0xFF) - 128) * factor + 128));
            int b = clamp((int) (((px & 0xFF) - 128) * factor + 128));
            dstPixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        return result;
    }

    /**
     * Remove alpha channel
     * 移除透明通道
     *
     * @param image the source image | 源图片
     * @return the image without alpha | 无透明通道的图片
     */
    public static BufferedImage removeAlpha(BufferedImage image) {
        BufferedImage result = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Add alpha channel
     * 添加透明通道
     *
     * @param image the source image | 源图片
     * @return the image with alpha | 带透明通道的图片
     */
    public static BufferedImage addAlpha(BufferedImage image) {
        BufferedImage result = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        try {
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Check if image has transparency
     * 检查图片是否有透明度
     *
     * @param image the image | 图片
     * @return true if has transparency | 如果有透明度返回true
     */
    public static boolean hasTransparency(BufferedImage image) {
        return image.getColorModel().hasAlpha();
    }

    /**
     * Create a copy of the image
     * 创建图片副本
     */
    private static BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
            source.getWidth(), source.getHeight(), getTargetType(source));
        Graphics2D g = copy.createGraphics();
        try {
            g.drawImage(source, 0, 0, null);
        } finally {
            g.dispose();
        }
        return copy;
    }

    /**
     * Clamp value to 0-255 range
     * 将值限制在0-255范围内
     */
    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
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
