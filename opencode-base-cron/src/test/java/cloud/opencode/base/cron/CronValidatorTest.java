package cloud.opencode.base.cron;

import cloud.opencode.base.cron.exception.OpenCronException;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * CronValidatorTest Tests
 * CronValidatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.0
 */
@DisplayName("CronValidator 测试")
class CronValidatorTest {

    @Nested
    @DisplayName("validate(String) 测试")
    class ValidateTests {

        @Test
        @DisplayName("有效表达式不抛异常")
        void should_pass_valid_expression() {
            assertThatNoException().isThrownBy(() -> CronValidator.validate("0 0 * * *"));
        }

        @Test
        @DisplayName("带别名的有效表达式不抛异常")
        void should_pass_with_aliases() {
            assertThatNoException().isThrownBy(() -> CronValidator.validate("0 9 * * MON-FRI"));
        }

        @Test
        @DisplayName("宏表达式不抛异常")
        void should_pass_macros() {
            assertThatNoException().isThrownBy(() -> CronValidator.validate("@daily"));
            assertThatNoException().isThrownBy(() -> CronValidator.validate("@yearly"));
        }

        @Test
        @DisplayName("特殊字符表达式不抛异常")
        void should_pass_special_chars() {
            assertThatNoException().isThrownBy(() -> CronValidator.validate("0 0 L * *"));
            assertThatNoException().isThrownBy(() -> CronValidator.validate("0 0 15W * *"));
            assertThatNoException().isThrownBy(() -> CronValidator.validate("0 0 * * 5#3"));
        }

        @Test
        @DisplayName("无效表达式抛出OpenCronException")
        void should_throw_on_invalid() {
            assertThatThrownBy(() -> CronValidator.validate("invalid"))
                    .isInstanceOf(OpenCronException.class);
        }

        @Test
        @DisplayName("null表达式抛出NullPointerException")
        void should_throw_on_null() {
            assertThatThrownBy(() -> CronValidator.validate(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("validate(String, Duration) 测试")
    class ValidateWithIntervalTests {

        @Test
        @DisplayName("间隔足够时不抛异常")
        void should_pass_when_interval_sufficient() {
            assertThatNoException().isThrownBy(
                    () -> CronValidator.validate("0 0 * * *", Duration.ofMinutes(1)));
        }

        @Test
        @DisplayName("间隔太短时抛出异常")
        void should_throw_when_interval_too_short() {
            // Every second, but minimum 5 seconds
            assertThatThrownBy(
                    () -> CronValidator.validate("* * * * * *", Duration.ofSeconds(5)))
                    .isInstanceOf(OpenCronException.class)
                    .hasMessageContaining("shorter than minimum");
        }

        @Test
        @DisplayName("秒级间隔在允许范围内")
        void should_pass_second_interval_within_limit() {
            assertThatNoException().isThrownBy(
                    () -> CronValidator.validate("*/10 * * * * *", Duration.ofSeconds(5)));
        }
    }

    @Nested
    @DisplayName("isValid 测试")
    class IsValidTests {

        @Test
        @DisplayName("有效表达式返回true")
        void should_return_true_for_valid() {
            assertThat(CronValidator.isValid("0 0 * * *")).isTrue();
            assertThat(CronValidator.isValid("@daily")).isTrue();
            assertThat(CronValidator.isValid("0 9 * * MON-FRI")).isTrue();
            assertThat(CronValidator.isValid("0 0 L * *")).isTrue();
        }

        @Test
        @DisplayName("无效表达式返回false")
        void should_return_false_for_invalid() {
            assertThat(CronValidator.isValid("invalid")).isFalse();
            assertThat(CronValidator.isValid("")).isFalse();
            assertThat(CronValidator.isValid("60 * * * *")).isFalse();
            assertThat(CronValidator.isValid("* * * * * * *")).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void should_return_false_for_null() {
            assertThat(CronValidator.isValid(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("getEstimatedInterval 测试")
    class GetEstimatedIntervalTests {

        @Test
        @DisplayName("每日表达式间隔24小时")
        void should_return_24h_for_daily() {
            Duration interval = CronValidator.getEstimatedInterval("0 0 * * *");
            assertThat(interval).isEqualTo(Duration.ofHours(24));
        }

        @Test
        @DisplayName("每小时表达式间隔1小时")
        void should_return_1h_for_hourly() {
            Duration interval = CronValidator.getEstimatedInterval("0 * * * *");
            assertThat(interval).isEqualTo(Duration.ofHours(1));
        }

        @Test
        @DisplayName("每分钟表达式间隔1分钟")
        void should_return_1min_for_every_minute() {
            Duration interval = CronValidator.getEstimatedInterval("* * * * *");
            assertThat(interval).isEqualTo(Duration.ofMinutes(1));
        }

        @Test
        @DisplayName("每5秒表达式间隔5秒")
        void should_return_5s_for_every_5_seconds() {
            Duration interval = CronValidator.getEstimatedInterval("*/5 * * * * *");
            assertThat(interval).isEqualTo(Duration.ofSeconds(5));
        }
    }
}
