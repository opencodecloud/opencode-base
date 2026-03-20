package cloud.opencode.base.cache.protection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BackoffStrategy")
class BackoffStrategyTest {

    private static final Duration INITIAL = Duration.ofMillis(100);
    private static final Duration MAX = Duration.ofSeconds(10);
    private static final double MULTIPLIER = 2.0;

    @Nested
    @DisplayName("FIXED")
    class FixedStrategy {

        @Test
        @DisplayName("should always return initial delay")
        void shouldReturnInitialDelay() {
            Duration delay1 = BackoffStrategy.FIXED.calculateDelay(1, INITIAL, MAX, MULTIPLIER);
            Duration delay5 = BackoffStrategy.FIXED.calculateDelay(5, INITIAL, MAX, MULTIPLIER);
            assertThat(delay1).isEqualTo(INITIAL);
            assertThat(delay5).isEqualTo(INITIAL);
        }
    }

    @Nested
    @DisplayName("LINEAR")
    class LinearStrategy {

        @Test
        @DisplayName("should grow linearly with attempt number")
        void shouldGrowLinearly() {
            Duration delay1 = BackoffStrategy.LINEAR.calculateDelay(1, INITIAL, MAX, MULTIPLIER);
            Duration delay2 = BackoffStrategy.LINEAR.calculateDelay(2, INITIAL, MAX, MULTIPLIER);
            Duration delay3 = BackoffStrategy.LINEAR.calculateDelay(3, INITIAL, MAX, MULTIPLIER);
            assertThat(delay1.toMillis()).isEqualTo(100);
            assertThat(delay2.toMillis()).isEqualTo(200);
            assertThat(delay3.toMillis()).isEqualTo(300);
        }

        @Test
        @DisplayName("should cap at max delay")
        void shouldCapAtMax() {
            Duration delay = BackoffStrategy.LINEAR.calculateDelay(1000, INITIAL, MAX, MULTIPLIER);
            assertThat(delay).isEqualTo(MAX);
        }

        @Test
        @DisplayName("should return zero delay for attempt 0")
        void shouldReturnZeroForAttemptZero() {
            Duration delay = BackoffStrategy.LINEAR.calculateDelay(0, INITIAL, MAX, MULTIPLIER);
            assertThat(delay.toMillis()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("EXPONENTIAL")
    class ExponentialStrategy {

        @Test
        @DisplayName("should grow exponentially with attempt number")
        void shouldGrowExponentially() {
            Duration delay1 = BackoffStrategy.EXPONENTIAL.calculateDelay(1, INITIAL, MAX, MULTIPLIER);
            Duration delay2 = BackoffStrategy.EXPONENTIAL.calculateDelay(2, INITIAL, MAX, MULTIPLIER);
            Duration delay3 = BackoffStrategy.EXPONENTIAL.calculateDelay(3, INITIAL, MAX, MULTIPLIER);
            assertThat(delay1.toMillis()).isEqualTo(100); // 100 * 2^0
            assertThat(delay2.toMillis()).isEqualTo(200); // 100 * 2^1
            assertThat(delay3.toMillis()).isEqualTo(400); // 100 * 2^2
        }

        @Test
        @DisplayName("should cap at max delay")
        void shouldCapAtMax() {
            Duration delay = BackoffStrategy.EXPONENTIAL.calculateDelay(100, INITIAL, MAX, MULTIPLIER);
            assertThat(delay).isEqualTo(MAX);
        }

        @Test
        @DisplayName("should handle overflow gracefully")
        void shouldHandleOverflow() {
            Duration delay = BackoffStrategy.EXPONENTIAL.calculateDelay(Integer.MAX_VALUE, INITIAL, MAX, MULTIPLIER);
            assertThat(delay).isEqualTo(MAX);
        }
    }

    @Nested
    @DisplayName("RANDOM")
    class RandomStrategy {

        @Test
        @DisplayName("should return delay between initial and max")
        void shouldReturnDelayInRange() {
            for (int i = 0; i < 10; i++) {
                Duration delay = BackoffStrategy.RANDOM.calculateDelay(1, INITIAL, MAX, MULTIPLIER);
                assertThat(delay.toMillis()).isBetween(INITIAL.toMillis(), MAX.toMillis());
            }
        }
    }

    @Nested
    @DisplayName("EXPONENTIAL_JITTER")
    class ExponentialJitterStrategy {

        @Test
        @DisplayName("should return delay between 0 and exponential cap")
        void shouldReturnJitteredDelay() {
            for (int i = 0; i < 10; i++) {
                Duration delay = BackoffStrategy.EXPONENTIAL_JITTER.calculateDelay(1, INITIAL, MAX, MULTIPLIER);
                assertThat(delay.toMillis()).isBetween(0L, INITIAL.toMillis());
            }
        }

        @Test
        @DisplayName("should cap at max delay for large attempts")
        void shouldCapForLargeAttempts() {
            for (int i = 0; i < 10; i++) {
                Duration delay = BackoffStrategy.EXPONENTIAL_JITTER.calculateDelay(100, INITIAL, MAX, MULTIPLIER);
                assertThat(delay.toMillis()).isBetween(0L, MAX.toMillis());
            }
        }

        @Test
        @DisplayName("should handle overflow gracefully")
        void shouldHandleOverflow() {
            Duration delay = BackoffStrategy.EXPONENTIAL_JITTER.calculateDelay(Integer.MAX_VALUE, INITIAL, MAX, MULTIPLIER);
            assertThat(delay.toMillis()).isBetween(0L, MAX.toMillis());
        }
    }

    @Nested
    @DisplayName("enum values")
    class EnumValues {

        @Test
        @DisplayName("should have all five strategies")
        void shouldHaveAllStrategies() {
            assertThat(BackoffStrategy.values()).hasSize(5);
            assertThat(BackoffStrategy.valueOf("FIXED")).isEqualTo(BackoffStrategy.FIXED);
            assertThat(BackoffStrategy.valueOf("LINEAR")).isEqualTo(BackoffStrategy.LINEAR);
            assertThat(BackoffStrategy.valueOf("EXPONENTIAL")).isEqualTo(BackoffStrategy.EXPONENTIAL);
            assertThat(BackoffStrategy.valueOf("RANDOM")).isEqualTo(BackoffStrategy.RANDOM);
            assertThat(BackoffStrategy.valueOf("EXPONENTIAL_JITTER")).isEqualTo(BackoffStrategy.EXPONENTIAL_JITTER);
        }
    }
}
