package cloud.opencode.base.graph.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphLimitExceededException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("GraphLimitExceededException 测试")
class GraphLimitExceededExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            GraphLimitExceededException ex = new GraphLimitExceededException("Limit exceeded");

            assertThat(ex.getMessage()).isEqualTo("Limit exceeded");
            assertThat(ex.getErrorCode()).isEqualTo(GraphErrorCode.LIMIT_EXCEEDED);
            assertThat(ex.getLimit()).isEqualTo(-1);
            assertThat(ex.getActual()).isEqualTo(-1);
        }

        @Test
        @DisplayName("消息、限制和实际值构造函数")
        void testMessageLimitActualConstructor() {
            GraphLimitExceededException ex = new GraphLimitExceededException("Vertex limit exceeded", 1000, 1500);

            assertThat(ex.getMessage()).contains("Vertex limit exceeded");
            assertThat(ex.getMessage()).contains("1000");
            assertThat(ex.getMessage()).contains("1500");
            assertThat(ex.getLimit()).isEqualTo(1000);
            assertThat(ex.getActual()).isEqualTo(1500);
        }
    }

    @Nested
    @DisplayName("getLimit和getActual测试")
    class GetLimitActualTests {

        @Test
        @DisplayName("返回配置的限制")
        void testGetLimit() {
            GraphLimitExceededException ex = new GraphLimitExceededException("test", 500, 600);

            assertThat(ex.getLimit()).isEqualTo(500);
        }

        @Test
        @DisplayName("返回实际值")
        void testGetActual() {
            GraphLimitExceededException ex = new GraphLimitExceededException("test", 500, 600);

            assertThat(ex.getActual()).isEqualTo(600);
        }

        @Test
        @DisplayName("无详情时返回-1")
        void testDefaultValues() {
            GraphLimitExceededException ex = new GraphLimitExceededException("test");

            assertThat(ex.getLimit()).isEqualTo(-1);
            assertThat(ex.getActual()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GraphException子类")
        void testIsGraphException() {
            GraphLimitExceededException ex = new GraphLimitExceededException("test");

            assertThat(ex).isInstanceOf(GraphException.class);
        }
    }

    @Nested
    @DisplayName("错误码测试")
    class ErrorCodeTests {

        @Test
        @DisplayName("错误码为LIMIT_EXCEEDED")
        void testErrorCode() {
            GraphLimitExceededException ex = new GraphLimitExceededException("test");

            assertThat(ex.getErrorCode()).isEqualTo(GraphErrorCode.LIMIT_EXCEEDED);
            assertThat(ex.getErrorCode().getCode()).isEqualTo(4001);
        }
    }
}
