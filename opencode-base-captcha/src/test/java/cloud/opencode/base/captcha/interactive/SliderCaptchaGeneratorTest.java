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

package cloud.opencode.base.captcha.interactive;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.generator.CaptchaGenerator;
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
 * Comprehensive tests for SliderCaptchaGenerator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("SliderCaptchaGenerator Tests")
class SliderCaptchaGeneratorTest {

    private SliderCaptchaGenerator generator;
    private CaptchaConfig defaultConfig;

    @BeforeEach
    void setUp() {
        generator = new SliderCaptchaGenerator();
        defaultConfig = CaptchaConfig.builder()
                .width(300)
                .height(150)
                .expireTime(Duration.ofMinutes(5))
                .build();
    }

    @Nested
    @DisplayName("Basic Generation Tests")
    class BasicGenerationTests {

        @Test
        @DisplayName("generate returns non-null captcha")
        void generateReturnsNonNullCaptcha() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha).isNotNull();
        }

        @Test
        @DisplayName("generate returns captcha with valid id")
        void generateReturnsCaptchaWithValidId() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.id()).isNotNull();
            assertThat(captcha.id()).isNotEmpty();
            assertThat(captcha.id()).hasSize(32);
        }

        @Test
        @DisplayName("generate returns captcha with SLIDER type")
        void generateReturnsCaptchaWithSliderType() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.type()).isEqualTo(CaptchaType.SLIDER);
        }

        @Test
        @DisplayName("generate returns captcha with non-null image data")
        void generateReturnsCaptchaWithImageData() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.imageData()).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
        }

        @Test
        @DisplayName("generate returns captcha with valid timestamps")
        void generateReturnsCaptchaWithValidTimestamps() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.createdAt()).isNotNull();
            assertThat(captcha.expiresAt()).isNotNull();
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }

        @Test
        @DisplayName("generate returns captcha that is not expired")
        void generateReturnsCaptchaThatIsNotExpired() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.isExpired()).isFalse();
        }

        @Test
        @DisplayName("generate returns captcha with answer as puzzle X position")
        void generateReturnsCaptchaWithAnswerAsPuzzlePosition() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.answer()).isNotNull();
            assertThat(captcha.answer()).isNotEmpty();
            int puzzleX = Integer.parseInt(captcha.answer());
            assertThat(puzzleX).isPositive();
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("metadata contains width")
        void metadataContainsWidth() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("width");
            assertThat((Integer) captcha.metadata().get("width")).isEqualTo(300);
        }

        @Test
        @DisplayName("metadata contains height")
        void metadataContainsHeight() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("height");
            assertThat((Integer) captcha.metadata().get("height")).isEqualTo(150);
        }

        @Test
        @DisplayName("metadata does not contain puzzleX (answer is server-side only)")
        void metadataDoesNotContainPuzzleX() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).doesNotContainKey("puzzleX");
            // The answer is stored in captcha.answer(), not in metadata
            int puzzleX = Integer.parseInt(captcha.answer());
            assertThat(puzzleX).isBetween(70, 230);
        }

        @Test
        @DisplayName("metadata contains puzzleY within valid range")
        void metadataContainsPuzzleYInRange() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("puzzleY");
            int puzzleY = (Integer) captcha.metadata().get("puzzleY");
            // puzzleY should be between 10 and (height - puzzleSize - 10)
            // With height=150 and puzzleSize=50, range is 10 to 90
            assertThat(puzzleY).isBetween(10, 90);
        }

        @Test
        @DisplayName("metadata contains puzzleSize of 50")
        void metadataContainsPuzzleSize() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("puzzleSize");
            assertThat((Integer) captcha.metadata().get("puzzleSize")).isEqualTo(50);
        }

        @Test
        @DisplayName("metadata contains valid puzzleImage as base64")
        void metadataContainsPuzzleImageAsBase64() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("puzzleImage");
            String puzzleImageBase64 = (String) captcha.metadata().get("puzzleImage");
            assertThat(puzzleImageBase64).isNotEmpty();

            byte[] decoded = Base64.getDecoder().decode(puzzleImageBase64);
            assertThat(decoded).isNotEmpty();
        }

        @Test
        @DisplayName("puzzleImage is valid PNG data")
        void puzzleImageIsValidPngData() {
            Captcha captcha = generator.generate(defaultConfig);

            String puzzleImageBase64 = (String) captcha.metadata().get("puzzleImage");
            byte[] decoded = Base64.getDecoder().decode(puzzleImageBase64);

            // PNG magic bytes
            assertThat(decoded[0]).isEqualTo((byte) 0x89);
            assertThat(decoded[1]).isEqualTo((byte) 0x50);
            assertThat(decoded[2]).isEqualTo((byte) 0x4E);
            assertThat(decoded[3]).isEqualTo((byte) 0x47);
        }

        @Test
        @DisplayName("metadata contains all expected keys")
        void metadataContainsAllExpectedKeys() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKeys(
                    "width", "height", "puzzleY", "puzzleSize", "puzzleImage"
            );
            assertThat(captcha.metadata()).doesNotContainKey("puzzleX");
        }
    }

    @Nested
    @DisplayName("Image Data Tests")
    class ImageDataTests {

        @Test
        @DisplayName("background image is valid PNG")
        void backgroundImageIsValidPng() {
            Captcha captcha = generator.generate(defaultConfig);

            byte[] imageData = captcha.imageData();
            assertThat(imageData[0]).isEqualTo((byte) 0x89);
            assertThat(imageData[1]).isEqualTo((byte) 0x50);
            assertThat(imageData[2]).isEqualTo((byte) 0x4E);
            assertThat(imageData[3]).isEqualTo((byte) 0x47);
        }

        @Test
        @DisplayName("toBase64 returns valid base64 string")
        void toBase64ReturnsValidString() {
            Captcha captcha = generator.generate(defaultConfig);

            String base64 = captcha.toBase64();
            assertThat(base64).isNotEmpty();

            byte[] decoded = Base64.getDecoder().decode(base64);
            assertThat(decoded).isEqualTo(captcha.imageData());
        }

        @Test
        @DisplayName("toBase64DataUrl returns correct format")
        void toBase64DataUrlReturnsCorrectFormat() {
            Captcha captcha = generator.generate(defaultConfig);

            String dataUrl = captcha.toBase64DataUrl();
            assertThat(dataUrl).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("image data has reasonable size")
        void imageDataHasReasonableSize() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.imageData().length).isGreaterThan(1000);
            assertThat(captcha.imageData().length).isLessThan(500000);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("respects custom width and height")
        void respectsCustomWidthAndHeight() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(400)
                    .height(200)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat((Integer) captcha.metadata().get("width")).isEqualTo(400);
            assertThat((Integer) captcha.metadata().get("height")).isEqualTo(200);
        }

        @Test
        @DisplayName("respects custom expire time")
        void respectsCustomExpireTime() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(300)
                    .height(150)
                    .expireTime(Duration.ofMinutes(10))
                    .build();

            Captcha captcha = generator.generate(config);

            long durationMillis = captcha.expiresAt().toEpochMilli() - captcha.createdAt().toEpochMilli();
            assertThat(durationMillis).isCloseTo(Duration.ofMinutes(10).toMillis(), within(1000L));
        }

        @Test
        @DisplayName("adjusts puzzle position for wider images")
        void adjustsPuzzlePositionForWiderImages() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(500)
                    .height(200)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            int puzzleX = Integer.parseInt(captcha.answer());
            // For width=500, puzzleX range is (50+20) to (500-50-20) = 70 to 430
            assertThat(puzzleX).isBetween(70, 430);
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns SLIDER")
        void getTypeReturnsSlider() {
            assertThat(generator.getType()).isEqualTo(CaptchaType.SLIDER);
        }

        @Test
        @DisplayName("captcha type is interactive")
        void captchaTypeIsInteractive() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.type().isInteractive()).isTrue();
        }

        @Test
        @DisplayName("captcha type is not text-based")
        void captchaTypeIsNotTextBased() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.type().isTextBased()).isFalse();
        }

        @Test
        @DisplayName("implements CaptchaGenerator interface")
        void implementsCaptchaGeneratorInterface() {
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("created via CaptchaGenerator.forType(SLIDER)")
        void createdViaForTypeFactory() {
            CaptchaGenerator factoryGenerator = CaptchaGenerator.forType(CaptchaType.SLIDER);

            assertThat(factoryGenerator).isInstanceOf(SliderCaptchaGenerator.class);
        }
    }

    @Nested
    @DisplayName("Randomness Tests")
    class RandomnessTests {

        @Test
        @DisplayName("generates different puzzle positions each time")
        void generatesDifferentPositionsEachTime() {
            Set<String> positions = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                Captcha captcha = generator.generate(defaultConfig);
                int x = Integer.parseInt(captcha.answer());
                int y = (Integer) captcha.metadata().get("puzzleY");
                positions.add(x + "," + y);
            }

            // Should have multiple distinct positions
            assertThat(positions.size()).isGreaterThan(3);
        }

        @Test
        @DisplayName("generates unique ids")
        void generatesUniqueIds() {
            String[] ids = new String[10];
            for (int i = 0; i < 10; i++) {
                ids[i] = generator.generate(defaultConfig).id();
            }

            assertThat(ids).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("generates different image data")
        void generatesDifferentImageData() {
            Captcha captcha1 = generator.generate(defaultConfig);
            Captcha captcha2 = generator.generate(defaultConfig);

            assertThat(captcha1.imageData()).isNotEqualTo(captcha2.imageData());
        }
    }

    @Nested
    @DisplayName("Answer Validation Tests")
    class AnswerValidationTests {

        @Test
        @DisplayName("answer is not leaked in metadata")
        void answerIsNotLeakedInMetadata() {
            Captcha captcha = generator.generate(defaultConfig);

            // puzzleX (the answer) must NOT be in metadata sent to the client
            assertThat(captcha.metadata()).doesNotContainKey("puzzleX");
            // But the answer is stored server-side in the answer field
            int answerValue = Integer.parseInt(captcha.answer());
            assertThat(answerValue).isPositive();
        }

        @Test
        @DisplayName("answer is parseable as integer")
        void answerIsParseableAsInteger() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThatCode(() -> Integer.parseInt(captcha.answer()))
                    .doesNotThrowAnyException();
        }

        @RepeatedTest(5)
        @DisplayName("answer is consistently a valid positive integer")
        void answerConsistentlyValid() {
            Captcha captcha = generator.generate(defaultConfig);

            int answerValue = Integer.parseInt(captcha.answer());
            assertThat(answerValue).isPositive();
            // Answer should not appear in metadata
            assertThat(captcha.metadata()).doesNotContainKey("puzzleX");
        }
    }

    @Nested
    @DisplayName("Default Generate Tests")
    class DefaultGenerateTests {

        @Test
        @DisplayName("generate with adequate size config produces valid captcha")
        void generateWithAdequateSizeProducesValidCaptcha() {
            // Slider requires dimensions large enough for the puzzle piece (50px + margins)
            // Default config (160x60) is too small for slider, so use adequate size
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(300)
                    .height(150)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotNull().isNotEmpty();
            assertThat(captcha.answer()).isNotNull().isNotEmpty();
            assertThat(captcha.type()).isEqualTo(CaptchaType.SLIDER);
        }
    }
}
