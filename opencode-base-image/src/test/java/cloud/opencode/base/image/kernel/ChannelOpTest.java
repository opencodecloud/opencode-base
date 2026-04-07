package cloud.opencode.base.image.kernel;

import cloud.opencode.base.image.exception.ImageOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ChannelOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("ChannelOp 通道操作测试")
class ChannelOpTest {

    @Nested
    @DisplayName("split 和 merge 往返一致性测试")
    class SplitMergeTests {

        @Test
        @DisplayName("split → merge 往返一致")
        void splitMergeRoundTrip() {
            int[] original = {
                    PixelOp.argb(255, 100, 150, 200),
                    PixelOp.argb(128, 50, 75, 25),
                    PixelOp.argb(0, 0, 0, 0),
                    PixelOp.argb(255, 255, 255, 255)
            };

            int[][] channels = ChannelOp.split(original);
            assertThat(channels).hasNumberOfRows(4);
            assertThat(channels[0]).hasSize(4); // alpha
            assertThat(channels[1]).hasSize(4); // red

            int[] merged = ChannelOp.merge(channels[0], channels[1], channels[2], channels[3]);
            assertThat(merged).isEqualTo(original);
        }

        @Test
        @DisplayName("split 通道值正确")
        void splitChannelsCorrect() {
            int px = PixelOp.argb(10, 20, 30, 40);
            int[][] channels = ChannelOp.split(new int[]{px});
            assertThat(channels[0][0]).isEqualTo(10);  // alpha
            assertThat(channels[1][0]).isEqualTo(20);  // red
            assertThat(channels[2][0]).isEqualTo(30);  // green
            assertThat(channels[3][0]).isEqualTo(40);  // blue
        }

        @Test
        @DisplayName("null 输入抛出异常")
        void nullInputThrows() {
            assertThatThrownBy(() -> ChannelOp.split(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空数组抛出异常")
        void emptyArrayThrows() {
            assertThatThrownBy(() -> ChannelOp.split(new int[0]))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("merge 通道长度不一致抛出异常")
        void mergeMismatchedLengthsThrows() {
            assertThatThrownBy(() -> ChannelOp.merge(new int[3], new int[4], new int[4], new int[4]))
                    .isInstanceOf(ImageOperationException.class);
        }
    }

    @Nested
    @DisplayName("toGray 灰度转换测试")
    class ToGrayTests {

        @Test
        @DisplayName("纯红色灰度值为 76")
        void pureRedGray() {
            int[] pixels = {PixelOp.argb(255, 255, 0, 0)};
            int[] gray = ChannelOp.toGray(pixels);
            // BT.601: 0.299 * 255 = 76.245, rounded with (299*255 + 500)/1000 = 76
            assertThat(gray[0]).isEqualTo(76);
        }

        @Test
        @DisplayName("纯绿色灰度值为 150")
        void pureGreenGray() {
            int[] pixels = {PixelOp.argb(255, 0, 255, 0)};
            int[] gray = ChannelOp.toGray(pixels);
            // BT.601: 0.587 * 255 = 149.685, rounded with (587*255 + 500)/1000 = 150
            assertThat(gray[0]).isEqualTo(150);
        }

        @Test
        @DisplayName("纯蓝色灰度值为 29")
        void pureBlueGray() {
            int[] pixels = {PixelOp.argb(255, 0, 0, 255)};
            int[] gray = ChannelOp.toGray(pixels);
            // BT.601: 0.114 * 255 = 29.07, rounded with (114*255 + 500)/1000 = 29
            assertThat(gray[0]).isEqualTo(29);
        }

        @Test
        @DisplayName("白色灰度值为 255")
        void whiteGray() {
            int[] pixels = {PixelOp.argb(255, 255, 255, 255)};
            int[] gray = ChannelOp.toGray(pixels);
            assertThat(gray[0]).isEqualTo(255);
        }

        @Test
        @DisplayName("黑色灰度值为 0")
        void blackGray() {
            int[] pixels = {PixelOp.argb(255, 0, 0, 0)};
            int[] gray = ChannelOp.toGray(pixels);
            assertThat(gray[0]).isEqualTo(0);
        }

        @Test
        @DisplayName("null 输入抛出异常")
        void nullInputThrows() {
            assertThatThrownBy(() -> ChannelOp.toGray(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("grayToArgb 灰度转 ARGB 测试")
    class GrayToArgbTests {

        @Test
        @DisplayName("灰度值复制到 RGB 且 A=255")
        void grayValueCopiedToRgb() {
            int[] gray = {100, 0, 255, 128};
            int[] argb = ChannelOp.grayToArgb(gray);

            for (int i = 0; i < gray.length; i++) {
                assertThat(PixelOp.alpha(argb[i])).isEqualTo(255);
                assertThat(PixelOp.red(argb[i])).isEqualTo(gray[i]);
                assertThat(PixelOp.green(argb[i])).isEqualTo(gray[i]);
                assertThat(PixelOp.blue(argb[i])).isEqualTo(gray[i]);
            }
        }

        @Test
        @DisplayName("null 输入抛出异常")
        void nullInputThrows() {
            assertThatThrownBy(() -> ChannelOp.grayToArgb(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空数组抛出异常")
        void emptyArrayThrows() {
            assertThatThrownBy(() -> ChannelOp.grayToArgb(new int[0]))
                    .isInstanceOf(ImageOperationException.class);
        }
    }
}
