package cloud.opencode.base.date;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DateRounding}.
 */
@DisplayName("DateRounding")
class DateRoundingTest {

    @Nested
    @DisplayName("Hour rounding")
    class HourRounding {

        @Test
        @DisplayName("roundToNearestHour rounds down when minutes < 30")
        void roundsDown() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 29, 59);
            assertThat(DateRounding.roundToNearestHour(dt))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 0));
        }

        @Test
        @DisplayName("roundToNearestHour rounds up when minutes >= 30")
        void roundsUp() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 30, 0);
            assertThat(DateRounding.roundToNearestHour(dt))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 11, 0));
        }

        @Test
        @DisplayName("floorToHour truncates to hour")
        void floor() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 45, 30);
            assertThat(DateRounding.floorToHour(dt))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 0));
        }

        @Test
        @DisplayName("ceilToHour returns next hour when not exact")
        void ceil() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 0, 1);
            assertThat(DateRounding.ceilToHour(dt))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 11, 0));
        }

        @Test
        @DisplayName("ceilToHour returns same when already exact hour")
        void ceilExact() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 0, 0);
            assertThat(DateRounding.ceilToHour(dt)).isEqualTo(dt);
        }

        @Test
        @DisplayName("midnight edge case")
        void midnight() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 23, 45);
            assertThat(DateRounding.roundToNearestHour(dt))
                    .isEqualTo(LocalDateTime.of(2026, 4, 4, 0, 0));
        }
    }

    @Nested
    @DisplayName("Minute rounding")
    class MinuteRounding {

        @Test
        @DisplayName("roundToNearestMinute rounds down when seconds < 30")
        void roundsDown() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 15, 29);
            assertThat(DateRounding.roundToNearestMinute(dt))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 15));
        }

        @Test
        @DisplayName("roundToNearestMinute rounds up when seconds >= 30")
        void roundsUp() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 15, 30);
            assertThat(DateRounding.roundToNearestMinute(dt))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 16));
        }

        @Test
        @DisplayName("floorToMinute truncates to minute")
        void floor() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 15, 45);
            assertThat(DateRounding.floorToMinute(dt))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 15));
        }

        @Test
        @DisplayName("ceilToMinute returns same when already exact")
        void ceilExact() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 15, 0);
            assertThat(DateRounding.ceilToMinute(dt)).isEqualTo(dt);
        }
    }

    @Nested
    @DisplayName("N-minute rounding")
    class NMinuteRounding {

        @Test
        @DisplayName("roundToNearest 15 minutes")
        void round15() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 7);
            assertThat(DateRounding.roundToNearest(dt, 15))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 0));

            LocalDateTime dt2 = LocalDateTime.of(2026, 4, 3, 10, 8);
            assertThat(DateRounding.roundToNearest(dt2, 15))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 15));
        }

        @Test
        @DisplayName("roundToNearestHalfHour")
        void halfHour() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 14);
            assertThat(DateRounding.roundToNearestHalfHour(dt))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 0));

            LocalDateTime dt2 = LocalDateTime.of(2026, 4, 3, 10, 16);
            assertThat(DateRounding.roundToNearestHalfHour(dt2))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 30));
        }

        @Test
        @DisplayName("roundToNearestQuarterHour")
        void quarterHour() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 37, 45);
            assertThat(DateRounding.roundToNearestQuarterHour(dt))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 45));
        }

        @Test
        @DisplayName("throws for non-positive minutes")
        void nonPositive() {
            assertThatThrownBy(() -> DateRounding.roundToNearest(LocalDateTime.now(), 0))
                    .isInstanceOf(OpenDateException.class);
            assertThatThrownBy(() -> DateRounding.roundToNearest(LocalDateTime.now(), -5))
                    .isInstanceOf(OpenDateException.class);
        }
    }

    @Nested
    @DisplayName("Generic Duration rounding")
    class GenericDuration {

        @Test
        @DisplayName("roundToNearest with 5-minute duration")
        void fiveMinutes() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 37, 45);
            assertThat(DateRounding.roundToNearest(dt, Duration.ofMinutes(5)))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 40));
        }

        @Test
        @DisplayName("ceilTo with 15-minute duration")
        void ceilTo15() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 31);
            assertThat(DateRounding.ceilTo(dt, Duration.ofMinutes(15)))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 45));
        }

        @Test
        @DisplayName("floorTo with 15-minute duration")
        void floorTo15() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 37);
            assertThat(DateRounding.floorTo(dt, Duration.ofMinutes(15)))
                    .isEqualTo(LocalDateTime.of(2026, 4, 3, 10, 30));
        }

        @Test
        @DisplayName("exact boundary returns unchanged for ceilTo")
        void exactBoundary() {
            LocalDateTime dt = LocalDateTime.of(2026, 4, 3, 10, 30);
            assertThat(DateRounding.ceilTo(dt, Duration.ofMinutes(15))).isEqualTo(dt);
        }

        @Test
        @DisplayName("throws for zero duration")
        void zeroDuration() {
            assertThatThrownBy(() -> DateRounding.roundToNearest(LocalDateTime.now(), Duration.ZERO))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("throws for negative duration")
        void negativeDuration() {
            assertThatThrownBy(() -> DateRounding.roundToNearest(LocalDateTime.now(), Duration.ofMinutes(-1)))
                    .isInstanceOf(OpenDateException.class);
        }
    }
}
