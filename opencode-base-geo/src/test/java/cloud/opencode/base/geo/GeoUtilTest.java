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

    @Nested
    @DisplayName("质心计算测试")
    class CentroidTests {

        @Test
        @DisplayName("centroid()计算多点质心")
        void testCentroidBasic() {
            List<Coordinate> coords = List.of(
                Coordinate.wgs84(0, 0),
                Coordinate.wgs84(2, 0),
                Coordinate.wgs84(2, 2),
                Coordinate.wgs84(0, 2)
            );
            Coordinate centroid = GeoUtil.centroid(coords);
            assertThat(centroid).isNotNull();
            assertThat(centroid.longitude()).isCloseTo(1.0, within(0.01));
            assertThat(centroid.latitude()).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("centroid()单点返回自身")
        void testCentroidSinglePoint() {
            List<Coordinate> coords = List.of(Coordinate.wgs84(116.4074, 39.9042));
            Coordinate centroid = GeoUtil.centroid(coords);
            assertThat(centroid).isNotNull();
            assertThat(centroid.longitude()).isCloseTo(116.4074, within(0.0001));
            assertThat(centroid.latitude()).isCloseTo(39.9042, within(0.0001));
        }

        @Test
        @DisplayName("centroid()null列表返回null")
        void testCentroidNull() {
            assertThat(GeoUtil.centroid(null)).isNull();
        }

        @Test
        @DisplayName("centroid()空列表返回null")
        void testCentroidEmpty() {
            assertThat(GeoUtil.centroid(List.of())).isNull();
        }

        @Test
        @DisplayName("centroid()忽略null元素")
        void testCentroidWithNulls() {
            List<Coordinate> coords = new ArrayList<>();
            coords.add(Coordinate.wgs84(0, 0));
            coords.add(null);
            coords.add(Coordinate.wgs84(2, 0));
            Coordinate centroid = GeoUtil.centroid(coords);
            assertThat(centroid).isNotNull();
            assertThat(centroid.longitude()).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("centroid()全null返回null")
        void testCentroidAllNulls() {
            List<Coordinate> coords = new ArrayList<>();
            coords.add(null);
            coords.add(null);
            assertThat(GeoUtil.centroid(coords)).isNull();
        }
    }

    @Nested
    @DisplayName("总路径距离测试")
    class TotalDistanceTests {

        @Test
        @DisplayName("totalDistance()计算路径总距离")
        void testTotalDistanceBasic() {
            List<Coordinate> path = List.of(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 39.0),
                Coordinate.wgs84(117.0, 40.0)
            );
            double totalDist = GeoUtil.totalDistance(path);
            // 两段距离之和，每段约85-111公里
            assertThat(totalDist).isGreaterThan(150000);
        }

        @Test
        @DisplayName("totalDistance()单点返回0")
        void testTotalDistanceSinglePoint() {
            assertThat(GeoUtil.totalDistance(List.of(Coordinate.wgs84(0, 0)))).isEqualTo(0);
        }

        @Test
        @DisplayName("totalDistance()null返回0")
        void testTotalDistanceNull() {
            assertThat(GeoUtil.totalDistance(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("totalDistance()空列表返回0")
        void testTotalDistanceEmpty() {
            assertThat(GeoUtil.totalDistance(List.of())).isEqualTo(0);
        }

        @Test
        @DisplayName("totalDistance()忽略null元素")
        void testTotalDistanceWithNulls() {
            List<Coordinate> path = new ArrayList<>();
            path.add(Coordinate.wgs84(0, 0));
            path.add(null);
            path.add(Coordinate.wgs84(1, 0));
            double dist = GeoUtil.totalDistance(path);
            assertThat(dist).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("点到线段距离测试")
    class DistanceToSegmentTests {

        @Test
        @DisplayName("distanceToSegment()点在线段上方")
        void testDistanceToSegmentAbove() {
            Coordinate point = Coordinate.wgs84(0.5, 1);
            Coordinate start = Coordinate.wgs84(0, 0);
            Coordinate end = Coordinate.wgs84(1, 0);
            double dist = GeoUtil.distanceToSegment(point, start, end);
            // 约111公里（1度纬度）
            assertThat(dist).isCloseTo(111000, within(2000.0));
        }

        @Test
        @DisplayName("distanceToSegment()点在端点外侧")
        void testDistanceToSegmentBeyondEndpoint() {
            Coordinate point = Coordinate.wgs84(2, 0);
            Coordinate start = Coordinate.wgs84(0, 0);
            Coordinate end = Coordinate.wgs84(1, 0);
            double dist = GeoUtil.distanceToSegment(point, start, end);
            // 应等于点到end的距离（约111公里）
            double distToEnd = OpenGeo.distance(point, end);
            assertThat(dist).isCloseTo(distToEnd, within(100.0));
        }

        @Test
        @DisplayName("distanceToSegment()退化线段（同一点）")
        void testDistanceToSegmentDegenerate() {
            Coordinate point = Coordinate.wgs84(1, 1);
            Coordinate start = Coordinate.wgs84(0, 0);
            double dist = GeoUtil.distanceToSegment(point, start, start);
            double expected = OpenGeo.distance(point, start);
            assertThat(dist).isCloseTo(expected, within(1.0));
        }

        @Test
        @DisplayName("distanceToSegment()null输入返回0")
        void testDistanceToSegmentNull() {
            assertThat(GeoUtil.distanceToSegment(null, Coordinate.wgs84(0, 0), Coordinate.wgs84(1, 0))).isEqualTo(0);
            assertThat(GeoUtil.distanceToSegment(Coordinate.wgs84(0, 0), null, Coordinate.wgs84(1, 0))).isEqualTo(0);
            assertThat(GeoUtil.distanceToSegment(Coordinate.wgs84(0, 0), Coordinate.wgs84(1, 0), null)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("点到折线距离测试")
    class DistanceToPolylineTests {

        @Test
        @DisplayName("distanceToPolyline()计算最短距离")
        void testDistanceToPolylineBasic() {
            Coordinate point = Coordinate.wgs84(0.5, 1);
            List<Coordinate> polyline = List.of(
                Coordinate.wgs84(0, 0),
                Coordinate.wgs84(1, 0),
                Coordinate.wgs84(2, 0)
            );
            double dist = GeoUtil.distanceToPolyline(point, polyline);
            assertThat(dist).isCloseTo(111000, within(2000.0));
        }

        @Test
        @DisplayName("distanceToPolyline()null输入返回0")
        void testDistanceToPolylineNull() {
            assertThat(GeoUtil.distanceToPolyline(null, List.of(Coordinate.wgs84(0, 0), Coordinate.wgs84(1, 0)))).isEqualTo(0);
            assertThat(GeoUtil.distanceToPolyline(Coordinate.wgs84(0, 0), null)).isEqualTo(0);
        }

        @Test
        @DisplayName("distanceToPolyline()不足2点返回0")
        void testDistanceToPolylineTooFewPoints() {
            assertThat(GeoUtil.distanceToPolyline(Coordinate.wgs84(0, 0), List.of(Coordinate.wgs84(1, 1)))).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("插值测试")
    class InterpolateTests {

        @Test
        @DisplayName("interpolate()起点处fraction=0")
        void testInterpolateAtStart() {
            Coordinate c1 = Coordinate.wgs84(0, 0);
            Coordinate c2 = Coordinate.wgs84(10, 0);
            Coordinate result = GeoUtil.interpolate(c1, c2, 0.0);
            assertThat(result.longitude()).isCloseTo(0, within(0.0001));
            assertThat(result.latitude()).isCloseTo(0, within(0.0001));
        }

        @Test
        @DisplayName("interpolate()终点处fraction=1")
        void testInterpolateAtEnd() {
            Coordinate c1 = Coordinate.wgs84(0, 0);
            Coordinate c2 = Coordinate.wgs84(10, 0);
            Coordinate result = GeoUtil.interpolate(c1, c2, 1.0);
            assertThat(result.longitude()).isCloseTo(10, within(0.0001));
            assertThat(result.latitude()).isCloseTo(0, within(0.0001));
        }

        @Test
        @DisplayName("interpolate()中点处fraction=0.5")
        void testInterpolateAtMidpoint() {
            Coordinate c1 = Coordinate.wgs84(0, 0);
            Coordinate c2 = Coordinate.wgs84(10, 0);
            Coordinate result = GeoUtil.interpolate(c1, c2, 0.5);
            assertThat(result.longitude()).isCloseTo(5.0, within(0.01));
            assertThat(result.latitude()).isCloseTo(0.0, within(0.01));
        }

        @Test
        @DisplayName("interpolate()相同点")
        void testInterpolateSamePoint() {
            Coordinate c = Coordinate.wgs84(5, 5);
            Coordinate result = GeoUtil.interpolate(c, c, 0.5);
            assertThat(result.longitude()).isCloseTo(5, within(0.0001));
            assertThat(result.latitude()).isCloseTo(5, within(0.0001));
        }

        @Test
        @DisplayName("interpolate()null输入返回null")
        void testInterpolateNull() {
            assertThat(GeoUtil.interpolate(null, Coordinate.wgs84(0, 0), 0.5)).isNull();
            assertThat(GeoUtil.interpolate(Coordinate.wgs84(0, 0), null, 0.5)).isNull();
        }

        @Test
        @DisplayName("interpolate()无效fraction抛出异常")
        void testInterpolateInvalidFraction() {
            Coordinate c1 = Coordinate.wgs84(0, 0);
            Coordinate c2 = Coordinate.wgs84(1, 1);
            assertThatThrownBy(() -> GeoUtil.interpolate(c1, c2, -0.1))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> GeoUtil.interpolate(c1, c2, 1.1))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("罗盘方向测试")
    class CompassDirectionTests {

        @Test
        @DisplayName("compassDirection()基本方位")
        void testCompassDirectionCardinal() {
            assertThat(GeoUtil.compassDirection(0)).isEqualTo("N");
            assertThat(GeoUtil.compassDirection(90)).isEqualTo("E");
            assertThat(GeoUtil.compassDirection(180)).isEqualTo("S");
            assertThat(GeoUtil.compassDirection(270)).isEqualTo("W");
        }

        @Test
        @DisplayName("compassDirection()16方位")
        void testCompassDirection16Points() {
            assertThat(GeoUtil.compassDirection(22.5)).isEqualTo("NNE");
            assertThat(GeoUtil.compassDirection(45)).isEqualTo("NE");
            assertThat(GeoUtil.compassDirection(67.5)).isEqualTo("ENE");
            assertThat(GeoUtil.compassDirection(135)).isEqualTo("SE");
            assertThat(GeoUtil.compassDirection(225)).isEqualTo("SW");
            assertThat(GeoUtil.compassDirection(315)).isEqualTo("NW");
        }

        @Test
        @DisplayName("compassDirection()360度等于北")
        void testCompassDirection360() {
            assertThat(GeoUtil.compassDirection(360)).isEqualTo("N");
        }

        @Test
        @DisplayName("compassDirection()负角度正确处理")
        void testCompassDirectionNegative() {
            assertThat(GeoUtil.compassDirection(-90)).isEqualTo("W");
            assertThat(GeoUtil.compassDirection(-180)).isEqualTo("S");
        }
    }
}
