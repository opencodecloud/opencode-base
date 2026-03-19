package cloud.opencode.base.geo.geohash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoHash 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("GeoHash 测试")
class GeoHashTest {

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("GeoHashEncoder实现GeoHash接口")
        void testGeoHashEncoderImplementsInterface() {
            GeoHash geoHash = GeoHashEncoder.INSTANCE;

            assertThat(geoHash).isInstanceOf(GeoHash.class);
        }
    }

    @Nested
    @DisplayName("encode()接口测试")
    class EncodeInterfaceTests {

        @Test
        @DisplayName("通过接口调用encode")
        void testEncodeViaInterface() {
            GeoHash geoHash = GeoHashEncoder.INSTANCE;

            String hash = geoHash.encode(39.9042, 116.4074, 8);

            assertThat(hash).isNotNull();
            assertThat(hash).hasSize(8);
        }
    }

    @Nested
    @DisplayName("decode()接口测试")
    class DecodeInterfaceTests {

        @Test
        @DisplayName("通过接口调用decode")
        void testDecodeViaInterface() {
            GeoHash geoHash = GeoHashEncoder.INSTANCE;

            double[] coords = geoHash.decode("wx4g0bce");

            assertThat(coords).hasSize(2);
            assertThat(coords[0]).isBetween(-90.0, 90.0); // latitude
            assertThat(coords[1]).isBetween(-180.0, 180.0); // longitude
        }
    }

    @Nested
    @DisplayName("neighbors()接口测试")
    class NeighborsInterfaceTests {

        @Test
        @DisplayName("通过接口调用neighbors")
        void testNeighborsViaInterface() {
            GeoHash geoHash = GeoHashEncoder.INSTANCE;

            List<String> neighbors = geoHash.neighbors("wx4g0b");

            assertThat(neighbors).hasSize(8);
        }
    }

    @Nested
    @DisplayName("getBoundingBox()接口测试")
    class GetBoundingBoxInterfaceTests {

        @Test
        @DisplayName("通过接口调用getBoundingBox")
        void testGetBoundingBoxViaInterface() {
            GeoHash geoHash = GeoHashEncoder.INSTANCE;

            double[] bbox = geoHash.getBoundingBox("wx4g0b");

            assertThat(bbox).hasSize(4);
            assertThat(bbox[0]).isLessThan(bbox[2]); // minLat < maxLat
            assertThat(bbox[1]).isLessThan(bbox[3]); // minLng < maxLng
        }
    }

    @Nested
    @DisplayName("多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("可以使用GeoHash接口引用")
        void testPolymorphism() {
            GeoHash encoder = GeoHashEncoder.INSTANCE;

            String hash = encoder.encode(39.9042, 116.4074, 6);
            double[] decoded = encoder.decode(hash);
            List<String> neighbors = encoder.neighbors(hash);
            double[] bbox = encoder.getBoundingBox(hash);

            assertThat(hash).hasSize(6);
            assertThat(decoded).hasSize(2);
            assertThat(neighbors).hasSize(8);
            assertThat(bbox).hasSize(4);
        }
    }
}
