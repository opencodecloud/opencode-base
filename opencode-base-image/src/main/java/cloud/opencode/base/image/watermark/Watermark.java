package cloud.opencode.base.image.watermark;

import cloud.opencode.base.image.Position;

/**
 * Watermark
 * 水印接口
 *
 * <p>Base interface for all watermark types.</p>
 * <p>所有水印类型的基础接口。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed interface for watermark types (text and image) - 水印类型的密封接口（文字和图片）</li>
 *   <li>Position, opacity, and margin configuration - 位置、透明度和边距配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use text watermark
 * Watermark wm = TextWatermark.of("Copyright 2024");
 * 
 * // Use image watermark
 * Watermark wm = ImageWatermark.of(logoImage, Position.BOTTOM_RIGHT);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable implementations) - 线程安全: 是（不可变实现）</li>
 *   <li>Null-safe: No (text/image must not be null) - 空值安全: 否（文字/图片不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public sealed interface Watermark permits TextWatermark, ImageWatermark {

    /**
     * Get watermark position
     * 获取水印位置
     *
     * @return the position | 位置
     */
    Position position();

    /**
     * Get watermark opacity
     * 获取水印透明度
     *
     * @return the opacity (0.0 to 1.0) | 透明度（0.0到1.0）
     */
    float opacity();

    /**
     * Get margin from edge
     * 获取边缘间距
     *
     * @return the margin in pixels | 边距（像素）
     */
    int margin();
}
