package cloud.opencode.base.image.internal;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Resize Operation
 * 调整尺寸操作
 *
 * <p>Internal utility for image resizing operations.</p>
 * <p>图片调整尺寸操作的内部工具类。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Image dimension resizing with quality hints - 带质量提示的图片尺寸调整</li>
 *   <li>Internal utility, not part of public API - 内部工具，非公共 API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Internal usage
 * BufferedImage resized = ResizeOp.resize(image, 800, 600);
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
public final class ResizeOp {

    private ResizeOp() {
        // Utility class
    }

    /**
     * Resize to exact dimensions
     * 调整到精确尺寸
     *
     * @param image the source image | 源图片
     * @param width target width | 目标宽度
     * @param height target height | 目标高度
     * @return the resized image | 调整后的图片
     */
    public static BufferedImage resize(BufferedImage image, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }

        BufferedImage result = new BufferedImage(width, height, getTargetType(image));
        Graphics2D g = result.createGraphics();
        try {
            configureGraphics(g);
            g.drawImage(image, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Resize maintaining aspect ratio to fit within bounds
     * 保持宽高比调整尺寸以适应边界
     *
     * @param image the source image | 源图片
     * @param maxWidth maximum width | 最大宽度
     * @param maxHeight maximum height | 最大高度
     * @return the resized image | 调整后的图片
     */
    public static BufferedImage resizeToFit(BufferedImage image, int maxWidth, int maxHeight) {
        int srcWidth = image.getWidth();
        int srcHeight = image.getHeight();

        // Calculate scale factors
        double widthScale = (double) maxWidth / srcWidth;
        double heightScale = (double) maxHeight / srcHeight;
        double scale = Math.min(widthScale, heightScale);

        // Don't upscale
        if (scale >= 1.0) {
            return image;
        }

        int targetWidth = Math.max(1, (int) (srcWidth * scale));
        int targetHeight = Math.max(1, (int) (srcHeight * scale));

        return resize(image, targetWidth, targetHeight);
    }

    /**
     * Scale by factor
     * 按因子缩放
     *
     * @param image the source image | 源图片
     * @param scale scale factor | 缩放因子
     * @return the scaled image | 缩放后的图片
     */
    public static BufferedImage scale(BufferedImage image, double scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale must be positive");
        }

        int targetWidth = (int) (image.getWidth() * scale);
        int targetHeight = (int) (image.getHeight() * scale);

        return resize(image, Math.max(1, targetWidth), Math.max(1, targetHeight));
    }

    /**
     * Scale to fit width maintaining aspect ratio
     * 缩放以适应宽度（保持宽高比）
     *
     * @param image the source image | 源图片
     * @param width target width | 目标宽度
     * @return the scaled image | 缩放后的图片
     */
    public static BufferedImage scaleToWidth(BufferedImage image, int width) {
        double scale = (double) width / image.getWidth();
        int height = (int) (image.getHeight() * scale);
        return resize(image, width, Math.max(1, height));
    }

    /**
     * Scale to fit height maintaining aspect ratio
     * 缩放以适应高度（保持宽高比）
     *
     * @param image the source image | 源图片
     * @param height target height | 目标高度
     * @return the scaled image | 缩放后的图片
     */
    public static BufferedImage scaleToHeight(BufferedImage image, int height) {
        double scale = (double) height / image.getHeight();
        int width = (int) (image.getWidth() * scale);
        return resize(image, Math.max(1, width), height);
    }

    /**
     * Resize to cover area (may crop)
     * 调整尺寸以覆盖区域（可能裁剪）
     *
     * @param image the source image | 源图片
     * @param width target width | 目标宽度
     * @param height target height | 目标高度
     * @return the resized image | 调整后的图片
     */
    public static BufferedImage resizeToCover(BufferedImage image, int width, int height) {
        int srcWidth = image.getWidth();
        int srcHeight = image.getHeight();

        double widthScale = (double) width / srcWidth;
        double heightScale = (double) height / srcHeight;
        double scale = Math.max(widthScale, heightScale);

        int scaledWidth = (int) (srcWidth * scale);
        int scaledHeight = (int) (srcHeight * scale);

        BufferedImage scaled = resize(image, scaledWidth, scaledHeight);

        // Crop to exact dimensions from center
        int x = (scaledWidth - width) / 2;
        int y = (scaledHeight - height) / 2;

        // Create independent copy (getSubimage returns a view sharing pixel data)
        BufferedImage result = new BufferedImage(width, height, getTargetType(scaled));
        Graphics2D g = result.createGraphics();
        try {
            g.drawImage(scaled, -x, -y, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Configure graphics for high quality rendering
     * 配置图形以获得高质量渲染
     */
    private static void configureGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
