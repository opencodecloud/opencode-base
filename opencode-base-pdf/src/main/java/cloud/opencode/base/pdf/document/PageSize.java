package cloud.opencode.base.pdf.document;

/**
 * PDF Page Size - Standard page dimensions
 * PDF 页面大小 - 标准页面尺寸
 *
 * <p>Provides standard page sizes in points (1 inch = 72 points).</p>
 * <p>提供以点为单位的标准页面大小（1英寸 = 72点）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ISO A and B series page sizes - ISO A 和 B 系列页面大小</li>
 *   <li>US standard sizes (Letter, Legal, Tabloid) - 美国标准尺寸</li>
 *   <li>Dimensions in points, millimeters, and inches - 点、毫米和英寸单位的尺寸</li>
 *   <li>Custom page size support - 自定义页面大小支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PageSize size = PageSize.A4;
 * float width = size.getWidth();         // 595 points
 * float widthMm = size.getWidthMm();     // ~210 mm
 * float[] custom = PageSize.customMm(100, 200);
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
public enum PageSize {

    // ==================== ISO A Series | ISO A 系列 ====================

    /** A0: 841 x 1189 mm */
    A0(2384, 3370),

    /** A1: 594 x 841 mm */
    A1(1684, 2384),

    /** A2: 420 x 594 mm */
    A2(1191, 1684),

    /** A3: 297 x 420 mm */
    A3(842, 1191),

    /** A4: 210 x 297 mm (most common) | A4: 210 x 297 毫米（最常用）*/
    A4(595, 842),

    /** A5: 148 x 210 mm */
    A5(420, 595),

    /** A6: 105 x 148 mm */
    A6(298, 420),

    // ==================== ISO B Series | ISO B 系列 ====================

    /** B4: 250 x 353 mm */
    B4(709, 1001),

    /** B5: 176 x 250 mm */
    B5(499, 709),

    // ==================== US Sizes | 美国尺寸 ====================

    /** Letter: 8.5 x 11 inches */
    LETTER(612, 792),

    /** Legal: 8.5 x 14 inches */
    LEGAL(612, 1008),

    /** Tabloid/Ledger: 11 x 17 inches */
    TABLOID(792, 1224),

    /** Executive: 7.25 x 10.5 inches */
    EXECUTIVE(522, 756),

    // ==================== Other Sizes | 其他尺寸 ====================

    /** Postcard: 100 x 148 mm */
    POSTCARD(283, 420);

    private final float width;
    private final float height;

    PageSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Gets width in points
     * 获取宽度（点）
     *
     * @return width | 宽度
     */
    public float getWidth() {
        return width;
    }

    /**
     * Gets height in points
     * 获取高度（点）
     *
     * @return height | 高度
     */
    public float getHeight() {
        return height;
    }

    /**
     * Gets width in millimeters
     * 获取宽度（毫米）
     *
     * @return width in mm | 宽度（毫米）
     */
    public float getWidthMm() {
        return width * 25.4f / 72f;
    }

    /**
     * Gets height in millimeters
     * 获取高度（毫米）
     *
     * @return height in mm | 高度（毫米）
     */
    public float getHeightMm() {
        return height * 25.4f / 72f;
    }

    /**
     * Gets width in inches
     * 获取宽度（英寸）
     *
     * @return width in inches | 宽度（英寸）
     */
    public float getWidthInches() {
        return width / 72f;
    }

    /**
     * Gets height in inches
     * 获取高度（英寸）
     *
     * @return height in inches | 高度（英寸）
     */
    public float getHeightInches() {
        return height / 72f;
    }

    /**
     * Creates a rotated (landscape) version of this page size
     * 创建旋转（横向）版本的页面大小
     *
     * @return rotated dimensions [width, height] | 旋转后的尺寸 [宽, 高]
     */
    public float[] rotate() {
        return new float[]{height, width};
    }

    /**
     * Creates a custom page size
     * 创建自定义页面大小
     *
     * @param widthPt  width in points | 宽度（点）
     * @param heightPt height in points | 高度（点）
     * @return dimensions array [width, height] | 尺寸数组 [宽, 高]
     */
    public static float[] custom(float widthPt, float heightPt) {
        return new float[]{widthPt, heightPt};
    }

    /**
     * Creates a custom page size from millimeters
     * 从毫米创建自定义页面大小
     *
     * @param widthMm  width in mm | 宽度（毫米）
     * @param heightMm height in mm | 高度（毫米）
     * @return dimensions array [width, height] in points | 尺寸数组 [宽, 高]（点）
     */
    public static float[] customMm(float widthMm, float heightMm) {
        return new float[]{widthMm * 72f / 25.4f, heightMm * 72f / 25.4f};
    }

    /**
     * Creates a custom page size from inches
     * 从英寸创建自定义页面大小
     *
     * @param widthIn  width in inches | 宽度（英寸）
     * @param heightIn height in inches | 高度（英寸）
     * @return dimensions array [width, height] in points | 尺寸数组 [宽, 高]（点）
     */
    public static float[] customInches(float widthIn, float heightIn) {
        return new float[]{widthIn * 72f, heightIn * 72f};
    }
}
