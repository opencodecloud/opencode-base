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
}
