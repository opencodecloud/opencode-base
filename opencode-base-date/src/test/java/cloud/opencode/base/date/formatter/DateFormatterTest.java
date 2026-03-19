package cloud.opencode.base.date.formatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * DateFormatter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("DateFormatter 测试")
class DateFormatterTest {

    @Nested
    @DisplayName("预定义日期格式测试")
    class PredefinedDateFormatTests {

        @Test
        @DisplayName("NORM_DATE 格式化日期")
        void testNormDateFormat() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            String result = DateFormatter.NORM_DATE.format(date);
            assertThat(result).isEqualTo("2024-01-15");
        }

        @Test
        @DisplayName("NORM_TIME 格式化时间")
        void testNormTimeFormat() {
            LocalTime time = LocalTime.of(14, 30, 45);
            String result = DateFormatter.NORM_TIME.format(time);
            assertThat(result).isEqualTo("14:30:45");
        }

        @Test
        @DisplayName("NORM_DATETIME 格式化日期时间")
        void testNormDateTimeFormat() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);
            String result = DateFormatter.NORM_DATETIME.format(dateTime);
            assertThat(result).isEqualTo("2024-01-15 14:30:45");
        }

        @Test
        @DisplayName("NORM_DATETIME_MS 格式化带毫秒的日期时间")
        void testNormDateTimeMsFormat() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45, 123000000);
            String result = DateFormatter.NORM_DATETIME_MS.format(dateTime);
            assertThat(result).isEqualTo("2024-01-15 14:30:45.123");
        }

        @Test
        @DisplayName("NORM_TIME_MS 格式化带毫秒的时间")
        void testNormTimeMsFormat() {
            LocalTime time = LocalTime.of(14, 30, 45, 123000000);
            String result = DateFormatter.NORM_TIME_MS.format(time);
            assertThat(result).isEqualTo("14:30:45.123");
        }
    }

    @Nested
    @DisplayName("紧凑格式测试")
    class CompactFormatTests {

        @Test
        @DisplayName("PURE_DATE 紧凑日期格式")
        void testPureDateFormat() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            String result = DateFormatter.PURE_DATE.format(date);
            assertThat(result).isEqualTo("20240115");
        }

        @Test
        @DisplayName("PURE_TIME 紧凑时间格式")
        void testPureTimeFormat() {
            LocalTime time = LocalTime.of(14, 30, 45);
            String result = DateFormatter.PURE_TIME.format(time);
            assertThat(result).isEqualTo("143045");
        }

        @Test
        @DisplayName("PURE_DATETIME 紧凑日期时间格式")
        void testPureDateTimeFormat() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);
            String result = DateFormatter.PURE_DATETIME.format(dateTime);
            assertThat(result).isEqualTo("20240115143045");
        }

        @Test
        @DisplayName("PURE_DATETIME_MS 紧凑日期时间带毫秒格式")
        void testPureDateTimeMsFormat() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45, 123000000);
            String result = DateFormatter.PURE_DATETIME_MS.format(dateTime);
            assertThat(result).isEqualTo("20240115143045123");
        }
    }

    @Nested
    @DisplayName("中文格式测试")
    class ChineseFormatTests {

        @Test
        @DisplayName("CHINESE_DATE 中文日期格式")
        void testChineseDateFormat() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            String result = DateFormatter.CHINESE_DATE.format(date);
            assertThat(result).isEqualTo("2024年01月15日");
        }

        @Test
        @DisplayName("CHINESE_TIME 中文时间格式")
        void testChineseTimeFormat() {
            LocalTime time = LocalTime.of(14, 30, 45);
            String result = DateFormatter.CHINESE_TIME.format(time);
            assertThat(result).isEqualTo("14时30分45秒");
        }

        @Test
        @DisplayName("CHINESE_DATETIME 中文日期时间格式")
        void testChineseDateTimeFormat() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);
            String result = DateFormatter.CHINESE_DATETIME.format(dateTime);
            assertThat(result).isEqualTo("2024年01月15日 14时30分45秒");
        }

        @Test
        @DisplayName("CHINESE_DATE_SHORT 短中文日期格式")
        void testChineseDateShortFormat() {
            LocalDate date = LocalDate.of(2024, 1, 5);
            String result = DateFormatter.CHINESE_DATE_SHORT.format(date);
            assertThat(result).isEqualTo("2024年1月5日");
        }

        @Test
        @DisplayName("CHINESE_MONTH 中文月份格式")
        void testChineseMonthFormat() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            String result = DateFormatter.CHINESE_MONTH.format(date);
            assertThat(result).isEqualTo("2024年01月");
        }
    }

    @Nested
    @DisplayName("ISO格式测试")
    class IsoFormatTests {

        @Test
        @DisplayName("ISO_DATE 格式化日期")
        void testIsoDateFormat() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            String result = DateFormatter.ISO_DATE.format(date);
            assertThat(result).isEqualTo("2024-01-15");
        }

        @Test
        @DisplayName("ISO_DATETIME 格式化日期时间")
        void testIsoDateTimeFormat() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);
            String result = DateFormatter.ISO_DATETIME.format(dateTime);
            assertThat(result).contains("2024-01-15T14:30:45");
        }

        @Test
        @DisplayName("ISO_OFFSET_DATETIME 格式化偏移日期时间")
        void testIsoOffsetDateTimeFormat() {
            OffsetDateTime dateTime = OffsetDateTime.of(2024, 1, 15, 14, 30, 45, 0, ZoneOffset.of("+08:00"));
            String result = DateFormatter.ISO_OFFSET_DATETIME.format(dateTime);
            assertThat(result).contains("+08:00");
        }

        @Test
        @DisplayName("ISO_ZONED_DATETIME 格式化时区日期时间")
        void testIsoZonedDateTimeFormat() {
            ZonedDateTime dateTime = ZonedDateTime.of(2024, 1, 15, 14, 30, 45, 0, ZoneId.of("Asia/Shanghai"));
            String result = DateFormatter.ISO_ZONED_DATETIME.format(dateTime);
            assertThat(result).contains("Asia/Shanghai");
        }

        @Test
        @DisplayName("ISO_TIME 格式化时间")
        void testIsoTimeFormat() {
            LocalTime time = LocalTime.of(14, 30, 45);
            String result = DateFormatter.ISO_TIME.format(time);
            assertThat(result).isEqualTo("14:30:45");
        }

        @Test
        @DisplayName("HTTP_DATE 格式化HTTP日期")
        void testHttpDateFormat() {
            ZonedDateTime dateTime = ZonedDateTime.of(2024, 1, 15, 14, 30, 45, 0, ZoneOffset.UTC);
            String result = DateFormatter.HTTP_DATE.format(dateTime);
            assertThat(result).contains("Mon, 15 Jan 2024");
        }
    }

    @Nested
    @DisplayName("月份/年份格式测试")
    class MonthYearFormatTests {

        @Test
        @DisplayName("NORM_MONTH 年月格式")
        void testNormMonthFormat() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            String result = DateFormatter.NORM_MONTH.format(date);
            assertThat(result).isEqualTo("2024-01");
        }

        @Test
        @DisplayName("NORM_YEAR 年份格式")
        void testNormYearFormat() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            String result = DateFormatter.NORM_YEAR.format(date);
            assertThat(result).isEqualTo("2024");
        }
    }

    @Nested
    @DisplayName("ofPattern() 缓存测试")
    class OfPatternCacheTests {

        @Test
        @DisplayName("ofPattern() 返回相同的格式化器实例")
        void testOfPatternCaching() {
            var formatter1 = DateFormatter.ofPattern("yyyy-MM-dd");
            var formatter2 = DateFormatter.ofPattern("yyyy-MM-dd");

            assertThat(formatter1).isSameAs(formatter2);
        }

        @Test
        @DisplayName("ofPattern() 不同模式返回不同的格式化器")
        void testOfPatternDifferentPatterns() {
            var formatter1 = DateFormatter.ofPattern("yyyy-MM-dd");
            var formatter2 = DateFormatter.ofPattern("yyyy/MM/dd");

            assertThat(formatter1).isNotSameAs(formatter2);
        }

        @Test
        @DisplayName("ofPattern(pattern, locale) 创建带区域设置的格式化器")
        void testOfPatternWithLocale() {
            var formatter = DateFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA);
            assertThat(formatter).isNotNull();
        }

        @Test
        @DisplayName("null模式抛出NullPointerException")
        void testOfPatternNullThrows() {
            assertThatThrownBy(() -> DateFormatter.ofPattern(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("快速格式化方法测试")
    class QuickFormatMethodTests {

        @Test
        @DisplayName("format(temporal, pattern) 使用模式格式化")
        void testFormatWithPattern() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
            String result = DateFormatter.format(dateTime, "yyyy/MM/dd HH:mm");
            assertThat(result).isEqualTo("2024/01/15 10:30");
        }

        @Test
        @DisplayName("formatDate() 格式化日期")
        void testFormatDate() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            String result = DateFormatter.formatDate(date);
            assertThat(result).isEqualTo("2024-01-15");
        }

        @Test
        @DisplayName("formatTime() 格式化时间")
        void testFormatTime() {
            LocalTime time = LocalTime.of(10, 30, 45);
            String result = DateFormatter.formatTime(time);
            assertThat(result).isEqualTo("10:30:45");
        }

        @Test
        @DisplayName("formatDateTime() 格式化日期时间")
        void testFormatDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
            String result = DateFormatter.formatDateTime(dateTime);
            assertThat(result).isEqualTo("2024-01-15 10:30:45");
        }

        @Test
        @DisplayName("formatIso() 格式化为ISO格式")
        void testFormatIso() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
            String result = DateFormatter.formatIso(dateTime);
            assertThat(result).contains("2024-01-15T10:30:45");
        }

        @Test
        @DisplayName("formatChinese(LocalDate) 格式化为中文日期")
        void testFormatChineseDate() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            String result = DateFormatter.formatChinese(date);
            assertThat(result).isEqualTo("2024年01月15日");
        }

        @Test
        @DisplayName("formatChinese(LocalDateTime) 格式化为中文日期时间")
        void testFormatChineseDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
            String result = DateFormatter.formatChinese(dateTime);
            assertThat(result).isEqualTo("2024年01月15日 10时30分45秒");
        }
    }

    @Nested
    @DisplayName("缓存管理测试")
    class CacheManagementTests {

        @Test
        @DisplayName("cacheSize() 返回缓存大小")
        void testCacheSize() {
            DateFormatter.clearCache();
            int initialSize = DateFormatter.cacheSize();

            DateFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateFormatter.ofPattern("yyyy/MM/dd HH:mm");

            assertThat(DateFormatter.cacheSize()).isGreaterThan(initialSize);
        }

        @Test
        @DisplayName("clearCache() 清除缓存")
        void testClearCache() {
            DateFormatter.ofPattern("yyyy-MM-dd");

            DateFormatter.clearCache();

            assertThat(DateFormatter.cacheSize()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("null安全性测试")
    class NullSafetyTests {

        @Test
        @DisplayName("formatDate() null参数抛出异常")
        void testFormatDateNull() {
            assertThatThrownBy(() -> DateFormatter.formatDate(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("formatTime() null参数抛出异常")
        void testFormatTimeNull() {
            assertThatThrownBy(() -> DateFormatter.formatTime(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("formatDateTime() null参数抛出异常")
        void testFormatDateTimeNull() {
            assertThatThrownBy(() -> DateFormatter.formatDateTime(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("format(temporal, pattern) null temporal抛出异常")
        void testFormatNullTemporal() {
            assertThatThrownBy(() -> DateFormatter.format(null, "yyyy-MM-dd"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("弹性格式测试")
    class FlexibleFormatTests {

        @Test
        @DisplayName("FLEXIBLE_DATETIME 解析带分隔符的日期")
        void testFlexibleDateTimeFormat() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);
            String result = DateFormatter.FLEXIBLE_DATETIME.format(dateTime);
            assertThat(result).contains("2024");
            assertThat(result).contains("01");
            assertThat(result).contains("15");
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("处理最小日期")
        void testMinDate() {
            LocalDate date = LocalDate.of(1, 1, 1);
            String result = DateFormatter.NORM_DATE.format(date);
            assertThat(result).isEqualTo("0001-01-01");
        }

        @Test
        @DisplayName("处理午夜时间")
        void testMidnightTime() {
            LocalTime time = LocalTime.MIDNIGHT;
            String result = DateFormatter.NORM_TIME.format(time);
            assertThat(result).isEqualTo("00:00:00");
        }

        @Test
        @DisplayName("处理一天的最后时刻")
        void testEndOfDayTime() {
            LocalTime time = LocalTime.of(23, 59, 59);
            String result = DateFormatter.NORM_TIME.format(time);
            assertThat(result).isEqualTo("23:59:59");
        }
    }
}
