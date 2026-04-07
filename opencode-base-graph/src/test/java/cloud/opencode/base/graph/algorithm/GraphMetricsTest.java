package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphMetrics 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
@DisplayName("GraphMetrics 测试")
class GraphMetricsTest {

    @Nested
    @DisplayName("density测试")
    class DensityTests {

        @Test
        @DisplayName("完全无向图密度为1")
        void testCompleteUndirectedGraphDensity() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "C");

            assertThat(GraphMetrics.density(graph)).isCloseTo(1.0, within(1e-9));
        }

        @Test
        @DisplayName("完全有向图密度为1")
        void testCompleteDirectedGraphDensity() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "A");
            graph.addEdge("A", "C");
            graph.addEdge("C", "A");
            graph.addEdge("B", "C");
            graph.addEdge("C", "B");

            assertThat(GraphMetrics.density(graph)).isCloseTo(1.0, within(1e-9));
        }

        @Test
        @DisplayName("无边图密度为0")
        void testEmptyEdgesDensity() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");

            assertThat(GraphMetrics.density(graph)).isCloseTo(0.0, within(1e-9));
        }

        @Test
        @DisplayName("单顶点图密度为0")
        void testSingleVertexDensity() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            assertThat(GraphMetrics.density(graph)).isCloseTo(0.0, within(1e-9));
        }

        @Test
        @DisplayName("空图密度为0")
        void testEmptyGraphDensity() {
            Graph<String> graph = OpenGraph.undirected();
            assertThat(GraphMetrics.density(graph)).isCloseTo(0.0, within(1e-9));
        }
    }

    @Nested
    @DisplayName("eccentricity测试")
    class EccentricityTests {

        @Test
        @DisplayName("路径图端点离心率")
        void testPathGraphEndpointEccentricity() {
            // Path: A - B - C - D
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            assertThat(GraphMetrics.eccentricity(graph, "A")).isEqualTo(3);
            assertThat(GraphMetrics.eccentricity(graph, "D")).isEqualTo(3);
        }

        @Test
        @DisplayName("路径图中间顶点离心率")
        void testPathGraphMiddleEccentricity() {
            // Path: A - B - C - D
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            assertThat(GraphMetrics.eccentricity(graph, "B")).isEqualTo(2);
            assertThat(GraphMetrics.eccentricity(graph, "C")).isEqualTo(2);
        }

        @Test
        @DisplayName("不连通图离心率为MAX_VALUE")
        void testDisconnectedGraphEccentricity() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addVertex("C");

            assertThat(GraphMetrics.eccentricity(graph, "A")).isEqualTo(Integer.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("diameter和radius测试")
    class DiameterRadiusTests {

        @Test
        @DisplayName("路径图的直径和半径")
        void testPathGraphDiameterAndRadius() {
            // Path: A - B - C - D - E (length 4)
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");
            graph.addEdge("D", "E");

            assertThat(GraphMetrics.diameter(graph)).isEqualTo(4);
            assertThat(GraphMetrics.radius(graph)).isEqualTo(2);
        }

        @Test
        @DisplayName("单顶点图直径和半径为0")
        void testSingleVertexDiameterAndRadius() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            assertThat(GraphMetrics.diameter(graph)).isEqualTo(0);
            assertThat(GraphMetrics.radius(graph)).isEqualTo(0);
        }

        @Test
        @DisplayName("空图直径和半径为0")
        void testEmptyGraphDiameterAndRadius() {
            Graph<String> graph = OpenGraph.undirected();
            assertThat(GraphMetrics.diameter(graph)).isEqualTo(0);
            assertThat(GraphMetrics.radius(graph)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("center测试")
    class CenterTests {

        @Test
        @DisplayName("路径图的中心")
        void testPathGraphCenter() {
            // Path: A - B - C - D - E
            // eccentricities: A=4, B=3, C=2, D=3, E=4
            // radius=2, center={C}
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");
            graph.addEdge("D", "E");

            Set<String> center = GraphMetrics.center(graph);
            assertThat(center).containsExactly("C");
        }

        @Test
        @DisplayName("偶数路径图的中心包含两个顶点")
        void testEvenPathGraphCenter() {
            // Path: A - B - C - D
            // eccentricities: A=3, B=2, C=2, D=3
            // radius=2, center={B, C}
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            Set<String> center = GraphMetrics.center(graph);
            assertThat(center).containsExactlyInAnyOrder("B", "C");
        }

        @Test
        @DisplayName("空图的中心为空集")
        void testEmptyGraphCenter() {
            Graph<String> graph = OpenGraph.undirected();
            assertThat(GraphMetrics.center(graph)).isEmpty();
        }
    }

    @Nested
    @DisplayName("clusteringCoefficient测试")
    class ClusteringCoefficientTests {

        @Test
        @DisplayName("三角形顶点聚类系数为1")
        void testTriangleClusteringCoefficient() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C");

            assertThat(GraphMetrics.clusteringCoefficient(graph, "A")).isCloseTo(1.0, within(1e-9));
            assertThat(GraphMetrics.clusteringCoefficient(graph, "B")).isCloseTo(1.0, within(1e-9));
            assertThat(GraphMetrics.clusteringCoefficient(graph, "C")).isCloseTo(1.0, within(1e-9));
        }

        @Test
        @DisplayName("星形图中心聚类系数为0")
        void testStarCenterClusteringCoefficient() {
            // Star: A connected to B, C, D (no edges among B, C, D)
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("A", "D");

            assertThat(GraphMetrics.clusteringCoefficient(graph, "A")).isCloseTo(0.0, within(1e-9));
        }

        @Test
        @DisplayName("度小于2的顶点聚类系数为0")
        void testLowDegreeClusteringCoefficient() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            assertThat(GraphMetrics.clusteringCoefficient(graph, "A")).isCloseTo(0.0, within(1e-9));
        }
    }

    @Nested
    @DisplayName("averagePathLength测试")
    class AveragePathLengthTests {

        @Test
        @DisplayName("三角形平均路径长度为1")
        void testTriangleAveragePathLength() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C");

            // All pairs have distance 1
            assertThat(GraphMetrics.averagePathLength(graph)).isCloseTo(1.0, within(1e-9));
        }

        @Test
        @DisplayName("路径A-B-C平均路径长度")
        void testPathAveragePathLength() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            // Pairs: A->B=1, A->C=2, B->A=1, B->C=1, C->A=2, C->B=1
            // Average = (1+2+1+1+2+1)/6 = 8/6 = 4/3
            assertThat(GraphMetrics.averagePathLength(graph)).isCloseTo(4.0 / 3.0, within(1e-9));
        }

        @Test
        @DisplayName("单顶点图平均路径长度为0")
        void testSingleVertexAveragePathLength() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            assertThat(GraphMetrics.averagePathLength(graph)).isCloseTo(0.0, within(1e-9));
        }
    }

    @Nested
    @DisplayName("averageClusteringCoefficient测试")
    class AverageClusteringCoefficientTests {

        @Test
        @DisplayName("完全图平均聚类系数为1")
        void testCompleteGraphAverageClusteringCoefficient() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C");

            assertThat(GraphMetrics.averageClusteringCoefficient(graph)).isCloseTo(1.0, within(1e-9));
        }

        @Test
        @DisplayName("空图平均聚类系数为0")
        void testEmptyGraphAverageClusteringCoefficient() {
            Graph<String> graph = OpenGraph.undirected();
            assertThat(GraphMetrics.averageClusteringCoefficient(graph)).isCloseTo(0.0, within(1e-9));
        }
    }

    @Nested
    @DisplayName("summary测试")
    class SummaryTests {

        @Test
        @DisplayName("三角形图摘要")
        void testTriangleSummary() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C");

            GraphMetrics.GraphSummary summary = GraphMetrics.summary(graph);

            assertThat(summary.vertexCount()).isEqualTo(3);
            assertThat(summary.edgeCount()).isEqualTo(3);
            assertThat(summary.density()).isCloseTo(1.0, within(1e-9));
            assertThat(summary.directed()).isFalse();
            assertThat(summary.connected()).isTrue();
            assertThat(summary.componentCount()).isEqualTo(1);
            assertThat(summary.diameter()).isEqualTo(1);
            assertThat(summary.radius()).isEqualTo(1);
            assertThat(summary.averagePathLength()).isCloseTo(1.0, within(1e-9));
            assertThat(summary.averageClusteringCoefficient()).isCloseTo(1.0, within(1e-9));
        }

        @Test
        @DisplayName("空图摘要")
        void testEmptyGraphSummary() {
            Graph<String> graph = OpenGraph.directed();
            GraphMetrics.GraphSummary summary = GraphMetrics.summary(graph);

            assertThat(summary.vertexCount()).isEqualTo(0);
            assertThat(summary.edgeCount()).isEqualTo(0);
            assertThat(summary.density()).isCloseTo(0.0, within(1e-9));
            assertThat(summary.directed()).isTrue();
            assertThat(summary.connected()).isTrue();
            assertThat(summary.componentCount()).isEqualTo(0);
            assertThat(summary.diameter()).isEqualTo(0);
            assertThat(summary.radius()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("null参数测试")
    class NullParameterTests {

        @Test
        @DisplayName("density(null)应抛出NullPointerException")
        void densityNullThrows() {
            assertThatThrownBy(() -> GraphMetrics.density(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("diameter(null)应抛出NullPointerException")
        void diameterNullThrows() {
            assertThatThrownBy(() -> GraphMetrics.diameter(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("radius(null)应抛出NullPointerException")
        void radiusNullThrows() {
            assertThatThrownBy(() -> GraphMetrics.radius(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("center(null)应抛出NullPointerException")
        void centerNullThrows() {
            assertThatThrownBy(() -> GraphMetrics.center(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("summary(null)应抛出NullPointerException")
        void summaryNullThrows() {
            assertThatThrownBy(() -> GraphMetrics.summary(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
