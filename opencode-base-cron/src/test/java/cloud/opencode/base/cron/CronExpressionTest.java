package cloud.opencode.base.cron;

import cloud.opencode.base.cron.exception.OpenCronException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.*;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

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

    // ==================== Configurable maxYears Tests ====================

    @Nested
    @DisplayName("可配置搜索窗口测试")
    class ConfigurableMaxYearsTests {

        @Test
        @DisplayName("nextExecution(from, maxYears) 正常搜索")
        void should_find_next_with_custom_max_years() {
            CronExpression expr = CronExpression.parse("0 0 29 2 *"); // Feb 29
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            ZonedDateTime next = expr.nextExecution(from, 10);
            assertThat(next).isEqualTo(zdt(2028, 2, 29, 0, 0));
        }

        @Test
        @DisplayName("nextExecution 搜索窗口过小返回null")
        void should_return_null_when_max_years_too_small() {
            CronExpression expr = CronExpression.parse("0 0 29 2 *"); // Feb 29
            ZonedDateTime from = zdt(2026, 3, 1, 0, 0);
            // maxYears=1: only searches through 2027, next Feb 29 is 2028
            ZonedDateTime next = expr.nextExecution(from, 1);
            assertThat(next).isNull();
        }

        @Test
        @DisplayName("previousExecution(from, maxYears) 正常搜索")
        void should_find_previous_with_custom_max_years() {
            CronExpression expr = CronExpression.parse("0 0 29 2 *"); // Feb 29
            ZonedDateTime from = zdt(2029, 1, 1, 0, 0);
            ZonedDateTime prev = expr.previousExecution(from, 10);
            assertThat(prev).isEqualTo(zdt(2028, 2, 29, 0, 0));
        }

        @Test
        @DisplayName("maxYears < 1 应抛出异常")
        void should_throw_on_max_years_below_1() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            assertThatThrownBy(() -> expr.nextExecution(from, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> expr.previousExecution(from, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("maxYears > 100 应抛出异常")
        void should_throw_on_max_years_above_100() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            assertThatThrownBy(() -> expr.nextExecution(from, 101))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> expr.previousExecution(from, 101))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Duration Convenience Tests ====================

    @Nested
    @DisplayName("时间间隔便利方法测试")
    class DurationConvenienceTests {

        @Test
        @DisplayName("timeToNextExecution 返回正确的Duration")
        void should_return_correct_time_to_next() {
            CronExpression expr = CronExpression.parse("30 10 * * *");
            ZonedDateTime from = zdt(2026, 3, 15, 10, 0);
            Duration duration = expr.timeToNextExecution(from);
            assertThat(duration).isNotNull();
            assertThat(duration).isEqualTo(Duration.ofMinutes(30));
        }

        @Test
        @DisplayName("timeFromLastExecution 返回正确的Duration")
        void should_return_correct_time_from_last() {
            CronExpression expr = CronExpression.parse("30 10 * * *");
            ZonedDateTime from = zdt(2026, 3, 15, 11, 0);
            Duration duration = expr.timeFromLastExecution(from);
            assertThat(duration).isNotNull();
            assertThat(duration).isEqualTo(Duration.ofMinutes(30));
        }

        @Test
        @DisplayName("timeToNextExecution(null) 应抛出 NullPointerException")
        void should_throw_npe_on_null_time_to_next() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThatThrownBy(() -> expr.timeToNextExecution(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("timeFromLastExecution(null) 应抛出 NullPointerException")
        void should_throw_npe_on_null_time_from_last() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThatThrownBy(() -> expr.timeFromLastExecution(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Count Executions Between Tests ====================

    @Nested
    @DisplayName("区间执行计数测试")
    class CountExecutionsBetweenTests {

        @Test
        @DisplayName("每天午夜7天应有7次执行")
        void should_count_daily_over_7_days() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            ZonedDateTime to = zdt(2026, 1, 8, 0, 0);
            long count = expr.countExecutionsBetween(from, to);
            assertThat(count).isEqualTo(7);
        }

        @Test
        @DisplayName("每小时24小时应有24次执行")
        void should_count_hourly_over_24_hours() {
            CronExpression expr = CronExpression.parse("0 * * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            ZonedDateTime to = zdt(2026, 1, 2, 0, 0);
            long count = expr.countExecutionsBetween(from, to);
            assertThat(count).isEqualTo(24);
        }

        @Test
        @DisplayName("from >= to 应抛出异常")
        void should_throw_when_from_not_before_to() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime t = zdt(2026, 1, 1, 0, 0);
            assertThatThrownBy(() -> expr.countExecutionsBetween(t, t))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> expr.countExecutionsBetween(t, t.minusDays(1)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null参数应抛出 NullPointerException")
        void should_throw_npe_on_null() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime t = zdt(2026, 1, 1, 0, 0);
            assertThatThrownBy(() -> expr.countExecutionsBetween(null, t))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> expr.countExecutionsBetween(t, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Executions Between Tests ====================

    @Nested
    @DisplayName("区间执行列表测试")
    class ExecutionsBetweenTests {

        @Test
        @DisplayName("executionsBetween 返回正确的列表")
        void should_list_executions_between() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            ZonedDateTime to = zdt(2026, 1, 4, 0, 0);
            List<ZonedDateTime> list = expr.executionsBetween(from, to);
            assertThat(list).hasSize(3);
            assertThat(list.get(0)).isEqualTo(zdt(2026, 1, 2, 0, 0));
            assertThat(list.get(1)).isEqualTo(zdt(2026, 1, 3, 0, 0));
            assertThat(list.get(2)).isEqualTo(zdt(2026, 1, 4, 0, 0));
        }

        @Test
        @DisplayName("executionsBetween 返回不可修改列表")
        void should_return_unmodifiable_list() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            ZonedDateTime to = zdt(2026, 1, 3, 0, 0);
            List<ZonedDateTime> list = expr.executionsBetween(from, to);
            assertThatThrownBy(() -> list.add(zdt(2026, 1, 5, 0, 0)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("executionsBetween 带limit限制")
        void should_respect_limit() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            ZonedDateTime to = zdt(2026, 12, 31, 23, 59);
            List<ZonedDateTime> list = expr.executionsBetween(from, to, 5);
            assertThat(list).hasSize(5);
        }

        @Test
        @DisplayName("executionsBetween from >= to 应抛出异常")
        void should_throw_when_from_not_before_to() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime t = zdt(2026, 1, 1, 0, 0);
            assertThatThrownBy(() -> expr.executionsBetween(t, t))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("executionsBetween limit超出范围应抛出异常")
        void should_throw_on_invalid_limit() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            ZonedDateTime to = zdt(2026, 12, 31, 23, 59);
            assertThatThrownBy(() -> expr.executionsBetween(from, to, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> expr.executionsBetween(from, to, 1_000_001))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== isEquivalentTo Tests ====================

    @Nested
    @DisplayName("等价性测试")
    class EquivalenceTests {

        @Test
        @DisplayName("@daily 等价于 0 0 * * *")
        void should_be_equivalent_daily_macro() {
            CronExpression daily = CronExpression.parse("@daily");
            CronExpression explicit = CronExpression.parse("0 0 * * *");
            assertThat(daily.isEquivalentTo(explicit)).isTrue();
        }

        @Test
        @DisplayName("@daily 不等价于 0 1 * * *")
        void should_not_be_equivalent_different_hour() {
            CronExpression daily = CronExpression.parse("@daily");
            CronExpression oneAm = CronExpression.parse("0 1 * * *");
            assertThat(daily.isEquivalentTo(oneAm)).isFalse();
        }

        @Test
        @DisplayName("isEquivalentTo(null) 返回false")
        void should_return_false_for_null() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThat(expr.isEquivalentTo(null)).isFalse();
        }

        @Test
        @DisplayName("自身等价")
        void should_be_equivalent_to_self() {
            CronExpression expr = CronExpression.parse("*/5 * * * *");
            assertThat(expr.isEquivalentTo(expr)).isTrue();
        }

        @Test
        @DisplayName("不同特殊字符不等价")
        void should_not_be_equivalent_different_specials() {
            CronExpression a = CronExpression.parse("0 0 L * *");
            CronExpression b = CronExpression.parse("0 0 LW * *");
            assertThat(a.isEquivalentTo(b)).isFalse();
        }
    }

    // ==================== Explain Tests ====================

    @Nested
    @DisplayName("解释测试")
    class ExplainTests {

        @Test
        @DisplayName("explain 返回正确的CronExplanation")
        void should_return_valid_explanation() {
            CronExpression expr = CronExpression.parse("0 9 * * MON-FRI");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            CronExplanation explanation = expr.explain(from);
            assertThat(explanation).isNotNull();
            assertThat(explanation.expression()).isEqualTo("0 9 * * MON-FRI");
            assertThat(explanation.description()).isNotBlank();
            assertThat(explanation.nextExecutions()).isNotEmpty();
            assertThat(explanation.nextExecutions()).hasSizeLessThanOrEqualTo(5);
        }

        @Test
        @DisplayName("explain 预估间隔正确")
        void should_have_reasonable_interval() {
            CronExpression expr = CronExpression.parse("0 0 * * *"); // daily
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0);
            CronExplanation explanation = expr.explain(from);
            assertThat(explanation.estimatedInterval()).isEqualTo(Duration.ofHours(24));
        }

        @Test
        @DisplayName("explain(null) 应抛出 NullPointerException")
        void should_throw_npe_on_null() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThatThrownBy(() -> expr.explain(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Describe with Locale Tests ====================

    @Nested
    @DisplayName("多语言描述测试")
    class DescribeLocaleTests {

        @Test
        @DisplayName("describe(Locale.CHINESE) 返回中文")
        void should_describe_in_chinese() {
            String desc = CronExpression.parse("*/5 * * * *").describe(Locale.CHINESE);
            assertThat(desc).contains("5");
            // Chinese description should contain Chinese characters
            assertThat(desc).containsPattern("[\\u4e00-\\u9fff]");
        }

        @Test
        @DisplayName("describe(Locale.ENGLISH) 返回英文")
        void should_describe_in_english() {
            String desc = CronExpression.parse("*/5 * * * *").describe(Locale.ENGLISH);
            assertThat(desc).containsIgnoringCase("5 minutes");
        }

        @Test
        @DisplayName("describe(Locale.SIMPLIFIED_CHINESE) 返回中文")
        void should_describe_in_simplified_chinese() {
            String desc = CronExpression.parse("0 0 * * *").describe(Locale.SIMPLIFIED_CHINESE);
            assertThat(desc).containsPattern("[\\u4e00-\\u9fff]");
        }

        @Test
        @DisplayName("describe(null) 应抛出 NullPointerException")
        void should_throw_npe_on_null_locale() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThatThrownBy(() -> expr.describe((Locale) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== DST Tests ====================

    @Nested
    @DisplayName("夏令时测试")
    class DstTests {

        @Test
        @DisplayName("春进（Spring Forward）跳过不存在的时间")
        void should_handle_spring_forward() {
            // US Eastern: 2026-03-08 2:00 AM → 3:00 AM (spring forward)
            ZoneId eastern = ZoneId.of("America/New_York");
            CronExpression expr = CronExpression.parse("30 2 * * *"); // 2:30 AM daily
            ZonedDateTime from = ZonedDateTime.of(2026, 3, 7, 23, 0, 0, 0, eastern);
            ZonedDateTime next = expr.nextExecution(from);

            // 2:30 AM does not exist on March 8 (clock jumps from 2:00 to 3:00)
            // Should skip to March 9 at 2:30 AM
            assertThat(next).isNotNull();
            assertThat(next.getDayOfMonth()).isEqualTo(9);
            assertThat(next.getHour()).isEqualTo(2);
            assertThat(next.getMinute()).isEqualTo(30);
        }
    }

    // ==================== Stream Tests ====================

    @Nested
    @DisplayName("Stream 流式调度测试")
    class StreamTests {

        @Test
        @DisplayName("stream 返回正确的前3个元素")
        void should_return_first_3_elements() {
            CronExpression expr = CronExpression.parse("0 0 * * *"); // daily at midnight
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);
            List<ZonedDateTime> first3 = expr.stream(from).limit(3).toList();

            assertThat(first3).hasSize(3);
            assertThat(first3.get(0)).isEqualTo(zdt(2026, 1, 2, 0, 0));
            assertThat(first3.get(1)).isEqualTo(zdt(2026, 1, 3, 0, 0));
            assertThat(first3.get(2)).isEqualTo(zdt(2026, 1, 4, 0, 0));
        }

        @Test
        @DisplayName("stream 配合 takeWhile 在边界处停止")
        void should_stop_at_boundary_with_takeWhile() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);
            ZonedDateTime boundary = zdt(2026, 1, 5, 0, 0, 0);

            List<ZonedDateTime> results = expr.stream(from)
                    .takeWhile(t -> t.isBefore(boundary))
                    .toList();

            assertThat(results).hasSize(3);
            assertThat(results).allMatch(t -> t.isBefore(boundary));
        }

        @Test
        @DisplayName("stream 是惰性的（limit 不会预先计算所有值）")
        void should_be_lazy() {
            CronExpression expr = CronExpression.parse("* * * * *"); // every minute
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);

            // If not lazy, this would attempt to compute an enormous number of elements
            List<ZonedDateTime> first5 = expr.stream(from).limit(5).toList();
            assertThat(first5).hasSize(5);
            assertThat(first5.get(0)).isEqualTo(zdt(2026, 1, 1, 0, 1));
        }

        @Test
        @DisplayName("reverseStream 返回降序时间")
        void should_return_descending_times() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            ZonedDateTime from = zdt(2026, 1, 5, 12, 0, 0);
            List<ZonedDateTime> results = expr.reverseStream(from).limit(3).toList();

            assertThat(results).hasSize(3);
            assertThat(results.get(0)).isEqualTo(zdt(2026, 1, 5, 0, 0));
            assertThat(results.get(1)).isEqualTo(zdt(2026, 1, 4, 0, 0));
            assertThat(results.get(2)).isEqualTo(zdt(2026, 1, 3, 0, 0));
        }

        @Test
        @DisplayName("stream(null) 抛出 NullPointerException")
        void should_throw_npe_on_null_from() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThatThrownBy(() -> expr.stream(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("reverseStream(null) 抛出 NullPointerException")
        void should_throw_npe_on_null_from_reverse() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            assertThatThrownBy(() -> expr.reverseStream(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== TemporalAdjuster Tests ====================

    @Nested
    @DisplayName("TemporalAdjuster 时间调节器测试")
    class TemporalAdjusterTests {

        @Test
        @DisplayName("ZonedDateTime.with(cronExpr) 返回下次执行时间")
        void should_adjust_to_next_execution() {
            CronExpression expr = CronExpression.parse("30 10 * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);
            ZonedDateTime result = (ZonedDateTime) from.with(expr);

            assertThat(result).isEqualTo(zdt(2026, 1, 1, 10, 30));
        }

        @Test
        @DisplayName("with(cronExpr) 结果与 nextExecution 一致")
        void should_match_nextExecution() {
            CronExpression expr = CronExpression.parse("0 9 * * MON-FRI");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0); // Thursday
            ZonedDateTime viaWith = (ZonedDateTime) from.with(expr);
            ZonedDateTime viaNext = expr.nextExecution(from);

            assertThat(viaWith).isEqualTo(viaNext);
        }

        @Test
        @DisplayName("无法匹配的表达式抛出 DateTimeException")
        void should_throw_when_no_execution_found() {
            // Feb 30 never exists - with maxYears=4 default, this should fail
            CronExpression expr = CronExpression.parse("0 0 30 2 *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);

            assertThatThrownBy(() -> from.with(expr))
                    .isInstanceOf(DateTimeException.class);
        }
    }

    // ==================== Filtered Scheduling Tests ====================

    @Nested
    @DisplayName("Filtered Scheduling 过滤调度测试")
    class FilteredSchedulingTests {

        @Test
        @DisplayName("nextExecution 配合工作日过滤跳过周末")
        void should_skip_weekends_with_weekday_filter() {
            // Every day at 9:00, but filter for weekdays only
            CronExpression expr = CronExpression.parse("0 9 * * *");
            // 2026-01-03 is Saturday
            ZonedDateTime from = zdt(2026, 1, 2, 12, 0, 0); // Friday afternoon
            ZonedDateTime result = expr.nextExecution(from,
                    t -> t.getDayOfWeek().getValue() <= 5);

            assertThat(result).isNotNull();
            // Should skip Sat (3rd) and Sun (4th), land on Monday (5th)
            assertThat(result).isEqualTo(zdt(2026, 1, 5, 9, 0));
            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        }

        @Test
        @DisplayName("nextExecution 配合节假日排除集合")
        void should_skip_holidays() {
            CronExpression expr = CronExpression.parse("0 9 * * *");
            Set<LocalDate> holidays = Set.of(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 2)
            );
            ZonedDateTime from = zdt(2025, 12, 31, 12, 0, 0);
            ZonedDateTime result = expr.nextExecution(from,
                    t -> !holidays.contains(t.toLocalDate()));

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(zdt(2026, 1, 3, 9, 0));
        }

        @Test
        @DisplayName("nextExecution 配合始终为 false 的过滤器返回 null")
        void should_return_null_for_always_false_filter() {
            CronExpression expr = CronExpression.parse("0 9 * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);
            ZonedDateTime result = expr.nextExecution(from, _ -> false);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("previousExecution 配合过滤器正确工作")
        void should_filter_previous_execution() {
            CronExpression expr = CronExpression.parse("0 9 * * *");
            // 2026-01-05 is Monday; look backward skipping weekends
            ZonedDateTime from = zdt(2026, 1, 5, 12, 0, 0);
            ZonedDateTime result = expr.previousExecution(from,
                    t -> t.getDayOfWeek().getValue() <= 5);

            assertThat(result).isNotNull();
            // Monday 9:00 should match since it's a weekday
            assertThat(result).isEqualTo(zdt(2026, 1, 5, 9, 0));
        }

        @Test
        @DisplayName("nextExecution(null, filter) 抛出 NullPointerException")
        void should_throw_npe_on_null_from() {
            CronExpression expr = CronExpression.parse("0 9 * * *");
            assertThatThrownBy(() -> expr.nextExecution(null, _ -> true))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("nextExecution(from, null) 抛出 NullPointerException")
        void should_throw_npe_on_null_filter() {
            CronExpression expr = CronExpression.parse("0 9 * * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);
            assertThatThrownBy(() -> expr.nextExecution(from, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Overlap Detection Tests ====================

    @Nested
    @DisplayName("Overlap Detection 重叠检测测试")
    class OverlapDetectionTests {

        @Test
        @DisplayName("每天午夜 与 每月1号午夜 在1号重叠")
        void should_find_overlap_on_first_of_month() {
            CronExpression daily = CronExpression.parse("0 0 * * *");
            CronExpression monthly = CronExpression.parse("0 0 1 * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);

            ZonedDateTime overlap = daily.nextOverlap(monthly, from);
            assertThat(overlap).isNotNull();
            assertThat(overlap).isEqualTo(zdt(2026, 2, 1, 0, 0));
        }

        @Test
        @DisplayName("周一9点 与 周五17点 无重叠")
        void should_not_find_overlap_for_disjoint_schedules() {
            CronExpression mon9 = CronExpression.parse("0 9 * * MON");
            CronExpression fri17 = CronExpression.parse("0 17 * * FRI");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);

            ZonedDateTime overlap = mon9.nextOverlap(fri17, from);
            assertThat(overlap).isNull();
        }

        @Test
        @DisplayName("等价的每天表达式总是重叠")
        void should_always_overlap_for_equivalent_schedules() {
            CronExpression expr1 = CronExpression.parse("0 0 * * *");
            CronExpression expr2 = CronExpression.parse("@daily");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);

            ZonedDateTime overlap = expr1.nextOverlap(expr2, from);
            assertThat(overlap).isNotNull();
            assertThat(overlap).isEqualTo(zdt(2026, 1, 2, 0, 0));
        }

        @Test
        @DisplayName("hasOverlapBetween 范围内有重叠返回 true")
        void should_return_true_when_overlap_in_range() {
            CronExpression daily = CronExpression.parse("0 0 * * *");
            CronExpression monthly = CronExpression.parse("0 0 1 * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);
            ZonedDateTime to = zdt(2026, 3, 1, 0, 0, 0);

            assertThat(daily.hasOverlapBetween(monthly, from, to)).isTrue();
        }

        @Test
        @DisplayName("hasOverlapBetween 范围内无重叠返回 false")
        void should_return_false_when_no_overlap_in_range() {
            CronExpression mon9 = CronExpression.parse("0 9 * * MON");
            CronExpression fri17 = CronExpression.parse("0 17 * * FRI");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);
            ZonedDateTime to = zdt(2026, 12, 31, 23, 59, 59);

            assertThat(mon9.hasOverlapBetween(fri17, from, to)).isFalse();
        }

        @Test
        @DisplayName("null 参数抛出 NullPointerException")
        void should_throw_npe_on_null_args() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            CronExpression other = CronExpression.parse("0 0 1 * *");
            ZonedDateTime from = zdt(2026, 1, 1, 0, 0, 0);
            ZonedDateTime to = zdt(2026, 12, 31, 0, 0, 0);

            assertThatThrownBy(() -> expr.nextOverlap(null, from))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> expr.nextOverlap(other, null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> expr.hasOverlapBetween(null, from, to))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("from >= to 抛出 IllegalArgumentException")
        void should_throw_iae_when_from_not_before_to() {
            CronExpression expr = CronExpression.parse("0 0 * * *");
            CronExpression other = CronExpression.parse("0 0 1 * *");
            ZonedDateTime time = zdt(2026, 1, 1, 0, 0, 0);

            assertThatThrownBy(() -> expr.hasOverlapBetween(other, time, time))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> expr.hasOverlapBetween(other,
                    zdt(2026, 6, 1, 0, 0, 0), zdt(2026, 1, 1, 0, 0, 0)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
