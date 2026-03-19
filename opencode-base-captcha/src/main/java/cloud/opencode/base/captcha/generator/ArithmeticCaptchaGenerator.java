package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.support.CaptchaChars;
import cloud.opencode.base.captcha.support.CaptchaFontUtil;

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
 * @since JDK 25, opencode-base-captcha V1.0.0
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

            // Convert to bytes
            byte[] imageData = toBytes(image);

            return buildCaptcha(CaptchaType.ARITHMETIC, imageData, answer, config);
        } finally {
            g.dispose();
        }
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.ARITHMETIC;
    }

    /**
     * Draws the arithmetic expression.
     * 绘制算术表达式。
     *
     * @param g          the graphics | 图形
     * @param expression the expression | 表达式
     * @param config     the configuration | 配置
     */
    private void drawExpression(Graphics2D g, String expression, CaptchaConfig config) {
        Font font = CaptchaFontUtil.getFont(config);
        g.setFont(font);

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(expression);
        int textHeight = fm.getHeight();

        // Center the expression
        int x = (config.getWidth() - textWidth) / 2;
        int y = (config.getHeight() + textHeight / 2) / 2;

        // Draw with random colors for each character
        int currentX = x;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            g.setColor(CaptchaFontUtil.getRandomColor(config));

            // Add slight rotation for non-space characters
            if (c != ' ') {
                double angle = (CaptchaChars.randomInt(20) - 10) * Math.PI / 180;
                Font rotatedFont = CaptchaFontUtil.getRotatedFont(font, angle);
                g.setFont(rotatedFont);
            }

            g.drawString(String.valueOf(c), currentX, y);
            currentX += fm.charWidth(c);
            g.setFont(font);
        }
    }
}
