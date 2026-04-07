package cloud.opencode.base.money.calc;

import cloud.opencode.base.money.Currency;
import cloud.opencode.base.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;

/**
 * MoneyRounding test class | MoneyRounding 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.3
 */
@DisplayName("MoneyRounding Test | 金额舍入测试")
class MoneyRoundingTest {

    // ============ swedish ============

    @Nested
    @DisplayName("swedish | 瑞典舍入测试")
    class SwedishTests {

        @Test
        @DisplayName("10.22 -> 10.20")
        void roundDown() {
            Money result = MoneyRounding.swedish(Money.of("10.22"));
            assertThat(result.amount()).isEqualByComparingTo("10.20");
        }

        @Test
        @DisplayName("10.23 -> 10.25")
        void roundUp() {
            Money result = MoneyRounding.swedish(Money.of("10.23"));
            assertThat(result.amount()).isEqualByComparingTo("10.25");
        }

        @Test
        @DisplayName("10.25 -> 10.25 (exact step)")
        void exactStep() {
            Money result = MoneyRounding.swedish(Money.of("10.25"));
            assertThat(result.amount()).isEqualByComparingTo("10.25");
        }

        @Test
        @DisplayName("10.27 -> 10.25")
        void roundDownFromAbove() {
            Money result = MoneyRounding.swedish(Money.of("10.27"));
            assertThat(result.amount()).isEqualByComparingTo("10.25");
        }

        @Test
        @DisplayName("10.28 -> 10.30")
        void roundUpToNext() {
            Money result = MoneyRounding.swedish(Money.of("10.28"));
            assertThat(result.amount()).isEqualByComparingTo("10.30");
        }
    }

    // ============ bankers ============

    @Nested
    @DisplayName("bankers | 银行家舍入测试")
    class BankersTests {

        @Test
        @DisplayName("10.225 -> 10.22 (HALF_EVEN rounds to even)")
        void halfEvenDown() {
            Money result = MoneyRounding.bankers(Money.of("10.225"));
            assertThat(result.amount()).isEqualByComparingTo("10.22");
        }

        @Test
        @DisplayName("10.235 -> 10.24 (HALF_EVEN rounds to even)")
        void halfEvenUp() {
            Money result = MoneyRounding.bankers(Money.of("10.235"));
            assertThat(result.amount()).isEqualByComparingTo("10.24");
        }

        @Test
        @DisplayName("10.245 -> 10.24 (HALF_EVEN rounds to even)")
        void halfEvenDown2() {
            Money result = MoneyRounding.bankers(Money.of("10.245"));
            assertThat(result.amount()).isEqualByComparingTo("10.24");
        }

        @Test
        @DisplayName("10.255 -> 10.26 (HALF_EVEN rounds to even)")
        void halfEvenUp2() {
            Money result = MoneyRounding.bankers(Money.of("10.255"));
            assertThat(result.amount()).isEqualByComparingTo("10.26");
        }
    }

    // ============ standard ============

    @Nested
    @DisplayName("standard | 标准舍入测试")
    class StandardTests {

        @Test
        @DisplayName("HALF_UP behavior: 10.225 -> 10.23")
        void halfUp() {
            Money result = MoneyRounding.standard(Money.of("10.225"));
            assertThat(result.amount()).isEqualByComparingTo("10.23");
        }

        @Test
        @DisplayName("HALF_UP behavior: 10.224 -> 10.22")
        void roundDown() {
            Money result = MoneyRounding.standard(Money.of("10.224"));
            assertThat(result.amount()).isEqualByComparingTo("10.22");
        }

        @Test
        @DisplayName("Already at scale: 10.23 -> 10.23")
        void alreadyAtScale() {
            Money result = MoneyRounding.standard(Money.of("10.23"));
            assertThat(result.amount()).isEqualByComparingTo("10.23");
        }
    }

    // ============ ceil / floor ============

    @Nested
    @DisplayName("ceil | 向上取整测试")
    class CeilTests {

        @Test
        @DisplayName("Ceil rounds up to currency scale | 向上取整到货币精度")
        void ceilUp() {
            Money result = MoneyRounding.ceil(Money.of("10.221"));
            assertThat(result.amount()).isEqualByComparingTo("10.23");
        }

        @Test
        @DisplayName("Ceil already at scale | 已在精度内")
        void ceilExact() {
            Money result = MoneyRounding.ceil(Money.of("10.23"));
            assertThat(result.amount()).isEqualByComparingTo("10.23");
        }

        @Test
        @DisplayName("Ceil negative rounds toward zero | 负数向零取整")
        void ceilNegative() {
            Money result = MoneyRounding.ceil(Money.of("-10.229"));
            assertThat(result.amount()).isEqualByComparingTo("-10.22");
        }
    }

    @Nested
    @DisplayName("floor | 向下取整测试")
    class FloorTests {

        @Test
        @DisplayName("Floor rounds down to currency scale | 向下取整到货币精度")
        void floorDown() {
            Money result = MoneyRounding.floor(Money.of("10.229"));
            assertThat(result.amount()).isEqualByComparingTo("10.22");
        }

        @Test
        @DisplayName("Floor already at scale | 已在精度内")
        void floorExact() {
            Money result = MoneyRounding.floor(Money.of("10.23"));
            assertThat(result.amount()).isEqualByComparingTo("10.23");
        }

        @Test
        @DisplayName("Floor negative rounds away from zero | 负数远离零取整")
        void floorNegative() {
            Money result = MoneyRounding.floor(Money.of("-10.221"));
            assertThat(result.amount()).isEqualByComparingTo("-10.23");
        }
    }

    // ============ roundToStep ============

    @Nested
    @DisplayName("roundToStep | 步进舍入测试")
    class RoundToStepTests {

        @Test
        @DisplayName("Step 0.05: 10.23 -> 10.25")
        void step005() {
            Money result = MoneyRounding.roundToStep(Money.of("10.23"), new BigDecimal("0.05"));
            assertThat(result.amount()).isEqualByComparingTo("10.25");
        }

        @Test
        @DisplayName("Step 0.5: 10.23 -> 10.00")
        void step05() {
            Money result = MoneyRounding.roundToStep(Money.of("10.23"), new BigDecimal("0.5"));
            assertThat(result.amount()).isEqualByComparingTo("10.00");
        }

        @Test
        @DisplayName("Step 1: 10.23 -> 10")
        void step1() {
            Money result = MoneyRounding.roundToStep(Money.of("10.23"), BigDecimal.ONE);
            assertThat(result.amount()).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("Step 10: 13.50 -> 10")
        void step10() {
            Money result = MoneyRounding.roundToStep(Money.of("13.50"), BigDecimal.TEN);
            assertThat(result.amount()).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("Step 10: 15.00 -> 20 (HALF_UP)")
        void step10HalfUp() {
            Money result = MoneyRounding.roundToStep(Money.of("15.00"), BigDecimal.TEN);
            assertThat(result.amount()).isEqualByComparingTo("20");
        }
    }

    // ============ ceilToStep / floorToStep ============

    @Nested
    @DisplayName("ceilToStep | 向上步进舍入测试")
    class CeilToStepTests {

        @Test
        @DisplayName("Ceil to step 1: 10.23 -> 11")
        void ceilToStep1() {
            Money result = MoneyRounding.ceilToStep(Money.of("10.23"), BigDecimal.ONE);
            assertThat(result.amount()).isEqualByComparingTo("11");
        }

        @Test
        @DisplayName("Ceil to step 0.05: 10.22 -> 10.25")
        void ceilToStep005() {
            Money result = MoneyRounding.ceilToStep(Money.of("10.22"), new BigDecimal("0.05"));
            assertThat(result.amount()).isEqualByComparingTo("10.25");
        }

        @Test
        @DisplayName("Ceil exact step: 10.25 -> 10.25")
        void ceilExact() {
            Money result = MoneyRounding.ceilToStep(Money.of("10.25"), new BigDecimal("0.05"));
            assertThat(result.amount()).isEqualByComparingTo("10.25");
        }
    }

    @Nested
    @DisplayName("floorToStep | 向下步进舍入测试")
    class FloorToStepTests {

        @Test
        @DisplayName("Floor to step 1: 10.99 -> 10")
        void floorToStep1() {
            Money result = MoneyRounding.floorToStep(Money.of("10.99"), BigDecimal.ONE);
            assertThat(result.amount()).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("Floor to step 0.05: 10.28 -> 10.25")
        void floorToStep005() {
            Money result = MoneyRounding.floorToStep(Money.of("10.28"), new BigDecimal("0.05"));
            assertThat(result.amount()).isEqualByComparingTo("10.25");
        }

        @Test
        @DisplayName("Floor exact step: 10.25 -> 10.25")
        void floorExact() {
            Money result = MoneyRounding.floorToStep(Money.of("10.25"), new BigDecimal("0.05"));
            assertThat(result.amount()).isEqualByComparingTo("10.25");
        }
    }

    // ============ round(Money, RoundingMode) ============

    @Nested
    @DisplayName("round(Money, RoundingMode) | 指定模式舍入测试")
    class RoundWithModeTests {

        @Test
        @DisplayName("HALF_UP mode")
        void halfUp() {
            Money result = MoneyRounding.round(Money.of("10.225"), RoundingMode.HALF_UP);
            assertThat(result.amount()).isEqualByComparingTo("10.23");
        }

        @Test
        @DisplayName("HALF_DOWN mode")
        void halfDown() {
            Money result = MoneyRounding.round(Money.of("10.225"), RoundingMode.HALF_DOWN);
            assertThat(result.amount()).isEqualByComparingTo("10.22");
        }

        @Test
        @DisplayName("CEILING mode")
        void ceiling() {
            Money result = MoneyRounding.round(Money.of("10.221"), RoundingMode.CEILING);
            assertThat(result.amount()).isEqualByComparingTo("10.23");
        }

        @Test
        @DisplayName("FLOOR mode")
        void floor() {
            Money result = MoneyRounding.round(Money.of("10.229"), RoundingMode.FLOOR);
            assertThat(result.amount()).isEqualByComparingTo("10.22");
        }
    }

    // ============ round(Money, int, RoundingMode) ============

    @Nested
    @DisplayName("round(Money, int, RoundingMode) | 自定义精度舍入测试")
    class RoundWithScaleTests {

        @Test
        @DisplayName("Scale 0: 10.55 -> 11")
        void scale0() {
            Money result = MoneyRounding.round(Money.of("10.55"), 0, RoundingMode.HALF_UP);
            assertThat(result.amount()).isEqualByComparingTo("11");
        }

        @Test
        @DisplayName("Scale 1: 10.55 -> 10.6")
        void scale1() {
            Money result = MoneyRounding.round(Money.of("10.55"), 1, RoundingMode.HALF_UP);
            assertThat(result.amount()).isEqualByComparingTo("10.6");
        }

        @Test
        @DisplayName("Scale 4: preserves precision")
        void scale4() {
            Money result = MoneyRounding.round(Money.of("10.12345"), 4, RoundingMode.HALF_UP);
            assertThat(result.amount()).isEqualByComparingTo("10.1235");
        }

        @Test
        @DisplayName("Negative scale throws IllegalArgumentException | 负精度抛出异常")
        void negativeScale() {
            assertThatThrownBy(() -> MoneyRounding.round(Money.of("10"), -1, RoundingMode.HALF_UP))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Scale must not be negative");
        }
    }

    // ============ null validation ============

    @Nested
    @DisplayName("Null validation | 空值校验测试")
    class NullValidationTests {

        @Test
        @DisplayName("swedish(null) throws NullPointerException")
        void swedishNull() {
            assertThatThrownBy(() -> MoneyRounding.swedish(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("bankers(null) throws NullPointerException")
        void bankersNull() {
            assertThatThrownBy(() -> MoneyRounding.bankers(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("standard(null) throws NullPointerException")
        void standardNull() {
            assertThatThrownBy(() -> MoneyRounding.standard(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("ceil(null) throws NullPointerException")
        void ceilNull() {
            assertThatThrownBy(() -> MoneyRounding.ceil(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("floor(null) throws NullPointerException")
        void floorNull() {
            assertThatThrownBy(() -> MoneyRounding.floor(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("roundToStep(null, step) throws NullPointerException")
        void roundToStepNullMoney() {
            assertThatThrownBy(() -> MoneyRounding.roundToStep(null, BigDecimal.ONE))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("roundToStep(money, null) throws NullPointerException")
        void roundToStepNullStep() {
            assertThatThrownBy(() -> MoneyRounding.roundToStep(Money.of("10"), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("round(null, RoundingMode) throws NullPointerException")
        void roundNullMoney() {
            assertThatThrownBy(() -> MoneyRounding.round(null, RoundingMode.HALF_UP))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("round(money, null) throws NullPointerException")
        void roundNullMode() {
            assertThatThrownBy(() -> MoneyRounding.round(Money.of("10"), (RoundingMode) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ============ negative step ============

    @Nested
    @DisplayName("Negative step | 负步进测试")
    class NegativeStepTests {

        @Test
        @DisplayName("Negative step throws IllegalArgumentException | 负步进抛出异常")
        void negativeStep() {
            assertThatThrownBy(() -> MoneyRounding.roundToStep(Money.of("10"), new BigDecimal("-0.05")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("Zero step throws IllegalArgumentException | 零步进抛出异常")
        void zeroStep() {
            assertThatThrownBy(() -> MoneyRounding.roundToStep(Money.of("10"), BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("Negative step for ceilToStep throws | ceilToStep负步进抛出异常")
        void negativeStepCeil() {
            assertThatThrownBy(() -> MoneyRounding.ceilToStep(Money.of("10"), new BigDecimal("-1")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Negative step for floorToStep throws | floorToStep负步进抛出异常")
        void negativeStepFloor() {
            assertThatThrownBy(() -> MoneyRounding.floorToStep(Money.of("10"), new BigDecimal("-1")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
