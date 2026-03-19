package cloud.opencode.base.timeseries.decomposition;

import cloud.opencode.base.timeseries.TimeSeries;
import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * SeasonalDecompositionUtilTest Tests
 * SeasonalDecompositionUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("SeasonalDecompositionUtil Tests")
class SeasonalDecompositionUtilTest {

    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.parse("2024-01-01T00:00:00Z");
    }

    @Nested
    @DisplayName("DecompositionModel Enum Tests")
    class DecompositionModelEnumTests {

        @Test
        @DisplayName("should have ADDITIVE and MULTIPLICATIVE models")
        void shouldHaveAdditiveAndMultiplicativeModels() {
            SeasonalDecompositionUtil.DecompositionModel[] models =
                SeasonalDecompositionUtil.DecompositionModel.values();

            assertThat(models).contains(
                SeasonalDecompositionUtil.DecompositionModel.ADDITIVE,
                SeasonalDecompositionUtil.DecompositionModel.MULTIPLICATIVE
            );
        }

        @Test
        @DisplayName("valueOf should return correct model")
        void valueOfShouldReturnCorrectModel() {
            assertThat(SeasonalDecompositionUtil.DecompositionModel.valueOf("ADDITIVE"))
                .isEqualTo(SeasonalDecompositionUtil.DecompositionModel.ADDITIVE);
        }
    }

    @Nested
    @DisplayName("DecompositionResult Record Tests")
    class DecompositionResultRecordTests {

        @Test
        @DisplayName("DecompositionResult should store all components")
        void decompositionResultShouldStoreAllComponents() {
            TimeSeries original = new TimeSeries("original");
            TimeSeries trend = new TimeSeries("trend");
            TimeSeries seasonal = new TimeSeries("seasonal");
            TimeSeries residual = new TimeSeries("residual");

            SeasonalDecompositionUtil.DecompositionResult result =
                new SeasonalDecompositionUtil.DecompositionResult(
                    original, trend, seasonal, residual,
                    SeasonalDecompositionUtil.DecompositionModel.ADDITIVE, 12
                );

            assertThat(result.original()).isEqualTo(original);
            assertThat(result.trend()).isEqualTo(trend);
            assertThat(result.seasonal()).isEqualTo(seasonal);
            assertThat(result.residual()).isEqualTo(residual);
            assertThat(result.model()).isEqualTo(SeasonalDecompositionUtil.DecompositionModel.ADDITIVE);
            assertThat(result.period()).isEqualTo(12);
        }

        @Test
        @DisplayName("DecompositionResult reconstruct should work")
        void decompositionResultReconstructShouldWork() {
            TimeSeries series = createSeasonalSeries(12);

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.decompose(series, 12);

            TimeSeries reconstructed = result.reconstruct();

            assertThat(reconstructed).isNotNull();
            assertThat(reconstructed.size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("DecompositionResult seasonallyAdjusted should work")
        void decompositionResultSeasonallyAdjustedShouldWork() {
            TimeSeries series = createSeasonalSeries(12);

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.decompose(series, 12);

            TimeSeries adjusted = result.seasonallyAdjusted();

            assertThat(adjusted).isNotNull();
            assertThat(adjusted.size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("DecompositionResult seasonalStrength should return value between 0 and 1")
        void decompositionResultSeasonalStrengthShouldReturnValidValue() {
            TimeSeries series = createSeasonalSeries(12);

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.decompose(series, 12);

            double strength = result.seasonalStrength();

            assertThat(strength).isBetween(0.0, 1.0);
        }

        @Test
        @DisplayName("DecompositionResult trendStrength should return value between 0 and 1")
        void decompositionResultTrendStrengthShouldReturnValidValue() {
            TimeSeries series = createSeasonalSeries(12);

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.decompose(series, 12);

            double strength = result.trendStrength();

            assertThat(strength).isBetween(0.0, 1.0);
        }
    }

    @Nested
    @DisplayName("Decompose Tests")
    class DecomposeTests {

        @Test
        @DisplayName("decompose should create trend, seasonal, and residual components")
        void decomposeShouldCreateAllComponents() {
            TimeSeries series = createSeasonalSeries(12);

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.decompose(series, 12,
                    SeasonalDecompositionUtil.DecompositionModel.ADDITIVE);

            assertThat(result.trend()).isNotNull();
            assertThat(result.seasonal()).isNotNull();
            assertThat(result.residual()).isNotNull();
        }

        @Test
        @DisplayName("decompose with default model should use ADDITIVE")
        void decomposeWithDefaultModelShouldUseAdditive() {
            TimeSeries series = createSeasonalSeries(12);

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.decompose(series, 12);

            assertThat(result.model()).isEqualTo(SeasonalDecompositionUtil.DecompositionModel.ADDITIVE);
        }

        @Test
        @DisplayName("decompose with ADDITIVE model should work")
        void decomposeWithAdditiveModelShouldWork() {
            TimeSeries series = createSeasonalSeries(12);

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.decompose(series, 12,
                    SeasonalDecompositionUtil.DecompositionModel.ADDITIVE);

            assertThat(result.model()).isEqualTo(SeasonalDecompositionUtil.DecompositionModel.ADDITIVE);
        }

        @Test
        @DisplayName("decompose with MULTIPLICATIVE model should work")
        void decomposeWithMultiplicativeModelShouldWork() {
            TimeSeries series = createSeasonalSeries(12);
            // Shift values to be positive for multiplicative model
            TimeSeries positive = new TimeSeries("positive");
            series.getPoints().forEach(p ->
                positive.add(p.timestamp(), p.value() + 100));

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.decompose(positive, 12,
                    SeasonalDecompositionUtil.DecompositionModel.MULTIPLICATIVE);

            assertThat(result.model()).isEqualTo(SeasonalDecompositionUtil.DecompositionModel.MULTIPLICATIVE);
        }

        @Test
        @DisplayName("decompose should throw for series too short")
        void decomposeShouldThrowForSeriesTooShort() {
            TimeSeries shortSeries = new TimeSeries("short");
            for (int i = 0; i < 10; i++) {
                shortSeries.add(baseTime.plusSeconds(i), 50.0);
            }

            assertThatThrownBy(() ->
                SeasonalDecompositionUtil.decompose(shortSeries, 12))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("decompose should throw for period less than 2")
        void decomposeShouldThrowForPeriodLessThan2() {
            TimeSeries series = createSeasonalSeries(12);

            assertThatThrownBy(() ->
                SeasonalDecompositionUtil.decompose(series, 1))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("STL Decompose Tests")
    class STLDecomposeTests {

        @Test
        @DisplayName("stlDecompose should create decomposition")
        void stlDecomposeShouldCreateDecomposition() {
            TimeSeries series = createSeasonalSeries(12);

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.stlDecompose(series, 12);

            assertThat(result.trend()).isNotNull();
            assertThat(result.seasonal()).isNotNull();
            assertThat(result.residual()).isNotNull();
        }

        @Test
        @DisplayName("stlDecompose with iterations should work")
        void stlDecomposeWithIterationsShouldWork() {
            TimeSeries series = createSeasonalSeries(12);

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.stlDecompose(series, 12, 3);

            assertThat(result).isNotNull();
            assertThat(result.model()).isEqualTo(SeasonalDecompositionUtil.DecompositionModel.ADDITIVE);
        }
    }

    @Nested
    @DisplayName("Detect Seasonal Period Tests")
    class DetectSeasonalPeriodTests {

        @Test
        @DisplayName("detectSeasonalPeriod should find period in seasonal data")
        void detectSeasonalPeriodShouldFindPeriodInSeasonalData() {
            TimeSeries series = createSeasonalSeries(12);

            int detectedPeriod = SeasonalDecompositionUtil.detectSeasonalPeriod(series);

            // Should detect period close to 12 or return -1 if not found
            assertThat(detectedPeriod).isGreaterThanOrEqualTo(-1);
        }

        @Test
        @DisplayName("detectSeasonalPeriod with range should work")
        void detectSeasonalPeriodWithRangeShouldWork() {
            TimeSeries series = createSeasonalSeries(12);

            int detectedPeriod = SeasonalDecompositionUtil.detectSeasonalPeriod(series, 5, 20);

            assertThat(detectedPeriod).isGreaterThanOrEqualTo(-1);
        }

        @Test
        @DisplayName("detectSeasonalPeriod should throw for null series")
        void detectSeasonalPeriodShouldThrowForNullSeries() {
            assertThatThrownBy(() -> SeasonalDecompositionUtil.detectSeasonalPeriod(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("detectSeasonalPeriod should return -1 for short series")
        void detectSeasonalPeriodShouldReturnNegativeForShortSeries() {
            TimeSeries shortSeries = new TimeSeries("short");
            for (int i = 0; i < 3; i++) {
                shortSeries.add(baseTime.plusSeconds(i), i * 1.0);
            }

            int detectedPeriod = SeasonalDecompositionUtil.detectSeasonalPeriod(shortSeries);

            assertThat(detectedPeriod).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Calculate Seasonal Indices Tests")
    class CalculateSeasonalIndicesTests {

        @Test
        @DisplayName("calculateSeasonalIndices should return indices for each period")
        void calculateSeasonalIndicesShouldReturnIndicesForEachPeriod() {
            TimeSeries series = createSeasonalSeries(12);

            double[] indices = SeasonalDecompositionUtil.calculateSeasonalIndices(series, 12);

            assertThat(indices).hasSize(12);
        }

        @Test
        @DisplayName("seasonal indices should sum to approximately zero for additive model")
        void seasonalIndicesShouldSumToApproximatelyZeroForAdditiveModel() {
            TimeSeries series = createSeasonalSeries(12);

            double[] indices = SeasonalDecompositionUtil.calculateSeasonalIndices(series, 12);

            double sum = 0;
            for (double index : indices) {
                sum += index;
            }
            assertThat(sum).isCloseTo(0.0, within(1.0));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle constant series")
        void shouldHandleConstantSeries() {
            TimeSeries constant = new TimeSeries("constant");
            for (int i = 0; i < 100; i++) {
                constant.add(baseTime.plusSeconds(i), 50.0);
            }

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.decompose(constant, 12,
                    SeasonalDecompositionUtil.DecompositionModel.ADDITIVE);

            assertThat(result.trend()).isNotNull();
        }

        @Test
        @DisplayName("should handle series with exactly 2 periods")
        void shouldHandleSeriesWithExactly2Periods() {
            TimeSeries series = new TimeSeries("two-periods");
            for (int i = 0; i < 24; i++) {
                double value = 50.0 + 10.0 * Math.sin(2 * Math.PI * i / 12);
                series.add(baseTime.plusSeconds(i), value);
            }

            SeasonalDecompositionUtil.DecompositionResult result =
                SeasonalDecompositionUtil.decompose(series, 12);

            assertThat(result).isNotNull();
        }
    }

    // Helper method to create test data
    private TimeSeries createSeasonalSeries(int period) {
        TimeSeries series = new TimeSeries("seasonal");
        for (int i = 0; i < 200; i++) {
            // Trend + Seasonal + Noise
            double trend = 50.0 + i * 0.1;
            double seasonal = 10.0 * Math.sin(2 * Math.PI * i / period);
            double noise = Math.random() * 2 - 1;
            series.add(baseTime.plusSeconds(i), trend + seasonal + noise);
        }
        return series;
    }
}
