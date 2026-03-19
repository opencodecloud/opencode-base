package cloud.opencode.base.captcha.interactive;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.exception.CaptchaGenerationException;
import cloud.opencode.base.captcha.generator.CaptchaGenerator;
import cloud.opencode.base.captcha.support.CaptchaChars;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * Image Select Captcha Generator - Generates image selection CAPTCHA
 * 图片选择验证码生成器 - 生成图片选择验证码
 *
 * <p>This generator creates image selection CAPTCHAs where users
 * must select images matching a category.</p>
 * <p>此生成器创建图片选择验证码，
 * 用户必须选择匹配类别的图片。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Image selection verification - 图片选择验证</li>
 *   <li>Category-based challenge - 基于类别的挑战</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator gen = new ImageSelectCaptchaGenerator();
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
public final class ImageSelectCaptchaGenerator implements CaptchaGenerator {

    private static final int GRID_SIZE = 3; // 3x3 grid
    private static final int CELL_SIZE = 80;
    private static final int GAP = 4;

    // Simple shape categories represented by shapes
    private static final String[] CATEGORIES = {"circle", "square", "triangle", "star"};

    @Override
    public Captcha generate(CaptchaConfig config) {
        try {
            // Select target category
            String targetCategory = CATEGORIES[CaptchaChars.randomInt(CATEGORIES.length)];

            // Generate grid with some matching and some non-matching
            int totalCells = GRID_SIZE * GRID_SIZE;
            int targetCount = CaptchaChars.randomInt(2, 5); // 2-4 targets

            // Create array of categories
            String[] cellCategories = new String[totalCells];
            List<Integer> targetIndices = new ArrayList<>();

            // Randomly place targets
            Set<Integer> targetSet = new HashSet<>();
            while (targetSet.size() < targetCount) {
                targetSet.add(CaptchaChars.randomInt(totalCells));
            }
            targetIndices.addAll(targetSet);
            Collections.sort(targetIndices);

            // Fill cells
            for (int i = 0; i < totalCells; i++) {
                if (targetSet.contains(i)) {
                    cellCategories[i] = targetCategory;
                } else {
                    // Random non-matching category
                    String cat;
                    do {
                        cat = CATEGORIES[CaptchaChars.randomInt(CATEGORIES.length)];
                    } while (cat.equals(targetCategory));
                    cellCategories[i] = cat;
                }
            }

            // Create image
            int gridWidth = GRID_SIZE * CELL_SIZE + (GRID_SIZE - 1) * GAP;
            int gridHeight = gridWidth;

            BufferedImage image = new BufferedImage(gridWidth, gridHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fill background
            g.setColor(new Color(240, 240, 240));
            g.fillRect(0, 0, gridWidth, gridHeight);

            // Draw cells
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    int index = row * GRID_SIZE + col;
                    int x = col * (CELL_SIZE + GAP);
                    int y = row * (CELL_SIZE + GAP);

                    drawCell(g, x, y, cellCategories[index]);
                }
            }

            g.dispose();

            // Create answer (comma-separated target indices)
            StringBuilder answer = new StringBuilder();
            for (int i = 0; i < targetIndices.size(); i++) {
                if (i > 0) answer.append(",");
                answer.append(targetIndices.get(i));
            }

            // Create metadata
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("width", gridWidth);
            metadata.put("height", gridHeight);
            metadata.put("gridSize", GRID_SIZE);
            metadata.put("cellSize", CELL_SIZE);
            metadata.put("gap", GAP);
            metadata.put("targetCategory", targetCategory);
            metadata.put("targetCount", targetCount);
            metadata.put("targetIndices", targetIndices);
            metadata.put("prompt", "Select all " + targetCategory + " shapes");

            byte[] imageData = toBytes(image);

            Instant now = Instant.now();
            return new Captcha(
                UUID.randomUUID().toString().replace("-", ""),
                CaptchaType.IMAGE_SELECT,
                imageData,
                answer.toString(),
                metadata,
                now,
                now.plus(config.getExpireTime())
            );
        } catch (Exception e) {
            throw new CaptchaGenerationException("Failed to generate image select CAPTCHA", CaptchaType.IMAGE_SELECT, e);
        }
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.IMAGE_SELECT;
    }

    /**
     * Draws a cell with the specified shape category.
     * 绘制具有指定形状类别的单元格。
     */
    private void drawCell(Graphics2D g, int x, int y, String category) {
        // Draw cell background
        g.setColor(Color.WHITE);
        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);

        // Draw border
        g.setColor(new Color(200, 200, 200));
        g.drawRect(x, y, CELL_SIZE, CELL_SIZE);

        // Draw shape
        Color shapeColor = new Color(
            CaptchaChars.randomInt(50, 200),
            CaptchaChars.randomInt(50, 200),
            CaptchaChars.randomInt(50, 200)
        );
        g.setColor(shapeColor);

        int margin = 10;
        int shapeSize = CELL_SIZE - 2 * margin;
        int cx = x + margin;
        int cy = y + margin;

        switch (category) {
            case "circle" -> g.fillOval(cx, cy, shapeSize, shapeSize);
            case "square" -> g.fillRect(cx, cy, shapeSize, shapeSize);
            case "triangle" -> {
                int[] xPoints = {cx + shapeSize / 2, cx, cx + shapeSize};
                int[] yPoints = {cy, cy + shapeSize, cy + shapeSize};
                g.fillPolygon(xPoints, yPoints, 3);
            }
            case "star" -> drawStar(g, cx + shapeSize / 2, cy + shapeSize / 2, shapeSize / 2);
        }
    }

    /**
     * Draws a star shape.
     * 绘制星形。
     */
    private void drawStar(Graphics2D g, int cx, int cy, int radius) {
        int points = 5;
        int[] xPoints = new int[points * 2];
        int[] yPoints = new int[points * 2];

        double angle = -Math.PI / 2;
        double angleStep = Math.PI / points;

        for (int i = 0; i < points * 2; i++) {
            int r = (i % 2 == 0) ? radius : radius / 2;
            xPoints[i] = cx + (int) (r * Math.cos(angle));
            yPoints[i] = cy + (int) (r * Math.sin(angle));
            angle += angleStep;
        }

        g.fillPolygon(xPoints, yPoints, points * 2);
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
