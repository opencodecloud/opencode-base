package cloud.opencode.base.image.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * PaddingOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
@DisplayName("PaddingOp 测试")
class PaddingOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(100, 80, BufferedImage.TYPE_INT_ARGB);
    }

    @Nested
    @DisplayName("pad(uniform) 方法测试")
    class UniformPadTests {

        @Test
        @DisplayName("四周等距填充宽高正确")
        void testUniformPadDimensions() {
            BufferedImage result = PaddingOp.pad(testImage, 10, Color.WHITE);

            assertThat(result.getWidth()).isEqualTo(120);  // 100 + 10 + 10
            assertThat(result.getHeight()).isEqualTo(100); // 80 + 10 + 10
        }

        @Test
        @DisplayName("填充区域颜色正确")
        void testUniformPadColor() {
            BufferedImage result = PaddingOp.pad(testImage, 10, Color.WHITE);

            // Check top-left corner pixel (should be WHITE)
            int argb = result.getRGB(0, 0);
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            assertThat(r).isEqualTo(255);
            assertThat(g).isEqualTo(255);
            assertThat(b).isEqualTo(255);
        }

        @Test
        @DisplayName("零填充返回等大小图像")
        void testZeroPad() {
            BufferedImage result = PaddingOp.pad(testImage, 0, Color.WHITE);

            assertThat(result.getWidth()).isEqualTo(100);
            assertThat(result.getHeight()).isEqualTo(80);
        }

        @Test
        @DisplayName("负填充抛出异常")
        void testNegativePadThrows() {
            assertThatThrownBy(() -> PaddingOp.pad(testImage, -1, Color.WHITE))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null图像抛出异常")
        void testNullImageThrows() {
            assertThatNullPointerException().isThrownBy(() ->
                    PaddingOp.pad(null, 10, Color.WHITE));
        }

        @Test
        @DisplayName("null颜色抛出异常")
        void testNullColorThrows() {
            assertThatNullPointerException().isThrownBy(() ->
                    PaddingOp.pad(testImage, 10, null));
        }

        @Test
        @DisplayName("极大填充值溢出抛出ArithmeticException")
        void testOverflowPadThrows() {
            assertThatThrownBy(() -> PaddingOp.pad(testImage, Integer.MAX_VALUE, Color.WHITE))
                    .isInstanceOf(ArithmeticException.class);
        }
    }

    @Nested
    @DisplayName("pad(TRBL) 方法测试")
    class TrblPadTests {

        @Test
        @DisplayName("四边独立填充宽高正确")
        void testTrblPadDimensions() {
            BufferedImage result = PaddingOp.pad(testImage, 5, 10, 15, 20, Color.BLACK);

            assertThat(result.getWidth()).isEqualTo(130);  // 100 + 10 + 20
            assertThat(result.getHeight()).isEqualTo(100); // 80 + 5 + 15
        }

        @Test
        @DisplayName("任意一边负值抛出异常")
        void testNegativeEdgeThrows() {
            assertThatThrownBy(() -> PaddingOp.pad(testImage, -1, 0, 0, 0, Color.BLACK))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> PaddingOp.pad(testImage, 0, -1, 0, 0, Color.BLACK))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("border 方法测试")
    class BorderTests {

        @Test
        @DisplayName("边框等同于等距填充")
        void testBorderEquivalentToPad() {
            BufferedImage bordered = PaddingOp.border(testImage, 5, Color.RED);
            BufferedImage padded = PaddingOp.pad(testImage, 5, Color.RED);

            assertThat(bordered.getWidth()).isEqualTo(padded.getWidth());
            assertThat(bordered.getHeight()).isEqualTo(padded.getHeight());
        }

        @Test
        @DisplayName("边框厚度2像素正确")
        void testBorderThickness() {
            BufferedImage result = PaddingOp.border(testImage, 2, Color.RED);

            assertThat(result.getWidth()).isEqualTo(104);
            assertThat(result.getHeight()).isEqualTo(84);
        }
    }
}
