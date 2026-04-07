package cloud.opencode.base.math.stats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for Spearman and Kendall correlation in {@link Statistics}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("Spearman & Kendall 非参数相关系数测试")
class SpearmanKendallTest {

    @Nested
    @DisplayName("spearmanCorrelation 斯皮尔曼等级相关")
    class SpearmanTests {

        @Test
        @DisplayName("完全单调递增关系：rho = 1.0")
        void perfectPositiveMonotonic() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {2, 4, 6, 8, 10};
            assertThat(Statistics.spearmanCorrelation(x, y)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("完全单调递减关系：rho = -1.0")
        void perfectNegativeMonotonic() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {10, 8, 6, 4, 2};
            assertThat(Statistics.spearmanCorrelation(x, y)).isCloseTo(-1.0, within(1e-10));
        }

        @Test
        @DisplayName("非线性单调关系仍然为 1.0")
        void nonLinearMonotonic() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {1, 4, 9, 16, 25}; // y = x^2, monotonically increasing
            assertThat(Statistics.spearmanCorrelation(x, y)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("含并列值的数据集")
        void withTies() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {5, 6, 7, 8, 7}; // tie in y: two 7s
            double rho = Statistics.spearmanCorrelation(x, y);
            assertThat(rho).isBetween(0.0, 1.0);
        }

        @Test
        @DisplayName("两个元素的最小数据集")
        void twoElements() {
            double[] x = {1, 2};
            double[] y = {3, 4};
            assertThat(Statistics.spearmanCorrelation(x, y)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("数组长度不同时抛出异常")
        void differentLengths() {
            assertThatThrownBy(() -> Statistics.spearmanCorrelation(new double[]{1, 2}, new double[]{1}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("same length");
        }

        @Test
        @DisplayName("少于 2 个元素时抛出异常")
        void tooFewElements() {
            assertThatThrownBy(() -> Statistics.spearmanCorrelation(new double[]{1}, new double[]{2}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least 2");
        }

        @Test
        @DisplayName("包含 NaN 时抛出异常")
        void containsNaN() {
            assertThatThrownBy(() -> Statistics.spearmanCorrelation(
                    new double[]{1, Double.NaN}, new double[]{1, 2}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not finite");
        }

        @Test
        @DisplayName("包含 Infinity 时抛出异常")
        void containsInfinity() {
            assertThatThrownBy(() -> Statistics.spearmanCorrelation(
                    new double[]{1, 2}, new double[]{1, Double.POSITIVE_INFINITY}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not finite");
        }
    }

    @Nested
    @DisplayName("kendallCorrelation 肯德尔 tau-b 相关")
    class KendallTests {

        @Test
        @DisplayName("完全一致排序：tau = 1.0")
        void perfectConcordance() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {2, 4, 6, 8, 10};
            assertThat(Statistics.kendallCorrelation(x, y)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("完全不一致排序：tau = -1.0")
        void perfectDiscordance() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {10, 8, 6, 4, 2};
            assertThat(Statistics.kendallCorrelation(x, y)).isCloseTo(-1.0, within(1e-10));
        }

        @Test
        @DisplayName("已知数据集的 tau-b 值")
        void knownDataset() {
            // Example: x = [1,2,3,4], y = [1,2,4,3]
            // Pairs: (1,2)C, (1,3)C, (1,4)C - wait, check y:
            // (i=0,j=1): dx>0,dy>0 -> C
            // (i=0,j=2): dx>0,dy>0 -> C
            // (i=0,j=3): dx>0,dy>0 -> C
            // (i=1,j=2): dx>0,dy>0 -> C
            // (i=1,j=3): dx>0,dy>0 -> C
            // (i=2,j=3): dx>0,dy<0 -> D
            // C=5, D=1, no ties -> tau = (5-1)/sqrt(6*6) = 4/6 = 2/3
            double[] x = {1, 2, 3, 4};
            double[] y = {1, 2, 4, 3};
            assertThat(Statistics.kendallCorrelation(x, y)).isCloseTo(2.0 / 3.0, within(1e-10));
        }

        @Test
        @DisplayName("含并列值的 tau-b")
        void withTies() {
            // x = [1,2,3], y = [1,1,2]
            // Pairs:
            // (0,1): dx>0, dy=0 -> tiedY
            // (0,2): dx>0, dy>0 -> C
            // (1,2): dx>0, dy>0 -> C
            // C=2, D=0, tiedX=0, tiedY=1
            // totalPairs=3, denomX=3-0=3, denomY=3-1=2
            // tau = (2-0)/sqrt(3*2) = 2/sqrt(6)
            double[] x = {1, 2, 3};
            double[] y = {1, 1, 2};
            double expected = 2.0 / Math.sqrt(6);
            assertThat(Statistics.kendallCorrelation(x, y)).isCloseTo(expected, within(1e-10));
        }

        @Test
        @DisplayName("所有值相同时 tau = 0")
        void allTied() {
            double[] x = {1, 1, 1};
            double[] y = {2, 2, 2};
            assertThat(Statistics.kendallCorrelation(x, y)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("两个元素的最小数据集")
        void twoElements() {
            double[] x = {1, 2};
            double[] y = {3, 4};
            assertThat(Statistics.kendallCorrelation(x, y)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("数组为 null 时抛出异常")
        void nullArray() {
            assertThatThrownBy(() -> Statistics.kendallCorrelation(null, new double[]{1, 2}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("少于 2 个元素时抛出异常")
        void tooFewElements() {
            assertThatThrownBy(() -> Statistics.kendallCorrelation(new double[]{1}, new double[]{2}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least 2");
        }
    }
}
