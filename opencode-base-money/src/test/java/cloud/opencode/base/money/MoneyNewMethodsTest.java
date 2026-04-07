package cloud.opencode.base.money;

import cloud.opencode.base.money.exception.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for new methods added to Money in V1.0.3:
 * min/max, clamp, percent, addPercent, subtractPercent,
 * ofMinorUnits, toMinorUnits, isNonNegative, isNonPositive.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.3
 */
@DisplayName("Money V1.0.3 New Methods")
class MoneyNewMethodsTest {

    // ==================== Static min / max ====================

    @Nested
    @DisplayName("Money.min(a, b)")
    class MinTest {

        @Test
        @DisplayName("returns the smaller of two amounts")
        void returnsSmaller() {
            Money a = Money.of("50");
            Money b = Money.of("100");
            assertThat(Money.min(a, b)).isSameAs(a);
            assertThat(Money.min(b, a)).isSameAs(a);
        }

        @Test
        @DisplayName("returns either when amounts are equal")
        void equalAmounts() {
            Money a = Money.of("100");
            Money b = Money.of("100");
            Money result = Money.min(a, b);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("throws NPE when first argument is null")
        void nullFirst() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Money.min(null, Money.of("1")));
        }

        @Test
        @DisplayName("throws NPE when second argument is null")
        void nullSecond() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Money.min(Money.of("1"), null));
        }

        @Test
        @DisplayName("throws CurrencyMismatchException for different currencies")
        void currencyMismatch() {
            Money cny = Money.of("100", Currency.CNY);
            Money usd = Money.of("100", Currency.USD);
            assertThatThrownBy(() -> Money.min(cny, usd))
                    .isInstanceOf(CurrencyMismatchException.class);
        }
    }

    @Nested
    @DisplayName("Money.max(a, b)")
    class MaxTest {

        @Test
        @DisplayName("returns the larger of two amounts")
        void returnsLarger() {
            Money a = Money.of("50");
            Money b = Money.of("100");
            assertThat(Money.max(a, b)).isSameAs(b);
            assertThat(Money.max(b, a)).isSameAs(b);
        }

        @Test
        @DisplayName("returns either when amounts are equal")
        void equalAmounts() {
            Money a = Money.of("100");
            Money b = Money.of("100");
            Money result = Money.max(a, b);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("throws NPE when first argument is null")
        void nullFirst() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Money.max(null, Money.of("1")));
        }

        @Test
        @DisplayName("throws NPE when second argument is null")
        void nullSecond() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Money.max(Money.of("1"), null));
        }

        @Test
        @DisplayName("throws CurrencyMismatchException for different currencies")
        void currencyMismatch() {
            Money cny = Money.of("100", Currency.CNY);
            Money usd = Money.of("100", Currency.USD);
            assertThatThrownBy(() -> Money.max(cny, usd))
                    .isInstanceOf(CurrencyMismatchException.class);
        }
    }

    // ==================== clamp ====================

    @Nested
    @DisplayName("clamp(min, max)")
    class ClampTest {

        @Test
        @DisplayName("below min returns min")
        void belowMin() {
            Money value = Money.of("5");
            Money min = Money.of("10");
            Money max = Money.of("100");
            assertThat(value.clamp(min, max)).isSameAs(min);
        }

        @Test
        @DisplayName("above max returns max")
        void aboveMax() {
            Money value = Money.of("150");
            Money min = Money.of("10");
            Money max = Money.of("100");
            assertThat(value.clamp(min, max)).isSameAs(max);
        }

        @Test
        @DisplayName("within range returns this")
        void withinRange() {
            Money value = Money.of("50");
            Money min = Money.of("10");
            Money max = Money.of("100");
            assertThat(value.clamp(min, max)).isSameAs(value);
        }

        @Test
        @DisplayName("on lower boundary returns this")
        void onLowerBoundary() {
            Money value = Money.of("10");
            Money min = Money.of("10");
            Money max = Money.of("100");
            assertThat(value.clamp(min, max)).isSameAs(value);
        }

        @Test
        @DisplayName("on upper boundary returns this")
        void onUpperBoundary() {
            Money value = Money.of("100");
            Money min = Money.of("10");
            Money max = Money.of("100");
            assertThat(value.clamp(min, max)).isSameAs(value);
        }

        @Test
        @DisplayName("min > max throws IllegalArgumentException")
        void minGreaterThanMax() {
            Money value = Money.of("50");
            Money min = Money.of("100");
            Money max = Money.of("10");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> value.clamp(min, max));
        }

        @Test
        @DisplayName("currency mismatch throws CurrencyMismatchException")
        void currencyMismatch() {
            Money value = Money.of("50", Currency.CNY);
            Money min = Money.of("10", Currency.USD);
            Money max = Money.of("100", Currency.USD);
            assertThatThrownBy(() -> value.clamp(min, max))
                    .isInstanceOf(CurrencyMismatchException.class);
        }

        @Test
        @DisplayName("null min throws NPE")
        void nullMin() {
            Money value = Money.of("50");
            assertThatNullPointerException()
                    .isThrownBy(() -> value.clamp(null, Money.of("100")));
        }

        @Test
        @DisplayName("null max throws NPE")
        void nullMax() {
            Money value = Money.of("50");
            assertThatNullPointerException()
                    .isThrownBy(() -> value.clamp(Money.of("10"), null));
        }
    }

    // ==================== percent ====================

    @Nested
    @DisplayName("percent(rate)")
    class PercentTest {

        @Test
        @DisplayName("13% of 100 = 13.00")
        void intPercent13Of100() {
            Money result = Money.of("100").percent(13);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("13.00"));
            assertThat(result.currency()).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("7.5% of 200 = 15.00")
        void bigDecimalPercent7point5Of200() {
            Money result = Money.of("200").percent(new BigDecimal("7.5"));
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("15.00"));
        }

        @Test
        @DisplayName("0% of 100 = 0.00")
        void zeroPercent() {
            Money result = Money.of("100").percent(0);
            assertThat(result.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("100% of 100 = 100.00")
        void hundredPercent() {
            Money result = Money.of("100").percent(100);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("null rate throws NPE")
        void nullRate() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Money.of("100").percent((BigDecimal) null));
        }
    }

    // ==================== addPercent ====================

    @Nested
    @DisplayName("addPercent(rate)")
    class AddPercentTest {

        @Test
        @DisplayName("100 + 13% = 113.00")
        void add13Percent() {
            Money result = Money.of("100").addPercent(13);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("113.00"));
        }

        @Test
        @DisplayName("200 + 10% = 220.00")
        void add10Percent() {
            Money result = Money.of("200").addPercent(10);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("220.00"));
        }
    }

    // ==================== subtractPercent ====================

    @Nested
    @DisplayName("subtractPercent(rate)")
    class SubtractPercentTest {

        @Test
        @DisplayName("100 - 20% = 80.00")
        void subtract20Percent() {
            Money result = Money.of("100").subtractPercent(20);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("80.00"));
        }

        @Test
        @DisplayName("200 - 50% = 100.00")
        void subtract50Percent() {
            Money result = Money.of("200").subtractPercent(50);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    // ==================== ofMinorUnits ====================

    @Nested
    @DisplayName("Money.ofMinorUnits(long, Currency)")
    class OfMinorUnitsTest {

        @Test
        @DisplayName("10050 minor units in CNY = 100.50")
        void cnyCents() {
            Money result = Money.ofMinorUnits(10050, Currency.CNY);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("100.50"));
            assertThat(result.currency()).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("10050 minor units in USD = 100.50")
        void usdCents() {
            Money result = Money.ofMinorUnits(10050, Currency.USD);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("100.50"));
            assertThat(result.currency()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("1000 minor units in JPY = 1000 (scale 0)")
        void jpyNoDecimals() {
            Money result = Money.ofMinorUnits(1000, Currency.JPY);
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("1000"));
            assertThat(result.currency()).isEqualTo(Currency.JPY);
        }

        @Test
        @DisplayName("null currency throws NPE")
        void nullCurrency() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Money.ofMinorUnits(100, null));
        }
    }

    // ==================== toMinorUnits ====================

    @Nested
    @DisplayName("toMinorUnits()")
    class ToMinorUnitsTest {

        @Test
        @DisplayName("100.50 CNY = 10050 minor units")
        void cnyToMinor() {
            long result = Money.of("100.50").toMinorUnits();
            assertThat(result).isEqualTo(10050L);
        }

        @Test
        @DisplayName("1000 JPY = 1000 minor units (scale 0)")
        void jpyToMinor() {
            long result = Money.of("1000", Currency.JPY).toMinorUnits();
            assertThat(result).isEqualTo(1000L);
        }
    }

    // ==================== isNonNegative / isNonPositive ====================

    @Nested
    @DisplayName("isNonNegative()")
    class IsNonNegativeTest {

        @Test
        @DisplayName("positive amount is non-negative")
        void positive() {
            assertThat(Money.of("10").isNonNegative()).isTrue();
        }

        @Test
        @DisplayName("zero is non-negative")
        void zero() {
            assertThat(Money.zero().isNonNegative()).isTrue();
        }

        @Test
        @DisplayName("negative amount is not non-negative")
        void negative() {
            assertThat(Money.of("-5").isNonNegative()).isFalse();
        }
    }

    @Nested
    @DisplayName("isNonPositive()")
    class IsNonPositiveTest {

        @Test
        @DisplayName("negative amount is non-positive")
        void negative() {
            assertThat(Money.of("-10").isNonPositive()).isTrue();
        }

        @Test
        @DisplayName("zero is non-positive")
        void zero() {
            assertThat(Money.zero().isNonPositive()).isTrue();
        }

        @Test
        @DisplayName("positive amount is not non-positive")
        void positive() {
            assertThat(Money.of("5").isNonPositive()).isFalse();
        }
    }
}
