package cloud.opencode.base.math.stats.inference;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Comprehensive tests for {@link AnovaTest}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("AnovaTest ANOVA 方差分析测试")
class AnovaTestTest {

    @Nested
    @DisplayName("oneWay 单因素方差分析")
    class OneWayTests {

        @Test
        @DisplayName("三组均值明显不同 → 显著")
        void threGroupsDifferentMeans() {
            double[] group1 = {1, 2, 3, 4, 5};
            double[] group2 = {11, 12, 13, 14, 15};
            double[] group3 = {21, 22, 23, 24, 25};
            TestResult result = AnovaTest.oneWay(group1, group2, group3);

            assertThat(result.testName()).isEqualTo("One-Way ANOVA F-Test");
            assertThat(result.isSignificant()).isTrue();
            assertThat(result.pValue()).isLessThan(0.001);
            assertThat(result.degreesOfFreedom()).isCloseTo(2.0, within(1e-10));
        }

        @Test
        @DisplayName("三组均值相同 → 不显著")
        void sameGroupMeans() {
            double[] group1 = {5, 6, 7, 8, 9};
            double[] group2 = {5, 6, 7, 8, 9};
            double[] group3 = {5, 6, 7, 8, 9};
            TestResult result = AnovaTest.oneWay(group1, group2, group3);

            assertThat(result.statistic()).isCloseTo(0.0, within(1e-10));
            assertThat(result.pValue()).isCloseTo(1.0, within(1e-5));
            assertThat(result.isSignificant()).isFalse();
        }

        @Test
        @DisplayName("两组等价于 F 检验")
        void twoGroupsEquivalent() {
            double[] group1 = {1, 2, 3, 4, 5};
            double[] group2 = {6, 7, 8, 9, 10};
            TestResult result = AnovaTest.oneWay(group1, group2);

            assertThat(result.degreesOfFreedom()).isCloseTo(1.0, within(1e-10));
            assertThat(result.isSignificant()).isTrue();
        }

        @Test
        @DisplayName("已知 F 值验证")
        void knownFValue() {
            // Three groups: {2,3,4}, {6,7,8}, {10,11,12}
            // Grand mean = 7, group means = 3, 7, 11
            // SSB = 3*(3-7)^2 + 3*(7-7)^2 + 3*(11-7)^2 = 3*16 + 0 + 3*16 = 96
            // SSW = (1+0+1)*3 = 6 (each group has var=1, sum of sq dev = 2 each, total = 6)
            // df1=2, df2=6, MSB=48, MSW=1, F=48
            double[] g1 = {2, 3, 4};
            double[] g2 = {6, 7, 8};
            double[] g3 = {10, 11, 12};
            TestResult result = AnovaTest.oneWay(g1, g2, g3);

            assertThat(result.statistic()).isCloseTo(48.0, within(1e-10));
            assertThat(result.degreesOfFreedom()).isCloseTo(2.0, within(1e-10));
        }

        @Test
        @DisplayName("不等长组")
        void unequalGroupSizes() {
            double[] g1 = {1, 2, 3};
            double[] g2 = {10, 11, 12, 13, 14};
            TestResult result = AnovaTest.oneWay(g1, g2);

            assertThat(result.isSignificant()).isTrue();
        }

        @Test
        @DisplayName("组内方差为零时所有组均值相同 → F=0")
        void zeroWithinGroupVarianceSameMeans() {
            double[] g1 = {5, 5, 5};
            double[] g2 = {5, 5, 5};
            TestResult result = AnovaTest.oneWay(g1, g2);

            assertThat(result.statistic()).isEqualTo(0.0);
            assertThat(result.pValue()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("组内方差为零但均值不同 → F=Infinity")
        void zeroWithinGroupVarianceDifferentMeans() {
            double[] g1 = {3, 3, 3};
            double[] g2 = {5, 5, 5};
            TestResult result = AnovaTest.oneWay(g1, g2);

            assertThat(result.statistic()).isEqualTo(Double.POSITIVE_INFINITY);
            assertThat(result.pValue()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("null groups 抛出 MathException")
        void nullGroupsThrows() {
            assertThatThrownBy(() -> AnovaTest.oneWay((double[][]) null))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("单组抛出 MathException")
        void singleGroupThrows() {
            assertThatThrownBy(() -> AnovaTest.oneWay(new double[]{1, 2, 3}))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("at least 2 groups");
        }

        @Test
        @DisplayName("组内元素不足抛出 MathException")
        void tooFewElementsThrows() {
            assertThatThrownBy(() -> AnovaTest.oneWay(new double[]{1}, new double[]{2, 3}))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("at least 2 elements");
        }

        @Test
        @DisplayName("包含 NaN 抛出 MathException")
        void nanThrows() {
            assertThatThrownBy(() -> AnovaTest.oneWay(
                    new double[]{1, Double.NaN, 3},
                    new double[]{4, 5, 6}))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("not finite");
        }

        @Test
        @DisplayName("null 组抛出 MathException")
        void nullGroupThrows() {
            assertThatThrownBy(() -> AnovaTest.oneWay(new double[]{1, 2}, null))
                    .isInstanceOf(MathException.class);
        }
    }
}
