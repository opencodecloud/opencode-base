package cloud.opencode.base.geo;

import cloud.opencode.base.geo.distance.DistanceCalculatorFactory.DistanceAccuracy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenGeo 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("OpenGeo 测试")
class OpenGeoTest {

    @Nested
    @DisplayName("距离计算测试")
    class DistanceTests {

        @Test
        @DisplayName("distance(Coordinate,Coordinate)计算两点距离")
        void testDistanceCoordinates() {
            Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate shanghai = Coordinate.wgs84(121.4737, 31.2304);

            double distance = OpenGeo.distance(beijing, shanghai);

            // 北京到上海约1068公里
            assertThat(distance).isGreaterThan(1000000);
            assertThat(distance).isLessThan(1200000);
        }

        @Test
        @DisplayName("distance(double,double,double,double)计算距离")
        void testDistanceValues() {
            double distance = OpenGeo.distance(116.4074, 39.9042, 121.4737, 31.2304);

            assertThat(distance).isGreaterThan(1000000);
            assertThat(distance).isLessThan(1200000);
        }

        @Test
        @DisplayName("null坐标返回0")
        void testDistanceNullCoordinate() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);

            assertThat(OpenGeo.distance(null, coord)).isEqualTo(0);
            assertThat(OpenGeo.distance(coord, null)).isEqualTo(0);
            assertThat(OpenGeo.distance(null, null)).isEqualTo(0);
        }

        @Test
        @DisplayName("distance()使用指定精度")
        void testDistanceWithAccuracy() {
            Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate shanghai = Coordinate.wgs84(121.4737, 31.2304);

            double fastDistance = OpenGeo.distance(beijing, shanghai, DistanceAccuracy.FAST);
            double preciseDistance = OpenGeo.distance(beijing, shanghai, DistanceAccuracy.PRECISE);

            // 两种算法结果应该接近
            assertThat(Math.abs(fastDistance - preciseDistance)).isLessThan(5000);
        }

        @Test
        @DisplayName("distancePrecise()使用Vincenty算法")
        void testDistancePrecise() {
            Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate shanghai = Coordinate.wgs84(121.4737, 31.2304);

            double distance = OpenGeo.distancePrecise(beijing, shanghai);

            assertThat(distance).isGreaterThan(1000000);
        }
    }

    @Nested
    @DisplayName("formatDistance()测试")
    class FormatDistanceTests {

        @Test
        @DisplayName("小于1000米显示为米")
        void testFormatMeters() {
            assertThat(OpenGeo.formatDistance(500)).isEqualTo("500米");
            assertThat(OpenGeo.formatDistance(999)).isEqualTo("999米");
        }

        @Test
        @DisplayName("大于等于1000米显示为公里")
        void testFormatKilometers() {
            assertThat(OpenGeo.formatDistance(1000)).isEqualTo("1.0公里");
            assertThat(OpenGeo.formatDistance(1500)).isEqualTo("1.5公里");
            assertThat(OpenGeo.formatDistance(10000)).isEqualTo("10.0公里");
        }
    }

    @Nested
    @DisplayName("坐标转换测试")
    class CoordinateTransformTests {

        @Test
        @DisplayName("wgs84ToGcj02()转换")
        void testWgs84ToGcj02() {
            Coordinate result = OpenGeo.wgs84ToGcj02(116.4074, 39.9042);

            assertThat(result.system()).isEqualTo(CoordinateSystem.GCJ02);
        }

        @Test
        @DisplayName("gcj02ToBd09()转换")
        void testGcj02ToBd09() {
            Coordinate result = OpenGeo.gcj02ToBd09(116.4074, 39.9042);

            assertThat(result.system()).isEqualTo(CoordinateSystem.BD09);
        }

        @Test
        @DisplayName("bd09ToGcj02()转换")
        void testBd09ToGcj02() {
            Coordinate result = OpenGeo.bd09ToGcj02(116.4074, 39.9042);

            assertThat(result.system()).isEqualTo(CoordinateSystem.GCJ02);
        }

        @Test
        @DisplayName("wgs84ToBd09()转换")
        void testWgs84ToBd09() {
            Coordinate result = OpenGeo.wgs84ToBd09(116.4074, 39.9042);

            assertThat(result.system()).isEqualTo(CoordinateSystem.BD09);
        }

        @Test
        @DisplayName("bd09ToWgs84()转换")
        void testBd09ToWgs84() {
            Coordinate result = OpenGeo.bd09ToWgs84(116.4074, 39.9042);

            assertThat(result.system()).isEqualTo(CoordinateSystem.WGS84);
        }

        @Test
        @DisplayName("gcj02ToWgs84()转换")
        void testGcj02ToWgs84() {
            Coordinate result = OpenGeo.gcj02ToWgs84(116.4074, 39.9042);

            assertThat(result.system()).isEqualTo(CoordinateSystem.WGS84);
        }
    }

    @Nested
    @DisplayName("地理围栏测试")
    class GeoFenceTests {

        @Test
        @DisplayName("inCircle()检测圆形围栏内")
        void testInCircle() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate inside = Coordinate.wgs84(116.4080, 39.9045);
            Coordinate outside = Coordinate.wgs84(117.0, 40.0);

            assertThat(OpenGeo.inCircle(inside, center, 1000)).isTrue();
            assertThat(OpenGeo.inCircle(outside, center, 1000)).isFalse();
        }

        @Test
        @DisplayName("inPolygon()检测多边形围栏内")
        void testInPolygon() {
            List<Coordinate> polygon = List.of(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 39.0),
                Coordinate.wgs84(117.0, 40.0),
                Coordinate.wgs84(116.0, 40.0)
            );
            Coordinate inside = Coordinate.wgs84(116.5, 39.5);
            Coordinate outside = Coordinate.wgs84(118.0, 39.5);

            assertThat(OpenGeo.inPolygon(inside, polygon)).isTrue();
            assertThat(OpenGeo.inPolygon(outside, polygon)).isFalse();
        }

        @Test
        @DisplayName("inPolygon()处理null和不足顶点")
        void testInPolygonEdgeCases() {
            assertThat(OpenGeo.inPolygon(null, List.of())).isFalse();
            assertThat(OpenGeo.inPolygon(Coordinate.wgs84(116, 39), null)).isFalse();
            assertThat(OpenGeo.inPolygon(Coordinate.wgs84(116, 39), List.of())).isFalse();
        }

        @Test
        @DisplayName("inRectangle()检测矩形围栏内")
        void testInRectangle() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            Coordinate inside = Coordinate.wgs84(116.5, 39.5);
            Coordinate outside = Coordinate.wgs84(118.0, 39.5);

            assertThat(OpenGeo.inRectangle(inside, sw, ne)).isTrue();
            assertThat(OpenGeo.inRectangle(outside, sw, ne)).isFalse();
        }

        @Test
        @DisplayName("inRectangle()处理null")
        void testInRectangleNull() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);

            assertThat(OpenGeo.inRectangle(null, sw, ne)).isFalse();
            assertThat(OpenGeo.inRectangle(Coordinate.wgs84(116, 39), null, ne)).isFalse();
            assertThat(OpenGeo.inRectangle(Coordinate.wgs84(116, 39), sw, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("GeoHash测试")
    class GeoHashTests {

        @Test
        @DisplayName("geoHash()编码")
        void testGeoHash() {
            String hash = OpenGeo.geoHash(116.4074, 39.9042, 8);

            assertThat(hash).isNotNull();
            assertThat(hash).hasSize(8);
        }

        @Test
        @DisplayName("fromGeoHash()解码")
        void testFromGeoHash() {
            String hash = OpenGeo.geoHash(116.4074, 39.9042, 8);
            Coordinate decoded = OpenGeo.fromGeoHash(hash);

            assertThat(decoded.system()).isEqualTo(CoordinateSystem.WGS84);
            assertThat(decoded.longitude()).isCloseTo(116.4074, within(0.001));
            assertThat(decoded.latitude()).isCloseTo(39.9042, within(0.001));
        }

        @Test
        @DisplayName("geoHashNeighbors()获取相邻")
        void testGeoHashNeighbors() {
            String hash = OpenGeo.geoHash(116.4074, 39.9042, 6);
            List<String> neighbors = OpenGeo.geoHashNeighbors(hash);

            assertThat(neighbors).hasSize(8);
        }

        @Test
        @DisplayName("geoHashBoundingBox()获取边界框")
        void testGeoHashBoundingBox() {
            String hash = OpenGeo.geoHash(116.4074, 39.9042, 6);
            double[] bbox = OpenGeo.geoHashBoundingBox(hash);

            assertThat(bbox).hasSize(4);
            // [minLat, minLng, maxLat, maxLng]
            assertThat(bbox[0]).isLessThan(bbox[2]); // minLat < maxLat
            assertThat(bbox[1]).isLessThan(bbox[3]); // minLng < maxLng
        }
    }

    @Nested
    @DisplayName("isInChina()测试")
    class IsInChinaTests {

        @Test
        @DisplayName("北京在中国境内")
        void testBeijingInChina() {
            assertThat(OpenGeo.isInChina(116.4074, 39.9042)).isTrue();
        }

        @Test
        @DisplayName("上海在中国境内")
        void testShanghaiInChina() {
            assertThat(OpenGeo.isInChina(121.4737, 31.2304)).isTrue();
        }

        @Test
        @DisplayName("东京不在中国境内")
        void testTokyoNotInChina() {
            assertThat(OpenGeo.isInChina(139.6917, 35.6895)).isFalse();
        }
    }

    @Nested
    @DisplayName("bearing()方位角测试")
    class BearingTests {

        @Test
        @DisplayName("计算方位角")
        void testBearing() {
            Coordinate from = Coordinate.wgs84(0, 0);
            Coordinate toNorth = Coordinate.wgs84(0, 1);
            Coordinate toEast = Coordinate.wgs84(1, 0);

            // 向北约0度
            double bearingNorth = OpenGeo.bearing(from, toNorth);
            assertThat(bearingNorth).isCloseTo(0, within(1.0));

            // 向东约90度
            double bearingEast = OpenGeo.bearing(from, toEast);
            assertThat(bearingEast).isCloseTo(90, within(1.0));
        }

        @Test
        @DisplayName("null坐标返回0")
        void testBearingNull() {
            Coordinate coord = Coordinate.wgs84(0, 0);

            assertThat(OpenGeo.bearing(null, coord)).isEqualTo(0);
            assertThat(OpenGeo.bearing(coord, null)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("destination()目标点计算测试")
    class DestinationTests {

        @Test
        @DisplayName("计算目标点")
        void testDestination() {
            Coordinate start = Coordinate.wgs84(0, 0);

            // 向北移动111公里（约1度）
            Coordinate dest = OpenGeo.destination(start, 111000, 0);

            assertThat(dest.latitude()).isCloseTo(1.0, within(0.1));
        }

        @Test
        @DisplayName("null起点返回null")
        void testDestinationNull() {
            assertThat(OpenGeo.destination(null, 1000, 0)).isNull();
        }
    }

    @Nested
    @DisplayName("midpoint()中点计算测试")
    class MidpointTests {

        @Test
        @DisplayName("计算中点")
        void testMidpoint() {
            Coordinate c1 = Coordinate.wgs84(0, 0);
            Coordinate c2 = Coordinate.wgs84(2, 2);

            Coordinate mid = OpenGeo.midpoint(c1, c2);

            assertThat(mid.longitude()).isCloseTo(1.0, within(0.1));
            assertThat(mid.latitude()).isCloseTo(1.0, within(0.1));
        }

        @Test
        @DisplayName("null坐标返回null")
        void testMidpointNull() {
            Coordinate coord = Coordinate.wgs84(0, 0);

            assertThat(OpenGeo.midpoint(null, coord)).isNull();
            assertThat(OpenGeo.midpoint(coord, null)).isNull();
        }
    }

    @Nested
    @DisplayName("isValidCoordinate()测试")
    class IsValidCoordinateTests {

        @Test
        @DisplayName("有效坐标返回true")
        void testValidCoordinate() {
            assertThat(OpenGeo.isValidCoordinate(116.4074, 39.9042)).isTrue();
            assertThat(OpenGeo.isValidCoordinate(-180, -90)).isTrue();
            assertThat(OpenGeo.isValidCoordinate(180, 90)).isTrue();
        }

        @Test
        @DisplayName("越界坐标返回false")
        void testInvalidCoordinate() {
            assertThat(OpenGeo.isValidCoordinate(200, 39)).isFalse();
            assertThat(OpenGeo.isValidCoordinate(116, 100)).isFalse();
        }

        @Test
        @DisplayName("NaN和无穷返回false")
        void testNanInfinite() {
            assertThat(OpenGeo.isValidCoordinate(Double.NaN, 39)).isFalse();
            assertThat(OpenGeo.isValidCoordinate(116, Double.POSITIVE_INFINITY)).isFalse();
        }
    }

    @Nested
    @DisplayName("EARTH_RADIUS常量测试")
    class EarthRadiusTests {

        @Test
        @DisplayName("地球半径常量正确")
        void testEarthRadius() {
            assertThat(OpenGeo.EARTH_RADIUS).isEqualTo(6371000.0);
        }
    }
}
