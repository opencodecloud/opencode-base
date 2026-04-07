package cloud.opencode.base.graph.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EdgeNotFoundException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("EdgeNotFoundException 测试")
class EdgeNotFoundExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("源和目标顶点构造函数")
        void testFromToConstructor() {
            EdgeNotFoundException ex = new EdgeNotFoundException("A", "B");

            assertThat(ex.getMessage()).contains("Edge not found");
            assertThat(ex.getMessage()).contains("A");
            assertThat(ex.getMessage()).contains("B");
            assertThat(ex.getGraphErrorCode()).isEqualTo(GraphErrorCode.EDGE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getFrom和getTo测试")
    class GetFromToTests {

        @Test
        @DisplayName("返回源顶点")
        void testGetFrom() {
            EdgeNotFoundException ex = new EdgeNotFoundException("source", "target");

            assertThat(ex.getFrom()).isEqualTo("source");
        }

        @Test
        @DisplayName("返回目标顶点")
        void testGetTo() {
            EdgeNotFoundException ex = new EdgeNotFoundException("source", "target");

            assertThat(ex.getTo()).isEqualTo("target");
        }

        @Test
        @DisplayName("支持不同类型顶点")
        void testDifferentTypes() {
            EdgeNotFoundException ex = new EdgeNotFoundException(1, 2);

            assertThat(ex.getFrom()).isEqualTo(1);
            assertThat(ex.getTo()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GraphException子类")
        void testIsGraphException() {
            EdgeNotFoundException ex = new EdgeNotFoundException("A", "B");

            assertThat(ex).isInstanceOf(GraphException.class);
        }
    }

    @Nested
    @DisplayName("错误码测试")
    class ErrorCodeTests {

        @Test
        @DisplayName("错误码为EDGE_NOT_FOUND")
        void testErrorCode() {
            EdgeNotFoundException ex = new EdgeNotFoundException("A", "B");

            assertThat(ex.getGraphErrorCode()).isEqualTo(GraphErrorCode.EDGE_NOT_FOUND);
            assertThat(ex.getGraphErrorCode().getCode()).isEqualTo(1002);
        }
    }
}
