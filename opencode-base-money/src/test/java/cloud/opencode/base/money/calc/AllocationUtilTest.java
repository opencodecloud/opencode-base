package cloud.opencode.base.money.calc;

import cloud.opencode.base.money.Currency;
import cloud.opencode.base.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * AllocationUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("AllocationUtil 测试")
class AllocationUtilTest {

    @Nested
    @DisplayName("allocate方法测试")
    class AllocateTests {

        @Test
        @DisplayName("按比例分摊")
        void testAllocate() {
            Money total = Money.of("100");
            List<Money> parts = AllocationUtil.allocate(total, 1, 2, 3);

            assertThat(parts).hasSize(3);
            assertThat(AllocationUtil.verify(total, parts)).isTrue();
        }

        @Test
        @DisplayName("1:1比例")
        void testAllocateEqual() {
            Money total = Money.of("100");
            List<Money> parts = AllocationUtil.allocate(total, 1, 1);

            assertThat(parts).hasSize(2);
        }

        @Test
        @DisplayName("null总额抛出异常")
        void testAllocateNullTotal() {
            assertThatThrownBy(() -> AllocationUtil.allocate(null, 1, 2))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空比例抛出异常")
        void testAllocateEmptyRatios() {
            Money total = Money.of("100");
            assertThatThrownBy(() -> AllocationUtil.allocate(total))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负比例抛出异常")
        void testAllocateNegativeRatio() {
            Money total = Money.of("100");
            assertThatThrownBy(() -> AllocationUtil.allocate(total, 1, -2))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("零比例总和抛出异常")
        void testAllocateZeroSum() {
            Money total = Money.of("100");
            assertThatThrownBy(() -> AllocationUtil.allocate(total, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("allocateByPercent方法测试")
    class AllocateByPercentTests {

        @Test
        @DisplayName("按百分比分摊")
        void testAllocateByPercent() {
            Money total = Money.of("100");
            List<Money> parts = AllocationUtil.allocateByPercent(total, 20, 30, 50);

            assertThat(parts).hasSize(3);
            assertThat(AllocationUtil.verify(total, parts)).isTrue();
        }

        @Test
        @DisplayName("百分比不等于100抛出异常")
        void testPercentNotSum100() {
            Money total = Money.of("100");
            assertThatThrownBy(() -> AllocationUtil.allocateByPercent(total, 20, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100");
        }
    }

    @Nested
    @DisplayName("split方法测试")
    class SplitTests {

        @Test
        @DisplayName("平均分摊")
        void testSplit() {
            Money total = Money.of("100");
            List<Money> parts = AllocationUtil.split(total, 3);

            assertThat(parts).hasSize(3);
            assertThat(AllocationUtil.verify(total, parts)).isTrue();
        }

        @Test
        @DisplayName("零份数抛出异常")
        void testSplitZeroParts() {
            Money total = Money.of("100");
            assertThatThrownBy(() -> AllocationUtil.split(total, 0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负份数抛出异常")
        void testSplitNegativeParts() {
            Money total = Money.of("100");
            assertThatThrownBy(() -> AllocationUtil.split(total, -1))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("allocateByWeights方法测试")
    class AllocateByWeightsTests {

        @Test
        @DisplayName("按权重分摊")
        void testAllocateByWeights() {
            Money total = Money.of("100");
            List<Money> parts = AllocationUtil.allocateByWeights(total,
                new BigDecimal("1.5"), new BigDecimal("2.5"));

            assertThat(parts).hasSize(2);
            assertThat(AllocationUtil.verify(total, parts)).isTrue();
        }

        @Test
        @DisplayName("null权重抛出异常")
        void testNullWeight() {
            Money total = Money.of("100");
            assertThatThrownBy(() -> AllocationUtil.allocateByWeights(total,
                new BigDecimal("1"), null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负权重抛出异常")
        void testNegativeWeight() {
            Money total = Money.of("100");
            assertThatThrownBy(() -> AllocationUtil.allocateByWeights(total,
                new BigDecimal("1"), new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("零权重总和抛出异常")
        void testZeroSumWeight() {
            Money total = Money.of("100");
            assertThatThrownBy(() -> AllocationUtil.allocateByWeights(total,
                BigDecimal.ZERO, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("splitWithMinimum方法测试")
    class SplitWithMinimumTests {

        @Test
        @DisplayName("带最小金额分摊")
        void testSplitWithMinimum() {
            Money total = Money.of("100");
            Money min = Money.of("10");
            List<Money> parts = AllocationUtil.splitWithMinimum(total, 3, min);

            assertThat(parts).hasSize(3);
            for (Money part : parts) {
                assertThat(part.isGreaterOrEqual(min)).isTrue();
            }
        }

        @Test
        @DisplayName("总额小于最小值抛出异常")
        void testTotalLessThanMinimum() {
            Money total = Money.of("20");
            Money min = Money.of("10");
            assertThatThrownBy(() -> AllocationUtil.splitWithMinimum(total, 3, min))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("splitRoundRobin方法测试")
    class SplitRoundRobinTests {

        @Test
        @DisplayName("轮询分摊")
        void testSplitRoundRobin() {
            Money total = Money.of("100");
            List<Money> parts = AllocationUtil.splitRoundRobin(total, 3);

            assertThat(parts).hasSize(3);
            assertThat(AllocationUtil.verify(total, parts)).isTrue();
        }

        @Test
        @DisplayName("零份数抛出异常")
        void testZeroParts() {
            Money total = Money.of("100");
            assertThatThrownBy(() -> AllocationUtil.splitRoundRobin(total, 0))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("verify方法测试")
    class VerifyTests {

        @Test
        @DisplayName("验证通过")
        void testVerifyPass() {
            Money total = Money.of("100");
            List<Money> parts = List.of(Money.of("50"), Money.of("50"));

            assertThat(AllocationUtil.verify(total, parts)).isTrue();
        }

        @Test
        @DisplayName("验证失败")
        void testVerifyFail() {
            Money total = Money.of("100");
            List<Money> parts = List.of(Money.of("40"), Money.of("50"));

            assertThat(AllocationUtil.verify(total, parts)).isFalse();
        }
    }
}
