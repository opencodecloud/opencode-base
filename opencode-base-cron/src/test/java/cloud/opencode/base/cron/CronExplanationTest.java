package cloud.opencode.base.cron;

import org.junit.jupiter.api.*;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CronExplanationTest Tests
 * CronExplanationTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.3
 */
@DisplayName("CronExplanation 测试")
class CronExplanationTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    private static ZonedDateTime zdt(int year, int month, int day, int hour, int minute) {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, UTC);
    }

    // ==================== Constructor Validation Tests ====================

    @Nested
    @DisplayName("构造器校验测试")
    class ConstructorValidationTests {

        @Test
        @DisplayName("null expression 应抛出 NullPointerException")
        void should_throw_npe_on_null_expression() {
            assertThatThrownBy(() -> new CronExplanation(
                    null, "desc", List.of(), Duration.ofHours(1)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("expression");
        }

        @Test
        @DisplayName("null description 应抛出 NullPointerException")
        void should_throw_npe_on_null_description() {
            assertThatThrownBy(() -> new CronExplanation(
                    "0 0 * * *", null, List.of(), Duration.ofHours(1)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("description");
        }

        @Test
        @DisplayName("null nextExecutions 应抛出 NullPointerException")
        void should_throw_npe_on_null_next_executions() {
            assertThatThrownBy(() -> new CronExplanation(
                    "0 0 * * *", "desc", null, Duration.ofHours(1)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("nextExecutions");
        }

        @Test
        @DisplayName("null estimatedInterval 应抛出 NullPointerException")
        void should_throw_npe_on_null_interval() {
            assertThatThrownBy(() -> new CronExplanation(
                    "0 0 * * *", "desc", List.of(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("estimatedInterval");
        }

        @Test
        @DisplayName("合法参数应正常创建")
        void should_create_with_valid_args() {
            List<ZonedDateTime> times = List.of(zdt(2026, 1, 2, 0, 0));
            CronExplanation explanation = new CronExplanation(
                    "0 0 * * *", "daily", times, Duration.ofHours(24));
            assertThat(explanation.expression()).isEqualTo("0 0 * * *");
            assertThat(explanation.description()).isEqualTo("daily");
            assertThat(explanation.nextExecutions()).hasSize(1);
            assertThat(explanation.estimatedInterval()).isEqualTo(Duration.ofHours(24));
        }
    }

    // ==================== Unmodifiable List Tests ====================

    @Nested
    @DisplayName("不可修改列表测试")
    class UnmodifiableListTests {

        @Test
        @DisplayName("nextExecutions 不可修改")
        void should_be_unmodifiable() {
            List<ZonedDateTime> mutable = new ArrayList<>();
            mutable.add(zdt(2026, 1, 2, 0, 0));
            CronExplanation explanation = new CronExplanation(
                    "0 0 * * *", "daily", mutable, Duration.ofHours(24));
            assertThatThrownBy(() -> explanation.nextExecutions().add(zdt(2026, 1, 3, 0, 0)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("修改原始列表不影响CronExplanation")
        void should_be_defensive_copy() {
            List<ZonedDateTime> mutable = new ArrayList<>();
            mutable.add(zdt(2026, 1, 2, 0, 0));
            CronExplanation explanation = new CronExplanation(
                    "0 0 * * *", "daily", mutable, Duration.ofHours(24));
            mutable.add(zdt(2026, 1, 3, 0, 0));
            assertThat(explanation.nextExecutions()).hasSize(1);
        }
    }

    // ==================== toString Tests ====================

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString 包含表达式和描述")
        void should_contain_expression_and_description() {
            CronExplanation explanation = new CronExplanation(
                    "0 0 * * *", "At 00:00",
                    List.of(zdt(2026, 1, 2, 0, 0)),
                    Duration.ofHours(24));
            String str = explanation.toString();
            assertThat(str).contains("Expression : 0 0 * * *");
            assertThat(str).contains("Description: At 00:00");
            assertThat(str).contains("Interval   : PT24H");
            assertThat(str).contains("Next executions:");
            assertThat(str).contains("1.");
        }

        @Test
        @DisplayName("toString 空列表显示 (none)")
        void should_show_none_for_empty_list() {
            CronExplanation explanation = new CronExplanation(
                    "0 0 29 2 *", "Feb 29", List.of(), Duration.ZERO);
            String str = explanation.toString();
            assertThat(str).contains("(none)");
        }
    }

    // ==================== Equals/HashCode Tests ====================

    @Nested
    @DisplayName("equals/hashCode 测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同参数的record应相等")
        void should_be_equal_for_same_values() {
            List<ZonedDateTime> times = List.of(zdt(2026, 1, 2, 0, 0));
            CronExplanation a = new CronExplanation("0 0 * * *", "daily", times, Duration.ofHours(24));
            CronExplanation b = new CronExplanation("0 0 * * *", "daily", times, Duration.ofHours(24));
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不同参数的record不应相等")
        void should_not_be_equal_for_different_values() {
            List<ZonedDateTime> times = List.of(zdt(2026, 1, 2, 0, 0));
            CronExplanation a = new CronExplanation("0 0 * * *", "daily", times, Duration.ofHours(24));
            CronExplanation b = new CronExplanation("0 1 * * *", "hourly", times, Duration.ofHours(1));
            assertThat(a).isNotEqualTo(b);
        }
    }
}
