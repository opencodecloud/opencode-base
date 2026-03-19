package cloud.opencode.base.captcha.renderer;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageCaptchaRendererTest Tests
 * ImageCaptchaRendererTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("ImageCaptchaRenderer Tests")
class ImageCaptchaRendererTest {

    private ImageCaptchaRenderer renderer;
    private Captcha testCaptcha;
    private byte[] imageData;

    @BeforeEach
    void setUp() {
        renderer = new ImageCaptchaRenderer();
        imageData = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG header
        testCaptcha = new Captcha(
            "test-id",
            CaptchaType.ALPHANUMERIC,
            imageData,
            "abcd",
            Map.of("width", 160, "height", 60),
            Instant.now(),
            Instant.now().plusSeconds(300)
        );
    }

    @Nested
    @DisplayName("Render Tests")
    class RenderTests {

        @Test
        @DisplayName("should render captcha image data to output stream")
        void shouldRenderToOutputStream() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            renderer.render(testCaptcha, out);

            assertThat(out.toByteArray()).isEqualTo(imageData);
        }
    }

    @Nested
    @DisplayName("Render To Bytes Tests")
    class RenderToBytesTests {

        @Test
        @DisplayName("should return raw image data bytes")
        void shouldReturnRawImageData() {
            byte[] result = renderer.renderToBytes(testCaptcha);
            assertThat(result).isEqualTo(imageData);
        }
    }

    @Nested
    @DisplayName("Render To Base64 Tests")
    class RenderToBase64Tests {

        @Test
        @DisplayName("should render image data as base64 string")
        void shouldRenderAsBase64() {
            String result = renderer.renderToBase64(testCaptcha);
            String expected = Base64.getEncoder().encodeToString(imageData);
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Content Type Tests")
    class ContentTypeTests {

        @Test
        @DisplayName("should return image/png content type")
        void shouldReturnPngContentType() {
            assertThat(renderer.getContentType()).isEqualTo("image/png");
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("CaptchaRenderer.image() should return ImageCaptchaRenderer")
        void shouldCreateViaFactoryMethod() {
            CaptchaRenderer created = CaptchaRenderer.image();
            assertThat(created).isInstanceOf(ImageCaptchaRenderer.class);
        }
    }
}
