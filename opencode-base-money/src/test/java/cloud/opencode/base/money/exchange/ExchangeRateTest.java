package cloud.opencode.base.money.exchange;

import cloud.opencode.base.money.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * ExchangeRate 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("ExchangeRate 测试")
class ExchangeRateTest {

    @Nested
    @DisplayName("Record基本测试")
    class RecordBasicTests {

        @Test
        @DisplayName("创建汇率")
        void testCreate() {
            ExchangeRate rate = new ExchangeRate(Currency.CNY, Currency.USD,
                new BigDecimal("0.14"), LocalDateTime.now());

            assertThat(rate.source()).isEqualTo(Currency.CNY);
            assertThat(rate.target()).isEqualTo(Currency.USD);
            assertThat(rate.rate()).isEqualTo(new BigDecimal("0.14"));
        }

        @Test
        @DisplayName("null源货币抛出异常")
        void testNullSource() {
            assertThatThrownBy(() -> new ExchangeRate(null, Currency.USD,
                BigDecimal.ONE, LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null目标货币抛出异常")
        void testNullTarget() {
            assertThatThrownBy(() -> new ExchangeRate(Currency.CNY, null,
                BigDecimal.ONE, LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null汇率抛出异常")
        void testNullRate() {
            assertThatThrownBy(() -> new ExchangeRate(Currency.CNY, Currency.USD,
                null, LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("非正汇率抛出异常")
        void testNonPositiveRate() {
            assertThatThrownBy(() -> new ExchangeRate(Currency.CNY, Currency.USD,
                BigDecimal.ZERO, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> new ExchangeRate(Currency.CNY, Currency.USD,
                new BigDecimal("-1"), LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null时间戳自动设置")
        void testNullTimestamp() {
            ExchangeRate rate = new ExchangeRate(Currency.CNY, Currency.USD,
                BigDecimal.ONE, null);

            assertThat(rate.timestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of(Currency, Currency, BigDecimal)")
        void testOfBigDecimal() {
            ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD, new BigDecimal("0.14"));

            assertThat(rate.source()).isEqualTo(Currency.CNY);
            assertThat(rate.target()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("of(Currency, Currency, String)")
        void testOfString() {
            ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD, "0.14");

            assertThat(rate.rate()).isEqualTo(new BigDecimal("0.14"));
        }

        @Test
        @DisplayName("of(Currency, Currency, BigDecimal, LocalDateTime)")
        void testOfWithTimestamp() {
            LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 0, 0);
            ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD,
                new BigDecimal("0.14"), timestamp);

            assertThat(rate.timestamp()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("identity")
        void testIdentity() {
            ExchangeRate rate = ExchangeRate.identity(Currency.CNY);

            assertThat(rate.source()).isEqualTo(Currency.CNY);
            assertThat(rate.target()).isEqualTo(Currency.CNY);
            assertThat(rate.rate()).isEqualTo(BigDecimal.ONE);
        }
    }

    @Nested
    @DisplayName("convert方法测试")
    class ConvertTests {

        @Test
        @DisplayName("转换金额")
        void testConvert() {
            ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD, "0.14");
            BigDecimal result = rate.convert(new BigDecimal("100"));

            assertThat(result).isEqualTo(new BigDecimal("14.00"));
        }
    }

    @Nested
    @DisplayName("inverse方法测试")
    class InverseTests {

        @Test
        @DisplayName("获取逆汇率")
        void testInverse() {
            ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD, "0.14");
            ExchangeRate inverse = rate.inverse();

            assertThat(inverse.source()).isEqualTo(Currency.USD);
            assertThat(inverse.target()).isEqualTo(Currency.CNY);
        }
    }

    @Nested
    @DisplayName("isExpired方法测试")
    class IsExpiredTests {

        @Test
        @DisplayName("未过期")
        void testNotExpired() {
            ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD, "0.14");
            assertThat(rate.isExpired(24)).isFalse();
        }

        @Test
        @DisplayName("已过期")
        void testExpired() {
            ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD,
                new BigDecimal("0.14"), LocalDateTime.now().minusHours(25));
            assertThat(rate.isExpired(24)).isTrue();
        }
    }

    @Nested
    @DisplayName("canConvert方法测试")
    class CanConvertTests {

        @Test
        @DisplayName("可以转换")
        void testCanConvert() {
            ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD, "0.14");
            assertThat(rate.canConvert(Currency.CNY, Currency.USD)).isTrue();
        }

        @Test
        @DisplayName("不能转换")
        void testCannotConvert() {
            ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD, "0.14");
            assertThat(rate.canConvert(Currency.USD, Currency.CNY)).isFalse();
        }
    }

    @Nested
    @DisplayName("format方法测试")
    class FormatTests {

        @Test
        @DisplayName("格式化汇率")
        void testFormat() {
            ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD, "0.14");
            String formatted = rate.format();

            assertThat(formatted).contains("CNY").contains("USD");
        }

        @Test
        @DisplayName("toString")
        void testToString() {
            ExchangeRate rate = ExchangeRate.of(Currency.CNY, Currency.USD, "0.14");
            assertThat(rate.toString()).isEqualTo(rate.format());
        }
    }
}
