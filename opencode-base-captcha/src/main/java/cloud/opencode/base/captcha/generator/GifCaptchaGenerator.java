package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.codec.GifEncoder;
import cloud.opencode.base.captcha.exception.CaptchaGenerationException;
import cloud.opencode.base.captcha.support.CaptchaChars;
import cloud.opencode.base.captcha.support.CaptchaFontUtil;
import cloud.opencode.base.captcha.support.CaptchaNoiseUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * GIF Captcha Generator - Generates animated GIF CAPTCHA
 * GIF 验证码生成器 - 生成动画 GIF 验证码
 *
 * <p>This generator creates animated GIF CAPTCHAs.</p>
 * <p>此生成器创建动画 GIF 验证码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Animated GIF CAPTCHA generation - 动画GIF验证码生成</li>
 *   <li>Multi-frame animation - 多帧动画</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator gen = new GifCaptchaGenerator();
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
public final class GifCaptchaGenerator extends AbstractCaptchaGenerator implements CaptchaGenerator {

    @Override
    public Captcha generate(CaptchaConfig config) {
        String code = CaptchaChars.generate(CaptchaType.ALPHANUMERIC, config.getLength());

        try {
            byte[] imageData = createGif(code, config);
            return buildCaptcha(CaptchaType.GIF, imageData, code, config);
        } catch (IOException e) {
            throw new CaptchaGenerationException("Failed to generate GIF CAPTCHA", CaptchaType.GIF, e);
        }
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.GIF;
    }

    /**
     * Creates an animated GIF.
     * 创建动画 GIF。
     *
     * @param code   the code | 代码
     * @param config the configuration | 配置
     * @return the GIF bytes | GIF 字节
     * @throws IOException if encoding fails | 如果编码失败
     */
    private byte[] createGif(String code, CaptchaConfig config) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GifEncoder encoder = new GifEncoder();

        encoder.start(baos);
        encoder.setRepeat(0); // Loop indefinitely
        encoder.setDelay(config.getGifDelay());
        encoder.setQuality(10);

        // Generate frames
        int frameCount = config.getGifFrameCount();
        for (int frame = 0; frame < frameCount; frame++) {
            BufferedImage image = createFrame(code, config, frame, frameCount);
            encoder.addFrame(image);
        }

        encoder.finish();
        return baos.toByteArray();
    }

    /**
     * Creates a single frame.
     * 创建单帧。
     *
     * @param code       the code | 代码
     * @param config     the configuration | 配置
     * @param frameIndex the frame index | 帧索引
     * @param totalFrames the total frames | 总帧数
     * @return the frame image | 帧图像
     */
    private BufferedImage createFrame(String code, CaptchaConfig config, int frameIndex, int totalFrames) {
        BufferedImage image = createImage(config);
        Graphics2D g = createGraphics(image, config);

        try {
            // Draw animated text
            drawAnimatedText(g, code, config, frameIndex, totalFrames);

            // Draw animated noise
            CaptchaNoiseUtil.drawNoiseLines(g, config);
            CaptchaNoiseUtil.drawNoiseDots(g, config);

            return image;
        } finally {
            g.dispose();
        }
    }

    /**
     * Draws animated text.
     * 绘制动画文本。
     *
     * @param g           the graphics | 图形
     * @param code        the code | 代码
     * @param config      the configuration | 配置
     * @param frameIndex  the frame index | 帧索引
     * @param totalFrames the total frames | 总帧数
     */
    private void drawAnimatedText(Graphics2D g, String code, CaptchaConfig config,
                                   int frameIndex, int totalFrames) {
        Font baseFont = CaptchaFontUtil.getFont(config);
        int charWidth = config.getWidth() / (code.length() + 1);
        int startX = charWidth / 2;
        int baseY = config.getHeight() / 2 + (int) (config.getFontSize() / 3);

        double phase = 2 * Math.PI * frameIndex / totalFrames;

        for (int i = 0; i < code.length(); i++) {
            g.setColor(CaptchaFontUtil.getRandomColor(config));

            // Animated rotation
            double charPhase = phase + (i * Math.PI / code.length());
            double angle = Math.sin(charPhase) * 0.3;
            Font rotatedFont = CaptchaFontUtil.getRotatedFont(baseFont, angle);
            g.setFont(rotatedFont);

            // Animated vertical offset
            int yOffset = (int) (Math.sin(charPhase) * 5);

            int x = startX + i * charWidth;
            int y = baseY + yOffset;
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
    }
}
