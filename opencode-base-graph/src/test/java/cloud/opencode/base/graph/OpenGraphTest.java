package cloud.opencode.base.graph;

import cloud.opencode.base.graph.builder.GraphBuilder;
import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenGraph 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("OpenGraph 测试")
class OpenGraphTest {

    @Nested
    @DisplayName("图创建测试")
    class GraphCreationTests {

        @Test
        @DisplayName("创建有向图")
        void testDirected() {
            Graph<String> graph = OpenGraph.directed();

            assertThat(graph).isNotNull();
            assertThat(graph.isDirected()).isTrue();
        }

        @Test
        @DisplayName("创建无向图")
        void testUndirected() {
            Graph<String> graph = OpenGraph.undirected();

            assertThat(graph).isNotNull();
            assertThat(graph.isDirected()).isFalse();
        }

        @Test
        @DisplayName("创建有向加权图")
        void testDirectedWeighted() {
            WeightedGraph<String> graph = OpenGraph.directedWeighted();

            assertThat(graph).isNotNull();
            assertThat(graph.isDirected()).isTrue();
        }

        @Test
        @DisplayName("创建无向加权图")
        void testUndirectedWeighted() {
            WeightedGraph<String> graph = OpenGraph.undirectedWeighted();

            assertThat(graph).isNotNull();
            assertThat(graph.isDirected()).isFalse();
        }

        @Test
        @DisplayName("创建有向图构建器")
        void testDirectedBuilder() {
            GraphBuilder<String> builder = OpenGraph.directedBuilder();

            assertThat(builder).isNotNull();
            Graph<String> graph = builder.addEdge("A", "B").build();
            assertThat(graph.isDirected()).isTrue();
        }

        @Test
        @DisplayName("创建无向图构建器")
        void testUndirectedBuilder() {
            GraphBuilder<String> builder = OpenGraph.undirectedBuilder();

            assertThat(builder).isNotNull();
            Graph<String> graph = builder.addEdge("A", "B").build();
            assertThat(graph.isDirected()).isFalse();
        }
    }

    @Nested
    @DisplayName("图遍历测试")
    class GraphTraversalTests {

        @Test
        @DisplayName("BFS遍历")
        void testBfs() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");

            List<String> result = OpenGraph.bfs(graph, "A");

            assertThat(result).startsWith("A");
            assertThat(result).containsAll(List.of("A", "B", "C", "D"));
        }

        @Test
        @DisplayName("DFS遍历")
        void testDfs() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");

            List<String> result = OpenGraph.dfs(graph, "A");

            assertThat(result).startsWith("A");
            assertThat(result).containsAll(List.of("A", "B", "C", "D"));
        }

        @Test
        @DisplayName("迭代式DFS遍历")
        void testDfsIterative() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            List<String> result = OpenGraph.dfsIterative(graph, "A");

            assertThat(result).startsWith("A");
            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("最短路径测试")
    class ShortestPathTests {

        @Test
        @DisplayName("Dijkstra算法")
        void testDijkstra() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 5.0);

            Map<String, Double> distances = OpenGraph.dijkstra(graph, "A");

            assertThat(distances.get("A")).isEqualTo(0.0);
            assertThat(distances.get("B")).isEqualTo(1.0);
            assertThat(distances.get("C")).isEqualTo(3.0); // A->B->C = 3
        }

        @Test
        @DisplayName("查找最短路径")
        void testShortestPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 5.0);

            List<String> path = OpenGraph.shortestPath(graph, "A", "C");

            assertThat(path).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("A*算法")
        void testAStar() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("A", "C", 3.0);

            // 简单的启发式函数：总是返回0
            List<String> path = OpenGraph.aStar(graph, "A", "C", (v1, v2) -> 0.0);

            assertThat(path).isNotEmpty();
            assertThat(path.get(0)).isEqualTo("A");
            assertThat(path.get(path.size() - 1)).isEqualTo("C");
        }

        @Test
        @DisplayName("双向BFS")
        void testBidirectionalBfs() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            List<String> path = OpenGraph.bidirectionalBfs(graph, "A", "D");

            assertThat(path).isNotEmpty();
            assertThat(path.get(0)).isEqualTo("A");
            assertThat(path.get(path.size() - 1)).isEqualTo("D");
        }
    }

    @Nested
    @DisplayName("拓扑排序测试")
    class TopologicalSortTests {

        @Test
        @DisplayName("拓扑排序")
        void testTopologicalSort() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");
            graph.addEdge("C", "D");

            List<String> order = OpenGraph.topologicalSort(graph);

            assertThat(order).hasSize(4);
            assertThat(order.indexOf("A")).isLessThan(order.indexOf("B"));
            assertThat(order.indexOf("A")).isLessThan(order.indexOf("C"));
            assertThat(order.indexOf("B")).isLessThan(order.indexOf("D"));
        }

        @Test
        @DisplayName("检查是否可以拓扑排序")
        void testCanTopologicalSort() {
            Graph<String> dag = OpenGraph.directed();
            dag.addEdge("A", "B");
            dag.addEdge("B", "C");

            Graph<String> cyclic = OpenGraph.directed();
            cyclic.addEdge("A", "B");
            cyclic.addEdge("B", "A");

            assertThat(OpenGraph.canTopologicalSort(dag)).isTrue();
            assertThat(OpenGraph.canTopologicalSort(cyclic)).isFalse();
        }
    }

    @Nested
    @DisplayName("环检测测试")
    class CycleDetectionTests {

        @Test
        @DisplayName("检测环")
        void testHasCycle() {
            Graph<String> cyclic = OpenGraph.directed();
            cyclic.addEdge("A", "B");
            cyclic.addEdge("B", "C");
            cyclic.addEdge("C", "A");

            Graph<String> acyclic = OpenGraph.directed();
            acyclic.addEdge("A", "B");
            acyclic.addEdge("B", "C");

            assertThat(OpenGraph.hasCycle(cyclic)).isTrue();
            assertThat(OpenGraph.hasCycle(acyclic)).isFalse();
        }

        @Test
        @DisplayName("查找环")
        void testFindCycle() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            List<String> cycle = OpenGraph.findCycle(graph);

            assertThat(cycle).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("连通性测试")
    class ConnectivityTests {

        @Test
        @DisplayName("查找连通分量")
        void testConnectedComponents() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("C", "D");

            List<Set<String>> components = OpenGraph.connectedComponents(graph);

            assertThat(components).hasSize(2);
        }

        @Test
        @DisplayName("检查两顶点是否连通")
        void testIsConnected() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addVertex("D");

            assertThat(OpenGraph.isConnected(graph, "A", "C")).isTrue();
            assertThat(OpenGraph.isConnected(graph, "A", "D")).isFalse();
        }

        @Test
        @DisplayName("检查图是否完全连通")
        void testIsFullyConnected() {
            Graph<String> connected = OpenGraph.undirected();
            connected.addEdge("A", "B");
            connected.addEdge("B", "C");

            Graph<String> disconnected = OpenGraph.undirected();
            disconnected.addEdge("A", "B");
            disconnected.addVertex("C");

            assertThat(OpenGraph.isFullyConnected(connected)).isTrue();
            assertThat(OpenGraph.isFullyConnected(disconnected)).isFalse();
        }

        @Test
        @DisplayName("获取连通分量数量")
        void testConnectedComponentCount() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("C", "D");
            graph.addVertex("E");

            assertThat(OpenGraph.connectedComponentCount(graph)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("最小生成树测试")
    class MstTests {

        @Test
        @DisplayName("Prim算法")
        void testPrim() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 3.0);

            Set<Edge<String>> mst = OpenGraph.prim(graph);

            assertThat(mst).hasSize(2);
        }

        @Test
        @DisplayName("Kruskal算法")
        void testKruskal() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 3.0);

            Set<Edge<String>> mst = OpenGraph.kruskal(graph);

            assertThat(mst).hasSize(2);
        }

        @Test
        @DisplayName("MST权重")
        void testMstWeight() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 5.0);

            double weight = OpenGraph.mstWeight(graph);

            assertThat(weight).isEqualTo(3.0);
        }

        @Test
        @DisplayName("检查是否有生成树")
        void testHasSpanningTree() {
            Graph<String> connected = OpenGraph.undirected();
            connected.addEdge("A", "B");

            Graph<String> disconnected = OpenGraph.undirected();
            disconnected.addVertex("A");
            disconnected.addVertex("B");

            assertThat(OpenGraph.hasSpanningTree(connected)).isTrue();
            assertThat(OpenGraph.hasSpanningTree(disconnected)).isFalse();
        }
    }

    @Nested
    @DisplayName("网络流测试")
    class NetworkFlowTests {

        @Test
        @DisplayName("计算最大流")
        void testMaxFlow() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("S", "A", 10.0);
            graph.addEdge("S", "B", 5.0);
            graph.addEdge("A", "T", 5.0);
            graph.addEdge("B", "T", 10.0);
            graph.addEdge("A", "B", 15.0);

            double maxFlow = OpenGraph.maxFlow(graph, "S", "T");

            assertThat(maxFlow).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("工具类测试")
    class UtilityClassTests {

        @Test
        @DisplayName("无法实例化")
        void testCannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = OpenGraph.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }
}
