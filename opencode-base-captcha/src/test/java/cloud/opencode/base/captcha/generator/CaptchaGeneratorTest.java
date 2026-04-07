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
import cloud.opencode.base.captcha.interactive.ClickCaptchaGenerator;
import cloud.opencode.base.captcha.interactive.ImageSelectCaptchaGenerator;
import cloud.opencode.base.captcha.interactive.RotateCaptchaGenerator;
import cloud.opencode.base.captcha.interactive.SliderCaptchaGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for CaptchaGenerator sealed interface
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("CaptchaGenerator Tests")
class CaptchaGeneratorTest {

    @Nested
    @DisplayName("Factory Method forType() Tests")
    class ForTypeTests {

        @Test
        @DisplayName("forType(NUMERIC) returns ImageCaptchaGenerator")
        void forTypeNumericReturnsImageCaptchaGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.NUMERIC);

            assertThat(generator).isInstanceOf(ImageCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.NUMERIC);
        }

        @Test
        @DisplayName("forType(ALPHA) returns ImageCaptchaGenerator")
        void forTypeAlphaReturnsImageCaptchaGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.ALPHA);

            assertThat(generator).isInstanceOf(ImageCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.ALPHA);
        }

        @Test
        @DisplayName("forType(ALPHANUMERIC) returns ImageCaptchaGenerator")
        void forTypeAlphanumericReturnsImageCaptchaGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.ALPHANUMERIC);

            assertThat(generator).isInstanceOf(ImageCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("forType(ARITHMETIC) returns ArithmeticCaptchaGenerator")
        void forTypeArithmeticReturnsArithmeticCaptchaGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.ARITHMETIC);

            assertThat(generator).isInstanceOf(ArithmeticCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.ARITHMETIC);
        }

        @Test
        @DisplayName("forType(CHINESE) returns ChineseCaptchaGenerator")
        void forTypeChineseReturnsChineseCaptchaGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.CHINESE);

            assertThat(generator).isInstanceOf(ChineseCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.CHINESE);
        }

        @Test
        @DisplayName("forType(GIF) returns GifCaptchaGenerator")
        void forTypeGifReturnsGifCaptchaGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.GIF);

            assertThat(generator).isInstanceOf(GifCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.GIF);
        }

        @Test
        @DisplayName("forType(SLIDER) returns SliderCaptchaGenerator")
        void forTypeSliderReturnsSliderCaptchaGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.SLIDER);

            assertThat(generator).isInstanceOf(SliderCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.SLIDER);
        }

        @Test
        @DisplayName("forType(CLICK) returns ClickCaptchaGenerator")
        void forTypeClickReturnsClickCaptchaGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.CLICK);

            assertThat(generator).isInstanceOf(ClickCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.CLICK);
        }

        @Test
        @DisplayName("forType(ROTATE) returns RotateCaptchaGenerator")
        void forTypeRotateReturnsRotateCaptchaGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.ROTATE);

            assertThat(generator).isInstanceOf(RotateCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.ROTATE);
        }

        @Test
        @DisplayName("forType(IMAGE_SELECT) returns ImageSelectCaptchaGenerator")
        void forTypeImageSelectReturnsImageSelectCaptchaGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.IMAGE_SELECT);

            assertThat(generator).isInstanceOf(ImageSelectCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.IMAGE_SELECT);
        }

        @ParameterizedTest
        @EnumSource(CaptchaType.class)
        @DisplayName("forType handles every CaptchaType enum value without exception")
        void forTypeHandlesAllEnumValues(CaptchaType type) {
            assertThatCode(() -> CaptchaGenerator.forType(type))
                    .doesNotThrowAnyException();

            CaptchaGenerator generator = CaptchaGenerator.forType(type);
            assertThat(generator).isNotNull();
        }
    }

    @Nested
    @DisplayName("Convenience Factory Methods Tests")
    class ConvenienceFactoryMethodsTests {

        @Test
        @DisplayName("numeric() returns ImageCaptchaGenerator with NUMERIC type")
        void numericReturnsCorrectGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.numeric();

            assertThat(generator).isInstanceOf(ImageCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.NUMERIC);
        }

        @Test
        @DisplayName("alpha() returns ImageCaptchaGenerator with ALPHA type")
        void alphaReturnsCorrectGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.alpha();

            assertThat(generator).isInstanceOf(ImageCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.ALPHA);
        }

        @Test
        @DisplayName("alphanumeric() returns ImageCaptchaGenerator with ALPHANUMERIC type")
        void alphanumericReturnsCorrectGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.alphanumeric();

            assertThat(generator).isInstanceOf(ImageCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("arithmetic() returns ArithmeticCaptchaGenerator")
        void arithmeticReturnsCorrectGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.arithmetic();

            assertThat(generator).isInstanceOf(ArithmeticCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.ARITHMETIC);
        }

        @Test
        @DisplayName("chinese() returns ChineseCaptchaGenerator")
        void chineseReturnsCorrectGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.chinese();

            assertThat(generator).isInstanceOf(ChineseCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.CHINESE);
        }

        @Test
        @DisplayName("gif() returns GifCaptchaGenerator")
        void gifReturnsCorrectGenerator() {
            CaptchaGenerator generator = CaptchaGenerator.gif();

            assertThat(generator).isInstanceOf(GifCaptchaGenerator.class);
            assertThat(generator.getType()).isEqualTo(CaptchaType.GIF);
        }

        @Test
        @DisplayName("each convenience method returns a new instance")
        void convenienceMethodsReturnNewInstances() {
            CaptchaGenerator gen1 = CaptchaGenerator.numeric();
            CaptchaGenerator gen2 = CaptchaGenerator.numeric();

            assertThat(gen1).isNotSameAs(gen2);
        }
    }

    @Nested
    @DisplayName("Default Generate Method Tests")
    class DefaultGenerateMethodTests {

        @Test
        @DisplayName("generate() without config uses default configuration")
        void generateWithoutConfigUsesDefaults() {
            CaptchaGenerator generator = CaptchaGenerator.numeric();

            Captcha captcha = generator.generate();

            assertThat(captcha).isNotNull();
            assertThat(captcha.imageData()).isNotNull();
            assertThat(captcha.imageData()).isNotEmpty();
            assertThat(captcha.answer()).isNotNull();
            assertThat(captcha.answer()).isNotEmpty();
        }

        @Test
        @DisplayName("generate() produces valid captcha with default dimensions")
        void generateProducesValidCaptchaWithDefaultDimensions() {
            CaptchaGenerator generator = CaptchaGenerator.alphanumeric();

            Captcha captcha = generator.generate();

            assertThat(captcha.getWidth()).isEqualTo(160);
            assertThat(captcha.getHeight()).isEqualTo(60);
        }

        @Test
        @DisplayName("generate() produces unique IDs")
        void generateProducesUniqueIds() {
            CaptchaGenerator generator = CaptchaGenerator.numeric();

            Captcha captcha1 = generator.generate();
            Captcha captcha2 = generator.generate();

            assertThat(captcha1.id()).isNotEqualTo(captcha2.id());
        }

        @Test
        @DisplayName("generate() produces captcha that is not expired")
        void generateProducesNonExpiredCaptcha() {
            CaptchaGenerator generator = CaptchaGenerator.alphanumeric();

            Captcha captcha = generator.generate();

            assertThat(captcha.isExpired()).isFalse();
            assertThat(captcha.createdAt()).isNotNull();
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }

        @Test
        @DisplayName("generate() default produces 4-character answer for text-based types")
        void generateDefaultProducesFourCharacterAnswer() {
            CaptchaGenerator generator = CaptchaGenerator.numeric();

            Captcha captcha = generator.generate();

            assertThat(captcha.answer()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("Sealed Interface Permits Tests")
    class SealedInterfacePermitsTests {

        @Test
        @DisplayName("ImageCaptchaGenerator implements CaptchaGenerator")
        void imageCaptchaGeneratorImplementsInterface() {
            CaptchaGenerator generator = new ImageCaptchaGenerator(CaptchaType.NUMERIC);
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("ArithmeticCaptchaGenerator implements CaptchaGenerator")
        void arithmeticCaptchaGeneratorImplementsInterface() {
            CaptchaGenerator generator = new ArithmeticCaptchaGenerator();
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("ChineseCaptchaGenerator implements CaptchaGenerator")
        void chineseCaptchaGeneratorImplementsInterface() {
            CaptchaGenerator generator = new ChineseCaptchaGenerator();
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("GifCaptchaGenerator implements CaptchaGenerator")
        void gifCaptchaGeneratorImplementsInterface() {
            CaptchaGenerator generator = new GifCaptchaGenerator();
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("SpecCaptchaGenerator implements CaptchaGenerator")
        void specCaptchaGeneratorImplementsInterface() {
            CaptchaGenerator generator = new SpecCaptchaGenerator();
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("SliderCaptchaGenerator implements CaptchaGenerator")
        void sliderCaptchaGeneratorImplementsInterface() {
            CaptchaGenerator generator = new SliderCaptchaGenerator();
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("ClickCaptchaGenerator implements CaptchaGenerator")
        void clickCaptchaGeneratorImplementsInterface() {
            CaptchaGenerator generator = new ClickCaptchaGenerator();
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("RotateCaptchaGenerator implements CaptchaGenerator")
        void rotateCaptchaGeneratorImplementsInterface() {
            CaptchaGenerator generator = new RotateCaptchaGenerator();
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("ImageSelectCaptchaGenerator implements CaptchaGenerator")
        void imageSelectCaptchaGeneratorImplementsInterface() {
            CaptchaGenerator generator = new ImageSelectCaptchaGenerator();
            assertThat(generator).isInstanceOf(CaptchaGenerator.class);
        }

        @Test
        @DisplayName("CaptchaGenerator is a sealed interface")
        void captchaGeneratorIsSealed() {
            assertThat(CaptchaGenerator.class.isSealed()).isTrue();
        }

        @Test
        @DisplayName("CaptchaGenerator permits exactly 13 implementations")
        void captchaGeneratorPermitsCorrectImplementations() {
            Class<?>[] permittedSubclasses = CaptchaGenerator.class.getPermittedSubclasses();

            assertThat(permittedSubclasses).isNotNull();
            assertThat(permittedSubclasses).hasSize(13);
        }
    }

    @Nested
    @DisplayName("Generate With Config Tests")
    class GenerateWithConfigTests {

        @Test
        @DisplayName("generate(config) respects custom width and height")
        void generateRespectsCustomDimensions() {
            CaptchaGenerator generator = CaptchaGenerator.numeric();
            CaptchaConfig config = CaptchaConfig.builder()
                    .width(200)
                    .height(80)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.getWidth()).isEqualTo(200);
            assertThat(captcha.getHeight()).isEqualTo(80);
        }

        @Test
        @DisplayName("generate(config) respects custom length")
        void generateRespectsCustomLength() {
            CaptchaGenerator generator = CaptchaGenerator.numeric();
            CaptchaConfig config = CaptchaConfig.builder()
                    .length(6)
                    .build();

            Captcha captcha = generator.generate(config);

            assertThat(captcha.answer()).hasSize(6);
        }

        @Test
        @DisplayName("generate(config) produces non-expired captcha")
        void generateProducesNonExpiredCaptcha() {
            CaptchaGenerator generator = CaptchaGenerator.alphanumeric();

            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.isExpired()).isFalse();
            assertThat(captcha.expiresAt()).isAfter(captcha.createdAt());
        }

        @Test
        @DisplayName("generate(config) respects custom expire time")
        void generateRespectsCustomExpireTime() {
            CaptchaGenerator generator = CaptchaGenerator.numeric();
            CaptchaConfig config = CaptchaConfig.builder()
                    .expireTime(Duration.ofMinutes(10))
                    .build();

            Captcha captcha = generator.generate(config);

            Duration expireDuration = Duration.between(captcha.createdAt(), captcha.expiresAt());
            assertThat(expireDuration).isEqualTo(Duration.ofMinutes(10));
        }

        @Test
        @DisplayName("generate(config) produces captcha with metadata")
        void generateProducesCaptchaWithMetadata() {
            CaptchaGenerator generator = CaptchaGenerator.numeric();

            Captcha captcha = generator.generate(CaptchaConfig.defaults());

            assertThat(captcha.metadata()).isNotNull();
            assertThat(captcha.metadata()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("forType Consistency Tests")
    class ForTypeConsistencyTests {

        @Test
        @DisplayName("forType and convenience methods produce equivalent generators for NUMERIC")
        void forTypeMatchesNumericConvenience() {
            CaptchaGenerator fromForType = CaptchaGenerator.forType(CaptchaType.NUMERIC);
            CaptchaGenerator fromConvenience = CaptchaGenerator.numeric();

            assertThat(fromForType.getClass()).isEqualTo(fromConvenience.getClass());
            assertThat(fromForType.getType()).isEqualTo(fromConvenience.getType());
        }

        @Test
        @DisplayName("forType and convenience methods produce equivalent generators for ALPHA")
        void forTypeMatchesAlphaConvenience() {
            CaptchaGenerator fromForType = CaptchaGenerator.forType(CaptchaType.ALPHA);
            CaptchaGenerator fromConvenience = CaptchaGenerator.alpha();

            assertThat(fromForType.getClass()).isEqualTo(fromConvenience.getClass());
            assertThat(fromForType.getType()).isEqualTo(fromConvenience.getType());
        }

        @Test
        @DisplayName("forType and convenience methods produce equivalent generators for ARITHMETIC")
        void forTypeMatchesArithmeticConvenience() {
            CaptchaGenerator fromForType = CaptchaGenerator.forType(CaptchaType.ARITHMETIC);
            CaptchaGenerator fromConvenience = CaptchaGenerator.arithmetic();

            assertThat(fromForType.getClass()).isEqualTo(fromConvenience.getClass());
            assertThat(fromForType.getType()).isEqualTo(fromConvenience.getType());
        }

        @Test
        @DisplayName("all text-based generators produce image data from generate()")
        void allTextBasedGeneratorsProduceImageData() {
            CaptchaType[] textTypes = {CaptchaType.NUMERIC, CaptchaType.ALPHA,
                    CaptchaType.ALPHANUMERIC, CaptchaType.ARITHMETIC, CaptchaType.CHINESE};

            for (CaptchaType type : textTypes) {
                CaptchaGenerator generator = CaptchaGenerator.forType(type);
                Captcha captcha = generator.generate(CaptchaConfig.defaults());

                assertThat(captcha.imageData())
                        .as("Image data for type %s should not be null or empty", type)
                        .isNotNull()
                        .isNotEmpty();
                assertThat(captcha.answer())
                        .as("Answer for type %s should not be null or empty", type)
                        .isNotNull()
                        .isNotEmpty();
            }
        }

        @Test
        @DisplayName("all generators produce unique IDs across multiple invocations")
        void allGeneratorsProduceUniqueIds() {
            Set<String> ids = new HashSet<>();

            for (CaptchaType type : CaptchaType.values()) {
                CaptchaGenerator generator = CaptchaGenerator.forType(type);
                CaptchaConfig config = CaptchaConfig.builder()
                        .width(300)
                        .height(200)
                        .build();
                Captcha captcha = generator.generate(config);
                ids.add(captcha.id());
            }

            assertThat(ids).hasSize(CaptchaType.values().length);
        }
    }
}
