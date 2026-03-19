package cloud.opencode.base.captcha.renderer;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Base64CaptchaRendererTest Tests
 * Base64CaptchaRendererTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("Base64CaptchaRenderer Tests")
class Base64CaptchaRendererTest {

    private Base64CaptchaRenderer renderer;
    private Captcha testCaptcha;

    @BeforeEach
    void setUp() {
        renderer = new Base64CaptchaRenderer();
        testCaptcha = new Captcha(
            "test-id",
            CaptchaType.ALPHANUMERIC,
            new byte[]{1, 2, 3, 4, 5},
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
        @DisplayName("should render captcha as base64 data URL to output stream")
        void shouldRenderToOutputStream() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            renderer.render(testCaptcha, out);

            String result = out.toString(StandardCharsets.UTF_8);
            assertThat(result).startsWith("data:image/png;base64,");
        }
    }

    @Nested
    @DisplayName("Render To Bytes Tests")
    class RenderToBytesTests {

        @Test
        @DisplayName("should render captcha to base64 data URL bytes")
        void shouldRenderToBytes() {
            byte[] bytes = renderer.renderToBytes(testCaptcha);

            String result = new String(bytes, StandardCharsets.UTF_8);
            assertThat(result).startsWith("data:image/png;base64,");
        }
    }

    @Nested
    @DisplayName("Render To Base64 Tests")
    class RenderToBase64Tests {

        @Test
        @DisplayName("should render captcha to base64 data URL string")
        void shouldRenderToBase64() {
            String result = renderer.renderToBase64(testCaptcha);
            assertThat(result).startsWith("data:image/png;base64,");
        }
    }

    @Nested
    @DisplayName("Content Type Tests")
    class ContentTypeTests {

        @Test
        @DisplayName("should return text/plain content type")
        void shouldReturnTextPlainContentType() {
            assertThat(renderer.getContentType()).isEqualTo("text/plain");
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("CaptchaRenderer.base64() should return Base64CaptchaRenderer")
        void shouldCreateViaFactoryMethod() {
            CaptchaRenderer created = CaptchaRenderer.base64();
            assertThat(created).isInstanceOf(Base64CaptchaRenderer.class);
        }
    }
}
