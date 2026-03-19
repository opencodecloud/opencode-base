package cloud.opencode.base.lock.manager;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.ReadWriteLock;
import cloud.opencode.base.lock.local.LocalLock;
import cloud.opencode.base.lock.local.LocalReadWriteLock;
import org.junit.jupiter.api.*;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * LockManager test - 锁管理器测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class LockManagerTest {

    private LockManager manager;

    @BeforeEach
    void setUp() {
        manager = new LockManager();
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Nested
    @DisplayName("Constructor Tests | 构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should create manager")
        void defaultConstructor_shouldCreateManager() {
            LockManager m = new LockManager();
            assertThat(m).isNotNull();
            m.close();
        }

        @Test
        @DisplayName("constructor with config should apply config")
        void constructorWithConfig_shouldApplyConfig() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .build();

            LockManager m = new LockManager(config);
            Lock<Long> lock = m.getLocalLock("test");

            assertThat(((LocalLock) lock).isFair()).isTrue();
            m.close();
        }
    }

    @Nested
    @DisplayName("Local Lock Tests | 本地锁测试")
    class LocalLockTests {

        @Test
        @DisplayName("getLocalLock() should return lock")
        void getLocalLock_shouldReturnLock() {
            Lock<Long> lock = manager.getLocalLock("test");

            assertThat(lock).isNotNull();
            assertThat(lock).isInstanceOf(LocalLock.class);
        }

        @Test
        @DisplayName("getLocalLock() should return same lock for same name")
        void getLocalLock_shouldReturnSameLockForSameName() {
            Lock<Long> lock1 = manager.getLocalLock("test");
            Lock<Long> lock2 = manager.getLocalLock("test");

            assertThat(lock1).isSameAs(lock2);
        }

        @Test
        @DisplayName("getLocalLock() should return different locks for different names")
        void getLocalLock_shouldReturnDifferentLocksForDifferentNames() {
            Lock<Long> lock1 = manager.getLocalLock("lock1");
            Lock<Long> lock2 = manager.getLocalLock("lock2");

            assertThat(lock1).isNotSameAs(lock2);
        }

        @Test
        @DisplayName("executeWithLocalLock() should execute action")
        void executeWithLocalLock_shouldExecuteAction() {
            AtomicBoolean executed = new AtomicBoolean(false);

            manager.executeWithLocalLock("test", () -> {
                executed.set(true);
            });

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("executeWithLocalLock() should acquire lock")
        void executeWithLocalLock_shouldAcquireLock() {
            Lock<Long> lock = manager.getLocalLock("test");

            manager.executeWithLocalLock("test", () -> {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
            });

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }
    }

    @Nested
    @DisplayName("Read-Write Lock Tests | 读写锁测试")
    class ReadWriteLockTests {

        @Test
        @DisplayName("getLocalReadWriteLock() should return lock")
        void getLocalReadWriteLock_shouldReturnLock() {
            ReadWriteLock<Long> lock = manager.getLocalReadWriteLock("test");

            assertThat(lock).isNotNull();
            assertThat(lock).isInstanceOf(LocalReadWriteLock.class);
        }

        @Test
        @DisplayName("getLocalReadWriteLock() should return same lock for same name")
        void getLocalReadWriteLock_shouldReturnSameLockForSameName() {
            ReadWriteLock<Long> lock1 = manager.getLocalReadWriteLock("test");
            ReadWriteLock<Long> lock2 = manager.getLocalReadWriteLock("test");

            assertThat(lock1).isSameAs(lock2);
        }

        @Test
        @DisplayName("getLocalReadWriteLock() should return different locks for different names")
        void getLocalReadWriteLock_shouldReturnDifferentLocksForDifferentNames() {
            ReadWriteLock<Long> lock1 = manager.getLocalReadWriteLock("lock1");
            ReadWriteLock<Long> lock2 = manager.getLocalReadWriteLock("lock2");

            assertThat(lock1).isNotSameAs(lock2);
        }
    }

    @Nested
    @DisplayName("HasLock Tests | 锁存在性测试")
    class HasLockTests {

        @Test
        @DisplayName("hasLock() should return false for non-existent lock")
        void hasLock_shouldReturnFalseForNonExistentLock() {
            assertThat(manager.hasLock("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("hasLock() should return true for local lock")
        void hasLock_shouldReturnTrueForLocalLock() {
            manager.getLocalLock("test");

            assertThat(manager.hasLock("test")).isTrue();
        }

        @Test
        @DisplayName("hasLock() should return true for read-write lock")
        void hasLock_shouldReturnTrueForReadWriteLock() {
            manager.getLocalReadWriteLock("test");

            assertThat(manager.hasLock("test")).isTrue();
        }
    }

    @Nested
    @DisplayName("RemoveLock Tests | 移除锁测试")
    class RemoveLockTests {

        @Test
        @DisplayName("removeLock() should return false for non-existent lock")
        void removeLock_shouldReturnFalseForNonExistentLock() {
            assertThat(manager.removeLock("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("removeLock() should return true and remove local lock")
        void removeLock_shouldReturnTrueAndRemoveLocalLock() {
            manager.getLocalLock("test");

            boolean removed = manager.removeLock("test");

            assertThat(removed).isTrue();
            assertThat(manager.hasLock("test")).isFalse();
        }

        @Test
        @DisplayName("removeLock() should return true and remove read-write lock")
        void removeLock_shouldReturnTrueAndRemoveReadWriteLock() {
            manager.getLocalReadWriteLock("test");

            boolean removed = manager.removeLock("test");

            assertThat(removed).isTrue();
            assertThat(manager.hasLock("test")).isFalse();
        }

        @Test
        @DisplayName("removeLock() should close the lock")
        void removeLock_shouldCloseTheLock() {
            Lock<Long> lock = manager.getLocalLock("test");

            manager.removeLock("test");

            // After removal, getting the same name should return a new lock
            Lock<Long> newLock = manager.getLocalLock("test");
            assertThat(newLock).isNotSameAs(lock);
        }
    }

    @Nested
    @DisplayName("Managed Locks Tests | 托管锁测试")
    class ManagedLocksTests {

        @Test
        @DisplayName("getManagedLockNames() should return empty set initially")
        void getManagedLockNames_shouldReturnEmptySetInitially() {
            Set<String> names = manager.getManagedLockNames();

            assertThat(names).isEmpty();
        }

        @Test
        @DisplayName("getManagedLockNames() should return all lock names")
        void getManagedLockNames_shouldReturnAllLockNames() {
            manager.getLocalLock("lock1");
            manager.getLocalLock("lock2");
            manager.getLocalReadWriteLock("rwlock1");

            Set<String> names = manager.getManagedLockNames();

            assertThat(names).containsExactlyInAnyOrder("lock1", "lock2", "rwlock1");
        }

        @Test
        @DisplayName("getManagedLockNames() should return unmodifiable set")
        void getManagedLockNames_shouldReturnUnmodifiableSet() {
            manager.getLocalLock("test");

            Set<String> names = manager.getManagedLockNames();

            assertThatThrownBy(() -> names.add("newLock"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getManagedLockCount() should return 0 initially")
        void getManagedLockCount_shouldReturnZeroInitially() {
            assertThat(manager.getManagedLockCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("getManagedLockCount() should return correct count")
        void getManagedLockCount_shouldReturnCorrectCount() {
            manager.getLocalLock("lock1");
            manager.getLocalLock("lock2");
            manager.getLocalReadWriteLock("rwlock1");

            assertThat(manager.getManagedLockCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Close Tests | 关闭测试")
    class CloseTests {

        @Test
        @DisplayName("close() should clear all locks")
        void close_shouldClearAllLocks() {
            manager.getLocalLock("lock1");
            manager.getLocalLock("lock2");

            manager.close();

            assertThat(manager.getManagedLockCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("close() should be idempotent")
        void close_shouldBeIdempotent() {
            manager.getLocalLock("test");

            manager.close();
            assertThatCode(() -> manager.close()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Concurrency Tests | 并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("should handle concurrent lock creation")
        void shouldHandleConcurrentLockCreation() throws Exception {
            AtomicInteger lockCount = new AtomicInteger(0);
            int threads = 10;

            Thread[] threadArray = new Thread[threads];
            for (int i = 0; i < threads; i++) {
                final int id = i;
                threadArray[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        manager.getLocalLock("lock" + (id * 100 + j));
                        lockCount.incrementAndGet();
                    }
                });
            }

            for (Thread t : threadArray) {
                t.start();
            }

            for (Thread t : threadArray) {
                t.join();
            }

            assertThat(lockCount.get()).isEqualTo(threads * 100);
            assertThat(manager.getManagedLockCount()).isEqualTo(threads * 100);
        }

        @Test
        @DisplayName("should handle concurrent access to same lock")
        void shouldHandleConcurrentAccessToSameLock() throws Exception {
            AtomicInteger counter = new AtomicInteger(0);
            int threads = 10;

            Thread[] threadArray = new Thread[threads];
            for (int i = 0; i < threads; i++) {
                threadArray[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        manager.executeWithLocalLock("shared", counter::incrementAndGet);
                    }
                });
            }

            for (Thread t : threadArray) {
                t.start();
            }

            for (Thread t : threadArray) {
                t.join();
            }

            assertThat(counter.get()).isEqualTo(threads * 100);
        }
    }
}
