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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ArithmeticCaptchaGenerator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("ArithmeticCaptchaGenerator Tests")
class ArithmeticCaptchaGeneratorTest {

    private ArithmeticCaptchaGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ArithmeticCaptchaGenerator();
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns ARITHMETIC")
        void getTypeReturnsArithmetic() {
            assertThat(generator.getType()).isEqualTo(CaptchaType.ARITHMETIC);
        }

        @Test
        @DisplayName("generated captcha has ARITHMETIC type")
        void generatedCaptchaHasArithmeticType() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.type()).isEqualTo(CaptchaType.ARITHMETIC);
        }

        @Test
        @DisplayName("ARITHMETIC type is text-based")
        void arithmeticTypeIsTextBased() {
            assertThat(CaptchaType.ARITHMETIC.isTextBased()).isTrue();
        }

        @Test
        @DisplayName("ARITHMETIC type is not interactive")
        void arithmeticTypeIsNotInteractive() {
            assertThat(CaptchaType.ARITHMETIC.isInteractive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Answer Generation Tests")
    class AnswerGenerationTests {

        @Test
        @DisplayName("answer is numeric")
        void answerIsNumeric() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).matches("-?\\d+");
        }

        @RepeatedTest(20)
        @DisplayName("answer is within valid range for arithmetic operations")
        void answerIsWithinValidRange() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());
            int answer = Integer.parseInt(captcha.answer());

            // Addition: max = 10 + 10 = 20, min = 1 + 1 = 2
            // Subtraction: max = 10 - 1 = 9, min = 0
            // Multiplication: max = 10 * 10 = 100, min = 1 * 1 = 1
            assertThat(answer).isBetween(0, 100);
        }

        @RepeatedTest(10)
        @DisplayName("subtraction always produces non-negative result")
        void subtractionProducesNonNegativeResult() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());
            int answer = Integer.parseInt(captcha.answer());

            assertThat(answer).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("answer is not null or empty")
        void answerIsNotNullOrEmpty() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).isNotNull();
            assertThat(captcha.answer()).isNotEmpty();
        }

        @Test
        @DisplayName("answer is parseable as an integer")
        void answerIsParseableAsInteger() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThatCode(() -> Integer.parseInt(captcha.answer()))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Expression Format Tests")
    class ExpressionFormatTests {

        @RepeatedTest(30)
        @DisplayName("expression produces valid answer within arithmetic bounds")
        void expressionProducesValidAnswer() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());
            int answer = Integer.parseInt(captcha.answer());

            assertThat(answer).isBetween(0, 100);
        }

        @Test
        @DisplayName("generates addition-like results (2-20 range)")
        void generatesAdditionExpressions() {
            boolean foundAdditionLikeResult = false;
            for (int i = 0; i < 50; i++) {
                Captcha captcha = generator.generate(CaptchaConfig.defaults());
                int answer = Integer.parseInt(captcha.answer());
                if (answer >= 2 && answer <= 20) {
                    foundAdditionLikeResult = true;
                    break;
                }
            }
            assertThat(foundAdditionLikeResult).isTrue();
        }

        @Test
        @DisplayName("generates multiplication-like results (> 20)")
        void generatesMultiplicationExpressions() {
            boolean foundMultiplicationLikeResult = false;
            for (int i = 0; i < 50; i++) {
                Captcha captcha = generator.generate(CaptchaConfig.defaults());
                int answer = Integer.parseInt(captcha.answer());
                if (answer > 20) {
                    foundMultiplicationLikeResult = true;
                    break;
                }
            }
            assertThat(foundMultiplicationLikeResult).isTrue();
        }

        @Test
        @DisplayName("generates zero results from subtraction (a - a = 0)")
        void generatesZeroSubtractionResult() {
            boolean foundZero = false;
            for (int i = 0; i < 200; i++) {
                Captcha captcha = generator.generate(CaptchaConfig.defaults());
                int answer = Integer.parseInt(captcha.answer());
                if (answer == 0) {
                    foundZero = true;
                    break;
                }
            }
            assertThat(foundZero).isTrue();
        }
    }

    @Nested
    @DisplayName("Image Generation Tests")
    class ImageGenerationTests {

        @Test
        @DisplayName("generates non-null image data")
        void generatesNonNullImageData() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.imageData()).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
        }

        @Test
        @DisplayName("generates valid PNG image")
        void generatesValidPngImage() throws Exception {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));
            assertThat(image).isNotNull();
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
        @DisplayName("generates PNG with correct magic bytes")
        void generatesPngWithCorrectMagicBytes() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());
            byte[] data = captcha.imageData();

            assertThat(data[0]).isEqualTo((byte) 0x89);
            assertThat(data[1]).isEqualTo((byte) 'P');
            assertThat(data[2]).isEqualTo((byte) 'N');
            assertThat(data[3]).isEqualTo((byte) 'G');
        }

        @Test
        @DisplayName("image contains visible content")
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
        @DisplayName("captcha has valid ID")
        void captchaHasValidId() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.id()).isNotNull();
            assertThat(captcha.id()).isNotEmpty();
            assertThat(captcha.id()).hasSize(32);
        }

        @Test
        @DisplayName("captcha has creation timestamp")
        void captchaHasCreationTimestamp() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("captcha has expiration timestamp after creation")
        void captchaHasExpirationTimestamp() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.expiresAt()).isNotNull();
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }

        @Test
        @DisplayName("captcha is not expired initially")
        void captchaIsNotExpiredInitially() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.isExpired()).isFalse();
        }

        @Test
        @DisplayName("metadata map is not null")
        void metadataMapIsNotNull() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.metadata()).isNotNull();
            assertThat(captcha.metadata()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("respects custom font size")
        void respectsCustomFontSize() throws Exception {
            CaptchaConfig config = CaptchaConfig.builder()
                    .fontSize(40.0f)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("respects noise lines configuration")
        void respectsNoiseLinesConfiguration() throws Exception {
            CaptchaConfig config = CaptchaConfig.builder()
                    .noiseLines(15)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("generates with zero noise lines and dots")
        void generatesWithZeroNoiseLines() throws Exception {
            CaptchaConfig config = CaptchaConfig.builder()
                    .noiseLines(0)
                    .noiseDots(0)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image).isNotNull();
        }

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
        @DisplayName("generate() produces ARITHMETIC type")
        void generateProducesArithmeticType() {
            Captcha captcha = generator.generate();

            assertThat(captcha.type()).isEqualTo(CaptchaType.ARITHMETIC);
        }
    }

    @Nested
    @DisplayName("Base64 Output Tests")
    class Base64OutputTests {

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

            assertThat(captcha.toBase64DataUrl()).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("getMimeType returns image/png")
        void getMimeTypeReturnsPng() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.getMimeType()).isEqualTo("image/png");
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
            for (int i = 0; i < 30; i++) {
                answers.add(generator.generate(CaptchaConfig.defaults()).answer());
            }

            assertThat(answers.size()).isGreaterThan(3);
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
