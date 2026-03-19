package cloud.opencode.base.timeseries.analysis;

import cloud.opencode.base.timeseries.TimeSeries;
import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * CorrelationUtilTest Tests
 * CorrelationUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("CorrelationUtil Tests")
class CorrelationUtilTest {

    private TimeSeries series1;
    private TimeSeries series2;
    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.parse("2024-01-01T00:00:00Z");
        series1 = new TimeSeries("series1");
        series2 = new TimeSeries("series2");

        // Create two correlated series
        for (int i = 0; i < 100; i++) {
            series1.add(baseTime.plusSeconds(i), i * 1.0);
            series2.add(baseTime.plusSeconds(i), i * 2.0 + 10);  // Perfectly correlated (scaled + offset)
        }
    }

    @Nested
    @DisplayName("Pearson Correlation Tests")
    class PearsonCorrelationTests {

        @Test
        @DisplayName("pearson should return 1.0 for perfectly correlated series")
        void pearsonShouldReturn1ForPerfectlyCorrelatedSeries() {
            double correlation = CorrelationUtil.pearson(series1, series2);

            assertThat(correlation).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("pearson should return -1.0 for negatively correlated series")
        void pearsonShouldReturnMinus1ForNegativelyCorrelatedSeries() {
            TimeSeries negative = new TimeSeries("negative");
            for (int i = 0; i < 100; i++) {
                negative.add(baseTime.plusSeconds(i), -i * 1.0);
            }

            double correlation = CorrelationUtil.pearson(series1, negative);

            assertThat(correlation).isCloseTo(-1.0, within(0.01));
        }

        @Test
        @DisplayName("pearson with arrays should work")
        void pearsonWithArraysShouldWork() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {2, 4, 6, 8, 10};

            double correlation = CorrelationUtil.pearson(x, y);

            assertThat(correlation).isCloseTo(1.0, within(0.01));
        }
    }

    @Nested
    @DisplayName("Spearman Correlation Tests")
    class SpearmanCorrelationTests {

        @Test
        @DisplayName("spearman should return 1.0 for monotonically increasing series")
        void spearmanShouldReturn1ForMonotonicallyIncreasingSeries() {
            double correlation = CorrelationUtil.spearman(series1, series2);

            assertThat(correlation).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("spearman should return -1.0 for monotonically decreasing relationship")
        void spearmanShouldReturnMinus1ForMonotonicallyDecreasingRelationship() {
            TimeSeries decreasing = new TimeSeries("decreasing");
            for (int i = 0; i < 100; i++) {
                decreasing.add(baseTime.plusSeconds(i), 100 - i);
            }

            double correlation = CorrelationUtil.spearman(series1, decreasing);

            assertThat(correlation).isCloseTo(-1.0, within(0.01));
        }
    }

    @Nested
    @DisplayName("Cross Correlation Tests")
    class CrossCorrelationTests {

        @Test
        @DisplayName("crossCorrelation should find lag with highest correlation")
        void crossCorrelationShouldFindLagWithHighestCorrelation() {
            // Create lagged series
            TimeSeries lagged = new TimeSeries("lagged");
            for (int i = 0; i < 100; i++) {
                lagged.add(baseTime.plusSeconds(i), i < 5 ? 0.0 : (i - 5) * 1.0);
            }

            CorrelationUtil.CrossCorrelationResult result = CorrelationUtil.crossCorrelation(series1, lagged, 10);

            assertThat(result.correlations()).isNotEmpty();
            assertThat(result.bestLag()).isGreaterThanOrEqualTo(-10);
            assertThat(result.bestLag()).isLessThanOrEqualTo(10);
        }

        @Test
        @DisplayName("crossCorrelation should return result with correlation values")
        void crossCorrelationShouldReturnResultWithCorrelationValues() {
            CorrelationUtil.CrossCorrelationResult result = CorrelationUtil.crossCorrelation(series1, series2, 5);

            assertThat(result.correlations()).hasSize(11); // -5 to +5
            assertThat(result.bestCorrelation()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Autocorrelation Tests")
    class AutocorrelationTests {

        @Test
        @DisplayName("autocorrelation should return 1.0 at lag 0")
        void autocorrelationShouldReturn1AtLag0() {
            double[] acf = CorrelationUtil.autocorrelation(series1, 10);

            assertThat(acf[0]).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("autocorrelation should return correlations for each lag")
        void autocorrelationShouldReturnCorrelationsForEachLag() {
            double[] acf = CorrelationUtil.autocorrelation(series1, 10);

            assertThat(acf).hasSize(11); // 0 to 10
        }
    }

    @Nested
    @DisplayName("Partial Autocorrelation Tests")
    class PartialAutocorrelationTests {

        @Test
        @DisplayName("partialAutocorrelation should return PACF values")
        void partialAutocorrelationShouldReturnPacfValues() {
            double[] pacf = CorrelationUtil.partialAutocorrelation(series1, 10);

            assertThat(pacf).isNotEmpty();
            assertThat(pacf.length).isLessThanOrEqualTo(11);
        }
    }

    @Nested
    @DisplayName("Find Optimal Lag Tests")
    class FindOptimalLagTests {

        @Test
        @DisplayName("findOptimalLag should find lag with best correlation")
        void findOptimalLagShouldFindLagWithBestCorrelation() {
            int optimalLag = CorrelationUtil.findOptimalLag(series1, series2, 10);

            assertThat(optimalLag).isBetween(-10, 10);
        }
    }

    @Nested
    @DisplayName("Correlation At Lag Tests")
    class CorrelationAtLagTests {

        @Test
        @DisplayName("correlationAtLag should calculate correlation at specific lag")
        void correlationAtLagShouldCalculateCorrelationAtSpecificLag() {
            double correlation = CorrelationUtil.correlationAtLag(series1, series2, 0);

            assertThat(correlation).isCloseTo(1.0, within(0.01));
        }
    }

    @Nested
    @DisplayName("Rolling Correlation Tests")
    class RollingCorrelationTests {

        @Test
        @DisplayName("rollingCorrelation should calculate correlation over windows")
        void rollingCorrelationShouldCalculateCorrelationOverWindows() {
            double[] rolling = CorrelationUtil.rollingCorrelation(series1, series2, 20);

            assertThat(rolling).isNotEmpty();
            assertThat(rolling.length).isLessThanOrEqualTo(series1.size() - 19);
        }

        @Test
        @DisplayName("rollingCorrelation should show stable correlation for correlated series")
        void rollingCorrelationShouldShowStableCorrelationForCorrelatedSeries() {
            double[] rolling = CorrelationUtil.rollingCorrelation(series1, series2, 20);

            // All rolling correlations should be close to 1.0 for perfectly correlated series
            for (double corr : rolling) {
                assertThat(corr).isCloseTo(1.0, within(0.1));
            }
        }
    }

    @Nested
    @DisplayName("CrossCorrelationResult Tests")
    class CrossCorrelationResultTests {

        @Test
        @DisplayName("CrossCorrelationResult should contain all fields")
        void crossCorrelationResultShouldContainAllFields() {
            CorrelationUtil.CrossCorrelationResult result = CorrelationUtil.crossCorrelation(series1, series2, 5);

            assertThat(result.correlations()).isNotNull();
            assertThat(result.lags()).isNotNull();
            assertThat(result.bestLag()).isNotNull();
            assertThat(result.bestCorrelation()).isNotNull();
        }

        @Test
        @DisplayName("CrossCorrelationResult correlationAt should work")
        void crossCorrelationResultCorrelationAtShouldWork() {
            CorrelationUtil.CrossCorrelationResult result = CorrelationUtil.crossCorrelation(series1, series2, 5);

            double corrAtZero = result.correlationAt(0);
            assertThat(corrAtZero).isNotNaN();
        }

        @Test
        @DisplayName("CrossCorrelationResult isSignificant should work")
        void crossCorrelationResultIsSignificantShouldWork() {
            CorrelationUtil.CrossCorrelationResult result = CorrelationUtil.crossCorrelation(series1, series2, 5);

            boolean significant = result.isSignificant(100);
            assertThat(significant).isTrue(); // Should be significant for perfectly correlated series
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle series with constant values")
        void shouldHandleSeriesWithConstantValues() {
            TimeSeries constant = new TimeSeries("constant");
            for (int i = 0; i < 10; i++) {
                constant.add(baseTime.plusSeconds(i), 5.0);
            }

            // Correlation with constant series should be 0 (no variance)
            double correlation = CorrelationUtil.pearson(series1, constant);

            assertThat(correlation).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should handle small series")
        void shouldHandleSmallSeries() {
            TimeSeries small1 = new TimeSeries("small1");
            TimeSeries small2 = new TimeSeries("small2");
            small1.add(baseTime, 1.0);
            small1.add(baseTime.plusSeconds(1), 2.0);
            small2.add(baseTime, 2.0);
            small2.add(baseTime.plusSeconds(1), 4.0);

            double correlation = CorrelationUtil.pearson(small1, small2);

            assertThat(correlation).isCloseTo(1.0, within(0.01));
        }
    }
}
