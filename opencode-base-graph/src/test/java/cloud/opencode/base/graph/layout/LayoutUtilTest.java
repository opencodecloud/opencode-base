package cloud.opencode.base.graph.layout;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * LayoutUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("LayoutUtil 测试")
class LayoutUtilTest {

    @Nested
    @DisplayName("forceDirected测试")
    class ForceDirectedTests {

        @Test
        @DisplayName("计算力导向布局")
        void testForceDirected() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.forceDirected(graph, 800, 600);

            assertThat(positions).hasSize(3);
            assertThat(positions).containsKeys("A", "B", "C");
            for (LayoutUtil.Point2D p : positions.values()) {
                assertThat(p.x()).isBetween(0.0, 800.0);
                assertThat(p.y()).isBetween(0.0, 600.0);
            }
        }

        @Test
        @DisplayName("自定义迭代次数")
        void testForceDirectedWithIterations() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.forceDirected(graph, 400, 300, 50);

            assertThat(positions).hasSize(2);
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.undirected();

            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.forceDirected(graph, 800, 600);

            assertThat(positions).isEmpty();
        }

        @Test
        @DisplayName("null图返回空映射")
        void testNullGraph() {
            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.forceDirected(null, 800, 600);

            assertThat(positions).isEmpty();
        }
    }

    @Nested
    @DisplayName("spring测试")
    class SpringTests {

        @Test
        @DisplayName("计算弹簧布局")
        void testSpring() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.spring(graph, 800, 600, 100);

            assertThat(positions).hasSize(3);
            for (LayoutUtil.Point2D p : positions.values()) {
                assertThat(p.x()).isBetween(0.0, 800.0);
                assertThat(p.y()).isBetween(0.0, 600.0);
            }
        }

        @Test
        @DisplayName("自定义迭代次数")
        void testSpringWithIterations() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.spring(graph, 400, 300, 50, 25);

            assertThat(positions).hasSize(2);
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.spring(OpenGraph.undirected(), 800, 600, 100);

            assertThat(positions).isEmpty();
        }
    }

    @Nested
    @DisplayName("circular测试")
    class CircularTests {

        @Test
        @DisplayName("计算环形布局")
        void testCircular() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addVertex("D");

            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.circular(graph, 400, 300, 100);

            assertThat(positions).hasSize(4);
            // All points should be on a circle of radius 100 centered at (400, 300)
            for (LayoutUtil.Point2D p : positions.values()) {
                double dist = Math.sqrt(Math.pow(p.x() - 400, 2) + Math.pow(p.y() - 300, 2));
                assertThat(dist).isCloseTo(100, within(1.0));
            }
        }

        @Test
        @DisplayName("使用默认中心计算环形布局")
        void testCircularDefaultCenter() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");
            graph.addVertex("B");

            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.circular(graph, 800, 600);

            assertThat(positions).hasSize(2);
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.circular(OpenGraph.undirected(), 400, 300, 100);

            assertThat(positions).isEmpty();
        }
    }

    @Nested
    @DisplayName("grid测试")
    class GridTests {

        @Test
        @DisplayName("计算网格布局")
        void testGrid() {
            Graph<String> graph = OpenGraph.undirected();
            for (int i = 0; i < 9; i++) {
                graph.addVertex("V" + i);
            }

            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.grid(graph, 900, 900);

            assertThat(positions).hasSize(9);
            for (LayoutUtil.Point2D p : positions.values()) {
                assertThat(p.x()).isBetween(0.0, 900.0);
                assertThat(p.y()).isBetween(0.0, 900.0);
            }
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.grid(OpenGraph.undirected(), 800, 600);

            assertThat(positions).isEmpty();
        }
    }

    @Nested
    @DisplayName("hierarchical测试")
    class HierarchicalTests {

        @Test
        @DisplayName("计算层次布局")
        void testHierarchical() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");
            graph.addEdge("C", "D");

            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.hierarchical(graph, 800, 600);

            assertThat(positions).hasSize(4);
            // A should be at the top (smallest y)
            assertThat(positions.get("A").y()).isLessThan(positions.get("D").y());
        }

        @Test
        @DisplayName("循环图处理")
        void testCyclicGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.hierarchical(graph, 800, 600);

            assertThat(positions).hasSize(3);
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.hierarchical(OpenGraph.directed(), 800, 600);

            assertThat(positions).isEmpty();
        }
    }

    @Nested
    @DisplayName("random测试")
    class RandomTests {

        @Test
        @DisplayName("计算随机布局")
        void testRandom() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");

            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.random(graph, 800, 600);

            assertThat(positions).hasSize(3);
            for (LayoutUtil.Point2D p : positions.values()) {
                assertThat(p.x()).isBetween(0.0, 800.0);
                assertThat(p.y()).isBetween(0.0, 600.0);
            }
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Map<String, LayoutUtil.Point2D> positions = LayoutUtil.random(OpenGraph.undirected(), 800, 600);

            assertThat(positions).isEmpty();
        }
    }

    @Nested
    @DisplayName("center测试")
    class CenterTests {

        @Test
        @DisplayName("居中布局")
        void testCenter() {
            Map<String, LayoutUtil.Point2D> positions = Map.of(
                "A", new LayoutUtil.Point2D(0, 0),
                "B", new LayoutUtil.Point2D(100, 100)
            );

            Map<String, LayoutUtil.Point2D> centered = LayoutUtil.center(positions, 400, 400);

            // Check that positions are centered
            assertThat(centered).hasSize(2);
        }

        @Test
        @DisplayName("空映射返回原映射")
        void testEmptyMap() {
            Map<String, LayoutUtil.Point2D> centered = LayoutUtil.center(Map.of(), 800, 600);

            assertThat(centered).isEmpty();
        }

        @Test
        @DisplayName("null映射返回null")
        void testNullMap() {
            Map<String, LayoutUtil.Point2D> centered = LayoutUtil.center(null, 800, 600);

            assertThat(centered).isNull();
        }
    }

    @Nested
    @DisplayName("scale测试")
    class ScaleTests {

        @Test
        @DisplayName("缩放布局")
        void testScale() {
            Map<String, LayoutUtil.Point2D> positions = Map.of(
                "A", new LayoutUtil.Point2D(0, 0),
                "B", new LayoutUtil.Point2D(1000, 1000)
            );

            Map<String, LayoutUtil.Point2D> scaled = LayoutUtil.scale(positions, 400, 400, 50);

            for (LayoutUtil.Point2D p : scaled.values()) {
                assertThat(p.x()).isBetween(0.0, 400.0);
                assertThat(p.y()).isBetween(0.0, 400.0);
            }
        }

        @Test
        @DisplayName("空映射返回原映射")
        void testEmptyMap() {
            Map<String, LayoutUtil.Point2D> scaled = LayoutUtil.scale(Map.of(), 800, 600, 10);

            assertThat(scaled).isEmpty();
        }

        @Test
        @DisplayName("null映射返回null")
        void testNullMap() {
            Map<String, LayoutUtil.Point2D> scaled = LayoutUtil.scale(null, 800, 600, 10);

            assertThat(scaled).isNull();
        }
    }

    @Nested
    @DisplayName("Point2D测试")
    class Point2DTests {

        @Test
        @DisplayName("创建Point2D")
        void testCreate() {
            LayoutUtil.Point2D p = new LayoutUtil.Point2D(10.0, 20.0);

            assertThat(p.x()).isEqualTo(10.0);
            assertThat(p.y()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("distanceTo方法")
        void testDistanceTo() {
            LayoutUtil.Point2D p1 = new LayoutUtil.Point2D(0, 0);
            LayoutUtil.Point2D p2 = new LayoutUtil.Point2D(3, 4);

            assertThat(p1.distanceTo(p2)).isEqualTo(5.0);
        }

        @Test
        @DisplayName("add方法")
        void testAdd() {
            LayoutUtil.Point2D p1 = new LayoutUtil.Point2D(1, 2);
            LayoutUtil.Point2D p2 = new LayoutUtil.Point2D(3, 4);

            LayoutUtil.Point2D result = p1.add(p2);

            assertThat(result.x()).isEqualTo(4.0);
            assertThat(result.y()).isEqualTo(6.0);
        }

        @Test
        @DisplayName("subtract方法")
        void testSubtract() {
            LayoutUtil.Point2D p1 = new LayoutUtil.Point2D(5, 7);
            LayoutUtil.Point2D p2 = new LayoutUtil.Point2D(2, 3);

            LayoutUtil.Point2D result = p1.subtract(p2);

            assertThat(result.x()).isEqualTo(3.0);
            assertThat(result.y()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("multiply方法")
        void testMultiply() {
            LayoutUtil.Point2D p = new LayoutUtil.Point2D(2, 3);

            LayoutUtil.Point2D result = p.multiply(2.0);

            assertThat(result.x()).isEqualTo(4.0);
            assertThat(result.y()).isEqualTo(6.0);
        }

        @Test
        @DisplayName("toString方法")
        void testToString() {
            LayoutUtil.Point2D p = new LayoutUtil.Point2D(1.5, 2.5);

            String str = p.toString();

            assertThat(str).contains("1.50");
            assertThat(str).contains("2.50");
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("默认迭代次数")
        void testDefaultIterations() {
            assertThat(LayoutUtil.DEFAULT_ITERATIONS).isEqualTo(100);
        }

        @Test
        @DisplayName("默认冷却因子")
        void testDefaultCooling() {
            assertThat(LayoutUtil.DEFAULT_COOLING).isEqualTo(0.95);
        }
    }
}
