package cloud.opencode.base.timeseries.quality;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * GapDetector Tests
 * GapDetector 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
@DisplayName("GapDetector Tests")
class GapDetectorTest {

    private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");
    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);

    private TimeSeries regularSeries() {
        TimeSeries ts = new TimeSeries("regular");
        for (int i = 0; i < 10; i++) {
            ts.add(BASE.plus(Duration.ofMinutes(i)), i * 1.0);
        }
        return ts;
    }

    private TimeSeries seriesWithGaps() {
        TimeSeries ts = new TimeSeries("gaps");
        // Points at 0, 1, 2, 5, 6, 10 minutes
        // Gap between 2->5 (3 min) and 6->10 (4 min), both > 1.5 * 1 min
        ts.add(BASE, 1.0);
        ts.add(BASE.plus(Duration.ofMinutes(1)), 2.0);
        ts.add(BASE.plus(Duration.ofMinutes(2)), 3.0);
        ts.add(BASE.plus(Duration.ofMinutes(5)), 4.0);
        ts.add(BASE.plus(Duration.ofMinutes(6)), 5.0);
        ts.add(BASE.plus(Duration.ofMinutes(10)), 6.0);
        return ts;
    }

    @Nested
    @DisplayName("detectGaps Tests")
    class DetectGapsTests {

        @Test
        @DisplayName("should find gaps exceeding 1.5x expected interval")
        void shouldFindGapsExceedingDefaultTolerance() {
            TimeSeries ts = seriesWithGaps();

            List<Gap> gaps = GapDetector.detectGaps(ts, ONE_MINUTE);

            assertThat(gaps).hasSize(2);
            // First gap: minute 2 -> minute 5
            assertThat(gaps.get(0).start()).isEqualTo(BASE.plus(Duration.ofMinutes(2)));
            assertThat(gaps.get(0).end()).isEqualTo(BASE.plus(Duration.ofMinutes(5)));
            // Second gap: minute 6 -> minute 10
            assertThat(gaps.get(1).start()).isEqualTo(BASE.plus(Duration.ofMinutes(6)));
            assertThat(gaps.get(1).end()).isEqualTo(BASE.plus(Duration.ofMinutes(10)));
        }

        @Test
        @DisplayName("should return no gaps for regular data")
        void shouldReturnNoGapsForRegularData() {
            TimeSeries ts = regularSeries();

            List<Gap> gaps = GapDetector.detectGaps(ts, ONE_MINUTE);

            assertThat(gaps).isEmpty();
        }

        @Test
        @DisplayName("should respect custom tolerance factor")
        void shouldRespectCustomToleranceFactor() {
            TimeSeries ts = seriesWithGaps();

            // With tolerance 5.0, only gaps > 5 minutes are detected
            // Gap 2->5 is 3 min (not > 5), gap 6->10 is 4 min (not > 5)
            List<Gap> gaps = GapDetector.detectGaps(ts, ONE_MINUTE, 5.0);

            assertThat(gaps).isEmpty();
        }

        @Test
        @DisplayName("should detect more gaps with lower tolerance")
        void shouldDetectMoreGapsWithLowerTolerance() {
            TimeSeries ts = seriesWithGaps();

            // With tolerance 1.1, gaps > 1.1 minutes are detected
            // 2->5 (3 min), 6->10 (4 min) are both detected
            List<Gap> gaps = GapDetector.detectGaps(ts, ONE_MINUTE, 1.1);

            assertThat(gaps).hasSize(2);
        }

        @Test
        @DisplayName("should return empty list for single-point series")
        void shouldReturnEmptyForSinglePoint() {
            TimeSeries ts = new TimeSeries("single");
            ts.add(BASE, 1.0);

            List<Gap> gaps = GapDetector.detectGaps(ts, ONE_MINUTE);

            assertThat(gaps).isEmpty();
        }

        @Test
        @DisplayName("should return empty list for empty series")
        void shouldReturnEmptyForEmptySeries() {
            TimeSeries ts = new TimeSeries("empty");

            List<Gap> gaps = GapDetector.detectGaps(ts, ONE_MINUTE);

            assertThat(gaps).isEmpty();
        }

        @Test
        @DisplayName("should return unmodifiable list")
        void shouldReturnUnmodifiableList() {
            TimeSeries ts = seriesWithGaps();

            List<Gap> gaps = GapDetector.detectGaps(ts, ONE_MINUTE);

            assertThatThrownBy(() -> gaps.add(new Gap(BASE, BASE.plusSeconds(10))))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("should throw on null interval")
        void shouldThrowOnNullInterval() {
            TimeSeries ts = regularSeries();

            assertThatThrownBy(() -> GapDetector.detectGaps(ts, null))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        @DisplayName("should throw on zero interval")
        void shouldThrowOnZeroInterval() {
            TimeSeries ts = regularSeries();

            assertThatThrownBy(() -> GapDetector.detectGaps(ts, Duration.ZERO))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        @DisplayName("should throw on negative interval")
        void shouldThrowOnNegativeInterval() {
            TimeSeries ts = regularSeries();

            assertThatThrownBy(() -> GapDetector.detectGaps(ts, Duration.ofMinutes(-1)))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        @DisplayName("should throw on null time series")
        void shouldThrowOnNullTimeSeries() {
            assertThatNullPointerException()
                    .isThrownBy(() -> GapDetector.detectGaps(null, ONE_MINUTE));
        }

        @Test
        @DisplayName("should throw on tolerance factor <= 1.0")
        void shouldThrowOnToleranceFactorNotGreaterThanOne() {
            TimeSeries ts = regularSeries();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GapDetector.detectGaps(ts, ONE_MINUTE, 1.0))
                    .withMessageContaining("1.0");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GapDetector.detectGaps(ts, ONE_MINUTE, 0.5));
        }
    }

    @Nested
    @DisplayName("dataCompleteness Tests")
    class DataCompletenessTests {

        @Test
        @DisplayName("should return 1.0 for fully complete data")
        void shouldReturnOneForCompleteData() {
            TimeSeries ts = regularSeries();
            Instant from = BASE;
            Instant to = BASE.plus(Duration.ofMinutes(9));

            double completeness = GapDetector.dataCompleteness(ts, ONE_MINUTE, from, to);

            // 9 minute range, 1-minute interval -> expected ~9 points, actual 10
            // Math.min(1.0, 10 / 9.0) = 1.0
            assertThat(completeness).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should return correct percentage for incomplete data")
        void shouldReturnCorrectPercentageForIncompleteData() {
            TimeSeries ts = new TimeSeries("incomplete");
            // 5 points over a 10-minute range -> expect ~10 points, got 5
            for (int i = 0; i < 5; i++) {
                ts.add(BASE.plus(Duration.ofMinutes(i * 2)), i * 1.0);
            }
            Instant from = BASE;
            Instant to = BASE.plus(Duration.ofMinutes(10));

            double completeness = GapDetector.dataCompleteness(ts, ONE_MINUTE, from, to);

            // 10 min range, 1-min interval -> expected 10 points, actual 5
            assertThat(completeness).isCloseTo(0.5, within(0.01));
        }

        @Test
        @DisplayName("should return 0.0 when from equals to")
        void shouldReturnZeroWhenFromEqualsTo() {
            TimeSeries ts = regularSeries();

            double completeness = GapDetector.dataCompleteness(ts, ONE_MINUTE, BASE, BASE);

            assertThat(completeness).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should return 0.0 when from is after to")
        void shouldReturnZeroWhenFromAfterTo() {
            TimeSeries ts = regularSeries();
            Instant to = BASE.minus(Duration.ofMinutes(1));

            double completeness = GapDetector.dataCompleteness(ts, ONE_MINUTE, BASE, to);

            assertThat(completeness).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("longestGap Tests")
    class LongestGapTests {

        @Test
        @DisplayName("should find the longest gap")
        void shouldFindLongestGap() {
            TimeSeries ts = seriesWithGaps();

            Optional<Gap> longest = GapDetector.longestGap(ts);

            assertThat(longest).isPresent();
            // Longest gap is 6->10 (4 minutes)
            assertThat(longest.get().start()).isEqualTo(BASE.plus(Duration.ofMinutes(6)));
            assertThat(longest.get().end()).isEqualTo(BASE.plus(Duration.ofMinutes(10)));
            assertThat(longest.get().length()).isEqualTo(Duration.ofMinutes(4));
        }

        @Test
        @DisplayName("should return empty for single-point series")
        void shouldReturnEmptyForSinglePoint() {
            TimeSeries ts = new TimeSeries("single");
            ts.add(BASE, 1.0);

            Optional<Gap> longest = GapDetector.longestGap(ts);

            assertThat(longest).isEmpty();
        }

        @Test
        @DisplayName("should return empty for empty series")
        void shouldReturnEmptyForEmptySeries() {
            TimeSeries ts = new TimeSeries("empty");

            Optional<Gap> longest = GapDetector.longestGap(ts);

            assertThat(longest).isEmpty();
        }

        @Test
        @DisplayName("should return the gap between two points")
        void shouldReturnGapBetweenTwoPoints() {
            TimeSeries ts = new TimeSeries("two");
            ts.add(BASE, 1.0);
            ts.add(BASE.plus(Duration.ofHours(1)), 2.0);

            Optional<Gap> longest = GapDetector.longestGap(ts);

            assertThat(longest).isPresent();
            assertThat(longest.get().length()).isEqualTo(Duration.ofHours(1));
        }
    }

    @Nested
    @DisplayName("gapCount Tests")
    class GapCountTests {

        @Test
        @DisplayName("should return correct count")
        void shouldReturnCorrectCount() {
            TimeSeries ts = seriesWithGaps();

            int count = GapDetector.gapCount(ts, ONE_MINUTE);

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("should return zero for regular data")
        void shouldReturnZeroForRegularData() {
            TimeSeries ts = regularSeries();

            int count = GapDetector.gapCount(ts, ONE_MINUTE);

            assertThat(count).isZero();
        }

        @Test
        @DisplayName("should return zero for empty series")
        void shouldReturnZeroForEmptySeries() {
            TimeSeries ts = new TimeSeries("empty");

            int count = GapDetector.gapCount(ts, ONE_MINUTE);

            assertThat(count).isZero();
        }
    }
}
