package cloud.opencode.base.string.format;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenDurationTest Tests
 * OpenDurationTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenDuration Tests")
class OpenDurationTest {

    @Nested
    @DisplayName("format Tests")
    class FormatTests {

        @Test
        @DisplayName("Should format seconds")
        void shouldFormatSeconds() {
            assertThat(OpenDuration.format(1000)).isEqualTo("1s");
            assertThat(OpenDuration.format(30000)).isEqualTo("30s");
        }

        @Test
        @DisplayName("Should format minutes and seconds")
        void shouldFormatMinutesAndSeconds() {
            assertThat(OpenDuration.format(60000)).isEqualTo("1m 0s");
            assertThat(OpenDuration.format(90000)).isEqualTo("1m 30s");
        }

        @Test
        @DisplayName("Should format hours, minutes and seconds")
        void shouldFormatHoursMinutesAndSeconds() {
            assertThat(OpenDuration.format(3600000)).isEqualTo("1h 0m 0s");
            assertThat(OpenDuration.format(3661000)).isEqualTo("1h 1m 1s");
        }

        @Test
        @DisplayName("Should format days, hours, minutes and seconds")
        void shouldFormatDaysHoursMinutesAndSeconds() {
            assertThat(OpenDuration.format(86400000L)).isEqualTo("1d 0h 0m 0s");
            assertThat(OpenDuration.format(90061000L)).isEqualTo("1d 1h 1m 1s");
        }

        @Test
        @DisplayName("Should format zero")
        void shouldFormatZero() {
            assertThat(OpenDuration.format(0)).isEqualTo("0s");
        }
    }

    @Nested
    @DisplayName("formatTime Tests")
    class FormatTimeTests {

        @Test
        @DisplayName("Should format as HH:MM:SS")
        void shouldFormatAsHhMmSs() {
            assertThat(OpenDuration.formatTime(0)).isEqualTo("00:00:00");
            assertThat(OpenDuration.formatTime(61)).isEqualTo("00:01:01");
            assertThat(OpenDuration.formatTime(3661)).isEqualTo("01:01:01");
        }

        @Test
        @DisplayName("Should handle large values")
        void shouldHandleLargeValues() {
            assertThat(OpenDuration.formatTime(86400)).isEqualTo("24:00:00");
        }
    }

    @Nested
    @DisplayName("formatRelativeTime Tests")
    class FormatRelativeTimeTests {

        @Test
        @DisplayName("Should format future time")
        void shouldFormatFutureTime() {
            long future = System.currentTimeMillis() + 10000;
            assertThat(OpenDuration.formatRelativeTime(future)).isEqualTo("future");
        }

        @Test
        @DisplayName("Should format just now")
        void shouldFormatJustNow() {
            long now = System.currentTimeMillis();
            assertThat(OpenDuration.formatRelativeTime(now)).isEqualTo("刚刚");
        }

        @Test
        @DisplayName("Should format minutes ago")
        void shouldFormatMinutesAgo() {
            long fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000;
            assertThat(OpenDuration.formatRelativeTime(fiveMinutesAgo)).isEqualTo("5分钟前");
        }

        @Test
        @DisplayName("Should format hours ago")
        void shouldFormatHoursAgo() {
            long twoHoursAgo = System.currentTimeMillis() - 2 * 60 * 60 * 1000;
            assertThat(OpenDuration.formatRelativeTime(twoHoursAgo)).isEqualTo("2小时前");
        }

        @Test
        @DisplayName("Should format days ago")
        void shouldFormatDaysAgo() {
            long threeDaysAgo = System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000;
            assertThat(OpenDuration.formatRelativeTime(threeDaysAgo)).isEqualTo("3天前");
        }

        @Test
        @DisplayName("Should format months ago")
        void shouldFormatMonthsAgo() {
            long twoMonthsAgo = System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000;
            assertThat(OpenDuration.formatRelativeTime(twoMonthsAgo)).isEqualTo("2月前");
        }

        @Test
        @DisplayName("Should format years ago")
        void shouldFormatYearsAgo() {
            long twoYearsAgo = System.currentTimeMillis() - 730L * 24 * 60 * 60 * 1000;
            assertThat(OpenDuration.formatRelativeTime(twoYearsAgo)).isEqualTo("2年前");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenDuration.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
