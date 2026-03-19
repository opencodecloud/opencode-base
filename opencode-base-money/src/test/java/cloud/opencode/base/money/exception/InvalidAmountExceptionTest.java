package cloud.opencode.base.money.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * InvalidAmountException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("InvalidAmountException 测试")
class InvalidAmountExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("消息构造函数")
        void testMessageConstructor() {
            InvalidAmountException ex = new InvalidAmountException("Invalid amount");

            assertThat(ex.getMessage()).isEqualTo("Invalid amount");
            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.INVALID_AMOUNT);
            assertThat(ex.getInvalidValue()).isNull();
        }

        @Test
        @DisplayName("消息和无效值构造函数")
        void testMessageValueConstructor() {
            InvalidAmountException ex = new InvalidAmountException("Invalid amount", "abc");

            assertThat(ex.getMessage()).isEqualTo("Invalid amount");
            assertThat(ex.getInvalidValue()).isEqualTo("abc");
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageCauseConstructor() {
            Throwable cause = new NumberFormatException();
            InvalidAmountException ex = new InvalidAmountException("Invalid amount", cause);

            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getInvalidValue()).isNull();
        }

        @Test
        @DisplayName("消息和错误码构造函数")
        void testMessageCodeConstructor() {
            InvalidAmountException ex = new InvalidAmountException(
                "Invalid amount", MoneyErrorCode.AMOUNT_OVERFLOW);

            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.AMOUNT_OVERFLOW);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("formatError")
        void testFormatError() {
            InvalidAmountException ex = InvalidAmountException.formatError("abc");

            assertThat(ex.getMessage()).contains("abc");
            assertThat(ex.getInvalidValue()).isEqualTo("abc");
        }

        @Test
        @DisplayName("precisionError")
        void testPrecisionError() {
            InvalidAmountException ex = InvalidAmountException.precisionError("100.123", 2);

            assertThat(ex.getMessage()).contains("100.123").contains("2");
            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.AMOUNT_PRECISION_ERROR);
        }

        @Test
        @DisplayName("overflow")
        void testOverflow() {
            InvalidAmountException ex = InvalidAmountException.overflow("999999999999");

            assertThat(ex.getMessage()).contains("overflow");
            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.AMOUNT_OVERFLOW);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承MoneyException")
        void testExtendsMoneyException() {
            InvalidAmountException ex = new InvalidAmountException("Test");
            assertThat(ex).isInstanceOf(MoneyException.class);
        }
    }
}
