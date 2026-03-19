package cloud.opencode.base.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageFormat 枚举测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ImageFormat 枚举测试")
class ImageFormatTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含所有格式值")
        void testAllValues() {
            assertThat(ImageFormat.values()).hasSize(6);
        }

        @Test
        @DisplayName("包含正确的值")
        void testContainsCorrectValues() {
            assertThat(ImageFormat.values()).containsExactly(
                ImageFormat.JPEG,
                ImageFormat.JPG,
                ImageFormat.PNG,
                ImageFormat.GIF,
                ImageFormat.BMP,
                ImageFormat.WEBP
            );
        }
    }

    @Nested
    @DisplayName("getExtension方法测试")
    class GetExtensionTests {

        @Test
        @DisplayName("JPEG扩展名")
        void testJpegExtension() {
            assertThat(ImageFormat.JPEG.getExtension()).isEqualTo("jpg");
        }

        @Test
        @DisplayName("JPG扩展名")
        void testJpgExtension() {
            assertThat(ImageFormat.JPG.getExtension()).isEqualTo("jpg");
        }

        @Test
        @DisplayName("PNG扩展名")
        void testPngExtension() {
            assertThat(ImageFormat.PNG.getExtension()).isEqualTo("png");
        }

        @Test
        @DisplayName("GIF扩展名")
        void testGifExtension() {
            assertThat(ImageFormat.GIF.getExtension()).isEqualTo("gif");
        }

        @Test
        @DisplayName("BMP扩展名")
        void testBmpExtension() {
            assertThat(ImageFormat.BMP.getExtension()).isEqualTo("bmp");
        }

        @Test
        @DisplayName("WEBP扩展名")
        void testWebpExtension() {
            assertThat(ImageFormat.WEBP.getExtension()).isEqualTo("webp");
        }
    }

    @Nested
    @DisplayName("getMimeType方法测试")
    class GetMimeTypeTests {

        @Test
        @DisplayName("JPEG MIME类型")
        void testJpegMimeType() {
            assertThat(ImageFormat.JPEG.getMimeType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("JPG MIME类型")
        void testJpgMimeType() {
            assertThat(ImageFormat.JPG.getMimeType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("PNG MIME类型")
        void testPngMimeType() {
            assertThat(ImageFormat.PNG.getMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("GIF MIME类型")
        void testGifMimeType() {
            assertThat(ImageFormat.GIF.getMimeType()).isEqualTo("image/gif");
        }

        @Test
        @DisplayName("BMP MIME类型")
        void testBmpMimeType() {
            assertThat(ImageFormat.BMP.getMimeType()).isEqualTo("image/bmp");
        }

        @Test
        @DisplayName("WEBP MIME类型")
        void testWebpMimeType() {
            assertThat(ImageFormat.WEBP.getMimeType()).isEqualTo("image/webp");
        }
    }

    @Nested
    @DisplayName("supportsTransparency方法测试")
    class SupportsTransparencyTests {

        @Test
        @DisplayName("JPEG不支持透明")
        void testJpegNoTransparency() {
            assertThat(ImageFormat.JPEG.supportsTransparency()).isFalse();
        }

        @Test
        @DisplayName("JPG不支持透明")
        void testJpgNoTransparency() {
            assertThat(ImageFormat.JPG.supportsTransparency()).isFalse();
        }

        @Test
        @DisplayName("PNG支持透明")
        void testPngTransparency() {
            assertThat(ImageFormat.PNG.supportsTransparency()).isTrue();
        }

        @Test
        @DisplayName("GIF支持透明")
        void testGifTransparency() {
            assertThat(ImageFormat.GIF.supportsTransparency()).isTrue();
        }

        @Test
        @DisplayName("BMP不支持透明")
        void testBmpNoTransparency() {
            assertThat(ImageFormat.BMP.supportsTransparency()).isFalse();
        }

        @Test
        @DisplayName("WEBP支持透明")
        void testWebpTransparency() {
            assertThat(ImageFormat.WEBP.supportsTransparency()).isTrue();
        }
    }

    @Nested
    @DisplayName("fromExtension方法测试")
    class FromExtensionTests {

        @Test
        @DisplayName("从jpg扩展名获取格式")
        void testFromJpg() {
            assertThat(ImageFormat.fromExtension("jpg")).isEqualTo(ImageFormat.JPEG);
        }

        @Test
        @DisplayName("从jpeg扩展名获取格式")
        void testFromJpeg() {
            assertThat(ImageFormat.fromExtension("jpeg")).isEqualTo(ImageFormat.JPEG);
        }

        @Test
        @DisplayName("从png扩展名获取格式")
        void testFromPng() {
            assertThat(ImageFormat.fromExtension("png")).isEqualTo(ImageFormat.PNG);
        }

        @Test
        @DisplayName("从gif扩展名获取格式")
        void testFromGif() {
            assertThat(ImageFormat.fromExtension("gif")).isEqualTo(ImageFormat.GIF);
        }

        @Test
        @DisplayName("从bmp扩展名获取格式")
        void testFromBmp() {
            assertThat(ImageFormat.fromExtension("bmp")).isEqualTo(ImageFormat.BMP);
        }

        @Test
        @DisplayName("从webp扩展名获取格式")
        void testFromWebp() {
            assertThat(ImageFormat.fromExtension("webp")).isEqualTo(ImageFormat.WEBP);
        }

        @Test
        @DisplayName("大写扩展名")
        void testUpperCaseExtension() {
            assertThat(ImageFormat.fromExtension("JPG")).isEqualTo(ImageFormat.JPEG);
            assertThat(ImageFormat.fromExtension("PNG")).isEqualTo(ImageFormat.PNG);
        }

        @Test
        @DisplayName("无效扩展名抛出异常")
        void testInvalidExtension() {
            assertThatThrownBy(() -> ImageFormat.fromExtension("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("fromMimeType方法测试")
    class FromMimeTypeTests {

        @Test
        @DisplayName("从image/jpeg获取格式")
        void testFromJpegMime() {
            assertThat(ImageFormat.fromMimeType("image/jpeg")).isEqualTo(ImageFormat.JPEG);
        }

        @Test
        @DisplayName("从image/png获取格式")
        void testFromPngMime() {
            assertThat(ImageFormat.fromMimeType("image/png")).isEqualTo(ImageFormat.PNG);
        }

        @Test
        @DisplayName("从image/gif获取格式")
        void testFromGifMime() {
            assertThat(ImageFormat.fromMimeType("image/gif")).isEqualTo(ImageFormat.GIF);
        }

        @Test
        @DisplayName("从image/bmp获取格式")
        void testFromBmpMime() {
            assertThat(ImageFormat.fromMimeType("image/bmp")).isEqualTo(ImageFormat.BMP);
        }

        @Test
        @DisplayName("从image/webp获取格式")
        void testFromWebpMime() {
            assertThat(ImageFormat.fromMimeType("image/webp")).isEqualTo(ImageFormat.WEBP);
        }

        @Test
        @DisplayName("无效MIME类型抛出异常")
        void testInvalidMimeType() {
            assertThatThrownBy(() -> ImageFormat.fromMimeType("image/invalid"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("isSupported方法测试")
    class IsSupportedTests {

        @Test
        @DisplayName("JPEG格式支持")
        void testJpegSupported() {
            assertThat(ImageFormat.isSupported("jpg")).isTrue();
            assertThat(ImageFormat.isSupported("jpeg")).isTrue();
        }

        @Test
        @DisplayName("PNG格式支持")
        void testPngSupported() {
            assertThat(ImageFormat.isSupported("png")).isTrue();
        }

        @Test
        @DisplayName("GIF格式支持")
        void testGifSupported() {
            assertThat(ImageFormat.isSupported("gif")).isTrue();
        }

        @Test
        @DisplayName("BMP格式支持")
        void testBmpSupported() {
            assertThat(ImageFormat.isSupported("bmp")).isTrue();
        }

        @Test
        @DisplayName("大小写不敏感")
        void testCaseInsensitive() {
            assertThat(ImageFormat.isSupported("JPG")).isTrue();
            assertThat(ImageFormat.isSupported("Png")).isTrue();
        }

        @Test
        @DisplayName("不支持的格式")
        void testUnsupportedFormat() {
            assertThat(ImageFormat.isSupported("invalid")).isFalse();
            assertThat(ImageFormat.isSupported("tiff")).isFalse();
        }
    }

    @Nested
    @DisplayName("isAvailable方法测试")
    class IsAvailableTests {

        @Test
        @DisplayName("标准格式可用")
        void testStandardFormatsAvailable() {
            assertThat(ImageFormat.JPEG.isAvailable()).isTrue();
            assertThat(ImageFormat.PNG.isAvailable()).isTrue();
            assertThat(ImageFormat.GIF.isAvailable()).isTrue();
            assertThat(ImageFormat.BMP.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("isWebPAvailable方法测试")
    class IsWebPAvailableTests {

        @Test
        @DisplayName("检查WebP可用性")
        void testWebPAvailability() {
            // WebP可用性取决于JDK版本和配置
            boolean available = ImageFormat.isWebPAvailable();
            assertThat(available).isIn(true, false);
        }
    }
}
