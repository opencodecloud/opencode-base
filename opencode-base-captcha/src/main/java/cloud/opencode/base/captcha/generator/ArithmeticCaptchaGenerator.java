package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.support.CaptchaChars;
import cloud.opencode.base.captcha.support.CaptchaFontUtil;
import cloud.opencode.base.captcha.support.CaptchaNoiseUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Arithmetic Captcha Generator - Generates arithmetic expression CAPTCHA
 * 算术验证码生成器 - 生成算术表达式验证码
 *
 * <p>This generator creates CAPTCHAs with mathematical expressions.</p>
 * <p>此生成器创建带数学表达式的验证码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Arithmetic expression generation (add, subtract, multiply) - 算术表达式生成（加、减、乘）</li>
 *   <li>Configurable difficulty - 可配置难度</li>
 *   <li>Random font per character for anti-OCR - 每字符随机字体抗OCR</li>
 *   <li>Outline shadow for anti-OCR - 轮廓阴影抗OCR</li>
 *   <li>Bezier noise through character area - 贝塞尔穿字噪声</li>
 *   <li>Sine wave warp distortion - 正弦波变形扭曲</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator gen = new ArithmeticCaptchaGenerator();
 * Captcha captcha = gen.generate(config);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No (config must not be null) - 空值安全: 否（配置不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class ArithmeticCaptchaGenerator extends AbstractCaptchaGenerator implements CaptchaGenerator {

    @Override
    public Captcha generate(CaptchaConfig config) {
        String[] arithmetic = CaptchaChars.generateArithmetic();
        String expression = arithmetic[0];
        String answer = arithmetic[1];

        BufferedImage image = createImage(config);
        Graphics2D g = createGraphics(image, config);

        try {
            // Draw expression
            drawExpression(g, expression, config);

            // Draw noise
            drawNoise(g, config);

            // Draw Bezier noise through character area | 绘制贝塞尔穿字噪声
            if (config.isBezierNoiseEnabled()) {
                CaptchaNoiseUtil.drawBezierNoise(g, config.getWidth(), config.getHeight(), config.getNoiseLines());
            }
        } finally {
            g.dispose();
        }

        // Apply sine wave warp distortion | 施加正弦波变形扭曲
        if (config.isSineWarpEnabled()) {
            image = CaptchaNoiseUtil.applySineWarp(image, 2.0, config.getHeight() * 0.8);
        }

        // Convert to bytes
        byte[] imageData = toBytes(image);

        return buildCaptcha(CaptchaType.ARITHMETIC, imageData, answer, config);
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.ARITHMETIC;
    }

    /**
     * Draws the arithmetic expression with optional enhancements (random fonts, outline shadow).
     * 绘制算术表达式，支持可选增强效果（随机字体、轮廓阴影）。
     *
     * <p>Character overlap is intentionally skipped for arithmetic expressions
     * to preserve readability of operators and operands.</p>
     * <p>算术表达式有意跳过字符重叠，以保持运算符和操作数的可读性。</p>
     *
     * @param g          the graphics | 图形
     * @param expression the expression | 表达式
     * @param config     the configuration | 配置
     */
    private void drawExpression(Graphics2D g, String expression, CaptchaConfig config) {
        Font font = CaptchaFontUtil.getFont(config);
        int len = expression.length();

        // Resolve per-character fonts | 解析每字符字体
        Font[] perCharFonts = null;
        if (config.isRandomFontPerChar()) {
            perCharFonts = CaptchaFontUtil.getRandomFontsPerChar(
                config.getFontName(), config.getCustomFontPaths(), config.getFontSize(), len
            );
        }

        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(expression);
        int textHeight = fm.getHeight();

        // Center the expression
        int x = (config.getWidth() - textWidth) / 2;
        int y = (config.getHeight() + textHeight / 2) / 2;

        // Draw with random colors for each character
        int currentX = x;
        for (int i = 0; i < len; i++) {
            char c = expression.charAt(i);
            Color color = CaptchaFontUtil.getRandomColor(config);
            g.setColor(color);

            // Select font for this character | 选择此字符的字体
            Font charFont = (perCharFonts != null) ? perCharFonts[i] : font;

            // Add slight rotation for non-space characters
            Font drawFont = charFont;
            if (c != ' ') {
                double angle = (CaptchaChars.randomInt(20) - 10) * Math.PI / 180;
                drawFont = CaptchaFontUtil.getRotatedFont(charFont, angle);
            }
            g.setFont(drawFont);

            // Draw outline shadow | 绘制轮廓阴影
            if (config.isOutlineShadowEnabled()) {
                CaptchaNoiseUtil.drawOutlineShadow(g, String.valueOf(c), drawFont, currentX, y, color);
            }

            g.setFont(drawFont);
            g.setColor(color);
            g.drawString(String.valueOf(c), currentX, y);
            currentX += fm.charWidth(c);
            g.setFont(font);
        }
    }
}
