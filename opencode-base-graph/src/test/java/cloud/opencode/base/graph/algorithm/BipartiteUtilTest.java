package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * BipartiteUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
@DisplayName("BipartiteUtil 测试")
class BipartiteUtilTest {

    @Nested
    @DisplayName("isBipartite 测试")
    class IsBipartiteTests {

        @Test
        @DisplayName("偶数环是二部图: A-B-C-D-A")
        void testEvenCycleBipartite() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");
            graph.addEdge("D", "A");

            assertThat(BipartiteUtil.isBipartite(graph)).isTrue();
        }

        @Test
        @DisplayName("奇数环不是二部图: A-B-C-A")
        void testOddCycleNotBipartite() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            assertThat(BipartiteUtil.isBipartite(graph)).isFalse();
        }

        @Test
        @DisplayName("树总是二部图")
        void testTreeAlwaysBipartite() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");
            graph.addEdge("B", "E");

            assertThat(BipartiteUtil.isBipartite(graph)).isTrue();
        }

        @Test
        @DisplayName("单顶点是二部图")
        void testSingleVertexBipartite() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            assertThat(BipartiteUtil.isBipartite(graph)).isTrue();
        }

        @Test
        @DisplayName("空图是二部图")
        void testEmptyGraphBipartite() {
            Graph<String> graph = OpenGraph.undirected();

            assertThat(BipartiteUtil.isBipartite(graph)).isTrue();
        }

        @Test
        @DisplayName("null图是二部图")
        void testNullGraphBipartite() {
            assertThat(BipartiteUtil.isBipartite(null)).isTrue();
        }

        @Test
        @DisplayName("非连通分量: 所有分量二部图则整体二部图")
        void testDisconnectedAllBipartite() {
            Graph<String> graph = OpenGraph.undirected();
            // Component 1: bipartite (path)
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            // Component 2: bipartite (single edge)
            graph.addEdge("D", "E");

            assertThat(BipartiteUtil.isBipartite(graph)).isTrue();
        }

        @Test
        @DisplayName("非连通分量: 一个分量含奇数环则非二部图")
        void testDisconnectedOneOddCycle() {
            Graph<String> graph = OpenGraph.undirected();
            // Component 1: bipartite
            graph.addEdge("A", "B");
            // Component 2: odd cycle
            graph.addEdge("C", "D");
            graph.addEdge("D", "E");
            graph.addEdge("E", "C");

            assertThat(BipartiteUtil.isBipartite(graph)).isFalse();
        }

        @Test
        @DisplayName("有向图按无向图处理")
        void testDirectedGraphTreatedAsUndirected() {
            // Directed triangle A->B->C->A, treated as undirected = odd cycle
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            assertThat(BipartiteUtil.isBipartite(graph)).isFalse();
        }
    }

    @Nested
    @DisplayName("partition 测试")
    class PartitionTests {

        @Test
        @DisplayName("偶数环的分区正确性")
        void testEvenCyclePartition() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");
            graph.addEdge("D", "A");

            BipartiteUtil.BipartiteResult<String> result = BipartiteUtil.partition(graph);

            assertThat(result.bipartite()).isTrue();
            assertThat(result.oddCycle()).isEmpty();

            // Verify left and right are disjoint
            Set<String> intersection = new HashSet<>(result.left());
            intersection.retainAll(result.right());
            assertThat(intersection).isEmpty();

            // Verify left union right = all vertices
            Set<String> union = new HashSet<>(result.left());
            union.addAll(result.right());
            assertThat(union).containsExactlyInAnyOrder("A", "B", "C", "D");
        }

        @Test
        @DisplayName("奇数环返回奇数环证据")
        void testOddCycleWitness() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            BipartiteUtil.BipartiteResult<String> result = BipartiteUtil.partition(graph);

            assertThat(result.bipartite()).isFalse();
            assertThat(result.left()).isEmpty();
            assertThat(result.right()).isEmpty();

            // Verify odd cycle: must have odd length and form a valid cycle
            List<String> cycle = result.oddCycle();
            assertThat(cycle).isNotEmpty();
            assertThat(cycle.size() % 2).isEqualTo(0); // closed cycle: odd vertices + repeat = even list size
            assertThat(cycle.getFirst()).isEqualTo(cycle.getLast()); // cycle is closed
        }

        @Test
        @DisplayName("树的分区验证")
        void testTreePartition() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");

            BipartiteUtil.BipartiteResult<String> result = BipartiteUtil.partition(graph);

            assertThat(result.bipartite()).isTrue();
            // A and D should be in same partition (both even distance from root)
            if (result.left().contains("A")) {
                assertThat(result.left()).contains("D");
                assertThat(result.right()).contains("B", "C");
            } else {
                assertThat(result.right()).contains("D");
                assertThat(result.left()).contains("B", "C");
            }
        }

        @Test
        @DisplayName("空图分区返回空集")
        void testEmptyPartition() {
            BipartiteUtil.BipartiteResult<String> result = BipartiteUtil.partition(null);

            assertThat(result.bipartite()).isTrue();
            assertThat(result.left()).isEmpty();
            assertThat(result.right()).isEmpty();
            assertThat(result.oddCycle()).isEmpty();
        }

        @Test
        @DisplayName("单顶点分区")
        void testSingleVertexPartition() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            BipartiteUtil.BipartiteResult<String> result = BipartiteUtil.partition(graph);

            assertThat(result.bipartite()).isTrue();
            Set<String> union = new HashSet<>(result.left());
            union.addAll(result.right());
            assertThat(union).containsExactly("A");
        }

        @Test
        @DisplayName("非连通二部图分区")
        void testDisconnectedPartition() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addVertex("C");

            BipartiteUtil.BipartiteResult<String> result = BipartiteUtil.partition(graph);

            assertThat(result.bipartite()).isTrue();
            Set<String> union = new HashSet<>(result.left());
            union.addAll(result.right());
            assertThat(union).containsExactlyInAnyOrder("A", "B", "C");
        }
    }

    @Nested
    @DisplayName("BipartiteResult 测试")
    class BipartiteResultTests {

        @Test
        @DisplayName("ofBipartite 创建不可变副本")
        void testOfBipartiteImmutable() {
            Set<String> left = new HashSet<>(Set.of("A", "C"));
            Set<String> right = new HashSet<>(Set.of("B", "D"));

            BipartiteUtil.BipartiteResult<String> result =
                BipartiteUtil.BipartiteResult.ofBipartite(left, right);

            assertThat(result.bipartite()).isTrue();
            assertThat(result.left()).containsExactlyInAnyOrder("A", "C");
            assertThat(result.right()).containsExactlyInAnyOrder("B", "D");
            assertThat(result.oddCycle()).isEmpty();

            // Original set mutation should not affect result
            left.add("X");
            assertThat(result.left()).doesNotContain("X");
        }

        @Test
        @DisplayName("ofNotBipartite 创建不可变副本")
        void testOfNotBipartiteImmutable() {
            List<String> cycle = new java.util.ArrayList<>(List.of("A", "B", "C", "A"));

            BipartiteUtil.BipartiteResult<String> result =
                BipartiteUtil.BipartiteResult.ofNotBipartite(cycle);

            assertThat(result.bipartite()).isFalse();
            assertThat(result.left()).isEmpty();
            assertThat(result.right()).isEmpty();
            assertThat(result.oddCycle()).containsExactly("A", "B", "C", "A");
        }
    }
}
