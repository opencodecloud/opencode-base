package cloud.opencode.base.image.internal;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Overlay/Composite Operation
 * 图片叠加/合成操作
 *
 * <p>Internal utility for overlaying one image on top of another
 * with configurable position and opacity.</p>
 * <p>将一张图片以指定位置和透明度叠加到另一张图片上的内部工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Image overlay at specified position - 在指定位置叠加图片</li>
 *   <li>Configurable opacity (0.0 to 1.0) - 可配置透明度（0.0 到 1.0）</li>
 *   <li>Internal utility, not part of public API - 内部工具，非公共 API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Internal usage
 * BufferedImage result = OverlayOp.overlay(base, overlay, 100, 50, 0.5f);
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
 * @since JDK 25, opencode-base-image V1.0.3
 */
public final class OverlayOp {

    private OverlayOp() {
        // Utility class
    }

    /**
     * Overlay an image on top of a base image at the specified position and opacity
     * 在指定位置以指定透明度将一张图片叠加到底图上
     *
     * <p>The result image has the same dimensions as the base image.
     * The overlay image may extend beyond the base image boundaries
     * (excess portions are clipped).</p>
     * <p>结果图片与底图尺寸相同。叠加图片可以超出底图边界（超出部分被裁剪）。</p>
     *
     * @param base the base image | 底图
     * @param overlay the overlay image | 叠加图片
     * @param x the x position for overlay | 叠加的 X 坐标
     * @param y the y position for overlay | 叠加的 Y 坐标
     * @param opacity the opacity from 0.0 (transparent) to 1.0 (opaque) | 透明度，0.0（完全透明）到 1.0（完全不透明）
     * @return the composited image | 合成后的图片
     * @throws NullPointerException if base or overlay is null | 如果底图或叠加图片为 null
     * @throws IllegalArgumentException if opacity is not in [0.0, 1.0] | 如果透明度不在 [0.0, 1.0] 范围内
     */
    public static BufferedImage overlay(BufferedImage base, BufferedImage overlay, int x, int y, float opacity) {
        Objects.requireNonNull(base, "Base image must not be null");
        Objects.requireNonNull(overlay, "Overlay image must not be null");
        if (Float.isNaN(opacity) || opacity < 0.0f || opacity > 1.0f) {
            throw new IllegalArgumentException("Opacity must be in [0.0, 1.0], got: " + opacity);
        }

        BufferedImage result = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        try {
            // Draw base image
            g.drawImage(base, 0, 0, null);

            // Draw overlay with opacity
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g.drawImage(overlay, x, y, null);
        } finally {
            g.dispose();
        }
        return result;
    }
}
