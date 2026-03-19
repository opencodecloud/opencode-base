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
import cloud.opencode.base.captcha.support.CaptchaChars;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ChineseCaptchaGenerator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("ChineseCaptchaGenerator Tests")
class ChineseCaptchaGeneratorTest {

    private ChineseCaptchaGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ChineseCaptchaGenerator();
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns CHINESE")
        void getTypeReturnsChinese() {
            assertThat(generator.getType()).isEqualTo(CaptchaType.CHINESE);
        }

        @Test
        @DisplayName("generated captcha has CHINESE type")
        void generatedCaptchaHasChineseType() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.type()).isEqualTo(CaptchaType.CHINESE);
        }

        @Test
        @DisplayName("CHINESE type is text-based")
        void chineseTypeIsTextBased() {
            assertThat(CaptchaType.CHINESE.isTextBased()).isTrue();
        }

        @Test
        @DisplayName("CHINESE type is not interactive")
        void chineseTypeIsNotInteractive() {
            assertThat(CaptchaType.CHINESE.isInteractive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Chinese Character Generation Tests")
    class ChineseCharacterGenerationTests {

        @Test
        @DisplayName("answer contains Chinese characters")
        void answerContainsChineseCharacters() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).matches("[\\u4e00-\\u9fff]+");
        }

        @RepeatedTest(10)
        @DisplayName("answer consistently contains only Chinese characters")
        void answerConsistentlyContainsChineseCharacters() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            for (char c : captcha.answer().toCharArray()) {
                assertThat(Character.UnicodeBlock.of(c))
                        .isEqualTo(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
            }
        }

        @Test
        @DisplayName("answer has correct length")
        void answerHasCorrectLength() {
            CaptchaConfig config = CaptchaConfig.builder().length(4).build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.answer()).hasSize(4);
        }

        @Test
        @DisplayName("answer has correct length for various sizes")
        void answerHasCorrectLengthForVariousSizes() {
            for (int length = 2; length <= 6; length++) {
                CaptchaConfig config = CaptchaConfig.builder().length(length).build();
                Captcha captcha = generator.generate(config);
                assertThat(captcha.answer()).hasSize(length);
            }
        }

        @Test
        @DisplayName("answer uses characters from predefined set")
        void answerUsesCharactersFromPredefinedSet() {
            Set<String> chineseChars = new HashSet<>(Arrays.asList(CaptchaChars.CHINESE));

            for (int i = 0; i < 10; i++) {
                Captcha captcha = generator.generate(CaptchaConfig.defaults());
                for (int j = 0; j < captcha.answer().length(); j++) {
                    String ch = String.valueOf(captcha.answer().charAt(j));
                    assertThat(chineseChars).contains(ch);
                }
            }
        }

        @RepeatedTest(5)
        @DisplayName("generates different Chinese characters across invocations")
        void generatesDifferentChineseCharacters() {
            Set<String> answers = new HashSet<>();
            for (int i = 0; i < 20; i++) {
                answers.add(generator.generate(CaptchaConfig.defaults()).answer());
            }

            assertThat(answers.size()).isGreaterThan(5);
        }

        @Test
        @DisplayName("answer is not null or empty")
        void answerIsNotNullOrEmpty() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.answer()).isNotNull();
            assertThat(captcha.answer()).isNotEmpty();
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
                    .width(240)
                    .height(90)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image.getWidth()).isEqualTo(240);
            assertThat(image.getHeight()).isEqualTo(90);
        }

        @Test
        @DisplayName("generates image with PNG format magic bytes")
        void generatesImageWithPngFormat() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            byte[] pngSignature = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
            byte[] imageStart = new byte[4];
            System.arraycopy(captcha.imageData(), 0, imageStart, 0, 4);

            assertThat(imageStart).isEqualTo(pngSignature);
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
    @DisplayName("Chinese Font Tests")
    class ChineseFontTests {

        @Test
        @DisplayName("image renders without exceptions")
        void imageRendersWithoutExceptions() {
            assertThatCode(() -> {
                Captcha captcha = generator.generate(CaptchaConfig.defaults());
                ImageIO.read(new ByteArrayInputStream(captcha.imageData()));
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("image has visible content for Chinese characters")
        void imageHasVisibleContentForChineseCharacters() throws Exception {
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
        @DisplayName("renders with custom font size")
        void rendersWithCustomFontSize() throws Exception {
            CaptchaConfig config = CaptchaConfig.builder()
                    .fontSize(36.0f)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("renders with large font size")
        void rendersWithLargeFontSize() throws Exception {
            CaptchaConfig config = CaptchaConfig.builder()
                    .fontSize(48.0f)
                    .width(300)
                    .height(100)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image).isNotNull();
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("captcha has valid ID")
        void captchaHasValidId() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.id()).isNotNull();
            assertThat(captcha.id()).hasSize(32);
        }

        @Test
        @DisplayName("metadata contains dimensions")
        void metadataContainsDimensions() {
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(200)
                    .height(80)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.getWidth()).isEqualTo(200);
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
        @DisplayName("captcha has timestamps")
        void captchaHasTimestamps() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.createdAt()).isNotNull();
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
        @DisplayName("respects noise lines configuration")
        void respectsNoiseLinesConfiguration() throws Exception {
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
            CaptchaConfig config = CaptchaConfig.builder()
                    .noiseDots(80)
                    .build();

            Captcha captcha = generator.generate(config);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.imageData()));

            assertThat(image).isNotNull();
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
    }

    @Nested
    @DisplayName("Default Generate Tests")
    class DefaultGenerateTests {

        @Test
        @DisplayName("generate() without config uses defaults")
        void generateWithoutConfigUsesDefaults() {
            Captcha captcha = generator.generate();

            assertThat(captcha).isNotNull();
            assertThat(captcha.answer()).hasSize(4);
            assertThat(captcha.getWidth()).isEqualTo(160);
            assertThat(captcha.getHeight()).isEqualTo(60);
        }

        @Test
        @DisplayName("generate() produces CHINESE type captcha")
        void generateProducesChineseType() {
            Captcha captcha = generator.generate();

            assertThat(captcha.type()).isEqualTo(CaptchaType.CHINESE);
        }
    }

    @Nested
    @DisplayName("Base64 Output Tests")
    class Base64OutputTests {

        @Test
        @DisplayName("toBase64 produces valid base64")
        void toBase64ProducesValidBase64() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            String base64 = captcha.toBase64();

            assertThat(base64).isNotEmpty();
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("toBase64DataUrl has correct prefix")
        void toBase64DataUrlHasCorrectPrefix() {
            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            String dataUrl = captcha.toBase64DataUrl();

            assertThat(dataUrl).startsWith("data:image/png;base64,");
        }

        @Test
        @DisplayName("getMimeType returns PNG")
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
        @DisplayName("generates different image data")
        void generatesDifferentImageData() {
            Captcha captcha1 = generator.generate(CaptchaConfig.defaults());
            Captcha captcha2 = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha1.imageData()).isNotEqualTo(captcha2.imageData());
        }
    }
}
