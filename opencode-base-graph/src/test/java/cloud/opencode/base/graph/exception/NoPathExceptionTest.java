package cloud.opencode.base.graph.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * NoPathException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("NoPathException 测试")
class NoPathExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("源和目标顶点构造函数")
        void testSourceTargetConstructor() {
            NoPathException ex = new NoPathException("A", "B");

            assertThat(ex.getMessage()).contains("No path exists");
            assertThat(ex.getMessage()).contains("A");
            assertThat(ex.getMessage()).contains("B");
            assertThat(ex.getGraphErrorCode()).isEqualTo(GraphErrorCode.NO_PATH);
        }
    }

    @Nested
    @DisplayName("getSource和getTarget测试")
    class GetSourceTargetTests {

        @Test
        @DisplayName("返回源顶点")
        void testGetSource() {
            NoPathException ex = new NoPathException("source", "target");

            assertThat(ex.getSource()).isEqualTo("source");
        }

        @Test
        @DisplayName("返回目标顶点")
        void testGetTarget() {
            NoPathException ex = new NoPathException("source", "target");

            assertThat(ex.getTarget()).isEqualTo("target");
        }

        @Test
        @DisplayName("支持不同类型")
        void testDifferentTypes() {
            NoPathException ex = new NoPathException(1, 99);

            assertThat(ex.getSource()).isEqualTo(1);
            assertThat(ex.getTarget()).isEqualTo(99);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GraphException子类")
        void testIsGraphException() {
            NoPathException ex = new NoPathException("A", "B");

            assertThat(ex).isInstanceOf(GraphException.class);
        }
    }

    @Nested
    @DisplayName("错误码测试")
    class ErrorCodeTests {

        @Test
        @DisplayName("错误码为NO_PATH")
        void testErrorCode() {
            NoPathException ex = new NoPathException("A", "B");

            assertThat(ex.getGraphErrorCode()).isEqualTo(GraphErrorCode.NO_PATH);
            assertThat(ex.getGraphErrorCode().getCode()).isEqualTo(2002);
        }
    }
}
