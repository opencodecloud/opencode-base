package cloud.opencode.base.date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ClosestDate}.
 */
@DisplayName("ClosestDate")
class ClosestDateTest {

    @Nested
    @DisplayName("closestTo(LocalDate)")
    class ClosestToDate {

        @Test
        @DisplayName("finds closest date from collection")
        void findsClosest() {
            LocalDate target = LocalDate.of(2026, 6, 10);
            List<LocalDate> dates = List.of(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 6, 15),
                    LocalDate.of(2026, 12, 31));
            Optional<LocalDate> result = ClosestDate.closestTo(target, dates);
            assertThat(result).contains(LocalDate.of(2026, 6, 15));
        }

        @Test
        @DisplayName("returns empty for null collection")
        void nullCollection() {
            assertThat(ClosestDate.closestTo(LocalDate.now(), null)).isEmpty();
        }

        @Test
        @DisplayName("returns empty for empty collection")
        void emptyCollection() {
            assertThat(ClosestDate.closestTo(LocalDate.now(), Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("returns the single element")
        void singleElement() {
            LocalDate date = LocalDate.of(2026, 3, 15);
            assertThat(ClosestDate.closestTo(LocalDate.now(), List.of(date))).contains(date);
        }

        @Test
        @DisplayName("throws for null target")
        void nullTarget() {
            assertThatThrownBy(() -> ClosestDate.closestTo((LocalDate) null, List.of()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("skips null elements in collection")
        void skipsNullElements() {
            LocalDate target = LocalDate.of(2026, 6, 10);
            List<LocalDate> dates = new java.util.ArrayList<>();
            dates.add(null);
            dates.add(LocalDate.of(2026, 6, 12));
            assertThat(ClosestDate.closestTo(target, dates)).contains(LocalDate.of(2026, 6, 12));
        }

        @Test
        @DisplayName("exact match returns the date itself")
        void exactMatch() {
            LocalDate target = LocalDate.of(2026, 6, 15);
            List<LocalDate> dates = List.of(
                    LocalDate.of(2026, 6, 10),
                    LocalDate.of(2026, 6, 15),
                    LocalDate.of(2026, 6, 20));
            assertThat(ClosestDate.closestTo(target, dates)).contains(target);
        }
    }

    @Nested
    @DisplayName("closestBefore(LocalDate)")
    class ClosestBeforeDate {

        @Test
        @DisplayName("finds closest date before target")
        void findsClosestBefore() {
            LocalDate target = LocalDate.of(2026, 6, 10);
            List<LocalDate> dates = List.of(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 6, 8),
                    LocalDate.of(2026, 6, 15));
            assertThat(ClosestDate.closestBefore(target, dates)).contains(LocalDate.of(2026, 6, 8));
        }

        @Test
        @DisplayName("returns empty when no dates are before target")
        void noDatesBefore() {
            LocalDate target = LocalDate.of(2026, 1, 1);
            List<LocalDate> dates = List.of(LocalDate.of(2026, 6, 15));
            assertThat(ClosestDate.closestBefore(target, dates)).isEmpty();
        }

        @Test
        @DisplayName("excludes exact match")
        void excludesExactMatch() {
            LocalDate target = LocalDate.of(2026, 6, 15);
            List<LocalDate> dates = List.of(LocalDate.of(2026, 6, 15));
            assertThat(ClosestDate.closestBefore(target, dates)).isEmpty();
        }
    }

    @Nested
    @DisplayName("closestAfter(LocalDate)")
    class ClosestAfterDate {

        @Test
        @DisplayName("finds closest date after target")
        void findsClosestAfter() {
            LocalDate target = LocalDate.of(2026, 6, 10);
            List<LocalDate> dates = List.of(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 6, 12),
                    LocalDate.of(2026, 12, 31));
            assertThat(ClosestDate.closestAfter(target, dates)).contains(LocalDate.of(2026, 6, 12));
        }

        @Test
        @DisplayName("returns empty when no dates are after target")
        void noDatesAfter() {
            LocalDate target = LocalDate.of(2026, 12, 31);
            List<LocalDate> dates = List.of(LocalDate.of(2026, 1, 1));
            assertThat(ClosestDate.closestAfter(target, dates)).isEmpty();
        }

        @Test
        @DisplayName("excludes exact match")
        void excludesExactMatch() {
            LocalDate target = LocalDate.of(2026, 6, 15);
            List<LocalDate> dates = List.of(LocalDate.of(2026, 6, 15));
            assertThat(ClosestDate.closestAfter(target, dates)).isEmpty();
        }
    }

    @Nested
    @DisplayName("DateTime variants")
    class DateTimeVariants {

        @Test
        @DisplayName("closestTo finds closest datetime")
        void closestToDateTime() {
            LocalDateTime target = LocalDateTime.of(2026, 6, 10, 12, 0);
            List<LocalDateTime> dateTimes = List.of(
                    LocalDateTime.of(2026, 6, 10, 10, 0),
                    LocalDateTime.of(2026, 6, 10, 13, 0),
                    LocalDateTime.of(2026, 6, 11, 12, 0));
            assertThat(ClosestDate.closestTo(target, dateTimes))
                    .contains(LocalDateTime.of(2026, 6, 10, 13, 0));
        }

        @Test
        @DisplayName("closestBefore finds closest datetime before target")
        void closestBeforeDateTime() {
            LocalDateTime target = LocalDateTime.of(2026, 6, 10, 12, 0);
            List<LocalDateTime> dateTimes = List.of(
                    LocalDateTime.of(2026, 6, 10, 10, 0),
                    LocalDateTime.of(2026, 6, 10, 11, 30));
            assertThat(ClosestDate.closestBefore(target, dateTimes))
                    .contains(LocalDateTime.of(2026, 6, 10, 11, 30));
        }

        @Test
        @DisplayName("closestAfter finds closest datetime after target")
        void closestAfterDateTime() {
            LocalDateTime target = LocalDateTime.of(2026, 6, 10, 12, 0);
            List<LocalDateTime> dateTimes = List.of(
                    LocalDateTime.of(2026, 6, 10, 12, 30),
                    LocalDateTime.of(2026, 6, 11, 0, 0));
            assertThat(ClosestDate.closestAfter(target, dateTimes))
                    .contains(LocalDateTime.of(2026, 6, 10, 12, 30));
        }

        @Test
        @DisplayName("empty collection returns empty")
        void emptyCollectionDateTime() {
            assertThat(ClosestDate.closestTo(LocalDateTime.now(), Collections.emptyList())).isEmpty();
        }
    }
}
