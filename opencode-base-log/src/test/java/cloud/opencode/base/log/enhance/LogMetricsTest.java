package cloud.opencode.base.log.enhance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * LogMetrics 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("LogMetrics 测试")
class LogMetricsTest {

    @BeforeEach
    void setUp() {
        LogMetrics.reset();
    }

    @AfterEach
    void tearDown() {
        LogMetrics.reset();
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(LogMetrics.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = LogMetrics.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("debugAndCount方法测试")
    class DebugAndCountTests {

        @Test
        @DisplayName("记录DEBUG日志并计数")
        void testDebugAndCount() {
            LogMetrics.debugAndCount("test.debug", "Debug message");

            assertThat(LogMetrics.getCount("test.debug")).isEqualTo(1);
            assertThat(LogMetrics.getStats().debugCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("带参数的DEBUG日志")
        void testDebugAndCountWithArgs() {
            LogMetrics.debugAndCount("test.debug", "Debug {} message", "test");

            assertThat(LogMetrics.getCount("test.debug")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("infoAndCount方法测试")
    class InfoAndCountTests {

        @Test
        @DisplayName("记录INFO日志并计数")
        void testInfoAndCount() {
            LogMetrics.infoAndCount("test.info", "Info message");

            assertThat(LogMetrics.getCount("test.info")).isEqualTo(1);
            assertThat(LogMetrics.getStats().infoCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("多次调用累计计数")
        void testInfoAndCountMultiple() {
            LogMetrics.infoAndCount("test.info", "Message 1");
            LogMetrics.infoAndCount("test.info", "Message 2");
            LogMetrics.infoAndCount("test.info", "Message 3");

            assertThat(LogMetrics.getCount("test.info")).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("warnAndCount方法测试")
    class WarnAndCountTests {

        @Test
        @DisplayName("记录WARN日志并计数")
        void testWarnAndCount() {
            LogMetrics.warnAndCount("test.warn", "Warn message");

            assertThat(LogMetrics.getCount("test.warn")).isEqualTo(1);
            assertThat(LogMetrics.getStats().warnCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("errorAndCount方法测试")
    class ErrorAndCountTests {

        @Test
        @DisplayName("记录ERROR日志并计数")
        void testErrorAndCount() {
            LogMetrics.errorAndCount("test.error", "Error message");

            assertThat(LogMetrics.getCount("test.error")).isEqualTo(1);
            assertThat(LogMetrics.getStats().errorCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("带异常的ERROR日志")
        void testErrorAndCountWithException() {
            LogMetrics.errorAndCount("test.error", "Error message", new RuntimeException("test"));

            assertThat(LogMetrics.getCount("test.error")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("debugAndTime方法测试")
    class DebugAndTimeTests {

        @Test
        @DisplayName("记录DEBUG日志并计时")
        void testDebugAndTime() throws Exception {
            String result = LogMetrics.debugAndTime("test.time", "Debug", () -> "result");

            assertThat(result).isEqualTo("result");
            assertThat(LogMetrics.getTimingStats("test.time")).isNotNull();
        }

        @Test
        @DisplayName("任务失败记录失败计时")
        void testDebugAndTimeFailure() {
            assertThatThrownBy(() ->
                LogMetrics.debugAndTime("test.time", "Debug", () -> {
                    throw new RuntimeException("failure");
                })
            ).isInstanceOf(RuntimeException.class);

            assertThat(LogMetrics.getTimingStats("test.time.failed")).isNotNull();
        }
    }

    @Nested
    @DisplayName("infoAndTime方法测试")
    class InfoAndTimeTests {

        @Test
        @DisplayName("Callable版本记录INFO日志并计时")
        void testInfoAndTimeCallable() throws Exception {
            Integer result = LogMetrics.infoAndTime("test.time", "Info", () -> 42);

            assertThat(result).isEqualTo(42);
            assertThat(LogMetrics.getTimingStats("test.time")).isNotNull();
        }

        @Test
        @DisplayName("Runnable版本记录INFO日志并计时")
        void testInfoAndTimeRunnable() {
            LogMetrics.infoAndTime("test.time", "Info", () -> {
                // do nothing
            });

            assertThat(LogMetrics.getTimingStats("test.time")).isNotNull();
        }

        @Test
        @DisplayName("Runnable失败记录失败计时")
        void testInfoAndTimeRunnableFailure() {
            assertThatThrownBy(() ->
                LogMetrics.infoAndTime("test.time", "Info", () -> {
                    throw new RuntimeException("failure");
                })
            ).isInstanceOf(RuntimeException.class);

            assertThat(LogMetrics.getTimingStats("test.time.failed")).isNotNull();
        }

        @Test
        @DisplayName("Callable失败记录失败计时")
        void testInfoAndTimeCallableFailure() {
            assertThatThrownBy(() ->
                LogMetrics.infoAndTime("test.time", "Info", () -> {
                    throw new Exception("failure");
                })
            ).isInstanceOf(Exception.class);

            assertThat(LogMetrics.getTimingStats("test.time.failed")).isNotNull();
        }
    }

    @Nested
    @DisplayName("getCount方法测试")
    class GetCountTests {

        @Test
        @DisplayName("获取存在的计数")
        void testGetCountExisting() {
            LogMetrics.infoAndCount("test.metric", "message");

            assertThat(LogMetrics.getCount("test.metric")).isEqualTo(1);
        }

        @Test
        @DisplayName("获取不存在的计数返回0")
        void testGetCountNotExisting() {
            assertThat(LogMetrics.getCount("nonexistent")).isZero();
        }
    }

    @Nested
    @DisplayName("getTimingStats方法测试")
    class GetTimingStatsTests {

        @Test
        @DisplayName("获取存在的计时统计")
        void testGetTimingStatsExisting() throws Exception {
            LogMetrics.infoAndTime("test.timing", "message", () -> "result");

            LogMetrics.TimingStats stats = LogMetrics.getTimingStats("test.timing");

            assertThat(stats).isNotNull();
            assertThat(stats.getCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("获取不存在的计时统计返回null")
        void testGetTimingStatsNotExisting() {
            assertThat(LogMetrics.getTimingStats("nonexistent")).isNull();
        }
    }

    @Nested
    @DisplayName("getStats方法测试")
    class GetStatsTests {

        @Test
        @DisplayName("获取总体统计")
        void testGetStats() {
            LogMetrics.infoAndCount("test.info", "info");
            LogMetrics.errorAndCount("test.error", "error");

            LogMetrics.LogStats stats = LogMetrics.getStats();

            assertThat(stats).isNotNull();
            assertThat(stats.totalLogs()).isEqualTo(2);
            assertThat(stats.infoCount()).isEqualTo(1);
            assertThat(stats.errorCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("重置所有统计")
        void testReset() {
            LogMetrics.infoAndCount("test", "message");
            LogMetrics.reset();

            LogMetrics.LogStats stats = LogMetrics.getStats();

            assertThat(stats.totalLogs()).isZero();
            assertThat(stats.namedCounts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("LogStats记录测试")
    class LogStatsTests {

        @Test
        @DisplayName("getNamedCount方法")
        void testGetNamedCount() {
            LogMetrics.infoAndCount("named.metric", "message");

            LogMetrics.LogStats stats = LogMetrics.getStats();

            assertThat(stats.getNamedCount("named.metric")).isEqualTo(1);
            assertThat(stats.getNamedCount("nonexistent")).isZero();
        }

        @Test
        @DisplayName("toString方法")
        void testLogStatsToString() {
            LogMetrics.infoAndCount("test", "message");

            LogMetrics.LogStats stats = LogMetrics.getStats();

            assertThat(stats.toString()).contains("LogStats");
            assertThat(stats.toString()).contains("total=1");
        }

        @Test
        @DisplayName("namedCounts返回Map")
        void testNamedCounts() {
            LogMetrics.infoAndCount("test1", "message");
            LogMetrics.infoAndCount("test2", "message");

            Map<String, Long> counts = LogMetrics.getStats().namedCounts();

            assertThat(counts).containsKeys("test1", "test2");
        }
    }

    @Nested
    @DisplayName("TimingStats类测试")
    class TimingStatsTests {

        @Test
        @DisplayName("记录和获取统计")
        void testTimingStats() throws Exception {
            LogMetrics.infoAndTime("timing.test", "test", () -> {
                Thread.sleep(10);
                return "result";
            });

            LogMetrics.TimingStats stats = LogMetrics.getTimingStats("timing.test");

            assertThat(stats.getCount()).isEqualTo(1);
            assertThat(stats.getTotalMillis()).isGreaterThanOrEqualTo(0);
            assertThat(stats.getAverageMillis()).isGreaterThanOrEqualTo(0);
            assertThat(stats.getMinMillis()).isGreaterThanOrEqualTo(0);
            assertThat(stats.getMaxMillis()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("toString方法")
        void testTimingStatsToString() throws Exception {
            LogMetrics.infoAndTime("timing.test", "test", () -> "result");

            LogMetrics.TimingStats stats = LogMetrics.getTimingStats("timing.test");

            assertThat(stats.toString()).contains("TimingStats");
            assertThat(stats.toString()).contains("count=1");
        }

        @Test
        @DisplayName("无记录时getMinMillis返回0")
        void testTimingStatsEmptyMin() {
            LogMetrics.TimingStats stats = new LogMetrics.TimingStats();
            assertThat(stats.getMinMillis()).isZero();
        }

        @Test
        @DisplayName("无记录时getAverageMillis返回0")
        void testTimingStatsEmptyAverage() {
            LogMetrics.TimingStats stats = new LogMetrics.TimingStats();
            assertThat(stats.getAverageMillis()).isZero();
        }
    }
}
