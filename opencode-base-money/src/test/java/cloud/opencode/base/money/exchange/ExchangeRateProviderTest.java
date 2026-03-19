package cloud.opencode.base.money.exchange;

import cloud.opencode.base.money.Currency;
import cloud.opencode.base.money.Money;
import cloud.opencode.base.money.exception.ExchangeRateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ExchangeRateProvider 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("ExchangeRateProvider 测试")
class ExchangeRateProviderTest {

    // 测试用的简单实现
    private static class TestProvider implements ExchangeRateProvider {
        @Override
        public Optional<ExchangeRate> getRate(Currency source, Currency target) {
            if (source == Currency.CNY && target == Currency.USD) {
                return Optional.of(ExchangeRate.of(source, target, "0.14"));
            }
            return Optional.empty();
        }
    }

    @Nested
    @DisplayName("getRate方法测试")
    class GetRateTests {

        @Test
        @DisplayName("获取存在的汇率")
        void testGetExistingRate() {
            ExchangeRateProvider provider = new TestProvider();
            Optional<ExchangeRate> rate = provider.getRate(Currency.CNY, Currency.USD);

            assertThat(rate).isPresent();
            assertThat(rate.get().rate()).isEqualTo(new BigDecimal("0.14"));
        }

        @Test
        @DisplayName("获取不存在的汇率")
        void testGetNonExistingRate() {
            ExchangeRateProvider provider = new TestProvider();
            Optional<ExchangeRate> rate = provider.getRate(Currency.EUR, Currency.GBP);

            assertThat(rate).isEmpty();
        }
    }

    @Nested
    @DisplayName("getRateOrThrow方法测试")
    class GetRateOrThrowTests {

        @Test
        @DisplayName("获取存在的汇率")
        void testGetExistingRate() {
            ExchangeRateProvider provider = new TestProvider();
            ExchangeRate rate = provider.getRateOrThrow(Currency.CNY, Currency.USD);

            assertThat(rate).isNotNull();
        }

        @Test
        @DisplayName("获取不存在的汇率抛出异常")
        void testThrowForNonExisting() {
            ExchangeRateProvider provider = new TestProvider();

            assertThatThrownBy(() -> provider.getRateOrThrow(Currency.EUR, Currency.GBP))
                .isInstanceOf(ExchangeRateException.class);
        }
    }

    @Nested
    @DisplayName("convert方法测试")
    class ConvertTests {

        @Test
        @DisplayName("转换金额")
        void testConvert() {
            ExchangeRateProvider provider = new TestProvider();
            Money cny = Money.of("100", Currency.CNY);
            Money usd = provider.convert(cny, Currency.USD);

            assertThat(usd.currency()).isEqualTo(Currency.USD);
            assertThat(usd.amount()).isEqualTo(new BigDecimal("14.00"));
        }

        @Test
        @DisplayName("转换相同货币返回原值")
        void testConvertSameCurrency() {
            ExchangeRateProvider provider = new TestProvider();
            Money cny = Money.of("100", Currency.CNY);
            Money result = provider.convert(cny, Currency.CNY);

            assertThat(result).isEqualTo(cny);
        }
    }

    @Nested
    @DisplayName("hasRate方法测试")
    class HasRateTests {

        @Test
        @DisplayName("存在汇率")
        void testHasRate() {
            ExchangeRateProvider provider = new TestProvider();
            assertThat(provider.hasRate(Currency.CNY, Currency.USD)).isTrue();
        }

        @Test
        @DisplayName("不存在汇率")
        void testNotHasRate() {
            ExchangeRateProvider provider = new TestProvider();
            assertThat(provider.hasRate(Currency.EUR, Currency.GBP)).isFalse();
        }
    }

    @Nested
    @DisplayName("getRateValue方法测试")
    class GetRateValueTests {

        @Test
        @DisplayName("获取汇率值")
        void testGetRateValue() {
            ExchangeRateProvider provider = new TestProvider();
            Optional<BigDecimal> rate = provider.getRateValue(Currency.CNY, Currency.USD);

            assertThat(rate).isPresent();
            assertThat(rate.get()).isEqualTo(new BigDecimal("0.14"));
        }

        @Test
        @DisplayName("不存在时返回空")
        void testGetNonExistingRateValue() {
            ExchangeRateProvider provider = new TestProvider();
            Optional<BigDecimal> rate = provider.getRateValue(Currency.EUR, Currency.GBP);

            assertThat(rate).isEmpty();
        }
    }
}
