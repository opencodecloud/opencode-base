package cloud.opencode.base.geo.fence;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoFence 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("GeoFence 测试")
class GeoFenceTest {

    private final Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
    private final Coordinate shanghai = Coordinate.wgs84(121.4737, 31.2304);

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("可以用lambda实现")
        void testLambdaImplementation() {
            // 简单的"总是返回true"围栏
            GeoFence alwaysInside = point -> true;

            assertThat(alwaysInside.contains(beijing)).isTrue();
            assertThat(alwaysInside.contains(shanghai)).isTrue();
        }

        @Test
        @DisplayName("可以用lambda实现复杂逻辑")
        void testComplexLambda() {
            // 只有在指定经度范围内的围栏
            GeoFence longitudeFence = point ->
                point != null && point.longitude() >= 115 && point.longitude() <= 117;

            assertThat(longitudeFence.contains(beijing)).isTrue();
            assertThat(longitudeFence.contains(shanghai)).isFalse();
        }
    }

    @Nested
    @DisplayName("实现类测试")
    class ImplementationsTests {

        @Test
        @DisplayName("CircleFence实现接口")
        void testCircleFenceImplements() {
            GeoFence fence = new CircleFence(beijing, 1000);

            assertThat(fence).isInstanceOf(GeoFence.class);
        }

        @Test
        @DisplayName("PolygonFence实现接口")
        void testPolygonFenceImplements() {
            List<Coordinate> vertices = List.of(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 39.0),
                Coordinate.wgs84(117.0, 40.0),
                Coordinate.wgs84(116.0, 40.0)
            );
            GeoFence fence = new PolygonFence(vertices);

            assertThat(fence).isInstanceOf(GeoFence.class);
        }

        @Test
        @DisplayName("RectangleFence实现接口")
        void testRectangleFenceImplements() {
            GeoFence fence = new RectangleFence(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 40.0)
            );

            assertThat(fence).isInstanceOf(GeoFence.class);
        }

        @Test
        @DisplayName("CrossDateLineFence实现接口")
        void testCrossDateLineFenceImplements() {
            GeoFence fence = new CrossDateLineFence(
                Coordinate.wgs84(170.0, -10.0),
                Coordinate.wgs84(-170.0, 10.0)
            );

            assertThat(fence).isInstanceOf(GeoFence.class);
        }
    }

    @Nested
    @DisplayName("通过接口调用测试")
    class InterfaceCallTests {

        @Test
        @DisplayName("通过接口引用调用contains")
        void testContainsViaInterface() {
            GeoFence fence = new CircleFence(beijing, 1000);

            boolean inside = fence.contains(beijing);
            boolean outside = fence.contains(shanghai);

            assertThat(inside).isTrue();
            assertThat(outside).isFalse();
        }
    }

    @Nested
    @DisplayName("多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("可以在方法参数中使用接口")
        void testAsMethodParameter() {
            GeoFence circle = new CircleFence(beijing, 1000);
            GeoFence rectangle = new RectangleFence(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 40.0)
            );

            assertThat(checkInFence(circle, beijing)).isTrue();
            assertThat(checkInFence(rectangle, beijing)).isTrue();
        }

        private boolean checkInFence(GeoFence fence, Coordinate point) {
            return fence.contains(point);
        }

        @Test
        @DisplayName("可以组合多个围栏")
        void testCombineFences() {
            GeoFence fence1 = new CircleFence(beijing, 100000); // 100km
            GeoFence fence2 = new CircleFence(shanghai, 100000);

            // 组合围栏：在任一围栏内就返回true
            GeoFence combined = point -> fence1.contains(point) || fence2.contains(point);

            assertThat(combined.contains(beijing)).isTrue();
            assertThat(combined.contains(shanghai)).isTrue();
            // 中间点可能不在任一围栏内
            Coordinate middle = Coordinate.wgs84(118.5, 35.0);
            // 判断中间点是否在组合围栏内取决于距离
        }

        @Test
        @DisplayName("可以取反围栏")
        void testNegatedFence() {
            GeoFence fence = new CircleFence(beijing, 1000);

            // 取反围栏：不在原围栏内就返回true
            GeoFence negated = point -> !fence.contains(point);

            assertThat(negated.contains(beijing)).isFalse();
            assertThat(negated.contains(shanghai)).isTrue();
        }
    }

    @Nested
    @DisplayName("null处理测试")
    class NullHandlingTests {

        @Test
        @DisplayName("各实现类处理null坐标")
        void testNullPointHandling() {
            GeoFence circle = new CircleFence(beijing, 1000);
            GeoFence polygon = new PolygonFence(List.of(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 39.0),
                Coordinate.wgs84(117.0, 40.0)
            ));
            GeoFence rectangle = new RectangleFence(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 40.0)
            );

            assertThat(circle.contains(null)).isFalse();
            assertThat(polygon.contains(null)).isFalse();
            assertThat(rectangle.contains(null)).isFalse();
        }
    }
}
