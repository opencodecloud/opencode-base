package cloud.opencode.base.image.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * OverlayOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
@DisplayName("OverlayOp 测试")
class OverlayOpTest {

    private BufferedImage base;
    private BufferedImage overlay;

    @BeforeEach
    void setUp() {
        // Base: solid white 200x200
        base = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                base.setRGB(x, y, 0xFFFFFFFF);
            }
        }

        // Overlay: solid red 50x50
        overlay = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                overlay.setRGB(x, y, 0xFFFF0000);
            }
        }
    }

    @Nested
    @DisplayName("overlay 方法测试")
    class OverlayTests {

        @Test
        @DisplayName("结果尺寸等于底图尺寸")
        void testResultSizeEqualsBase() {
            BufferedImage result = OverlayOp.overlay(base, overlay, 10, 10, 1.0f);

            assertThat(result.getWidth()).isEqualTo(200);
            assertThat(result.getHeight()).isEqualTo(200);
        }

        @Test
        @DisplayName("完全不透明叠加覆盖底图像素")
        void testFullOpacityCoversBase() {
            BufferedImage result = OverlayOp.overlay(base, overlay, 0, 0, 1.0f);

            int argb = result.getRGB(25, 25);
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            assertThat(r).isGreaterThan(200);
            assertThat(g).isLessThan(50);
            assertThat(b).isLessThan(50);
        }

        @Test
        @DisplayName("完全透明叠加不影响底图")
        void testZeroOpacityPreservesBase() {
            BufferedImage result = OverlayOp.overlay(base, overlay, 0, 0, 0.0f);

            int argb = result.getRGB(25, 25);
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            // Should remain white-ish (base is white)
            assertThat(r).isGreaterThan(200);
            assertThat(g).isGreaterThan(200);
            assertThat(b).isGreaterThan(200);
        }

        @Test
        @DisplayName("叠加图片超出底图边界时被裁剪")
        void testOverlayExceedsBoundaryClipped() {
            // Overlay starts at (180, 180), extends to (230, 230) — beyond base
            BufferedImage result = OverlayOp.overlay(base, overlay, 180, 180, 1.0f);

            // Should not throw; result size equals base
            assertThat(result.getWidth()).isEqualTo(200);
            assertThat(result.getHeight()).isEqualTo(200);
        }

        @Test
        @DisplayName("透明度超出[0,1]范围抛出异常")
        void testInvalidOpacityThrows() {
            assertThatThrownBy(() -> OverlayOp.overlay(base, overlay, 0, 0, -0.1f))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> OverlayOp.overlay(base, overlay, 0, 0, 1.1f))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NaN透明度抛出异常")
        void testNaNOpacityThrows() {
            assertThatThrownBy(() -> OverlayOp.overlay(base, overlay, 0, 0, Float.NaN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null底图抛出NullPointerException")
        void testNullBaseThrows() {
            assertThatNullPointerException().isThrownBy(() ->
                    OverlayOp.overlay(null, overlay, 0, 0, 1.0f));
        }

        @Test
        @DisplayName("null叠加图片抛出NullPointerException")
        void testNullOverlayThrows() {
            assertThatNullPointerException().isThrownBy(() ->
                    OverlayOp.overlay(base, null, 0, 0, 1.0f));
        }
    }
}
