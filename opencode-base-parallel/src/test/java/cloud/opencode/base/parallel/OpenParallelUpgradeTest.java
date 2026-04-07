package cloud.opencode.base.parallel;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenParallel Upgrade Tests - Tests for newly added methods in V1.0.3.
 * OpenParallel 升级测试 - V1.0.3 新增方法的测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.3
 */
@DisplayName("OpenParallel 升级方法测试")
class OpenParallelUpgradeTest {

    @Nested
    @DisplayName("parallelForEach方法测试")
    class ParallelForEachTests {

        @Test
        @DisplayName("基本并行遍历所有元素")
        void testBasicParallelForEach() {
            AtomicInteger sum = new AtomicInteger(0);
            List<Integer> items = List.of(1, 2, 3, 4, 5);

            OpenParallel.parallelForEach(items, 3, item -> sum.addAndGet(item));

            assertThat(sum.get()).isEqualTo(15);
        }

        @Test
        @DisplayName("并发度限制有效")
        void testParallelismIsRespected() {
            AtomicInteger concurrentCount = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);
            int parallelism = 2;
            CountDownLatch allStarted = new CountDownLatch(1);

            List<Integer> items = List.of(1, 2, 3, 4, 5, 6);

            OpenParallel.parallelForEach(items, parallelism, item -> {
                int current = concurrentCount.incrementAndGet();
                maxConcurrent.updateAndGet(prev -> Math.max(prev, current));
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    concurrentCount.decrementAndGet();
                }
            });

            assertThat(maxConcurrent.get()).isLessThanOrEqualTo(parallelism);
        }

        @Test
        @DisplayName("超时版本正常完成")
        void testParallelForEachWithTimeoutSuccess() {
            AtomicInteger counter = new AtomicInteger(0);
            List<Integer> items = List.of(1, 2, 3);

            OpenParallel.parallelForEach(items, 2, item -> counter.incrementAndGet(),
                    Duration.ofSeconds(5));

            assertThat(counter.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("超时版本超时抛出异常")
        void testParallelForEachTimeout() {
            List<Integer> items = List.of(1, 2, 3);

            assertThatThrownBy(() ->
                    OpenParallel.parallelForEach(items, 1, item -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, Duration.ofMillis(50))
            ).isInstanceOf(OpenParallelException.class);
        }

        @Test
        @DisplayName("空集合不抛异常")
        void testEmptyCollection() {
            AtomicInteger counter = new AtomicInteger(0);

            OpenParallel.parallelForEach(List.of(), 3, item -> counter.incrementAndGet());

            assertThat(counter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("空集合带超时不抛异常")
        void testEmptyCollectionWithTimeout() {
            AtomicInteger counter = new AtomicInteger(0);

            OpenParallel.parallelForEach(List.<Integer>of(), 3,
                    item -> counter.incrementAndGet(), Duration.ofSeconds(1));

            assertThat(counter.get()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("parallelMapSettled方法测试")
    class ParallelMapSettledTests {

        @Test
        @DisplayName("所有任务成功")
        void testAllSucceed() {
            List<Integer> items = List.of(1, 2, 3, 4, 5);

            ParallelResult<Integer> result = OpenParallel.parallelMapSettled(
                    items, x -> x * 2, 3);

            assertThat(result.isAllSuccessful()).isTrue();
            assertThat(result.hasFailures()).isFalse();
            assertThat(result.successCount()).isEqualTo(5);
            assertThat(result.failureCount()).isEqualTo(0);
            assertThat(result.successes()).containsExactlyInAnyOrder(2, 4, 6, 8, 10);
        }

        @Test
        @DisplayName("部分任务失败")
        void testSomeFail() {
            List<Integer> items = List.of(1, 2, 0, 4, 0);

            ParallelResult<Integer> result = OpenParallel.parallelMapSettled(
                    items, x -> {
                        if (x == 0) {
                            throw new IllegalArgumentException("Zero not allowed");
                        }
                        return x * 2;
                    }, 3);

            assertThat(result.hasFailures()).isTrue();
            assertThat(result.successCount()).isEqualTo(3);
            assertThat(result.failureCount()).isEqualTo(2);
            assertThat(result.successes()).containsExactlyInAnyOrder(2, 4, 8);
            assertThat(result.failures()).hasSize(2);
            assertThat(result.failures()).allSatisfy(t ->
                    assertThat(t).isInstanceOf(IllegalArgumentException.class));
        }

        @Test
        @DisplayName("所有任务失败")
        void testAllFail() {
            List<Integer> items = List.of(1, 2, 3);

            ParallelResult<Integer> result = OpenParallel.parallelMapSettled(
                    items, x -> {
                        throw new RuntimeException("fail-" + x);
                    }, 2);

            assertThat(result.isAllFailed()).isTrue();
            assertThat(result.successCount()).isEqualTo(0);
            assertThat(result.failureCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("空列表返回空结果")
        void testEmptyList() {
            ParallelResult<Integer> result = OpenParallel.parallelMapSettled(
                    List.<Integer>of(), x -> x * 2, 3);

            assertThat(result.isAllSuccessful()).isTrue();
            assertThat(result.successCount()).isEqualTo(0);
            assertThat(result.failureCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("并发度限制有效")
        void testParallelismIsRespected() {
            AtomicInteger concurrentCount = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);
            int parallelism = 2;

            List<Integer> items = List.of(1, 2, 3, 4, 5, 6);

            ParallelResult<Integer> result = OpenParallel.parallelMapSettled(
                    items, x -> {
                        int current = concurrentCount.incrementAndGet();
                        maxConcurrent.updateAndGet(prev -> Math.max(prev, current));
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            concurrentCount.decrementAndGet();
                        }
                        return x * 2;
                    }, parallelism);

            assertThat(maxConcurrent.get()).isLessThanOrEqualTo(parallelism);
            assertThat(result.successCount()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("forEachAsCompleted方法测试")
    class ForEachAsCompletedTests {

        @Test
        @DisplayName("所有结果被处理")
        void testAllResultsProcessed() {
            List<Supplier<Integer>> suppliers = List.of(
                    () -> 1, () -> 2, () -> 3, () -> 4, () -> 5
            );
            List<Integer> collected = Collections.synchronizedList(new ArrayList<>());

            OpenParallel.forEachAsCompleted(suppliers, collected::add);

            assertThat(collected).containsExactlyInAnyOrder(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("按完成顺序处理（快任务先完成）")
        void testCompletionOrder() {
            // Use CountDownLatch to ensure deterministic ordering:
            // supplier "fast" completes immediately, "slow" waits for a latch
            CountDownLatch slowGate = new CountDownLatch(1);
            AtomicBoolean fastFirst = new AtomicBoolean(false);

            List<Supplier<String>> suppliers = List.of(
                    () -> {
                        try {
                            slowGate.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "slow";
                    },
                    () -> "fast"
            );

            List<String> order = Collections.synchronizedList(new ArrayList<>());

            OpenParallel.forEachAsCompleted(suppliers, result -> {
                order.add(result);
                if (result.equals("fast")) {
                    // Release the slow task after fast has been received
                    slowGate.countDown();
                }
            });

            assertThat(order).containsExactly("fast", "slow");
        }

        @Test
        @DisplayName("有界并发版本所有结果被处理")
        void testBoundedAllResultsProcessed() {
            List<Supplier<Integer>> suppliers = List.of(
                    () -> 10, () -> 20, () -> 30
            );
            List<Integer> collected = Collections.synchronizedList(new ArrayList<>());

            OpenParallel.forEachAsCompleted(suppliers, 2, collected::add);

            assertThat(collected).containsExactlyInAnyOrder(10, 20, 30);
        }

        @Test
        @DisplayName("有界并发度限制有效")
        void testBoundedParallelismRespected() {
            AtomicInteger concurrentCount = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);
            int parallelism = 2;

            List<Supplier<Integer>> suppliers = List.of(
                    () -> compute(1, concurrentCount, maxConcurrent),
                    () -> compute(2, concurrentCount, maxConcurrent),
                    () -> compute(3, concurrentCount, maxConcurrent),
                    () -> compute(4, concurrentCount, maxConcurrent),
                    () -> compute(5, concurrentCount, maxConcurrent),
                    () -> compute(6, concurrentCount, maxConcurrent)
            );
            List<Integer> collected = Collections.synchronizedList(new ArrayList<>());

            OpenParallel.forEachAsCompleted(suppliers, parallelism, collected::add);

            assertThat(maxConcurrent.get()).isLessThanOrEqualTo(parallelism);
            assertThat(collected).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6);
        }

        private int compute(int value, AtomicInteger concurrentCount, AtomicInteger maxConcurrent) {
            int current = concurrentCount.incrementAndGet();
            maxConcurrent.updateAndGet(prev -> Math.max(prev, current));
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                concurrentCount.decrementAndGet();
            }
            return value;
        }
    }

    @Nested
    @DisplayName("invokeAny取消剩余Future测试")
    class InvokeAnyCancellationTests {

        @Test
        @DisplayName("varargs版本返回结果")
        void testVarargsReturnsResult() {
            String result = OpenParallel.invokeAny(
                    () -> "immediate",
                    () -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "slow";
                    }
            );

            assertThat(result).isIn("immediate", "slow");
        }

        @Test
        @DisplayName("首个完成后剩余被取消")
        void testRemainingAreCancelledAfterFirstCompletes() throws InterruptedException {
            CountDownLatch slowTaskStarted = new CountDownLatch(1);
            AtomicBoolean slowTaskInterrupted = new AtomicBoolean(false);
            AtomicBoolean slowTaskCompleted = new AtomicBoolean(false);

            List<Supplier<String>> suppliers = List.of(
                    () -> "fast",
                    () -> {
                        slowTaskStarted.countDown();
                        try {
                            Thread.sleep(10_000);
                            slowTaskCompleted.set(true);
                        } catch (InterruptedException e) {
                            slowTaskInterrupted.set(true);
                            Thread.currentThread().interrupt();
                        }
                        return "slow";
                    }
            );

            String result = OpenParallel.invokeAny(suppliers);

            assertThat(result).isEqualTo("fast");

            // Wait briefly for cancellation to propagate
            // The slow task should not complete normally
            Thread.sleep(200);
            assertThat(slowTaskCompleted.get()).isFalse();
        }

        @Test
        @DisplayName("Collection版本返回首个完成的结果")
        void testCollectionVersionReturnsFirst() {
            List<Supplier<String>> suppliers = List.of(
                    () -> "result1",
                    () -> "result2"
            );

            String result = OpenParallel.invokeAny(suppliers);

            assertThat(result).isIn("result1", "result2");
        }
    }
}
