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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ImageCaptchaGenerator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("ImageCaptchaGenerator Tests")
class ImageCaptchaGeneratorTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor sets NUMERIC type")
        void constructorSetsNumericType() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            assertThat(generator.getType()).isEqualTo(CaptchaType.NUMERIC);
        }

        @Test
        @DisplayName("constructor sets ALPHA type")
        void constructorSetsAlphaType() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.ALPHA);
            assertThat(generator.getType()).isEqualTo(CaptchaType.ALPHA);
        }

        @Test
        @DisplayName("constructor sets ALPHANUMERIC type")
        void constructorSetsAlphanumericType() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.ALPHANUMERIC);
            assertThat(generator.getType()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("constructor stores the given type immutably")
        void constructorStoresTypeImmutably() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);

            assertThat(generator.getType()).isEqualTo(CaptchaType.NUMERIC);
            assertThat(generator.getType()).isEqualTo(CaptchaType.NUMERIC);
        }
    }

    @Nested
    @DisplayName("Generate NUMERIC Captcha Tests")
    class GenerateNumericTests {

        private final ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);

        @Test
        @DisplayName("generate produces non-null captcha")
        void generateProducesNonNullCaptcha() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha).isNotNull();
        }

        @Test
        @DisplayName("generate produces numeric-only answer")
        void generateProducesNumericOnlyAnswer() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).matches("\\d+");
        }

        @RepeatedTest(10)
        @DisplayName("generate consistently produces numeric-only answers")
        void generateConsistentlyProducesNumericOnlyAnswers() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).matches("\\d+");
        }

        @Test
        @DisplayName("generate produces answer with correct length")
        void generateProducesCorrectLength() {
            CaptchaConfig config = CaptchaConfig.builder().length(6).build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.answer()).hasSize(6);
        }

        @Test
        @DisplayName("generate produces answer with default length of 4")
        void generateProducesDefaultLength() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).hasSize(4);
        }

        @Test
        @DisplayName("generate produces valid PNG image data")
        void generateProducesValidPngImageData() throws Exception {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));
            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("generate produces image with correct dimensions")
        void generateProducesCorrectDimensions() throws Exception {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(200)
                    .height(80)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image.getWidth()).isEqualTo(200);
            assertThat(image.getHeight()).isEqualTo(80);
        }

        @Test
        @DisplayName("generate sets correct NUMERIC type on captcha")
        void generateSetsCorrectType() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.type()).isEqualTo(CaptchaType.NUMERIC);
        }

        @Test
        @DisplayName("generate produces answer with varying lengths")
        void generateProducesAnswerWithVaryingLengths() {
            for (int length = 2; length <= 8; length++) {
                CaptchaConfig config = CaptchaConfig.builder().length(length).build();
                Captcha captcha = generator.generate(config);
                assertThat(captcha.answer()).hasSize(length);
            }
        }
    }

    @Nested
    @DisplayName("Generate ALPHA Captcha Tests")
    class GenerateAlphaTests {

        private final ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.ALPHA);

        @Test
        @DisplayName("generate produces alphabetic-only answer")
        void generateProducesAlphabeticOnlyAnswer() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).matches("[a-zA-Z]+");
        }

        @RepeatedTest(10)
        @DisplayName("generate consistently produces alphabetic-only answers")
        void generateConsistentlyProducesAlphabeticOnlyAnswers() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).matches("[a-zA-Z]+");
        }

        @Test
        @DisplayName("generate produces different answers on multiple calls")
        void generateProducesDifferentAnswers() {
            Captcha captcha1 = generator.generate(CaptchaConfig.defaults());
            Captcha captcha2 = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha1.id()).isNotEqualTo(captcha2.id());
        }

        @Test
        @DisplayName("generate sets correct ALPHA type")
        void generateSetsCorrectType() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHA);
        }

        @Test
        @DisplayName("generate avoids confusing characters like l and I and O")
        void generateAvoidsConfusingCharacters() {
            for (int i = 0; i < 20; i++) {
                Captcha captcha = generator.generate(CaptchaConfig.builder().length(10).build());
                assertThat(captcha.answer()).doesNotContain("l", "I", "O");
            }
        }
    }

    @Nested
    @DisplayName("Generate ALPHANUMERIC Captcha Tests")
    class GenerateAlphanumericTests {

        private final ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.ALPHANUMERIC);

        @Test
        @DisplayName("generate produces alphanumeric answer")
        void generateProducesAlphanumericAnswer() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).matches("[a-zA-Z0-9]+");
        }

        @Test
        @DisplayName("generate sets correct ALPHANUMERIC type")
        void generateSetsCorrectType() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("generate avoids confusing characters")
        void generateAvoidsConfusingCharacters() {
            for (int i = 0; i < 20; i++) {
                Captcha captcha = generator.generate(CaptchaConfig.builder().length(10).build());
                assertThat(captcha.answer()).doesNotContain("0", "1", "l", "I", "O");
            }
        }

        @Test
        @DisplayName("generate produces mix of letters and digits over many iterations")
        void generateProducesMixedContent() {
            boolean hasDigit = false;
            boolean hasLetter = false;
            for (int i = 0; i < 50 && (!hasDigit || !hasLetter); i++) {
                Captcha captcha = generator.generate(CaptchaConfig.builder().length(8).build());
                for (char c : captcha.answer().toCharArray()) {
                    if (Character.isDigit(c)) hasDigit = true;
                    if (Character.isLetter(c)) hasLetter = true;
                }
            }
            assertThat(hasDigit).isTrue();
            assertThat(hasLetter).isTrue();
        }
    }

    @Nested
    @DisplayName("Image Output Verification Tests")
    class ImageOutputTests {

        @Test
        @DisplayName("generated image has correct format (PNG)")
        void generatedImageHasCorrectFormat() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            byte[] pngSignature = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
            byte[] imageStart = new byte[4];
            System.arraycopy(captcha.imageData(), 0, imageStart, 0, 4);

            assertThat(imageStart).isEqualTo(pngSignature);
        }

        @Test
        @DisplayName("generated image has non-empty byte array")
        void generatedImageHasNonEmptyByteArray() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.imageData()).hasSizeGreaterThan(100);
        }

        @Test
        @DisplayName("toBase64 produces valid base64 string")
        void toBase64ProducesValidBase64() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            String base64 = captcha.toBase64();

            assertThat(base64).isNotEmpty();
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("toBase64DataUrl produces valid data URL")
        void toBase64DataUrlProducesValidDataUrl() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            String dataUrl = captcha.toBase64DataUrl();

            assertThat(dataUrl).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("getMimeType returns image/png for non-GIF types")
        void getMimeTypeReturnsPngForImageTypes() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("image contains visible content (not blank)")
        void imageContainsVisibleContent() throws Exception {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
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
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("metadata contains width")
        void metadataContainsWidth() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            CaptchaConfig config = CaptchaConfig.builder().width(200).build();

            Captcha captcha = generator.generate(config);

            assertThat((Integer) captcha.getMetadata("width")).isEqualTo(200);
        }

        @Test
        @DisplayName("metadata contains height")
        void metadataContainsHeight() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            CaptchaConfig config = CaptchaConfig.builder().height(80).build();

            Captcha captcha = generator.generate(config);

            assertThat((Integer) captcha.getMetadata("height")).isEqualTo(80);
        }

        @Test
        @DisplayName("metadata contains length")
        void metadataContainsLength() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            CaptchaConfig config = CaptchaConfig.builder().length(6).build();

            Captcha captcha = generator.generate(config);

            assertThat((Integer) captcha.getMetadata("length")).isEqualTo(6);
        }

        @Test
        @DisplayName("captcha has valid 32-character ID")
        void captchaHasValidId() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.id()).isNotNull();
            assertThat(captcha.id()).hasSize(32);
            assertThat(captcha.id()).matches("[a-f0-9]+");
        }

        @Test
        @DisplayName("captcha has both creation and expiration timestamps")
        void captchaHasTimestamps() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.createdAt()).isNotNull();
            assertThat(captcha.expiresAt()).isNotNull();
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("respects custom font size")
        void respectsCustomFontSize() throws Exception {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            CaptchaConfig config = CaptchaConfig.builder()
                    .fontSize(48.0f)
                    .build();

            Captcha captcha = generator.generate(config);

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));
            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("respects custom background color")
        void respectsCustomBackgroundColor() throws Exception {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            CaptchaConfig config = CaptchaConfig.builder()
                    .backgroundColor(Color.LIGHT_GRAY)
                    .build();

            Captcha captcha = generator.generate(config);

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));
            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("respects custom expire time")
        void respectsCustomExpireTime() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            CaptchaConfig config = CaptchaConfig.builder()
                    .expireTime(Duration.ofMinutes(10))
                    .build();

            Captcha captcha = generator.generate(config);

            Duration expireDuration = Duration.between(captcha.createdAt(), captcha.expiresAt());
            assertThat(expireDuration).isEqualTo(Duration.ofMinutes(10));
        }

        @Test
        @DisplayName("respects noise lines configuration")
        void respectsNoiseLinesConfiguration() throws Exception {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            CaptchaConfig config = CaptchaConfig.builder()
                    .noiseLines(10)
                    .build();

            Captcha captcha = generator.generate(config);

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));
            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("respects noise dots configuration")
        void respectsNoiseDotsConfiguration() throws Exception {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            CaptchaConfig config = CaptchaConfig.builder()
                    .noiseDots(100)
                    .build();

            Captcha captcha = generator.generate(config);

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));
            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("generates successfully with zero noise")
        void generatesWithZeroNoise() throws Exception {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            CaptchaConfig config = CaptchaConfig.builder()
                    .noiseLines(0)
                    .noiseDots(0)
                    .build();

            Captcha captcha = generator.generate(config);

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));
            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("generates successfully with custom font colors")
        void generatesWithCustomFontColors() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            CaptchaConfig config = CaptchaConfig.builder()
                    .fontColors(Color.RED, Color.BLUE)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Uniqueness Tests")
    class UniquenessTests {

        @Test
        @DisplayName("generates unique IDs across multiple captchas")
        void generatesUniqueIds() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);

            String[] ids = new String[10];
            for (int i = 0; i < 10; i++) {
                ids[i] = generator.generate(CaptchaConfig.defaults()).id();
            }

            assertThat(ids).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("generates different image data for different invocations")
        void generatesDifferentImageData() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);

            Captcha captcha1 = generator.generate(CaptchaConfig.defaults());
            Captcha captcha2 = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha1.imageData()).isNotEqualTo(captcha2.imageData());
        }
    }

    @Nested
    @DisplayName("Default Generate Tests")
    class DefaultGenerateTests {

        @Test
        @DisplayName("generate() without config uses defaults")
        void generateWithoutConfigUsesDefaults() {
            ImageCaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);

            Captcha captcha = generator.generate();

            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotNull().isNotEmpty();
            assertThat(captcha.answer()).isNotNull().isNotEmpty();
            assertThat(captcha.getWidth()).isEqualTo(160);
            assertThat(captcha.getHeight()).isEqualTo(60);
        }
    }
}
