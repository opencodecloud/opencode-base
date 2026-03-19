package cloud.opencode.base.parallel.structured;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * ScheduledScope Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
class ScheduledScopeTest {

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        void shouldCreate() {
            try (ScheduledScope<String> scope = ScheduledScope.create()) {
                scope.fork(() -> "test");
                scope.joinAll();
                assertThat(scope.getDeadline()).isNull();
            }
        }

        @Test
        void shouldCreateWithDeadline() {
            Instant deadline = Instant.now().plusSeconds(10);
            try (ScheduledScope<String> scope = ScheduledScope.withDeadline(deadline)) {
                scope.fork(() -> "test");
                scope.joinAll();
                assertThat(scope.getDeadline()).isEqualTo(deadline);
            }
        }

        @Test
        void shouldCreateWithTimeout() {
            try (ScheduledScope<String> scope = ScheduledScope.withTimeout(Duration.ofSeconds(5))) {
                scope.fork(() -> "test");
                scope.joinAll();
                assertThat(scope.getDeadline()).isNotNull();
            }
        }

        @Test
        void shouldCreateWithBuilder() {
            try (ScheduledScope<String> scope = ScheduledScope.<String>builder()
                    .timeout(Duration.ofSeconds(10))
                    .schedulerThreads(2)
                    .build()) {
                scope.fork(() -> "test");
                scope.joinAll();
                assertThat(scope.getDeadline()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Immediate Fork Tests")
    class ImmediateForkTests {

        @Test
        void shouldForkImmediately() {
            try (ScheduledScope<String> scope = ScheduledScope.create()) {
                scope.fork(() -> "result");
                List<String> results = scope.joinAll();

                assertThat(results).containsExactly("result");
            }
        }

        @Test
        void shouldForkAllImmediately() {
            try (ScheduledScope<Integer> scope = ScheduledScope.create()) {
                scope.forkAll(
                        () -> 1,
                        () -> 2,
                        () -> 3
                );
                List<Integer> results = scope.joinAll();

                assertThat(results).containsExactlyInAnyOrder(1, 2, 3);
            }
        }

        @Test
        void shouldForkAllFromIterable() {
            try (ScheduledScope<String> scope = ScheduledScope.create()) {
                scope.forkAll(List.of(
                        () -> "a",
                        () -> "b"
                ));
                List<String> results = scope.joinAll();

                assertThat(results).containsExactlyInAnyOrder("a", "b");
            }
        }
    }

    @Nested
    @DisplayName("Delayed Fork Tests")
    class DelayedForkTests {

        @Test
        void shouldForkWithDelay() throws Exception {
            try (ScheduledScope<String> scope = ScheduledScope.create()) {
                scope.fork(() -> "immediate");
                scope.forkDelayed(Duration.ofMillis(50), () -> "delayed");

                // Wait for delayed task to be scheduled
                Thread.sleep(100);

                List<String> results = scope.joinAll();
                assertThat(results).contains("immediate");
            }
        }

        @Test
        void shouldForkImmediatelyIfInstantInPast() {
            try (ScheduledScope<String> scope = ScheduledScope.create()) {
                Instant pastTime = Instant.now().minusSeconds(1);

                scope.forkAt(pastTime, () -> "executed");
                List<String> results = scope.joinAll();

                assertThat(results).containsExactly("executed");
            }
        }
    }

    @Nested
    @DisplayName("Join Tests")
    class JoinTests {

        @Test
        void shouldJoinAll() {
            try (ScheduledScope<String> scope = ScheduledScope.create()) {
                scope.fork(() -> "a");
                scope.fork(() -> "b");

                List<String> results = scope.joinAll();

                assertThat(results).containsExactlyInAnyOrder("a", "b");
            }
        }

        @Test
        void shouldJoinAllWithTimeout() {
            try (ScheduledScope<String> scope = ScheduledScope.create()) {
                scope.fork(() -> "result");

                List<String> results = scope.joinAll(Duration.ofSeconds(5));

                assertThat(results).containsExactly("result");
            }
        }

        @Test
        void shouldJoinAndReduce() {
            try (ScheduledScope<Integer> scope = ScheduledScope.create()) {
                scope.fork(() -> 1);
                scope.fork(() -> 2);
                scope.fork(() -> 3);

                Integer sum = scope.joinAndReduce(0, Integer::sum);

                assertThat(sum).isEqualTo(6);
            }
        }

        @Test
        void shouldJoinAsResultsForSuccess() {
            try (ScheduledScope<String> scope = ScheduledScope.create()) {
                scope.fork(() -> "success");

                List<TaskResult<String>> results = scope.joinAsResults();

                assertThat(results).hasSize(1);
                assertThat(results.getFirst().isSuccess()).isTrue();
                assertThat(results.getFirst().get()).isEqualTo("success");
            }
        }
    }

    @Nested
    @DisplayName("Deadline Tests")
    class DeadlineTests {

        @Test
        void shouldTrackRemainingTime() {
            try (ScheduledScope<String> scope = ScheduledScope.withTimeout(Duration.ofSeconds(10))) {
                scope.fork(() -> "test");
                Duration remaining = scope.getRemainingTime();
                scope.joinAll();

                assertThat(remaining).isNotNull();
                assertThat(remaining.toSeconds()).isLessThanOrEqualTo(10);
            }
        }

        @Test
        void shouldReturnNullRemainingTimeIfNoDeadline() {
            try (ScheduledScope<String> scope = ScheduledScope.create()) {
                scope.fork(() -> "test");
                scope.joinAll();
                assertThat(scope.getRemainingTime()).isNull();
            }
        }

        @Test
        void shouldDetectDeadlinePassed() throws Exception {
            try (ScheduledScope<String> scope = ScheduledScope.withTimeout(Duration.ofMillis(50))) {
                scope.fork(() -> "test");
                scope.joinAll();

                Thread.sleep(100);

                assertThat(scope.isDeadlinePassed()).isTrue();
            }
        }

        @Test
        void shouldRejectDelayedForkExceedingDeadline() {
            try (ScheduledScope<String> scope = ScheduledScope.withTimeout(Duration.ofMillis(100))) {
                scope.fork(() -> "test"); // Need at least one task
                assertThatThrownBy(() ->
                        scope.forkDelayed(Duration.ofSeconds(1), () -> "test")
                ).isInstanceOf(OpenParallelException.class)
                        .hasMessageContaining("exceed deadline");
                scope.joinAll();
            }
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        void shouldCloseAfterJoin() {
            ScheduledScope<String> scope = ScheduledScope.create();
            scope.fork(() -> "test");
            scope.joinAll();
            scope.close();
            // Should not throw
        }

        @Test
        void shouldRejectForkAfterClose() {
            ScheduledScope<String> scope = ScheduledScope.create();
            scope.fork(() -> "test");
            scope.joinAll();
            scope.close();

            assertThatThrownBy(() -> scope.fork(() -> "test"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        void shouldPropagateException() {
            try (ScheduledScope<String> scope = ScheduledScope.create()) {
                scope.fork(() -> {
                    throw new RuntimeException("Test error");
                });

                assertThatThrownBy(scope::joinAll)
                        .isInstanceOf(Exception.class);
            }
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        void shouldBuildWithDeadline() {
            Instant deadline = Instant.now().plus(Duration.ofMinutes(5));

            try (ScheduledScope<String> scope = ScheduledScope.<String>builder()
                    .deadline(deadline)
                    .build()) {
                scope.fork(() -> "test");
                scope.joinAll();
                assertThat(scope.getDeadline()).isEqualTo(deadline);
            }
        }

        @Test
        void shouldBuildWithTimeout() {
            try (ScheduledScope<String> scope = ScheduledScope.<String>builder()
                    .timeout(Duration.ofMinutes(1))
                    .build()) {
                scope.fork(() -> "test");
                scope.joinAll();
                assertThat(scope.getDeadline()).isNotNull();
            }
        }

        @Test
        void shouldRejectInvalidSchedulerThreads() {
            assertThatThrownBy(() ->
                    ScheduledScope.<String>builder()
                            .schedulerThreads(0)
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }
}
