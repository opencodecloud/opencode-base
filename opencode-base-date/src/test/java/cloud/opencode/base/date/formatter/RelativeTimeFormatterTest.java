package cloud.opencode.base.date.formatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * RelativeTimeFormatter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("RelativeTimeFormatter 测试")
class RelativeTimeFormatterTest {

    @Nested
    @DisplayName("英文格式化测试")
    class EnglishFormatTests {

        @Test
        @DisplayName("format() 刚刚")
        void testFormatJustNow() {
            LocalDateTime now = LocalDateTime.now();
            String result = RelativeTimeFormatter.format(now);

            assertThat(result).isEqualTo("just now");
        }

        @Test
        @DisplayName("format() 几秒前")
        void testFormatSecondsAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusSeconds(30);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("just now");
        }

        @Test
        @DisplayName("format() 1分钟前")
        void testFormat1MinuteAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusMinutes(1);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("1 minute ago");
        }

        @Test
        @DisplayName("format() 多分钟前")
        void testFormatMinutesAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusMinutes(5);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("5 minutes ago");
        }

        @Test
        @DisplayName("format() 1小时前")
        void testFormat1HourAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusHours(1);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("1 hour ago");
        }

        @Test
        @DisplayName("format() 多小时前")
        void testFormatHoursAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusHours(5);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("5 hours ago");
        }

        @Test
        @DisplayName("format() 1天前")
        void testFormat1DayAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusDays(1);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("1 day ago");
        }

        @Test
        @DisplayName("format() 多天前")
        void testFormatDaysAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusDays(3);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("3 days ago");
        }

        @Test
        @DisplayName("format() 1周前")
        void testFormat1WeekAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusWeeks(1);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("1 week ago");
        }

        @Test
        @DisplayName("format() 多周前")
        void testFormatWeeksAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusWeeks(3);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("3 weeks ago");
        }

        @Test
        @DisplayName("format() 1个月前")
        void testFormat1MonthAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusDays(35);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("1 month ago");
        }

        @Test
        @DisplayName("format() 多月前")
        void testFormatMonthsAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusDays(90);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("3 months ago");
        }

        @Test
        @DisplayName("format() 1年前")
        void testFormat1YearAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusYears(1);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("1 year ago");
        }

        @Test
        @DisplayName("format() 多年前")
        void testFormatYearsAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusYears(3);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("3 years ago");
        }
    }

    @Nested
    @DisplayName("英文未来时间格式化测试")
    class EnglishFutureFormatTests {

        @Test
        @DisplayName("format() 几分钟后")
        void testFormatInMinutes() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = now.plusMinutes(5);
            String result = RelativeTimeFormatter.format(future, now);

            assertThat(result).isEqualTo("in 5 minutes");
        }

        @Test
        @DisplayName("format() 几小时后")
        void testFormatInHours() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = now.plusHours(3);
            String result = RelativeTimeFormatter.format(future, now);

            assertThat(result).isEqualTo("in 3 hours");
        }

        @Test
        @DisplayName("format() 几天后")
        void testFormatInDays() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = now.plusDays(2);
            String result = RelativeTimeFormatter.format(future, now);

            assertThat(result).isEqualTo("in 2 days");
        }

        @Test
        @DisplayName("format() 几周后")
        void testFormatInWeeks() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = now.plusWeeks(2);
            String result = RelativeTimeFormatter.format(future, now);

            assertThat(result).isEqualTo("in 2 weeks");
        }

        @Test
        @DisplayName("format() 几月后")
        void testFormatInMonths() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = now.plusDays(60);
            String result = RelativeTimeFormatter.format(future, now);

            assertThat(result).isEqualTo("in 2 months");
        }

        @Test
        @DisplayName("format() 几年后")
        void testFormatInYears() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = now.plusYears(2);
            String result = RelativeTimeFormatter.format(future, now);

            assertThat(result).isEqualTo("in 2 years");
        }
    }

    @Nested
    @DisplayName("中文格式化测试")
    class ChineseFormatTests {

        @Test
        @DisplayName("formatChinese() 刚刚")
        void testFormatChineseJustNow() {
            LocalDateTime now = LocalDateTime.now();
            String result = RelativeTimeFormatter.formatChinese(now);

            assertThat(result).isEqualTo("刚刚");
        }

        @Test
        @DisplayName("formatChinese() 分钟前")
        void testFormatChineseMinutesAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusMinutes(5);
            String result = RelativeTimeFormatter.formatChinese(past, now);

            assertThat(result).isEqualTo("5分钟前");
        }

        @Test
        @DisplayName("formatChinese() 小时前")
        void testFormatChineseHoursAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusHours(3);
            String result = RelativeTimeFormatter.formatChinese(past, now);

            assertThat(result).isEqualTo("3小时前");
        }

        @Test
        @DisplayName("formatChinese() 天前")
        void testFormatChineseDaysAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusDays(2);
            String result = RelativeTimeFormatter.formatChinese(past, now);

            assertThat(result).isEqualTo("2天前");
        }

        @Test
        @DisplayName("formatChinese() 周前")
        void testFormatChineseWeeksAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusWeeks(2);
            String result = RelativeTimeFormatter.formatChinese(past, now);

            assertThat(result).isEqualTo("2周前");
        }

        @Test
        @DisplayName("formatChinese() 月前")
        void testFormatChineseMonthsAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusDays(60);
            String result = RelativeTimeFormatter.formatChinese(past, now);

            assertThat(result).isEqualTo("2个月前");
        }

        @Test
        @DisplayName("formatChinese() 年前")
        void testFormatChineseYearsAgo() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusYears(2);
            String result = RelativeTimeFormatter.formatChinese(past, now);

            assertThat(result).isEqualTo("2年前");
        }

        @Test
        @DisplayName("formatChinese() 分钟后")
        void testFormatChineseMinutesLater() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = now.plusMinutes(5);
            String result = RelativeTimeFormatter.formatChinese(future, now);

            assertThat(result).isEqualTo("5分钟后");
        }

        @Test
        @DisplayName("formatChinese() 天后")
        void testFormatChineseDaysLater() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = now.plusDays(3);
            String result = RelativeTimeFormatter.formatChinese(future, now);

            assertThat(result).isEqualTo("3天后");
        }
    }

    @Nested
    @DisplayName("Instant格式化测试")
    class InstantFormatTests {

        @Test
        @DisplayName("format(Instant) 格式化Instant")
        void testFormatInstant() {
            Instant instant = Instant.now();
            String result = RelativeTimeFormatter.format(instant);

            assertThat(result).isEqualTo("just now");
        }

        @Test
        @DisplayName("formatChinese(Instant) 中文格式化Instant")
        void testFormatChineseInstant() {
            Instant instant = Instant.now();
            String result = RelativeTimeFormatter.formatChinese(instant);

            assertThat(result).isEqualTo("刚刚");
        }
    }

    @Nested
    @DisplayName("Duration格式化测试")
    class DurationFormatTests {

        @Test
        @DisplayName("formatDuration() 格式化Duration")
        void testFormatDuration() {
            Duration duration = Duration.ofMinutes(5);
            String result = RelativeTimeFormatter.formatDuration(duration);

            assertThat(result).isEqualTo("5 minutes");
        }

        @Test
        @DisplayName("formatDuration() 格式化小时Duration")
        void testFormatDurationHours() {
            Duration duration = Duration.ofHours(3);
            String result = RelativeTimeFormatter.formatDuration(duration);

            assertThat(result).isEqualTo("3 hours");
        }

        @Test
        @DisplayName("formatDuration() 格式化天数Duration")
        void testFormatDurationDays() {
            Duration duration = Duration.ofDays(2);
            String result = RelativeTimeFormatter.formatDuration(duration);

            assertThat(result).isEqualTo("2 days");
        }

        @Test
        @DisplayName("formatDurationChinese() 中文格式化Duration")
        void testFormatDurationChinese() {
            Duration duration = Duration.ofMinutes(10);
            String result = RelativeTimeFormatter.formatDurationChinese(duration);

            assertThat(result).isEqualTo("10分钟");
        }

        @Test
        @DisplayName("formatDurationChinese() 中文格式化小时Duration")
        void testFormatDurationChineseHours() {
            Duration duration = Duration.ofHours(5);
            String result = RelativeTimeFormatter.formatDurationChinese(duration);

            assertThat(result).isEqualTo("5小时");
        }

        @Test
        @DisplayName("formatDuration() 格式化负Duration")
        void testFormatDurationNegative() {
            Duration duration = Duration.ofMinutes(-5);
            String result = RelativeTimeFormatter.formatDuration(duration);

            assertThat(result).isEqualTo("5 minutes");
        }
    }

    @Nested
    @DisplayName("智能格式化测试")
    class SmartFormatTests {

        @Test
        @DisplayName("formatSmart() 今天")
        void testFormatSmartToday() {
            LocalDateTime now = LocalDateTime.now();
            String result = RelativeTimeFormatter.formatSmart(now);

            assertThat(result).isEqualTo("just now");
        }

        @Test
        @DisplayName("formatSmart() 昨天")
        void testFormatSmartYesterday() {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            String result = RelativeTimeFormatter.formatSmart(yesterday);

            assertThat(result).isEqualTo("yesterday");
        }

        @Test
        @DisplayName("formatSmart() 明天")
        void testFormatSmartTomorrow() {
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            String result = RelativeTimeFormatter.formatSmart(tomorrow);

            assertThat(result).isEqualTo("tomorrow");
        }

        @Test
        @DisplayName("formatSmart() 前天")
        void testFormatSmart2DaysAgo() {
            LocalDateTime dayBeforeYesterday = LocalDateTime.now().minusDays(2);
            String result = RelativeTimeFormatter.formatSmart(dayBeforeYesterday);

            assertThat(result).isEqualTo("2 days ago");
        }

        @Test
        @DisplayName("formatSmart() 后天")
        void testFormatSmart2DaysLater() {
            LocalDateTime dayAfterTomorrow = LocalDateTime.now().plusDays(2);
            String result = RelativeTimeFormatter.formatSmart(dayAfterTomorrow);

            assertThat(result).isEqualTo("in 2 days");
        }

        @Test
        @DisplayName("formatSmart() 几天前")
        void testFormatSmartDaysAgo() {
            LocalDateTime fewDaysAgo = LocalDateTime.now().minusDays(5);
            String result = RelativeTimeFormatter.formatSmart(fewDaysAgo);

            assertThat(result).isEqualTo("5 days ago");
        }

        @Test
        @DisplayName("formatSmart() 几天后")
        void testFormatSmartDaysLater() {
            LocalDateTime fewDaysLater = LocalDateTime.now().plusDays(5);
            String result = RelativeTimeFormatter.formatSmart(fewDaysLater);

            assertThat(result).isEqualTo("in 5 days");
        }

        @Test
        @DisplayName("formatSmartChinese() 昨天")
        void testFormatSmartChineseYesterday() {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            String result = RelativeTimeFormatter.formatSmartChinese(yesterday);

            assertThat(result).isEqualTo("昨天");
        }

        @Test
        @DisplayName("formatSmartChinese() 明天")
        void testFormatSmartChineseTomorrow() {
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            String result = RelativeTimeFormatter.formatSmartChinese(tomorrow);

            assertThat(result).isEqualTo("明天");
        }

        @Test
        @DisplayName("formatSmartChinese() 前天")
        void testFormatSmartChinese2DaysAgo() {
            LocalDateTime dayBeforeYesterday = LocalDateTime.now().minusDays(2);
            String result = RelativeTimeFormatter.formatSmartChinese(dayBeforeYesterday);

            assertThat(result).isEqualTo("前天");
        }

        @Test
        @DisplayName("formatSmartChinese() 后天")
        void testFormatSmartChinese2DaysLater() {
            LocalDateTime dayAfterTomorrow = LocalDateTime.now().plusDays(2);
            String result = RelativeTimeFormatter.formatSmartChinese(dayAfterTomorrow);

            assertThat(result).isEqualTo("后天");
        }

        @Test
        @DisplayName("formatSmartChinese() 几天前")
        void testFormatSmartChineseDaysAgo() {
            LocalDateTime fewDaysAgo = LocalDateTime.now().minusDays(5);
            String result = RelativeTimeFormatter.formatSmartChinese(fewDaysAgo);

            assertThat(result).isEqualTo("5天前");
        }

        @Test
        @DisplayName("formatSmartChinese() 几天后")
        void testFormatSmartChineseDaysLater() {
            LocalDateTime fewDaysLater = LocalDateTime.now().plusDays(5);
            String result = RelativeTimeFormatter.formatSmartChinese(fewDaysLater);

            assertThat(result).isEqualTo("5天后");
        }

        @Test
        @DisplayName("formatSmart() 超过一周回退到常规格式")
        void testFormatSmartFallbackToRegular() {
            LocalDateTime longAgo = LocalDateTime.now().minusDays(30);
            String result = RelativeTimeFormatter.formatSmart(longAgo);

            assertThat(result).isEqualTo("1 month ago");
        }
    }

    @Nested
    @DisplayName("紧凑格式化测试")
    class CompactFormatTests {

        @Test
        @DisplayName("formatCompact() 秒")
        void testFormatCompactSeconds() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusSeconds(30);
            String result = RelativeTimeFormatter.formatCompact(past);

            assertThat(result).matches("\\d+s");
        }

        @Test
        @DisplayName("formatCompact() 分钟")
        void testFormatCompactMinutes() {
            LocalDateTime past = LocalDateTime.now().minusMinutes(5);
            String result = RelativeTimeFormatter.formatCompact(past);

            assertThat(result).isEqualTo("5m");
        }

        @Test
        @DisplayName("formatCompact() 小时")
        void testFormatCompactHours() {
            LocalDateTime past = LocalDateTime.now().minusHours(3);
            String result = RelativeTimeFormatter.formatCompact(past);

            assertThat(result).isEqualTo("3h");
        }

        @Test
        @DisplayName("formatCompact() 天")
        void testFormatCompactDays() {
            LocalDateTime past = LocalDateTime.now().minusDays(2);
            String result = RelativeTimeFormatter.formatCompact(past);

            assertThat(result).isEqualTo("2d");
        }

        @Test
        @DisplayName("formatCompact() 周")
        void testFormatCompactWeeks() {
            LocalDateTime past = LocalDateTime.now().minusWeeks(2);
            String result = RelativeTimeFormatter.formatCompact(past);

            assertThat(result).isEqualTo("2w");
        }

        @Test
        @DisplayName("formatCompact() 月")
        void testFormatCompactMonths() {
            LocalDateTime past = LocalDateTime.now().minusDays(60);
            String result = RelativeTimeFormatter.formatCompact(past);

            assertThat(result).isEqualTo("2mo");
        }

        @Test
        @DisplayName("formatCompact() 年")
        void testFormatCompactYears() {
            LocalDateTime past = LocalDateTime.now().minusYears(2);
            String result = RelativeTimeFormatter.formatCompact(past);

            assertThat(result).isEqualTo("2y");
        }
    }

    @Nested
    @DisplayName("LocalDate格式化测试")
    class LocalDateFormatTests {

        @Test
        @DisplayName("format() LocalDate与LocalDate比较")
        void testFormatLocalDateWithLocalDate() {
            LocalDate today = LocalDate.now();
            LocalDate past = today.minusDays(3);
            String result = RelativeTimeFormatter.format(past, today);

            assertThat(result).isEqualTo("3 days ago");
        }

        @Test
        @DisplayName("format() LocalDate与LocalDateTime比较")
        void testFormatLocalDateWithLocalDateTime() {
            LocalDate date = LocalDate.now().minusDays(2);
            LocalDateTime reference = LocalDateTime.now();
            String result = RelativeTimeFormatter.format(date, reference);

            assertThat(result).contains("day");
        }

        @Test
        @DisplayName("format() LocalDateTime与LocalDate比较")
        void testFormatLocalDateTimeWithLocalDate() {
            LocalDateTime dateTime = LocalDateTime.now().minusDays(2);
            LocalDate reference = LocalDate.now();
            String result = RelativeTimeFormatter.format(dateTime, reference);

            assertThat(result).contains("day");
        }
    }

    @Nested
    @DisplayName("null安全性测试")
    class NullSafetyTests {

        @Test
        @DisplayName("format() null temporal抛出异常")
        void testFormatNullTemporal() {
            assertThatThrownBy(() -> RelativeTimeFormatter.format(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("format() null reference抛出异常")
        void testFormatNullReference() {
            assertThatThrownBy(() -> RelativeTimeFormatter.format(LocalDateTime.now(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("formatChinese() null temporal抛出异常")
        void testFormatChineseNullTemporal() {
            assertThatThrownBy(() -> RelativeTimeFormatter.formatChinese(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("formatDuration() null duration抛出异常")
        void testFormatDurationNull() {
            assertThatThrownBy(() -> RelativeTimeFormatter.formatDuration(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("formatDurationChinese() null duration抛出异常")
        void testFormatDurationChineseNull() {
            assertThatThrownBy(() -> RelativeTimeFormatter.formatDurationChinese(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("formatCompact() null temporal抛出异常")
        void testFormatCompactNull() {
            assertThatThrownBy(() -> RelativeTimeFormatter.formatCompact(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("format() 恰好1分钟")
        void testFormatExactly1Minute() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusSeconds(60);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("1 minute ago");
        }

        @Test
        @DisplayName("format() 恰好1小时")
        void testFormatExactly1Hour() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusSeconds(3600);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("1 hour ago");
        }

        @Test
        @DisplayName("format() 恰好1天")
        void testFormatExactly1Day() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusSeconds(86400);
            String result = RelativeTimeFormatter.format(past, now);

            assertThat(result).isEqualTo("1 day ago");
        }

        @Test
        @DisplayName("format() 单数形式 - 1 second")
        void testFormatSingularSecond() {
            // Note: < 60 seconds returns "just now"
            // Test at the boundary
            Duration duration = Duration.ofSeconds(1);
            String result = RelativeTimeFormatter.formatDuration(duration);

            assertThat(result).isEqualTo("just now");
        }

        @Test
        @DisplayName("formatChinese() 秒")
        void testFormatChineseSeconds() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusSeconds(30);
            String result = RelativeTimeFormatter.formatChinese(past, now);

            assertThat(result).isEqualTo("刚刚");
        }
    }
}
