package cloud.opencode.base.money.exception;

import cloud.opencode.base.money.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ExchangeRateException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("ExchangeRateException 测试")
class ExchangeRateExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("消息构造函数")
        void testMessageConstructor() {
            ExchangeRateException ex = new ExchangeRateException("Rate error");

            assertThat(ex.getMessage()).isEqualTo("Rate error");
            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.RATE_NOT_FOUND);
            assertThat(ex.getSource()).isNull();
            assertThat(ex.getTarget()).isNull();
        }

        @Test
        @DisplayName("消息和错误码构造函数")
        void testMessageCodeConstructor() {
            ExchangeRateException ex = new ExchangeRateException("Rate error", MoneyErrorCode.RATE_INVALID);

            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.RATE_INVALID);
        }

        @Test
        @DisplayName("消息和货币构造函数")
        void testMessageCurrencyConstructor() {
            ExchangeRateException ex = new ExchangeRateException("Rate error", Currency.CNY, Currency.USD);

            assertThat(ex.getSource()).isEqualTo(Currency.CNY);
            assertThat(ex.getTarget()).isEqualTo(Currency.USD);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("notFound")
        void testNotFound() {
            ExchangeRateException ex = ExchangeRateException.notFound(Currency.CNY, Currency.USD);

            assertThat(ex.getMessage()).contains("CNY").contains("USD");
            assertThat(ex.getSource()).isEqualTo(Currency.CNY);
            assertThat(ex.getTarget()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("invalidRate")
        void testInvalidRate() {
            ExchangeRateException ex = ExchangeRateException.invalidRate("-1.5");

            assertThat(ex.getMessage()).contains("-1.5");
            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.RATE_INVALID);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承MoneyException")
        void testExtendsMoneyException() {
            ExchangeRateException ex = new ExchangeRateException("Test");
            assertThat(ex).isInstanceOf(MoneyException.class);
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getSource")
        void testGetSource() {
            ExchangeRateException ex = ExchangeRateException.notFound(Currency.EUR, Currency.GBP);
            assertThat(ex.getSource()).isEqualTo(Currency.EUR);
        }

        @Test
        @DisplayName("getTarget")
        void testGetTarget() {
            ExchangeRateException ex = ExchangeRateException.notFound(Currency.EUR, Currency.GBP);
            assertThat(ex.getTarget()).isEqualTo(Currency.GBP);
        }
    }
}
