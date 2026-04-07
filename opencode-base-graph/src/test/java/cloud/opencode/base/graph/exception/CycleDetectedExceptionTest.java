package cloud.opencode.base.graph.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CycleDetectedException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("CycleDetectedException 测试")
class CycleDetectedExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("环列表构造函数")
        void testCycleListConstructor() {
            List<String> cycle = List.of("A", "B", "C", "A");
            CycleDetectedException ex = new CycleDetectedException(cycle);

            assertThat(ex.getMessage()).contains("Cycle detected");
            assertThat(ex.getGraphErrorCode()).isEqualTo(GraphErrorCode.CYCLE_DETECTED);
            assertThat(ex.getCycle()).isEqualTo(cycle);
        }

        @Test
        @DisplayName("消息构造函数")
        void testMessageConstructor() {
            CycleDetectedException ex = new CycleDetectedException("Topological sort not possible");

            assertThat(ex.getMessage()).contains("Topological sort not possible");
            assertThat(ex.getGraphErrorCode()).isEqualTo(GraphErrorCode.CYCLE_DETECTED);
            assertThat(ex.getCycle()).isNull();
        }
    }

    @Nested
    @DisplayName("getCycle测试")
    class GetCycleTests {

        @Test
        @DisplayName("返回检测到的环")
        void testGetCycle() {
            List<String> cycle = List.of("X", "Y", "Z", "X");
            CycleDetectedException ex = new CycleDetectedException(cycle);

            assertThat(ex.getCycle()).isEqualTo(List.of("X", "Y", "Z", "X"));
        }

        @Test
        @DisplayName("消息构造时返回null")
        void testGetCycleNull() {
            CycleDetectedException ex = new CycleDetectedException("message");

            assertThat(ex.getCycle()).isNull();
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GraphException子类")
        void testIsGraphException() {
            CycleDetectedException ex = new CycleDetectedException("test");

            assertThat(ex).isInstanceOf(GraphException.class);
        }
    }

    @Nested
    @DisplayName("错误码测试")
    class ErrorCodeTests {

        @Test
        @DisplayName("错误码为CYCLE_DETECTED")
        void testErrorCode() {
            CycleDetectedException ex = new CycleDetectedException("test");

            assertThat(ex.getGraphErrorCode()).isEqualTo(GraphErrorCode.CYCLE_DETECTED);
            assertThat(ex.getGraphErrorCode().getCode()).isEqualTo(2001);
        }
    }
}
