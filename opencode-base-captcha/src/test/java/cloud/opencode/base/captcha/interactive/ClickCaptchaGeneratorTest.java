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

import java.awt.*;
import java.time.Duration;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ClickCaptchaGenerator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("ClickCaptchaGenerator Tests")
class ClickCaptchaGeneratorTest {

    private ClickCaptchaGenerator generator;
    private CaptchaConfig defaultConfig;

    @BeforeEach
    void setUp() {
        generator = new ClickCaptchaGenerator();
        defaultConfig = CaptchaConfig.builder()
                .width(400)
                .height(200)
                .expireTime(Duration.ofMinutes(5))
                .backgroundColor(Color.WHITE)
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
        @DisplayName("generate returns captcha with CLICK type")
        void generateReturnsCaptchaWithClickType() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.type()).isEqualTo(CaptchaType.CLICK);
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
    }

    @Nested
    @DisplayName("Chinese Character Tests")
    class ChineseCharacterTests {

        @Test
        @DisplayName("metadata contains targetChars with Chinese characters")
        void metadataContainsChineseTargetChars() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("targetChars");
            String targetChars = (String) captcha.metadata().get("targetChars");
            assertThat(targetChars).isNotEmpty();
            assertThat(targetChars).hasSize(4); // CLICK_TARGETS = 4
        }

        @Test
        @DisplayName("target characters are Chinese")
        void targetCharactersAreChinese() {
            Captcha captcha = generator.generate(defaultConfig);

            String targetChars = (String) captcha.metadata().get("targetChars");
            for (char c : targetChars.toCharArray()) {
                assertThat(Character.UnicodeBlock.of(c))
                        .isEqualTo(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
            }
        }

        @RepeatedTest(5)
        @DisplayName("target characters are consistently Chinese across invocations")
        void targetCharactersConsistentlyChinese() {
            Captcha captcha = generator.generate(defaultConfig);

            String targetChars = (String) captcha.metadata().get("targetChars");
            assertThat(targetChars).matches("[\\u4e00-\\u9fff]+");
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
            assertThat((Integer) captcha.metadata().get("width")).isEqualTo(400);
        }

        @Test
        @DisplayName("metadata contains height")
        void metadataContainsHeight() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("height");
            assertThat((Integer) captcha.metadata().get("height")).isEqualTo(200);
        }

        @Test
        @DisplayName("metadata contains targetCount")
        void metadataContainsTargetCount() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("targetCount");
            assertThat((Integer) captcha.metadata().get("targetCount")).isEqualTo(4);
        }

        @Test
        @DisplayName("metadata contains targetSize")
        void metadataContainsTargetSize() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("targetSize");
            assertThat((Integer) captcha.metadata().get("targetSize")).isEqualTo(40);
        }

        @Test
        @DisplayName("metadata contains targetPositions list")
        void metadataContainsTargetPositions() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("targetPositions");
            @SuppressWarnings("unchecked")
            List<Map<String, Integer>> positions =
                    (List<Map<String, Integer>>) captcha.metadata().get("targetPositions");
            assertThat(positions).hasSize(4);
        }

        @Test
        @DisplayName("each target position has x and y coordinates")
        void targetPositionsHaveXAndY() {
            Captcha captcha = generator.generate(defaultConfig);

            @SuppressWarnings("unchecked")
            List<Map<String, Integer>> positions =
                    (List<Map<String, Integer>>) captcha.metadata().get("targetPositions");

            for (Map<String, Integer> pos : positions) {
                assertThat(pos).containsKeys("x", "y");
                assertThat(pos.get("x")).isNotNull();
                assertThat(pos.get("y")).isNotNull();
            }
        }

        @Test
        @DisplayName("target positions are within image bounds")
        void targetPositionsWithinBounds() {
            Captcha captcha = generator.generate(defaultConfig);

            int width = (Integer) captcha.metadata().get("width");
            int height = (Integer) captcha.metadata().get("height");

            @SuppressWarnings("unchecked")
            List<Map<String, Integer>> positions =
                    (List<Map<String, Integer>>) captcha.metadata().get("targetPositions");

            for (Map<String, Integer> pos : positions) {
                int x = pos.get("x");
                int y = pos.get("y");
                assertThat(x).isBetween(0, width);
                assertThat(y).isBetween(0, height);
            }
        }

        @Test
        @DisplayName("metadata contains all expected keys")
        void metadataContainsAllExpectedKeys() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKeys(
                    "width", "height", "targetChars", "targetCount",
                    "targetSize", "targetPositions"
            );
        }
    }

    @Nested
    @DisplayName("Answer Format Tests")
    class AnswerFormatTests {

        @Test
        @DisplayName("answer is pipe-separated coordinates")
        void answerIsPipeSeparatedCoordinates() {
            Captcha captcha = generator.generate(defaultConfig);

            String answer = captcha.answer();
            assertThat(answer).contains("|");
            String[] parts = answer.split("\\|");
            assertThat(parts).hasSize(4); // CLICK_TARGETS = 4
        }

        @Test
        @DisplayName("each coordinate pair is comma-separated")
        void coordinatePairsAreCommaSeparated() {
            Captcha captcha = generator.generate(defaultConfig);

            String[] parts = captcha.answer().split("\\|");
            for (String part : parts) {
                assertThat(part).contains(",");
                String[] coords = part.split(",");
                assertThat(coords).hasSize(2);
            }
        }

        @Test
        @DisplayName("coordinates are valid integers")
        void coordinatesAreValidIntegers() {
            Captcha captcha = generator.generate(defaultConfig);

            String[] parts = captcha.answer().split("\\|");
            for (String part : parts) {
                String[] coords = part.split(",");
                assertThatCode(() -> {
                    Integer.parseInt(coords[0]);
                    Integer.parseInt(coords[1]);
                }).doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("answer coordinates match target positions")
        void answerCoordinatesMatchTargetPositions() {
            Captcha captcha = generator.generate(defaultConfig);

            @SuppressWarnings("unchecked")
            List<Map<String, Integer>> positions =
                    (List<Map<String, Integer>>) captcha.metadata().get("targetPositions");

            String[] parts = captcha.answer().split("\\|");
            for (int i = 0; i < parts.length; i++) {
                String[] coords = parts[i].split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);

                assertThat(x).isEqualTo(positions.get(i).get("x"));
                assertThat(y).isEqualTo(positions.get(i).get("y"));
            }
        }

        @Test
        @DisplayName("answer is not null or empty")
        void answerIsNotNullOrEmpty() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.answer()).isNotNull();
            assertThat(captcha.answer()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Image Data Tests")
    class ImageDataTests {

        @Test
        @DisplayName("image data is valid PNG")
        void imageDataIsValidPng() {
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
        @DisplayName("getMimeType returns image/png")
        void getMimeTypeReturnsPng() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("toBase64DataUrl has correct prefix")
        void toBase64DataUrlHasCorrectPrefix() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.toBase64DataUrl()).startsWith("data:image/png;base64,");
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
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns CLICK")
        void getTypeReturnsClick() {
            assertThat(generator.getType()).isEqualTo(CaptchaType.CLICK);
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
        @DisplayName("created via CaptchaGenerator.forType(CLICK)")
        void createdViaForTypeFactory() {
            CaptchaGenerator factoryGenerator = CaptchaGenerator.forType(CaptchaType.CLICK);

            assertThat(factoryGenerator).isInstanceOf(ClickCaptchaGenerator.class);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("respects custom width and height")
        void respectsCustomWidthAndHeight() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(500)
                    .height(300)
                    .expireTime(Duration.ofMinutes(5))
                    .backgroundColor(Color.WHITE)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat((Integer) captcha.metadata().get("width")).isEqualTo(500);
            assertThat((Integer) captcha.metadata().get("height")).isEqualTo(300);
        }

        @Test
        @DisplayName("respects custom expire time")
        void respectsCustomExpireTime() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(400)
                    .height(200)
                    .expireTime(Duration.ofMinutes(10))
                    .backgroundColor(Color.WHITE)
                    .build();

            Captcha captcha = generator.generate(config);

            long durationMillis = captcha.expiresAt().toEpochMilli() - captcha.createdAt().toEpochMilli();
            assertThat(durationMillis).isCloseTo(Duration.ofMinutes(10).toMillis(), within(1000L));
        }

        @Test
        @DisplayName("respects custom background color")
        void respectsCustomBackgroundColor() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(400)
                    .height(200)
                    .expireTime(Duration.ofMinutes(5))
                    .backgroundColor(Color.LIGHT_GRAY)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Randomness Tests")
    class RandomnessTests {

        @Test
        @DisplayName("generates different target characters each time")
        void generatesDifferentTargetChars() {
            Set<String> allChars = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                Captcha captcha = generator.generate(defaultConfig);
                allChars.add((String) captcha.metadata().get("targetChars"));
            }

            // Should have multiple different character sets
            assertThat(allChars.size()).isGreaterThan(3);
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
        @DisplayName("generates different positions each time")
        void generatesDifferentPositions() {
            Captcha captcha1 = generator.generate(defaultConfig);
            Captcha captcha2 = generator.generate(defaultConfig);

            @SuppressWarnings("unchecked")
            List<Map<String, Integer>> positions1 =
                    (List<Map<String, Integer>>) captcha1.metadata().get("targetPositions");
            @SuppressWarnings("unchecked")
            List<Map<String, Integer>> positions2 =
                    (List<Map<String, Integer>>) captcha2.metadata().get("targetPositions");

            assertThat(positions1).isNotEqualTo(positions2);
        }

        @Test
        @DisplayName("generates different image data each time")
        void generatesDifferentImageData() {
            Captcha captcha1 = generator.generate(defaultConfig);
            Captcha captcha2 = generator.generate(defaultConfig);

            assertThat(captcha1.imageData()).isNotEqualTo(captcha2.imageData());
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
            assertThat(captcha.type()).isEqualTo(CaptchaType.CLICK);
        }
    }
}
