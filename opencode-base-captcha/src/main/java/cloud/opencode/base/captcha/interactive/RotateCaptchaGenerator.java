package cloud.opencode.base.captcha.interactive;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.exception.CaptchaGenerationException;
import cloud.opencode.base.captcha.generator.CaptchaGenerator;
import cloud.opencode.base.captcha.support.CaptchaChars;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rotate Captcha Generator - Generates rotation verification CAPTCHA
 * 旋转验证码生成器 - 生成旋转验证验证码
 *
 * <p>This generator creates rotation-based interactive CAPTCHAs where
 * users must rotate an image to the correct angle.</p>
 * <p>此生成器创建基于旋转的交互式验证码，
 * 用户必须将图像旋转到正确角度。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Rotation-based interactive verification - 基于旋转的交互式验证</li>
 *   <li>Angle-based challenge - 基于角度的挑战</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator gen = new RotateCaptchaGenerator();
 * Captcha captcha = gen.generate(config);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No (config must not be null) - 空值安全: 否（配置不能为null）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class RotateCaptchaGenerator implements CaptchaGenerator {

    private static final int ANGLE_TOLERANCE = 10; // Degrees

    @Override
    public Captcha generate(CaptchaConfig config) {
        int size = Math.min(config.getWidth(), config.getHeight());

        try {
            // Generate random rotation angle (avoid 0, 90, 180, 270)
            int correctAngle = generateRandomAngle();

            // Create base image (circular)
            BufferedImage baseImage = createBaseImage(size);

            // Create rotated image for display
            BufferedImage rotatedImage = rotateImage(baseImage, correctAngle);

            // Create metadata
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("width", size);
            metadata.put("height", size);
            metadata.put("correctAngle", correctAngle);
            metadata.put("tolerance", ANGLE_TOLERANCE);

            byte[] imageData = toBytes(rotatedImage);

            Instant now = Instant.now();
            return new Captcha(
                UUID.randomUUID().toString().replace("-", ""),
                CaptchaType.ROTATE,
                imageData,
                String.valueOf(correctAngle),
                metadata,
                now,
                now.plus(config.getExpireTime())
            );
        } catch (Exception e) {
            throw new CaptchaGenerationException("Failed to generate rotate CAPTCHA", CaptchaType.ROTATE, e);
        }
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.ROTATE;
    }

    /**
     * Generates a random angle avoiding straight angles.
     * 生成随机角度，避免直角。
     */
    private int generateRandomAngle() {
        int angle;
        do {
            angle = CaptchaChars.randomInt(30, 330);
        } while (isNearStraightAngle(angle));
        return angle;
    }

    /**
     * Checks if angle is near a straight angle.
     * 检查角度是否接近直角。
     */
    private boolean isNearStraightAngle(int angle) {
        int[] straightAngles = {0, 90, 180, 270, 360};
        for (int sa : straightAngles) {
            if (Math.abs(angle - sa) < 20) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the base image with a recognizable pattern.
     * 创建具有可识别图案的基础图像。
     */
    private BufferedImage createBaseImage(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int center = size / 2;
        int radius = size / 2 - 5;

        // Create circular clip
        Ellipse2D circle = new Ellipse2D.Float(5, 5, size - 10, size - 10);
        g.setClip(circle);

        // Draw gradient background
        Color startColor = new Color(
            CaptchaChars.randomInt(100, 200),
            CaptchaChars.randomInt(100, 200),
            CaptchaChars.randomInt(100, 200)
        );
        Color endColor = new Color(
            CaptchaChars.randomInt(100, 200),
            CaptchaChars.randomInt(100, 200),
            CaptchaChars.randomInt(100, 200)
        );
        GradientPaint gradient = new GradientPaint(0, 0, startColor, size, size, endColor);
        g.setPaint(gradient);
        g.fillOval(5, 5, size - 10, size - 10);

        // Draw a clear "up" indicator (arrow or triangle at top)
        g.setColor(Color.WHITE);
        int arrowSize = size / 6;
        int[] xPoints = {center, center - arrowSize / 2, center + arrowSize / 2};
        int[] yPoints = {15, 15 + arrowSize, 15 + arrowSize};
        g.fillPolygon(xPoints, yPoints, 3);

        // Draw some patterns
        g.setColor(new Color(255, 255, 255, 100));
        for (int i = 0; i < 5; i++) {
            int x = CaptchaChars.randomInt(size);
            int y = CaptchaChars.randomInt(size);
            int patternSize = CaptchaChars.randomInt(10, 30);
            g.fillOval(x, y, patternSize, patternSize);
        }

        // Draw border
        g.setClip(null);
        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(3));
        g.drawOval(5, 5, size - 10, size - 10);

        g.dispose();
        return image;
    }

    /**
     * Rotates the image by the specified angle.
     * 按指定角度旋转图像。
     */
    private BufferedImage rotateImage(BufferedImage image, int angle) {
        int size = image.getWidth();
        BufferedImage rotated = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = rotated.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        AffineTransform transform = new AffineTransform();
        transform.translate(size / 2.0, size / 2.0);
        transform.rotate(Math.toRadians(angle));
        transform.translate(-size / 2.0, -size / 2.0);

        g.setTransform(transform);
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return rotated;
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
