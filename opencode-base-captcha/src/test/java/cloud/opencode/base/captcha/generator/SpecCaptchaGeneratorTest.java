/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for SpecCaptchaGenerator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("SpecCaptchaGenerator Tests")
class SpecCaptchaGeneratorTest {

    private SpecCaptchaGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new SpecCaptchaGenerator();
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns ALPHANUMERIC")
        void getTypeReturnsAlphanumeric() {
            assertThat(generator.getType()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("generated captcha has ALPHANUMERIC type")
        void generatedCaptchaHasAlphanumericType() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("implements CaptchaGenerator interface")
        void implementsCaptchaGeneratorInterface() {
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("extends AbstractCaptchaGenerator")
        void extendsAbstractCaptchaGenerator() {
            assertThat(generator).isInstanceOf(AbstractCaptchaGenerator.class);
        }
    }

    @Nested
    @DisplayName("Basic Generation Tests")
    class BasicGenerationTests {

        @Test
        @DisplayName("generate returns non-null captcha")
        void generateReturnsNonNullCaptcha() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha).isNotNull();
        }

        @Test
        @DisplayName("generate returns captcha with non-null image data")
        void generateReturnsCaptchaWithImageData() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.imageData()).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
        }

        @Test
        @DisplayName("generate returns captcha with non-null answer")
        void generateReturnsCaptchaWithAnswer() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).isNotNull();
            assertThat(captcha.answer()).isNotEmpty();
        }

        @Test
        @DisplayName("generate returns captcha with valid ID")
        void generateReturnsCaptchaWithValidId() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.id()).isNotNull();
            assertThat(captcha.id()).isNotEmpty();
            assertThat(captcha.id()).hasSize(32);
        }

        @Test
        @DisplayName("generate returns captcha with valid timestamps")
        void generateReturnsCaptchaWithValidTimestamps() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.createdAt()).isNotNull();
            assertThat(captcha.expiresAt()).isNotNull();
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }

        @Test
        @DisplayName("generate returns non-expired captcha")
        void generateReturnsNonExpiredCaptcha() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("Answer Tests")
    class AnswerTests {

        @Test
        @DisplayName("answer is alphanumeric")
        void answerIsAlphanumeric() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).matches("[a-zA-Z0-9]+");
        }

        @Test
        @DisplayName("answer has correct default length of 4")
        void answerHasCorrectDefaultLength() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).hasSize(4);
        }

        @Test
        @DisplayName("answer respects custom length")
        void answerRespectsCustomLength() {
            CaptchaConfig config = CaptchaConfig.builder().length(6).build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.answer()).hasSize(6);
        }

        @Test
        @DisplayName("answer respects various custom lengths")
        void answerRespectsVariousCustomLengths() {
            for (int length = 2; length <= 8; length++) {
                CaptchaConfig config = CaptchaConfig.builder().length(length).build();
                Captcha captcha = generator.generate(config);
                assertThat(captcha.answer())
                        .as("Answer length should be %d", length)
                        .hasSize(length);
            }
        }

        @RepeatedTest(10)
        @DisplayName("answer consistently contains only alphanumeric characters")
        void answerConsistentlyAlphanumeric() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).matches("[a-zA-Z0-9]+");
        }

        @Test
        @DisplayName("answer avoids confusing characters")
        void answerAvoidsConfusingCharacters() {
            for (int i = 0; i < 20; i++) {
                Captcha captcha = generator.generate(CaptchaConfig.builder().length(10).build());
                assertThat(captcha.answer()).doesNotContain("0", "1", "l", "I", "O");
            }
        }
    }

    @Nested
    @DisplayName("Image Output Tests")
    class ImageOutputTests {

        @Test
        @DisplayName("generates valid PNG image")
        void generatesValidPngImage() throws Exception {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));
            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("generates image with correct PNG magic bytes")
        void generatesImageWithPngMagicBytes() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            byte[] data = captcha.imageData();
            assertThat(data[0]).isEqualTo((byte) 0x89);
            assertThat(data[1]).isEqualTo((byte) 'P');
            assertThat(data[2]).isEqualTo((byte) 'N');
            assertThat(data[3]).isEqualTo((byte) 'G');
        }

        @Test
        @DisplayName("generates image with correct dimensions")
        void generatesImageWithCorrectDimensions() throws Exception {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(250)
                    .height(100)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image.getWidth()).isEqualTo(250);
            assertThat(image.getHeight()).isEqualTo(100);
        }

        @Test
        @DisplayName("generates image with default dimensions")
        void generatesImageWithDefaultDimensions() throws Exception {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image.getWidth()).isEqualTo(160);
            assertThat(image.getHeight()).isEqualTo(60);
        }

        @Test
        @DisplayName("image contains visible content (not uniform)")
        void imageContainsVisibleContent() throws Exception {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            int firstPixel = image.getRGB(0, 0);
            boolean hasVariety = false;
            for (int x = 0; x < image.getWidth() && !hasVariety; x++) {
                for (int y = 0; y < image.getHeight() && !hasVariety; y++) {
                    if (image.getRGB(x, y) != firstPixel) {
                        hasVariety = true;
                    }
                }
            }
            assertThat(hasVariety).isTrue();
        }

        @Test
        @DisplayName("image data has reasonable size")
        void imageDataHasReasonableSize() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.imageData().length).isGreaterThan(100);
            assertThat(captcha.imageData().length).isLessThan(500000);
        }
    }

    @Nested
    @DisplayName("Special Effects Tests")
    class SpecialEffectsTests {

        @Test
        @DisplayName("spec captcha generates without exceptions")
        void specCaptchaGeneratesWithoutExceptions() {
            assertThatCode(() -> generator.generate(CaptchaConfig.defaults()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("spec captcha with custom font size generates successfully")
        void specCaptchaWithCustomFontSize() throws Exception {
            CaptchaConfig config = CaptchaConfig.builder()
                    .fontSize(48.0f)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("spec captcha with large dimensions generates successfully")
        void specCaptchaWithLargeDimensions() throws Exception {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(400)
                    .height(150)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(400);
            assertThat(image.getHeight()).isEqualTo(150);
        }

        @Test
        @DisplayName("spec captcha with custom background color generates successfully")
        void specCaptchaWithCustomBackgroundColor() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .backgroundColor(Color.DARK_GRAY)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
        }

        @Test
        @DisplayName("spec captcha produces different images due to random effects")
        void specCaptchaProducesDifferentImages() {
            Captcha captcha1 = generator.generate(CaptchaConfig.defaults());
            Captcha captcha2 = generator.generate(CaptchaConfig.defaults());

            // Due to random rotation, scale, wave, and shadow effects, images should differ
            assertThat(captcha1.imageData()).isNotEqualTo(captcha2.imageData());
        }
    }

    @Nested
    @DisplayName("MIME Type and Base64 Tests")
    class MimeTypeAndBase64Tests {

        @Test
        @DisplayName("getMimeType returns image/png")
        void getMimeTypeReturnsPng() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("toBase64 produces valid base64 string")
        void toBase64ProducesValidBase64() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            String base64 = captcha.toBase64();

            assertThat(base64).isNotEmpty();
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("toBase64DataUrl has correct PNG prefix")
        void toBase64DataUrlHasCorrectPrefix() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            String dataUrl = captcha.toBase64DataUrl();

            assertThat(dataUrl).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("base64 round-trip produces identical data")
        void base64RoundTripProducesIdenticalData() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            String base64 = captcha.toBase64();
            byte[] decoded = Base64.getDecoder().decode(base64);

            assertThat(decoded).isEqualTo(captcha.imageData());
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("metadata contains width")
        void metadataContainsWidth() {
            CaptchaConfig config = CaptchaConfig.builder().width(200).build();
            Captcha captcha = generator.generate(config);

            assertThat(captcha.getWidth()).isEqualTo(200);
        }

        @Test
        @DisplayName("metadata contains height")
        void metadataContainsHeight() {
            CaptchaConfig config = CaptchaConfig.builder().height(80).build();
            Captcha captcha = generator.generate(config);

            assertThat(captcha.getHeight()).isEqualTo(80);
        }

        @Test
        @DisplayName("metadata contains length")
        void metadataContainsLength() {
            CaptchaConfig config = CaptchaConfig.builder().length(6).build();
            Captcha captcha = generator.generate(config);

            assertThat((Integer) captcha.getMetadata("length")).isEqualTo(6);
        }

        @Test
        @DisplayName("metadata map is not null or empty")
        void metadataMapIsNotNullOrEmpty() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.metadata()).isNotNull();
            assertThat(captcha.metadata()).isNotEmpty();
            assertThat(captcha.metadata()).containsKeys("width", "height", "length");
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("respects custom expire time")
        void respectsCustomExpireTime() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .expireTime(Duration.ofMinutes(15))
                    .build();

            Captcha captcha = generator.generate(config);

            Duration actual = Duration.between(captcha.createdAt(), captcha.expiresAt());
            assertThat(actual).isEqualTo(Duration.ofMinutes(15));
        }

        @Test
        @DisplayName("respects custom noise configuration")
        void respectsCustomNoiseConfiguration() throws Exception {
            CaptchaConfig config = CaptchaConfig.builder()
                    .noiseLines(10)
                    .noiseDots(100)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("generates with zero noise successfully")
        void generatesWithZeroNoise() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .noiseLines(0)
                    .noiseDots(0)
                    .build();

            Captcha captcha = generator.generate(config);
            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
        }

        @Test
        @DisplayName("generates with custom font colors")
        void generatesWithCustomFontColors() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .fontColors(Color.RED, Color.GREEN, Color.BLUE)
                    .build();

            Captcha captcha = generator.generate(config);
            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Default Generate Tests")
    class DefaultGenerateTests {

        @Test
        @DisplayName("generate() without config uses defaults")
        void generateWithoutConfigUsesDefaults() {
            Captcha captcha = generator.generate();

            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotNull().isNotEmpty();
            assertThat(captcha.answer()).isNotNull().isNotEmpty();
            assertThat(captcha.getWidth()).isEqualTo(160);
            assertThat(captcha.getHeight()).isEqualTo(60);
        }

        @Test
        @DisplayName("generate() produces ALPHANUMERIC type")
        void generateProducesAlphanumericType() {
            Captcha captcha = generator.generate();

            assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("generate() produces 4-character answer")
        void generateProducesFourCharacterAnswer() {
            Captcha captcha = generator.generate();

            assertThat(captcha.answer()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("Uniqueness Tests")
    class UniquenessTests {

        @Test
        @DisplayName("generates unique IDs")
        void generatesUniqueIds() {
            String[] ids = new String[10];
            for (int i = 0; i < 10; i++) {
                ids[i] = generator.generate(CaptchaConfig.defaults()).id();
            }

            assertThat(ids).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("generates varying answers")
        void generatesVaryingAnswers() {
            Set<String> answers = new HashSet<>();
            for (int i = 0; i < 20; i++) {
                answers.add(generator.generate(CaptchaConfig.defaults()).answer());
            }

            assertThat(answers.size()).isGreaterThan(5);
        }

        @Test
        @DisplayName("generates different image data across invocations")
        void generatesDifferentImageData() {
            Captcha captcha1 = generator.generate(CaptchaConfig.defaults());
            Captcha captcha2 = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha1.imageData()).isNotEqualTo(captcha2.imageData());
        }
    }
}
