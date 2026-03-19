package cloud.opencode.base.date.cron;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * CronExpression 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("CronExpression 测试")
class CronExpressionTest {

    @Nested
    @DisplayName("parse方法测试")
    class ParseTests {

        @Test
        @DisplayName("parse() 解析每分钟表达式")
        void testParseEveryMinute() {
            CronExpression cron = CronExpression.parse("* * * * *");
            assertThat(cron.getMinutes()).hasSize(60);
            assertThat(cron.getHours()).hasSize(24);
            assertThat(cron.getDaysOfMonth()).hasSize(31);
            assertThat(cron.getMonths()).hasSize(12);
            assertThat(cron.getDaysOfWeek()).hasSize(7);
        }

        @Test
        @DisplayName("parse() 解析特定时间表达式")
        void testParseSpecificTime() {
            CronExpression cron = CronExpression.parse("30 8 * * *");
            assertThat(cron.getMinutes()).containsExactly(30);
            assertThat(cron.getHours()).containsExactly(8);
        }

        @Test
        @DisplayName("parse() 解析列表表达式")
        void testParseList() {
            CronExpression cron = CronExpression.parse("0,15,30,45 * * * *");
            assertThat(cron.getMinutes()).containsExactly(0, 15, 30, 45);
        }

        @Test
        @DisplayName("parse() 解析范围表达式")
        void testParseRange() {
            CronExpression cron = CronExpression.parse("0 9-17 * * *");
            assertThat(cron.getHours()).containsExactly(9, 10, 11, 12, 13, 14, 15, 16, 17);
        }

        @Test
        @DisplayName("parse() 解析步进表达式")
        void testParseStep() {
            CronExpression cron = CronExpression.parse("*/15 * * * *");
            assertThat(cron.getMinutes()).containsExactly(0, 15, 30, 45);
        }

        @Test
        @DisplayName("parse() 解析范围步进表达式")
        void testParseRangeStep() {
            CronExpression cron = CronExpression.parse("0-30/10 * * * *");
            assertThat(cron.getMinutes()).containsExactly(0, 10, 20, 30);
        }

        @Test
        @DisplayName("parse() null抛出异常")
        void testParseNull() {
            assertThatThrownBy(() -> CronExpression.parse(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("parse() 格式错误抛出异常")
        void testParseInvalidFormat() {
            assertThatThrownBy(() -> CronExpression.parse("invalid"))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("parse() 字段不足抛出异常")
        void testParseTooFewFields() {
            assertThatThrownBy(() -> CronExpression.parse("* * * *"))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("parse() 值超出范围抛出异常")
        void testParseValueOutOfRange() {
            assertThatThrownBy(() -> CronExpression.parse("60 * * * *"))
                    .isInstanceOf(OpenDateException.class);
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("daily() 每日特定时间")
        void testDaily() {
            CronExpression cron = CronExpression.daily(8, 30);
            assertThat(cron.getMinutes()).containsExactly(30);
            assertThat(cron.getHours()).containsExactly(8);
        }

        @Test
        @DisplayName("everyMinutes() 每N分钟")
        void testEveryMinutes() {
            CronExpression cron = CronExpression.everyMinutes(15);
            assertThat(cron.getMinutes()).containsExactly(0, 15, 30, 45);
        }

        @Test
        @DisplayName("everyMinutes() 参数超出范围抛出异常")
        void testEveryMinutesOutOfRange() {
            assertThatThrownBy(() -> CronExpression.everyMinutes(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronExpression.everyMinutes(60))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("everyHours() 每N小时")
        void testEveryHours() {
            CronExpression cron = CronExpression.everyHours(6);
            assertThat(cron.getHours()).containsExactly(0, 6, 12, 18);
        }

        @Test
        @DisplayName("everyHours() 参数超出范围抛出异常")
        void testEveryHoursOutOfRange() {
            assertThatThrownBy(() -> CronExpression.everyHours(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronExpression.everyHours(24))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("matches方法测试")
    class MatchesTests {

        @Test
        @DisplayName("matches() 每分钟表达式匹配任意时间")
        void testMatchesEveryMinute() {
            CronExpression cron = CronExpression.parse("* * * * *");
            assertThat(cron.matches(LocalDateTime.of(2024, 6, 15, 10, 30))).isTrue();
        }

        @Test
        @DisplayName("matches() 特定时间匹配")
        void testMatchesSpecificTime() {
            CronExpression cron = CronExpression.parse("30 8 * * *");
            assertThat(cron.matches(LocalDateTime.of(2024, 6, 15, 8, 30))).isTrue();
            assertThat(cron.matches(LocalDateTime.of(2024, 6, 15, 8, 31))).isFalse();
            assertThat(cron.matches(LocalDateTime.of(2024, 6, 15, 9, 30))).isFalse();
        }

        @Test
        @DisplayName("matches() 特定日期匹配")
        void testMatchesSpecificDay() {
            CronExpression cron = CronExpression.parse("0 0 15 * *");
            assertThat(cron.matches(LocalDateTime.of(2024, 6, 15, 0, 0))).isTrue();
            assertThat(cron.matches(LocalDateTime.of(2024, 6, 14, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("matches() 特定星期几匹配")
        void testMatchesDayOfWeek() {
            // Monday = 1
            CronExpression cron = CronExpression.parse("0 0 * * 1");
            // 2024-06-17 is Monday
            assertThat(cron.matches(LocalDateTime.of(2024, 6, 17, 0, 0))).isTrue();
            // 2024-06-18 is Tuesday
            assertThat(cron.matches(LocalDateTime.of(2024, 6, 18, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("matches() 特定月份匹配")
        void testMatchesSpecificMonth() {
            CronExpression cron = CronExpression.parse("0 0 1 6 *");
            assertThat(cron.matches(LocalDateTime.of(2024, 6, 1, 0, 0))).isTrue();
            assertThat(cron.matches(LocalDateTime.of(2024, 7, 1, 0, 0))).isFalse();
        }
    }

    @Nested
    @DisplayName("nextExecution方法测试")
    class NextExecutionTests {

        @Test
        @DisplayName("nextExecution() 每分钟表达式")
        void testNextExecutionEveryMinute() {
            CronExpression cron = CronExpression.parse("* * * * *");
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            LocalDateTime next = cron.nextExecution(now);
            assertThat(next).isEqualTo(LocalDateTime.of(2024, 6, 15, 10, 31, 0));
        }

        @Test
        @DisplayName("nextExecution() 跨小时")
        void testNextExecutionCrossHour() {
            CronExpression cron = CronExpression.parse("30 * * * *");
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 45, 0);
            LocalDateTime next = cron.nextExecution(now);
            assertThat(next).isEqualTo(LocalDateTime.of(2024, 6, 15, 11, 30, 0));
        }

        @Test
        @DisplayName("nextExecution() 跨天")
        void testNextExecutionCrossDay() {
            CronExpression cron = CronExpression.parse("30 8 * * *");
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 0, 0);
            LocalDateTime next = cron.nextExecution(now);
            assertThat(next).isEqualTo(LocalDateTime.of(2024, 6, 16, 8, 30, 0));
        }

        @Test
        @DisplayName("nextExecution() 跨月")
        void testNextExecutionCrossMonth() {
            CronExpression cron = CronExpression.parse("0 0 1 * *");
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 0, 0, 0);
            LocalDateTime next = cron.nextExecution(now);
            assertThat(next).isEqualTo(LocalDateTime.of(2024, 7, 1, 0, 0, 0));
        }

        @Test
        @DisplayName("nextExecution() 无参数使用当前时间")
        void testNextExecutionNoArgs() {
            CronExpression cron = CronExpression.parse("* * * * *");
            LocalDateTime next = cron.nextExecution();
            assertThat(next).isAfter(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("previousExecution方法测试")
    class PreviousExecutionTests {

        @Test
        @DisplayName("previousExecution() 每分钟表达式")
        void testPreviousExecutionEveryMinute() {
            CronExpression cron = CronExpression.parse("* * * * *");
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            LocalDateTime prev = cron.previousExecution(now);
            assertThat(prev).isEqualTo(LocalDateTime.of(2024, 6, 15, 10, 29, 0));
        }

        @Test
        @DisplayName("previousExecution() 特定时间")
        void testPreviousExecutionSpecificTime() {
            CronExpression cron = CronExpression.parse("30 8 * * *");
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 0, 0);
            LocalDateTime prev = cron.previousExecution(now);
            assertThat(prev).isEqualTo(LocalDateTime.of(2024, 6, 15, 8, 30, 0));
        }

        @Test
        @DisplayName("previousExecution() 无参数使用当前时间")
        void testPreviousExecutionNoArgs() {
            CronExpression cron = CronExpression.parse("* * * * *");
            LocalDateTime prev = cron.previousExecution();
            assertThat(prev).isBefore(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("nextExecutions方法测试")
    class NextExecutionsTests {

        @Test
        @DisplayName("nextExecutions() 获取多次执行时间")
        void testNextExecutions() {
            CronExpression cron = CronExpression.parse("0 * * * *");
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            List<LocalDateTime> next5 = cron.nextExecutions(now, 5);

            assertThat(next5).hasSize(5);
            assertThat(next5.get(0)).isEqualTo(LocalDateTime.of(2024, 6, 15, 11, 0, 0));
            assertThat(next5.get(1)).isEqualTo(LocalDateTime.of(2024, 6, 15, 12, 0, 0));
            assertThat(next5.get(2)).isEqualTo(LocalDateTime.of(2024, 6, 15, 13, 0, 0));
        }

        @Test
        @DisplayName("nextExecutions() count为0返回空列表")
        void testNextExecutionsZeroCount() {
            CronExpression cron = CronExpression.parse("* * * * *");
            List<LocalDateTime> result = cron.nextExecutions(LocalDateTime.now(), 0);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("nextExecutions() 无参数使用当前时间")
        void testNextExecutionsNoArgs() {
            CronExpression cron = CronExpression.parse("* * * * *");
            List<LocalDateTime> result = cron.nextExecutions(3);
            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("预设表达式测试")
    class PresetExpressionTests {

        @Test
        @DisplayName("EVERY_MINUTE 常量")
        void testEveryMinuteConstant() {
            assertThat(CronExpression.EVERY_MINUTE).isEqualTo("* * * * *");
            CronExpression cron = CronExpression.parse(CronExpression.EVERY_MINUTE);
            assertThat(cron.getMinutes()).hasSize(60);
        }

        @Test
        @DisplayName("EVERY_HOUR 常量")
        void testEveryHourConstant() {
            assertThat(CronExpression.EVERY_HOUR).isEqualTo("0 * * * *");
            CronExpression cron = CronExpression.parse(CronExpression.EVERY_HOUR);
            assertThat(cron.getMinutes()).containsExactly(0);
        }

        @Test
        @DisplayName("DAILY 常量")
        void testDailyConstant() {
            assertThat(CronExpression.DAILY).isEqualTo("0 0 * * *");
        }

        @Test
        @DisplayName("WEEKLY 常量")
        void testWeeklyConstant() {
            assertThat(CronExpression.WEEKLY).isEqualTo("0 0 * * 1");
        }

        @Test
        @DisplayName("MONTHLY 常量")
        void testMonthlyConstant() {
            assertThat(CronExpression.MONTHLY).isEqualTo("0 0 1 * *");
        }

        @Test
        @DisplayName("YEARLY 常量")
        void testYearlyConstant() {
            assertThat(CronExpression.YEARLY).isEqualTo("0 0 1 1 *");
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("getExpression() 获取原始表达式")
        void testGetExpression() {
            String expr = "30 8 * * *";
            CronExpression cron = CronExpression.parse(expr);
            assertThat(cron.getExpression()).isEqualTo(expr);
        }

        @Test
        @DisplayName("getMinutes() 返回不可变集合")
        void testGetMinutesImmutable() {
            CronExpression cron = CronExpression.parse("30 * * * *");
            Set<Integer> minutes = cron.getMinutes();
            assertThatThrownBy(() -> minutes.add(0))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("describe方法测试")
    class DescribeTests {

        @Test
        @DisplayName("describe() 每分钟")
        void testDescribeEveryMinute() {
            CronExpression cron = CronExpression.parse("* * * * *");
            assertThat(cron.describe()).contains("Every minute");
        }

        @Test
        @DisplayName("describe() 特定分钟")
        void testDescribeSpecificMinute() {
            CronExpression cron = CronExpression.parse("30 * * * *");
            assertThat(cron.describe()).contains("At minute 30");
        }

        @Test
        @DisplayName("describe() 多个分钟")
        void testDescribeMultipleMinutes() {
            CronExpression cron = CronExpression.parse("0,15,30 * * * *");
            assertThat(cron.describe()).contains("At minutes");
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等判断")
        void testEquals() {
            CronExpression c1 = CronExpression.parse("30 8 * * *");
            CronExpression c2 = CronExpression.parse("30 8 * * *");
            CronExpression c3 = CronExpression.parse("0 8 * * *");

            assertThat(c1).isEqualTo(c2);
            assertThat(c1).isNotEqualTo(c3);
            assertThat(c1).isEqualTo(c1);
            assertThat(c1).isNotEqualTo(null);
            assertThat(c1).isNotEqualTo("30 8 * * *");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            CronExpression c1 = CronExpression.parse("30 8 * * *");
            CronExpression c2 = CronExpression.parse("30 8 * * *");
            assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        }

        @Test
        @DisplayName("toString() 返回表达式")
        void testToString() {
            String expr = "30 8 * * *";
            CronExpression cron = CronExpression.parse(expr);
            assertThat(cron.toString()).isEqualTo(expr);
        }
    }
}
