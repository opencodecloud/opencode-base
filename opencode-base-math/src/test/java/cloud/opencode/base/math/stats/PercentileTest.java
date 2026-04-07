package cloud.opencode.base.math.stats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Comprehensive tests for {@link Percentile}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("Percentile 百分位数计算器测试")
class PercentileTest {

    private static final double[] DATA_10 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private static final double[] DATA_5 = {15, 20, 35, 40, 50};

    // ==================== Factory Tests ====================

    @Nested
    @DisplayName("of 工厂方法")
    class FactoryTests {

        @Test
        @DisplayName("正常创建")
        void normalCreation() {
            Percentile p = Percentile.of(new double[]{1, 2, 3});
            assertThat(p).isNotNull();
        }

        @Test
        @DisplayName("不修改原始数组")
        void doesNotMutateInput() {
            double[] data = {5, 3, 1, 4, 2};
            double[] copy = data.clone();
            Percentile.of(data);
            assertThat(data).containsExactly(copy);
        }

        @Test
        @DisplayName("null数组抛出异常")
        void nullArray() {
            assertThatThrownBy(() -> Percentile.of(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空数组抛出异常")
        void emptyArray() {
            assertThatThrownBy(() -> Percentile.of(new double[]{}))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== LINEAR Method Tests ====================

    @Nested
    @DisplayName("LINEAR 线性插值")
    class LinearTests {

        @Test
        @DisplayName("中位数")
        void median() {
            Percentile p = Percentile.of(DATA_10);
            assertThat(p.value(50)).isCloseTo(5.5, within(1e-10));
        }

        @Test
        @DisplayName("第0百分位数等于最小值")
        void zeroth() {
            Percentile p = Percentile.of(DATA_10);
            assertThat(p.value(0)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("第100百分位数等于最大值")
        void hundredth() {
            Percentile p = Percentile.of(DATA_10);
            assertThat(p.value(100)).isCloseTo(10.0, within(1e-10));
        }

        @Test
        @DisplayName("第25百分位数")
        void q1() {
            Percentile p = Percentile.of(DATA_10);
            // rank = 0.25 * 9 = 2.25 -> 3 + 0.25*(4-3) = 3.25
            assertThat(p.value(25)).isCloseTo(3.25, within(1e-10));
        }

        @Test
        @DisplayName("第75百分位数")
        void q3() {
            Percentile p = Percentile.of(DATA_10);
            // rank = 0.75 * 9 = 6.75 -> 7 + 0.75*(8-7) = 7.75
            assertThat(p.value(75)).isCloseTo(7.75, within(1e-10));
        }

        @Test
        @DisplayName("单元素数组")
        void singleElement() {
            Percentile p = Percentile.of(new double[]{42});
            assertThat(p.value(0)).isCloseTo(42.0, within(1e-10));
            assertThat(p.value(50)).isCloseTo(42.0, within(1e-10));
            assertThat(p.value(100)).isCloseTo(42.0, within(1e-10));
        }

        @Test
        @DisplayName("默认方法是LINEAR")
        void defaultIsLinear() {
            Percentile p = Percentile.of(DATA_10);
            assertThat(p.value(30)).isCloseTo(p.value(30, Percentile.Method.LINEAR), within(1e-10));
        }
    }

    // ==================== LOWER Method Tests ====================

    @Nested
    @DisplayName("LOWER 取下界")
    class LowerTests {

        @Test
        @DisplayName("取较低值")
        void takesLowerValue() {
            Percentile p = Percentile.of(DATA_10);
            // rank = 0.25 * 9 = 2.25 -> floor=2, sorted[2]=3
            assertThat(p.value(25, Percentile.Method.LOWER)).isCloseTo(3.0, within(1e-10));
        }

        @Test
        @DisplayName("精确位置返回该值")
        void exactPosition() {
            Percentile p = Percentile.of(DATA_5);
            // rank = 0.50 * 4 = 2.0 -> floor=2, sorted[2]=35
            assertThat(p.value(50, Percentile.Method.LOWER)).isCloseTo(35.0, within(1e-10));
        }

        @Test
        @DisplayName("第0百分位数")
        void zeroth() {
            Percentile p = Percentile.of(DATA_10);
            assertThat(p.value(0, Percentile.Method.LOWER)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("第100百分位数")
        void hundredth() {
            Percentile p = Percentile.of(DATA_10);
            assertThat(p.value(100, Percentile.Method.LOWER)).isCloseTo(10.0, within(1e-10));
        }
    }

    // ==================== HIGHER Method Tests ====================

    @Nested
    @DisplayName("HIGHER 取上界")
    class HigherTests {

        @Test
        @DisplayName("取较高值")
        void takesHigherValue() {
            Percentile p = Percentile.of(DATA_10);
            // rank = 0.25 * 9 = 2.25 -> ceil=3, sorted[3]=4
            assertThat(p.value(25, Percentile.Method.HIGHER)).isCloseTo(4.0, within(1e-10));
        }

        @Test
        @DisplayName("精确位置返回该值")
        void exactPosition() {
            Percentile p = Percentile.of(DATA_5);
            // rank = 0.50 * 4 = 2.0 -> ceil=2, sorted[2]=35
            assertThat(p.value(50, Percentile.Method.HIGHER)).isCloseTo(35.0, within(1e-10));
        }

        @Test
        @DisplayName("第0百分位数")
        void zeroth() {
            Percentile p = Percentile.of(DATA_10);
            assertThat(p.value(0, Percentile.Method.HIGHER)).isCloseTo(1.0, within(1e-10));
        }
    }

    // ==================== NEAREST Method Tests ====================

    @Nested
    @DisplayName("NEAREST 最近值")
    class NearestTests {

        @Test
        @DisplayName("接近下界取下界")
        void closerToLower() {
            Percentile p = Percentile.of(DATA_10);
            // rank = 0.10 * 9 = 0.9 -> frac=0.9 > 0.5 -> upper -> sorted[1]=2
            assertThat(p.value(10, Percentile.Method.NEAREST)).isCloseTo(2.0, within(1e-10));
        }

        @Test
        @DisplayName("刚好在中间取下界")
        void exactMiddle() {
            Percentile p = Percentile.of(DATA_10);
            // For p that gives frac exactly 0.5: rank = 0.5*(9) but let's try
            // p = 50/9*100 = ... more naturally, p=50: rank=4.5 -> frac=0.5 -> lower -> sorted[4]=5
            // Actually frac = 0.5 so <= 0.5 picks lower
            assertThat(p.value(50, Percentile.Method.NEAREST)).isCloseTo(5.0, within(1e-10));
        }

        @Test
        @DisplayName("接近上界取上界")
        void closerToHigher() {
            Percentile p = Percentile.of(DATA_10);
            // rank = 0.30 * 9 = 2.7 -> frac=0.7 > 0.5 -> upper -> sorted[3]=4
            assertThat(p.value(30, Percentile.Method.NEAREST)).isCloseTo(4.0, within(1e-10));
        }
    }

    // ==================== MIDPOINT Method Tests ====================

    @Nested
    @DisplayName("MIDPOINT 中点")
    class MidpointTests {

        @Test
        @DisplayName("两个相邻值的中点")
        void midpointBetweenValues() {
            Percentile p = Percentile.of(DATA_10);
            // rank = 0.25 * 9 = 2.25 -> (sorted[2]+sorted[3])/2 = (3+4)/2 = 3.5
            assertThat(p.value(25, Percentile.Method.MIDPOINT)).isCloseTo(3.5, within(1e-10));
        }

        @Test
        @DisplayName("精确位置返回该值")
        void exactPosition() {
            Percentile p = Percentile.of(DATA_5);
            // rank = 0.50 * 4 = 2.0 -> (sorted[2]+sorted[2])/2 = 35
            assertThat(p.value(50, Percentile.Method.MIDPOINT)).isCloseTo(35.0, within(1e-10));
        }

        @Test
        @DisplayName("第0百分位数")
        void zeroth() {
            Percentile p = Percentile.of(DATA_10);
            // (sorted[0]+sorted[0])/2 = 1.0
            assertThat(p.value(0, Percentile.Method.MIDPOINT)).isCloseTo(1.0, within(1e-10));
        }
    }

    // ==================== Quartile Tests ====================

    @Nested
    @DisplayName("quartile 四分位数")
    class QuartileTests {

        @Test
        @DisplayName("Q1 等于 value(25)")
        void q1() {
            Percentile p = Percentile.of(DATA_10);
            assertThat(p.quartile(1)).isCloseTo(p.value(25), within(1e-10));
        }

        @Test
        @DisplayName("Q2（中位数）等于 value(50)")
        void q2() {
            Percentile p = Percentile.of(DATA_10);
            assertThat(p.quartile(2)).isCloseTo(p.value(50), within(1e-10));
        }

        @Test
        @DisplayName("Q3 等于 value(75)")
        void q3() {
            Percentile p = Percentile.of(DATA_10);
            assertThat(p.quartile(3)).isCloseTo(p.value(75), within(1e-10));
        }

        @Test
        @DisplayName("Q0 抛出异常")
        void q0() {
            Percentile p = Percentile.of(DATA_10);
            assertThatThrownBy(() -> p.quartile(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Q4 抛出异常")
        void q4() {
            Percentile p = Percentile.of(DATA_10);
            assertThatThrownBy(() -> p.quartile(4))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Validation Tests ====================

    @Nested
    @DisplayName("输入验证")
    class ValidationTests {

        @Test
        @DisplayName("p小于0抛出异常")
        void pBelowZero() {
            Percentile p = Percentile.of(DATA_10);
            assertThatThrownBy(() -> p.value(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("p大于100抛出异常")
        void pAbove100() {
            Percentile p = Percentile.of(DATA_10);
            assertThatThrownBy(() -> p.value(101))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null方法抛出异常")
        void nullMethod() {
            Percentile p = Percentile.of(DATA_10);
            assertThatThrownBy(() -> p.value(50, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Edge Case Tests ====================

    @Nested
    @DisplayName("边界情况")
    class EdgeCaseTests {

        @Test
        @DisplayName("两元素数组")
        void twoElements() {
            Percentile p = Percentile.of(new double[]{10, 20});
            assertThat(p.value(0)).isCloseTo(10.0, within(1e-10));
            assertThat(p.value(50)).isCloseTo(15.0, within(1e-10));
            assertThat(p.value(100)).isCloseTo(20.0, within(1e-10));
        }

        @Test
        @DisplayName("所有值相同")
        void allSame() {
            Percentile p = Percentile.of(new double[]{7, 7, 7, 7});
            assertThat(p.value(0)).isCloseTo(7.0, within(1e-10));
            assertThat(p.value(50)).isCloseTo(7.0, within(1e-10));
            assertThat(p.value(100)).isCloseTo(7.0, within(1e-10));
        }

        @Test
        @DisplayName("负数值")
        void negativeValues() {
            Percentile p = Percentile.of(new double[]{-10, -5, 0, 5, 10});
            assertThat(p.value(50)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("无序输入仍正确计算")
        void unsortedInput() {
            Percentile p1 = Percentile.of(new double[]{5, 3, 1, 4, 2});
            Percentile p2 = Percentile.of(new double[]{1, 2, 3, 4, 5});
            assertThat(p1.value(50)).isCloseTo(p2.value(50), within(1e-10));
        }

        @Test
        @DisplayName("所有方法对单元素数组返回相同值")
        void singleElementAllMethods() {
            Percentile p = Percentile.of(new double[]{42});
            for (Percentile.Method method : Percentile.Method.values()) {
                assertThat(p.value(50, method)).isCloseTo(42.0, within(1e-10));
            }
        }
    }
}
