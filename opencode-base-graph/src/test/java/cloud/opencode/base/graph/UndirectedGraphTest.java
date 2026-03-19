package cloud.opencode.base.graph;

import cloud.opencode.base.graph.exception.InvalidVertexException;
import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * UndirectedGraph 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("UndirectedGraph 测试")
class UndirectedGraphTest {

    private UndirectedGraph<String> graph;

    @BeforeEach
    void setUp() {
        graph = new UndirectedGraph<>();
    }

    @Nested
    @DisplayName("addVertex测试")
    class AddVertexTests {

        @Test
        @DisplayName("添加顶点")
        void testAddVertex() {
            graph.addVertex("A");

            assertThat(graph.containsVertex("A")).isTrue();
            assertThat(graph.vertexCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("添加null顶点抛出异常")
        void testAddNullVertex() {
            assertThatThrownBy(() -> graph.addVertex(null))
                .isInstanceOf(InvalidVertexException.class);
        }
    }

    @Nested
    @DisplayName("addEdge测试")
    class AddEdgeTests {

        @Test
        @DisplayName("添加边（双向）")
        void testAddEdgeBidirectional() {
            graph.addEdge("A", "B");

            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("B", "A")).isTrue();
        }

        @Test
        @DisplayName("添加带权重的边")
        void testAddWeightedEdge() {
            graph.addEdge("A", "B", 5.0);

            assertThat(graph.getWeight("A", "B")).isEqualTo(5.0);
            assertThat(graph.getWeight("B", "A")).isEqualTo(5.0);
        }

        @Test
        @DisplayName("添加null顶点的边抛出异常")
        void testAddEdgeWithNullVertex() {
            assertThatThrownBy(() -> graph.addEdge(null, "B"))
                .isInstanceOf(InvalidVertexException.class);
        }
    }

    @Nested
    @DisplayName("removeVertex测试")
    class RemoveVertexTests {

        @Test
        @DisplayName("删除顶点及相关边")
        void testRemoveVertex() {
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            graph.removeVertex("B");

            assertThat(graph.containsVertex("B")).isFalse();
            assertThat(graph.containsEdge("A", "B")).isFalse();
            assertThat(graph.containsEdge("B", "C")).isFalse();
        }
    }

    @Nested
    @DisplayName("removeEdge测试")
    class RemoveEdgeTests {

        @Test
        @DisplayName("删除边（双向删除）")
        void testRemoveEdge() {
            graph.addEdge("A", "B");

            graph.removeEdge("A", "B");

            assertThat(graph.containsEdge("A", "B")).isFalse();
            assertThat(graph.containsEdge("B", "A")).isFalse();
        }
    }

    @Nested
    @DisplayName("vertices和edges测试")
    class VerticesEdgesTests {

        @Test
        @DisplayName("获取所有顶点")
        void testVertices() {
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(graph.vertices()).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("获取所有边（无重复）")
        void testEdgesNoDuplicates() {
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            Set<Edge<String>> edges = graph.edges();

            // 无向图每条边只应出现一次
            assertThat(edges).hasSize(2);
        }
    }

    @Nested
    @DisplayName("neighbors测试")
    class NeighborsTests {

        @Test
        @DisplayName("获取邻居（双向）")
        void testNeighbors() {
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(graph.neighbors("A")).contains("B");
            assertThat(graph.neighbors("B")).containsExactlyInAnyOrder("A", "C");
        }

        @Test
        @DisplayName("不存在的顶点返回空集")
        void testNeighborsNonExistent() {
            assertThat(graph.neighbors("X")).isEmpty();
        }
    }

    @Nested
    @DisplayName("outEdges和inEdges测试")
    class OutInEdgesTests {

        @Test
        @DisplayName("无向图中outEdges和inEdges相同")
        void testOutInEdgesSame() {
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");

            Set<Edge<String>> outEdges = graph.outEdges("A");
            Set<Edge<String>> inEdges = graph.inEdges("A");

            assertThat(outEdges).isEqualTo(inEdges);
        }
    }

    @Nested
    @DisplayName("count方法测试")
    class CountTests {

        @Test
        @DisplayName("顶点数量")
        void testVertexCount() {
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(graph.vertexCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("边数量（无重复计算）")
        void testEdgeCount() {
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C");

            // 无向图边数是实际边数，不是存储数量的两倍
            assertThat(graph.edgeCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("contains方法测试")
    class ContainsTests {

        @Test
        @DisplayName("containsVertex")
        void testContainsVertex() {
            graph.addVertex("A");

            assertThat(graph.containsVertex("A")).isTrue();
            assertThat(graph.containsVertex("B")).isFalse();
            assertThat(graph.containsVertex(null)).isFalse();
        }

        @Test
        @DisplayName("containsEdge（双向）")
        void testContainsEdgeBidirectional() {
            graph.addEdge("A", "B");

            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("B", "A")).isTrue();
        }
    }

    @Nested
    @DisplayName("getWeight测试")
    class GetWeightTests {

        @Test
        @DisplayName("获取边权重（双向）")
        void testGetWeight() {
            graph.addEdge("A", "B", 5.0);

            assertThat(graph.getWeight("A", "B")).isEqualTo(5.0);
            assertThat(graph.getWeight("B", "A")).isEqualTo(5.0);
        }

        @Test
        @DisplayName("不存在的边返回MAX_VALUE")
        void testGetWeightNonExistent() {
            assertThat(graph.getWeight("A", "B")).isEqualTo(Double.MAX_VALUE);
        }

        @Test
        @DisplayName("null参数返回MAX_VALUE")
        void testGetWeightNull() {
            assertThat(graph.getWeight(null, "B")).isEqualTo(Double.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("isDirected测试")
    class IsDirectedTests {

        @Test
        @DisplayName("不是有向图")
        void testIsNotDirected() {
            assertThat(graph.isDirected()).isFalse();
        }
    }

    @Nested
    @DisplayName("clear测试")
    class ClearTests {

        @Test
        @DisplayName("清空图")
        void testClear() {
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            graph.clear();

            assertThat(graph.vertexCount()).isEqualTo(0);
            assertThat(graph.edgeCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString返回图信息")
        void testToString() {
            graph.addEdge("A", "B");

            String str = graph.toString();

            assertThat(str).contains("UndirectedGraph");
        }
    }
}
