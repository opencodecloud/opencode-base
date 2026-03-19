package cloud.opencode.base.geo.geohash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoHashUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("GeoHashUtil 测试")
class GeoHashUtilTest {

    @Nested
    @DisplayName("encode()测试")
    class EncodeTests {

        @Test
        @DisplayName("encode(lat, lng, precision)编码")
        void testEncodeWithPrecision() {
            String hash = GeoHashUtil.encode(39.9042, 116.4074, 8);

            assertThat(hash).isNotNull();
            assertThat(hash).hasSize(8);
        }

        @Test
        @DisplayName("encode(lat, lng)使用默认精度8")
        void testEncodeDefaultPrecision() {
            String hash = GeoHashUtil.encode(39.9042, 116.4074);

            assertThat(hash).hasSize(8);
        }

        @Test
        @DisplayName("相同坐标编码相同")
        void testEncodeSameCoordinates() {
            String hash1 = GeoHashUtil.encode(39.9042, 116.4074, 6);
            String hash2 = GeoHashUtil.encode(39.9042, 116.4074, 6);

            assertThat(hash1).isEqualTo(hash2);
        }
    }

    @Nested
    @DisplayName("decode()测试")
    class DecodeTests {

        @Test
        @DisplayName("解码GeoHash")
        void testDecode() {
            String hash = GeoHashUtil.encode(39.9042, 116.4074, 8);
            double[] coords = GeoHashUtil.decode(hash);

            assertThat(coords).hasSize(2);
            assertThat(coords[0]).isCloseTo(39.9042, within(0.001));
            assertThat(coords[1]).isCloseTo(116.4074, within(0.001));
        }
    }

    @Nested
    @DisplayName("neighbors()测试")
    class NeighborsTests {

        @Test
        @DisplayName("获取8个相邻格子")
        void testNeighbors() {
            String hash = GeoHashUtil.encode(39.9042, 116.4074, 6);
            List<String> neighbors = GeoHashUtil.neighbors(hash);

            assertThat(neighbors).hasSize(8);
        }
    }

    @Nested
    @DisplayName("getBoundingBox()测试")
    class GetBoundingBoxTests {

        @Test
        @DisplayName("获取边界框")
        void testGetBoundingBox() {
            String hash = GeoHashUtil.encode(39.9042, 116.4074, 6);
            double[] bbox = GeoHashUtil.getBoundingBox(hash);

            assertThat(bbox).hasSize(4);
        }
    }

    @Nested
    @DisplayName("getPrecisionForRadius()测试")
    class GetPrecisionForRadiusTests {

        @Test
        @DisplayName("大半径返回低精度")
        void testLargeRadius() {
            assertThat(GeoHashUtil.getPrecisionForRadius(100)).isEqualTo(3);
            assertThat(GeoHashUtil.getPrecisionForRadius(150)).isEqualTo(3);
        }

        @Test
        @DisplayName("中等半径返回中等精度")
        void testMediumRadius() {
            assertThat(GeoHashUtil.getPrecisionForRadius(20)).isEqualTo(4);
            assertThat(GeoHashUtil.getPrecisionForRadius(5)).isEqualTo(5);
            assertThat(GeoHashUtil.getPrecisionForRadius(1)).isEqualTo(6);
        }

        @Test
        @DisplayName("小半径返回高精度")
        void testSmallRadius() {
            assertThat(GeoHashUtil.getPrecisionForRadius(0.1)).isEqualTo(7);
            assertThat(GeoHashUtil.getPrecisionForRadius(0.05)).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("isValid()测试")
    class IsValidTests {

        @Test
        @DisplayName("有效GeoHash返回true")
        void testValidGeoHash() {
            String hash = GeoHashUtil.encode(39.9042, 116.4074, 6);

            assertThat(GeoHashUtil.isValid(hash)).isTrue();
        }

        @Test
        @DisplayName("null返回false")
        void testNullGeoHash() {
            assertThat(GeoHashUtil.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("空字符串返回false")
        void testEmptyGeoHash() {
            assertThat(GeoHashUtil.isValid("")).isFalse();
        }

        @Test
        @DisplayName("超长返回false")
        void testTooLongGeoHash() {
            assertThat(GeoHashUtil.isValid("wx4g0bcewx4g0")).isFalse();
        }

        @Test
        @DisplayName("无效字符返回false")
        void testInvalidCharGeoHash() {
            assertThat(GeoHashUtil.isValid("wx4g0!")).isFalse();
            assertThat(GeoHashUtil.isValid("wx4goa")).isFalse(); // 'a' is not valid
            assertThat(GeoHashUtil.isValid("wx4goi")).isFalse(); // 'i' is not valid
            assertThat(GeoHashUtil.isValid("wx4gol")).isFalse(); // 'l' is not valid
        }

        @Test
        @DisplayName("Base32字符有效")
        void testValidBase32() {
            // Valid characters: 0123456789bcdefghjkmnpqrstuvwxyz
            assertThat(GeoHashUtil.isValid("0123456789")).isTrue();
            assertThat(GeoHashUtil.isValid("bcdefghjkm")).isTrue();
            assertThat(GeoHashUtil.isValid("npqrstuvwx")).isTrue();
            assertThat(GeoHashUtil.isValid("yz")).isTrue();
        }
    }

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("编码解码一致")
        void testRoundTrip() {
            double lat = 39.9042;
            double lng = 116.4074;

            String hash = GeoHashUtil.encode(lat, lng, 12);
            double[] decoded = GeoHashUtil.decode(hash);

            assertThat(decoded[0]).isCloseTo(lat, within(0.0001));
            assertThat(decoded[1]).isCloseTo(lng, within(0.0001));
        }
    }
}
