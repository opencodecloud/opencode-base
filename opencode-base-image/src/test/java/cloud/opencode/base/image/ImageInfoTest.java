package cloud.opencode.base.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageInfo 记录测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ImageInfo 记录测试")
class ImageInfoTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建有效的ImageInfo")
        void testValidConstruction() {
            ImageInfo info = new ImageInfo(800, 600, ImageFormat.JPEG, 50000);

            assertThat(info.width()).isEqualTo(800);
            assertThat(info.height()).isEqualTo(600);
            assertThat(info.format()).isEqualTo(ImageFormat.JPEG);
            assertThat(info.fileSize()).isEqualTo(50000);
        }

        @Test
        @DisplayName("允许零尺寸")
        void testZeroDimensions() {
            ImageInfo info = new ImageInfo(0, 0, ImageFormat.PNG, 0);

            assertThat(info.width()).isEqualTo(0);
            assertThat(info.height()).isEqualTo(0);
        }

        @Test
        @DisplayName("允许null格式")
        void testNullFormat() {
            ImageInfo info = new ImageInfo(100, 100, null, 1000);

            assertThat(info.format()).isNull();
        }
    }

    @Nested
    @DisplayName("aspectRatio方法测试")
    class AspectRatioTests {

        @Test
        @DisplayName("横向图片宽高比")
        void testLandscapeAspectRatio() {
            ImageInfo info = new ImageInfo(800, 600, ImageFormat.JPEG, 1000);

            assertThat(info.aspectRatio()).isCloseTo(1.333, within(0.001));
        }

        @Test
        @DisplayName("纵向图片宽高比")
        void testPortraitAspectRatio() {
            ImageInfo info = new ImageInfo(600, 800, ImageFormat.JPEG, 1000);

            assertThat(info.aspectRatio()).isCloseTo(0.75, within(0.001));
        }

        @Test
        @DisplayName("正方形图片宽高比")
        void testSquareAspectRatio() {
            ImageInfo info = new ImageInfo(500, 500, ImageFormat.JPEG, 1000);

            assertThat(info.aspectRatio()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("零高度返回0")
        void testZeroHeightAspectRatio() {
            ImageInfo info = new ImageInfo(100, 0, ImageFormat.JPEG, 1000);

            assertThat(info.aspectRatio()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("pixels方法测试")
    class PixelsTests {

        @Test
        @DisplayName("计算像素总数")
        void testPixelCount() {
            ImageInfo info = new ImageInfo(800, 600, ImageFormat.JPEG, 1000);

            assertThat(info.pixels()).isEqualTo(480000);
        }

        @Test
        @DisplayName("正方形图片像素数")
        void testSquarePixels() {
            ImageInfo info = new ImageInfo(100, 100, ImageFormat.JPEG, 1000);

            assertThat(info.pixels()).isEqualTo(10000);
        }

        @Test
        @DisplayName("零尺寸返回0")
        void testZeroPixels() {
            ImageInfo info = new ImageInfo(0, 100, ImageFormat.JPEG, 1000);

            assertThat(info.pixels()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("estimatedMemorySize方法测试")
    class EstimatedMemorySizeTests {

        @Test
        @DisplayName("估算内存大小")
        void testMemoryEstimation() {
            ImageInfo info = new ImageInfo(800, 600, ImageFormat.JPEG, 1000);

            // 800 * 600 * 4 bytes (ARGB) = 1,920,000 bytes
            assertThat(info.estimatedMemorySize()).isEqualTo(1920000);
        }

        @Test
        @DisplayName("小图片内存估算")
        void testSmallImageMemory() {
            ImageInfo info = new ImageInfo(100, 100, ImageFormat.PNG, 500);

            // 100 * 100 * 4 = 40,000 bytes
            assertThat(info.estimatedMemorySize()).isEqualTo(40000);
        }
    }

    @Nested
    @DisplayName("isLandscape方法测试")
    class IsLandscapeTests {

        @Test
        @DisplayName("宽大于高是横向")
        void testLandscape() {
            ImageInfo info = new ImageInfo(800, 600, ImageFormat.JPEG, 1000);

            assertThat(info.isLandscape()).isTrue();
        }

        @Test
        @DisplayName("高大于宽不是横向")
        void testNotLandscape() {
            ImageInfo info = new ImageInfo(600, 800, ImageFormat.JPEG, 1000);

            assertThat(info.isLandscape()).isFalse();
        }

        @Test
        @DisplayName("正方形不是横向")
        void testSquareNotLandscape() {
            ImageInfo info = new ImageInfo(500, 500, ImageFormat.JPEG, 1000);

            assertThat(info.isLandscape()).isFalse();
        }
    }

    @Nested
    @DisplayName("isPortrait方法测试")
    class IsPortraitTests {

        @Test
        @DisplayName("高大于宽是纵向")
        void testPortrait() {
            ImageInfo info = new ImageInfo(600, 800, ImageFormat.JPEG, 1000);

            assertThat(info.isPortrait()).isTrue();
        }

        @Test
        @DisplayName("宽大于高不是纵向")
        void testNotPortrait() {
            ImageInfo info = new ImageInfo(800, 600, ImageFormat.JPEG, 1000);

            assertThat(info.isPortrait()).isFalse();
        }

        @Test
        @DisplayName("正方形不是纵向")
        void testSquareNotPortrait() {
            ImageInfo info = new ImageInfo(500, 500, ImageFormat.JPEG, 1000);

            assertThat(info.isPortrait()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSquare方法测试")
    class IsSquareTests {

        @Test
        @DisplayName("宽高相等是正方形")
        void testSquare() {
            ImageInfo info = new ImageInfo(500, 500, ImageFormat.JPEG, 1000);

            assertThat(info.isSquare()).isTrue();
        }

        @Test
        @DisplayName("横向图片不是正方形")
        void testLandscapeNotSquare() {
            ImageInfo info = new ImageInfo(800, 600, ImageFormat.JPEG, 1000);

            assertThat(info.isSquare()).isFalse();
        }

        @Test
        @DisplayName("纵向图片不是正方形")
        void testPortraitNotSquare() {
            ImageInfo info = new ImageInfo(600, 800, ImageFormat.JPEG, 1000);

            assertThat(info.isSquare()).isFalse();
        }
    }

    @Nested
    @DisplayName("fileSizeFormatted方法测试")
    class FileSizeFormattedTests {

        @Test
        @DisplayName("格式化字节")
        void testBytesFormat() {
            ImageInfo info = new ImageInfo(100, 100, ImageFormat.JPEG, 500);

            assertThat(info.fileSizeFormatted()).isEqualTo("500 B");
        }

        @Test
        @DisplayName("格式化KB")
        void testKBFormat() {
            ImageInfo info = new ImageInfo(100, 100, ImageFormat.JPEG, 2048);

            assertThat(info.fileSizeFormatted()).isEqualTo("2.0 KB");
        }

        @Test
        @DisplayName("格式化MB")
        void testMBFormat() {
            ImageInfo info = new ImageInfo(100, 100, ImageFormat.JPEG, 2 * 1024 * 1024);

            assertThat(info.fileSizeFormatted()).isEqualTo("2.0 MB");
        }

        @Test
        @DisplayName("格式化GB")
        void testGBFormat() {
            ImageInfo info = new ImageInfo(100, 100, ImageFormat.JPEG, 2L * 1024 * 1024 * 1024);

            assertThat(info.fileSizeFormatted()).isEqualTo("2.0 GB");
        }

        @Test
        @DisplayName("零字节")
        void testZeroBytes() {
            ImageInfo info = new ImageInfo(100, 100, ImageFormat.JPEG, 0);

            assertThat(info.fileSizeFormatted()).isEqualTo("0 B");
        }
    }

    @Nested
    @DisplayName("记录方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals方法")
        void testEquals() {
            ImageInfo info1 = new ImageInfo(800, 600, ImageFormat.JPEG, 1000);
            ImageInfo info2 = new ImageInfo(800, 600, ImageFormat.JPEG, 1000);
            ImageInfo info3 = new ImageInfo(800, 600, ImageFormat.PNG, 1000);

            assertThat(info1).isEqualTo(info2);
            assertThat(info1).isNotEqualTo(info3);
        }

        @Test
        @DisplayName("hashCode方法")
        void testHashCode() {
            ImageInfo info1 = new ImageInfo(800, 600, ImageFormat.JPEG, 1000);
            ImageInfo info2 = new ImageInfo(800, 600, ImageFormat.JPEG, 1000);

            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("toString方法")
        void testToString() {
            ImageInfo info = new ImageInfo(800, 600, ImageFormat.JPEG, 1000);

            assertThat(info.toString()).contains("800");
            assertThat(info.toString()).contains("600");
            assertThat(info.toString()).contains("JPEG");
        }
    }
}
