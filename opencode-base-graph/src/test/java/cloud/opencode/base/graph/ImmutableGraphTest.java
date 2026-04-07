package cloud.opencode.base.graph;

import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableGraph 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
@DisplayName("ImmutableGraph 测试")
class ImmutableGraphTest {

    @Nested
    @DisplayName("copyOf测试")
    class CopyOfTests {

        @Test
        @DisplayName("保留有向图的顶点和边")
        void testCopyOfDirectedPreservesVerticesAndEdges() {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B", 2.0);
            original.addEdge("B", "C", 3.0);

            Graph<String> snapshot = ImmutableGraph.copyOf(original);

            assertThat(snapshot.vertexCount()).isEqualTo(3);
            assertThat(snapshot.edgeCount()).isEqualTo(2);
            assertThat(snapshot.containsVertex("A")).isTrue();
            assertThat(snapshot.containsVertex("B")).isTrue();
            assertThat(snapshot.containsVertex("C")).isTrue();
            assertThat(snapshot.containsEdge("A", "B")).isTrue();
            assertThat(snapshot.containsEdge("B", "C")).isTrue();
            assertThat(snapshot.getWeight("A", "B")).isEqualTo(2.0);
            assertThat(snapshot.getWeight("B", "C")).isEqualTo(3.0);
            assertThat(snapshot.isDirected()).isTrue();
        }

        @Test
        @DisplayName("保留无向图的顶点和边")
        void testCopyOfUndirectedPreservesVerticesAndEdges() {
            Graph<String> original = OpenGraph.undirected();
            original.addEdge("X", "Y", 5.0);

            Graph<String> snapshot = ImmutableGraph.copyOf(original);

            assertThat(snapshot.vertexCount()).isEqualTo(2);
            assertThat(snapshot.containsEdge("X", "Y")).isTrue();
            assertThat(snapshot.containsEdge("Y", "X")).isTrue();
            assertThat(snapshot.isDirected()).isFalse();
        }

        @Test
        @DisplayName("copyOf已有ImmutableGraph返回同一实例")
        void testCopyOfImmutableReturnsSameInstance() {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B");

            ImmutableGraph<String> immutable = ImmutableGraph.copyOf(original);
            ImmutableGraph<String> copy = ImmutableGraph.copyOf(immutable);

            assertThat(copy).isSameAs(immutable);
        }
    }

    @Nested
    @DisplayName("不可变性测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("addVertex抛出UnsupportedOperationException")
        void testAddVertexThrows() {
            Graph<String> snapshot = ImmutableGraph.copyOf(OpenGraph.directed());
            assertThatThrownBy(() -> snapshot.addVertex("X"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("addEdge(from,to)抛出UnsupportedOperationException")
        void testAddEdgeThrows() {
            Graph<String> snapshot = ImmutableGraph.copyOf(OpenGraph.directed());
            assertThatThrownBy(() -> snapshot.addEdge("A", "B"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("addEdge(from,to,weight)抛出UnsupportedOperationException")
        void testAddWeightedEdgeThrows() {
            Graph<String> snapshot = ImmutableGraph.copyOf(OpenGraph.directed());
            assertThatThrownBy(() -> snapshot.addEdge("A", "B", 1.0))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("removeVertex抛出UnsupportedOperationException")
        void testRemoveVertexThrows() {
            Graph<String> original = OpenGraph.directed();
            original.addVertex("A");
            Graph<String> snapshot = ImmutableGraph.copyOf(original);

            assertThatThrownBy(() -> snapshot.removeVertex("A"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("removeEdge抛出UnsupportedOperationException")
        void testRemoveEdgeThrows() {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B");
            Graph<String> snapshot = ImmutableGraph.copyOf(original);

            assertThatThrownBy(() -> snapshot.removeEdge("A", "B"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("clear抛出UnsupportedOperationException")
        void testClearThrows() {
            Graph<String> snapshot = ImmutableGraph.copyOf(OpenGraph.directed());
            assertThatThrownBy(snapshot::clear)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("快照隔离测试")
    class SnapshotIsolationTests {

        @Test
        @DisplayName("原图修改不影响快照")
        void testOriginalModificationDoesNotAffectSnapshot() {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B");
            original.addEdge("B", "C");

            Graph<String> snapshot = ImmutableGraph.copyOf(original);

            // Modify original
            original.addEdge("C", "D");
            original.removeVertex("A");

            // Snapshot should be unchanged
            assertThat(snapshot.vertexCount()).isEqualTo(3);
            assertThat(snapshot.edgeCount()).isEqualTo(2);
            assertThat(snapshot.containsVertex("A")).isTrue();
            assertThat(snapshot.containsVertex("D")).isFalse();
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryTests {

        @Test
        @DisplayName("neighbors返回正确结果")
        void testNeighbors() {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B");
            original.addEdge("A", "C");
            Graph<String> snapshot = ImmutableGraph.copyOf(original);

            assertThat(snapshot.neighbors("A")).containsExactlyInAnyOrder("B", "C");
            assertThat(snapshot.neighbors("B")).isEmpty();
        }

        @Test
        @DisplayName("不存在的顶点neighbors返回空集")
        void testNeighborsNonExistentVertex() {
            Graph<String> snapshot = ImmutableGraph.copyOf(OpenGraph.directed());
            assertThat(snapshot.neighbors("X")).isEmpty();
        }

        @Test
        @DisplayName("snapshot默认方法测试")
        void testSnapshotDefaultMethod() {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B");

            Graph<String> snapshot = original.snapshot();

            assertThat(snapshot).isInstanceOf(ImmutableGraph.class);
            assertThat(snapshot.containsEdge("A", "B")).isTrue();
        }

        @Test
        @DisplayName("outEdges和inEdges")
        void testOutEdgesAndInEdges() {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B", 2.0);
            Graph<String> snapshot = ImmutableGraph.copyOf(original);

            assertThat(snapshot.outEdges("A")).hasSize(1);
            assertThat(snapshot.inEdges("B")).hasSize(1);
            assertThat(snapshot.outEdges("Z")).isEmpty();
            assertThat(snapshot.inEdges("Z")).isEmpty();
        }

        @Test
        @DisplayName("isEmpty和vertexCount")
        void testIsEmptyAndVertexCount() {
            Graph<String> empty = ImmutableGraph.copyOf(OpenGraph.directed());
            assertThat(empty.isEmpty()).isTrue();
            assertThat(empty.vertexCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("null参数测试")
    class NullParameterTests {

        @Test
        @DisplayName("copyOf(null)应抛出NullPointerException")
        void copyOfNullThrows() {
            assertThatThrownBy(() -> ImmutableGraph.copyOf(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
