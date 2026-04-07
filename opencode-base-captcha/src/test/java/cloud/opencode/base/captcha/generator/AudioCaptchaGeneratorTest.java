package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link AudioCaptchaGenerator}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
@DisplayName("AudioCaptchaGenerator Tests")
class AudioCaptchaGeneratorTest {

    private AudioCaptchaGenerator generator;
    private CaptchaConfig defaultConfig;

    @BeforeEach
    void setUp() {
        generator = new AudioCaptchaGenerator();
        defaultConfig = CaptchaConfig.builder()
                .length(4)
                .expireTime(Duration.ofMinutes(5))
                .build();
    }

    @Nested
    @DisplayName("Generate Tests")
    class GenerateTests {

        @Test
        @DisplayName("should generate audio CAPTCHA when default config")
        void should_generateAudioCaptcha_whenDefaultConfig() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha).isNotNull();
            assertThat(captcha.id()).isNotNull().isNotEmpty();
            assertThat(captcha.type()).isEqualTo(CaptchaType.AUDIO);
            assertThat(captcha.answer()).isNotNull().isNotEmpty();
            assertThat(captcha.createdAt()).isNotNull();
            assertThat(captcha.expiresAt()).isNotNull();
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }

        @Test
        @DisplayName("should have non-empty audio data larger than WAV header")
        void should_haveNonEmptyAudioData() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.imageData()).isNotNull().isNotEmpty();
            // WAV header is 44 bytes; audio data must be larger
            assertThat(captcha.imageData().length).isGreaterThan(44);
        }

        @Test
        @DisplayName("should have valid WAV header with RIFF and WAVE markers")
        void should_haveValidWavHeader() {
            Captcha captcha = generator.generate(defaultConfig);

            byte[] data = captcha.imageData();
            // Bytes 0-3: "RIFF"
            assertThat(new String(data, 0, 4)).isEqualTo("RIFF");
            // Bytes 8-11: "WAVE"
            assertThat(new String(data, 8, 4)).isEqualTo("WAVE");
        }

        @Test
        @DisplayName("should have correct answer length matching config")
        void should_haveCorrectAnswerLength() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.answer()).hasSize(defaultConfig.getLength());
        }

        @Test
        @DisplayName("should have audio metadata with format wav")
        void should_haveAudioMetadata() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("format");
            assertThat(captcha.metadata().get("format")).isEqualTo("wav");
        }

        @Test
        @DisplayName("should have alphanumeric answer only")
        void should_haveAlphanumericAnswer() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.answer()).matches("[a-zA-Z0-9]+");
        }
    }

    @Nested
    @DisplayName("MimeType Tests")
    class MimeTypeTests {

        @Test
        @DisplayName("should return audio/wav MIME type")
        void should_returnAudioWavMimeType() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.getMimeType()).isEqualTo("audio/wav");
        }

        @Test
        @DisplayName("should generate correct Base64 data URL with audio/wav prefix")
        void should_generateCorrectBase64DataUrl() {
            Captcha captcha = generator.generate(defaultConfig);

            String dataUrl = captcha.toBase64DataUrl();
            assertThat(dataUrl).startsWith("data:audio/wav;base64,");
        }
    }

    @Nested
    @DisplayName("Generate With Different Lengths Tests")
    class GenerateWithDifferentLengthsTests {

        @Test
        @DisplayName("should generate audio CAPTCHA with length 1")
        void should_generateAudio_when_lengthIsOne() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .length(1)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.answer()).hasSize(1);
            assertThat(captcha.imageData().length).isGreaterThan(44);
        }

        @Test
        @DisplayName("should generate audio CAPTCHA with length 2")
        void should_generateAudio_when_lengthIsTwo() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .length(2)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.answer()).hasSize(2);
            assertThat(captcha.imageData().length).isGreaterThan(44);
        }

        @Test
        @DisplayName("should generate audio CAPTCHA with maximum length 20")
        void should_generateAudio_when_lengthIsMax() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .length(20)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.answer()).hasSize(20);
            assertThat(captcha.imageData().length).isGreaterThan(44);
        }
    }

    @Nested
    @DisplayName("Audio Speed Variation Tests")
    class AudioSpeedVariationTests {

        @Test
        @DisplayName("should generate audio with zero speed variation")
        void should_generateAudio_when_speedVariationIsZero() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .length(4)
                    .audioSpeedVariation(0.0f)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.imageData()).isNotNull().isNotEmpty();
            assertThat(captcha.imageData().length).isGreaterThan(44);
        }

        @Test
        @DisplayName("should generate audio with maximum speed variation 0.5")
        void should_generateAudio_when_speedVariationIsMax() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .length(4)
                    .audioSpeedVariation(0.5f)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.imageData()).isNotNull().isNotEmpty();
            assertThat(captcha.imageData().length).isGreaterThan(44);
        }
    }

    @Nested
    @DisplayName("WAV Data Validation Tests")
    class WavDataValidationTests {

        @Test
        @DisplayName("should have 16kHz sample rate in WAV header")
        void should_haveSampleRate16kHz_when_wavGenerated() {
            Captcha captcha = generator.generate(defaultConfig);
            byte[] data = captcha.imageData();

            // Sample rate is at bytes 24-27 (little-endian 32-bit int)
            int sampleRate = (data[24] & 0xFF)
                    | ((data[25] & 0xFF) << 8)
                    | ((data[26] & 0xFF) << 16)
                    | ((data[27] & 0xFF) << 24);

            assertThat(sampleRate).isEqualTo(16000);
        }

        @Test
        @DisplayName("should have 16-bit sample size in WAV header")
        void should_have16BitSampleSize_when_wavGenerated() {
            Captcha captcha = generator.generate(defaultConfig);
            byte[] data = captcha.imageData();

            // Bits per sample is at bytes 34-35 (little-endian 16-bit short)
            int bitsPerSample = (data[34] & 0xFF) | ((data[35] & 0xFF) << 8);

            assertThat(bitsPerSample).isEqualTo(16);
        }

        @Test
        @DisplayName("should have mono channel in WAV header")
        void should_haveMonoChannel_when_wavGenerated() {
            Captcha captcha = generator.generate(defaultConfig);
            byte[] data = captcha.imageData();

            // Number of channels is at bytes 22-23 (little-endian 16-bit short)
            int channels = (data[22] & 0xFF) | ((data[23] & 0xFF) << 8);

            assertThat(channels).isEqualTo(1);
        }

        @Test
        @DisplayName("should have PCM format in WAV header")
        void should_havePcmFormat_when_wavGenerated() {
            Captcha captcha = generator.generate(defaultConfig);
            byte[] data = captcha.imageData();

            // Audio format is at bytes 20-21 (little-endian 16-bit short, 1 = PCM)
            int audioFormat = (data[20] & 0xFF) | ((data[21] & 0xFF) << 8);

            assertThat(audioFormat).isEqualTo(1);
        }

        @Test
        @DisplayName("should have fmt and data chunk markers")
        void should_haveChunkMarkers_when_wavGenerated() {
            Captcha captcha = generator.generate(defaultConfig);
            byte[] data = captcha.imageData();

            // "fmt " at bytes 12-15
            assertThat(new String(data, 12, 4)).isEqualTo("fmt ");
            // "data" at bytes 36-39
            assertThat(new String(data, 36, 4)).isEqualTo("data");
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("should return AUDIO type")
        void should_returnAudioType() {
            assertThat(generator.getType()).isEqualTo(CaptchaType.AUDIO);
        }
    }
}
