package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * AbstractCaptchaGeneratorTest Tests
 * AbstractCaptchaGeneratorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("AbstractCaptchaGenerator Tests")
class AbstractCaptchaGeneratorTest {

    private TestCaptchaGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new TestCaptchaGenerator();
    }

    @Nested
    @DisplayName("Create Image Tests")
    class CreateImageTests {

        @Test
        @DisplayName("should create image with configured dimensions")
        void shouldCreateImageWithConfiguredDimensions() {
            CaptchaConfig config = CaptchaConfig.builder()
                .width(200)
                .height(80)
                .build();

            BufferedImage image = generator.testCreateImage(config);

            assertThat(image.getWidth()).isEqualTo(200);
            assertThat(image.getHeight()).isEqualTo(80);
        }

        @Test
        @DisplayName("should create RGB image type")
        void shouldCreateRgbImageType() {
            CaptchaConfig config = CaptchaConfig.defaults();

            BufferedImage image = generator.testCreateImage(config);

            assertThat(image.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }
    }

    @Nested
    @DisplayName("Create Graphics Tests")
    class CreateGraphicsTests {

        @Test
        @DisplayName("should create graphics with anti-aliasing enabled")
        void shouldCreateGraphicsWithAntiAliasing() {
            CaptchaConfig config = CaptchaConfig.defaults();
            BufferedImage image = generator.testCreateImage(config);

            Graphics2D g = generator.testCreateGraphics(image, config);

            assertThat(g).isNotNull();
            assertThat(g.getRenderingHint(RenderingHints.KEY_ANTIALIASING))
                .isEqualTo(RenderingHints.VALUE_ANTIALIAS_ON);
            g.dispose();
        }
    }

    @Nested
    @DisplayName("To Bytes Tests")
    class ToBytesTests {

        @Test
        @DisplayName("should convert image to PNG bytes")
        void shouldConvertImageToPngBytes() {
            BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

            byte[] bytes = generator.testToBytes(image);

            assertThat(bytes).isNotEmpty();
            // PNG magic bytes
            assertThat(bytes[0]).isEqualTo((byte) 0x89);
            assertThat(bytes[1]).isEqualTo((byte) 0x50); // 'P'
            assertThat(bytes[2]).isEqualTo((byte) 0x4E); // 'N'
            assertThat(bytes[3]).isEqualTo((byte) 0x47); // 'G'
        }
    }

    @Nested
    @DisplayName("Generate ID Tests")
    class GenerateIdTests {

        @Test
        @DisplayName("should generate non-empty unique ID")
        void shouldGenerateNonEmptyId() {
            String id = generator.testGenerateId();

            assertThat(id).isNotEmpty();
            assertThat(id).doesNotContain("-");
        }

        @Test
        @DisplayName("should generate unique IDs")
        void shouldGenerateUniqueIds() {
            String id1 = generator.testGenerateId();
            String id2 = generator.testGenerateId();

            assertThat(id1).isNotEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("Create Metadata Tests")
    class CreateMetadataTests {

        @Test
        @DisplayName("should create metadata with config values")
        void shouldCreateMetadataWithConfigValues() {
            CaptchaConfig config = CaptchaConfig.builder()
                .width(200)
                .height(80)
                .length(6)
                .build();

            Map<String, Object> metadata = generator.testCreateMetadata(config);

            assertThat(metadata).containsEntry("width", 200);
            assertThat(metadata).containsEntry("height", 80);
            assertThat(metadata).containsEntry("length", 6);
        }
    }

    @Nested
    @DisplayName("Build Captcha Tests")
    class BuildCaptchaTests {

        @Test
        @DisplayName("should build captcha with all fields populated")
        void shouldBuildCaptchaWithAllFields() {
            CaptchaConfig config = CaptchaConfig.builder()
                .width(160)
                .height(60)
                .length(4)
                .build();
            byte[] imageData = new byte[]{1, 2, 3};

            Captcha captcha = generator.testBuildCaptcha(CaptchaType.ALPHANUMERIC, imageData, "abc123", config);

            assertThat(captcha).isNotNull();
            assertThat(captcha.id()).isNotEmpty();
            assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHANUMERIC);
            assertThat(captcha.imageData()).isEqualTo(imageData);
            assertThat(captcha.answer()).isEqualTo("abc123");
            assertThat(captcha.metadata()).containsEntry("width", 160);
            assertThat(captcha.createdAt()).isNotNull();
            assertThat(captcha.expiresAt()).isNotNull();
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }
    }

    // Test subclass to expose protected methods
    private static class TestCaptchaGenerator extends AbstractCaptchaGenerator {

        BufferedImage testCreateImage(CaptchaConfig config) {
            return createImage(config);
        }

        Graphics2D testCreateGraphics(BufferedImage image, CaptchaConfig config) {
            return createGraphics(image, config);
        }

        byte[] testToBytes(BufferedImage image) {
            return toBytes(image);
        }

        String testGenerateId() {
            return generateId();
        }

        Map<String, Object> testCreateMetadata(CaptchaConfig config) {
            return createMetadata(config);
        }

        Captcha testBuildCaptcha(CaptchaType type, byte[] imageData, String answer, CaptchaConfig config) {
            return buildCaptcha(type, imageData, answer, config);
        }
    }
}
