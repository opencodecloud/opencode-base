package cloud.opencode.base.captcha.renderer;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaRenderer Test - Unit tests for all CAPTCHA renderers
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaRendererTest {

    private static final byte[] SAMPLE_IMAGE_DATA = {0x01, 0x02, 0x03, 0x04, 0x05};
    private Captcha sampleCaptcha;
    private Captcha gifCaptcha;

    @BeforeEach
    void setUp() {
        sampleCaptcha = new Captcha(
            "test-id",
            CaptchaType.ALPHANUMERIC,
            SAMPLE_IMAGE_DATA,
            "AbCd",
            Map.of("width", 160, "height", 60),
            Instant.now(),
            Instant.now().plusSeconds(300)
        );
        gifCaptcha = new Captcha(
            "gif-id",
            CaptchaType.GIF,
            SAMPLE_IMAGE_DATA,
            "XyZw",
            Map.of("width", 160, "height", 60),
            Instant.now(),
            Instant.now().plusSeconds(300)
        );
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("image() should create ImageCaptchaRenderer")
        void imageShouldCreateImageCaptchaRenderer() {
            CaptchaRenderer renderer = CaptchaRenderer.image();

            assertThat(renderer).isNotNull();
            assertThat(renderer).isInstanceOf(ImageCaptchaRenderer.class);
        }

        @Test
        @DisplayName("gif() should create GifCaptchaRenderer")
        void gifShouldCreateGifCaptchaRenderer() {
            CaptchaRenderer renderer = CaptchaRenderer.gif();

            assertThat(renderer).isNotNull();
            assertThat(renderer).isInstanceOf(GifCaptchaRenderer.class);
        }

        @Test
        @DisplayName("base64() should create Base64CaptchaRenderer")
        void base64ShouldCreateBase64CaptchaRenderer() {
            CaptchaRenderer renderer = CaptchaRenderer.base64();

            assertThat(renderer).isNotNull();
            assertThat(renderer).isInstanceOf(Base64CaptchaRenderer.class);
        }
    }

    @Nested
    @DisplayName("ImageCaptchaRenderer Tests")
    class ImageCaptchaRendererTests {

        private ImageCaptchaRenderer renderer;

        @BeforeEach
        void setUp() {
            renderer = new ImageCaptchaRenderer();
        }

        @Test
        @DisplayName("should return image/png content type")
        void shouldReturnImagePngContentType() {
            assertThat(renderer.getContentType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("should render to bytes returning image data")
        void shouldRenderToBytesReturningImageData() {
            byte[] result = renderer.renderToBytes(sampleCaptcha);

            assertThat(result).isEqualTo(SAMPLE_IMAGE_DATA);
        }

        @Test
        @DisplayName("should render to output stream")
        void shouldRenderToOutputStream() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            renderer.render(sampleCaptcha, out);

            assertThat(out.toByteArray()).isEqualTo(SAMPLE_IMAGE_DATA);
        }

        @Test
        @DisplayName("should render to base64 string")
        void shouldRenderToBase64String() {
            String result = renderer.renderToBase64(sampleCaptcha);

            String expected = Base64.getEncoder().encodeToString(SAMPLE_IMAGE_DATA);
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("base64 should be decodable back to original bytes")
        void base64ShouldBeDecodableBackToOriginalBytes() {
            String base64 = renderer.renderToBase64(sampleCaptcha);

            byte[] decoded = Base64.getDecoder().decode(base64);

            assertThat(decoded).isEqualTo(SAMPLE_IMAGE_DATA);
        }
    }

    @Nested
    @DisplayName("GifCaptchaRenderer Tests")
    class GifCaptchaRendererTests {

        private GifCaptchaRenderer renderer;

        @BeforeEach
        void setUp() {
            renderer = new GifCaptchaRenderer();
        }

        @Test
        @DisplayName("should return image/gif content type")
        void shouldReturnImageGifContentType() {
            assertThat(renderer.getContentType()).isEqualTo("image/gif");
        }

        @Test
        @DisplayName("should render to bytes returning image data")
        void shouldRenderToBytesReturningImageData() {
            byte[] result = renderer.renderToBytes(gifCaptcha);

            assertThat(result).isEqualTo(SAMPLE_IMAGE_DATA);
        }

        @Test
        @DisplayName("should render to output stream")
        void shouldRenderToOutputStream() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            renderer.render(gifCaptcha, out);

            assertThat(out.toByteArray()).isEqualTo(SAMPLE_IMAGE_DATA);
        }

        @Test
        @DisplayName("should render to base64 string")
        void shouldRenderToBase64String() {
            String result = renderer.renderToBase64(gifCaptcha);

            String expected = Base64.getEncoder().encodeToString(SAMPLE_IMAGE_DATA);
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Base64CaptchaRenderer Tests")
    class Base64CaptchaRendererTests {

        private Base64CaptchaRenderer renderer;

        @BeforeEach
        void setUp() {
            renderer = new Base64CaptchaRenderer();
        }

        @Test
        @DisplayName("should return text/plain content type")
        void shouldReturnTextPlainContentType() {
            assertThat(renderer.getContentType()).isEqualTo("text/plain");
        }

        @Test
        @DisplayName("should render to base64 data URL")
        void shouldRenderToBase64DataUrl() {
            String result = renderer.renderToBase64(sampleCaptcha);

            assertThat(result).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("should render GIF captcha with image/gif MIME type")
        void shouldRenderGifCaptchaWithGifMimeType() {
            String result = renderer.renderToBase64(gifCaptcha);

            assertThat(result).startsWith("data:image/gif;base64,");
        }

        @Test
        @DisplayName("should render to bytes as UTF-8 data URL")
        void shouldRenderToBytesAsUtf8DataUrl() {
            byte[] result = renderer.renderToBytes(sampleCaptcha);

            String asString = new String(result, StandardCharsets.UTF_8);
            assertThat(asString).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("should render to output stream as UTF-8 data URL")
        void shouldRenderToOutputStreamAsUtf8DataUrl() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            renderer.render(sampleCaptcha, out);

            String asString = out.toString(StandardCharsets.UTF_8);
            assertThat(asString).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("data URL should contain valid base64 after prefix")
        void dataUrlShouldContainValidBase64AfterPrefix() {
            String result = renderer.renderToBase64(sampleCaptcha);

            String base64Part = result.substring(result.indexOf(",") + 1);
            byte[] decoded = Base64.getDecoder().decode(base64Part);

            assertThat(decoded).isEqualTo(SAMPLE_IMAGE_DATA);
        }
    }

    @Nested
    @DisplayName("Content Type Consistency Tests")
    class ContentTypeConsistencyTests {

        @Test
        @DisplayName("all renderers should return non-null content types")
        void allRenderersShouldReturnNonNullContentTypes() {
            assertThat(CaptchaRenderer.image().getContentType()).isNotNull();
            assertThat(CaptchaRenderer.gif().getContentType()).isNotNull();
            assertThat(CaptchaRenderer.base64().getContentType()).isNotNull();
        }

        @Test
        @DisplayName("all renderers should return non-empty content types")
        void allRenderersShouldReturnNonEmptyContentTypes() {
            assertThat(CaptchaRenderer.image().getContentType()).isNotEmpty();
            assertThat(CaptchaRenderer.gif().getContentType()).isNotEmpty();
            assertThat(CaptchaRenderer.base64().getContentType()).isNotEmpty();
        }
    }
}
