package cloud.opencode.base.geo.wkt;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.exception.GeoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * WktCodec 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.3
 */
@DisplayName("WktCodec 测试")
class WktCodecTest {

    @Nested
    @DisplayName("parsePoint 解析测试")
    class ParsePointTests {

        @Test
        @DisplayName("解析标准 POINT 格式")
        void testParseStandardPoint() {
            Coordinate c = WktCodec.parsePoint("POINT(116.4074 39.9042)");
            assertThat(c.longitude()).isCloseTo(116.4074, within(0.0001));
            assertThat(c.latitude()).isCloseTo(39.9042, within(0.0001));
        }

        @Test
        @DisplayName("解析带空格的 POINT 格式")
        void testParsePointWithSpace() {
            Coordinate c = WktCodec.parsePoint("POINT (116.4074 39.9042)");
            assertThat(c.longitude()).isCloseTo(116.4074, within(0.0001));
            assertThat(c.latitude()).isCloseTo(39.9042, within(0.0001));
        }

        @Test
        @DisplayName("解析大小写不敏感的 POINT")
        void testParsePointCaseInsensitive() {
            Coordinate c = WktCodec.parsePoint("point(1.0 2.0)");
            assertThat(c.longitude()).isCloseTo(1.0, within(0.0001));
            assertThat(c.latitude()).isCloseTo(2.0, within(0.0001));
        }

        @Test
        @DisplayName("解析负坐标 POINT")
        void testParsePointNegativeCoords() {
            Coordinate c = WktCodec.parsePoint("POINT(-73.9857 40.7484)");
            assertThat(c.longitude()).isCloseTo(-73.9857, within(0.0001));
            assertThat(c.latitude()).isCloseTo(40.7484, within(0.0001));
        }

        @Test
        @DisplayName("解析整数坐标 POINT")
        void testParsePointIntegerCoords() {
            Coordinate c = WktCodec.parsePoint("POINT(0 0)");
            assertThat(c.longitude()).isEqualTo(0.0);
            assertThat(c.latitude()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("null 输入抛出 GeoException")
        void testParsePointNull() {
            assertThatThrownBy(() -> WktCodec.parsePoint(null))
                .isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("无效类型抛出 GeoException")
        void testParsePointWrongType() {
            assertThatThrownBy(() -> WktCodec.parsePoint("LINESTRING(0 0, 1 1)"))
                .isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("空坐标体抛出 GeoException")
        void testParsePointEmptyBody() {
            assertThatThrownBy(() -> WktCodec.parsePoint("POINT()"))
                .isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("缺少括号抛出 GeoException")
        void testParsePointMissingParens() {
            assertThatThrownBy(() -> WktCodec.parsePoint("POINT 1 2"))
                .isInstanceOf(GeoException.class);
        }
    }

    @Nested
    @DisplayName("parseLineString 解析测试")
    class ParseLineStringTests {

        @Test
        @DisplayName("解析标准 LINESTRING")
        void testParseStandardLineString() {
            List<Coordinate> coords = WktCodec.parseLineString("LINESTRING(0 0, 1 1, 2 2)");
            assertThat(coords).hasSize(3);
            assertThat(coords.get(0).longitude()).isEqualTo(0.0);
            assertThat(coords.get(1).longitude()).isCloseTo(1.0, within(0.0001));
            assertThat(coords.get(2).latitude()).isCloseTo(2.0, within(0.0001));
        }

        @Test
        @DisplayName("解析带空格的 LINESTRING")
        void testParseLineStringWithSpaces() {
            List<Coordinate> coords = WktCodec.parseLineString("LINESTRING (0 0, 1 1)");
            assertThat(coords).hasSize(2);
        }

        @Test
        @DisplayName("解析带负坐标的 LINESTRING")
        void testParseLineStringNegative() {
            List<Coordinate> coords = WktCodec.parseLineString("LINESTRING(-1.5 -2.5, 3.5 4.5)");
            assertThat(coords).hasSize(2);
            assertThat(coords.get(0).longitude()).isCloseTo(-1.5, within(0.0001));
            assertThat(coords.get(0).latitude()).isCloseTo(-2.5, within(0.0001));
        }

        @Test
        @DisplayName("null 输入抛出 GeoException")
        void testParseLineStringNull() {
            assertThatThrownBy(() -> WktCodec.parseLineString(null))
                .isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("无效类型抛出 GeoException")
        void testParseLineStringWrongType() {
            assertThatThrownBy(() -> WktCodec.parseLineString("POINT(0 0)"))
                .isInstanceOf(GeoException.class);
        }
    }

    @Nested
    @DisplayName("parsePolygon 解析测试")
    class ParsePolygonTests {

        @Test
        @DisplayName("解析标准 POLYGON（单环）")
        void testParseSingleRingPolygon() {
            List<List<Coordinate>> rings = WktCodec.parsePolygon(
                "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
            assertThat(rings).hasSize(1);
            assertThat(rings.get(0)).hasSize(5);
        }

        @Test
        @DisplayName("解析带孔洞的 POLYGON")
        void testParsePolygonWithHole() {
            List<List<Coordinate>> rings = WktCodec.parsePolygon(
                "POLYGON((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 8 2, 8 8, 2 8, 2 2))");
            assertThat(rings).hasSize(2);
            assertThat(rings.get(0)).hasSize(5);
            assertThat(rings.get(1)).hasSize(5);
        }

        @Test
        @DisplayName("大小写不敏感解析 POLYGON")
        void testParsePolygonCaseInsensitive() {
            List<List<Coordinate>> rings = WktCodec.parsePolygon(
                "polygon((0 0, 1 0, 1 1, 0 0))");
            assertThat(rings).hasSize(1);
        }

        @Test
        @DisplayName("null 输入抛出 GeoException")
        void testParsePolygonNull() {
            assertThatThrownBy(() -> WktCodec.parsePolygon(null))
                .isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("空体抛出 GeoException")
        void testParsePolygonEmptyBody() {
            assertThatThrownBy(() -> WktCodec.parsePolygon("POLYGON()"))
                .isInstanceOf(GeoException.class);
        }
    }

    @Nested
    @DisplayName("toWkt 序列化测试")
    class ToWktTests {

        @Test
        @DisplayName("坐标序列化为 POINT WKT")
        void testToWktPoint() {
            String wkt = WktCodec.toWkt(Coordinate.wgs84(116.4074, 39.9042));
            assertThat(wkt).isEqualTo("POINT(116.4074 39.9042)");
        }

        @Test
        @DisplayName("整数坐标序列化")
        void testToWktIntegerCoords() {
            String wkt = WktCodec.toWkt(Coordinate.wgs84(1.0, 2.0));
            assertThat(wkt).isEqualTo("POINT(1 2)");
        }

        @Test
        @DisplayName("null 坐标抛出 GeoException")
        void testToWktNull() {
            assertThatThrownBy(() -> WktCodec.toWkt(null))
                .isInstanceOf(GeoException.class);
        }
    }

    @Nested
    @DisplayName("lineStringToWkt 序列化测试")
    class LineStringToWktTests {

        @Test
        @DisplayName("坐标列表序列化为 LINESTRING WKT")
        void testLineStringToWkt() {
            List<Coordinate> coords = List.of(
                Coordinate.wgs84(0, 0),
                Coordinate.wgs84(1, 1),
                Coordinate.wgs84(2, 2)
            );
            String wkt = WktCodec.lineStringToWkt(coords);
            assertThat(wkt).isEqualTo("LINESTRING(0 0, 1 1, 2 2)");
        }

        @Test
        @DisplayName("null 列表抛出 GeoException")
        void testLineStringToWktNull() {
            assertThatThrownBy(() -> WktCodec.lineStringToWkt(null))
                .isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("空列表抛出 GeoException")
        void testLineStringToWktEmpty() {
            assertThatThrownBy(() -> WktCodec.lineStringToWkt(List.of()))
                .isInstanceOf(GeoException.class);
        }
    }

    @Nested
    @DisplayName("polygonToWkt 序列化测试")
    class PolygonToWktTests {

        @Test
        @DisplayName("外环序列化为 POLYGON WKT（无孔洞）")
        void testPolygonToWktNoHoles() {
            List<Coordinate> ring = List.of(
                Coordinate.wgs84(0, 0),
                Coordinate.wgs84(1, 0),
                Coordinate.wgs84(1, 1),
                Coordinate.wgs84(0, 0)
            );
            String wkt = WktCodec.polygonToWkt(ring);
            assertThat(wkt).isEqualTo("POLYGON((0 0, 1 0, 1 1, 0 0))");
        }

        @Test
        @DisplayName("外环和孔洞序列化为 POLYGON WKT")
        void testPolygonToWktWithHoles() {
            List<Coordinate> exterior = List.of(
                Coordinate.wgs84(0, 0),
                Coordinate.wgs84(10, 0),
                Coordinate.wgs84(10, 10),
                Coordinate.wgs84(0, 0)
            );
            List<List<Coordinate>> holes = List.of(
                List.of(
                    Coordinate.wgs84(2, 2),
                    Coordinate.wgs84(8, 2),
                    Coordinate.wgs84(8, 8),
                    Coordinate.wgs84(2, 2)
                )
            );
            String wkt = WktCodec.polygonToWkt(exterior, holes);
            assertThat(wkt).isEqualTo("POLYGON((0 0, 10 0, 10 10, 0 0), (2 2, 8 2, 8 8, 2 2))");
        }

        @Test
        @DisplayName("null 外环抛出 GeoException")
        void testPolygonToWktNullExterior() {
            assertThatThrownBy(() -> WktCodec.polygonToWkt(null))
                .isInstanceOf(GeoException.class);
        }
    }

    @Nested
    @DisplayName("往返解析测试")
    class RoundTripTests {

        @Test
        @DisplayName("POINT 往返")
        void testPointRoundTrip() {
            Coordinate original = Coordinate.wgs84(116.4074, 39.9042);
            String wkt = WktCodec.toWkt(original);
            Coordinate parsed = WktCodec.parsePoint(wkt);
            assertThat(parsed.longitude()).isCloseTo(original.longitude(), within(0.0001));
            assertThat(parsed.latitude()).isCloseTo(original.latitude(), within(0.0001));
        }

        @Test
        @DisplayName("LINESTRING 往返")
        void testLineStringRoundTrip() {
            List<Coordinate> original = List.of(
                Coordinate.wgs84(0, 0),
                Coordinate.wgs84(1.5, 2.5),
                Coordinate.wgs84(3, 4)
            );
            String wkt = WktCodec.lineStringToWkt(original);
            List<Coordinate> parsed = WktCodec.parseLineString(wkt);
            assertThat(parsed).hasSameSizeAs(original);
            for (int i = 0; i < original.size(); i++) {
                assertThat(parsed.get(i).longitude()).isCloseTo(original.get(i).longitude(), within(0.0001));
                assertThat(parsed.get(i).latitude()).isCloseTo(original.get(i).latitude(), within(0.0001));
            }
        }

        @Test
        @DisplayName("POLYGON 往返")
        void testPolygonRoundTrip() {
            List<Coordinate> ring = List.of(
                Coordinate.wgs84(0, 0),
                Coordinate.wgs84(1, 0),
                Coordinate.wgs84(1, 1),
                Coordinate.wgs84(0, 1),
                Coordinate.wgs84(0, 0)
            );
            String wkt = WktCodec.polygonToWkt(ring);
            List<List<Coordinate>> parsed = WktCodec.parsePolygon(wkt);
            assertThat(parsed).hasSize(1);
            assertThat(parsed.get(0)).hasSameSizeAs(ring);
        }
    }

    @Nested
    @DisplayName("安全限制测试")
    class SecurityTests {

        @Test
        @DisplayName("超长输入抛出 GeoException")
        void testMaxInputLength() {
            String longWkt = "POINT(" + "1".repeat(1_000_001) + ")";
            assertThatThrownBy(() -> WktCodec.parsePoint(longWkt))
                .isInstanceOf(GeoException.class)
                .hasMessageContaining("maximum length");
        }
    }
}
