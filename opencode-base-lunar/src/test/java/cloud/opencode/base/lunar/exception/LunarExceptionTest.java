package cloud.opencode.base.lunar.exception;

import cloud.opencode.base.core.exception.OpenException;
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
        @DisplayName("继承OpenException")
        void testExtendsOpenException() {
            LunarException exception = new LunarException("test");
            assertThat(exception).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("继承RuntimeException")
        void testExtendsRuntimeException() {
            LunarException exception = new LunarException("test");
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("组件名为Lunar")
        void testComponent() {
            LunarException exception = new LunarException("test");
            assertThat(exception.getComponent()).isEqualTo("Lunar");
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageConstructor() {
            LunarException exception = new LunarException("Test message");

            assertThat(exception.getRawMessage()).isEqualTo("Test message");
            assertThat(exception.getMessage()).contains("Test message");
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.UNKNOWN);
            assertThat(exception.getErrorCode()).isEqualTo("LUNAR_0");
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            Throwable cause = new RuntimeException("cause");
            LunarException exception = new LunarException("Test message", cause);

            assertThat(exception.getRawMessage()).isEqualTo("Test message");
            assertThat(exception.getMessage()).contains("Test message");
            assertThat(exception.getCause()).isSameAs(cause);
            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("消息和错误码构造函数")
        void testMessageAndErrorCodeConstructor() {
            LunarException exception = new LunarException("Custom message", LunarErrorCode.YEAR_OUT_OF_RANGE);

            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.YEAR_OUT_OF_RANGE);
            assertThat(exception.getRawMessage()).isEqualTo("Custom message");
            assertThat(exception.getMessage()).contains("Custom message");
            assertThat(exception.getErrorCode()).isEqualTo("LUNAR_2001");
        }

        @Test
        @DisplayName("消息、原因和错误码构造函数")
        void testMessageCauseAndErrorCodeConstructor() {
            Throwable cause = new RuntimeException("cause");
            LunarException exception = new LunarException("Custom message", cause, LunarErrorCode.CONVERSION_FAILED);

            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.CONVERSION_FAILED);
            assertThat(exception.getRawMessage()).isEqualTo("Custom message");
            assertThat(exception.getMessage()).contains("Custom message");
            assertThat(exception.getCause()).isSameAs(cause);
            assertThat(exception.getErrorCode()).isEqualTo("LUNAR_1001");
        }
    }

    @Nested
    @DisplayName("getLunarErrorCode方法测试")
    class GetLunarErrorCodeTests {

        @Test
        @DisplayName("返回正确的错误码")
        void testGetLunarErrorCode() {
            LunarException exception = new LunarException("message", LunarErrorCode.YEAR_OUT_OF_RANGE);
            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.YEAR_OUT_OF_RANGE);
        }

        @Test
        @DisplayName("默认错误码是UNKNOWN")
        void testDefaultErrorCode() {
            LunarException exception = new LunarException("test");
            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("OpenException集成测试")
    class OpenExceptionIntegrationTests {

        @Test
        @DisplayName("getErrorCode返回字符串错误码")
        void testStringErrorCode() {
            LunarException exception = new LunarException("msg", LunarErrorCode.CONVERSION_FAILED);
            assertThat(exception.getErrorCode()).isEqualTo("LUNAR_1001");
        }

        @Test
        @DisplayName("getComponent返回Lunar")
        void testGetComponent() {
            LunarException exception = new LunarException("msg");
            assertThat(exception.getComponent()).isEqualTo("Lunar");
        }

        @Test
        @DisplayName("getMessage包含组件和错误码")
        void testFormattedMessage() {
            LunarException exception = new LunarException("Year 1800", LunarErrorCode.YEAR_OUT_OF_RANGE);
            String msg = exception.getMessage();
            assertThat(msg).contains("[Lunar]");
            assertThat(msg).contains("(LUNAR_2001)");
            assertThat(msg).contains("Year 1800");
        }

        @Test
        @DisplayName("getRawMessage返回原始消息")
        void testRawMessage() {
            LunarException exception = new LunarException("Year 1800", LunarErrorCode.YEAR_OUT_OF_RANGE);
            assertThat(exception.getRawMessage()).isEqualTo("Year 1800");
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
