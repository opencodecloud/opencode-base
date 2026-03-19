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

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for CaptchaType enum
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("CaptchaType Tests")
class CaptchaTypeTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("values returns exactly 10 enum constants")
        void valuesReturnsExactlyTenConstants() {
            assertThat(CaptchaType.values()).hasSize(10);
        }

        @Test
        @DisplayName("NUMERIC constant exists")
        void numericConstantExists() {
            assertThat(CaptchaType.NUMERIC).isNotNull();
        }

        @Test
        @DisplayName("ALPHA constant exists")
        void alphaConstantExists() {
            assertThat(CaptchaType.ALPHA).isNotNull();
        }

        @Test
        @DisplayName("ALPHANUMERIC constant exists")
        void alphanumericConstantExists() {
            assertThat(CaptchaType.ALPHANUMERIC).isNotNull();
        }

        @Test
        @DisplayName("ARITHMETIC constant exists")
        void arithmeticConstantExists() {
            assertThat(CaptchaType.ARITHMETIC).isNotNull();
        }

        @Test
        @DisplayName("CHINESE constant exists")
        void chineseConstantExists() {
            assertThat(CaptchaType.CHINESE).isNotNull();
        }

        @Test
        @DisplayName("GIF constant exists")
        void gifConstantExists() {
            assertThat(CaptchaType.GIF).isNotNull();
        }

        @Test
        @DisplayName("SLIDER constant exists")
        void sliderConstantExists() {
            assertThat(CaptchaType.SLIDER).isNotNull();
        }

        @Test
        @DisplayName("CLICK constant exists")
        void clickConstantExists() {
            assertThat(CaptchaType.CLICK).isNotNull();
        }

        @Test
        @DisplayName("ROTATE constant exists")
        void rotateConstantExists() {
            assertThat(CaptchaType.ROTATE).isNotNull();
        }

        @Test
        @DisplayName("IMAGE_SELECT constant exists")
        void imageSelectConstantExists() {
            assertThat(CaptchaType.IMAGE_SELECT).isNotNull();
        }

        @Test
        @DisplayName("values contains all expected constants")
        void valuesContainsAllExpectedConstants() {
            assertThat(CaptchaType.values()).containsExactly(
                CaptchaType.NUMERIC,
                CaptchaType.ALPHA,
                CaptchaType.ALPHANUMERIC,
                CaptchaType.ARITHMETIC,
                CaptchaType.CHINESE,
                CaptchaType.GIF,
                CaptchaType.SLIDER,
                CaptchaType.CLICK,
                CaptchaType.ROTATE,
                CaptchaType.IMAGE_SELECT
            );
        }
    }

    @Nested
    @DisplayName("valueOf Tests")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf returns NUMERIC for string NUMERIC")
        void valueOfReturnsNumeric() {
            assertThat(CaptchaType.valueOf("NUMERIC")).isEqualTo(CaptchaType.NUMERIC);
        }

        @Test
        @DisplayName("valueOf returns ALPHA for string ALPHA")
        void valueOfReturnsAlpha() {
            assertThat(CaptchaType.valueOf("ALPHA")).isEqualTo(CaptchaType.ALPHA);
        }

        @Test
        @DisplayName("valueOf returns ALPHANUMERIC for string ALPHANUMERIC")
        void valueOfReturnsAlphanumeric() {
            assertThat(CaptchaType.valueOf("ALPHANUMERIC")).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("valueOf returns ARITHMETIC for string ARITHMETIC")
        void valueOfReturnsArithmetic() {
            assertThat(CaptchaType.valueOf("ARITHMETIC")).isEqualTo(CaptchaType.ARITHMETIC);
        }

        @Test
        @DisplayName("valueOf returns CHINESE for string CHINESE")
        void valueOfReturnsChinese() {
            assertThat(CaptchaType.valueOf("CHINESE")).isEqualTo(CaptchaType.CHINESE);
        }

        @Test
        @DisplayName("valueOf returns GIF for string GIF")
        void valueOfReturnsGif() {
            assertThat(CaptchaType.valueOf("GIF")).isEqualTo(CaptchaType.GIF);
        }

        @Test
        @DisplayName("valueOf returns SLIDER for string SLIDER")
        void valueOfReturnsSlider() {
            assertThat(CaptchaType.valueOf("SLIDER")).isEqualTo(CaptchaType.SLIDER);
        }

        @Test
        @DisplayName("valueOf returns CLICK for string CLICK")
        void valueOfReturnsClick() {
            assertThat(CaptchaType.valueOf("CLICK")).isEqualTo(CaptchaType.CLICK);
        }

        @Test
        @DisplayName("valueOf returns ROTATE for string ROTATE")
        void valueOfReturnsRotate() {
            assertThat(CaptchaType.valueOf("ROTATE")).isEqualTo(CaptchaType.ROTATE);
        }

        @Test
        @DisplayName("valueOf returns IMAGE_SELECT for string IMAGE_SELECT")
        void valueOfReturnsImageSelect() {
            assertThat(CaptchaType.valueOf("IMAGE_SELECT")).isEqualTo(CaptchaType.IMAGE_SELECT);
        }

        @Test
        @DisplayName("valueOf throws IllegalArgumentException for invalid name")
        void valueOfThrowsForInvalidName() {
            assertThatThrownBy(() -> CaptchaType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("valueOf throws IllegalArgumentException for lowercase name")
        void valueOfThrowsForLowercaseName() {
            assertThatThrownBy(() -> CaptchaType.valueOf("numeric"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("valueOf throws NullPointerException for null name")
        void valueOfThrowsForNullName() {
            assertThatThrownBy(() -> CaptchaType.valueOf(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("isInteractive Tests")
    class IsInteractiveTests {

        @Test
        @DisplayName("SLIDER is interactive")
        void sliderIsInteractive() {
            assertThat(CaptchaType.SLIDER.isInteractive()).isTrue();
        }

        @Test
        @DisplayName("CLICK is interactive")
        void clickIsInteractive() {
            assertThat(CaptchaType.CLICK.isInteractive()).isTrue();
        }

        @Test
        @DisplayName("ROTATE is interactive")
        void rotateIsInteractive() {
            assertThat(CaptchaType.ROTATE.isInteractive()).isTrue();
        }

        @Test
        @DisplayName("IMAGE_SELECT is interactive")
        void imageSelectIsInteractive() {
            assertThat(CaptchaType.IMAGE_SELECT.isInteractive()).isTrue();
        }

        @Test
        @DisplayName("NUMERIC is not interactive")
        void numericIsNotInteractive() {
            assertThat(CaptchaType.NUMERIC.isInteractive()).isFalse();
        }

        @Test
        @DisplayName("ALPHA is not interactive")
        void alphaIsNotInteractive() {
            assertThat(CaptchaType.ALPHA.isInteractive()).isFalse();
        }

        @Test
        @DisplayName("ALPHANUMERIC is not interactive")
        void alphanumericIsNotInteractive() {
            assertThat(CaptchaType.ALPHANUMERIC.isInteractive()).isFalse();
        }

        @Test
        @DisplayName("ARITHMETIC is not interactive")
        void arithmeticIsNotInteractive() {
            assertThat(CaptchaType.ARITHMETIC.isInteractive()).isFalse();
        }

        @Test
        @DisplayName("CHINESE is not interactive")
        void chineseIsNotInteractive() {
            assertThat(CaptchaType.CHINESE.isInteractive()).isFalse();
        }

        @Test
        @DisplayName("GIF is not interactive")
        void gifIsNotInteractive() {
            assertThat(CaptchaType.GIF.isInteractive()).isFalse();
        }

        @Test
        @DisplayName("exactly 4 types are interactive")
        void exactlyFourTypesAreInteractive() {
            long interactiveCount = java.util.Arrays.stream(CaptchaType.values())
                .filter(CaptchaType::isInteractive)
                .count();

            assertThat(interactiveCount).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("isTextBased Tests")
    class IsTextBasedTests {

        @Test
        @DisplayName("NUMERIC is text-based")
        void numericIsTextBased() {
            assertThat(CaptchaType.NUMERIC.isTextBased()).isTrue();
        }

        @Test
        @DisplayName("ALPHA is text-based")
        void alphaIsTextBased() {
            assertThat(CaptchaType.ALPHA.isTextBased()).isTrue();
        }

        @Test
        @DisplayName("ALPHANUMERIC is text-based")
        void alphanumericIsTextBased() {
            assertThat(CaptchaType.ALPHANUMERIC.isTextBased()).isTrue();
        }

        @Test
        @DisplayName("ARITHMETIC is text-based")
        void arithmeticIsTextBased() {
            assertThat(CaptchaType.ARITHMETIC.isTextBased()).isTrue();
        }

        @Test
        @DisplayName("CHINESE is text-based")
        void chineseIsTextBased() {
            assertThat(CaptchaType.CHINESE.isTextBased()).isTrue();
        }

        @Test
        @DisplayName("GIF is not text-based")
        void gifIsNotTextBased() {
            assertThat(CaptchaType.GIF.isTextBased()).isFalse();
        }

        @Test
        @DisplayName("SLIDER is not text-based")
        void sliderIsNotTextBased() {
            assertThat(CaptchaType.SLIDER.isTextBased()).isFalse();
        }

        @Test
        @DisplayName("CLICK is not text-based")
        void clickIsNotTextBased() {
            assertThat(CaptchaType.CLICK.isTextBased()).isFalse();
        }

        @Test
        @DisplayName("ROTATE is not text-based")
        void rotateIsNotTextBased() {
            assertThat(CaptchaType.ROTATE.isTextBased()).isFalse();
        }

        @Test
        @DisplayName("IMAGE_SELECT is not text-based")
        void imageSelectIsNotTextBased() {
            assertThat(CaptchaType.IMAGE_SELECT.isTextBased()).isFalse();
        }

        @Test
        @DisplayName("exactly 5 types are text-based")
        void exactlyFiveTypesAreTextBased() {
            long textBasedCount = java.util.Arrays.stream(CaptchaType.values())
                .filter(CaptchaType::isTextBased)
                .count();

            assertThat(textBasedCount).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Mutual Exclusivity Tests")
    class MutualExclusivityTests {

        @Test
        @DisplayName("no type is both interactive and text-based")
        void noTypeIsBothInteractiveAndTextBased() {
            for (CaptchaType type : CaptchaType.values()) {
                if (type.isInteractive()) {
                    assertThat(type.isTextBased())
                        .as("Type %s should not be both interactive and text-based", type)
                        .isFalse();
                }
            }
        }

        @Test
        @DisplayName("GIF type is neither interactive nor text-based")
        void gifIsNeitherInteractiveNorTextBased() {
            assertThat(CaptchaType.GIF.isInteractive()).isFalse();
            assertThat(CaptchaType.GIF.isTextBased()).isFalse();
        }
    }

    @Nested
    @DisplayName("Enum Name Tests")
    class EnumNameTests {

        @Test
        @DisplayName("name returns correct string for each constant")
        void nameReturnsCorrectString() {
            assertThat(CaptchaType.NUMERIC.name()).isEqualTo("NUMERIC");
            assertThat(CaptchaType.ALPHA.name()).isEqualTo("ALPHA");
            assertThat(CaptchaType.ALPHANUMERIC.name()).isEqualTo("ALPHANUMERIC");
            assertThat(CaptchaType.ARITHMETIC.name()).isEqualTo("ARITHMETIC");
            assertThat(CaptchaType.CHINESE.name()).isEqualTo("CHINESE");
            assertThat(CaptchaType.GIF.name()).isEqualTo("GIF");
            assertThat(CaptchaType.SLIDER.name()).isEqualTo("SLIDER");
            assertThat(CaptchaType.CLICK.name()).isEqualTo("CLICK");
            assertThat(CaptchaType.ROTATE.name()).isEqualTo("ROTATE");
            assertThat(CaptchaType.IMAGE_SELECT.name()).isEqualTo("IMAGE_SELECT");
        }

        @Test
        @DisplayName("ordinal values are sequential starting from 0")
        void ordinalValuesAreSequential() {
            assertThat(CaptchaType.NUMERIC.ordinal()).isZero();
            assertThat(CaptchaType.ALPHA.ordinal()).isEqualTo(1);
            assertThat(CaptchaType.ALPHANUMERIC.ordinal()).isEqualTo(2);
            assertThat(CaptchaType.ARITHMETIC.ordinal()).isEqualTo(3);
            assertThat(CaptchaType.CHINESE.ordinal()).isEqualTo(4);
            assertThat(CaptchaType.GIF.ordinal()).isEqualTo(5);
            assertThat(CaptchaType.SLIDER.ordinal()).isEqualTo(6);
            assertThat(CaptchaType.CLICK.ordinal()).isEqualTo(7);
            assertThat(CaptchaType.ROTATE.ordinal()).isEqualTo(8);
            assertThat(CaptchaType.IMAGE_SELECT.ordinal()).isEqualTo(9);
        }
    }
}
