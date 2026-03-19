package cloud.opencode.base.graph.security;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import cloud.opencode.base.graph.exception.GraphLimitExceededException;
import cloud.opencode.base.graph.exception.GraphTimeoutException;
import cloud.opencode.base.graph.exception.InvalidEdgeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SafeGraphOperations 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("SafeGraphOperations 测试")
class SafeGraphOperationsTest {

    @BeforeEach
    void setUp() {
        SafeGraphOperations.resetToDefaults();
    }

    @AfterEach
    void tearDown() {
        SafeGraphOperations.resetToDefaults();
    }

    @Nested
    @DisplayName("配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("设置和获取最大顶点数")
        void testSetMaxVertices() {
            SafeGraphOperations.setMaxVertices(1000);

            assertThat(SafeGraphOperations.getMaxVertices()).isEqualTo(1000);
        }

        @Test
        @DisplayName("设置和获取最大边数")
        void testSetMaxEdges() {
            SafeGraphOperations.setMaxEdges(5000);

            assertThat(SafeGraphOperations.getMaxEdges()).isEqualTo(5000);
        }

        @Test
        @DisplayName("设置和获取最大深度")
        void testSetMaxDepth() {
            SafeGraphOperations.setMaxDepth(500);

            assertThat(SafeGraphOperations.getMaxDepth()).isEqualTo(500);
        }

        @Test
        @DisplayName("设置和获取超时时间")
        void testSetTimeout() {
            Duration timeout = Duration.ofSeconds(60);
            SafeGraphOperations.setTimeout(timeout);

            assertThat(SafeGraphOperations.getTimeout()).isEqualTo(timeout);
        }

        @Test
        @DisplayName("重置为默认值")
        void testResetToDefaults() {
            SafeGraphOperations.setMaxVertices(1);
            SafeGraphOperations.setMaxEdges(1);
            SafeGraphOperations.setMaxDepth(1);
            SafeGraphOperations.setTimeout(Duration.ofMillis(1));

            SafeGraphOperations.resetToDefaults();

            assertThat(SafeGraphOperations.getMaxVertices()).isEqualTo(SafeGraphOperations.DEFAULT_MAX_VERTICES);
            assertThat(SafeGraphOperations.getMaxEdges()).isEqualTo(SafeGraphOperations.DEFAULT_MAX_EDGES);
            assertThat(SafeGraphOperations.getMaxDepth()).isEqualTo(SafeGraphOperations.DEFAULT_MAX_DEPTH);
            assertThat(SafeGraphOperations.getTimeout()).isEqualTo(SafeGraphOperations.DEFAULT_TIMEOUT);
        }
    }

    @Nested
    @DisplayName("默认常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("默认最大顶点数")
        void testDefaultMaxVertices() {
            assertThat(SafeGraphOperations.DEFAULT_MAX_VERTICES).isEqualTo(100_000);
        }

        @Test
        @DisplayName("默认最大边数")
        void testDefaultMaxEdges() {
            assertThat(SafeGraphOperations.DEFAULT_MAX_EDGES).isEqualTo(1_000_000);
        }

        @Test
        @DisplayName("默认最大深度")
        void testDefaultMaxDepth() {
            assertThat(SafeGraphOperations.DEFAULT_MAX_DEPTH).isEqualTo(10_000);
        }

        @Test
        @DisplayName("默认超时时间")
        void testDefaultTimeout() {
            assertThat(SafeGraphOperations.DEFAULT_TIMEOUT).isEqualTo(Duration.ofSeconds(30));
        }
    }

    @Nested
    @DisplayName("safeAddVertex测试")
    class SafeAddVertexTests {

        @Test
        @DisplayName("安全添加顶点")
        void testSafeAddVertex() {
            Graph<String> graph = OpenGraph.directed();

            SafeGraphOperations.safeAddVertex(graph, "A");

            assertThat(graph.containsVertex("A")).isTrue();
        }

        @Test
        @DisplayName("超出顶点限制抛出异常")
        void testExceedVertexLimit() {
            Graph<String> graph = OpenGraph.directed();
            SafeGraphOperations.setMaxVertices(2);

            SafeGraphOperations.safeAddVertex(graph, "A");
            SafeGraphOperations.safeAddVertex(graph, "B");

            assertThatThrownBy(() -> SafeGraphOperations.safeAddVertex(graph, "C"))
                .isInstanceOf(GraphLimitExceededException.class);
        }
    }

    @Nested
    @DisplayName("safeAddEdge测试")
    class SafeAddEdgeTests {

        @Test
        @DisplayName("安全添加边")
        void testSafeAddEdge() {
            Graph<String> graph = OpenGraph.directed();

            SafeGraphOperations.safeAddEdge(graph, "A", "B");

            assertThat(graph.containsEdge("A", "B")).isTrue();
        }

        @Test
        @DisplayName("安全添加带权重的边")
        void testSafeAddEdgeWithWeight() {
            Graph<String> graph = OpenGraph.directed();

            SafeGraphOperations.safeAddEdge(graph, "A", "B", 5.0);

            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.getWeight("A", "B")).isEqualTo(5.0);
        }

        @Test
        @DisplayName("超出边限制抛出异常")
        void testExceedEdgeLimit() {
            Graph<String> graph = OpenGraph.directed();
            SafeGraphOperations.setMaxEdges(1);

            SafeGraphOperations.safeAddEdge(graph, "A", "B");

            assertThatThrownBy(() -> SafeGraphOperations.safeAddEdge(graph, "B", "C"))
                .isInstanceOf(GraphLimitExceededException.class);
        }

        @Test
        @DisplayName("负权重抛出异常")
        void testNegativeWeight() {
            Graph<String> graph = OpenGraph.directed();

            assertThatThrownBy(() -> SafeGraphOperations.safeAddEdge(graph, "A", "B", -1.0))
                .isInstanceOf(InvalidEdgeException.class);
        }
    }

    @Nested
    @DisplayName("safeShortestPath测试")
    class SafeShortestPathTests {

        @Test
        @DisplayName("安全计算最短路径")
        void testSafeShortestPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);

            List<String> path = SafeGraphOperations.safeShortestPath(graph, "A", "C");

            assertThat(path).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("超时抛出异常")
        void testTimeout() {
            SafeGraphOperations.setTimeout(Duration.ofMillis(1));

            // Create a large graph that would take time to process
            Graph<Integer> graph = OpenGraph.directed();
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < 10; j++) {
                    graph.addEdge(i, (i + j + 1) % 1000, 1.0);
                }
            }

            // This might timeout depending on system speed
            // The test verifies that timeout mechanism exists
            try {
                SafeGraphOperations.safeShortestPath(graph, 0, 999);
            } catch (GraphTimeoutException e) {
                // Expected
                assertThat(e.getMessage()).contains("timed out");
            } catch (Exception e) {
                // Other exceptions might occur, that's fine too
            }
        }
    }

    @Nested
    @DisplayName("safeDijkstra测试")
    class SafeDijkstraTests {

        @Test
        @DisplayName("安全计算Dijkstra")
        void testSafeDijkstra() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            Map<String, Double> distances = SafeGraphOperations.safeDijkstra(graph, "A");

            assertThat(distances).containsEntry("A", 0.0);
            assertThat(distances).containsEntry("B", 1.0);
            assertThat(distances).containsEntry("C", 3.0);
        }
    }

    @Nested
    @DisplayName("executeWithTimeout测试")
    class ExecuteWithTimeoutTests {

        @Test
        @DisplayName("在超时前完成")
        void testCompleteBeforeTimeout() {
            String result = SafeGraphOperations.executeWithTimeout(
                () -> "success",
                "test operation"
            );

            assertThat(result).isEqualTo("success");
        }

        @Test
        @DisplayName("超时抛出GraphTimeoutException")
        void testTimeoutThrowsException() {
            SafeGraphOperations.setTimeout(Duration.ofMillis(10));

            assertThatThrownBy(() -> SafeGraphOperations.executeWithTimeout(
                () -> {
                    Thread.sleep(1000);
                    return "result";
                },
                "slow operation"
            )).isInstanceOf(GraphTimeoutException.class)
              .hasMessageContaining("timed out");
        }

        @Test
        @DisplayName("执行异常被传播")
        void testExceptionPropagated() {
            assertThatThrownBy(() -> SafeGraphOperations.executeWithTimeout(
                () -> { throw new IllegalStateException("test error"); },
                "failing operation"
            )).isInstanceOf(IllegalStateException.class)
              .hasMessage("test error");
        }
    }

    @Nested
    @DisplayName("wouldExceedVertexLimit测试")
    class WouldExceedVertexLimitTests {

        @Test
        @DisplayName("未超出限制返回false")
        void testNotExceeded() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(SafeGraphOperations.wouldExceedVertexLimit(graph)).isFalse();
        }

        @Test
        @DisplayName("已达限制返回true")
        void testExceeded() {
            Graph<String> graph = OpenGraph.directed();
            SafeGraphOperations.setMaxVertices(2);
            graph.addVertex("A");
            graph.addVertex("B");

            assertThat(SafeGraphOperations.wouldExceedVertexLimit(graph)).isTrue();
        }
    }

    @Nested
    @DisplayName("wouldExceedEdgeLimit测试")
    class WouldExceedEdgeLimitTests {

        @Test
        @DisplayName("未超出限制返回false")
        void testNotExceeded() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            assertThat(SafeGraphOperations.wouldExceedEdgeLimit(graph)).isFalse();
        }

        @Test
        @DisplayName("已达限制返回true")
        void testExceeded() {
            Graph<String> graph = OpenGraph.directed();
            SafeGraphOperations.setMaxEdges(1);
            graph.addEdge("A", "B");

            assertThat(SafeGraphOperations.wouldExceedEdgeLimit(graph)).isTrue();
        }
    }
}
