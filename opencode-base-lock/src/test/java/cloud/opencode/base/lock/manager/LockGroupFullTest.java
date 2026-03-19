package cloud.opencode.base.lock.manager;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.OpenLock;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * LockGroup full test - 锁组完整测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class LockGroupFullTest {

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder() should create new builder")
        void builder_shouldCreateNewBuilder() {
            LockGroup.Builder builder = LockGroup.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("add() should add lock to group")
        void add_shouldAddLockToGroup() {
            Lock<Long> lock = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lock)
                    .build();

            assertThat(group.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("addAll() should add multiple locks")
        void addAll_shouldAddMultipleLocks() {
            Lock<Long> lock1 = OpenLock.lock();
            Lock<Long> lock2 = OpenLock.lock();
            Lock<Long> lock3 = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .addAll(Arrays.asList(lock1, lock2, lock3))
                    .build();

            assertThat(group.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("timeout() should set timeout")
        void timeout_shouldSetTimeout() {
            Lock<Long> lock = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lock)
                    .timeout(Duration.ofSeconds(10))
                    .build();

            assertThat(group.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("build() should throw on empty locks")
        void build_shouldThrowOnEmptyLocks() {
            assertThatThrownBy(() -> LockGroup.builder().build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one lock");
        }

        @Test
        @DisplayName("builder should support chaining")
        void builder_shouldSupportChaining() {
            Lock<Long> lock1 = OpenLock.lock();
            Lock<Long> lock2 = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lock1)
                    .add(lock2)
                    .timeout(Duration.ofSeconds(5))
                    .build();

            assertThat(group.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("LockAll Tests | 锁定所有测试")
    class LockAllTests {

        @Test
        @DisplayName("lockAll() should acquire all locks")
        void lockAll_shouldAcquireAllLocks() {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();
            Lock<Long> lockC = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lockA).add(lockB).add(lockC)
                    .build();

            try (var guard = group.lockAll()) {
                assertThat(lockA.isHeldByCurrentThread()).isTrue();
                assertThat(lockB.isHeldByCurrentThread()).isTrue();
                assertThat(lockC.isHeldByCurrentThread()).isTrue();
            }

            assertThat(lockA.isHeldByCurrentThread()).isFalse();
            assertThat(lockB.isHeldByCurrentThread()).isFalse();
            assertThat(lockC.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("lockAll() should return LockGroupGuard")
        void lockAll_shouldReturnLockGroupGuard() {
            Lock<Long> lock = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lock)
                    .build();

            LockGroup.LockGroupGuard guard = group.lockAll();

            assertThat(guard).isNotNull();
            assertThat(guard.group()).isSameAs(group);

            guard.close();
        }

        @Test
        @DisplayName("lockAll() should throw on timeout")
        void lockAll_shouldThrowOnTimeout() throws Exception {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            // Hold lockB in another thread
            CountDownLatch locked = new CountDownLatch(1);
            Thread holder = new Thread(() -> {
                lockB.lock();
                locked.countDown();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lockB.unlock();
                }
            });
            holder.start();
            locked.await();

            LockGroup group = LockGroup.builder()
                    .add(lockA).add(lockB)
                    .timeout(Duration.ofMillis(50))
                    .build();

            assertThatThrownBy(() -> group.lockAll())
                    .isInstanceOf(OpenLockTimeoutException.class);

            // Verify lockA was released on failure
            assertThat(lockA.isHeldByCurrentThread()).isFalse();

            holder.interrupt();
            holder.join(1000);
        }
    }

    @Nested
    @DisplayName("TryLockAll Tests | 尝试锁定所有测试")
    class TryLockAllTests {

        @Test
        @DisplayName("tryLockAll() should return true when all available")
        void tryLockAll_shouldReturnTrueWhenAllAvailable() {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lockA).add(lockB)
                    .build();

            boolean acquired = group.tryLockAll();

            assertThat(acquired).isTrue();
            assertThat(lockA.isHeldByCurrentThread()).isTrue();
            assertThat(lockB.isHeldByCurrentThread()).isTrue();

            group.releaseAll();
        }

        @Test
        @DisplayName("tryLockAll() should return false when any unavailable")
        void tryLockAll_shouldReturnFalseWhenAnyUnavailable() throws Exception {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            // Hold lockB
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);
            Thread holder = new Thread(() -> {
                lockB.lock();
                locked.countDown();
                try {
                    done.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lockB.unlock();
                }
            });
            holder.start();
            locked.await();

            LockGroup group = LockGroup.builder()
                    .add(lockA).add(lockB)
                    .build();

            boolean acquired = group.tryLockAll();

            assertThat(acquired).isFalse();
            // All locks should be released on failure
            assertThat(lockA.isHeldByCurrentThread()).isFalse();

            done.countDown();
            holder.join();
        }

        @Test
        @DisplayName("tryLockAll(timeout) should wait and acquire")
        void tryLockAllWithTimeout_shouldWaitAndAcquire() throws Exception {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            // Hold lockB briefly
            CountDownLatch locked = new CountDownLatch(1);
            Thread holder = new Thread(() -> {
                lockB.lock();
                locked.countDown();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lockB.unlock();
                }
            });
            holder.start();
            locked.await();

            LockGroup group = LockGroup.builder()
                    .add(lockA).add(lockB)
                    .build();

            boolean acquired = group.tryLockAll(Duration.ofSeconds(2));

            assertThat(acquired).isTrue();
            group.releaseAll();

            holder.join();
        }

        @Test
        @DisplayName("tryLockAll(timeout) should return false on timeout")
        void tryLockAllWithTimeout_shouldReturnFalseOnTimeout() throws Exception {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            // Hold lockB
            CountDownLatch locked = new CountDownLatch(1);
            Thread holder = new Thread(() -> {
                lockB.lock();
                locked.countDown();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lockB.unlock();
                }
            });
            holder.start();
            locked.await();

            LockGroup group = LockGroup.builder()
                    .add(lockA).add(lockB)
                    .build();

            boolean acquired = group.tryLockAll(Duration.ofMillis(50));

            assertThat(acquired).isFalse();

            holder.interrupt();
            holder.join(1000);
        }
    }

    @Nested
    @DisplayName("ReleaseAll Tests | 释放所有测试")
    class ReleaseAllTests {

        @Test
        @DisplayName("releaseAll() should release all acquired locks")
        void releaseAll_shouldReleaseAllAcquiredLocks() {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lockA).add(lockB)
                    .build();

            group.tryLockAll();
            assertThat(lockA.isHeldByCurrentThread()).isTrue();
            assertThat(lockB.isHeldByCurrentThread()).isTrue();

            group.releaseAll();

            assertThat(lockA.isHeldByCurrentThread()).isFalse();
            assertThat(lockB.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("releaseAll() should be safe when no locks acquired")
        void releaseAll_shouldBeSafeWhenNoLocksAcquired() {
            Lock<Long> lock = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lock)
                    .build();

            assertThatCode(() -> group.releaseAll()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("releaseAll() should clear acquired count")
        void releaseAll_shouldClearAcquiredCount() {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lockA).add(lockB)
                    .build();

            group.tryLockAll();
            assertThat(group.acquiredCount()).isEqualTo(2);

            group.releaseAll();
            assertThat(group.acquiredCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Size and Count Tests | 大小和计数测试")
    class SizeAndCountTests {

        @Test
        @DisplayName("size() should return number of locks")
        void size_shouldReturnNumberOfLocks() {
            Lock<Long> lock1 = OpenLock.lock();
            Lock<Long> lock2 = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lock1).add(lock2)
                    .build();

            assertThat(group.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("acquiredCount() should track acquired locks")
        void acquiredCount_shouldTrackAcquiredLocks() {
            Lock<Long> lock = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lock)
                    .build();

            assertThat(group.acquiredCount()).isEqualTo(0);

            group.tryLockAll();
            assertThat(group.acquiredCount()).isEqualTo(1);

            group.releaseAll();
            assertThat(group.acquiredCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Close/AutoCloseable Tests | 关闭/自动关闭测试")
    class CloseTests {

        @Test
        @DisplayName("close() should release all locks")
        void close_shouldReleaseAllLocks() {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lockA).add(lockB)
                    .build();

            group.tryLockAll();
            group.close();

            assertThat(lockA.isHeldByCurrentThread()).isFalse();
            assertThat(lockB.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("LockGroup should be AutoCloseable")
        void lockGroup_shouldBeAutoCloseable() {
            Lock<Long> lock = OpenLock.lock();

            try (LockGroup group = LockGroup.builder()
                    .add(lock)
                    .build()) {
                group.tryLockAll();
                assertThat(lock.isHeldByCurrentThread()).isTrue();
            }

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }
    }

    @Nested
    @DisplayName("Deadlock Prevention Tests | 死锁预防测试")
    class DeadlockPreventionTests {

        @Test
        @DisplayName("should prevent deadlock with consistent ordering")
        void shouldPreventDeadlockWithConsistentOrdering() throws Exception {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            AtomicInteger successCount = new AtomicInteger(0);
            int threads = 4;
            int iterations = 50;

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            // Half threads lock A then B
            for (int i = 0; i < threads / 2; i++) {
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < iterations; j++) {
                        LockGroup group = LockGroup.builder()
                                .add(lockA).add(lockB)
                                .timeout(Duration.ofSeconds(5))
                                .build();
                        try (var guard = group.lockAll()) {
                            successCount.incrementAndGet();
                        }
                    }
                }));
            }

            // Other half lock B then A (different order)
            for (int i = 0; i < threads / 2; i++) {
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < iterations; j++) {
                        LockGroup group = LockGroup.builder()
                                .add(lockB).add(lockA) // Different order
                                .timeout(Duration.ofSeconds(5))
                                .build();
                        try (var guard = group.lockAll()) {
                            successCount.incrementAndGet();
                        }
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }

            executor.shutdown();

            // All operations should complete without deadlock
            assertThat(successCount.get()).isEqualTo(threads * iterations);
        }

        @Test
        @DisplayName("should handle many locks without deadlock")
        void shouldHandleManyLocksWithoutDeadlock() throws Exception {
            int lockCount = 10;
            List<Lock<Long>> locks = new ArrayList<>();
            for (int i = 0; i < lockCount; i++) {
                locks.add(OpenLock.lock());
            }

            AtomicInteger successCount = new AtomicInteger(0);
            int threads = 5;
            int iterations = 20;

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                final int threadId = i;
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < iterations; j++) {
                        // Each thread locks different subsets
                        LockGroup.Builder builder = LockGroup.builder()
                                .timeout(Duration.ofSeconds(5));

                        for (int k = 0; k < 3; k++) {
                            int idx = (threadId + k + j) % lockCount;
                            builder.add(locks.get(idx));
                        }

                        try (var guard = builder.build().lockAll()) {
                            successCount.incrementAndGet();
                        }
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }

            executor.shutdown();

            assertThat(successCount.get()).isEqualTo(threads * iterations);
        }
    }

    @Nested
    @DisplayName("LockGroupGuard Tests | 锁组守卫测试")
    class LockGroupGuardTests {

        @Test
        @DisplayName("guard close() should release locks")
        void guardClose_shouldReleaseLocks() {
            Lock<Long> lock = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lock)
                    .build();

            LockGroup.LockGroupGuard guard = group.lockAll();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            guard.close();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("guard group() should return group")
        void guardGroup_shouldReturnGroup() {
            Lock<Long> lock = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lock)
                    .build();

            LockGroup.LockGroupGuard guard = group.lockAll();

            assertThat(guard.group()).isSameAs(group);

            guard.close();
        }

        @Test
        @DisplayName("guard should work with try-with-resources")
        void guard_shouldWorkWithTryWithResources() {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            LockGroup group = LockGroup.builder()
                    .add(lockA).add(lockB)
                    .build();

            try (var guard = group.lockAll()) {
                assertThat(lockA.isHeldByCurrentThread()).isTrue();
                assertThat(lockB.isHeldByCurrentThread()).isTrue();
            }

            assertThat(lockA.isHeldByCurrentThread()).isFalse();
            assertThat(lockB.isHeldByCurrentThread()).isFalse();
        }
    }
}
