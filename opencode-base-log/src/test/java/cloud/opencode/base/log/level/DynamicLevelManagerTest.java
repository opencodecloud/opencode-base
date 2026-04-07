package cloud.opencode.base.log.level;

import cloud.opencode.base.log.LogLevel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DynamicLevelManager 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
@DisplayName("DynamicLevelManager 测试")
class DynamicLevelManagerTest {

    private DynamicLevelManager manager;

    @BeforeEach
    void setUp() {
        manager = DynamicLevelManager.getInstance();
        manager.resetAll();
    }

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("getInstance 返回相同实例")
        void singletonReturnsSameInstance() {
            DynamicLevelManager instance1 = DynamicLevelManager.getInstance();
            DynamicLevelManager instance2 = DynamicLevelManager.getInstance();
            assertThat(instance1).isSameAs(instance2);
        }
    }

    @Nested
    @DisplayName("setLevel 和 getLevel 测试")
    class SetAndGetLevelTests {

        @Test
        @DisplayName("setLevel 和 getLevel 正确设置和获取级别")
        void setAndGetLevel() {
            manager.setLevel("com.example.Service", LogLevel.DEBUG);
            assertThat(manager.getLevel("com.example.Service")).isEqualTo(LogLevel.DEBUG);
        }

        @Test
        @DisplayName("getLevel 未设置时返回 null")
        void getLevelReturnsNullWhenNotSet() {
            assertThat(manager.getLevel("com.example.Unknown")).isNull();
        }

        @Test
        @DisplayName("setLevel 覆盖已有级别")
        void setLevelOverrides() {
            manager.setLevel("com.example.Service", LogLevel.DEBUG);
            manager.setLevel("com.example.Service", LogLevel.ERROR);
            assertThat(manager.getLevel("com.example.Service")).isEqualTo(LogLevel.ERROR);
        }

        @Test
        @DisplayName("setLevel null 名称抛出异常")
        void setLevelNullNameThrows() {
            assertThatThrownBy(() -> manager.setLevel(null, LogLevel.INFO))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setLevel null 级别抛出异常")
        void setLevelNullLevelThrows() {
            assertThatThrownBy(() -> manager.setLevel("test", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getLevel null 名称抛出异常")
        void getLevelNullNameThrows() {
            assertThatThrownBy(() -> manager.getLevel(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getEffectiveLevel 测试")
    class EffectiveLevelTests {

        @Test
        @DisplayName("有覆盖时返回覆盖级别")
        void effectiveLevelWithOverride() {
            manager.setLevel("com.example.Service", LogLevel.TRACE);
            LogLevel effective = manager.getEffectiveLevel("com.example.Service", LogLevel.INFO);
            assertThat(effective).isEqualTo(LogLevel.TRACE);
        }

        @Test
        @DisplayName("无覆盖时返回默认级别")
        void effectiveLevelWithoutOverride() {
            LogLevel effective = manager.getEffectiveLevel("com.example.Unknown", LogLevel.WARN);
            assertThat(effective).isEqualTo(LogLevel.WARN);
        }

        @Test
        @DisplayName("getEffectiveLevel null 参数抛出异常")
        void effectiveLevelNullThrows() {
            assertThatThrownBy(() -> manager.getEffectiveLevel(null, LogLevel.INFO))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> manager.getEffectiveLevel("test", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("resetLevel 测试")
    class ResetLevelTests {

        @Test
        @DisplayName("resetLevel 移除覆盖")
        void resetLevelRemovesOverride() {
            manager.setLevel("com.example.Service", LogLevel.DEBUG);
            manager.resetLevel("com.example.Service");
            assertThat(manager.getLevel("com.example.Service")).isNull();
            assertThat(manager.hasOverride("com.example.Service")).isFalse();
        }

        @Test
        @DisplayName("resetLevel 不存在的名称不抛异常")
        void resetLevelNonExistentIsSafe() {
            manager.resetLevel("com.example.DoesNotExist"); // should not throw
        }

        @Test
        @DisplayName("resetLevel null 名称抛出异常")
        void resetLevelNullThrows() {
            assertThatThrownBy(() -> manager.resetLevel(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("resetAll 测试")
    class ResetAllTests {

        @Test
        @DisplayName("resetAll 清除所有覆盖")
        void resetAllClearsAll() {
            manager.setLevel("logger1", LogLevel.DEBUG);
            manager.setLevel("logger2", LogLevel.WARN);
            manager.setLevel("logger3", LogLevel.ERROR);

            manager.resetAll();

            assertThat(manager.getOverrideCount()).isZero();
            assertThat(manager.getLevel("logger1")).isNull();
            assertThat(manager.getLevel("logger2")).isNull();
            assertThat(manager.getLevel("logger3")).isNull();
        }
    }

    @Nested
    @DisplayName("getAllLevels 测试")
    class GetAllLevelsTests {

        @Test
        @DisplayName("getAllLevels 返回不可修改副本")
        void getAllLevelsReturnsUnmodifiableCopy() {
            manager.setLevel("logger1", LogLevel.DEBUG);
            manager.setLevel("logger2", LogLevel.WARN);

            Map<String, LogLevel> levels = manager.getAllLevels();
            assertThat(levels).hasSize(2);
            assertThat(levels).containsEntry("logger1", LogLevel.DEBUG);
            assertThat(levels).containsEntry("logger2", LogLevel.WARN);

            assertThatThrownBy(() -> levels.put("logger3", LogLevel.ERROR))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getAllLevels 是副本，不受后续修改影响")
        void getAllLevelsIsCopy() {
            manager.setLevel("logger1", LogLevel.DEBUG);
            Map<String, LogLevel> snapshot = manager.getAllLevels();

            manager.setLevel("logger2", LogLevel.WARN);
            assertThat(snapshot).hasSize(1); // snapshot unchanged
        }
    }

    @Nested
    @DisplayName("hasOverride 测试")
    class HasOverrideTests {

        @Test
        @DisplayName("hasOverride 存在时返回 true")
        void hasOverrideReturnsTrue() {
            manager.setLevel("logger1", LogLevel.DEBUG);
            assertThat(manager.hasOverride("logger1")).isTrue();
        }

        @Test
        @DisplayName("hasOverride 不存在时返回 false")
        void hasOverrideReturnsFalse() {
            assertThat(manager.hasOverride("non-existent")).isFalse();
        }

        @Test
        @DisplayName("hasOverride null 名称抛出异常")
        void hasOverrideNullThrows() {
            assertThatThrownBy(() -> manager.hasOverride(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getOverrideCount 测试")
    class OverrideCountTests {

        @Test
        @DisplayName("getOverrideCount 返回正确数量")
        void overrideCountIsCorrect() {
            assertThat(manager.getOverrideCount()).isZero();

            manager.setLevel("logger1", LogLevel.DEBUG);
            assertThat(manager.getOverrideCount()).isEqualTo(1);

            manager.setLevel("logger2", LogLevel.WARN);
            assertThat(manager.getOverrideCount()).isEqualTo(2);

            manager.resetLevel("logger1");
            assertThat(manager.getOverrideCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("并发 set/get 不丢数据")
        void concurrentSetAndGet() throws Exception {
            int threadCount = 16;
            int operationsPerThread = 500;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            CopyOnWriteArrayList<Throwable> errors = new CopyOnWriteArrayList<>();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            try {
                for (int t = 0; t < threadCount; t++) {
                    final int threadId = t;
                    executor.submit(() -> {
                        try {
                            startLatch.await();
                            for (int i = 0; i < operationsPerThread; i++) {
                                String loggerName = "logger-" + threadId + "-" + i;
                                LogLevel level = LogLevel.values()[i % 5];
                                manager.setLevel(loggerName, level);
                                LogLevel retrieved = manager.getLevel(loggerName);
                                if (retrieved == null) {
                                    errors.add(new AssertionError(
                                            "Expected non-null level for " + loggerName));
                                }
                                manager.getEffectiveLevel(loggerName, LogLevel.INFO);
                                manager.hasOverride(loggerName);
                                manager.getOverrideCount();
                            }
                        } catch (Throwable e) {
                            errors.add(e);
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }

                startLatch.countDown();
                assertThat(doneLatch.await(30, TimeUnit.SECONDS)).isTrue();
                assertThat(errors).isEmpty();
                assertThat(manager.getOverrideCount())
                        .isEqualTo(threadCount * operationsPerThread);
            } finally {
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        }
    }
}
