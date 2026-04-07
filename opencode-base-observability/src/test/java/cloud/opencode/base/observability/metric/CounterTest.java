package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Counter} via {@link MetricRegistry}.
 */
@DisplayName("Counter")
class CounterTest {

    private MetricRegistry registry;
    private Counter counter;

    @BeforeEach
    void setUp() {
        registry = MetricRegistry.create();
        counter = registry.counter("test.counter");
    }

    @Nested
    @DisplayName("increment()")
    class Increment {

        @Test
        @DisplayName("should increment by one")
        void shouldIncrementByOne() {
            counter.increment();
            assertThat(counter.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("should increment multiple times")
        void shouldIncrementMultipleTimes() {
            counter.increment();
            counter.increment();
            counter.increment();
            assertThat(counter.count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("increment(amount)")
    class IncrementAmount {

        @Test
        @DisplayName("should increment by given amount")
        void shouldIncrementByAmount() {
            counter.increment(10);
            assertThat(counter.count()).isEqualTo(10);
        }

        @Test
        @DisplayName("should increment by zero")
        void shouldIncrementByZero() {
            counter.increment(0);
            assertThat(counter.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("should throw on negative amount")
        void shouldThrowOnNegativeAmount() {
            assertThatThrownBy(() -> counter.increment(-1))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("must not be negative");
        }
    }

    @Nested
    @DisplayName("count()")
    class Count {

        @Test
        @DisplayName("should start at zero")
        void shouldStartAtZero() {
            assertThat(counter.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("reset()")
    class Reset {

        @Test
        @DisplayName("should reset to zero")
        void shouldResetToZero() {
            counter.increment(42);
            counter.reset();
            assertThat(counter.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("id()")
    class Id {

        @Test
        @DisplayName("should return correct metric id")
        void shouldReturnCorrectId() {
            assertThat(counter.id().name()).isEqualTo("test.counter");
            assertThat(counter.id().tags()).isEmpty();
        }

        @Test
        @DisplayName("should include tags in id")
        void shouldIncludeTagsInId() {
            Counter tagged = registry.counter("tagged", Tag.of("env", "prod"));
            assertThat(tagged.id().tags()).hasSize(1);
            assertThat(tagged.id().tags().getFirst().key()).isEqualTo("env");
        }
    }

    @Nested
    @DisplayName("Concurrency")
    class Concurrency {

        @Test
        @DisplayName("should be thread-safe under concurrent increments")
        void shouldBeThreadSafe() throws InterruptedException {
            int threads = 8;
            int incrementsPerThread = 10_000;
            CountDownLatch latch = new CountDownLatch(threads);

            try (var executor = Executors.newFixedThreadPool(threads)) {
                for (int t = 0; t < threads; t++) {
                    executor.submit(() -> {
                        for (int i = 0; i < incrementsPerThread; i++) {
                            counter.increment();
                        }
                        latch.countDown();
                    });
                }
                latch.await();
            }

            assertThat(counter.count()).isEqualTo((long) threads * incrementsPerThread);
        }
    }
}
