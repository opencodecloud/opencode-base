package cloud.opencode.base.money.exchange;

import cloud.opencode.base.money.Currency;
import cloud.opencode.base.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * FixedRateProvider 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("FixedRateProvider 测试")
class FixedRateProviderTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造函数")
        void testDefaultConstructor() {
            FixedRateProvider provider = new FixedRateProvider();
            assertThat(provider.getRateCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用builder创建")
        void testBuilder() {
            FixedRateProvider provider = FixedRateProvider.builder()
                .rate(Currency.CNY, Currency.USD, "0.14")
                .rate(Currency.CNY, Currency.EUR, "0.13")
                .build();

            assertThat(provider.getRateCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("rate方法添加BigDecimal汇率")
        void testRateBigDecimal() {
            FixedRateProvider provider = FixedRateProvider.builder()
                .rate(Currency.CNY, Currency.USD, new BigDecimal("0.14"))
                .build();

            assertThat(provider.hasRate(Currency.CNY, Currency.USD)).isTrue();
        }
    }

    @Nested
    @DisplayName("withCommonCnyRates测试")
    class WithCommonCnyRatesTests {

        @Test
        @DisplayName("包含常用人民币汇率")
        void testCommonRates() {
            FixedRateProvider provider = FixedRateProvider.withCommonCnyRates();

            assertThat(provider.hasRate(Currency.CNY, Currency.USD)).isTrue();
            assertThat(provider.hasRate(Currency.CNY, Currency.EUR)).isTrue();
            assertThat(provider.hasRate(Currency.CNY, Currency.GBP)).isTrue();
            assertThat(provider.hasRate(Currency.CNY, Currency.JPY)).isTrue();
            assertThat(provider.hasRate(Currency.CNY, Currency.HKD)).isTrue();
        }
    }

    @Nested
    @DisplayName("getRate方法测试")
    class GetRateTests {

        @Test
        @DisplayName("获取存在的汇率")
        void testGetExistingRate() {
            FixedRateProvider provider = FixedRateProvider.builder()
                .rate(Currency.CNY, Currency.USD, "0.14")
                .build();

            Optional<ExchangeRate> rate = provider.getRate(Currency.CNY, Currency.USD);
            assertThat(rate).isPresent();
        }

        @Test
        @DisplayName("相同货币返回identity汇率")
        void testGetIdentityRate() {
            FixedRateProvider provider = new FixedRateProvider();

            Optional<ExchangeRate> rate = provider.getRate(Currency.CNY, Currency.CNY);
            assertThat(rate).isPresent();
            assertThat(rate.get().rate()).isEqualTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("获取逆汇率")
        void testGetInverseRate() {
            FixedRateProvider provider = FixedRateProvider.builder()
                .rate(Currency.CNY, Currency.USD, "0.14")
                .build();

            Optional<ExchangeRate> rate = provider.getRate(Currency.USD, Currency.CNY);
            assertThat(rate).isPresent();
        }

        @Test
        @DisplayName("不存在的汇率返回空")
        void testGetNonExistingRate() {
            FixedRateProvider provider = new FixedRateProvider();

            Optional<ExchangeRate> rate = provider.getRate(Currency.EUR, Currency.GBP);
            assertThat(rate).isEmpty();
        }
    }

    @Nested
    @DisplayName("addRate方法测试")
    class AddRateTests {

        @Test
        @DisplayName("添加汇率")
        void testAddRate() {
            FixedRateProvider provider = new FixedRateProvider();
            provider.addRate(Currency.CNY, Currency.USD, new BigDecimal("0.14"));

            assertThat(provider.hasRate(Currency.CNY, Currency.USD)).isTrue();
        }

        @Test
        @DisplayName("添加字符串汇率")
        void testAddRateString() {
            FixedRateProvider provider = new FixedRateProvider();
            provider.addRate(Currency.CNY, Currency.USD, "0.14");

            assertThat(provider.hasRate(Currency.CNY, Currency.USD)).isTrue();
        }
    }

    @Nested
    @DisplayName("removeRate方法测试")
    class RemoveRateTests {

        @Test
        @DisplayName("移除汇率")
        void testRemoveRate() {
            FixedRateProvider provider = FixedRateProvider.builder()
                .rate(Currency.CNY, Currency.USD, "0.14")
                .build();

            provider.removeRate(Currency.CNY, Currency.USD);
            assertThat(provider.getRateCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("clearRates方法测试")
    class ClearRatesTests {

        @Test
        @DisplayName("清除所有汇率")
        void testClearRates() {
            FixedRateProvider provider = FixedRateProvider.withCommonCnyRates();
            provider.clearRates();

            assertThat(provider.getRateCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getAllRates方法测试")
    class GetAllRatesTests {

        @Test
        @DisplayName("获取所有汇率")
        void testGetAllRates() {
            FixedRateProvider provider = FixedRateProvider.builder()
                .rate(Currency.CNY, Currency.USD, "0.14")
                .rate(Currency.CNY, Currency.EUR, "0.13")
                .build();

            assertThat(provider.getAllRates()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConvertTests {

        @Test
        @DisplayName("使用provider转换")
        void testConvert() {
            FixedRateProvider provider = FixedRateProvider.builder()
                .rate(Currency.CNY, Currency.USD, "0.14")
                .build();

            Money cny = Money.of("100", Currency.CNY);
            Money usd = provider.convert(cny, Currency.USD);

            assertThat(usd.currency()).isEqualTo(Currency.USD);
        }
    }
}
