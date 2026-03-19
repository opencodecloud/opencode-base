package cloud.opencode.base.money.calc;

import cloud.opencode.base.money.Currency;
import cloud.opencode.base.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * MoneyCalcUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("MoneyCalcUtil 测试")
class MoneyCalcUtilTest {

    @Nested
    @DisplayName("sum方法测试")
    class SumTests {

        @Test
        @DisplayName("计算总和")
        void testSum() {
            List<Money> moneys = List.of(Money.of("100"), Money.of("200"), Money.of("300"));
            Money sum = MoneyCalcUtil.sum(moneys);

            assertThat(sum.amount()).isEqualTo(new BigDecimal("600"));
        }

        @Test
        @DisplayName("空集合返回零")
        void testSumEmpty() {
            Money sum = MoneyCalcUtil.sum(Collections.emptyList());
            assertThat(sum.isZero()).isTrue();
        }

        @Test
        @DisplayName("null集合返回零")
        void testSumNull() {
            Money sum = MoneyCalcUtil.sum(null);
            assertThat(sum.isZero()).isTrue();
        }

        @Test
        @DisplayName("带货币参数")
        void testSumWithCurrency() {
            List<Money> moneys = List.of(
                Money.of("100", Currency.USD),
                Money.of("200", Currency.USD)
            );
            Money sum = MoneyCalcUtil.sum(moneys, Currency.USD);

            assertThat(sum.currency()).isEqualTo(Currency.USD);
        }
    }

    @Nested
    @DisplayName("average方法测试")
    class AverageTests {

        @Test
        @DisplayName("计算平均值")
        void testAverage() {
            List<Money> moneys = List.of(Money.of("100"), Money.of("200"), Money.of("300"));
            Money avg = MoneyCalcUtil.average(moneys);

            assertThat(avg.amount()).isEqualTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("空集合抛出异常")
        void testAverageEmpty() {
            assertThatThrownBy(() -> MoneyCalcUtil.average(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("max方法测试")
    class MaxTests {

        @Test
        @DisplayName("找最大值")
        void testMax() {
            List<Money> moneys = List.of(Money.of("100"), Money.of("300"), Money.of("200"));
            Money max = MoneyCalcUtil.max(moneys);

            assertThat(max.amount()).isEqualTo(new BigDecimal("300"));
        }

        @Test
        @DisplayName("空集合抛出异常")
        void testMaxEmpty() {
            assertThatThrownBy(() -> MoneyCalcUtil.max(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("min方法测试")
    class MinTests {

        @Test
        @DisplayName("找最小值")
        void testMin() {
            List<Money> moneys = List.of(Money.of("100"), Money.of("300"), Money.of("200"));
            Money min = MoneyCalcUtil.min(moneys);

            assertThat(min.amount()).isEqualTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("空集合抛出异常")
        void testMinEmpty() {
            assertThatThrownBy(() -> MoneyCalcUtil.min(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("percentage方法测试")
    class PercentageTests {

        @Test
        @DisplayName("计算百分比")
        void testPercentage() {
            Money part = Money.of("25");
            Money total = Money.of("100");
            BigDecimal pct = MoneyCalcUtil.percentage(part, total, 2);

            assertThat(pct).isEqualTo(new BigDecimal("0.2500"));
        }

        @Test
        @DisplayName("零总额返回零")
        void testPercentageZeroTotal() {
            Money part = Money.of("25");
            Money total = Money.zero();
            BigDecimal pct = MoneyCalcUtil.percentage(part, total, 2);

            assertThat(pct).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("计算整数百分比")
        void testPercentageInt() {
            Money part = Money.of("25");
            Money total = Money.of("100");
            int pct = MoneyCalcUtil.percentageInt(part, total);

            assertThat(pct).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("applyDiscount方法测试")
    class ApplyDiscountTests {

        @Test
        @DisplayName("应用折扣率")
        void testApplyDiscount() {
            Money money = Money.of("100");
            Money discounted = MoneyCalcUtil.applyDiscount(money, new BigDecimal("0.1"));

            assertThat(discounted.amount()).isEqualTo(new BigDecimal("90.00"));
        }

        @Test
        @DisplayName("应用折扣百分比")
        void testApplyDiscountPercent() {
            Money money = Money.of("100");
            Money discounted = MoneyCalcUtil.applyDiscountPercent(money, 10);

            assertThat(discounted.amount()).isEqualTo(new BigDecimal("90.00"));
        }
    }

    @Nested
    @DisplayName("税金计算测试")
    class TaxTests {

        @Test
        @DisplayName("计算税额")
        void testCalculateTax() {
            Money money = Money.of("100");
            Money tax = MoneyCalcUtil.calculateTax(money, new BigDecimal("0.1"));

            assertThat(tax.amount()).isEqualTo(new BigDecimal("10.00"));
        }

        @Test
        @DisplayName("加税")
        void testAddTax() {
            Money money = Money.of("100");
            Money withTax = MoneyCalcUtil.addTax(money, new BigDecimal("0.1"));

            assertThat(withTax.amount()).isEqualTo(new BigDecimal("110.00"));
        }

        @Test
        @DisplayName("去税")
        void testRemoveTax() {
            Money withTax = Money.of("110");
            Money preTax = MoneyCalcUtil.removeTax(withTax, new BigDecimal("0.1"));

            assertThat(preTax.amount()).isEqualTo(new BigDecimal("100.00"));
        }
    }

    @Nested
    @DisplayName("roundToNearest方法测试")
    class RoundToNearestTests {

        @Test
        @DisplayName("四舍五入到最近的整数")
        void testRoundToNearestOne() {
            Money money = Money.of("100.56");
            Money rounded = MoneyCalcUtil.roundToNearest(money, BigDecimal.ONE);

            assertThat(rounded.amount()).isEqualTo(new BigDecimal("101"));
        }

        @Test
        @DisplayName("四舍五入到最近的十")
        void testRoundToNearestTen() {
            Money money = Money.of("105.50");
            Money rounded = MoneyCalcUtil.roundToNearest(money, BigDecimal.TEN);

            assertThat(rounded.amount()).isEqualTo(new BigDecimal("110"));
        }
    }

    @Nested
    @DisplayName("areEqual方法测试")
    class AreEqualTests {

        @Test
        @DisplayName("相等金额")
        void testAreEqual() {
            Money m1 = Money.of("100.00");
            Money m2 = Money.of("100");

            assertThat(MoneyCalcUtil.areEqual(m1, m2)).isTrue();
        }

        @Test
        @DisplayName("不相等金额")
        void testAreNotEqual() {
            Money m1 = Money.of("100");
            Money m2 = Money.of("200");

            assertThat(MoneyCalcUtil.areEqual(m1, m2)).isFalse();
        }

        @Test
        @DisplayName("不同货币不相等")
        void testDifferentCurrencyNotEqual() {
            Money m1 = Money.of("100", Currency.CNY);
            Money m2 = Money.of("100", Currency.USD);

            assertThat(MoneyCalcUtil.areEqual(m1, m2)).isFalse();
        }

        @Test
        @DisplayName("null比较")
        void testNullComparison() {
            assertThat(MoneyCalcUtil.areEqual(null, null)).isTrue();
            assertThat(MoneyCalcUtil.areEqual(null, Money.of("100"))).isFalse();
            assertThat(MoneyCalcUtil.areEqual(Money.of("100"), null)).isFalse();
        }
    }
}
