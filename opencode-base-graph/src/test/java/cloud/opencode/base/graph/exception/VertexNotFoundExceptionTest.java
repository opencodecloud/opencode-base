package cloud.opencode.base.graph.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * VertexNotFoundException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("VertexNotFoundException 测试")
class VertexNotFoundExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("顶点构造函数")
        void testVertexConstructor() {
            VertexNotFoundException ex = new VertexNotFoundException("A");

            assertThat(ex.getMessage()).contains("Vertex not found");
            assertThat(ex.getMessage()).contains("A");
            assertThat(ex.getErrorCode()).isEqualTo(GraphErrorCode.VERTEX_NOT_FOUND);
        }

        @Test
        @DisplayName("Integer类型顶点")
        void testIntegerVertex() {
            VertexNotFoundException ex = new VertexNotFoundException(42);

            assertThat(ex.getMessage()).contains("42");
            assertThat(ex.getVertex()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("getVertex测试")
    class GetVertexTests {

        @Test
        @DisplayName("返回缺失的顶点")
        void testGetVertex() {
            VertexNotFoundException ex = new VertexNotFoundException("missing");

            assertThat(ex.getVertex()).isEqualTo("missing");
        }

        @Test
        @DisplayName("支持不同类型顶点")
        void testDifferentTypes() {
            VertexNotFoundException ex1 = new VertexNotFoundException("string");
            VertexNotFoundException ex2 = new VertexNotFoundException(123);

            assertThat(ex1.getVertex()).isEqualTo("string");
            assertThat(ex2.getVertex()).isEqualTo(123);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GraphException子类")
        void testIsGraphException() {
            VertexNotFoundException ex = new VertexNotFoundException("test");

            assertThat(ex).isInstanceOf(GraphException.class);
        }
    }

    @Nested
    @DisplayName("错误码测试")
    class ErrorCodeTests {

        @Test
        @DisplayName("错误码为VERTEX_NOT_FOUND")
        void testErrorCode() {
            VertexNotFoundException ex = new VertexNotFoundException("test");

            assertThat(ex.getErrorCode()).isEqualTo(GraphErrorCode.VERTEX_NOT_FOUND);
            assertThat(ex.getErrorCode().getCode()).isEqualTo(1001);
        }
    }
}
