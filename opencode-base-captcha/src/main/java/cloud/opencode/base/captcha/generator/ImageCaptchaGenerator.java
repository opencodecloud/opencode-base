package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.support.CaptchaChars;
import cloud.opencode.base.captcha.support.CaptchaFontUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Image Captcha Generator - Generates static image CAPTCHA
 * 图像验证码生成器 - 生成静态图像验证码
 *
 * <p>This generator creates standard image-based CAPTCHAs with text.</p>
 * <p>此生成器创建带文本的标准图像验证码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Static image CAPTCHA generation - 静态图像验证码生成</li>
 *   <li>Multiple type support (alphanumeric, numeric, alpha) - 多种类型支持（字母数字、纯数字、纯字母）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator gen = new ImageCaptchaGenerator(CaptchaType.ALPHANUMERIC);
 * Captcha captcha = gen.generate(config);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (config must not be null) - 空值安全: 否（配置不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class ImageCaptchaGenerator extends AbstractCaptchaGenerator implements CaptchaGenerator {

    private final CaptchaType type;

    /**
     * Creates a new generator with the specified type.
     * 使用指定类型创建新生成器。
     *
     * @param type the CAPTCHA type | 验证码类型
     */
    public ImageCaptchaGenerator(CaptchaType type) {
        this.type = type;
    }

    @Override
    public Captcha generate(CaptchaConfig config) {
        String code = CaptchaChars.generate(type, config.getLength());

        BufferedImage image = createImage(config);
        Graphics2D g = createGraphics(image, config);

        try {
            // Draw text
            drawText(g, code, config);

            // Draw noise
            drawNoise(g, config);

            // Convert to bytes
            byte[] imageData = toBytes(image);

            return buildCaptcha(type, imageData, code, config);
        } finally {
            g.dispose();
        }
    }

    @Override
    public CaptchaType getType() {
        return type;
    }

    /**
     * Draws the CAPTCHA text.
     * 绘制验证码文本。
     *
     * @param g      the graphics | 图形
     * @param code   the code to draw | 要绘制的代码
     * @param config the configuration | 配置
     */
    private void drawText(Graphics2D g, String code, CaptchaConfig config) {
        Font baseFont = CaptchaFontUtil.getFont(config);
        int charWidth = config.getWidth() / (code.length() + 1);
        int startX = charWidth / 2;
        int baseY = config.getHeight() / 2 + (int) (config.getFontSize() / 3);

        for (int i = 0; i < code.length(); i++) {
            // Random color
            g.setColor(CaptchaFontUtil.getRandomColor(config));

            // Random rotation
            double angle = (CaptchaChars.randomInt(40) - 20) * Math.PI / 180;
            Font rotatedFont = CaptchaFontUtil.getRotatedFont(
                CaptchaFontUtil.getRandomStyleFont(baseFont),
                angle
            );
            g.setFont(rotatedFont);

            // Random vertical offset
            int yOffset = CaptchaChars.randomInt(10) - 5;

            // Draw character
            int x = startX + i * charWidth;
            int y = baseY + yOffset;
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
    }
}
