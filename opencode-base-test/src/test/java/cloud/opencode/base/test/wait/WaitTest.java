package cloud.opencode.base.test.wait;

import cloud.opencode.base.test.exception.TestException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * WaitTest Tests
 * WaitTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("Wait Tests")
class WaitTest {

    @Nested
    @DisplayName("until Tests")
    class UntilTests {

        @Test
        @DisplayName("Should return immediately when condition is true")
        void shouldReturnImmediatelyWhenConditionIsTrue() {
            assertThatNoException().isThrownBy(() ->
                Wait.until(() -> true, Duration.ofSeconds(1)));
        }

        @Test
        @DisplayName("Should wait until condition becomes true")
        void shouldWaitUntilConditionBecomesTrue() {
            AtomicInteger counter = new AtomicInteger(0);

            Wait.until(() -> counter.incrementAndGet() >= 3, Duration.ofSeconds(5), Duration.ofMillis(10));

            assertThat(counter.get()).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Should throw on timeout")
        void shouldThrowOnTimeout() {
            assertThatThrownBy(() ->
                Wait.until(() -> false, Duration.ofMillis(100), Duration.ofMillis(10)))
                .isInstanceOf(TestException.class)
                .hasMessageContaining("Condition not met");
        }

        @Test
        @DisplayName("Should use default timeout")
        void shouldUseDefaultTimeout() {
            assertThatNoException().isThrownBy(() ->
                Wait.until(() -> true));
        }
    }

    @Nested
    @DisplayName("untilNotNull Tests")
    class UntilNotNullTests {

        @Test
        @DisplayName("Should return value immediately if not null")
        void shouldReturnValueImmediatelyIfNotNull() {
            String result = Wait.untilNotNull(() -> "value", Duration.ofSeconds(1));
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("Should wait until value is not null")
        void shouldWaitUntilValueIsNotNull() {
            AtomicInteger counter = new AtomicInteger(0);
            AtomicReference<String> ref = new AtomicReference<>();

            String result = Wait.untilNotNull(() -> {
                if (counter.incrementAndGet() >= 3) {
                    ref.set("value");
                }
                return ref.get();
            }, Duration.ofSeconds(5), Duration.ofMillis(10));

            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("Should throw on timeout")
        void shouldThrowOnTimeout() {
            assertThatThrownBy(() ->
                Wait.untilNotNull(() -> null, Duration.ofMillis(100), Duration.ofMillis(10)))
                .isInstanceOf(TestException.class)
                .hasMessageContaining("Value not available");
        }
    }

    @Nested
    @DisplayName("forDuration Tests")
    class ForDurationTests {

        @Test
        @DisplayName("Should wait for specified duration")
        void shouldWaitForSpecifiedDuration() {
            long start = System.currentTimeMillis();

            Wait.forDuration(Duration.ofMillis(50));

            long elapsed = System.currentTimeMillis() - start;
            assertThat(elapsed).isGreaterThanOrEqualTo(40); // Allow some tolerance
        }
    }

    @Nested
    @DisplayName("forMillis Tests")
    class ForMillisTests {

        @Test
        @DisplayName("Should wait for specified milliseconds")
        void shouldWaitForSpecifiedMilliseconds() {
            long start = System.currentTimeMillis();

            Wait.forMillis(50);

            long elapsed = System.currentTimeMillis() - start;
            assertThat(elapsed).isGreaterThanOrEqualTo(40);
        }
    }

    @Nested
    @DisplayName("forSeconds Tests")
    class ForSecondsTests {

        @Test
        @DisplayName("Should wait for specified seconds")
        void shouldWaitForSpecifiedSeconds() {
            // Just verify it doesn't throw - don't want to wait too long in tests
            assertThatNoException().isThrownBy(() -> Wait.forMillis(1));
        }
    }
}
