package cloud.opencode.base.date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * StopWatch 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("StopWatch 测试")
class StopWatchTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("无参构造创建空名称计时器")
        void testDefaultConstructor() {
            StopWatch stopWatch = new StopWatch();

            assertThat(stopWatch).isNotNull();
            assertThat(stopWatch.getName()).isEmpty();
            assertThat(stopWatch.isRunning()).isFalse();
        }

        @Test
        @DisplayName("带名称构造创建命名计时器")
        void testConstructorWithName() {
            StopWatch stopWatch = new StopWatch("my-timer");

            assertThat(stopWatch.getName()).isEqualTo("my-timer");
            assertThat(stopWatch.isRunning()).isFalse();
        }

        @Test
        @DisplayName("null名称转为空字符串")
        void testConstructorWithNullName() {
            StopWatch stopWatch = new StopWatch(null);

            assertThat(stopWatch.getName()).isEmpty();
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("createStarted() 创建并启动计时器")
        void testCreateStarted() {
            StopWatch stopWatch = StopWatch.createStarted();

            assertThat(stopWatch.isRunning()).isTrue();
        }

        @Test
        @DisplayName("createStarted(name) 创建命名并启动的计时器")
        void testCreateStartedWithName() {
            StopWatch stopWatch = StopWatch.createStarted("test-timer");

            assertThat(stopWatch.getName()).isEqualTo("test-timer");
            assertThat(stopWatch.isRunning()).isTrue();
        }

        @Test
        @DisplayName("time(Runnable) 计时代码块")
        void testTimeRunnable() {
            Duration duration = StopWatch.time(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            assertThat(duration.toMillis()).isGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("time(taskName, Runnable) 计时命名代码块")
        void testTimeRunnableWithName() {
            Duration duration = StopWatch.time("my-task", () -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            assertThat(duration.toMillis()).isGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("time() null runnable抛出异常")
        void testTimeNullRunnable() {
            assertThatThrownBy(() -> StopWatch.time(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("time(taskName, null) null runnable抛出异常")
        void testTimeWithNameNullRunnable() {
            assertThatThrownBy(() -> StopWatch.time("task", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("启动和停止测试")
    class StartStopTests {

        @Test
        @DisplayName("start(taskName) 启动计时器")
        void testStart() {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("task1");

            assertThat(stopWatch.isRunning()).isTrue();
        }

        @Test
        @DisplayName("stop() 停止计时器并返回Duration")
        void testStop() throws InterruptedException {
            StopWatch stopWatch = StopWatch.createStarted();
            Thread.sleep(10);
            Duration elapsed = stopWatch.stop();

            assertThat(stopWatch.isRunning()).isFalse();
            assertThat(elapsed.toNanos()).isPositive();
        }

        @Test
        @DisplayName("重复启动抛出异常")
        void testDoubleStartThrowsException() {
            StopWatch stopWatch = StopWatch.createStarted();

            assertThatThrownBy(() -> stopWatch.start("another"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already running");
        }

        @Test
        @DisplayName("未启动时停止抛出异常")
        void testStopWithoutStartThrowsException() {
            StopWatch stopWatch = new StopWatch();

            assertThatThrownBy(stopWatch::stop)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not running");
        }

        @Test
        @DisplayName("start() null任务名转为空字符串")
        void testStartWithNullTaskName() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start(null);
            Thread.sleep(5);
            stopWatch.stop();

            assertThat(stopWatch.getTasks().getFirst().name()).isEmpty();
        }
    }

    @Nested
    @DisplayName("重置测试")
    class ResetTests {

        @Test
        @DisplayName("reset() 重置计时器")
        void testReset() throws InterruptedException {
            StopWatch stopWatch = StopWatch.createStarted();
            Thread.sleep(10);
            stopWatch.stop();

            assertThat(stopWatch.getTotalTimeMillis()).isPositive();

            stopWatch.reset();

            assertThat(stopWatch.getTotalTimeMillis()).isEqualTo(0);
            assertThat(stopWatch.getTaskCount()).isEqualTo(0);
            assertThat(stopWatch.isRunning()).isFalse();
        }

        @Test
        @DisplayName("reset() 清空任务列表")
        void testResetClearsTasks() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("task1");
            Thread.sleep(5);
            stopWatch.stop();
            stopWatch.start("task2");
            Thread.sleep(5);
            stopWatch.stop();

            assertThat(stopWatch.getTaskCount()).isEqualTo(2);

            stopWatch.reset();

            assertThat(stopWatch.getTasks()).isEmpty();
        }
    }

    @Nested
    @DisplayName("split测试")
    class SplitTests {

        @Test
        @DisplayName("split() 分割并开始新任务")
        void testSplit() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("task1");
            Thread.sleep(10);
            Duration task1Duration = stopWatch.split("task2");
            Thread.sleep(10);
            stopWatch.stop();

            assertThat(task1Duration.toNanos()).isPositive();
            assertThat(stopWatch.getTaskCount()).isEqualTo(2);
            assertThat(stopWatch.getTasks().get(0).name()).isEqualTo("task1");
            assertThat(stopWatch.getTasks().get(1).name()).isEqualTo("task2");
        }
    }

    @Nested
    @DisplayName("时间测量测试")
    class TimeMeasurementTests {

        @Test
        @DisplayName("getTotalTimeNanos() 返回纳秒级总时间")
        void testGetTotalTimeNanos() throws InterruptedException {
            StopWatch stopWatch = StopWatch.createStarted();
            Thread.sleep(10);
            stopWatch.stop();

            assertThat(stopWatch.getTotalTimeNanos()).isPositive();
        }

        @Test
        @DisplayName("getTotalTimeMillis() 返回毫秒级总时间")
        void testGetTotalTimeMillis() throws InterruptedException {
            StopWatch stopWatch = StopWatch.createStarted();
            Thread.sleep(20);
            stopWatch.stop();

            assertThat(stopWatch.getTotalTimeMillis()).isGreaterThanOrEqualTo(10);
        }

        @Test
        @DisplayName("getTotalTimeSeconds() 返回秒级总时间")
        void testGetTotalTimeSeconds() throws InterruptedException {
            StopWatch stopWatch = StopWatch.createStarted();
            Thread.sleep(50);
            stopWatch.stop();

            assertThat(stopWatch.getTotalTimeSeconds()).isGreaterThan(0);
        }

        @Test
        @DisplayName("getTotalDuration() 返回Duration")
        void testGetTotalDuration() throws InterruptedException {
            StopWatch stopWatch = StopWatch.createStarted();
            Thread.sleep(10);
            stopWatch.stop();

            Duration duration = stopWatch.getTotalDuration();
            assertThat(duration.toNanos()).isPositive();
        }

        @Test
        @DisplayName("getCurrentTimeNanos() 运行中返回当前时间")
        void testGetCurrentTimeNanosWhileRunning() throws InterruptedException {
            StopWatch stopWatch = StopWatch.createStarted();
            Thread.sleep(10);

            long current = stopWatch.getCurrentTimeNanos();

            assertThat(current).isPositive();
            assertThat(stopWatch.isRunning()).isTrue();
        }

        @Test
        @DisplayName("getCurrentTimeNanos() 停止后返回总时间")
        void testGetCurrentTimeNanosAfterStop() throws InterruptedException {
            StopWatch stopWatch = StopWatch.createStarted();
            Thread.sleep(10);
            stopWatch.stop();

            assertThat(stopWatch.getCurrentTimeNanos()).isEqualTo(stopWatch.getTotalTimeNanos());
        }
    }

    @Nested
    @DisplayName("多任务测试")
    class MultiTaskTests {

        @Test
        @DisplayName("多任务时间累加")
        void testMultipleTasksAccumulate() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();

            stopWatch.start("task1");
            Thread.sleep(10);
            stopWatch.stop();

            stopWatch.start("task2");
            Thread.sleep(10);
            stopWatch.stop();

            assertThat(stopWatch.getTaskCount()).isEqualTo(2);
            assertThat(stopWatch.getTotalTimeMillis()).isGreaterThanOrEqualTo(15);
        }

        @Test
        @DisplayName("getTaskCount() 返回任务数量")
        void testGetTaskCount() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();

            stopWatch.start("task1");
            Thread.sleep(5);
            stopWatch.stop();

            stopWatch.start("task2");
            Thread.sleep(5);
            stopWatch.stop();

            stopWatch.start("task3");
            Thread.sleep(5);
            stopWatch.stop();

            assertThat(stopWatch.getTaskCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("getTasks() 返回所有任务信息")
        void testGetTasks() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();

            stopWatch.start("task1");
            Thread.sleep(5);
            stopWatch.stop();

            stopWatch.start("task2");
            Thread.sleep(5);
            stopWatch.stop();

            List<StopWatch.TaskInfo> tasks = stopWatch.getTasks();

            assertThat(tasks).hasSize(2);
            assertThat(tasks.get(0).name()).isEqualTo("task1");
            assertThat(tasks.get(1).name()).isEqualTo("task2");
        }

        @Test
        @DisplayName("getTasks() 返回不可修改的列表")
        void testGetTasksReturnsUnmodifiableList() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("task1");
            Thread.sleep(5);
            stopWatch.stop();

            List<StopWatch.TaskInfo> tasks = stopWatch.getTasks();

            assertThatThrownBy(() -> tasks.add(new StopWatch.TaskInfo("test", 100)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("TaskInfo 测试")
    class TaskInfoTests {

        @Test
        @DisplayName("TaskInfo name() 返回任务名")
        void testTaskInfoName() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("my-task");
            Thread.sleep(5);
            stopWatch.stop();

            StopWatch.TaskInfo task = stopWatch.getTasks().getFirst();
            assertThat(task.name()).isEqualTo("my-task");
        }

        @Test
        @DisplayName("TaskInfo timeNanos() 返回纳秒")
        void testTaskInfoTimeNanos() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("my-task");
            Thread.sleep(10);
            stopWatch.stop();

            StopWatch.TaskInfo task = stopWatch.getTasks().getFirst();
            assertThat(task.timeNanos()).isPositive();
        }

        @Test
        @DisplayName("TaskInfo timeMillis() 返回毫秒")
        void testTaskInfoTimeMillis() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("my-task");
            Thread.sleep(15);
            stopWatch.stop();

            StopWatch.TaskInfo task = stopWatch.getTasks().getFirst();
            assertThat(task.timeMillis()).isGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("TaskInfo timeSeconds() 返回秒")
        void testTaskInfoTimeSeconds() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("my-task");
            Thread.sleep(20);
            stopWatch.stop();

            StopWatch.TaskInfo task = stopWatch.getTasks().getFirst();
            assertThat(task.timeSeconds()).isGreaterThan(0);
        }

        @Test
        @DisplayName("TaskInfo duration() 返回Duration")
        void testTaskInfoDuration() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("my-task");
            Thread.sleep(10);
            stopWatch.stop();

            StopWatch.TaskInfo task = stopWatch.getTasks().getFirst();
            Duration duration = task.duration();
            assertThat(duration.toNanos()).isPositive();
        }

        @Test
        @DisplayName("TaskInfo toString() 返回格式化字符串")
        void testTaskInfoToString() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("my-task");
            Thread.sleep(5);
            stopWatch.stop();

            StopWatch.TaskInfo task = stopWatch.getTasks().getFirst();
            String str = task.toString();

            assertThat(str).contains("my-task");
        }

        @Test
        @DisplayName("TaskInfo record构造")
        void testTaskInfoRecord() {
            StopWatch.TaskInfo task = new StopWatch.TaskInfo("test", 1_000_000L);

            assertThat(task.name()).isEqualTo("test");
            assertThat(task.timeNanos()).isEqualTo(1_000_000L);
            assertThat(task.timeMillis()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("状态查询测试")
    class StateQueryTests {

        @Test
        @DisplayName("isRunning() 返回运行状态")
        void testIsRunning() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();

            assertThat(stopWatch.isRunning()).isFalse();

            stopWatch.start("task");
            assertThat(stopWatch.isRunning()).isTrue();

            Thread.sleep(5);
            stopWatch.stop();
            assertThat(stopWatch.isRunning()).isFalse();
        }

        @Test
        @DisplayName("getName() 返回计时器名称")
        void testGetName() {
            StopWatch stopWatch = new StopWatch("my-watch");
            assertThat(stopWatch.getName()).isEqualTo("my-watch");
        }
    }

    @Nested
    @DisplayName("格式化输出测试")
    class FormatOutputTests {

        @Test
        @DisplayName("toString() 返回简短摘要")
        void testToString() throws InterruptedException {
            StopWatch stopWatch = new StopWatch("my-watch");
            stopWatch.start("task1");
            Thread.sleep(5);
            stopWatch.stop();

            String result = stopWatch.toString();

            assertThat(result).contains("my-watch");
        }

        @Test
        @DisplayName("shortSummary() 返回简短摘要")
        void testShortSummary() throws InterruptedException {
            StopWatch stopWatch = new StopWatch("my-watch");
            stopWatch.start("task1");
            Thread.sleep(5);
            stopWatch.stop();

            String summary = stopWatch.shortSummary();

            assertThat(summary).contains("my-watch");
            assertThat(summary).contains("StopWatch");
        }

        @Test
        @DisplayName("shortSummary() 无名称时返回格式正确")
        void testShortSummaryNoName() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("task1");
            Thread.sleep(5);
            stopWatch.stop();

            String summary = stopWatch.shortSummary();

            assertThat(summary).startsWith("StopWatch:");
        }

        @Test
        @DisplayName("prettyPrint() 返回格式化输出")
        void testPrettyPrint() throws InterruptedException {
            StopWatch stopWatch = new StopWatch("my-watch");

            stopWatch.start("task1");
            Thread.sleep(5);
            stopWatch.stop();

            stopWatch.start("task2");
            Thread.sleep(5);
            stopWatch.stop();

            String prettyPrint = stopWatch.prettyPrint();

            assertThat(prettyPrint).contains("task1");
            assertThat(prettyPrint).contains("task2");
            assertThat(prettyPrint).contains("my-watch");
        }

        @Test
        @DisplayName("prettyPrint() 无任务时返回正确格式")
        void testPrettyPrintNoTasks() {
            StopWatch stopWatch = new StopWatch("empty-watch");

            String prettyPrint = stopWatch.prettyPrint();

            assertThat(prettyPrint).contains("No tasks recorded");
        }

        @Test
        @DisplayName("formatTime() 返回格式化时间")
        void testFormatTime() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("task");
            Thread.sleep(10);
            stopWatch.stop();

            String formatted = stopWatch.formatTime();

            assertThat(formatted).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("时间百分比测试")
    class TimePercentageTests {

        @Test
        @DisplayName("任务时间百分比计算")
        void testTaskTimePercentage() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();

            stopWatch.start("task1");
            Thread.sleep(30);
            stopWatch.stop();

            stopWatch.start("task2");
            Thread.sleep(10);
            stopWatch.stop();

            List<StopWatch.TaskInfo> tasks = stopWatch.getTasks();
            long total = stopWatch.getTotalTimeNanos();

            double task1Pct = (double) tasks.get(0).timeNanos() / total * 100;
            double task2Pct = (double) tasks.get(1).timeNanos() / total * 100;

            assertThat(task1Pct + task2Pct).isCloseTo(100.0, within(1.0));
        }
    }

    @Nested
    @DisplayName("时间格式化测试")
    class TimeFormattingTests {

        @Test
        @DisplayName("纳秒级格式化")
        void testNanosFormat() {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("quick");
            stopWatch.stop();

            String formatted = stopWatch.formatTime();
            // Very short time, should be in ns or µs
            assertThat(formatted).matches(".*\\d.*");
        }

        @Test
        @DisplayName("毫秒级格式化")
        void testMillisFormat() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("medium");
            Thread.sleep(15);
            stopWatch.stop();

            String formatted = stopWatch.formatTime();
            assertThat(formatted).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空任务名启动")
        void testEmptyTaskName() throws InterruptedException {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("");
            Thread.sleep(5);
            stopWatch.stop();

            assertThat(stopWatch.getTasks().getFirst().name()).isEmpty();
        }

        @Test
        @DisplayName("单次快速启停")
        void testQuickStartStop() {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("quick");
            stopWatch.stop();

            assertThat(stopWatch.getTaskCount()).isEqualTo(1);
            assertThat(stopWatch.getTotalTimeNanos()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("长任务名被截断")
        void testLongTaskNameTruncation() throws InterruptedException {
            String longName = "a".repeat(50);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start(longName);
            Thread.sleep(5);
            stopWatch.stop();

            String prettyPrint = stopWatch.prettyPrint();
            // prettyPrint truncates task names to 20 chars
            assertThat(prettyPrint).contains("...");
        }
    }

    @Nested
    @DisplayName("并发安全性测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("单线程顺序操作正常")
        void testSingleThreadSequentialOperations() throws InterruptedException {
            StopWatch stopWatch = new StopWatch("sequential");

            for (int i = 0; i < 5; i++) {
                stopWatch.start("task" + i);
                Thread.sleep(5);
                stopWatch.stop();
            }

            assertThat(stopWatch.getTaskCount()).isEqualTo(5);
        }
    }
}
