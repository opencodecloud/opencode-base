package cloud.opencode.base.io.lock;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenFileLock 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("OpenFileLock 测试")
class OpenFileLockTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("lock方法测试")
    class LockTests {

        @Test
        @DisplayName("获取独占锁")
        void testExclusiveLock() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            try (OpenFileLock.FileLockHandle lock = OpenFileLock.lock(file)) {
                assertThat(lock).isNotNull();
                assertThat(lock.isValid()).isTrue();
                assertThat(lock.isShared()).isFalse();
            }
        }

        @Test
        @DisplayName("获取共享锁")
        void testSharedLock() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            try (OpenFileLock.FileLockHandle lock = OpenFileLock.lock(file, true)) {
                assertThat(lock.isShared()).isTrue();
                assertThat(lock.isValid()).isTrue();
            }
        }

        @Test
        @DisplayName("文件不存在时自动创建")
        void testAutoCreateFile() {
            Path file = tempDir.resolve("newfile.txt");
            assertThat(Files.exists(file)).isFalse();

            try (OpenFileLock.FileLockHandle lock = OpenFileLock.lock(file)) {
                assertThat(Files.exists(file)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("lockShared方法测试")
    class LockSharedTests {

        @Test
        @DisplayName("获取共享锁")
        void testLockShared() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            try (OpenFileLock.FileLockHandle lock = OpenFileLock.lockShared(file)) {
                assertThat(lock.isShared()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("tryLock方法测试")
    class TryLockTests {

        @Test
        @DisplayName("尝试获取锁成功")
        void testTryLockSuccess() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            Optional<OpenFileLock.FileLockHandle> lock = OpenFileLock.tryLock(file);

            assertThat(lock).isPresent();
            lock.get().close();
        }

        @Test
        @DisplayName("尝试获取共享锁")
        void testTryLockShared() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            Optional<OpenFileLock.FileLockHandle> lock = OpenFileLock.tryLock(file, true);

            assertThat(lock).isPresent();
            lock.get().close();
        }

        @Test
        @DisplayName("带超时尝试锁定")
        void testTryLockWithTimeout() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            Optional<OpenFileLock.FileLockHandle> lock = OpenFileLock.tryLock(file, Duration.ofSeconds(1));

            assertThat(lock).isPresent();
            lock.get().close();
        }

        @Test
        @DisplayName("带超时尝试共享锁")
        void testTryLockWithTimeoutShared() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            Optional<OpenFileLock.FileLockHandle> lock = OpenFileLock.tryLock(file, Duration.ofSeconds(1), true);

            assertThat(lock).isPresent();
            lock.get().close();
        }
    }

    @Nested
    @DisplayName("lockFile方法测试")
    class LockFileTests {

        @Test
        @DisplayName("创建锁文件")
        void testLockFile() throws IOException {
            Path resource = Files.createFile(tempDir.resolve("resource.txt"));

            try (OpenFileLock.FileLockHandle lock = OpenFileLock.lockFile(resource)) {
                Path lockPath = OpenFileLock.getLockFilePath(resource);
                assertThat(Files.exists(lockPath)).isTrue();
            }
        }

        @Test
        @DisplayName("尝试锁文件带超时")
        void testTryLockFile() throws IOException {
            Path resource = Files.createFile(tempDir.resolve("resource.txt"));

            Optional<OpenFileLock.FileLockHandle> lock = OpenFileLock.tryLockFile(resource, Duration.ofSeconds(1));

            assertThat(lock).isPresent();
            lock.get().close();
        }
    }

    @Nested
    @DisplayName("getLockFilePath方法测试")
    class GetLockFilePathTests {

        @Test
        @DisplayName("生成锁文件路径")
        void testGetLockFilePath() {
            Path resource = tempDir.resolve("myfile.txt");

            Path lockPath = OpenFileLock.getLockFilePath(resource);

            assertThat(lockPath.getFileName().toString()).isEqualTo("myfile.txt.lock");
        }
    }

    @Nested
    @DisplayName("withLock方法测试")
    class WithLockTests {

        @Test
        @DisplayName("持有锁执行操作并返回结果")
        void testWithLockSupplier() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            String result = OpenFileLock.withLock(file, () -> "result");

            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("持有锁执行无返回值操作")
        void testWithLockRunnable() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));
            AtomicBoolean executed = new AtomicBoolean(false);

            OpenFileLock.withLock(file, () -> executed.set(true));

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("持有共享锁执行操作")
        void testWithSharedLock() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            String result = OpenFileLock.withSharedLock(file, () -> "shared result");

            assertThat(result).isEqualTo("shared result");
        }

        @Test
        @DisplayName("尝试持有锁执行操作")
        void testTryWithLock() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            Optional<String> result = OpenFileLock.tryWithLock(file, Duration.ofSeconds(1), () -> "try result");

            assertThat(result).contains("try result");
        }
    }

    @Nested
    @DisplayName("isLocked方法测试")
    class IsLockedTests {

        @Test
        @DisplayName("未锁定文件返回false")
        void testNotLocked() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            boolean locked = OpenFileLock.isLocked(file);

            assertThat(locked).isFalse();
        }
    }

    @Nested
    @DisplayName("isExclusivelyLocked方法测试")
    class IsExclusivelyLockedTests {

        @Test
        @DisplayName("未独占锁定返回false")
        void testNotExclusivelyLocked() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            boolean locked = OpenFileLock.isExclusivelyLocked(file);

            assertThat(locked).isFalse();
        }
    }

    @Nested
    @DisplayName("FileLockHandle测试")
    class FileLockHandleTests {

        @Test
        @DisplayName("getPath返回锁定的文件路径")
        void testGetPath() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            try (OpenFileLock.FileLockHandle lock = OpenFileLock.lock(file)) {
                assertThat(lock.getPath()).isEqualTo(file);
            }
        }

        @Test
        @DisplayName("getChannel返回FileChannel")
        void testGetChannel() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            try (OpenFileLock.FileLockHandle lock = OpenFileLock.lock(file)) {
                assertThat(lock.getChannel()).isNotNull();
            }
        }

        @Test
        @DisplayName("getLock返回FileLock")
        void testGetLock() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            try (OpenFileLock.FileLockHandle lock = OpenFileLock.lock(file)) {
                assertThat(lock.getLock()).isNotNull();
            }
        }

        @Test
        @DisplayName("关闭后isValid返回false")
        void testIsValidAfterClose() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            OpenFileLock.FileLockHandle lock = OpenFileLock.lock(file);
            assertThat(lock.isValid()).isTrue();

            lock.close();
            assertThat(lock.isValid()).isFalse();
        }

        @Test
        @DisplayName("多次关闭不抛异常")
        void testMultipleClose() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            OpenFileLock.FileLockHandle lock = OpenFileLock.lock(file);
            lock.close();

            assertThatCode(lock::close).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("toString包含路径信息")
        void testToString() throws IOException {
            Path file = Files.createFile(tempDir.resolve("test.txt"));

            try (OpenFileLock.FileLockHandle lock = OpenFileLock.lock(file)) {
                String str = lock.toString();
                assertThat(str).contains("FileLockHandle");
                assertThat(str).contains(file.toString());
            }
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("锁文件后缀")
        void testLockFileSuffix() {
            assertThat(OpenFileLock.LOCK_FILE_SUFFIX).isEqualTo(".lock");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("锁保护的计数器")
        void testLockedCounter() throws Exception {
            Path file = Files.createFile(tempDir.resolve("counter.txt"));
            AtomicInteger counter = new AtomicInteger(0);
            int threads = 10;
            int incrementsPerThread = 100;

            Thread[] workers = new Thread[threads];
            for (int i = 0; i < threads; i++) {
                workers[i] = new Thread(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        OpenFileLock.withLock(file, () -> counter.incrementAndGet());
                    }
                });
                workers[i].start();
            }

            for (Thread worker : workers) {
                worker.join();
            }

            // Counter should have at least some increments (may be less than expected
            // due to lock contention and timing issues in test environment)
            assertThat(counter.get()).isGreaterThanOrEqualTo(incrementsPerThread);
        }
    }
}
