package cloud.opencode.base.pdf.content;

/**
 * PDF Line Element - Line graphic
 * PDF 线条元素 - 线条图形
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Line from point to point - 从一点到另一点的线条</li>
 *   <li>Line width and color customization - 线宽和颜色自定义</li>
 *   <li>Line style (solid, dashed, dotted) - 线条样式（实线、虚线、点线）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfLine line = PdfLine.of(50, 700, 550, 700)
 *     .lineWidth(2f)
 *     .color(PdfColor.RED)
 *     .style(LineStyle.DASHED);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — mutable builder pattern - 线程安全: 否 — 可变构建器模式</li>
 *   <li>Null-safe: No — callers must ensure non-null values - 空值安全: 否 — 调用方需确保非空值</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public final class PdfLine implements PdfElement {

    private float x1;
    private float y1;
    private float x2;
    private float y2;
    private float lineWidth = 1f;
    private PdfColor color = PdfColor.BLACK;
    private LineStyle style = LineStyle.SOLID;

    private PdfLine() {}

    /**
     * Creates a line from point to point
     * 从一点到另一点创建线条
     *
     * @param x1 start x | 起点 x
     * @param y1 start y | 起点 y
     * @param x2 end x | 终点 x
     * @param y2 end y | 终点 y
     * @return PdfLine instance | PdfLine 实例
     */
    public static PdfLine of(float x1, float y1, float x2, float y2) {
        PdfLine line = new PdfLine();
        line.x1 = x1;
        line.y1 = y1;
        line.x2 = x2;
        line.y2 = y2;
        return line;
    }

    /**
     * Creates a line builder
     * 创建线条构建器
     *
     * @return new PdfLine instance | 新 PdfLine 实例
     */
    public static PdfLine builder() {
        return new PdfLine();
    }

    public PdfLine from(float x, float y) {
        this.x1 = x;
        this.y1 = y;
        return this;
    }

    public PdfLine to(float x, float y) {
        this.x2 = x;
        this.y2 = y;
        return this;
    }

    public PdfLine lineWidth(float width) {
        this.lineWidth = width;
        return this;
    }

    public PdfLine color(PdfColor color) {
        this.color = color;
        return this;
    }

    public PdfLine style(LineStyle style) {
        this.style = style;
        return this;
    }

    @Override
    public float getX() {
        return x1;
    }

    @Override
    public float getY() {
        return y1;
    }

    public float getX1() {
        return x1;
    }

    public float getY1() {
        return y1;
    }

    public float getX2() {
        return x2;
    }

    public float getY2() {
        return y2;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public PdfColor getColor() {
        return color;
    }

    public LineStyle getStyle() {
        return style;
    }

    /**
     * Line Style
     * 线条样式
     */
    public enum LineStyle {
        /** Solid line | 实线 */
        SOLID,
        /** Dashed line | 虚线 */
        DASHED,
        /** Dotted line | 点线 */
        DOTTED
    }
}
