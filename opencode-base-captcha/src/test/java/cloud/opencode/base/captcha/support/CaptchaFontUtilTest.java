package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaConfig;
import org.junit.jupiter.api.*;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaFontUtil Test - Unit tests for font utilities
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaFontUtilTest {

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("should not be instantiable")
        void shouldNotBeInstantiable() throws NoSuchMethodException {
            Constructor<CaptchaFontUtil> constructor = CaptchaFontUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("GetFont with Config Tests")
    class GetFontWithConfigTests {

        @Test
        @DisplayName("should return font from config")
        void shouldReturnFontFromConfig() {
            CaptchaConfig config = CaptchaConfig.builder()
                .fontName("Arial")
                .fontSize(32f)
                .build();

            Font font = CaptchaFontUtil.getFont(config);

            assertThat(font).isNotNull();
            assertThat(font.getSize()).isEqualTo(32);
        }

        @Test
        @DisplayName("should return font with default config")
        void shouldReturnFontWithDefaultConfig() {
            CaptchaConfig config = CaptchaConfig.defaults();

            Font font = CaptchaFontUtil.getFont(config);

            assertThat(font).isNotNull();
        }
    }

    @Nested
    @DisplayName("GetFont with Name and Size Tests")
    class GetFontWithNameAndSizeTests {

        @Test
        @DisplayName("should return font with specified size")
        void shouldReturnFontWithSpecifiedSize() {
            Font font = CaptchaFontUtil.getFont("Arial", 24f);

            assertThat(font).isNotNull();
            assertThat(font.getSize()).isEqualTo(24);
        }

        @Test
        @DisplayName("should handle unknown font name gracefully")
        void shouldHandleUnknownFontNameGracefully() {
            Font font = CaptchaFontUtil.getFont("NonExistentFont12345", 20f);

            // Should still return a font (fallback)
            assertThat(font).isNotNull();
        }

        @Test
        @DisplayName("should use Dialog font as fallback")
        void shouldUseFallbackFont() {
            Font font = CaptchaFontUtil.getFont("VeryNonExistentFont999", 20f);

            assertThat(font).isNotNull();
            assertThat(font.getSize()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("GetRandomStyleFont Tests")
    class GetRandomStyleFontTests {

        @Test
        @DisplayName("should return a styled font")
        void shouldReturnStyledFont() {
            Font base = new Font("Arial", Font.PLAIN, 20);

            Font styled = CaptchaFontUtil.getRandomStyleFont(base);

            assertThat(styled).isNotNull();
            assertThat(styled.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("should produce valid font styles")
        void shouldProduceValidFontStyles() {
            Font base = new Font("Arial", Font.PLAIN, 20);

            // Call multiple times to cover different random styles
            for (int i = 0; i < 20; i++) {
                Font styled = CaptchaFontUtil.getRandomStyleFont(base);
                int style = styled.getStyle();
                // Style should be PLAIN(0), BOLD(1), ITALIC(2), or BOLD|ITALIC(3)
                assertThat(style).isBetween(0, 3);
            }
        }
    }

    @Nested
    @DisplayName("GetRotatedFont Tests")
    class GetRotatedFontTests {

        @Test
        @DisplayName("should return a rotated font")
        void shouldReturnRotatedFont() {
            Font base = new Font("Arial", Font.PLAIN, 20);

            Font rotated = CaptchaFontUtil.getRotatedFont(base, Math.PI / 6);

            assertThat(rotated).isNotNull();
        }

        @Test
        @DisplayName("should handle zero angle")
        void shouldHandleZeroAngle() {
            Font base = new Font("Arial", Font.PLAIN, 20);

            Font rotated = CaptchaFontUtil.getRotatedFont(base, 0);

            assertThat(rotated).isNotNull();
        }

        @Test
        @DisplayName("should handle negative angle")
        void shouldHandleNegativeAngle() {
            Font base = new Font("Arial", Font.PLAIN, 20);

            Font rotated = CaptchaFontUtil.getRotatedFont(base, -Math.PI / 4);

            assertThat(rotated).isNotNull();
        }
    }

    @Nested
    @DisplayName("GetChineseFont Tests")
    class GetChineseFontTests {

        @Test
        @DisplayName("should return a font")
        void shouldReturnAFont() {
            Font font = CaptchaFontUtil.getChineseFont(24f);

            assertThat(font).isNotNull();
        }

        @Test
        @DisplayName("should respect font size")
        void shouldRespectFontSize() {
            Font font = CaptchaFontUtil.getChineseFont(32f);

            assertThat(font.getSize()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("GetRandomColor with Config Tests")
    class GetRandomColorWithConfigTests {

        @Test
        @DisplayName("should return a color from config colors")
        void shouldReturnColorFromConfigColors() {
            Color[] expectedColors = new Color[] { Color.RED, Color.BLUE, Color.GREEN };
            CaptchaConfig config = CaptchaConfig.builder()
                .fontColors(expectedColors)
                .build();

            Color color = CaptchaFontUtil.getRandomColor(config);

            assertThat(color).isIn((Object[]) expectedColors);
        }
    }

    @Nested
    @DisplayName("RandomColor Tests")
    class RandomColorTests {

        @Test
        @DisplayName("should return non-null color")
        void shouldReturnNonNullColor() {
            Color color = CaptchaFontUtil.randomColor();

            assertThat(color).isNotNull();
        }

        @Test
        @DisplayName("should return color with valid RGB values")
        void shouldReturnColorWithValidRgbValues() {
            Color color = CaptchaFontUtil.randomColor();

            assertThat(color.getRed()).isBetween(0, 199);
            assertThat(color.getGreen()).isBetween(0, 199);
            assertThat(color.getBlue()).isBetween(0, 199);
        }
    }

    @Nested
    @DisplayName("RandomLightColor Tests")
    class RandomLightColorTests {

        @Test
        @DisplayName("should return non-null light color")
        void shouldReturnNonNullLightColor() {
            Color color = CaptchaFontUtil.randomLightColor();

            assertThat(color).isNotNull();
        }

        @Test
        @DisplayName("should return light color with high RGB values")
        void shouldReturnLightColorWithHighRgbValues() {
            Color color = CaptchaFontUtil.randomLightColor();

            assertThat(color.getRed()).isBetween(155, 254);
            assertThat(color.getGreen()).isBetween(155, 254);
            assertThat(color.getBlue()).isBetween(155, 254);
        }
    }

    @Nested
    @DisplayName("RandomDarkColor Tests")
    class RandomDarkColorTests {

        @Test
        @DisplayName("should return non-null dark color")
        void shouldReturnNonNullDarkColor() {
            Color color = CaptchaFontUtil.randomDarkColor();

            assertThat(color).isNotNull();
        }

        @Test
        @DisplayName("should return dark color with low RGB values")
        void shouldReturnDarkColorWithLowRgbValues() {
            Color color = CaptchaFontUtil.randomDarkColor();

            assertThat(color.getRed()).isBetween(0, 99);
            assertThat(color.getGreen()).isBetween(0, 99);
            assertThat(color.getBlue()).isBetween(0, 99);
        }
    }
}
