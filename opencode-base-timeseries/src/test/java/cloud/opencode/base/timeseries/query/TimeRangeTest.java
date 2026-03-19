package cloud.opencode.base.timeseries.query;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * TimeRangeTest Tests
 * TimeRangeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("TimeRange Tests")
class TimeRangeTest {

    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create time range from instants")
        void ofShouldCreateTimeRangeFromInstants() {
            Instant from = now.minusSeconds(100);
            Instant to = now;
            TimeRange range = TimeRange.of(from, to);

            assertThat(range.from()).isEqualTo(from);
            assertThat(range.to()).isEqualTo(to);
        }

        @Test
        @DisplayName("ofMillis should create time range from milliseconds")
        void ofMillisShouldCreateTimeRangeFromMilliseconds() {
            long fromMillis = 1000000L;
            long toMillis = 2000000L;
            TimeRange range = TimeRange.ofMillis(fromMillis, toMillis);

            assertThat(range.from()).isEqualTo(Instant.ofEpochMilli(fromMillis));
            assertThat(range.to()).isEqualTo(Instant.ofEpochMilli(toMillis));
        }

        @Test
        @DisplayName("last should create range for last duration")
        void lastShouldCreateRangeForLastDuration() {
            Instant before = Instant.now();
            TimeRange range = TimeRange.last(Duration.ofHours(1));
            Instant after = Instant.now();

            assertThat(range.to()).isBetween(before, after.plusMillis(1));
            assertThat(range.from()).isBefore(range.to());
            assertThat(Duration.between(range.from(), range.to())).isCloseTo(Duration.ofHours(1), Duration.ofMillis(100));
        }

        @Test
        @DisplayName("today should create range for current day")
        void todayShouldCreateRangeForCurrentDay() {
            TimeRange range = TimeRange.today();

            assertThat(range.from()).isBeforeOrEqualTo(now);
            assertThat(range.to()).isAfterOrEqualTo(now);
        }

        @Test
        @DisplayName("thisHour should create range for current hour")
        void thisHourShouldCreateRangeForCurrentHour() {
            TimeRange range = TimeRange.thisHour();

            assertThat(range.from()).isBeforeOrEqualTo(now);
            assertThat(range.to()).isAfterOrEqualTo(now);
            assertThat(range.duration()).isLessThanOrEqualTo(Duration.ofHours(1));
        }
    }

    @Nested
    @DisplayName("Duration Tests")
    class DurationTests {

        @Test
        @DisplayName("duration should return time span")
        void durationShouldReturnTimeSpan() {
            TimeRange range = TimeRange.of(
                now.minusSeconds(3600),
                now
            );

            assertThat(range.duration()).isEqualTo(Duration.ofHours(1));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("isValid should return true for valid range")
        void isValidShouldReturnTrueForValidRange() {
            TimeRange range = TimeRange.of(now.minusSeconds(100), now);

            assertThat(range.isValid()).isTrue();
        }

        @Test
        @DisplayName("isValid should return false for invalid range")
        void isValidShouldReturnFalseForInvalidRange() {
            TimeRange range = TimeRange.of(now, now.minusSeconds(100));

            assertThat(range.isValid()).isFalse();
        }

        @Test
        @DisplayName("isEmpty should return true for zero duration")
        void isEmptyShouldReturnTrueForZeroDuration() {
            TimeRange range = TimeRange.of(now, now);

            assertThat(range.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty should return false for non-zero duration")
        void isEmptyShouldReturnFalseForNonZeroDuration() {
            TimeRange range = TimeRange.of(now.minusSeconds(1), now);

            assertThat(range.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("Contains Tests")
    class ContainsTests {

        @Test
        @DisplayName("contains should return true for instant in range")
        void containsShouldReturnTrueForInstantInRange() {
            TimeRange range = TimeRange.of(now.minusSeconds(100), now);

            assertThat(range.contains(now.minusSeconds(50))).isTrue();
        }

        @Test
        @DisplayName("contains should return false for instant outside range")
        void containsShouldReturnFalseForInstantOutsideRange() {
            TimeRange range = TimeRange.of(now.minusSeconds(100), now);

            assertThat(range.contains(now.plusSeconds(50))).isFalse();
        }

        @Test
        @DisplayName("contains should return true for boundary instants")
        void containsShouldReturnTrueForBoundaryInstants() {
            TimeRange range = TimeRange.of(now.minusSeconds(100), now);

            assertThat(range.contains(now.minusSeconds(100))).isTrue();
            assertThat(range.contains(now)).isTrue();
        }
    }

    @Nested
    @DisplayName("Overlaps Tests")
    class OverlapsTests {

        @Test
        @DisplayName("overlaps should return true for overlapping ranges")
        void overlapsShouldReturnTrueForOverlappingRanges() {
            TimeRange range1 = TimeRange.of(now.minusSeconds(100), now);
            TimeRange range2 = TimeRange.of(now.minusSeconds(50), now.plusSeconds(50));

            assertThat(range1.overlaps(range2)).isTrue();
            assertThat(range2.overlaps(range1)).isTrue();
        }

        @Test
        @DisplayName("overlaps should return false for non-overlapping ranges")
        void overlapsShouldReturnFalseForNonOverlappingRanges() {
            TimeRange range1 = TimeRange.of(now.minusSeconds(200), now.minusSeconds(100));
            TimeRange range2 = TimeRange.of(now.minusSeconds(50), now);

            assertThat(range1.overlaps(range2)).isFalse();
            assertThat(range2.overlaps(range1)).isFalse();
        }

        @Test
        @DisplayName("overlaps should return true for adjacent ranges")
        void overlapsShouldReturnTrueForAdjacentRanges() {
            TimeRange range1 = TimeRange.of(now.minusSeconds(100), now.minusSeconds(50));
            TimeRange range2 = TimeRange.of(now.minusSeconds(50), now);

            assertThat(range1.overlaps(range2)).isTrue();
        }
    }

    @Nested
    @DisplayName("Extend Tests")
    class ExtendTests {

        @Test
        @DisplayName("extend should extend range by duration")
        void extendShouldExtendRangeByDuration() {
            TimeRange range = TimeRange.of(now.minusSeconds(100), now);
            TimeRange extended = range.extend(Duration.ofSeconds(50));

            assertThat(extended.from()).isEqualTo(now.minusSeconds(150));
            assertThat(extended.to()).isEqualTo(now.plusSeconds(50));
        }

        @Test
        @DisplayName("extend should handle zero duration")
        void extendShouldHandleZeroDuration() {
            TimeRange range = TimeRange.of(now.minusSeconds(100), now);
            TimeRange extended = range.extend(Duration.ZERO);

            assertThat(extended).isEqualTo(range);
        }
    }

    @Nested
    @DisplayName("Shift Tests")
    class ShiftTests {

        @Test
        @DisplayName("shift should move range by duration")
        void shiftShouldMoveRangeByDuration() {
            TimeRange range = TimeRange.of(now.minusSeconds(100), now);
            TimeRange shifted = range.shift(Duration.ofSeconds(50));

            assertThat(shifted.from()).isEqualTo(now.minusSeconds(50));
            assertThat(shifted.to()).isEqualTo(now.plusSeconds(50));
            assertThat(shifted.duration()).isEqualTo(range.duration());
        }

        @Test
        @DisplayName("shift should support negative duration")
        void shiftShouldSupportNegativeDuration() {
            TimeRange range = TimeRange.of(now.minusSeconds(100), now);
            TimeRange shifted = range.shift(Duration.ofSeconds(-50));

            assertThat(shifted.from()).isEqualTo(now.minusSeconds(150));
            assertThat(shifted.to()).isEqualTo(now.minusSeconds(50));
        }
    }

    @Nested
    @DisplayName("Record Method Tests")
    class RecordMethodTests {

        @Test
        @DisplayName("equals should compare from and to")
        void equalsShouldCompareFromAndTo() {
            TimeRange r1 = TimeRange.of(now.minusSeconds(100), now);
            TimeRange r2 = TimeRange.of(now.minusSeconds(100), now);
            TimeRange r3 = TimeRange.of(now.minusSeconds(50), now);

            assertThat(r1).isEqualTo(r2);
            assertThat(r1).isNotEqualTo(r3);
        }

        @Test
        @DisplayName("hashCode should be consistent with equals")
        void hashCodeShouldBeConsistentWithEquals() {
            TimeRange r1 = TimeRange.of(now.minusSeconds(100), now);
            TimeRange r2 = TimeRange.of(now.minusSeconds(100), now);

            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("toString should contain from and to")
        void toStringShouldContainFromAndTo() {
            Instant from = now.minusSeconds(100);
            TimeRange range = TimeRange.of(from, now);
            String str = range.toString();

            assertThat(str).contains(from.toString());
            assertThat(str).contains(now.toString());
        }
    }
}
