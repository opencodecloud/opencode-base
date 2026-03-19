package cloud.opencode.base.captcha.codec;

import org.junit.jupiter.api.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * GifEncoder Test - Unit tests for GIF encoding
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class GifEncoderTest {

    private GifEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new GifEncoder();
    }

    @Nested
    @DisplayName("Start Tests")
    class StartTests {

        @Test
        @DisplayName("should start encoding with valid output stream")
        void shouldStartEncodingWithValidOutputStream() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            boolean result = encoder.start(out);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false for null output stream")
        void shouldReturnFalseForNullOutputStream() {
            boolean result = encoder.start(null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should write GIF89a header")
        void shouldWriteGif89aHeader() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            encoder.start(out);

            byte[] bytes = out.toByteArray();
            assertThat(bytes).hasSizeGreaterThanOrEqualTo(6);
            // GIF89a
            assertThat(new String(bytes, 0, 6)).isEqualTo("GIF89a");
        }
    }

    @Nested
    @DisplayName("AddFrame Tests")
    class AddFrameTests {

        @Test
        @DisplayName("should add frame with valid image")
        void shouldAddFrameWithValidImage() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encoder.start(out);

            BufferedImage frame = createTestImage(10, 10, Color.RED);
            boolean result = encoder.addFrame(frame);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false for null image")
        void shouldReturnFalseForNullImage() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encoder.start(out);

            boolean result = encoder.addFrame(null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when not started")
        void shouldReturnFalseWhenNotStarted() {
            BufferedImage frame = createTestImage(10, 10, Color.RED);

            boolean result = encoder.addFrame(frame);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should add multiple frames")
        void shouldAddMultipleFrames() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encoder.start(out);

            assertThat(encoder.addFrame(createTestImage(10, 10, Color.RED))).isTrue();
            assertThat(encoder.addFrame(createTestImage(10, 10, Color.GREEN))).isTrue();
            assertThat(encoder.addFrame(createTestImage(10, 10, Color.BLUE))).isTrue();
        }

        @Test
        @DisplayName("should handle non-BGR image type")
        void shouldHandleNonBgrImageType() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encoder.start(out);

            // TYPE_INT_ARGB - not TYPE_3BYTE_BGR
            BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 10, 10);
            g.dispose();

            boolean result = encoder.addFrame(image);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Finish Tests")
    class FinishTests {

        @Test
        @DisplayName("should finish encoding after frames")
        void shouldFinishEncodingAfterFrames() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encoder.start(out);
            encoder.addFrame(createTestImage(10, 10, Color.RED));

            boolean result = encoder.finish();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when not started")
        void shouldReturnFalseWhenNotStarted() {
            boolean result = encoder.finish();

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should write GIF trailer byte")
        void shouldWriteGifTrailerByte() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encoder.start(out);
            encoder.addFrame(createTestImage(10, 10, Color.RED));

            encoder.finish();

            byte[] bytes = out.toByteArray();
            assertThat(bytes[bytes.length - 1]).isEqualTo((byte) 0x3b);
        }
    }

    @Nested
    @DisplayName("SetDelay Tests")
    class SetDelayTests {

        @Test
        @DisplayName("should set delay without throwing")
        void shouldSetDelayWithoutThrowing() {
            assertThatCode(() -> encoder.setDelay(100))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle zero delay")
        void shouldHandleZeroDelay() {
            assertThatCode(() -> encoder.setDelay(0))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("SetRepeat Tests")
    class SetRepeatTests {

        @Test
        @DisplayName("should set repeat to loop forever")
        void shouldSetRepeatToLoopForever() {
            assertThatCode(() -> encoder.setRepeat(0))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should set repeat count")
        void shouldSetRepeatCount() {
            assertThatCode(() -> encoder.setRepeat(5))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should ignore negative repeat (besides -1)")
        void shouldIgnoreNegativeRepeat() {
            assertThatCode(() -> encoder.setRepeat(-2))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("SetDispose Tests")
    class SetDisposeTests {

        @Test
        @DisplayName("should set dispose code")
        void shouldSetDisposeCode() {
            assertThatCode(() -> encoder.setDispose(2))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should ignore negative dispose code")
        void shouldIgnoreNegativeDisposeCode() {
            assertThatCode(() -> encoder.setDispose(-1))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("SetQuality Tests")
    class SetQualityTests {

        @Test
        @DisplayName("should set quality")
        void shouldSetQuality() {
            assertThatCode(() -> encoder.setQuality(10))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should clamp quality to minimum of 1")
        void shouldClampQualityToMinimumOf1() {
            assertThatCode(() -> encoder.setQuality(0))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Full Encoding Workflow Tests")
    class FullEncodingWorkflowTests {

        @Test
        @DisplayName("should produce valid GIF with single frame")
        void shouldProduceValidGifWithSingleFrame() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            encoder.start(out);
            encoder.setDelay(100);
            encoder.addFrame(createTestImage(20, 20, Color.RED));
            encoder.finish();

            byte[] gif = out.toByteArray();
            assertThat(gif).isNotEmpty();
            assertThat(new String(gif, 0, 6)).isEqualTo("GIF89a");
            assertThat(gif[gif.length - 1]).isEqualTo((byte) 0x3b);
        }

        @Test
        @DisplayName("should produce valid animated GIF with multiple frames")
        void shouldProduceValidAnimatedGifWithMultipleFrames() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            encoder.start(out);
            encoder.setRepeat(0);
            encoder.setDelay(100);
            encoder.addFrame(createTestImage(20, 20, Color.RED));
            encoder.addFrame(createTestImage(20, 20, Color.GREEN));
            encoder.addFrame(createTestImage(20, 20, Color.BLUE));
            encoder.finish();

            byte[] gif = out.toByteArray();
            assertThat(gif).isNotEmpty();
            assertThat(new String(gif, 0, 6)).isEqualTo("GIF89a");
        }

        @Test
        @DisplayName("should produce larger data for more frames")
        void shouldProduceLargerDataForMoreFrames() {
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            GifEncoder encoder1 = new GifEncoder();
            encoder1.start(out1);
            encoder1.addFrame(createTestImage(20, 20, Color.RED));
            encoder1.finish();

            ByteArrayOutputStream out3 = new ByteArrayOutputStream();
            GifEncoder encoder3 = new GifEncoder();
            encoder3.start(out3);
            encoder3.addFrame(createTestImage(20, 20, Color.RED));
            encoder3.addFrame(createTestImage(20, 20, Color.GREEN));
            encoder3.addFrame(createTestImage(20, 20, Color.BLUE));
            encoder3.finish();

            assertThat(out3.size()).isGreaterThan(out1.size());
        }

        @Test
        @DisplayName("should include Netscape extension for looping GIFs")
        void shouldIncludeNetscapeExtensionForLoopingGifs() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            encoder.start(out);
            encoder.setRepeat(0);
            encoder.addFrame(createTestImage(10, 10, Color.RED));
            encoder.finish();

            byte[] gif = out.toByteArray();
            String gifStr = new String(gif);
            assertThat(gifStr).contains("NETSCAPE2.0");
        }
    }

    private BufferedImage createTestImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = image.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }
}
