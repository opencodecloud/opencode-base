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
 * Chinese Captcha Generator - Generates Chinese character CAPTCHA
 * 中文验证码生成器 - 生成中文字符验证码
 *
 * <p>This generator creates CAPTCHAs with Chinese characters.</p>
 * <p>此生成器创建带中文字符的验证码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Chinese character CAPTCHA generation - 中文字符验证码生成</li>
 *   <li>Random Chinese character selection - 随机中文字符选择</li>
 *   <li>Random font per character for anti-OCR - 每字符随机字体抗OCR</li>
 *   <li>Character overlap for anti-segmentation - 字符重叠抗分割</li>
 *   <li>Outline shadow for anti-OCR - 轮廓阴影抗OCR</li>
 *   <li>Bezier noise through character area - 贝塞尔穿字噪声</li>
 *   <li>Sine wave warp distortion - 正弦波变形扭曲</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator gen = new ChineseCaptchaGenerator();
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
public final class ChineseCaptchaGenerator extends AbstractCaptchaGenerator implements CaptchaGenerator {

    @Override
    public Captcha generate(CaptchaConfig config) {
        String code = CaptchaChars.generateChinese(config.getLength());

        BufferedImage image = createImage(config);
        Graphics2D g = createGraphics(image, config);

        try {
            // Draw Chinese text
            drawChineseText(g, code, config);

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

        return buildCaptcha(CaptchaType.CHINESE, imageData, code, config);
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.CHINESE;
    }

    /**
     * Draws Chinese text with optional enhancements (random fonts, overlap, outline shadow).
     * 绘制中文文本，支持可选增强效果（随机字体、重叠、轮廓阴影）。
     *
     * @param g      the graphics | 图形
     * @param code   the code to draw | 要绘制的代码
     * @param config the configuration | 配置
     */
    private void drawChineseText(Graphics2D g, String code, CaptchaConfig config) {
        Font baseFont = CaptchaFontUtil.getChineseFont(config.getFontSize());
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
            // Random color
            Color color = CaptchaFontUtil.getRandomColor(config);
            g.setColor(color);

            // Select font for this character | 选择此字符的字体
            Font charBaseFont = (perCharFonts != null) ? perCharFonts[i] : baseFont;

            // Random rotation
            double angle = (CaptchaChars.randomInt(30) - 15) * Math.PI / 180;
            Font rotatedFont = CaptchaFontUtil.getRotatedFont(charBaseFont, angle);
            g.setFont(rotatedFont);

            // Random vertical offset
            int yOffset = CaptchaChars.randomInt(8) - 4;

            // Draw character
            int x = startX + i * charWidth;
            int y = baseY + yOffset;

            // Draw outline shadow before the main character | 在主字符前绘制轮廓阴影
            if (config.isOutlineShadowEnabled()) {
                CaptchaNoiseUtil.drawOutlineShadow(g, String.valueOf(code.charAt(i)), rotatedFont, x, y, color);
            }

            g.setFont(rotatedFont);
            g.setColor(color);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
    }
}
