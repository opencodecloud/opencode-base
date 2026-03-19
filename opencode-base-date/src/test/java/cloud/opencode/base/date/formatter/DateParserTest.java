package cloud.opencode.base.date.formatter;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DateParser 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("DateParser 测试")
class DateParserTest {

    @Nested
    @DisplayName("parseDateTime() 智能解析测试")
    class ParseDateTimeTests {

        @Test
        @DisplayName("解析标准日期时间格式")
        void testParseStandardDateTime() {
            LocalDateTime result = DateParser.parseDateTime("2024-01-15 10:30:45");

            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonthValue()).isEqualTo(1);
            assertThat(result.getDayOfMonth()).isEqualTo(15);
            assertThat(result.getHour()).isEqualTo(10);
            assertThat(result.getMinute()).isEqualTo(30);
            assertThat(result.getSecond()).isEqualTo(45);
        }

        @Test
        @DisplayName("解析斜杠分隔的日期时间")
        void testParseSlashDateTime() {
            LocalDateTime result = DateParser.parseDateTime("2024/01/15 10:30:45");

            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonthValue()).isEqualTo(1);
            assertThat(result.getDayOfMonth()).isEqualTo(15);
        }

        @Test
        @DisplayName("解析ISO格式日期时间")
        void testParseIsoDateTime() {
            LocalDateTime result = DateParser.parseDateTime("2024-01-15T10:30:45");

            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getHour()).isEqualTo(10);
        }

        @Test
        @DisplayName("解析带毫秒的日期时间")
        void testParseDateTimeWithMillis() {
            LocalDateTime result = DateParser.parseDateTime("2024-01-15 10:30:45.123");

            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getNano()).isEqualTo(123000000);
        }

        @Test
        @DisplayName("解析紧凑格式日期时间")
        void testParsePureDatetime() {
            LocalDateTime result = DateParser.parseDateTime("20240115143045");

            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonthValue()).isEqualTo(1);
            assertThat(result.getDayOfMonth()).isEqualTo(15);
            assertThat(result.getHour()).isEqualTo(14);
            assertThat(result.getMinute()).isEqualTo(30);
            assertThat(result.getSecond()).isEqualTo(45);
        }

        @Test
        @DisplayName("解析中文日期时间格式")
        void testParseChineseDateTime() {
            LocalDateTime result = DateParser.parseDateTime("2024年01月15日 10时30分45秒");

            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonthValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("解析毫秒时间戳")
        void testParseEpochMilli() {
            long epochMilli = 1705312245000L;
            LocalDateTime result = DateParser.parseDateTime(String.valueOf(epochMilli));

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("解析秒时间戳")
        void testParseEpochSecond() {
            long epochSecond = 1705312245L;
            LocalDateTime result = DateParser.parseDateTime(String.valueOf(epochSecond));

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("null字符串抛出异常")
        void testParseNullThrows() {
            assertThatThrownBy(() -> DateParser.parseDateTime(null))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("空字符串抛出异常")
        void testParseBlankThrows() {
            assertThatThrownBy(() -> DateParser.parseDateTime(""))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("无效格式抛出异常")
        void testParseInvalidThrows() {
            assertThatThrownBy(() -> DateParser.parseDateTime("invalid-date"))
                    .isInstanceOf(OpenDateException.class);
        }
    }

    @Nested
    @DisplayName("parseDate() 日期解析测试")
    class ParseDateTests {

        @Test
        @DisplayName("解析标准日期格式")
        void testParseStandardDate() {
            LocalDate result = DateParser.parseDate("2024-01-15");

            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("解析斜杠分隔的日期")
        void testParseSlashDate() {
            LocalDate result = DateParser.parseDate("2024/01/15");

            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("解析点分隔的日期")
        void testParseDotDate() {
            LocalDate result = DateParser.parseDate("2024.01.15");

            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("解析紧凑格式日期")
        void testParsePureDate() {
            LocalDate result = DateParser.parseDate("20240115");

            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("解析中文日期格式")
        void testParseChineseDate() {
            LocalDate result = DateParser.parseDate("2024年01月15日");

            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("从日期时间字符串提取日期")
        void testParseDateFromDateTime() {
            LocalDate result = DateParser.parseDate("2024-01-15 10:30:45");

            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }
    }

    @Nested
    @DisplayName("parseTime() 时间解析测试")
    class ParseTimeTests {

        @Test
        @DisplayName("解析标准时间格式")
        void testParseStandardTime() {
            LocalTime result = DateParser.parseTime("10:30:45");

            assertThat(result).isEqualTo(LocalTime.of(10, 30, 45));
        }

        @Test
        @DisplayName("解析不带秒的时间")
        void testParseTimeWithoutSeconds() {
            LocalTime result = DateParser.parseTime("10:30");

            assertThat(result).isEqualTo(LocalTime.of(10, 30));
        }

        @Test
        @DisplayName("解析带毫秒的时间")
        void testParseTimeWithMillis() {
            LocalTime result = DateParser.parseTime("10:30:45.123");

            assertThat(result.getHour()).isEqualTo(10);
            assertThat(result.getMinute()).isEqualTo(30);
            assertThat(result.getSecond()).isEqualTo(45);
        }

        @Test
        @DisplayName("解析紧凑时间格式")
        void testParsePureTime() {
            LocalTime result = DateParser.parseTime("143045");

            assertThat(result).isEqualTo(LocalTime.of(14, 30, 45));
        }

        @Test
        @DisplayName("解析中文时间格式")
        void testParseChineseTime() {
            LocalTime result = DateParser.parseTime("10时30分45秒");

            assertThat(result).isEqualTo(LocalTime.of(10, 30, 45));
        }
    }

    @Nested
    @DisplayName("parse() 显式模式解析测试")
    class ExplicitPatternParseTests {

        @Test
        @DisplayName("使用自定义模式解析日期时间")
        void testParseWithPattern() {
            LocalDateTime result = DateParser.parse("15/01/2024 10:30:45", "dd/MM/yyyy HH:mm:ss");

            assertThat(result.getDayOfMonth()).isEqualTo(15);
            assertThat(result.getMonthValue()).isEqualTo(1);
            assertThat(result.getYear()).isEqualTo(2024);
        }

        @Test
        @DisplayName("parseDate() 使用自定义模式")
        void testParseDateWithPattern() {
            LocalDate result = DateParser.parseDate("15/01/2024", "dd/MM/yyyy");

            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("parseTime() 使用自定义模式")
        void testParseTimeWithPattern() {
            LocalTime result = DateParser.parseTime("10-30-45", "HH-mm-ss");

            assertThat(result).isEqualTo(LocalTime.of(10, 30, 45));
        }

        @Test
        @DisplayName("模式不匹配抛出异常")
        void testPatternMismatchThrows() {
            assertThatThrownBy(() -> DateParser.parse("2024-01-15", "dd/MM/yyyy"))
                    .isInstanceOf(OpenDateException.class);
        }
    }

    @Nested
    @DisplayName("时间戳转换测试")
    class EpochConversionTests {

        @Test
        @DisplayName("fromEpochMilli() 从毫秒时间戳创建")
        void testFromEpochMilli() {
            long epochMilli = System.currentTimeMillis();
            LocalDateTime result = DateParser.fromEpochMilli(epochMilli);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("fromEpochMilli() 指定时区")
        void testFromEpochMilliWithZone() {
            long epochMilli = 1705312245000L;
            ZoneId tokyo = ZoneId.of("Asia/Tokyo");
            LocalDateTime result = DateParser.fromEpochMilli(epochMilli, tokyo);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("fromEpochSecond() 从秒时间戳创建")
        void testFromEpochSecond() {
            long epochSecond = System.currentTimeMillis() / 1000;
            LocalDateTime result = DateParser.fromEpochSecond(epochSecond);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("fromEpochSecond() 指定时区")
        void testFromEpochSecondWithZone() {
            long epochSecond = 1705312245L;
            ZoneId tokyo = ZoneId.of("Asia/Tokyo");
            LocalDateTime result = DateParser.fromEpochSecond(epochSecond, tokyo);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("tryParse() 尝试解析测试")
    class TryParseTests {

        @Test
        @DisplayName("tryParseDateTime() 成功时返回结果")
        void testTryParseDateTimeSuccess() {
            LocalDateTime result = DateParser.tryParseDateTime("2024-01-15 10:30:45");

            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2024);
        }

        @Test
        @DisplayName("tryParseDateTime() 失败时返回null")
        void testTryParseDateTimeFailure() {
            LocalDateTime result = DateParser.tryParseDateTime("invalid");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("tryParseDate() 成功时返回结果")
        void testTryParseDateSuccess() {
            LocalDate result = DateParser.tryParseDate("2024-01-15");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("tryParseDate() 失败时返回null")
        void testTryParseDateFailure() {
            LocalDate result = DateParser.tryParseDate("invalid");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("tryParseTime() 成功时返回结果")
        void testTryParseTimeSuccess() {
            LocalTime result = DateParser.tryParseTime("10:30:45");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("tryParseTime() 失败时返回null")
        void testTryParseTimeFailure() {
            LocalTime result = DateParser.tryParseTime("invalid");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("isValidDateTime() 有效日期时间返回true")
        void testIsValidDateTimeTrue() {
            assertThat(DateParser.isValidDateTime("2024-01-15 10:30:45")).isTrue();
        }

        @Test
        @DisplayName("isValidDateTime() 无效日期时间返回false")
        void testIsValidDateTimeFalse() {
            assertThat(DateParser.isValidDateTime("invalid")).isFalse();
        }

        @Test
        @DisplayName("isValidDate() 有效日期返回true")
        void testIsValidDateTrue() {
            assertThat(DateParser.isValidDate("2024-01-15")).isTrue();
        }

        @Test
        @DisplayName("isValidDate() 无效日期返回false")
        void testIsValidDateFalse() {
            assertThat(DateParser.isValidDate("invalid")).isFalse();
        }

        @Test
        @DisplayName("isValidTime() 有效时间返回true")
        void testIsValidTimeTrue() {
            assertThat(DateParser.isValidTime("10:30:45")).isTrue();
        }

        @Test
        @DisplayName("isValidTime() 无效时间返回false")
        void testIsValidTimeFalse() {
            assertThat(DateParser.isValidTime("invalid")).isFalse();
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("处理带空格的输入")
        void testParseWithWhitespace() {
            LocalDateTime result = DateParser.parseDateTime("  2024-01-15 10:30:45  ");

            assertThat(result.getYear()).isEqualTo(2024);
        }

        @Test
        @DisplayName("处理午夜时间")
        void testParseMidnight() {
            LocalTime result = DateParser.parseTime("00:00:00");

            assertThat(result).isEqualTo(LocalTime.MIDNIGHT);
        }

        @Test
        @DisplayName("处理最后一秒")
        void testParseEndOfDay() {
            LocalTime result = DateParser.parseTime("23:59:59");

            assertThat(result).isEqualTo(LocalTime.of(23, 59, 59));
        }

        @Test
        @DisplayName("处理闰年2月29日")
        void testParseLeapYearFeb29() {
            LocalDate result = DateParser.parseDate("2024-02-29");

            assertThat(result).isEqualTo(LocalDate.of(2024, 2, 29));
        }
    }
}
