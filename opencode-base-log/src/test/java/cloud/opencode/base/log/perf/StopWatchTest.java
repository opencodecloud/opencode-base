package cloud.opencode.base.log.perf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * StopWatch 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("StopWatch 测试")
class StopWatchTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(StopWatch.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("实现AutoCloseable")
        void testImplementsAutoCloseable() {
            assertThat(AutoCloseable.class.isAssignableFrom(StopWatch.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("start工厂方法测试")
    class StartFactoryMethodTests {

        @Test
        @DisplayName("创建并启动StopWatch")
        void testStart() {
            StopWatch watch = StopWatch.start("testOp");

            assertThat(watch).isNotNull();
            assertThat(watch.isRunning()).isTrue();
            assertThat(watch.getOperation()).isEqualTo("testOp");
        }
    }

    @Nested
    @DisplayName("create工厂方法测试")
    class CreateFactoryMethodTests {

        @Test
        @DisplayName("创建未启动的StopWatch")
        void testCreate() {
            StopWatch watch = StopWatch.create("testOp");

            assertThat(watch).isNotNull();
            assertThat(watch.isRunning()).isFalse();
            assertThat(watch.getOperation()).isEqualTo("testOp");
        }
    }

    @Nested
    @DisplayName("startTiming方法测试")
    class StartTimingTests {

        @Test
        @DisplayName("启动计时器")
        void testStartTiming() {
            StopWatch watch = StopWatch.create("testOp");

            StopWatch result = watch.startTiming();

            assertThat(result).isSameAs(watch);
            assertThat(watch.isRunning()).isTrue();
        }

        @Test
        @DisplayName("重复启动抛出异常")
        void testStartTimingAlreadyRunning() {
            StopWatch watch = StopWatch.start("testOp");

            assertThatThrownBy(watch::startTiming)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already running");
        }
    }

    @Nested
    @DisplayName("stop方法测试")
    class StopTests {

        @Test
        @DisplayName("停止计时器")
        void testStop() {
            StopWatch watch = StopWatch.start("testOp");

            StopWatch result = watch.stop();

            assertThat(result).isSameAs(watch);
            assertThat(watch.isRunning()).isFalse();
        }

        @Test
        @DisplayName("未启动时停止抛出异常")
        void testStopNotRunning() {
            StopWatch watch = StopWatch.create("testOp");

            assertThatThrownBy(watch::stop)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not running");
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("重置计时器")
        void testReset() {
            StopWatch watch = StopWatch.start("testOp");
            watch.stop();

            StopWatch result = watch.reset();

            assertThat(result).isSameAs(watch);
            assertThat(watch.isRunning()).isFalse();
            assertThat(watch.getElapsedMillis()).isEqualTo(0);
        }

        @Test
        @DisplayName("重置运行中的计时器")
        void testResetRunning() {
            StopWatch watch = StopWatch.start("testOp");

            watch.reset();

            assertThat(watch.isRunning()).isFalse();
        }
    }

    @Nested
    @DisplayName("getElapsedMillis方法测试")
    class GetElapsedMillisTests {

        @Test
        @DisplayName("获取停止后的毫秒耗时")
        void testGetElapsedMillisStopped() throws Exception {
            StopWatch watch = StopWatch.start("testOp");
            Thread.sleep(50);
            watch.stop();

            long elapsed = watch.getElapsedMillis();

            assertThat(elapsed).isGreaterThanOrEqualTo(40);
        }

        @Test
        @DisplayName("获取运行中的毫秒耗时")
        void testGetElapsedMillisRunning() throws Exception {
            StopWatch watch = StopWatch.start("testOp");
            Thread.sleep(50);

            long elapsed = watch.getElapsedMillis();

            assertThat(elapsed).isGreaterThanOrEqualTo(40);
        }
    }

    @Nested
    @DisplayName("getElapsedNanos方法测试")
    class GetElapsedNanosTests {

        @Test
        @DisplayName("获取纳秒耗时")
        void testGetElapsedNanos() throws Exception {
            StopWatch watch = StopWatch.start("testOp");
            Thread.sleep(10);
            watch.stop();

            long elapsed = watch.getElapsedNanos();

            assertThat(elapsed).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("getElapsed方法测试")
    class GetElapsedTests {

        @Test
        @DisplayName("获取Duration耗时")
        void testGetElapsed() throws Exception {
            StopWatch watch = StopWatch.start("testOp");
            Thread.sleep(50);
            watch.stop();

            Duration elapsed = watch.getElapsed();

            assertThat(elapsed.toMillis()).isGreaterThanOrEqualTo(40);
        }
    }

    @Nested
    @DisplayName("isRunning方法测试")
    class IsRunningTests {

        @Test
        @DisplayName("启动后返回true")
        void testIsRunningTrue() {
            StopWatch watch = StopWatch.start("testOp");
            assertThat(watch.isRunning()).isTrue();
        }

        @Test
        @DisplayName("停止后返回false")
        void testIsRunningFalse() {
            StopWatch watch = StopWatch.start("testOp");
            watch.stop();
            assertThat(watch.isRunning()).isFalse();
        }

        @Test
        @DisplayName("未启动时返回false")
        void testIsRunningNotStarted() {
            StopWatch watch = StopWatch.create("testOp");
            assertThat(watch.isRunning()).isFalse();
        }
    }

    @Nested
    @DisplayName("getOperation方法测试")
    class GetOperationTests {

        @Test
        @DisplayName("返回操作名称")
        void testGetOperation() {
            StopWatch watch = StopWatch.start("myOperation");
            assertThat(watch.getOperation()).isEqualTo("myOperation");
        }
    }

    @Nested
    @DisplayName("stopAndLog方法测试")
    class StopAndLogTests {

        @Test
        @DisplayName("停止并记录日志")
        void testStopAndLog() {
            StopWatch watch = StopWatch.start("testOp");

            assertThatCode(watch::stopAndLog).doesNotThrowAnyException();
            assertThat(watch.isRunning()).isFalse();
        }

        @Test
        @DisplayName("已停止时仍可调用")
        void testStopAndLogAlreadyStopped() {
            StopWatch watch = StopWatch.start("testOp");
            watch.stop();

            assertThatCode(watch::stopAndLog).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("带阈值停止并记录")
        void testStopAndLogWithThreshold() {
            StopWatch watch = StopWatch.start("testOp");

            assertThatCode(() -> watch.stopAndLog(1000)).doesNotThrowAnyException();
            assertThat(watch.isRunning()).isFalse();
        }

        @Test
        @DisplayName("超过阈值记录警告")
        void testStopAndLogExceedsThreshold() throws Exception {
            StopWatch watch = StopWatch.start("testOp");
            Thread.sleep(50);

            assertThatCode(() -> watch.stopAndLog(10)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("关闭运行中的计时器")
        void testCloseRunning() {
            StopWatch watch = StopWatch.start("testOp");

            assertThatCode(watch::close).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("关闭已停止的计时器")
        void testCloseStopped() {
            StopWatch watch = StopWatch.start("testOp");
            watch.stop();

            assertThatCode(watch::close).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("try-with-resources使用")
        void testTryWithResources() {
            assertThatCode(() -> {
                try (StopWatch watch = StopWatch.start("testOp")) {
                    Thread.sleep(10);
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("运行中的toString")
        void testToStringRunning() {
            StopWatch watch = StopWatch.start("testOp");

            String str = watch.toString();

            assertThat(str).contains("StopWatch");
            assertThat(str).contains("testOp");
            assertThat(str).contains("running");
        }

        @Test
        @DisplayName("停止后的toString")
        void testToStringStopped() {
            StopWatch watch = StopWatch.start("testOp");
            watch.stop();

            String str = watch.toString();

            assertThat(str).contains("StopWatch");
            assertThat(str).contains("testOp");
            assertThat(str).contains("stopped");
        }
    }
}
