package cloud.opencode.base.cron;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CronMacroTest Tests
 * CronMacroTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.0
 */
@DisplayName("CronMacro 测试")
class CronMacroTest {

    @Nested
    @DisplayName("resolve 测试")
    class ResolveTests {

        @Test
        @DisplayName("解析 @yearly")
        void should_resolve_yearly() {
            assertThat(CronMacro.resolve("@yearly")).isEqualTo("0 0 1 1 *");
        }

        @Test
        @DisplayName("解析 @annually (等同 @yearly)")
        void should_resolve_annually() {
            assertThat(CronMacro.resolve("@annually")).isEqualTo("0 0 1 1 *");
        }

        @Test
        @DisplayName("解析 @monthly")
        void should_resolve_monthly() {
            assertThat(CronMacro.resolve("@monthly")).isEqualTo("0 0 1 * *");
        }

        @Test
        @DisplayName("解析 @weekly")
        void should_resolve_weekly() {
            assertThat(CronMacro.resolve("@weekly")).isEqualTo("0 0 * * 0");
        }

        @Test
        @DisplayName("解析 @daily")
        void should_resolve_daily() {
            assertThat(CronMacro.resolve("@daily")).isEqualTo("0 0 * * *");
        }

        @Test
        @DisplayName("解析 @midnight (等同 @daily)")
        void should_resolve_midnight() {
            assertThat(CronMacro.resolve("@midnight")).isEqualTo("0 0 * * *");
        }

        @Test
        @DisplayName("解析 @hourly")
        void should_resolve_hourly() {
            assertThat(CronMacro.resolve("@hourly")).isEqualTo("0 * * * *");
        }

        @Test
        @DisplayName("大小写不敏感")
        void should_be_case_insensitive() {
            assertThat(CronMacro.resolve("@DAILY")).isEqualTo("0 0 * * *");
            assertThat(CronMacro.resolve("@Daily")).isEqualTo("0 0 * * *");
        }

        @Test
        @DisplayName("非宏返回null")
        void should_return_null_for_non_macro() {
            assertThat(CronMacro.resolve("0 * * * *")).isNull();
            assertThat(CronMacro.resolve("daily")).isNull();
            assertThat(CronMacro.resolve("@unknown")).isNull();
        }

        @Test
        @DisplayName("null返回null")
        void should_return_null_for_null() {
            assertThat(CronMacro.resolve(null)).isNull();
        }

        @Test
        @DisplayName("空字符串返回null")
        void should_return_null_for_empty() {
            assertThat(CronMacro.resolve("")).isNull();
        }
    }

    @Nested
    @DisplayName("isMacro 测试")
    class IsMacroTests {

        @Test
        @DisplayName("已知宏返回true")
        void should_return_true_for_known() {
            assertThat(CronMacro.isMacro("@daily")).isTrue();
            assertThat(CronMacro.isMacro("@yearly")).isTrue();
            assertThat(CronMacro.isMacro("@hourly")).isTrue();
            assertThat(CronMacro.isMacro("@monthly")).isTrue();
            assertThat(CronMacro.isMacro("@weekly")).isTrue();
            assertThat(CronMacro.isMacro("@annually")).isTrue();
            assertThat(CronMacro.isMacro("@midnight")).isTrue();
        }

        @Test
        @DisplayName("未知宏返回false")
        void should_return_false_for_unknown() {
            assertThat(CronMacro.isMacro("@secondly")).isFalse();
            assertThat(CronMacro.isMacro("0 * * * *")).isFalse();
            assertThat(CronMacro.isMacro(null)).isFalse();
        }
    }
}
