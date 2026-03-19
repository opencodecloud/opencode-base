package cloud.opencode.base.cron;

import cloud.opencode.base.cron.exception.OpenCronException;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenCronTest Tests
 * OpenCronTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.0
 */
@DisplayName("OpenCron 门面测试")
class OpenCronTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Nested
    @DisplayName("解析测试")
    class ParseTests {

        @Test
        @DisplayName("解析有效表达式")
        void should_parse_valid() {
            assertThat(OpenCron.parse("30 10 * * *")).isNotNull();
        }

        @Test
        @DisplayName("解析宏")
        void should_parse_macro() {
            assertThat(OpenCron.parse(OpenCron.DAILY)).isNotNull();
            assertThat(OpenCron.parse(OpenCron.YEARLY)).isNotNull();
            assertThat(OpenCron.parse(OpenCron.HOURLY)).isNotNull();
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("有效表达式返回true")
        void should_return_true_for_valid() {
            assertThat(OpenCron.isValid("30 10 * * *")).isTrue();
            assertThat(OpenCron.isValid("@daily")).isTrue();
            assertThat(OpenCron.isValid("0 9 * * MON-FRI")).isTrue();
        }

        @Test
        @DisplayName("无效表达式返回false")
        void should_return_false_for_invalid() {
            assertThat(OpenCron.isValid("invalid")).isFalse();
            assertThat(OpenCron.isValid("")).isFalse();
            assertThat(OpenCron.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("validate应在无效时抛出异常")
        void should_throw_on_validate_invalid() {
            assertThatThrownBy(() -> OpenCron.validate("invalid"))
                    .isInstanceOf(OpenCronException.class);
        }

        @Test
        @DisplayName("validate(expr, duration) 间隔检查")
        void should_validate_with_interval() {
            assertThatNoException().isThrownBy(
                    () -> OpenCron.validate("0 0 * * *", Duration.ofMinutes(1)));
            assertThatThrownBy(
                    () -> OpenCron.validate("* * * * * *", Duration.ofSeconds(5)))
                    .isInstanceOf(OpenCronException.class);
        }
    }

    @Nested
    @DisplayName("调度测试")
    class SchedulingTests {

        @Test
        @DisplayName("获取下次执行时间")
        void should_get_next_execution() {
            ZonedDateTime from = ZonedDateTime.of(2026, 3, 15, 9, 0, 0, 0, UTC);
            ZonedDateTime next = OpenCron.nextExecution("30 10 * * *", from);
            assertThat(next).isEqualTo(ZonedDateTime.of(2026, 3, 15, 10, 30, 0, 0, UTC));
        }

        @Test
        @DisplayName("获取多个执行时间")
        void should_get_next_executions() {
            ZonedDateTime from = ZonedDateTime.of(2026, 3, 15, 0, 0, 0, 0, UTC);
            List<ZonedDateTime> times = OpenCron.nextExecutions("0 0 * * *", from, 3);
            assertThat(times).hasSize(3);
        }

        @Test
        @DisplayName("获取上次执行时间")
        void should_get_previous_execution() {
            ZonedDateTime from = ZonedDateTime.of(2026, 3, 15, 11, 0, 0, 0, UTC);
            ZonedDateTime prev = OpenCron.previousExecution("30 10 * * *", from);
            assertThat(prev).isEqualTo(ZonedDateTime.of(2026, 3, 15, 10, 30, 0, 0, UTC));
        }

        @Test
        @DisplayName("获取预估间隔")
        void should_get_estimated_interval() {
            Duration interval = OpenCron.getEstimatedInterval("0 0 * * *");
            assertThat(interval).isEqualTo(Duration.ofHours(24));
        }

        @Test
        @DisplayName("获取多个历史执行时间")
        void should_get_previous_executions() {
            ZonedDateTime from = ZonedDateTime.of(2026, 3, 15, 12, 0, 0, 0, UTC);
            List<ZonedDateTime> times = OpenCron.previousExecutions("0 0 * * *", from, 3);
            assertThat(times).hasSize(3);
            assertThat(times.get(0)).isEqualTo(ZonedDateTime.of(2026, 3, 15, 0, 0, 0, 0, UTC));
        }
    }

    @Nested
    @DisplayName("描述测试")
    class DescribeTests {

        @Test
        @DisplayName("描述表达式")
        void should_describe() {
            String desc = OpenCron.describe("*/5 * * * *");
            assertThat(desc).isNotBlank();
        }
    }

    @Nested
    @DisplayName("构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("通过门面获取构建器")
        void should_get_builder() {
            CronBuilder builder = OpenCron.builder();
            assertThat(builder).isNotNull();
            CronExpression expr = builder.weekdays().at(9, 0).build();
            assertThat(expr).isNotNull();
        }
    }
}
