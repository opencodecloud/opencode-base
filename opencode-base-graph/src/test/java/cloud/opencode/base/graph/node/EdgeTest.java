package cloud.opencode.base.graph.node;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Edge 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("Edge 测试")
class EdgeTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建带权重的边")
        void testWeightedEdge() {
            Edge<String> edge = new Edge<>("A", "B", 5.0);

            assertThat(edge.from()).isEqualTo("A");
            assertThat(edge.to()).isEqualTo("B");
            assertThat(edge.weight()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("创建默认权重的边")
        void testUnweightedEdge() {
            Edge<String> edge = new Edge<>("A", "B");

            assertThat(edge.from()).isEqualTo("A");
            assertThat(edge.to()).isEqualTo("B");
            assertThat(edge.weight()).isEqualTo(Edge.DEFAULT_WEIGHT);
        }

        @Test
        @DisplayName("null源顶点抛出异常")
        void testNullFromThrows() {
            assertThatThrownBy(() -> new Edge<>(null, "B", 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source vertex cannot be null");
        }

        @Test
        @DisplayName("null目标顶点抛出异常")
        void testNullToThrows() {
            assertThatThrownBy(() -> new Edge<>("A", null, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target vertex cannot be null");
        }

        @Test
        @DisplayName("NaN权重抛出异常")
        void testNaNWeightThrows() {
            assertThatThrownBy(() -> new Edge<>("A", "B", Double.NaN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NaN");
        }

        @Test
        @DisplayName("Infinite权重抛出异常")
        void testInfiniteWeightThrows() {
            assertThatThrownBy(() -> new Edge<>("A", "B", Double.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("infinite");
        }
    }

    @Nested
    @DisplayName("默认权重测试")
    class DefaultWeightTests {

        @Test
        @DisplayName("DEFAULT_WEIGHT为1.0")
        void testDefaultWeight() {
            assertThat(Edge.DEFAULT_WEIGHT).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("reversed()方法测试")
    class ReversedTests {

        @Test
        @DisplayName("创建反向边")
        void testReversed() {
            Edge<String> edge = new Edge<>("A", "B", 5.0);
            Edge<String> reversed = edge.reversed();

            assertThat(reversed.from()).isEqualTo("B");
            assertThat(reversed.to()).isEqualTo("A");
            assertThat(reversed.weight()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("双重反向恢复原边")
        void testDoubleReversed() {
            Edge<String> edge = new Edge<>("A", "B", 5.0);
            Edge<String> doubleReversed = edge.reversed().reversed();

            assertThat(doubleReversed).isEqualTo(edge);
        }
    }

    @Nested
    @DisplayName("withWeight()方法测试")
    class WithWeightTests {

        @Test
        @DisplayName("创建新权重的边")
        void testWithWeight() {
            Edge<String> edge = new Edge<>("A", "B", 5.0);
            Edge<String> newEdge = edge.withWeight(10.0);

            assertThat(newEdge.from()).isEqualTo("A");
            assertThat(newEdge.to()).isEqualTo("B");
            assertThat(newEdge.weight()).isEqualTo(10.0);
        }

        @Test
        @DisplayName("原边不变")
        void testOriginalUnchanged() {
            Edge<String> edge = new Edge<>("A", "B", 5.0);
            edge.withWeight(10.0);

            assertThat(edge.weight()).isEqualTo(5.0);
        }
    }

    @Nested
    @DisplayName("isSelfLoop()方法测试")
    class IsSelfLoopTests {

        @Test
        @DisplayName("自环返回true")
        void testSelfLoop() {
            Edge<String> edge = new Edge<>("A", "A", 1.0);

            assertThat(edge.isSelfLoop()).isTrue();
        }

        @Test
        @DisplayName("非自环返回false")
        void testNotSelfLoop() {
            Edge<String> edge = new Edge<>("A", "B", 1.0);

            assertThat(edge.isSelfLoop()).isFalse();
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals方法")
        void testEquals() {
            Edge<String> edge1 = new Edge<>("A", "B", 5.0);
            Edge<String> edge2 = new Edge<>("A", "B", 5.0);
            Edge<String> edge3 = new Edge<>("A", "C", 5.0);

            assertThat(edge1).isEqualTo(edge2);
            assertThat(edge1).isNotEqualTo(edge3);
        }

        @Test
        @DisplayName("hashCode方法")
        void testHashCode() {
            Edge<String> edge1 = new Edge<>("A", "B", 5.0);
            Edge<String> edge2 = new Edge<>("A", "B", 5.0);

            assertThat(edge1.hashCode()).isEqualTo(edge2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString()方法测试")
    class ToStringTests {

        @Test
        @DisplayName("默认权重的toString")
        void testToStringDefaultWeight() {
            Edge<String> edge = new Edge<>("A", "B");

            assertThat(edge.toString()).isEqualTo("A -> B");
        }

        @Test
        @DisplayName("带权重的toString")
        void testToStringWithWeight() {
            Edge<String> edge = new Edge<>("A", "B", 5.0);

            assertThat(edge.toString()).isEqualTo("A -(5.0)-> B");
        }
    }

    @Nested
    @DisplayName("泛型测试")
    class GenericTests {

        @Test
        @DisplayName("Integer类型顶点")
        void testIntegerVertices() {
            Edge<Integer> edge = new Edge<>(1, 2, 3.0);

            assertThat(edge.from()).isEqualTo(1);
            assertThat(edge.to()).isEqualTo(2);
        }

        @Test
        @DisplayName("自定义类型顶点")
        void testCustomTypeVertices() {
            record Vertex(String name) {}
            Edge<Vertex> edge = new Edge<>(new Vertex("A"), new Vertex("B"), 1.0);

            assertThat(edge.from().name()).isEqualTo("A");
            assertThat(edge.to().name()).isEqualTo("B");
        }
    }
}
