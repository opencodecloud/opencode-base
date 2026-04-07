package cloud.opencode.base.captcha.interactive;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.exception.CaptchaGenerationException;
import cloud.opencode.base.captcha.generator.AbstractCaptchaGenerator;
import cloud.opencode.base.captcha.generator.CaptchaGenerator;
import cloud.opencode.base.captcha.support.CaptchaChars;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Jigsaw Captcha Generator — Generates puzzle piece ordering CAPTCHA
 * 拼接验证码生成器 — 生成碎片排序验证码
 *
 * <p>This generator creates a jigsaw-style interactive CAPTCHA where
 * an image is split into a grid of pieces that are shuffled. The user
 * must arrange the pieces back into the correct order.</p>
 * <p>此生成器创建拼图式交互验证码，将图像切割成网格碎片并打乱顺序。
 * 用户需要将碎片重新排列为正确顺序。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Grid-based puzzle piece ordering - 基于网格的碎片排序</li>
 *   <li>Procedurally generated background (no external images) - 程序生成背景（无需外部图片）</li>
 *   <li>Fisher-Yates shuffle with guaranteed displacement - Fisher-Yates 洗牌确保至少一个碎片移位</li>
 *   <li>Base64-encoded piece images in metadata - 元数据中包含 Base64 编码的碎片图像</li>
 *   <li>Configurable grid size (default 3x3) - 可配置网格大小（默认 3x3）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaGenerator gen = new JigsawCaptchaGenerator();
 * Captcha captcha = gen.generate(config);
 *
 * // Retrieve shuffled piece images from metadata
 * List<String> pieces = (List<String>) captcha.metadata().get("pieces");
 * int gridSize = (int) captcha.metadata().get("gridSize");
 *
 * // User submits answer as comma-separated positions: "1,2,0,..."
 * // Answer represents the position of each original piece in the shuffled layout
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No (config must not be null) - 空值安全: 否（配置不能为null）</li>
 *   <li>Low entropy: A 3x3 grid has 9! = 362,880 permutations. Must be used with
 *       BehaviorAnalyzer and CaptchaRateLimiter for adequate protection.
 *       低熵: 3x3 网格有 9! = 362,880 种排列。必须配合 BehaviorAnalyzer 和 CaptchaRateLimiter 使用。</li>
 * </ul>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>Image generation is O(width * height) for background rendering - 背景渲染时间复杂度 O(width * height)</li>
 *   <li>Piece cutting and encoding is O(gridSize^2 * pieceArea) - 碎片切割和编码 O(gridSize^2 * pieceArea)</li>
 *   <li>Each piece is independently PNG-encoded for transport - 每个碎片独立 PNG 编码以便传输</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class JigsawCaptchaGenerator extends AbstractCaptchaGenerator
        implements CaptchaGenerator {

    /**
     * Default grid size (3x3 = 9 pieces).
     * 默认网格大小（3x3 = 9 块）。
     */
    private static final int DEFAULT_GRID_SIZE = 3;

    /**
     * Minimum number of random geometric shapes to draw on the background.
     * 背景上绘制的最少随机几何形状数量。
     */
    private static final int MIN_SHAPES = 5;

    /**
     * Maximum number of random geometric shapes to draw on the background.
     * 背景上绘制的最多随机几何形状数量。
     */
    private static final int MAX_SHAPES = 10;

    /**
     * Number of thick crossing lines to draw for visual distinctiveness.
     * 为增加视觉辨识度绘制的粗线条数量。
     */
    private static final int CROSSING_LINES = 3;

    @Override
    public CaptchaType getType() {
        return CaptchaType.JIGSAW;
    }

    @Override
    public Captcha generate(CaptchaConfig config) {
        int width = config.getWidth();
        int height = config.getHeight();
        int gridSize = DEFAULT_GRID_SIZE;

        try {
            // 1. Generate background image (random gradient + geometric shapes, no external images)
            BufferedImage background = createJigsawBackground(width, height);

            // 2. Cut image into gridSize x gridSize pieces
            BufferedImage[] pieces = cutIntoPieces(background, gridSize);

            // 3. Generate correct order indices [0, 1, 2, ..., n-1]
            int totalPieces = gridSize * gridSize;
            int[] correctOrder = new int[totalPieces];
            for (int i = 0; i < totalPieces; i++) {
                correctOrder[i] = i;
            }

            // 4. Randomly shuffle the order
            int[] shuffledOrder = shuffle(correctOrder.clone());

            // 5. Encode shuffled pieces as Base64
            List<String> pieceImages = new ArrayList<>(totalPieces);
            for (int idx : shuffledOrder) {
                pieceImages.add(Base64.getEncoder().encodeToString(toBytes(pieces[idx])));
            }

            // 6. Build answer: for each original piece i, find its position in shuffledOrder
            //    e.g. shuffledOrder = [2,0,1] → piece 0 is at position 1,
            //    piece 1 is at position 2, piece 2 is at position 0 → answer = "1,2,0"
            String answer = buildAnswer(shuffledOrder);

            // 7. Build metadata
            Map<String, Object> metadata = new LinkedHashMap<>(createMetadata(config));
            metadata.put("pieces", pieceImages);
            metadata.put("gridSize", gridSize);

            // 8. imageData is the original complete image as PNG
            byte[] imageData = toBytes(background);

            String id = generateId();
            Instant now = Instant.now();
            return new Captcha(
                    id,
                    CaptchaType.JIGSAW,
                    imageData,
                    answer,
                    metadata,
                    now,
                    now.plus(config.getExpireTime())
            );
        } catch (RuntimeException e) {
            throw new CaptchaGenerationException(
                    "Failed to generate jigsaw CAPTCHA", CaptchaType.JIGSAW, e);
        }
    }

    /**
     * Creates a jigsaw background image — no external image resources required.
     * Uses random gradient colors + random geometric shapes to produce a visually
     * distinctive image suitable for puzzle identification.
     * 创建拼接背景图 — 不依赖外部图片资源。
     * 使用随机渐变色 + 随机几何形状生成有辨识度的图片，适合拼图识别。
     *
     * @param width  the image width | 图像宽度
     * @param height the image height | 图像高度
     * @return the generated background image | 生成的背景图像
     */
    private BufferedImage createJigsawBackground(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        try {
            // 1. Draw random gradient background
            Color startColor = randomColor(80, 180);
            Color endColor = randomColor(80, 180);
            GradientPaint gradient = new GradientPaint(0, 0, startColor, width, height, endColor);
            g.setPaint(gradient);
            g.fillRect(0, 0, width, height);

            // 2. Draw 5-10 random geometric shapes (circles, rectangles, triangles)
            //    with different colors for visual distinctiveness between pieces
            int shapeCount = CaptchaChars.randomInt(MIN_SHAPES, MAX_SHAPES + 1);
            for (int i = 0; i < shapeCount; i++) {
                drawRandomShape(g, width, height);
            }

            // 3. Draw 2-3 thick crossing lines to increase recognizability
            g.setStroke(new BasicStroke(3.0f));
            for (int i = 0; i < CROSSING_LINES; i++) {
                g.setColor(randomColor(60, 220, 180));
                int x1 = CaptchaChars.randomInt(width);
                int y1 = CaptchaChars.randomInt(height);
                int x2 = CaptchaChars.randomInt(width);
                int y2 = CaptchaChars.randomInt(height);
                g.drawLine(x1, y1, x2, y2);
            }
        } finally {
            g.dispose();
        }

        return image;
    }

    /**
     * Draws a random geometric shape on the graphics context.
     * 在图形上下文上绘制一个随机几何形状。
     *
     * @param g      the graphics context | 图形上下文
     * @param width  the image width | 图像宽度
     * @param height the image height | 图像高度
     */
    private void drawRandomShape(Graphics2D g, int width, int height) {
        g.setColor(randomColor(50, 230, 150));
        int x = CaptchaChars.randomInt(width);
        int y = CaptchaChars.randomInt(height);
        int size = CaptchaChars.randomInt(20, Math.max(21, Math.min(width, height) / 3));
        int shapeType = CaptchaChars.randomInt(4);

        switch (shapeType) {
            case 0 -> g.fillOval(x, y, size, size);
            case 1 -> g.fillRect(x, y, size, size / 2 + 1);
            case 2 -> {
                // Triangle
                int[] xPoints = {x, x + size / 2, x + size};
                int[] yPoints = {y + size, y, y + size};
                g.fillPolygon(xPoints, yPoints, 3);
            }
            case 3 -> {
                // Diamond
                int half = size / 2;
                int[] xPoints = {x + half, x + size, x + half, x};
                int[] yPoints = {y, y + half, y + size, y + half};
                g.fillPolygon(xPoints, yPoints, 4);
            }
            default -> g.fillOval(x, y, size, size);
        }
    }

    /**
     * Cuts the image into gridSize x gridSize independent pieces.
     * Note: getSubimage returns a view of the original image, so each piece
     * is copied to a new independent BufferedImage for correct encoding.
     * 将图像切割为 gridSize x gridSize 块独立碎片。
     * 注意：getSubimage 返回原图的视图，因此每块碎片需要复制为独立的 BufferedImage 以便正确编码。
     *
     * @param image    the source image | 源图像
     * @param gridSize the grid size (pieces per row/column) | 网格大小（每行/列的碎片数）
     * @return array of independent piece images | 独立碎片图像数组
     */
    private BufferedImage[] cutIntoPieces(BufferedImage image, int gridSize) {
        int pieceW = image.getWidth() / gridSize;
        int pieceH = image.getHeight() / gridSize;
        BufferedImage[] pieces = new BufferedImage[gridSize * gridSize];

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                // getSubimage returns a view — copy to independent image
                BufferedImage subView = image.getSubimage(
                        col * pieceW, row * pieceH, pieceW, pieceH);
                BufferedImage independent = new BufferedImage(
                        pieceW, pieceH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = independent.createGraphics();
                try {
                    g.drawImage(subView, 0, 0, null);
                } finally {
                    g.dispose();
                }
                pieces[row * gridSize + col] = independent;
            }
        }

        return pieces;
    }

    /**
     * Fisher-Yates shuffle with guaranteed displacement.
     * Ensures at least one element is not in its original position,
     * so the puzzle is always a valid challenge.
     * Fisher-Yates 洗牌，保证至少有一个位置发生变化，
     * 确保拼图始终是有效的挑战。
     *
     * @param array the array to shuffle (modified in place) | 待洗牌的数组（原地修改）
     * @return the shuffled array | 洗牌后的数组
     */
    private int[] shuffle(int[] array) {
        Random random = CaptchaChars.getRandom();
        // Fisher-Yates shuffle
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }

        // Ensure at least one position is different (not identity permutation)
        boolean allSame = true;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != i) {
                allSame = false;
                break;
            }
        }
        if (allSame && array.length > 1) {
            // Swap the first two elements
            int temp = array[0];
            array[0] = array[1];
            array[1] = temp;
        }

        return array;
    }

    /**
     * Builds the answer string from the shuffled order.
     * The shuffled order indicates: position i displays original piece shuffledOrder[i].
     * The answer is: for each original piece k, find the position where it appears.
     * Format: "pos0,pos1,pos2,..." where posK is the position of original piece K.
     * 根据打乱顺序构建答案字符串。
     * 打乱顺序表示：位置 i 显示原始碎片 shuffledOrder[i]。
     * 答案是：对于每个原始碎片 k，找到它所在的位置。
     * 格式: "pos0,pos1,pos2,..." 其中 posK 是原始碎片 K 的位置。
     *
     * @param shuffledOrder the shuffled order array | 打乱顺序数组
     * @return the answer string | 答案字符串
     */
    private String buildAnswer(int[] shuffledOrder) {
        int[] positions = new int[shuffledOrder.length];
        for (int i = 0; i < shuffledOrder.length; i++) {
            positions[shuffledOrder[i]] = i;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < positions.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(positions[i]);
        }
        return sb.toString();
    }

    /**
     * Creates a random opaque color with RGB values in the specified range.
     * 创建 RGB 值在指定范围内的随机不透明颜色。
     *
     * @param min minimum RGB component value (inclusive) | 最小 RGB 分量值（含）
     * @param max maximum RGB component value (exclusive) | 最大 RGB 分量值（不含）
     * @return the random color | 随机颜色
     */
    private Color randomColor(int min, int max) {
        return new Color(
                CaptchaChars.randomInt(min, max),
                CaptchaChars.randomInt(min, max),
                CaptchaChars.randomInt(min, max)
        );
    }

    /**
     * Creates a random color with the specified alpha transparency.
     * 创建具有指定 alpha 透明度的随机颜色。
     *
     * @param min   minimum RGB component value (inclusive) | 最小 RGB 分量值（含）
     * @param max   maximum RGB component value (exclusive) | 最大 RGB 分量值（不含）
     * @param alpha the alpha transparency (0-255) | alpha 透明度（0-255）
     * @return the random color | 随机颜色
     */
    private Color randomColor(int min, int max, int alpha) {
        return new Color(
                CaptchaChars.randomInt(min, max),
                CaptchaChars.randomInt(min, max),
                CaptchaChars.randomInt(min, max),
                alpha
        );
    }
}
