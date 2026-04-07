package cloud.opencode.base.cron;

import org.junit.jupiter.api.*;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * CronDescriberZhTest Tests
 * CronDescriberZhTest 中文描述测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.3
 */
@DisplayName("CronDescriberZh 中文描述测试")
class CronDescriberZhTest {

    private static String describeZh(String expr) {
        return CronExpression.parse(expr).describe(Locale.CHINESE);
    }

    // ==================== Basic Time Patterns ====================

    @Nested
    @DisplayName("基本时间模式测试")
    class BasicTimePatternTests {

        @Test
        @DisplayName("每分钟: * * * * *")
        void should_describe_every_minute() {
            String desc = describeZh("* * * * *");
            assertThat(desc).isEqualTo("每分钟");
        }

        @Test
        @DisplayName("每5分钟: */5 * * * *")
        void should_describe_every_5_minutes() {
            String desc = describeZh("*/5 * * * *");
            assertThat(desc).isEqualTo("每5分钟");
        }

        @Test
        @DisplayName("指定时间: 0 9 * * *")
        void should_describe_specific_time() {
            String desc = describeZh("0 9 * * *");
            assertThat(desc).isEqualTo("在09:00");
        }

        @Test
        @DisplayName("工作日指定时间: 0 9 * * MON-FRI")
        void should_describe_weekday_time() {
            String desc = describeZh("0 9 * * MON-FRI");
            assertThat(desc).isEqualTo("在09:00，周一到周五");
        }
    }

    // ==================== Second-Level Patterns ====================

    @Nested
    @DisplayName("秒级模式测试")
    class SecondLevelTests {

        @Test
        @DisplayName("每秒: * * * * * *")
        void should_describe_every_second() {
            String desc = describeZh("* * * * * *");
            assertThat(desc).isEqualTo("每秒");
        }

        @Test
        @DisplayName("每10秒: */10 * * * * *")
        void should_describe_every_10_seconds() {
            String desc = describeZh("*/10 * * * * *");
            assertThat(desc).isEqualTo("每10秒");
        }
    }

    // ==================== Special Characters ====================

    @Nested
    @DisplayName("特殊字符描述测试")
    class SpecialCharTests {

        @Test
        @DisplayName("月最后一天: 0 0 L * *")
        void should_describe_last_day_of_month() {
            String desc = describeZh("0 0 L * *");
            assertThat(desc).contains("每月最后一天");
        }

        @Test
        @DisplayName("月最后一个工作日: 0 0 LW * *")
        void should_describe_last_weekday_of_month() {
            String desc = describeZh("0 0 LW * *");
            assertThat(desc).contains("每月最后一个工作日");
        }

        @Test
        @DisplayName("第N个星期几: 0 0 * * 5#3")
        void should_describe_nth_day_of_week() {
            String desc = describeZh("0 0 * * 5#3");
            assertThat(desc).contains("每月第3个周五");
        }

        @Test
        @DisplayName("最近工作日: 0 0 15W * *")
        void should_describe_nearest_weekday() {
            String desc = describeZh("0 0 15W * *");
            assertThat(desc).contains("工作日");
        }

        @Test
        @DisplayName("周末: 0 0 * * 0,6")
        void should_describe_weekends() {
            String desc = describeZh("0 0 * * 0,6");
            assertThat(desc).contains("周末");
        }
    }

    // ==================== Month Descriptions ====================

    @Nested
    @DisplayName("月份描述测试")
    class MonthTests {

        @Test
        @DisplayName("特定月份: 0 0 1 6 *")
        void should_describe_specific_month() {
            String desc = describeZh("0 0 1 6 *");
            assertThat(desc).contains("六月");
        }
    }
}
