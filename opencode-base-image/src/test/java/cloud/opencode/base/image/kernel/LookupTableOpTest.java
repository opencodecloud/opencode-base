package cloud.opencode.base.image.kernel;

import cloud.opencode.base.image.exception.ImageOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * LookupTableOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("LookupTableOp 查找表操作测试")
class LookupTableOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(testImage);
        for (int i = 0; i < pixels.length; i++) {
            int v = i * 16; // 0, 16, 32, ..., 240
            pixels[i] = PixelOp.argb(200, v, v, v);
        }
    }

    @Nested
    @DisplayName("identityLut 恒等查找表测试")
    class IdentityLutTests {

        @Test
        @DisplayName("identity LUT 长度为 256")
        void identityLutLength() {
            int[] lut = LookupTableOp.identityLut();
            assertThat(lut).hasSize(256);
        }

        @Test
        @DisplayName("identity LUT 输出等于输入")
        void identityLutValues() {
            int[] lut = LookupTableOp.identityLut();
            for (int i = 0; i < 256; i++) {
                assertThat(lut[i]).isEqualTo(i);
            }
        }

        @Test
        @DisplayName("identity LUT apply 后图像不变")
        void identityApplyPreservesImage() {
            int[] srcPixels = PixelOp.getPixels(testImage).clone();
            BufferedImage result = LookupTableOp.apply(testImage, LookupTableOp.identityLut());
            int[] dstPixels = PixelOp.getPixels(result);

            for (int i = 0; i < srcPixels.length; i++) {
                assertThat(PixelOp.red(dstPixels[i])).isEqualTo(PixelOp.red(srcPixels[i]));
                assertThat(PixelOp.green(dstPixels[i])).isEqualTo(PixelOp.green(srcPixels[i]));
                assertThat(PixelOp.blue(dstPixels[i])).isEqualTo(PixelOp.blue(srcPixels[i]));
                assertThat(PixelOp.alpha(dstPixels[i])).isEqualTo(PixelOp.alpha(srcPixels[i]));
            }
        }
    }

    @Nested
    @DisplayName("gammaLut 伽马查找表测试")
    class GammaLutTests {

        @Test
        @DisplayName("gamma(1.0) 等同于 identity")
        void gamma1IsIdentity() {
            int[] lut = LookupTableOp.gammaLut(1.0);
            int[] identity = LookupTableOp.identityLut();
            assertThat(lut).isEqualTo(identity);
        }

        @Test
        @DisplayName("gamma 端点正确: 0→0, 255→255")
        void gammaEndpoints() {
            int[] lut = LookupTableOp.gammaLut(2.2);
            assertThat(lut[0]).isEqualTo(0);
            assertThat(lut[255]).isEqualTo(255);
        }

        @Test
        @DisplayName("非正数 gamma 抛出异常")
        void invalidGammaThrows() {
            assertThatThrownBy(() -> LookupTableOp.gammaLut(0))
                    .isInstanceOf(ImageOperationException.class);
            assertThatThrownBy(() -> LookupTableOp.gammaLut(-1))
                    .isInstanceOf(ImageOperationException.class);
            assertThatThrownBy(() -> LookupTableOp.gammaLut(Double.NaN))
                    .isInstanceOf(ImageOperationException.class);
        }
    }

    @Nested
    @DisplayName("thresholdLut 阈值查找表测试")
    class ThresholdLutTests {

        @Test
        @DisplayName("threshold(128): 0-127→0, 128-255→255")
        void threshold128() {
            int[] lut = LookupTableOp.thresholdLut(128);
            for (int i = 0; i < 128; i++) {
                assertThat(lut[i]).isEqualTo(0);
            }
            for (int i = 128; i < 256; i++) {
                assertThat(lut[i]).isEqualTo(255);
            }
        }

        @Test
        @DisplayName("threshold(0): 所有值→255")
        void threshold0AllWhite() {
            int[] lut = LookupTableOp.thresholdLut(0);
            for (int v : lut) {
                assertThat(v).isEqualTo(255);
            }
        }

        @Test
        @DisplayName("threshold(255): 只有 255→255，其余→0")
        void threshold255() {
            int[] lut = LookupTableOp.thresholdLut(255);
            for (int i = 0; i < 255; i++) {
                assertThat(lut[i]).isEqualTo(0);
            }
            assertThat(lut[255]).isEqualTo(255);
        }

        @Test
        @DisplayName("越界 threshold 抛出异常")
        void outOfRangeThrows() {
            assertThatThrownBy(() -> LookupTableOp.thresholdLut(-1))
                    .isInstanceOf(ImageOperationException.class);
            assertThatThrownBy(() -> LookupTableOp.thresholdLut(256))
                    .isInstanceOf(ImageOperationException.class);
        }
    }

    @Nested
    @DisplayName("invertLut 反转查找表测试")
    class InvertLutTests {

        @Test
        @DisplayName("invert: 0→255, 255→0")
        void invertEndpoints() {
            int[] lut = LookupTableOp.invertLut();
            assertThat(lut[0]).isEqualTo(255);
            assertThat(lut[255]).isEqualTo(0);
        }

        @Test
        @DisplayName("invert: 128→127")
        void invertMiddle() {
            int[] lut = LookupTableOp.invertLut();
            assertThat(lut[128]).isEqualTo(127);
        }

        @Test
        @DisplayName("双重反转等于 identity")
        void doubleInvertIsIdentity() {
            int[] invert = LookupTableOp.invertLut();
            BufferedImage inverted = LookupTableOp.apply(testImage, invert);
            BufferedImage restored = LookupTableOp.apply(inverted, invert);

            int[] srcPixels = PixelOp.getPixels(testImage);
            int[] dstPixels = PixelOp.getPixels(restored);
            for (int i = 0; i < srcPixels.length; i++) {
                assertThat(PixelOp.red(dstPixels[i])).isEqualTo(PixelOp.red(srcPixels[i]));
                assertThat(PixelOp.green(dstPixels[i])).isEqualTo(PixelOp.green(srcPixels[i]));
                assertThat(PixelOp.blue(dstPixels[i])).isEqualTo(PixelOp.blue(srcPixels[i]));
            }
        }
    }

    @Nested
    @DisplayName("contrastLut 对比度查找表测试")
    class ContrastLutTests {

        @Test
        @DisplayName("contrast(1.0) 不变")
        void contrast1IsIdentity() {
            int[] lut = LookupTableOp.contrastLut(1.0);
            int[] identity = LookupTableOp.identityLut();
            assertThat(lut).isEqualTo(identity);
        }

        @Test
        @DisplayName("contrast 中心值 128 不变")
        void contrastCenter128Unchanged() {
            int[] lut = LookupTableOp.contrastLut(2.0);
            assertThat(lut[128]).isEqualTo(128);
        }
    }

    @Nested
    @DisplayName("brightnessLut 亮度查找表测试")
    class BrightnessLutTests {

        @Test
        @DisplayName("brightness(0) 不变")
        void brightness0IsIdentity() {
            int[] lut = LookupTableOp.brightnessLut(0);
            int[] identity = LookupTableOp.identityLut();
            assertThat(lut).isEqualTo(identity);
        }

        @Test
        @DisplayName("正偏移增大值")
        void positiveBrightnessIncreasesValues() {
            int[] lut = LookupTableOp.brightnessLut(50);
            assertThat(lut[0]).isEqualTo(50);
            assertThat(lut[200]).isEqualTo(250);
            assertThat(lut[210]).isEqualTo(255); // clamped
        }
    }

    @Nested
    @DisplayName("三通道 LUT 应用测试")
    class PerChannelLutTests {

        @Test
        @DisplayName("不同通道使用不同 LUT")
        void perChannelLut() {
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            PixelOp.getPixels(img)[0] = PixelOp.argb(255, 100, 100, 100);

            int[] lutR = LookupTableOp.brightnessLut(50);  // 100 -> 150
            int[] lutG = LookupTableOp.invertLut();          // 100 -> 155
            int[] lutB = LookupTableOp.identityLut();        // 100 -> 100

            BufferedImage result = LookupTableOp.apply(img, lutR, lutG, lutB);
            int px = PixelOp.getPixels(result)[0];

            assertThat(PixelOp.red(px)).isEqualTo(150);
            assertThat(PixelOp.green(px)).isEqualTo(155);
            assertThat(PixelOp.blue(px)).isEqualTo(100);
            assertThat(PixelOp.alpha(px)).isEqualTo(255); // alpha preserved
        }
    }

    @Nested
    @DisplayName("参数校验测试")
    class ValidationTests {

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrows() {
            assertThatThrownBy(() -> LookupTableOp.apply(null, LookupTableOp.identityLut()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null LUT 抛出异常")
        void nullLutThrows() {
            assertThatThrownBy(() -> LookupTableOp.apply(testImage, (int[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("错误长度 LUT 抛出异常")
        void wrongLutLengthThrows() {
            assertThatThrownBy(() -> LookupTableOp.apply(testImage, new int[100]))
                    .isInstanceOf(ImageOperationException.class);
        }
    }
}
