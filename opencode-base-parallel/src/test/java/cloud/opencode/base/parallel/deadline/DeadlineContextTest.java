package cloud.opencode.base.parallel.deadline;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link DeadlineContext}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("DeadlineContext")
class DeadlineContextTest {

    @Nested
    @DisplayName("isBound()")
    class IsBoundTests {

        @Test
        @DisplayName("should return false when no deadline is bound")
        void shouldReturnFalseWhenNotBound() {
            assertThat(DeadlineContext.isBound()).isFalse();
        }

        @Test
        @DisplayName("should return true inside withDeadline scope")
        void shouldReturnTrueInsideScope() {
            Instant deadline = Instant.now().plusSeconds(60);
            DeadlineContext.withDeadline(deadline, () -> {
                assertThat(DeadlineContext.isBound()).isTrue();
            });
        }

        @Test
        @DisplayName("should return false after withDeadline scope exits")
        void shouldReturnFalseAfterScopeExits() {
            Instant deadline = Instant.now().plusSeconds(60);
            DeadlineContext.withDeadline(deadline, () -> {
                // inside scope
            });
            assertThat(DeadlineContext.isBound()).isFalse();
        }
    }

    @Nested
    @DisplayName("current()")
    class CurrentTests {

        @Test
        @DisplayName("should return empty when no deadline is bound")
        void shouldReturnEmptyWhenNotBound() {
            assertThat(DeadlineContext.current()).isEmpty();
        }

        @Test
        @DisplayName("should return the bound deadline inside scope")
        void shouldReturnBoundDeadline() {
            Instant deadline = Instant.now().plusSeconds(30);
            DeadlineContext.withDeadline(deadline, () -> {
                Optional<Instant> current = DeadlineContext.current();
                assertThat(current).isPresent();
                assertThat(current.get()).isEqualTo(deadline);
            });
        }
    }

    @Nested
    @DisplayName("withDeadline(Instant, Runnable)")
    class WithDeadlineRunnable {

        @Test
        @DisplayName("should execute the action")
        void shouldExecuteAction() {
            boolean[] executed = {false};
            Instant deadline = Instant.now().plusSeconds(10);
            DeadlineContext.withDeadline(deadline, (Runnable) () -> executed[0] = true);
            assertThat(executed[0]).isTrue();
        }

        @Test
        @DisplayName("should throw for null deadline")
        void shouldThrowForNullDeadline() {
            assertThatThrownBy(() -> DeadlineContext.withDeadline(null, () -> {}))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw for null action")
        void shouldThrowForNullAction() {
            assertThatThrownBy(() -> DeadlineContext.withDeadline(Instant.now(), (Runnable) null))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("withDeadline(Instant, Callable)")
    class WithDeadlineCallable {

        @Test
        @DisplayName("should return the callable result")
        void shouldReturnResult() throws Exception {
            Instant deadline = Instant.now().plusSeconds(10);
            String result = DeadlineContext.withDeadline(deadline, () -> "hello");
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("should propagate callable exceptions")
        void shouldPropagateExceptions() {
            Instant deadline = Instant.now().plusSeconds(10);
            assertThatThrownBy(() -> DeadlineContext.withDeadline(deadline,
                    (Callable<String>) () -> { throw new IllegalStateException("boom"); }))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("should throw for null deadline")
        void shouldThrowForNullDeadline() {
            assertThatThrownBy(() -> DeadlineContext.withDeadline(null, (Callable<String>) () -> "x"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw for null action")
        void shouldThrowForNullAction() {
            assertThatThrownBy(() -> DeadlineContext.withDeadline(Instant.now(), (Callable<String>) null))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("withTimeout(Duration, Runnable)")
    class WithTimeoutRunnable {

        @Test
        @DisplayName("should execute the action")
        void shouldExecuteAction() {
            boolean[] executed = {false};
            DeadlineContext.withTimeout(Duration.ofSeconds(5), (Runnable) () -> executed[0] = true);
            assertThat(executed[0]).isTrue();
        }

        @Test
        @DisplayName("should bind a deadline in the future")
        void shouldBindFutureDeadline() {
            Instant before = Instant.now();
            DeadlineContext.withTimeout(Duration.ofSeconds(10), () -> {
                Optional<Instant> current = DeadlineContext.current();
                assertThat(current).isPresent();
                assertThat(current.get()).isAfter(before);
                assertThat(current.get()).isBefore(before.plusSeconds(15));
            });
        }
    }

    @Nested
    @DisplayName("withTimeout(Duration, Callable)")
    class WithTimeoutCallable {

        @Test
        @DisplayName("should return the callable result")
        void shouldReturnResult() throws Exception {
            int result = DeadlineContext.withTimeout(Duration.ofSeconds(5), () -> 42);
            assertThat(result).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("remaining()")
    class RemainingTests {

        @Test
        @DisplayName("should return empty when no deadline is bound")
        void shouldReturnEmptyWhenNotBound() {
            assertThat(DeadlineContext.remaining()).isEmpty();
        }

        @Test
        @DisplayName("should return positive duration for future deadline")
        void shouldReturnPositiveForFutureDeadline() {
            DeadlineContext.withTimeout(Duration.ofSeconds(30), () -> {
                Optional<Duration> remaining = DeadlineContext.remaining();
                assertThat(remaining).isPresent();
                assertThat(remaining.get()).isPositive();
                assertThat(remaining.get().getSeconds()).isLessThanOrEqualTo(30);
            });
        }

        @Test
        @DisplayName("should return ZERO for past deadline")
        void shouldReturnZeroForPastDeadline() {
            Instant pastDeadline = Instant.now().minusSeconds(10);
            DeadlineContext.withDeadline(pastDeadline, () -> {
                Optional<Duration> remaining = DeadlineContext.remaining();
                assertThat(remaining).isPresent();
                assertThat(remaining.get()).isEqualTo(Duration.ZERO);
            });
        }
    }

    @Nested
    @DisplayName("isExpired()")
    class IsExpiredTests {

        @Test
        @DisplayName("should return false when no deadline is bound")
        void shouldReturnFalseWhenNotBound() {
            assertThat(DeadlineContext.isExpired()).isFalse();
        }

        @Test
        @DisplayName("should return false for future deadline")
        void shouldReturnFalseForFutureDeadline() {
            DeadlineContext.withTimeout(Duration.ofSeconds(60), () -> {
                assertThat(DeadlineContext.isExpired()).isFalse();
            });
        }

        @Test
        @DisplayName("should return true for past deadline")
        void shouldReturnTrueForPastDeadline() {
            Instant pastDeadline = Instant.now().minusSeconds(10);
            DeadlineContext.withDeadline(pastDeadline, () -> {
                assertThat(DeadlineContext.isExpired()).isTrue();
            });
        }
    }

    @Nested
    @DisplayName("nested scopes")
    class NestedScopes {

        @Test
        @DisplayName("inner scope should override outer deadline")
        void innerScopeShouldOverride() {
            Instant outerDeadline = Instant.now().plusSeconds(60);
            Instant innerDeadline = Instant.now().plusSeconds(5);

            DeadlineContext.withDeadline(outerDeadline, () -> {
                assertThat(DeadlineContext.current().get()).isEqualTo(outerDeadline);

                DeadlineContext.withDeadline(innerDeadline, () -> {
                    assertThat(DeadlineContext.current().get()).isEqualTo(innerDeadline);
                });

                // outer scope should be restored
                assertThat(DeadlineContext.current().get()).isEqualTo(outerDeadline);
            });
        }
    }
}
