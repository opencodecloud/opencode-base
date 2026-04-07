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
 *   <li>Random font per character for anti-OCR - 每字符随机字体抗OCR</li>
 *   <li>Character overlap for anti-segmentation - 字符重叠抗分割</li>
 *   <li>Outline shadow for anti-OCR - 轮廓阴影抗OCR</li>
 *   <li>Bezier noise through character area - 贝塞尔穿字噪声</li>
 *   <li>Sine wave warp distortion - 正弦波变形扭曲</li>
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
 * @since JDK 25, opencode-base-captcha V1.0.3
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

        return buildCaptcha(CaptchaType.ALPHANUMERIC, imageData, code, config);
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.ALPHANUMERIC;
    }

    /**
     * Draws text with special effects and optional enhancements (random fonts, overlap, outline shadow).
     * 绘制带特效的文本，支持可选增强效果（随机字体、重叠、轮廓阴影）。
     *
     * @param g      the graphics | 图形
     * @param code   the code | 代码
     * @param config the configuration | 配置
     */
    private void drawSpecText(Graphics2D g, String code, CaptchaConfig config) {
        Font baseFont = CaptchaFontUtil.getFont(config);
        int len = code.length();

        // Resolve per-character fonts | 解析每字符字体
        Font[] perCharFonts = null;
        if (config.isRandomFontPerChar()) {
            perCharFonts = CaptchaFontUtil.getRandomFontsPerChar(
                config.getFontName(), config.getCustomFontPaths(), config.getFontSize(), len
            );
        }

        // Calculate character spacing | 计算字符间距
        int charWidth;
        int startX;
        if (config.getCharOverlapRatio() > 0) {
            charWidth = CaptchaNoiseUtil.calculateOverlapSpacing(
                config.getWidth(), len, config.getFontSize(), config.getCharOverlapRatio()
            );
            int totalTextWidth = charWidth * (len - 1) + (int) (config.getFontSize() * 0.7f);
            startX = Math.max(0, (config.getWidth() - totalTextWidth) / 2);
        } else {
            charWidth = config.getWidth() / (len + 1);
            startX = charWidth / 2;
        }

        int baseY = config.getHeight() / 2 + (int) (config.getFontSize() / 3);

        for (int i = 0; i < len; i++) {
            // Select font for this character | 选择此字符的字体
            Font charBaseFont = (perCharFonts != null) ? perCharFonts[i] : baseFont;

            // Create transform with multiple effects
            AffineTransform transform = new AffineTransform();

            // Random rotation
            double angle = (CaptchaChars.randomInt(50) - 25) * Math.PI / 180;
            transform.rotate(angle);

            // Random scale
            double scale = 0.8 + CaptchaChars.getRandom().nextDouble() * 0.4;
            transform.scale(scale, scale);

            // Apply transform to font
            Font derivedFont = charBaseFont.deriveFont(transform);
            g.setFont(derivedFont);

            // Random color with transparency effect
            Color color = CaptchaFontUtil.getRandomColor(config);
            g.setColor(color);

            // Calculate position with wave effect
            double wave = Math.sin(i * Math.PI / 2) * 5;
            int x = startX + i * charWidth;
            int y = baseY + (int) wave;

            // Draw outline shadow | 绘制轮廓阴影
            if (config.isOutlineShadowEnabled()) {
                CaptchaNoiseUtil.drawOutlineShadow(g, String.valueOf(code.charAt(i)), derivedFont, x, y, color);
            }

            // Draw shadow
            g.setFont(derivedFont);
            g.setColor(new Color(0, 0, 0, 50));
            g.drawString(String.valueOf(code.charAt(i)), x + 2, y + 2);

            // Draw main character
            g.setColor(color);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
    }
}
