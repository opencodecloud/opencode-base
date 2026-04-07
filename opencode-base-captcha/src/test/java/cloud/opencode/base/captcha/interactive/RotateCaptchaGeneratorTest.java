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
 * Comprehensive tests for RotateCaptchaGenerator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("RotateCaptchaGenerator Tests")
class RotateCaptchaGeneratorTest {

    private RotateCaptchaGenerator generator;
    private CaptchaConfig defaultConfig;

    @BeforeEach
    void setUp() {
        generator = new RotateCaptchaGenerator();
        defaultConfig = CaptchaConfig.builder()
                .width(200)
                .height(200)
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
        @DisplayName("generate returns captcha with ROTATE type")
        void generateReturnsCaptchaWithRotateType() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.type()).isEqualTo(CaptchaType.ROTATE);
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
        @DisplayName("generate returns captcha with non-null answer")
        void generateReturnsCaptchaWithAnswer() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.answer()).isNotNull();
            assertThat(captcha.answer()).isNotEmpty();
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
            // Uses min(width, height) = 200
            assertThat((Integer) captcha.metadata().get("width")).isEqualTo(200);
        }

        @Test
        @DisplayName("metadata contains height")
        void metadataContainsHeight() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("height");
            assertThat((Integer) captcha.metadata().get("height")).isEqualTo(200);
        }

        @Test
        @DisplayName("metadata does not contain correctAngle (security)")
        void metadataDoesNotContainCorrectAngle() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).doesNotContainKey("correctAngle");
        }

        @Test
        @DisplayName("metadata contains tolerance")
        void metadataContainsTolerance() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKey("tolerance");
            assertThat((Integer) captcha.metadata().get("tolerance")).isEqualTo(10);
        }

        @Test
        @DisplayName("metadata contains all expected keys")
        void metadataContainsAllExpectedKeys() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.metadata()).containsKeys(
                    "width", "height", "tolerance"
            );
        }
    }

    @Nested
    @DisplayName("Angle Tests")
    class AngleTests {

        @Test
        @DisplayName("correct angle from answer is within valid range")
        void correctAngleInValidRange() {
            Captcha captcha = generator.generate(defaultConfig);

            int angle = Integer.parseInt(captcha.answer());
            assertThat(angle).isBetween(30, 330);
        }

        @RepeatedTest(20)
        @DisplayName("correct angle avoids straight angles")
        void correctAngleAvoidsStraightAngles() {
            Captcha captcha = generator.generate(defaultConfig);
            int angle = Integer.parseInt(captcha.answer());

            // Should not be near 0, 90, 180, 270, 360 (within 20 degrees)
            assertThat(Math.abs(angle - 0)).isGreaterThanOrEqualTo(20);
            assertThat(Math.abs(angle - 90)).isGreaterThanOrEqualTo(20);
            assertThat(Math.abs(angle - 180)).isGreaterThanOrEqualTo(20);
            assertThat(Math.abs(angle - 270)).isGreaterThanOrEqualTo(20);
            assertThat(Math.abs(angle - 360)).isGreaterThanOrEqualTo(20);
        }

        @Test
        @DisplayName("answer is parseable as integer")
        void answerIsParseableAsInteger() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThatCode(() -> Integer.parseInt(captcha.answer()))
                    .doesNotThrowAnyException();
        }

        @RepeatedTest(5)
        @DisplayName("answer consistently represents a valid angle")
        void answerConsistentlyRepresentsAngle() {
            Captcha captcha = generator.generate(defaultConfig);

            int answerAngle = Integer.parseInt(captcha.answer());
            assertThat(answerAngle).isBetween(30, 330);
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
        @DisplayName("toBase64DataUrl returns correct format")
        void toBase64DataUrlReturnsCorrectFormat() {
            Captcha captcha = generator.generate(defaultConfig);

            String dataUrl = captcha.toBase64DataUrl();
            assertThat(dataUrl).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("getMimeType returns image/png")
        void getMimeTypeReturnsPng() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("image has reasonable size")
        void imageHasReasonableSize() {
            Captcha captcha = generator.generate(defaultConfig);

            assertThat(captcha.imageData().length).isGreaterThan(1000);
            assertThat(captcha.imageData().length).isLessThan(500000);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("uses minimum of width and height for size")
        void usesMinimumForSize() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(300)
                    .height(200)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            // Should use min(300, 200) = 200
            assertThat((Integer) captcha.metadata().get("width")).isEqualTo(200);
            assertThat((Integer) captcha.metadata().get("height")).isEqualTo(200);
        }

        @Test
        @DisplayName("respects custom expire time")
        void respectsCustomExpireTime() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(200)
                    .height(200)
                    .expireTime(Duration.ofMinutes(15))
                    .build();

            Captcha captcha = generator.generate(config);

            long durationMillis = captcha.expiresAt().toEpochMilli() - captcha.createdAt().toEpochMilli();
            assertThat(durationMillis).isCloseTo(Duration.ofMinutes(15).toMillis(), within(1000L));
        }

        @Test
        @DisplayName("generates square image")
        void generatesSquareImage() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(250)
                    .height(180)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            int width = (Integer) captcha.metadata().get("width");
            int height = (Integer) captcha.metadata().get("height");
            assertThat(width).isEqualTo(height);
        }

        @Test
        @DisplayName("uses width when width is smaller than height")
        void usesWidthWhenSmaller() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(150)
                    .height(250)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat((Integer) captcha.metadata().get("width")).isEqualTo(150);
            assertThat((Integer) captcha.metadata().get("height")).isEqualTo(150);
        }

        @Test
        @DisplayName("uses height when height is smaller than width")
        void usesHeightWhenSmaller() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(300)
                    .height(180)
                    .expireTime(Duration.ofMinutes(5))
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat((Integer) captcha.metadata().get("width")).isEqualTo(180);
            assertThat((Integer) captcha.metadata().get("height")).isEqualTo(180);
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns ROTATE")
        void getTypeReturnsRotate() {
            assertThat(generator.getType()).isEqualTo(CaptchaType.ROTATE);
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
        @DisplayName("created via CaptchaGenerator.forType(ROTATE)")
        void createdViaForTypeFactory() {
            CaptchaGenerator factoryGenerator = CaptchaGenerator.forType(CaptchaType.ROTATE);

            assertThat(factoryGenerator).isInstanceOf(RotateCaptchaGenerator.class);
        }
    }

    @Nested
    @DisplayName("Randomness Tests")
    class RandomnessTests {

        @Test
        @DisplayName("generates different angles each time")
        void generatesDifferentAngles() {
            Set<Integer> angles = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                Captcha captcha = generator.generate(defaultConfig);
                angles.add(Integer.parseInt(captcha.answer()));
            }

            assertThat(angles.size()).isGreaterThan(3);
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
        @DisplayName("generates different image data each time")
        void generatesDifferentImageData() {
            Captcha captcha1 = generator.generate(defaultConfig);
            Captcha captcha2 = generator.generate(defaultConfig);

            assertThat(captcha1.imageData()).isNotEqualTo(captcha2.imageData());
        }
    }

    @Nested
    @DisplayName("Tolerance Validation Tests")
    class ToleranceValidationTests {

        @Test
        @DisplayName("tolerance is 10 degrees")
        void toleranceIsTenDegrees() {
            Captcha captcha = generator.generate(defaultConfig);

            int tolerance = (Integer) captcha.metadata().get("tolerance");
            assertThat(tolerance).isEqualTo(10);
        }

        @Test
        @DisplayName("angle within tolerance range is computable")
        void angleWithinToleranceRangeIsComputable() {
            Captcha captcha = generator.generate(defaultConfig);

            int correctAngle = Integer.parseInt(captcha.answer());
            int tolerance = (Integer) captcha.metadata().get("tolerance");

            int minValid = correctAngle - tolerance;
            int maxValid = correctAngle + tolerance;

            assertThat(minValid).isLessThan(correctAngle);
            assertThat(maxValid).isGreaterThan(correctAngle);
            assertThat(maxValid - minValid).isEqualTo(2 * tolerance);
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
            assertThat(captcha.type()).isEqualTo(CaptchaType.ROTATE);
        }
    }
}
