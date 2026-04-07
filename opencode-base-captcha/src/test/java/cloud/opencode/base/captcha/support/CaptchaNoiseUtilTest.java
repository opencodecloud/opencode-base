package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaConfig;
import org.junit.jupiter.api.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaNoiseUtil Test - Unit tests for noise generation utilities
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaNoiseUtilTest {

    private Graphics2D graphics;
    private CaptchaConfig config;
    private BufferedImage image;

    @BeforeEach
    void setUp() {
        image = new BufferedImage(160, 60, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        config = CaptchaConfig.builder()
            .width(160)
            .height(60)
            .noiseLines(5)
            .noiseDots(50)
            .build();
    }

    @AfterEach
    void tearDown() {
        if (graphics != null) {
            graphics.dispose();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("should not be instantiable")
        void shouldNotBeInstantiable() throws NoSuchMethodException {
            Constructor<CaptchaNoiseUtil> constructor = CaptchaNoiseUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("DrawNoiseLines Tests")
    class DrawNoiseLinesTests {

        @Test
        @DisplayName("should not throw with valid config")
        void shouldNotThrowWithValidConfig() {
            assertThatCode(() -> CaptchaNoiseUtil.drawNoiseLines(graphics, config))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should not throw with zero noise lines")
        void shouldNotThrowWithZeroNoiseLines() {
            CaptchaConfig zeroConfig = CaptchaConfig.builder()
                .width(160).height(60).noiseLines(0).build();

            assertThatCode(() -> CaptchaNoiseUtil.drawNoiseLines(graphics, zeroConfig))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should modify the image when lines are drawn")
        void shouldModifyTheImage() {
            // Fill with white first
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, 160, 60);

            CaptchaConfig highConfig = CaptchaConfig.builder()
                .width(160).height(60).noiseLines(20).build();

            CaptchaNoiseUtil.drawNoiseLines(graphics, highConfig);

            // Check that image is no longer all white
            boolean hasNonWhite = false;
            for (int y = 0; y < 60 && !hasNonWhite; y++) {
                for (int x = 0; x < 160; x++) {
                    if (image.getRGB(x, y) != Color.WHITE.getRGB()) {
                        hasNonWhite = true;
                        break;
                    }
                }
            }
            assertThat(hasNonWhite).isTrue();
        }
    }

    @Nested
    @DisplayName("DrawCurveLines Tests")
    class DrawCurveLinesTests {

        @Test
        @DisplayName("should not throw with valid config")
        void shouldNotThrowWithValidConfig() {
            assertThatCode(() -> CaptchaNoiseUtil.drawCurveLines(graphics, config))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should not throw with zero noise lines")
        void shouldNotThrowWithZeroNoiseLines() {
            CaptchaConfig zeroConfig = CaptchaConfig.builder()
                .width(160).height(60).noiseLines(0).build();

            assertThatCode(() -> CaptchaNoiseUtil.drawCurveLines(graphics, zeroConfig))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("DrawCubicCurveLines Tests")
    class DrawCubicCurveLinesTests {

        @Test
        @DisplayName("should not throw with valid config")
        void shouldNotThrowWithValidConfig() {
            assertThatCode(() -> CaptchaNoiseUtil.drawCubicCurveLines(graphics, config))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should not throw with zero noise lines")
        void shouldNotThrowWithZeroNoiseLines() {
            CaptchaConfig zeroConfig = CaptchaConfig.builder()
                .width(160).height(60).noiseLines(0).build();

            assertThatCode(() -> CaptchaNoiseUtil.drawCubicCurveLines(graphics, zeroConfig))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("DrawNoiseDots Tests")
    class DrawNoiseDotsTests {

        @Test
        @DisplayName("should not throw with valid config")
        void shouldNotThrowWithValidConfig() {
            assertThatCode(() -> CaptchaNoiseUtil.drawNoiseDots(graphics, config))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should not throw with zero noise dots")
        void shouldNotThrowWithZeroNoiseDots() {
            CaptchaConfig zeroConfig = CaptchaConfig.builder()
                .width(160).height(60).noiseDots(0).build();

            assertThatCode(() -> CaptchaNoiseUtil.drawNoiseDots(graphics, zeroConfig))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should modify the image when dots are drawn")
        void shouldModifyTheImage() {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, 160, 60);

            CaptchaConfig highConfig = CaptchaConfig.builder()
                .width(160).height(60).noiseDots(200).build();

            CaptchaNoiseUtil.drawNoiseDots(graphics, highConfig);

            boolean hasNonWhite = false;
            for (int y = 0; y < 60 && !hasNonWhite; y++) {
                for (int x = 0; x < 160; x++) {
                    if (image.getRGB(x, y) != Color.WHITE.getRGB()) {
                        hasNonWhite = true;
                        break;
                    }
                }
            }
            assertThat(hasNonWhite).isTrue();
        }
    }

    @Nested
    @DisplayName("DrawBackgroundNoise Tests")
    class DrawBackgroundNoiseTests {

        @Test
        @DisplayName("should not throw with valid config")
        void shouldNotThrowWithValidConfig() {
            assertThatCode(() -> CaptchaNoiseUtil.drawBackgroundNoise(graphics, config))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Shear Tests")
    class ShearTests {

        @Test
        @DisplayName("should not throw with valid config")
        void shouldNotThrowWithValidConfig() {
            assertThatCode(() -> CaptchaNoiseUtil.shear(graphics, config))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("DrawGradientBackground Tests")
    class DrawGradientBackgroundTests {

        @Test
        @DisplayName("should not throw with valid config")
        void shouldNotThrowWithValidConfig() {
            assertThatCode(() -> CaptchaNoiseUtil.drawGradientBackground(graphics, config))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should fill the entire area")
        void shouldFillTheEntireArea() {
            // Set background to a known color
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, 160, 60);

            CaptchaNoiseUtil.drawGradientBackground(graphics, config);

            // After gradient, image should have some non-black pixels
            boolean hasNonBlack = false;
            for (int y = 0; y < 60 && !hasNonBlack; y++) {
                for (int x = 0; x < 160; x++) {
                    if (image.getRGB(x, y) != Color.BLACK.getRGB()) {
                        hasNonBlack = true;
                        break;
                    }
                }
            }
            assertThat(hasNonBlack).isTrue();
        }
    }

    @Nested
    @DisplayName("DrawInterferencePattern Tests")
    class DrawInterferencePatternTests {

        @Test
        @DisplayName("should not throw with valid config")
        void shouldNotThrowWithValidConfig() {
            assertThatCode(() -> CaptchaNoiseUtil.drawInterferencePattern(graphics, config))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should draw grid pattern")
        void shouldDrawGridPattern() {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, 160, 60);

            CaptchaNoiseUtil.drawInterferencePattern(graphics, config);

            // Grid pattern should have altered some pixels
            boolean hasNonWhite = false;
            for (int y = 0; y < 60 && !hasNonWhite; y++) {
                for (int x = 0; x < 160; x++) {
                    if (image.getRGB(x, y) != Color.WHITE.getRGB()) {
                        hasNonWhite = true;
                        break;
                    }
                }
            }
            assertThat(hasNonWhite).isTrue();
        }
    }

    // ==================== Sprint 1 New Method Tests ====================

    /**
     * Tests for drawBezierNoise method.
     * drawBezierNoise 方法测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("DrawBezierNoise Tests")
    class DrawBezierNoiseTests {

        @Test
        @DisplayName("should not throw with valid parameters")
        void should_notThrow_when_validParameters() {
            assertThatCode(() -> CaptchaNoiseUtil.drawBezierNoise(graphics, 160, 60, 5))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should modify image pixels when curves are drawn")
        void should_modifyPixels_when_curvesDrawn() {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, 160, 60);

            CaptchaNoiseUtil.drawBezierNoise(graphics, 160, 60, 10);

            boolean hasNonWhite = false;
            for (int y = 0; y < 60 && !hasNonWhite; y++) {
                for (int x = 0; x < 160; x++) {
                    if (image.getRGB(x, y) != Color.WHITE.getRGB()) {
                        hasNonWhite = true;
                        break;
                    }
                }
            }
            assertThat(hasNonWhite).isTrue();
        }

        @Test
        @DisplayName("should not throw with zero count")
        void should_notThrow_when_zeroCount() {
            assertThatCode(() -> CaptchaNoiseUtil.drawBezierNoise(graphics, 160, 60, 0))
                .doesNotThrowAnyException();
        }
    }

    /**
     * Tests for applySineWarp method.
     * applySineWarp 方法测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("ApplySineWarp Tests")
    class ApplySineWarpTests {

        @Test
        @DisplayName("should return non-null image")
        void should_returnNonNull_when_warpApplied() {
            BufferedImage result = CaptchaNoiseUtil.applySineWarp(image, 3.0, 20.0);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("should preserve image dimensions")
        void should_preserveDimensions_when_warpApplied() {
            BufferedImage result = CaptchaNoiseUtil.applySineWarp(image, 5.0, 30.0);

            assertThat(result.getWidth()).isEqualTo(image.getWidth());
            assertThat(result.getHeight()).isEqualTo(image.getHeight());
        }

        @Test
        @DisplayName("should not significantly change image when amplitude is 0")
        void should_notChangeImage_when_amplitudeIsZero() {
            // Fill with a known pattern
            graphics.setColor(Color.RED);
            graphics.fillRect(0, 0, 160, 60);

            BufferedImage result = CaptchaNoiseUtil.applySineWarp(image, 0.0, 20.0);

            // With amplitude=0, shift is always 0, so pixels should be identical
            int centerRgb = result.getRGB(80, 30);
            assertThat(centerRgb).isEqualTo(image.getRGB(80, 30));
        }
    }

    /**
     * Tests for drawOutlineShadow method.
     * drawOutlineShadow 方法测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("DrawOutlineShadow Tests")
    class DrawOutlineShadowTests {

        @Test
        @DisplayName("should not throw with valid parameters")
        void should_notThrow_when_validParameters() {
            Font font = new Font("Arial", Font.PLAIN, 20);
            assertThatCode(() -> CaptchaNoiseUtil.drawOutlineShadow(
                graphics, "A", font, 50, 40, Color.GRAY))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should restore original graphics state after drawing")
        void should_restoreState_when_drawCompleted() {
            Font originalFont = new Font("Arial", Font.BOLD, 16);
            Color originalColor = Color.BLUE;
            graphics.setFont(originalFont);
            graphics.setColor(originalColor);

            Font shadowFont = new Font("Courier", Font.PLAIN, 20);
            CaptchaNoiseUtil.drawOutlineShadow(graphics, "X", shadowFont, 50, 40, Color.RED);

            assertThat(graphics.getFont()).isEqualTo(originalFont);
            assertThat(graphics.getColor()).isEqualTo(originalColor);
        }
    }

    /**
     * Tests for calculateOverlapSpacing method.
     * calculateOverlapSpacing 方法测试。
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    @Nested
    @DisplayName("CalculateOverlapSpacing Tests")
    class CalculateOverlapSpacingTests {

        @Test
        @DisplayName("should return normal spacing when ratio is 0")
        void should_returnNormalSpacing_when_ratioIsZero() {
            int spacing = CaptchaNoiseUtil.calculateOverlapSpacing(160, 4, 32.0f, 0.0f);

            assertThat(spacing).isGreaterThan(0);
        }

        @Test
        @DisplayName("should return reduced spacing when ratio is 0.3")
        void should_returnReducedSpacing_when_ratioIs0point3() {
            int normalSpacing = CaptchaNoiseUtil.calculateOverlapSpacing(160, 4, 32.0f, 0.0f);
            int reducedSpacing = CaptchaNoiseUtil.calculateOverlapSpacing(160, 4, 32.0f, 0.3f);

            assertThat(reducedSpacing).isLessThan(normalSpacing);
        }

        @Test
        @DisplayName("should return totalWidth when charCount is 1")
        void should_returnTotalWidth_when_singleChar() {
            int spacing = CaptchaNoiseUtil.calculateOverlapSpacing(160, 1, 32.0f, 0.0f);

            assertThat(spacing).isEqualTo(160);
        }

        @Test
        @DisplayName("should auto-adjust spacing for large font to fit within totalWidth")
        void should_autoAdjust_when_largeFontExceedsWidth() {
            // Very large font with many chars should still produce positive spacing
            int spacing = CaptchaNoiseUtil.calculateOverlapSpacing(100, 10, 100.0f, 0.0f);

            assertThat(spacing).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("should clamp negative overlap ratio to 0")
        void should_clampToZero_when_negativeRatio() {
            int spacingNeg = CaptchaNoiseUtil.calculateOverlapSpacing(160, 4, 32.0f, -0.5f);
            int spacingZero = CaptchaNoiseUtil.calculateOverlapSpacing(160, 4, 32.0f, 0.0f);

            assertThat(spacingNeg).isEqualTo(spacingZero);
        }

        @Test
        @DisplayName("should clamp overlap ratio above 0.5 to 0.5")
        void should_clampToHalf_when_ratioAboveHalf() {
            int spacingOver = CaptchaNoiseUtil.calculateOverlapSpacing(160, 4, 32.0f, 1.0f);
            int spacingHalf = CaptchaNoiseUtil.calculateOverlapSpacing(160, 4, 32.0f, 0.5f);

            assertThat(spacingOver).isEqualTo(spacingHalf);
        }
    }
}
