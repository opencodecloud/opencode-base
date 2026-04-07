package cloud.opencode.base.date;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DateStreams}.
 */
@DisplayName("DateStreams")
class DateStreamsTest {

    @Nested
    @DisplayName("Days")
    class Days {

        @Test
        @DisplayName("days() produces correct day sequence")
        void days_producesCorrectSequence() {
            List<LocalDate> dates = DateStreams.days(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 5)
            ).toList();

            assertThat(dates).containsExactly(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 2),
                    LocalDate.of(2026, 1, 3),
                    LocalDate.of(2026, 1, 4)
            );
        }

        @Test
        @DisplayName("days() with same start and end returns empty stream")
        void days_sameStartEnd_returnsEmpty() {
            LocalDate date = LocalDate.of(2026, 3, 15);
            assertThat(DateStreams.days(date, date).toList()).isEmpty();
        }

        @Test
        @DisplayName("days() with start after end throws IllegalArgumentException")
        void days_startAfterEnd_throws() {
            assertThatThrownBy(() -> DateStreams.days(
                    LocalDate.of(2026, 3, 15),
                    LocalDate.of(2026, 3, 10)
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("days() with step produces correct sequence")
        void daysWithStep_producesCorrectSequence() {
            List<LocalDate> dates = DateStreams.days(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 10),
                    Period.ofDays(3)
            ).toList();

            assertThat(dates).containsExactly(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 4),
                    LocalDate.of(2026, 1, 7)
            );
        }

        @Test
        @DisplayName("days() with weekly step")
        void daysWithWeeklyStep() {
            List<LocalDate> dates = DateStreams.days(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 2, 1),
                    Period.ofWeeks(1)
            ).toList();

            assertThat(dates).hasSize(5);
            assertThat(dates.getFirst()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(dates.getLast()).isEqualTo(LocalDate.of(2026, 1, 29));
        }

        @Test
        @DisplayName("days() with monthly step")
        void daysWithMonthlyStep() {
            List<LocalDate> dates = DateStreams.days(
                    LocalDate.of(2026, 1, 15),
                    LocalDate.of(2026, 7, 1),
                    Period.ofMonths(1)
            ).toList();

            assertThat(dates).containsExactly(
                    LocalDate.of(2026, 1, 15),
                    LocalDate.of(2026, 2, 15),
                    LocalDate.of(2026, 3, 15),
                    LocalDate.of(2026, 4, 15),
                    LocalDate.of(2026, 5, 15),
                    LocalDate.of(2026, 6, 15)
            );
        }

        @Test
        @DisplayName("days() with zero step throws OpenDateException")
        void daysWithZeroStep_throws() {
            assertThatThrownBy(() -> DateStreams.days(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 10),
                    Period.ZERO
            )).isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("days() with negative step throws OpenDateException")
        void daysWithNegativeStep_throws() {
            assertThatThrownBy(() -> DateStreams.days(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 10),
                    Period.ofDays(-1)
            )).isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("days() with null start throws NullPointerException")
        void days_nullStart_throws() {
            assertThatThrownBy(() -> DateStreams.days(null, LocalDate.of(2026, 1, 5)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("days() with null end throws NullPointerException")
        void days_nullEnd_throws() {
            assertThatThrownBy(() -> DateStreams.days(LocalDate.of(2026, 1, 1), (LocalDate) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("days() with null step throws NullPointerException")
        void days_nullStep_throws() {
            assertThatThrownBy(() -> DateStreams.days(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 10),
                    null
            )).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Weeks")
    class Weeks {

        @Test
        @DisplayName("weeks() produces Monday-aligned dates")
        void weeks_producesMondayAligned() {
            List<LocalDate> weeks = DateStreams.weeks(
                    LocalDate.of(2026, 1, 1),   // Thursday
                    LocalDate.of(2026, 2, 1)
            ).toList();

            assertThat(weeks).isNotEmpty();
            assertThat(weeks).allSatisfy(date ->
                    assertThat(date.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY)
            );
        }

        @Test
        @DisplayName("weeks() starting on Monday includes that Monday")
        void weeks_startingOnMonday_includesIt() {
            // 2026-01-05 is Monday
            List<LocalDate> weeks = DateStreams.weeks(
                    LocalDate.of(2026, 1, 5),
                    LocalDate.of(2026, 1, 20)
            ).toList();

            assertThat(weeks).containsExactly(
                    LocalDate.of(2026, 1, 5),
                    LocalDate.of(2026, 1, 12),
                    LocalDate.of(2026, 1, 19)
            );
        }

        @Test
        @DisplayName("weeks() starting on non-Monday skips to next Monday")
        void weeks_startingOnNonMonday_skipsToNextMonday() {
            // 2026-01-01 is Thursday, next Monday is 2026-01-05
            List<LocalDate> weeks = DateStreams.weeks(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 12)
            ).toList();

            assertThat(weeks.getFirst()).isEqualTo(LocalDate.of(2026, 1, 5));
        }

        @Test
        @DisplayName("weeks() with empty range returns empty stream")
        void weeks_emptyRange_returnsEmpty() {
            // End is before first Monday after start
            assertThat(DateStreams.weeks(
                    LocalDate.of(2026, 1, 6),   // Tuesday
                    LocalDate.of(2026, 1, 10)    // Saturday, before next Monday
            ).toList()).isEmpty();
        }

        @Test
        @DisplayName("weeks() with null start throws NullPointerException")
        void weeks_nullStart_throws() {
            assertThatThrownBy(() -> DateStreams.weeks(null, LocalDate.of(2026, 2, 1)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("weeks() with null end throws NullPointerException")
        void weeks_nullEnd_throws() {
            assertThatThrownBy(() -> DateStreams.weeks(LocalDate.of(2026, 1, 1), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Months")
    class Months {

        @Test
        @DisplayName("months(YearMonth) produces correct sequence")
        void monthsYearMonth_producesCorrectSequence() {
            List<YearMonth> months = DateStreams.months(
                    YearMonth.of(2026, 1),
                    YearMonth.of(2026, 5)
            ).toList();

            assertThat(months).containsExactly(
                    YearMonth.of(2026, 1),
                    YearMonth.of(2026, 2),
                    YearMonth.of(2026, 3),
                    YearMonth.of(2026, 4)
            );
        }

        @Test
        @DisplayName("months(YearMonth) crossing year boundary")
        void monthsYearMonth_crossingYearBoundary() {
            List<YearMonth> months = DateStreams.months(
                    YearMonth.of(2025, 11),
                    YearMonth.of(2026, 3)
            ).toList();

            assertThat(months).containsExactly(
                    YearMonth.of(2025, 11),
                    YearMonth.of(2025, 12),
                    YearMonth.of(2026, 1),
                    YearMonth.of(2026, 2)
            );
        }

        @Test
        @DisplayName("months(YearMonth) with same start and end returns empty stream")
        void monthsYearMonth_sameStartEnd_returnsEmpty() {
            YearMonth ym = YearMonth.of(2026, 6);
            assertThat(DateStreams.months(ym, ym).toList()).isEmpty();
        }

        @Test
        @DisplayName("months(LocalDate) produces correct sequence")
        void monthsLocalDate_producesCorrectSequence() {
            List<YearMonth> months = DateStreams.months(
                    LocalDate.of(2026, 3, 15),
                    LocalDate.of(2026, 6, 10)
            ).toList();

            assertThat(months).containsExactly(
                    YearMonth.of(2026, 3),
                    YearMonth.of(2026, 4),
                    YearMonth.of(2026, 5)
            );
        }

        @Test
        @DisplayName("months() with null args throws NullPointerException")
        void months_nullArgs_throws() {
            assertThatThrownBy(() -> DateStreams.months((YearMonth) null, YearMonth.of(2026, 5)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> DateStreams.months(YearMonth.of(2026, 1), (YearMonth) null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> DateStreams.months((LocalDate) null, LocalDate.of(2026, 5, 1)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> DateStreams.months(LocalDate.of(2026, 1, 1), (LocalDate) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Hours")
    class Hours {

        @Test
        @DisplayName("hours() produces correct sequence")
        void hours_producesCorrectSequence() {
            List<LocalDateTime> hours = DateStreams.hours(
                    LocalDateTime.of(2026, 1, 1, 10, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0)
            ).toList();

            assertThat(hours).containsExactly(
                    LocalDateTime.of(2026, 1, 1, 10, 0),
                    LocalDateTime.of(2026, 1, 1, 11, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    LocalDateTime.of(2026, 1, 1, 13, 0)
            );
        }

        @Test
        @DisplayName("hours() crossing midnight")
        void hours_crossingMidnight() {
            List<LocalDateTime> hours = DateStreams.hours(
                    LocalDateTime.of(2026, 1, 1, 22, 0),
                    LocalDateTime.of(2026, 1, 2, 2, 0)
            ).toList();

            assertThat(hours).hasSize(4);
            assertThat(hours.getFirst()).isEqualTo(LocalDateTime.of(2026, 1, 1, 22, 0));
            assertThat(hours.getLast()).isEqualTo(LocalDateTime.of(2026, 1, 2, 1, 0));
        }

        @Test
        @DisplayName("hours() with empty range returns empty stream")
        void hours_emptyRange_returnsEmpty() {
            LocalDateTime dt = LocalDateTime.of(2026, 1, 1, 12, 0);
            assertThat(DateStreams.hours(dt, dt).toList()).isEmpty();
        }

        @Test
        @DisplayName("hours() with null args throws NullPointerException")
        void hours_nullArgs_throws() {
            assertThatThrownBy(() -> DateStreams.hours(null, LocalDateTime.of(2026, 1, 1, 12, 0)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> DateStreams.hours(LocalDateTime.of(2026, 1, 1, 10, 0), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Iterate")
    class Iterate {

        @Test
        @DisplayName("iterate() with 30-minute step")
        void iterate_30minStep() {
            List<LocalDateTime> result = DateStreams.iterate(
                    LocalDateTime.of(2026, 1, 1, 10, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    Duration.ofMinutes(30)
            ).toList();

            assertThat(result).containsExactly(
                    LocalDateTime.of(2026, 1, 1, 10, 0),
                    LocalDateTime.of(2026, 1, 1, 10, 30),
                    LocalDateTime.of(2026, 1, 1, 11, 0),
                    LocalDateTime.of(2026, 1, 1, 11, 30)
            );
        }

        @Test
        @DisplayName("iterate() with 15-second step")
        void iterate_15secStep() {
            List<LocalDateTime> result = DateStreams.iterate(
                    LocalDateTime.of(2026, 1, 1, 0, 0, 0),
                    LocalDateTime.of(2026, 1, 1, 0, 1, 0),
                    Duration.ofSeconds(15)
            ).toList();

            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("iterate() with empty range returns empty stream")
        void iterate_emptyRange_returnsEmpty() {
            LocalDateTime dt = LocalDateTime.of(2026, 1, 1, 12, 0);
            assertThat(DateStreams.iterate(dt, dt, Duration.ofHours(1)).toList()).isEmpty();
        }

        @Test
        @DisplayName("iterate() with zero duration throws OpenDateException")
        void iterate_zeroDuration_throws() {
            assertThatThrownBy(() -> DateStreams.iterate(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 2, 0, 0),
                    Duration.ZERO
            )).isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("iterate() with negative duration throws OpenDateException")
        void iterate_negativeDuration_throws() {
            assertThatThrownBy(() -> DateStreams.iterate(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 2, 0, 0),
                    Duration.ofHours(-1)
            )).isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("iterate() with null args throws NullPointerException")
        void iterate_nullArgs_throws() {
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 1, 2, 0, 0);
            Duration step = Duration.ofHours(1);

            assertThatThrownBy(() -> DateStreams.iterate(null, end, step))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> DateStreams.iterate(start, null, step))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> DateStreams.iterate(start, end, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Weekends and Weekdays")
    class WeekendsAndWeekdays {

        @Test
        @DisplayName("weekends() returns only Saturday and Sunday")
        void weekends_onlySaturdayAndSunday() {
            // 2026-01-05 (Mon) to 2026-01-19 (Mon) - 2 full weeks
            List<LocalDate> weekends = DateStreams.weekends(
                    LocalDate.of(2026, 1, 5),
                    LocalDate.of(2026, 1, 19)
            ).toList();

            assertThat(weekends).hasSize(4); // 2 Saturdays + 2 Sundays
            assertThat(weekends).allSatisfy(date ->
                    assertThat(date.getDayOfWeek()).isIn(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            );
            assertThat(weekends).containsExactly(
                    LocalDate.of(2026, 1, 10),
                    LocalDate.of(2026, 1, 11),
                    LocalDate.of(2026, 1, 17),
                    LocalDate.of(2026, 1, 18)
            );
        }

        @Test
        @DisplayName("weekdays() returns only Monday through Friday")
        void weekdays_onlyMondayThroughFriday() {
            // One full week: Mon 2026-01-05 to Mon 2026-01-12
            List<LocalDate> weekdays = DateStreams.weekdays(
                    LocalDate.of(2026, 1, 5),
                    LocalDate.of(2026, 1, 12)
            ).toList();

            assertThat(weekdays).hasSize(5);
            assertThat(weekdays).allSatisfy(date ->
                    assertThat(date.getDayOfWeek()).isNotIn(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            );
        }

        @Test
        @DisplayName("weekends() with no weekends in range returns empty stream")
        void weekends_noWeekendsInRange_returnsEmpty() {
            // Mon to Fri
            assertThat(DateStreams.weekends(
                    LocalDate.of(2026, 1, 5),
                    LocalDate.of(2026, 1, 10)
            ).toList()).isEmpty();
        }

        @Test
        @DisplayName("weekdays() with no weekdays in range returns empty stream")
        void weekdays_noWeekdaysInRange_returnsEmpty() {
            // Sat to Mon (exclusive)
            assertThat(DateStreams.weekdays(
                    LocalDate.of(2026, 1, 10),
                    LocalDate.of(2026, 1, 12)
            ).toList()).isEmpty();
        }

        @Test
        @DisplayName("weekends() with empty range returns empty stream")
        void weekends_emptyRange_returnsEmpty() {
            LocalDate date = LocalDate.of(2026, 1, 10);
            assertThat(DateStreams.weekends(date, date).toList()).isEmpty();
        }

        @Test
        @DisplayName("weekdays() with empty range returns empty stream")
        void weekdays_emptyRange_returnsEmpty() {
            LocalDate date = LocalDate.of(2026, 1, 5);
            assertThat(DateStreams.weekdays(date, date).toList()).isEmpty();
        }

        @Test
        @DisplayName("weekends() with null args throws NullPointerException")
        void weekends_nullArgs_throws() {
            assertThatThrownBy(() -> DateStreams.weekends(null, LocalDate.of(2026, 1, 10)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> DateStreams.weekends(LocalDate.of(2026, 1, 1), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("weekdays() with null args throws NullPointerException")
        void weekdays_nullArgs_throws() {
            assertThatThrownBy(() -> DateStreams.weekdays(null, LocalDate.of(2026, 1, 10)))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> DateStreams.weekdays(LocalDate.of(2026, 1, 1), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
