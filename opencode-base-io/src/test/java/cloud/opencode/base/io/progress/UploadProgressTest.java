package cloud.opencode.base.io.progress;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UploadProgress")
class UploadProgressTest {

    @Nested
    @DisplayName("formattedPercentage")
    class FormattedPercentage {

        @Test
        @DisplayName("should return formatted percentage for valid upload")
        void shouldReturnFormattedPercentage() {
            var progress = new UploadProgress("file.zip", 500000, 1000000, 5000);
            assertThat(progress.formattedPercentage()).isEqualTo("50.0%");
        }

        @Test
        @DisplayName("should return 100% when complete")
        void shouldReturn100Percent() {
            var progress = new UploadProgress("file.zip", 1000000, 1000000, 5000);
            assertThat(progress.formattedPercentage()).isEqualTo("100.0%");
        }

        @Test
        @DisplayName("should return ?% when total bytes is zero")
        void shouldReturnQuestionMarkWhenTotalIsZero() {
            var progress = new UploadProgress("file.zip", 500, 0, 1000);
            assertThat(progress.formattedPercentage()).isEqualTo("?%");
        }

        @Test
        @DisplayName("should return ?% when total bytes is negative")
        void shouldReturnQuestionMarkWhenTotalIsNegative() {
            var progress = new UploadProgress("file.zip", 500, -1, 1000);
            assertThat(progress.formattedPercentage()).isEqualTo("?%");
        }
    }

    @Nested
    @DisplayName("throughputBytesPerSec")
    class ThroughputBytesPerSec {

        @Test
        @DisplayName("should calculate throughput correctly")
        void shouldCalculateThroughput() {
            var progress = new UploadProgress("file.zip", 500000, 1000000, 5000);
            assertThat(progress.throughputBytesPerSec()).isEqualTo(100000.0);
        }

        @Test
        @DisplayName("should return 0 when elapsed time is zero")
        void shouldReturnZeroWhenNoTimeElapsed() {
            var progress = new UploadProgress("file.zip", 500000, 1000000, 0);
            assertThat(progress.throughputBytesPerSec()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should return 0 when elapsed time is negative")
        void shouldReturnZeroWhenNegativeTime() {
            var progress = new UploadProgress("file.zip", 500000, 1000000, -1);
            assertThat(progress.throughputBytesPerSec()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("percentage")
    class Percentage {

        @Test
        @DisplayName("should return correct percentage")
        void shouldReturnCorrectPercentage() {
            var progress = new UploadProgress("file.zip", 250000, 1000000, 5000);
            assertThat(progress.percentage()).isEqualTo(25.0);
        }

        @Test
        @DisplayName("should return 0 when total bytes is zero")
        void shouldReturnZeroWhenTotalIsZero() {
            var progress = new UploadProgress("file.zip", 500, 0, 1000);
            assertThat(progress.percentage()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("record accessors")
    class RecordAccessors {

        @Test
        @DisplayName("should return all record components")
        void shouldReturnRecordComponents() {
            var progress = new UploadProgress("data.csv", 100, 200, 300);
            assertThat(progress.fileName()).isEqualTo("data.csv");
            assertThat(progress.bytesUploaded()).isEqualTo(100);
            assertThat(progress.totalBytes()).isEqualTo(200);
            assertThat(progress.elapsedMs()).isEqualTo(300);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("should contain file name and progress info")
        void shouldContainFileInfo() {
            var progress = new UploadProgress("file.zip", 500000, 1000000, 5000);
            String str = progress.toString();
            assertThat(str).contains("file.zip")
                    .contains("500000")
                    .contains("1000000")
                    .contains("50.0%");
        }
    }
}
