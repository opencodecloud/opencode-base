package cloud.opencode.base.cache.protection;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * CircuitBreaker Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class CircuitBreakerTest {

    @Test
    void shouldCreateWithDefaultConfig() {
        CircuitBreaker breaker = CircuitBreaker.create();

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldCreateWithCustomConfig() {
        CircuitBreaker.Config config = new CircuitBreaker.Config(3, Duration.ofSeconds(10), 2, 0.3);
        CircuitBreaker breaker = CircuitBreaker.create(config);

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldCreateConfigWithBuilder() {
        CircuitBreaker.Config config = CircuitBreaker.Config.builder()
                .failureThreshold(10)
                .openDuration(Duration.ofMinutes(1))
                .halfOpenRequests(5)
                .failureRateThreshold(0.5)
                .build();

        assertThat(config.failureThreshold()).isEqualTo(10);
        assertThat(config.openDuration()).isEqualTo(Duration.ofMinutes(1));
        assertThat(config.halfOpenRequests()).isEqualTo(5);
        assertThat(config.failureRateThreshold()).isEqualTo(0.5);
    }

    @Test
    void shouldExecuteSuccessfully() {
        CircuitBreaker breaker = CircuitBreaker.create();

        String result = breaker.execute(() -> "success");

        assertThat(result).isEqualTo("success");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldPropagateException() {
        CircuitBreaker breaker = CircuitBreaker.create();

        assertThatThrownBy(() ->
                breaker.execute(() -> {
                    throw new RuntimeException("Test error");
                })
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Test error");
    }

    @Test
    void shouldOpenAfterFailureThreshold() {
        CircuitBreaker.Config config = new CircuitBreaker.Config(3, Duration.ofSeconds(30), 1, 0.5);
        CircuitBreaker breaker = CircuitBreaker.create(config);

        for (int i = 0; i < 3; i++) {
            try {
                breaker.execute(() -> {
                    throw new RuntimeException("Failure");
                });
            } catch (RuntimeException e) {
                // Expected
            }
        }

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldRejectWhenOpen() {
        CircuitBreaker.Config config = new CircuitBreaker.Config(1, Duration.ofSeconds(30), 1, 0.5);
        CircuitBreaker breaker = CircuitBreaker.create(config);

        // Trigger open
        try {
            breaker.execute(() -> {
                throw new RuntimeException("Failure");
            });
        } catch (RuntimeException e) {
            // Expected
        }

        // Should reject
        assertThatThrownBy(() -> breaker.execute(() -> "success"))
                .isInstanceOf(CircuitBreaker.CircuitBreakerOpenException.class)
                .hasMessageContaining("open");
    }

    @Test
    void shouldUseFallbackWhenOpen() {
        CircuitBreaker.Config config = new CircuitBreaker.Config(1, Duration.ofSeconds(30), 1, 0.5);
        CircuitBreaker breaker = CircuitBreaker.create(config);

        // Trigger open
        try {
            breaker.execute(() -> {
                throw new RuntimeException("Failure");
            });
        } catch (RuntimeException e) {
            // Expected
        }

        // Use fallback
        String result = breaker.execute(() -> "primary", () -> "fallback");

        assertThat(result).isEqualTo("fallback");
    }

    @Test
    void shouldTransitionToHalfOpenAfterDuration() throws Exception {
        CircuitBreaker.Config config = new CircuitBreaker.Config(1, Duration.ofMillis(100), 1, 0.5);
        CircuitBreaker breaker = CircuitBreaker.create(config);

        // Trigger open
        try {
            breaker.execute(() -> {
                throw new RuntimeException("Failure");
            });
        } catch (RuntimeException e) {
            // Expected
        }

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Thread.sleep(150);

        // Next request should transition to HALF_OPEN
        String result = breaker.execute(() -> "success");

        assertThat(result).isEqualTo("success");
    }

    @Test
    void shouldCloseAfterSuccessfulHalfOpen() throws Exception {
        CircuitBreaker.Config config = new CircuitBreaker.Config(1, Duration.ofMillis(50), 2, 0.5);
        CircuitBreaker breaker = CircuitBreaker.create(config);

        // Trigger open
        try {
            breaker.execute(() -> {
                throw new RuntimeException("Failure");
            });
        } catch (RuntimeException e) {
            // Expected
        }

        Thread.sleep(100);

        // Successful requests in half-open
        breaker.execute(() -> "success");
        breaker.execute(() -> "success");

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldReopenOnFailureInHalfOpen() throws Exception {
        CircuitBreaker.Config config = new CircuitBreaker.Config(1, Duration.ofMillis(50), 2, 0.5);
        CircuitBreaker breaker = CircuitBreaker.create(config);

        // Trigger open
        try {
            breaker.execute(() -> {
                throw new RuntimeException("Failure");
            });
        } catch (RuntimeException e) {
            // Expected
        }

        Thread.sleep(100);

        // Failure in half-open should reopen
        try {
            breaker.execute(() -> {
                throw new RuntimeException("Failure");
            });
        } catch (RuntimeException e) {
            // Expected
        }

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldManuallyOpen() {
        CircuitBreaker breaker = CircuitBreaker.create();

        breaker.open();

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldManuallyClose() {
        CircuitBreaker.Config config = new CircuitBreaker.Config(1, Duration.ofSeconds(30), 1, 0.5);
        CircuitBreaker breaker = CircuitBreaker.create(config);

        // Trigger open
        try {
            breaker.execute(() -> {
                throw new RuntimeException("Failure");
            });
        } catch (RuntimeException e) {
            // Expected
        }

        breaker.close();

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldReset() {
        CircuitBreaker breaker = CircuitBreaker.create();

        breaker.execute(() -> "success");
        breaker.reset();

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldExecuteAsync() throws Exception {
        CircuitBreaker breaker = CircuitBreaker.create();

        CompletableFuture<String> future = breaker.executeAsync(() ->
                CompletableFuture.supplyAsync(() -> "async-success"));

        assertThat(future.get()).isEqualTo("async-success");
    }

    @Test
    void shouldRecordAsyncFailure() throws Exception {
        CircuitBreaker.Config config = new CircuitBreaker.Config(1, Duration.ofSeconds(30), 1, 0.5);
        CircuitBreaker breaker = CircuitBreaker.create(config);

        CompletableFuture<String> future = breaker.executeAsync(() ->
                CompletableFuture.failedFuture(new RuntimeException("Async failure")));

        try {
            future.get();
        } catch (Exception e) {
            // Expected
        }

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldRejectAsyncWhenOpen() {
        CircuitBreaker.Config config = new CircuitBreaker.Config(1, Duration.ofSeconds(30), 1, 0.5);
        CircuitBreaker breaker = CircuitBreaker.create(config);

        // Trigger open
        breaker.open();

        CompletableFuture<String> future = breaker.executeAsync(() ->
                CompletableFuture.supplyAsync(() -> "success"));

        assertThat(future).isCompletedExceptionally();
    }

    @Test
    void shouldHaveStatesEnum() {
        assertThat(CircuitBreaker.State.values()).containsExactly(
                CircuitBreaker.State.CLOSED,
                CircuitBreaker.State.OPEN,
                CircuitBreaker.State.HALF_OPEN
        );
    }
}
