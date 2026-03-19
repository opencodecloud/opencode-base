package cloud.opencode.base.timeseries.forecast;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * ForecastUtil Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
class ForecastUtilTest {

    private TimeSeries createTestSeries() {
        TimeSeries series = new TimeSeries("test");
        Instant base = Instant.parse("2024-01-01T00:00:00Z");
        for (int i = 0; i < 30; i++) {
            series.add(DataPoint.of(base.plusSeconds(i * 60), 10 + i * 0.5));
        }
        return series;
    }

    private TimeSeries createTrendSeries() {
        TimeSeries series = new TimeSeries("trend");
        Instant base = Instant.parse("2024-01-01T00:00:00Z");
        for (int i = 0; i < 20; i++) {
            series.add(DataPoint.of(base.plusSeconds(i * 60), 100 + i * 5));
        }
        return series;
    }

    @Nested
    @DisplayName("SMA Forecast Tests")
    class SmaForecastTests {

        @Test
        void shouldForecastWithSma() {
            TimeSeries series = createTestSeries();

            TimeSeries forecast = ForecastUtil.smaForecast(series, 5, 3);

            assertThat(forecast.size()).isEqualTo(3);
            assertThat(forecast.getPoints()).allMatch(p -> !Double.isNaN(p.value()));
        }

        @Test
        void shouldReturnEmptyForSmallSeries() {
            TimeSeries series = new TimeSeries("small");
            series.add(DataPoint.of(Instant.now(), 1.0));

            TimeSeries forecast = ForecastUtil.smaForecast(series, 5, 3);

            assertThat(forecast.isEmpty()).isTrue();
        }

        @Test
        void shouldRejectInvalidWindowSize() {
            TimeSeries series = createTestSeries();

            assertThatThrownBy(() -> ForecastUtil.smaForecast(series, 0, 3))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectInvalidSteps() {
            TimeSeries series = createTestSeries();

            assertThatThrownBy(() -> ForecastUtil.smaForecast(series, 5, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("SMA Forecast with Bounds Tests")
    class SmaForecastWithBoundsTests {

        @Test
        void shouldForecastWithBounds() {
            TimeSeries series = createTestSeries();

            ForecastUtil.ForecastResult result =
                    ForecastUtil.smaForecastWithBounds(series, 5, 3, 0.95);

            assertThat(result.isEmpty()).isFalse();
            assertThat(result.size()).isEqualTo(3);
            assertThat(result.forecast().size()).isEqualTo(3);
            assertThat(result.lower().size()).isEqualTo(3);
            assertThat(result.upper().size()).isEqualTo(3);
            assertThat(result.confidenceLevel()).isEqualTo(0.95);
        }

        @Test
        void shouldHaveLowerBoundLessThanUpper() {
            TimeSeries series = createTestSeries();

            ForecastUtil.ForecastResult result =
                    ForecastUtil.smaForecastWithBounds(series, 5, 3, 0.95);

            for (int i = 0; i < result.size(); i++) {
                double lower = result.lower().getPoints().get(i).value();
                double forecast = result.forecast().getPoints().get(i).value();
                double upper = result.upper().getPoints().get(i).value();

                assertThat(lower).isLessThanOrEqualTo(forecast);
                assertThat(forecast).isLessThanOrEqualTo(upper);
            }
        }
    }

    @Nested
    @DisplayName("WMA Forecast Tests")
    class WmaForecastTests {

        @Test
        void shouldForecastWithWma() {
            TimeSeries series = createTestSeries();

            TimeSeries forecast = ForecastUtil.wmaForecast(series, 5, 3);

            assertThat(forecast.size()).isEqualTo(3);
        }

        @Test
        void shouldWeightRecentValuesMore() {
            TimeSeries series = new TimeSeries("wma_test");
            Instant base = Instant.now();
            // Create series with increasing trend
            for (int i = 0; i < 10; i++) {
                series.add(DataPoint.of(base.plusSeconds(i * 60), i * 10.0));
            }

            double sma = 0;
            for (int i = 5; i < 10; i++) {
                sma += i * 10.0;
            }
            sma /= 5;

            TimeSeries wmaForecast = ForecastUtil.wmaForecast(series, 5, 1);

            // WMA should be higher than SMA for increasing trend
            assertThat(wmaForecast.getPoints().getFirst().value()).isGreaterThan(sma);
        }
    }

    @Nested
    @DisplayName("EMA Forecast Tests")
    class EmaForecastTests {

        @Test
        void shouldForecastWithEma() {
            TimeSeries series = createTestSeries();

            TimeSeries forecast = ForecastUtil.emaForecast(series, 0.3, 5);

            assertThat(forecast.size()).isEqualTo(5);
        }

        @Test
        void shouldRejectInvalidAlpha() {
            TimeSeries series = createTestSeries();

            assertThatThrownBy(() -> ForecastUtil.emaForecast(series, 0, 3))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> ForecastUtil.emaForecast(series, 1.5, 3))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldReturnEmptyForEmptySeries() {
            TimeSeries series = new TimeSeries("empty");

            TimeSeries forecast = ForecastUtil.emaForecast(series, 0.3, 3);

            assertThat(forecast.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Holt Forecast Tests")
    class HoltForecastTests {

        @Test
        void shouldForecastWithHolt() {
            TimeSeries series = createTrendSeries();

            TimeSeries forecast = ForecastUtil.holtForecast(series, 0.3, 0.1, 5);

            assertThat(forecast.size()).isEqualTo(5);
        }

        @Test
        void shouldCaptureUpwardTrend() {
            TimeSeries series = createTrendSeries();

            TimeSeries forecast = ForecastUtil.holtForecast(series, 0.3, 0.1, 3);

            // Forecast values should be increasing
            var points = forecast.getPoints();
            for (int i = 1; i < points.size(); i++) {
                assertThat(points.get(i).value()).isGreaterThan(points.get(i - 1).value());
            }
        }

        @Test
        void shouldRejectInvalidParameters() {
            TimeSeries series = createTestSeries();

            assertThatThrownBy(() -> ForecastUtil.holtForecast(series, 0, 0.1, 3))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> ForecastUtil.holtForecast(series, 0.3, 0, 3))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Linear Forecast Tests")
    class LinearForecastTests {

        @Test
        void shouldForecastWithLinearRegression() {
            TimeSeries series = createTrendSeries();

            TimeSeries forecast = ForecastUtil.linearForecast(series, 5);

            assertThat(forecast.size()).isEqualTo(5);
        }

        @Test
        void shouldExtendLinearTrend() {
            TimeSeries series = createTrendSeries();
            double lastValue = series.getLast().get().value();

            TimeSeries forecast = ForecastUtil.linearForecast(series, 1);

            // Linear forecast should extend beyond last value for upward trend
            assertThat(forecast.getPoints().getFirst().value()).isGreaterThan(lastValue);
        }

        @Test
        void shouldReturnEmptyForSinglePoint() {
            TimeSeries series = new TimeSeries("single");
            series.add(DataPoint.of(Instant.now(), 1.0));

            TimeSeries forecast = ForecastUtil.linearForecast(series, 3);

            assertThat(forecast.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Linear Forecast with Bounds Tests")
    class LinearForecastWithBoundsTests {

        @Test
        void shouldForecastWithBounds() {
            TimeSeries series = createTrendSeries();

            ForecastUtil.ForecastResult result =
                    ForecastUtil.linearForecastWithBounds(series, 5, 0.95);

            assertThat(result.isEmpty()).isFalse();
            assertThat(result.size()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Naive Forecast Tests")
    class NaiveForecastTests {

        @Test
        void shouldRepeatLastValue() {
            TimeSeries series = createTestSeries();
            double lastValue = series.getLast().get().value();

            TimeSeries forecast = ForecastUtil.naiveForecast(series, 3);

            assertThat(forecast.size()).isEqualTo(3);
            assertThat(forecast.getPoints()).allMatch(p -> p.value() == lastValue);
        }

        @Test
        void shouldReturnEmptyForEmptySeries() {
            TimeSeries series = new TimeSeries("empty");

            TimeSeries forecast = ForecastUtil.naiveForecast(series, 3);

            assertThat(forecast.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Seasonal Naive Forecast Tests")
    class SeasonalNaiveForecastTests {

        @Test
        void shouldRepeatSeasonalPattern() {
            TimeSeries series = new TimeSeries("seasonal");
            Instant base = Instant.now();
            // Create a repeating pattern: 1, 2, 3, 4, 1, 2, 3, 4, ...
            for (int i = 0; i < 12; i++) {
                series.add(DataPoint.of(base.plusSeconds(i * 60), (i % 4) + 1));
            }

            TimeSeries forecast = ForecastUtil.seasonalNaiveForecast(series, 4, 4);

            assertThat(forecast.size()).isEqualTo(4);
        }

        @Test
        void shouldReturnEmptyIfSeasonTooLong() {
            TimeSeries series = createTestSeries();

            TimeSeries forecast = ForecastUtil.seasonalNaiveForecast(series, 100, 3);

            assertThat(forecast.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Drift Forecast Tests")
    class DriftForecastTests {

        @Test
        void shouldExtrapolateAverageChange() {
            TimeSeries series = createTrendSeries();

            TimeSeries forecast = ForecastUtil.driftForecast(series, 3);

            assertThat(forecast.size()).isEqualTo(3);

            // Drift forecast should continue the trend
            var points = forecast.getPoints();
            for (int i = 1; i < points.size(); i++) {
                assertThat(points.get(i).value()).isGreaterThan(points.get(i - 1).value());
            }
        }
    }

    @Nested
    @DisplayName("Linear Coefficients Tests")
    class LinearCoefficientsTests {

        @Test
        void shouldPredictCorrectly() {
            ForecastUtil.LinearCoefficients coeffs =
                    new ForecastUtil.LinearCoefficients(10.0, 2.0);

            assertThat(coeffs.predict(0)).isEqualTo(10.0);
            assertThat(coeffs.predict(5)).isEqualTo(20.0);
        }
    }

    @Nested
    @DisplayName("Forecast Result Tests")
    class ForecastResultTests {

        @Test
        void shouldCreateEmptyResult() {
            ForecastUtil.ForecastResult result = ForecastUtil.ForecastResult.empty("test");

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.size()).isEqualTo(0);
            assertThat(result.confidenceLevel()).isEqualTo(0.0);
        }
    }
}
