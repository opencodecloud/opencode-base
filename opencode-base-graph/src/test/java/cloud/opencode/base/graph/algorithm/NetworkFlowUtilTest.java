package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * NetworkFlowUtil Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
class NetworkFlowUtilTest {

    @Nested
    @DisplayName("Max Flow Basic Tests")
    class MaxFlowBasicTests {

        @Test
        void shouldCalculateMaxFlowForSimpleGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("S", "B", 10.0);
            graph.addEdge("A", "T", 10.0);
            graph.addEdge("B", "T", 10.0);

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(20.0);
        }

        @Test
        void shouldCalculateMaxFlowWithBottleneck() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("A", "B", 5.0);  // Bottleneck
            graph.addEdge("B", "T", 10.0);

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(5.0);
        }

        @Test
        void shouldHandleParallelPaths() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 3.0);
            graph.addEdge("S", "B", 2.0);
            graph.addEdge("A", "T", 2.0);
            graph.addEdge("B", "T", 3.0);

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(4.0); // min(3,2) + min(2,3)
        }

        @Test
        void shouldReturnZeroForNoPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addVertex("T");

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(0.0);
        }

        @Test
        void shouldReturnZeroForNullGraph() {
            double maxFlow = NetworkFlowUtil.maxFlow(null, "S", "T");
            assertThat(maxFlow).isEqualTo(0.0);
        }

        @Test
        void shouldReturnZeroForNullSource() {
            Graph<String> graph = OpenGraph.directed();
            double maxFlow = NetworkFlowUtil.maxFlow(graph, null, "T");
            assertThat(maxFlow).isEqualTo(0.0);
        }

        @Test
        void shouldReturnZeroForSameSourceAndSink() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("S");

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "S");
            assertThat(maxFlow).isEqualTo(0.0);
        }

        @Test
        void shouldReturnZeroForMissingVertices() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");
            assertThat(maxFlow).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Max Flow DFS Tests")
    class MaxFlowDfsTests {

        @Test
        void shouldCalculateMaxFlowUsingDfs() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("S", "B", 10.0);
            graph.addEdge("A", "T", 10.0);
            graph.addEdge("B", "T", 10.0);

            double maxFlow = NetworkFlowUtil.maxFlowDfs(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(20.0);
        }

        @Test
        void shouldMatchBfsResult() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 3.0);
            graph.addEdge("S", "B", 2.0);
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("A", "T", 2.0);
            graph.addEdge("B", "T", 3.0);

            double maxFlowBfs = NetworkFlowUtil.maxFlow(graph, "S", "T");
            double maxFlowDfs = NetworkFlowUtil.maxFlowDfs(graph, "S", "T");

            assertThat(maxFlowDfs).isEqualTo(maxFlowBfs);
        }
    }

    @Nested
    @DisplayName("Complex Flow Network Tests")
    class ComplexFlowNetworkTests {

        @Test
        void shouldHandleComplexNetwork() {
            // Classic max flow example
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 16.0);
            graph.addEdge("S", "C", 13.0);
            graph.addEdge("A", "B", 12.0);
            graph.addEdge("A", "C", 10.0);
            graph.addEdge("B", "C", 9.0);
            graph.addEdge("B", "T", 20.0);
            graph.addEdge("C", "A", 4.0);
            graph.addEdge("C", "D", 14.0);
            graph.addEdge("D", "B", 7.0);
            graph.addEdge("D", "T", 4.0);

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(23.0);
        }

        @Test
        void shouldHandleDiamondNetwork() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 5.0);
            graph.addEdge("S", "B", 5.0);
            graph.addEdge("A", "C", 3.0);
            graph.addEdge("A", "D", 3.0);
            graph.addEdge("B", "C", 3.0);
            graph.addEdge("B", "D", 3.0);
            graph.addEdge("C", "T", 5.0);
            graph.addEdge("D", "T", 5.0);

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(10.0);
        }
    }

    @Nested
    @DisplayName("Get Flows Tests")
    class GetFlowsTests {

        @Test
        void shouldReturnFlowsOnEdges() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("A", "T", 10.0);

            Map<Edge<String>, Double> flows = NetworkFlowUtil.getFlows(graph, "S", "T");

            assertThat(flows).isNotEmpty();
            assertThat(flows.values().stream().mapToDouble(d -> d).sum()).isGreaterThan(0);
        }

        @Test
        void shouldReturnEmptyForNoFlow() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("S");
            graph.addVertex("T");

            Map<Edge<String>, Double> flows = NetworkFlowUtil.getFlows(graph, "S", "T");

            assertThat(flows).isEmpty();
        }

        @Test
        void shouldReturnEmptyForNullGraph() {
            Map<Edge<String>, Double> flows = NetworkFlowUtil.getFlows(null, "S", "T");
            assertThat(flows).isEmpty();
        }
    }

    @Nested
    @DisplayName("Min Cut Tests")
    class MinCutTests {

        @Test
        void shouldFindMinCut() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("A", "B", 5.0);  // Bottleneck - part of min cut
            graph.addEdge("B", "T", 10.0);

            Set<Edge<String>> minCut = NetworkFlowUtil.minCut(graph, "S", "T");

            assertThat(minCut).isNotEmpty();
        }

        @Test
        void shouldHaveMinCutCapacityEqualMaxFlow() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("S", "B", 5.0);
            graph.addEdge("A", "T", 8.0);
            graph.addEdge("B", "T", 7.0);

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");
            double minCutCapacity = NetworkFlowUtil.minCutCapacity(graph, "S", "T");

            assertThat(minCutCapacity).isEqualTo(maxFlow);
        }

        @Test
        void shouldReturnEmptyForNullGraph() {
            Set<Edge<String>> minCut = NetworkFlowUtil.minCut(null, "S", "T");
            assertThat(minCut).isEmpty();
        }
    }

    @Nested
    @DisplayName("Compute Flow Result Tests")
    class ComputeFlowResultTests {

        @Test
        void shouldReturnCompleteFlowResult() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("A", "T", 10.0);

            NetworkFlowUtil.FlowResult<String> result =
                    NetworkFlowUtil.computeFlow(graph, "S", "T");

            assertThat(result.maxFlow()).isEqualTo(10.0);
            assertThat(result.edgeFlows()).isNotEmpty();
            assertThat(result.minCut()).isNotEmpty();
            assertThat(result.hasFlow()).isTrue();
            assertThat(result.minCutCapacity()).isEqualTo(10.0);
        }

        @Test
        void shouldReturnEmptyResultForNoPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("S");
            graph.addVertex("T");

            NetworkFlowUtil.FlowResult<String> result =
                    NetworkFlowUtil.computeFlow(graph, "S", "T");

            assertThat(result.maxFlow()).isEqualTo(0.0);
            assertThat(result.hasFlow()).isFalse();
        }

        @Test
        void shouldReturnEmptyResultForNullGraph() {
            NetworkFlowUtil.FlowResult<String> result =
                    NetworkFlowUtil.computeFlow(null, "S", "T");

            assertThat(result.maxFlow()).isEqualTo(0.0);
            assertThat(result.edgeFlows()).isEmpty();
            assertThat(result.minCut()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Flow Conservation Tests")
    class FlowConservationTests {

        @Test
        void shouldConserveFlowAtIntermediateNodes() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("S", "B", 10.0);
            graph.addEdge("A", "C", 5.0);
            graph.addEdge("A", "D", 5.0);
            graph.addEdge("B", "C", 5.0);
            graph.addEdge("B", "D", 5.0);
            graph.addEdge("C", "T", 10.0);
            graph.addEdge("D", "T", 10.0);

            NetworkFlowUtil.FlowResult<String> result =
                    NetworkFlowUtil.computeFlow(graph, "S", "T");

            // Max flow should be 20
            assertThat(result.maxFlow()).isEqualTo(20.0);
        }
    }

    @Nested
    @DisplayName("OpenGraph Integration Tests")
    class OpenGraphIntegrationTests {

        @Test
        void shouldUseMaxFlowFromOpenGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("A", "T", 10.0);

            double maxFlow = OpenGraph.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(10.0);
        }

        @Test
        void shouldGetFlowsFromOpenGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("A", "T", 10.0);

            Map<Edge<String>, Double> flows = OpenGraph.getFlows(graph, "S", "T");

            assertThat(flows).isNotEmpty();
        }

        @Test
        void shouldFindMinCutFromOpenGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("A", "T", 5.0);

            Set<Edge<String>> minCut = OpenGraph.minCut(graph, "S", "T");

            assertThat(minCut).isNotEmpty();
        }

        @Test
        void shouldComputeFlowFromOpenGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("A", "T", 10.0);

            NetworkFlowUtil.FlowResult<String> result = OpenGraph.computeFlow(graph, "S", "T");

            assertThat(result.maxFlow()).isEqualTo(10.0);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        void shouldHandleSingleEdge() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "T", 5.0);

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(5.0);
        }

        @Test
        void shouldHandleZeroCapacityEdge() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("A", "T", 0.0);

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(0.0);
        }

        @Test
        void shouldHandleLargeCapacity() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 1_000_000.0);
            graph.addEdge("A", "T", 1_000_000.0);

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(1_000_000.0);
        }

        @Test
        void shouldHandleMultipleSourcePaths() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 5.0);
            graph.addEdge("S", "B", 5.0);
            graph.addEdge("S", "C", 5.0);
            graph.addEdge("A", "T", 5.0);
            graph.addEdge("B", "T", 5.0);
            graph.addEdge("C", "T", 5.0);

            double maxFlow = NetworkFlowUtil.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isEqualTo(15.0);
        }
    }
}
