package cloud.opencode.base.money;

import cloud.opencode.base.money.exchange.ExchangeRate;
import cloud.opencode.base.money.exchange.FixedRateProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenMoney 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("OpenMoney 测试")
class OpenMoneyTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of(String)")
        void testOfString() {
            Money money = OpenMoney.of("100.50");
            assertThat(money.amount()).isEqualTo(new BigDecimal("100.50"));
            assertThat(money.currency()).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("of(String, Currency)")
        void testOfStringCurrency() {
            Money money = OpenMoney.of("100", Currency.USD);
            assertThat(money.currency()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("ofCents")
        void testOfCents() {
            Money money = OpenMoney.ofCents(10050);
            assertThat(money.amount()).isEqualTo(new BigDecimal("100.50"));
        }

        @Test
        @DisplayName("zero()")
        void testZero() {
            Money money = OpenMoney.zero();
            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("zero(Currency)")
        void testZeroCurrency() {
            Money money = OpenMoney.zero(Currency.USD);
            assertThat(money.isZero()).isTrue();
            assertThat(money.currency()).isEqualTo(Currency.USD);
        }
    }

    @Nested
    @DisplayName("中文大写测试")
    class ChineseUpperCaseTests {

        @Test
        @DisplayName("toChineseUpperCase(BigDecimal)")
        void testToChineseUpperCaseBigDecimal() {
            String result = OpenMoney.toChineseUpperCase(new BigDecimal("1234.56"));
            assertThat(result).contains("壹仟");
            assertThat(result).contains("元");
        }

        @Test
        @DisplayName("toChineseUpperCase(Money)")
        void testToChineseUpperCaseMoney() {
            Money money = Money.of("100.50");
            String result = OpenMoney.toChineseUpperCase(money);
            assertThat(result).contains("壹佰");
        }
    }

    @Nested
    @DisplayName("分摊测试")
    class AllocationTests {

        @Test
        @DisplayName("allocate")
        void testAllocate() {
            Money total = Money.of("100");
            List<Money> parts = OpenMoney.allocate(total, 1, 2, 3);

            assertThat(parts).hasSize(3);
        }

        @Test
        @DisplayName("split")
        void testSplit() {
            Money total = Money.of("100");
            List<Money> parts = OpenMoney.split(total, 3);

            assertThat(parts).hasSize(3);
        }

        @Test
        @DisplayName("allocateByPercent")
        void testAllocateByPercent() {
            Money total = Money.of("100");
            List<Money> parts = OpenMoney.allocateByPercent(total, 20, 30, 50);

            assertThat(parts).hasSize(3);
        }
    }

    @Nested
    @DisplayName("聚合运算测试")
    class AggregationTests {

        @Test
        @DisplayName("sum")
        void testSum() {
            List<Money> moneys = List.of(Money.of("100"), Money.of("200"), Money.of("300"));
            Money sum = OpenMoney.sum(moneys);

            assertThat(sum.amount()).isEqualTo(new BigDecimal("600"));
        }

        @Test
        @DisplayName("average")
        void testAverage() {
            List<Money> moneys = List.of(Money.of("100"), Money.of("200"), Money.of("300"));
            Money avg = OpenMoney.average(moneys);

            assertThat(avg.amount()).isEqualTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("max")
        void testMax() {
            List<Money> moneys = List.of(Money.of("100"), Money.of("300"), Money.of("200"));
            Money max = OpenMoney.max(moneys);

            assertThat(max.amount()).isEqualTo(new BigDecimal("300"));
        }

        @Test
        @DisplayName("min")
        void testMin() {
            List<Money> moneys = List.of(Money.of("100"), Money.of("300"), Money.of("200"));
            Money min = OpenMoney.min(moneys);

            assertThat(min.amount()).isEqualTo(new BigDecimal("100"));
        }
    }

    @Nested
    @DisplayName("折扣与税测试")
    class DiscountTaxTests {

        @Test
        @DisplayName("applyDiscount")
        void testApplyDiscount() {
            Money money = Money.of("100");
            Money discounted = OpenMoney.applyDiscount(money, new BigDecimal("0.1"));

            assertThat(discounted.amount()).isEqualTo(new BigDecimal("90.00"));
        }

        @Test
        @DisplayName("calculateTax")
        void testCalculateTax() {
            Money money = Money.of("100");
            Money tax = OpenMoney.calculateTax(money, new BigDecimal("0.1"));

            assertThat(tax.amount()).isEqualTo(new BigDecimal("10.00"));
        }

        @Test
        @DisplayName("addTax")
        void testAddTax() {
            Money money = Money.of("100");
            Money withTax = OpenMoney.addTax(money, new BigDecimal("0.1"));

            assertThat(withTax.amount()).isEqualTo(new BigDecimal("110.00"));
        }
    }

    @Nested
    @DisplayName("汇率转换测试")
    class ExchangeRateTests {

        @Test
        @DisplayName("convert")
        void testConvert() {
            Money cny = Money.of("100", Currency.CNY);
            Money usd = OpenMoney.convert(cny, Currency.USD, new BigDecimal("0.14"));

            assertThat(usd.currency()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("convert with provider")
        void testConvertWithProvider() {
            Money cny = Money.of("100", Currency.CNY);
            FixedRateProvider provider = FixedRateProvider.builder()
                .rate(Currency.CNY, Currency.USD, "0.14")
                .build();

            Money usd = OpenMoney.convert(cny, Currency.USD, provider);
            assertThat(usd.currency()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("rate")
        void testRate() {
            ExchangeRate rate = OpenMoney.rate(Currency.CNY, Currency.USD, new BigDecimal("0.14"));

            assertThat(rate.source()).isEqualTo(Currency.CNY);
            assertThat(rate.target()).isEqualTo(Currency.USD);
        }
    }

    @Nested
    @DisplayName("格式化测试")
    class FormatTests {

        @Test
        @DisplayName("format")
        void testFormat() {
            Money money = Money.of("1234.50");
            String formatted = OpenMoney.format(money);

            assertThat(formatted).startsWith("¥");
        }

        @Test
        @DisplayName("formatWithCode")
        void testFormatWithCode() {
            Money money = Money.of("100");
            String formatted = OpenMoney.formatWithCode(money);

            assertThat(formatted).startsWith("CNY");
        }

        @Test
        @DisplayName("formatAccounting")
        void testFormatAccounting() {
            Money negative = Money.of("-100");
            String formatted = OpenMoney.formatAccounting(negative);

            assertThat(formatted).contains("(");
        }

        @Test
        @DisplayName("formatCompact")
        void testFormatCompact() {
            Money money = Money.of("12345678");
            String formatted = OpenMoney.formatCompact(money);

            assertThat(formatted).contains("万");
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("validateAndParse")
        void testValidateAndParse() {
            BigDecimal amount = OpenMoney.validateAndParse("100.50");
            assertThat(amount).isEqualTo(new BigDecimal("100.50"));
        }

        @Test
        @DisplayName("isValid")
        void testIsValid() {
            assertThat(OpenMoney.isValid("100.50")).isTrue();
            assertThat(OpenMoney.isValid("invalid")).isFalse();
        }

        @Test
        @DisplayName("validatePositive")
        void testValidatePositive() {
            assertThatCode(() -> OpenMoney.validatePositive(Money.of("100")))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validateNonNegative")
        void testValidateNonNegative() {
            assertThatCode(() -> OpenMoney.validateNonNegative(Money.of("0")))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("比较测试")
    class ComparisonTests {

        @Test
        @DisplayName("areEqual相等")
        void testAreEqualTrue() {
            Money m1 = Money.of("100.00");
            Money m2 = Money.of("100");

            assertThat(OpenMoney.areEqual(m1, m2)).isTrue();
        }

        @Test
        @DisplayName("areEqual不相等")
        void testAreEqualFalse() {
            Money m1 = Money.of("100");
            Money m2 = Money.of("200");

            assertThat(OpenMoney.areEqual(m1, m2)).isFalse();
        }
    }
}
