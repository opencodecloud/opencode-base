package cloud.opencode.base.money;

import cloud.opencode.base.money.exception.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Money 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("Money 测试")
class MoneyTest {

    @Nested
    @DisplayName("Record基本测试")
    class RecordBasicTests {

        @Test
        @DisplayName("创建Money")
        void testCreate() {
            Money money = new Money(new BigDecimal("100.50"), Currency.CNY);

            assertThat(money.amount()).isEqualTo(new BigDecimal("100.50"));
            assertThat(money.currency()).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("null金额抛出异常")
        void testNullAmount() {
            assertThatThrownBy(() -> new Money(null, Currency.CNY))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null货币抛出异常")
        void testNullCurrency() {
            assertThatThrownBy(() -> new Money(BigDecimal.ONE, null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of(BigDecimal, Currency)")
        void testOfBigDecimalCurrency() {
            Money money = Money.of(new BigDecimal("100"), Currency.USD);
            assertThat(money.amount()).isEqualTo(new BigDecimal("100"));
            assertThat(money.currency()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("of(BigDecimal)默认CNY")
        void testOfBigDecimalDefaultCny() {
            Money money = Money.of(new BigDecimal("100"));
            assertThat(money.currency()).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("of(String, Currency)")
        void testOfStringCurrency() {
            Money money = Money.of("100.50", Currency.EUR);
            assertThat(money.amount()).isEqualTo(new BigDecimal("100.50"));
        }

        @Test
        @DisplayName("of(String)默认CNY")
        void testOfStringDefaultCny() {
            Money money = Money.of("100");
            assertThat(money.currency()).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("of(long, Currency)")
        void testOfLongCurrency() {
            Money money = Money.of(100L, Currency.GBP);
            assertThat(money.amount()).isEqualTo(BigDecimal.valueOf(100));
        }

        @Test
        @DisplayName("of(long)默认CNY")
        void testOfLongDefaultCny() {
            Money money = Money.of(100L);
            assertThat(money.currency()).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("ofYuan")
        void testOfYuan() {
            Money money = Money.ofYuan(100);
            assertThat(money.amount()).isEqualTo(BigDecimal.valueOf(100));
            assertThat(money.currency()).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("ofCents")
        void testOfCents() {
            Money money = Money.ofCents(10050);
            assertThat(money.amount()).isEqualTo(new BigDecimal("100.50"));
        }

        @Test
        @DisplayName("zero(Currency)")
        void testZeroCurrency() {
            Money money = Money.zero(Currency.USD);
            assertThat(money.amount()).isEqualTo(BigDecimal.ZERO);
            assertThat(money.currency()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("zero()默认CNY")
        void testZeroDefaultCny() {
            Money money = Money.zero();
            assertThat(money.currency()).isEqualTo(Currency.CNY);
        }
    }

    @Nested
    @DisplayName("算术运算测试")
    class ArithmeticTests {

        @Test
        @DisplayName("add")
        void testAdd() {
            Money m1 = Money.of("100");
            Money m2 = Money.of("50");
            Money result = m1.add(m2);

            assertThat(result.amount()).isEqualTo(new BigDecimal("150"));
        }

        @Test
        @DisplayName("add不同货币抛出异常")
        void testAddDifferentCurrency() {
            Money m1 = Money.of("100", Currency.CNY);
            Money m2 = Money.of("50", Currency.USD);

            assertThatThrownBy(() -> m1.add(m2))
                .isInstanceOf(CurrencyMismatchException.class);
        }

        @Test
        @DisplayName("subtract")
        void testSubtract() {
            Money m1 = Money.of("100");
            Money m2 = Money.of("30");
            Money result = m1.subtract(m2);

            assertThat(result.amount()).isEqualTo(new BigDecimal("70"));
        }

        @Test
        @DisplayName("multiply(BigDecimal)")
        void testMultiplyBigDecimal() {
            Money money = Money.of("100");
            Money result = money.multiply(new BigDecimal("1.5"));

            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("150.00"));
        }

        @Test
        @DisplayName("multiply(long)")
        void testMultiplyLong() {
            Money money = Money.of("100");
            Money result = money.multiply(3L);

            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("multiply(double)")
        void testMultiplyDouble() {
            Money money = Money.of("100");
            Money result = money.multiply(0.5);

            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        @DisplayName("divide(BigDecimal)")
        void testDivideBigDecimal() {
            Money money = Money.of("100");
            Money result = money.divide(new BigDecimal("4"));

            assertThat(result.amount()).isEqualTo(new BigDecimal("25.00"));
        }

        @Test
        @DisplayName("divide(long)")
        void testDivideLong() {
            Money money = Money.of("100");
            Money result = money.divide(4L);

            assertThat(result.amount()).isEqualTo(new BigDecimal("25.00"));
        }

        @Test
        @DisplayName("divide(double)")
        void testDivideDouble() {
            Money money = Money.of("100");
            Money result = money.divide(4.0);

            assertThat(result.amount()).isEqualTo(new BigDecimal("25.00"));
        }

        @Test
        @DisplayName("negate")
        void testNegate() {
            Money money = Money.of("100");
            Money result = money.negate();

            assertThat(result.amount()).isEqualTo(new BigDecimal("-100"));
        }

        @Test
        @DisplayName("abs")
        void testAbs() {
            Money money = Money.of("-100");
            Money result = money.abs();

            assertThat(result.amount()).isEqualTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("round")
        void testRound() {
            Money money = Money.of("100.556");
            Money result = money.round();

            assertThat(result.amount()).isEqualTo(new BigDecimal("100.56"));
        }
    }

    @Nested
    @DisplayName("比较方法测试")
    class ComparisonTests {

        @Test
        @DisplayName("compareTo")
        void testCompareTo() {
            Money m1 = Money.of("100");
            Money m2 = Money.of("50");

            assertThat(m1.compareTo(m2)).isGreaterThan(0);
        }

        @Test
        @DisplayName("isGreaterThan")
        void testIsGreaterThan() {
            Money m1 = Money.of("100");
            Money m2 = Money.of("50");

            assertThat(m1.isGreaterThan(m2)).isTrue();
            assertThat(m2.isGreaterThan(m1)).isFalse();
        }

        @Test
        @DisplayName("isLessThan")
        void testIsLessThan() {
            Money m1 = Money.of("50");
            Money m2 = Money.of("100");

            assertThat(m1.isLessThan(m2)).isTrue();
        }

        @Test
        @DisplayName("isGreaterOrEqual")
        void testIsGreaterOrEqual() {
            Money m1 = Money.of("100");
            Money m2 = Money.of("100");

            assertThat(m1.isGreaterOrEqual(m2)).isTrue();
        }

        @Test
        @DisplayName("isLessOrEqual")
        void testIsLessOrEqual() {
            Money m1 = Money.of("100");
            Money m2 = Money.of("100");

            assertThat(m1.isLessOrEqual(m2)).isTrue();
        }

        @Test
        @DisplayName("isPositive")
        void testIsPositive() {
            assertThat(Money.of("100").isPositive()).isTrue();
            assertThat(Money.of("-100").isPositive()).isFalse();
            assertThat(Money.of("0").isPositive()).isFalse();
        }

        @Test
        @DisplayName("isNegative")
        void testIsNegative() {
            assertThat(Money.of("-100").isNegative()).isTrue();
            assertThat(Money.of("100").isNegative()).isFalse();
        }

        @Test
        @DisplayName("isZero")
        void testIsZero() {
            assertThat(Money.of("0").isZero()).isTrue();
            assertThat(Money.zero().isZero()).isTrue();
            assertThat(Money.of("100").isZero()).isFalse();
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("toCents")
        void testToCents() {
            Money money = Money.of("100.50");
            assertThat(money.toCents()).isEqualTo(10050);
        }

        @Test
        @DisplayName("convertTo")
        void testConvertTo() {
            Money cny = Money.of("100", Currency.CNY);
            Money usd = cny.convertTo(Currency.USD, new BigDecimal("0.14"));

            assertThat(usd.currency()).isEqualTo(Currency.USD);
            assertThat(usd.amount()).isEqualTo(new BigDecimal("14.00"));
        }
    }

    @Nested
    @DisplayName("格式化方法测试")
    class FormatTests {

        @Test
        @DisplayName("format")
        void testFormat() {
            Money money = Money.of("1234.50");
            String formatted = money.format();

            assertThat(formatted).startsWith("¥");
            assertThat(formatted.contains("1,234.50") || formatted.contains("1234.50")).isTrue();
        }

        @Test
        @DisplayName("formatNumber")
        void testFormatNumber() {
            Money money = Money.of("1234.50");
            String formatted = money.formatNumber();

            assertThat(formatted.contains("1234.50") || formatted.contains("1,234.50")).isTrue();
        }

        @Test
        @DisplayName("toChineseUpperCase")
        void testToChineseUpperCase() {
            Money money = Money.of("1234.56");
            String chinese = money.toChineseUpperCase();

            assertThat(chinese).contains("壹仟");
            assertThat(chinese).contains("元");
        }

        @Test
        @DisplayName("toString")
        void testToString() {
            Money money = Money.of("100");
            assertThat(money.toString()).contains("¥");
        }
    }
}
