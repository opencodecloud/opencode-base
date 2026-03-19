package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.LockGuard;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * LocalReadWriteLock full test - 本地读写锁完整测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class LocalReadWriteLockFullTest {

    private LocalReadWriteLock rwLock;

    @BeforeEach
    void setUp() {
        rwLock = new LocalReadWriteLock();
    }

    @Nested
    @DisplayName("Constructor Tests | 构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should create lock")
        void defaultConstructor_shouldCreateLock() {
            LocalReadWriteLock lock = new LocalReadWriteLock();

            assertThat(lock).isNotNull();
            assertThat(lock.readLock()).isNotNull();
            assertThat(lock.writeLock()).isNotNull();
        }

        @Test
        @DisplayName("constructor with config should apply config")
        void constructorWithConfig_shouldApplyConfig() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .build();

            LocalReadWriteLock lock = new LocalReadWriteLock(config);

            assertThat(lock).isNotNull();
        }
    }

    @Nested
    @DisplayName("ReadLock Tests | 读锁测试")
    class ReadLockTests {

        @Test
        @DisplayName("readLock() should return Lock")
        void readLock_shouldReturnLock() {
            Lock<Long> readLock = rwLock.readLock();

            assertThat(readLock).isNotNull();
        }

        @Test
        @DisplayName("readLock should be acquirable")
        void readLock_shouldBeAcquirable() {
            try (var guard = rwLock.readLock().lock()) {
                assertThat(rwLock.getReadLockCount()).isEqualTo(1);
            }

            assertThat(rwLock.getReadLockCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("multiple readers should be allowed")
        void multipleReaders_shouldBeAllowed() throws Exception {
            AtomicInteger concurrentReaders = new AtomicInteger(0);
            AtomicInteger maxReaders = new AtomicInteger(0);

            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch ready = new CountDownLatch(threads);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        rwLock.executeRead(() -> {
                            int c = concurrentReaders.incrementAndGet();
                            maxReaders.updateAndGet(m -> Math.max(m, c));
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            concurrentReaders.decrementAndGet();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await();
            start.countDown();
            done.await();
            executor.shutdown();

            assertThat(maxReaders.get()).isGreaterThan(1);
        }

        @Test
        @DisplayName("tryLock() on read lock should work")
        void tryLock_onReadLock_shouldWork() {
            boolean acquired = rwLock.readLock().tryLock();

            assertThat(acquired).isTrue();
            assertThat(rwLock.getReadLockCount()).isEqualTo(1);

            rwLock.readLock().unlock();
        }

        @Test
        @DisplayName("tryLock(timeout) on read lock should work")
        void tryLockWithTimeout_onReadLock_shouldWork() {
            boolean acquired = rwLock.readLock().tryLock(Duration.ofSeconds(1));

            assertThat(acquired).isTrue();

            rwLock.readLock().unlock();
        }
    }

    @Nested
    @DisplayName("WriteLock Tests | 写锁测试")
    class WriteLockTests {

        @Test
        @DisplayName("writeLock() should return Lock")
        void writeLock_shouldReturnLock() {
            Lock<Long> writeLock = rwLock.writeLock();

            assertThat(writeLock).isNotNull();
        }

        @Test
        @DisplayName("writeLock should be exclusive")
        void writeLock_shouldBeExclusive() throws Exception {
            AtomicInteger concurrentWriters = new AtomicInteger(0);
            AtomicInteger maxWriters = new AtomicInteger(0);
            AtomicBoolean failed = new AtomicBoolean(false);

            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < 20; j++) {
                        rwLock.executeWrite(() -> {
                            int c = concurrentWriters.incrementAndGet();
                            maxWriters.updateAndGet(m -> Math.max(m, c));
                            if (c > 1) failed.set(true);
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            concurrentWriters.decrementAndGet();
                        });
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            assertThat(failed.get()).isFalse();
            assertThat(maxWriters.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("writeLock should track status")
        void writeLock_shouldTrackStatus() {
            assertThat(rwLock.isWriteLocked()).isFalse();

            try (var guard = rwLock.writeLock().lock()) {
                assertThat(rwLock.isWriteLocked()).isTrue();
                assertThat(rwLock.isWriteLockedByCurrentThread()).isTrue();
            }

            assertThat(rwLock.isWriteLocked()).isFalse();
        }

        @Test
        @DisplayName("tryLock() on write lock should work")
        void tryLock_onWriteLock_shouldWork() {
            boolean acquired = rwLock.writeLock().tryLock();

            assertThat(acquired).isTrue();
            assertThat(rwLock.isWriteLocked()).isTrue();

            rwLock.writeLock().unlock();
        }
    }

    @Nested
    @DisplayName("Read-Write Interaction Tests | 读写交互测试")
    class ReadWriteInteractionTests {

        @Test
        @DisplayName("write lock should block readers")
        void writeLock_shouldBlockReaders() throws Exception {
            CountDownLatch writeLocked = new CountDownLatch(1);
            CountDownLatch readerStarted = new CountDownLatch(1);
            AtomicBoolean readerWaited = new AtomicBoolean(false);

            Thread writer = new Thread(() -> {
                try (var guard = rwLock.writeLock().lock()) {
                    writeLocked.countDown();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            writer.start();
            writeLocked.await();

            Thread reader = new Thread(() -> {
                readerStarted.countDown();
                long start = System.currentTimeMillis();
                rwLock.executeRead(() -> {
                    long waited = System.currentTimeMillis() - start;
                    if (waited > 50) readerWaited.set(true);
                });
            });
            reader.start();
            readerStarted.await();

            writer.join();
            reader.join();

            assertThat(readerWaited.get()).isTrue();
        }

        @Test
        @DisplayName("readers should block writers")
        void readers_shouldBlockWriters() throws Exception {
            CountDownLatch readLocked = new CountDownLatch(1);
            CountDownLatch writerStarted = new CountDownLatch(1);
            AtomicBoolean writerWaited = new AtomicBoolean(false);

            Thread reader = new Thread(() -> {
                try (var guard = rwLock.readLock().lock()) {
                    readLocked.countDown();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            reader.start();
            readLocked.await();

            Thread writer = new Thread(() -> {
                writerStarted.countDown();
                long start = System.currentTimeMillis();
                rwLock.executeWrite(() -> {
                    long waited = System.currentTimeMillis() - start;
                    if (waited > 50) writerWaited.set(true);
                });
            });
            writer.start();
            writerStarted.await();

            reader.join();
            writer.join();

            assertThat(writerWaited.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("Execute Methods Tests | 执行方法测试")
    class ExecuteMethodsTests {

        @Test
        @DisplayName("executeRead(Runnable) should work")
        void executeReadRunnable_shouldWork() {
            AtomicBoolean executed = new AtomicBoolean(false);

            rwLock.executeRead(() -> executed.set(true));

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("executeRead(Supplier) should return result")
        void executeReadSupplier_shouldReturnResult() {
            String result = rwLock.executeRead(() -> "read result");

            assertThat(result).isEqualTo("read result");
        }

        @Test
        @DisplayName("executeWrite(Runnable) should work")
        void executeWriteRunnable_shouldWork() {
            AtomicBoolean executed = new AtomicBoolean(false);

            rwLock.executeWrite(() -> executed.set(true));

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("executeWrite(Supplier) should return result")
        void executeWriteSupplier_shouldReturnResult() {
            String result = rwLock.executeWrite(() -> "write result");

            assertThat(result).isEqualTo("write result");
        }
    }

    @Nested
    @DisplayName("Status Methods Tests | 状态方法测试")
    class StatusMethodsTests {

        @Test
        @DisplayName("getReadLockCount() should track count")
        void getReadLockCount_shouldTrackCount() {
            assertThat(rwLock.getReadLockCount()).isEqualTo(0);

            LockGuard<Long> guard1 = rwLock.readLock().lock();
            assertThat(rwLock.getReadLockCount()).isEqualTo(1);

            LockGuard<Long> guard2 = rwLock.readLock().lock();
            assertThat(rwLock.getReadLockCount()).isEqualTo(2);

            guard1.close();
            assertThat(rwLock.getReadLockCount()).isEqualTo(1);

            guard2.close();
            assertThat(rwLock.getReadLockCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("getReadHoldCount() should track per-thread count")
        void getReadHoldCount_shouldTrackPerThreadCount() {
            assertThat(rwLock.getReadHoldCount()).isEqualTo(0);

            LockGuard<Long> guard1 = rwLock.readLock().lock();
            assertThat(rwLock.getReadHoldCount()).isEqualTo(1);

            LockGuard<Long> guard2 = rwLock.readLock().lock();
            assertThat(rwLock.getReadHoldCount()).isEqualTo(2);

            guard2.close();
            guard1.close();

            assertThat(rwLock.getReadHoldCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("getWriteHoldCount() should track count")
        void getWriteHoldCount_shouldTrackCount() {
            assertThat(rwLock.getWriteHoldCount()).isEqualTo(0);

            LockGuard<Long> guard1 = rwLock.writeLock().lock();
            assertThat(rwLock.getWriteHoldCount()).isEqualTo(1);

            LockGuard<Long> guard2 = rwLock.writeLock().lock();
            assertThat(rwLock.getWriteHoldCount()).isEqualTo(2);

            guard2.close();
            guard1.close();

            assertThat(rwLock.getWriteHoldCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Edge Cases | 边界情况")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle exception in read action")
        void shouldHandleExceptionInReadAction() {
            assertThatThrownBy(() -> rwLock.executeRead(() -> {
                throw new RuntimeException("read error");
            })).isInstanceOf(RuntimeException.class);

            assertThat(rwLock.getReadLockCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("should handle exception in write action")
        void shouldHandleExceptionInWriteAction() {
            assertThatThrownBy(() -> rwLock.executeWrite(() -> {
                throw new RuntimeException("write error");
            })).isInstanceOf(RuntimeException.class);

            assertThat(rwLock.isWriteLocked()).isFalse();
        }

        @Test
        @DisplayName("should handle null result in executeRead")
        void shouldHandleNullResultInExecuteRead() {
            String result = rwLock.executeRead(() -> null);

            assertThat(result).isNull();
        }
    }
}
