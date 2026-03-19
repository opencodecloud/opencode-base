package cloud.opencode.base.image.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

/**
 * CompressOp 操作测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("CompressOp 操作测试")
class CompressOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        // 填充一些颜色以获得有意义的压缩
        for (int y = 0; y < 600; y++) {
            for (int x = 0; x < 800; x++) {
                testImage.setRGB(x, y, (x * y) % 16777216);
            }
        }
    }

    @Nested
    @DisplayName("compress方法测试")
    class CompressTests {

        @Test
        @DisplayName("高质量压缩")
        void testCompressHighQuality() {
            BufferedImage result = CompressOp.compress(testImage, 0.9f);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("低质量压缩")
        void testCompressLowQuality() {
            BufferedImage result = CompressOp.compress(testImage, 0.3f);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("质量为1")
        void testCompressMaxQuality() {
            BufferedImage result = CompressOp.compress(testImage, 1.0f);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("质量为0")
        void testCompressMinQuality() {
            // 质量为0虽然极端，但应该不抛异常
            assertThatCode(() -> CompressOp.compress(testImage, 0.0f))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("负质量抛出异常")
        void testCompressNegativeQuality() {
            assertThatThrownBy(() -> CompressOp.compress(testImage, -0.1f))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("超过1的质量抛出异常")
        void testCompressQualityGreaterThanOne() {
            assertThatThrownBy(() -> CompressOp.compress(testImage, 1.1f))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("compressToBytes方法测试")
    class CompressToBytesTests {

        @Test
        @DisplayName("压缩到JPEG字节数组")
        void testCompressToBytesJpeg() throws IOException {
            byte[] result = CompressOp.compressToBytes(testImage, 0.8f, "jpg");

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("压缩到PNG字节数组")
        void testCompressToBytessPng() throws IOException {
            byte[] result = CompressOp.compressToBytes(testImage, 0.8f, "png");

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("低质量产生更小的文件")
        void testLowerQualitySmallerFile() throws IOException {
            byte[] high = CompressOp.compressToBytes(testImage, 0.9f, "jpg");
            byte[] low = CompressOp.compressToBytes(testImage, 0.3f, "jpg");

            assertThat(low.length).isLessThan(high.length);
        }
    }

    @Nested
    @DisplayName("compressToSize方法测试")
    class CompressToSizeTests {

        @Test
        @DisplayName("压缩到目标大小")
        void testCompressToSize() {
            BufferedImage result = CompressOp.compressToSize(testImage, 50000);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("optimize方法测试")
    class OptimizeTests {

        @Test
        @DisplayName("优化RGB图片")
        void testOptimizeRGB() {
            BufferedImage result = CompressOp.optimize(testImage);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("优化ARGB图片")
        void testOptimizeARGB() {
            BufferedImage argb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            BufferedImage result = CompressOp.optimize(argb);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }
    }

    @Nested
    @DisplayName("removeAlpha方法测试")
    class RemoveAlphaTests {

        @Test
        @DisplayName("移除透明通道")
        void testRemoveAlpha() {
            BufferedImage argb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            BufferedImage result = CompressOp.removeAlpha(argb);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }

        @Test
        @DisplayName("RGB图片不变")
        void testRemoveAlphaFromRGB() {
            BufferedImage result = CompressOp.removeAlpha(testImage);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }
    }

    @Nested
    @DisplayName("reduceColors方法测试")
    class ReduceColorsTests {

        @Test
        @DisplayName("减少到8位颜色")
        void testReduceColors8Bits() {
            BufferedImage result = CompressOp.reduceColors(testImage, 8);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(800);
        }

        @Test
        @DisplayName("减少到4位颜色")
        void testReduceColors4Bits() {
            BufferedImage result = CompressOp.reduceColors(testImage, 4);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("减少到1位颜色")
        void testReduceColors1Bit() {
            BufferedImage result = CompressOp.reduceColors(testImage, 1);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("0位抛出异常")
        void testReduceColorsZeroBits() {
            assertThatThrownBy(() -> CompressOp.reduceColors(testImage, 0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("超过8位抛出异常")
        void testReduceColorsMoreThan8Bits() {
            assertThatThrownBy(() -> CompressOp.reduceColors(testImage, 9))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("estimateCompressedSize方法测试")
    class EstimateCompressedSizeTests {

        @Test
        @DisplayName("估算压缩大小")
        void testEstimateCompressedSize() {
            long size = CompressOp.estimateCompressedSize(testImage, 0.8f);

            assertThat(size).isGreaterThan(0);
        }

        @Test
        @DisplayName("高质量估算大于低质量")
        void testHighQualityLargerEstimate() {
            long highSize = CompressOp.estimateCompressedSize(testImage, 0.9f);
            long lowSize = CompressOp.estimateCompressedSize(testImage, 0.3f);

            assertThat(highSize).isGreaterThan(lowSize);
        }
    }
}
