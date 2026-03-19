package cloud.opencode.base.core.convert.impl;

import cloud.opencode.base.core.convert.Converter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.*;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * DateConverter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("DateConverter 测试")
class DateConverterTest {

    @Nested
    @DisplayName("LocalDate 转换测试")
    class LocalDateConversionTests {

        @Test
        @DisplayName("字符串转 LocalDate ISO 格式")
        void testStringToLocalDateISO() {
            Converter<LocalDate> converter = DateConverter.localDateConverter();

            assertThat(converter.convert("2024-01-15")).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(converter.convert("2024-12-31")).isEqualTo(LocalDate.of(2024, 12, 31));
        }

        @Test
        @DisplayName("字符串转 LocalDate 其他格式")
        void testStringToLocalDateOtherFormats() {
            Converter<LocalDate> converter = DateConverter.localDateConverter();

            assertThat(converter.convert("2024/01/15")).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(converter.convert("20240115")).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(converter.convert("2024.01.15")).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("时间戳转 LocalDate")
        void testTimestampToLocalDate() {
            Converter<LocalDate> converter = DateConverter.localDateConverter();

            // 2024-01-15 00:00:00 UTC
            long millis = 1705276800000L;
            LocalDate result = converter.convert(millis);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Date 转 LocalDate")
        void testDateToLocalDate() {
            Converter<LocalDate> converter = DateConverter.localDateConverter();

            Date date = new Date();
            LocalDate result = converter.convert(date);
            assertThat(result).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("LocalDateTime 转 LocalDate")
        void testLocalDateTimeToLocalDate() {
            Converter<LocalDate> converter = DateConverter.localDateConverter();

            LocalDateTime ldt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            assertThat(converter.convert(ldt)).isEqualTo(LocalDate.of(2024, 1, 15));
        }
    }

    @Nested
    @DisplayName("LocalDateTime 转换测试")
    class LocalDateTimeConversionTests {

        @Test
        @DisplayName("字符串转 LocalDateTime ISO 格式")
        void testStringToLocalDateTimeISO() {
            Converter<LocalDateTime> converter = DateConverter.localDateTimeConverter();

            LocalDateTime result = converter.convert("2024-01-15T10:30:00");
            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.of(10, 30, 0));
        }

        @Test
        @DisplayName("字符串转 LocalDateTime 常见格式")
        void testStringToLocalDateTimeCommonFormats() {
            Converter<LocalDateTime> converter = DateConverter.localDateTimeConverter();

            LocalDateTime result = converter.convert("2024-01-15 10:30:00");
            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.of(10, 30, 0));

            result = converter.convert("2024/01/15 10:30:00");
            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("时间戳转 LocalDateTime")
        void testTimestampToLocalDateTime() {
            Converter<LocalDateTime> converter = DateConverter.localDateTimeConverter();

            long millis = System.currentTimeMillis();
            LocalDateTime result = converter.convert(millis);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Date 转 LocalDateTime")
        void testDateToLocalDateTime() {
            Converter<LocalDateTime> converter = DateConverter.localDateTimeConverter();

            Date date = new Date();
            LocalDateTime result = converter.convert(date);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("LocalTime 转换测试")
    class LocalTimeConversionTests {

        @Test
        @DisplayName("字符串转 LocalTime")
        void testStringToLocalTime() {
            Converter<LocalTime> converter = DateConverter.localTimeConverter();

            assertThat(converter.convert("10:30:00")).isEqualTo(LocalTime.of(10, 30, 0));
            assertThat(converter.convert("10:30")).isEqualTo(LocalTime.of(10, 30, 0));
            assertThat(converter.convert("103000")).isEqualTo(LocalTime.of(10, 30, 0));
        }

        @Test
        @DisplayName("LocalDateTime 转 LocalTime")
        void testLocalDateTimeToLocalTime() {
            Converter<LocalTime> converter = DateConverter.localTimeConverter();

            LocalDateTime ldt = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
            assertThat(converter.convert(ldt)).isEqualTo(LocalTime.of(10, 30, 45));
        }
    }

    @Nested
    @DisplayName("Instant 转换测试")
    class InstantConversionTests {

        @Test
        @DisplayName("时间戳转 Instant")
        void testTimestampToInstant() {
            Converter<Instant> converter = DateConverter.instantConverter();

            long millis = 1705276800000L;
            Instant result = converter.convert(millis);
            assertThat(result.toEpochMilli()).isEqualTo(millis);
        }

        @Test
        @DisplayName("字符串时间戳转 Instant")
        void testStringTimestampToInstant() {
            Converter<Instant> converter = DateConverter.instantConverter();

            // 毫秒时间戳
            Instant result = converter.convert("1705276800000");
            assertThat(result.toEpochMilli()).isEqualTo(1705276800000L);

            // 秒时间戳
            result = converter.convert("1705276800");
            assertThat(result.getEpochSecond()).isEqualTo(1705276800L);
        }

        @Test
        @DisplayName("Date 转 Instant")
        void testDateToInstant() {
            Converter<Instant> converter = DateConverter.instantConverter();

            Date date = new Date();
            Instant result = converter.convert(date);
            assertThat(result).isEqualTo(date.toInstant());
        }

        @Test
        @DisplayName("Instant 字符串格式")
        void testInstantStringFormat() {
            Converter<Instant> converter = DateConverter.instantConverter();

            Instant result = converter.convert("2024-01-15T00:00:00Z");
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("ZonedDateTime 转换测试")
    class ZonedDateTimeConversionTests {

        @Test
        @DisplayName("Date 转 ZonedDateTime")
        void testDateToZonedDateTime() {
            Converter<ZonedDateTime> converter = DateConverter.zonedDateTimeConverter();

            Date date = new Date();
            ZonedDateTime result = converter.convert(date);
            assertThat(result).isNotNull();
            assertThat(result.toInstant()).isEqualTo(date.toInstant());
        }

        @Test
        @DisplayName("时间戳转 ZonedDateTime")
        void testTimestampToZonedDateTime() {
            Converter<ZonedDateTime> converter = DateConverter.zonedDateTimeConverter();

            long millis = System.currentTimeMillis();
            ZonedDateTime result = converter.convert(millis);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("LocalDateTime 转 ZonedDateTime")
        void testLocalDateTimeToZonedDateTime() {
            Converter<ZonedDateTime> converter = DateConverter.zonedDateTimeConverter();

            LocalDateTime ldt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            ZonedDateTime result = converter.convert(ldt);
            assertThat(result.toLocalDateTime()).isEqualTo(ldt);
        }
    }

    @Nested
    @DisplayName("OffsetDateTime 转换测试")
    class OffsetDateTimeConversionTests {

        @Test
        @DisplayName("Date 转 OffsetDateTime")
        void testDateToOffsetDateTime() {
            Converter<OffsetDateTime> converter = DateConverter.offsetDateTimeConverter();

            Date date = new Date();
            OffsetDateTime result = converter.convert(date);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("时间戳转 OffsetDateTime")
        void testTimestampToOffsetDateTime() {
            Converter<OffsetDateTime> converter = DateConverter.offsetDateTimeConverter();

            long millis = System.currentTimeMillis();
            OffsetDateTime result = converter.convert(millis);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("旧版日期类型转换测试")
    class LegacyDateConversionTests {

        @Test
        @DisplayName("字符串转 Date")
        void testStringToDate() {
            Converter<Date> converter = DateConverter.dateConverter();

            Date result = converter.convert("2024-01-15");
            assertThat(result).isNotNull();

            result = converter.convert("2024-01-15 10:30:00");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("时间戳转 Date")
        void testTimestampToDate() {
            Converter<Date> converter = DateConverter.dateConverter();

            long millis = System.currentTimeMillis();
            Date result = converter.convert(millis);
            assertThat(result.getTime()).isEqualTo(millis);
        }

        @Test
        @DisplayName("LocalDateTime 转 Date")
        void testLocalDateTimeToDate() {
            Converter<Date> converter = DateConverter.dateConverter();

            LocalDateTime ldt = LocalDateTime.now();
            Date result = converter.convert(ldt);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("字符串转 java.sql.Date")
        void testStringToSqlDate() {
            Converter<java.sql.Date> converter = DateConverter.sqlDateConverter();

            java.sql.Date result = converter.convert("2024-01-15");
            assertThat(result).isNotNull();
            assertThat(result.toString()).isEqualTo("2024-01-15");
        }

        @Test
        @DisplayName("字符串转 java.sql.Time")
        void testStringToSqlTime() {
            Converter<java.sql.Time> converter = DateConverter.sqlTimeConverter();

            java.sql.Time result = converter.convert("10:30:00");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("字符串转 Timestamp")
        void testStringToTimestamp() {
            Converter<Timestamp> converter = DateConverter.timestampConverter();

            Timestamp result = converter.convert("2024-01-15 10:30:00");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("时间戳转 Timestamp")
        void testMillisToTimestamp() {
            Converter<Timestamp> converter = DateConverter.timestampConverter();

            long millis = System.currentTimeMillis();
            Timestamp result = converter.convert(millis);
            assertThat(result.getTime()).isEqualTo(millis);
        }

        @Test
        @DisplayName("字符串转 Calendar")
        void testStringToCalendar() {
            Converter<Calendar> converter = DateConverter.calendarConverter();

            Calendar result = converter.convert("2024-01-15");
            assertThat(result).isNotNull();
            assertThat(result.get(Calendar.YEAR)).isEqualTo(2024);
            assertThat(result.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
            assertThat(result.get(Calendar.DAY_OF_MONTH)).isEqualTo(15);
        }

        @Test
        @DisplayName("时间戳转 Calendar")
        void testMillisToCalendar() {
            Converter<Calendar> converter = DateConverter.calendarConverter();

            long millis = System.currentTimeMillis();
            Calendar result = converter.convert(millis);
            assertThat(result.getTimeInMillis()).isEqualTo(millis);
        }
    }

    @Nested
    @DisplayName("null 和无效值处理测试")
    class NullAndInvalidHandlingTests {

        @Test
        @DisplayName("null 返回默认值")
        void testNullReturnsDefault() {
            LocalDate defaultDate = LocalDate.of(2000, 1, 1);
            assertThat(DateConverter.localDateConverter().convert(null, defaultDate)).isEqualTo(defaultDate);
        }

        @Test
        @DisplayName("null 无默认值返回 null")
        void testNullWithoutDefaultReturnsNull() {
            assertThat(DateConverter.localDateConverter().convert(null)).isNull();
            assertThat(DateConverter.localDateTimeConverter().convert(null)).isNull();
            assertThat(DateConverter.dateConverter().convert(null)).isNull();
        }

        @Test
        @DisplayName("空字符串返回默认值")
        void testEmptyStringReturnsDefault() {
            LocalDate defaultDate = LocalDate.of(2000, 1, 1);
            assertThat(DateConverter.localDateConverter().convert("", defaultDate)).isEqualTo(defaultDate);
            assertThat(DateConverter.localDateConverter().convert("   ", defaultDate)).isEqualTo(defaultDate);
        }

        @Test
        @DisplayName("无效字符串返回默认值")
        void testInvalidStringReturnsDefault() {
            LocalDate defaultDate = LocalDate.of(2000, 1, 1);
            assertThat(DateConverter.localDateConverter().convert("not a date", defaultDate)).isEqualTo(defaultDate);
            assertThat(DateConverter.localDateConverter().convert("2024-99-99", defaultDate)).isEqualTo(defaultDate);
        }
    }

    @Nested
    @DisplayName("类型保持测试")
    class TypePreservationTests {

        @Test
        @DisplayName("已是目标类型直接返回")
        void testSameTypeReturned() {
            LocalDate localDate = LocalDate.now();
            assertThat(DateConverter.localDateConverter().convert(localDate)).isSameAs(localDate);

            LocalDateTime localDateTime = LocalDateTime.now();
            assertThat(DateConverter.localDateTimeConverter().convert(localDateTime)).isSameAs(localDateTime);

            Instant instant = Instant.now();
            assertThat(DateConverter.instantConverter().convert(instant)).isSameAs(instant);

            Date date = new Date();
            assertThat(DateConverter.dateConverter().convert(date)).isSameAs(date);
        }
    }

    @Nested
    @DisplayName("带毫秒格式测试")
    class MillisecondsFormatTests {

        @Test
        @DisplayName("带毫秒的日期时间字符串")
        void testDateTimeWithMilliseconds() {
            Converter<LocalDateTime> converter = DateConverter.localDateTimeConverter();

            LocalDateTime result = converter.convert("2024-01-15 10:30:00.123");
            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 1, 15));

            result = converter.convert("2024-01-15T10:30:00.123");
            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        }
    }
}
