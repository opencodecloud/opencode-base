package cloud.opencode.base.lock;

import cloud.opencode.base.lock.manager.LockGroup;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * LockGroup test - 锁组测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class LockGroupTest {

    @Nested
    @DisplayName("Lock All Tests | 锁定所有测试")
    class LockAllTests {

        @Test
        @DisplayName("lockAll should acquire all locks atomically")
        void lockAll_shouldAcquireAllLocksAtomically() {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();
            Lock<Long> lockC = OpenLock.lock();

            LockGroup group = OpenLock.lockGroup()
                    .add(lockA)
                    .add(lockB)
                    .add(lockC)
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
        @DisplayName("tryLockAll should return true when all locks available")
        void tryLockAll_shouldReturnTrueWhenAllLocksAvailable() {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            LockGroup group = OpenLock.lockGroup()
                    .add(lockA)
                    .add(lockB)
                    .build();

            assertThat(group.tryLockAll()).isTrue();

            assertThat(lockA.isHeldByCurrentThread()).isTrue();
            assertThat(lockB.isHeldByCurrentThread()).isTrue();

            group.releaseAll();
        }
    }

    @Nested
    @DisplayName("Deadlock Prevention Tests | 死锁预防测试")
    class DeadlockPreventionTests {

        @Test
        @DisplayName("lockGroup should prevent deadlock")
        void lockGroup_shouldPreventDeadlock() throws Exception {
            Lock<Long> lockA = OpenLock.lock();
            Lock<Long> lockB = OpenLock.lock();

            AtomicInteger successCount = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            List<Future<?>> futures = new ArrayList<>();

            // Thread 1: locks A then B
            futures.add(executor.submit(() -> {
                LockGroup group = OpenLock.lockGroup()
                        .add(lockA).add(lockB)
                        .timeout(Duration.ofSeconds(5))
                        .build();

                for (int i = 0; i < 100; i++) {
                    try (var guard = group.lockAll()) {
                        successCount.incrementAndGet();
                    }
                }
            }));

            // Thread 2: locks B then A (different order)
            futures.add(executor.submit(() -> {
                LockGroup group = OpenLock.lockGroup()
                        .add(lockB).add(lockA)  // Different order
                        .timeout(Duration.ofSeconds(5))
                        .build();

                for (int i = 0; i < 100; i++) {
                    try (var guard = group.lockAll()) {
                        successCount.incrementAndGet();
                    }
                }
            }));

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            // All operations should succeed without deadlock
            assertThat(successCount.get()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder should throw on empty locks")
        void builder_shouldThrowOnEmptyLocks() {
            assertThatThrownBy(() -> OpenLock.lockGroup().build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one lock");
        }

        @Test
        @DisplayName("builder should accept timeout")
        void builder_shouldAcceptTimeout() {
            Lock<Long> lock = OpenLock.lock();

            LockGroup group = OpenLock.lockGroup()
                    .add(lock)
                    .timeout(Duration.ofSeconds(10))
                    .build();

            assertThat(group.size()).isEqualTo(1);
        }
    }
}
