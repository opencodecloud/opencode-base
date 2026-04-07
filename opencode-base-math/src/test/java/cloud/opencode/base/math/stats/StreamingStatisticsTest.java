package cloud.opencode.base.math.stats;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Comprehensive tests for {@link StreamingStatistics}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("StreamingStatistics 流式统计测试")
class StreamingStatisticsTest {

    @Nested
    @DisplayName("基本统计量计算")
    class BasicStatisticsTests {

        @Test
        @DisplayName("1..100 的均值为 50.5")
        void meanOf1To100() {
            StreamingStatistics stats = StreamingStatistics.create();
            for (int i = 1; i <= 100; i++) {
                stats.add(i);
            }
            assertThat(stats.count()).isEqualTo(100);
            assertThat(stats.mean()).isCloseTo(50.5, within(1e-10));
        }

        @Test
        @DisplayName("1..100 的总和为 5050")
        void sumOf1To100() {
            StreamingStatistics stats = StreamingStatistics.create();
            for (int i = 1; i <= 100; i++) {
                stats.add(i);
            }
            assertThat(stats.sum()).isCloseTo(5050.0, within(1e-10));
        }

        @Test
        @DisplayName("1..100 的最小值和最大值")
        void minMaxOf1To100() {
            StreamingStatistics stats = StreamingStatistics.create();
            for (int i = 1; i <= 100; i++) {
                stats.add(i);
            }
            assertThat(stats.min()).isCloseTo(1.0, within(1e-10));
            assertThat(stats.max()).isCloseTo(100.0, within(1e-10));
        }

        @Test
        @DisplayName("1..100 的总体方差")
        void varianceOf1To100() {
            StreamingStatistics stats = StreamingStatistics.create();
            for (int i = 1; i <= 100; i++) {
                stats.add(i);
            }
            // Population variance of 1..100 = (100^2 - 1) / 12 = 9999/12 = 833.25
            assertThat(stats.variance()).isCloseTo(833.25, within(1e-10));
        }

        @Test
        @DisplayName("1..100 的样本方差")
        void sampleVarianceOf1To100() {
            StreamingStatistics stats = StreamingStatistics.create();
            for (int i = 1; i <= 100; i++) {
                stats.add(i);
            }
            // Sample variance = n/(n-1) * population_variance = 100/99 * 833.25 = 841.6666...
            assertThat(stats.sampleVariance()).isCloseTo(841.6666666666666, within(1e-8));
        }

        @Test
        @DisplayName("标准差计算")
        void stdDevComputation() {
            StreamingStatistics stats = StreamingStatistics.create();
            for (int i = 1; i <= 100; i++) {
                stats.add(i);
            }
            assertThat(stats.stdDev()).isCloseTo(Math.sqrt(833.25), within(1e-8));
            assertThat(stats.sampleStdDev()).isCloseTo(Math.sqrt(841.6666666666666), within(1e-8));
        }

        @Test
        @DisplayName("单个值的统计量")
        void singleValue() {
            StreamingStatistics stats = StreamingStatistics.create();
            stats.add(42.0);

            assertThat(stats.count()).isEqualTo(1);
            assertThat(stats.mean()).isCloseTo(42.0, within(1e-10));
            assertThat(stats.variance()).isCloseTo(0.0, within(1e-10));
            assertThat(stats.min()).isCloseTo(42.0, within(1e-10));
            assertThat(stats.max()).isCloseTo(42.0, within(1e-10));
            assertThat(stats.sum()).isCloseTo(42.0, within(1e-10));
        }

        @Test
        @DisplayName("两个值的样本方差")
        void twoValues() {
            StreamingStatistics stats = StreamingStatistics.create();
            stats.add(10.0);
            stats.add(20.0);

            assertThat(stats.mean()).isCloseTo(15.0, within(1e-10));
            assertThat(stats.sampleVariance()).isCloseTo(50.0, within(1e-10));
        }

        @Test
        @DisplayName("负数和零的统计量")
        void negativeAndZero() {
            StreamingStatistics stats = StreamingStatistics.create();
            stats.add(-5.0);
            stats.add(0.0);
            stats.add(5.0);

            assertThat(stats.mean()).isCloseTo(0.0, within(1e-10));
            assertThat(stats.min()).isCloseTo(-5.0, within(1e-10));
            assertThat(stats.max()).isCloseTo(5.0, within(1e-10));
        }
    }

    @Nested
    @DisplayName("merge 合并操作")
    class MergeTests {

        @Test
        @DisplayName("合并两个累加器得到与单个累加器相同的结果")
        void mergeEquivalentToSingleAccumulator() {
            // Single accumulator with 1..100
            StreamingStatistics single = StreamingStatistics.create();
            for (int i = 1; i <= 100; i++) {
                single.add(i);
            }

            // Split into two halves and merge
            StreamingStatistics first = StreamingStatistics.create();
            for (int i = 1; i <= 50; i++) {
                first.add(i);
            }
            StreamingStatistics second = StreamingStatistics.create();
            for (int i = 51; i <= 100; i++) {
                second.add(i);
            }

            StreamingStatistics merged = first.merge(second);

            assertThat(merged.count()).isEqualTo(single.count());
            assertThat(merged.mean()).isCloseTo(single.mean(), within(1e-10));
            assertThat(merged.variance()).isCloseTo(single.variance(), within(1e-8));
            assertThat(merged.sampleVariance()).isCloseTo(single.sampleVariance(), within(1e-8));
            assertThat(merged.min()).isCloseTo(single.min(), within(1e-10));
            assertThat(merged.max()).isCloseTo(single.max(), within(1e-10));
            assertThat(merged.sum()).isCloseTo(single.sum(), within(1e-10));
        }

        @Test
        @DisplayName("合并后原始实例保持不变")
        void mergeDoesNotModifyOriginals() {
            StreamingStatistics a = StreamingStatistics.create();
            a.add(1.0);
            a.add(2.0);

            StreamingStatistics b = StreamingStatistics.create();
            b.add(3.0);
            b.add(4.0);

            StreamingStatistics merged = a.merge(b);

            // Originals unchanged
            assertThat(a.count()).isEqualTo(2);
            assertThat(a.mean()).isCloseTo(1.5, within(1e-10));
            assertThat(b.count()).isEqualTo(2);
            assertThat(b.mean()).isCloseTo(3.5, within(1e-10));

            // Merged has all 4
            assertThat(merged.count()).isEqualTo(4);
            assertThat(merged.mean()).isCloseTo(2.5, within(1e-10));
        }

        @Test
        @DisplayName("合并空累加器")
        void mergeWithEmpty() {
            StreamingStatistics stats = StreamingStatistics.create();
            stats.add(1.0);
            stats.add(2.0);

            StreamingStatistics empty = StreamingStatistics.create();

            StreamingStatistics result1 = stats.merge(empty);
            assertThat(result1.count()).isEqualTo(2);
            assertThat(result1.mean()).isCloseTo(1.5, within(1e-10));

            StreamingStatistics result2 = empty.merge(stats);
            assertThat(result2.count()).isEqualTo(2);
            assertThat(result2.mean()).isCloseTo(1.5, within(1e-10));
        }

        @Test
        @DisplayName("合并 null 抛出 MathException")
        void mergeNullThrows() {
            StreamingStatistics stats = StreamingStatistics.create();
            assertThatThrownBy(() -> stats.merge(null))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("reset 重置操作")
    class ResetTests {

        @Test
        @DisplayName("重置后状态为空")
        void resetClearsState() {
            StreamingStatistics stats = StreamingStatistics.create();
            stats.add(1.0);
            stats.add(2.0);
            stats.add(3.0);

            stats.reset();

            assertThat(stats.count()).isEqualTo(0);
            assertThatThrownBy(stats::mean).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("重置后可以重新添加值")
        void canAddAfterReset() {
            StreamingStatistics stats = StreamingStatistics.create();
            stats.add(100.0);
            stats.reset();
            stats.add(42.0);

            assertThat(stats.count()).isEqualTo(1);
            assertThat(stats.mean()).isCloseTo(42.0, within(1e-10));
        }
    }

    @Nested
    @DisplayName("collector Stream 收集器")
    class CollectorTests {

        @Test
        @DisplayName("使用 Stream collector 计算统计量")
        void streamCollector() {
            StreamingStatistics stats = IntStream.rangeClosed(1, 100)
                    .mapToObj(i -> (double) i)
                    .collect(StreamingStatistics.collector());

            assertThat(stats.count()).isEqualTo(100);
            assertThat(stats.mean()).isCloseTo(50.5, within(1e-10));
            assertThat(stats.min()).isCloseTo(1.0, within(1e-10));
            assertThat(stats.max()).isCloseTo(100.0, within(1e-10));
        }

        @Test
        @DisplayName("并行 Stream collector 得到正确结果")
        void parallelStreamCollector() {
            StreamingStatistics stats = IntStream.rangeClosed(1, 1000)
                    .parallel()
                    .mapToObj(i -> (double) i)
                    .collect(StreamingStatistics.collector());

            assertThat(stats.count()).isEqualTo(1000);
            assertThat(stats.mean()).isCloseTo(500.5, within(1e-10));
            assertThat(stats.sum()).isCloseTo(500500.0, within(1e-6));
        }
    }

    @Nested
    @DisplayName("边界条件和错误处理")
    class EdgeCaseTests {

        @Test
        @DisplayName("空累加器调用 mean 抛出 MathException")
        void emptyMeanThrows() {
            StreamingStatistics stats = StreamingStatistics.create();
            assertThatThrownBy(stats::mean).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("空累加器调用 variance 抛出 MathException")
        void emptyVarianceThrows() {
            StreamingStatistics stats = StreamingStatistics.create();
            assertThatThrownBy(stats::variance).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("单个值调用 sampleVariance 抛出 MathException")
        void singleValueSampleVarianceThrows() {
            StreamingStatistics stats = StreamingStatistics.create();
            stats.add(1.0);
            assertThatThrownBy(stats::sampleVariance).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("空累加器调用 min 抛出 MathException")
        void emptyMinThrows() {
            StreamingStatistics stats = StreamingStatistics.create();
            assertThatThrownBy(stats::min).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("空累加器调用 max 抛出 MathException")
        void emptyMaxThrows() {
            StreamingStatistics stats = StreamingStatistics.create();
            assertThatThrownBy(stats::max).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("空累加器调用 sum 抛出 MathException")
        void emptySumThrows() {
            StreamingStatistics stats = StreamingStatistics.create();
            assertThatThrownBy(stats::sum).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("添加 NaN 抛出 MathException")
        void addNanThrows() {
            StreamingStatistics stats = StreamingStatistics.create();
            assertThatThrownBy(() -> stats.add(Double.NaN))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("添加正无穷抛出 MathException")
        void addPositiveInfinityThrows() {
            StreamingStatistics stats = StreamingStatistics.create();
            assertThatThrownBy(() -> stats.add(Double.POSITIVE_INFINITY))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("添加负无穷抛出 MathException")
        void addNegativeInfinityThrows() {
            StreamingStatistics stats = StreamingStatistics.create();
            assertThatThrownBy(() -> stats.add(Double.NEGATIVE_INFINITY))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("toString 字符串表示")
    class ToStringTests {

        @Test
        @DisplayName("空累加器的 toString")
        void emptyToString() {
            StreamingStatistics stats = StreamingStatistics.create();
            assertThat(stats.toString()).isEqualTo("StreamingStatistics{count=0}");
        }

        @Test
        @DisplayName("非空累加器的 toString 包含统计信息")
        void nonEmptyToString() {
            StreamingStatistics stats = StreamingStatistics.create();
            stats.add(1.0);
            stats.add(2.0);
            String s = stats.toString();
            assertThat(s).contains("count=2");
            assertThat(s).contains("mean=");
            assertThat(s).contains("min=");
            assertThat(s).contains("max=");
        }
    }

    @Nested
    @DisplayName("数值精度验证")
    class NumericalPrecisionTests {

        @Test
        @DisplayName("大量数据的 Welford 算法精度")
        void welfordPrecisionLargeDataSet() {
            StreamingStatistics stats = StreamingStatistics.create();
            // Add values near 1e8 to stress-test numerical stability
            double base = 1e8;
            int n = 10000;
            for (int i = 0; i < n; i++) {
                stats.add(base + i);
            }

            double expectedMean = base + (n - 1) / 2.0;
            assertThat(stats.mean()).isCloseTo(expectedMean, within(1e-4));

            // Variance of {0, 1, ..., n-1} = (n^2 - 1) / 12
            double expectedPopVar = ((long) n * n - 1) / 12.0;
            assertThat(stats.variance()).isCloseTo(expectedPopVar, within(1.0));
        }
    }
}
