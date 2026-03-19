package cloud.opencode.base.geo.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoErrorCode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("GeoErrorCode 测试")
class GeoErrorCodeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("UNKNOWN枚举存在")
        void testUnknownExists() {
            assertThat(GeoErrorCode.UNKNOWN).isNotNull();
            assertThat(GeoErrorCode.UNKNOWN.getCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("坐标错误码存在")
        void testCoordinateErrorCodes() {
            assertThat(GeoErrorCode.INVALID_COORDINATE.getCode()).isEqualTo(1001);
            assertThat(GeoErrorCode.COORDINATE_OUT_OF_RANGE.getCode()).isEqualTo(1002);
            assertThat(GeoErrorCode.TRANSFORM_FAILED.getCode()).isEqualTo(1003);
            assertThat(GeoErrorCode.NOT_IN_CHINA.getCode()).isEqualTo(1004);
        }

        @Test
        @DisplayName("GeoHash错误码存在")
        void testGeoHashErrorCodes() {
            assertThat(GeoErrorCode.INVALID_GEOHASH.getCode()).isEqualTo(2001);
            assertThat(GeoErrorCode.GEOHASH_ENCODE_FAILED.getCode()).isEqualTo(2002);
            assertThat(GeoErrorCode.GEOHASH_DECODE_FAILED.getCode()).isEqualTo(2003);
        }

        @Test
        @DisplayName("围栏错误码存在")
        void testFenceErrorCodes() {
            assertThat(GeoErrorCode.FENCE_NOT_FOUND.getCode()).isEqualTo(3001);
            assertThat(GeoErrorCode.INVALID_FENCE.getCode()).isEqualTo(3002);
            assertThat(GeoErrorCode.FENCE_CHECK_FAILED.getCode()).isEqualTo(3003);
            assertThat(GeoErrorCode.INSUFFICIENT_VERTICES.getCode()).isEqualTo(3004);
        }

        @Test
        @DisplayName("安全错误码存在")
        void testSecurityErrorCodes() {
            assertThat(GeoErrorCode.LOCATION_SPOOFING.getCode()).isEqualTo(4001);
            assertThat(GeoErrorCode.INVALID_TIMESTAMP.getCode()).isEqualTo(4002);
            assertThat(GeoErrorCode.IMPOSSIBLE_SPEED.getCode()).isEqualTo(4003);
        }

        @Test
        @DisplayName("values()返回所有枚举值")
        void testValues() {
            GeoErrorCode[] values = GeoErrorCode.values();
            assertThat(values).contains(
                GeoErrorCode.UNKNOWN,
                GeoErrorCode.INVALID_COORDINATE,
                GeoErrorCode.FENCE_NOT_FOUND,
                GeoErrorCode.LOCATION_SPOOFING
            );
        }
    }

    @Nested
    @DisplayName("getCode()测试")
    class GetCodeTests {

        @Test
        @DisplayName("错误码在正确范围内")
        void testCodeRanges() {
            // UNKNOWN = 0
            assertThat(GeoErrorCode.UNKNOWN.getCode()).isEqualTo(0);

            // Coordinate errors 1xxx
            assertThat(GeoErrorCode.INVALID_COORDINATE.getCode()).isBetween(1000, 1999);
            assertThat(GeoErrorCode.COORDINATE_OUT_OF_RANGE.getCode()).isBetween(1000, 1999);

            // GeoHash errors 2xxx
            assertThat(GeoErrorCode.INVALID_GEOHASH.getCode()).isBetween(2000, 2999);

            // Fence errors 3xxx
            assertThat(GeoErrorCode.FENCE_NOT_FOUND.getCode()).isBetween(3000, 3999);

            // Security errors 4xxx
            assertThat(GeoErrorCode.LOCATION_SPOOFING.getCode()).isBetween(4000, 4999);
        }
    }

    @Nested
    @DisplayName("getMessage()测试")
    class GetMessageTests {

        @Test
        @DisplayName("英文消息非空")
        void testEnglishMessageNotEmpty() {
            for (GeoErrorCode code : GeoErrorCode.values()) {
                assertThat(code.getMessage()).isNotNull().isNotEmpty();
            }
        }

        @Test
        @DisplayName("UNKNOWN消息正确")
        void testUnknownMessage() {
            assertThat(GeoErrorCode.UNKNOWN.getMessage()).isEqualTo("Unknown error");
        }

        @Test
        @DisplayName("INVALID_COORDINATE消息正确")
        void testInvalidCoordinateMessage() {
            assertThat(GeoErrorCode.INVALID_COORDINATE.getMessage()).isEqualTo("Invalid coordinate");
        }
    }

    @Nested
    @DisplayName("getMessageZh()测试")
    class GetMessageZhTests {

        @Test
        @DisplayName("中文消息非空")
        void testChineseMessageNotEmpty() {
            for (GeoErrorCode code : GeoErrorCode.values()) {
                assertThat(code.getMessageZh()).isNotNull().isNotEmpty();
            }
        }

        @Test
        @DisplayName("UNKNOWN中文消息正确")
        void testUnknownMessageZh() {
            assertThat(GeoErrorCode.UNKNOWN.getMessageZh()).isEqualTo("未知错误");
        }

        @Test
        @DisplayName("INVALID_COORDINATE中文消息正确")
        void testInvalidCoordinateMessageZh() {
            assertThat(GeoErrorCode.INVALID_COORDINATE.getMessageZh()).isEqualTo("无效坐标");
        }

        @Test
        @DisplayName("FENCE_NOT_FOUND中文消息正确")
        void testFenceNotFoundMessageZh() {
            assertThat(GeoErrorCode.FENCE_NOT_FOUND.getMessageZh()).isEqualTo("围栏不存在");
        }
    }

    @Nested
    @DisplayName("valueOf()测试")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf()返回正确枚举")
        void testValueOf() {
            assertThat(GeoErrorCode.valueOf("UNKNOWN")).isEqualTo(GeoErrorCode.UNKNOWN);
            assertThat(GeoErrorCode.valueOf("INVALID_COORDINATE")).isEqualTo(GeoErrorCode.INVALID_COORDINATE);
            assertThat(GeoErrorCode.valueOf("FENCE_NOT_FOUND")).isEqualTo(GeoErrorCode.FENCE_NOT_FOUND);
        }

        @Test
        @DisplayName("无效名称抛出异常")
        void testInvalidName() {
            assertThatThrownBy(() -> GeoErrorCode.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
