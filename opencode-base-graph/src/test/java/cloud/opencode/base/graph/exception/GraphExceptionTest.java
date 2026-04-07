package cloud.opencode.base.graph.exception;

import cloud.opencode.base.core.exception.OpenException;
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

            assertThat(ex.getMessage()).contains("Test error");
            assertThat(ex.getGraphErrorCode()).isEqualTo(GraphErrorCode.UNKNOWN);
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("消息、原因和错误码构造函数")
        void testMessageCauseAndErrorCodeConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            GraphException ex = new GraphException("Test error", cause, GraphErrorCode.CYCLE_DETECTED);

            assertThat(ex.getMessage()).contains("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getGraphErrorCode()).isEqualTo(GraphErrorCode.CYCLE_DETECTED);
        }
    }

    @Nested
    @DisplayName("getGraphErrorCode测试")
    class GetGraphErrorCodeTests {

        @Test
        @DisplayName("返回正确的错误码")
        void testGetGraphErrorCode() {
            GraphException ex1 = new GraphException("error", GraphErrorCode.VERTEX_NOT_FOUND);
            GraphException ex2 = new GraphException("error", GraphErrorCode.NO_PATH);

            assertThat(ex1.getGraphErrorCode()).isEqualTo(GraphErrorCode.VERTEX_NOT_FOUND);
            assertThat(ex2.getGraphErrorCode()).isEqualTo(GraphErrorCode.NO_PATH);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            GraphException ex = new GraphException("test", GraphErrorCode.UNKNOWN);

            assertThat(ex).isInstanceOf(OpenException.class);
        }

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
