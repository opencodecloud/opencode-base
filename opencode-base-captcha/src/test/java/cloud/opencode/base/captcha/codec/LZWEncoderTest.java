package cloud.opencode.base.captcha.codec;

import org.junit.jupiter.api.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

/**
 * LZWEncoder Test - Unit tests for LZW compression
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class LZWEncoderTest {

    @Nested
    @DisplayName("Encoding Tests")
    class EncodingTests {

        @Test
        @DisplayName("should encode pixel data without throwing")
        void shouldEncodePixelDataWithoutThrowing() {
            byte[] pixels = new byte[100];
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = (byte) (i % 256);
            }

            LZWEncoder encoder = new LZWEncoder(10, 10, pixels, 8);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            assertThatCode(() -> encoder.encode(out))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should produce non-empty output")
        void shouldProduceNonEmptyOutput() throws IOException {
            byte[] pixels = new byte[100];
            LZWEncoder encoder = new LZWEncoder(10, 10, pixels, 8);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            encoder.encode(out);

            assertThat(out.toByteArray()).isNotEmpty();
        }

        @Test
        @DisplayName("should write initial code size as first byte")
        void shouldWriteInitialCodeSizeAsFirstByte() throws IOException {
            byte[] pixels = new byte[100];
            LZWEncoder encoder = new LZWEncoder(10, 10, pixels, 8);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            encoder.encode(out);

            byte[] result = out.toByteArray();
            assertThat(result[0]).isEqualTo((byte) 8);
        }

        @Test
        @DisplayName("should end with block terminator (0)")
        void shouldEndWithBlockTerminator() throws IOException {
            byte[] pixels = new byte[100];
            LZWEncoder encoder = new LZWEncoder(10, 10, pixels, 8);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            encoder.encode(out);

            byte[] result = out.toByteArray();
            assertThat(result[result.length - 1]).isEqualTo((byte) 0);
        }

        @Test
        @DisplayName("should handle single pixel")
        void shouldHandleSinglePixel() throws IOException {
            byte[] pixels = new byte[]{42};
            LZWEncoder encoder = new LZWEncoder(1, 1, pixels, 8);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            encoder.encode(out);

            assertThat(out.toByteArray()).isNotEmpty();
        }

        @Test
        @DisplayName("should handle uniform pixel data")
        void shouldHandleUniformPixelData() throws IOException {
            byte[] pixels = new byte[100];
            // All same value
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = 5;
            }

            LZWEncoder encoder = new LZWEncoder(10, 10, pixels, 8);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            encoder.encode(out);

            assertThat(out.toByteArray()).isNotEmpty();
        }

        @Test
        @DisplayName("should compress uniform data better than random data")
        void shouldCompressUniformDataBetterThanRandomData() throws IOException {
            // Uniform data
            byte[] uniform = new byte[1000];
            for (int i = 0; i < uniform.length; i++) {
                uniform[i] = 0;
            }
            LZWEncoder uniformEncoder = new LZWEncoder(100, 10, uniform, 8);
            ByteArrayOutputStream uniformOut = new ByteArrayOutputStream();
            uniformEncoder.encode(uniformOut);

            // Varied data
            byte[] varied = new byte[1000];
            for (int i = 0; i < varied.length; i++) {
                varied[i] = (byte) (i % 200);
            }
            LZWEncoder variedEncoder = new LZWEncoder(100, 10, varied, 8);
            ByteArrayOutputStream variedOut = new ByteArrayOutputStream();
            variedEncoder.encode(variedOut);

            assertThat(uniformOut.size()).isLessThan(variedOut.size());
        }

        @Test
        @DisplayName("should handle minimum color depth of 2")
        void shouldHandleMinimumColorDepthOf2() throws IOException {
            byte[] pixels = new byte[4];
            LZWEncoder encoder = new LZWEncoder(2, 2, pixels, 2);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            encoder.encode(out);

            assertThat(out.toByteArray()).isNotEmpty();
            // Minimum code size should be 2
            assertThat(out.toByteArray()[0]).isEqualTo((byte) 2);
        }
    }

    @Nested
    @DisplayName("Integration with GifEncoder Tests")
    class IntegrationWithGifEncoderTests {

        @Test
        @DisplayName("should work as part of GIF encoding pipeline")
        void shouldWorkAsPartOfGifEncodingPipeline() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GifEncoder gifEncoder = new GifEncoder();
            gifEncoder.start(out);

            BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_3BYTE_BGR);
            Graphics g = image.getGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 20, 20);
            g.dispose();

            assertThat(gifEncoder.addFrame(image)).isTrue();
            assertThat(gifEncoder.finish()).isTrue();
            assertThat(out.toByteArray()).isNotEmpty();
        }
    }
}
