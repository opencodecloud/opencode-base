package cloud.opencode.base.lunar.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LunarException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("LunarException 测试")
class LunarExceptionTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("继承RuntimeException")
        void testExtendsRuntimeException() {
            LunarException exception = new LunarException("test");
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageConstructor() {
            LunarException exception = new LunarException("Test message");

            assertThat(exception.getMessage()).isEqualTo("Test message");
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getErrorCode()).isEqualTo(LunarErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            Throwable cause = new RuntimeException("cause");
            LunarException exception = new LunarException("Test message", cause);

            assertThat(exception.getMessage()).isEqualTo("Test message");
            assertThat(exception.getCause()).isSameAs(cause);
            assertThat(exception.getErrorCode()).isEqualTo(LunarErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("消息和错误码构造函数")
        void testMessageAndErrorCodeConstructor() {
            LunarException exception = new LunarException("Custom message", LunarErrorCode.YEAR_OUT_OF_RANGE);

            assertThat(exception.getErrorCode()).isEqualTo(LunarErrorCode.YEAR_OUT_OF_RANGE);
            assertThat(exception.getMessage()).isEqualTo("Custom message");
        }

        @Test
        @DisplayName("消息、原因和错误码构造函数")
        void testMessageCauseAndErrorCodeConstructor() {
            Throwable cause = new RuntimeException("cause");
            LunarException exception = new LunarException("Custom message", cause, LunarErrorCode.CONVERSION_FAILED);

            assertThat(exception.getErrorCode()).isEqualTo(LunarErrorCode.CONVERSION_FAILED);
            assertThat(exception.getMessage()).isEqualTo("Custom message");
            assertThat(exception.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("getErrorCode方法测试")
    class GetErrorCodeTests {

        @Test
        @DisplayName("返回正确的错误码")
        void testGetErrorCode() {
            LunarException exception = new LunarException("message", LunarErrorCode.YEAR_OUT_OF_RANGE);
            assertThat(exception.getErrorCode()).isEqualTo(LunarErrorCode.YEAR_OUT_OF_RANGE);
        }

        @Test
        @DisplayName("默认错误码是UNKNOWN")
        void testDefaultErrorCode() {
            LunarException exception = new LunarException("test");
            assertThat(exception.getErrorCode()).isEqualTo(LunarErrorCode.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含异常类名")
        void testToString() {
            LunarException exception = new LunarException("Year 1800", LunarErrorCode.YEAR_OUT_OF_RANGE);

            String str = exception.toString();
            assertThat(str).contains("LunarException");
        }
    }
}
