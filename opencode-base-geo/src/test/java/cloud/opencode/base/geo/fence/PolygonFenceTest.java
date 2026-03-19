package cloud.opencode.base.geo.fence;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PolygonFence 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("PolygonFence 测试")
class PolygonFenceTest {

    private List<Coordinate> createSquare() {
        return List.of(
            Coordinate.wgs84(116.0, 39.0),
            Coordinate.wgs84(117.0, 39.0),
            Coordinate.wgs84(117.0, 40.0),
            Coordinate.wgs84(116.0, 40.0)
        );
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("正常创建PolygonFence")
        void testConstructor() {
            List<Coordinate> vertices = createSquare();
            PolygonFence fence = new PolygonFence(vertices);

            assertThat(fence.vertices()).hasSize(4);
        }

        @Test
        @DisplayName("vertices为null抛出异常")
        void testNullVertices() {
            assertThatThrownBy(() -> new PolygonFence(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 3 vertices");
        }

        @Test
        @DisplayName("顶点不足3个抛出异常")
        void testInsufficientVertices() {
            List<Coordinate> vertices = List.of(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 39.0)
            );

            assertThatThrownBy(() -> new PolygonFence(vertices))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 3 vertices");
        }

        @Test
        @DisplayName("顶点列表不可变")
        void testVerticesImmutable() {
            List<Coordinate> vertices = new ArrayList<>(createSquare());
            PolygonFence fence = new PolygonFence(vertices);

            // 修改原列表不影响围栏
            vertices.clear();

            assertThat(fence.vertices()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("contains()测试")
    class ContainsTests {

        @Test
        @DisplayName("内部点在围栏内")
        void testPointInside() {
            PolygonFence fence = new PolygonFence(createSquare());
            Coordinate inside = Coordinate.wgs84(116.5, 39.5);

            assertThat(fence.contains(inside)).isTrue();
        }

        @Test
        @DisplayName("外部点不在围栏内")
        void testPointOutside() {
            PolygonFence fence = new PolygonFence(createSquare());
            Coordinate outside = Coordinate.wgs84(118.0, 39.5);

            assertThat(fence.contains(outside)).isFalse();
        }

        @Test
        @DisplayName("顶点在围栏内")
        void testVertexInside() {
            PolygonFence fence = new PolygonFence(createSquare());
            Coordinate vertex = Coordinate.wgs84(116.0, 39.0);

            assertThat(fence.contains(vertex)).isTrue();
        }

        @Test
        @DisplayName("边上的点在围栏内")
        void testPointOnEdge() {
            PolygonFence fence = new PolygonFence(createSquare());
            Coordinate onEdge = Coordinate.wgs84(116.5, 39.0);

            assertThat(fence.contains(onEdge)).isTrue();
        }

        @Test
        @DisplayName("null点返回false")
        void testNullPoint() {
            PolygonFence fence = new PolygonFence(createSquare());

            assertThat(fence.contains(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("vertexCount()测试")
    class VertexCountTests {

        @Test
        @DisplayName("返回正确的顶点数")
        void testVertexCount() {
            PolygonFence fence = new PolygonFence(createSquare());

            assertThat(fence.vertexCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("三角形返回3")
        void testTriangle() {
            List<Coordinate> triangle = List.of(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 39.0),
                Coordinate.wgs84(116.5, 40.0)
            );
            PolygonFence fence = new PolygonFence(triangle);

            assertThat(fence.vertexCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getBoundingBox()测试")
    class GetBoundingBoxTests {

        @Test
        @DisplayName("返回正确的边界框")
        void testGetBoundingBox() {
            PolygonFence fence = new PolygonFence(createSquare());
            RectangleFence bbox = fence.getBoundingBox();

            assertThat(bbox.southwest().longitude()).isEqualTo(116.0);
            assertThat(bbox.southwest().latitude()).isEqualTo(39.0);
            assertThat(bbox.northeast().longitude()).isEqualTo(117.0);
            assertThat(bbox.northeast().latitude()).isEqualTo(40.0);
        }
    }

    @Nested
    @DisplayName("vertices()测试")
    class VerticesTests {

        @Test
        @DisplayName("返回顶点列表")
        void testVertices() {
            List<Coordinate> original = createSquare();
            PolygonFence fence = new PolygonFence(original);

            assertThat(fence.vertices()).hasSize(4);
            assertThat(fence.vertices()).containsExactlyElementsOf(original);
        }
    }

    @Nested
    @DisplayName("复杂多边形测试")
    class ComplexPolygonTests {

        @Test
        @DisplayName("凹多边形检测")
        void testConcavePolygon() {
            // L形多边形
            List<Coordinate> lShape = List.of(
                Coordinate.wgs84(0, 0),
                Coordinate.wgs84(2, 0),
                Coordinate.wgs84(2, 1),
                Coordinate.wgs84(1, 1),
                Coordinate.wgs84(1, 2),
                Coordinate.wgs84(0, 2)
            );
            PolygonFence fence = new PolygonFence(lShape);

            // 在L内部
            assertThat(fence.contains(Coordinate.wgs84(0.5, 0.5))).isTrue();
            // 在L的凹处（外部）
            assertThat(fence.contains(Coordinate.wgs84(1.5, 1.5))).isFalse();
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("equals()正确比较")
        void testEquals() {
            List<Coordinate> vertices = createSquare();
            PolygonFence fence1 = new PolygonFence(vertices);
            PolygonFence fence2 = new PolygonFence(vertices);

            assertThat(fence1).isEqualTo(fence2);
        }

        @Test
        @DisplayName("hashCode()一致性")
        void testHashCode() {
            List<Coordinate> vertices = createSquare();
            PolygonFence fence1 = new PolygonFence(vertices);
            PolygonFence fence2 = new PolygonFence(vertices);

            assertThat(fence1.hashCode()).isEqualTo(fence2.hashCode());
        }
    }

    @Nested
    @DisplayName("GeoFence接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现GeoFence接口")
        void testImplementsInterface() {
            PolygonFence fence = new PolygonFence(createSquare());

            assertThat(fence).isInstanceOf(GeoFence.class);
        }
    }
}
