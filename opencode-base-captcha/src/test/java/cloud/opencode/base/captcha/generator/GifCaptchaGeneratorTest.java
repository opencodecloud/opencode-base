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

import java.time.Duration;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for GifCaptchaGenerator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("GifCaptchaGenerator Tests")
class GifCaptchaGeneratorTest {

    private GifCaptchaGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new GifCaptchaGenerator();
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns GIF")
        void getTypeReturnsGif() {
            assertThat(generator.getType()).isEqualTo(CaptchaType.GIF);
        }

        @Test
        @DisplayName("generated captcha has GIF type")
        void generatedCaptchaHasGifType() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.type()).isEqualTo(CaptchaType.GIF);
        }

        @Test
        @DisplayName("GIF type is not text-based")
        void gifTypeIsNotTextBased() {
            assertThat(CaptchaType.GIF.isTextBased()).isFalse();
        }

        @Test
        @DisplayName("GIF type is not interactive")
        void gifTypeIsNotInteractive() {
            assertThat(CaptchaType.GIF.isInteractive()).isFalse();
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
        @DisplayName("generate returns captcha that is not expired")
        void generateReturnsCaptchaThatIsNotExpired() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("GIF Image Data Tests")
    class GifImageDataTests {

        @Test
        @DisplayName("image data starts with GIF magic bytes")
        void imageDataStartsWithGifMagicBytes() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            byte[] data = captcha.imageData();
            // GIF89a or GIF87a signature
            assertThat(data[0]).isEqualTo((byte) 'G');
            assertThat(data[1]).isEqualTo((byte) 'I');
            assertThat(data[2]).isEqualTo((byte) 'F');
        }

        @Test
        @DisplayName("image data contains GIF89a header for animated GIF")
        void imageDataContainsGif89aHeader() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            byte[] data = captcha.imageData();
            String header = new String(data, 0, 6);
            assertThat(header).isEqualTo("GIF89a");
        }

        @Test
        @DisplayName("image data has reasonable size for animated GIF")
        void imageDataHasReasonableSize() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            // Animated GIF with multiple frames should be larger than a static image
            assertThat(captcha.imageData().length).isGreaterThan(500);
        }

        @Test
        @DisplayName("larger GIF with more frames produces bigger data")
        void largerGifProducesBiggerData() {
            CaptchaConfig smallConfig = CaptchaConfig.builder()
                    .gifFrameCount(3)
                    .build();
            CaptchaConfig largeConfig = CaptchaConfig.builder()
                    .gifFrameCount(15)
                    .build();

            Captcha smallCaptcha = generator.generate(smallConfig);
            Captcha largeCaptcha = generator.generate(largeConfig);

            assertThat(largeCaptcha.imageData().length)
                    .isGreaterThan(smallCaptcha.imageData().length);
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

        @RepeatedTest(10)
        @DisplayName("answer consistently produces alphanumeric characters")
        void answerConsistentlyProducesAlphanumeric() {
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
    @DisplayName("MIME Type Tests")
    class MimeTypeTests {

        @Test
        @DisplayName("getMimeType returns image/gif")
        void getMimeTypeReturnsGif() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.getMimeType()).isEqualTo("image/gif");
        }

        @Test
        @DisplayName("toBase64DataUrl uses gif MIME type")
        void toBase64DataUrlUsesGifMimeType() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            String dataUrl = captcha.toBase64DataUrl();

            assertThat(dataUrl).startsWith("data:image/gif;base64,");
        }

        @Test
        @DisplayName("toBase64 produces valid base64 string")
        void toBase64ProducesValidBase64() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            String base64 = captcha.toBase64();

            assertThat(base64).isNotEmpty();
            assertThat(base64).matches("[A-Za-z0-9+/=]+");

            // Verify round-trip
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
            CaptchaConfig config = CaptchaConfig.builder().length(5).build();
            Captcha captcha = generator.generate(config);

            assertThat((Integer) captcha.getMetadata("length")).isEqualTo(5);
        }

        @Test
        @DisplayName("metadata map is not null or empty")
        void metadataMapIsNotNullOrEmpty() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.metadata()).isNotNull();
            assertThat(captcha.metadata()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("respects custom width and height")
        void respectsCustomWidthAndHeight() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(250)
                    .height(100)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.getWidth()).isEqualTo(250);
            assertThat(captcha.getHeight()).isEqualTo(100);
        }

        @Test
        @DisplayName("respects custom GIF frame count")
        void respectsCustomGifFrameCount() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .gifFrameCount(5)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
        }

        @Test
        @DisplayName("respects custom GIF delay")
        void respectsCustomGifDelay() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .gifDelay(200)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
        }

        @Test
        @DisplayName("respects custom expire time")
        void respectsCustomExpireTime() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .expireTime(Duration.ofMinutes(10))
                    .build();

            Captcha captcha = generator.generate(config);

            Duration actual = Duration.between(captcha.createdAt(), captcha.expiresAt());
            assertThat(actual).isEqualTo(Duration.ofMinutes(10));
        }

        @Test
        @DisplayName("generates successfully with minimal frame count")
        void generatesWithMinimalFrameCount() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .gifFrameCount(1)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
            // Still should be valid GIF
            String header = new String(captcha.imageData(), 0, 3);
            assertThat(header).isEqualTo("GIF");
        }

        @Test
        @DisplayName("generates with custom font size")
        void generatesWithCustomFontSize() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .fontSize(48.0f)
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
            assertThat(captcha.type()).isEqualTo(CaptchaType.GIF);
            assertThat(captcha.getWidth()).isEqualTo(160);
            assertThat(captcha.getHeight()).isEqualTo(60);
        }

        @Test
        @DisplayName("generate() produces GIF format data")
        void generateProducesGifFormatData() {
            Captcha captcha = generator.generate();

            String header = new String(captcha.imageData(), 0, 3);
            assertThat(header).isEqualTo("GIF");
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

    @Nested
    @DisplayName("CaptchaGenerator Interface Tests")
    class CaptchaGeneratorInterfaceTests {

        @Test
        @DisplayName("implements CaptchaGenerator interface")
        void implementsCaptchaGeneratorInterface() {
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("created via CaptchaGenerator.gif() factory")
        void createdViaGifFactory() {
            CaptchaGenerator factoryGenerator = CaptchaGenerator.gif();

            assertThat(factoryGenerator).isInstanceOf(GifCaptchaGenerator.class);
            assertThat(factoryGenerator.getType()).isEqualTo(CaptchaType.GIF);
        }

        @Test
        @DisplayName("created via CaptchaGenerator.forType(GIF)")
        void createdViaForTypeFactory() {
            CaptchaGenerator factoryGenerator = CaptchaGenerator.forType(CaptchaType.GIF);

            assertThat(factoryGenerator).isInstanceOf(GifCaptchaGenerator.class);
        }
    }

    @Nested
    @DisplayName("Animation Frame Tests")
    class AnimationFrameTests {

        @Test
        @DisplayName("GIF with more frames is larger than GIF with fewer frames")
        void moreFramesProducesLargerGif() {
            CaptchaConfig fewFrames = CaptchaConfig.builder()
                    .gifFrameCount(2)
                    .length(4)
                    .build();
            CaptchaConfig manyFrames = CaptchaConfig.builder()
                    .gifFrameCount(20)
                    .length(4)
                    .build();

            Captcha few = generator.generate(fewFrames);
            Captcha many = generator.generate(manyFrames);

            assertThat(many.imageData().length).isGreaterThan(few.imageData().length);
        }

        @Test
        @DisplayName("default frame count of 10 produces valid GIF")
        void defaultFrameCountProducesValidGif() {
            // Default gifFrameCount is 10
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.imageData()).isNotEmpty();
            String header = new String(captcha.imageData(), 0, 6);
            assertThat(header).isEqualTo("GIF89a");
        }

        @Test
        @DisplayName("custom delay does not affect image validity")
        void customDelayDoesNotAffectValidity() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .gifDelay(50)
                    .build();

            Captcha captcha = generator.generate(config);

            String header = new String(captcha.imageData(), 0, 6);
            assertThat(header).isEqualTo("GIF89a");
        }
    }
}
