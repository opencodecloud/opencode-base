package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * TestCaptchaGenerator Test - Unit tests for the test CAPTCHA generator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
@DisplayName("TestCaptchaGenerator Tests")
class TestCaptchaGeneratorTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create generator with fixed answer")
        void shouldCreateWithFixedAnswer() {
            TestCaptchaGenerator generator = new TestCaptchaGenerator("ABC123");

            assertThat(generator.getFixedAnswer()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("should reject null fixed answer")
        void shouldRejectNullFixedAnswer() {
            assertThatNullPointerException()
                .isThrownBy(() -> new TestCaptchaGenerator(null))
                .withMessageContaining("fixedAnswer must not be null");
        }

        @Test
        @DisplayName("should accept empty string as fixed answer")
        void shouldAcceptEmptyStringAsFixedAnswer() {
            TestCaptchaGenerator generator = new TestCaptchaGenerator("");

            assertThat(generator.getFixedAnswer()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Generate Tests")
    class GenerateTests {

        private TestCaptchaGenerator generator;

        @BeforeEach
        void setUp() {
            generator = new TestCaptchaGenerator("TEST42");
        }

        @Test
        @DisplayName("should generate captcha with fixed answer")
        void shouldGenerateWithFixedAnswer() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).isEqualTo("TEST42");
        }

        @Test
        @DisplayName("should generate captcha with ALPHANUMERIC type")
        void shouldGenerateWithAlphanumericType() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("should generate captcha with empty image data")
        void shouldGenerateWithEmptyImageData() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.imageData()).isEmpty();
        }

        @Test
        @DisplayName("should generate captcha with non-null ID")
        void shouldGenerateWithNonNullId() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.id()).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("should generate unique IDs for each captcha")
        void shouldGenerateUniqueIds() {
            Captcha captcha1 = generator.generate(CaptchaConfig.defaults());
            Captcha captcha2 = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha1.id()).isNotEqualTo(captcha2.id());
        }

        @Test
        @DisplayName("should generate captcha with test metadata marker")
        void shouldGenerateWithTestMetadata() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.metadata()).containsEntry("test", true);
        }

        @Test
        @DisplayName("should respect config expiration time")
        void shouldRespectConfigExpirationTime() {
            Duration customExpire = Duration.ofMinutes(10);
            CaptchaConfig config = CaptchaConfig.builder()
                .expireTime(customExpire)
                .build();

            Instant beforeGenerate = Instant.now();
            Captcha captcha = generator.generate(config);

            assertThat(captcha.createdAt()).isAfterOrEqualTo(beforeGenerate);
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
            // expiresAt should be approximately createdAt + 10 minutes
            Duration actualDuration = Duration.between(captcha.createdAt(), captcha.expiresAt());
            assertThat(actualDuration).isEqualTo(customExpire);
        }

        @Test
        @DisplayName("should produce same answer regardless of config differences")
        void shouldProduceSameAnswerRegardlessOfConfig() {
            CaptchaConfig config1 = CaptchaConfig.builder()
                .width(100).height(50).length(6).build();
            CaptchaConfig config2 = CaptchaConfig.builder()
                .width(200).height(80).length(4).build();

            Captcha captcha1 = generator.generate(config1);
            Captcha captcha2 = generator.generate(config2);

            assertThat(captcha1.answer()).isEqualTo(captcha2.answer()).isEqualTo("TEST42");
        }

        @Test
        @DisplayName("should reject null config")
        void shouldRejectNullConfig() {
            assertThatNullPointerException()
                .isThrownBy(() -> generator.generate(null))
                .withMessageContaining("config must not be null");
        }
    }

    @Nested
    @DisplayName("GetType Tests")
    class GetTypeTests {

        @Test
        @DisplayName("should return ALPHANUMERIC type")
        void shouldReturnAlphanumericType() {
            TestCaptchaGenerator generator = new TestCaptchaGenerator("any");

            assertThat(generator.getType()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }
    }

    @Nested
    @DisplayName("Default Generate Tests")
    class DefaultGenerateTests {

        @Test
        @DisplayName("should work with default generate() method")
        void shouldWorkWithDefaultGenerate() {
            TestCaptchaGenerator generator = new TestCaptchaGenerator("DEFAULT");

            Captcha captcha = generator.generate();

            assertThat(captcha.answer()).isEqualTo("DEFAULT");
            assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }
    }

    @Nested
    @DisplayName("CaptchaGenerator Interface Contract Tests")
    class InterfaceContractTests {

        @Test
        @DisplayName("should be assignable to CaptchaGenerator")
        void shouldBeAssignableToCaptchaGenerator() {
            CaptchaGenerator generator = new TestCaptchaGenerator("ABC");

            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("should work polymorphically through interface")
        void shouldWorkPolymorphically() {
            CaptchaGenerator generator = new TestCaptchaGenerator("POLY");
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).isEqualTo("POLY");
            assertThat(generator.getType()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }
    }
}
