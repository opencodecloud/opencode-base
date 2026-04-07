package cloud.opencode.base.timeseries.math;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * MathUtil Tests
 * MathUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
@DisplayName("MathUtil Tests")
class MathUtilTest {

    private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");

    private TimeSeries createSeries(double... values) {
        TimeSeries ts = new TimeSeries("test");
        for (int i = 0; i < values.length; i++) {
            ts.add(BASE.plus(Duration.ofSeconds(i)), values[i]);
        }
        return ts;
    }

    private TimeSeries emptySeries() {
        return new TimeSeries("empty");
    }

    @Nested
    @DisplayName("normalize Tests")
    class NormalizeTests {

        @Test
        @DisplayName("should map values to [0, 1]")
        void shouldMapValuesToZeroOne() {
            TimeSeries ts = createSeries(10, 20, 30, 40, 50);

            TimeSeries normalized = MathUtil.normalize(ts);

            double[] values = normalized.getValues();
            assertThat(values[0]).isCloseTo(0.0, within(1e-10));
            assertThat(values[4]).isCloseTo(1.0, within(1e-10));
            assertThat(values[2]).isCloseTo(0.5, within(1e-10));
        }

        @Test
        @DisplayName("should return all 0.0 for constant values")
        void shouldReturnAllZeroForConstantValues() {
            TimeSeries ts = createSeries(5, 5, 5, 5);

            TimeSeries normalized = MathUtil.normalize(ts);

            for (double v : normalized.getValues()) {
                assertThat(v).isEqualTo(0.0);
            }
        }

        @Test
        @DisplayName("should throw for empty series")
        void shouldThrowForEmptySeries() {
            assertThatThrownBy(() -> MathUtil.normalize(emptySeries()))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        @DisplayName("normalized name should contain _normalized suffix")
        void normalizedNameShouldContainSuffix() {
            TimeSeries ts = createSeries(1, 2, 3);

            TimeSeries normalized = MathUtil.normalize(ts);

            assertThat(normalized.getName()).endsWith("_normalized");
        }
    }

    @Nested
    @DisplayName("zScore Tests")
    class ZScoreTests {

        @Test
        @DisplayName("should standardize to mean 0 and stdDev ~1")
        void shouldStandardize() {
            TimeSeries ts = createSeries(10, 20, 30, 40, 50);

            TimeSeries zScored = MathUtil.zScore(ts);
            double[] values = zScored.getValues();

            // Mean of z-scores should be ~0
            double meanZ = 0;
            for (double v : values) {
                meanZ += v;
            }
            meanZ /= values.length;
            assertThat(meanZ).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("should return all 0.0 for constant values (zero stdDev)")
        void shouldReturnAllZeroForConstantValues() {
            TimeSeries ts = createSeries(7, 7, 7);

            TimeSeries zScored = MathUtil.zScore(ts);

            for (double v : zScored.getValues()) {
                assertThat(v).isEqualTo(0.0);
            }
        }

        @Test
        @DisplayName("should throw for empty series")
        void shouldThrowForEmptySeries() {
            assertThatThrownBy(() -> MathUtil.zScore(emptySeries()))
                    .isInstanceOf(TimeSeriesException.class);
        }
    }

    @Nested
    @DisplayName("Element-wise Math Tests")
    class ElementWiseMathTests {

        @Test
        @DisplayName("log should compute natural logarithm")
        void logShouldComputeNaturalLogarithm() {
            TimeSeries ts = createSeries(1, Math.E, Math.E * Math.E);

            TimeSeries result = MathUtil.log(ts);
            double[] values = result.getValues();

            assertThat(values[0]).isCloseTo(0.0, within(1e-10));
            assertThat(values[1]).isCloseTo(1.0, within(1e-10));
            assertThat(values[2]).isCloseTo(2.0, within(1e-10));
        }

        @Test
        @DisplayName("log10 should compute base-10 logarithm")
        void log10ShouldComputeBase10Logarithm() {
            TimeSeries ts = createSeries(1, 10, 100, 1000);

            TimeSeries result = MathUtil.log10(ts);
            double[] values = result.getValues();

            assertThat(values[0]).isCloseTo(0.0, within(1e-10));
            assertThat(values[1]).isCloseTo(1.0, within(1e-10));
            assertThat(values[2]).isCloseTo(2.0, within(1e-10));
            assertThat(values[3]).isCloseTo(3.0, within(1e-10));
        }

        @Test
        @DisplayName("exp should compute exponential")
        void expShouldComputeExponential() {
            TimeSeries ts = createSeries(0, 1, 2);

            TimeSeries result = MathUtil.exp(ts);
            double[] values = result.getValues();

            assertThat(values[0]).isCloseTo(1.0, within(1e-10));
            assertThat(values[1]).isCloseTo(Math.E, within(1e-10));
            assertThat(values[2]).isCloseTo(Math.E * Math.E, within(1e-10));
        }

        @Test
        @DisplayName("abs should compute absolute values")
        void absShouldComputeAbsoluteValues() {
            TimeSeries ts = createSeries(-3, -1, 0, 1, 3);

            TimeSeries result = MathUtil.abs(ts);
            double[] values = result.getValues();

            assertThat(values).containsExactly(3, 1, 0, 1, 3);
        }

        @Test
        @DisplayName("scale should multiply by factor")
        void scaleShouldMultiplyByFactor() {
            TimeSeries ts = createSeries(1, 2, 3);

            TimeSeries result = MathUtil.scale(ts, 2.5);
            double[] values = result.getValues();

            assertThat(values).containsExactly(2.5, 5.0, 7.5);
        }

        @Test
        @DisplayName("offset should add delta to all values")
        void offsetShouldAddDelta() {
            TimeSeries ts = createSeries(10, 20, 30);

            TimeSeries result = MathUtil.offset(ts, -5.0);
            double[] values = result.getValues();

            assertThat(values).containsExactly(5.0, 15.0, 25.0);
        }

        @Test
        @DisplayName("power should raise values to exponent")
        void powerShouldRaiseToExponent() {
            TimeSeries ts = createSeries(2, 3, 4);

            TimeSeries result = MathUtil.power(ts, 2.0);
            double[] values = result.getValues();

            assertThat(values).containsExactly(4.0, 9.0, 16.0);
        }

        @Test
        @DisplayName("all element-wise ops should throw for empty series")
        void allOpsShouldThrowForEmptySeries() {
            TimeSeries empty = emptySeries();

            assertThatThrownBy(() -> MathUtil.log(empty)).isInstanceOf(TimeSeriesException.class);
            assertThatThrownBy(() -> MathUtil.log10(empty)).isInstanceOf(TimeSeriesException.class);
            assertThatThrownBy(() -> MathUtil.exp(empty)).isInstanceOf(TimeSeriesException.class);
            assertThatThrownBy(() -> MathUtil.abs(empty)).isInstanceOf(TimeSeriesException.class);
            assertThatThrownBy(() -> MathUtil.scale(empty, 2.0)).isInstanceOf(TimeSeriesException.class);
            assertThatThrownBy(() -> MathUtil.offset(empty, 1.0)).isInstanceOf(TimeSeriesException.class);
            assertThatThrownBy(() -> MathUtil.power(empty, 2.0)).isInstanceOf(TimeSeriesException.class);
        }
    }

    @Nested
    @DisplayName("rollingStdDev Tests")
    class RollingStdDevTests {

        @Test
        @DisplayName("should compute rolling std dev with known values")
        void shouldComputeRollingStdDevWithKnownValues() {
            // Values: 2, 4, 6, 8, 10
            // Window=3: [2,4,6] stddev, [4,6,8] stddev, [6,8,10] stddev
            TimeSeries ts = createSeries(2, 4, 6, 8, 10);

            TimeSeries result = MathUtil.rollingStdDev(ts, 3);

            assertThat(result.size()).isEqualTo(3);
            // [2,4,6]: mean=4, var=((4+0+4)/2)=4, std=2
            assertThat(result.getValues()[0]).isCloseTo(2.0, within(1e-10));
            // [4,6,8]: mean=6, var=((4+0+4)/2)=4, std=2
            assertThat(result.getValues()[1]).isCloseTo(2.0, within(1e-10));
            // [6,8,10]: mean=8, var=((4+0+4)/2)=4, std=2
            assertThat(result.getValues()[2]).isCloseTo(2.0, within(1e-10));
        }

        @Test
        @DisplayName("should throw for window <= 0")
        void shouldThrowForZeroWindow() {
            TimeSeries ts = createSeries(1, 2, 3);

            assertThatThrownBy(() -> MathUtil.rollingStdDev(ts, 0))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        @DisplayName("should throw for window > series size")
        void shouldThrowForWindowLargerThanSeries() {
            TimeSeries ts = createSeries(1, 2);

            assertThatThrownBy(() -> MathUtil.rollingStdDev(ts, 5))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        @DisplayName("window=1 should return all zeros")
        void windowOneShouldReturnAllZeros() {
            TimeSeries ts = createSeries(1, 2, 3, 4, 5);

            TimeSeries result = MathUtil.rollingStdDev(ts, 1);

            assertThat(result.size()).isEqualTo(5);
            for (double v : result.getValues()) {
                assertThat(v).isEqualTo(0.0);
            }
        }
    }

    @Nested
    @DisplayName("bollingerBands Tests")
    class BollingerBandsTests {

        @Test
        @DisplayName("upper should be greater than middle, middle greater than lower")
        void upperGreaterThanMiddleGreaterThanLower() {
            TimeSeries ts = createSeries(10, 12, 11, 13, 14, 12, 15, 16, 13, 17);

            MathUtil.BollingerBands bands = MathUtil.bollingerBands(ts, 3, 2.0);

            List<DataPoint> upper = bands.upper().getPoints();
            List<DataPoint> middle = bands.middle().getPoints();
            List<DataPoint> lower = bands.lower().getPoints();

            assertThat(upper).hasSameSizeAs(middle);
            assertThat(middle).hasSameSizeAs(lower);

            for (int i = 0; i < middle.size(); i++) {
                assertThat(upper.get(i).value())
                        .isGreaterThanOrEqualTo(middle.get(i).value());
                assertThat(middle.get(i).value())
                        .isGreaterThanOrEqualTo(lower.get(i).value());
            }
        }

        @Test
        @DisplayName("with constant values, upper should equal middle should equal lower")
        void constantValuesAllBandsEqual() {
            TimeSeries ts = createSeries(5, 5, 5, 5, 5);

            MathUtil.BollingerBands bands = MathUtil.bollingerBands(ts, 3, 2.0);

            List<DataPoint> upper = bands.upper().getPoints();
            List<DataPoint> middle = bands.middle().getPoints();
            List<DataPoint> lower = bands.lower().getPoints();

            for (int i = 0; i < middle.size(); i++) {
                assertThat(upper.get(i).value()).isCloseTo(middle.get(i).value(), within(1e-10));
                assertThat(lower.get(i).value()).isCloseTo(middle.get(i).value(), within(1e-10));
            }
        }

        @Test
        @DisplayName("band width should scale with numStdDev")
        void bandWidthShouldScaleWithNumStdDev() {
            TimeSeries ts = createSeries(10, 20, 30, 40, 50);

            MathUtil.BollingerBands bands1 = MathUtil.bollingerBands(ts, 3, 1.0);
            MathUtil.BollingerBands bands2 = MathUtil.bollingerBands(ts, 3, 2.0);

            double width1 = bands1.upper().getValues()[0] - bands1.lower().getValues()[0];
            double width2 = bands2.upper().getValues()[0] - bands2.lower().getValues()[0];

            // 2x numStdDev should give ~2x width
            assertThat(width2).isCloseTo(width1 * 2.0, within(1e-10));
        }
    }
}
