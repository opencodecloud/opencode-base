package cloud.opencode.base.graph.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("GraphException 测试")
class GraphExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("消息和错误码构造函数")
        void testMessageAndErrorCodeConstructor() {
            GraphException ex = new GraphException("Test error", GraphErrorCode.UNKNOWN);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getErrorCode()).isEqualTo(GraphErrorCode.UNKNOWN);
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("消息、原因和错误码构造函数")
        void testMessageCauseAndErrorCodeConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            GraphException ex = new GraphException("Test error", cause, GraphErrorCode.CYCLE_DETECTED);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(GraphErrorCode.CYCLE_DETECTED);
        }
    }

    @Nested
    @DisplayName("getErrorCode测试")
    class GetErrorCodeTests {

        @Test
        @DisplayName("返回正确的错误码")
        void testGetErrorCode() {
            GraphException ex1 = new GraphException("error", GraphErrorCode.VERTEX_NOT_FOUND);
            GraphException ex2 = new GraphException("error", GraphErrorCode.NO_PATH);

            assertThat(ex1.getErrorCode()).isEqualTo(GraphErrorCode.VERTEX_NOT_FOUND);
            assertThat(ex2.getErrorCode()).isEqualTo(GraphErrorCode.NO_PATH);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是RuntimeException子类")
        void testIsRuntimeException() {
            GraphException ex = new GraphException("test", GraphErrorCode.UNKNOWN);

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被catch为Exception")
        void testCanBeCaughtAsException() {
            try {
                throw new GraphException("test", GraphErrorCode.UNKNOWN);
            } catch (Exception e) {
                assertThat(e).isInstanceOf(GraphException.class);
            }
        }
    }
}
