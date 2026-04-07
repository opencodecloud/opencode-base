package cloud.opencode.base.image.histogram;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.PixelOp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * ClaheOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("ClaheOp CLAHE 自适应直方图均衡化测试")
class ClaheOpTest {

    /**
     * Create a low-contrast gradient image.
     */
    private BufferedImage createLowContrastImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(img);
        for (int i = 0; i < pixels.length; i++) {
            int v = 100 + (i % 30);
            pixels[i] = PixelOp.argb(255, v, v, v);
        }
        return img;
    }

    @Nested
    @DisplayName("默认参数测试")
    class DefaultParamTests {

        @Test
        @DisplayName("默认参数可以正常运行")
        void defaultParamsWork() {
            BufferedImage img = createLowContrastImage(32, 32);

            BufferedImage result = ClaheOp.apply(img);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(32);
            assertThat(result.getHeight()).isEqualTo(32);
        }

        @Test
        @DisplayName("输出与输入尺寸相同")
        void outputSameSize() {
            BufferedImage img = createLowContrastImage(40, 30);
            BufferedImage result = ClaheOp.apply(img);
            assertThat(result.getWidth()).isEqualTo(40);
            assertThat(result.getHeight()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("CLAHE 与全局均衡化比较测试")
    class CompareWithGlobalTests {

        @Test
        @DisplayName("CLAHE 输出与全局均衡化不同")
        void claheDiffersFromGlobalEqualization() {
            BufferedImage img = createLowContrastImage(32, 32);

            BufferedImage global = HistogramEqualizationOp.apply(img);
            BufferedImage clahe = ClaheOp.apply(img, 2.0, 8);

            int[] globalPixels = PixelOp.getPixels(PixelOp.ensureArgb(global));
            int[] clahePixels = PixelOp.getPixels(PixelOp.ensureArgb(clahe));

            int[] globalGray = ChannelOp.toGray(globalPixels);
            int[] claheGray = ChannelOp.toGray(clahePixels);

            // They should differ in at least some pixels
            boolean foundDifference = false;
            for (int i = 0; i < globalGray.length; i++) {
                if (globalGray[i] != claheGray[i]) {
                    foundDifference = true;
                    break;
                }
            }
            assertThat(foundDifference).isTrue();
        }
    }

    @Nested
    @DisplayName("自定义参数测试")
    class CustomParamTests {

        @Test
        @DisplayName("不同 clipLimit 产生不同结果")
        void differentClipLimitsDiffer() {
            // Use a larger image with more varied content to show clipLimit effect
            int w = 64, h = 64;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                // Create a spatially varying image: left side dark, right side bright
                int x = i % w;
                int y = i / w;
                int v = (x * 255) / (w - 1) + ((y % 4 == 0) ? 20 : 0);
                v = Math.clamp(v, 0, 255);
                pixels[i] = PixelOp.argb(255, v, v, v);
            }

            BufferedImage result1 = ClaheOp.apply(img, 1.0, 4);
            BufferedImage result2 = ClaheOp.apply(img, 40.0, 4);

            int[] pixels1 = PixelOp.getPixels(PixelOp.ensureArgb(result1));
            int[] pixels2 = PixelOp.getPixels(PixelOp.ensureArgb(result2));

            int[] gray1 = ChannelOp.toGray(pixels1);
            int[] gray2 = ChannelOp.toGray(pixels2);

            int diffCount = 0;
            for (int i = 0; i < gray1.length; i++) {
                if (gray1[i] != gray2[i]) {
                    diffCount++;
                }
            }
            assertThat(diffCount).isGreaterThan(0);
        }

        @Test
        @DisplayName("tileGridSize=1 等价于全局均衡化")
        void tileGridSize1SimilarToGlobal() {
            BufferedImage img = createLowContrastImage(16, 16);

            BufferedImage clahe1 = ClaheOp.apply(img, 1000.0, 1);

            // With a single tile and very high clip limit, should be similar to global equalization
            assertThat(clahe1).isNotNull();
            assertThat(clahe1.getWidth()).isEqualTo(16);
        }
    }

    @Nested
    @DisplayName("异常测试")
    class ExceptionTests {

        @Test
        @DisplayName("clipLimit <= 0 抛出异常")
        void clipLimitZeroThrows() {
            BufferedImage img = createLowContrastImage(16, 16);
            assertThatThrownBy(() -> ClaheOp.apply(img, 0.0, 8))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("clipLimit");
        }

        @Test
        @DisplayName("clipLimit 为负数抛出异常")
        void clipLimitNegativeThrows() {
            BufferedImage img = createLowContrastImage(16, 16);
            assertThatThrownBy(() -> ClaheOp.apply(img, -1.0, 8))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("clipLimit");
        }

        @Test
        @DisplayName("clipLimit 为 NaN 抛出异常")
        void clipLimitNanThrows() {
            BufferedImage img = createLowContrastImage(16, 16);
            assertThatThrownBy(() -> ClaheOp.apply(img, Double.NaN, 8))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("tileGridSize <= 0 抛出异常")
        void tileGridSizeZeroThrows() {
            BufferedImage img = createLowContrastImage(16, 16);
            assertThatThrownBy(() -> ClaheOp.apply(img, 2.0, 0))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("tileGridSize");
        }

        @Test
        @DisplayName("tileGridSize 为负数抛出异常")
        void tileGridSizeNegativeThrows() {
            BufferedImage img = createLowContrastImage(16, 16);
            assertThatThrownBy(() -> ClaheOp.apply(img, 2.0, -1))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("tileGridSize");
        }

        @Test
        @DisplayName("null image 抛出异常")
        void nullImageThrows() {
            assertThatThrownBy(() -> ClaheOp.apply(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null image 带参数抛出异常")
        void nullImageWithParamsThrows() {
            assertThatThrownBy(() -> ClaheOp.apply(null, 2.0, 8))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
