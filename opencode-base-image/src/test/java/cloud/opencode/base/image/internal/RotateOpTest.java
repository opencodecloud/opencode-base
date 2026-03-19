package cloud.opencode.base.image.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * RotateOp 操作测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("RotateOp 操作测试")
class RotateOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
    }

    @Nested
    @DisplayName("rotate方法测试")
    class RotateTests {

        @Test
        @DisplayName("旋转0度")
        void testRotateZero() {
            BufferedImage result = RotateOp.rotate(testImage, 0);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("旋转45度")
        void testRotate45() {
            BufferedImage result = RotateOp.rotate(testImage, 45);

            // 旋转后尺寸会变大
            assertThat(result.getWidth()).isGreaterThan(800);
            assertThat(result.getHeight()).isGreaterThan(600);
        }

        @Test
        @DisplayName("旋转90度")
        void testRotate90() {
            BufferedImage result = RotateOp.rotate(testImage, 90);

            // 旋转90度后宽高互换
            assertThat(result.getWidth()).isEqualTo(600);
            assertThat(result.getHeight()).isEqualTo(800);
        }

        @Test
        @DisplayName("旋转180度")
        void testRotate180() {
            BufferedImage result = RotateOp.rotate(testImage, 180);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("旋转负角度")
        void testRotateNegative() {
            BufferedImage result = RotateOp.rotate(testImage, -90);

            assertThat(result.getWidth()).isEqualTo(600);
            assertThat(result.getHeight()).isEqualTo(800);
        }
    }

    @Nested
    @DisplayName("rotate90方法测试")
    class Rotate90Tests {

        @Test
        @DisplayName("顺时针旋转90度")
        void testRotate90Clockwise() {
            BufferedImage result = RotateOp.rotate90(testImage);

            assertThat(result.getWidth()).isEqualTo(600);
            assertThat(result.getHeight()).isEqualTo(800);
        }

        @Test
        @DisplayName("正方形旋转90度")
        void testRotate90Square() {
            BufferedImage square = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
            BufferedImage result = RotateOp.rotate90(square);

            assertThat(result.getWidth()).isEqualTo(500);
            assertThat(result.getHeight()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("rotate180方法测试")
    class Rotate180Tests {

        @Test
        @DisplayName("旋转180度")
        void testRotate180() {
            BufferedImage result = RotateOp.rotate180(testImage);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }
    }

    @Nested
    @DisplayName("rotate270方法测试")
    class Rotate270Tests {

        @Test
        @DisplayName("逆时针旋转90度")
        void testRotate270() {
            BufferedImage result = RotateOp.rotate270(testImage);

            assertThat(result.getWidth()).isEqualTo(600);
            assertThat(result.getHeight()).isEqualTo(800);
        }
    }

    @Nested
    @DisplayName("flipHorizontal方法测试")
    class FlipHorizontalTests {

        @Test
        @DisplayName("水平翻转")
        void testFlipHorizontal() {
            BufferedImage result = RotateOp.flipHorizontal(testImage);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }
    }

    @Nested
    @DisplayName("flipVertical方法测试")
    class FlipVerticalTests {

        @Test
        @DisplayName("垂直翻转")
        void testFlipVertical() {
            BufferedImage result = RotateOp.flipVertical(testImage);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }
    }

    @Nested
    @DisplayName("applyExifOrientation方法测试")
    class ApplyExifOrientationTests {

        @Test
        @DisplayName("方向1 - 正常")
        void testOrientation1() {
            BufferedImage result = RotateOp.applyExifOrientation(testImage, 1);

            assertThat(result).isEqualTo(testImage);
        }

        @Test
        @DisplayName("方向2 - 水平翻转")
        void testOrientation2() {
            BufferedImage result = RotateOp.applyExifOrientation(testImage, 2);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("方向3 - 旋转180度")
        void testOrientation3() {
            BufferedImage result = RotateOp.applyExifOrientation(testImage, 3);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("方向4 - 垂直翻转")
        void testOrientation4() {
            BufferedImage result = RotateOp.applyExifOrientation(testImage, 4);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("方向5 - 旋转90度+水平翻转")
        void testOrientation5() {
            BufferedImage result = RotateOp.applyExifOrientation(testImage, 5);

            assertThat(result.getWidth()).isEqualTo(600);
            assertThat(result.getHeight()).isEqualTo(800);
        }

        @Test
        @DisplayName("方向6 - 顺时针旋转90度")
        void testOrientation6() {
            BufferedImage result = RotateOp.applyExifOrientation(testImage, 6);

            assertThat(result.getWidth()).isEqualTo(600);
            assertThat(result.getHeight()).isEqualTo(800);
        }

        @Test
        @DisplayName("方向7 - 逆时针旋转90度+水平翻转")
        void testOrientation7() {
            BufferedImage result = RotateOp.applyExifOrientation(testImage, 7);

            assertThat(result.getWidth()).isEqualTo(600);
            assertThat(result.getHeight()).isEqualTo(800);
        }

        @Test
        @DisplayName("方向8 - 逆时针旋转90度")
        void testOrientation8() {
            BufferedImage result = RotateOp.applyExifOrientation(testImage, 8);

            assertThat(result.getWidth()).isEqualTo(600);
            assertThat(result.getHeight()).isEqualTo(800);
        }

        @Test
        @DisplayName("无效方向返回原图")
        void testInvalidOrientation() {
            BufferedImage result = RotateOp.applyExifOrientation(testImage, 0);
            assertThat(result).isEqualTo(testImage);

            result = RotateOp.applyExifOrientation(testImage, 9);
            assertThat(result).isEqualTo(testImage);
        }
    }

    @Nested
    @DisplayName("图片类型测试")
    class ImageTypeTests {

        @Test
        @DisplayName("保持RGB类型")
        void testPreservesRGBType() {
            BufferedImage rgb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            BufferedImage result = RotateOp.rotate90(rgb);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }

        @Test
        @DisplayName("保持ARGB类型")
        void testPreservesARGBType() {
            BufferedImage argb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            BufferedImage result = RotateOp.rotate90(argb);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }
    }
}
