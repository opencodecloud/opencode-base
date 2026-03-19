package cloud.opencode.base.pdf.content;

/**
 * PDF Ellipse Element - Ellipse/Circle graphic
 * PDF 椭圆元素 - 椭圆/圆形图形
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Circle and ellipse creation - 圆形和椭圆创建</li>
 *   <li>Stroke and fill color configuration - 描边和填充颜色配置</li>
 *   <li>Stroke width customization - 描边宽度自定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfEllipse circle = PdfEllipse.circle(200, 400, 50)
 *     .fillColor(PdfColor.BLUE)
 *     .strokeWidth(2f);
 *
 * PdfEllipse ellipse = PdfEllipse.of(300, 500, 80, 40);
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
public final class PdfEllipse implements PdfElement {

    private float centerX;
    private float centerY;
    private float radiusX;
    private float radiusY;
    private PdfColor strokeColor = PdfColor.BLACK;
    private PdfColor fillColor;
    private float strokeWidth = 1f;

    private PdfEllipse() {}

    /**
     * Creates a circle
     * 创建圆形
     *
     * @param centerX center x | 中心点 x
     * @param centerY center y | 中心点 y
     * @param radius  radius | 半径
     * @return PdfEllipse instance | PdfEllipse 实例
     */
    public static PdfEllipse circle(float centerX, float centerY, float radius) {
        PdfEllipse ellipse = new PdfEllipse();
        ellipse.centerX = centerX;
        ellipse.centerY = centerY;
        ellipse.radiusX = radius;
        ellipse.radiusY = radius;
        return ellipse;
    }

    /**
     * Creates an ellipse
     * 创建椭圆
     *
     * @param centerX center x | 中心点 x
     * @param centerY center y | 中心点 y
     * @param radiusX x radius | x 半径
     * @param radiusY y radius | y 半径
     * @return PdfEllipse instance | PdfEllipse 实例
     */
    public static PdfEllipse of(float centerX, float centerY, float radiusX, float radiusY) {
        PdfEllipse ellipse = new PdfEllipse();
        ellipse.centerX = centerX;
        ellipse.centerY = centerY;
        ellipse.radiusX = radiusX;
        ellipse.radiusY = radiusY;
        return ellipse;
    }

    /**
     * Creates an ellipse builder
     * 创建椭圆构建器
     *
     * @return new PdfEllipse instance | 新 PdfEllipse 实例
     */
    public static PdfEllipse builder() {
        return new PdfEllipse();
    }

    public PdfEllipse center(float x, float y) {
        this.centerX = x;
        this.centerY = y;
        return this;
    }

    public PdfEllipse radius(float radiusX, float radiusY) {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        return this;
    }

    public PdfEllipse radius(float radius) {
        this.radiusX = radius;
        this.radiusY = radius;
        return this;
    }

    public PdfEllipse strokeColor(PdfColor color) {
        this.strokeColor = color;
        return this;
    }

    public PdfEllipse fillColor(PdfColor color) {
        this.fillColor = color;
        return this;
    }

    public PdfEllipse strokeWidth(float width) {
        this.strokeWidth = width;
        return this;
    }

    @Override
    public float getX() {
        return centerX - radiusX;
    }

    @Override
    public float getY() {
        return centerY - radiusY;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public float getRadiusX() {
        return radiusX;
    }

    public float getRadiusY() {
        return radiusY;
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

    public boolean isFilled() {
        return fillColor != null;
    }

    public boolean isCircle() {
        return radiusX == radiusY;
    }
}
