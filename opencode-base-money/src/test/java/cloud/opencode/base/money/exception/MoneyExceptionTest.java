package cloud.opencode.base.money.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * MoneyException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("MoneyException 测试")
class MoneyExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("消息构造函数")
        void testMessageConstructor() {
            MoneyException ex = new MoneyException("Test error");

            assertThat(ex.getRawMessage()).isEqualTo("Test error");
            assertThat(ex.getMoneyErrorCode()).isEqualTo(MoneyErrorCode.UNKNOWN);
            assertThat(ex.getComponent()).isEqualTo("money");
            assertThat(ex.getErrorCode()).isEqualTo("0");
        }

        @Test
        @DisplayName("消息和错误码构造函数")
        void testMessageCodeConstructor() {
            MoneyException ex = new MoneyException("Test error", MoneyErrorCode.INVALID_AMOUNT);

            assertThat(ex.getRawMessage()).isEqualTo("Test error");
            assertThat(ex.getMoneyErrorCode()).isEqualTo(MoneyErrorCode.INVALID_AMOUNT);
            assertThat(ex.getErrorCode()).isEqualTo("1001");
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageCauseConstructor() {
            Throwable cause = new RuntimeException("Cause");
            MoneyException ex = new MoneyException("Test error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getMoneyErrorCode()).isEqualTo(MoneyErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("完整构造函数")
        void testFullConstructor() {
            Throwable cause = new RuntimeException("Cause");
            MoneyException ex = new MoneyException("Test error", cause, MoneyErrorCode.CURRENCY_MISMATCH);

            assertThat(ex.getRawMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getMoneyErrorCode()).isEqualTo(MoneyErrorCode.CURRENCY_MISMATCH);
            assertThat(ex.getErrorCode()).isEqualTo("2001");
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            MoneyException ex = new MoneyException("Test");
            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("是RuntimeException子类")
        void testIsRuntimeException() {
            MoneyException ex = new MoneyException("Test");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("消息格式化测试")
    class MessageFormatTests {

        @Test
        @DisplayName("getMessage包含组件名和错误码")
        void testFormattedMessage() {
            MoneyException ex = new MoneyException("Amount invalid", MoneyErrorCode.INVALID_AMOUNT);

            assertThat(ex.getMessage()).contains("[money]");
            assertThat(ex.getMessage()).contains("(1001)");
            assertThat(ex.getMessage()).contains("Amount invalid");
        }

        @Test
        @DisplayName("getComponent返回money")
        void testComponent() {
            MoneyException ex = new MoneyException("Test");
            assertThat(ex.getComponent()).isEqualTo("money");
        }
    }
}
