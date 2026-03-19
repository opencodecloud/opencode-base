package cloud.opencode.base.geo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Coordinate 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("Coordinate 测试")
class CoordinateTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("wgs84()创建WGS84坐标")
        void testWgs84Factory() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);

            assertThat(coord.longitude()).isEqualTo(116.4074);
            assertThat(coord.latitude()).isEqualTo(39.9042);
            assertThat(coord.system()).isEqualTo(CoordinateSystem.WGS84);
        }

        @Test
        @DisplayName("gcj02()创建GCJ02坐标")
        void testGcj02Factory() {
            Coordinate coord = Coordinate.gcj02(116.4074, 39.9042);

            assertThat(coord.longitude()).isEqualTo(116.4074);
            assertThat(coord.latitude()).isEqualTo(39.9042);
            assertThat(coord.system()).isEqualTo(CoordinateSystem.GCJ02);
        }

        @Test
        @DisplayName("bd09()创建BD09坐标")
        void testBd09Factory() {
            Coordinate coord = Coordinate.bd09(116.4074, 39.9042);

            assertThat(coord.longitude()).isEqualTo(116.4074);
            assertThat(coord.latitude()).isEqualTo(39.9042);
            assertThat(coord.system()).isEqualTo(CoordinateSystem.BD09);
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("equals()正确比较")
        void testEquals() {
            Coordinate coord1 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate coord2 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate coord3 = Coordinate.wgs84(116.4074, 39.9043);

            assertThat(coord1).isEqualTo(coord2);
            assertThat(coord1).isNotEqualTo(coord3);
        }

        @Test
        @DisplayName("hashCode()一致性")
        void testHashCode() {
            Coordinate coord1 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate coord2 = Coordinate.wgs84(116.4074, 39.9042);

            assertThat(coord1.hashCode()).isEqualTo(coord2.hashCode());
        }

        @Test
        @DisplayName("toString()包含坐标信息")
        void testToString() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);
            String str = coord.toString();

            assertThat(str).contains("116.4074");
            assertThat(str).contains("39.9042");
            assertThat(str).contains("WGS84");
        }
    }

    @Nested
    @DisplayName("to()坐标转换测试")
    class ToTransformTests {

        @Test
        @DisplayName("相同坐标系返回相同坐标")
        void testToSameSystem() {
            Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate result = wgs84.to(CoordinateSystem.WGS84);

            assertThat(result).isEqualTo(wgs84);
        }

        @Test
        @DisplayName("WGS84转GCJ02")
        void testWgs84ToGcj02() {
            Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate gcj02 = wgs84.to(CoordinateSystem.GCJ02);

            assertThat(gcj02.system()).isEqualTo(CoordinateSystem.GCJ02);
            // 转换后坐标会有偏移
            assertThat(gcj02.longitude()).isNotEqualTo(wgs84.longitude());
        }

        @Test
        @DisplayName("WGS84转BD09")
        void testWgs84ToBd09() {
            Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate bd09 = wgs84.to(CoordinateSystem.BD09);

            assertThat(bd09.system()).isEqualTo(CoordinateSystem.BD09);
        }
    }

    @Nested
    @DisplayName("distanceTo()距离计算测试")
    class DistanceToTests {

        @Test
        @DisplayName("计算两点距离")
        void testDistanceTo() {
            Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate shanghai = Coordinate.wgs84(121.4737, 31.2304);

            double distance = beijing.distanceTo(shanghai);

            // 北京到上海约1000公里
            assertThat(distance).isGreaterThan(1000000);
            assertThat(distance).isLessThan(1200000);
        }

        @Test
        @DisplayName("相同点距离为0")
        void testDistanceToSamePoint() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);

            assertThat(coord.distanceTo(coord)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("toGeoHash()测试")
    class ToGeoHashTests {

        @Test
        @DisplayName("编码为GeoHash")
        void testToGeoHash() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);

            String hash = coord.toGeoHash(8);

            assertThat(hash).isNotNull();
            assertThat(hash).hasSize(8);
        }

        @Test
        @DisplayName("不同精度返回不同长度")
        void testDifferentPrecision() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);

            String hash4 = coord.toGeoHash(4);
            String hash8 = coord.toGeoHash(8);

            assertThat(hash4).hasSize(4);
            assertThat(hash8).hasSize(8);
            assertThat(hash8).startsWith(hash4);
        }
    }

    @Nested
    @DisplayName("isValid()测试")
    class IsValidTests {

        @Test
        @DisplayName("有效坐标返回true")
        void testValidCoordinate() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);

            assertThat(coord.isValid()).isTrue();
        }

        @Test
        @DisplayName("边界值有效")
        void testBoundaryValues() {
            assertThat(Coordinate.wgs84(-180, -90).isValid()).isTrue();
            assertThat(Coordinate.wgs84(180, 90).isValid()).isTrue();
            assertThat(Coordinate.wgs84(0, 0).isValid()).isTrue();
        }

        @Test
        @DisplayName("经度越界返回false")
        void testInvalidLongitude() {
            Coordinate coord = new Coordinate(200, 39.9042, CoordinateSystem.WGS84);
            assertThat(coord.isValid()).isFalse();
        }

        @Test
        @DisplayName("纬度越界返回false")
        void testInvalidLatitude() {
            Coordinate coord = new Coordinate(116, 100, CoordinateSystem.WGS84);
            assertThat(coord.isValid()).isFalse();
        }

        @Test
        @DisplayName("NaN坐标返回false")
        void testNanCoordinate() {
            Coordinate coord = new Coordinate(Double.NaN, 39.9042, CoordinateSystem.WGS84);
            assertThat(coord.isValid()).isFalse();
        }

        @Test
        @DisplayName("无穷坐标返回false")
        void testInfiniteCoordinate() {
            Coordinate coord = new Coordinate(Double.POSITIVE_INFINITY, 39.9042, CoordinateSystem.WGS84);
            assertThat(coord.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("isInChina()测试")
    class IsInChinaTests {

        @Test
        @DisplayName("北京在中国境内")
        void testBeijingInChina() {
            Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);

            assertThat(beijing.isInChina()).isTrue();
        }

        @Test
        @DisplayName("东京不在中国境内")
        void testTokyoNotInChina() {
            Coordinate tokyo = Coordinate.wgs84(139.6917, 35.6895);

            assertThat(tokyo.isInChina()).isFalse();
        }

        @Test
        @DisplayName("纽约不在中国境内")
        void testNewYorkNotInChina() {
            Coordinate newYork = Coordinate.wgs84(-74.006, 40.7128);

            assertThat(newYork.isInChina()).isFalse();
        }
    }
}
