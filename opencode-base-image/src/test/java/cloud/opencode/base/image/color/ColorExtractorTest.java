package cloud.opencode.base.image.color;

import cloud.opencode.base.image.exception.ImageOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ColorExtractor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
@DisplayName("ColorExtractor 测试")
class ColorExtractorTest {

    private BufferedImage redImage;
    private BufferedImage halfRedHalfBlue;

    @BeforeEach
    void setUp() {
        // Solid red image
        redImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                redImage.setRGB(x, y, 0xFFFF0000);
            }
        }

        // Left half red, right half blue
        halfRedHalfBlue = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 100; y++) {
                halfRedHalfBlue.setRGB(x, y, 0xFFFF0000);
            }
        }
        for (int x = 50; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                halfRedHalfBlue.setRGB(x, y, 0xFF0000FF);
            }
        }
    }

    @Nested
    @DisplayName("dominantColors 方法测试")
    class DominantColorsTests {

        @Test
        @DisplayName("纯红图返回1个主色")
        void testSolidRedDominantColor() {
            List<Color> colors = ColorExtractor.dominantColors(redImage, 1);

            assertThat(colors).hasSize(1);
            Color c = colors.getFirst();
            assertThat(c.getRed()).isGreaterThan(200);
            assertThat(c.getGreen()).isLessThan(50);
            assertThat(c.getBlue()).isLessThan(50);
        }

        @Test
        @DisplayName("双色图提取2个主色")
        void testTwoColorsExtract() {
            List<Color> colors = ColorExtractor.dominantColors(halfRedHalfBlue, 2);

            assertThat(colors).hasSize(2);
        }

        @Test
        @DisplayName("count超出范围抛出异常")
        void testCountOutOfRange() {
            assertThatThrownBy(() -> ColorExtractor.dominantColors(redImage, 0))
                    .isInstanceOf(ImageOperationException.class);
            assertThatThrownBy(() -> ColorExtractor.dominantColors(redImage, 21))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("null 图像抛出 NullPointerException")
        void testNullImage() {
            assertThatNullPointerException().isThrownBy(() ->
                    ColorExtractor.dominantColors(null, 1));
        }

        @Test
        @DisplayName("count=1 与 dominantColor 结果一致")
        void testCountOneMatchesDominantColor() {
            List<Color> colors = ColorExtractor.dominantColors(redImage, 1);
            Color dominant = ColorExtractor.dominantColor(redImage);

            assertThat(colors.getFirst().getRed()).isEqualTo(dominant.getRed());
            assertThat(colors.getFirst().getGreen()).isEqualTo(dominant.getGreen());
            assertThat(colors.getFirst().getBlue()).isEqualTo(dominant.getBlue());
        }

        @Test
        @DisplayName("返回列表不超过请求数量")
        void testReturnedSizeNotExceedCount() {
            List<Color> colors = ColorExtractor.dominantColors(redImage, 5);

            assertThat(colors).hasSizeLessThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("dominantColor 方法测试")
    class DominantColorTests {

        @Test
        @DisplayName("纯红图返回红色")
        void testDominantColorRed() {
            Color c = ColorExtractor.dominantColor(redImage);

            assertThat(c.getRed()).isGreaterThan(200);
            assertThat(c.getGreen()).isLessThan(50);
            assertThat(c.getBlue()).isLessThan(50);
        }

        @Test
        @DisplayName("null 图像抛出 NullPointerException")
        void testDominantColorNull() {
            assertThatNullPointerException().isThrownBy(() ->
                    ColorExtractor.dominantColor(null));
        }
    }
}
