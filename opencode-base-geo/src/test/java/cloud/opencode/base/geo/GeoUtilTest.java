package cloud.opencode.base.geo;

import cloud.opencode.base.geo.fence.GeoFence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("GeoUtil 测试")
class GeoUtilTest {

    @Nested
    @DisplayName("围栏创建测试")
    class FenceCreationTests {

        @Test
        @DisplayName("createCircleFence(double,double,double)创建圆形围栏")
        void testCreateCircleFenceByCoords() {
            GeoFence fence = GeoUtil.createCircleFence(116.4074, 39.9042, 1000);

            assertThat(fence).isNotNull();
            assertThat(fence.contains(Coordinate.wgs84(116.4074, 39.9042))).isTrue();
        }

        @Test
        @DisplayName("createCircleFence(Coordinate,double)创建圆形围栏")
        void testCreateCircleFenceByCoordinate() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            GeoFence fence = GeoUtil.createCircleFence(center, 1000);

            assertThat(fence).isNotNull();
            assertThat(fence.contains(center)).isTrue();
        }

        @Test
        @DisplayName("createRectangleFence()创建矩形围栏")
        void testCreateRectangleFence() {
            GeoFence fence = GeoUtil.createRectangleFence(116.0, 39.0, 117.0, 40.0);

            assertThat(fence).isNotNull();
            assertThat(fence.contains(Coordinate.wgs84(116.5, 39.5))).isTrue();
            assertThat(fence.contains(Coordinate.wgs84(118.0, 39.5))).isFalse();
        }

        @Test
        @DisplayName("createPolygonFence(List)创建多边形围栏")
        void testCreatePolygonFenceFromList() {
            List<Coordinate> vertices = List.of(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 39.0),
                Coordinate.wgs84(117.0, 40.0),
                Coordinate.wgs84(116.0, 40.0)
            );

            GeoFence fence = GeoUtil.createPolygonFence(vertices);

            assertThat(fence).isNotNull();
            assertThat(fence.contains(Coordinate.wgs84(116.5, 39.5))).isTrue();
        }

        @Test
        @DisplayName("createPolygonFence(double...)创建多边形围栏")
        void testCreatePolygonFenceFromVarargs() {
            GeoFence fence = GeoUtil.createPolygonFence(
                116.0, 39.0,
                117.0, 39.0,
                117.0, 40.0,
                116.0, 40.0
            );

            assertThat(fence).isNotNull();
            assertThat(fence.contains(Coordinate.wgs84(116.5, 39.5))).isTrue();
        }

        @Test
        @DisplayName("createPolygonFence()参数不足抛出异常")
        void testCreatePolygonFenceInvalid() {
            assertThatThrownBy(() -> GeoUtil.createPolygonFence(116.0, 39.0))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> GeoUtil.createPolygonFence((double[]) null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("边界框测试")
    class BoundingBoxTests {

        @Test
        @DisplayName("getBoundingBox()计算边界框")
        void testGetBoundingBox() {
            List<Coordinate> coordinates = List.of(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 40.0),
                Coordinate.wgs84(118.0, 38.0)
            );

            double[] bbox = GeoUtil.getBoundingBox(coordinates);

            assertThat(bbox).isNotNull();
            assertThat(bbox).hasSize(4);
            // [minLng, minLat, maxLng, maxLat]
            assertThat(bbox[0]).isEqualTo(116.0); // minLng
            assertThat(bbox[1]).isEqualTo(38.0);  // minLat
            assertThat(bbox[2]).isEqualTo(118.0); // maxLng
            assertThat(bbox[3]).isEqualTo(40.0);  // maxLat
        }

        @Test
        @DisplayName("getBoundingBox()空列表返回null")
        void testGetBoundingBoxEmpty() {
            assertThat(GeoUtil.getBoundingBox(null)).isNull();
            assertThat(GeoUtil.getBoundingBox(List.of())).isNull();
        }

        @Test
        @DisplayName("getBoundingBox()忽略null元素")
        void testGetBoundingBoxWithNulls() {
            List<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(Coordinate.wgs84(116.0, 39.0));
            coordinates.add(null);
            coordinates.add(Coordinate.wgs84(117.0, 40.0));

            double[] bbox = GeoUtil.getBoundingBox(coordinates);

            assertThat(bbox).isNotNull();
        }

        @Test
        @DisplayName("getBoundingBoxCenter()计算中心")
        void testGetBoundingBoxCenter() {
            double[] bbox = {116.0, 39.0, 118.0, 41.0};

            Coordinate center = GeoUtil.getBoundingBoxCenter(bbox);

            assertThat(center).isNotNull();
            assertThat(center.longitude()).isEqualTo(117.0);
            assertThat(center.latitude()).isEqualTo(40.0);
        }

        @Test
        @DisplayName("getBoundingBoxCenter()无效输入返回null")
        void testGetBoundingBoxCenterInvalid() {
            assertThat(GeoUtil.getBoundingBoxCenter(null)).isNull();
            assertThat(GeoUtil.getBoundingBoxCenter(new double[]{1, 2, 3})).isNull();
        }

        @Test
        @DisplayName("expandBoundingBox()扩展边界框")
        void testExpandBoundingBox() {
            double[] bbox = {116.0, 39.0, 117.0, 40.0};

            double[] expanded = GeoUtil.expandBoundingBox(bbox, 10000); // 10km

            assertThat(expanded).isNotNull();
            assertThat(expanded[0]).isLessThan(bbox[0]); // minLng更小
            assertThat(expanded[1]).isLessThan(bbox[1]); // minLat更小
            assertThat(expanded[2]).isGreaterThan(bbox[2]); // maxLng更大
            assertThat(expanded[3]).isGreaterThan(bbox[3]); // maxLat更大
        }

        @Test
        @DisplayName("expandBoundingBox()无效输入返回null")
        void testExpandBoundingBoxInvalid() {
            assertThat(GeoUtil.expandBoundingBox(null, 1000)).isNull();
            assertThat(GeoUtil.expandBoundingBox(new double[]{1, 2}, 1000)).isNull();
        }
    }

    @Nested
    @DisplayName("距离操作测试")
    class DistanceOperationsTests {

        @Test
        @DisplayName("sortByDistance()按距离排序")
        void testSortByDistance() {
            Coordinate from = Coordinate.wgs84(116.0, 39.0);
            List<Coordinate> coords = List.of(
                Coordinate.wgs84(118.0, 39.0), // 远
                Coordinate.wgs84(116.1, 39.0), // 近
                Coordinate.wgs84(117.0, 39.0)  // 中
            );

            List<Coordinate> sorted = GeoUtil.sortByDistance(coords, from, c -> c);

            assertThat(sorted.get(0).longitude()).isCloseTo(116.1, within(0.01));
            assertThat(sorted.get(1).longitude()).isCloseTo(117.0, within(0.01));
            assertThat(sorted.get(2).longitude()).isCloseTo(118.0, within(0.01));
        }

        @Test
        @DisplayName("sortByDistance()处理空列表")
        void testSortByDistanceEmpty() {
            Coordinate from = Coordinate.wgs84(116.0, 39.0);
            java.util.function.Function<Coordinate, Coordinate> identity = c -> c;

            assertThat(GeoUtil.sortByDistance(null, from, identity)).isNull();
            assertThat(GeoUtil.sortByDistance(List.of(), from, identity)).isEmpty();
        }

        @Test
        @DisplayName("filterByDistance()过滤距离内元素")
        void testFilterByDistance() {
            Coordinate from = Coordinate.wgs84(116.0, 39.0);
            List<Coordinate> coords = List.of(
                Coordinate.wgs84(116.001, 39.0), // 约100米
                Coordinate.wgs84(116.1, 39.0),   // 约10公里
                Coordinate.wgs84(117.0, 39.0)    // 约100公里
            );

            List<Coordinate> filtered = GeoUtil.filterByDistance(coords, from, 5000, c -> c);

            assertThat(filtered).hasSize(1);
        }

        @Test
        @DisplayName("filterByDistance()处理空列表")
        void testFilterByDistanceEmpty() {
            Coordinate from = Coordinate.wgs84(116.0, 39.0);
            java.util.function.Function<Coordinate, Coordinate> identity = c -> c;

            assertThat(GeoUtil.filterByDistance(null, from, 1000, identity)).isEmpty();
            assertThat(GeoUtil.filterByDistance(List.of(), from, 1000, identity)).isEmpty();
        }

        @Test
        @DisplayName("findNearest()查找最近元素")
        void testFindNearest() {
            Coordinate from = Coordinate.wgs84(116.0, 39.0);
            List<Coordinate> coords = List.of(
                Coordinate.wgs84(118.0, 39.0),
                Coordinate.wgs84(116.1, 39.0),
                Coordinate.wgs84(117.0, 39.0)
            );

            Coordinate nearest = GeoUtil.findNearest(coords, from, c -> c);

            assertThat(nearest.longitude()).isCloseTo(116.1, within(0.01));
        }

        @Test
        @DisplayName("findNearest()空列表返回null")
        void testFindNearestEmpty() {
            Coordinate from = Coordinate.wgs84(116.0, 39.0);
            java.util.function.Function<Coordinate, Coordinate> identity = c -> c;

            assertThat(GeoUtil.findNearest(null, from, identity)).isNull();
            assertThat(GeoUtil.findNearest(List.of(), from, identity)).isNull();
        }
    }

    @Nested
    @DisplayName("面积计算测试")
    class AreaCalculationTests {

        @Test
        @DisplayName("calculatePolygonArea()计算多边形面积")
        void testCalculatePolygonArea() {
            // 约1度 x 1度的正方形
            List<Coordinate> vertices = List.of(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 39.0),
                Coordinate.wgs84(117.0, 40.0),
                Coordinate.wgs84(116.0, 40.0)
            );

            double area = GeoUtil.calculatePolygonArea(vertices);

            // 约10000平方公里 = 1e10平方米
            assertThat(area).isGreaterThan(1e9);
        }

        @Test
        @DisplayName("calculatePolygonArea()顶点不足返回0")
        void testCalculatePolygonAreaInvalid() {
            assertThat(GeoUtil.calculatePolygonArea(null)).isEqualTo(0);
            assertThat(GeoUtil.calculatePolygonArea(List.of())).isEqualTo(0);
            assertThat(GeoUtil.calculatePolygonArea(List.of(
                Coordinate.wgs84(116, 39),
                Coordinate.wgs84(117, 39)
            ))).isEqualTo(0);
        }

        @Test
        @DisplayName("calculatePolygonCircumference()计算周长")
        void testCalculatePolygonCircumference() {
            List<Coordinate> vertices = List.of(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 39.0),
                Coordinate.wgs84(117.0, 40.0),
                Coordinate.wgs84(116.0, 40.0)
            );

            double circumference = GeoUtil.calculatePolygonCircumference(vertices);

            // 约4 * 100公里 = 400公里
            assertThat(circumference).isGreaterThan(300000);
        }

        @Test
        @DisplayName("calculatePolygonCircumference()顶点不足返回0")
        void testCalculatePolygonCircumferenceInvalid() {
            assertThat(GeoUtil.calculatePolygonCircumference(null)).isEqualTo(0);
            assertThat(GeoUtil.calculatePolygonCircumference(List.of())).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("转换工具测试")
    class ConversionTests {

        @Test
        @DisplayName("toRadians()转换为弧度")
        void testToRadians() {
            assertThat(GeoUtil.toRadians(180)).isCloseTo(Math.PI, within(0.0001));
            assertThat(GeoUtil.toRadians(90)).isCloseTo(Math.PI / 2, within(0.0001));
        }

        @Test
        @DisplayName("toDegrees()转换为度")
        void testToDegrees() {
            assertThat(GeoUtil.toDegrees(Math.PI)).isCloseTo(180, within(0.0001));
            assertThat(GeoUtil.toDegrees(Math.PI / 2)).isCloseTo(90, within(0.0001));
        }

        @Test
        @DisplayName("metersToDegrees()米转度")
        void testMetersToDegrees() {
            // 赤道上111.3km约等于1度
            double degrees = GeoUtil.metersToDegrees(111319.9, 0);
            assertThat(degrees).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("degreesToMeters()度转米")
        void testDegreesToMeters() {
            double meters = GeoUtil.degreesToMeters(1, 0);
            assertThat(meters).isCloseTo(111319.9, within(100.0));
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("METERS_PER_DEGREE常量正确")
        void testMetersPerDegree() {
            assertThat(GeoUtil.METERS_PER_DEGREE).isEqualTo(111319.9);
        }
    }
}
