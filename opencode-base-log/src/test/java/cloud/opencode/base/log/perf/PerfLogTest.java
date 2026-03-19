package cloud.opencode.base.log.perf;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PerfLog 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("PerfLog 测试")
class PerfLogTest {

    @BeforeEach
    void setUp() {
        PerfLog.setGlobalThreshold(1000);
        PerfLog.setSlowOperationConfig(SlowOperationConfig.DEFAULT);
    }

    @AfterEach
    void tearDown() {
        PerfLog.setGlobalThreshold(1000);
        PerfLog.setSlowOperationConfig(SlowOperationConfig.DEFAULT);
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(PerfLog.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = PerfLog.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("start方法测试")
    class StartTests {

        @Test
        @DisplayName("启动新的StopWatch")
        void testStart() {
            StopWatch watch = PerfLog.start("testOperation");

            assertThat(watch).isNotNull();
            assertThat(watch.isRunning()).isTrue();
            assertThat(watch.getOperation()).isEqualTo("testOperation");
        }
    }

    @Nested
    @DisplayName("timed(Runnable)方法测试")
    class TimedRunnableTests {

        @Test
        @DisplayName("执行任务并记录时间")
        void testTimedRunnable() {
            assertThatCode(() ->
                PerfLog.timed("testOp", () -> {
                    // Do nothing
                })
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("任务异常时仍记录时间")
        void testTimedRunnableWithException() {
            assertThatThrownBy(() ->
                PerfLog.timed("testOp", () -> {
                    throw new RuntimeException("test error");
                })
            ).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("timed(Callable)方法测试")
    class TimedCallableTests {

        @Test
        @DisplayName("执行任务并返回结果")
        void testTimedCallable() {
            String result = PerfLog.timed("testOp", () -> "result");

            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("任务异常时抛出RuntimeException")
        void testTimedCallableWithException() {
            assertThatThrownBy(() ->
                PerfLog.timed("testOp", () -> {
                    throw new Exception("test error");
                })
            ).isInstanceOf(RuntimeException.class)
             .hasMessageContaining("testOp");
        }
    }

    @Nested
    @DisplayName("timedWithThreshold(Runnable)方法测试")
    class TimedWithThresholdRunnableTests {

        @Test
        @DisplayName("执行任务并检查阈值")
        void testTimedWithThreshold() {
            assertThatCode(() ->
                PerfLog.timedWithThreshold("testOp", 1000, () -> {
                    // Do nothing
                })
            ).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("timedWithThreshold(Callable)方法测试")
    class TimedWithThresholdCallableTests {

        @Test
        @DisplayName("执行任务并返回结果")
        void testTimedWithThresholdCallable() {
            String result = PerfLog.timedWithThreshold("testOp", 1000, () -> "result");

            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("任务异常时抛出RuntimeException")
        void testTimedWithThresholdCallableWithException() {
            assertThatThrownBy(() ->
                PerfLog.timedWithThreshold("testOp", 1000, () -> {
                    throw new Exception("test error");
                })
            ).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("log方法测试")
    class LogTests {

        @Test
        @DisplayName("记录性能信息")
        void testLog() {
            assertThatCode(() -> PerfLog.log("testOp", 100)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("warnSlow方法测试")
    class WarnSlowTests {

        @Test
        @DisplayName("记录慢操作警告")
        void testWarnSlow() {
            assertThatCode(() -> PerfLog.warnSlow("testOp", 1500, 1000)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("setGlobalThreshold方法测试")
    class SetGlobalThresholdTests {

        @Test
        @DisplayName("设置全局阈值")
        void testSetGlobalThreshold() {
            PerfLog.setGlobalThreshold(500);

            assertThat(PerfLog.getGlobalThreshold()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("getGlobalThreshold方法测试")
    class GetGlobalThresholdTests {

        @Test
        @DisplayName("获取全局阈值")
        void testGetGlobalThreshold() {
            PerfLog.setGlobalThreshold(2000);

            assertThat(PerfLog.getGlobalThreshold()).isEqualTo(2000);
        }
    }

    @Nested
    @DisplayName("setSlowOperationConfig方法测试")
    class SetSlowOperationConfigTests {

        @Test
        @DisplayName("设置慢操作配置")
        void testSetSlowOperationConfig() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .thresholdMillis(500)
                .build();

            PerfLog.setSlowOperationConfig(config);

            assertThat(PerfLog.getSlowOperationConfig()).isSameAs(config);
        }

        @Test
        @DisplayName("设置null使用默认配置")
        void testSetSlowOperationConfigNull() {
            PerfLog.setSlowOperationConfig(null);

            assertThat(PerfLog.getSlowOperationConfig()).isEqualTo(SlowOperationConfig.DEFAULT);
        }
    }

    @Nested
    @DisplayName("getSlowOperationConfig方法测试")
    class GetSlowOperationConfigTests {

        @Test
        @DisplayName("获取慢操作配置")
        void testGetSlowOperationConfig() {
            SlowOperationConfig config = PerfLog.getSlowOperationConfig();
            assertThat(config).isNotNull();
        }
    }

    @Nested
    @DisplayName("isSlow方法测试")
    class IsSlowTests {

        @Test
        @DisplayName("超过阈值返回true")
        void testIsSlowTrue() {
            PerfLog.setGlobalThreshold(100);

            assertThat(PerfLog.isSlow(150)).isTrue();
        }

        @Test
        @DisplayName("未超过阈值返回false")
        void testIsSlowFalse() {
            PerfLog.setGlobalThreshold(100);

            assertThat(PerfLog.isSlow(50)).isFalse();
        }

        @Test
        @DisplayName("等于阈值返回false")
        void testIsSlowEqual() {
            PerfLog.setGlobalThreshold(100);

            assertThat(PerfLog.isSlow(100)).isFalse();
        }
    }
}
