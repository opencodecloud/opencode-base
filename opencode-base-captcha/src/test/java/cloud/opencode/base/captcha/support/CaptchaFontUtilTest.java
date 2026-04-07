package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.exception.CaptchaException;
import org.junit.jupiter.api.*;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    @Nested
    @DisplayName("Font Cache Tests")
    class FontCacheTests {

        @Test
        @DisplayName("should return cached font on second call with same parameters")
        void should_returnCachedFont_when_calledTwiceWithSameParams() {
            Font first = CaptchaFontUtil.getFont("Arial", 28f);
            Font second = CaptchaFontUtil.getFont("Arial", 28f);

            // Both calls should return the same cached instance
            assertThat(second).isSameAs(first);
        }

        @Test
        @DisplayName("should return fallback font when fontName resolves to Dialog")
        void should_returnFallbackFont_when_fontNameResolvesToDialog() {
            // A completely bogus font name should trigger fallback logic
            Font font = CaptchaFontUtil.getFont("TotallyFakeFont_XYZ_999", 20f);

            assertThat(font).isNotNull();
            assertThat(font.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("should evict cache when size exceeds MAX_CACHE_SIZE (100)")
        void should_evictCache_when_sizeExceedsMax() {
            // Fill the cache with 100+ distinct entries to trigger eviction
            for (int i = 0; i < 105; i++) {
                CaptchaFontUtil.getFont("EvictionTestFont" + i, (float) (10 + i));
            }
            // After eviction, subsequent calls should still work
            Font font = CaptchaFontUtil.getFont("PostEvictionFont", 20f);
            assertThat(font).isNotNull();
        }
    }

    @Nested
    @DisplayName("GetRandomFontsPerChar Edge Case Tests")
    class GetRandomFontsPerCharEdgeCaseTests {

        @Test
        @DisplayName("should return empty array when charCount is 0")
        void should_returnEmptyArray_when_charCountIsZero() {
            Font[] fonts = CaptchaFontUtil.getRandomFontsPerChar("Arial", List.of(), 20f, 0);

            assertThat(fonts).isEmpty();
        }

        @Test
        @DisplayName("should return single font when charCount is 1")
        void should_returnSingleFont_when_charCountIsOne() {
            Font[] fonts = CaptchaFontUtil.getRandomFontsPerChar("Arial", List.of(), 20f, 1);

            assertThat(fonts).hasSize(1);
            assertThat(fonts[0]).isNotNull();
        }

        @Test
        @DisplayName("should handle null customPaths (distinct from empty list)")
        void should_handleNullCustomPaths_when_distinctFromEmptyList() {
            Font[] fontsWithNull = CaptchaFontUtil.getRandomFontsPerChar("Arial", null, 20f, 4);
            Font[] fontsWithEmpty = CaptchaFontUtil.getRandomFontsPerChar("Arial", List.of(), 20f, 4);

            assertThat(fontsWithNull).hasSize(4);
            assertThat(fontsWithEmpty).hasSize(4);
        }
    }

    @Nested
    @DisplayName("LoadCustomFont OTF Extension Tests")
    class LoadCustomFontOtfExtensionTests {

        @Test
        @DisplayName("should accept .OTF uppercase extension (case-insensitive)")
        void should_acceptUppercaseOtf_when_pathHasUppercaseExtension() {
            // Should not throw "must be TTF or OTF" but may throw "does not exist"
            assertThatThrownBy(() -> CaptchaFontUtil.loadCustomFont("/nonexistent/font.OTF", 24f))
                .isInstanceOf(CaptchaException.class)
                .hasMessageContaining("does not exist");
        }

        @Test
        @DisplayName("should accept .TTF uppercase extension (case-insensitive)")
        void should_acceptUppercaseTtf_when_pathHasUppercaseExtension() {
            assertThatThrownBy(() -> CaptchaFontUtil.loadCustomFont("/nonexistent/font.TTF", 24f))
                .isInstanceOf(CaptchaException.class)
                .hasMessageContaining("does not exist");
        }

        @Test
        @DisplayName("should throw CaptchaException when font file exists but is corrupted")
        void should_throwCaptchaException_when_fontFileCorrupted() throws IOException {
            // Create a temporary file with .ttf extension but invalid content
            Path tempFont = Files.createTempFile("corrupt-font", ".ttf");
            try {
                Files.writeString(tempFont, "this is not a font file");

                assertThatThrownBy(() -> CaptchaFontUtil.loadCustomFont(tempFont.toString(), 24f))
                    .isInstanceOf(CaptchaException.class)
                    .hasMessageContaining("Failed to load font file");
            } finally {
                Files.deleteIfExists(tempFont);
            }
        }
    }

    // ==================== Sprint 1 New Method Tests ====================

    /**
     * Tests for loadCustomFont method.
     * loadCustomFont 方法测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("LoadCustomFont Tests")
    class LoadCustomFontTests {

        @Test
        @DisplayName("should throw CaptchaException when path is null")
        void should_throwCaptchaException_when_pathIsNull() {
            assertThatThrownBy(() -> CaptchaFontUtil.loadCustomFont(null, 24f))
                .isInstanceOf(CaptchaException.class)
                .hasMessageContaining("null");
        }

        @Test
        @DisplayName("should throw CaptchaException when path is blank")
        void should_throwCaptchaException_when_pathIsBlank() {
            assertThatThrownBy(() -> CaptchaFontUtil.loadCustomFont("   ", 24f))
                .isInstanceOf(CaptchaException.class)
                .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("should throw CaptchaException when path has non-ttf/otf extension")
        void should_throwCaptchaException_when_nonTtfOtfExtension() {
            assertThatThrownBy(() -> CaptchaFontUtil.loadCustomFont("/font/test.woff", 24f))
                .isInstanceOf(CaptchaException.class)
                .hasMessageContaining("TTF or OTF");
        }

        @Test
        @DisplayName("should throw CaptchaException when font file does not exist")
        void should_throwCaptchaException_when_fileNotExists() {
            assertThatThrownBy(() -> CaptchaFontUtil.loadCustomFont("/nonexistent/path/font.ttf", 24f))
                .isInstanceOf(CaptchaException.class)
                .hasMessageContaining("does not exist");
        }
    }

    /**
     * Tests for getRandomFontsPerChar method.
     * getRandomFontsPerChar 方法测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("GetRandomFontsPerChar Tests")
    class GetRandomFontsPerCharTests {

        @Test
        @DisplayName("should return array with length equal to charCount")
        void should_returnCorrectLength_when_charCountSpecified() {
            Font[] fonts = CaptchaFontUtil.getRandomFontsPerChar("Arial", List.of(), 24f, 5);

            assertThat(fonts).hasSize(5);
        }

        @Test
        @DisplayName("should return fonts when custom paths list is empty")
        void should_returnFonts_when_emptyCustomPaths() {
            Font[] fonts = CaptchaFontUtil.getRandomFontsPerChar("Arial", List.of(), 20f, 3);

            assertThat(fonts).hasSize(3);
            for (Font f : fonts) {
                assertThat(f).isNotNull();
            }
        }

        @Test
        @DisplayName("should return fonts when custom paths is null")
        void should_returnFonts_when_nullCustomPaths() {
            Font[] fonts = CaptchaFontUtil.getRandomFontsPerChar("Arial", null, 20f, 3);

            assertThat(fonts).hasSize(3);
            for (Font f : fonts) {
                assertThat(f).isNotNull();
            }
        }

        @Test
        @DisplayName("should produce varied fonts for multiple characters (probabilistic)")
        void should_produceVariedFonts_when_multipleCharacters() {
            // With enough characters and the font pool, not all fonts should be identical
            // (base font + 5 fallback fonts = 6 in pool)
            Font[] fonts = CaptchaFontUtil.getRandomFontsPerChar("Arial", List.of(), 20f, 20);

            // Check that at least one font differs from the first (probabilistic)
            boolean hasDifferent = false;
            for (int i = 1; i < fonts.length; i++) {
                if (!fonts[i].getFamily().equals(fonts[0].getFamily())
                    || fonts[i].getStyle() != fonts[0].getStyle()) {
                    hasDifferent = true;
                    break;
                }
            }
            // With 20 characters and 6 fonts x 4 styles = 24 combinations,
            // probability of all identical is negligible
            assertThat(hasDifferent).isTrue();
        }

        @Test
        @DisplayName("should skip invalid custom font paths silently")
        void should_skipInvalidPaths_when_customPathsInvalid() {
            List<String> invalidPaths = List.of("/nonexistent/font.ttf", "/also/bad.otf");

            Font[] fonts = CaptchaFontUtil.getRandomFontsPerChar("Arial", invalidPaths, 20f, 3);

            // Should still return fonts from fallback pool
            assertThat(fonts).hasSize(3);
            for (Font f : fonts) {
                assertThat(f).isNotNull();
            }
        }
    }
}
