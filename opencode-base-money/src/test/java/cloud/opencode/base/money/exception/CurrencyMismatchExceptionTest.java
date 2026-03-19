package cloud.opencode.base.money.exception;

import cloud.opencode.base.money.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CurrencyMismatchException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("CurrencyMismatchException 测试")
class CurrencyMismatchExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("两参数构造函数")
        void testTwoArgConstructor() {
            CurrencyMismatchException ex = new CurrencyMismatchException(Currency.CNY, Currency.USD);

            assertThat(ex.getExpected()).isEqualTo(Currency.CNY);
            assertThat(ex.getActual()).isEqualTo(Currency.USD);
            assertThat(ex.getErrorCode()).isEqualTo(MoneyErrorCode.CURRENCY_MISMATCH);
            assertThat(ex.getMessage()).contains("CNY").contains("USD");
        }

        @Test
        @DisplayName("三参数构造函数")
        void testThreeArgConstructor() {
            CurrencyMismatchException ex = new CurrencyMismatchException(
                "Custom message", Currency.EUR, Currency.GBP);

            assertThat(ex.getMessage()).isEqualTo("Custom message");
            assertThat(ex.getExpected()).isEqualTo(Currency.EUR);
            assertThat(ex.getActual()).isEqualTo(Currency.GBP);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承MoneyException")
        void testExtendsMoneyException() {
            CurrencyMismatchException ex = new CurrencyMismatchException(Currency.CNY, Currency.USD);
            assertThat(ex).isInstanceOf(MoneyException.class);
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getExpected返回期望的货币")
        void testGetExpected() {
            CurrencyMismatchException ex = new CurrencyMismatchException(Currency.CNY, Currency.USD);
            assertThat(ex.getExpected()).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("getActual返回实际的货币")
        void testGetActual() {
            CurrencyMismatchException ex = new CurrencyMismatchException(Currency.CNY, Currency.USD);
            assertThat(ex.getActual()).isEqualTo(Currency.USD);
        }
    }
}
