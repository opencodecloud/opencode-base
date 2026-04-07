package cloud.opencode.base.cron;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CronDescriber")
class CronDescriberTest {

    private String describe(String expression) {
        return CronExpression.parse(expression).describe();
    }

    @Nested
    @DisplayName("time patterns")
    class TimePatterns {

        @Test
        @DisplayName("should describe every minute")
        void shouldDescribeEveryMinute() {
            assertThat(describe("* * * * *")).isEqualTo("Every minute");
        }

        @Test
        @DisplayName("should describe every N minutes")
        void shouldDescribeEveryNMinutes() {
            assertThat(describe("*/5 * * * *")).isEqualTo("Every 5 minutes");
        }

        @Test
        @DisplayName("should describe specific time")
        void shouldDescribeSpecificTime() {
            assertThat(describe("0 9 * * *")).isEqualTo("At 09:00");
        }

        @Test
        @DisplayName("should describe every N hours")
        void shouldDescribeEveryNHours() {
            assertThat(describe("* */2 * * *")).isEqualTo("Every 2 hours");
        }

        @Test
        @DisplayName("should describe at minute of every hour")
        void shouldDescribeMinuteOfEveryHour() {
            assertThat(describe("30 * * * *")).isEqualTo("At minute 30 of every hour");
        }
    }

    @Nested
    @DisplayName("time with seconds")
    class SecondsPatterns {

        @Test
        @DisplayName("should describe every second")
        void shouldDescribeEverySecond() {
            assertThat(describe("* * * * * *")).isEqualTo("Every second");
        }

        @Test
        @DisplayName("should describe every N seconds")
        void shouldDescribeEveryNSeconds() {
            assertThat(describe("*/10 * * * * *")).isEqualTo("Every 10 seconds");
        }
    }

    @Nested
    @DisplayName("day of week patterns")
    class DayOfWeekPatterns {

        @Test
        @DisplayName("should describe Monday through Friday")
        void shouldDescribeWeekdays() {
            String result = describe("0 9 * * MON-FRI");
            assertThat(result).contains("Monday through Friday");
        }

        @Test
        @DisplayName("should describe single day of week")
        void shouldDescribeSingleDay() {
            String result = describe("0 9 * * MON");
            assertThat(result).contains("Monday");
        }

        @Test
        @DisplayName("should describe weekends")
        void shouldDescribeWeekends() {
            String result = describe("0 9 * * SAT,SUN");
            assertThat(result).contains("weekends");
        }
    }

    @Nested
    @DisplayName("day of month patterns")
    class DayOfMonthPatterns {

        @Test
        @DisplayName("should describe specific day of month")
        void shouldDescribeSpecificDay() {
            String result = describe("0 9 15 * *");
            assertThat(result).contains("day 15");
        }

        @Test
        @DisplayName("should describe last day of month")
        void shouldDescribeLastDay() {
            String result = describe("0 9 L * *");
            assertThat(result).contains("last day of the month");
        }
    }

    @Nested
    @DisplayName("month patterns")
    class MonthPatterns {

        @Test
        @DisplayName("should describe specific month")
        void shouldDescribeSpecificMonth() {
            String result = describe("0 9 * 1 *");
            assertThat(result).contains("January");
        }

        @Test
        @DisplayName("should describe all months as empty")
        void shouldDescribeAllMonths() {
            String result = describe("0 9 * * *");
            assertThat(result).doesNotContain("January")
                    .doesNotContain("in ");
        }
    }

    @Nested
    @DisplayName("complex expressions")
    class ComplexExpressions {

        @Test
        @DisplayName("should describe weekday time expression")
        void shouldDescribeWeekdayTime() {
            String result = describe("0 9 * * MON-FRI");
            assertThat(result).contains("09:00")
                    .contains("Monday through Friday");
        }

        @Test
        @DisplayName("should describe monthly expression")
        void shouldDescribeMonthly() {
            String result = describe("0 0 1 * *");
            assertThat(result).contains("00:00")
                    .contains("day 1");
        }
    }
}
