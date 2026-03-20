package cloud.opencode.base.captcha.interactive;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.exception.CaptchaGenerationException;
import cloud.opencode.base.captcha.generator.CaptchaGenerator;
import cloud.opencode.base.captcha.support.CaptchaChars;
import cloud.opencode.base.captcha.support.CaptchaFontUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * Click Captcha Generator - Generates click verification CAPTCHA
 * 点击验证码生成器 - 生成点击验证验证码
 *
 * <p>This generator creates click-based interactive CAPTCHAs where
 * users must click on specific characters in order.</p>
 * <p>此生成器创建基于点击的交互式验证码，
 * 用户必须按顺序点击特定字符。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Click-based interactive verification - 基于点击的交互式验证</li>
 *   <li>Character position tracking - 字符位置跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator gen = new ClickCaptchaGenerator();
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
public final class ClickCaptchaGenerator implements CaptchaGenerator {

    private static final int CLICK_TARGETS = 4;
    private static final int TARGET_SIZE = 40;

    @Override
    public Captcha generate(CaptchaConfig config) {
        int width = config.getWidth();
        int height = config.getHeight();

        try {
            // Generate random Chinese characters
            String allChars = CaptchaChars.generateChinese(CLICK_TARGETS + 3);

            // Select target characters (first 4 are targets)
            String targetChars = allChars.substring(0, CLICK_TARGETS);

            // Generate random positions for all characters
            List<int[]> positions = generatePositions(width, height, allChars.length());

            // Create image
            BufferedImage image = createImage(width, height, allChars, positions, config);

            // Create answer as coordinates
            StringBuilder answer = new StringBuilder();
            for (int i = 0; i < CLICK_TARGETS; i++) {
                int[] pos = positions.get(i);
                if (i > 0) answer.append("|");
                answer.append(pos[0]).append(",").append(pos[1]);
            }

            // Create metadata
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("width", width);
            metadata.put("height", height);
            metadata.put("targetChars", targetChars);
            metadata.put("targetCount", CLICK_TARGETS);
            metadata.put("targetSize", TARGET_SIZE);

            byte[] imageData = toBytes(image);

            Instant now = Instant.now();
            return new Captcha(
                UUID.randomUUID().toString().replace("-", ""),
                CaptchaType.CLICK,
                imageData,
                answer.toString(),
                metadata,
                now,
                now.plus(config.getExpireTime())
            );
        } catch (Exception e) {
            throw new CaptchaGenerationException("Failed to generate click CAPTCHA", CaptchaType.CLICK, e);
        }
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.CLICK;
    }

    /**
     * Generates non-overlapping positions.
     * 生成不重叠的位置。
     */
    private List<int[]> generatePositions(int width, int height, int count) {
        List<int[]> positions = new ArrayList<>();
        int margin = TARGET_SIZE / 2 + 5;
        int maxAttempts = 100;

        for (int i = 0; i < count; i++) {
            int attempts = 0;
            boolean valid = false;
            int x = 0, y = 0;

            while (!valid && attempts < maxAttempts) {
                x = CaptchaChars.randomInt(margin, width - margin);
                y = CaptchaChars.randomInt(margin, height - margin);

                valid = true;
                for (int[] pos : positions) {
                    double distance = Math.sqrt(Math.pow(x - pos[0], 2) + Math.pow(y - pos[1], 2));
                    if (distance < TARGET_SIZE + 5) {
                        valid = false;
                        break;
                    }
                }
                attempts++;
            }

            positions.add(new int[]{x, y});
        }

        return positions;
    }

    /**
     * Creates the click CAPTCHA image.
     * 创建点击验证码图像。
     */
    private BufferedImage createImage(int width, int height, String chars,
                                       List<int[]> positions, CaptchaConfig config) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw background
        g.setColor(config.getBackgroundColor());
        g.fillRect(0, 0, width, height);

        // Add background noise
        for (int i = 0; i < 50; i++) {
            g.setColor(CaptchaFontUtil.randomLightColor());
            int x = CaptchaChars.randomInt(width);
            int y = CaptchaChars.randomInt(height);
            int size = CaptchaChars.randomInt(2, 8);
            g.fillOval(x, y, size, size);
        }

        // Draw characters
        Font font = CaptchaFontUtil.getChineseFont(TARGET_SIZE * 0.8f);

        for (int i = 0; i < chars.length(); i++) {
            int[] pos = positions.get(i);

            // Random rotation
            double angle = (CaptchaChars.randomInt(40) - 20) * Math.PI / 180;
            Font rotatedFont = CaptchaFontUtil.getRotatedFont(font, angle);
            g.setFont(rotatedFont);

            // Random color
            g.setColor(CaptchaFontUtil.randomColor());

            // Center the character at position
            FontMetrics fm = g.getFontMetrics();
            int charWidth = fm.charWidth(chars.charAt(i));
            int charHeight = fm.getHeight();

            g.drawString(
                String.valueOf(chars.charAt(i)),
                pos[0] - charWidth / 2,
                pos[1] + charHeight / 4
            );
        }

        g.dispose();
        return image;
    }

    /**
     * Converts image to PNG bytes.
     */
    private byte[] toBytes(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new CaptchaGenerationException("Failed to convert image to bytes", e);
        }
    }
}
