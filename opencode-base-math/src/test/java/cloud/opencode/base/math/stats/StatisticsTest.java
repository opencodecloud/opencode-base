package cloud.opencode.base.math.stats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Comprehensive tests for {@link Statistics}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("Statistics 统计工具测试")
class StatisticsTest {

    // ==================== Percentile Tests ====================

    @Nested
    @DisplayName("percentile 百分位数")
    class PercentileTests {

        @Test
        @DisplayName("计算中位数（偶数个元素）")
        void medianEvenElements() {
            double[] data = {1, 2, 3, 4};
            assertThat(Statistics.percentile(data, 50)).isCloseTo(2.5, within(1e-10));
        }

        @Test
        @DisplayName("计算中位数（奇数个元素）")
        void medianOddElements() {
            double[] data = {1, 2, 3, 4, 5};
            assertThat(Statistics.percentile(data, 50)).isCloseTo(3.0, within(1e-10));
        }

        @Test
        @DisplayName("第0百分位数等于最小值")
        void zerothPercentileIsMin() {
            double[] data = {5, 3, 1, 4, 2};
            assertThat(Statistics.percentile(data, 0)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("第100百分位数等于最大值")
        void hundredthPercentileIsMax() {
            double[] data = {5, 3, 1, 4, 2};
            assertThat(Statistics.percentile(data, 100)).isCloseTo(5.0, within(1e-10));
        }

        @Test
        @DisplayName("单元素数组返回该元素")
        void singleElement() {
            assertThat(Statistics.percentile(new double[]{42}, 50)).isCloseTo(42.0, within(1e-10));
        }

        @Test
        @DisplayName("线性插值计算第25百分位数")
        void linearInterpolationQ1() {
            double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            // rank = 0.25 * 9 = 2.25 -> sorted[2] + 0.25*(sorted[3]-sorted[2]) = 3 + 0.25 = 3.25
            assertThat(Statistics.percentile(data, 25)).isCloseTo(3.25, within(1e-10));
        }

        @Test
        @DisplayName("null数组抛出异常")
        void nullArray() {
            assertThatThrownBy(() -> Statistics.percentile(null, 50))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空数组抛出异常")
        void emptyArray() {
            assertThatThrownBy(() -> Statistics.percentile(new double[]{}, 50))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("p超出范围抛出异常")
        void outOfRange() {
            assertThatThrownBy(() -> Statistics.percentile(new double[]{1}, -1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Statistics.percentile(new double[]{1}, 101))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("不修改原始数组")
        void doesNotMutateInput() {
            double[] data = {5, 3, 1, 4, 2};
            double[] copy = data.clone();
            Statistics.percentile(data, 50);
            assertThat(data).containsExactly(copy);
        }
    }

    // ==================== Mode Tests ====================

    @Nested
    @DisplayName("mode 众数")
    class ModeTests {

        @Test
        @DisplayName("单一众数")
        void singleMode() {
            double[] data = {1, 2, 2, 3};
            assertThat(Statistics.mode(data)).containsExactly(2.0);
        }

        @Test
        @DisplayName("多个众数")
        void multipleModes() {
            double[] data = {1, 1, 2, 2, 3};
            assertThat(Statistics.mode(data)).containsExactly(1.0, 2.0);
        }

        @Test
        @DisplayName("所有元素相同")
        void allSame() {
            double[] data = {7, 7, 7};
            assertThat(Statistics.mode(data)).containsExactly(7.0);
        }

        @Test
        @DisplayName("所有元素不同（全部都是众数）")
        void allUnique() {
            double[] data = {1, 2, 3};
            assertThat(Statistics.mode(data)).containsExactly(1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("单元素数组")
        void singleElement() {
            assertThat(Statistics.mode(new double[]{42})).containsExactly(42.0);
        }

        @Test
        @DisplayName("null数组抛出异常")
        void nullArray() {
            assertThatThrownBy(() -> Statistics.mode(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("包含负数")
        void negativeValues() {
            double[] data = {-1, -1, 2, 3};
            assertThat(Statistics.mode(data)).containsExactly(-1.0);
        }
    }

    // ==================== Skewness Tests ====================

    @Nested
    @DisplayName("skewness 偏度")
    class SkewnessTests {

        @Test
        @DisplayName("对称分布偏度接近0")
        void symmetricDistribution() {
            double[] data = {1, 2, 3, 4, 5};
            assertThat(Statistics.skewness(data)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("右偏分布偏度为正")
        void rightSkewed() {
            double[] data = {1, 1, 1, 1, 10};
            assertThat(Statistics.skewness(data)).isGreaterThan(0);
        }

        @Test
        @DisplayName("左偏分布偏度为负")
        void leftSkewed() {
            double[] data = {1, 10, 10, 10, 10};
            assertThat(Statistics.skewness(data)).isLessThan(0);
        }

        @Test
        @DisplayName("所有值相同偏度为0")
        void allSameValues() {
            double[] data = {5, 5, 5, 5, 5};
            assertThat(Statistics.skewness(data)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("少于3个数据点抛出异常")
        void tooFewElements() {
            assertThatThrownBy(() -> Statistics.skewness(new double[]{1, 2}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null数组抛出异常")
        void nullArray() {
            assertThatThrownBy(() -> Statistics.skewness(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Kurtosis Tests ====================

    @Nested
    @DisplayName("kurtosis 峰度")
    class KurtosisTests {

        @Test
        @DisplayName("均匀分布超额峰度为负")
        void uniformDistribution() {
            // Uniform data should have negative excess kurtosis
            double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            assertThat(Statistics.kurtosis(data)).isLessThan(0);
        }

        @Test
        @DisplayName("所有值相同峰度为0")
        void allSameValues() {
            double[] data = {3, 3, 3, 3, 3};
            assertThat(Statistics.kurtosis(data)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("尖峰分布超额峰度为正")
        void leptokurtic() {
            // Data with heavy tails
            double[] data = {0, 0, 0, 0, 0, 0, 0, 0, 0, 100};
            assertThat(Statistics.kurtosis(data)).isGreaterThan(0);
        }

        @Test
        @DisplayName("少于4个数据点抛出异常")
        void tooFewElements() {
            assertThatThrownBy(() -> Statistics.kurtosis(new double[]{1, 2, 3}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null数组抛出异常")
        void nullArray() {
            assertThatThrownBy(() -> Statistics.kurtosis(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Covariance Tests ====================

    @Nested
    @DisplayName("covariance 协方差")
    class CovarianceTests {

        @Test
        @DisplayName("完全正相关")
        void perfectPositive() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {2, 4, 6, 8, 10};
            assertThat(Statistics.covariance(x, y)).isCloseTo(5.0, within(1e-10));
        }

        @Test
        @DisplayName("完全负相关")
        void perfectNegative() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {10, 8, 6, 4, 2};
            assertThat(Statistics.covariance(x, y)).isCloseTo(-5.0, within(1e-10));
        }

        @Test
        @DisplayName("独立变量协方差接近0")
        void independentVariables() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {5, 5, 5, 5, 5};
            assertThat(Statistics.covariance(x, y)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("数组长度不同抛出异常")
        void differentLengths() {
            assertThatThrownBy(() -> Statistics.covariance(new double[]{1, 2}, new double[]{1}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("少于2个数据点抛出异常")
        void tooFewElements() {
            assertThatThrownBy(() -> Statistics.covariance(new double[]{1}, new double[]{2}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null数组抛出异常")
        void nullArray() {
            assertThatThrownBy(() -> Statistics.covariance(null, new double[]{1, 2}))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Statistics.covariance(new double[]{1, 2}, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Correlation Tests ====================

    @Nested
    @DisplayName("correlation 相关系数")
    class CorrelationTests {

        @Test
        @DisplayName("完全正相关等于1")
        void perfectPositive() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {2, 4, 6, 8, 10};
            assertThat(Statistics.correlation(x, y)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("完全负相关等于-1")
        void perfectNegative() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {10, 8, 6, 4, 2};
            assertThat(Statistics.correlation(x, y)).isCloseTo(-1.0, within(1e-10));
        }

        @Test
        @DisplayName("常数y相关系数为0")
        void constantY() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {5, 5, 5, 5, 5};
            assertThat(Statistics.correlation(x, y)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("相关系数在[-1, 1]范围内")
        void withinBounds() {
            double[] x = {1, 3, 5, 7, 9};
            double[] y = {2, 1, 8, 3, 10};
            double r = Statistics.correlation(x, y);
            assertThat(r).isBetween(-1.0, 1.0);
        }

        @Test
        @DisplayName("数组长度不同抛出异常")
        void differentLengths() {
            assertThatThrownBy(() -> Statistics.correlation(new double[]{1, 2, 3}, new double[]{1, 2}))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Weighted Mean Tests ====================

    @Nested
    @DisplayName("weightedMean 加权平均")
    class WeightedMeanTests {

        @Test
        @DisplayName("等权重退化为算术平均")
        void equalWeights() {
            double[] values = {1, 2, 3, 4, 5};
            double[] weights = {1, 1, 1, 1, 1};
            assertThat(Statistics.weightedMean(values, weights)).isCloseTo(3.0, within(1e-10));
        }

        @Test
        @DisplayName("不等权重")
        void unequalWeights() {
            double[] values = {10, 20};
            double[] weights = {1, 3};
            // (10*1 + 20*3) / (1+3) = 70/4 = 17.5
            assertThat(Statistics.weightedMean(values, weights)).isCloseTo(17.5, within(1e-10));
        }

        @Test
        @DisplayName("零权重被忽略")
        void zeroWeights() {
            double[] values = {10, 20, 999};
            double[] weights = {1, 1, 0};
            assertThat(Statistics.weightedMean(values, weights)).isCloseTo(15.0, within(1e-10));
        }

        @Test
        @DisplayName("负权重抛出异常")
        void negativeWeight() {
            assertThatThrownBy(() -> Statistics.weightedMean(new double[]{1, 2}, new double[]{1, -1}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("全零权重抛出异常")
        void allZeroWeights() {
            assertThatThrownBy(() -> Statistics.weightedMean(new double[]{1, 2}, new double[]{0, 0}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("数组长度不同抛出异常")
        void differentLengths() {
            assertThatThrownBy(() -> Statistics.weightedMean(new double[]{1}, new double[]{1, 2}))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Geometric Mean Tests ====================

    @Nested
    @DisplayName("geometricMean 几何平均")
    class GeometricMeanTests {

        @Test
        @DisplayName("已知值")
        void knownValues() {
            double[] values = {2, 8};
            // sqrt(2*8) = sqrt(16) = 4
            assertThat(Statistics.geometricMean(values)).isCloseTo(4.0, within(1e-10));
        }

        @Test
        @DisplayName("所有值相同")
        void allSame() {
            double[] values = {5, 5, 5};
            assertThat(Statistics.geometricMean(values)).isCloseTo(5.0, within(1e-10));
        }

        @Test
        @DisplayName("单元素")
        void singleElement() {
            assertThat(Statistics.geometricMean(new double[]{7})).isCloseTo(7.0, within(1e-10));
        }

        @Test
        @DisplayName("三个值")
        void threeValues() {
            double[] values = {1, 2, 4};
            // (1*2*4)^(1/3) = 8^(1/3) = 2
            assertThat(Statistics.geometricMean(values)).isCloseTo(2.0, within(1e-10));
        }

        @Test
        @DisplayName("零值抛出异常")
        void zeroValue() {
            assertThatThrownBy(() -> Statistics.geometricMean(new double[]{1, 0, 3}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负值抛出异常")
        void negativeValue() {
            assertThatThrownBy(() -> Statistics.geometricMean(new double[]{1, -2, 3}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null数组抛出异常")
        void nullArray() {
            assertThatThrownBy(() -> Statistics.geometricMean(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Harmonic Mean Tests ====================

    @Nested
    @DisplayName("harmonicMean 调和平均")
    class HarmonicMeanTests {

        @Test
        @DisplayName("已知值")
        void knownValues() {
            double[] values = {1, 4, 4};
            // 3 / (1/1 + 1/4 + 1/4) = 3 / 1.5 = 2
            assertThat(Statistics.harmonicMean(values)).isCloseTo(2.0, within(1e-10));
        }

        @Test
        @DisplayName("所有值相同")
        void allSame() {
            double[] values = {6, 6, 6};
            assertThat(Statistics.harmonicMean(values)).isCloseTo(6.0, within(1e-10));
        }

        @Test
        @DisplayName("单元素")
        void singleElement() {
            assertThat(Statistics.harmonicMean(new double[]{3})).isCloseTo(3.0, within(1e-10));
        }

        @Test
        @DisplayName("调和平均小于等于几何平均")
        void harmonicLeGeometric() {
            double[] values = {1, 2, 3, 4, 5};
            double hm = Statistics.harmonicMean(values);
            double gm = Statistics.geometricMean(values);
            assertThat(hm).isLessThanOrEqualTo(gm + 1e-10);
        }

        @Test
        @DisplayName("零值抛出异常")
        void zeroValue() {
            assertThatThrownBy(() -> Statistics.harmonicMean(new double[]{1, 0, 3}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负值抛出异常")
        void negativeValue() {
            assertThatThrownBy(() -> Statistics.harmonicMean(new double[]{1, -2, 3}))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Range Tests ====================

    @Nested
    @DisplayName("range 极差")
    class RangeTests {

        @Test
        @DisplayName("正常范围")
        void normalRange() {
            double[] data = {3, 1, 7, 2, 9};
            assertThat(Statistics.range(data)).isCloseTo(8.0, within(1e-10));
        }

        @Test
        @DisplayName("所有值相同极差为0")
        void allSame() {
            assertThat(Statistics.range(new double[]{4, 4, 4})).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("单元素极差为0")
        void singleElement() {
            assertThat(Statistics.range(new double[]{42})).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("负数范围")
        void negativeValues() {
            double[] data = {-10, -3, -7};
            assertThat(Statistics.range(data)).isCloseTo(7.0, within(1e-10));
        }

        @Test
        @DisplayName("null数组抛出异常")
        void nullArray() {
            assertThatThrownBy(() -> Statistics.range(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空数组抛出异常")
        void emptyArray() {
            assertThatThrownBy(() -> Statistics.range(new double[]{}))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Interquartile Range Tests ====================

    @Nested
    @DisplayName("interquartileRange 四分位距")
    class InterquartileRangeTests {

        @Test
        @DisplayName("已知数据集")
        void knownDataset() {
            double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            double q1 = Statistics.percentile(data, 25);
            double q3 = Statistics.percentile(data, 75);
            assertThat(Statistics.interquartileRange(data)).isCloseTo(q3 - q1, within(1e-10));
        }

        @Test
        @DisplayName("所有值相同四分位距为0")
        void allSame() {
            assertThat(Statistics.interquartileRange(new double[]{5, 5, 5, 5})).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("单元素四分位距为0")
        void singleElement() {
            assertThat(Statistics.interquartileRange(new double[]{42})).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("null数组抛出异常")
        void nullArray() {
            assertThatThrownBy(() -> Statistics.interquartileRange(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Large Array Tests ====================

    @Nested
    @DisplayName("大数组测试")
    class LargeArrayTests {

        @Test
        @DisplayName("大数组百分位数计算")
        void largeArrayPercentile() {
            double[] data = IntStream.rangeClosed(1, 10000).asDoubleStream().toArray();
            assertThat(Statistics.percentile(data, 50)).isCloseTo(5000.5, within(1e-10));
        }

        @Test
        @DisplayName("大数组极差")
        void largeArrayRange() {
            double[] data = IntStream.rangeClosed(1, 10000).asDoubleStream().toArray();
            assertThat(Statistics.range(data)).isCloseTo(9999.0, within(1e-10));
        }

        @Test
        @DisplayName("大数组相关系数")
        void largeArrayCorrelation() {
            double[] x = IntStream.rangeClosed(1, 1000).asDoubleStream().toArray();
            double[] y = new double[1000];
            for (int i = 0; i < 1000; i++) {
                y[i] = 2 * x[i] + 3;
            }
            assertThat(Statistics.correlation(x, y)).isCloseTo(1.0, within(1e-10));
        }
    }
}
