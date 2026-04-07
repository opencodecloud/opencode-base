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
 * Unit tests for {@link PowCaptchaGenerator}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
@DisplayName("PowCaptchaGenerator Tests")
class PowCaptchaGeneratorTest {

    private PowCaptchaGenerator generator;
    private CaptchaConfig defaultConfig;

    @BeforeEach
    void setUp() {
        generator = new PowCaptchaGenerator();
        defaultConfig = CaptchaConfig.builder()
                .powDifficulty(20)
                .expireTime(Duration.ofMinutes(5))
                .build();
    }

    @Nested
    @DisplayName("Generate Tests")
    class GenerateTests {

        @Test
        @DisplayName("should generate PoW CAPTCHA when default config")
        void should_generatePowCaptcha_whenDefaultConfig() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha).isNotNull();
            assertThat(captcha.id()).isNotNull().isNotEmpty();
            assertThat(captcha.type()).isEqualTo(CaptchaType.POW);
            assertThat(captcha.answer()).isNotNull().isNotEmpty();
            assertThat(captcha.createdAt()).isNotNull();
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }

        @Test
        @DisplayName("should have empty image data for PoW type")
        void should_haveEmptyImageData() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.imageData()).hasSize(0);
        }

        @Test
        @DisplayName("should have challenge in metadata as non-empty string")
        void should_haveChallengeInMetadata() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("challenge");
            Object challenge = captcha.metadata().get("challenge");
            assertThat(challenge).isInstanceOf(String.class);
            assertThat((String) challenge).isNotEmpty();
        }

        @Test
        @DisplayName("should have default difficulty 20 in metadata")
        void should_haveDifficultyInMetadata() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata().get("difficulty")).isEqualTo(20);
        }

        @Test
        @DisplayName("should have algorithm SHA-256 in metadata")
        void should_haveAlgorithmInMetadata() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata().get("algorithm")).isEqualTo("SHA-256");
        }

        @Test
        @DisplayName("should have answer in challenge:difficulty format")
        void should_haveAnswerWithChallengeAndDifficulty() {
            Captcha captcha = generator.generate(defaultConfig);

            String answer = captcha.answer();
            String challenge = (String) captcha.metadata().get("challenge");
            int difficulty = (int) captcha.metadata().get("difficulty");

            assertThat(answer).isEqualTo(challenge + ":" + difficulty);
        }
    }

    @Nested
    @DisplayName("Custom Difficulty Tests")
    class CustomDifficultyTests {

        @Test
        @DisplayName("should use configured difficulty of 16")
        void should_useConfiguredDifficulty() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .powDifficulty(16)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.metadata().get("difficulty")).isEqualTo(16);
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("should return POW type")
        void should_returnPowType() {
            assertThat(generator.getType()).isEqualTo(CaptchaType.POW);
        }

        @Test
        @DisplayName("should be invisible type")
        void should_beInvisible() {
            assertThat(CaptchaType.POW.isInvisible()).isTrue();
        }

        @Test
        @DisplayName("should not be interactive type")
        void should_notBeInteractive() {
            assertThat(CaptchaType.POW.isInteractive()).isFalse();
        }
    }
}
