package cloud.opencode.base.image.internal;

import cloud.opencode.base.image.Position;
import cloud.opencode.base.image.watermark.ImageWatermark;
import cloud.opencode.base.image.watermark.TextWatermark;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Watermark Operation
 * 水印操作
 *
 * <p>Internal utility for applying watermarks to images.</p>
 * <p>应用水印到图片的内部工具类。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Text and image watermark application - 文字和图片水印应用</li>
 *   <li>Internal utility, not part of public API - 内部工具，非公共 API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Internal usage
 * BufferedImage result = WatermarkOp.apply(image, textWatermark);
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
public final class WatermarkOp {

    private WatermarkOp() {
        // Utility class
    }

    /**
     * Apply text watermark
     * 应用文字水印
     *
     * @param image the source image | 源图片
     * @param watermark the text watermark | 文字水印
     * @return the watermarked image | 加水印后的图片
     */
    public static BufferedImage apply(BufferedImage image, TextWatermark watermark) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = copyImage(image);
        Graphics2D g = result.createGraphics();
        try {
            configureGraphics(g);

            // Set font and get text bounds
            g.setFont(watermark.font());
            FontRenderContext frc = g.getFontRenderContext();
            TextLayout layout = new TextLayout(watermark.text(), watermark.font(), frc);
            Rectangle2D bounds = layout.getBounds();

            int textWidth = (int) bounds.getWidth();
            int textHeight = (int) bounds.getHeight();

            // Calculate position
            int[] pos = calculatePosition(width, height, textWidth, textHeight,
                watermark.position(), watermark.margin());

            // Set opacity
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, watermark.opacity()));

            // Draw text
            g.setColor(watermark.color());
            g.drawString(watermark.text(), pos[0], pos[1] + textHeight);

        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Apply image watermark
     * 应用图片水印
     *
     * @param image the source image | 源图片
     * @param watermark the image watermark | 图片水印
     * @return the watermarked image | 加水印后的图片
     */
    public static BufferedImage apply(BufferedImage image, ImageWatermark watermark) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = copyImage(image);
        Graphics2D g = result.createGraphics();
        try {
            configureGraphics(g);

            int wmWidth = watermark.getWidth();
            int wmHeight = watermark.getHeight();

            // Calculate position
            int[] pos = calculatePosition(width, height, wmWidth, wmHeight,
                watermark.position(), watermark.margin());

            // Set opacity
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, watermark.opacity()));

            // Draw watermark image
            g.drawImage(watermark.image(), pos[0], pos[1], null);

        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Apply tiled watermark
     * 应用平铺水印
     *
     * @param image the source image | 源图片
     * @param watermark the text watermark | 文字水印
     * @param spacingX horizontal spacing | 水平间距
     * @param spacingY vertical spacing | 垂直间距
     * @return the watermarked image | 加水印后的图片
     */
    public static BufferedImage applyTiled(BufferedImage image, TextWatermark watermark,
                                           int spacingX, int spacingY) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = copyImage(image);
        Graphics2D g = result.createGraphics();
        try {
            configureGraphics(g);

            g.setFont(watermark.font());
            FontRenderContext frc = g.getFontRenderContext();
            TextLayout layout = new TextLayout(watermark.text(), watermark.font(), frc);
            Rectangle2D bounds = layout.getBounds();

            int textWidth = (int) bounds.getWidth();
            int textHeight = (int) bounds.getHeight();

            // Guard against zero or negative step sizes that would cause infinite loop
            int stepX = textWidth + spacingX;
            int stepY = textHeight + spacingY;
            if (stepX <= 0 || stepY <= 0) {
                throw new IllegalArgumentException(
                        "Tile step must be positive: stepX=" + stepX + ", stepY=" + stepY);
            }

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, watermark.opacity()));
            g.setColor(watermark.color());

            // Tile watermark across image
            for (int y = watermark.margin(); y < height; y += stepY) {
                for (int x = watermark.margin(); x < width; x += stepX) {
                    g.drawString(watermark.text(), x, y + textHeight);
                }
            }

        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Apply tiled image watermark
     * 应用平铺图片水印
     *
     * @param image the source image | 源图片
     * @param watermark the image watermark | 图片水印
     * @param spacingX horizontal spacing | 水平间距
     * @param spacingY vertical spacing | 垂直间距
     * @return the watermarked image | 加水印后的图片
     */
    public static BufferedImage applyTiled(BufferedImage image, ImageWatermark watermark,
                                           int spacingX, int spacingY) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = copyImage(image);
        Graphics2D g = result.createGraphics();
        try {
            configureGraphics(g);

            int wmWidth = watermark.getWidth();
            int wmHeight = watermark.getHeight();

            // Guard against zero or negative step sizes that would cause infinite loop
            int stepX = wmWidth + spacingX;
            int stepY = wmHeight + spacingY;
            if (stepX <= 0 || stepY <= 0) {
                throw new IllegalArgumentException(
                        "Tile step must be positive: stepX=" + stepX + ", stepY=" + stepY);
            }

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, watermark.opacity()));

            // Tile watermark across image
            for (int y = watermark.margin(); y < height; y += stepY) {
                for (int x = watermark.margin(); x < width; x += stepX) {
                    g.drawImage(watermark.image(), x, y, null);
                }
            }

        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Calculate position based on Position enum
     * 根据Position枚举计算位置
     */
    private static int[] calculatePosition(int imageWidth, int imageHeight,
                                           int wmWidth, int wmHeight,
                                           Position position, int margin) {
        int x, y;

        // Calculate X position
        if (position.isLeft()) {
            x = margin;
        } else if (position.isRight()) {
            x = imageWidth - wmWidth - margin;
        } else {
            x = (imageWidth - wmWidth) / 2;
        }

        // Calculate Y position
        if (position.isTop()) {
            y = margin;
        } else if (position.isBottom()) {
            y = imageHeight - wmHeight - margin;
        } else {
            y = (imageHeight - wmHeight) / 2;
        }

        return new int[]{x, y};
    }

    /**
     * Create a copy of the image
     * 创建图片副本
     */
    private static BufferedImage copyImage(BufferedImage source) {
        int type = source.getType();
        if (type == BufferedImage.TYPE_CUSTOM || type == 0) {
            type = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), type);
        Graphics2D g = copy.createGraphics();
        try {
            g.drawImage(source, 0, 0, null);
        } finally {
            g.dispose();
        }
        return copy;
    }

    /**
     * Configure graphics for high quality rendering
     * 配置图形以获得高质量渲染
     */
    private static void configureGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}
