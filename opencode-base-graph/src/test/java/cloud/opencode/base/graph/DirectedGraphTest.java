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
 * DirectedGraph 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("DirectedGraph 测试")
class DirectedGraphTest {

    private DirectedGraph<String> graph;

    @BeforeEach
    void setUp() {
        graph = new DirectedGraph<>();
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
        @DisplayName("重复添加顶点不影响")
        void testAddDuplicateVertex() {
            graph.addVertex("A");
            graph.addVertex("A");

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
        @DisplayName("添加边（自动创建顶点）")
        void testAddEdge() {
            graph.addEdge("A", "B");

            assertThat(graph.containsVertex("A")).isTrue();
            assertThat(graph.containsVertex("B")).isTrue();
            assertThat(graph.containsEdge("A", "B")).isTrue();
        }

        @Test
        @DisplayName("添加带权重的边")
        void testAddWeightedEdge() {
            graph.addEdge("A", "B", 5.0);

            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.getWeight("A", "B")).isEqualTo(5.0);
        }

        @Test
        @DisplayName("添加null顶点的边抛出异常")
        void testAddEdgeWithNullVertex() {
            assertThatThrownBy(() -> graph.addEdge(null, "B"))
                .isInstanceOf(InvalidVertexException.class);
            assertThatThrownBy(() -> graph.addEdge("A", null))
                .isInstanceOf(InvalidVertexException.class);
        }

        @Test
        @DisplayName("有向图不自动添加反向边")
        void testDirectedEdgeNotBidirectional() {
            graph.addEdge("A", "B");

            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("B", "A")).isFalse();
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
            graph.addEdge("C", "A");

            graph.removeVertex("B");

            assertThat(graph.containsVertex("B")).isFalse();
            assertThat(graph.containsEdge("A", "B")).isFalse();
            assertThat(graph.containsEdge("B", "C")).isFalse();
        }

        @Test
        @DisplayName("删除不存在的顶点不抛异常")
        void testRemoveNonExistentVertex() {
            assertThatCode(() -> graph.removeVertex("X")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("删除null顶点不抛异常")
        void testRemoveNullVertex() {
            assertThatCode(() -> graph.removeVertex(null)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("removeEdge测试")
    class RemoveEdgeTests {

        @Test
        @DisplayName("删除边")
        void testRemoveEdge() {
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            graph.removeEdge("A", "B");

            assertThat(graph.containsEdge("A", "B")).isFalse();
            assertThat(graph.containsEdge("B", "C")).isTrue();
        }

        @Test
        @DisplayName("删除不存在的边不抛异常")
        void testRemoveNonExistentEdge() {
            assertThatCode(() -> graph.removeEdge("X", "Y")).doesNotThrowAnyException();
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

            Set<String> vertices = graph.vertices();

            assertThat(vertices).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("获取所有边")
        void testEdges() {
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            Set<Edge<String>> edges = graph.edges();

            assertThat(edges).hasSize(2);
        }
    }

    @Nested
    @DisplayName("neighbors测试")
    class NeighborsTests {

        @Test
        @DisplayName("获取邻居")
        void testNeighbors() {
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "C");

            Set<String> neighbors = graph.neighbors("A");

            assertThat(neighbors).containsExactlyInAnyOrder("B", "C");
        }

        @Test
        @DisplayName("无邻居返回空集")
        void testNoNeighbors() {
            graph.addVertex("A");

            Set<String> neighbors = graph.neighbors("A");

            assertThat(neighbors).isEmpty();
        }

        @Test
        @DisplayName("不存在的顶点返回空集")
        void testNeighborsNonExistent() {
            Set<String> neighbors = graph.neighbors("X");

            assertThat(neighbors).isEmpty();
        }
    }

    @Nested
    @DisplayName("outEdges和inEdges测试")
    class OutInEdgesTests {

        @Test
        @DisplayName("获取出边")
        void testOutEdges() {
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("A", "C", 2.0);

            Set<Edge<String>> outEdges = graph.outEdges("A");

            assertThat(outEdges).hasSize(2);
        }

        @Test
        @DisplayName("获取入边")
        void testInEdges() {
            graph.addEdge("A", "C", 1.0);
            graph.addEdge("B", "C", 2.0);

            Set<Edge<String>> inEdges = graph.inEdges("C");

            assertThat(inEdges).hasSize(2);
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
        @DisplayName("边数量")
        void testEdgeCount() {
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C");

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
        @DisplayName("containsEdge")
        void testContainsEdge() {
            graph.addEdge("A", "B");

            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("B", "A")).isFalse();
            assertThat(graph.containsEdge(null, "B")).isFalse();
        }
    }

    @Nested
    @DisplayName("getWeight测试")
    class GetWeightTests {

        @Test
        @DisplayName("获取边权重")
        void testGetWeight() {
            graph.addEdge("A", "B", 5.0);

            assertThat(graph.getWeight("A", "B")).isEqualTo(5.0);
        }

        @Test
        @DisplayName("不存在的边返回MAX_VALUE")
        void testGetWeightNonExistent() {
            assertThat(graph.getWeight("A", "B")).isEqualTo(Double.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("isDirected测试")
    class IsDirectedTests {

        @Test
        @DisplayName("是有向图")
        void testIsDirected() {
            assertThat(graph.isDirected()).isTrue();
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

            assertThat(str).contains("DirectedGraph");
        }
    }
}
