package cloud.opencode.base.image.internal;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Rotate Operation
 * 旋转操作
 *
 * <p>Internal utility for image rotation and flip operations.</p>
 * <p>图片旋转和翻转操作的内部工具类。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Image rotation and flip transformations - 图片旋转和翻转变换</li>
 *   <li>Internal utility, not part of public API - 内部工具，非公共 API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Internal usage
 * BufferedImage rotated = RotateOp.rotate(image, 90);
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
public final class RotateOp {

    private RotateOp() {
        // Utility class
    }

    /**
     * Rotate by degrees
     * 按角度旋转
     *
     * @param image the source image | 源图片
     * @param degrees rotation degrees | 旋转角度
     * @return the rotated image | 旋转后的图片
     */
    public static BufferedImage rotate(BufferedImage image, double degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int srcWidth = image.getWidth();
        int srcHeight = image.getHeight();

        // Calculate new dimensions
        int newWidth = (int) Math.floor(srcWidth * cos + srcHeight * sin);
        int newHeight = (int) Math.floor(srcHeight * cos + srcWidth * sin);

        BufferedImage result = new BufferedImage(newWidth, newHeight, getTargetType(image));
        Graphics2D g = result.createGraphics();
        try {
            configureGraphics(g);

            // Translate to center, rotate, then translate back
            AffineTransform transform = new AffineTransform();
            transform.translate((newWidth - srcWidth) / 2.0, (newHeight - srcHeight) / 2.0);
            transform.rotate(radians, srcWidth / 2.0, srcHeight / 2.0);

            g.setTransform(transform);
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Rotate 90 degrees clockwise
     * 顺时针旋转90度
     *
     * @param image the source image | 源图片
     * @return the rotated image | 旋转后的图片
     */
    public static BufferedImage rotate90(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(height, width, getTargetType(image));
        Graphics2D g = result.createGraphics();
        try {
            configureGraphics(g);
            g.translate(height, 0);
            g.rotate(Math.PI / 2);
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Rotate 180 degrees
     * 旋转180度
     *
     * @param image the source image | 源图片
     * @return the rotated image | 旋转后的图片
     */
    public static BufferedImage rotate180(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, getTargetType(image));
        Graphics2D g = result.createGraphics();
        try {
            configureGraphics(g);
            g.translate(width, height);
            g.rotate(Math.PI);
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Rotate 270 degrees (90 counter-clockwise)
     * 旋转270度（逆时针90度）
     *
     * @param image the source image | 源图片
     * @return the rotated image | 旋转后的图片
     */
    public static BufferedImage rotate270(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(height, width, getTargetType(image));
        Graphics2D g = result.createGraphics();
        try {
            configureGraphics(g);
            g.translate(0, width);
            g.rotate(-Math.PI / 2);
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Flip horizontally (mirror)
     * 水平翻转（镜像）
     *
     * @param image the source image | 源图片
     * @return the flipped image | 翻转后的图片
     */
    public static BufferedImage flipHorizontal(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, getTargetType(image));
        Graphics2D g = result.createGraphics();
        try {
            configureGraphics(g);
            g.drawImage(image, width, 0, -width, height, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Flip vertically
     * 垂直翻转
     *
     * @param image the source image | 源图片
     * @return the flipped image | 翻转后的图片
     */
    public static BufferedImage flipVertical(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, getTargetType(image));
        Graphics2D g = result.createGraphics();
        try {
            configureGraphics(g);
            g.drawImage(image, 0, height, width, -height, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Rotate and flip based on EXIF orientation
     * 根据EXIF方向旋转和翻转
     *
     * @param image the source image | 源图片
     * @param orientation EXIF orientation (1-8) | EXIF方向（1-8）
     * @return the corrected image | 校正后的图片
     */
    public static BufferedImage applyExifOrientation(BufferedImage image, int orientation) {
        return switch (orientation) {
            case 1 -> image; // Normal
            case 2 -> flipHorizontal(image); // Flipped horizontally
            case 3 -> rotate180(image); // Rotated 180
            case 4 -> flipVertical(image); // Flipped vertically
            case 5 -> flipHorizontal(rotate90(image)); // Rotated 90 CW and flipped horizontally
            case 6 -> rotate90(image); // Rotated 90 CW
            case 7 -> flipHorizontal(rotate270(image)); // Rotated 90 CCW and flipped horizontally
            case 8 -> rotate270(image); // Rotated 90 CCW
            default -> image;
        };
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
