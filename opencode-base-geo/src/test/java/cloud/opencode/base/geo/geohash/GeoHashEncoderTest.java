package cloud.opencode.base.geo.geohash;

import cloud.opencode.base.geo.exception.InvalidGeoHashException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoHashEncoder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("GeoHashEncoder 测试")
class GeoHashEncoderTest {

    private final GeoHashEncoder encoder = GeoHashEncoder.INSTANCE;

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("INSTANCE不为null")
        void testInstanceNotNull() {
            assertThat(GeoHashEncoder.INSTANCE).isNotNull();
        }

        @Test
        @DisplayName("INSTANCE是同一实例")
        void testSameInstance() {
            assertThat(GeoHashEncoder.INSTANCE).isSameAs(encoder);
        }
    }

    @Nested
    @DisplayName("encode()测试")
    class EncodeTests {

        @Test
        @DisplayName("编码北京坐标")
        void testEncodeBeijing() {
            String hash = encoder.encode(39.9042, 116.4074, 8);

            assertThat(hash).isNotNull();
            assertThat(hash).hasSize(8);
        }

        @Test
        @DisplayName("不同精度返回不同长度")
        void testEncodeDifferentPrecision() {
            String hash4 = encoder.encode(39.9042, 116.4074, 4);
            String hash6 = encoder.encode(39.9042, 116.4074, 6);
            String hash8 = encoder.encode(39.9042, 116.4074, 8);

            assertThat(hash4).hasSize(4);
            assertThat(hash6).hasSize(6);
            assertThat(hash8).hasSize(8);
            assertThat(hash6).startsWith(hash4);
            assertThat(hash8).startsWith(hash6);
        }

        @Test
        @DisplayName("无效精度抛出异常")
        void testEncodeInvalidPrecision() {
            assertThatThrownBy(() -> encoder.encode(39.9042, 116.4074, -1))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> encoder.encode(39.9042, 116.4074, 0))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> encoder.encode(39.9042, 116.4074, 100))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("边界坐标编码")
        void testEncodeBoundary() {
            assertThat(encoder.encode(0, 0, 6)).isNotNull();
            assertThat(encoder.encode(-90, -180, 6)).isNotNull();
            assertThat(encoder.encode(90, 180, 6)).isNotNull();
        }
    }

    @Nested
    @DisplayName("decode()测试")
    class DecodeTests {

        @Test
        @DisplayName("解码GeoHash")
        void testDecode() {
            String hash = encoder.encode(39.9042, 116.4074, 8);
            double[] coords = encoder.decode(hash);

            assertThat(coords).hasSize(2);
            assertThat(coords[0]).isCloseTo(39.9042, within(0.001)); // latitude
            assertThat(coords[1]).isCloseTo(116.4074, within(0.001)); // longitude
        }

        @Test
        @DisplayName("null GeoHash抛出异常")
        void testDecodeNull() {
            assertThatThrownBy(() -> encoder.decode(null))
                .isInstanceOf(InvalidGeoHashException.class);
        }

        @Test
        @DisplayName("空GeoHash抛出异常")
        void testDecodeEmpty() {
            assertThatThrownBy(() -> encoder.decode(""))
                .isInstanceOf(InvalidGeoHashException.class);
        }

        @Test
        @DisplayName("无效字符抛出异常")
        void testDecodeInvalidChar() {
            assertThatThrownBy(() -> encoder.decode("wx4g0!"))
                .isInstanceOf(InvalidGeoHashException.class);
        }
    }

    @Nested
    @DisplayName("neighbors()测试")
    class NeighborsTests {

        @Test
        @DisplayName("获取8个相邻格子")
        void testNeighbors() {
            String hash = encoder.encode(39.9042, 116.4074, 6);
            List<String> neighbors = encoder.neighbors(hash);

            assertThat(neighbors).hasSize(8);
        }

        @Test
        @DisplayName("相邻格子长度相同")
        void testNeighborsSameLength() {
            String hash = encoder.encode(39.9042, 116.4074, 6);
            List<String> neighbors = encoder.neighbors(hash);

            for (String neighbor : neighbors) {
                assertThat(neighbor).hasSize(hash.length());
            }
        }

        @Test
        @DisplayName("相邻格子都不同")
        void testNeighborsAllDifferent() {
            String hash = encoder.encode(39.9042, 116.4074, 6);
            List<String> neighbors = encoder.neighbors(hash);

            assertThat(neighbors).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("null GeoHash抛出异常")
        void testNeighborsNull() {
            assertThatThrownBy(() -> encoder.neighbors(null))
                .isInstanceOf(InvalidGeoHashException.class);
        }

        @Test
        @DisplayName("空GeoHash抛出异常")
        void testNeighborsEmpty() {
            assertThatThrownBy(() -> encoder.neighbors(""))
                .isInstanceOf(InvalidGeoHashException.class);
        }
    }

    @Nested
    @DisplayName("getBoundingBox()测试")
    class GetBoundingBoxTests {

        @Test
        @DisplayName("获取边界框")
        void testGetBoundingBox() {
            String hash = encoder.encode(39.9042, 116.4074, 6);
            double[] bbox = encoder.getBoundingBox(hash);

            assertThat(bbox).hasSize(4);
            // [minLat, minLng, maxLat, maxLng]
            assertThat(bbox[0]).isLessThan(bbox[2]); // minLat < maxLat
            assertThat(bbox[1]).isLessThan(bbox[3]); // minLng < maxLng
        }

        @Test
        @DisplayName("边界框包含原始坐标")
        void testBoundingBoxContainsOriginal() {
            double lat = 39.9042;
            double lng = 116.4074;
            String hash = encoder.encode(lat, lng, 6);
            double[] bbox = encoder.getBoundingBox(hash);

            assertThat(lat).isBetween(bbox[0], bbox[2]);
            assertThat(lng).isBetween(bbox[1], bbox[3]);
        }

        @Test
        @DisplayName("null GeoHash抛出异常")
        void testGetBoundingBoxNull() {
            assertThatThrownBy(() -> encoder.getBoundingBox(null))
                .isInstanceOf(InvalidGeoHashException.class);
        }

        @Test
        @DisplayName("空GeoHash抛出异常")
        void testGetBoundingBoxEmpty() {
            assertThatThrownBy(() -> encoder.getBoundingBox(""))
                .isInstanceOf(InvalidGeoHashException.class);
        }

        @Test
        @DisplayName("无效字符抛出异常")
        void testGetBoundingBoxInvalidChar() {
            assertThatThrownBy(() -> encoder.getBoundingBox("wx4!"))
                .isInstanceOf(InvalidGeoHashException.class);
        }
    }

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("编码解码往返一致")
        void testEncodeDecodeRoundTrip() {
            double lat = 39.9042;
            double lng = 116.4074;

            String hash = encoder.encode(lat, lng, 12);
            double[] decoded = encoder.decode(hash);

            assertThat(decoded[0]).isCloseTo(lat, within(0.0001));
            assertThat(decoded[1]).isCloseTo(lng, within(0.0001));
        }
    }

    @Nested
    @DisplayName("GeoHash接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现GeoHash接口")
        void testImplementsInterface() {
            assertThat(encoder).isInstanceOf(GeoHash.class);
        }
    }
}
