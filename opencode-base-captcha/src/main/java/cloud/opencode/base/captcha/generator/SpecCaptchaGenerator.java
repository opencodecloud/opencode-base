package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.support.CaptchaChars;
import cloud.opencode.base.captcha.support.CaptchaFontUtil;
import cloud.opencode.base.captcha.support.CaptchaNoiseUtil;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Spec Captcha Generator - Generates special effect CAPTCHA
 * 特效验证码生成器 - 生成特效验证码
 *
 * <p>This generator creates CAPTCHAs with special visual effects.</p>
 * <p>此生成器创建带特殊视觉效果的验证码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Special visual effects (rotation, distortion) - 特殊视觉效果（旋转、扭曲）</li>
 *   <li>Enhanced noise and interference lines - 增强噪点和干扰线</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator gen = new SpecCaptchaGenerator();
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
public final class SpecCaptchaGenerator extends AbstractCaptchaGenerator implements CaptchaGenerator {

    @Override
    public Captcha generate(CaptchaConfig config) {
        String code = CaptchaChars.generate(CaptchaType.ALPHANUMERIC, config.getLength());

        BufferedImage image = createImage(config);
        Graphics2D g = createGraphics(image, config);

        try {
            // Draw gradient background
            CaptchaNoiseUtil.drawGradientBackground(g, config);

            // Draw text with effects
            drawSpecText(g, code, config);

            // Draw curve noise
            CaptchaNoiseUtil.drawCubicCurveLines(g, config);
            CaptchaNoiseUtil.drawNoiseDots(g, config);

            // Convert to bytes
            byte[] imageData = toBytes(image);

            return buildCaptcha(CaptchaType.ALPHANUMERIC, imageData, code, config);
        } finally {
            g.dispose();
        }
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.ALPHANUMERIC;
    }

    /**
     * Draws text with special effects.
     * 绘制带特效的文本。
     *
     * @param g      the graphics | 图形
     * @param code   the code | 代码
     * @param config the configuration | 配置
     */
    private void drawSpecText(Graphics2D g, String code, CaptchaConfig config) {
        Font baseFont = CaptchaFontUtil.getFont(config);
        int charWidth = config.getWidth() / (code.length() + 1);
        int startX = charWidth / 2;
        int baseY = config.getHeight() / 2 + (int) (config.getFontSize() / 3);

        for (int i = 0; i < code.length(); i++) {
            // Create transform with multiple effects
            AffineTransform transform = new AffineTransform();

            // Random rotation
            double angle = (CaptchaChars.randomInt(50) - 25) * Math.PI / 180;
            transform.rotate(angle);

            // Random scale
            double scale = 0.8 + CaptchaChars.getRandom().nextDouble() * 0.4;
            transform.scale(scale, scale);

            // Apply transform to font
            Font derivedFont = baseFont.deriveFont(transform);
            g.setFont(derivedFont);

            // Random color with transparency effect
            Color color = CaptchaFontUtil.getRandomColor(config);
            g.setColor(color);

            // Calculate position with wave effect
            double wave = Math.sin(i * Math.PI / 2) * 5;
            int x = startX + i * charWidth;
            int y = baseY + (int) wave;

            // Draw shadow
            g.setColor(new Color(0, 0, 0, 50));
            g.drawString(String.valueOf(code.charAt(i)), x + 2, y + 2);

            // Draw main character
            g.setColor(color);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
    }
}
