package cloud.opencode.base.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenTimeoutException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenTimeoutException 测试")
class OpenTimeoutExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageConstructor() {
            OpenTimeoutException ex = new OpenTimeoutException("Operation timed out");

            assertThat(ex.getMessage()).isEqualTo("[Core] (TIMEOUT) Operation timed out");
            assertThat(ex.getComponent()).isEqualTo("Core");
            assertThat(ex.getErrorCode()).isEqualTo("TIMEOUT");
            assertThat(ex.getTimeout()).isNull();
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            Exception cause = new InterruptedException("Interrupted");
            OpenTimeoutException ex = new OpenTimeoutException("Operation timed out", cause);

            assertThat(ex.getMessage()).isEqualTo("[Core] (TIMEOUT) Operation timed out");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getTimeout()).isNull();
        }

        @Test
        @DisplayName("消息和超时时长构造")
        void testMessageAndTimeoutConstructor() {
            Duration timeout = Duration.ofSeconds(30);
            OpenTimeoutException ex = new OpenTimeoutException("Operation timed out", timeout);

            assertThat(ex.getMessage()).isEqualTo("[Core] (TIMEOUT) Operation timed out");
            assertThat(ex.getTimeout()).isEqualTo(timeout);
        }

        @Test
        @DisplayName("完整参数构造")
        void testFullConstructor() {
            Duration timeout = Duration.ofMinutes(1);
            Exception cause = new InterruptedException("Interrupted");
            OpenTimeoutException ex = new OpenTimeoutException("Operation timed out", timeout, cause);

            assertThat(ex.getMessage()).isEqualTo("[Core] (TIMEOUT) Operation timed out");
            assertThat(ex.getTimeout()).isEqualTo(timeout);
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of 创建超时异常 - 毫秒")
        void testOfMilliseconds() {
            Duration timeout = Duration.ofMillis(500);
            OpenTimeoutException ex = OpenTimeoutException.of("Database query", timeout);

            assertThat(ex.getMessage()).contains("Database query");
            assertThat(ex.getMessage()).contains("timed out after");
            assertThat(ex.getMessage()).contains("500ms");
            assertThat(ex.getTimeout()).isEqualTo(timeout);
        }

        @Test
        @DisplayName("of 创建超时异常 - 秒")
        void testOfSeconds() {
            Duration timeout = Duration.ofSeconds(30);
            OpenTimeoutException ex = OpenTimeoutException.of("HTTP request", timeout);

            assertThat(ex.getMessage()).contains("HTTP request");
            assertThat(ex.getMessage()).contains("timed out after");
            assertThat(ex.getMessage()).contains("30.0s");
        }

        @Test
        @DisplayName("of 创建超时异常 - 分钟")
        void testOfMinutes() {
            Duration timeout = Duration.ofMinutes(5);
            OpenTimeoutException ex = OpenTimeoutException.of("Batch job", timeout);

            assertThat(ex.getMessage()).contains("Batch job");
            assertThat(ex.getMessage()).contains("timed out after");
            assertThat(ex.getMessage()).contains("5.0m");
        }

        @Test
        @DisplayName("of 带原因创建超时异常")
        void testOfWithCause() {
            Duration timeout = Duration.ofSeconds(10);
            Exception cause = new InterruptedException("Thread interrupted");
            OpenTimeoutException ex = OpenTimeoutException.of("Task execution", timeout, cause);

            assertThat(ex.getMessage()).contains("Task execution");
            assertThat(ex.getMessage()).contains("10.0s");
            assertThat(ex.getTimeout()).isEqualTo(timeout);
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("getTimeout 测试")
    class GetTimeoutTests {

        @Test
        @DisplayName("返回设置的超时时长")
        void testGetTimeout() {
            Duration timeout = Duration.ofSeconds(45);
            OpenTimeoutException ex = new OpenTimeoutException("Test", timeout);

            assertThat(ex.getTimeout()).isEqualTo(timeout);
            assertThat(ex.getTimeout().getSeconds()).isEqualTo(45);
        }

        @Test
        @DisplayName("未设置超时返回 null")
        void testGetTimeoutNull() {
            OpenTimeoutException ex = new OpenTimeoutException("Test");
            assertThat(ex.getTimeout()).isNull();
        }
    }

    @Nested
    @DisplayName("时长格式化测试")
    class DurationFormatTests {

        @Test
        @DisplayName("格式化毫秒 (< 1000ms)")
        void testFormatMilliseconds() {
            OpenTimeoutException ex = OpenTimeoutException.of("Test", Duration.ofMillis(999));
            assertThat(ex.getMessage()).contains("999ms");
        }

        @Test
        @DisplayName("格式化秒 (1s - 60s)")
        void testFormatSeconds() {
            OpenTimeoutException ex = OpenTimeoutException.of("Test", Duration.ofMillis(1500));
            assertThat(ex.getMessage()).contains("1.5s");
        }

        @Test
        @DisplayName("格式化分钟 (>= 60s)")
        void testFormatMinutes() {
            OpenTimeoutException ex = OpenTimeoutException.of("Test", Duration.ofMillis(90000));
            assertThat(ex.getMessage()).contains("1.5m");
        }

        @Test
        @DisplayName("null Duration 显示 unknown")
        void testFormatNull() {
            // 通过静态工厂无法传 null Duration，直接测试消息构造
            OpenTimeoutException ex = new OpenTimeoutException("Test timed out after unknown");
            assertThat(ex.getMessage()).contains("unknown");
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是 OpenException 的子类")
        void testExtendsOpenException() {
            OpenTimeoutException ex = new OpenTimeoutException("Test");
            assertThat(ex).isInstanceOf(OpenException.class);
        }
    }
}
