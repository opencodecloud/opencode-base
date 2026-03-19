package cloud.opencode.base.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Currency 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("Currency 测试")
class CurrencyTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含12种货币")
        void testCurrencyCount() {
            assertThat(Currency.values()).hasSize(12);
        }

        @Test
        @DisplayName("包含主要货币")
        void testMainCurrencies() {
            assertThat(Currency.CNY).isNotNull();
            assertThat(Currency.USD).isNotNull();
            assertThat(Currency.EUR).isNotNull();
            assertThat(Currency.GBP).isNotNull();
            assertThat(Currency.JPY).isNotNull();
        }
    }

    @Nested
    @DisplayName("getSymbol方法测试")
    class GetSymbolTests {

        @Test
        @DisplayName("CNY符号是¥")
        void testCnySymbol() {
            assertThat(Currency.CNY.getSymbol()).isEqualTo("¥");
        }

        @Test
        @DisplayName("USD符号是$")
        void testUsdSymbol() {
            assertThat(Currency.USD.getSymbol()).isEqualTo("$");
        }

        @Test
        @DisplayName("EUR符号是€")
        void testEurSymbol() {
            assertThat(Currency.EUR.getSymbol()).isEqualTo("€");
        }

        @Test
        @DisplayName("GBP符号是£")
        void testGbpSymbol() {
            assertThat(Currency.GBP.getSymbol()).isEqualTo("£");
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("CNY名称是人民币")
        void testCnyName() {
            assertThat(Currency.CNY.getName()).isEqualTo("人民币");
            assertThat(Currency.CNY.getNameZh()).isEqualTo("人民币");
        }

        @Test
        @DisplayName("USD英文名称")
        void testUsdNameEn() {
            assertThat(Currency.USD.getNameEn()).isEqualTo("US Dollar");
        }
    }

    @Nested
    @DisplayName("getCode方法测试")
    class GetCodeTests {

        @ParameterizedTest
        @EnumSource(Currency.class)
        @DisplayName("所有货币都有代码")
        void testAllHaveCode(Currency currency) {
            assertThat(currency.getCode()).isNotBlank();
        }

        @Test
        @DisplayName("CNY代码")
        void testCnyCode() {
            assertThat(Currency.CNY.getCode()).isEqualTo("CNY");
        }
    }

    @Nested
    @DisplayName("getScale方法测试")
    class GetScaleTests {

        @Test
        @DisplayName("CNY小数位是2")
        void testCnyScale() {
            assertThat(Currency.CNY.getScale()).isEqualTo(2);
        }

        @Test
        @DisplayName("JPY小数位是0")
        void testJpyScale() {
            assertThat(Currency.JPY.getScale()).isEqualTo(0);
        }

        @Test
        @DisplayName("KRW小数位是0")
        void testKrwScale() {
            assertThat(Currency.KRW.getScale()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("of方法测试")
    class OfMethodTests {

        @Test
        @DisplayName("通过代码获取CNY")
        void testOfCny() {
            assertThat(Currency.of("CNY")).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("代码不区分大小写")
        void testCaseInsensitive() {
            assertThat(Currency.of("cny")).isEqualTo(Currency.CNY);
            assertThat(Currency.of("Cny")).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("无效代码抛出异常")
        void testInvalidCode() {
            assertThatThrownBy(() -> Currency.of("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown currency");
        }
    }

    @Nested
    @DisplayName("isSupported方法测试")
    class IsSupportedTests {

        @Test
        @DisplayName("CNY是支持的")
        void testCnySupported() {
            assertThat(Currency.isSupported("CNY")).isTrue();
        }

        @Test
        @DisplayName("不区分大小写")
        void testCaseInsensitive() {
            assertThat(Currency.isSupported("cny")).isTrue();
        }

        @Test
        @DisplayName("不支持的货币返回false")
        void testUnsupported() {
            assertThat(Currency.isSupported("INVALID")).isFalse();
        }
    }
}
