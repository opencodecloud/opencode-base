package cloud.opencode.base.captcha.codec;

import org.junit.jupiter.api.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import static org.assertj.core.api.Assertions.*;

/**
 * NeuQuantEncoder Test - Unit tests for NeuQuant color quantization
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class NeuQuantEncoderTest {

    @Nested
    @DisplayName("Process Tests")
    class ProcessTests {

        @Test
        @DisplayName("should produce color map with 768 bytes (256 * 3)")
        void shouldProduceColorMapWith768Bytes() {
            byte[] pixels = createBgrPixels(50, 50, Color.RED);

            NeuQuantEncoder encoder = new NeuQuantEncoder(pixels, 50 * 50, 10);
            byte[] colorMap = encoder.process();

            assertThat(colorMap).hasSize(768); // 256 colors * 3 bytes (RGB)
        }

        @Test
        @DisplayName("should produce non-null color map")
        void shouldProduceNonNullColorMap() {
            byte[] pixels = createBgrPixels(20, 20, Color.BLUE);

            NeuQuantEncoder encoder = new NeuQuantEncoder(pixels, 20 * 20, 10);
            byte[] colorMap = encoder.process();

            assertThat(colorMap).isNotNull();
        }

        @Test
        @DisplayName("should handle single-color image")
        void shouldHandleSingleColorImage() {
            byte[] pixels = createBgrPixels(30, 30, Color.GREEN);

            NeuQuantEncoder encoder = new NeuQuantEncoder(pixels, 30 * 30, 10);
            byte[] colorMap = encoder.process();

            assertThat(colorMap).isNotNull();
            assertThat(colorMap).hasSize(768);
        }

        @Test
        @DisplayName("should handle multi-color image")
        void shouldHandleMultiColorImage() {
            BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_3BYTE_BGR);
            Graphics g = image.getGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 20, 40);
            g.setColor(Color.BLUE);
            g.fillRect(20, 0, 20, 40);
            g.dispose();

            byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            NeuQuantEncoder encoder = new NeuQuantEncoder(pixels, 40 * 40, 10);
            byte[] colorMap = encoder.process();

            assertThat(colorMap).isNotNull();
            assertThat(colorMap).hasSize(768);
        }

        @Test
        @DisplayName("should handle different quality settings")
        void shouldHandleDifferentQualitySettings() {
            byte[] pixels = createBgrPixels(30, 30, Color.YELLOW);

            // High quality (low number)
            NeuQuantEncoder highQ = new NeuQuantEncoder(pixels, 30 * 30, 1);
            byte[] mapHigh = highQ.process();

            // Low quality (high number)
            NeuQuantEncoder lowQ = new NeuQuantEncoder(pixels, 30 * 30, 30);
            byte[] mapLow = lowQ.process();

            assertThat(mapHigh).hasSize(768);
            assertThat(mapLow).hasSize(768);
        }
    }

    @Nested
    @DisplayName("Map Tests")
    class MapTests {

        @Test
        @DisplayName("should map color to valid palette index")
        void shouldMapColorToValidPaletteIndex() {
            byte[] pixels = createBgrPixels(30, 30, Color.RED);

            NeuQuantEncoder encoder = new NeuQuantEncoder(pixels, 30 * 30, 10);
            encoder.process();

            int index = encoder.map(0, 0, 255); // B=0, G=0, R=255 (RED in BGR)

            assertThat(index).isBetween(0, 255);
        }

        @Test
        @DisplayName("should map different colors to potentially different indices")
        void shouldMapDifferentColorsToPotentiallyDifferentIndices() {
            // Create an image with multiple colors
            BufferedImage image = new BufferedImage(60, 60, BufferedImage.TYPE_3BYTE_BGR);
            Graphics g = image.getGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 30, 60);
            g.setColor(Color.BLUE);
            g.fillRect(30, 0, 30, 60);
            g.dispose();

            byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            NeuQuantEncoder encoder = new NeuQuantEncoder(pixels, 60 * 60, 10);
            encoder.process();

            int redIndex = encoder.map(0, 0, 255);  // RED in BGR
            int blueIndex = encoder.map(255, 0, 0);  // BLUE in BGR

            // Both should be valid indices
            assertThat(redIndex).isBetween(0, 255);
            assertThat(blueIndex).isBetween(0, 255);
        }

        @Test
        @DisplayName("should return consistent mapping for same color")
        void shouldReturnConsistentMappingForSameColor() {
            byte[] pixels = createBgrPixels(30, 30, Color.GREEN);

            NeuQuantEncoder encoder = new NeuQuantEncoder(pixels, 30 * 30, 10);
            encoder.process();

            int index1 = encoder.map(0, 255, 0); // GREEN in BGR
            int index2 = encoder.map(0, 255, 0);

            assertThat(index1).isEqualTo(index2);
        }

        @Test
        @DisplayName("should map black to valid index")
        void shouldMapBlackToValidIndex() {
            byte[] pixels = createBgrPixels(30, 30, Color.BLACK);

            NeuQuantEncoder encoder = new NeuQuantEncoder(pixels, 30 * 30, 10);
            encoder.process();

            int index = encoder.map(0, 0, 0);

            assertThat(index).isBetween(0, 255);
        }

        @Test
        @DisplayName("should map white to valid index")
        void shouldMapWhiteToValidIndex() {
            byte[] pixels = createBgrPixels(30, 30, Color.WHITE);

            NeuQuantEncoder encoder = new NeuQuantEncoder(pixels, 30 * 30, 10);
            encoder.process();

            int index = encoder.map(255, 255, 255);

            assertThat(index).isBetween(0, 255);
        }
    }

    @Nested
    @DisplayName("Small Image Tests")
    class SmallImageTests {

        @Test
        @DisplayName("should handle minimum viable image")
        void shouldHandleMinimumViableImage() {
            // Very small image
            byte[] pixels = createBgrPixels(5, 5, Color.RED);

            NeuQuantEncoder encoder = new NeuQuantEncoder(pixels, 5 * 5, 1);

            assertThatCode(() -> encoder.process()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should work with GifEncoder end-to-end")
        void shouldWorkWithGifEncoderEndToEnd() {
            // Create a multi-color image
            BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_3BYTE_BGR);
            Graphics g = image.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 40, 40);
            g.setColor(Color.RED);
            g.fillOval(5, 5, 30, 30);
            g.dispose();

            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            GifEncoder gifEncoder = new GifEncoder();
            gifEncoder.start(out);
            gifEncoder.addFrame(image);
            gifEncoder.finish();

            byte[] gif = out.toByteArray();
            assertThat(gif).isNotEmpty();
            assertThat(new String(gif, 0, 6)).isEqualTo("GIF89a");
        }
    }

    /**
     * Creates BGR pixel data for a solid color image.
     */
    private byte[] createBgrPixels(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = image.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    }
}
