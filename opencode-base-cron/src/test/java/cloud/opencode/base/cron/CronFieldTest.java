package cloud.opencode.base.cron;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CronFieldTest Tests
 * CronFieldTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.0
 */
@DisplayName("CronField 测试")
class CronFieldTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("SECOND 范围 0-59")
        void second_range() {
            assertThat(CronField.SECOND.min()).isEqualTo(0);
            assertThat(CronField.SECOND.max()).isEqualTo(59);
            assertThat(CronField.SECOND.displayName()).isEqualTo("second");
        }

        @Test
        @DisplayName("MINUTE 范围 0-59")
        void minute_range() {
            assertThat(CronField.MINUTE.min()).isEqualTo(0);
            assertThat(CronField.MINUTE.max()).isEqualTo(59);
            assertThat(CronField.MINUTE.displayName()).isEqualTo("minute");
        }

        @Test
        @DisplayName("HOUR 范围 0-23")
        void hour_range() {
            assertThat(CronField.HOUR.min()).isEqualTo(0);
            assertThat(CronField.HOUR.max()).isEqualTo(23);
            assertThat(CronField.HOUR.displayName()).isEqualTo("hour");
        }

        @Test
        @DisplayName("DAY_OF_MONTH 范围 1-31")
        void day_of_month_range() {
            assertThat(CronField.DAY_OF_MONTH.min()).isEqualTo(1);
            assertThat(CronField.DAY_OF_MONTH.max()).isEqualTo(31);
            assertThat(CronField.DAY_OF_MONTH.displayName()).isEqualTo("day-of-month");
        }

        @Test
        @DisplayName("MONTH 范围 1-12")
        void month_range() {
            assertThat(CronField.MONTH.min()).isEqualTo(1);
            assertThat(CronField.MONTH.max()).isEqualTo(12);
            assertThat(CronField.MONTH.displayName()).isEqualTo("month");
        }

        @Test
        @DisplayName("DAY_OF_WEEK 范围 0-6")
        void day_of_week_range() {
            assertThat(CronField.DAY_OF_WEEK.min()).isEqualTo(0);
            assertThat(CronField.DAY_OF_WEEK.max()).isEqualTo(6);
            assertThat(CronField.DAY_OF_WEEK.displayName()).isEqualTo("day-of-week");
        }

        @Test
        @DisplayName("枚举包含所有6个字段")
        void all_fields_present() {
            assertThat(CronField.values()).hasSize(6);
        }
    }

    @Nested
    @DisplayName("isInRange 测试")
    class IsInRangeTests {

        @Test
        @DisplayName("范围内返回true")
        void should_return_true_in_range() {
            assertThat(CronField.SECOND.isInRange(0)).isTrue();
            assertThat(CronField.SECOND.isInRange(30)).isTrue();
            assertThat(CronField.SECOND.isInRange(59)).isTrue();
            assertThat(CronField.HOUR.isInRange(0)).isTrue();
            assertThat(CronField.HOUR.isInRange(23)).isTrue();
            assertThat(CronField.DAY_OF_MONTH.isInRange(1)).isTrue();
            assertThat(CronField.DAY_OF_MONTH.isInRange(31)).isTrue();
        }

        @Test
        @DisplayName("范围外返回false")
        void should_return_false_out_of_range() {
            assertThat(CronField.SECOND.isInRange(-1)).isFalse();
            assertThat(CronField.SECOND.isInRange(60)).isFalse();
            assertThat(CronField.HOUR.isInRange(24)).isFalse();
            assertThat(CronField.DAY_OF_MONTH.isInRange(0)).isFalse();
            assertThat(CronField.DAY_OF_MONTH.isInRange(32)).isFalse();
            assertThat(CronField.MONTH.isInRange(0)).isFalse();
            assertThat(CronField.MONTH.isInRange(13)).isFalse();
        }
    }

    @Nested
    @DisplayName("resolveAliases 测试")
    class ResolveAliasesTests {

        @Test
        @DisplayName("月份别名解析")
        void should_resolve_month_aliases() {
            assertThat(CronField.MONTH.resolveAliases("JAN")).isEqualTo("1");
            assertThat(CronField.MONTH.resolveAliases("DEC")).isEqualTo("12");
            assertThat(CronField.MONTH.resolveAliases("JAN-JUN")).isEqualTo("1-6");
        }

        @Test
        @DisplayName("星期别名解析")
        void should_resolve_dow_aliases() {
            assertThat(CronField.DAY_OF_WEEK.resolveAliases("MON")).isEqualTo("1");
            assertThat(CronField.DAY_OF_WEEK.resolveAliases("SUN")).isEqualTo("0");
            assertThat(CronField.DAY_OF_WEEK.resolveAliases("MON-FRI")).isEqualTo("1-5");
            assertThat(CronField.DAY_OF_WEEK.resolveAliases("SAT")).isEqualTo("6");
        }

        @Test
        @DisplayName("不区分大小写")
        void should_be_case_insensitive() {
            assertThat(CronField.DAY_OF_WEEK.resolveAliases("mon")).isEqualTo("1");
            assertThat(CronField.DAY_OF_WEEK.resolveAliases("Mon")).isEqualTo("1");
            assertThat(CronField.MONTH.resolveAliases("jan")).isEqualTo("1");
        }

        @Test
        @DisplayName("无别名的字段返回原值")
        void should_return_original_for_no_alias_fields() {
            assertThat(CronField.SECOND.resolveAliases("30")).isEqualTo("30");
            assertThat(CronField.MINUTE.resolveAliases("*/5")).isEqualTo("*/5");
            assertThat(CronField.HOUR.resolveAliases("9-17")).isEqualTo("9-17");
        }

        @Test
        @DisplayName("null输入返回null")
        void should_return_null_for_null() {
            assertThat(CronField.MONTH.resolveAliases(null)).isNull();
            assertThat(CronField.SECOND.resolveAliases(null)).isNull();
        }

        @Test
        @DisplayName("数字不受别名影响")
        void should_not_affect_numbers() {
            assertThat(CronField.MONTH.resolveAliases("1")).isEqualTo("1");
            assertThat(CronField.DAY_OF_WEEK.resolveAliases("0-6")).isEqualTo("0-6");
        }

        @Test
        @DisplayName("列表中的别名解析")
        void should_resolve_in_list() {
            assertThat(CronField.DAY_OF_WEEK.resolveAliases("MON,WED,FRI")).isEqualTo("1,3,5");
        }
    }
}
