package cloud.opencode.base.collections.graph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphTraversalUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("GraphTraversalUtil 测试")
class GraphTraversalUtilTest {

    @Nested
    @DisplayName("BFS 遍历测试")
    class BfsTests {

        @Test
        @DisplayName("bfs - 基本广度优先搜索")
        void testBfs() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");
            graph.putEdge("B", "D");
            graph.putEdge("C", "D");

            List<String> result = GraphTraversalUtil.bfs(graph, "A");

            assertThat(result).startsWith("A");
            assertThat(result).containsExactlyInAnyOrder("A", "B", "C", "D");
        }

        @Test
        @DisplayName("bfs - 节点不存在")
        void testBfsNodeNotExists() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            List<String> result = GraphTraversalUtil.bfs(graph, "B");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("bfs - 使用 visitor")
        void testBfsWithVisitor() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");

            List<String> visited = new ArrayList<>();
            GraphTraversalUtil.bfs(graph, "A", visited::add);

            assertThat(visited).startsWith("A");
            assertThat(visited).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("bfsUntil - 找到满足条件的节点")
        void testBfsUntil() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");
            graph.putEdge("C", "Target");

            Optional<String> result = GraphTraversalUtil.bfsUntil(graph, "A", s -> s.equals("Target"));

            assertThat(result).hasValue("Target");
        }

        @Test
        @DisplayName("bfsUntil - 未找到")
        void testBfsUntilNotFound() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");

            Optional<String> result = GraphTraversalUtil.bfsUntil(graph, "A", s -> s.equals("X"));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("DFS 遍历测试")
    class DfsTests {

        @Test
        @DisplayName("dfs - 基本深度优先搜索")
        void testDfs() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");
            graph.putEdge("B", "D");

            List<String> result = GraphTraversalUtil.dfs(graph, "A");

            assertThat(result).startsWith("A");
            assertThat(result).containsExactlyInAnyOrder("A", "B", "C", "D");
        }

        @Test
        @DisplayName("dfs - 节点不存在")
        void testDfsNodeNotExists() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            List<String> result = GraphTraversalUtil.dfs(graph, "B");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("dfsIterative - 迭代式 DFS")
        void testDfsIterative() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");
            graph.putEdge("B", "D");

            List<String> result = GraphTraversalUtil.dfsIterative(graph, "A");

            assertThat(result).startsWith("A");
            assertThat(result).containsExactlyInAnyOrder("A", "B", "C", "D");
        }

        @Test
        @DisplayName("dfs - 使用 visitor")
        void testDfsWithVisitor() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");

            List<String> visited = new ArrayList<>();
            GraphTraversalUtil.dfs(graph, "A", visited::add);

            assertThat(visited).containsExactly("A", "B", "C");
        }
    }

    @Nested
    @DisplayName("拓扑排序测试")
    class TopologicalSortTests {

        @Test
        @DisplayName("topologicalSort - DAG 拓扑排序")
        void testTopologicalSort() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");
            graph.putEdge("B", "D");
            graph.putEdge("C", "D");

            List<String> result = GraphTraversalUtil.topologicalSort(graph);

            // A 应该在 B 和 C 之前，B 和 C 应该在 D 之前
            assertThat(result.indexOf("A")).isLessThan(result.indexOf("B"));
            assertThat(result.indexOf("A")).isLessThan(result.indexOf("C"));
            assertThat(result.indexOf("B")).isLessThan(result.indexOf("D"));
            assertThat(result.indexOf("C")).isLessThan(result.indexOf("D"));
        }

        @Test
        @DisplayName("topologicalSort - 有环抛异常")
        void testTopologicalSortWithCycle() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");
            graph.putEdge("C", "A");

            assertThatThrownBy(() -> GraphTraversalUtil.topologicalSort(graph))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cycle");
        }

        @Test
        @DisplayName("topologicalSort - 无向图抛异常")
        void testTopologicalSortUndirected() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.putEdge("A", "B");

            assertThatThrownBy(() -> GraphTraversalUtil.topologicalSort(graph))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("directed");
        }

        @Test
        @DisplayName("topologicalSortKahn - Kahn 算法")
        void testTopologicalSortKahn() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");
            graph.putEdge("B", "D");
            graph.putEdge("C", "D");

            Optional<List<String>> result = GraphTraversalUtil.topologicalSortKahn(graph);

            assertThat(result).isPresent();
            List<String> order = result.get();
            assertThat(order.indexOf("A")).isLessThan(order.indexOf("B"));
            assertThat(order.indexOf("A")).isLessThan(order.indexOf("C"));
        }

        @Test
        @DisplayName("topologicalSortKahn - 有环返回空")
        void testTopologicalSortKahnWithCycle() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");
            graph.putEdge("C", "A");

            Optional<List<String>> result = GraphTraversalUtil.topologicalSortKahn(graph);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("环检测测试")
    class CycleDetectionTests {

        @Test
        @DisplayName("hasCycle - 有向图有环")
        void testHasCycleDirected() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");
            graph.putEdge("C", "A");

            assertThat(GraphTraversalUtil.hasCycle(graph)).isTrue();
        }

        @Test
        @DisplayName("hasCycle - 有向图无环")
        void testHasNoCycleDirected() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");

            assertThat(GraphTraversalUtil.hasCycle(graph)).isFalse();
        }

        @Test
        @DisplayName("hasCycle - 无向图有环")
        void testHasCycleUndirected() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");
            graph.putEdge("C", "A");

            assertThat(GraphTraversalUtil.hasCycle(graph)).isTrue();
        }

        @Test
        @DisplayName("hasCycle - 无向图无环")
        void testHasNoCycleUndirected() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");

            assertThat(GraphTraversalUtil.hasCycle(graph)).isFalse();
        }

        @Test
        @DisplayName("isDag - 是 DAG")
        void testIsDag() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");

            assertThat(GraphTraversalUtil.isDag(graph)).isTrue();
        }

        @Test
        @DisplayName("isDag - 不是 DAG")
        void testIsNotDag() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "A");

            assertThat(GraphTraversalUtil.isDag(graph)).isFalse();
        }

        @Test
        @DisplayName("isDag - 无向图返回 false")
        void testIsDagUndirected() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.putEdge("A", "B");

            assertThat(GraphTraversalUtil.isDag(graph)).isFalse();
        }
    }

    @Nested
    @DisplayName("路径查找测试")
    class PathFindingTests {

        @Test
        @DisplayName("shortestPath - 找到最短路径")
        void testShortestPath() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");
            graph.putEdge("A", "C");  // 直接路径

            Optional<List<String>> path = GraphTraversalUtil.shortestPath(graph, "A", "C");

            assertThat(path).isPresent();
            assertThat(path.get()).containsExactly("A", "C");
        }

        @Test
        @DisplayName("shortestPath - 无路径")
        void testShortestPathNoPath() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");

            Optional<List<String>> path = GraphTraversalUtil.shortestPath(graph, "A", "B");

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("shortestPath - 源和目标相同")
        void testShortestPathSameNode() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            Optional<List<String>> path = GraphTraversalUtil.shortestPath(graph, "A", "A");

            assertThat(path).isPresent();
            assertThat(path.get()).containsExactly("A");
        }

        @Test
        @DisplayName("hasPath - 有路径")
        void testHasPath() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");

            assertThat(GraphTraversalUtil.hasPath(graph, "A", "C")).isTrue();
        }

        @Test
        @DisplayName("hasPath - 无路径")
        void testHasNoPath() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");

            assertThat(GraphTraversalUtil.hasPath(graph, "A", "B")).isFalse();
        }

        @Test
        @DisplayName("allPaths - 所有路径")
        void testAllPaths() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");
            graph.putEdge("B", "D");
            graph.putEdge("C", "D");

            List<List<String>> paths = GraphTraversalUtil.allPaths(graph, "A", "D", 10);

            assertThat(paths).hasSize(2);
        }
    }

    @Nested
    @DisplayName("连通分量测试")
    class ConnectedComponentsTests {

        @Test
        @DisplayName("connectedComponents - 多个连通分量")
        void testConnectedComponents() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");
            graph.putEdge("D", "E");

            List<Set<String>> components = GraphTraversalUtil.connectedComponents(graph);

            assertThat(components).hasSize(2);
        }

        @Test
        @DisplayName("connectedComponents - 单个连通分量")
        void testConnectedComponentsSingle() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");
            graph.putEdge("C", "A");

            List<Set<String>> components = GraphTraversalUtil.connectedComponents(graph);

            assertThat(components).hasSize(1);
            assertThat(components.getFirst()).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("connectedComponents - 有向图抛异常")
        void testConnectedComponentsDirected() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");

            assertThatThrownBy(() -> GraphTraversalUtil.connectedComponents(graph))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("stronglyConnectedComponents - 强连通分量")
        void testStronglyConnectedComponents() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");
            graph.putEdge("C", "A");
            graph.putEdge("C", "D");

            List<Set<String>> sccs = GraphTraversalUtil.stronglyConnectedComponents(graph);

            // A, B, C 是一个 SCC，D 是另一个
            assertThat(sccs).hasSize(2);
        }

        @Test
        @DisplayName("isConnected - 连通")
        void testIsConnected() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");

            assertThat(GraphTraversalUtil.isConnected(graph)).isTrue();
        }

        @Test
        @DisplayName("isConnected - 不连通")
        void testIsNotConnected() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.putEdge("A", "B");
            graph.addNode("C");

            assertThat(GraphTraversalUtil.isConnected(graph)).isFalse();
        }

        @Test
        @DisplayName("isConnected - 空图")
        void testIsConnectedEmpty() {
            MutableGraph<String> graph = MutableGraph.undirected();

            assertThat(GraphTraversalUtil.isConnected(graph)).isTrue();
        }
    }

    @Nested
    @DisplayName("可达性测试")
    class ReachabilityTests {

        @Test
        @DisplayName("reachableFrom - 从节点可达的所有节点")
        void testReachableFrom() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");
            graph.addNode("D");  // 不可达

            Set<String> reachable = GraphTraversalUtil.reachableFrom(graph, "A");

            assertThat(reachable).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("reachableFrom - 节点不存在")
        void testReachableFromNotExists() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            Set<String> reachable = GraphTraversalUtil.reachableFrom(graph, "B");

            assertThat(reachable).isEmpty();
        }
    }
}
