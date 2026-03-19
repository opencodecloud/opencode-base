package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaConfig;

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;

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

    private CaptchaNoiseUtil() {
        throw new AssertionError("Utility class - do not instantiate");
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
            g.setStroke(new BasicStroke(CaptchaChars.randomInt(1, 3)));

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
            g.setStroke(new BasicStroke(CaptchaChars.randomInt(1, 3)));

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
            g.setStroke(new BasicStroke(CaptchaChars.randomInt(1, 3)));

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
}
