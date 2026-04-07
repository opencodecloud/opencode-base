package cloud.opencode.base.geo.polyline;

import cloud.opencode.base.geo.Coordinate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PolylineCodec 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.3
 */
@DisplayName("PolylineCodec 测试")
class PolylineCodecTest {

    @Nested
    @DisplayName("encode()测试")
    class EncodeTests {

        @Test
        @DisplayName("Google官方示例编码正确")
        void testGoogleExample() {
            // Google's example: (38.5, -120.2), (40.7, -120.95), (43.252, -126.453)
            List<Coordinate> coords = List.of(
                    Coordinate.wgs84(-120.2, 38.5),
                    Coordinate.wgs84(-120.95, 40.7),
                    Coordinate.wgs84(-126.453, 43.252)
            );

            String encoded = PolylineCodec.encode(coords);
            assertThat(encoded).isNotNull().isNotEmpty();

            // Verify round-trip
            List<Coordinate> decoded = PolylineCodec.decode(encoded);
            assertThat(decoded).hasSize(3);
            assertThat(decoded.get(0).latitude()).isCloseTo(38.5, within(0.00001));
            assertThat(decoded.get(0).longitude()).isCloseTo(-120.2, within(0.00001));
        }

        @Test
        @DisplayName("null输入返回空字符串")
        void testNullInput() {
            assertThat(PolylineCodec.encode(null)).isEmpty();
        }

        @Test
        @DisplayName("空列表返回空字符串")
        void testEmptyInput() {
            assertThat(PolylineCodec.encode(Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("单点编码")
        void testSinglePoint() {
            List<Coordinate> coords = List.of(Coordinate.wgs84(0.0, 0.0));
            String encoded = PolylineCodec.encode(coords);
            assertThat(encoded).isNotNull().isNotEmpty();

            List<Coordinate> decoded = PolylineCodec.decode(encoded);
            assertThat(decoded).hasSize(1);
            assertThat(decoded.getFirst().latitude()).isCloseTo(0.0, within(0.00001));
            assertThat(decoded.getFirst().longitude()).isCloseTo(0.0, within(0.00001));
        }

        @Test
        @DisplayName("负坐标编码正确")
        void testNegativeCoordinates() {
            List<Coordinate> coords = List.of(
                    Coordinate.wgs84(-73.9857, 40.7484),
                    Coordinate.wgs84(-0.1278, 51.5074)
            );
            String encoded = PolylineCodec.encode(coords);
            List<Coordinate> decoded = PolylineCodec.decode(encoded);

            assertThat(decoded).hasSize(2);
            assertThat(decoded.get(0).latitude()).isCloseTo(40.7484, within(0.00001));
            assertThat(decoded.get(0).longitude()).isCloseTo(-73.9857, within(0.00001));
            assertThat(decoded.get(1).latitude()).isCloseTo(51.5074, within(0.00001));
            assertThat(decoded.get(1).longitude()).isCloseTo(-0.1278, within(0.00001));
        }
    }

    @Nested
    @DisplayName("encode(coords, precision)自定义精度测试")
    class CustomPrecisionEncodeTests {

        @Test
        @DisplayName("精度6编码解码往返")
        void testPrecision6RoundTrip() {
            List<Coordinate> coords = List.of(
                    Coordinate.wgs84(116.4074, 39.9042),
                    Coordinate.wgs84(121.4737, 31.2304)
            );
            String encoded = PolylineCodec.encode(coords, 6);
            List<Coordinate> decoded = PolylineCodec.decode(encoded, 6);

            assertThat(decoded).hasSize(2);
            assertThat(decoded.get(0).latitude()).isCloseTo(39.9042, within(0.000001));
            assertThat(decoded.get(0).longitude()).isCloseTo(116.4074, within(0.000001));
        }

        @Test
        @DisplayName("无效精度抛出异常")
        void testInvalidPrecision() {
            List<Coordinate> coords = List.of(Coordinate.wgs84(0, 0));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> PolylineCodec.encode(coords, 0));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> PolylineCodec.encode(coords, 11));
        }
    }

    @Nested
    @DisplayName("decode()测试")
    class DecodeTests {

        @Test
        @DisplayName("null输入返回空列表")
        void testNullInput() {
            assertThat(PolylineCodec.decode(null)).isEmpty();
        }

        @Test
        @DisplayName("空字符串返回空列表")
        void testEmptyInput() {
            assertThat(PolylineCodec.decode("")).isEmpty();
        }

        @Test
        @DisplayName("超长字符串抛出异常")
        void testTooLongInput() {
            String longStr = "?".repeat(1_000_001);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> PolylineCodec.decode(longStr))
                    .withMessageContaining("maximum length");
        }

        @Test
        @DisplayName("格式错误的字符串抛出异常")
        void testMalformedString() {
            // Single encoded value without a pair (latitude without longitude)
            // Using a minimal valid single-value encoding that ends prematurely
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> PolylineCodec.decode("?"));
        }

        @Test
        @DisplayName("无效精度抛出异常")
        void testInvalidPrecision() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> PolylineCodec.decode("??", 0));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> PolylineCodec.decode("??", 11));
        }
    }

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("多点编码解码一致")
        void testMultiPointRoundTrip() {
            List<Coordinate> original = List.of(
                    Coordinate.wgs84(116.4074, 39.9042),
                    Coordinate.wgs84(121.4737, 31.2304),
                    Coordinate.wgs84(113.2644, 23.1291),
                    Coordinate.wgs84(104.0665, 30.5728)
            );

            String encoded = PolylineCodec.encode(original);
            List<Coordinate> decoded = PolylineCodec.decode(encoded);

            assertThat(decoded).hasSize(original.size());
            for (int i = 0; i < original.size(); i++) {
                assertThat(decoded.get(i).latitude())
                        .isCloseTo(original.get(i).latitude(), within(0.00001));
                assertThat(decoded.get(i).longitude())
                        .isCloseTo(original.get(i).longitude(), within(0.00001));
            }
        }

        @Test
        @DisplayName("极端坐标编码解码一致")
        void testExtremeCoordinatesRoundTrip() {
            List<Coordinate> coords = List.of(
                    Coordinate.wgs84(-180.0, -90.0),
                    Coordinate.wgs84(180.0, 90.0),
                    Coordinate.wgs84(0.0, 0.0)
            );

            String encoded = PolylineCodec.encode(coords);
            List<Coordinate> decoded = PolylineCodec.decode(encoded);

            assertThat(decoded).hasSize(3);
            assertThat(decoded.get(0).latitude()).isCloseTo(-90.0, within(0.00001));
            assertThat(decoded.get(0).longitude()).isCloseTo(-180.0, within(0.00001));
            assertThat(decoded.get(1).latitude()).isCloseTo(90.0, within(0.00001));
            assertThat(decoded.get(1).longitude()).isCloseTo(180.0, within(0.00001));
        }

        @Test
        @DisplayName("大量点编码解码一致")
        void testManyPointsRoundTrip() {
            List<Coordinate> original = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                double lat = -90 + (180.0 * i / 999);
                double lng = -180 + (360.0 * i / 999);
                original.add(Coordinate.wgs84(lng, lat));
            }

            String encoded = PolylineCodec.encode(original);
            List<Coordinate> decoded = PolylineCodec.decode(encoded);

            assertThat(decoded).hasSize(1000);
            for (int i = 0; i < original.size(); i++) {
                assertThat(decoded.get(i).latitude())
                        .isCloseTo(original.get(i).latitude(), within(0.00001));
                assertThat(decoded.get(i).longitude())
                        .isCloseTo(original.get(i).longitude(), within(0.00001));
            }
        }
    }

    @Nested
    @DisplayName("已知编码测试")
    class KnownEncodingTests {

        @Test
        @DisplayName("Google文档示例编码验证")
        void testGoogleDocExample() {
            // The Google polyline algorithm documentation example:
            // Encoding -179.9832104 gives: `~ps|U`
            // We test the full 3-point example for round-trip correctness
            List<Coordinate> coords = List.of(
                    Coordinate.wgs84(-120.2, 38.5),
                    Coordinate.wgs84(-120.95, 40.7),
                    Coordinate.wgs84(-126.453, 43.252)
            );

            String encoded = PolylineCodec.encode(coords);
            // The expected encoding from Google docs: _p~iF~ps|U_ulLnnqC_mqNvxq`@
            assertThat(encoded).isEqualTo("_p~iF~ps|U_ulLnnqC_mqNvxq`@");
        }
    }
}
