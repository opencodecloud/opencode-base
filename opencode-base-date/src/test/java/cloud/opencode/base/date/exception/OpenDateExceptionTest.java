package cloud.opencode.base.date.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenDateException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("OpenDateException 测试")
class OpenDateExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息构造")
        void testConstructorWithMessage() {
            OpenDateException exception = new OpenDateException("Test message");

            assertThat(exception.getMessage()).isEqualTo("[date] Test message");
            assertThat(exception.getRawMessage()).isEqualTo("Test message");
            assertThat(exception.getComponent()).isEqualTo("date");
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getInputValue()).isNull();
            assertThat(exception.getExpectedFormat()).isNull();
        }

        @Test
        @DisplayName("使用消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("cause");
            OpenDateException exception = new OpenDateException("Test message", cause);

            assertThat(exception.getMessage()).isEqualTo("[date] Test message");
            assertThat(exception.getRawMessage()).isEqualTo("Test message");
            assertThat(exception.getCause()).isSameAs(cause);
            assertThat(exception.getInputValue()).isNull();
            assertThat(exception.getExpectedFormat()).isNull();
        }

        @Test
        @DisplayName("使用消息、输入值和格式构造")
        void testConstructorWithInputAndFormat() {
            OpenDateException exception = new OpenDateException(
                    "Test message",
                    "2024-13-45",
                    "yyyy-MM-dd"
            );

            assertThat(exception.getMessage()).isEqualTo("[date] Test message");
            assertThat(exception.getRawMessage()).isEqualTo("Test message");
            assertThat(exception.getInputValue()).isEqualTo("2024-13-45");
            assertThat(exception.getExpectedFormat()).isEqualTo("yyyy-MM-dd");
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("使用消息、输入值、格式和原因构造")
        void testConstructorWithAllParams() {
            Throwable cause = new RuntimeException("cause");
            OpenDateException exception = new OpenDateException(
                    "Test message",
                    "2024-13-45",
                    "yyyy-MM-dd",
                    cause
            );

            assertThat(exception.getMessage()).isEqualTo("[date] Test message");
            assertThat(exception.getRawMessage()).isEqualTo("Test message");
            assertThat(exception.getInputValue()).isEqualTo("2024-13-45");
            assertThat(exception.getExpectedFormat()).isEqualTo("yyyy-MM-dd");
            assertThat(exception.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("parseError(input, pattern) 创建解析错误")
        void testParseErrorWithPattern() {
            OpenDateException exception = OpenDateException.parseError("2024-13-45", "yyyy-MM-dd");

            assertThat(exception.getMessage()).contains("2024-13-45");
            assertThat(exception.getMessage()).contains("yyyy-MM-dd");
            assertThat(exception.getInputValue()).isEqualTo("2024-13-45");
            assertThat(exception.getExpectedFormat()).isEqualTo("yyyy-MM-dd");
        }

        @Test
        @DisplayName("parseError(input, pattern, cause) 创建带原因的解析错误")
        void testParseErrorWithPatternAndCause() {
            Throwable cause = new RuntimeException("parse failed");
            OpenDateException exception = OpenDateException.parseError("2024-13-45", "yyyy-MM-dd", cause);

            assertThat(exception.getMessage()).contains("2024-13-45");
            assertThat(exception.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("parseError(input) 创建智能解析错误")
        void testParseErrorSmartParsing() {
            OpenDateException exception = OpenDateException.parseError("invalid-date");

            assertThat(exception.getMessage()).contains("invalid-date");
            assertThat(exception.getMessage()).contains("no matching format found");
            assertThat(exception.getInputValue()).isEqualTo("invalid-date");
            assertThat(exception.getExpectedFormat()).isNull();
        }

        @Test
        @DisplayName("formatError(message) 创建格式化错误")
        void testFormatError() {
            OpenDateException exception = OpenDateException.formatError("Unable to format date");

            assertThat(exception.getMessage()).contains("Format error:");
            assertThat(exception.getMessage()).contains("Unable to format date");
        }

        @Test
        @DisplayName("formatError(message, cause) 创建带原因的格式化错误")
        void testFormatErrorWithCause() {
            Throwable cause = new RuntimeException("format failed");
            OpenDateException exception = OpenDateException.formatError("Unable to format date", cause);

            assertThat(exception.getMessage()).contains("Format error:");
            assertThat(exception.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("invalidValue() 创建无效值错误")
        void testInvalidValue() {
            OpenDateException exception = OpenDateException.invalidValue("month", 13, "1-12");

            assertThat(exception.getMessage()).contains("Invalid month value");
            assertThat(exception.getMessage()).contains("13");
            assertThat(exception.getMessage()).contains("1-12");
            assertThat(exception.getInputValue()).isEqualTo("13");
            assertThat(exception.getExpectedFormat()).isEqualTo("1-12");
        }

        @Test
        @DisplayName("timezoneError() 创建时区错误")
        void testTimezoneError() {
            OpenDateException exception = OpenDateException.timezoneError("Invalid/Timezone");

            assertThat(exception.getMessage()).contains("Unknown timezone");
            assertThat(exception.getMessage()).contains("Invalid/Timezone");
            assertThat(exception.getInputValue()).isEqualTo("Invalid/Timezone");
        }

        @Test
        @DisplayName("rangeError() 创建范围错误")
        void testRangeError() {
            OpenDateException exception = OpenDateException.rangeError("End date before start date");

            assertThat(exception.getMessage()).contains("Range error:");
            assertThat(exception.getMessage()).contains("End date before start date");
        }

        @Test
        @DisplayName("cronError() 创建Cron表达式错误")
        void testCronError() {
            OpenDateException exception = OpenDateException.cronError("* * *", "Must have 5 fields");

            assertThat(exception.getMessage()).contains("Invalid cron expression");
            assertThat(exception.getMessage()).contains("* * *");
            assertThat(exception.getMessage()).contains("Must have 5 fields");
            assertThat(exception.getInputValue()).isEqualTo("* * *");
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("getInputValue() 获取输入值")
        void testGetInputValue() {
            OpenDateException exception = new OpenDateException("msg", "input", "format");
            assertThat(exception.getInputValue()).isEqualTo("input");
        }

        @Test
        @DisplayName("getExpectedFormat() 获取期望格式")
        void testGetExpectedFormat() {
            OpenDateException exception = new OpenDateException("msg", "input", "format");
            assertThat(exception.getExpectedFormat()).isEqualTo("format");
        }

        @Test
        @DisplayName("hasInputValue() 检查输入值是否存在")
        void testHasInputValue() {
            OpenDateException withInput = new OpenDateException("msg", "input", "format");
            OpenDateException withoutInput = new OpenDateException("msg");

            assertThat(withInput.hasInputValue()).isTrue();
            assertThat(withoutInput.hasInputValue()).isFalse();
        }

        @Test
        @DisplayName("hasExpectedFormat() 检查期望格式是否存在")
        void testHasExpectedFormat() {
            OpenDateException withFormat = new OpenDateException("msg", "input", "format");
            OpenDateException withoutFormat = new OpenDateException("msg", "input", null);

            assertThat(withFormat.hasExpectedFormat()).isTrue();
            assertThat(withoutFormat.hasExpectedFormat()).isFalse();
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自OpenException")
        void testExtendsOpenException() {
            OpenDateException exception = new OpenDateException("test");

            assertThat(exception).isInstanceOf(OpenException.class);
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception).isInstanceOf(Exception.class);
            assertThat(exception).isInstanceOf(Throwable.class);
        }

        @Test
        @DisplayName("可以被捕获为RuntimeException")
        void testCatchAsRuntimeException() {
            assertThatThrownBy(() -> {
                throw new OpenDateException("test");
            }).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被抛出和重新抛出")
        void testThrowAndRethrow() {
            OpenDateException original = new OpenDateException("original", "input", "format");

            assertThatThrownBy(() -> {
                try {
                    throw original;
                } catch (OpenDateException e) {
                    throw e;
                }
            })
                    .isSameAs(original)
                    .hasMessage("[date] original");
        }
    }
}
