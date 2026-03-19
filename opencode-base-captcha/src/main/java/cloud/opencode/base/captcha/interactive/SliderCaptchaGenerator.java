package cloud.opencode.base.captcha.interactive;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.exception.CaptchaGenerationException;
import cloud.opencode.base.captcha.generator.CaptchaGenerator;
import cloud.opencode.base.captcha.support.CaptchaChars;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Slider Captcha Generator - Generates slider verification CAPTCHA
 * 滑块验证码生成器 - 生成滑块验证验证码
 *
 * <p>This generator creates slider-based interactive CAPTCHAs.</p>
 * <p>此生成器创建基于滑块的交互式验证码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Slider puzzle verification - 滑块拼图验证</li>
 *   <li>Position-based challenge - 基于位置的挑战</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator gen = new SliderCaptchaGenerator();
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
public final class SliderCaptchaGenerator implements CaptchaGenerator {

    private static final int PUZZLE_SIZE = 50;
    private static final int PUZZLE_RADIUS = 8;

    @Override
    public Captcha generate(CaptchaConfig config) {
        int width = config.getWidth();
        int height = config.getHeight();

        // Generate random position for puzzle piece
        int puzzleX = CaptchaChars.randomInt(PUZZLE_SIZE + 20, width - PUZZLE_SIZE - 20);
        int puzzleY = CaptchaChars.randomInt(10, height - PUZZLE_SIZE - 10);

        try {
            // Create background image with puzzle cutout
            BufferedImage bgImage = createBackgroundImage(width, height);

            // Create puzzle piece shape
            GeneralPath puzzlePath = createPuzzlePath(puzzleX, puzzleY);

            // Create puzzle piece image
            BufferedImage puzzleImage = createPuzzlePiece(bgImage, puzzlePath, puzzleX, puzzleY);

            // Cut out puzzle from background
            cutPuzzleFromBackground(bgImage, puzzlePath);

            // Convert images to bytes
            byte[] bgBytes = toBytes(bgImage);
            byte[] puzzleBytes = toBytes(puzzleImage);

            // Create metadata
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("width", width);
            metadata.put("height", height);
            metadata.put("puzzleY", puzzleY);
            metadata.put("puzzleSize", PUZZLE_SIZE);
            metadata.put("puzzleImage", Base64.getEncoder().encodeToString(puzzleBytes));

            Instant now = Instant.now();
            return new Captcha(
                UUID.randomUUID().toString().replace("-", ""),
                CaptchaType.SLIDER,
                bgBytes,
                String.valueOf(puzzleX),
                metadata,
                now,
                now.plus(config.getExpireTime())
            );
        } catch (Exception e) {
            throw new CaptchaGenerationException("Failed to generate slider CAPTCHA", CaptchaType.SLIDER, e);
        }
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.SLIDER;
    }

    /**
     * Creates a background image with random gradient.
     * 创建带随机渐变的背景图像。
     */
    private BufferedImage createBackgroundImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

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

        GradientPaint gradient = new GradientPaint(0, 0, startColor, width, height, endColor);
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);

        // Add some random patterns
        g.setColor(new Color(255, 255, 255, 50));
        for (int i = 0; i < 20; i++) {
            int x = CaptchaChars.randomInt(width);
            int y = CaptchaChars.randomInt(height);
            int size = CaptchaChars.randomInt(10, 50);
            g.fillOval(x, y, size, size);
        }

        g.dispose();
        return image;
    }

    /**
     * Creates the puzzle piece shape path.
     * 创建拼图块形状路径。
     */
    private GeneralPath createPuzzlePath(int x, int y) {
        GeneralPath path = new GeneralPath();

        // Start from top-left corner
        path.moveTo(x, y);

        // Top edge with protrusion
        path.lineTo(x + (PUZZLE_SIZE - PUZZLE_RADIUS * 2) / 2, y);
        path.curveTo(
            x + PUZZLE_SIZE / 2 - PUZZLE_RADIUS, y - PUZZLE_RADIUS,
            x + PUZZLE_SIZE / 2 + PUZZLE_RADIUS, y - PUZZLE_RADIUS,
            x + (PUZZLE_SIZE + PUZZLE_RADIUS * 2) / 2, y
        );
        path.lineTo(x + PUZZLE_SIZE, y);

        // Right edge with protrusion
        path.lineTo(x + PUZZLE_SIZE, y + (PUZZLE_SIZE - PUZZLE_RADIUS * 2) / 2);
        path.curveTo(
            x + PUZZLE_SIZE + PUZZLE_RADIUS, y + PUZZLE_SIZE / 2 - PUZZLE_RADIUS,
            x + PUZZLE_SIZE + PUZZLE_RADIUS, y + PUZZLE_SIZE / 2 + PUZZLE_RADIUS,
            x + PUZZLE_SIZE, y + (PUZZLE_SIZE + PUZZLE_RADIUS * 2) / 2
        );
        path.lineTo(x + PUZZLE_SIZE, y + PUZZLE_SIZE);

        // Bottom edge
        path.lineTo(x, y + PUZZLE_SIZE);

        // Left edge
        path.lineTo(x, y);

        path.closePath();
        return path;
    }

    /**
     * Creates the puzzle piece image.
     * 创建拼图块图像。
     */
    private BufferedImage createPuzzlePiece(BufferedImage source, GeneralPath path, int x, int y) {
        BufferedImage piece = new BufferedImage(PUZZLE_SIZE + PUZZLE_RADIUS * 2,
                                                 PUZZLE_SIZE + PUZZLE_RADIUS * 2,
                                                 BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = piece.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Translate path to origin
        GeneralPath translatedPath = new GeneralPath(path);
        translatedPath.transform(java.awt.geom.AffineTransform.getTranslateInstance(-x + PUZZLE_RADIUS, -y + PUZZLE_RADIUS));

        // Clip to puzzle shape
        g.setClip(translatedPath);

        // Draw the source image portion
        g.drawImage(source,
            -x + PUZZLE_RADIUS, -y + PUZZLE_RADIUS,
            source.getWidth(), source.getHeight(),
            null);

        // Draw border
        g.setClip(null);
        g.setColor(new Color(255, 255, 255, 200));
        g.setStroke(new BasicStroke(2));
        g.draw(translatedPath);

        g.dispose();
        return piece;
    }

    /**
     * Cuts the puzzle shape from the background.
     * 从背景中切割拼图形状。
     */
    private void cutPuzzleFromBackground(BufferedImage image, GeneralPath path) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill puzzle area with darker color
        g.setColor(new Color(0, 0, 0, 100));
        g.fill(path);

        // Draw border
        g.setColor(new Color(0, 0, 0, 150));
        g.setStroke(new BasicStroke(2));
        g.draw(path);

        g.dispose();
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
