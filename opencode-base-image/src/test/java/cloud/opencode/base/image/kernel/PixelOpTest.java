package cloud.opencode.base.image.kernel;

import cloud.opencode.base.image.exception.ImageOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * PixelOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("PixelOp 像素操作测试")
class PixelOpTest {

    @Nested
    @DisplayName("ensureArgb 类型转换测试")
    class EnsureArgbTests {

        @Test
        @DisplayName("TYPE_INT_RGB 转换为 TYPE_INT_ARGB")
        void typeIntRgbConvertsToArgb() {
            BufferedImage rgb = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 10, 10);
            g.dispose();

            BufferedImage result = PixelOp.ensureArgb(rgb);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
            assertThat(result.getWidth()).isEqualTo(10);
            assertThat(result.getHeight()).isEqualTo(10);
            // Verify pixel color preserved (red with full alpha)
            int px = result.getRGB(0, 0);
            assertThat(PixelOp.red(px)).isEqualTo(255);
            assertThat(PixelOp.green(px)).isEqualTo(0);
            assertThat(PixelOp.blue(px)).isEqualTo(0);
            assertThat(PixelOp.alpha(px)).isEqualTo(255);
        }

        @Test
        @DisplayName("TYPE_INT_ARGB 返回原图实例")
        void typeIntArgbReturnsSameInstance() {
            BufferedImage argb = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            BufferedImage result = PixelOp.ensureArgb(argb);
            assertThat(result).isSameAs(argb);
        }

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrowsException() {
            assertThatThrownBy(() -> PixelOp.ensureArgb(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getPixels 零拷贝像素访问测试")
    class GetPixelsTests {

        @Test
        @DisplayName("返回底层数组（零拷贝）")
        void returnsBackingArray() {
            BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);

            assertThat(pixels).hasSize(25);

            // Modify through array, verify via getRGB
            pixels[0] = PixelOp.argb(255, 100, 150, 200);
            int rgb = img.getRGB(0, 0);
            assertThat(PixelOp.red(rgb)).isEqualTo(100);
            assertThat(PixelOp.green(rgb)).isEqualTo(150);
            assertThat(PixelOp.blue(rgb)).isEqualTo(200);
        }

        @Test
        @DisplayName("非 TYPE_INT_ARGB 图像抛出异常")
        void nonArgbThrows() {
            BufferedImage rgb = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
            assertThatThrownBy(() -> PixelOp.getPixels(rgb))
                    .isInstanceOf(ImageOperationException.class);
        }
    }

    @Nested
    @DisplayName("createCompatible 创建兼容图像测试")
    class CreateCompatibleTests {

        @Test
        @DisplayName("创建同尺寸 ARGB 图像")
        void createsSameSizeArgb() {
            BufferedImage src = new BufferedImage(123, 456, BufferedImage.TYPE_INT_ARGB);
            BufferedImage result = PixelOp.createCompatible(src);

            assertThat(result.getWidth()).isEqualTo(123);
            assertThat(result.getHeight()).isEqualTo(456);
            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
            assertThat(result).isNotSameAs(src);
        }
    }

    @Nested
    @DisplayName("createArgb 创建指定尺寸图像测试")
    class CreateArgbTests {

        @Test
        @DisplayName("创建指定尺寸图像")
        void createsSpecifiedSize() {
            BufferedImage img = PixelOp.createArgb(320, 240);
            assertThat(img.getWidth()).isEqualTo(320);
            assertThat(img.getHeight()).isEqualTo(240);
            assertThat(img.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }

        @Test
        @DisplayName("非正数尺寸抛出异常")
        void invalidDimensionsThrow() {
            assertThatThrownBy(() -> PixelOp.createArgb(0, 10))
                    .isInstanceOf(ImageOperationException.class);
            assertThatThrownBy(() -> PixelOp.createArgb(10, -1))
                    .isInstanceOf(ImageOperationException.class);
        }
    }

    @Nested
    @DisplayName("clamp 像素裁剪测试")
    class ClampTests {

        @Test
        @DisplayName("负数裁剪为 0")
        void negativeClampsToZero() {
            assertThat(PixelOp.clamp(-100)).isEqualTo(0);
            assertThat(PixelOp.clamp(-1)).isEqualTo(0);
        }

        @Test
        @DisplayName("超过 255 裁剪为 255")
        void overflowClampsTo255() {
            assertThat(PixelOp.clamp(256)).isEqualTo(255);
            assertThat(PixelOp.clamp(1000)).isEqualTo(255);
        }

        @Test
        @DisplayName("范围内值不变")
        void inRangeUnchanged() {
            assertThat(PixelOp.clamp(0)).isEqualTo(0);
            assertThat(PixelOp.clamp(128)).isEqualTo(128);
            assertThat(PixelOp.clamp(255)).isEqualTo(255);
        }
    }

    @Nested
    @DisplayName("ARGB 通道提取与合成测试")
    class ArgbChannelTests {

        @Test
        @DisplayName("argb 合成与各通道提取往返一致")
        void argbRoundTrip() {
            int packed = PixelOp.argb(200, 100, 150, 50);
            assertThat(PixelOp.alpha(packed)).isEqualTo(200);
            assertThat(PixelOp.red(packed)).isEqualTo(100);
            assertThat(PixelOp.green(packed)).isEqualTo(150);
            assertThat(PixelOp.blue(packed)).isEqualTo(50);
        }

        @Test
        @DisplayName("边界值 0 和 255")
        void boundaryValues() {
            int allZero = PixelOp.argb(0, 0, 0, 0);
            assertThat(PixelOp.alpha(allZero)).isEqualTo(0);
            assertThat(PixelOp.red(allZero)).isEqualTo(0);
            assertThat(PixelOp.green(allZero)).isEqualTo(0);
            assertThat(PixelOp.blue(allZero)).isEqualTo(0);

            int allMax = PixelOp.argb(255, 255, 255, 255);
            assertThat(PixelOp.alpha(allMax)).isEqualTo(255);
            assertThat(PixelOp.red(allMax)).isEqualTo(255);
            assertThat(PixelOp.green(allMax)).isEqualTo(255);
            assertThat(PixelOp.blue(allMax)).isEqualTo(255);
        }

        @Test
        @DisplayName("各通道独立不干扰")
        void channelsAreIndependent() {
            // Only alpha set
            int onlyA = PixelOp.argb(255, 0, 0, 0);
            assertThat(PixelOp.alpha(onlyA)).isEqualTo(255);
            assertThat(PixelOp.red(onlyA)).isEqualTo(0);

            // Only red set
            int onlyR = PixelOp.argb(0, 255, 0, 0);
            assertThat(PixelOp.red(onlyR)).isEqualTo(255);
            assertThat(PixelOp.green(onlyR)).isEqualTo(0);

            // Only green set
            int onlyG = PixelOp.argb(0, 0, 255, 0);
            assertThat(PixelOp.green(onlyG)).isEqualTo(255);
            assertThat(PixelOp.blue(onlyG)).isEqualTo(0);

            // Only blue set
            int onlyB = PixelOp.argb(0, 0, 0, 255);
            assertThat(PixelOp.blue(onlyB)).isEqualTo(255);
            assertThat(PixelOp.alpha(onlyB)).isEqualTo(0);
        }
    }
}
