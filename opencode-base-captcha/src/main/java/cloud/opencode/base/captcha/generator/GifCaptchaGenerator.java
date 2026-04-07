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
 *   <li>Random font per character for anti-OCR (consistent across frames) - 每字符随机字体抗OCR（跨帧一致）</li>
 *   <li>Character overlap for anti-segmentation (consistent across frames) - 字符重叠抗分割（跨帧一致）</li>
 *   <li>Outline shadow for anti-OCR - 轮廓阴影抗OCR</li>
 *   <li>Bezier noise through character area - 贝塞尔穿字噪声</li>
 *   <li>Sine wave warp distortion with animated variation - 正弦波变形扭曲（带动画变化）</li>
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
 * @since JDK 25, opencode-base-captcha V1.0.3
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
     * Creates an animated GIF with optional enhancement effects.
     * 创建带可选增强效果的动画 GIF。
     *
     * <p>Per-character fonts and overlap spacing are computed once before the
     * frame loop to ensure visual consistency across all frames.</p>
     * <p>每字符字体和重叠间距在帧循环前计算一次，确保所有帧视觉一致。</p>
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

        int len = code.length();

        // Pre-compute per-character fonts once for consistency across frames
        // 预计算每字符字体，确保跨帧一致
        Font[] perCharFonts = null;
        if (config.isRandomFontPerChar()) {
            perCharFonts = CaptchaFontUtil.getRandomFontsPerChar(
                config.getFontName(), config.getCustomFontPaths(), config.getFontSize(), len
            );
        }

        // Pre-compute overlap spacing once for consistency across frames
        // 预计算重叠间距，确保跨帧一致
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

        // Generate frames
        int frameCount = config.getGifFrameCount();
        for (int frame = 0; frame < frameCount; frame++) {
            BufferedImage image = createFrame(code, config, frame, frameCount,
                perCharFonts, charWidth, startX);
            encoder.addFrame(image);
        }

        encoder.finish();
        return baos.toByteArray();
    }

    /**
     * Creates a single frame with enhancement effects.
     * 创建带增强效果的单帧。
     *
     * @param code         the code | 代码
     * @param config       the configuration | 配置
     * @param frameIndex   the frame index | 帧索引
     * @param totalFrames  the total frames | 总帧数
     * @param perCharFonts the per-character fonts (may be null) | 每字符字体（可为null）
     * @param charWidth    the character spacing | 字符间距
     * @param startX       the starting x position | 起始 x 坐标
     * @return the frame image | 帧图像
     */
    private BufferedImage createFrame(String code, CaptchaConfig config, int frameIndex, int totalFrames,
                                       Font[] perCharFonts, int charWidth, int startX) {
        BufferedImage image = createImage(config);
        Graphics2D g = createGraphics(image, config);

        try {
            // Draw animated text
            drawAnimatedText(g, code, config, frameIndex, totalFrames, perCharFonts, charWidth, startX);

            // Draw animated noise
            CaptchaNoiseUtil.drawNoiseLines(g, config);
            CaptchaNoiseUtil.drawNoiseDots(g, config);

            // Draw Bezier noise through character area | 绘制贝塞尔穿字噪声
            if (config.isBezierNoiseEnabled()) {
                CaptchaNoiseUtil.drawBezierNoise(g, config.getWidth(), config.getHeight(), config.getNoiseLines());
            }
        } finally {
            g.dispose();
        }

        // Apply sine wave warp with slight per-frame variation for animation effect
        // 施加正弦波变形，每帧略有变化以产生动画效果
        if (config.isSineWarpEnabled()) {
            double phaseShift = 0.5 * Math.sin(2 * Math.PI * frameIndex / totalFrames);
            double amplitude = 2.0 + phaseShift;
            image = CaptchaNoiseUtil.applySineWarp(image, amplitude, config.getHeight() * 0.8);
        }

        return image;
    }

    /**
     * Draws animated text with enhancement effects.
     * 绘制带增强效果的动画文本。
     *
     * @param g            the graphics | 图形
     * @param code         the code | 代码
     * @param config       the configuration | 配置
     * @param frameIndex   the frame index | 帧索引
     * @param totalFrames  the total frames | 总帧数
     * @param perCharFonts the per-character fonts (may be null) | 每字符字体（可为null）
     * @param charWidth    the character spacing | 字符间距
     * @param startX       the starting x position | 起始 x 坐标
     */
    private void drawAnimatedText(Graphics2D g, String code, CaptchaConfig config,
                                   int frameIndex, int totalFrames,
                                   Font[] perCharFonts, int charWidth, int startX) {
        Font baseFont = CaptchaFontUtil.getFont(config);
        int baseY = config.getHeight() / 2 + (int) (config.getFontSize() / 3);

        double phase = 2 * Math.PI * frameIndex / totalFrames;

        for (int i = 0; i < code.length(); i++) {
            Color color = CaptchaFontUtil.getRandomColor(config);
            g.setColor(color);

            // Select font for this character | 选择此字符的字体
            Font charBaseFont = (perCharFonts != null) ? perCharFonts[i] : baseFont;

            // Animated rotation
            double charPhase = phase + (i * Math.PI / code.length());
            double angle = Math.sin(charPhase) * 0.3;
            Font rotatedFont = CaptchaFontUtil.getRotatedFont(charBaseFont, angle);
            g.setFont(rotatedFont);

            // Animated vertical offset
            int yOffset = (int) (Math.sin(charPhase) * 5);

            int x = startX + i * charWidth;
            int y = baseY + yOffset;

            // Draw outline shadow | 绘制轮廓阴影
            if (config.isOutlineShadowEnabled()) {
                CaptchaNoiseUtil.drawOutlineShadow(g, String.valueOf(code.charAt(i)), rotatedFont, x, y, color);
            }

            g.setFont(rotatedFont);
            g.setColor(color);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
    }
}
