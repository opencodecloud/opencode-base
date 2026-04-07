package cloud.opencode.base.image.color;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ColorSpaceOp 颜色空间转换测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("ColorSpaceOp 颜色空间转换测试")
class ColorSpaceOpTest {

    private static final int RED = PixelOp.argb(255, 255, 0, 0);
    private static final int GREEN = PixelOp.argb(255, 0, 255, 0);
    private static final int BLUE = PixelOp.argb(255, 0, 0, 255);
    private static final int WHITE = PixelOp.argb(255, 255, 255, 255);
    private static final int BLACK = PixelOp.argb(255, 0, 0, 0);

    private static int[] testPixels() {
        return new int[]{RED, GREEN, BLUE, WHITE, BLACK,
                PixelOp.argb(255, 128, 64, 192),
                PixelOp.argb(255, 10, 200, 100),
                PixelOp.argb(255, 255, 128, 0)};
    }

    /**
     * Assert roundtrip pixel error is within tolerance.
     */
    private static void assertRoundtrip(int[] original, int[] restored, int maxError) {
        assertThat(restored).hasSameSizeAs(original);
        for (int i = 0; i < original.length; i++) {
            int r1 = PixelOp.red(original[i]);
            int g1 = PixelOp.green(original[i]);
            int b1 = PixelOp.blue(original[i]);
            int r2 = PixelOp.red(restored[i]);
            int g2 = PixelOp.green(restored[i]);
            int b2 = PixelOp.blue(restored[i]);
            assertThat(Math.abs(r1 - r2))
                    .as("Red channel difference at pixel %d: expected %d got %d", i, r1, r2)
                    .isLessThanOrEqualTo(maxError);
            assertThat(Math.abs(g1 - g2))
                    .as("Green channel difference at pixel %d: expected %d got %d", i, g1, g2)
                    .isLessThanOrEqualTo(maxError);
            assertThat(Math.abs(b1 - b2))
                    .as("Blue channel difference at pixel %d: expected %d got %d", i, b1, b2)
                    .isLessThanOrEqualTo(maxError);
        }
    }

    @Nested
    @DisplayName("HSV 转换测试")
    class HsvTests {

        @Test
        @DisplayName("toHsv -> fromHsv 往返误差 < 2")
        void roundtrip() {
            int[] pixels = testPixels();
            float[][] hsv = ColorSpaceOp.toHsv(pixels);
            int[] restored = ColorSpaceOp.fromHsv(hsv);
            assertRoundtrip(pixels, restored, 2);
        }

        @Test
        @DisplayName("纯红色 (255,0,0) -> HSV(0, 1, 1)")
        void pureRed() {
            float[][] hsv = ColorSpaceOp.toHsv(new int[]{RED});
            assertThat(hsv[0][0]).isCloseTo(0.0f, within(0.1f));
            assertThat(hsv[1][0]).isCloseTo(1.0f, within(0.001f));
            assertThat(hsv[2][0]).isCloseTo(1.0f, within(0.001f));
        }

        @Test
        @DisplayName("纯白色 (255,255,255) -> HSV(0, 0, 1)")
        void pureWhite() {
            float[][] hsv = ColorSpaceOp.toHsv(new int[]{WHITE});
            assertThat(hsv[0][0]).isCloseTo(0.0f, within(0.1f));
            assertThat(hsv[1][0]).isCloseTo(0.0f, within(0.001f));
            assertThat(hsv[2][0]).isCloseTo(1.0f, within(0.001f));
        }

        @Test
        @DisplayName("null 输入抛出异常")
        void nullInput() {
            assertThatThrownBy(() -> ColorSpaceOp.toHsv(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("HSL 转换测试")
    class HslTests {

        @Test
        @DisplayName("toHsl -> fromHsl 往返误差 < 2")
        void roundtrip() {
            int[] pixels = testPixels();
            float[][] hsl = ColorSpaceOp.toHsl(pixels);
            int[] restored = ColorSpaceOp.fromHsl(hsl);
            assertRoundtrip(pixels, restored, 2);
        }

        @Test
        @DisplayName("null 输入抛出异常")
        void nullInput() {
            assertThatThrownBy(() -> ColorSpaceOp.toHsl(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("LAB 转换测试")
    class LabTests {

        @Test
        @DisplayName("toLab -> fromLab 往返误差 < 2")
        void roundtrip() {
            int[] pixels = testPixels();
            float[][] lab = ColorSpaceOp.toLab(pixels);
            int[] restored = ColorSpaceOp.fromLab(lab);
            assertRoundtrip(pixels, restored, 2);
        }

        @Test
        @DisplayName("null 输入抛出异常")
        void nullInput() {
            assertThatThrownBy(() -> ColorSpaceOp.toLab(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("YCbCr 转换测试")
    class YCbCrTests {

        @Test
        @DisplayName("toYCbCr -> fromYCbCr 往返误差 < 2")
        void roundtrip() {
            int[] pixels = testPixels();
            float[][] ycbcr = ColorSpaceOp.toYCbCr(pixels);
            int[] restored = ColorSpaceOp.fromYCbCr(ycbcr);
            assertRoundtrip(pixels, restored, 2);
        }

        @Test
        @DisplayName("null 输入抛出异常")
        void nullInput() {
            assertThatThrownBy(() -> ColorSpaceOp.toYCbCr(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("参数校验测试")
    class ValidationTests {

        @Test
        @DisplayName("空数组抛出 ImageOperationException")
        void emptyPixels() {
            assertThatThrownBy(() -> ColorSpaceOp.toHsv(new int[0]))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("fromHsv null 输入抛出异常")
        void fromHsvNull() {
            assertThatThrownBy(() -> ColorSpaceOp.fromHsv(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fromHsv 通道数不对抛出异常")
        void fromHsvWrongChannels() {
            assertThatThrownBy(() -> ColorSpaceOp.fromHsv(new float[2][1]))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("fromLab null 输入抛出异常")
        void fromLabNull() {
            assertThatThrownBy(() -> ColorSpaceOp.fromLab(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fromYCbCr null 输入抛出异常")
        void fromYCbCrNull() {
            assertThatThrownBy(() -> ColorSpaceOp.fromYCbCr(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
