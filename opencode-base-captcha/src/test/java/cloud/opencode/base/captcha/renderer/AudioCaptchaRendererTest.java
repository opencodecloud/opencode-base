package cloud.opencode.base.captcha.renderer;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.generator.AudioCaptchaGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link AudioCaptchaRenderer}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
@DisplayName("AudioCaptchaRenderer Tests")
class AudioCaptchaRendererTest {

    private AudioCaptchaRenderer renderer;
    private Captcha audioCaptcha;

    @BeforeEach
    void setUp() {
        renderer = new AudioCaptchaRenderer();
        AudioCaptchaGenerator generator = new AudioCaptchaGenerator();
        CaptchaConfig config = CaptchaConfig.builder()
                .length(4)
                .expireTime(Duration.ofMinutes(5))
                .build();
        audioCaptcha = generator.generate(config);
    }

    @Test
    @DisplayName("should return audio/wav content type")
    void should_returnAudioWavContentType() {
        assertThat(renderer.getContentType()).isEqualTo("audio/wav");
    }

    @Test
    @DisplayName("should render complete audio data to output stream")
    void should_renderToOutputStream() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.render(audioCaptcha, out);

        byte[] rendered = out.toByteArray();
        assertThat(rendered).isNotEmpty();
        assertThat(rendered).isEqualTo(audioCaptcha.imageData());
    }

    @Test
    @DisplayName("should render to non-empty Base64 string")
    void should_renderToBase64() {
        String base64 = renderer.renderToBase64(audioCaptcha);

        assertThat(base64).isNotNull().isNotEmpty();
        // Verify it is valid Base64
        assertThatCode(() -> Base64.getDecoder().decode(base64))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should render to bytes identical to original imageData")
    void should_renderToBytes() {
        byte[] rendered = renderer.renderToBytes(audioCaptcha);

        assertThat(rendered).isEqualTo(audioCaptcha.imageData());
    }
}
