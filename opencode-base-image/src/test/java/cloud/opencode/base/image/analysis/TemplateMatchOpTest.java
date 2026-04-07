package cloud.opencode.base.image.analysis;

import cloud.opencode.base.image.analysis.TemplateMatchOp.MatchResult;
import cloud.opencode.base.image.exception.ImageOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TemplateMatchOp 模板匹配测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("TemplateMatchOp 模板匹配测试")
class TemplateMatchOpTest {

    /**
     * Create a black image of specified size.
     */
    private static BufferedImage createBlackImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }

    /**
     * Create a source image with a known white rectangle at the specified position.
     */
    private static BufferedImage createSourceWithRect(int imgW, int imgH,
                                                      int rx, int ry, int rw, int rh) {
        BufferedImage img = createBlackImage(imgW, imgH);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(rx, ry, rw, rh);
        g.dispose();
        return img;
    }

    /**
     * Crop a sub-image from the source.
     */
    private static BufferedImage crop(BufferedImage source, int x, int y, int w, int h) {
        BufferedImage cropped = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cropped.createGraphics();
        g.drawImage(source, 0, 0, w, h, x, y, x + w, y + h, null);
        g.dispose();
        return cropped;
    }

    @Nested
    @DisplayName("最佳匹配测试")
    class BestMatchTests {

        @Test
        @DisplayName("模板是源图像的裁剪区域，分数接近 1.0，位置正确")
        void templateIsCropOfSource() {
            // Create source with a distinctive pattern
            BufferedImage source = createBlackImage(40, 40);
            Graphics2D g = source.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(10, 10, 8, 8);
            // Add some gray variation
            g.setColor(new Color(128, 128, 128));
            g.fillRect(12, 12, 4, 4);
            g.dispose();

            // Crop template from the exact location
            BufferedImage template = crop(source, 10, 10, 8, 8);

            MatchResult result = TemplateMatchOp.match(source, template);

            assertThat(result.x()).isEqualTo(10);
            assertThat(result.y()).isEqualTo(10);
            assertThat(result.score()).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("不匹配的模板分数较低")
        void templateNotInSource() {
            // Source: all black with white rect at center
            BufferedImage source = createSourceWithRect(30, 30, 10, 10, 5, 5);

            // Template: specific gray pattern that doesn't exist in source
            BufferedImage template = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = template.createGraphics();
            // Create a gradient-like pattern
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) {
                    int gray = (x + y) * 25 + 10;
                    g.setColor(new Color(gray, gray, gray));
                    g.fillRect(x, y, 1, 1);
                }
            }
            g.dispose();

            MatchResult result = TemplateMatchOp.match(source, template);

            // Score should not be close to 1.0
            assertThat(result.score()).isLessThan(0.9);
        }
    }

    @Nested
    @DisplayName("多匹配测试")
    class MultiMatchTests {

        @Test
        @DisplayName("源图像中有两个相同模板的拷贝，matchAll 找到 2 个匹配")
        void findsTwoCopiesOfTemplate() {
            // Create source with two identical white rectangles on black
            BufferedImage source = createBlackImage(50, 20);
            Graphics2D g = source.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(5, 5, 6, 6);    // Copy 1
            g.fillRect(30, 5, 6, 6);   // Copy 2
            // Add gray center for distinctiveness
            g.setColor(new Color(128, 128, 128));
            g.fillRect(7, 7, 2, 2);    // Copy 1 detail
            g.fillRect(32, 7, 2, 2);   // Copy 2 detail
            g.dispose();

            // Template: crop from first copy
            BufferedImage template = crop(source, 5, 5, 6, 6);

            List<MatchResult> results = TemplateMatchOp.matchAll(source, template, 0.9);

            assertThat(results).hasSizeGreaterThanOrEqualTo(2);

            // Both matches should have high scores
            for (MatchResult r : results) {
                assertThat(r.score()).isGreaterThanOrEqualTo(0.9);
            }

            // Check that both positions are found (approximately)
            boolean foundFirst = results.stream()
                    .anyMatch(r -> Math.abs(r.x() - 5) <= 1 && Math.abs(r.y() - 5) <= 1);
            boolean foundSecond = results.stream()
                    .anyMatch(r -> Math.abs(r.x() - 30) <= 1 && Math.abs(r.y() - 5) <= 1);
            assertThat(foundFirst).isTrue();
            assertThat(foundSecond).isTrue();
        }

        @Test
        @DisplayName("阈值为 1.0 时只返回完美匹配")
        void highThresholdFilters() {
            BufferedImage source = createSourceWithRect(30, 30, 10, 10, 5, 5);
            BufferedImage template = crop(source, 10, 10, 5, 5);

            List<MatchResult> results = TemplateMatchOp.matchAll(source, template, 1.0);

            // Should find exact match
            assertThat(results).isNotEmpty();
            for (MatchResult r : results) {
                assertThat(r.score()).isGreaterThanOrEqualTo(1.0 - 1e-6);
            }
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionTests {

        @Test
        @DisplayName("模板大于源图像应抛出 ImageOperationException")
        void templateLargerThanSource() {
            BufferedImage source = createBlackImage(10, 10);
            BufferedImage template = createBlackImage(20, 20);

            assertThatThrownBy(() -> TemplateMatchOp.match(source, template))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("larger");
        }

        @Test
        @DisplayName("模板宽度大于源图像应抛出异常")
        void templateWiderThanSource() {
            BufferedImage source = createBlackImage(10, 20);
            BufferedImage template = createBlackImage(15, 5);

            assertThatThrownBy(() -> TemplateMatchOp.match(source, template))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("null 源图像应抛出 NullPointerException")
        void nullSourceThrowsNpe() {
            BufferedImage template = createBlackImage(5, 5);

            assertThatNullPointerException()
                    .isThrownBy(() -> TemplateMatchOp.match(null, template))
                    .withMessageContaining("null");
        }

        @Test
        @DisplayName("null 模板应抛出 NullPointerException")
        void nullTemplateThrowsNpe() {
            BufferedImage source = createBlackImage(10, 10);

            assertThatNullPointerException()
                    .isThrownBy(() -> TemplateMatchOp.match(source, null))
                    .withMessageContaining("null");
        }

        @Test
        @DisplayName("matchAll 的 null 参数应抛出 NullPointerException")
        void matchAllNullThrowsNpe() {
            BufferedImage img = createBlackImage(10, 10);

            assertThatNullPointerException()
                    .isThrownBy(() -> TemplateMatchOp.matchAll(null, img, 0.5))
                    .withMessageContaining("null");

            assertThatNullPointerException()
                    .isThrownBy(() -> TemplateMatchOp.matchAll(img, null, 0.5))
                    .withMessageContaining("null");
        }

        @Test
        @DisplayName("matchAll 中模板大于源图像应抛出异常")
        void matchAllTemplateLargerThrows() {
            BufferedImage source = createBlackImage(5, 5);
            BufferedImage template = createBlackImage(10, 10);

            assertThatThrownBy(() -> TemplateMatchOp.matchAll(source, template, 0.5))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("无效阈值应抛出 ImageOperationException")
        void invalidThresholdThrows() {
            BufferedImage source = createBlackImage(10, 10);
            BufferedImage template = createBlackImage(5, 5);

            assertThatThrownBy(() -> TemplateMatchOp.matchAll(source, template, 1.5))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("Threshold");

            assertThatThrownBy(() -> TemplateMatchOp.matchAll(source, template, -1.5))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("Threshold");
        }
    }

    @Nested
    @DisplayName("NCC 分数范围测试")
    class ScoreRangeTests {

        @Test
        @DisplayName("匹配分数在 [-1, 1] 范围内")
        void scoreInValidRange() {
            BufferedImage source = createSourceWithRect(20, 20, 5, 5, 6, 6);
            BufferedImage template = createBlackImage(4, 4);
            Graphics2D g = template.createGraphics();
            g.setColor(new Color(100, 100, 100));
            g.fillRect(0, 0, 2, 2);
            g.setColor(new Color(200, 200, 200));
            g.fillRect(2, 2, 2, 2);
            g.dispose();

            MatchResult result = TemplateMatchOp.match(source, template);

            assertThat(result.score()).isBetween(-1.0, 1.0);
        }
    }

    @Nested
    @DisplayName("同尺寸匹配测试")
    class SameSizeTests {

        @Test
        @DisplayName("模板与源图像尺寸相同时应正常匹配")
        void sameSizeTemplateAndSource() {
            BufferedImage source = createSourceWithRect(10, 10, 2, 2, 5, 5);
            BufferedImage template = crop(source, 0, 0, 10, 10);

            MatchResult result = TemplateMatchOp.match(source, template);

            assertThat(result.x()).isEqualTo(0);
            assertThat(result.y()).isEqualTo(0);
            assertThat(result.score()).isCloseTo(1.0, within(0.01));
        }
    }
}
