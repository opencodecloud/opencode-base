package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * SegmentLock test - 分段锁测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class SegmentLockTest {

    @Nested
    @DisplayName("Constructor Tests | 构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should create 16 segments")
        void defaultConstructor_shouldCreate16Segments() {
            SegmentLock<String> lock = new SegmentLock<>();

            assertThat(lock.getSegments()).isEqualTo(16);
        }

        @Test
        @DisplayName("constructor with segments should create specified segments")
        void constructorWithSegments_shouldCreateSpecifiedSegments() {
            SegmentLock<String> lock = new SegmentLock<>(32);

            assertThat(lock.getSegments()).isEqualTo(32);
        }

        @Test
        @DisplayName("constructor with config should apply config")
        void constructorWithConfig_shouldApplyConfig() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .build();

            SegmentLock<String> lock = new SegmentLock<>(8, config);

            assertThat(lock.getSegments()).isEqualTo(8);
            Lock<Long> segment = lock.getLock("test");
            assertThat(((LocalLock) segment).isFair()).isTrue();
        }

        @Test
        @DisplayName("constructor should throw on zero segments")
        void constructor_shouldThrowOnZeroSegments() {
            assertThatThrownBy(() -> new SegmentLock<>(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("constructor should throw on negative segments")
        void constructor_shouldThrowOnNegativeSegments() {
            assertThatThrownBy(() -> new SegmentLock<>(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }

    @Nested
    @DisplayName("GetLock Tests | 获取锁测试")
    class GetLockTests {

        @Test
        @DisplayName("getLock() should return lock for key")
        void getLock_shouldReturnLockForKey() {
            SegmentLock<String> segmentLock = new SegmentLock<>();

            Lock<Long> lock = segmentLock.getLock("user:123");

            assertThat(lock).isNotNull();
            assertThat(lock).isInstanceOf(LocalLock.class);
        }

        @Test
        @DisplayName("getLock() should return same lock for same key")
        void getLock_shouldReturnSameLockForSameKey() {
            SegmentLock<String> segmentLock = new SegmentLock<>();

            Lock<Long> lock1 = segmentLock.getLock("key1");
            Lock<Long> lock2 = segmentLock.getLock("key1");

            assertThat(lock1).isSameAs(lock2);
        }

        @Test
        @DisplayName("getLock() may return same lock for different keys (due to hashing)")
        void getLock_mayReturnSameLockForDifferentKeys() {
            SegmentLock<String> segmentLock = new SegmentLock<>(2);

            // With only 2 segments, collisions are likely
            Lock<Long> lock1 = segmentLock.getLock("key1");
            Lock<Long> lock2 = segmentLock.getLock("key2");
            Lock<Long> lock3 = segmentLock.getLock("key3");
            Lock<Long> lock4 = segmentLock.getLock("key4");

            // At least some should be the same
            boolean hasSame = lock1 == lock2 || lock1 == lock3 || lock1 == lock4
                    || lock2 == lock3 || lock2 == lock4 || lock3 == lock4;
            assertThat(hasSame).isTrue();
        }

        @Test
        @DisplayName("getLock() should work with integer keys")
        void getLock_shouldWorkWithIntegerKeys() {
            SegmentLock<Integer> segmentLock = new SegmentLock<>();

            Lock<Long> lock = segmentLock.getLock(123);

            assertThat(lock).isNotNull();
        }

        @Test
        @DisplayName("getLock() should work with custom object keys")
        void getLock_shouldWorkWithCustomObjectKeys() {
            record UserKey(String id, String name) {}
            SegmentLock<UserKey> segmentLock = new SegmentLock<>();

            Lock<Long> lock = segmentLock.getLock(new UserKey("1", "test"));

            assertThat(lock).isNotNull();
        }
    }

    @Nested
    @DisplayName("GetSegmentIndex Tests | 获取分段索引测试")
    class GetSegmentIndexTests {

        @Test
        @DisplayName("getSegmentIndex() should return valid index")
        void getSegmentIndex_shouldReturnValidIndex() {
            SegmentLock<String> segmentLock = new SegmentLock<>(16);

            int index = segmentLock.getSegmentIndex("test");

            assertThat(index).isBetween(0, 15);
        }

        @Test
        @DisplayName("getSegmentIndex() should be consistent")
        void getSegmentIndex_shouldBeConsistent() {
            SegmentLock<String> segmentLock = new SegmentLock<>();

            int index1 = segmentLock.getSegmentIndex("key");
            int index2 = segmentLock.getSegmentIndex("key");

            assertThat(index1).isEqualTo(index2);
        }

        @Test
        @DisplayName("getSegmentIndex() should distribute keys")
        void getSegmentIndex_shouldDistributeKeys() {
            SegmentLock<String> segmentLock = new SegmentLock<>(16);

            int[] counts = new int[16];
            for (int i = 0; i < 1000; i++) {
                int index = segmentLock.getSegmentIndex("key" + i);
                counts[index]++;
            }

            // Check that distribution is somewhat even
            for (int count : counts) {
                assertThat(count).isGreaterThan(10); // Rough check
            }
        }
    }

    @Nested
    @DisplayName("Execute Tests | 执行测试")
    class ExecuteTests {

        @Test
        @DisplayName("execute() should run action with lock")
        void execute_shouldRunActionWithLock() {
            SegmentLock<String> segmentLock = new SegmentLock<>();
            AtomicBoolean executed = new AtomicBoolean(false);

            segmentLock.execute("key", () -> {
                executed.set(true);
                assertThat(segmentLock.getLock("key").isHeldByCurrentThread()).isTrue();
            });

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("execute() should release lock after action")
        void execute_shouldReleaseLockAfterAction() {
            SegmentLock<String> segmentLock = new SegmentLock<>();

            segmentLock.execute("key", () -> {});

            assertThat(segmentLock.getLock("key").isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("execute() should release lock on exception")
        void execute_shouldReleaseLockOnException() {
            SegmentLock<String> segmentLock = new SegmentLock<>();

            assertThatThrownBy(() -> segmentLock.execute("key", () -> {
                throw new RuntimeException("test");
            })).isInstanceOf(RuntimeException.class);

            assertThat(segmentLock.getLock("key").isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("executeWithResult() should return result")
        void executeWithResult_shouldReturnResult() {
            SegmentLock<String> segmentLock = new SegmentLock<>();

            String result = segmentLock.executeWithResult("key", () -> "result");

            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("executeWithResult() should release lock after execution")
        void executeWithResult_shouldReleaseLockAfterExecution() {
            SegmentLock<String> segmentLock = new SegmentLock<>();

            segmentLock.executeWithResult("key", () -> "result");

            assertThat(segmentLock.getLock("key").isHeldByCurrentThread()).isFalse();
        }
    }

    @Nested
    @DisplayName("Concurrency Tests | 并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("different keys should allow concurrent access")
        void differentKeys_shouldAllowConcurrentAccess() throws Exception {
            SegmentLock<String> segmentLock = new SegmentLock<>(64);
            AtomicInteger concurrentCount = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);

            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch startLatch = new CountDownLatch(threads);
            CountDownLatch doneLatch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                final String key = "key" + (i * 1000); // Different keys
                executor.submit(() -> {
                    try {
                        startLatch.countDown();
                        startLatch.await();

                        segmentLock.execute(key, () -> {
                            int current = concurrentCount.incrementAndGet();
                            maxConcurrent.updateAndGet(max -> Math.max(max, current));
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            concurrentCount.decrementAndGet();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            doneLatch.await();
            executor.shutdown();

            // With different keys and enough segments, should see concurrent access
            assertThat(maxConcurrent.get()).isGreaterThan(1);
        }

        @Test
        @DisplayName("same key should serialize access")
        void sameKey_shouldSerializeAccess() throws Exception {
            SegmentLock<String> segmentLock = new SegmentLock<>();
            AtomicInteger concurrentCount = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);
            AtomicBoolean failed = new AtomicBoolean(false);

            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < 20; j++) {
                        segmentLock.execute("sameKey", () -> {
                            int current = concurrentCount.incrementAndGet();
                            maxConcurrent.updateAndGet(max -> Math.max(max, current));
                            if (current > 1) {
                                failed.set(true);
                            }
                            concurrentCount.decrementAndGet();
                        });
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            assertThat(failed.get()).isFalse();
            assertThat(maxConcurrent.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("should handle mixed keys correctly")
        void shouldHandleMixedKeysCorrectly() throws Exception {
            SegmentLock<String> segmentLock = new SegmentLock<>(16);
            AtomicInteger totalOperations = new AtomicInteger(0);

            int threads = 10;
            int keysPerThread = 5;
            int operations = 50;

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                final int threadId = i;
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < operations; j++) {
                        String key = "key" + (threadId * keysPerThread + (j % keysPerThread));
                        segmentLock.execute(key, totalOperations::incrementAndGet);
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            assertThat(totalOperations.get()).isEqualTo(threads * operations);
        }
    }

    @Nested
    @DisplayName("Edge Cases | 边界情况")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle null result in executeWithResult")
        void shouldHandleNullResultInExecuteWithResult() {
            SegmentLock<String> segmentLock = new SegmentLock<>();

            String result = segmentLock.executeWithResult("key", () -> null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should handle keys with same hash code")
        void shouldHandleKeysWithSameHashCode() {
            SegmentLock<String> segmentLock = new SegmentLock<>(16);

            // "Aa" and "BB" have the same hashCode in Java
            segmentLock.execute("Aa", () -> {});
            segmentLock.execute("BB", () -> {});

            // Should not throw
            assertThat(segmentLock.getSegmentIndex("Aa"))
                    .isEqualTo(segmentLock.getSegmentIndex("BB"));
        }

        @Test
        @DisplayName("should handle single segment")
        void shouldHandleSingleSegment() throws Exception {
            SegmentLock<String> segmentLock = new SegmentLock<>(1);

            AtomicInteger counter = new AtomicInteger(0);
            int threads = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                final int threadId = i;
                futures.add(executor.submit(() -> {
                    segmentLock.execute("key" + threadId, counter::incrementAndGet);
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            assertThat(counter.get()).isEqualTo(threads);
        }
    }
}
