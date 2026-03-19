package cloud.opencode.base.date.cron;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * CronUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("CronUtil 测试")
class CronUtilTest {

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("isValid() 有效表达式")
        void testIsValidTrue() {
            assertThat(CronUtil.isValid("* * * * *")).isTrue();
            assertThat(CronUtil.isValid("0 0 * * *")).isTrue();
            assertThat(CronUtil.isValid("30 8 1 6 1")).isTrue();
        }

        @Test
        @DisplayName("isValid() 无效表达式")
        void testIsValidFalse() {
            assertThat(CronUtil.isValid(null)).isFalse();
            assertThat(CronUtil.isValid("")).isFalse();
            assertThat(CronUtil.isValid("   ")).isFalse();
            assertThat(CronUtil.isValid("invalid")).isFalse();
            assertThat(CronUtil.isValid("* * * *")).isFalse(); // 4 fields
        }

        @Test
        @DisplayName("validate() 有效表达式不抛异常")
        void testValidateValid() {
            assertThatCode(() -> CronUtil.validate("* * * * *")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validate() null抛出异常")
        void testValidateNull() {
            assertThatThrownBy(() -> CronUtil.validate(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("validate() 无效表达式抛出异常")
        void testValidateInvalid() {
            assertThatThrownBy(() -> CronUtil.validate("invalid"))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("执行时间计算测试")
    class ExecutionTimeTests {

        @Test
        @DisplayName("getNextExecutionTime() 基本用法")
        void testGetNextExecutionTime() {
            Optional<LocalDateTime> next = CronUtil.getNextExecutionTime("* * * * *");
            assertThat(next).isPresent();
            assertThat(next.get()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("getNextExecutionTime() 指定时间")
        void testGetNextExecutionTimeAfter() {
            LocalDateTime after = LocalDateTime.of(2024, 6, 15, 10, 30);
            Optional<LocalDateTime> next = CronUtil.getNextExecutionTime("0 * * * *", after);
            assertThat(next).contains(LocalDateTime.of(2024, 6, 15, 11, 0));
        }

        @Test
        @DisplayName("getNextExecutionTime() 无效表达式返回空")
        void testGetNextExecutionTimeInvalid() {
            Optional<LocalDateTime> next = CronUtil.getNextExecutionTime("invalid");
            assertThat(next).isEmpty();
        }

        @Test
        @DisplayName("getNextExecutionTime() null参数抛出异常")
        void testGetNextExecutionTimeNull() {
            assertThatThrownBy(() -> CronUtil.getNextExecutionTime(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CronUtil.getNextExecutionTime("* * * * *", (LocalDateTime) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getNextExecutionTimes() 获取多次执行时间")
        void testGetNextExecutionTimes() {
            // Use a time that is clearly before a cron boundary
            LocalDateTime after = LocalDateTime.of(2024, 6, 15, 10, 30);
            List<LocalDateTime> times = CronUtil.getNextExecutionTimes("0 * * * *", after, 3);

            assertThat(times).hasSize(3);
            assertThat(times.get(0)).isEqualTo(LocalDateTime.of(2024, 6, 15, 11, 0));
            assertThat(times.get(1)).isEqualTo(LocalDateTime.of(2024, 6, 15, 12, 0));
            assertThat(times.get(2)).isEqualTo(LocalDateTime.of(2024, 6, 15, 13, 0));
        }

        @Test
        @DisplayName("getNextExecutionTimes() count为0返回空列表")
        void testGetNextExecutionTimesZero() {
            List<LocalDateTime> times = CronUtil.getNextExecutionTimes("* * * * *", LocalDateTime.now(), 0);
            assertThat(times).isEmpty();
        }

        @Test
        @DisplayName("getNextExecutionTimes() count为负返回空列表")
        void testGetNextExecutionTimesNegative() {
            List<LocalDateTime> times = CronUtil.getNextExecutionTimes("* * * * *", LocalDateTime.now(), -1);
            assertThat(times).isEmpty();
        }

        @Test
        @DisplayName("getNextExecutionTimes() null参数抛出异常")
        void testGetNextExecutionTimesNull() {
            assertThatThrownBy(() -> CronUtil.getNextExecutionTimes(null, LocalDateTime.now(), 5))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CronUtil.getNextExecutionTimes("* * * * *", (LocalDateTime) null, 5))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("时区方法测试")
    class TimezoneTests {

        @Test
        @DisplayName("getNextExecutionTime() 指定时区")
        void testGetNextExecutionTimeWithZone() {
            ZoneId zone = ZoneId.of("Asia/Shanghai");
            Optional<ZonedDateTime> next = CronUtil.getNextExecutionTime("* * * * *", zone);
            assertThat(next).isPresent();
            assertThat(next.get().getZone()).isEqualTo(zone);
        }

        @Test
        @DisplayName("getNextExecutionTime() 使用ZonedDateTime")
        void testGetNextExecutionTimeWithZonedDateTime() {
            ZonedDateTime after = ZonedDateTime.of(2024, 6, 15, 10, 0, 0, 0, ZoneId.of("UTC"));
            Optional<ZonedDateTime> next = CronUtil.getNextExecutionTime("0 * * * *", after);
            assertThat(next).isPresent();
            assertThat(next.get().getZone()).isEqualTo(ZoneId.of("UTC"));
        }

        @Test
        @DisplayName("getNextExecutionTime() ZonedDateTime null抛出异常")
        void testGetNextExecutionTimeZonedNull() {
            assertThatThrownBy(() -> CronUtil.getNextExecutionTime("* * * * *", (ZonedDateTime) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("描述方法测试")
    class DescribeTests {

        @Test
        @DisplayName("describe() 常见表达式英文描述")
        void testDescribe() {
            assertThat(CronUtil.describe(CronUtil.EVERY_SECOND)).isEqualTo("Every second");
            assertThat(CronUtil.describe(CronUtil.EVERY_MINUTE)).isEqualTo("Every minute");
            assertThat(CronUtil.describe(CronUtil.EVERY_HOUR)).isEqualTo("Every hour");
            assertThat(CronUtil.describe(CronUtil.DAILY_MIDNIGHT)).isEqualTo("Every day at midnight");
            assertThat(CronUtil.describe(CronUtil.DAILY_NOON)).isEqualTo("Every day at noon");
        }

        @Test
        @DisplayName("describe() 未知表达式")
        void testDescribeUnknown() {
            String desc = CronUtil.describe("15 10 * * *");
            assertThat(desc).contains("Cron:");
        }

        @Test
        @DisplayName("describe() null抛出异常")
        void testDescribeNull() {
            assertThatThrownBy(() -> CronUtil.describe(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("describeInChinese() 中文描述")
        void testDescribeInChinese() {
            assertThat(CronUtil.describeInChinese(CronUtil.EVERY_SECOND)).isEqualTo("每秒执行");
            assertThat(CronUtil.describeInChinese(CronUtil.EVERY_MINUTE)).isEqualTo("每分钟执行");
            assertThat(CronUtil.describeInChinese(CronUtil.DAILY_MIDNIGHT)).isEqualTo("每天午夜执行");
            assertThat(CronUtil.describeInChinese(CronUtil.WEEKDAYS_9AM)).isEqualTo("每个工作日上午9点执行");
        }

        @Test
        @DisplayName("describeInChinese() 未知表达式")
        void testDescribeInChineseUnknown() {
            String desc = CronUtil.describeInChinese("15 10 * * *");
            assertThat(desc).contains("Cron表达式:");
        }

        @Test
        @DisplayName("describeInChinese() null抛出异常")
        void testDescribeInChineseNull() {
            assertThatThrownBy(() -> CronUtil.describeInChinese(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("构建器方法测试")
    class BuilderTests {

        @Test
        @DisplayName("dailyAt() 每日特定时间")
        void testDailyAt() {
            String expr = CronUtil.dailyAt(8, 30);
            assertThat(expr).isEqualTo("0 30 8 * * ?");
        }

        @Test
        @DisplayName("dailyAt() 小时超出范围抛出异常")
        void testDailyAtHourOutOfRange() {
            assertThatThrownBy(() -> CronUtil.dailyAt(-1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronUtil.dailyAt(24, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("dailyAt() 分钟超出范围抛出异常")
        void testDailyAtMinuteOutOfRange() {
            assertThatThrownBy(() -> CronUtil.dailyAt(0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronUtil.dailyAt(0, 60))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("weekdaysAt() 工作日特定时间")
        void testWeekdaysAt() {
            String expr = CronUtil.weekdaysAt(9, 0);
            assertThat(expr).isEqualTo("0 0 9 ? * MON-FRI");
        }

        @Test
        @DisplayName("weekdaysAt() 参数验证")
        void testWeekdaysAtValidation() {
            assertThatThrownBy(() -> CronUtil.weekdaysAt(-1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronUtil.weekdaysAt(0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("everyMinutes() 每N分钟")
        void testEveryMinutes() {
            String expr = CronUtil.everyMinutes(15);
            assertThat(expr).isEqualTo("0 0/15 * * * ?");
        }

        @Test
        @DisplayName("everyMinutes() 参数超出范围抛出异常")
        void testEveryMinutesOutOfRange() {
            assertThatThrownBy(() -> CronUtil.everyMinutes(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronUtil.everyMinutes(60))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("everyHours() 每N小时")
        void testEveryHours() {
            String expr = CronUtil.everyHours(6);
            assertThat(expr).isEqualTo("0 0 0/6 * * ?");
        }

        @Test
        @DisplayName("everyHours() 参数超出范围抛出异常")
        void testEveryHoursOutOfRange() {
            assertThatThrownBy(() -> CronUtil.everyHours(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronUtil.everyHours(24))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("预设常量值正确")
        void testConstants() {
            assertThat(CronUtil.EVERY_SECOND).isEqualTo("* * * * * ?");
            assertThat(CronUtil.EVERY_MINUTE).isEqualTo("0 * * * * ?");
            assertThat(CronUtil.EVERY_HOUR).isEqualTo("0 0 * * * ?");
            assertThat(CronUtil.DAILY_MIDNIGHT).isEqualTo("0 0 0 * * ?");
            assertThat(CronUtil.DAILY_NOON).isEqualTo("0 0 12 * * ?");
            assertThat(CronUtil.WEEKLY_MONDAY).isEqualTo("0 0 0 ? * MON");
            assertThat(CronUtil.MONTHLY_FIRST).isEqualTo("0 0 0 1 * ?");
            assertThat(CronUtil.WEEKDAYS_9AM).isEqualTo("0 0 9 ? * MON-FRI");
            assertThat(CronUtil.EVERY_5_MINUTES).isEqualTo("0 0/5 * * * ?");
            assertThat(CronUtil.EVERY_15_MINUTES).isEqualTo("0 0/15 * * * ?");
            assertThat(CronUtil.EVERY_30_MINUTES).isEqualTo("0 0/30 * * * ?");
        }
    }
}
