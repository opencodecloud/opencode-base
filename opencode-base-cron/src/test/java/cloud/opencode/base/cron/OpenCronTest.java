package cloud.opencode.base.cron;

import cloud.opencode.base.cron.exception.OpenCronException;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

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

    // ==================== Duration Convenience Tests ====================

    @Nested
    @DisplayName("时间间隔便利方法测试")
    class DurationConvenienceTests {

        @Test
        @DisplayName("timeToNextExecution 返回正确Duration")
        void should_get_time_to_next() {
            ZonedDateTime from = ZonedDateTime.of(2026, 3, 15, 10, 0, 0, 0, UTC);
            Duration duration = OpenCron.timeToNextExecution("30 10 * * *", from);
            assertThat(duration).isNotNull();
            assertThat(duration).isEqualTo(Duration.ofMinutes(30));
        }

        @Test
        @DisplayName("timeFromLastExecution 返回正确Duration")
        void should_get_time_from_last() {
            ZonedDateTime from = ZonedDateTime.of(2026, 3, 15, 11, 0, 0, 0, UTC);
            Duration duration = OpenCron.timeFromLastExecution("30 10 * * *", from);
            assertThat(duration).isNotNull();
            assertThat(duration).isEqualTo(Duration.ofMinutes(30));
        }
    }

    // ==================== Count/List Executions Between Tests ====================

    @Nested
    @DisplayName("区间执行测试")
    class ExecutionsBetweenTests {

        @Test
        @DisplayName("countExecutionsBetween 正确计数")
        void should_count_executions_between() {
            ZonedDateTime from = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, UTC);
            ZonedDateTime to = ZonedDateTime.of(2026, 1, 8, 0, 0, 0, 0, UTC);
            long count = OpenCron.countExecutionsBetween("0 0 * * *", from, to);
            assertThat(count).isEqualTo(7);
        }

        @Test
        @DisplayName("executionsBetween 返回正确列表")
        void should_list_executions_between() {
            ZonedDateTime from = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, UTC);
            ZonedDateTime to = ZonedDateTime.of(2026, 1, 4, 0, 0, 0, 0, UTC);
            List<ZonedDateTime> list = OpenCron.executionsBetween("0 0 * * *", from, to);
            assertThat(list).hasSize(3);
        }
    }

    // ==================== Equivalence Tests ====================

    @Nested
    @DisplayName("等价性测试")
    class EquivalenceTests {

        @Test
        @DisplayName("@daily 等价于 0 0 * * *")
        void should_detect_equivalent() {
            assertThat(OpenCron.isEquivalent("@daily", "0 0 * * *")).isTrue();
        }

        @Test
        @DisplayName("不同表达式不等价")
        void should_detect_not_equivalent() {
            assertThat(OpenCron.isEquivalent("0 0 * * *", "0 1 * * *")).isFalse();
        }
    }

    // ==================== Explain Tests ====================

    @Nested
    @DisplayName("解释测试")
    class ExplainTests {

        @Test
        @DisplayName("explain 返回CronExplanation")
        void should_return_explanation() {
            ZonedDateTime from = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, UTC);
            CronExplanation explanation = OpenCron.explain("0 9 * * MON-FRI", from);
            assertThat(explanation).isNotNull();
            assertThat(explanation.expression()).isEqualTo("0 9 * * MON-FRI");
            assertThat(explanation.description()).isNotBlank();
            assertThat(explanation.nextExecutions()).isNotEmpty();
        }
    }

    // ==================== Describe with Locale Tests ====================

    @Nested
    @DisplayName("多语言描述测试")
    class DescribeLocaleTests {

        @Test
        @DisplayName("describe(expr, Locale.CHINESE) 返回中文")
        void should_describe_in_chinese() {
            String desc = OpenCron.describe("*/5 * * * *", Locale.CHINESE);
            assertThat(desc).containsPattern("[\\u4e00-\\u9fff]");
        }

        @Test
        @DisplayName("describe(expr, Locale.ENGLISH) 返回英文")
        void should_describe_in_english() {
            String desc = OpenCron.describe("*/5 * * * *", Locale.ENGLISH);
            assertThat(desc).containsIgnoringCase("5 minutes");
        }

        @Test
        @DisplayName("describe(expr) 默认英文")
        void should_describe_default_english() {
            String desc = OpenCron.describe("0 9 * * MON-FRI");
            assertThat(desc).containsIgnoringCase("Monday");
        }
    }

    // ==================== Stream Tests ====================

    @Nested
    @DisplayName("Stream 流式调度测试")
    class StreamTests {

        @Test
        @DisplayName("OpenCron.stream 返回正确元素")
        void should_return_correct_stream_elements() {
            ZonedDateTime from = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, UTC);
            List<ZonedDateTime> results = OpenCron.stream("0 0 * * *", from)
                    .limit(3).toList();

            assertThat(results).hasSize(3);
            assertThat(results.get(0)).isEqualTo(
                    ZonedDateTime.of(2026, 1, 2, 0, 0, 0, 0, UTC));
            assertThat(results.get(1)).isEqualTo(
                    ZonedDateTime.of(2026, 1, 3, 0, 0, 0, 0, UTC));
            assertThat(results.get(2)).isEqualTo(
                    ZonedDateTime.of(2026, 1, 4, 0, 0, 0, 0, UTC));
        }

        @Test
        @DisplayName("OpenCron.reverseStream 返回正确元素")
        void should_return_correct_reverse_stream_elements() {
            ZonedDateTime from = ZonedDateTime.of(2026, 1, 5, 12, 0, 0, 0, UTC);
            List<ZonedDateTime> results = OpenCron.reverseStream("0 0 * * *", from)
                    .limit(3).toList();

            assertThat(results).hasSize(3);
            assertThat(results.get(0)).isEqualTo(
                    ZonedDateTime.of(2026, 1, 5, 0, 0, 0, 0, UTC));
            assertThat(results.get(1)).isEqualTo(
                    ZonedDateTime.of(2026, 1, 4, 0, 0, 0, 0, UTC));
            assertThat(results.get(2)).isEqualTo(
                    ZonedDateTime.of(2026, 1, 3, 0, 0, 0, 0, UTC));
        }
    }

    // ==================== Filtered Scheduling Tests ====================

    @Nested
    @DisplayName("Filtered Scheduling 过滤调度测试")
    class FilteredSchedulingTests {

        @Test
        @DisplayName("OpenCron.nextExecution 配合过滤器")
        void should_next_execution_with_filter() {
            ZonedDateTime from = ZonedDateTime.of(2026, 1, 2, 12, 0, 0, 0, UTC); // Friday
            ZonedDateTime result = OpenCron.nextExecution("0 9 * * *", from,
                    t -> t.getDayOfWeek().getValue() <= 5);

            assertThat(result).isNotNull();
            // Should skip Sat (3rd) and Sun (4th), land on Monday (5th)
            assertThat(result).isEqualTo(
                    ZonedDateTime.of(2026, 1, 5, 9, 0, 0, 0, UTC));
        }

        @Test
        @DisplayName("OpenCron.previousExecution 配合过滤器")
        void should_previous_execution_with_filter() {
            ZonedDateTime from = ZonedDateTime.of(2026, 1, 5, 12, 0, 0, 0, UTC); // Monday
            ZonedDateTime result = OpenCron.previousExecution("0 9 * * *", from,
                    t -> t.getDayOfWeek().getValue() <= 5);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(
                    ZonedDateTime.of(2026, 1, 5, 9, 0, 0, 0, UTC));
        }
    }

    // ==================== Overlap Detection Tests ====================

    @Nested
    @DisplayName("Overlap Detection 重叠检测测试")
    class OverlapDetectionTests {

        @Test
        @DisplayName("OpenCron.nextOverlap 找到重叠时间")
        void should_find_next_overlap() {
            ZonedDateTime from = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, UTC);
            ZonedDateTime overlap = OpenCron.nextOverlap("0 0 * * *", "0 0 1 * *", from);

            assertThat(overlap).isNotNull();
            assertThat(overlap).isEqualTo(
                    ZonedDateTime.of(2026, 2, 1, 0, 0, 0, 0, UTC));
        }

        @Test
        @DisplayName("OpenCron.hasOverlap 检测范围内重叠")
        void should_detect_overlap_in_range() {
            ZonedDateTime from = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, UTC);
            ZonedDateTime to = ZonedDateTime.of(2026, 3, 1, 0, 0, 0, 0, UTC);

            assertThat(OpenCron.hasOverlap("0 0 * * *", "0 0 1 * *", from, to))
                    .isTrue();
            assertThat(OpenCron.hasOverlap("0 9 * * MON", "0 17 * * FRI", from, to))
                    .isFalse();
        }
    }
}
