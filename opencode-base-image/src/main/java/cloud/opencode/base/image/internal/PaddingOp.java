package cloud.opencode.base.image.internal;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Padding and Border Operation
 * 内边距和边框操作
 *
 * <p>Internal utility for adding padding and borders around images.</p>
 * <p>图片添加内边距和边框的内部工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Uniform padding around all edges - 四周等距内边距</li>
 *   <li>Independent padding per edge - 四边独立内边距</li>
 *   <li>Border (equivalent to uniform padding) - 边框（等同于等距内边距）</li>
 *   <li>Internal utility, not part of public API - 内部工具，非公共 API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Internal usage
 * BufferedImage padded = PaddingOp.pad(image, 10, Color.WHITE);
 * BufferedImage bordered = PaddingOp.border(image, 2, Color.BLACK);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (throws on null image/color) - 空值安全: 否（null 图片/颜色抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
public final class PaddingOp {

    private PaddingOp() {
        // Utility class
    }

    /**
     * Add uniform padding around the image
     * 在图片四周添加等距内边距
     *
     * @param image the source image | 源图片
     * @param padding the padding size in pixels | 内边距大小（像素）
     * @param color the padding color | 内边距颜色
     * @return the padded image | 添加内边距后的图片
     * @throws NullPointerException if image or color is null | 如果图片或颜色为 null
     * @throws IllegalArgumentException if padding is negative | 如果内边距为负数
     */
    public static BufferedImage pad(BufferedImage image, int padding, Color color) {
        return pad(image, padding, padding, padding, padding, color);
    }

    /**
     * Add independent padding for each edge of the image
     * 为图片的每条边添加独立的内边距
     *
     * @param image the source image | 源图片
     * @param top the top padding in pixels | 顶部内边距（像素）
     * @param right the right padding in pixels | 右侧内边距（像素）
     * @param bottom the bottom padding in pixels | 底部内边距（像素）
     * @param left the left padding in pixels | 左侧内边距（像素）
     * @param color the padding color | 内边距颜色
     * @return the padded image | 添加内边距后的图片
     * @throws NullPointerException if image or color is null | 如果图片或颜色为 null
     * @throws IllegalArgumentException if any padding value is negative | 如果任何内边距值为负数
     */
    public static BufferedImage pad(BufferedImage image, int top, int right, int bottom, int left, Color color) {
        Objects.requireNonNull(image, "Image must not be null");
        Objects.requireNonNull(color, "Color must not be null");
        if (top < 0 || right < 0 || bottom < 0 || left < 0) {
            throw new IllegalArgumentException(
                    "Padding values must be >= 0, got: top=" + top +
                    ", right=" + right + ", bottom=" + bottom + ", left=" + left);
        }

        int newWidth = Math.addExact(Math.addExact(image.getWidth(), left), right);
        int newHeight = Math.addExact(Math.addExact(image.getHeight(), top), bottom);

        BufferedImage result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        try {
            g.setColor(color);
            g.fillRect(0, 0, newWidth, newHeight);
            g.drawImage(image, left, top, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Add a border around the image (equivalent to uniform padding)
     * 在图片外部添加边框（等同于等距内边距）
     *
     * @param image the source image | 源图片
     * @param thickness the border thickness in pixels | 边框厚度（像素）
     * @param color the border color | 边框颜色
     * @return the bordered image | 添加边框后的图片
     * @throws NullPointerException if image or color is null | 如果图片或颜色为 null
     * @throws IllegalArgumentException if thickness is negative | 如果厚度为负数
     */
    public static BufferedImage border(BufferedImage image, int thickness, Color color) {
        return pad(image, thickness, color);
    }
}
