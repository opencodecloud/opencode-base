package cloud.opencode.base.io.progress;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DownloadProgress")
class DownloadProgressTest {

    @Nested
    @DisplayName("of factory method")
    class OfFactory {

        @Test
        @DisplayName("should create progress with calculated percentage and remaining time")
        void shouldCreateProgress() {
            DownloadProgress p = DownloadProgress.of(1000000, 500000, 100000);
            assertThat(p.getTotalBytes()).isEqualTo(1000000);
            assertThat(p.getDownloadedBytes()).isEqualTo(500000);
            assertThat(p.getPercentage()).isEqualTo(50.0);
            assertThat(p.getSpeed()).isEqualTo(100000);
            assertThat(p.getRemainingTime()).isEqualTo(5); // (1000000-500000)/100000
        }

        @Test
        @DisplayName("should handle zero speed with zero remaining time")
        void shouldHandleZeroSpeed() {
            DownloadProgress p = DownloadProgress.of(1000, 500, 0);
            assertThat(p.getRemainingTime()).isEqualTo(0);
        }

        @Test
        @DisplayName("should handle zero total bytes")
        void shouldHandleZeroTotal() {
            DownloadProgress p = DownloadProgress.of(0, 500, 100);
            assertThat(p.getPercentage()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("completed factory method")
    class CompletedFactory {

        @Test
        @DisplayName("should create completed progress")
        void shouldCreateCompleted() {
            DownloadProgress p = DownloadProgress.completed(2000);
            assertThat(p.getTotalBytes()).isEqualTo(2000);
            assertThat(p.getDownloadedBytes()).isEqualTo(2000);
            assertThat(p.getPercentage()).isEqualTo(100.0);
            assertThat(p.isComplete()).isTrue();
        }
    }

    @Nested
    @DisplayName("unknown factory method")
    class UnknownFactory {

        @Test
        @DisplayName("should create unknown progress")
        void shouldCreateUnknown() {
            DownloadProgress p = DownloadProgress.unknown(5000, 100);
            assertThat(p.getTotalBytes()).isEqualTo(-1);
            assertThat(p.getPercentage()).isEqualTo(-1.0);
            assertThat(p.getRemainingTime()).isEqualTo(-1);
            assertThat(p.isTotalKnown()).isFalse();
        }
    }

    @Nested
    @DisplayName("isComplete")
    class IsComplete {

        @Test
        @DisplayName("should return true when downloaded equals total")
        void shouldBeCompleteWhenEqual() {
            DownloadProgress p = DownloadProgress.of(100, 100, 10);
            assertThat(p.isComplete()).isTrue();
        }

        @Test
        @DisplayName("should return false when not complete")
        void shouldNotBeCompleteWhenPartial() {
            DownloadProgress p = DownloadProgress.of(100, 50, 10);
            assertThat(p.isComplete()).isFalse();
        }

        @Test
        @DisplayName("should return false when total is zero")
        void shouldNotBeCompleteWhenTotalZero() {
            DownloadProgress p = DownloadProgress.of(0, 0, 0);
            assertThat(p.isComplete()).isFalse();
        }
    }

    @Nested
    @DisplayName("isTotalKnown")
    class IsTotalKnown {

        @Test
        @DisplayName("should return true for known total")
        void shouldReturnTrueForKnown() {
            DownloadProgress p = DownloadProgress.of(100, 50, 10);
            assertThat(p.isTotalKnown()).isTrue();
        }

        @Test
        @DisplayName("should return false for unknown total")
        void shouldReturnFalseForUnknown() {
            DownloadProgress p = DownloadProgress.unknown(50, 10);
            assertThat(p.isTotalKnown()).isFalse();
        }
    }

    @Nested
    @DisplayName("formattedPercentage")
    class FormattedPercentage {

        @Test
        @DisplayName("should return formatted percentage")
        void shouldFormat() {
            DownloadProgress p = DownloadProgress.of(1000000, 500000, 100000);
            assertThat(p.formattedPercentage()).isEqualTo("50.00%");
        }

        @Test
        @DisplayName("should return Unknown for negative percentage")
        void shouldReturnUnknown() {
            DownloadProgress p = DownloadProgress.unknown(500, 100);
            assertThat(p.formattedPercentage()).isEqualTo("Unknown");
        }
    }

    @Nested
    @DisplayName("formattedSpeed")
    class FormattedSpeed {

        @Test
        @DisplayName("should format speed in KB/s")
        void shouldFormatKB() {
            DownloadProgress p = DownloadProgress.of(1000000, 500000, 100000);
            assertThat(p.formattedSpeed()).isEqualTo("97.66 KB/s");
        }

        @Test
        @DisplayName("should format speed in MB/s")
        void shouldFormatMB() {
            DownloadProgress p = DownloadProgress.of(1000000, 500000, 2 * 1024 * 1024);
            assertThat(p.formattedSpeed()).contains("MB/s");
        }

        @Test
        @DisplayName("should format zero speed")
        void shouldFormatZeroSpeed() {
            DownloadProgress p = DownloadProgress.of(1000, 500, 0);
            assertThat(p.formattedSpeed()).isEqualTo("0 B/s");
        }
    }

    @Nested
    @DisplayName("formattedTotalBytes")
    class FormattedTotalBytes {

        @Test
        @DisplayName("should return Unknown for negative total")
        void shouldReturnUnknown() {
            DownloadProgress p = DownloadProgress.unknown(500, 100);
            assertThat(p.formattedTotalBytes()).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("should format bytes")
        void shouldFormatBytes() {
            DownloadProgress p = DownloadProgress.of(500, 100, 10);
            assertThat(p.formattedTotalBytes()).isEqualTo("500 B");
        }

        @Test
        @DisplayName("should format GB")
        void shouldFormatGB() {
            long gb = 2L * 1024 * 1024 * 1024;
            DownloadProgress p = DownloadProgress.of(gb, 100, 10);
            assertThat(p.formattedTotalBytes()).contains("GB");
        }
    }

    @Nested
    @DisplayName("formattedDownloadedBytes")
    class FormattedDownloadedBytes {

        @Test
        @DisplayName("should format downloaded bytes")
        void shouldFormat() {
            DownloadProgress p = DownloadProgress.of(1000, 500, 10);
            assertThat(p.formattedDownloadedBytes()).isEqualTo("500 B");
        }
    }

    @Nested
    @DisplayName("formattedRemainingTime")
    class FormattedRemainingTime {

        @Test
        @DisplayName("should return Unknown for negative remaining time")
        void shouldReturnUnknown() {
            DownloadProgress p = DownloadProgress.unknown(500, 100);
            assertThat(p.formattedRemainingTime()).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("should return 0s for zero remaining")
        void shouldReturnZero() {
            DownloadProgress p = DownloadProgress.completed(1000);
            assertThat(p.formattedRemainingTime()).isEqualTo("0s");
        }

        @Test
        @DisplayName("should format hours minutes seconds")
        void shouldFormatHMS() {
            DownloadProgress p = new DownloadProgress(100000, 0, 0, 1, 3661);
            assertThat(p.formattedRemainingTime()).isEqualTo("1h 1m 1s");
        }

        @Test
        @DisplayName("should format minutes and seconds without hours")
        void shouldFormatMS() {
            DownloadProgress p = new DownloadProgress(100000, 0, 0, 1, 125);
            assertThat(p.formattedRemainingTime()).isEqualTo("2m 5s");
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("should be equal for same values")
        void shouldBeEqual() {
            DownloadProgress p1 = DownloadProgress.of(1000, 500, 100);
            DownloadProgress p2 = DownloadProgress.of(1000, 500, 100);
            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different values")
        void shouldNotBeEqual() {
            DownloadProgress p1 = DownloadProgress.of(1000, 500, 100);
            DownloadProgress p2 = DownloadProgress.of(1000, 600, 100);
            assertThat(p1).isNotEqualTo(p2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("should contain progress info")
        void shouldContainInfo() {
            DownloadProgress p = DownloadProgress.of(1000, 500, 100);
            assertThat(p.toString()).contains("DownloadProgress");
        }
    }
}
