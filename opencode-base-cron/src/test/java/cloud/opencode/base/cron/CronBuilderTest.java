package cloud.opencode.base.cron;

import org.junit.jupiter.api.*;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.*;

/**
 * CronBuilderTest Tests
 * CronBuilderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.0
 */
@DisplayName("CronBuilder 测试")
class CronBuilderTest {

    @Nested
    @DisplayName("基本构建测试")
    class BasicBuildTests {

        @Test
        @DisplayName("构建每日指定时间")
        void should_build_daily_at() {
            String expr = CronBuilder.every().day().at(10, 30).buildExpression();
            assertThat(expr).isEqualTo("30 10 * * *");
        }

        @Test
        @DisplayName("构建工作日表达式")
        void should_build_weekdays() {
            String expr = CronBuilder.every().weekdays().at(9, 0).buildExpression();
            assertThat(expr).isEqualTo("0 9 * * 1-5");
        }

        @Test
        @DisplayName("构建周末表达式")
        void should_build_weekends() {
            String expr = CronBuilder.every().weekends().at(10, 0).buildExpression();
            assertThat(expr).isEqualTo("0 10 * * 0,6");
        }

        @Test
        @DisplayName("构建每N秒表达式")
        void should_build_every_seconds() {
            String expr = CronBuilder.everySeconds(5).buildExpression();
            assertThat(expr).isEqualTo("0/5 * * * * *");
        }

        @Test
        @DisplayName("构建每N分钟表达式")
        void should_build_every_minutes() {
            String expr = CronBuilder.everyMinutes(10).buildExpression();
            assertThat(expr).isEqualTo("*/10 * * * *");
        }

        @Test
        @DisplayName("构建每N小时表达式")
        void should_build_every_hours() {
            String expr = CronBuilder.everyHours(2).buildExpression();
            assertThat(expr).isEqualTo("0 */2 * * *");
        }

        @Test
        @DisplayName("构建指定星期几")
        void should_build_specific_day_of_week() {
            assertThat(CronBuilder.every().monday().buildExpression()).contains("1");
            assertThat(CronBuilder.every().friday().buildExpression()).contains("5");
            assertThat(CronBuilder.every().sunday().buildExpression()).contains("0");
        }

        @Test
        @DisplayName("构建使用DayOfWeek枚举")
        void should_build_with_day_of_week_enum() {
            String expr = CronBuilder.every().dayOfWeek(DayOfWeek.WEDNESDAY).at(15, 0).buildExpression();
            assertThat(expr).isEqualTo("0 15 * * 3");
        }
    }

    @Nested
    @DisplayName("特殊字符构建测试")
    class SpecialCharBuildTests {

        @Test
        @DisplayName("构建L表达式")
        void should_build_last_day() {
            assertThat(CronBuilder.create().lastDayOfMonth().at(18, 0).buildExpression())
                    .isEqualTo("0 18 L * *");
        }

        @Test
        @DisplayName("构建L-N表达式")
        void should_build_last_day_with_offset() {
            assertThat(CronBuilder.create().lastDayOfMonth(3).at(0, 0).buildExpression())
                    .isEqualTo("0 0 L-3 * *");
        }

        @Test
        @DisplayName("构建LW表达式")
        void should_build_last_weekday() {
            assertThat(CronBuilder.create().lastWeekdayOfMonth().at(0, 0).buildExpression())
                    .isEqualTo("0 0 LW * *");
        }

        @Test
        @DisplayName("构建W表达式")
        void should_build_nearest_weekday() {
            assertThat(CronBuilder.create().nearestWeekday(15).at(0, 0).buildExpression())
                    .isEqualTo("0 0 15W * *");
        }

        @Test
        @DisplayName("构建#表达式")
        void should_build_nth_day_of_week() {
            assertThat(CronBuilder.create().nthDayOfWeek(DayOfWeek.FRIDAY, 3).at(10, 0).buildExpression())
                    .isEqualTo("0 10 * * 5#3");
        }

        @Test
        @DisplayName("构建nL表达式")
        void should_build_last_dow() {
            assertThat(CronBuilder.create().lastDayOfWeek(DayOfWeek.FRIDAY).at(0, 0).buildExpression())
                    .isEqualTo("0 0 * * 5L");
        }
    }

    @Nested
    @DisplayName("输入校验测试")
    class ValidationTests {

        @Test
        @DisplayName("无效小时应抛出异常")
        void should_reject_invalid_hour() {
            assertThatThrownBy(() -> CronBuilder.every().at(24, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronBuilder.every().at(-1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("无效分钟应抛出异常")
        void should_reject_invalid_minute() {
            assertThatThrownBy(() -> CronBuilder.every().at(10, 60))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("无效秒间隔应抛出异常")
        void should_reject_invalid_second_interval() {
            assertThatThrownBy(() -> CronBuilder.everySeconds(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronBuilder.everySeconds(60))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("无效月份应抛出异常")
        void should_reject_invalid_month() {
            assertThatThrownBy(() -> CronBuilder.create().month(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronBuilder.create().month(13))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("无效月中日应抛出异常")
        void should_reject_invalid_day_of_month() {
            assertThatThrownBy(() -> CronBuilder.create().dayOfMonth(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronBuilder.create().dayOfMonth(32))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("无效nth应抛出异常")
        void should_reject_invalid_nth() {
            assertThatThrownBy(() -> CronBuilder.create().nthDayOfWeek(DayOfWeek.MONDAY, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronBuilder.create().nthDayOfWeek(DayOfWeek.MONDAY, 6))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("单字段设置测试")
    class SingleFieldTests {

        @Test
        @DisplayName("second() 设置秒并启用6字段")
        void should_set_second() {
            String expr = CronBuilder.create().second(30).buildExpression();
            assertThat(expr).startsWith("30 ");
        }

        @Test
        @DisplayName("minute() 设置分钟")
        void should_set_minute() {
            String expr = CronBuilder.create().minute(15).buildExpression();
            assertThat(expr).startsWith("15 ");
        }

        @Test
        @DisplayName("hour() 设置小时")
        void should_set_hour() {
            String expr = CronBuilder.create().hour(8).buildExpression();
            assertThat(expr).contains(" 8 ");
        }

        @Test
        @DisplayName("dayOfMonth() 设置月中日")
        void should_set_day_of_month() {
            String expr = CronBuilder.create().dayOfMonth(15).at(0, 0).buildExpression();
            assertThat(expr).isEqualTo("0 0 15 * *");
        }

        @Test
        @DisplayName("month() 设置月份")
        void should_set_month() {
            String expr = CronBuilder.create().month(6).at(0, 0).buildExpression();
            assertThat(expr).isEqualTo("0 0 * 6 *");
        }

        @Test
        @DisplayName("everyDays() 设置天间隔")
        void should_set_every_days() {
            String expr = CronBuilder.everyDays(3).buildExpression();
            assertThat(expr).isEqualTo("0 0 */3 * *");
        }

        @Test
        @DisplayName("everyDays(1) 设置每天")
        void should_set_every_day() {
            String expr = CronBuilder.everyDays(1).buildExpression();
            assertThat(expr).isEqualTo("0 0 * * *");
        }
    }

    @Nested
    @DisplayName("范围设置测试")
    class RangeTests {

        @Test
        @DisplayName("secondRange() 设置秒范围")
        void should_set_second_range() {
            String expr = CronBuilder.create().secondRange(10, 30).buildExpression();
            assertThat(expr).startsWith("10-30 ");
        }

        @Test
        @DisplayName("minuteRange() 设置分钟范围")
        void should_set_minute_range() {
            String expr = CronBuilder.create().minuteRange(0, 30).buildExpression();
            assertThat(expr).startsWith("0-30 ");
        }

        @Test
        @DisplayName("hourRange() 设置小时范围")
        void should_set_hour_range() {
            String expr = CronBuilder.create().hourRange(9, 17).buildExpression();
            assertThat(expr).contains(" 9-17 ");
        }

        @Test
        @DisplayName("范围值越界应抛出异常")
        void should_reject_out_of_range() {
            assertThatThrownBy(() -> CronBuilder.create().secondRange(-1, 30))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronBuilder.create().secondRange(0, 60))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CronBuilder.create().hourRange(0, 24))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("build()应返回可用的CronExpression")
        void should_build_and_parse() {
            CronExpression expr = CronBuilder.every().weekdays().at(9, 0).build();
            assertThat(expr).isNotNull();
            assertThat(expr.hasSeconds()).isFalse();
        }

        @Test
        @DisplayName("秒级表达式应标记hasSeconds")
        void should_have_seconds_flag() {
            CronExpression expr = CronBuilder.everySeconds(5).build();
            assertThat(expr.hasSeconds()).isTrue();
        }
    }
}
