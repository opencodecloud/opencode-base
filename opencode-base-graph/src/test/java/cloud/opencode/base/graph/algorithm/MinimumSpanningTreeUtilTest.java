package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.*;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * MinimumSpanningTreeUtil Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
class MinimumSpanningTreeUtilTest {

    @Nested
    @DisplayName("Prim Algorithm Tests")
    class PrimAlgorithmTests {

        @Test
        void shouldFindMstForSimpleGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 3.0);

            Set<Edge<String>> mst = MinimumSpanningTreeUtil.prim(graph, "A");

            assertThat(mst).hasSize(2);
            double totalWeight = MinimumSpanningTreeUtil.totalWeight(mst);
            assertThat(totalWeight).isEqualTo(3.0); // 1 + 2
        }

        @Test
        void shouldFindMstForComplexGraph() {
            Graph<String> graph = OpenGraph.undirected();
            // Classic MST example
            graph.addEdge("A", "B", 4.0);
            graph.addEdge("A", "C", 2.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("B", "D", 5.0);
            graph.addEdge("C", "D", 8.0);
            graph.addEdge("C", "E", 10.0);
            graph.addEdge("D", "E", 2.0);

            Set<Edge<String>> mst = MinimumSpanningTreeUtil.prim(graph);

            assertThat(mst).hasSize(4); // V - 1 edges
            double totalWeight = MinimumSpanningTreeUtil.totalWeight(mst);
            // MST: B-C(1) + A-C(2) + D-E(2) + B-D(5) = 10
            assertThat(totalWeight).isEqualTo(10.0);
        }

        @Test
        void shouldReturnEmptyForNullGraph() {
            Set<Edge<String>> mst = MinimumSpanningTreeUtil.prim(null, "A");
            assertThat(mst).isEmpty();
        }

        @Test
        void shouldReturnEmptyForEmptyGraph() {
            Graph<String> graph = OpenGraph.undirected();
            Set<Edge<String>> mst = MinimumSpanningTreeUtil.prim(graph);
            assertThat(mst).isEmpty();
        }

        @Test
        void shouldReturnEmptyForSingleVertex() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            Set<Edge<String>> mst = MinimumSpanningTreeUtil.prim(graph, "A");
            assertThat(mst).isEmpty();
        }

        @Test
        void shouldReturnEmptyForInvalidStart() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);

            Set<Edge<String>> mst = MinimumSpanningTreeUtil.prim(graph, "C");
            assertThat(mst).isEmpty();
        }

        @Test
        void shouldAutoSelectStart() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            Set<Edge<String>> mst = MinimumSpanningTreeUtil.prim(graph);
            assertThat(mst).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Kruskal Algorithm Tests")
    class KruskalAlgorithmTests {

        @Test
        void shouldFindMstForSimpleGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 3.0);

            Set<Edge<String>> mst = MinimumSpanningTreeUtil.kruskal(graph);

            assertThat(mst).hasSize(2);
            double totalWeight = MinimumSpanningTreeUtil.totalWeight(mst);
            assertThat(totalWeight).isEqualTo(3.0);
        }

        @Test
        void shouldFindMstForComplexGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 4.0);
            graph.addEdge("A", "C", 2.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("B", "D", 5.0);
            graph.addEdge("C", "D", 8.0);
            graph.addEdge("C", "E", 10.0);
            graph.addEdge("D", "E", 2.0);

            Set<Edge<String>> mst = MinimumSpanningTreeUtil.kruskal(graph);

            assertThat(mst).hasSize(4);
            double totalWeight = MinimumSpanningTreeUtil.totalWeight(mst);
            // MST: B-C(1) + A-C(2) + D-E(2) + B-D(5) = 10
            assertThat(totalWeight).isEqualTo(10.0);
        }

        @Test
        void shouldReturnEmptyForNullGraph() {
            Set<Edge<String>> mst = MinimumSpanningTreeUtil.kruskal(null);
            assertThat(mst).isEmpty();
        }

        @Test
        void shouldReturnEmptyForSingleVertex() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            Set<Edge<String>> mst = MinimumSpanningTreeUtil.kruskal(graph);
            assertThat(mst).isEmpty();
        }

        @Test
        void shouldHandleDisconnectedGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("C", "D", 2.0);

            Set<Edge<String>> mst = MinimumSpanningTreeUtil.kruskal(graph);

            // Should return edges that don't form cycles
            assertThat(mst).hasSize(2);
        }
    }

    @Nested
    @DisplayName("MST Weight Tests")
    class MstWeightTests {

        @Test
        void shouldCalculateMstWeight() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 3.0);

            double weight = MinimumSpanningTreeUtil.mstWeight(graph);
            assertThat(weight).isEqualTo(3.0);
        }

        @Test
        void shouldReturnZeroForEmptyGraph() {
            Graph<String> graph = OpenGraph.undirected();
            double weight = MinimumSpanningTreeUtil.mstWeight(graph);
            assertThat(weight).isEqualTo(0.0);
        }

        @Test
        void shouldCalculateTotalWeightOfEdges() {
            Set<Edge<String>> edges = Set.of(
                    new Edge<>("A", "B", 1.0),
                    new Edge<>("B", "C", 2.0)
            );

            double weight = MinimumSpanningTreeUtil.totalWeight(edges);
            assertThat(weight).isEqualTo(3.0);
        }

        @Test
        void shouldReturnZeroForNullEdges() {
            double weight = MinimumSpanningTreeUtil.totalWeight(null);
            assertThat(weight).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Spanning Tree Check Tests")
    class SpanningTreeCheckTests {

        @Test
        void shouldReturnTrueForConnectedGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            assertThat(MinimumSpanningTreeUtil.hasSpanningTree(graph)).isTrue();
        }

        @Test
        void shouldReturnFalseForDisconnectedGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addVertex("C"); // Isolated vertex

            assertThat(MinimumSpanningTreeUtil.hasSpanningTree(graph)).isFalse();
        }

        @Test
        void shouldReturnTrueForSingleVertex() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            assertThat(MinimumSpanningTreeUtil.hasSpanningTree(graph)).isTrue();
        }

        @Test
        void shouldReturnFalseForEmptyGraph() {
            Graph<String> graph = OpenGraph.undirected();
            assertThat(MinimumSpanningTreeUtil.hasSpanningTree(graph)).isFalse();
        }

        @Test
        void shouldReturnFalseForNullGraph() {
            assertThat(MinimumSpanningTreeUtil.hasSpanningTree(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Minimum Spanning Forest Tests")
    class MinimumSpanningForestTests {

        @Test
        void shouldFindForestForDisconnectedGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("D", "E", 3.0);

            Set<Edge<String>> forest = MinimumSpanningTreeUtil.minimumSpanningForest(graph);

            assertThat(forest).hasSize(3); // 2 + 1 for two components
        }

        @Test
        void shouldCountComponents() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("D", "E", 2.0);
            graph.addVertex("X");

            int count = MinimumSpanningTreeUtil.componentCount(graph);
            assertThat(count).isEqualTo(3); // {A,B}, {D,E}, {X}
        }

        @Test
        void shouldReturnOneForConnectedGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            int count = MinimumSpanningTreeUtil.componentCount(graph);
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Union-Find Tests")
    class UnionFindTests {

        @Test
        void shouldInitializeEachElementAsOwnRoot() {
            Set<String> vertices = Set.of("A", "B", "C");
            MinimumSpanningTreeUtil.UnionFind<String> uf = new MinimumSpanningTreeUtil.UnionFind<>(vertices);

            assertThat(uf.find("A")).isEqualTo("A");
            assertThat(uf.find("B")).isEqualTo("B");
            assertThat(uf.find("C")).isEqualTo("C");
        }

        @Test
        void shouldUnionElements() {
            Set<String> vertices = Set.of("A", "B", "C");
            MinimumSpanningTreeUtil.UnionFind<String> uf = new MinimumSpanningTreeUtil.UnionFind<>(vertices);

            uf.union("A", "B");

            assertThat(uf.connected("A", "B")).isTrue();
            assertThat(uf.connected("A", "C")).isFalse();
        }

        @Test
        void shouldHandleTransitiveUnion() {
            Set<String> vertices = Set.of("A", "B", "C");
            MinimumSpanningTreeUtil.UnionFind<String> uf = new MinimumSpanningTreeUtil.UnionFind<>(vertices);

            uf.union("A", "B");
            uf.union("B", "C");

            assertThat(uf.connected("A", "C")).isTrue();
        }
    }

    @Nested
    @DisplayName("Prim vs Kruskal Comparison Tests")
    class ComparisonTests {

        @Test
        void shouldProduceSameWeightForBothAlgorithms() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 4.0);
            graph.addEdge("A", "C", 2.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("B", "D", 5.0);
            graph.addEdge("C", "D", 8.0);
            graph.addEdge("C", "E", 10.0);
            graph.addEdge("D", "E", 2.0);
            graph.addEdge("D", "F", 3.0);
            graph.addEdge("E", "F", 6.0);

            Set<Edge<String>> mstPrim = MinimumSpanningTreeUtil.prim(graph);
            Set<Edge<String>> mstKruskal = MinimumSpanningTreeUtil.kruskal(graph);

            double weightPrim = MinimumSpanningTreeUtil.totalWeight(mstPrim);
            double weightKruskal = MinimumSpanningTreeUtil.totalWeight(mstKruskal);

            assertThat(weightPrim).isEqualTo(weightKruskal);
            assertThat(mstPrim).hasSameSizeAs(mstKruskal);
        }
    }

    @Nested
    @DisplayName("OpenGraph Integration Tests")
    class OpenGraphIntegrationTests {

        @Test
        void shouldUsePrimFromOpenGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            Set<Edge<String>> mst = OpenGraph.prim(graph);
            assertThat(mst).hasSize(2);
        }

        @Test
        void shouldUseKruskalFromOpenGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            Set<Edge<String>> mst = OpenGraph.kruskal(graph);
            assertThat(mst).hasSize(2);
        }

        @Test
        void shouldCalculateMstWeightFromOpenGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 5.0);

            double weight = OpenGraph.mstWeight(graph);
            assertThat(weight).isEqualTo(3.0);
        }

        @Test
        void shouldCheckSpanningTreeFromOpenGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);

            assertThat(OpenGraph.hasSpanningTree(graph)).isTrue();
        }
    }
}
