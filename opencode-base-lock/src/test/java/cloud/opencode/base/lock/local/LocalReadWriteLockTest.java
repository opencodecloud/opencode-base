package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.LockGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * LocalReadWriteLock Tests
 * LocalReadWriteLock 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
@DisplayName("LocalReadWriteLock Tests | LocalReadWriteLock 测试")
class LocalReadWriteLockTest {

    private LocalReadWriteLock rwLock;

    @BeforeEach
    void setUp() {
        rwLock = new LocalReadWriteLock();
    }

    @Nested
    @DisplayName("Construction Tests | 构造测试")
    class ConstructionTests {

        @Test
        @DisplayName("default construction | 默认构造")
        void testDefaultConstruction() {
            LocalReadWriteLock lock = new LocalReadWriteLock();
            assertThat(lock).isNotNull();
            assertThat(lock.readLock()).isNotNull();
            assertThat(lock.writeLock()).isNotNull();
        }

        @Test
        @DisplayName("construction with config | 带配置构造")
        void testConstructionWithConfig() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .build();

            LocalReadWriteLock lock = new LocalReadWriteLock(config);

            assertThat(lock).isNotNull();
        }
    }

    @Nested
    @DisplayName("Read Lock Tests | 读锁测试")
    class ReadLockTests {

        @Test
        @DisplayName("readLock returns lock | readLock 返回锁")
        void testReadLock() {
            Lock<Long> readLock = rwLock.readLock();
            assertThat(readLock).isNotNull();
        }

        @Test
        @DisplayName("read lock can be acquired | 读锁可以获取")
        void testReadLockAcquire() {
            Lock<Long> readLock = rwLock.readLock();

            try (LockGuard<Long> guard = readLock.lock()) {
                assertThat(readLock.isHeldByCurrentThread()).isTrue();
            }
        }

        @Test
        @DisplayName("multiple read locks allowed | 允许多个读锁")
        void testMultipleReadLocks() throws InterruptedException {
            Lock<Long> readLock = rwLock.readLock();
            AtomicInteger concurrentReaders = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);
            int readerCount = 5;

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch holdLatch = new CountDownLatch(readerCount);
            CountDownLatch doneLatch = new CountDownLatch(readerCount);

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < readerCount; i++) {
                    executor.submit(() -> {
                        try {
                            startLatch.await();
                            try (LockGuard<Long> guard = readLock.lock()) {
                                int current = concurrentReaders.incrementAndGet();
                                maxConcurrent.updateAndGet(max -> Math.max(max, current));
                                holdLatch.countDown();
                                Thread.sleep(50);
                                concurrentReaders.decrementAndGet();
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }

                startLatch.countDown();
                holdLatch.await();
                doneLatch.await();
            }

            assertThat(maxConcurrent.get()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("Write Lock Tests | 写锁测试")
    class WriteLockTests {

        @Test
        @DisplayName("writeLock returns lock | writeLock 返回锁")
        void testWriteLock() {
            Lock<Long> writeLock = rwLock.writeLock();
            assertThat(writeLock).isNotNull();
        }

        @Test
        @DisplayName("write lock can be acquired | 写锁可以获取")
        void testWriteLockAcquire() {
            Lock<Long> writeLock = rwLock.writeLock();

            try (LockGuard<Long> guard = writeLock.lock()) {
                assertThat(writeLock.isHeldByCurrentThread()).isTrue();
            }
        }

        @Test
        @DisplayName("write lock is exclusive | 写锁是排他的")
        void testWriteLockExclusive() throws InterruptedException {
            Lock<Long> writeLock = rwLock.writeLock();
            AtomicInteger concurrentWriters = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);
            int writerCount = 3;

            CountDownLatch doneLatch = new CountDownLatch(writerCount);

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < writerCount; i++) {
                    executor.submit(() -> {
                        try (LockGuard<Long> guard = writeLock.lock()) {
                            int current = concurrentWriters.incrementAndGet();
                            maxConcurrent.updateAndGet(max -> Math.max(max, current));
                            Thread.sleep(10);
                            concurrentWriters.decrementAndGet();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }

                doneLatch.await();
            }

            assertThat(maxConcurrent.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Execute Methods Tests | 执行方法测试")
    class ExecuteMethodsTests {

        @Test
        @DisplayName("executeRead runs with read lock | executeRead 持有读锁运行")
        void testExecuteRead() {
            String result = rwLock.executeRead(() -> "read result");
            assertThat(result).isEqualTo("read result");
        }

        @Test
        @DisplayName("executeWrite runs with write lock | executeWrite 持有写锁运行")
        void testExecuteWrite() {
            AtomicInteger value = new AtomicInteger(0);

            rwLock.executeWrite(() -> value.set(42));

            assertThat(value.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("executeWrite with result | executeWrite 带结果")
        void testExecuteWriteWithResult() {
            Integer result = rwLock.executeWrite(() -> 42);
            assertThat(result).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("Read-Write Exclusion Tests | 读写排斥测试")
    class ReadWriteExclusionTests {

        @Test
        @DisplayName("write blocks while reading | 读取时写入阻塞")
        void testWriteBlocksWhileReading() throws InterruptedException {
            Lock<Long> readLock = rwLock.readLock();
            Lock<Long> writeLock = rwLock.writeLock();

            CountDownLatch readAcquired = new CountDownLatch(1);
            CountDownLatch writeAttempted = new CountDownLatch(1);
            CountDownLatch readReleased = new CountDownLatch(1);
            AtomicInteger writeAcquiredTime = new AtomicInteger(-1);

            Thread reader = Thread.ofVirtual().start(() -> {
                try (LockGuard<Long> guard = readLock.lock()) {
                    readAcquired.countDown();
                    writeAttempted.await();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    readReleased.countDown();
                }
            });

            Thread writer = Thread.ofVirtual().start(() -> {
                try {
                    readAcquired.await();
                    writeAttempted.countDown();
                    long start = System.currentTimeMillis();
                    try (LockGuard<Long> guard = writeLock.lock()) {
                        writeAcquiredTime.set((int) (System.currentTimeMillis() - start));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            reader.join();
            writer.join();

            // Writer should have waited for reader
            assertThat(writeAcquiredTime.get()).isGreaterThanOrEqualTo(50);
        }
    }
}
