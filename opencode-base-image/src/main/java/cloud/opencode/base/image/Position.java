package cloud.opencode.base.image;

/**
 * Position
 * 位置枚举
 *
 * <p>Positions for watermark placement.</p>
 * <p>水印放置的位置。</p>
 *
 * <p><strong>Layout | 布局:</strong></p>
 * <pre>
 * +-------------+-------------+-------------+
 * | TOP_LEFT    | TOP_CENTER  | TOP_RIGHT   |
 * +-------------+-------------+-------------+
 * | CENTER_LEFT | CENTER      | CENTER_RIGHT|
 * +-------------+-------------+-------------+
 * | BOTTOM_LEFT |BOTTOM_CENTER| BOTTOM_RIGHT|
 * +-------------+-------------+-------------+
 * </pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Nine standard positions for watermark placement - 九个标准水印放置位置</li>
 *   <li>Grid-based layout (TOP/CENTER/BOTTOM x LEFT/CENTER/RIGHT) - 基于网格的布局（上/中/下 x 左/中/右）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use with watermark
 * TextWatermark watermark = TextWatermark.of("Copyright", Position.BOTTOM_RIGHT);
 * 
 * // Use with image watermark
 * ImageWatermark logo = ImageWatermark.of(logoImage, Position.TOP_LEFT);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public enum Position {

    /**
     * Top left corner | 左上角
     */
    TOP_LEFT,

    /**
     * Top center | 顶部居中
     */
    TOP_CENTER,

    /**
     * Top right corner | 右上角
     */
    TOP_RIGHT,

    /**
     * Center left | 左侧居中
     */
    CENTER_LEFT,

    /**
     * Center | 居中
     */
    CENTER,

    /**
     * Center right | 右侧居中
     */
    CENTER_RIGHT,

    /**
     * Bottom left corner | 左下角
     */
    BOTTOM_LEFT,

    /**
     * Bottom center | 底部居中
     */
    BOTTOM_CENTER,

    /**
     * Bottom right corner | 右下角
     */
    BOTTOM_RIGHT;

    /**
     * Check if position is on the left
     * 检查位置是否在左侧
     *
     * @return true if on the left | 如果在左侧返回true
     */
    public boolean isLeft() {
        return this == TOP_LEFT || this == CENTER_LEFT || this == BOTTOM_LEFT;
    }

    /**
     * Check if position is on the right
     * 检查位置是否在右侧
     *
     * @return true if on the right | 如果在右侧返回true
     */
    public boolean isRight() {
        return this == TOP_RIGHT || this == CENTER_RIGHT || this == BOTTOM_RIGHT;
    }

    /**
     * Check if position is at the top
     * 检查位置是否在顶部
     *
     * @return true if at the top | 如果在顶部返回true
     */
    public boolean isTop() {
        return this == TOP_LEFT || this == TOP_CENTER || this == TOP_RIGHT;
    }

    /**
     * Check if position is at the bottom
     * 检查位置是否在底部
     *
     * @return true if at the bottom | 如果在底部返回true
     */
    public boolean isBottom() {
        return this == BOTTOM_LEFT || this == BOTTOM_CENTER || this == BOTTOM_RIGHT;
    }

    /**
     * Check if position is centered horizontally
     * 检查位置是否水平居中
     *
     * @return true if centered horizontally | 如果水平居中返回true
     */
    public boolean isCenterHorizontal() {
        return this == TOP_CENTER || this == CENTER || this == BOTTOM_CENTER;
    }

    /**
     * Check if position is centered vertically
     * 检查位置是否垂直居中
     *
     * @return true if centered vertically | 如果垂直居中返回true
     */
    public boolean isCenterVertical() {
        return this == CENTER_LEFT || this == CENTER || this == CENTER_RIGHT;
    }
}
