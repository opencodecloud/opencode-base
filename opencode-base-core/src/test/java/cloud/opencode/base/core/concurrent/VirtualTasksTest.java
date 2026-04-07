package cloud.opencode.base.core.concurrent;

import cloud.opencode.base.core.exception.OpenException;
import cloud.opencode.base.core.exception.OpenTimeoutException;
import cloud.opencode.base.core.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link VirtualTasks}.
 *
 * @author Leon Soo
 * @since 1.0.3
 */
@DisplayName("VirtualTasks")
class VirtualTasksTest {

    @Nested
    @DisplayName("invokeAll")
    class InvokeAllTest {

        @Test
        @DisplayName("should return all results when all tasks succeed")
        void allSucceed() {
            List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2, () -> 3);

            List<Integer> results = VirtualTasks.invokeAll(tasks);

            assertThat(results).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("should throw OpenException when first task fails and cancel remaining")
        void firstFails() {
            AtomicBoolean secondTaskStarted = new AtomicBoolean(false);
            CountDownLatch failLatch = new CountDownLatch(1);

            List<Callable<Integer>> tasks = List.of(
                    () -> {
                        throw new RuntimeException("boom");
                    },
                    () -> {
                        secondTaskStarted.set(true);
                        failLatch.await(5, TimeUnit.SECONDS);
                        return 2;
                    }
            );

            assertThatThrownBy(() -> VirtualTasks.invokeAll(tasks))
                    .isInstanceOf(OpenException.class)
                    .hasMessageContaining("Task execution failed")
                    .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw OpenTimeoutException when timeout expires")
        void timeout() {
            List<Callable<Integer>> tasks = List.of(
                    () -> {
                        Thread.sleep(5000);
                        return 1;
                    }
            );

            assertThatThrownBy(() -> VirtualTasks.invokeAll(tasks, Duration.ofMillis(50)))
                    .isInstanceOf(OpenTimeoutException.class);
        }

        @Test
        @DisplayName("should reject null task list")
        void nullRejected() {
            assertThatThrownBy(() -> VirtualTasks.invokeAll(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject empty task list")
        void emptyRejected() {
            assertThatThrownBy(() -> VirtualTasks.invokeAll(List.of()))
                    .hasMessageContaining("must not be empty");
        }

        @Test
        @DisplayName("should return results in submission order")
        void preservesOrder() {
            List<Callable<String>> tasks = List.of(
                    () -> {
                        Thread.sleep(50);
                        return "slow";
                    },
                    () -> "fast"
            );

            List<String> results = VirtualTasks.invokeAll(tasks);

            assertThat(results).containsExactly("slow", "fast");
        }

        @Test
        @DisplayName("should succeed within timeout when tasks complete in time")
        void withinTimeout() {
            List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2);

            List<Integer> results = VirtualTasks.invokeAll(tasks, Duration.ofSeconds(5));

            assertThat(results).containsExactly(1, 2);
        }
    }

    @Nested
    @DisplayName("invokeAny")
    class InvokeAnyTest {

        @Test
        @DisplayName("should return the first successful result")
        void firstWins() {
            List<Callable<String>> tasks = List.of(
                    () -> {
                        Thread.sleep(500);
                        return "slow";
                    },
                    () -> "fast"
            );

            String result = VirtualTasks.invokeAny(tasks);

            assertThat(result).isEqualTo("fast");
        }

        @Test
        @DisplayName("should throw OpenException when all tasks fail")
        void allFail() {
            List<Callable<String>> tasks = List.of(
                    () -> { throw new RuntimeException("fail1"); },
                    () -> { throw new RuntimeException("fail2"); }
            );

            assertThatThrownBy(() -> VirtualTasks.invokeAny(tasks))
                    .isInstanceOf(OpenException.class)
                    .hasMessageContaining("All tasks failed");
        }

        @Test
        @DisplayName("should throw OpenTimeoutException when timeout expires")
        void timeout() {
            List<Callable<String>> tasks = List.of(
                    () -> {
                        Thread.sleep(5000);
                        return "never";
                    }
            );

            assertThatThrownBy(() -> VirtualTasks.invokeAny(tasks, Duration.ofMillis(50)))
                    .isInstanceOf(OpenTimeoutException.class);
        }

        @Test
        @DisplayName("should succeed even if some tasks fail")
        void someFailSomeSucceed() {
            List<Callable<String>> tasks = List.of(
                    () -> { throw new RuntimeException("fail"); },
                    () -> "success"
            );

            String result = VirtualTasks.invokeAny(tasks);

            assertThat(result).isEqualTo("success");
        }

        @Test
        @DisplayName("should reject null task list")
        void nullRejected() {
            assertThatThrownBy(() -> VirtualTasks.invokeAny(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject empty task list")
        void emptyRejected() {
            assertThatThrownBy(() -> VirtualTasks.invokeAny(List.of()))
                    .hasMessageContaining("must not be empty");
        }
    }

    @Nested
    @DisplayName("invokeAllSettled")
    class InvokeAllSettledTest {

        @Test
        @DisplayName("should collect mixed success and failure results")
        void mixedResults() {
            List<Callable<Integer>> tasks = List.of(
                    () -> 1,
                    () -> { throw new RuntimeException("boom"); },
                    () -> 3
            );

            List<Result<Integer>> results = VirtualTasks.invokeAllSettled(tasks);

            assertThat(results).hasSize(3);
            assertThat(results.get(0).isSuccess()).isTrue();
            assertThat(results.get(0).getOrElse(-1)).isEqualTo(1);
            assertThat(results.get(1).isFailure()).isTrue();
            assertThat(results.get(2).isSuccess()).isTrue();
            assertThat(results.get(2).getOrElse(-1)).isEqualTo(3);
        }

        @Test
        @DisplayName("should return all successes when no task fails")
        void allSuccess() {
            List<Callable<String>> tasks = List.of(() -> "a", () -> "b", () -> "c");

            List<Result<String>> results = VirtualTasks.invokeAllSettled(tasks);

            assertThat(results).hasSize(3);
            assertThat(results).allMatch(Result::isSuccess);
        }

        @Test
        @DisplayName("should return all failures when every task fails")
        void allFailure() {
            List<Callable<String>> tasks = List.of(
                    () -> { throw new RuntimeException("fail1"); },
                    () -> { throw new RuntimeException("fail2"); }
            );

            List<Result<String>> results = VirtualTasks.invokeAllSettled(tasks);

            assertThat(results).hasSize(2);
            assertThat(results).allMatch(Result::isFailure);
        }

        @Test
        @DisplayName("should record timeout as failure for slow tasks")
        void timeoutRecordedAsFailure() {
            List<Callable<Integer>> tasks = List.of(
                    () -> 1,
                    () -> {
                        Thread.sleep(5000);
                        return 2;
                    }
            );

            List<Result<Integer>> results = VirtualTasks.invokeAllSettled(tasks, Duration.ofMillis(200));

            assertThat(results).hasSize(2);
            assertThat(results.get(0).isSuccess()).isTrue();
            assertThat(results.get(1).isFailure()).isTrue();
        }

        @Test
        @DisplayName("should reject null task list")
        void nullRejected() {
            assertThatThrownBy(() -> VirtualTasks.invokeAllSettled(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject empty task list")
        void emptyRejected() {
            assertThatThrownBy(() -> VirtualTasks.invokeAllSettled(List.of()))
                    .hasMessageContaining("must not be empty");
        }
    }

    @Nested
    @DisplayName("parallelMap")
    class ParallelMapTest {

        @Test
        @DisplayName("should map items in parallel and preserve order")
        void basicMapping() {
            List<Integer> items = List.of(1, 2, 3, 4, 5);

            List<Integer> results = VirtualTasks.parallelMap(items, x -> x * 2);

            assertThat(results).containsExactly(2, 4, 6, 8, 10);
        }

        @Test
        @DisplayName("should propagate mapper exception as OpenException")
        void mapperThrows() {
            List<Integer> items = List.of(1, 2, 0, 4);

            assertThatThrownBy(() -> VirtualTasks.parallelMap(items, x -> 10 / x))
                    .isInstanceOf(OpenException.class)
                    .hasCauseInstanceOf(ArithmeticException.class);
        }

        @Test
        @DisplayName("should throw OpenTimeoutException when mapping times out")
        void mappingTimeout() {
            List<Integer> items = List.of(1, 2, 3);

            assertThatThrownBy(() -> VirtualTasks.parallelMap(
                    items,
                    x -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                        return x;
                    },
                    Duration.ofMillis(50)
            )).isInstanceOf(OpenTimeoutException.class);
        }

        @Test
        @DisplayName("should reject empty list")
        void emptyRejected() {
            assertThatThrownBy(() -> VirtualTasks.parallelMap(List.of(), x -> x))
                    .hasMessageContaining("must not be empty");
        }

        @Test
        @DisplayName("should reject null items")
        void nullItemsRejected() {
            assertThatThrownBy(() -> VirtualTasks.parallelMap(null, x -> x))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null mapper")
        void nullMapperRejected() {
            assertThatThrownBy(() -> VirtualTasks.parallelMap(List.of(1), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("runAll")
    class RunAllTest {

        @Test
        @DisplayName("should run all tasks to completion")
        void allComplete() {
            AtomicInteger counter = new AtomicInteger(0);
            List<Runnable> tasks = List.of(
                    counter::incrementAndGet,
                    counter::incrementAndGet,
                    counter::incrementAndGet
            );

            VirtualTasks.runAll(tasks);

            assertThat(counter.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("should throw OpenException when a task fails")
        void taskFails() {
            List<Runnable> tasks = List.of(
                    () -> {},
                    () -> { throw new RuntimeException("boom"); }
            );

            assertThatThrownBy(() -> VirtualTasks.runAll(tasks))
                    .isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("should throw OpenTimeoutException when timeout expires")
        void timeout() {
            List<Runnable> tasks = List.of(
                    () -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
            );

            assertThatThrownBy(() -> VirtualTasks.runAll(tasks, Duration.ofMillis(50)))
                    .isInstanceOf(OpenTimeoutException.class);
        }

        @Test
        @DisplayName("should reject null task list")
        void nullRejected() {
            assertThatThrownBy(() -> VirtualTasks.runAll(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject empty task list")
        void emptyRejected() {
            assertThatThrownBy(() -> VirtualTasks.runAll(List.of()))
                    .hasMessageContaining("must not be empty");
        }

        @Test
        @DisplayName("should complete within timeout when tasks are fast")
        void withinTimeout() {
            AtomicInteger counter = new AtomicInteger(0);
            List<Runnable> tasks = List.of(
                    counter::incrementAndGet,
                    counter::incrementAndGet
            );

            VirtualTasks.runAll(tasks, Duration.ofSeconds(5));

            assertThat(counter.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("supplyAsync")
    class SupplyAsyncTests {

        @Test
        @DisplayName("should complete with result")
        void shouldCompleteWithResult() {
            CompletableFuture<String> future = VirtualTasks.supplyAsync(() -> "hello");
            assertThat(future.join()).isEqualTo("hello");
        }

        @Test
        @DisplayName("should complete exceptionally on failure")
        void shouldCompleteExceptionallyOnFailure() {
            CompletableFuture<String> future = VirtualTasks.supplyAsync(() -> {
                throw new RuntimeException("boom");
            });
            assertThatThrownBy(future::join)
                    .isInstanceOf(java.util.concurrent.CompletionException.class)
                    .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should reject null task")
        void shouldRejectNullTask() {
            assertThatThrownBy(() -> VirtualTasks.supplyAsync(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should run on virtual thread")
        void shouldRunOnVirtualThread() {
            CompletableFuture<Boolean> future = VirtualTasks.supplyAsync(
                    () -> Thread.currentThread().isVirtual()
            );
            assertThat(future.join()).isTrue();
        }
    }

    @Nested
    @DisplayName("runAsync")
    class RunAsyncTests {

        @Test
        @DisplayName("should complete successfully")
        void shouldCompleteSuccessfully() {
            AtomicInteger counter = new AtomicInteger(0);
            CompletableFuture<Void> future = VirtualTasks.runAsync(counter::incrementAndGet);
            future.join();
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("should complete exceptionally on failure")
        void shouldCompleteExceptionallyOnFailure() {
            CompletableFuture<Void> future = VirtualTasks.runAsync(() -> {
                throw new RuntimeException("boom");
            });
            assertThatThrownBy(future::join)
                    .isInstanceOf(java.util.concurrent.CompletionException.class)
                    .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should reject null task")
        void shouldRejectNullTask() {
            assertThatThrownBy(() -> VirtualTasks.runAsync(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("parallelMap with concurrency limit")
    class ParallelMapConcurrencyTests {

        @Test
        @DisplayName("should respect concurrency limit")
        void shouldRespectConcurrencyLimit() {
            AtomicInteger concurrent = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);

            List<Integer> items = IntStream.rangeClosed(1, 20).boxed().toList();
            List<Integer> results = VirtualTasks.parallelMap(items, item -> {
                int c = concurrent.incrementAndGet();
                maxConcurrent.updateAndGet(max -> Math.max(max, c));
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                concurrent.decrementAndGet();
                return item * 2;
            }, 3);

            assertThat(results).hasSize(20);
            assertThat(results.getFirst()).isEqualTo(2);
            assertThat(maxConcurrent.get()).isLessThanOrEqualTo(3);
        }

        @Test
        @DisplayName("should preserve order")
        void shouldPreserveOrder() {
            List<Integer> items = List.of(1, 2, 3, 4, 5);
            List<Integer> results = VirtualTasks.parallelMap(items, i -> i * 10, 2);
            assertThat(results).containsExactly(10, 20, 30, 40, 50);
        }

        @Test
        @DisplayName("should reject non-positive concurrency")
        void shouldRejectNonPositiveConcurrency() {
            assertThatThrownBy(() -> VirtualTasks.parallelMap(List.of(1), i -> i, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should fall back to regular parallelMap when concurrency >= size")
        void shouldFallBackWhenConcurrencyExceedsSize() {
            List<Integer> items = List.of(1, 2, 3);
            List<Integer> results = VirtualTasks.parallelMap(items, i -> i * 2, 100);
            assertThat(results).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("should work with timeout")
        void shouldWorkWithTimeout() {
            List<Integer> items = List.of(1, 2, 3, 4, 5);
            List<Integer> results = VirtualTasks.parallelMap(
                    items, i -> i * 2, 2, Duration.ofSeconds(10));
            assertThat(results).containsExactly(2, 4, 6, 8, 10);
        }
    }
}
