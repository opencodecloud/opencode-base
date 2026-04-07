package cloud.opencode.base.graph.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphTimeoutException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("GraphTimeoutException 测试")
class GraphTimeoutExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            GraphTimeoutException ex = new GraphTimeoutException("Computation timed out");

            assertThat(ex.getMessage()).contains("Computation timed out");
            assertThat(ex.getGraphErrorCode()).isEqualTo(GraphErrorCode.TIMEOUT);
            assertThat(ex.getTimeout()).isNull();
        }

        @Test
        @DisplayName("消息和超时时长构造函数")
        void testMessageAndTimeoutConstructor() {
            Duration timeout = Duration.ofSeconds(30);
            GraphTimeoutException ex = new GraphTimeoutException("Computation timed out", timeout);

            assertThat(ex.getMessage()).contains("Computation timed out");
            assertThat(ex.getMessage()).contains("PT30S");
            assertThat(ex.getTimeout()).isEqualTo(timeout);
        }
    }

    @Nested
    @DisplayName("getTimeout测试")
    class GetTimeoutTests {

        @Test
        @DisplayName("返回超时时长")
        void testGetTimeout() {
            Duration timeout = Duration.ofMinutes(5);
            GraphTimeoutException ex = new GraphTimeoutException("test", timeout);

            assertThat(ex.getTimeout()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("无超时时长返回null")
        void testGetTimeoutNull() {
            GraphTimeoutException ex = new GraphTimeoutException("test");

            assertThat(ex.getTimeout()).isNull();
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GraphException子类")
        void testIsGraphException() {
            GraphTimeoutException ex = new GraphTimeoutException("test");

            assertThat(ex).isInstanceOf(GraphException.class);
        }
    }

    @Nested
    @DisplayName("错误码测试")
    class ErrorCodeTests {

        @Test
        @DisplayName("错误码为TIMEOUT")
        void testErrorCode() {
            GraphTimeoutException ex = new GraphTimeoutException("test");

            assertThat(ex.getGraphErrorCode()).isEqualTo(GraphErrorCode.TIMEOUT);
            assertThat(ex.getGraphErrorCode().getCode()).isEqualTo(4002);
        }
    }
}
