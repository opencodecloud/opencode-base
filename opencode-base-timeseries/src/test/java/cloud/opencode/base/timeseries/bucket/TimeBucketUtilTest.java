package cloud.opencode.base.timeseries.bucket;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;
import cloud.opencode.base.timeseries.sampling.AggregationType;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TimeBucketUtil Tests
 * TimeBucketUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
@DisplayName("TimeBucketUtil Tests")
class TimeBucketUtilTest {

    private static final ZoneId UTC = ZoneOffset.UTC;

    @Nested
    @DisplayName("Calendar-Aware Bucket Tests")
    class CalendarBucketTests {

        @Test
        @DisplayName("HOUR bucket should group by hour with AVG aggregation")
        void hourBucketShouldGroupByHour() {
            TimeSeries ts = new TimeSeries("hourly");
            // Hour 0: 10, 20 => avg 15
            ts.add(Instant.parse("2026-01-01T00:10:00Z"), 10.0);
            ts.add(Instant.parse("2026-01-01T00:30:00Z"), 20.0);
            // Hour 1: 30, 40 => avg 35
            ts.add(Instant.parse("2026-01-01T01:15:00Z"), 30.0);
            ts.add(Instant.parse("2026-01-01T01:45:00Z"), 40.0);

            TimeSeries result = TimeBucketUtil.bucket(ts, TimeBucket.HOUR, UTC, AggregationType.AVG);

            assertThat(result.size()).isEqualTo(2);
            List<DataPoint> points = result.getPoints();
            // First bucket at 00:00
            assertThat(points.get(0).timestamp()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
            assertThat(points.get(0).value()).isCloseTo(15.0, within(1e-10));
            // Second bucket at 01:00
            assertThat(points.get(1).timestamp()).isEqualTo(Instant.parse("2026-01-01T01:00:00Z"));
            assertThat(points.get(1).value()).isCloseTo(35.0, within(1e-10));
        }

        @Test
        @DisplayName("DAY bucket should group by calendar day with SUM aggregation")
        void dayBucketShouldGroupByDay() {
            TimeSeries ts = new TimeSeries("daily");
            // Day 1: 10 + 20 = 30
            ts.add(Instant.parse("2026-01-01T06:00:00Z"), 10.0);
            ts.add(Instant.parse("2026-01-01T18:00:00Z"), 20.0);
            // Day 2: 30 + 40 = 70
            ts.add(Instant.parse("2026-01-02T06:00:00Z"), 30.0);
            ts.add(Instant.parse("2026-01-02T18:00:00Z"), 40.0);

            TimeSeries result = TimeBucketUtil.bucket(ts, TimeBucket.DAY, UTC, AggregationType.SUM);

            assertThat(result.size()).isEqualTo(2);
            List<DataPoint> points = result.getPoints();
            assertThat(points.get(0).timestamp()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
            assertThat(points.get(0).value()).isCloseTo(30.0, within(1e-10));
            assertThat(points.get(1).timestamp()).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"));
            assertThat(points.get(1).value()).isCloseTo(70.0, within(1e-10));
        }

        @Test
        @DisplayName("DAY bucket with timezone should respect timezone boundaries")
        void dayBucketWithTimezoneShouldRespectTimezoneBoundaries() {
            // UTC+8: midnight is 16:00 UTC previous day
            ZoneId shanghai = ZoneId.of("Asia/Shanghai");
            TimeSeries ts = new TimeSeries("tz");
            // In UTC+8, 2026-01-01T23:00 = 2026-01-01T15:00 UTC (still Jan 1 in +8)
            ts.add(Instant.parse("2026-01-01T15:00:00Z"), 10.0);
            // In UTC+8, 2026-01-02T01:00 = 2026-01-01T17:00 UTC (Jan 2 in +8)
            ts.add(Instant.parse("2026-01-01T17:00:00Z"), 20.0);

            TimeSeries result = TimeBucketUtil.bucket(ts, TimeBucket.DAY, shanghai, AggregationType.SUM);

            // Should group into two different days in Shanghai timezone
            assertThat(result.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("MONTH bucket should group by month")
        void monthBucketShouldGroupByMonth() {
            TimeSeries ts = new TimeSeries("monthly");
            // January
            ts.add(Instant.parse("2026-01-15T00:00:00Z"), 100.0);
            ts.add(Instant.parse("2026-01-20T00:00:00Z"), 200.0);
            // February
            ts.add(Instant.parse("2026-02-10T00:00:00Z"), 300.0);
            // March
            ts.add(Instant.parse("2026-03-05T00:00:00Z"), 400.0);

            TimeSeries result = TimeBucketUtil.bucket(ts, TimeBucket.MONTH, UTC, AggregationType.SUM);

            assertThat(result.size()).isEqualTo(3);
            List<DataPoint> points = result.getPoints();
            assertThat(points.get(0).timestamp()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
            assertThat(points.get(0).value()).isCloseTo(300.0, within(1e-10));
            assertThat(points.get(1).timestamp()).isEqualTo(Instant.parse("2026-02-01T00:00:00Z"));
            assertThat(points.get(1).value()).isCloseTo(300.0, within(1e-10));
            assertThat(points.get(2).timestamp()).isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
            assertThat(points.get(2).value()).isCloseTo(400.0, within(1e-10));
        }

        @Test
        @DisplayName("should throw for empty series")
        void shouldThrowForEmptySeries() {
            TimeSeries empty = new TimeSeries("empty");

            assertThatThrownBy(() -> TimeBucketUtil.bucket(empty, TimeBucket.HOUR, UTC, AggregationType.AVG))
                    .isInstanceOf(TimeSeriesException.class);
        }
    }

    @Nested
    @DisplayName("Fixed-Duration Bucket Tests")
    class FixedDurationBucketTests {

        @Test
        @DisplayName("should bucket with custom origin and duration")
        void shouldBucketWithCustomOriginAndDuration() {
            Instant origin = Instant.parse("2026-01-01T00:00:00Z");
            TimeSeries ts = new TimeSeries("fixed");
            // 5-minute buckets from origin
            // Bucket 0 (0-5 min): 10, 20 => sum 30
            ts.add(origin.plus(Duration.ofMinutes(1)), 10.0);
            ts.add(origin.plus(Duration.ofMinutes(3)), 20.0);
            // Bucket 1 (5-10 min): 30 => sum 30
            ts.add(origin.plus(Duration.ofMinutes(7)), 30.0);

            TimeSeries result = TimeBucketUtil.bucket(ts, Duration.ofMinutes(5), origin, AggregationType.SUM);

            assertThat(result.size()).isEqualTo(2);
            List<DataPoint> points = result.getPoints();
            assertThat(points.get(0).timestamp()).isEqualTo(origin);
            assertThat(points.get(0).value()).isCloseTo(30.0, within(1e-10));
            assertThat(points.get(1).timestamp()).isEqualTo(origin.plus(Duration.ofMinutes(5)));
            assertThat(points.get(1).value()).isCloseTo(30.0, within(1e-10));
        }

        @Test
        @DisplayName("should throw for zero interval")
        void shouldThrowForZeroInterval() {
            TimeSeries ts = new TimeSeries("ts");
            ts.add(Instant.now(), 1.0);

            assertThatThrownBy(() -> TimeBucketUtil.bucket(ts, Duration.ZERO, Instant.EPOCH, AggregationType.SUM))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        @DisplayName("should throw for negative interval")
        void shouldThrowForNegativeInterval() {
            TimeSeries ts = new TimeSeries("ts");
            ts.add(Instant.now(), 1.0);

            assertThatThrownBy(() ->
                    TimeBucketUtil.bucket(ts, Duration.ofMinutes(-1), Instant.EPOCH, AggregationType.SUM))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        @DisplayName("should throw for empty series")
        void shouldThrowForEmptySeries() {
            assertThatThrownBy(() ->
                    TimeBucketUtil.bucket(new TimeSeries("e"), Duration.ofMinutes(5), Instant.EPOCH, AggregationType.SUM))
                    .isInstanceOf(TimeSeriesException.class);
        }
    }

    @Nested
    @DisplayName("All AggregationType Tests")
    class AggregationTypeTests {

        private TimeSeries singleBucketSeries() {
            // All points in the same hour
            TimeSeries ts = new TimeSeries("agg");
            ts.add(Instant.parse("2026-01-01T00:10:00Z"), 10.0);
            ts.add(Instant.parse("2026-01-01T00:20:00Z"), 30.0);
            ts.add(Instant.parse("2026-01-01T00:30:00Z"), 20.0);
            ts.add(Instant.parse("2026-01-01T00:40:00Z"), 50.0);
            ts.add(Instant.parse("2026-01-01T00:50:00Z"), 40.0);
            return ts;
        }

        @Test
        @DisplayName("SUM should sum all values in bucket")
        void sumShouldSumValues() {
            TimeSeries result = TimeBucketUtil.bucket(singleBucketSeries(), TimeBucket.HOUR, UTC, AggregationType.SUM);

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getValues()[0]).isCloseTo(150.0, within(1e-10));
        }

        @Test
        @DisplayName("AVG should average all values in bucket")
        void avgShouldAverageValues() {
            TimeSeries result = TimeBucketUtil.bucket(singleBucketSeries(), TimeBucket.HOUR, UTC, AggregationType.AVG);

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getValues()[0]).isCloseTo(30.0, within(1e-10));
        }

        @Test
        @DisplayName("MIN should find minimum in bucket")
        void minShouldFindMinimum() {
            TimeSeries result = TimeBucketUtil.bucket(singleBucketSeries(), TimeBucket.HOUR, UTC, AggregationType.MIN);

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getValues()[0]).isCloseTo(10.0, within(1e-10));
        }

        @Test
        @DisplayName("MAX should find maximum in bucket")
        void maxShouldFindMaximum() {
            TimeSeries result = TimeBucketUtil.bucket(singleBucketSeries(), TimeBucket.HOUR, UTC, AggregationType.MAX);

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getValues()[0]).isCloseTo(50.0, within(1e-10));
        }

        @Test
        @DisplayName("FIRST should return earliest value in bucket")
        void firstShouldReturnEarliestValue() {
            TimeSeries result = TimeBucketUtil.bucket(singleBucketSeries(), TimeBucket.HOUR, UTC, AggregationType.FIRST);

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getValues()[0]).isCloseTo(10.0, within(1e-10));
        }

        @Test
        @DisplayName("LAST should return latest value in bucket")
        void lastShouldReturnLatestValue() {
            TimeSeries result = TimeBucketUtil.bucket(singleBucketSeries(), TimeBucket.HOUR, UTC, AggregationType.LAST);

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getValues()[0]).isCloseTo(40.0, within(1e-10));
        }

        @Test
        @DisplayName("COUNT should return number of points in bucket")
        void countShouldReturnPointCount() {
            TimeSeries result = TimeBucketUtil.bucket(singleBucketSeries(), TimeBucket.HOUR, UTC, AggregationType.COUNT);

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getValues()[0]).isCloseTo(5.0, within(1e-10));
        }
    }

    @Nested
    @DisplayName("Null Argument Tests")
    class NullArgumentTests {

        @Test
        @DisplayName("should throw on null TimeSeries for calendar bucket")
        void shouldThrowOnNullTsCalendar() {
            assertThatNullPointerException()
                    .isThrownBy(() -> TimeBucketUtil.bucket(null, TimeBucket.HOUR, UTC, AggregationType.AVG));
        }

        @Test
        @DisplayName("should throw on null bucket")
        void shouldThrowOnNullBucket() {
            TimeSeries ts = new TimeSeries("ts");
            ts.add(Instant.now(), 1.0);

            assertThatNullPointerException()
                    .isThrownBy(() -> TimeBucketUtil.bucket(ts, (TimeBucket) null, UTC, AggregationType.AVG));
        }

        @Test
        @DisplayName("should throw on null zone")
        void shouldThrowOnNullZone() {
            TimeSeries ts = new TimeSeries("ts");
            ts.add(Instant.now(), 1.0);

            assertThatNullPointerException()
                    .isThrownBy(() -> TimeBucketUtil.bucket(ts, TimeBucket.HOUR, null, AggregationType.AVG));
        }

        @Test
        @DisplayName("should throw on null agg type")
        void shouldThrowOnNullAggType() {
            TimeSeries ts = new TimeSeries("ts");
            ts.add(Instant.now(), 1.0);

            assertThatNullPointerException()
                    .isThrownBy(() -> TimeBucketUtil.bucket(ts, TimeBucket.HOUR, UTC, null));
        }

        @Test
        @DisplayName("should throw on null TimeSeries for fixed bucket")
        void shouldThrowOnNullTsFixed() {
            assertThatNullPointerException()
                    .isThrownBy(() -> TimeBucketUtil.bucket(null, Duration.ofMinutes(5), Instant.EPOCH, AggregationType.AVG));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("single point should produce single bucket")
        void singlePointShouldProduceSingleBucket() {
            TimeSeries ts = new TimeSeries("single");
            ts.add(Instant.parse("2026-06-15T12:30:00Z"), 42.0);

            TimeSeries result = TimeBucketUtil.bucket(ts, TimeBucket.HOUR, UTC, AggregationType.AVG);

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getValues()[0]).isCloseTo(42.0, within(1e-10));
        }

        @Test
        @DisplayName("result series name should contain _bucketed suffix")
        void resultNameShouldContainSuffix() {
            TimeSeries ts = new TimeSeries("metrics");
            ts.add(Instant.now(), 1.0);

            TimeSeries result = TimeBucketUtil.bucket(ts, TimeBucket.DAY, UTC, AggregationType.AVG);

            assertThat(result.getName()).endsWith("_bucketed");
        }

        @Test
        @DisplayName("buckets should be ordered by time")
        void bucketsShouldBeOrderedByTime() {
            TimeSeries ts = new TimeSeries("ordered");
            ts.add(Instant.parse("2026-01-03T00:00:00Z"), 3.0);
            ts.add(Instant.parse("2026-01-01T00:00:00Z"), 1.0);
            ts.add(Instant.parse("2026-01-02T00:00:00Z"), 2.0);

            TimeSeries result = TimeBucketUtil.bucket(ts, TimeBucket.DAY, UTC, AggregationType.AVG);

            List<DataPoint> points = result.getPoints();
            assertThat(points.get(0).value()).isCloseTo(1.0, within(1e-10));
            assertThat(points.get(1).value()).isCloseTo(2.0, within(1e-10));
            assertThat(points.get(2).value()).isCloseTo(3.0, within(1e-10));
        }
    }
}
