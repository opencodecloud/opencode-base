package cloud.opencode.base.cron;

import cloud.opencode.base.cron.exception.OpenCronException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CronExpressionTest Tests
 * CronExpressionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.0
 */
@DisplayName("CronExpression 测试")
class CronExpressionTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    private static ZonedDateTime zdt(int year, int month, int day, int hour, int minute, int second) {
        return ZonedDateTime.of(year, month, day, hour, minute, second, 0, UTC);
    }

    private static ZonedDateTime zdt(int year, int month, int day, int hour, int minute) {
        return zdt(year, month, day, hour, minute, 0);
    }

    // ==================== Parse Tests ====================

    @Nested
    @DisplayName("解析测试")
    class ParseTests {

        @Test
        @DisplayName("解析5字段表达式")
        void should_parse_5field() {
            CronExpression expr = CronExpression.parse("30 10 * * *");
            assertThat(expr.hasSeconds()).isFalse();
            assertThat(expr.getExpression()).isEqualTo("30 10 * * *");
        }

        @Test
        @DisplayName("解析6字段表达式")
        void should_parse_6field() {
            CronExpression expr = CronExpression.parse("0 30 10 * * *");
            assertThat(expr.hasSeconds()).isTrue();
        }

        @Test
        @DisplayName("解析宏表达式")
        void should_parse_macros() {
            assertThat(CronExpression.parse("@daily")).isNotNull();
            assertThat(CronExpression.parse("@yearly")).isNotNull();
            assertThat(CronExpression.parse("@monthly")).isNotNull();
            assertThat(CronExpression.parse("@weekly")).isNotNull();
            assertThat(CronExpression.parse("@hourly")).isNotNull();
            assertThat(CronExpression.parse("@annually")).isNotNull();
            assertThat(CronExpression.parse("@midnight")).isNotNull();
        }

        @Test
        @DisplayName("解析英文名别名")
        void should_parse_name_aliases() {
            CronExpression expr = CronExpression.parse("0 9 * * MON-FRI");
            ZonedDateTime monday = zdt(2026, 3, 16, 9, 0); // Monday
            assertThat(expr.matches(monday)).isTrue();

            CronExpression monthExpr = CronExpression.parse("0 0 1 JAN *");
            ZonedDateTime jan1 = zdt(2027, 1, 1, 0, 0);
            assertThat(monthExpr.matches(jan1)).isTrue();
        }

        @Test
        @DisplayName("解析?通配符")
        void should_parse_question_mark() {
            CronExpression expr = CronExpression.parse("0 0 ? * MON");
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("解析带步长的范围")
        void should_parse_range_with_step() {
            CronExpression expr = CronExpression.parse("1-30/5 * * * *");
            assertThat(expr).isNotNull();
            // Should match minutes 1,6,11,16,21,26
            ZonedDateTime t = zdt(2026, 1, 1, 0, 6);
            assertThat(expr.matches(t)).isTrue();
            t = zdt(2026, 1, 1, 0, 7);
            assertThat(expr.matches(t)).isFalse();
        }

        @Test
        @DisplayName("解析范围回绕")
        void should_parse_wrap_around_range() {
            CronExpression expr = CronExpression.parse("0 22-2 * * *");
            assertThat(expr.matches(zdt(2026, 1, 1, 22, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 1, 1, 23, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 1, 1, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 1, 1, 1, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 1, 1, 2, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 1, 1, 3, 0))).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "  ", "* *", "* * * * * * *"})
        @DisplayName("无效表达式应抛出异常")
        void should_throw_on_invalid(String expr) {
            assertThatThrownBy(() -> CronExpression.parse(expr))
                    .isInstanceOf(OpenCronException.class);
        }

        @Test
        @DisplayName("null表达式应抛出异常")
        void should_throw_on_null() {
            assertThatThrownBy(() -> CronExpression.parse(null))
                    .isInstanceOf(OpenCronException.class);
        }

        @Test
        @DisplayName("无效范围应抛出异常")
        void should_throw_on_invalid_range() {
            assertThatThrownBy(() -> CronExpression.parse("60 * * * *"))
                    .isInstanceOf(OpenCronException.class);
        }

        @Test
        @DisplayName("无效步长应抛出异常")
        void should_throw_on_invalid_step() {
            assertThatThrownBy(() -> CronExpression.parse("*/0 * * * *"))
                    .isInstanceOf(OpenCronException.class);
        }

        @Test
        @DisplayName("解析L表达式")
        void should_parse_last_day() {
            assertThat(CronExpression.parse("0 0 L * *")).isNotNull();
            assertThat(CronExpression.parse("0 0 L-3 * *")).isNotNull();
            assertThat(CronExpression.parse("0 0 LW * *")).isNotNull();
        }

        @Test
        @DisplayName("解析#表达式")
        void should_parse_nth_day_of_week() {
            assertThat(CronExpression.parse("0 0 * * 5#3")).isNotNull();
        }

        @Test
        @DisplayName("解析W表达式")
        void should_parse_nearest_weekday() {
            assertThat(CronExpression.parse("0 0 15W * *")).isNotNull();
        }

        @Test
        @DisplayName("解析nL表达式")
        void should_parse_last_dow() {
            assertThat(CronExpression.parse("0 0 * * 5L")).isNotNull();
        }
    }

    // ==================== Matches Tests ====================

    @Nested
    @DisplayName("匹配测试")
    class MatchesTests {

        @Test
        @DisplayName("基本匹配")
        void should_match_basic() {
            CronExpression expr = CronExpression.parse("30 10 * * *");
            assertThat(expr.matches(zdt(2026, 3, 15, 10, 30))).isTrue();
            assertThat(expr.matches(zdt(2026, 3, 15, 10, 31))).isFalse();
        }

        @Test
        @DisplayName("特定星期匹配")
        void should_match_specific_day_of_week() {
            CronExpression expr = CronExpression.parse("0 9 * * 1"); // Monday
            ZonedDateTime monday = zdt(2026, 3, 16, 9, 0); // 2026-03-16 is Monday
            ZonedDateTime tuesday = zdt(2026, 3, 17, 9, 0);
            assertThat(expr.matches(monday)).isTrue();
            assertThat(expr.matches(tuesday)).isFalse();
        }

        @Test
        @DisplayName("L - 月最后一天匹配")
        void should_match_last_day_of_month() {
            CronExpression expr = CronExpression.parse("0 0 L * *");
            assertThat(expr.matches(zdt(2026, 1, 31, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 2, 28, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 1, 30, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("L-N - 月倒数第N天匹配")
        void should_match_last_day_minus_offset() {
            CronExpression expr = CronExpression.parse("0 0 L-3 * *");
            // January: 31 - 3 = 28
            assertThat(expr.matches(zdt(2026, 1, 28, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 1, 31, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("W - 最近工作日匹配")
        void should_match_nearest_weekday() {
            CronExpression expr = CronExpression.parse("0 0 15W * *");
            // 2026-03-15 is Sunday, nearest weekday is Monday 16th
            assertThat(expr.matches(zdt(2026, 3, 16, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 3, 15, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("LW - 月最后工作日匹配")
        void should_match_last_weekday() {
            CronExpression expr = CronExpression.parse("0 0 LW * *");
            // January 2026: 31st is Saturday, so last weekday = 30th (Friday)
            assertThat(expr.matches(zdt(2026, 1, 30, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 1, 31, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("# - 第N个星期几匹配")
        void should_match_nth_day_of_week() {
            CronExpression expr = CronExpression.parse("0 0 * * 5#3"); // 3rd Friday
            // 2026-03: Fridays are 6, 13, 20, 27 → 3rd Friday = 20
            assertThat(expr.matches(zdt(2026, 3, 20, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 3, 13, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("nL - 月最后一个星期几匹配")
        void should_match_last_day_of_week_in_month() {
            CronExpression expr = CronExpression.parse("0 0 * * 5L"); // Last Friday
            // 2026-03: last Friday = 27
            assertThat(expr.matches(zdt(2026, 3, 27, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 3, 20, 0, 0))).isFalse();
        }
    }

    // ==================== OR Semantics Tests ====================

    @Nested
    @DisplayName("OR语义测试")
    class OrSemanticsTests {

        @Test
        @DisplayName("dom和dow都限制时使用OR语义")
        void should_use_or_when_both_restricted() {
            // "0 12 15 * 1" → 15th OR Monday at noon
            CronExpression expr = CronExpression.parse("0 12 15 * 1");

            // 15th of month (not Monday) should match
            ZonedDateTime mar15 = zdt(2026, 3, 15, 12, 0); // Sunday
            assertThat(expr.matches(mar15)).isTrue();

            // Monday (not 15th) should match
            ZonedDateTime mar16 = zdt(2026, 3, 16, 12, 0); // Monday
            assertThat(expr.matches(mar16)).isTrue();

            // Neither 15th nor Monday
            ZonedDateTime mar17 = zdt(2026, 3, 17, 12, 0); // Tuesday, 17th
            assertThat(expr.matches(mar17)).isFalse();
        }

        @Test
        @DisplayName("只有dom限制时使用AND")
        void should_use_and_when_only_dom() {
            CronExpression expr = CronExpression.parse("0 0 15 * *");
            assertThat(expr.matches(zdt(2026, 3, 15, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 3, 16, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("只有dow限制时使用AND")
        void should_use_and_when_only_dow() {
            CronExpression expr = CronExpression.parse("0 0 * * 1"); // Monday
            ZonedDateTime monday = zdt(2026, 3, 16, 0, 0);
            ZonedDateTime tuesday = zdt(2026, 3, 17, 0, 0);
            assertThat(expr.matches(monday)).isTrue();
            assertThat(expr.matches(tuesday)).isFalse();
        }
    }

    // ==================== Next Execution Tests ====================

    @Nested
    @DisplayName("下次执行测试")
    class NextExecutionTests {

        @Test
        @DisplayName("基本下次执行")
        void should_find_next_execution() {
            CronExpression expr = CronExpression.parse("30 10 * * *");
            ZonedDateTime from = zdt(2026, 3, 15, 9, 0);
            ZonedDateTime next = expr.nextExecution(from);
            assertThat(next).isEqualTo(zdt(2026, 3, 15, 10, 30));
        }

        @Test
        @DisplayName("已过今天触发时间应跳到明天")
        void should_advance_to_next_day() {
            CronExpression expr = CronExpression.parse("30 10 * * *");
            ZonedDateTime from = zdt(2026, 3, 15, 11, 0);
            ZonedDateTime next = expr.nextExecution(from);
            assertThat(next).isEqualTo(zdt(2026, 3, 16, 10, 30));
        }

        @Test
        @DisplayName("跨月触发")
        void should_advance_to_next_month() {
            CronExpression expr = CronExpression.parse("0 0 1 * *"); // 1st of month
            ZonedDateTime from = zdt(2026, 3, 15, 0, 0);
            ZonedDateTime next = expr.nextExecution(from);
            assertThat(next).isEqualTo(zdt(2026, 4, 1, 0, 0));
        }

        @Test
        @DisplayName("跨年触发")
        void should_advance_to_next_year() {
            CronExpression expr = CronExpression.parse("0 0 1 1 *"); // Jan 1st
            ZonedDateTime from = zdt(2026, 3, 15, 0, 0);
            ZonedDateTime next = expr.nextExecution(from);
            assertThat(next).isEqualTo(zdt(2027, 1, 1, 0, 0));
        }

        @Test
        @DisplayName("6字段秒级触发")
        void should_handle_seconds() {
            CronExpression expr = CronExpression.parse("30 * * * * *");
            ZonedDateTime from = zdt(2026, 3, 15, 10, 0, 0);
            ZonedDateTime next = expr.nextExecution(from);
            assertThat(next).isEqualTo(zdt(2026, 3, 15, 10, 0, 30));
        }

        @Test
        @DisplayName("@daily 宏下次执行")
        void should_handle_daily_macro() {
            CronExpression expr = CronExpression.parse("@daily");
            ZonedDateTime from = zdt(2026, 3, 15, 1, 0);
            ZonedDateTime next = expr.nextExecution(from);
            assertThat(next).isEqualTo(zdt(2026, 3, 16, 0, 0));
        }

        @Test
        @DisplayName("L 最后一天下次执行")
        void should_find_next_last_day() {
            CronExpression expr = CronExpression.parse("0 0 L * *");
            ZonedDateTime from = zdt(2026, 2, 1, 0, 0);
            ZonedDateTime next = expr.nextExecution(from);
            assertThat(next).isEqualTo(zdt(2026, 2, 28, 0, 0));
        }

        @Test
        @DisplayName("# 第N个星期几下次执行")
        void should_find_next_nth_dow() {
            CronExpression expr = CronExpression.parse("0 10 * * 5#3"); // 3rd Friday
            ZonedDateTime from = zdt(2026, 3, 1, 0, 0);
            ZonedDateTime next = expr.nextExecution(from);
            // 2026-03 Fridays: 6,13,20,27 → 3rd = 20
            assertThat(next).isEqualTo(zdt(2026, 3, 20, 10, 0));
        }
    }

    // ==================== Next Executions (batch) Tests ====================

    @Nested
    @DisplayName("批量下次执行测试")
    class NextExecutionsTests {

        @Test
        @DisplayName("获取多个执行时间")
        void should_return_multiple_executions() {
            CronExpression expr = CronExpression.parse("0 0 * * *"); // daily midnight
            ZonedDateTime from = zdt(2026, 3, 15, 0, 0);
            List<ZonedDateTime> times = expr.nextExecutions(from, 3);
            assertThat(times).hasSize(3);
            assertThat(times.get(0)).isEqualTo(zdt(2026, 3, 16, 0, 0));
            assertThat(times.get(1)).isEqualTo(zdt(2026, 3, 17, 0, 0));
            assertThat(times.get(2)).isEqualTo(zdt(2026, 3, 18, 0, 0));
        }

        @Test
        @DisplayName("count为负应抛出异常")
        void should_throw_on_negative_count() {
            CronExpression expr = CronExpression.parse("* * * * *");
            assertThatThrownBy(() -> expr.nextExecutions(ZonedDateTime.now(), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Previous Execution Tests ====================

    @Nested
    @DisplayName("上次执行测试")
    class PreviousExecutionTests {

        @Test
        @DisplayName("基本上次执行")
        void should_find_previous_execution() {
            CronExpression expr = CronExpression.parse("30 10 * * *");
            ZonedDateTime from = zdt(2026, 3, 15, 11, 0);
            ZonedDateTime prev = expr.previousExecution(from);
            assertThat(prev).isEqualTo(zdt(2026, 3, 15, 10, 30));
        }

        @Test
        @DisplayName("跨天回溯")
        void should_go_back_to_previous_day() {
            CronExpression expr = CronExpression.parse("30 10 * * *");
            ZonedDateTime from = zdt(2026, 3, 15, 10, 0);
            ZonedDateTime prev = expr.previousExecution(from);
            assertThat(prev).isEqualTo(zdt(2026, 3, 14, 10, 30));
        }

        @Test
        @DisplayName("跨月回溯")
        void should_go_back_to_previous_month() {
            CronExpression expr = CronExpression.parse("0 0 28 * *"); // 28th
            ZonedDateTime from = zdt(2026, 3, 15, 0, 0);
            ZonedDateTime prev = expr.previousExecution(from);
            assertThat(prev).isEqualTo(zdt(2026, 2, 28, 0, 0));
        }
    }

    // ==================== Previous Executions (batch) Tests ====================

    @Nested
    @DisplayName("批量上次执行测试")
    class PreviousExecutionsTests {

        @Test
        @DisplayName("获取多个历史执行时间")
        void should_return_multiple_previous() {
            CronExpression expr = CronExpression.parse("0 0 * * *"); // daily midnight
            ZonedDateTime from = zdt(2026, 3, 15, 12, 0);
            List<ZonedDateTime> times = expr.previousExecutions(from, 3);
            assertThat(times).hasSize(3);
            assertThat(times.get(0)).isEqualTo(zdt(2026, 3, 15, 0, 0));
            assertThat(times.get(1)).isEqualTo(zdt(2026, 3, 14, 0, 0));
            assertThat(times.get(2)).isEqualTo(zdt(2026, 3, 13, 0, 0));
        }

        @Test
        @DisplayName("count为负应抛出异常")
        void should_throw_on_negative_count() {
            CronExpression expr = CronExpression.parse("* * * * *");
            assertThatThrownBy(() -> expr.previousExecutions(ZonedDateTime.now(), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Null Safety Tests ====================

    @Nested
    @DisplayName("空值安全测试")
    class NullSafetyTests {

        @Test
        @DisplayName("nextExecution(null) 应抛出 NullPointerException")
        void should_throw_npe_on_next_null() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThatThrownBy(() -> expr.nextExecution(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("previousExecution(null) 应抛出 NullPointerException")
        void should_throw_npe_on_prev_null() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThatThrownBy(() -> expr.previousExecution(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("matches(null) 应抛出 NullPointerException")
        void should_throw_npe_on_matches_null() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThatThrownBy(() -> expr.matches(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("nextExecutions(null, n) 应抛出 NullPointerException")
        void should_throw_npe_on_next_executions_null() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThatThrownBy(() -> expr.nextExecutions(null, 5))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Leap Year & Month Boundary Tests ====================

    @Nested
    @DisplayName("闰年和月末边界测试")
    class LeapYearBoundaryTests {

        @Test
        @DisplayName("闰年2月29日 - L匹配")
        void should_match_leap_year_last_day() {
            CronExpression expr = CronExpression.parse("0 0 L * *");
            // 2028 is a leap year
            assertThat(expr.matches(zdt(2028, 2, 29, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2028, 2, 28, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("非闰年2月28日 - L匹配")
        void should_match_non_leap_year_last_day() {
            CronExpression expr = CronExpression.parse("0 0 L * *");
            // 2026 is not a leap year
            assertThat(expr.matches(zdt(2026, 2, 28, 0, 0))).isTrue();
        }

        @Test
        @DisplayName("闰年2月29日 - nextExecution跨越")
        void should_find_feb29_in_leap_year() {
            CronExpression expr = CronExpression.parse("0 0 29 2 *"); // Feb 29
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            ZonedDateTime next = expr.nextExecution(from);
            // Next Feb 29 is in 2028
            assertThat(next).isEqualTo(zdt(2028, 2, 29, 0, 0));
        }

        @Test
        @DisplayName("月末30日 - 在2月不触发")
        void should_skip_feb_for_day30() {
            CronExpression expr = CronExpression.parse("0 0 30 * *");
            ZonedDateTime from = zdt(2026, 1, 31, 0, 0);
            ZonedDateTime next = expr.nextExecution(from);
            // Feb has no 30th, should skip to March 30
            assertThat(next).isNotNull();
            assertThat(next.getMonthValue()).isEqualTo(3);
            assertThat(next.getDayOfMonth()).isEqualTo(30);
        }

        @Test
        @DisplayName("月末31日 - 跳过短月")
        void should_skip_short_months_for_day31() {
            CronExpression expr = CronExpression.parse("0 0 31 * *");
            ZonedDateTime from = zdt(2026, 3, 31, 1, 0);
            ZonedDateTime next = expr.nextExecution(from);
            // April has no 31st, should go to May 31
            assertThat(next).isNotNull();
            assertThat(next.getMonthValue()).isEqualTo(5);
            assertThat(next.getDayOfMonth()).isEqualTo(31);
        }

        @Test
        @DisplayName("LW - 2月最后工作日")
        void should_match_last_weekday_feb() {
            CronExpression expr = CronExpression.parse("0 0 LW * *");
            // 2026-02-28 is Saturday, last weekday = Feb 27 (Friday)
            assertThat(expr.matches(zdt(2026, 2, 27, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 2, 28, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("1W - 1号是周六时使用周一")
        void should_use_monday_when_1st_is_saturday() {
            CronExpression expr = CronExpression.parse("0 0 1W * *");
            // 2026-08-01 is Saturday → nearest weekday = Monday Aug 3
            assertThat(expr.matches(zdt(2026, 8, 3, 0, 0))).isTrue();
            assertThat(expr.matches(zdt(2026, 8, 1, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("previousExecution - 跨年回溯")
        void should_go_back_across_year() {
            CronExpression expr = CronExpression.parse("0 0 1 1 *"); // Jan 1
            ZonedDateTime from = zdt(2026, 6, 15, 0, 0);
            ZonedDateTime prev = expr.previousExecution(from);
            assertThat(prev).isEqualTo(zdt(2026, 1, 1, 0, 0));
        }
    }

    // ==================== Describe Tests ====================

    @Nested
    @DisplayName("描述测试")
    class DescribeTests {

        @Test
        @DisplayName("描述每日午夜")
        void should_describe_daily() {
            String desc = CronExpression.parse("0 0 * * *").describe();
            assertThat(desc).containsIgnoringCase("00:00");
        }

        @Test
        @DisplayName("描述每N分钟")
        void should_describe_every_n_minutes() {
            String desc = CronExpression.parse("*/5 * * * *").describe();
            assertThat(desc).containsIgnoringCase("5 minutes");
        }

        @Test
        @DisplayName("描述工作日")
        void should_describe_weekdays() {
            String desc = CronExpression.parse("0 9 * * MON-FRI").describe();
            assertThat(desc).containsIgnoringCase("Monday").containsIgnoringCase("Friday");
        }

        @Test
        @DisplayName("描述每小时")
        void should_describe_every_hour() {
            String desc = CronExpression.parse("0 * * * *").describe();
            assertThat(desc).isNotBlank();
        }

        @Test
        @DisplayName("描述月最后一天")
        void should_describe_last_day() {
            String desc = CronExpression.parse("0 0 L * *").describe();
            assertThat(desc).containsIgnoringCase("last day");
        }

        @Test
        @DisplayName("描述第N个星期几")
        void should_describe_nth_dow() {
            String desc = CronExpression.parse("0 10 * * 5#3").describe();
            assertThat(desc).containsIgnoringCase("3rd").containsIgnoringCase("Friday");
        }

        @Test
        @DisplayName("描述特定月份")
        void should_describe_specific_month() {
            String desc = CronExpression.parse("0 0 1 JAN *").describe();
            assertThat(desc).containsIgnoringCase("January");
        }

        @Test
        @DisplayName("描述LW")
        void should_describe_last_weekday() {
            String desc = CronExpression.parse("0 0 LW * *").describe();
            assertThat(desc).containsIgnoringCase("last weekday");
        }

        @Test
        @DisplayName("描述nW")
        void should_describe_nearest_weekday() {
            String desc = CronExpression.parse("0 0 15W * *").describe();
            assertThat(desc).containsIgnoringCase("nearest weekday");
        }

        @Test
        @DisplayName("描述每秒")
        void should_describe_every_second() {
            String desc = CronExpression.parse("* * * * * *").describe();
            assertThat(desc).containsIgnoringCase("second");
        }

        @Test
        @DisplayName("描述周末")
        void should_describe_weekends() {
            String desc = CronExpression.parse("0 10 * * 0,6").describe();
            assertThat(desc).containsIgnoringCase("weekend");
        }
    }

    // ==================== Equals/HashCode Tests ====================

    @Nested
    @DisplayName("equals/hashCode 测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同表达式应相等")
        void should_be_equal_for_same_expression() {
            CronExpression a = CronExpression.parse("30 10 * * *");
            CronExpression b = CronExpression.parse("30 10 * * *");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不同表达式不应相等")
        void should_not_be_equal_for_different_expression() {
            CronExpression a = CronExpression.parse("30 10 * * *");
            CronExpression b = CronExpression.parse("0 9 * * *");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("toString返回原始表达式")
        void should_return_expression_from_toString() {
            CronExpression expr = CronExpression.parse("30 10 * * *");
            assertThat(expr.toString()).isEqualTo("30 10 * * *");
        }

        @Test
        @DisplayName("getExpression返回原始表达式")
        void should_return_expression_from_getter() {
            CronExpression expr = CronExpression.parse("0 9 * * MON-FRI");
            assertThat(expr.getExpression()).isEqualTo("0 9 * * MON-FRI");
        }

        @Test
        @DisplayName("不等于null")
        void should_not_equal_null() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThat(expr).isNotEqualTo(null);
        }

        @Test
        @DisplayName("不等于其他类型")
        void should_not_equal_other_type() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThat(expr).isNotEqualTo("0 0 * * *");
        }
    }

    // ==================== Serialization Tests ====================

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("CronExpression实现Serializable")
        void should_be_serializable() {
            CronExpression expr = CronExpression.parse("30 10 * * *");
            assertThat(expr).isInstanceOf(java.io.Serializable.class);
        }
    }
}
