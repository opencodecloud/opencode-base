package cloud.opencode.base.test.wait;

import cloud.opencode.base.test.exception.TestException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * PollerTest Tests
 * PollerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("Poller Tests")
class PollerTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("await should create new poller")
        void awaitShouldCreateNewPoller() {
            Poller poller = Poller.await();
            assertThat(poller).isNotNull();
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("timeout should set timeout duration")
        void timeoutShouldSetTimeoutDuration() {
            Poller poller = Poller.await()
                .timeout(Duration.ofMillis(100))
                .pollInterval(Duration.ofMillis(10));

            assertThatThrownBy(() -> poller.until(() -> false))
                .isInstanceOf(TestException.class);
        }

        @Test
        @DisplayName("pollInterval should set poll interval")
        void pollIntervalShouldSetPollInterval() {
            AtomicInteger counter = new AtomicInteger(0);
            long startTime = System.currentTimeMillis();

            Poller.await()
                .timeout(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(50))
                .until(() -> counter.incrementAndGet() >= 3);

            long elapsed = System.currentTimeMillis() - startTime;
            // Should have waited at least 2 poll intervals (100ms)
            assertThat(elapsed).isGreaterThanOrEqualTo(80);
        }

        @Test
        @DisplayName("describedAs should set description for error messages")
        void describedAsShouldSetDescriptionForErrorMessages() {
            assertThatThrownBy(() ->
                Poller.await()
                    .timeout(Duration.ofMillis(100))
                    .pollInterval(Duration.ofMillis(10))
                    .describedAs("my condition")
                    .until(() -> false))
                .isInstanceOf(TestException.class)
                .hasMessageContaining("my condition");
        }
    }

    @Nested
    @DisplayName("until BooleanSupplier Tests")
    class UntilBooleanSupplierTests {

        @Test
        @DisplayName("Should return immediately when condition is true")
        void shouldReturnImmediatelyWhenConditionIsTrue() {
            assertThatNoException().isThrownBy(() ->
                Poller.await()
                    .timeout(Duration.ofSeconds(1))
                    .until(() -> true));
        }

        @Test
        @DisplayName("Should wait until condition becomes true")
        void shouldWaitUntilConditionBecomesTrue() {
            AtomicInteger counter = new AtomicInteger(0);

            Poller.await()
                .timeout(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(10))
                .until(() -> counter.incrementAndGet() >= 3);

            assertThat(counter.get()).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Should throw on timeout")
        void shouldThrowOnTimeout() {
            assertThatThrownBy(() ->
                Poller.await()
                    .timeout(Duration.ofMillis(100))
                    .pollInterval(Duration.ofMillis(10))
                    .until(() -> false))
                .isInstanceOf(TestException.class)
                .hasMessageContaining("not met");
        }

        @Test
        @DisplayName("Should continue polling on exception")
        void shouldContinuePollingOnException() {
            AtomicInteger counter = new AtomicInteger(0);

            Poller.await()
                .timeout(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(10))
                .until(() -> {
                    int count = counter.incrementAndGet();
                    if (count < 3) {
                        throw new RuntimeException("not ready");
                    }
                    return true;
                });

            assertThat(counter.get()).isGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("until with Predicate Tests")
    class UntilWithPredicateTests {

        @Test
        @DisplayName("Should return value when predicate matches")
        void shouldReturnValueWhenPredicateMatches() {
            AtomicInteger counter = new AtomicInteger(0);

            Integer result = Poller.await()
                .timeout(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(10))
                .until(counter::incrementAndGet, v -> v >= 3);

            assertThat(result).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Should throw on timeout with last value")
        void shouldThrowOnTimeoutWithLastValue() {
            AtomicInteger counter = new AtomicInteger(0);

            assertThatThrownBy(() ->
                Poller.await()
                    .timeout(Duration.ofMillis(100))
                    .pollInterval(Duration.ofMillis(10))
                    .until(() -> counter.incrementAndGet(), v -> v > 1000))
                .isInstanceOf(TestException.class)
                .hasMessageContaining("last value:");
        }
    }

    @Nested
    @DisplayName("untilNotNull Tests")
    class UntilNotNullTests {

        @Test
        @DisplayName("Should return value when not null")
        void shouldReturnValueWhenNotNull() {
            String result = Poller.await()
                .timeout(Duration.ofSeconds(1))
                .untilNotNull(() -> "value");

            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("Should wait until value is not null")
        void shouldWaitUntilValueIsNotNull() {
            AtomicReference<String> ref = new AtomicReference<>();
            AtomicInteger counter = new AtomicInteger(0);

            String result = Poller.await()
                .timeout(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(10))
                .untilNotNull(() -> {
                    if (counter.incrementAndGet() >= 3) {
                        ref.set("value");
                    }
                    return ref.get();
                });

            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("Should throw on timeout")
        void shouldThrowOnTimeout() {
            assertThatThrownBy(() ->
                Poller.await()
                    .timeout(Duration.ofMillis(100))
                    .pollInterval(Duration.ofMillis(10))
                    .untilNotNull(() -> null))
                .isInstanceOf(TestException.class);
        }
    }

    @Nested
    @DisplayName("untilEquals Tests")
    class UntilEqualsTests {

        @Test
        @DisplayName("Should return value when equals expected")
        void shouldReturnValueWhenEqualsExpected() {
            AtomicInteger counter = new AtomicInteger(0);

            Integer result = Poller.await()
                .timeout(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(10))
                .untilEquals(counter::incrementAndGet, 5);

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Should throw on timeout")
        void shouldThrowOnTimeout() {
            assertThatThrownBy(() ->
                Poller.await()
                    .timeout(Duration.ofMillis(100))
                    .pollInterval(Duration.ofMillis(10))
                    .untilEquals(() -> "actual", "expected"))
                .isInstanceOf(TestException.class);
        }
    }
}
