package cloud.opencode.base.date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DatePredicates}.
 */
@DisplayName("DatePredicates")
class DatePredicatesTest {

    @Nested
    @DisplayName("Future and Past")
    class FutureAndPast {

        @Test
        @DisplayName("isFuture(LocalDate) returns true for future date")
        void isFutureLocalDate_futureDate_returnsTrue() {
            LocalDate future = LocalDate.now().plusDays(1);
            assertThat(DatePredicates.isFuture(future)).isTrue();
        }

        @Test
        @DisplayName("isFuture(LocalDate) returns false for today")
        void isFutureLocalDate_today_returnsFalse() {
            assertThat(DatePredicates.isFuture(LocalDate.now())).isFalse();
        }

        @Test
        @DisplayName("isFuture(LocalDate) returns false for past date")
        void isFutureLocalDate_pastDate_returnsFalse() {
            LocalDate past = LocalDate.now().minusDays(1);
            assertThat(DatePredicates.isFuture(past)).isFalse();
        }

        @Test
        @DisplayName("isFuture(LocalDate) returns false for null")
        void isFutureLocalDate_null_returnsFalse() {
            assertThat(DatePredicates.isFuture((LocalDate) null)).isFalse();
        }

        @Test
        @DisplayName("isFuture(LocalDateTime) returns true for future date-time")
        void isFutureLocalDateTime_future_returnsTrue() {
            LocalDateTime future = LocalDateTime.now().plusHours(1);
            assertThat(DatePredicates.isFuture(future)).isTrue();
        }

        @Test
        @DisplayName("isFuture(LocalDateTime) returns false for null")
        void isFutureLocalDateTime_null_returnsFalse() {
            assertThat(DatePredicates.isFuture((LocalDateTime) null)).isFalse();
        }

        @Test
        @DisplayName("isPast(LocalDate) returns true for past date")
        void isPastLocalDate_pastDate_returnsTrue() {
            LocalDate past = LocalDate.now().minusDays(1);
            assertThat(DatePredicates.isPast(past)).isTrue();
        }

        @Test
        @DisplayName("isPast(LocalDate) returns false for today")
        void isPastLocalDate_today_returnsFalse() {
            assertThat(DatePredicates.isPast(LocalDate.now())).isFalse();
        }

        @Test
        @DisplayName("isPast(LocalDate) returns false for future date")
        void isPastLocalDate_futureDate_returnsFalse() {
            assertThat(DatePredicates.isPast(LocalDate.now().plusDays(1))).isFalse();
        }

        @Test
        @DisplayName("isPast(LocalDate) returns false for null")
        void isPastLocalDate_null_returnsFalse() {
            assertThat(DatePredicates.isPast((LocalDate) null)).isFalse();
        }

        @Test
        @DisplayName("isPast(LocalDateTime) returns true for past date-time")
        void isPastLocalDateTime_past_returnsTrue() {
            LocalDateTime past = LocalDateTime.now().minusHours(1);
            assertThat(DatePredicates.isPast(past)).isTrue();
        }

        @Test
        @DisplayName("isPast(LocalDateTime) returns false for null")
        void isPastLocalDateTime_null_returnsFalse() {
            assertThat(DatePredicates.isPast((LocalDateTime) null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Same Comparisons")
    class SameComparisons {

        @Test
        @DisplayName("isSameDay(LocalDate) returns true for equal dates")
        void isSameDayLocalDate_equalDates_returnsTrue() {
            LocalDate date = LocalDate.of(2026, 6, 15);
            assertThat(DatePredicates.isSameDay(date, LocalDate.of(2026, 6, 15))).isTrue();
        }

        @Test
        @DisplayName("isSameDay(LocalDate) returns false for different dates")
        void isSameDayLocalDate_differentDates_returnsFalse() {
            assertThat(DatePredicates.isSameDay(
                    LocalDate.of(2026, 6, 15),
                    LocalDate.of(2026, 6, 16))).isFalse();
        }

        @Test
        @DisplayName("isSameDay(LocalDate) returns false when either is null")
        void isSameDayLocalDate_null_returnsFalse() {
            assertThat(DatePredicates.isSameDay((LocalDate) null, LocalDate.now())).isFalse();
            assertThat(DatePredicates.isSameDay(LocalDate.now(), (LocalDate) null)).isFalse();
            assertThat(DatePredicates.isSameDay((LocalDate) null, (LocalDate) null)).isFalse();
        }

        @Test
        @DisplayName("isSameDay(LocalDateTime) returns true for same day different times")
        void isSameDayLocalDateTime_sameDayDifferentTimes_returnsTrue() {
            LocalDateTime morning = LocalDateTime.of(2026, 6, 15, 8, 0);
            LocalDateTime evening = LocalDateTime.of(2026, 6, 15, 20, 0);
            assertThat(DatePredicates.isSameDay(morning, evening)).isTrue();
        }

        @Test
        @DisplayName("isSameDay(LocalDateTime) returns false for different days")
        void isSameDayLocalDateTime_differentDays_returnsFalse() {
            LocalDateTime a = LocalDateTime.of(2026, 6, 15, 23, 59);
            LocalDateTime b = LocalDateTime.of(2026, 6, 16, 0, 0);
            assertThat(DatePredicates.isSameDay(a, b)).isFalse();
        }

        @Test
        @DisplayName("isSameDay(LocalDateTime) returns false when null")
        void isSameDayLocalDateTime_null_returnsFalse() {
            assertThat(DatePredicates.isSameDay((LocalDateTime) null, LocalDateTime.now())).isFalse();
        }

        @Test
        @DisplayName("isSameMonth returns true for same year-month")
        void isSameMonth_sameYearMonth_returnsTrue() {
            assertThat(DatePredicates.isSameMonth(
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 31))).isTrue();
        }

        @Test
        @DisplayName("isSameMonth returns false for different months")
        void isSameMonth_differentMonth_returnsFalse() {
            assertThat(DatePredicates.isSameMonth(
                    LocalDate.of(2026, 3, 31),
                    LocalDate.of(2026, 4, 1))).isFalse();
        }

        @Test
        @DisplayName("isSameMonth returns false for same month different year")
        void isSameMonth_sameMonthDifferentYear_returnsFalse() {
            assertThat(DatePredicates.isSameMonth(
                    LocalDate.of(2025, 3, 15),
                    LocalDate.of(2026, 3, 15))).isFalse();
        }

        @Test
        @DisplayName("isSameMonth returns false for null")
        void isSameMonth_null_returnsFalse() {
            assertThat(DatePredicates.isSameMonth(null, LocalDate.now())).isFalse();
        }

        @Test
        @DisplayName("isSameYear returns true for same year")
        void isSameYear_sameYear_returnsTrue() {
            assertThat(DatePredicates.isSameYear(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31))).isTrue();
        }

        @Test
        @DisplayName("isSameYear returns false for different years")
        void isSameYear_differentYears_returnsFalse() {
            assertThat(DatePredicates.isSameYear(
                    LocalDate.of(2025, 12, 31),
                    LocalDate.of(2026, 1, 1))).isFalse();
        }

        @Test
        @DisplayName("isSameYear returns false for null")
        void isSameYear_null_returnsFalse() {
            assertThat(DatePredicates.isSameYear(null, LocalDate.now())).isFalse();
        }

        @Test
        @DisplayName("isSameWeek returns true for dates in same ISO week")
        void isSameWeek_sameWeek_returnsTrue() {
            // 2026-01-05 is Monday, 2026-01-11 is Sunday - same ISO week
            assertThat(DatePredicates.isSameWeek(
                    LocalDate.of(2026, 1, 5),
                    LocalDate.of(2026, 1, 11))).isTrue();
        }

        @Test
        @DisplayName("isSameWeek returns false for dates in different weeks")
        void isSameWeek_differentWeek_returnsFalse() {
            // 2026-01-11 (Sunday) and 2026-01-12 (Monday) are in different ISO weeks
            assertThat(DatePredicates.isSameWeek(
                    LocalDate.of(2026, 1, 11),
                    LocalDate.of(2026, 1, 12))).isFalse();
        }

        @Test
        @DisplayName("isSameWeek returns false for null")
        void isSameWeek_null_returnsFalse() {
            assertThat(DatePredicates.isSameWeek(null, LocalDate.now())).isFalse();
            assertThat(DatePredicates.isSameWeek(LocalDate.now(), null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Day Position")
    class DayPosition {

        @Test
        @DisplayName("isFirstDayOfMonth returns true for day 1")
        void isFirstDayOfMonth_day1_returnsTrue() {
            assertThat(DatePredicates.isFirstDayOfMonth(LocalDate.of(2026, 3, 1))).isTrue();
        }

        @Test
        @DisplayName("isFirstDayOfMonth returns false for other days")
        void isFirstDayOfMonth_notDay1_returnsFalse() {
            assertThat(DatePredicates.isFirstDayOfMonth(LocalDate.of(2026, 3, 2))).isFalse();
        }

        @Test
        @DisplayName("isFirstDayOfMonth returns false for null")
        void isFirstDayOfMonth_null_returnsFalse() {
            assertThat(DatePredicates.isFirstDayOfMonth(null)).isFalse();
        }

        @Test
        @DisplayName("isLastDayOfMonth returns true for last day of month")
        void isLastDayOfMonth_lastDay_returnsTrue() {
            assertThat(DatePredicates.isLastDayOfMonth(LocalDate.of(2026, 2, 28))).isTrue();
            assertThat(DatePredicates.isLastDayOfMonth(LocalDate.of(2024, 2, 29))).isTrue();
            assertThat(DatePredicates.isLastDayOfMonth(LocalDate.of(2026, 1, 31))).isTrue();
            assertThat(DatePredicates.isLastDayOfMonth(LocalDate.of(2026, 4, 30))).isTrue();
        }

        @Test
        @DisplayName("isLastDayOfMonth returns false for non-last day")
        void isLastDayOfMonth_notLastDay_returnsFalse() {
            assertThat(DatePredicates.isLastDayOfMonth(LocalDate.of(2026, 1, 30))).isFalse();
        }

        @Test
        @DisplayName("isLastDayOfMonth returns false for null")
        void isLastDayOfMonth_null_returnsFalse() {
            assertThat(DatePredicates.isLastDayOfMonth(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Day of Week")
    class DayOfWeekTests {

        // 2026-01-05 is Monday
        private static final LocalDate MONDAY = LocalDate.of(2026, 1, 5);
        private static final LocalDate TUESDAY = LocalDate.of(2026, 1, 6);
        private static final LocalDate WEDNESDAY = LocalDate.of(2026, 1, 7);
        private static final LocalDate THURSDAY = LocalDate.of(2026, 1, 8);
        private static final LocalDate FRIDAY = LocalDate.of(2026, 1, 9);
        private static final LocalDate SATURDAY = LocalDate.of(2026, 1, 10);
        private static final LocalDate SUNDAY = LocalDate.of(2026, 1, 11);

        @Test
        @DisplayName("isMonday returns true for Monday")
        void isMonday_monday_returnsTrue() {
            assertThat(DatePredicates.isMonday(MONDAY)).isTrue();
        }

        @Test
        @DisplayName("isMonday returns false for non-Monday")
        void isMonday_notMonday_returnsFalse() {
            assertThat(DatePredicates.isMonday(TUESDAY)).isFalse();
        }

        @Test
        @DisplayName("isMonday returns false for null")
        void isMonday_null_returnsFalse() {
            assertThat(DatePredicates.isMonday(null)).isFalse();
        }

        @Test
        @DisplayName("isTuesday returns true for Tuesday")
        void isTuesday_tuesday_returnsTrue() {
            assertThat(DatePredicates.isTuesday(TUESDAY)).isTrue();
        }

        @Test
        @DisplayName("isTuesday returns false for non-Tuesday")
        void isTuesday_notTuesday_returnsFalse() {
            assertThat(DatePredicates.isTuesday(MONDAY)).isFalse();
        }

        @Test
        @DisplayName("isWednesday returns true for Wednesday")
        void isWednesday_wednesday_returnsTrue() {
            assertThat(DatePredicates.isWednesday(WEDNESDAY)).isTrue();
        }

        @Test
        @DisplayName("isThursday returns true for Thursday")
        void isThursday_thursday_returnsTrue() {
            assertThat(DatePredicates.isThursday(THURSDAY)).isTrue();
        }

        @Test
        @DisplayName("isFriday returns true for Friday")
        void isFriday_friday_returnsTrue() {
            assertThat(DatePredicates.isFriday(FRIDAY)).isTrue();
        }

        @Test
        @DisplayName("isSaturday returns true for Saturday")
        void isSaturday_saturday_returnsTrue() {
            assertThat(DatePredicates.isSaturday(SATURDAY)).isTrue();
        }

        @Test
        @DisplayName("isSunday returns true for Sunday")
        void isSunday_sunday_returnsTrue() {
            assertThat(DatePredicates.isSunday(SUNDAY)).isTrue();
        }

        @Test
        @DisplayName("isWeekend returns true for Saturday and Sunday")
        void isWeekend_weekendDays_returnsTrue() {
            assertThat(DatePredicates.isWeekend(SATURDAY)).isTrue();
            assertThat(DatePredicates.isWeekend(SUNDAY)).isTrue();
        }

        @Test
        @DisplayName("isWeekend returns false for weekdays")
        void isWeekend_weekday_returnsFalse() {
            assertThat(DatePredicates.isWeekend(MONDAY)).isFalse();
            assertThat(DatePredicates.isWeekend(FRIDAY)).isFalse();
        }

        @Test
        @DisplayName("isWeekend returns false for null")
        void isWeekend_null_returnsFalse() {
            assertThat(DatePredicates.isWeekend(null)).isFalse();
        }

        @Test
        @DisplayName("isWeekday returns true for weekdays")
        void isWeekday_weekdays_returnsTrue() {
            assertThat(DatePredicates.isWeekday(MONDAY)).isTrue();
            assertThat(DatePredicates.isWeekday(TUESDAY)).isTrue();
            assertThat(DatePredicates.isWeekday(WEDNESDAY)).isTrue();
            assertThat(DatePredicates.isWeekday(THURSDAY)).isTrue();
            assertThat(DatePredicates.isWeekday(FRIDAY)).isTrue();
        }

        @Test
        @DisplayName("isWeekday returns false for weekends")
        void isWeekday_weekend_returnsFalse() {
            assertThat(DatePredicates.isWeekday(SATURDAY)).isFalse();
            assertThat(DatePredicates.isWeekday(SUNDAY)).isFalse();
        }

        @Test
        @DisplayName("isWeekday returns false for null")
        void isWeekday_null_returnsFalse() {
            assertThat(DatePredicates.isWeekday(null)).isFalse();
        }

        @Test
        @DisplayName("isLeapYear returns true for leap year")
        void isLeapYear_leapYear_returnsTrue() {
            assertThat(DatePredicates.isLeapYear(LocalDate.of(2024, 1, 1))).isTrue();
            assertThat(DatePredicates.isLeapYear(LocalDate.of(2000, 6, 15))).isTrue();
        }

        @Test
        @DisplayName("isLeapYear returns false for non-leap year")
        void isLeapYear_nonLeapYear_returnsFalse() {
            assertThat(DatePredicates.isLeapYear(LocalDate.of(2026, 1, 1))).isFalse();
            assertThat(DatePredicates.isLeapYear(LocalDate.of(1900, 1, 1))).isFalse();
        }

        @Test
        @DisplayName("isLeapYear returns false for null")
        void isLeapYear_null_returnsFalse() {
            assertThat(DatePredicates.isLeapYear(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Between")
    class Between {

        @Test
        @DisplayName("isBetween(LocalDate) returns true for date within range")
        void isBetweenLocalDate_withinRange_returnsTrue() {
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);
            LocalDate date = LocalDate.of(2026, 6, 15);
            assertThat(DatePredicates.isBetween(date, start, end)).isTrue();
        }

        @Test
        @DisplayName("isBetween(LocalDate) returns true for date at start boundary")
        void isBetweenLocalDate_atStart_returnsTrue() {
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);
            assertThat(DatePredicates.isBetween(start, start, end)).isTrue();
        }

        @Test
        @DisplayName("isBetween(LocalDate) returns true for date at end boundary")
        void isBetweenLocalDate_atEnd_returnsTrue() {
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);
            assertThat(DatePredicates.isBetween(end, start, end)).isTrue();
        }

        @Test
        @DisplayName("isBetween(LocalDate) returns false for date outside range")
        void isBetweenLocalDate_outsideRange_returnsFalse() {
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);
            assertThat(DatePredicates.isBetween(LocalDate.of(2025, 12, 31), start, end)).isFalse();
            assertThat(DatePredicates.isBetween(LocalDate.of(2027, 1, 1), start, end)).isFalse();
        }

        @Test
        @DisplayName("isBetween(LocalDate) returns false when any argument is null")
        void isBetweenLocalDate_null_returnsFalse() {
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);
            assertThat(DatePredicates.isBetween(null, start, end)).isFalse();
            assertThat(DatePredicates.isBetween(date, null, end)).isFalse();
            assertThat(DatePredicates.isBetween(date, start, null)).isFalse();
        }

        @Test
        @DisplayName("isBetween(LocalDateTime) returns true for date-time within range")
        void isBetweenLocalDateTime_withinRange_returnsTrue() {
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59, 59);
            LocalDateTime dateTime = LocalDateTime.of(2026, 6, 15, 12, 0);
            assertThat(DatePredicates.isBetween(dateTime, start, end)).isTrue();
        }

        @Test
        @DisplayName("isBetween(LocalDateTime) returns true at boundaries")
        void isBetweenLocalDateTime_atBoundaries_returnsTrue() {
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59, 59);
            assertThat(DatePredicates.isBetween(start, start, end)).isTrue();
            assertThat(DatePredicates.isBetween(end, start, end)).isTrue();
        }

        @Test
        @DisplayName("isBetween(LocalDateTime) returns false for date-time outside range")
        void isBetweenLocalDateTime_outsideRange_returnsFalse() {
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59, 59);
            assertThat(DatePredicates.isBetween(
                    LocalDateTime.of(2025, 12, 31, 23, 59, 59), start, end)).isFalse();
        }

        @Test
        @DisplayName("isBetween(LocalDateTime) returns false when any argument is null")
        void isBetweenLocalDateTime_null_returnsFalse() {
            LocalDateTime dt = LocalDateTime.of(2026, 6, 15, 12, 0);
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59, 59);
            assertThat(DatePredicates.isBetween((LocalDateTime) null, start, end)).isFalse();
            assertThat(DatePredicates.isBetween(dt, null, end)).isFalse();
            assertThat(DatePredicates.isBetween(dt, start, null)).isFalse();
        }
    }
}
