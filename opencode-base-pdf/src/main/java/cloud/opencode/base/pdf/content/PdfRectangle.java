package cloud.opencode.base.pdf.content;

/**
 * PDF Rectangle Element - Rectangle graphic
 * PDF 矩形元素 - 矩形图形
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Rectangle with position and size - 带位置和大小的矩形</li>
 *   <li>Stroke and fill color configuration - 描边和填充颜色配置</li>
 *   <li>Rounded corner support - 圆角支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfRectangle rect = PdfRectangle.of(100, 500, 200, 100)
 *     .fillColor(PdfColor.LIGHT_GRAY)
 *     .strokeColor(PdfColor.BLACK)
 *     .cornerRadius(5f);
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
public final class PdfRectangle implements PdfElement {

    private float x;
    private float y;
    private float width;
    private float height;
    private PdfColor strokeColor = PdfColor.BLACK;
    private PdfColor fillColor;
    private float strokeWidth = 1f;
    private float cornerRadius;

    private PdfRectangle() {}

    /**
     * Creates a rectangle
     * 创建矩形
     *
     * @param x      x coordinate | x 坐标
     * @param y      y coordinate | y 坐标
     * @param width  width | 宽度
     * @param height height | 高度
     * @return PdfRectangle instance | PdfRectangle 实例
     */
    public static PdfRectangle of(float x, float y, float width, float height) {
        PdfRectangle rect = new PdfRectangle();
        rect.x = x;
        rect.y = y;
        rect.width = width;
        rect.height = height;
        return rect;
    }

    /**
     * Creates a rectangle builder
     * 创建矩形构建器
     *
     * @return new PdfRectangle instance | 新 PdfRectangle 实例
     */
    public static PdfRectangle builder() {
        return new PdfRectangle();
    }

    public PdfRectangle position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public PdfRectangle size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public PdfRectangle strokeColor(PdfColor color) {
        this.strokeColor = color;
        return this;
    }

    public PdfRectangle fillColor(PdfColor color) {
        this.fillColor = color;
        return this;
    }

    public PdfRectangle strokeWidth(float width) {
        this.strokeWidth = width;
        return this;
    }

    public PdfRectangle cornerRadius(float radius) {
        this.cornerRadius = radius;
        return this;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public PdfColor getStrokeColor() {
        return strokeColor;
    }

    public PdfColor getFillColor() {
        return fillColor;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public float getCornerRadius() {
        return cornerRadius;
    }

    public boolean isFilled() {
        return fillColor != null;
    }
}
