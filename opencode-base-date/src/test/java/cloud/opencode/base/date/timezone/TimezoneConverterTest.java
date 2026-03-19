package cloud.opencode.base.date.timezone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.*;

/**
 * TimezoneConverter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("TimezoneConverter 测试")
class TimezoneConverterTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("时区常量正确")
        void testConstants() {
            assertThat(TimezoneConverter.UTC).isEqualTo(ZoneOffset.UTC);
            assertThat(TimezoneConverter.CHINA).isEqualTo(ZoneId.of("Asia/Shanghai"));
            assertThat(TimezoneConverter.BEIJING).isEqualTo(ZoneId.of("Asia/Shanghai"));
            assertThat(TimezoneConverter.TOKYO).isEqualTo(ZoneId.of("Asia/Tokyo"));
            assertThat(TimezoneConverter.NEW_YORK).isEqualTo(ZoneId.of("America/New_York"));
            assertThat(TimezoneConverter.LOS_ANGELES).isEqualTo(ZoneId.of("America/Los_Angeles"));
            assertThat(TimezoneConverter.LONDON).isEqualTo(ZoneId.of("Europe/London"));
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("from() 创建转换器")
        void testFrom() {
            TimezoneConverter converter = TimezoneConverter.from(TimezoneConverter.CHINA);
            assertThat(converter).isNotNull();
        }

        @Test
        @DisplayName("from() null抛出异常")
        void testFromNull() {
            assertThatThrownBy(() -> TimezoneConverter.from(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fromUTC() 创建UTC转换器")
        void testFromUTC() {
            TimezoneConverter converter = TimezoneConverter.fromUTC();
            assertThat(converter).isNotNull();
        }

        @Test
        @DisplayName("fromChina() 创建中国时区转换器")
        void testFromChina() {
            TimezoneConverter converter = TimezoneConverter.fromChina();
            assertThat(converter).isNotNull();
        }

        @Test
        @DisplayName("fromSystem() 创建系统默认时区转换器")
        void testFromSystem() {
            TimezoneConverter converter = TimezoneConverter.fromSystem();
            assertThat(converter).isNotNull();
        }
    }

    @Nested
    @DisplayName("目标时区方法测试")
    class TargetZoneTests {

        @Test
        @DisplayName("to() 设置目标时区")
        void testTo() {
            TimezoneConverter converter = TimezoneConverter.fromUTC().to(TimezoneConverter.CHINA);
            assertThat(converter).isNotNull();
        }

        @Test
        @DisplayName("to() null抛出异常")
        void testToNull() {
            assertThatThrownBy(() -> TimezoneConverter.fromUTC().to(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("toUTC() 设置目标时区为UTC")
        void testToUTC() {
            TimezoneConverter converter = TimezoneConverter.fromChina().toUTC();
            assertThat(converter).isNotNull();
        }

        @Test
        @DisplayName("toChina() 设置目标时区为中国")
        void testToChina() {
            TimezoneConverter converter = TimezoneConverter.fromUTC().toChina();
            assertThat(converter).isNotNull();
        }

        @Test
        @DisplayName("toSystem() 设置目标时区为系统默认")
        void testToSystem() {
            TimezoneConverter converter = TimezoneConverter.fromUTC().toSystem();
            assertThat(converter).isNotNull();
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("convert(LocalDateTime) 转换LocalDateTime")
        void testConvertLocalDateTime() {
            LocalDateTime beijing = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
            ZonedDateTime utc = TimezoneConverter.fromChina().toUTC().convert(beijing);

            // UTC is 8 hours behind Beijing
            assertThat(utc.getHour()).isEqualTo(4);
            assertThat(utc.getZone()).isEqualTo(ZoneOffset.UTC);
        }

        @Test
        @DisplayName("convert(LocalDateTime) null抛出异常")
        void testConvertLocalDateTimeNull() {
            assertThatThrownBy(() ->
                    TimezoneConverter.fromChina().toUTC().convert((LocalDateTime) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("convert(LocalDateTime) 未设置目标时区抛出异常")
        void testConvertNoTargetZone() {
            assertThatThrownBy(() ->
                    TimezoneConverter.fromChina().convert(LocalDateTime.now()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target timezone not set");
        }

        @Test
        @DisplayName("convert(Instant) 转换Instant")
        void testConvertInstant() {
            Instant instant = Instant.parse("2024-06-15T04:00:00Z");
            ZonedDateTime beijing = TimezoneConverter.fromUTC().toChina().convert(instant);

            // Beijing is 8 hours ahead of UTC
            assertThat(beijing.getHour()).isEqualTo(12);
        }

        @Test
        @DisplayName("convert(Instant) null抛出异常")
        void testConvertInstantNull() {
            assertThatThrownBy(() ->
                    TimezoneConverter.fromUTC().toChina().convert((Instant) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("convert(ZonedDateTime) 转换ZonedDateTime")
        void testConvertZonedDateTime() {
            ZonedDateTime tokyo = ZonedDateTime.of(2024, 6, 15, 21, 0, 0, 0, TimezoneConverter.TOKYO);
            ZonedDateTime newYork = TimezoneConverter.from(TimezoneConverter.TOKYO)
                    .to(TimezoneConverter.NEW_YORK)
                    .convert(tokyo);

            // Tokyo is ahead of New York
            assertThat(newYork.getZone()).isEqualTo(TimezoneConverter.NEW_YORK);
        }

        @Test
        @DisplayName("convert(ZonedDateTime) null抛出异常")
        void testConvertZonedDateTimeNull() {
            assertThatThrownBy(() ->
                    TimezoneConverter.fromUTC().toChina().convert((ZonedDateTime) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("convert(long) 转换毫秒时间戳")
        void testConvertEpochMilli() {
            // Use a reliable epoch milli from known Instant
            Instant instant = Instant.parse("2024-06-15T04:00:00Z");
            long epochMilli = instant.toEpochMilli();
            ZonedDateTime beijing = TimezoneConverter.fromUTC().toChina().convert(epochMilli);

            assertThat(beijing.getHour()).isEqualTo(12);
        }

        @Test
        @DisplayName("convertToLocal() 转换为LocalDateTime")
        void testConvertToLocal() {
            LocalDateTime beijing = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
            LocalDateTime utc = TimezoneConverter.fromChina().toUTC().convertToLocal(beijing);

            assertThat(utc.getHour()).isEqualTo(4);
        }

        @Test
        @DisplayName("convertToOffset() 转换为OffsetDateTime")
        void testConvertToOffset() {
            LocalDateTime beijing = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
            OffsetDateTime utc = TimezoneConverter.fromChina().toUTC().convertToOffset(beijing);

            assertThat(utc.getHour()).isEqualTo(4);
            assertThat(utc.getOffset()).isEqualTo(ZoneOffset.UTC);
        }
    }

    @Nested
    @DisplayName("静态便捷方法测试")
    class StaticConvenienceTests {

        @Test
        @DisplayName("convert() 静态方法转换")
        void testStaticConvert() {
            LocalDateTime beijing = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
            ZonedDateTime utc = TimezoneConverter.convert(beijing, TimezoneConverter.CHINA, TimezoneConverter.UTC);

            assertThat(utc.getHour()).isEqualTo(4);
        }

        @Test
        @DisplayName("fromUTC() 静态方法从UTC转换")
        void testStaticFromUTC() {
            LocalDateTime utc = LocalDateTime.of(2024, 6, 15, 4, 0, 0);
            ZonedDateTime beijing = TimezoneConverter.fromUTC(utc, TimezoneConverter.CHINA);

            assertThat(beijing.getHour()).isEqualTo(12);
        }

        @Test
        @DisplayName("toUTC() 静态方法转换为UTC")
        void testStaticToUTC() {
            LocalDateTime beijing = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
            ZonedDateTime utc = TimezoneConverter.toUTC(beijing, TimezoneConverter.CHINA);

            assertThat(utc.getHour()).isEqualTo(4);
        }

        @Test
        @DisplayName("now() 获取指定时区当前时间")
        void testNow() {
            ZonedDateTime now = TimezoneConverter.now(TimezoneConverter.CHINA);
            assertThat(now.getZone()).isEqualTo(TimezoneConverter.CHINA);
        }

        @Test
        @DisplayName("getOffsetHours() 获取时区偏移小时数")
        void testGetOffsetHours() {
            double offset = TimezoneConverter.getOffsetHours(TimezoneConverter.UTC, TimezoneConverter.CHINA);
            assertThat(offset).isEqualTo(8.0);
        }

        @Test
        @DisplayName("format() 格式化Instant")
        void testFormat() {
            Instant instant = Instant.parse("2024-06-15T04:00:00Z");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String result = TimezoneConverter.format(instant, TimezoneConverter.CHINA, formatter);

            assertThat(result).isEqualTo("2024-06-15 12:00:00");
        }
    }
}
