package cloud.opencode.base.pdf.content;

/**
 * PDF Color - Color representation for PDF content
 * PDF 颜色 - PDF 内容的颜色表示
 *
 * <p>Supports RGB, CMYK, and grayscale color models.</p>
 * <p>支持 RGB、CMYK 和灰度颜色模型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RGB, RGBA, and grayscale color creation - RGB、RGBA 和灰度颜色创建</li>
 *   <li>Hex string parsing - 十六进制字符串解析</li>
 *   <li>Color manipulation (darker, lighter, alpha) - 颜色操作（加深、变浅、透明度）</li>
 *   <li>AWT Color interoperability - AWT Color 互操作</li>
 *   <li>Predefined color constants - 预定义颜色常量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfColor red = PdfColor.rgb(255, 0, 0);
 * PdfColor custom = PdfColor.hex("#3366CC");
 * PdfColor semiTransparent = PdfColor.rgba(0, 0, 0, 0.5f);
 * PdfColor darkRed = PdfColor.RED.darker(0.7f);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — immutable record - 线程安全: 是 — 不可变记录</li>
 *   <li>Null-safe: No — factory methods do not validate null inputs - 空值安全: 否 — 工厂方法不验证空输入</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 *
 * @param red red component (0-1) | 红色分量
 * @param green green component (0-1) | 绿色分量
 * @param blue blue component (0-1) | 蓝色分量
 * @param alpha alpha (0-1, 0=transparent, 1=opaque) | 透明度
 * @param model color model | 颜色模型
 */
public record PdfColor(float red, float green, float blue, float alpha, ColorModel model) {

    // ==================== Predefined Colors | 预定义颜色 ====================

    /** Black color | 黑色 */
    public static final PdfColor BLACK = rgb(0, 0, 0);

    /** White color | 白色 */
    public static final PdfColor WHITE = rgb(255, 255, 255);

    /** Red color | 红色 */
    public static final PdfColor RED = rgb(255, 0, 0);

    /** Green color | 绿色 */
    public static final PdfColor GREEN = rgb(0, 255, 0);

    /** Blue color | 蓝色 */
    public static final PdfColor BLUE = rgb(0, 0, 255);

    /** Gray color | 灰色 */
    public static final PdfColor GRAY = rgb(128, 128, 128);

    /** Light gray color | 浅灰色 */
    public static final PdfColor LIGHT_GRAY = rgb(192, 192, 192);

    /** Dark gray color | 深灰色 */
    public static final PdfColor DARK_GRAY = rgb(64, 64, 64);

    /** Yellow color | 黄色 */
    public static final PdfColor YELLOW = rgb(255, 255, 0);

    /** Cyan color | 青色 */
    public static final PdfColor CYAN = rgb(0, 255, 255);

    /** Magenta color | 品红色 */
    public static final PdfColor MAGENTA = rgb(255, 0, 255);

    /** Orange color | 橙色 */
    public static final PdfColor ORANGE = rgb(255, 200, 0);

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates RGB color from 0-255 values
     * 从 0-255 值创建 RGB 颜色
     *
     * @param r red component (0-255) | 红色分量
     * @param g green component (0-255) | 绿色分量
     * @param b blue component (0-255) | 蓝色分量
     * @return PdfColor instance | PdfColor 实例
     */
    public static PdfColor rgb(int r, int g, int b) {
        return new PdfColor(r / 255f, g / 255f, b / 255f, 1f, ColorModel.RGB);
    }

    /**
     * Creates RGB color from 0-1 values
     * 从 0-1 值创建 RGB 颜色
     *
     * @param r red component (0-1) | 红色分量
     * @param g green component (0-1) | 绿色分量
     * @param b blue component (0-1) | 蓝色分量
     * @return PdfColor instance | PdfColor 实例
     */
    public static PdfColor rgb(float r, float g, float b) {
        return new PdfColor(r, g, b, 1f, ColorModel.RGB);
    }

    /**
     * Creates RGBA color with alpha
     * 创建带透明度的 RGBA 颜色
     *
     * @param r     red component (0-255) | 红色分量
     * @param g     green component (0-255) | 绿色分量
     * @param b     blue component (0-255) | 蓝色分量
     * @param alpha alpha (0-1, 0=transparent, 1=opaque) | 透明度
     * @return PdfColor instance | PdfColor 实例
     */
    public static PdfColor rgba(int r, int g, int b, float alpha) {
        return new PdfColor(r / 255f, g / 255f, b / 255f, alpha, ColorModel.RGB);
    }

    /**
     * Creates grayscale color
     * 创建灰度颜色
     *
     * @param gray gray level (0-255) | 灰度级别
     * @return PdfColor instance | PdfColor 实例
     */
    public static PdfColor gray(int gray) {
        float g = gray / 255f;
        return new PdfColor(g, g, g, 1f, ColorModel.GRAYSCALE);
    }

    /**
     * Creates grayscale color from 0-1 value
     * 从 0-1 值创建灰度颜色
     *
     * @param gray gray level (0-1) | 灰度级别
     * @return PdfColor instance | PdfColor 实例
     */
    public static PdfColor gray(float gray) {
        return new PdfColor(gray, gray, gray, 1f, ColorModel.GRAYSCALE);
    }

    /**
     * Creates color from hex string
     * 从十六进制字符串创建颜色
     *
     * @param hex hex color (e.g., "#FF0000" or "FF0000") | 十六进制颜色
     * @return PdfColor instance | PdfColor 实例
     */
    public static PdfColor hex(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        int r = Integer.parseInt(h.substring(0, 2), 16);
        int g = Integer.parseInt(h.substring(2, 4), 16);
        int b = Integer.parseInt(h.substring(4, 6), 16);
        return rgb(r, g, b);
    }

    /**
     * Creates color from java.awt.Color
     * 从 java.awt.Color 创建颜色
     *
     * @param color AWT color | AWT 颜色
     * @return PdfColor instance | PdfColor 实例
     */
    public static PdfColor from(java.awt.Color color) {
        return rgba(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 255f);
    }

    // ==================== Legacy Accessors | 兼容访问方法 ====================

    /**
     * Gets red component (0-1)
     * 获取红色分量 (0-1)
     *
     * @return red component | 红色分量
     */
    public float getRed() {
        return red;
    }

    /**
     * Gets green component (0-1)
     * 获取绿色分量 (0-1)
     *
     * @return green component | 绿色分量
     */
    public float getGreen() {
        return green;
    }

    /**
     * Gets blue component (0-1)
     * 获取蓝色分量 (0-1)
     *
     * @return blue component | 蓝色分量
     */
    public float getBlue() {
        return blue;
    }

    /**
     * Gets alpha (opacity)
     * 获取透明度
     *
     * @return alpha (0-1) | 透明度
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * Gets color model
     * 获取颜色模型
     *
     * @return color model | 颜色模型
     */
    public ColorModel getModel() {
        return model;
    }

    // ==================== Behavioral Methods | 行为方法 ====================

    /**
     * Converts to java.awt.Color
     * 转换为 java.awt.Color
     *
     * @return AWT color | AWT 颜色
     */
    public java.awt.Color toAwtColor() {
        return new java.awt.Color(red, green, blue, alpha);
    }

    /**
     * Converts to hex string
     * 转换为十六进制字符串
     *
     * @return hex string (e.g., "#FF0000") | 十六进制字符串
     */
    public String toHex() {
        return String.format("#%02X%02X%02X",
            (int) (red * 255), (int) (green * 255), (int) (blue * 255));
    }

    /**
     * Creates a darker version of this color
     * 创建当前颜色的深色版本
     *
     * @param factor darkening factor (0-1) | 加深因子
     * @return darker color | 深色颜色
     */
    public PdfColor darker(float factor) {
        return new PdfColor(red * factor, green * factor, blue * factor, alpha, model);
    }

    /**
     * Creates a lighter version of this color
     * 创建当前颜色的浅色版本
     *
     * @param factor lightening factor (0-1) | 变浅因子
     * @return lighter color | 浅色颜色
     */
    public PdfColor lighter(float factor) {
        return new PdfColor(
            Math.min(1f, red + (1f - red) * factor),
            Math.min(1f, green + (1f - green) * factor),
            Math.min(1f, blue + (1f - blue) * factor),
            alpha, model);
    }

    /**
     * Creates a color with different alpha
     * 创建具有不同透明度的颜色
     *
     * @param newAlpha new alpha value | 新的透明度值
     * @return color with new alpha | 具有新透明度的颜色
     */
    public PdfColor withAlpha(float newAlpha) {
        return new PdfColor(red, green, blue, newAlpha, model);
    }

    @Override
    public String toString() {
        return String.format("PdfColor[r=%.2f, g=%.2f, b=%.2f, a=%.2f]", red, green, blue, alpha);
    }

    /**
     * Color Model
     * 颜色模型
     */
    public enum ColorModel {
        /** RGB color model | RGB 颜色模型 */
        RGB,
        /** CMYK color model | CMYK 颜色模型 */
        CMYK,
        /** Grayscale color model | 灰度颜色模型 */
        GRAYSCALE
    }
}
