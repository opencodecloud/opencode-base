package cloud.opencode.base.cron.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenCronExceptionTest Tests
 * OpenCronExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.0
 */
@DisplayName("OpenCronException 测试")
class OpenCronExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("单参数构造")
        void should_create_with_message() {
            OpenCronException ex = new OpenCronException("error");
            assertThat(ex.getRawMessage()).isEqualTo("error");
            assertThat(ex.getMessage()).contains("error");
            assertThat(ex.getComponent()).isEqualTo("cron");
            assertThat(ex.getExpression()).isNull();
            assertThat(ex.getField()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("消息+原因构造")
        void should_create_with_message_and_cause() {
            Throwable cause = new RuntimeException("root");
            OpenCronException ex = new OpenCronException("error", cause);
            assertThat(ex.getRawMessage()).isEqualTo("error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getExpression()).isNull();
            assertThat(ex.getField()).isNull();
        }

        @Test
        @DisplayName("全参数构造")
        void should_create_with_all_fields() {
            Throwable cause = new RuntimeException("root");
            OpenCronException ex = new OpenCronException("error", "* * * *", "minute", cause);
            assertThat(ex.getRawMessage()).isEqualTo("error");
            assertThat(ex.getExpression()).isEqualTo("* * * *");
            assertThat(ex.getField()).isEqualTo("minute");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("parseError 包含表达式和原因")
        void should_create_parse_error() {
            OpenCronException ex = OpenCronException.parseError("bad expr", "too few fields");
            assertThat(ex.getMessage()).contains("bad expr").contains("too few fields");
            assertThat(ex.getExpression()).isEqualTo("bad expr");
            assertThat(ex.getField()).isNull();
        }

        @Test
        @DisplayName("parseError 带cause")
        void should_create_parse_error_with_cause() {
            Throwable cause = new NumberFormatException("abc");
            OpenCronException ex = OpenCronException.parseError("bad", "number", cause);
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getExpression()).isEqualTo("bad");
        }

        @Test
        @DisplayName("fieldError(int) 包含字段名和范围")
        void should_create_field_error_int() {
            OpenCronException ex = OpenCronException.fieldError("hour", 25, 0, 23);
            assertThat(ex.getMessage()).contains("25").contains("0").contains("23").contains("hour");
            assertThat(ex.getField()).isEqualTo("hour");
            assertThat(ex.getExpression()).isNull();
        }

        @Test
        @DisplayName("fieldError(String) 包含字段名和原因")
        void should_create_field_error_string() {
            OpenCronException ex = OpenCronException.fieldError("second", "*/0", "step must be positive");
            assertThat(ex.getMessage()).contains("second").contains("*/0").contains("step must be positive");
            assertThat(ex.getField()).isEqualTo("second");
        }

        @Test
        @DisplayName("unknownMacro 包含宏名")
        void should_create_unknown_macro() {
            OpenCronException ex = OpenCronException.unknownMacro("@secondly");
            assertThat(ex.getMessage()).contains("@secondly");
            assertThat(ex.getExpression()).isEqualTo("@secondly");
        }
    }

    @Nested
    @DisplayName("Getter 测试")
    class GetterTests {

        @Test
        @DisplayName("getExpression 返回表达式")
        void should_return_expression() {
            OpenCronException ex = OpenCronException.parseError("0 0 * *", "4 fields");
            assertThat(ex.getExpression()).isEqualTo("0 0 * *");
        }

        @Test
        @DisplayName("getField 返回字段")
        void should_return_field() {
            OpenCronException ex = OpenCronException.fieldError("month", 13, 1, 12);
            assertThat(ex.getField()).isEqualTo("month");
        }

        @Test
        @DisplayName("无诊断信息时返回null")
        void should_return_null_when_not_set() {
            OpenCronException ex = new OpenCronException("generic error");
            assertThat(ex.getExpression()).isNull();
            assertThat(ex.getField()).isNull();
        }
    }

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("OpenCronException 是 Serializable")
        void should_be_serializable() {
            OpenCronException ex = OpenCronException.parseError("test", "reason");
            assertThat(ex).isInstanceOf(java.io.Serializable.class);
        }

        @Test
        @DisplayName("继承 OpenException → RuntimeException")
        void should_extend_open_exception() {
            OpenCronException ex = new OpenCronException("test");
            assertThat(ex).isInstanceOf(OpenException.class);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
