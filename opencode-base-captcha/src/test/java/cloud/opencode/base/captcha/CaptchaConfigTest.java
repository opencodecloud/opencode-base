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

package cloud.opencode.base.captcha;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for CaptchaConfig
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("CaptchaConfig Tests")
class CaptchaConfigTest {

    @Nested
    @DisplayName("Default Configuration Tests")
    class DefaultConfigurationTests {

        @Test
        @DisplayName("defaults returns config with default width of 160")
        void defaultsReturnsConfigWithDefaultWidth() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getWidth()).isEqualTo(160);
        }

        @Test
        @DisplayName("defaults returns config with default height of 60")
        void defaultsReturnsConfigWithDefaultHeight() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getHeight()).isEqualTo(60);
        }

        @Test
        @DisplayName("defaults returns config with default length of 4")
        void defaultsReturnsConfigWithDefaultLength() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getLength()).isEqualTo(4);
        }

        @Test
        @DisplayName("defaults returns config with ALPHANUMERIC type")
        void defaultsReturnsConfigWithAlphanumericType() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getType()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("defaults returns config with 5 minute expiration")
        void defaultsReturnsConfigWithFiveMinuteExpiration() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getExpireTime()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("defaults returns config with 5 noise lines")
        void defaultsReturnsConfigWithDefaultNoiseLines() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getNoiseLines()).isEqualTo(5);
        }

        @Test
        @DisplayName("defaults returns config with 50 noise dots")
        void defaultsReturnsConfigWithDefaultNoiseDots() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getNoiseDots()).isEqualTo(50);
        }

        @Test
        @DisplayName("defaults returns config with font size 32.0")
        void defaultsReturnsConfigWithDefaultFontSize() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getFontSize()).isEqualTo(32.0f);
        }

        @Test
        @DisplayName("defaults returns config with Arial font")
        void defaultsReturnsConfigWithArialFont() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getFontName()).isEqualTo("Arial");
        }

        @Test
        @DisplayName("defaults returns config with white background")
        void defaultsReturnsConfigWithWhiteBackground() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getBackgroundColor()).isEqualTo(Color.WHITE);
        }

        @Test
        @DisplayName("defaults returns config with 5 font colors")
        void defaultsReturnsConfigWithFiveFontColors() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getFontColors()).hasSize(5);
        }

        @Test
        @DisplayName("defaults returns config with expected font color values")
        void defaultsReturnsConfigWithExpectedFontColors() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getFontColors()).containsExactly(
                new Color(0, 0, 0),
                new Color(0, 0, 255),
                new Color(255, 0, 0),
                new Color(0, 128, 0),
                new Color(128, 0, 128)
            );
        }

        @Test
        @DisplayName("defaults returns config with case insensitive")
        void defaultsReturnsConfigWithCaseInsensitive() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.isCaseSensitive()).isFalse();
        }

        @Test
        @DisplayName("defaults returns config with GIF frame count of 10")
        void defaultsReturnsConfigWithDefaultGifFrameCount() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getGifFrameCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("defaults returns config with GIF delay of 100")
        void defaultsReturnsConfigWithDefaultGifDelay() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getGifDelay()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Builder Width Tests")
    class BuilderWidthTests {

        @Test
        @DisplayName("width sets custom width")
        void widthSetsCustomWidth() {
            CaptchaConfig config = CaptchaConfig.builder()
                .width(200)
                .build();

            assertThat(config.getWidth()).isEqualTo(200);
        }

        @Test
        @DisplayName("width rejects zero")
        void widthRejectsZero() {
            assertThatThrownBy(() -> CaptchaConfig.builder().width(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width");
        }

        @Test
        @DisplayName("width accepts large value")
        void widthAcceptsLargeValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .width(1920)
                .build();

            assertThat(config.getWidth()).isEqualTo(1920);
        }

        @Test
        @DisplayName("width accepts small value")
        void widthAcceptsSmallValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .width(10)
                .build();

            assertThat(config.getWidth()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Builder Height Tests")
    class BuilderHeightTests {

        @Test
        @DisplayName("height sets custom height")
        void heightSetsCustomHeight() {
            CaptchaConfig config = CaptchaConfig.builder()
                .height(80)
                .build();

            assertThat(config.getHeight()).isEqualTo(80);
        }

        @Test
        @DisplayName("height rejects zero")
        void heightRejectsZero() {
            assertThatThrownBy(() -> CaptchaConfig.builder().height(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("height");
        }

        @Test
        @DisplayName("height accepts small value")
        void heightAcceptsSmallValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .height(20)
                .build();

            assertThat(config.getHeight()).isEqualTo(20);
        }

        @Test
        @DisplayName("height accepts large value")
        void heightAcceptsLargeValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .height(1080)
                .build();

            assertThat(config.getHeight()).isEqualTo(1080);
        }
    }

    @Nested
    @DisplayName("Builder Length Tests")
    class BuilderLengthTests {

        @Test
        @DisplayName("length sets custom length")
        void lengthSetsCustomLength() {
            CaptchaConfig config = CaptchaConfig.builder()
                .length(6)
                .build();

            assertThat(config.getLength()).isEqualTo(6);
        }

        @Test
        @DisplayName("length accepts single character")
        void lengthAcceptsSingleCharacter() {
            CaptchaConfig config = CaptchaConfig.builder()
                .length(1)
                .build();

            assertThat(config.getLength()).isEqualTo(1);
        }

        @Test
        @DisplayName("length accepts large value")
        void lengthAcceptsLargeValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .length(10)
                .build();

            assertThat(config.getLength()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Builder Type Tests")
    class BuilderTypeTests {

        @Test
        @DisplayName("type sets NUMERIC type")
        void typeSetsNumericType() {
            CaptchaConfig config = CaptchaConfig.builder()
                .type(CaptchaType.NUMERIC)
                .build();

            assertThat(config.getType()).isEqualTo(CaptchaType.NUMERIC);
        }

        @Test
        @DisplayName("type sets ALPHA type")
        void typeSetsAlphaType() {
            CaptchaConfig config = CaptchaConfig.builder()
                .type(CaptchaType.ALPHA)
                .build();

            assertThat(config.getType()).isEqualTo(CaptchaType.ALPHA);
        }

        @Test
        @DisplayName("type sets ALPHANUMERIC type")
        void typeSetsAlphanumericType() {
            CaptchaConfig config = CaptchaConfig.builder()
                .type(CaptchaType.ALPHANUMERIC)
                .build();

            assertThat(config.getType()).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("type sets ARITHMETIC type")
        void typeSetsArithmeticType() {
            CaptchaConfig config = CaptchaConfig.builder()
                .type(CaptchaType.ARITHMETIC)
                .build();

            assertThat(config.getType()).isEqualTo(CaptchaType.ARITHMETIC);
        }

        @Test
        @DisplayName("type sets CHINESE type")
        void typeSetsChineseType() {
            CaptchaConfig config = CaptchaConfig.builder()
                .type(CaptchaType.CHINESE)
                .build();

            assertThat(config.getType()).isEqualTo(CaptchaType.CHINESE);
        }

        @Test
        @DisplayName("type sets GIF type")
        void typeSetsGifType() {
            CaptchaConfig config = CaptchaConfig.builder()
                .type(CaptchaType.GIF)
                .build();

            assertThat(config.getType()).isEqualTo(CaptchaType.GIF);
        }

        @Test
        @DisplayName("type sets SLIDER type")
        void typeSetsSliderType() {
            CaptchaConfig config = CaptchaConfig.builder()
                .type(CaptchaType.SLIDER)
                .build();

            assertThat(config.getType()).isEqualTo(CaptchaType.SLIDER);
        }

        @Test
        @DisplayName("type sets CLICK type")
        void typeSetsClickType() {
            CaptchaConfig config = CaptchaConfig.builder()
                .type(CaptchaType.CLICK)
                .build();

            assertThat(config.getType()).isEqualTo(CaptchaType.CLICK);
        }

        @Test
        @DisplayName("type sets ROTATE type")
        void typeSetsRotateType() {
            CaptchaConfig config = CaptchaConfig.builder()
                .type(CaptchaType.ROTATE)
                .build();

            assertThat(config.getType()).isEqualTo(CaptchaType.ROTATE);
        }

        @Test
        @DisplayName("type sets IMAGE_SELECT type")
        void typeSetsImageSelectType() {
            CaptchaConfig config = CaptchaConfig.builder()
                .type(CaptchaType.IMAGE_SELECT)
                .build();

            assertThat(config.getType()).isEqualTo(CaptchaType.IMAGE_SELECT);
        }
    }

    @Nested
    @DisplayName("Builder Expiration Tests")
    class BuilderExpirationTests {

        @Test
        @DisplayName("expireTime sets custom duration in minutes")
        void expireTimeSetsCustomDurationMinutes() {
            Duration duration = Duration.ofMinutes(10);

            CaptchaConfig config = CaptchaConfig.builder()
                .expireTime(duration)
                .build();

            assertThat(config.getExpireTime()).isEqualTo(duration);
        }

        @Test
        @DisplayName("expireTime accepts seconds")
        void expireTimeAcceptsSeconds() {
            Duration duration = Duration.ofSeconds(30);

            CaptchaConfig config = CaptchaConfig.builder()
                .expireTime(duration)
                .build();

            assertThat(config.getExpireTime()).isEqualTo(duration);
        }

        @Test
        @DisplayName("expireTime accepts hours")
        void expireTimeAcceptsHours() {
            Duration duration = Duration.ofHours(1);

            CaptchaConfig config = CaptchaConfig.builder()
                .expireTime(duration)
                .build();

            assertThat(config.getExpireTime()).isEqualTo(duration);
        }

        @Test
        @DisplayName("expireTime accepts zero duration")
        void expireTimeAcceptsZero() {
            Duration duration = Duration.ZERO;

            CaptchaConfig config = CaptchaConfig.builder()
                .expireTime(duration)
                .build();

            assertThat(config.getExpireTime()).isEqualTo(Duration.ZERO);
        }
    }

    @Nested
    @DisplayName("Builder Noise Tests")
    class BuilderNoiseTests {

        @Test
        @DisplayName("noiseLines sets custom noise lines count")
        void noiseLinesSetCustomNoiseLines() {
            CaptchaConfig config = CaptchaConfig.builder()
                .noiseLines(10)
                .build();

            assertThat(config.getNoiseLines()).isEqualTo(10);
        }

        @Test
        @DisplayName("noiseLines accepts zero")
        void noiseLinesAcceptsZero() {
            CaptchaConfig config = CaptchaConfig.builder()
                .noiseLines(0)
                .build();

            assertThat(config.getNoiseLines()).isZero();
        }

        @Test
        @DisplayName("noiseLines accepts high value")
        void noiseLinesAcceptsHighValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .noiseLines(50)
                .build();

            assertThat(config.getNoiseLines()).isEqualTo(50);
        }

        @Test
        @DisplayName("noiseDots sets custom noise dots count")
        void noiseDotsSetCustomNoiseDots() {
            CaptchaConfig config = CaptchaConfig.builder()
                .noiseDots(100)
                .build();

            assertThat(config.getNoiseDots()).isEqualTo(100);
        }

        @Test
        @DisplayName("noiseDots accepts zero")
        void noiseDotsAcceptsZero() {
            CaptchaConfig config = CaptchaConfig.builder()
                .noiseDots(0)
                .build();

            assertThat(config.getNoiseDots()).isZero();
        }

        @Test
        @DisplayName("noiseDots accepts high value")
        void noiseDotsAcceptsHighValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .noiseDots(500)
                .build();

            assertThat(config.getNoiseDots()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("Builder Font Tests")
    class BuilderFontTests {

        @Test
        @DisplayName("fontSize sets custom font size")
        void fontSizeSetsCustomFontSize() {
            CaptchaConfig config = CaptchaConfig.builder()
                .fontSize(48.0f)
                .build();

            assertThat(config.getFontSize()).isEqualTo(48.0f);
        }

        @Test
        @DisplayName("fontSize accepts small value")
        void fontSizeAcceptsSmallValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .fontSize(8.0f)
                .build();

            assertThat(config.getFontSize()).isEqualTo(8.0f);
        }

        @Test
        @DisplayName("fontSize accepts fractional value")
        void fontSizeAcceptsFractionalValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .fontSize(24.5f)
                .build();

            assertThat(config.getFontSize()).isEqualTo(24.5f);
        }

        @Test
        @DisplayName("fontName sets custom font name")
        void fontNameSetsCustomFontName() {
            CaptchaConfig config = CaptchaConfig.builder()
                .fontName("Courier New")
                .build();

            assertThat(config.getFontName()).isEqualTo("Courier New");
        }

        @Test
        @DisplayName("fontName sets Times New Roman")
        void fontNameSetsTimesNewRoman() {
            CaptchaConfig config = CaptchaConfig.builder()
                .fontName("Times New Roman")
                .build();

            assertThat(config.getFontName()).isEqualTo("Times New Roman");
        }

        @Test
        @DisplayName("fontName sets Verdana")
        void fontNameSetsVerdana() {
            CaptchaConfig config = CaptchaConfig.builder()
                .fontName("Verdana")
                .build();

            assertThat(config.getFontName()).isEqualTo("Verdana");
        }
    }

    @Nested
    @DisplayName("Builder Color Tests")
    class BuilderColorTests {

        @Test
        @DisplayName("backgroundColor sets custom background color")
        void backgroundColorSetsCustomBackgroundColor() {
            CaptchaConfig config = CaptchaConfig.builder()
                .backgroundColor(Color.LIGHT_GRAY)
                .build();

            assertThat(config.getBackgroundColor()).isEqualTo(Color.LIGHT_GRAY);
        }

        @Test
        @DisplayName("backgroundColor sets black")
        void backgroundColorSetsBlack() {
            CaptchaConfig config = CaptchaConfig.builder()
                .backgroundColor(Color.BLACK)
                .build();

            assertThat(config.getBackgroundColor()).isEqualTo(Color.BLACK);
        }

        @Test
        @DisplayName("backgroundColor sets custom RGB color")
        void backgroundColorSetsCustomRgb() {
            Color customColor = new Color(240, 240, 240);

            CaptchaConfig config = CaptchaConfig.builder()
                .backgroundColor(customColor)
                .build();

            assertThat(config.getBackgroundColor()).isEqualTo(customColor);
        }

        @Test
        @DisplayName("fontColors sets custom font colors")
        void fontColorsSetsCustomFontColors() {
            Color[] colors = new Color[]{Color.RED, Color.BLUE};

            CaptchaConfig config = CaptchaConfig.builder()
                .fontColors(colors)
                .build();

            assertThat(config.getFontColors()).containsExactly(Color.RED, Color.BLUE);
        }

        @Test
        @DisplayName("fontColors accepts single color")
        void fontColorsAcceptsSingleColor() {
            CaptchaConfig config = CaptchaConfig.builder()
                .fontColors(Color.BLACK)
                .build();

            assertThat(config.getFontColors()).hasSize(1);
            assertThat(config.getFontColors()[0]).isEqualTo(Color.BLACK);
        }

        @Test
        @DisplayName("fontColors accepts varargs")
        void fontColorsAcceptsVarargs() {
            CaptchaConfig config = CaptchaConfig.builder()
                .fontColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
                .build();

            assertThat(config.getFontColors()).hasSize(4);
        }

        @Test
        @DisplayName("default fontColors has 5 colors")
        void defaultFontColorsHasFiveColors() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getFontColors()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Builder Case Sensitivity Tests")
    class BuilderCaseSensitivityTests {

        @Test
        @DisplayName("caseSensitive sets true")
        void caseSensitiveSetsTrue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .caseSensitive(true)
                .build();

            assertThat(config.isCaseSensitive()).isTrue();
        }

        @Test
        @DisplayName("caseSensitive sets false")
        void caseSensitiveSetsFalse() {
            CaptchaConfig config = CaptchaConfig.builder()
                .caseSensitive(false)
                .build();

            assertThat(config.isCaseSensitive()).isFalse();
        }

        @Test
        @DisplayName("default caseSensitive is false")
        void defaultCaseSensitiveIsFalse() {
            CaptchaConfig config = CaptchaConfig.builder().build();

            assertThat(config.isCaseSensitive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder GIF Options Tests")
    class BuilderGifOptionsTests {

        @Test
        @DisplayName("gifFrameCount sets custom frame count")
        void gifFrameCountSetsCustomFrameCount() {
            CaptchaConfig config = CaptchaConfig.builder()
                .gifFrameCount(20)
                .build();

            assertThat(config.getGifFrameCount()).isEqualTo(20);
        }

        @Test
        @DisplayName("gifFrameCount accepts small value")
        void gifFrameCountAcceptsSmallValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .gifFrameCount(1)
                .build();

            assertThat(config.getGifFrameCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("gifDelay sets custom delay")
        void gifDelaySetsCustomDelay() {
            CaptchaConfig config = CaptchaConfig.builder()
                .gifDelay(50)
                .build();

            assertThat(config.getGifDelay()).isEqualTo(50);
        }

        @Test
        @DisplayName("gifDelay accepts large value")
        void gifDelayAcceptsLargeValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .gifDelay(500)
                .build();

            assertThat(config.getGifDelay()).isEqualTo(500);
        }

        @Test
        @DisplayName("gifFrameCount and gifDelay can be combined")
        void gifFrameCountAndDelayCanBeCombined() {
            CaptchaConfig config = CaptchaConfig.builder()
                .gifFrameCount(15)
                .gifDelay(80)
                .build();

            assertThat(config.getGifFrameCount()).isEqualTo(15);
            assertThat(config.getGifDelay()).isEqualTo(80);
        }
    }

    @Nested
    @DisplayName("toBuilder Tests")
    class ToBuilderTests {

        @Test
        @DisplayName("toBuilder preserves width")
        void toBuilderPreservesWidth() {
            CaptchaConfig original = CaptchaConfig.builder()
                .width(250)
                .build();

            CaptchaConfig copy = original.toBuilder().build();

            assertThat(copy.getWidth()).isEqualTo(250);
        }

        @Test
        @DisplayName("toBuilder preserves height")
        void toBuilderPreservesHeight() {
            CaptchaConfig original = CaptchaConfig.builder()
                .height(100)
                .build();

            CaptchaConfig copy = original.toBuilder().build();

            assertThat(copy.getHeight()).isEqualTo(100);
        }

        @Test
        @DisplayName("toBuilder preserves length")
        void toBuilderPreservesLength() {
            CaptchaConfig original = CaptchaConfig.builder()
                .length(8)
                .build();

            CaptchaConfig copy = original.toBuilder().build();

            assertThat(copy.getLength()).isEqualTo(8);
        }

        @Test
        @DisplayName("toBuilder preserves type")
        void toBuilderPreservesType() {
            CaptchaConfig original = CaptchaConfig.builder()
                .type(CaptchaType.CHINESE)
                .build();

            CaptchaConfig copy = original.toBuilder().build();

            assertThat(copy.getType()).isEqualTo(CaptchaType.CHINESE);
        }

        @Test
        @DisplayName("toBuilder allows modification while preserving other values")
        void toBuilderAllowsModification() {
            CaptchaConfig original = CaptchaConfig.builder()
                .width(160)
                .height(60)
                .length(4)
                .type(CaptchaType.NUMERIC)
                .build();

            CaptchaConfig modified = original.toBuilder()
                .width(200)
                .build();

            assertThat(modified.getWidth()).isEqualTo(200);
            assertThat(modified.getHeight()).isEqualTo(60);
            assertThat(modified.getLength()).isEqualTo(4);
            assertThat(modified.getType()).isEqualTo(CaptchaType.NUMERIC);
        }

        @Test
        @DisplayName("toBuilder preserves all properties")
        void toBuilderPreservesAllProperties() {
            CaptchaConfig original = CaptchaConfig.builder()
                .width(200)
                .height(80)
                .length(6)
                .type(CaptchaType.ARITHMETIC)
                .expireTime(Duration.ofMinutes(10))
                .noiseLines(10)
                .noiseDots(100)
                .fontSize(40.0f)
                .fontName("Verdana")
                .backgroundColor(Color.CYAN)
                .fontColors(Color.RED, Color.BLUE)
                .caseSensitive(true)
                .gifFrameCount(15)
                .gifDelay(75)
                .build();

            CaptchaConfig copy = original.toBuilder().build();

            assertThat(copy.getWidth()).isEqualTo(200);
            assertThat(copy.getHeight()).isEqualTo(80);
            assertThat(copy.getLength()).isEqualTo(6);
            assertThat(copy.getType()).isEqualTo(CaptchaType.ARITHMETIC);
            assertThat(copy.getExpireTime()).isEqualTo(Duration.ofMinutes(10));
            assertThat(copy.getNoiseLines()).isEqualTo(10);
            assertThat(copy.getNoiseDots()).isEqualTo(100);
            assertThat(copy.getFontSize()).isEqualTo(40.0f);
            assertThat(copy.getFontName()).isEqualTo("Verdana");
            assertThat(copy.getBackgroundColor()).isEqualTo(Color.CYAN);
            assertThat(copy.getFontColors()).containsExactly(Color.RED, Color.BLUE);
            assertThat(copy.isCaseSensitive()).isTrue();
            assertThat(copy.getGifFrameCount()).isEqualTo(15);
            assertThat(copy.getGifDelay()).isEqualTo(75);
        }

        @Test
        @DisplayName("toBuilder creates independent builder")
        void toBuilderCreatesIndependentBuilder() {
            CaptchaConfig original = CaptchaConfig.builder()
                .width(160)
                .build();

            CaptchaConfig.Builder builder = original.toBuilder();
            CaptchaConfig copy1 = builder.width(200).build();
            CaptchaConfig copy2 = builder.width(300).build();

            assertThat(original.getWidth()).isEqualTo(160);
            assertThat(copy1.getWidth()).isEqualTo(200);
            assertThat(copy2.getWidth()).isEqualTo(300);
        }
    }

    @Nested
    @DisplayName("Builder Chaining Tests")
    class BuilderChainingTests {

        @Test
        @DisplayName("all builder methods can be chained")
        void allBuilderMethodsCanBeChained() {
            CaptchaConfig config = CaptchaConfig.builder()
                .width(200)
                .height(80)
                .length(6)
                .type(CaptchaType.NUMERIC)
                .expireTime(Duration.ofMinutes(10))
                .noiseLines(8)
                .noiseDots(80)
                .fontSize(36.0f)
                .fontName("Helvetica")
                .backgroundColor(Color.YELLOW)
                .fontColors(Color.RED, Color.BLUE, Color.GREEN)
                .caseSensitive(true)
                .gifFrameCount(12)
                .gifDelay(120)
                .build();

            assertThat(config.getWidth()).isEqualTo(200);
            assertThat(config.getHeight()).isEqualTo(80);
            assertThat(config.getLength()).isEqualTo(6);
            assertThat(config.getType()).isEqualTo(CaptchaType.NUMERIC);
            assertThat(config.getExpireTime()).isEqualTo(Duration.ofMinutes(10));
            assertThat(config.getNoiseLines()).isEqualTo(8);
            assertThat(config.getNoiseDots()).isEqualTo(80);
            assertThat(config.getFontSize()).isEqualTo(36.0f);
            assertThat(config.getFontName()).isEqualTo("Helvetica");
            assertThat(config.getBackgroundColor()).isEqualTo(Color.YELLOW);
            assertThat(config.getFontColors()).containsExactly(Color.RED, Color.BLUE, Color.GREEN);
            assertThat(config.isCaseSensitive()).isTrue();
            assertThat(config.getGifFrameCount()).isEqualTo(12);
            assertThat(config.getGifDelay()).isEqualTo(120);
        }

        @Test
        @DisplayName("builder returns new config instance each time build is called")
        void builderReturnsNewConfigInstanceEachTime() {
            CaptchaConfig.Builder builder = CaptchaConfig.builder();

            CaptchaConfig config1 = builder.width(100).build();
            CaptchaConfig config2 = builder.width(200).build();

            assertThat(config1).isNotSameAs(config2);
        }

        @Test
        @DisplayName("each builder method returns the same builder instance")
        void eachBuilderMethodReturnsSameBuilderInstance() {
            CaptchaConfig.Builder builder = CaptchaConfig.builder();

            CaptchaConfig.Builder returned = builder.width(100);

            assertThat(returned).isSameAs(builder);
        }

        @Test
        @DisplayName("last value wins when property set multiple times")
        void lastValueWinsWhenSetMultipleTimes() {
            CaptchaConfig config = CaptchaConfig.builder()
                .width(100)
                .width(200)
                .width(300)
                .build();

            assertThat(config.getWidth()).isEqualTo(300);
        }
    }

    @Nested
    @DisplayName("Builder Validation Tests")
    class BuilderValidationTests {

        // ---- width bounds ----

        @Test
        @DisplayName("width rejects negative value")
        void widthRejectsNegative() {
            assertThatThrownBy(() -> CaptchaConfig.builder().width(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width");
        }

        @Test
        @DisplayName("width rejects value exceeding maximum")
        void widthRejectsExceedingMax() {
            assertThatThrownBy(() -> CaptchaConfig.builder().width(2001).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width");
        }

        @Test
        @DisplayName("width accepts boundary value 1")
        void widthAcceptsMinBoundary() {
            CaptchaConfig config = CaptchaConfig.builder().width(1).build();
            assertThat(config.getWidth()).isEqualTo(1);
        }

        @Test
        @DisplayName("width accepts boundary value 2000")
        void widthAcceptsMaxBoundary() {
            CaptchaConfig config = CaptchaConfig.builder().width(2000).build();
            assertThat(config.getWidth()).isEqualTo(2000);
        }

        // ---- height bounds ----

        @Test
        @DisplayName("height rejects negative value")
        void heightRejectsNegative() {
            assertThatThrownBy(() -> CaptchaConfig.builder().height(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("height");
        }

        @Test
        @DisplayName("height rejects value exceeding maximum")
        void heightRejectsExceedingMax() {
            assertThatThrownBy(() -> CaptchaConfig.builder().height(2001).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("height");
        }

        @Test
        @DisplayName("height accepts boundary value 1")
        void heightAcceptsMinBoundary() {
            CaptchaConfig config = CaptchaConfig.builder().height(1).build();
            assertThat(config.getHeight()).isEqualTo(1);
        }

        @Test
        @DisplayName("height accepts boundary value 2000")
        void heightAcceptsMaxBoundary() {
            CaptchaConfig config = CaptchaConfig.builder().height(2000).build();
            assertThat(config.getHeight()).isEqualTo(2000);
        }

        // ---- length bounds ----

        @Test
        @DisplayName("length rejects zero")
        void lengthRejectsZero() {
            assertThatThrownBy(() -> CaptchaConfig.builder().length(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("length");
        }

        @Test
        @DisplayName("length rejects value exceeding maximum")
        void lengthRejectsExceedingMax() {
            assertThatThrownBy(() -> CaptchaConfig.builder().length(21).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("length");
        }

        @Test
        @DisplayName("length accepts boundary value 20")
        void lengthAcceptsMaxBoundary() {
            CaptchaConfig config = CaptchaConfig.builder().length(20).build();
            assertThat(config.getLength()).isEqualTo(20);
        }

        // ---- gifFrameCount bounds ----

        @Test
        @DisplayName("gifFrameCount rejects zero")
        void gifFrameCountRejectsZero() {
            assertThatThrownBy(() -> CaptchaConfig.builder().gifFrameCount(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gifFrameCount");
        }

        @Test
        @DisplayName("gifFrameCount rejects value exceeding maximum")
        void gifFrameCountRejectsExceedingMax() {
            assertThatThrownBy(() -> CaptchaConfig.builder().gifFrameCount(101).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gifFrameCount");
        }

        @Test
        @DisplayName("gifFrameCount accepts boundary value 100")
        void gifFrameCountAcceptsMaxBoundary() {
            CaptchaConfig config = CaptchaConfig.builder().gifFrameCount(100).build();
            assertThat(config.getGifFrameCount()).isEqualTo(100);
        }

        // ---- noiseLines bounds ----

        @Test
        @DisplayName("noiseLines rejects negative value")
        void noiseLinesRejectsNegative() {
            assertThatThrownBy(() -> CaptchaConfig.builder().noiseLines(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("noiseLines");
        }

        @Test
        @DisplayName("noiseLines rejects value exceeding maximum")
        void noiseLinesRejectsExceedingMax() {
            assertThatThrownBy(() -> CaptchaConfig.builder().noiseLines(101).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("noiseLines");
        }

        @Test
        @DisplayName("noiseLines accepts boundary value 100")
        void noiseLinesAcceptsMaxBoundary() {
            CaptchaConfig config = CaptchaConfig.builder().noiseLines(100).build();
            assertThat(config.getNoiseLines()).isEqualTo(100);
        }

        // ---- noiseDots bounds ----

        @Test
        @DisplayName("noiseDots rejects negative value")
        void noiseDotsRejectsNegative() {
            assertThatThrownBy(() -> CaptchaConfig.builder().noiseDots(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("noiseDots");
        }

        @Test
        @DisplayName("noiseDots rejects value exceeding maximum")
        void noiseDotsRejectsExceedingMax() {
            assertThatThrownBy(() -> CaptchaConfig.builder().noiseDots(1001).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("noiseDots");
        }

        @Test
        @DisplayName("noiseDots accepts boundary value 1000")
        void noiseDotsAcceptsMaxBoundary() {
            CaptchaConfig config = CaptchaConfig.builder().noiseDots(1000).build();
            assertThat(config.getNoiseDots()).isEqualTo(1000);
        }

        // ---- fontSize bounds ----

        @Test
        @DisplayName("fontSize rejects zero")
        void fontSizeRejectsZero() {
            assertThatThrownBy(() -> CaptchaConfig.builder().fontSize(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fontSize");
        }

        @Test
        @DisplayName("fontSize rejects value exceeding maximum")
        void fontSizeRejectsExceedingMax() {
            assertThatThrownBy(() -> CaptchaConfig.builder().fontSize(201).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fontSize");
        }

        @Test
        @DisplayName("fontSize accepts boundary value 200")
        void fontSizeAcceptsMaxBoundary() {
            CaptchaConfig config = CaptchaConfig.builder().fontSize(200).build();
            assertThat(config.getFontSize()).isEqualTo(200.0f);
        }

        // ---- gifDelay bounds ----

        @Test
        @DisplayName("gifDelay rejects zero")
        void gifDelayRejectsZero() {
            assertThatThrownBy(() -> CaptchaConfig.builder().gifDelay(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gifDelay");
        }

        @Test
        @DisplayName("gifDelay rejects value exceeding maximum")
        void gifDelayRejectsExceedingMax() {
            assertThatThrownBy(() -> CaptchaConfig.builder().gifDelay(10001).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gifDelay");
        }

        @Test
        @DisplayName("gifDelay accepts boundary value 10000")
        void gifDelayAcceptsMaxBoundary() {
            CaptchaConfig config = CaptchaConfig.builder().gifDelay(10000).build();
            assertThat(config.getGifDelay()).isEqualTo(10000);
        }

        // ---- null checks ----

        @Test
        @DisplayName("type rejects null")
        void typeRejectsNull() {
            assertThatThrownBy(() -> CaptchaConfig.builder().type(null).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("type");
        }

        @Test
        @DisplayName("expireTime rejects null")
        void expireTimeRejectsNull() {
            assertThatThrownBy(() -> CaptchaConfig.builder().expireTime(null).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("expireTime");
        }

        @Test
        @DisplayName("fontName rejects null")
        void fontNameRejectsNull() {
            assertThatThrownBy(() -> CaptchaConfig.builder().fontName(null).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("fontName");
        }

        // ---- DoS prevention: extreme values ----

        @Test
        @DisplayName("rejects extreme width that would cause OOM")
        void rejectsExtremeWidth() {
            assertThatThrownBy(() -> CaptchaConfig.builder().width(100000).build())
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects extreme height that would cause OOM")
        void rejectsExtremeHeight() {
            assertThatThrownBy(() -> CaptchaConfig.builder().height(100000).build())
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("builder returns non-null builder")
        void builderReturnsNonNullBuilder() {
            CaptchaConfig.Builder builder = CaptchaConfig.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("defaults returns non-null config")
        void defaultsReturnsNonNullConfig() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("defaults returns cached singleton instance")
        void defaultsReturnsCachedSingletonInstance() {
            CaptchaConfig config1 = CaptchaConfig.defaults();
            CaptchaConfig config2 = CaptchaConfig.defaults();

            assertThat(config1).isSameAs(config2);
        }

        @Test
        @DisplayName("builder().build() produces same defaults as defaults()")
        void builderBuildProducesSameDefaultsAsDefaults() {
            CaptchaConfig fromDefaults = CaptchaConfig.defaults();
            CaptchaConfig fromBuilder = CaptchaConfig.builder().build();

            assertThat(fromBuilder.getWidth()).isEqualTo(fromDefaults.getWidth());
            assertThat(fromBuilder.getHeight()).isEqualTo(fromDefaults.getHeight());
            assertThat(fromBuilder.getLength()).isEqualTo(fromDefaults.getLength());
            assertThat(fromBuilder.getType()).isEqualTo(fromDefaults.getType());
            assertThat(fromBuilder.getExpireTime()).isEqualTo(fromDefaults.getExpireTime());
            assertThat(fromBuilder.getNoiseLines()).isEqualTo(fromDefaults.getNoiseLines());
            assertThat(fromBuilder.getNoiseDots()).isEqualTo(fromDefaults.getNoiseDots());
            assertThat(fromBuilder.getFontSize()).isEqualTo(fromDefaults.getFontSize());
            assertThat(fromBuilder.getFontName()).isEqualTo(fromDefaults.getFontName());
            assertThat(fromBuilder.getBackgroundColor()).isEqualTo(fromDefaults.getBackgroundColor());
            assertThat(fromBuilder.isCaseSensitive()).isEqualTo(fromDefaults.isCaseSensitive());
            assertThat(fromBuilder.getGifFrameCount()).isEqualTo(fromDefaults.getGifFrameCount());
            assertThat(fromBuilder.getGifDelay()).isEqualTo(fromDefaults.getGifDelay());
        }
    }

    // ==================== Sprint 1 New Field Tests ====================

    /**
     * Tests for Sprint 1 new field default values.
     * Sprint 1 新字段默认值测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("Sprint 1 New Field Default Values Tests")
    class NewFieldDefaultValuesTests {

        @Test
        @DisplayName("default customFontPaths is empty list")
        void should_returnEmptyList_when_defaultCustomFontPaths() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getCustomFontPaths()).isEmpty();
        }

        @Test
        @DisplayName("default randomFontPerChar is false")
        void should_returnFalse_when_defaultRandomFontPerChar() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.isRandomFontPerChar()).isFalse();
        }

        @Test
        @DisplayName("default charOverlapRatio is 0.0")
        void should_returnZero_when_defaultCharOverlapRatio() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getCharOverlapRatio()).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("default sineWarpEnabled is false")
        void should_returnFalse_when_defaultSineWarpEnabled() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.isSineWarpEnabled()).isFalse();
        }

        @Test
        @DisplayName("default outlineShadowEnabled is false")
        void should_returnFalse_when_defaultOutlineShadowEnabled() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.isOutlineShadowEnabled()).isFalse();
        }

        @Test
        @DisplayName("default bezierNoiseEnabled is false")
        void should_returnFalse_when_defaultBezierNoiseEnabled() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.isBezierNoiseEnabled()).isFalse();
        }

        @Test
        @DisplayName("default powDifficulty is 20")
        void should_return20_when_defaultPowDifficulty() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getPowDifficulty()).isEqualTo(20);
        }

        @Test
        @DisplayName("default audioSpeedVariation is 0.2")
        void should_return0point2_when_defaultAudioSpeedVariation() {
            CaptchaConfig config = CaptchaConfig.defaults();

            assertThat(config.getAudioSpeedVariation()).isEqualTo(0.2f);
        }
    }

    /**
     * Tests for Sprint 1 new field Builder set/get.
     * Sprint 1 新字段 Builder 设置/获取测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("Sprint 1 New Field Builder Tests")
    class NewFieldBuilderTests {

        @Test
        @DisplayName("should set and get randomFontPerChar")
        void should_setRandomFontPerChar_when_builderUsed() {
            CaptchaConfig config = CaptchaConfig.builder()
                .randomFontPerChar(true)
                .build();

            assertThat(config.isRandomFontPerChar()).isTrue();
        }

        @Test
        @DisplayName("should set and get sineWarpEnabled")
        void should_setSineWarpEnabled_when_builderUsed() {
            CaptchaConfig config = CaptchaConfig.builder()
                .sineWarpEnabled(true)
                .build();

            assertThat(config.isSineWarpEnabled()).isTrue();
        }

        @Test
        @DisplayName("should set and get outlineShadowEnabled")
        void should_setOutlineShadowEnabled_when_builderUsed() {
            CaptchaConfig config = CaptchaConfig.builder()
                .outlineShadowEnabled(true)
                .build();

            assertThat(config.isOutlineShadowEnabled()).isTrue();
        }

        @Test
        @DisplayName("should set and get bezierNoiseEnabled")
        void should_setBezierNoiseEnabled_when_builderUsed() {
            CaptchaConfig config = CaptchaConfig.builder()
                .bezierNoiseEnabled(true)
                .build();

            assertThat(config.isBezierNoiseEnabled()).isTrue();
        }
    }

    /**
     * Tests for charOverlapRatio boundary validation.
     * charOverlapRatio 边界校验测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("CharOverlapRatio Boundary Tests")
    class CharOverlapRatioBoundaryTests {

        @Test
        @DisplayName("should accept charOverlapRatio = 0.0")
        void should_accept_when_ratioIsZero() {
            CaptchaConfig config = CaptchaConfig.builder()
                .charOverlapRatio(0.0f)
                .build();

            assertThat(config.getCharOverlapRatio()).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("should accept charOverlapRatio = 0.5")
        void should_accept_when_ratioIsHalf() {
            CaptchaConfig config = CaptchaConfig.builder()
                .charOverlapRatio(0.5f)
                .build();

            assertThat(config.getCharOverlapRatio()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("should reject negative charOverlapRatio")
        void should_reject_when_ratioIsNegative() {
            assertThatThrownBy(() -> CaptchaConfig.builder()
                .charOverlapRatio(-0.1f)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("charOverlapRatio");
        }

        @Test
        @DisplayName("should reject charOverlapRatio > 0.5")
        void should_reject_when_ratioExceedsHalf() {
            assertThatThrownBy(() -> CaptchaConfig.builder()
                .charOverlapRatio(0.6f)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("charOverlapRatio");
        }

        @Test
        @DisplayName("should accept charOverlapRatio = 0.3 (middle value)")
        void should_accept_when_ratioIsMiddleValue() {
            CaptchaConfig config = CaptchaConfig.builder()
                .charOverlapRatio(0.3f)
                .build();

            assertThat(config.getCharOverlapRatio()).isEqualTo(0.3f);
        }
    }

    /**
     * Tests for powDifficulty boundary validation.
     * powDifficulty 边界校验测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("PowDifficulty Boundary Tests")
    class PowDifficultyBoundaryTests {

        @Test
        @DisplayName("should accept powDifficulty = 10 (minimum)")
        void should_accept_when_difficultyIs10() {
            CaptchaConfig config = CaptchaConfig.builder()
                .powDifficulty(10)
                .build();

            assertThat(config.getPowDifficulty()).isEqualTo(10);
        }

        @Test
        @DisplayName("should reject powDifficulty = 1 (below minimum)")
        void should_reject_when_difficultyIs1() {
            assertThatThrownBy(() -> CaptchaConfig.builder()
                .powDifficulty(1)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("powDifficulty");
        }

        @Test
        @DisplayName("should reject powDifficulty = 9 (below minimum)")
        void should_reject_when_difficultyIs9() {
            assertThatThrownBy(() -> CaptchaConfig.builder()
                .powDifficulty(9)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("powDifficulty");
        }

        @Test
        @DisplayName("should accept powDifficulty = 32")
        void should_accept_when_difficultyIs32() {
            CaptchaConfig config = CaptchaConfig.builder()
                .powDifficulty(32)
                .build();

            assertThat(config.getPowDifficulty()).isEqualTo(32);
        }

        @Test
        @DisplayName("should reject powDifficulty = 0")
        void should_reject_when_difficultyIsZero() {
            assertThatThrownBy(() -> CaptchaConfig.builder()
                .powDifficulty(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("powDifficulty");
        }

        @Test
        @DisplayName("should reject powDifficulty = 33")
        void should_reject_when_difficultyIs33() {
            assertThatThrownBy(() -> CaptchaConfig.builder()
                .powDifficulty(33)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("powDifficulty");
        }
    }

    /**
     * Tests for audioSpeedVariation boundary validation.
     * audioSpeedVariation 边界校验测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("AudioSpeedVariation Boundary Tests")
    class AudioSpeedVariationBoundaryTests {

        @Test
        @DisplayName("should accept audioSpeedVariation = 0.0")
        void should_accept_when_variationIsZero() {
            CaptchaConfig config = CaptchaConfig.builder()
                .audioSpeedVariation(0.0f)
                .build();

            assertThat(config.getAudioSpeedVariation()).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("should accept audioSpeedVariation = 0.5")
        void should_accept_when_variationIsHalf() {
            CaptchaConfig config = CaptchaConfig.builder()
                .audioSpeedVariation(0.5f)
                .build();

            assertThat(config.getAudioSpeedVariation()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("should reject negative audioSpeedVariation")
        void should_reject_when_variationIsNegative() {
            assertThatThrownBy(() -> CaptchaConfig.builder()
                .audioSpeedVariation(-0.1f)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("audioSpeedVariation");
        }

        @Test
        @DisplayName("should reject audioSpeedVariation > 0.5")
        void should_reject_when_variationExceedsHalf() {
            assertThatThrownBy(() -> CaptchaConfig.builder()
                .audioSpeedVariation(0.6f)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("audioSpeedVariation");
        }
    }

    /**
     * Tests for customFontPaths limits and operations.
     * customFontPaths 限制和操作测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("CustomFontPaths Tests")
    class CustomFontPathsTests {

        @Test
        @DisplayName("should accept up to 20 custom font paths")
        void should_accept_when_exactly20Paths() {
            List<String> paths = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                paths.add("/font/custom" + i + ".ttf");
            }
            CaptchaConfig config = CaptchaConfig.builder()
                .customFontPaths(paths)
                .build();

            assertThat(config.getCustomFontPaths()).hasSize(20);
        }

        @Test
        @DisplayName("should reject more than 20 custom font paths")
        void should_reject_when_moreThan20Paths() {
            List<String> paths = new ArrayList<>();
            for (int i = 0; i < 21; i++) {
                paths.add("/font/custom" + i + ".ttf");
            }
            assertThatThrownBy(() -> CaptchaConfig.builder()
                .customFontPaths(paths)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customFontPaths");
        }

        @Test
        @DisplayName("should append custom font path with customFontPath()")
        void should_appendPath_when_customFontPathCalled() {
            CaptchaConfig config = CaptchaConfig.builder()
                .customFontPath("/font/a.ttf")
                .customFontPath("/font/b.ttf")
                .build();

            assertThat(config.getCustomFontPaths())
                .containsExactly("/font/a.ttf", "/font/b.ttf");
        }

        @Test
        @DisplayName("customFontPaths should be defensively copied")
        void should_notBeAffected_when_inputListModified() {
            List<String> paths = new ArrayList<>(List.of("/font/a.ttf"));
            CaptchaConfig config = CaptchaConfig.builder()
                .customFontPaths(paths)
                .build();

            paths.add("/font/b.ttf");

            assertThat(config.getCustomFontPaths()).hasSize(1);
        }

        @Test
        @DisplayName("getCustomFontPaths should return unmodifiable list")
        void should_returnUnmodifiable_when_getCustomFontPaths() {
            CaptchaConfig config = CaptchaConfig.builder()
                .customFontPath("/font/a.ttf")
                .build();

            assertThatThrownBy(() -> config.getCustomFontPaths().add("/font/b.ttf"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    /**
     * Tests for toBuilder() copying new Sprint 1 fields.
     * toBuilder() 拷贝 Sprint 1 新字段测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("ToBuilder Sprint 1 New Fields Tests")
    class ToBuilderNewFieldsTests {

        @Test
        @DisplayName("toBuilder preserves all Sprint 1 new fields")
        void should_preserveAllNewFields_when_toBuilderUsed() {
            CaptchaConfig original = CaptchaConfig.builder()
                .randomFontPerChar(true)
                .charOverlapRatio(0.2f)
                .sineWarpEnabled(true)
                .outlineShadowEnabled(true)
                .bezierNoiseEnabled(true)
                .powDifficulty(25)
                .audioSpeedVariation(0.4f)
                .customFontPath("/font/test.ttf")
                .build();

            CaptchaConfig copy = original.toBuilder().build();

            assertThat(copy.isRandomFontPerChar()).isTrue();
            assertThat(copy.getCharOverlapRatio()).isEqualTo(0.2f);
            assertThat(copy.isSineWarpEnabled()).isTrue();
            assertThat(copy.isOutlineShadowEnabled()).isTrue();
            assertThat(copy.isBezierNoiseEnabled()).isTrue();
            assertThat(copy.getPowDifficulty()).isEqualTo(25);
            assertThat(copy.getAudioSpeedVariation()).isEqualTo(0.4f);
            assertThat(copy.getCustomFontPaths()).containsExactly("/font/test.ttf");
        }
    }
}
