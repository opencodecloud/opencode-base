package cloud.opencode.base.image.draw;

import cloud.opencode.base.image.exception.ImageOperationException;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Drawing Operations for BufferedImage
 * BufferedImage 绘图操作工具
 *
 * <p>Provides static methods to draw geometric shapes, lines, arrows, and text
 * onto images. All methods return a NEW BufferedImage and never modify the input.</p>
 * <p>提供在图像上绘制几何形状、线条、箭头和文本的静态方法。
 * 所有方法返回新的 BufferedImage，不修改输入图像。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Draw lines with configurable thickness - 绘制可配置粗细的线条</li>
 *   <li>Draw rectangles (outline or filled) - 绘制矩形（轮廓或填充）</li>
 *   <li>Draw circles and ellipses (outline or filled) - 绘制圆和椭圆（轮廓或填充）</li>
 *   <li>Draw polygons (outline or filled) - 绘制多边形（轮廓或填充）</li>
 *   <li>Draw arrows with arrowheads - 绘制带箭头的线条</li>
 *   <li>Draw text with configurable font and color - 绘制可配置字体和颜色的文本</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Draw a red line
 * BufferedImage result = DrawOp.line(image, 10, 10, 100, 100, Color.RED, 2);
 *
 * // Draw a filled blue rectangle
 * BufferedImage result = DrawOp.rect(image, 20, 20, 80, 60, Color.BLUE, 1, true);
 *
 * // Draw a green circle outline
 * BufferedImage result = DrawOp.circle(image, 50, 50, 30, Color.GREEN, 2, false);
 *
 * // Draw text
 * BufferedImage result = DrawOp.text(image, "Hello", 10, 30, new Font("Arial", Font.PLAIN, 16), Color.BLACK);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, all methods are pure functions) - 线程安全: 是（无状态，所有方法均为纯函数）</li>
 *   <li>Null-safe: No (null parameters throw IllegalArgumentException) - 空值安全: 否（null 参数抛出 IllegalArgumentException）</li>
 *   <li>Input immutability: All methods copy the input image - 输入不可变性: 所有方法复制输入图像</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class DrawOp {

    private DrawOp() {
        // utility class
    }

    /**
     * Draw a line from (x1, y1) to (x2, y2)
     * 从 (x1, y1) 到 (x2, y2) 绘制线条
     *
     * @param image     the source image | 源图像
     * @param x1        start x coordinate | 起点 x 坐标
     * @param y1        start y coordinate | 起点 y 坐标
     * @param x2        end x coordinate | 终点 x 坐标
     * @param y2        end y coordinate | 终点 y 坐标
     * @param color     the line color | 线条颜色
     * @param thickness the line thickness in pixels | 线条粗细（像素）
     * @return a new BufferedImage with the line drawn | 绘制了线条的新 BufferedImage
     * @throws ImageOperationException if image is null, color is null, or thickness &lt;= 0
     */
    public static BufferedImage line(BufferedImage image, int x1, int y1, int x2, int y2,
                                     Color color, int thickness) {
        validateBase(image, color, thickness);
        BufferedImage copy = copyImage(image);
        Graphics2D g = createGraphics(copy, color, thickness);
        try {
            g.drawLine(x1, y1, x2, y2);
        } finally {
            g.dispose();
        }
        return copy;
    }

    /**
     * Draw a rectangle (outline or filled)
     * 绘制矩形（轮廓或填充）
     *
     * @param image     the source image | 源图像
     * @param x         top-left x coordinate | 左上角 x 坐标
     * @param y         top-left y coordinate | 左上角 y 坐标
     * @param w         width | 宽度
     * @param h         height | 高度
     * @param color     the rectangle color | 矩形颜色
     * @param thickness the stroke thickness in pixels | 线条粗细（像素）
     * @param fill      true to fill, false for outline | true 填充, false 仅轮廓
     * @return a new BufferedImage with the rectangle drawn | 绘制了矩形的新 BufferedImage
     * @throws ImageOperationException if image is null, color is null, or thickness &lt;= 0
     */
    public static BufferedImage rect(BufferedImage image, int x, int y, int w, int h,
                                     Color color, int thickness, boolean fill) {
        validateBase(image, color, thickness);
        BufferedImage copy = copyImage(image);
        Graphics2D g = createGraphics(copy, color, thickness);
        try {
            if (fill) {
                g.fillRect(x, y, w, h);
            } else {
                g.drawRect(x, y, w, h);
            }
        } finally {
            g.dispose();
        }
        return copy;
    }

    /**
     * Draw a circle (outline or filled)
     * 绘制圆形（轮廓或填充）
     *
     * @param image     the source image | 源图像
     * @param cx        center x coordinate | 圆心 x 坐标
     * @param cy        center y coordinate | 圆心 y 坐标
     * @param radius    the circle radius | 圆的半径
     * @param color     the circle color | 圆的颜色
     * @param thickness the stroke thickness in pixels | 线条粗细（像素）
     * @param fill      true to fill, false for outline | true 填充, false 仅轮廓
     * @return a new BufferedImage with the circle drawn | 绘制了圆形的新 BufferedImage
     * @throws ImageOperationException if image is null, color is null, or thickness &lt;= 0
     */
    public static BufferedImage circle(BufferedImage image, int cx, int cy, int radius,
                                       Color color, int thickness, boolean fill) {
        validateBase(image, color, thickness);
        BufferedImage copy = copyImage(image);
        Graphics2D g = createGraphics(copy, color, thickness);
        try {
            int x = cx - radius;
            int y = cy - radius;
            int d = radius * 2;
            if (fill) {
                g.fillOval(x, y, d, d);
            } else {
                g.drawOval(x, y, d, d);
            }
        } finally {
            g.dispose();
        }
        return copy;
    }

    /**
     * Draw an ellipse (outline or filled)
     * 绘制椭圆（轮廓或填充）
     *
     * @param image     the source image | 源图像
     * @param cx        center x coordinate | 椭圆中心 x 坐标
     * @param cy        center y coordinate | 椭圆中心 y 坐标
     * @param rx        horizontal radius | 水平半径
     * @param ry        vertical radius | 垂直半径
     * @param color     the ellipse color | 椭圆颜色
     * @param thickness the stroke thickness in pixels | 线条粗细（像素）
     * @param fill      true to fill, false for outline | true 填充, false 仅轮廓
     * @return a new BufferedImage with the ellipse drawn | 绘制了椭圆的新 BufferedImage
     * @throws ImageOperationException if image is null, color is null, or thickness &lt;= 0
     */
    public static BufferedImage ellipse(BufferedImage image, int cx, int cy, int rx, int ry,
                                        Color color, int thickness, boolean fill) {
        validateBase(image, color, thickness);
        BufferedImage copy = copyImage(image);
        Graphics2D g = createGraphics(copy, color, thickness);
        try {
            int x = cx - rx;
            int y = cy - ry;
            int w = rx * 2;
            int h = ry * 2;
            if (fill) {
                g.fillOval(x, y, w, h);
            } else {
                g.drawOval(x, y, w, h);
            }
        } finally {
            g.dispose();
        }
        return copy;
    }

    /**
     * Draw a polygon (outline or filled)
     * 绘制多边形（轮廓或填充）
     *
     * @param image     the source image | 源图像
     * @param xPoints   array of x coordinates | x 坐标数组
     * @param yPoints   array of y coordinates | y 坐标数组
     * @param color     the polygon color | 多边形颜色
     * @param thickness the stroke thickness in pixels | 线条粗细（像素）
     * @param fill      true to fill, false for outline | true 填充, false 仅轮廓
     * @return a new BufferedImage with the polygon drawn | 绘制了多边形的新 BufferedImage
     * @throws ImageOperationException if image is null, color is null, thickness &lt;= 0,
     *                                  or point arrays are null/mismatched
     */
    public static BufferedImage polygon(BufferedImage image, int[] xPoints, int[] yPoints,
                                        Color color, int thickness, boolean fill) {
        validateBase(image, color, thickness);
        Objects.requireNonNull(xPoints, "xPoints must not be null");
        Objects.requireNonNull(yPoints, "yPoints must not be null");
        if (xPoints.length != yPoints.length) {
            throw new ImageOperationException(
                    "xPoints and yPoints must have the same length, got " +
                            xPoints.length + " and " + yPoints.length, "polygon");
        }
        if (xPoints.length < 3) {
            throw new ImageOperationException(
                    "Polygon requires at least 3 points, got " + xPoints.length, "polygon");
        }
        BufferedImage copy = copyImage(image);
        Graphics2D g = createGraphics(copy, color, thickness);
        try {
            int n = xPoints.length;
            if (fill) {
                g.fillPolygon(xPoints, yPoints, n);
            } else {
                g.drawPolygon(xPoints, yPoints, n);
            }
        } finally {
            g.dispose();
        }
        return copy;
    }

    /**
     * Draw an arrow from (x1, y1) to (x2, y2) with an arrowhead at the endpoint
     * 从 (x1, y1) 到 (x2, y2) 绘制带箭头的线条
     *
     * @param image     the source image | 源图像
     * @param x1        start x coordinate | 起点 x 坐标
     * @param y1        start y coordinate | 起点 y 坐标
     * @param x2        end x coordinate (arrowhead) | 终点 x 坐标（箭头端）
     * @param y2        end y coordinate (arrowhead) | 终点 y 坐标（箭头端）
     * @param color     the arrow color | 箭头颜色
     * @param thickness the line thickness in pixels | 线条粗细（像素）
     * @return a new BufferedImage with the arrow drawn | 绘制了箭头的新 BufferedImage
     * @throws ImageOperationException if image is null, color is null, or thickness &lt;= 0
     */
    public static BufferedImage arrow(BufferedImage image, int x1, int y1, int x2, int y2,
                                      Color color, int thickness) {
        validateBase(image, color, thickness);
        BufferedImage copy = copyImage(image);
        Graphics2D g = createGraphics(copy, color, thickness);
        try {
            // Draw the main line
            g.drawLine(x1, y1, x2, y2);

            // Draw arrowhead: two short lines at ~30 degrees from the endpoint
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int arrowSize = Math.max(thickness * 4, 10);
            double arrowAngle = Math.toRadians(30);

            int ax1 = (int) Math.round(x2 - arrowSize * Math.cos(angle - arrowAngle));
            int ay1 = (int) Math.round(y2 - arrowSize * Math.sin(angle - arrowAngle));
            int ax2 = (int) Math.round(x2 - arrowSize * Math.cos(angle + arrowAngle));
            int ay2 = (int) Math.round(y2 - arrowSize * Math.sin(angle + arrowAngle));

            g.drawLine(x2, y2, ax1, ay1);
            g.drawLine(x2, y2, ax2, ay2);
        } finally {
            g.dispose();
        }
        return copy;
    }

    /**
     * Draw text at the specified position
     * 在指定位置绘制文本
     *
     * @param image the source image | 源图像
     * @param text  the text to draw | 要绘制的文本
     * @param x     the x coordinate of the text baseline start | 文本基线起始 x 坐标
     * @param y     the y coordinate of the text baseline | 文本基线 y 坐标
     * @param font  the font to use | 使用的字体
     * @param color the text color | 文本颜色
     * @return a new BufferedImage with the text drawn | 绘制了文本的新 BufferedImage
     * @throws ImageOperationException if image, text, font, or color is null
     */
    public static BufferedImage text(BufferedImage image, String text, int x, int y,
                                     Font font, Color color) {
        if (image == null) {
            throw new ImageOperationException("Image must not be null", "text");
        }
        if (text == null) {
            throw new ImageOperationException("Text must not be null", "text");
        }
        if (font == null) {
            throw new ImageOperationException("Font must not be null", "text");
        }
        if (color == null) {
            throw new ImageOperationException("Color must not be null", "text");
        }
        BufferedImage copy = copyImage(image);
        Graphics2D g = copy.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(color);
            g.setFont(font);
            g.drawString(text, x, y);
        } finally {
            g.dispose();
        }
        return copy;
    }

    // ─── Internal helpers ──────────────────────────────────────────────

    /**
     * Validate common parameters for draw operations
     * 验证绘图操作的通用参数
     */
    private static void validateBase(BufferedImage image, Color color, int thickness) {
        if (image == null) {
            throw new ImageOperationException("Image must not be null", "draw");
        }
        if (color == null) {
            throw new ImageOperationException("Color must not be null", "draw");
        }
        if (thickness <= 0) {
            throw new ImageOperationException(
                    "Thickness must be positive, got " + thickness, "draw");
        }
    }

    /**
     * Create a deep copy of the input image
     * 创建输入图像的深拷贝
     */
    private static BufferedImage copyImage(BufferedImage source) {
        int type = source.getType();
        if (type == BufferedImage.TYPE_CUSTOM) {
            type = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage copy = new BufferedImage(
                source.getWidth(), source.getHeight(), type);
        Graphics2D g = copy.createGraphics();
        try {
            g.drawImage(source, 0, 0, null);
        } finally {
            g.dispose();
        }
        return copy;
    }

    /**
     * Create a configured Graphics2D with color, stroke, and anti-aliasing
     * 创建配置了颜色、笔触和抗锯齿的 Graphics2D
     */
    private static Graphics2D createGraphics(BufferedImage image, Color color, int thickness) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        return g;
    }
}
