package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.support.CaptchaChars;
import cloud.opencode.base.captcha.support.CaptchaFontUtil;

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
 * @since JDK 25, opencode-base-captcha V1.0.0
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

            // Convert to bytes
            byte[] imageData = toBytes(image);

            return buildCaptcha(CaptchaType.CHINESE, imageData, code, config);
        } finally {
            g.dispose();
        }
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.CHINESE;
    }

    /**
     * Draws Chinese text.
     * 绘制中文文本。
     *
     * @param g      the graphics | 图形
     * @param code   the code to draw | 要绘制的代码
     * @param config the configuration | 配置
     */
    private void drawChineseText(Graphics2D g, String code, CaptchaConfig config) {
        Font baseFont = CaptchaFontUtil.getChineseFont(config.getFontSize());
        int charWidth = config.getWidth() / (code.length() + 1);
        int startX = charWidth / 2;
        int baseY = config.getHeight() / 2 + (int) (config.getFontSize() / 3);

        for (int i = 0; i < code.length(); i++) {
            // Random color
            g.setColor(CaptchaFontUtil.getRandomColor(config));

            // Random rotation
            double angle = (CaptchaChars.randomInt(30) - 15) * Math.PI / 180;
            Font rotatedFont = CaptchaFontUtil.getRotatedFont(baseFont, angle);
            g.setFont(rotatedFont);

            // Random vertical offset
            int yOffset = CaptchaChars.randomInt(8) - 4;

            // Draw character
            int x = startX + i * charWidth;
            int y = baseY + yOffset;
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
    }
}
