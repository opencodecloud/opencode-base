package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * SubgraphUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("SubgraphUtil 测试")
class SubgraphUtilTest {

    @Nested
    @DisplayName("induced测试")
    class InducedTests {

        @Test
        @DisplayName("创建顶点诱导子图")
        void testInduced() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("C", "D", 3.0);

            Graph<String> subgraph = SubgraphUtil.induced(graph, Set.of("A", "B", "C"));

            assertThat(subgraph.containsVertex("A")).isTrue();
            assertThat(subgraph.containsVertex("B")).isTrue();
            assertThat(subgraph.containsVertex("C")).isTrue();
            assertThat(subgraph.containsVertex("D")).isFalse();
            assertThat(subgraph.containsEdge("A", "B")).isTrue();
            assertThat(subgraph.containsEdge("B", "C")).isTrue();
            assertThat(subgraph.containsEdge("C", "D")).isFalse();
        }

        @Test
        @DisplayName("null图返回空图")
        void testNullGraph() {
            Graph<String> subgraph = SubgraphUtil.induced(null, Set.of("A"));

            assertThat(subgraph.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("空顶点集返回空图")
        void testEmptyVertices() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            Graph<String> subgraph = SubgraphUtil.induced(graph, Set.of());

            assertThat(subgraph.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("edgeInduced测试")
    class EdgeInducedTests {

        @Test
        @DisplayName("创建边诱导子图")
        void testEdgeInduced() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("C", "D", 3.0);

            Set<Edge<String>> edges = Set.of(new Edge<>("A", "B", 1.0));
            Graph<String> subgraph = SubgraphUtil.edgeInduced(graph, edges);

            assertThat(subgraph.containsVertex("A")).isTrue();
            assertThat(subgraph.containsVertex("B")).isTrue();
            assertThat(subgraph.containsEdge("A", "B")).isTrue();
            assertThat(subgraph.containsEdge("B", "C")).isFalse();
        }

        @Test
        @DisplayName("空边集返回空图")
        void testEmptyEdges() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            Graph<String> subgraph = SubgraphUtil.edgeInduced(graph, Set.of());

            assertThat(subgraph.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("filterVertices测试")
    class FilterVerticesTests {

        @Test
        @DisplayName("按谓词过滤顶点")
        void testFilterVertices() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A1", "A2");
            graph.addEdge("A2", "B1");
            graph.addEdge("B1", "B2");

            Graph<String> filtered = SubgraphUtil.filterVertices(graph, v -> v.startsWith("A"));

            assertThat(filtered.containsVertex("A1")).isTrue();
            assertThat(filtered.containsVertex("A2")).isTrue();
            assertThat(filtered.containsVertex("B1")).isFalse();
        }

        @Test
        @DisplayName("null参数返回空图")
        void testNullParams() {
            assertThat(SubgraphUtil.filterVertices(null, v -> true).isEmpty()).isTrue();
            assertThat(SubgraphUtil.filterVertices(OpenGraph.directed(), null).isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("filterEdges测试")
    class FilterEdgesTests {

        @Test
        @DisplayName("按谓词过滤边")
        void testFilterEdges() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 5.0);
            graph.addEdge("C", "D", 2.0);

            Graph<String> filtered = SubgraphUtil.filterEdges(graph, e -> e.weight() < 3.0);

            assertThat(filtered.containsEdge("A", "B")).isTrue();
            assertThat(filtered.containsEdge("B", "C")).isFalse();
            assertThat(filtered.containsEdge("C", "D")).isTrue();
        }
    }

    @Nested
    @DisplayName("filterByWeight测试")
    class FilterByWeightTests {

        @Test
        @DisplayName("按权重范围过滤")
        void testFilterByWeight() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 5.0);
            graph.addEdge("C", "D", 3.0);

            Graph<String> filtered = SubgraphUtil.filterByWeight(graph, 2.0, 4.0);

            assertThat(filtered.containsEdge("A", "B")).isFalse();
            assertThat(filtered.containsEdge("B", "C")).isFalse();
            assertThat(filtered.containsEdge("C", "D")).isTrue();
        }
    }

    @Nested
    @DisplayName("neighborhood测试")
    class NeighborhoodTests {

        @Test
        @DisplayName("提取k跳邻域")
        void testNeighborhood() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            Graph<String> neighborhood = SubgraphUtil.neighborhood(graph, "A", 2);

            assertThat(neighborhood.containsVertex("A")).isTrue();
            assertThat(neighborhood.containsVertex("B")).isTrue();
            assertThat(neighborhood.containsVertex("C")).isTrue();
            assertThat(neighborhood.containsVertex("D")).isFalse();
        }

        @Test
        @DisplayName("k=0只包含中心顶点")
        void testNeighborhoodZero() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            Graph<String> neighborhood = SubgraphUtil.neighborhood(graph, "A", 0);

            assertThat(neighborhood.vertexCount()).isEqualTo(1);
            assertThat(neighborhood.containsVertex("A")).isTrue();
        }

        @Test
        @DisplayName("负k返回空图")
        void testNegativeK() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            Graph<String> neighborhood = SubgraphUtil.neighborhood(graph, "A", -1);

            assertThat(neighborhood.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("顶点不存在返回空图")
        void testVertexNotFound() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            Graph<String> neighborhood = SubgraphUtil.neighborhood(graph, "X", 1);

            assertThat(neighborhood.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("egoNetwork测试")
    class EgoNetworkTests {

        @Test
        @DisplayName("提取自我网络（1跳）")
        void testEgoNetwork() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");

            Graph<String> ego = SubgraphUtil.egoNetwork(graph, "A");

            assertThat(ego.containsVertex("A")).isTrue();
            assertThat(ego.containsVertex("B")).isTrue();
            assertThat(ego.containsVertex("C")).isTrue();
            assertThat(ego.containsVertex("D")).isFalse();
        }

        @Test
        @DisplayName("指定半径的自我网络")
        void testEgoNetworkWithRadius() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Graph<String> ego = SubgraphUtil.egoNetwork(graph, "A", 2);

            assertThat(ego.vertexCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("union测试")
    class UnionTests {

        @Test
        @DisplayName("计算两图并集")
        void testUnion() {
            Graph<String> g1 = OpenGraph.directed();
            g1.addEdge("A", "B");

            Graph<String> g2 = OpenGraph.directed();
            g2.addEdge("C", "D");

            Graph<String> union = SubgraphUtil.union(g1, g2);

            assertThat(union.containsVertex("A")).isTrue();
            assertThat(union.containsVertex("C")).isTrue();
            assertThat(union.containsEdge("A", "B")).isTrue();
            assertThat(union.containsEdge("C", "D")).isTrue();
        }

        @Test
        @DisplayName("null图处理")
        void testNullGraphs() {
            Graph<String> g = OpenGraph.directed();
            g.addEdge("A", "B");

            assertThat(SubgraphUtil.union(null, g).containsEdge("A", "B")).isTrue();
            assertThat(SubgraphUtil.union(g, null).containsEdge("A", "B")).isTrue();
            assertThat(SubgraphUtil.union(null, null).isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("intersection测试")
    class IntersectionTests {

        @Test
        @DisplayName("计算两图交集")
        void testIntersection() {
            Graph<String> g1 = OpenGraph.directed();
            g1.addEdge("A", "B");
            g1.addEdge("B", "C");

            Graph<String> g2 = OpenGraph.directed();
            g2.addEdge("A", "B");
            g2.addEdge("D", "E");

            Graph<String> intersection = SubgraphUtil.intersection(g1, g2);

            assertThat(intersection.containsEdge("A", "B")).isTrue();
            assertThat(intersection.containsEdge("B", "C")).isFalse();
            assertThat(intersection.containsEdge("D", "E")).isFalse();
        }

        @Test
        @DisplayName("null图返回空图")
        void testNullGraphs() {
            Graph<String> g = OpenGraph.directed();
            g.addVertex("A");

            assertThat(SubgraphUtil.intersection(null, g).isEmpty()).isTrue();
            assertThat(SubgraphUtil.intersection(g, null).isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("difference测试")
    class DifferenceTests {

        @Test
        @DisplayName("计算两图差集")
        void testDifference() {
            Graph<String> g1 = OpenGraph.directed();
            g1.addVertex("A");
            g1.addVertex("B");
            g1.addVertex("C");

            Graph<String> g2 = OpenGraph.directed();
            g2.addVertex("B");

            Graph<String> diff = SubgraphUtil.difference(g1, g2);

            assertThat(diff.containsVertex("A")).isTrue();
            assertThat(diff.containsVertex("C")).isTrue();
            assertThat(diff.containsVertex("B")).isFalse();
        }
    }

    @Nested
    @DisplayName("symmetricDifference测试")
    class SymmetricDifferenceTests {

        @Test
        @DisplayName("计算对称差集")
        void testSymmetricDifference() {
            Graph<String> g1 = OpenGraph.directed();
            g1.addVertex("A");
            g1.addVertex("B");

            Graph<String> g2 = OpenGraph.directed();
            g2.addVertex("B");
            g2.addVertex("C");

            Graph<String> symDiff = SubgraphUtil.symmetricDifference(g1, g2);

            assertThat(symDiff.containsVertex("A")).isTrue();
            assertThat(symDiff.containsVertex("C")).isTrue();
        }
    }

    @Nested
    @DisplayName("reverse测试")
    class ReverseTests {

        @Test
        @DisplayName("反转有向图")
        void testReverse() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            Graph<String> reversed = SubgraphUtil.reverse(graph);

            assertThat(reversed.containsEdge("B", "A")).isTrue();
            assertThat(reversed.containsEdge("C", "B")).isTrue();
            assertThat(reversed.containsEdge("A", "B")).isFalse();
        }

        @Test
        @DisplayName("null图返回空图")
        void testNullGraph() {
            Graph<String> reversed = SubgraphUtil.reverse(null);

            assertThat(reversed.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("copy测试")
    class CopyTests {

        @Test
        @DisplayName("复制图")
        void testCopy() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            Graph<String> copy = SubgraphUtil.copy(graph);

            assertThat(copy.containsEdge("A", "B")).isTrue();
            assertThat(copy.containsEdge("B", "C")).isTrue();
            assertThat(copy.getWeight("A", "B")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("null图返回空图")
        void testNullGraph() {
            Graph<String> copy = SubgraphUtil.copy(null);

            assertThat(copy.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("complement测试")
    class ComplementTests {

        @Test
        @DisplayName("计算补图")
        void testComplement() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");
            graph.addEdge("A", "B");

            Graph<String> complement = SubgraphUtil.complement(graph);

            assertThat(complement.containsEdge("A", "B")).isFalse();
            assertThat(complement.containsEdge("A", "C")).isTrue();
            assertThat(complement.containsEdge("B", "C")).isTrue();
        }

        @Test
        @DisplayName("null图返回空图")
        void testNullGraph() {
            Graph<String> complement = SubgraphUtil.complement(null);

            assertThat(complement.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("removeIsolated测试")
    class RemoveIsolatedTests {

        @Test
        @DisplayName("移除孤立顶点")
        void testRemoveIsolated() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addVertex("C"); // Isolated

            Graph<String> result = SubgraphUtil.removeIsolated(graph);

            assertThat(result.containsVertex("A")).isTrue();
            assertThat(result.containsVertex("B")).isTrue();
            assertThat(result.containsVertex("C")).isFalse();
        }
    }

    @Nested
    @DisplayName("sampleVertices测试")
    class SampleVerticesTests {

        @Test
        @DisplayName("采样顶点子图")
        void testSampleVertices() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");
            graph.addEdge("D", "E");

            Graph<String> sampled = SubgraphUtil.sampleVertices(graph, 3, new Random(42));

            assertThat(sampled.vertexCount()).isLessThanOrEqualTo(3);
        }

        @Test
        @DisplayName("null图返回空图")
        void testNullGraph() {
            Graph<String> sampled = SubgraphUtil.sampleVertices(null, 5, new Random());

            assertThat(sampled.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("sampleEdges测试")
    class SampleEdgesTests {

        @Test
        @DisplayName("采样边子图")
        void testSampleEdges() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");
            graph.addEdge("D", "E");

            Graph<String> sampled = SubgraphUtil.sampleEdges(graph, 2, new Random(42));

            assertThat(sampled.edgeCount()).isLessThanOrEqualTo(2);
        }

        @Test
        @DisplayName("null图返回空图")
        void testNullGraph() {
            Graph<String> sampled = SubgraphUtil.sampleEdges(null, 5, new Random());

            assertThat(sampled.isEmpty()).isTrue();
        }
    }
}
