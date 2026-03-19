package cloud.opencode.base.date.timezone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * TimezoneUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("TimezoneUtil 测试")
class TimezoneUtilTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("时区常量正确")
        void testConstants() {
            assertThat(TimezoneUtil.UTC).isEqualTo(ZoneId.of("UTC"));
            assertThat(TimezoneUtil.CHINA).isEqualTo(ZoneId.of("Asia/Shanghai"));
            assertThat(TimezoneUtil.JAPAN).isEqualTo(ZoneId.of("Asia/Tokyo"));
            assertThat(TimezoneUtil.KOREA).isEqualTo(ZoneId.of("Asia/Seoul"));
            assertThat(TimezoneUtil.NEW_YORK).isEqualTo(ZoneId.of("America/New_York"));
            assertThat(TimezoneUtil.LOS_ANGELES).isEqualTo(ZoneId.of("America/Los_Angeles"));
            assertThat(TimezoneUtil.LONDON).isEqualTo(ZoneId.of("Europe/London"));
            assertThat(TimezoneUtil.PARIS).isEqualTo(ZoneId.of("Europe/Paris"));
            assertThat(TimezoneUtil.BERLIN).isEqualTo(ZoneId.of("Europe/Berlin"));
            assertThat(TimezoneUtil.INDIA).isEqualTo(ZoneId.of("Asia/Kolkata"));
            assertThat(TimezoneUtil.SINGAPORE).isEqualTo(ZoneId.of("Asia/Singapore"));
            assertThat(TimezoneUtil.HONG_KONG).isEqualTo(ZoneId.of("Asia/Hong_Kong"));
            assertThat(TimezoneUtil.SYDNEY).isEqualTo(ZoneId.of("Australia/Sydney"));
        }
    }

    @Nested
    @DisplayName("当前时间方法测试")
    class NowMethodTests {

        @Test
        @DisplayName("nowUtc() 获取UTC当前时间")
        void testNowUtc() {
            ZonedDateTime utc = TimezoneUtil.nowUtc();
            assertThat(utc.getZone()).isEqualTo(TimezoneUtil.UTC);
        }

        @Test
        @DisplayName("now() 获取指定时区当前时间")
        void testNow() {
            ZonedDateTime beijing = TimezoneUtil.now(TimezoneUtil.CHINA);
            assertThat(beijing.getZone()).isEqualTo(TimezoneUtil.CHINA);
        }

        @Test
        @DisplayName("nowLocal() 获取系统默认时区当前时间")
        void testNowLocal() {
            ZonedDateTime local = TimezoneUtil.nowLocal();
            assertThat(local.getZone()).isEqualTo(ZoneId.systemDefault());
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionMethodTests {

        @Test
        @DisplayName("convert(ZonedDateTime, ZoneId) 转换ZonedDateTime")
        void testConvertZonedDateTime() {
            ZonedDateTime beijing = ZonedDateTime.of(2024, 6, 15, 12, 0, 0, 0, TimezoneUtil.CHINA);
            ZonedDateTime utc = TimezoneUtil.convert(beijing, TimezoneUtil.UTC);

            assertThat(utc.getHour()).isEqualTo(4);
            assertThat(utc.getZone()).isEqualTo(TimezoneUtil.UTC);
        }

        @Test
        @DisplayName("toZoned(LocalDateTime, ZoneId) 转换LocalDateTime")
        void testToZonedFromLocal() {
            LocalDateTime local = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
            ZonedDateTime zoned = TimezoneUtil.toZoned(local, TimezoneUtil.CHINA);

            assertThat(zoned.toLocalDateTime()).isEqualTo(local);
            assertThat(zoned.getZone()).isEqualTo(TimezoneUtil.CHINA);
        }

        @Test
        @DisplayName("toLocal(ZonedDateTime) 转换为LocalDateTime")
        void testToLocal() {
            ZonedDateTime zoned = ZonedDateTime.of(2024, 6, 15, 12, 0, 0, 0, TimezoneUtil.CHINA);
            LocalDateTime local = TimezoneUtil.toLocal(zoned);

            assertThat(local).isEqualTo(LocalDateTime.of(2024, 6, 15, 12, 0, 0));
        }

        @Test
        @DisplayName("toZoned(Instant, ZoneId) 转换Instant")
        void testToZonedFromInstant() {
            Instant instant = Instant.parse("2024-06-15T04:00:00Z");
            ZonedDateTime beijing = TimezoneUtil.toZoned(instant, TimezoneUtil.CHINA);

            assertThat(beijing.getHour()).isEqualTo(12);
        }

        @Test
        @DisplayName("toInstant(ZonedDateTime) 转换为Instant")
        void testToInstant() {
            ZonedDateTime zoned = ZonedDateTime.of(2024, 6, 15, 12, 0, 0, 0, TimezoneUtil.CHINA);
            Instant instant = TimezoneUtil.toInstant(zoned);

            assertThat(instant).isEqualTo(Instant.parse("2024-06-15T04:00:00Z"));
        }

        @Test
        @DisplayName("convert(LocalDateTime, fromZone, toZone) 在时区之间转换LocalDateTime")
        void testConvertLocalDateTime() {
            LocalDateTime beijing = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
            LocalDateTime utc = TimezoneUtil.convert(beijing, TimezoneUtil.CHINA, TimezoneUtil.UTC);

            assertThat(utc.getHour()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("偏移方法测试")
    class OffsetMethodTests {

        @Test
        @DisplayName("getOffset() 获取时区偏移")
        void testGetOffset() {
            Duration offset = TimezoneUtil.getOffset(TimezoneUtil.CHINA);
            assertThat(offset).isEqualTo(Duration.ofHours(8));
        }

        @Test
        @DisplayName("getOffsetBetween() 获取两个时区之间的偏移")
        void testGetOffsetBetween() {
            Duration offset = TimezoneUtil.getOffsetBetween(TimezoneUtil.UTC, TimezoneUtil.CHINA);
            assertThat(offset).isEqualTo(Duration.ofHours(8));
        }

        @Test
        @DisplayName("getOffsetHours() 获取小时偏移")
        void testGetOffsetHours() {
            int hours = TimezoneUtil.getOffsetHours(TimezoneUtil.CHINA);
            assertThat(hours).isEqualTo(8);
        }

        @Test
        @DisplayName("formatOffset() 格式化偏移")
        void testFormatOffset() {
            String offset = TimezoneUtil.formatOffset(TimezoneUtil.CHINA);
            assertThat(offset).isEqualTo("+08:00");
        }

        @Test
        @DisplayName("formatOffset() UTC偏移")
        void testFormatOffsetUtc() {
            String offset = TimezoneUtil.formatOffset(TimezoneUtil.UTC);
            assertThat(offset).isEqualTo("Z");
        }
    }

    @Nested
    @DisplayName("时区信息方法测试")
    class TimezoneInfoTests {

        @Test
        @DisplayName("getAllTimezoneIds() 获取所有时区ID")
        void testGetAllTimezoneIds() {
            Set<String> ids = TimezoneUtil.getAllTimezoneIds();
            assertThat(ids).isNotEmpty();
            assertThat(ids).contains("Asia/Shanghai", "America/New_York", "Europe/London");
        }

        @Test
        @DisplayName("getAllTimezones() 获取所有时区")
        void testGetAllTimezones() {
            List<ZoneId> zones = TimezoneUtil.getAllTimezones();
            assertThat(zones).isNotEmpty();
            assertThat(zones).contains(TimezoneUtil.CHINA, TimezoneUtil.NEW_YORK);
        }

        @Test
        @DisplayName("findTimezones() 查找时区")
        void testFindTimezones() {
            List<String> asiaZones = TimezoneUtil.findTimezones("asia");
            assertThat(asiaZones).isNotEmpty();
            assertThat(asiaZones).anyMatch(z -> z.contains("Shanghai"));
        }

        @Test
        @DisplayName("getDisplayName() 获取显示名称")
        void testGetDisplayName() {
            String name = TimezoneUtil.getDisplayName(TimezoneUtil.CHINA, Locale.ENGLISH);
            assertThat(name).isNotEmpty();
        }

        @Test
        @DisplayName("getShortDisplayName() 获取短显示名称")
        void testGetShortDisplayName() {
            String name = TimezoneUtil.getShortDisplayName(TimezoneUtil.CHINA, Locale.ENGLISH);
            assertThat(name).isNotEmpty();
        }

        @Test
        @DisplayName("isValidTimezone() 检查有效时区")
        void testIsValidTimezone() {
            assertThat(TimezoneUtil.isValidTimezone("Asia/Shanghai")).isTrue();
            assertThat(TimezoneUtil.isValidTimezone("UTC")).isTrue();
            assertThat(TimezoneUtil.isValidTimezone("Invalid/Timezone")).isFalse();
        }

        @Test
        @DisplayName("getDefault() 获取系统默认时区")
        void testGetDefault() {
            ZoneId defaultZone = TimezoneUtil.getDefault();
            assertThat(defaultZone).isEqualTo(ZoneId.systemDefault());
        }
    }

    @Nested
    @DisplayName("夏令时方法测试")
    class DstMethodTests {

        @Test
        @DisplayName("isDaylightSavingTime() 检查是否夏令时")
        void testIsDaylightSavingTime() {
            // China doesn't use DST, so it should always be false
            boolean china = TimezoneUtil.isDaylightSavingTime(TimezoneUtil.CHINA);
            assertThat(china).isFalse();
        }

        @Test
        @DisplayName("usesDaylightSavingTime() 检查是否使用夏令时")
        void testUsesDaylightSavingTime() {
            // New York uses DST
            assertThat(TimezoneUtil.usesDaylightSavingTime(TimezoneUtil.NEW_YORK)).isTrue();
            // UTC has no DST rules
            assertThat(TimezoneUtil.usesDaylightSavingTime(TimezoneUtil.UTC)).isFalse();
        }

        @Test
        @DisplayName("getNextDstTransition() 获取下一个夏令时转换")
        void testGetNextDstTransition() {
            // China doesn't have DST transitions
            ZonedDateTime chinaTransition = TimezoneUtil.getNextDstTransition(TimezoneUtil.CHINA);
            assertThat(chinaTransition).isNull();

            // New York should have DST transitions
            ZonedDateTime nyTransition = TimezoneUtil.getNextDstTransition(TimezoneUtil.NEW_YORK);
            // May or may not be null depending on current date
        }
    }
}
