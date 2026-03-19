package cloud.opencode.base.image.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * CropOp 操作测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("CropOp 操作测试")
class CropOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
    }

    @Nested
    @DisplayName("crop方法测试")
    class CropTests {

        @Test
        @DisplayName("裁剪指定区域")
        void testCrop() {
            BufferedImage result = CropOp.crop(testImage, 100, 100, 200, 150);

            assertThat(result.getWidth()).isEqualTo(200);
            assertThat(result.getHeight()).isEqualTo(150);
        }

        @Test
        @DisplayName("从原点裁剪")
        void testCropFromOrigin() {
            BufferedImage result = CropOp.crop(testImage, 0, 0, 400, 300);

            assertThat(result.getWidth()).isEqualTo(400);
            assertThat(result.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("裁剪整个图片")
        void testCropFullImage() {
            BufferedImage result = CropOp.crop(testImage, 0, 0, 800, 600);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("负坐标调整为0")
        void testCropNegativeCoordinates() {
            BufferedImage result = CropOp.crop(testImage, -50, -50, 200, 200);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("超出边界自动调整")
        void testCropOutOfBounds() {
            BufferedImage result = CropOp.crop(testImage, 700, 500, 200, 200);

            // 应该调整到图片边界
            assertThat(result.getWidth()).isEqualTo(100);
            assertThat(result.getHeight()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("cropCenter方法测试")
    class CropCenterTests {

        @Test
        @DisplayName("从中心裁剪")
        void testCropCenter() {
            BufferedImage result = CropOp.cropCenter(testImage, 400, 300);

            assertThat(result.getWidth()).isEqualTo(400);
            assertThat(result.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("从中心裁剪正方形")
        void testCropCenterSquare() {
            BufferedImage result = CropOp.cropCenter(testImage, 300, 300);

            assertThat(result.getWidth()).isEqualTo(300);
            assertThat(result.getHeight()).isEqualTo(300);
        }
    }

    @Nested
    @DisplayName("cropSquare方法测试")
    class CropSquareTests {

        @Test
        @DisplayName("裁剪为正方形")
        void testCropSquare() {
            BufferedImage result = CropOp.cropSquare(testImage);

            // 取最小边作为正方形大小
            assertThat(result.getWidth()).isEqualTo(600);
            assertThat(result.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("已经是正方形的图片")
        void testCropSquareAlreadySquare() {
            BufferedImage square = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
            BufferedImage result = CropOp.cropSquare(square);

            assertThat(result.getWidth()).isEqualTo(500);
            assertThat(result.getHeight()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("cropToAspectRatio方法测试")
    class CropToAspectRatioTests {

        @Test
        @DisplayName("裁剪到16:9比例")
        void testCropToAspectRatio16x9() {
            BufferedImage result = CropOp.cropToAspectRatio(testImage, 16, 9);

            double ratio = (double) result.getWidth() / result.getHeight();
            assertThat(ratio).isCloseTo(16.0 / 9.0, within(0.01));
        }

        @Test
        @DisplayName("裁剪到4:3比例")
        void testCropToAspectRatio4x3() {
            BufferedImage result = CropOp.cropToAspectRatio(testImage, 4, 3);

            double ratio = (double) result.getWidth() / result.getHeight();
            assertThat(ratio).isCloseTo(4.0 / 3.0, within(0.01));
        }

        @Test
        @DisplayName("裁剪到1:1比例")
        void testCropToAspectRatio1x1() {
            BufferedImage result = CropOp.cropToAspectRatio(testImage, 1, 1);

            assertThat(result.getWidth()).isEqualTo(result.getHeight());
        }
    }

    @Nested
    @DisplayName("cropTop方法测试")
    class CropTopTests {

        @Test
        @DisplayName("裁剪顶部")
        void testCropTop() {
            BufferedImage result = CropOp.cropTop(testImage, 200);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("cropBottom方法测试")
    class CropBottomTests {

        @Test
        @DisplayName("裁剪底部")
        void testCropBottom() {
            BufferedImage result = CropOp.cropBottom(testImage, 200);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("cropLeft方法测试")
    class CropLeftTests {

        @Test
        @DisplayName("裁剪左侧")
        void testCropLeft() {
            BufferedImage result = CropOp.cropLeft(testImage, 300);

            assertThat(result.getWidth()).isEqualTo(300);
            assertThat(result.getHeight()).isEqualTo(600);
        }
    }

    @Nested
    @DisplayName("cropRight方法测试")
    class CropRightTests {

        @Test
        @DisplayName("裁剪右侧")
        void testCropRight() {
            BufferedImage result = CropOp.cropRight(testImage, 300);

            assertThat(result.getWidth()).isEqualTo(300);
            assertThat(result.getHeight()).isEqualTo(600);
        }
    }

    @Nested
    @DisplayName("trim方法测试")
    class TrimTests {

        @Test
        @DisplayName("修剪四边")
        void testTrimFourSides() {
            BufferedImage result = CropOp.trim(testImage, 50, 50, 50, 50);

            assertThat(result.getWidth()).isEqualTo(700);
            assertThat(result.getHeight()).isEqualTo(500);
        }

        @Test
        @DisplayName("修剪相同数量")
        void testTrimEqual() {
            BufferedImage result = CropOp.trim(testImage, 50);

            assertThat(result.getWidth()).isEqualTo(700);
            assertThat(result.getHeight()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("图片类型测试")
    class ImageTypeTests {

        @Test
        @DisplayName("保持RGB类型")
        void testPreservesRGBType() {
            BufferedImage rgb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            BufferedImage result = CropOp.crop(rgb, 0, 0, 50, 50);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }

        @Test
        @DisplayName("保持ARGB类型")
        void testPreservesARGBType() {
            BufferedImage argb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            BufferedImage result = CropOp.crop(argb, 0, 0, 50, 50);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }
    }
}
