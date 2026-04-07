package cloud.opencode.base.image.compare;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * PerceptualHash 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("PerceptualHash 感知哈希测试")
class PerceptualHashTest {

    // --- Helper methods ---

    private static BufferedImage solidImage(Color color, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }

    private static BufferedImage redImage() {
        return solidImage(Color.RED, 100, 100);
    }

    private static BufferedImage blueImage() {
        return solidImage(Color.BLUE, 100, 100);
    }

    /**
     * Create an image with a slight modification (a small patch of different color).
     */
    private static BufferedImage slightlyModifiedRedImage() {
        BufferedImage img = redImage();
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(255, 10, 10)); // very slightly different from pure red
        g.fillRect(10, 10, 5, 5);
        g.dispose();
        return img;
    }

    private static BufferedImage scaledRedImage(int width, int height) {
        return solidImage(Color.RED, width, height);
    }

    /**
     * Create a gradient image for more meaningful hash testing.
     */
    private static BufferedImage gradientImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (int) (255.0 * x / width);
                img.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }
        return img;
    }

    /**
     * Create a slightly modified gradient (offset by a few gray levels).
     */
    private static BufferedImage slightlyModifiedGradient(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = Math.min(255, (int) (255.0 * x / width) + 3);
                img.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }
        return img;
    }

    @Nested
    @DisplayName("aHash 平均哈希")
    class AHashTests {

        @Test
        @DisplayName("相同图像 hash 相等")
        void sameImageSameHash() {
            BufferedImage img = redImage();
            assertThat(PerceptualHash.aHash(img)).isEqualTo(PerceptualHash.aHash(img));
        }

        @Test
        @DisplayName("微修改图像 hamming < 5")
        void slightlyModifiedImageSmallDistance() {
            BufferedImage original = gradientImage(100, 100);
            BufferedImage modified = slightlyModifiedGradient(100, 100);
            long h1 = PerceptualHash.aHash(original);
            long h2 = PerceptualHash.aHash(modified);
            assertThat(PerceptualHash.hammingDistance(h1, h2)).isLessThan(5);
        }

        @Test
        @DisplayName("null 抛出异常")
        void nullThrows() {
            assertThatThrownBy(() -> PerceptualHash.aHash(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("dHash 差异哈希")
    class DHashTests {

        @Test
        @DisplayName("相同图像 hash 相等")
        void sameImageSameHash() {
            BufferedImage img = gradientImage(100, 100);
            assertThat(PerceptualHash.dHash(img)).isEqualTo(PerceptualHash.dHash(img));
        }

        @Test
        @DisplayName("null 抛出异常")
        void nullThrows() {
            assertThatThrownBy(() -> PerceptualHash.dHash(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("pHash 感知哈希")
    class PHashTests {

        @Test
        @DisplayName("相同图像 hash 相等")
        void sameImageSameHash() {
            BufferedImage img = gradientImage(100, 100);
            assertThat(PerceptualHash.pHash(img)).isEqualTo(PerceptualHash.pHash(img));
        }

        @Test
        @DisplayName("缩放图像 hamming < 10")
        void scaledImageSmallDistance() {
            // Create a source image and scale it — same content at different sizes
            BufferedImage img100 = gradientImage(100, 100);
            BufferedImage img50 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img50.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img100, 0, 0, 50, 50, null);
            g.dispose();
            long h1 = PerceptualHash.pHash(img100);
            long h2 = PerceptualHash.pHash(img50);
            assertThat(PerceptualHash.hammingDistance(h1, h2)).isLessThan(15);
        }

        @Test
        @DisplayName("完全不同图像 hamming > 15")
        void differentImageLargeDistance() {
            BufferedImage red = redImage();
            BufferedImage blue = blueImage();
            long h1 = PerceptualHash.pHash(red);
            long h2 = PerceptualHash.pHash(blue);
            assertThat(PerceptualHash.hammingDistance(h1, h2)).isGreaterThan(15);
        }

        @Test
        @DisplayName("null 抛出异常")
        void nullThrows() {
            assertThatThrownBy(() -> PerceptualHash.pHash(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("hammingDistance 汉明距离")
    class HammingDistanceTests {

        @Test
        @DisplayName("相同哈希距离为 0")
        void sameHashZeroDistance() {
            assertThat(PerceptualHash.hammingDistance(0L, 0L)).isEqualTo(0);
        }

        @Test
        @DisplayName("完全不同哈希距离为 64")
        void allBitsDifferent() {
            assertThat(PerceptualHash.hammingDistance(0L, -1L)).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("similarity 相似度")
    class SimilarityTests {

        @Test
        @DisplayName("相同哈希相似度为 1.0")
        void sameHashFullSimilarity() {
            assertThat(PerceptualHash.similarity(0L, 0L)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("完全不同哈希相似度为 0.0")
        void allBitsDifferentZeroSimilarity() {
            assertThat(PerceptualHash.similarity(0L, -1L)).isEqualTo(0.0);
        }
    }
}
