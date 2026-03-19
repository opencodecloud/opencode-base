package cloud.opencode.base.captcha;

import cloud.opencode.base.captcha.store.CaptchaStore;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenCaptcha Test - Unit tests for the main CAPTCHA facade
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class OpenCaptchaTest {

    @Nested
    @DisplayName("Static Create Tests")
    class StaticCreateTests {

        @Test
        @DisplayName("create() should generate a CAPTCHA with defaults")
        void createShouldGenerateCaptchaWithDefaults() {
            Captcha captcha = OpenCaptcha.create();

            assertThat(captcha).isNotNull();
            assertThat(captcha.id()).isNotNull().isNotEmpty();
            assertThat(captcha.answer()).isNotNull().isNotEmpty();
            assertThat(captcha.imageData()).isNotNull().isNotEmpty();
            assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("create(config) should use provided config")
        void createWithConfigShouldUseProvidedConfig() {
            CaptchaConfig config = CaptchaConfig.builder()
                .type(CaptchaType.NUMERIC)
                .length(6)
                .build();

            Captcha captcha = OpenCaptcha.create(config);

            assertThat(captcha).isNotNull();
            assertThat(captcha.type()).isEqualTo(CaptchaType.NUMERIC);
            assertThat(captcha.answer()).hasSize(6);
        }

        @Test
        @DisplayName("create(type) should create captcha of specified type")
        void createWithTypeShouldCreateCaptchaOfSpecifiedType() {
            Captcha captcha = OpenCaptcha.create(CaptchaType.NUMERIC);

            assertThat(captcha).isNotNull();
            assertThat(captcha.type()).isEqualTo(CaptchaType.NUMERIC);
            assertThat(captcha.answer()).matches("\\d+");
        }
    }

    @Nested
    @DisplayName("Type-Specific Static Factory Tests")
    class TypeSpecificStaticFactoryTests {

        @Test
        @DisplayName("numeric() should create numeric CAPTCHA")
        void numericShouldCreateNumericCaptcha() {
            Captcha captcha = OpenCaptcha.numeric();

            assertThat(captcha).isNotNull();
            assertThat(captcha.type()).isEqualTo(CaptchaType.NUMERIC);
            assertThat(captcha.answer()).matches("\\d+");
        }

        @Test
        @DisplayName("alpha() should create alphabetic CAPTCHA")
        void alphaShouldCreateAlphabeticCaptcha() {
            Captcha captcha = OpenCaptcha.alpha();

            assertThat(captcha).isNotNull();
            assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHA);
            assertThat(captcha.answer()).matches("[a-zA-Z]+");
        }

        @Test
        @DisplayName("alphanumeric() should create alphanumeric CAPTCHA")
        void alphanumericShouldCreateAlphanumericCaptcha() {
            Captcha captcha = OpenCaptcha.alphanumeric();

            assertThat(captcha).isNotNull();
            assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHANUMERIC);
            assertThat(captcha.answer()).matches("[a-zA-Z0-9]+");
        }

        @Test
        @DisplayName("arithmetic() should create arithmetic CAPTCHA")
        void arithmeticShouldCreateArithmeticCaptcha() {
            Captcha captcha = OpenCaptcha.arithmetic();

            assertThat(captcha).isNotNull();
            assertThat(captcha.type()).isEqualTo(CaptchaType.ARITHMETIC);
            // Arithmetic answer should be a number
            assertThat(captcha.answer()).matches("-?\\d+");
        }

        @Test
        @DisplayName("chinese() should create Chinese CAPTCHA")
        void chineseShouldCreateChineseCaptcha() {
            Captcha captcha = OpenCaptcha.chinese();

            assertThat(captcha).isNotNull();
            assertThat(captcha.type()).isEqualTo(CaptchaType.CHINESE);
        }

        @Test
        @DisplayName("gif() should create GIF CAPTCHA")
        void gifShouldCreateGifCaptcha() {
            Captcha captcha = OpenCaptcha.gif();

            assertThat(captcha).isNotNull();
            assertThat(captcha.type()).isEqualTo(CaptchaType.GIF);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("should create OpenCaptcha with builder defaults")
        void shouldCreateOpenCaptchaWithBuilderDefaults() {
            OpenCaptcha openCaptcha = OpenCaptcha.builder().build();

            assertThat(openCaptcha).isNotNull();
            assertThat(openCaptcha.getStore()).isNotNull();
            assertThat(openCaptcha.getConfig()).isNotNull();
        }

        @Test
        @DisplayName("should configure store via builder")
        void shouldConfigureStoreViaBuilder() {
            CaptchaStore customStore = CaptchaStore.memory(100);
            OpenCaptcha openCaptcha = OpenCaptcha.builder()
                .store(customStore)
                .build();

            assertThat(openCaptcha.getStore()).isSameAs(customStore);
        }

        @Test
        @DisplayName("should configure config via builder")
        void shouldConfigureConfigViaBuilder() {
            CaptchaConfig customConfig = CaptchaConfig.builder()
                .width(200)
                .height(80)
                .build();

            OpenCaptcha openCaptcha = OpenCaptcha.builder()
                .config(customConfig)
                .build();

            assertThat(openCaptcha.getConfig().getWidth()).isEqualTo(200);
            assertThat(openCaptcha.getConfig().getHeight()).isEqualTo(80);
        }

        @Test
        @DisplayName("should configure type via builder")
        void shouldConfigureTypeViaBuilder() {
            OpenCaptcha openCaptcha = OpenCaptcha.builder()
                .type(CaptchaType.NUMERIC)
                .build();

            assertThat(openCaptcha.getConfig().getType()).isEqualTo(CaptchaType.NUMERIC);
        }
    }

    @Nested
    @DisplayName("Instance Generate Tests")
    class InstanceGenerateTests {

        @Test
        @DisplayName("generate() should create and store CAPTCHA")
        void generateShouldCreateAndStoreCaptcha() {
            CaptchaStore store = CaptchaStore.memory();
            OpenCaptcha openCaptcha = OpenCaptcha.builder()
                .store(store)
                .build();

            Captcha captcha = openCaptcha.generate();

            assertThat(captcha).isNotNull();
            assertThat(store.exists(captcha.id())).isTrue();
        }

        @Test
        @DisplayName("generate(config) should use provided config and store")
        void generateWithConfigShouldUseProvidedConfigAndStore() {
            CaptchaStore store = CaptchaStore.memory();
            OpenCaptcha openCaptcha = OpenCaptcha.builder()
                .store(store)
                .build();

            CaptchaConfig numericConfig = CaptchaConfig.builder()
                .type(CaptchaType.NUMERIC)
                .length(6)
                .build();

            Captcha captcha = openCaptcha.generate(numericConfig);

            assertThat(captcha).isNotNull();
            assertThat(captcha.type()).isEqualTo(CaptchaType.NUMERIC);
            assertThat(store.exists(captcha.id())).isTrue();
        }

        @Test
        @DisplayName("generate() should produce unique IDs")
        void generateShouldProduceUniqueIds() {
            OpenCaptcha openCaptcha = OpenCaptcha.builder().build();

            Captcha c1 = openCaptcha.generate();
            Captcha c2 = openCaptcha.generate();
            Captcha c3 = openCaptcha.generate();

            assertThat(c1.id()).isNotEqualTo(c2.id());
            assertThat(c2.id()).isNotEqualTo(c3.id());
            assertThat(c1.id()).isNotEqualTo(c3.id());
        }
    }

    @Nested
    @DisplayName("Instance Validate Tests")
    class InstanceValidateTests {

        @Test
        @DisplayName("validate() should succeed for correct answer")
        void validateShouldSucceedForCorrectAnswer() {
            OpenCaptcha openCaptcha = OpenCaptcha.builder().build();
            Captcha captcha = openCaptcha.generate();

            ValidationResult result = openCaptcha.validate(captcha.id(), captcha.answer());

            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUCCESS);
        }

        @Test
        @DisplayName("validate() should fail for wrong answer")
        void validateShouldFailForWrongAnswer() {
            OpenCaptcha openCaptcha = OpenCaptcha.builder().build();
            Captcha captcha = openCaptcha.generate();

            ValidationResult result = openCaptcha.validate(captcha.id(), "wronganswer99");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }

        @Test
        @DisplayName("validate() should fail for nonexistent captcha")
        void validateShouldFailForNonexistentCaptcha() {
            OpenCaptcha openCaptcha = OpenCaptcha.builder().build();

            ValidationResult result = openCaptcha.validate("nonexistent", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.NOT_FOUND);
        }

        @Test
        @DisplayName("validate() should consume captcha on use (single use)")
        void validateShouldConsumeCaptchaOnUse() {
            OpenCaptcha openCaptcha = OpenCaptcha.builder().build();
            Captcha captcha = openCaptcha.generate();

            openCaptcha.validate(captcha.id(), captcha.answer());
            ValidationResult second = openCaptcha.validate(captcha.id(), captcha.answer());

            assertThat(second.success()).isFalse();
            assertThat(second.code()).isEqualTo(ValidationResult.ResultCode.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Render Tests")
    class RenderTests {

        @Test
        @DisplayName("should render captcha to output stream")
        void shouldRenderCaptchaToOutputStream() throws IOException {
            OpenCaptcha openCaptcha = OpenCaptcha.builder().build();
            Captcha captcha = openCaptcha.generate();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            openCaptcha.render(captcha, out);

            assertThat(out.toByteArray()).isNotEmpty();
        }

        @Test
        @DisplayName("should render GIF captcha")
        void shouldRenderGifCaptcha() throws IOException {
            OpenCaptcha openCaptcha = OpenCaptcha.builder()
                .type(CaptchaType.GIF)
                .build();
            Captcha captcha = openCaptcha.generate();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            openCaptcha.render(captcha, out);

            assertThat(out.toByteArray()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Accessor Tests")
    class AccessorTests {

        @Test
        @DisplayName("getStore() should return the configured store")
        void getStoreShouldReturnConfiguredStore() {
            CaptchaStore store = CaptchaStore.memory();
            OpenCaptcha openCaptcha = OpenCaptcha.builder()
                .store(store)
                .build();

            assertThat(openCaptcha.getStore()).isSameAs(store);
        }

        @Test
        @DisplayName("getConfig() should return the configured config")
        void getConfigShouldReturnConfiguredConfig() {
            CaptchaConfig config = CaptchaConfig.builder()
                .width(300)
                .build();
            OpenCaptcha openCaptcha = OpenCaptcha.builder()
                .config(config)
                .build();

            assertThat(openCaptcha.getConfig().getWidth()).isEqualTo(300);
        }
    }
}
