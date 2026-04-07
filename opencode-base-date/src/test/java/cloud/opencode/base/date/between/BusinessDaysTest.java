package cloud.opencode.base.date.between;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BusinessDays}.
 */
@DisplayName("BusinessDays")
class BusinessDaysTest {

    @Nested
    @DisplayName("between")
    class Between {

        @Test
        @DisplayName("full week Mon-Fri has 5 business days")
        void fullWeek() {
            // Monday to next Monday
            LocalDate mon = LocalDate.of(2026, 4, 6);   // Monday
            LocalDate nextMon = LocalDate.of(2026, 4, 13); // next Monday
            assertThat(BusinessDays.between(mon, nextMon)).isEqualTo(5);
        }

        @Test
        @DisplayName("two weeks has 10 business days")
        void twoWeeks() {
            LocalDate mon = LocalDate.of(2026, 4, 6);
            LocalDate twoWeeksLater = LocalDate.of(2026, 4, 20);
            assertThat(BusinessDays.between(mon, twoWeeksLater)).isEqualTo(10);
        }

        @Test
        @DisplayName("start equals end returns 0")
        void sameDay() {
            LocalDate date = LocalDate.of(2026, 4, 6);
            assertThat(BusinessDays.between(date, date)).isEqualTo(0);
        }

        @Test
        @DisplayName("start after end returns 0")
        void startAfterEnd() {
            LocalDate start = LocalDate.of(2026, 4, 13);
            LocalDate end = LocalDate.of(2026, 4, 6);
            assertThat(BusinessDays.between(start, end)).isEqualTo(0);
        }

        @Test
        @DisplayName("weekend days are not counted")
        void weekendSkipped() {
            // Saturday to Monday: only 0 business days (Sat and Sun excluded)
            LocalDate sat = LocalDate.of(2026, 4, 11); // Saturday
            LocalDate mon = LocalDate.of(2026, 4, 13); // Monday
            assertThat(BusinessDays.between(sat, mon)).isEqualTo(0);
        }

        @Test
        @DisplayName("with holidays subtracts holiday days")
        void withHolidays() {
            LocalDate mon = LocalDate.of(2026, 4, 6);
            LocalDate nextMon = LocalDate.of(2026, 4, 13);
            // Tuesday and Wednesday are holidays
            Set<LocalDate> holidays = Set.of(
                    LocalDate.of(2026, 4, 7),
                    LocalDate.of(2026, 4, 8));
            assertThat(BusinessDays.between(mon, nextMon, holidays)).isEqualTo(3);
        }

        @Test
        @DisplayName("holiday on weekend does not double-count")
        void holidayOnWeekend() {
            LocalDate mon = LocalDate.of(2026, 4, 6);
            LocalDate nextMon = LocalDate.of(2026, 4, 13);
            // Saturday is a holiday, but it's already excluded
            Set<LocalDate> holidays = Set.of(LocalDate.of(2026, 4, 11));
            assertThat(BusinessDays.between(mon, nextMon, holidays)).isEqualTo(5);
        }

        @Test
        @DisplayName("custom weekend (Friday-Saturday) counts Sunday as workday")
        void customWeekend() {
            LocalDate mon = LocalDate.of(2026, 4, 6);
            LocalDate nextMon = LocalDate.of(2026, 4, 13);
            Set<DayOfWeek> weekend = EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
            assertThat(BusinessDays.between(mon, nextMon, Set.of(), weekend)).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("addBusinessDays")
    class AddBusinessDays {

        @Test
        @DisplayName("add 5 business days from Monday = next Monday")
        void addFiveFromMonday() {
            LocalDate mon = LocalDate.of(2026, 4, 6); // Monday
            assertThat(BusinessDays.addBusinessDays(mon, 5))
                    .isEqualTo(LocalDate.of(2026, 4, 13)); // next Monday
        }

        @Test
        @DisplayName("add 1 business day from Friday = Monday")
        void addOneFromFriday() {
            LocalDate fri = LocalDate.of(2026, 4, 10); // Friday
            assertThat(BusinessDays.addBusinessDays(fri, 1))
                    .isEqualTo(LocalDate.of(2026, 4, 13)); // Monday
        }

        @Test
        @DisplayName("add 0 business days returns same date")
        void addZero() {
            LocalDate date = LocalDate.of(2026, 4, 6);
            assertThat(BusinessDays.addBusinessDays(date, 0)).isEqualTo(date);
        }

        @Test
        @DisplayName("subtract business days (negative)")
        void subtractDays() {
            LocalDate mon = LocalDate.of(2026, 4, 13); // Monday
            assertThat(BusinessDays.addBusinessDays(mon, -5))
                    .isEqualTo(LocalDate.of(2026, 4, 6)); // previous Monday
        }

        @Test
        @DisplayName("subtract 1 from Monday = Friday")
        void subtractOneFromMonday() {
            LocalDate mon = LocalDate.of(2026, 4, 13); // Monday
            assertThat(BusinessDays.addBusinessDays(mon, -1))
                    .isEqualTo(LocalDate.of(2026, 4, 10)); // Friday
        }

        @Test
        @DisplayName("add with holidays skips holiday")
        void addWithHolidays() {
            LocalDate mon = LocalDate.of(2026, 4, 6); // Monday
            // Tuesday is a holiday
            Set<LocalDate> holidays = Set.of(LocalDate.of(2026, 4, 7));
            assertThat(BusinessDays.addBusinessDays(mon, 1, holidays))
                    .isEqualTo(LocalDate.of(2026, 4, 8)); // Wednesday
        }

        @Test
        @DisplayName("add from weekend starts counting from next weekday")
        void addFromWeekend() {
            LocalDate sat = LocalDate.of(2026, 4, 11); // Saturday
            assertThat(BusinessDays.addBusinessDays(sat, 1))
                    .isEqualTo(LocalDate.of(2026, 4, 13)); // Monday
        }
    }
}
