package cloud.opencode.base.lock;

import cloud.opencode.base.lock.local.LocalReadWriteLock;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * ReadWriteLock test - 读写锁测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class ReadWriteLockTest {

    private LocalReadWriteLock rwLock;

    @BeforeEach
    void setUp() {
        rwLock = new LocalReadWriteLock();
    }

    @Nested
    @DisplayName("Read Lock Tests | 读锁测试")
    class ReadLockTests {

        @Test
        @DisplayName("read lock should allow concurrent readers")
        void readLock_shouldAllowConcurrentReaders() throws Exception {
            AtomicInteger concurrentReaders = new AtomicInteger(0);
            AtomicInteger maxConcurrentReaders = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(() -> {
                    rwLock.executeRead(() -> {
                        int current = concurrentReaders.incrementAndGet();
                        maxConcurrentReaders.updateAndGet(max -> Math.max(max, current));
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        concurrentReaders.decrementAndGet();
                    });
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            // Multiple readers should have run concurrently
            assertThat(maxConcurrentReaders.get()).isGreaterThan(1);
        }

        @Test
        @DisplayName("executeRead should return result")
        void executeRead_shouldReturnResult() {
            String result = rwLock.executeRead(() -> "data");
            assertThat(result).isEqualTo("data");
        }
    }

    @Nested
    @DisplayName("Write Lock Tests | 写锁测试")
    class WriteLockTests {

        @Test
        @DisplayName("write lock should be exclusive")
        void writeLock_shouldBeExclusive() throws Exception {
            AtomicInteger concurrentWriters = new AtomicInteger(0);
            AtomicInteger maxConcurrentWriters = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(() -> {
                    rwLock.executeWrite(() -> {
                        int current = concurrentWriters.incrementAndGet();
                        maxConcurrentWriters.updateAndGet(max -> Math.max(max, current));
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        concurrentWriters.decrementAndGet();
                    });
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            // Only one writer at a time
            assertThat(maxConcurrentWriters.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("executeWrite should return result")
        void executeWrite_shouldReturnResult() {
            String result = rwLock.executeWrite(() -> "written");
            assertThat(result).isEqualTo("written");
        }
    }

    @Nested
    @DisplayName("Status Tests | 状态测试")
    class StatusTests {

        @Test
        @DisplayName("should track read lock count")
        void shouldTrackReadLockCount() {
            assertThat(rwLock.getReadLockCount()).isEqualTo(0);

            try (var guard = rwLock.readLock().lock()) {
                assertThat(rwLock.getReadLockCount()).isEqualTo(1);
            }

            assertThat(rwLock.getReadLockCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("should track write lock status")
        void shouldTrackWriteLockStatus() {
            assertThat(rwLock.isWriteLocked()).isFalse();

            try (var guard = rwLock.writeLock().lock()) {
                assertThat(rwLock.isWriteLocked()).isTrue();
                assertThat(rwLock.isWriteLockedByCurrentThread()).isTrue();
            }

            assertThat(rwLock.isWriteLocked()).isFalse();
        }
    }
}
