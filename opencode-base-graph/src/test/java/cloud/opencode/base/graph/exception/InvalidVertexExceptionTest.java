package cloud.opencode.base.graph.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * InvalidVertexException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("InvalidVertexException 测试")
class InvalidVertexExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("消息构造函数")
        void testMessageConstructor() {
            InvalidVertexException ex = new InvalidVertexException("Vertex cannot be null");

            assertThat(ex.getMessage()).isEqualTo("Vertex cannot be null");
            assertThat(ex.getErrorCode()).isEqualTo(GraphErrorCode.INVALID_VERTEX);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GraphException子类")
        void testIsGraphException() {
            InvalidVertexException ex = new InvalidVertexException("test");

            assertThat(ex).isInstanceOf(GraphException.class);
        }

        @Test
        @DisplayName("是RuntimeException子类")
        void testIsRuntimeException() {
            InvalidVertexException ex = new InvalidVertexException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("错误码测试")
    class ErrorCodeTests {

        @Test
        @DisplayName("错误码为INVALID_VERTEX")
        void testErrorCode() {
            InvalidVertexException ex = new InvalidVertexException("test");

            assertThat(ex.getErrorCode()).isEqualTo(GraphErrorCode.INVALID_VERTEX);
            assertThat(ex.getErrorCode().getCode()).isEqualTo(3001);
        }
    }
}
