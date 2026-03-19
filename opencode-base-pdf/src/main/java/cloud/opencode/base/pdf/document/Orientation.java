package cloud.opencode.base.pdf.document;

/**
 * Page Orientation - Portrait or Landscape
 * 页面方向 - 纵向或横向
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Portrait and landscape orientation constants - 纵向和横向方向常量</li>
 *   <li>Dimension adjustment based on orientation - 根据方向调整尺寸</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Orientation orient = Orientation.LANDSCAPE;
 * float[] dims = orient.apply(595, 842); // swaps to [842, 595]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — immutable enum - 线程安全: 是 — 不可变枚举</li>
 *   <li>Null-safe: N/A — enum values are never null - 空值安全: 不适用 — 枚举值不会为空</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public enum Orientation {

    /**
     * Portrait orientation (height > width)
     * 纵向（高度 > 宽度）
     */
    PORTRAIT,

    /**
     * Landscape orientation (width > height)
     * 横向（宽度 > 高度）
     */
    LANDSCAPE;

    /**
     * Checks if this is portrait orientation
     * 检查是否为纵向
     *
     * @return true if portrait | 如果纵向返回 true
     */
    public boolean isPortrait() {
        return this == PORTRAIT;
    }

    /**
     * Checks if this is landscape orientation
     * 检查是否为横向
     *
     * @return true if landscape | 如果横向返回 true
     */
    public boolean isLandscape() {
        return this == LANDSCAPE;
    }

    /**
     * Applies orientation to page dimensions
     * 将方向应用于页面尺寸
     *
     * @param width  original width | 原始宽度
     * @param height original height | 原始高度
     * @return adjusted dimensions [width, height] | 调整后的尺寸 [宽, 高]
     */
    public float[] apply(float width, float height) {
        if (this == LANDSCAPE && height > width) {
            return new float[]{height, width};
        } else if (this == PORTRAIT && width > height) {
            return new float[]{height, width};
        }
        return new float[]{width, height};
    }
}
