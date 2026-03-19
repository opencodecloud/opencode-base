package cloud.opencode.base.money.exception;

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

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("消息和错误码构造函数")
        void testMessageCodeConstructor() {
            MoneyException ex = new MoneyException("Test error", MoneyErrorCode.INVALID_AMOUNT);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.INVALID_AMOUNT);
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageCauseConstructor() {
            Throwable cause = new RuntimeException("Cause");
            MoneyException ex = new MoneyException("Test error", cause);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("完整构造函数")
        void testFullConstructor() {
            Throwable cause = new RuntimeException("Cause");
            MoneyException ex = new MoneyException("Test error", cause, MoneyErrorCode.CURRENCY_MISMATCH);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.CURRENCY_MISMATCH);
        }
    }

    @Nested
    @DisplayName("RuntimeException继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是RuntimeException子类")
        void testIsRuntimeException() {
            MoneyException ex = new MoneyException("Test");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
