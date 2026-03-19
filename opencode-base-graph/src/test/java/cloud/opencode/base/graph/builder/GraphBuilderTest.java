package cloud.opencode.base.graph.builder;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("GraphBuilder 测试")
class GraphBuilderTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("创建有向图构建器")
        void testDirected() {
            GraphBuilder<String> builder = GraphBuilder.directed();

            assertThat(builder).isNotNull();
            Graph<String> graph = builder.build();
            assertThat(graph.isDirected()).isTrue();
        }

        @Test
        @DisplayName("创建无向图构建器")
        void testUndirected() {
            GraphBuilder<String> builder = GraphBuilder.undirected();

            assertThat(builder).isNotNull();
            Graph<String> graph = builder.build();
            assertThat(graph.isDirected()).isFalse();
        }
    }

    @Nested
    @DisplayName("addVertex测试")
    class AddVertexTests {

        @Test
        @DisplayName("添加单个顶点")
        void testAddVertex() {
            Graph<String> graph = GraphBuilder.<String>directed()
                .addVertex("A")
                .build();

            assertThat(graph.containsVertex("A")).isTrue();
        }

        @Test
        @DisplayName("添加多个顶点（varargs）")
        void testAddVerticesVarargs() {
            Graph<String> graph = GraphBuilder.<String>directed()
                .addVertices("A", "B", "C")
                .build();

            assertThat(graph.containsVertex("A")).isTrue();
            assertThat(graph.containsVertex("B")).isTrue();
            assertThat(graph.containsVertex("C")).isTrue();
        }

        @Test
        @DisplayName("添加顶点集合")
        void testAddVerticesCollection() {
            Graph<String> graph = GraphBuilder.<String>directed()
                .addVertices(List.of("A", "B", "C"))
                .build();

            assertThat(graph.vertexCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("addEdge测试")
    class AddEdgeTests {

        @Test
        @DisplayName("添加默认权重边")
        void testAddEdgeDefault() {
            Graph<String> graph = GraphBuilder.<String>directed()
                .addEdge("A", "B")
                .build();

            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.getWeight("A", "B")).isEqualTo(Edge.DEFAULT_WEIGHT);
        }

        @Test
        @DisplayName("添加带权重边")
        void testAddEdgeWithWeight() {
            Graph<String> graph = GraphBuilder.<String>directed()
                .addEdge("A", "B", 5.0)
                .build();

            assertThat(graph.getWeight("A", "B")).isEqualTo(5.0);
        }

        @Test
        @DisplayName("添加Edge对象")
        void testAddEdgeObject() {
            Edge<String> edge = new Edge<>("A", "B", 3.0);
            Graph<String> graph = GraphBuilder.<String>directed()
                .addEdge(edge)
                .build();

            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.getWeight("A", "B")).isEqualTo(3.0);
        }

        @Test
        @DisplayName("添加边集合")
        void testAddEdgesCollection() {
            Set<Edge<String>> edges = Set.of(
                new Edge<>("A", "B", 1.0),
                new Edge<>("B", "C", 2.0)
            );

            Graph<String> graph = GraphBuilder.<String>directed()
                .addEdges(edges)
                .build();

            assertThat(graph.edgeCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("initialCapacity测试")
    class InitialCapacityTests {

        @Test
        @DisplayName("设置初始容量")
        void testInitialCapacity() {
            Graph<String> graph = GraphBuilder.<String>directed()
                .initialCapacity(100)
                .addEdge("A", "B")
                .build();

            // 容量不影响基本功能
            assertThat(graph.containsEdge("A", "B")).isTrue();
        }
    }

    @Nested
    @DisplayName("configure测试")
    class ConfigureTests {

        @Test
        @DisplayName("使用配置函数")
        void testConfigure() {
            Graph<String> graph = GraphBuilder.<String>directed()
                .configure(builder -> {
                    builder.addVertex("A");
                    builder.addVertex("B");
                    builder.addEdge("A", "B");
                })
                .build();

            assertThat(graph.vertexCount()).isEqualTo(2);
            assertThat(graph.edgeCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("build测试")
    class BuildTests {

        @Test
        @DisplayName("构建空图")
        void testBuildEmptyGraph() {
            Graph<String> graph = GraphBuilder.<String>directed().build();

            assertThat(graph.vertexCount()).isEqualTo(0);
            assertThat(graph.edgeCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("构建复杂图")
        void testBuildComplexGraph() {
            Graph<String> graph = GraphBuilder.<String>directed()
                .addVertices("A", "B", "C", "D")
                .addEdge("A", "B", 1.0)
                .addEdge("B", "C", 2.0)
                .addEdge("C", "D", 3.0)
                .addEdge("A", "D", 10.0)
                .build();

            assertThat(graph.vertexCount()).isEqualTo(4);
            assertThat(graph.edgeCount()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("buildAndValidate测试")
    class BuildAndValidateTests {

        @Test
        @DisplayName("构建并验证有效图")
        void testBuildAndValidateValid() {
            Graph<String> graph = GraphBuilder.<String>directed()
                .addEdge("A", "B", 1.0)
                .addEdge("B", "C", 2.0)
                .buildAndValidate();

            assertThat(graph).isNotNull();
            assertThat(graph.edgeCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("所有方法都返回builder")
        void testFluentApi() {
            Graph<String> graph = GraphBuilder.<String>directed()
                .initialCapacity(50)
                .addVertex("X")
                .addVertices("Y", "Z")
                .addVertices(List.of("W"))
                .addEdge("X", "Y")
                .addEdge("Y", "Z", 2.0)
                .addEdge(new Edge<>("Z", "W", 3.0))
                .configure(b -> b.addEdge("W", "X"))
                .build();

            assertThat(graph.vertexCount()).isEqualTo(4);
            assertThat(graph.edgeCount()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("无向图构建测试")
    class UndirectedBuildTests {

        @Test
        @DisplayName("构建无向图")
        void testBuildUndirected() {
            Graph<String> graph = GraphBuilder.<String>undirected()
                .addEdge("A", "B")
                .build();

            assertThat(graph.isDirected()).isFalse();
            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("B", "A")).isTrue();
        }
    }
}
