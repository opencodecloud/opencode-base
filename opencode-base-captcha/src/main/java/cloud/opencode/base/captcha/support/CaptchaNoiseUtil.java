package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaConfig;

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;

/**
 * Captcha Noise Utility - Noise generation utilities
 * 验证码噪点工具 - 噪点生成工具
 *
 * <p>This class provides noise and distortion utilities for CAPTCHA.</p>
 * <p>此类提供验证码的噪点和干扰工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Noise dot generation - 噪点生成</li>
 *   <li>Interference line drawing - 干扰线绘制</li>
 *   <li>Curve distortion - 曲线扭曲</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaNoiseUtil.drawNoise(graphics, config);
 * CaptchaNoiseUtil.drawLines(graphics, config);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (graphics and config must not be null) - 空值安全: 否（图形和配置不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(k) where k is the configured noise count - 时间复杂度: O(k)，k 为配置的噪点数量</li>
 *   <li>Space complexity: O(1) - no intermediate data structures - 空间复杂度: O(1) 无中间数据结构</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class CaptchaNoiseUtil {

    /** Cached thin stroke (width 1) | 缓存的细线条（宽度 1） */
    private static final BasicStroke STROKE_THIN = new BasicStroke(1.0f);

    /** Cached thick stroke (width 2) | 缓存的粗线条（宽度 2） */
    private static final BasicStroke STROKE_THICK = new BasicStroke(2.0f);

    private CaptchaNoiseUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Returns a randomly chosen cached stroke (thin or thick).
     * 随机返回一个缓存的线条（细或粗）。
     *
     * @return a BasicStroke of width 1 or 2 | 宽度为 1 或 2 的 BasicStroke
     */
    private static BasicStroke randomStroke() {
        return CaptchaChars.randomInt(2) == 0 ? STROKE_THIN : STROKE_THICK;
    }

    /**
     * Draws noise lines on the graphics.
     * 在图形上绘制干扰线。
     *
     * @param g      the graphics | 图形
     * @param config the configuration | 配置
     */
    public static void drawNoiseLines(Graphics2D g, CaptchaConfig config) {
        int width = config.getWidth();
        int height = config.getHeight();
        int count = config.getNoiseLines();

        for (int i = 0; i < count; i++) {
            g.setColor(CaptchaFontUtil.randomColor());
            g.setStroke(randomStroke());

            int x1 = CaptchaChars.randomInt(width);
            int y1 = CaptchaChars.randomInt(height);
            int x2 = CaptchaChars.randomInt(width);
            int y2 = CaptchaChars.randomInt(height);

            g.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * Draws curve lines on the graphics.
     * 在图形上绘制曲线。
     *
     * @param g      the graphics | 图形
     * @param config the configuration | 配置
     */
    public static void drawCurveLines(Graphics2D g, CaptchaConfig config) {
        int width = config.getWidth();
        int height = config.getHeight();
        int count = config.getNoiseLines();

        for (int i = 0; i < count; i++) {
            g.setColor(CaptchaFontUtil.randomColor());
            g.setStroke(randomStroke());

            QuadCurve2D curve = new QuadCurve2D.Float(
                CaptchaChars.randomInt(width),
                CaptchaChars.randomInt(height),
                CaptchaChars.randomInt(width),
                CaptchaChars.randomInt(height),
                CaptchaChars.randomInt(width),
                CaptchaChars.randomInt(height)
            );
            g.draw(curve);
        }
    }

    /**
     * Draws cubic curve lines on the graphics.
     * 在图形上绘制三次曲线。
     *
     * @param g      the graphics | 图形
     * @param config the configuration | 配置
     */
    public static void drawCubicCurveLines(Graphics2D g, CaptchaConfig config) {
        int width = config.getWidth();
        int height = config.getHeight();
        int count = config.getNoiseLines();

        for (int i = 0; i < count; i++) {
            g.setColor(CaptchaFontUtil.randomColor());
            g.setStroke(randomStroke());

            CubicCurve2D curve = new CubicCurve2D.Float(
                CaptchaChars.randomInt(width),
                CaptchaChars.randomInt(height),
                CaptchaChars.randomInt(width),
                CaptchaChars.randomInt(height),
                CaptchaChars.randomInt(width),
                CaptchaChars.randomInt(height),
                CaptchaChars.randomInt(width),
                CaptchaChars.randomInt(height)
            );
            g.draw(curve);
        }
    }

    /**
     * Draws noise dots on the graphics.
     * 在图形上绘制干扰点。
     *
     * @param g      the graphics | 图形
     * @param config the configuration | 配置
     */
    public static void drawNoiseDots(Graphics2D g, CaptchaConfig config) {
        int width = config.getWidth();
        int height = config.getHeight();
        int count = config.getNoiseDots();

        for (int i = 0; i < count; i++) {
            g.setColor(CaptchaFontUtil.randomColor());
            int x = CaptchaChars.randomInt(width);
            int y = CaptchaChars.randomInt(height);
            int size = CaptchaChars.randomInt(1, 4);
            g.fillOval(x, y, size, size);
        }
    }

    /**
     * Draws background noise.
     * 绘制背景噪点。
     *
     * @param g      the graphics | 图形
     * @param config the configuration | 配置
     */
    public static void drawBackgroundNoise(Graphics2D g, CaptchaConfig config) {
        int width = config.getWidth();
        int height = config.getHeight();

        // Draw light colored rectangles
        for (int i = 0; i < 10; i++) {
            g.setColor(CaptchaFontUtil.randomLightColor());
            int x = CaptchaChars.randomInt(width);
            int y = CaptchaChars.randomInt(height);
            int w = CaptchaChars.randomInt(5, 20);
            int h = CaptchaChars.randomInt(5, 20);
            g.fillRect(x, y, w, h);
        }
    }

    /**
     * Draws a shear transform effect.
     * 绘制剪切变换效果。
     *
     * @param g      the graphics | 图形
     * @param config the configuration | 配置
     */
    public static void shear(Graphics2D g, CaptchaConfig config) {
        double shx = (CaptchaChars.randomInt(10) - 5) / 100.0;
        double shy = (CaptchaChars.randomInt(10) - 5) / 100.0;
        g.shear(shx, shy);
    }

    /**
     * Draws background with gradient.
     * 绘制渐变背景。
     *
     * @param g      the graphics | 图形
     * @param config the configuration | 配置
     */
    public static void drawGradientBackground(Graphics2D g, CaptchaConfig config) {
        int width = config.getWidth();
        int height = config.getHeight();

        Color startColor = CaptchaFontUtil.randomLightColor();
        Color endColor = CaptchaFontUtil.randomLightColor();

        GradientPaint gradient = new GradientPaint(
            0, 0, startColor,
            width, height, endColor
        );

        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);
    }

    /**
     * Draws interference pattern.
     * 绘制干扰图案。
     *
     * @param g      the graphics | 图形
     * @param config the configuration | 配置
     */
    public static void drawInterferencePattern(Graphics2D g, CaptchaConfig config) {
        int width = config.getWidth();
        int height = config.getHeight();

        g.setColor(new Color(200, 200, 200, 100));

        // Draw grid pattern
        for (int i = 0; i < width; i += 10) {
            g.drawLine(i, 0, i, height);
        }
        for (int i = 0; i < height; i += 10) {
            g.drawLine(0, i, width, i);
        }
    }

    /**
     * Draws Bezier curve noise that passes through the character area for anti-OCR.
     * 绘制穿过字符区域的贝塞尔曲线噪声，用于抗 OCR。
     *
     * <p>Unlike {@link #drawCubicCurveLines(Graphics2D, CaptchaConfig)}, this method
     * forces all curves to pass through the character center area (20%-80% of height),
     * making OCR recognition more difficult.</p>
     * <p>与 {@link #drawCubicCurveLines(Graphics2D, CaptchaConfig)} 不同，此方法强制所有曲线
     * 穿过字符中心区域（高度的 20%-80%），使 OCR 识别更加困难。</p>
     *
     * @param g      the graphics | 图形
     * @param width  the image width | 图像宽度
     * @param height the image height | 图像高度
     * @param count  the number of curves to draw | 要绘制的曲线数量
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    public static void drawBezierNoise(Graphics2D g, int width, int height, int count) {
        int yMin = (int) (height * 0.2);
        int yMax = (int) (height * 0.8);
        int yRange = Math.max(yMax - yMin, 1);

        for (int i = 0; i < count; i++) {
            g.setColor(CaptchaFontUtil.randomDarkColor());
            g.setStroke(randomStroke());

            // All y-coordinates are confined to the 20%-80% character area
            // 所有 y 坐标限制在 20%-80% 的字符区域内
            CubicCurve2D curve = new CubicCurve2D.Float(
                CaptchaChars.randomInt(width),
                yMin + CaptchaChars.randomInt(yRange),
                CaptchaChars.randomInt(width),
                yMin + CaptchaChars.randomInt(yRange),
                CaptchaChars.randomInt(width),
                yMin + CaptchaChars.randomInt(yRange),
                CaptchaChars.randomInt(width),
                yMin + CaptchaChars.randomInt(yRange)
            );
            g.draw(curve);
        }
    }

    /**
     * Applies sine wave warp distortion to an image.
     * 对图像施加正弦波变形扭曲。
     *
     * <p>Each row of pixels is shifted horizontally by a sine function,
     * creating a wave-like distortion effect. Pixels shifted beyond the
     * image boundary are filled with the background color (sampled from
     * the top-left corner of the source image).</p>
     * <p>每行像素按正弦函数水平偏移，产生波浪状扭曲效果。超出图像边界的像素
     * 用背景色（取自源图像左上角像素颜色）填充。</p>
     *
     * @param image     the source image | 源图像
     * @param amplitude the wave amplitude in pixels | 波浪振幅（像素）
     * @param period    the wave period in pixels | 波浪周期（像素）
     * @return the warped image | 变形后的图像
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    public static BufferedImage applySineWarp(BufferedImage image, double amplitude, double period) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());

        // Use the top-left pixel as background color | 使用左上角像素作为背景色
        int bgRgb = image.getRGB(0, 0);

        for (int y = 0; y < height; y++) {
            int shift = (int) (amplitude * Math.sin(2 * Math.PI * y / period));
            for (int x = 0; x < width; x++) {
                int srcX = x + shift;
                if (srcX >= 0 && srcX < width) {
                    result.setRGB(x, y, image.getRGB(srcX, y));
                } else {
                    result.setRGB(x, y, bgRgb);
                }
            }
        }

        return result;
    }

    /**
     * Draws character outline shadow for anti-OCR.
     * 绘制字符轮廓阴影，用于抗 OCR。
     *
     * <p>Renders semi-transparent copies of the text offset by 1-2 pixels in
     * four directions (up, down, left, right), creating a blurred outline
     * that disrupts OCR segmentation.</p>
     * <p>在上下左右四个方向各偏移 1-2 像素绘制半透明文本副本，产生模糊轮廓
     * 以干扰 OCR 分割。</p>
     *
     * @param g           the graphics | 图形
     * @param text        the text to draw shadow for | 要绘制阴影的文本
     * @param font        the font | 字体
     * @param x           the x position | x 坐标
     * @param y           the y position | y 坐标
     * @param shadowColor the shadow color | 阴影颜色
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    public static void drawOutlineShadow(Graphics2D g, String text, Font font, int x, int y, Color shadowColor) {
        // Apply semi-transparent alpha (~80) to the shadow color
        // 对阴影颜色应用半透明 alpha（约 80）
        Color semiTransparent = new Color(
            shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), 80
        );

        Font originalFont = g.getFont();
        Color originalColor = g.getColor();

        g.setFont(font);
        g.setColor(semiTransparent);

        int offset = CaptchaChars.randomInt(1, 3); // 1 or 2 pixels | 1 或 2 像素

        // Draw in four directions: up, down, left, right
        // 在四个方向绘制：上、下、左、右
        g.drawString(text, x, y - offset); // up | 上
        g.drawString(text, x, y + offset); // down | 下
        g.drawString(text, x - offset, y); // left | 左
        g.drawString(text, x + offset, y); // right | 右

        // Restore original state | 恢复原始状态
        g.setFont(originalFont);
        g.setColor(originalColor);
    }

    /**
     * Calculates character spacing with overlap for anti-OCR.
     * 计算带重叠的字符间距，用于抗 OCR。
     *
     * <p>Computes the horizontal spacing between character start positions,
     * allowing adjacent characters to overlap by a configurable ratio.
     * This makes character segmentation harder for OCR engines.</p>
     * <p>计算字符起始位置之间的水平间距，允许相邻字符按可配置比例重叠。
     * 这使 OCR 引擎更难进行字符分割。</p>
     *
     * @param totalWidth   the total available width | 总可用宽度
     * @param charCount    the number of characters | 字符数
     * @param fontSize     the font size | 字体大小
     * @param overlapRatio the overlap ratio (0.0 to 0.5) | 重叠比例（0.0 到 0.5）
     * @return the spacing between character start positions | 字符起始位置间距
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    public static int calculateOverlapSpacing(int totalWidth, int charCount, float fontSize, float overlapRatio) {
        if (charCount <= 1) {
            return totalWidth;
        }

        // Clamp overlapRatio to [0.0, 0.5] | 将重叠比例限制在 [0.0, 0.5]
        float clampedRatio = Math.max(0.0f, Math.min(0.5f, overlapRatio));

        float charWidth = fontSize * 0.7f;
        int spacing = Math.max(1, (int) (charWidth * (1.0f - clampedRatio)));

        // Ensure all characters fit within totalWidth
        // 确保所有字符能放入 totalWidth 内
        // Total occupied = spacing * (charCount - 1) + charWidth
        int totalOccupied = spacing * (charCount - 1) + (int) charWidth;
        if (totalOccupied > totalWidth) {
            // Auto-adjust spacing to fit | 自动调整间距以适应
            spacing = (int) ((totalWidth - charWidth) / (charCount - 1));
            spacing = Math.max(spacing, 1);
        }

        return spacing;
    }
}
