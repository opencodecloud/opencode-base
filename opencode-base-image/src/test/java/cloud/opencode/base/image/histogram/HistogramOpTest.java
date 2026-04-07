package cloud.opencode.base.image.histogram;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * HistogramOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("HistogramOp 直方图操作测试")
class HistogramOpTest {

    @Nested
    @DisplayName("Histogram record 验证测试")
    class HistogramRecordTests {

        @Test
        @DisplayName("counts 为 null 抛出 NullPointerException")
        void nullCountsThrows() {
            assertThatThrownBy(() -> new HistogramOp.Histogram(null, 0, 0, 255, 128.0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("counts 长度不为 256 抛出异常")
        void invalidCountsLengthThrows() {
            assertThatThrownBy(() -> new HistogramOp.Histogram(new int[100], 0, 0, 255, 128.0))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("256");
        }

        @Test
        @DisplayName("有效 Histogram 记录创建成功")
        void validHistogramCreation() {
            int[] counts = new int[256];
            counts[128] = 100;
            HistogramOp.Histogram hist = new HistogramOp.Histogram(counts, 0, 128, 128, 128.0);
            assertThat(hist.channel()).isEqualTo(0);
            assertThat(hist.min()).isEqualTo(128);
            assertThat(hist.max()).isEqualTo(128);
            assertThat(hist.mean()).isEqualTo(128.0);
        }

        @Test
        @DisplayName("counts 是防御性副本")
        void countsIsDefensiveCopy() {
            int[] counts = new int[256];
            counts[0] = 10;
            HistogramOp.Histogram hist = new HistogramOp.Histogram(counts, 0, 0, 0, 0.0);
            int[] retrieved = hist.counts();
            retrieved[0] = 999;
            // Original should not be affected
            assertThat(hist.counts()[0]).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("computeGray 灰度直方图测试")
    class ComputeGrayTests {

        @Test
        @DisplayName("灰度直方图计数总和等于总像素数")
        void grayHistogramSumEqualsPixelCount() {
            int w = 10, h = 10;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                int v = (i * 7) % 256;
                pixels[i] = PixelOp.argb(255, v, v, v);
            }

            HistogramOp.Histogram hist = HistogramOp.computeGray(img);

            int[] counts = hist.counts();
            long sum = 0;
            for (int c : counts) {
                sum += c;
            }
            assertThat(sum).isEqualTo(w * h);
        }

        @Test
        @DisplayName("均匀灰度图像产生单峰直方图")
        void uniformImageSinglePeak() {
            int w = 8, h = 8;
            int grayValue = 100;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, grayValue, grayValue, grayValue);
            }

            HistogramOp.Histogram hist = HistogramOp.computeGray(img);
            int[] counts = hist.counts();

            // The peak should be at the gray value
            assertThat(counts[grayValue]).isEqualTo(w * h);
            // All other bins should be zero
            for (int i = 0; i < 256; i++) {
                if (i != grayValue) {
                    assertThat(counts[i]).isEqualTo(0);
                }
            }
            assertThat(hist.min()).isEqualTo(grayValue);
            assertThat(hist.max()).isEqualTo(grayValue);
            assertThat(hist.mean()).isCloseTo(grayValue, within(1.0));
        }

        @Test
        @DisplayName("null image 抛出 NullPointerException")
        void nullImageThrows() {
            assertThatThrownBy(() -> HistogramOp.computeGray(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("compute RGB 直方图测试")
    class ComputeRgbTests {

        @Test
        @DisplayName("compute 返回 3 个直方图")
        void computeReturnsThreeHistograms() {
            BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, 100, 150, 200);
            }

            HistogramOp.Histogram[] histograms = HistogramOp.compute(img);

            assertThat(histograms).hasSize(3);
            assertThat(histograms[0].channel()).isEqualTo(1); // Red
            assertThat(histograms[1].channel()).isEqualTo(2); // Green
            assertThat(histograms[2].channel()).isEqualTo(3); // Blue
        }

        @Test
        @DisplayName("RGB 各通道计数总和等于总像素数")
        void rgbHistogramSumsEqual() {
            BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, i % 256, (i * 2) % 256, (i * 3) % 256);
            }

            HistogramOp.Histogram[] histograms = HistogramOp.compute(img);

            for (HistogramOp.Histogram h : histograms) {
                int[] counts = h.counts();
                long sum = 0;
                for (int c : counts) {
                    sum += c;
                }
                assertThat(sum).isEqualTo(64);
            }
        }

        @Test
        @DisplayName("纯红色图像 R 通道峰值正确")
        void pureRedImageHistogram() {
            BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, 200, 0, 0);
            }

            HistogramOp.Histogram[] histograms = HistogramOp.compute(img);

            // Red channel should peak at 200
            assertThat(histograms[0].counts()[200]).isEqualTo(16);
            assertThat(histograms[0].min()).isEqualTo(200);
            assertThat(histograms[0].max()).isEqualTo(200);

            // Green and Blue should peak at 0
            assertThat(histograms[1].counts()[0]).isEqualTo(16);
            assertThat(histograms[2].counts()[0]).isEqualTo(16);
        }

        @Test
        @DisplayName("null image 抛出 NullPointerException")
        void nullImageThrows() {
            assertThatThrownBy(() -> HistogramOp.compute(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
