package cloud.opencode.base.image.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageErrorCode 枚举测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ImageErrorCode 枚举测试")
class ImageErrorCodeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含所有错误码")
        void testAllValues() {
            assertThat(ImageErrorCode.values()).hasSizeGreaterThanOrEqualTo(18);
        }

        @Test
        @DisplayName("UNKNOWN错误码")
        void testUnknown() {
            assertThat(ImageErrorCode.UNKNOWN.getCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("IO错误码测试")
    class IOErrorCodesTests {

        @Test
        @DisplayName("READ_FAILED错误码")
        void testReadFailed() {
            assertThat(ImageErrorCode.READ_FAILED.getCode()).isEqualTo(1001);
            assertThat(ImageErrorCode.READ_FAILED.getMessage()).isEqualTo("Image read failed");
            assertThat(ImageErrorCode.READ_FAILED.getDescription()).isEqualTo("图片读取失败");
        }

        @Test
        @DisplayName("WRITE_FAILED错误码")
        void testWriteFailed() {
            assertThat(ImageErrorCode.WRITE_FAILED.getCode()).isEqualTo(1002);
            assertThat(ImageErrorCode.WRITE_FAILED.getMessage()).isEqualTo("Image write failed");
        }

        @Test
        @DisplayName("FILE_NOT_FOUND错误码")
        void testFileNotFound() {
            assertThat(ImageErrorCode.FILE_NOT_FOUND.getCode()).isEqualTo(1003);
        }

        @Test
        @DisplayName("IO_ERROR错误码")
        void testIOError() {
            assertThat(ImageErrorCode.IO_ERROR.getCode()).isEqualTo(1004);
        }
    }

    @Nested
    @DisplayName("格式错误码测试")
    class FormatErrorCodesTests {

        @Test
        @DisplayName("UNSUPPORTED_FORMAT错误码")
        void testUnsupportedFormat() {
            assertThat(ImageErrorCode.UNSUPPORTED_FORMAT.getCode()).isEqualTo(2001);
            assertThat(ImageErrorCode.UNSUPPORTED_FORMAT.getMessage()).contains("Unsupported");
        }

        @Test
        @DisplayName("INVALID_IMAGE错误码")
        void testInvalidImage() {
            assertThat(ImageErrorCode.INVALID_IMAGE.getCode()).isEqualTo(2002);
        }

        @Test
        @DisplayName("FORMAT_MISMATCH错误码")
        void testFormatMismatch() {
            assertThat(ImageErrorCode.FORMAT_MISMATCH.getCode()).isEqualTo(2003);
        }

        @Test
        @DisplayName("MAGIC_NUMBER_MISMATCH错误码")
        void testMagicNumberMismatch() {
            assertThat(ImageErrorCode.MAGIC_NUMBER_MISMATCH.getCode()).isEqualTo(2004);
        }
    }

    @Nested
    @DisplayName("操作错误码测试")
    class OperationErrorCodesTests {

        @Test
        @DisplayName("RESIZE_FAILED错误码")
        void testResizeFailed() {
            assertThat(ImageErrorCode.RESIZE_FAILED.getCode()).isEqualTo(3001);
        }

        @Test
        @DisplayName("CROP_FAILED错误码")
        void testCropFailed() {
            assertThat(ImageErrorCode.CROP_FAILED.getCode()).isEqualTo(3002);
        }

        @Test
        @DisplayName("ROTATE_FAILED错误码")
        void testRotateFailed() {
            assertThat(ImageErrorCode.ROTATE_FAILED.getCode()).isEqualTo(3003);
        }

        @Test
        @DisplayName("WATERMARK_FAILED错误码")
        void testWatermarkFailed() {
            assertThat(ImageErrorCode.WATERMARK_FAILED.getCode()).isEqualTo(3004);
        }

        @Test
        @DisplayName("COMPRESS_FAILED错误码")
        void testCompressFailed() {
            assertThat(ImageErrorCode.COMPRESS_FAILED.getCode()).isEqualTo(3005);
        }

        @Test
        @DisplayName("INVALID_PARAMETERS错误码")
        void testInvalidParameters() {
            assertThat(ImageErrorCode.INVALID_PARAMETERS.getCode()).isEqualTo(3006);
        }

        @Test
        @DisplayName("CONVERT_FAILED错误码")
        void testConvertFailed() {
            assertThat(ImageErrorCode.CONVERT_FAILED.getCode()).isEqualTo(3007);
        }
    }

    @Nested
    @DisplayName("验证错误码测试")
    class ValidationErrorCodesTests {

        @Test
        @DisplayName("IMAGE_TOO_LARGE错误码")
        void testImageTooLarge() {
            assertThat(ImageErrorCode.IMAGE_TOO_LARGE.getCode()).isEqualTo(4001);
        }

        @Test
        @DisplayName("FILE_TOO_LARGE错误码")
        void testFileTooLarge() {
            assertThat(ImageErrorCode.FILE_TOO_LARGE.getCode()).isEqualTo(4002);
        }

        @Test
        @DisplayName("INVALID_DIMENSIONS错误码")
        void testInvalidDimensions() {
            assertThat(ImageErrorCode.INVALID_DIMENSIONS.getCode()).isEqualTo(4003);
        }

        @Test
        @DisplayName("VALIDATION_FAILED错误码")
        void testValidationFailed() {
            assertThat(ImageErrorCode.VALIDATION_FAILED.getCode()).isEqualTo(4004);
        }
    }

    @Nested
    @DisplayName("资源错误码测试")
    class ResourceErrorCodesTests {

        @Test
        @DisplayName("TIMEOUT错误码")
        void testTimeout() {
            assertThat(ImageErrorCode.TIMEOUT.getCode()).isEqualTo(5001);
            assertThat(ImageErrorCode.TIMEOUT.getMessage()).contains("timeout");
        }

        @Test
        @DisplayName("OUT_OF_MEMORY错误码")
        void testOutOfMemory() {
            assertThat(ImageErrorCode.OUT_OF_MEMORY.getCode()).isEqualTo(5002);
        }

        @Test
        @DisplayName("TOO_MANY_REQUESTS错误码")
        void testTooManyRequests() {
            assertThat(ImageErrorCode.TOO_MANY_REQUESTS.getCode()).isEqualTo(5003);
        }

        @Test
        @DisplayName("RESOURCE_UNAVAILABLE错误码")
        void testResourceUnavailable() {
            assertThat(ImageErrorCode.RESOURCE_UNAVAILABLE.getCode()).isEqualTo(5004);
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterMethodsTests {

        @Test
        @DisplayName("getCode返回正确的值")
        void testGetCode() {
            assertThat(ImageErrorCode.UNKNOWN.getCode()).isEqualTo(0);
            assertThat(ImageErrorCode.READ_FAILED.getCode()).isEqualTo(1001);
        }

        @Test
        @DisplayName("getMessage返回正确的值")
        void testGetMessage() {
            assertThat(ImageErrorCode.UNKNOWN.getMessage()).isEqualTo("Unknown error");
            assertThat(ImageErrorCode.READ_FAILED.getMessage()).isEqualTo("Image read failed");
        }

        @Test
        @DisplayName("getDescription返回正确的值")
        void testGetDescription() {
            assertThat(ImageErrorCode.UNKNOWN.getDescription()).isEqualTo("未知错误");
            assertThat(ImageErrorCode.READ_FAILED.getDescription()).isEqualTo("图片读取失败");
        }
    }

    @Nested
    @DisplayName("错误码范围测试")
    class CodeRangeTests {

        @Test
        @DisplayName("IO错误码在1xxx范围")
        void testIOCodesInRange() {
            assertThat(ImageErrorCode.READ_FAILED.getCode()).isBetween(1000, 1999);
            assertThat(ImageErrorCode.WRITE_FAILED.getCode()).isBetween(1000, 1999);
            assertThat(ImageErrorCode.FILE_NOT_FOUND.getCode()).isBetween(1000, 1999);
            assertThat(ImageErrorCode.IO_ERROR.getCode()).isBetween(1000, 1999);
        }

        @Test
        @DisplayName("格式错误码在2xxx范围")
        void testFormatCodesInRange() {
            assertThat(ImageErrorCode.UNSUPPORTED_FORMAT.getCode()).isBetween(2000, 2999);
            assertThat(ImageErrorCode.INVALID_IMAGE.getCode()).isBetween(2000, 2999);
            assertThat(ImageErrorCode.FORMAT_MISMATCH.getCode()).isBetween(2000, 2999);
        }

        @Test
        @DisplayName("操作错误码在3xxx范围")
        void testOperationCodesInRange() {
            assertThat(ImageErrorCode.RESIZE_FAILED.getCode()).isBetween(3000, 3999);
            assertThat(ImageErrorCode.CROP_FAILED.getCode()).isBetween(3000, 3999);
            assertThat(ImageErrorCode.ROTATE_FAILED.getCode()).isBetween(3000, 3999);
        }

        @Test
        @DisplayName("验证错误码在4xxx范围")
        void testValidationCodesInRange() {
            assertThat(ImageErrorCode.IMAGE_TOO_LARGE.getCode()).isBetween(4000, 4999);
            assertThat(ImageErrorCode.FILE_TOO_LARGE.getCode()).isBetween(4000, 4999);
        }

        @Test
        @DisplayName("资源错误码在5xxx范围")
        void testResourceCodesInRange() {
            assertThat(ImageErrorCode.TIMEOUT.getCode()).isBetween(5000, 5999);
            assertThat(ImageErrorCode.OUT_OF_MEMORY.getCode()).isBetween(5000, 5999);
        }
    }
}
