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

package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for CaptchaStrength enum
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("CaptchaStrength Tests")
class CaptchaStrengthTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("values returns exactly 4 enum constants")
        void valuesReturnsFourConstants() {
            assertThat(CaptchaStrength.values()).hasSize(4);
        }

        @Test
        @DisplayName("values contains EASY, MEDIUM, HARD, EXTREME in order")
        void valuesContainsAllConstantsInOrder() {
            assertThat(CaptchaStrength.values()).containsExactly(
                CaptchaStrength.EASY,
                CaptchaStrength.MEDIUM,
                CaptchaStrength.HARD,
                CaptchaStrength.EXTREME
            );
        }

        @Test
        @DisplayName("EASY constant exists")
        void easyConstantExists() {
            assertThat(CaptchaStrength.EASY).isNotNull();
        }

        @Test
        @DisplayName("MEDIUM constant exists")
        void mediumConstantExists() {
            assertThat(CaptchaStrength.MEDIUM).isNotNull();
        }

        @Test
        @DisplayName("HARD constant exists")
        void hardConstantExists() {
            assertThat(CaptchaStrength.HARD).isNotNull();
        }

        @Test
        @DisplayName("EXTREME constant exists")
        void extremeConstantExists() {
            assertThat(CaptchaStrength.EXTREME).isNotNull();
        }

        @Test
        @DisplayName("valueOf returns correct constants")
        void valueOfReturnsCorrectConstants() {
            assertThat(CaptchaStrength.valueOf("EASY")).isEqualTo(CaptchaStrength.EASY);
            assertThat(CaptchaStrength.valueOf("MEDIUM")).isEqualTo(CaptchaStrength.MEDIUM);
            assertThat(CaptchaStrength.valueOf("HARD")).isEqualTo(CaptchaStrength.HARD);
            assertThat(CaptchaStrength.valueOf("EXTREME")).isEqualTo(CaptchaStrength.EXTREME);
        }

        @Test
        @DisplayName("valueOf throws for invalid name")
        void valueOfThrowsForInvalidName() {
            assertThatThrownBy(() -> CaptchaStrength.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("ordinal values are sequential starting from 0")
        void ordinalValuesAreSequential() {
            assertThat(CaptchaStrength.EASY.ordinal()).isZero();
            assertThat(CaptchaStrength.MEDIUM.ordinal()).isEqualTo(1);
            assertThat(CaptchaStrength.HARD.ordinal()).isEqualTo(2);
            assertThat(CaptchaStrength.EXTREME.ordinal()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("EASY Strength Tests")
    class EasyStrengthTests {

        @Test
        @DisplayName("EASY has 3 noise lines")
        void easyHasThreeNoiseLines() {
            assertThat(CaptchaStrength.EASY.getNoiseLines()).isEqualTo(3);
        }

        @Test
        @DisplayName("EASY has 3 noise dots")
        void easyHasThreeNoiseDots() {
            assertThat(CaptchaStrength.EASY.getNoiseDots()).isEqualTo(3);
        }

        @Test
        @DisplayName("EASY has font size 20")
        void easyHasFontSizeTwenty() {
            assertThat(CaptchaStrength.EASY.getFontSize()).isEqualTo(20.0f);
        }
    }

    @Nested
    @DisplayName("MEDIUM Strength Tests")
    class MediumStrengthTests {

        @Test
        @DisplayName("MEDIUM has 5 noise lines")
        void mediumHasFiveNoiseLines() {
            assertThat(CaptchaStrength.MEDIUM.getNoiseLines()).isEqualTo(5);
        }

        @Test
        @DisplayName("MEDIUM has 50 noise dots")
        void mediumHasFiftyNoiseDots() {
            assertThat(CaptchaStrength.MEDIUM.getNoiseDots()).isEqualTo(50);
        }

        @Test
        @DisplayName("MEDIUM has font size 32")
        void mediumHasFontSizeThirtyTwo() {
            assertThat(CaptchaStrength.MEDIUM.getFontSize()).isEqualTo(32.0f);
        }
    }

    @Nested
    @DisplayName("HARD Strength Tests")
    class HardStrengthTests {

        @Test
        @DisplayName("HARD has 8 noise lines")
        void hardHasEightNoiseLines() {
            assertThat(CaptchaStrength.HARD.getNoiseLines()).isEqualTo(8);
        }

        @Test
        @DisplayName("HARD has 100 noise dots")
        void hardHasOneHundredNoiseDots() {
            assertThat(CaptchaStrength.HARD.getNoiseDots()).isEqualTo(100);
        }

        @Test
        @DisplayName("HARD has font size 28")
        void hardHasFontSizeTwentyEight() {
            assertThat(CaptchaStrength.HARD.getFontSize()).isEqualTo(28.0f);
        }
    }

    @Nested
    @DisplayName("EXTREME Strength Tests")
    class ExtremeStrengthTests {

        @Test
        @DisplayName("EXTREME has 12 noise lines")
        void extremeHasTwelveNoiseLines() {
            assertThat(CaptchaStrength.EXTREME.getNoiseLines()).isEqualTo(12);
        }

        @Test
        @DisplayName("EXTREME has 200 noise dots")
        void extremeHasTwoHundredNoiseDots() {
            assertThat(CaptchaStrength.EXTREME.getNoiseDots()).isEqualTo(200);
        }

        @Test
        @DisplayName("EXTREME has font size 26")
        void extremeHasFontSizeTwentySix() {
            assertThat(CaptchaStrength.EXTREME.getFontSize()).isEqualTo(26.0f);
        }
    }

    @Nested
    @DisplayName("Progressive Difficulty Tests")
    class ProgressiveDifficultyTests {

        @Test
        @DisplayName("noise lines increase with difficulty")
        void noiseLinesIncreaseWithDifficulty() {
            assertThat(CaptchaStrength.EASY.getNoiseLines())
                .isLessThan(CaptchaStrength.MEDIUM.getNoiseLines());
            assertThat(CaptchaStrength.MEDIUM.getNoiseLines())
                .isLessThan(CaptchaStrength.HARD.getNoiseLines());
            assertThat(CaptchaStrength.HARD.getNoiseLines())
                .isLessThan(CaptchaStrength.EXTREME.getNoiseLines());
        }

        @Test
        @DisplayName("noise dots increase with difficulty")
        void noiseDotsIncreaseWithDifficulty() {
            assertThat(CaptchaStrength.EASY.getNoiseDots())
                .isLessThan(CaptchaStrength.MEDIUM.getNoiseDots());
            assertThat(CaptchaStrength.MEDIUM.getNoiseDots())
                .isLessThan(CaptchaStrength.HARD.getNoiseDots());
            assertThat(CaptchaStrength.HARD.getNoiseDots())
                .isLessThan(CaptchaStrength.EXTREME.getNoiseDots());
        }

        @Test
        @DisplayName("font size decreases from MEDIUM to EXTREME (harder to read)")
        void fontSizeDecreasesFromMediumToExtreme() {
            assertThat(CaptchaStrength.MEDIUM.getFontSize())
                .isGreaterThan(CaptchaStrength.HARD.getFontSize());
            assertThat(CaptchaStrength.HARD.getFontSize())
                .isGreaterThan(CaptchaStrength.EXTREME.getFontSize());
        }
    }

    @Nested
    @DisplayName("applyTo Tests")
    class ApplyToTests {

        @Test
        @DisplayName("applyTo sets noise lines on builder")
        void applyToSetsNoiseLines() {
            CaptchaConfig.Builder builder = CaptchaConfig.builder();

            CaptchaStrength.HARD.applyTo(builder);
            CaptchaConfig config = builder.build();

            assertThat(config.getNoiseLines()).isEqualTo(8);
        }

        @Test
        @DisplayName("applyTo sets noise dots on builder")
        void applyToSetsNoiseDots() {
            CaptchaConfig.Builder builder = CaptchaConfig.builder();

            CaptchaStrength.HARD.applyTo(builder);
            CaptchaConfig config = builder.build();

            assertThat(config.getNoiseDots()).isEqualTo(100);
        }

        @Test
        @DisplayName("applyTo sets font size on builder")
        void applyToSetsFontSize() {
            CaptchaConfig.Builder builder = CaptchaConfig.builder();

            CaptchaStrength.HARD.applyTo(builder);
            CaptchaConfig config = builder.build();

            assertThat(config.getFontSize()).isEqualTo(28.0f);
        }

        @Test
        @DisplayName("applyTo returns the builder for chaining")
        void applyToReturnsBuilder() {
            CaptchaConfig.Builder builder = CaptchaConfig.builder();

            CaptchaConfig.Builder returned = CaptchaStrength.EASY.applyTo(builder);

            assertThat(returned).isSameAs(builder);
        }

        @Test
        @DisplayName("applyTo preserves other builder properties")
        void applyToPreservesOtherProperties() {
            CaptchaConfig.Builder builder = CaptchaConfig.builder()
                .width(200)
                .height(80)
                .length(6);

            CaptchaStrength.MEDIUM.applyTo(builder);
            CaptchaConfig config = builder.build();

            assertThat(config.getWidth()).isEqualTo(200);
            assertThat(config.getHeight()).isEqualTo(80);
            assertThat(config.getLength()).isEqualTo(6);
            assertThat(config.getNoiseLines()).isEqualTo(5);
            assertThat(config.getNoiseDots()).isEqualTo(50);
            assertThat(config.getFontSize()).isEqualTo(32.0f);
        }

        @Test
        @DisplayName("applyTo can be chained with other builder methods")
        void applyToCanBeChained() {
            CaptchaConfig config = CaptchaStrength.EXTREME
                .applyTo(CaptchaConfig.builder())
                .width(300)
                .height(100)
                .build();

            assertThat(config.getWidth()).isEqualTo(300);
            assertThat(config.getHeight()).isEqualTo(100);
            assertThat(config.getNoiseLines()).isEqualTo(12);
            assertThat(config.getNoiseDots()).isEqualTo(200);
            assertThat(config.getFontSize()).isEqualTo(26.0f);
        }

        @Test
        @DisplayName("applyTo with EASY sets correct values")
        void applyToWithEasySetsCorrectValues() {
            CaptchaConfig config = CaptchaStrength.EASY
                .applyTo(CaptchaConfig.builder())
                .build();

            assertThat(config.getNoiseLines()).isEqualTo(3);
            assertThat(config.getNoiseDots()).isEqualTo(3);
            assertThat(config.getFontSize()).isEqualTo(20.0f);
        }

        @Test
        @DisplayName("applyTo with EXTREME sets correct values")
        void applyToWithExtremeSetsCorrectValues() {
            CaptchaConfig config = CaptchaStrength.EXTREME
                .applyTo(CaptchaConfig.builder())
                .build();

            assertThat(config.getNoiseLines()).isEqualTo(12);
            assertThat(config.getNoiseDots()).isEqualTo(200);
            assertThat(config.getFontSize()).isEqualTo(26.0f);
        }
    }

    @Nested
    @DisplayName("toConfig Tests")
    class ToConfigTests {

        @Test
        @DisplayName("toConfig returns non-null config")
        void toConfigReturnsNonNullConfig() {
            CaptchaConfig config = CaptchaStrength.MEDIUM.toConfig();

            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("toConfig for EASY returns config with EASY noise lines")
        void toConfigEasyReturnsCorrectNoiseLines() {
            CaptchaConfig config = CaptchaStrength.EASY.toConfig();

            assertThat(config.getNoiseLines()).isEqualTo(3);
        }

        @Test
        @DisplayName("toConfig for EASY returns config with EASY noise dots")
        void toConfigEasyReturnsCorrectNoiseDots() {
            CaptchaConfig config = CaptchaStrength.EASY.toConfig();

            assertThat(config.getNoiseDots()).isEqualTo(3);
        }

        @Test
        @DisplayName("toConfig for EASY returns config with EASY font size")
        void toConfigEasyReturnsCorrectFontSize() {
            CaptchaConfig config = CaptchaStrength.EASY.toConfig();

            assertThat(config.getFontSize()).isEqualTo(20.0f);
        }

        @Test
        @DisplayName("toConfig for MEDIUM returns correct strength values")
        void toConfigMediumReturnsCorrectValues() {
            CaptchaConfig config = CaptchaStrength.MEDIUM.toConfig();

            assertThat(config.getNoiseLines()).isEqualTo(5);
            assertThat(config.getNoiseDots()).isEqualTo(50);
            assertThat(config.getFontSize()).isEqualTo(32.0f);
        }

        @Test
        @DisplayName("toConfig for HARD returns correct strength values")
        void toConfigHardReturnsCorrectValues() {
            CaptchaConfig config = CaptchaStrength.HARD.toConfig();

            assertThat(config.getNoiseLines()).isEqualTo(8);
            assertThat(config.getNoiseDots()).isEqualTo(100);
            assertThat(config.getFontSize()).isEqualTo(28.0f);
        }

        @Test
        @DisplayName("toConfig for EXTREME returns correct strength values")
        void toConfigExtremeReturnsCorrectValues() {
            CaptchaConfig config = CaptchaStrength.EXTREME.toConfig();

            assertThat(config.getNoiseLines()).isEqualTo(12);
            assertThat(config.getNoiseDots()).isEqualTo(200);
            assertThat(config.getFontSize()).isEqualTo(26.0f);
        }

        @Test
        @DisplayName("toConfig preserves default values for unset properties")
        void toConfigPreservesDefaultValues() {
            CaptchaConfig config = CaptchaStrength.MEDIUM.toConfig();
            CaptchaConfig defaults = CaptchaConfig.defaults();

            assertThat(config.getWidth()).isEqualTo(defaults.getWidth());
            assertThat(config.getHeight()).isEqualTo(defaults.getHeight());
            assertThat(config.getLength()).isEqualTo(defaults.getLength());
            assertThat(config.getType()).isEqualTo(defaults.getType());
            assertThat(config.getExpireTime()).isEqualTo(defaults.getExpireTime());
            assertThat(config.getFontName()).isEqualTo(defaults.getFontName());
            assertThat(config.getBackgroundColor()).isEqualTo(defaults.getBackgroundColor());
            assertThat(config.isCaseSensitive()).isEqualTo(defaults.isCaseSensitive());
            assertThat(config.getGifFrameCount()).isEqualTo(defaults.getGifFrameCount());
            assertThat(config.getGifDelay()).isEqualTo(defaults.getGifDelay());
        }

        @Test
        @DisplayName("toConfig returns new instance each time")
        void toConfigReturnsNewInstanceEachTime() {
            CaptchaConfig config1 = CaptchaStrength.MEDIUM.toConfig();
            CaptchaConfig config2 = CaptchaStrength.MEDIUM.toConfig();

            assertThat(config1).isNotSameAs(config2);
        }

        @Test
        @DisplayName("each strength produces config with its specific values")
        void eachStrengthProducesConfigWithSpecificValues() {
            for (CaptchaStrength strength : CaptchaStrength.values()) {
                CaptchaConfig config = strength.toConfig();

                assertThat(config.getNoiseLines()).isEqualTo(strength.getNoiseLines());
                assertThat(config.getNoiseDots()).isEqualTo(strength.getNoiseDots());
                assertThat(config.getFontSize()).isEqualTo(strength.getFontSize());
            }
        }
    }
}
