package cloud.opencode.base.date.formatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Period;

import static org.assertj.core.api.Assertions.*;

/**
 * PeriodFormatter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("PeriodFormatter 测试")
class PeriodFormatterTest {

    @Nested
    @DisplayName("Period格式化测试")
    class PeriodFormatTests {

        @Test
        @DisplayName("format() 格式化完整Period")
        void testFormatFullPeriod() {
            Period period = Period.of(1, 2, 15);
            String result = PeriodFormatter.format(period);

            assertThat(result).isEqualTo("1 year, 2 months, 15 days");
        }

        @Test
        @DisplayName("format() 格式化仅年份")
        void testFormatYearsOnly() {
            Period period = Period.ofYears(2);
            String result = PeriodFormatter.format(period);

            assertThat(result).isEqualTo("2 years");
        }

        @Test
        @DisplayName("format() 格式化仅月份")
        void testFormatMonthsOnly() {
            Period period = Period.ofMonths(5);
            String result = PeriodFormatter.format(period);

            assertThat(result).isEqualTo("5 months");
        }

        @Test
        @DisplayName("format() 格式化仅天数")
        void testFormatDaysOnly() {
            Period period = Period.ofDays(10);
            String result = PeriodFormatter.format(period);

            assertThat(result).isEqualTo("10 days");
        }

        @Test
        @DisplayName("format() 单数形式")
        void testFormatSingular() {
            Period period = Period.of(1, 1, 1);
            String result = PeriodFormatter.format(period);

            assertThat(result).isEqualTo("1 year, 1 month, 1 day");
        }

        @Test
        @DisplayName("format() 零Period")
        void testFormatZeroPeriod() {
            Period period = Period.ZERO;
            String result = PeriodFormatter.format(period);

            assertThat(result).isEqualTo("0 days");
        }

        @Test
        @DisplayName("formatChinese() 格式化中文Period")
        void testFormatChinesePeriod() {
            Period period = Period.of(1, 2, 15);
            String result = PeriodFormatter.formatChinese(period);

            assertThat(result).isEqualTo("1年2个月15天");
        }

        @Test
        @DisplayName("formatChinese() 零Period")
        void testFormatChineseZeroPeriod() {
            Period period = Period.ZERO;
            String result = PeriodFormatter.formatChinese(period);

            assertThat(result).isEqualTo("0天");
        }

        @Test
        @DisplayName("formatShort() 短格式")
        void testFormatShortPeriod() {
            Period period = Period.of(1, 2, 15);
            String result = PeriodFormatter.formatShort(period);

            assertThat(result).isEqualTo("1y 2m 15d");
        }

        @Test
        @DisplayName("formatShort() 零Period")
        void testFormatShortZeroPeriod() {
            Period period = Period.ZERO;
            String result = PeriodFormatter.formatShort(period);

            assertThat(result).isEqualTo("0d");
        }

        @Test
        @DisplayName("formatCompact() 紧凑格式")
        void testFormatCompactPeriod() {
            Period period = Period.of(1, 2, 15);
            String result = PeriodFormatter.formatCompact(period);

            assertThat(result).isEqualTo("P1Y2M15D");
        }
    }

    @Nested
    @DisplayName("Duration格式化测试")
    class DurationFormatTests {

        @Test
        @DisplayName("format() 格式化完整Duration")
        void testFormatFullDuration() {
            Duration duration = Duration.ofDays(1).plusHours(2).plusMinutes(30).plusSeconds(45);
            String result = PeriodFormatter.format(duration);

            assertThat(result).isEqualTo("1 day, 2 hours, 30 minutes, 45 seconds");
        }

        @Test
        @DisplayName("format() 格式化仅小时")
        void testFormatHoursOnly() {
            Duration duration = Duration.ofHours(5);
            String result = PeriodFormatter.format(duration);

            assertThat(result).isEqualTo("5 hours");
        }

        @Test
        @DisplayName("format() 格式化仅分钟")
        void testFormatMinutesOnly() {
            Duration duration = Duration.ofMinutes(30);
            String result = PeriodFormatter.format(duration);

            assertThat(result).isEqualTo("30 minutes");
        }

        @Test
        @DisplayName("format() 格式化仅秒")
        void testFormatSecondsOnly() {
            Duration duration = Duration.ofSeconds(45);
            String result = PeriodFormatter.format(duration);

            assertThat(result).isEqualTo("45 seconds");
        }

        @Test
        @DisplayName("format() 单数形式")
        void testFormatSingularDuration() {
            Duration duration = Duration.ofHours(1).plusMinutes(1).plusSeconds(1);
            String result = PeriodFormatter.format(duration);

            assertThat(result).isEqualTo("1 hour, 1 minute, 1 second");
        }

        @Test
        @DisplayName("format() 零Duration")
        void testFormatZeroDuration() {
            Duration duration = Duration.ZERO;
            String result = PeriodFormatter.format(duration);

            assertThat(result).isEqualTo("0 seconds");
        }

        @Test
        @DisplayName("format() 负Duration")
        void testFormatNegativeDuration() {
            Duration duration = Duration.ofHours(-2);
            String result = PeriodFormatter.format(duration);

            assertThat(result).startsWith("-");
            assertThat(result).contains("2 hours");
        }

        @Test
        @DisplayName("formatChinese() 格式化中文Duration")
        void testFormatChineseDuration() {
            Duration duration = Duration.ofDays(1).plusHours(2).plusMinutes(30).plusSeconds(45);
            String result = PeriodFormatter.formatChinese(duration);

            assertThat(result).isEqualTo("1天2小时30分钟45秒");
        }

        @Test
        @DisplayName("formatChinese() 零Duration")
        void testFormatChineseZeroDuration() {
            Duration duration = Duration.ZERO;
            String result = PeriodFormatter.formatChinese(duration);

            assertThat(result).isEqualTo("0秒");
        }

        @Test
        @DisplayName("formatShort() 短格式Duration")
        void testFormatShortDuration() {
            Duration duration = Duration.ofDays(1).plusHours(2).plusMinutes(30).plusSeconds(15);
            String result = PeriodFormatter.formatShort(duration);

            assertThat(result).isEqualTo("1d 2h 30m 15s");
        }

        @Test
        @DisplayName("formatShort() 零Duration")
        void testFormatShortZeroDuration() {
            Duration duration = Duration.ZERO;
            String result = PeriodFormatter.formatShort(duration);

            assertThat(result).isEqualTo("0s");
        }

        @Test
        @DisplayName("formatTime() 时间格式")
        void testFormatTimeDuration() {
            Duration duration = Duration.ofHours(2).plusMinutes(30).plusSeconds(45);
            String result = PeriodFormatter.formatTime(duration);

            assertThat(result).isEqualTo("02:30:45");
        }

        @Test
        @DisplayName("formatTime() 超过24小时")
        void testFormatTimeOver24Hours() {
            Duration duration = Duration.ofHours(30).plusMinutes(15).plusSeconds(30);
            String result = PeriodFormatter.formatTime(duration);

            assertThat(result).isEqualTo("30:15:30");
        }

        @Test
        @DisplayName("formatTime() 负Duration")
        void testFormatTimeNegative() {
            Duration duration = Duration.ofHours(-2).minusMinutes(30);
            String result = PeriodFormatter.formatTime(duration);

            assertThat(result).startsWith("-");
        }

        @Test
        @DisplayName("formatCompact() 紧凑格式Duration")
        void testFormatCompactDuration() {
            Duration duration = Duration.ofHours(2).plusMinutes(30);
            String result = PeriodFormatter.formatCompact(duration);

            assertThat(result).isEqualTo("PT2H30M");
        }
    }

    @Nested
    @DisplayName("Period解析测试")
    class ParsePeriodTests {

        @Test
        @DisplayName("parsePeriod() 解析完整格式")
        void testParsePeriodFull() {
            Period result = PeriodFormatter.parsePeriod("2 years 3 months 15 days");

            assertThat(result.getYears()).isEqualTo(2);
            assertThat(result.getMonths()).isEqualTo(3);
            assertThat(result.getDays()).isEqualTo(15);
        }

        @Test
        @DisplayName("parsePeriod() 解析ISO格式")
        void testParsePeriodIso() {
            Period result = PeriodFormatter.parsePeriod("P1Y2M15D");

            assertThat(result.getYears()).isEqualTo(1);
            assertThat(result.getMonths()).isEqualTo(2);
            assertThat(result.getDays()).isEqualTo(15);
        }

        @Test
        @DisplayName("parsePeriod() 解析短格式")
        void testParsePeriodShort() {
            Period result = PeriodFormatter.parsePeriod("1y 2m 15d");

            assertThat(result.getYears()).isEqualTo(1);
            assertThat(result.getMonths()).isEqualTo(2);
            assertThat(result.getDays()).isEqualTo(15);
        }

        @Test
        @DisplayName("parsePeriod() 解析中文格式")
        void testParsePeriodChinese() {
            Period result = PeriodFormatter.parsePeriod("2年3个月15天");

            assertThat(result.getYears()).isEqualTo(2);
            assertThat(result.getMonths()).isEqualTo(3);
            assertThat(result.getDays()).isEqualTo(15);
        }

        @Test
        @DisplayName("parsePeriod() 解析周")
        void testParsePeriodWeeks() {
            Period result = PeriodFormatter.parsePeriod("2 weeks");

            assertThat(result.getDays()).isEqualTo(14);
        }

        @Test
        @DisplayName("parsePeriod() 无效格式抛出异常")
        void testParsePeriodInvalidThrows() {
            assertThatThrownBy(() -> PeriodFormatter.parsePeriod("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("tryParsePeriod() 成功返回结果")
        void testTryParsePeriodSuccess() {
            Period result = PeriodFormatter.tryParsePeriod("2 years");

            assertThat(result).isNotNull();
            assertThat(result.getYears()).isEqualTo(2);
        }

        @Test
        @DisplayName("tryParsePeriod() 失败返回null")
        void testTryParsePeriodFailure() {
            Period result = PeriodFormatter.tryParsePeriod("invalid");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Duration解析测试")
    class ParseDurationTests {

        @Test
        @DisplayName("parseDuration() 解析完整格式")
        void testParseDurationFull() {
            Duration result = PeriodFormatter.parseDuration("2 days 3 hours 30 minutes 45 seconds");

            assertThat(result.toDays()).isEqualTo(2);
            assertThat(result.toHoursPart()).isEqualTo(3);
            assertThat(result.toMinutesPart()).isEqualTo(30);
            assertThat(result.toSecondsPart()).isEqualTo(45);
        }

        @Test
        @DisplayName("parseDuration() 解析ISO格式")
        void testParseDurationIso() {
            Duration result = PeriodFormatter.parseDuration("PT2H30M");

            assertThat(result.toHours()).isEqualTo(2);
            assertThat(result.toMinutesPart()).isEqualTo(30);
        }

        @Test
        @DisplayName("parseDuration() 解析短格式")
        void testParseDurationShort() {
            Duration result = PeriodFormatter.parseDuration("2h 30m 15s");

            assertThat(result.toHours()).isEqualTo(2);
            assertThat(result.toMinutesPart()).isEqualTo(30);
            assertThat(result.toSecondsPart()).isEqualTo(15);
        }

        @Test
        @DisplayName("parseDuration() 解析时间格式")
        void testParseDurationTimeFormat() {
            Duration result = PeriodFormatter.parseDuration("02:30:45");

            assertThat(result.toHours()).isEqualTo(2);
            assertThat(result.toMinutesPart()).isEqualTo(30);
            assertThat(result.toSecondsPart()).isEqualTo(45);
        }

        @Test
        @DisplayName("parseDuration() 解析中文格式")
        void testParseDurationChinese() {
            Duration result = PeriodFormatter.parseDuration("2天3小时30分钟45秒");

            assertThat(result.toDays()).isEqualTo(2);
            assertThat(result.toHoursPart()).isEqualTo(3);
        }

        @Test
        @DisplayName("parseDuration() 解析负时间格式")
        void testParseDurationNegativeTimeFormat() {
            Duration result = PeriodFormatter.parseDuration("-02:30:45");

            assertThat(result.isNegative()).isTrue();
        }

        @Test
        @DisplayName("parseDuration() 无效格式抛出异常")
        void testParseDurationInvalidThrows() {
            assertThatThrownBy(() -> PeriodFormatter.parseDuration("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("tryParseDuration() 成功返回结果")
        void testTryParseDurationSuccess() {
            Duration result = PeriodFormatter.tryParseDuration("2 hours");

            assertThat(result).isNotNull();
            assertThat(result.toHours()).isEqualTo(2);
        }

        @Test
        @DisplayName("tryParseDuration() 失败返回null")
        void testTryParseDurationFailure() {
            Duration result = PeriodFormatter.tryParseDuration("invalid");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("通用格式化测试")
    class GenericFormatTests {

        @Test
        @DisplayName("format(TemporalAmount) 格式化Period")
        void testFormatTemporalAmountPeriod() {
            Period period = Period.ofDays(10);
            String result = PeriodFormatter.format(period);

            assertThat(result).isEqualTo("10 days");
        }

        @Test
        @DisplayName("format(TemporalAmount) 格式化Duration")
        void testFormatTemporalAmountDuration() {
            Duration duration = Duration.ofHours(5);
            String result = PeriodFormatter.format(duration);

            assertThat(result).isEqualTo("5 hours");
        }
    }

    @Nested
    @DisplayName("null安全性测试")
    class NullSafetyTests {

        @Test
        @DisplayName("format(Period) null参数抛出异常")
        void testFormatPeriodNull() {
            assertThatThrownBy(() -> PeriodFormatter.format((Period) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("format(Duration) null参数抛出异常")
        void testFormatDurationNull() {
            assertThatThrownBy(() -> PeriodFormatter.format((Duration) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("parsePeriod() null参数抛出异常")
        void testParsePeriodNull() {
            assertThatThrownBy(() -> PeriodFormatter.parsePeriod(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("parseDuration() null参数抛出异常")
        void testParseDurationNull() {
            assertThatThrownBy(() -> PeriodFormatter.parseDuration(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
