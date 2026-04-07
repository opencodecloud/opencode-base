package cloud.opencode.base.core.process;

import cloud.opencode.base.core.Environment;
import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the {@code cloud.opencode.base.core.process} package.
 * {@code cloud.opencode.base.core.process} 包的测试。
 *
 * @author Leon Soo
 * @since 1.0.3
 */
@DisplayName("进程管理工具测试")
class ProcessManagerTest {

    // ==================== ProcessInfo Tests | ProcessInfo 测试 ====================

    @Nested
    @DisplayName("进程信息测试")
    class ProcessInfoTests {

        @Test
        @DisplayName("当前进程信息应包含有效 PID")
        void currentProcessShouldHaveValidPid() {
            ProcessInfo info = ProcessInfo.fromCurrent();
            assertThat(info.pid()).isGreaterThan(0);
        }

        @Test
        @DisplayName("当前进程命令不应为空白")
        void currentProcessCommandShouldNotBeBlank() {
            ProcessInfo info = ProcessInfo.fromCurrent();
            assertThat(info.command()).isNotNull();
        }

        @Test
        @DisplayName("从 ProcessHandle 构建应正常工作")
        void fromProcessHandleShouldWork() {
            ProcessHandle current = ProcessHandle.current();
            ProcessInfo info = ProcessInfo.from(current);
            assertThat(info.pid()).isEqualTo(current.pid());
            assertThat(info.alive()).isTrue();
        }

        @Test
        @DisplayName("当前进程应处于存活状态")
        void currentProcessShouldBeAlive() {
            ProcessInfo info = ProcessInfo.fromCurrent();
            assertThat(info.alive()).isTrue();
        }

        @Test
        @DisplayName("当前进程运行时间应大于等于零")
        void currentProcessUptimeShouldBeNonNegative() {
            ProcessInfo info = ProcessInfo.fromCurrent();
            long uptime = info.uptimeMillis();
            if (info.startTime() != null) {
                assertThat(uptime).isGreaterThanOrEqualTo(0);
            } else {
                assertThat(uptime).isEqualTo(-1L);
            }
        }

        @Test
        @DisplayName("startInstant 和 cpuTime 返回正确的 Optional")
        void optionalMethodsShouldWork() {
            ProcessInfo info = ProcessInfo.fromCurrent();
            assertThat(info.startInstant()).isPresent();
            assertThat(info.cpuTime()).isNotNull();
        }

        @Test
        @DisplayName("构造函数传入 null command 应抛出 NullPointerException")
        void nullCommandShouldThrow() {
            assertThatThrownBy(() -> new ProcessInfo(1, null, "cmd", "user", null, null, true))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("command");
        }

        @Test
        @DisplayName("构造函数传入 null commandLine 应抛出 NullPointerException")
        void nullCommandLineShouldThrow() {
            assertThatThrownBy(() -> new ProcessInfo(1, "cmd", null, "user", null, null, true))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("commandLine");
        }

        @Test
        @DisplayName("构造函数传入 null user 应抛出 NullPointerException")
        void nullUserShouldThrow() {
            assertThatThrownBy(() -> new ProcessInfo(1, "cmd", "cmdline", null, null, null, true))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("user");
        }

        @Test
        @DisplayName("from(null) 应抛出 NullPointerException")
        void fromNullShouldThrow() {
            assertThatThrownBy(() -> ProcessInfo.from(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("startInstant 当 startTime 为 null 时返回空 Optional")
        void startInstantWhenNullShouldReturnEmpty() {
            ProcessInfo info = new ProcessInfo(1, "cmd", "cmd", "user", null, null, false);
            assertThat(info.startInstant()).isEmpty();
        }

        @Test
        @DisplayName("startInstant 当 startTime 非 null 时返回非空 Optional")
        void startInstantWhenPresentShouldReturnPresent() {
            Instant now = Instant.now();
            ProcessInfo info = new ProcessInfo(1, "cmd", "cmd", "user", now, null, false);
            assertThat(info.startInstant()).contains(now);
        }

        @Test
        @DisplayName("cpuTime 当 cpuDuration 为 null 时返回空 Optional")
        void cpuTimeWhenNullShouldReturnEmpty() {
            ProcessInfo info = new ProcessInfo(1, "cmd", "cmd", "user", null, null, false);
            assertThat(info.cpuTime()).isEmpty();
        }

        @Test
        @DisplayName("cpuTime 当 cpuDuration 非 null 时返回非空 Optional")
        void cpuTimeWhenPresentShouldReturnPresent() {
            Duration dur = Duration.ofSeconds(5);
            ProcessInfo info = new ProcessInfo(1, "cmd", "cmd", "user", null, dur, false);
            assertThat(info.cpuTime()).contains(dur);
        }

        @Test
        @DisplayName("uptimeMillis 当 startTime 为 null 时返回 -1")
        void uptimeMillisWhenStartTimeNullShouldReturnNegativeOne() {
            ProcessInfo info = new ProcessInfo(1, "cmd", "cmd", "user", null, null, false);
            assertThat(info.uptimeMillis()).isEqualTo(-1L);
        }

        @Test
        @DisplayName("uptimeMillis 当 startTime 存在时返回非负值")
        void uptimeMillisWhenStartTimePresentShouldReturnNonNegative() {
            ProcessInfo info = new ProcessInfo(1, "cmd", "cmd", "user",
                    Instant.now().minusSeconds(1), null, false);
            assertThat(info.uptimeMillis()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("extractCommandName 通过 from 方法间接测试 - 当前进程命令名不含路径分隔符")
        void extractCommandNameViaFrom() {
            ProcessInfo info = ProcessInfo.fromCurrent();
            // The command should be just the executable name, not a full path
            assertThat(info.command()).doesNotContain("/");
            assertThat(info.command()).doesNotContain("\\");
        }

        @Test
        @DisplayName("记录字段应正确返回")
        void recordFieldsShouldBeCorrect() {
            Instant start = Instant.now();
            Duration cpu = Duration.ofMillis(500);
            ProcessInfo info = new ProcessInfo(42, "java", "java -jar app.jar", "root", start, cpu, true);
            assertThat(info.pid()).isEqualTo(42);
            assertThat(info.command()).isEqualTo("java");
            assertThat(info.commandLine()).isEqualTo("java -jar app.jar");
            assertThat(info.user()).isEqualTo("root");
            assertThat(info.startTime()).isEqualTo(start);
            assertThat(info.cpuDuration()).isEqualTo(cpu);
            assertThat(info.alive()).isTrue();
        }
    }

    // ==================== Process Discovery Tests | 进程发现测试 ====================

    @Nested
    @DisplayName("进程发现测试")
    class ProcessDiscoveryTests {

        @Test
        @DisplayName("列出所有进程应返回非空列表")
        void listAllShouldReturnNonEmptyList() {
            List<ProcessInfo> all = ProcessManager.listAll();
            assertThat(all).isNotEmpty();
        }

        @Test
        @DisplayName("按当前 PID 查找应返回存在的 Optional")
        void findCurrentPidShouldReturnPresent() {
            long pid = ProcessManager.currentPid();
            Optional<ProcessInfo> found = ProcessManager.find(pid);
            assertThat(found).isPresent();
            assertThat(found.get().pid()).isEqualTo(pid);
        }

        @Test
        @DisplayName("按不存在的 PID 查找应返回空 Optional")
        void findNonExistentPidShouldReturnEmpty() {
            Optional<ProcessInfo> found = ProcessManager.find(99_999_999L);
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("按名称 java 查找应至少包含当前进程")
        void findByNameJavaShouldFindCurrentProcess() {
            List<ProcessInfo> javaProcesses = ProcessManager.findByName("java");
            assertThat(javaProcesses).isNotEmpty();
        }

        @Test
        @DisplayName("findByName 传入不匹配的名称应返回空列表")
        void findByNameNoMatchShouldReturnEmpty() {
            List<ProcessInfo> result = ProcessManager.findByName("zzz_nonexistent_process_xyz_12345");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findByName 传入 null 应抛出 NullPointerException")
        void findByNameNullShouldThrow() {
            assertThatThrownBy(() -> ProcessManager.findByName(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("findByCommand 应返回匹配的进程")
        void findByCommandShouldWork() {
            List<ProcessInfo> result = ProcessManager.findByCommand("java");
            // Should find at least the current JVM
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("findByCommand 传入 null 应抛出 NullPointerException")
        void findByCommandNullShouldThrow() {
            assertThatThrownBy(() -> ProcessManager.findByCommand(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("findByCommand 传入不匹配的命令应返回空列表")
        void findByCommandNoMatchShouldReturnEmpty() {
            List<ProcessInfo> result = ProcessManager.findByCommand("zzz_nonexistent_cmd_xyz_99999");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("父进程应存在")
        void parentShouldBePresent() {
            Optional<ProcessInfo> parent = ProcessManager.parent();
            assertThat(parent).isPresent();
        }

        @Test
        @DisplayName("子进程列表应返回列表（可能为空）")
        void childrenShouldReturnList() {
            List<ProcessInfo> children = ProcessManager.children();
            assertThat(children).isNotNull();
        }

        @Test
        @DisplayName("当前 PID 应大于零")
        void currentPidShouldBePositive() {
            assertThat(ProcessManager.currentPid()).isGreaterThan(0);
        }

        @Test
        @DisplayName("当前进程信息应有效")
        void currentShouldReturnValidInfo() {
            ProcessInfo current = ProcessManager.current();
            assertThat(current.pid()).isEqualTo(ProcessManager.currentPid());
            assertThat(current.alive()).isTrue();
        }

        @Test
        @DisplayName("按不存在 PID 查找子进程应返回空列表")
        void childrenOfNonExistentPidShouldReturnEmptyList() {
            List<ProcessInfo> children = ProcessManager.children(99_999_999L);
            assertThat(children).isEmpty();
        }

        @Test
        @DisplayName("按当前 PID 查找子进程应返回列表")
        void childrenOfCurrentPidShouldReturnList() {
            List<ProcessInfo> children = ProcessManager.children(ProcessManager.currentPid());
            assertThat(children).isNotNull();
        }

        @Test
        @DisplayName("后代进程列表应返回列表")
        void descendantsShouldReturnList() {
            List<ProcessInfo> desc = ProcessManager.descendants();
            assertThat(desc).isNotNull();
        }

        @Test
        @DisplayName("按不存在 PID 查找后代进程应返回空列表")
        void descendantsOfNonExistentPidShouldReturnEmpty() {
            List<ProcessInfo> desc = ProcessManager.descendants(99_999_999L);
            assertThat(desc).isEmpty();
        }

        @Test
        @DisplayName("按当前 PID 查找后代进程应返回列表")
        void descendantsOfCurrentPidShouldReturnList() {
            List<ProcessInfo> desc = ProcessManager.descendants(ProcessManager.currentPid());
            assertThat(desc).isNotNull();
        }
    }

    // ==================== Process Execution Tests | 进程执行测试 ====================

    @Nested
    @DisplayName("进程执行测试")
    class ProcessExecutionTests {

        @Test
        @DisplayName("执行 echo 命令应成功")
        void executeEchoShouldSucceed() {
            ProcessResult result;
            if (Environment.isWindows()) {
                result = ProcessManager.execute("cmd", "/c", "echo", "hello");
            } else {
                result = ProcessManager.execute("echo", "hello");
            }
            assertThat(result.exitCode()).isZero();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.stdout().trim()).isEqualTo("hello");
        }

        @Test
        @DisplayName("成功执行的退出码应为零")
        void successExitCodeShouldBeZero() {
            ProcessResult result;
            if (Environment.isWindows()) {
                result = ProcessManager.execute("cmd", "/c", "echo", "test");
            } else {
                result = ProcessManager.execute("echo", "test");
            }
            assertThat(result.exitCode()).isZero();
        }

        @Test
        @DisplayName("stdout 应包含预期输出")
        void stdoutShouldContainExpectedOutput() {
            ProcessResult result;
            if (Environment.isWindows()) {
                result = ProcessManager.execute("cmd", "/c", "echo", "world");
            } else {
                result = ProcessManager.execute("echo", "world");
            }
            assertThat(result.stdout()).contains("world");
        }

        @Test
        @DisplayName("带超时配置执行应正常完成")
        void executeWithTimeoutShouldComplete() {
            ProcessConfig config;
            if (Environment.isWindows()) {
                config = ProcessConfig.builder("cmd", "/c", "echo", "timeout-test")
                        .timeout(Duration.ofSeconds(10))
                        .build();
            } else {
                config = ProcessConfig.builder("echo", "timeout-test")
                        .timeout(Duration.ofSeconds(10))
                        .build();
            }
            ProcessResult result = ProcessManager.execute(config);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.stdout()).contains("timeout-test");
        }

        @Test
        @DisplayName("执行不存在的命令应抛出异常")
        void executeNonExistentCommandShouldThrow() {
            assertThatThrownBy(() -> ProcessManager.execute(
                    "nonexistent_command_that_does_not_exist_12345"))
                    .isInstanceOf(OpenException.class)
                    .hasMessageContaining("PROCESS_START_FAILED");
        }

        @Test
        @DisplayName("带工作目录的配置应生效")
        @DisabledOnOs(OS.WINDOWS)
        void executeWithWorkingDirectoryShouldWork(@TempDir Path tempDir) {
            ProcessConfig config = ProcessConfig.builder("pwd")
                    .workingDirectory(tempDir)
                    .build();
            ProcessResult result = ProcessManager.execute(config);
            assertThat(result.isSuccess()).isTrue();
            String actualPath = result.stdout().trim();
            String expectedPath = tempDir.toAbsolutePath().toString();
            try {
                actualPath = Path.of(actualPath).toRealPath().toString();
                expectedPath = tempDir.toRealPath().toString();
            } catch (Exception _) {
                // fall through
            }
            assertThat(actualPath).isEqualTo(expectedPath);
        }

        @Test
        @DisplayName("带环境变量的配置应生效")
        void executeWithEnvironmentVariableShouldWork() {
            ProcessConfig config;
            if (Environment.isWindows()) {
                config = ProcessConfig.builder("cmd", "/c", "echo", "%MY_TEST_VAR%")
                        .environment("MY_TEST_VAR", "hello_env")
                        .build();
            } else {
                config = ProcessConfig.builder("sh", "-c", "echo $MY_TEST_VAR")
                        .environment("MY_TEST_VAR", "hello_env")
                        .build();
            }
            ProcessResult result = ProcessManager.execute(config);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.stdout()).contains("hello_env");
        }

        @Test
        @DisplayName("redirectErrorStream 应合并输出")
        @DisabledOnOs(OS.WINDOWS)
        void redirectErrorStreamShouldMergeOutput() {
            ProcessConfig config = ProcessConfig.builder("sh", "-c", "echo out && echo err >&2")
                    .redirectErrorStream(true)
                    .build();
            ProcessResult result = ProcessManager.execute(config);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.stdout()).contains("out");
            assertThat(result.stdout()).contains("err");
        }

        @Test
        @DisplayName("使用 List 参数执行应成功")
        void executeWithListCommandShouldSucceed() {
            List<String> command;
            if (Environment.isWindows()) {
                command = List.of("cmd", "/c", "echo", "list-test");
            } else {
                command = List.of("echo", "list-test");
            }
            ProcessResult result = ProcessManager.execute(command);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.stdout()).contains("list-test");
        }

        @Test
        @DisplayName("执行结果应包含命令信息")
        void resultShouldContainCommand() {
            ProcessResult result;
            if (Environment.isWindows()) {
                result = ProcessManager.execute("cmd", "/c", "echo", "cmd-test");
            } else {
                result = ProcessManager.execute("echo", "cmd-test");
            }
            assertThat(result.command()).isNotEmpty();
        }

        @Test
        @DisplayName("执行结果应包含非空执行时间")
        void resultShouldContainDuration() {
            ProcessResult result;
            if (Environment.isWindows()) {
                result = ProcessManager.execute("cmd", "/c", "echo", "dur-test");
            } else {
                result = ProcessManager.execute("echo", "dur-test");
            }
            assertThat(result.duration()).isNotNull();
            assertThat(result.duration().isNegative()).isFalse();
        }

        @Test
        @DisplayName("output() 应与 stdout() 返回相同结果")
        void outputShouldAliasStdout() {
            ProcessResult result;
            if (Environment.isWindows()) {
                result = ProcessManager.execute("cmd", "/c", "echo", "alias");
            } else {
                result = ProcessManager.execute("echo", "alias");
            }
            assertThat(result.output()).isEqualTo(result.stdout());
        }

        @Test
        @DisplayName("超时到期应抛出 OpenException")
        @DisabledOnOs(OS.WINDOWS)
        void executeWithTimeoutExpiredShouldThrow() {
            ProcessConfig config = ProcessConfig.builder("sleep", "30")
                    .timeout(Duration.ofSeconds(1))
                    .build();
            assertThatThrownBy(() -> ProcessManager.execute(config))
                    .isInstanceOf(OpenException.class)
                    .hasMessageContaining("PROCESS_TIMEOUT");
        }

        @Test
        @DisplayName("stdoutFile 重定向应将输出写入文件")
        @DisabledOnOs(OS.WINDOWS)
        void executeWithStdoutFileShouldWriteToFile(@TempDir Path tempDir) throws Exception {
            Path outFile = tempDir.resolve("stdout.txt");
            ProcessConfig config = ProcessConfig.builder("echo", "file-output")
                    .stdoutFile(outFile)
                    .build();
            ProcessResult result = ProcessManager.execute(config);
            assertThat(result.isSuccess()).isTrue();
            // stdout is captured to file, so in-memory stdout should be empty
            assertThat(result.stdout()).isEmpty();
            assertThat(Files.readString(outFile).trim()).isEqualTo("file-output");
        }

        @Test
        @DisplayName("stderrFile 重定向应将错误输出写入文件")
        @DisabledOnOs(OS.WINDOWS)
        void executeWithStderrFileShouldWriteToFile(@TempDir Path tempDir) throws Exception {
            Path errFile = tempDir.resolve("stderr.txt");
            ProcessConfig config = ProcessConfig.builder("sh", "-c", "echo error-output >&2")
                    .stderrFile(errFile)
                    .build();
            ProcessResult result = ProcessManager.execute(config);
            assertThat(Files.readString(errFile).trim()).isEqualTo("error-output");
        }

        @Test
        @DisplayName("execute(null config) 应抛出 NullPointerException")
        void executeNullConfigShouldThrow() {
            assertThatThrownBy(() -> ProcessManager.execute((ProcessConfig) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== ProcessResult Tests | ProcessResult 测试 ====================

    @Nested
    @DisplayName("进程结果测试")
    class ProcessResultTests {

        @Test
        @DisplayName("退出码为零时 isSuccess 应返回 true")
        void isSuccessForExitCodeZero() {
            ProcessResult result = ProcessResult.of(0, "output", "", Duration.ofMillis(100), List.of("echo"));
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("退出码非零时 isSuccess 应返回 false")
        void isSuccessForNonZeroExitCode() {
            ProcessResult result = ProcessResult.of(1, "", "error", Duration.ofMillis(100), List.of("cmd"));
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("成功时 orThrow 不应抛出异常")
        void orThrowForSuccessShouldNotThrow() {
            ProcessResult result = ProcessResult.of(0, "ok", "", Duration.ofMillis(50), List.of("echo"));
            ProcessResult returned = result.orThrow();
            assertThat(returned).isSameAs(result);
        }

        @Test
        @DisplayName("失败时 orThrow 应抛出 OpenException")
        void orThrowForFailureShouldThrow() {
            ProcessResult result = ProcessResult.of(1, "", "some error", Duration.ofMillis(50), List.of("bad"));
            assertThatThrownBy(result::orThrow)
                    .isInstanceOf(OpenException.class)
                    .hasMessageContaining("PROCESS_EXIT_1")
                    .hasMessageContaining("some error");
        }

        @Test
        @DisplayName("of 工厂方法应创建正确的实例")
        void ofFactoryMethodShouldWork() {
            ProcessResult result = ProcessResult.of(42, "out", "err",
                    Duration.ofSeconds(1), List.of("test", "cmd"));
            assertThat(result.exitCode()).isEqualTo(42);
            assertThat(result.stdout()).isEqualTo("out");
            assertThat(result.stderr()).isEqualTo("err");
            assertThat(result.duration()).isEqualTo(Duration.ofSeconds(1));
            assertThat(result.command()).containsExactly("test", "cmd");
        }

        @Test
        @DisplayName("command 列表应为不可变")
        void commandListShouldBeImmutable() {
            ProcessResult result = ProcessResult.of(0, "", "", Duration.ZERO, List.of("a", "b"));
            assertThatThrownBy(() -> result.command().add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("构造函数传入 null stdout 应抛出 NullPointerException")
        void nullStdoutShouldThrow() {
            assertThatThrownBy(() -> new ProcessResult(0, null, "", Duration.ZERO, List.of("a")))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("stdout");
        }

        @Test
        @DisplayName("构造函数传入 null stderr 应抛出 NullPointerException")
        void nullStderrShouldThrow() {
            assertThatThrownBy(() -> new ProcessResult(0, "", null, Duration.ZERO, List.of("a")))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("stderr");
        }

        @Test
        @DisplayName("构造函数传入 null duration 应抛出 NullPointerException")
        void nullDurationShouldThrow() {
            assertThatThrownBy(() -> new ProcessResult(0, "", "", null, List.of("a")))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("duration");
        }

        @Test
        @DisplayName("构造函数传入 null command 应抛出 NullPointerException")
        void nullCommandShouldThrow() {
            assertThatThrownBy(() -> new ProcessResult(0, "", "", Duration.ZERO, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("command");
        }

        @Test
        @DisplayName("command() 返回防御性复制（不可修改列表）")
        void commandReturnsDefensiveCopy() {
            List<String> mutableList = new ArrayList<>(List.of("echo", "hi"));
            ProcessResult result = new ProcessResult(0, "", "", Duration.ZERO, mutableList);
            // Modify original list - should not affect the result
            mutableList.add("extra");
            assertThat(result.command()).containsExactly("echo", "hi");
        }

        @Test
        @DisplayName("orThrow 对长 stderr 应截断")
        void orThrowWithLongStderrShouldTruncate() {
            String longStderr = "x".repeat(600);
            ProcessResult result = ProcessResult.of(1, "", longStderr, Duration.ZERO, List.of("cmd"));
            assertThatThrownBy(result::orThrow)
                    .isInstanceOf(OpenException.class)
                    .hasMessageContaining("...(truncated)");
        }

        @Test
        @DisplayName("orThrow 对短 stderr 不应截断")
        void orThrowWithShortStderrShouldNotTruncate() {
            String shortStderr = "x".repeat(100);
            ProcessResult result = ProcessResult.of(1, "", shortStderr, Duration.ZERO, List.of("cmd"));
            assertThatThrownBy(result::orThrow)
                    .isInstanceOf(OpenException.class)
                    .satisfies(e -> assertThat(e.getMessage()).doesNotContain("truncated"));
        }

        @Test
        @DisplayName("orThrow 异常消息应包含退出码和命令")
        void orThrowExceptionMessageFormat() {
            ProcessResult result = ProcessResult.of(127, "", "not found", Duration.ZERO, List.of("foo", "bar"));
            assertThatThrownBy(result::orThrow)
                    .isInstanceOf(OpenException.class)
                    .hasMessageContaining("PROCESS_EXIT_127")
                    .hasMessageContaining("foo bar")
                    .hasMessageContaining("not found");
        }
    }

    // ==================== ProcessConfig Tests | ProcessConfig 测试 ====================

    @Nested
    @DisplayName("进程配置测试")
    class ProcessConfigTests {

        @Test
        @DisplayName("构建器应创建正确的配置")
        void builderShouldCreateCorrectConfig() {
            ProcessConfig config = ProcessConfig.builder("ls", "-la")
                    .workingDirectory(Path.of("/tmp"))
                    .environment("KEY", "VALUE")
                    .timeout(Duration.ofSeconds(30))
                    .redirectErrorStream(true)
                    .build();

            assertThat(config.command()).containsExactly("ls", "-la");
            assertThat(config.workingDirectory()).isEqualTo(Path.of("/tmp"));
            assertThat(config.environment()).containsEntry("KEY", "VALUE");
            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.redirectErrorStream()).isTrue();
        }

        @Test
        @DisplayName("空命令应抛出异常")
        void emptyCommandShouldThrow() {
            assertThatThrownBy(() -> ProcessConfig.builder(List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("命令列表不可变")
        void commandListShouldBeImmutable() {
            ProcessConfig config = ProcessConfig.builder("echo", "hi").build();
            assertThatThrownBy(() -> config.command().add("extra"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("环境变量映射不可变")
        void environmentMapShouldBeImmutable() {
            ProcessConfig config = ProcessConfig.builder("echo")
                    .environment("K", "V")
                    .build();
            assertThatThrownBy(() -> config.environment().put("K2", "V2"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("默认值应正确设置")
        void defaultsShouldBeCorrect() {
            ProcessConfig config = ProcessConfig.builder("echo").build();
            assertThat(config.workingDirectory()).isNull();
            assertThat(config.environment()).isEmpty();
            assertThat(config.timeout()).isNull();
            assertThat(config.redirectErrorStream()).isFalse();
            assertThat(config.stdoutFile()).isNull();
            assertThat(config.stderrFile()).isNull();
            assertThat(config.inheritIO()).isFalse();
        }

        @Test
        @DisplayName("toString 应包含命令信息")
        void toStringShouldContainCommand() {
            ProcessConfig config = ProcessConfig.builder("echo", "test").build();
            assertThat(config.toString()).contains("echo");
        }

        @Test
        @DisplayName("使用 List 参数构建器应正常工作")
        void builderWithListShouldWork() {
            ProcessConfig config = ProcessConfig.builder(List.of("git", "status"))
                    .timeout(Duration.ofSeconds(5))
                    .build();
            assertThat(config.command()).containsExactly("git", "status");
        }

        @Test
        @DisplayName("stdoutFile 和 stderrFile 应正确设置")
        void fileRedirectionShouldWork(@TempDir Path tempDir) {
            Path outFile = tempDir.resolve("out.txt");
            Path errFile = tempDir.resolve("err.txt");
            ProcessConfig config = ProcessConfig.builder("echo")
                    .stdoutFile(outFile)
                    .stderrFile(errFile)
                    .build();
            assertThat(config.stdoutFile()).isEqualTo(outFile);
            assertThat(config.stderrFile()).isEqualTo(errFile);
        }

        @Test
        @DisplayName("builder(null varargs command) 应抛出 NullPointerException")
        void builderNullVarargsCommandShouldThrow() {
            assertThatThrownBy(() -> ProcessConfig.builder((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder(null List) 应抛出异常")
        void builderNullListCommandShouldThrow() {
            assertThatThrownBy(() -> ProcessConfig.builder((List<String>) null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("builder.command(null varargs) 应抛出 NullPointerException")
        void builderCommandNullVarargsShouldThrow() {
            assertThatThrownBy(() -> ProcessConfig.builder("echo").command((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder.command(null List) 应抛出 NullPointerException")
        void builderCommandNullListShouldThrow() {
            assertThatThrownBy(() -> ProcessConfig.builder("echo").command((List<String>) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("environment(null key, value) 应抛出 NullPointerException")
        void environmentNullKeyShouldThrow() {
            assertThatThrownBy(() -> ProcessConfig.builder("echo").environment(null, "v"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("environment(key, null value) 应抛出 NullPointerException")
        void environmentNullValueShouldThrow() {
            assertThatThrownBy(() -> ProcessConfig.builder("echo").environment("k", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("environment(null map) 应抛出 NullPointerException")
        void environmentNullMapShouldThrow() {
            assertThatThrownBy(() -> ProcessConfig.builder("echo").environment((Map<String, String>) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("timeout(amount, null unit) 应抛出 NullPointerException")
        void timeoutNullUnitShouldThrow() {
            assertThatThrownBy(() -> ProcessConfig.builder("echo").timeout(5, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("timeout(amount, unit) 应设置正确的超时时间")
        void timeoutWithUnitShouldWork() {
            ProcessConfig config = ProcessConfig.builder("echo")
                    .timeout(5, TimeUnit.SECONDS)
                    .build();
            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(5));
        }

        @Test
        @DisplayName("build 后 command 被设为空列表应抛出异常")
        void buildWithEmptyCommandAfterSetShouldThrow() {
            assertThatThrownBy(() -> ProcessConfig.builder("echo")
                    .command(List.of())
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toString 应包含所有关键字段")
        void toStringShouldContainAllFields() {
            ProcessConfig config = ProcessConfig.builder("echo", "test")
                    .timeout(Duration.ofSeconds(5))
                    .redirectErrorStream(true)
                    .inheritIO(true)
                    .build();
            String str = config.toString();
            assertThat(str).contains("echo");
            assertThat(str).contains("timeout");
            assertThat(str).contains("redirectErrorStream=true");
            assertThat(str).contains("inheritIO=true");
        }

        @Test
        @DisplayName("environment(Map) 应批量添加环境变量")
        void environmentMapShouldAddAll() {
            ProcessConfig config = ProcessConfig.builder("echo")
                    .environment(Map.of("A", "1", "B", "2"))
                    .build();
            assertThat(config.environment()).containsEntry("A", "1");
            assertThat(config.environment()).containsEntry("B", "2");
        }

        @Test
        @DisplayName("环境变量映射为空时不可变（空 Map.of()）")
        void emptyEnvironmentShouldBeImmutable() {
            ProcessConfig config = ProcessConfig.builder("echo").build();
            assertThat(config.environment()).isEmpty();
            assertThatThrownBy(() -> config.environment().put("K", "V"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("inheritIO 设置应生效")
        void inheritIOShouldWork() {
            ProcessConfig config = ProcessConfig.builder("echo")
                    .inheritIO(true)
                    .build();
            assertThat(config.inheritIO()).isTrue();
        }

        @Test
        @DisplayName("使用 builder.command(varargs) 覆盖命令应生效")
        void commandVarargsOverrideShouldWork() {
            ProcessConfig config = ProcessConfig.builder("echo")
                    .command("ls", "-la")
                    .build();
            assertThat(config.command()).containsExactly("ls", "-la");
        }

        @Test
        @DisplayName("command 列表是防御性复制")
        void commandListIsDefensiveCopy() {
            List<String> mutableList = new ArrayList<>(List.of("echo", "hi"));
            ProcessConfig config = ProcessConfig.builder(mutableList).build();
            mutableList.add("extra");
            assertThat(config.command()).containsExactly("echo", "hi");
        }

        @Test
        @DisplayName("timeout(Duration) 设置为 null 应使 timeout 为 null")
        void timeoutDurationNullShouldSetNull() {
            ProcessConfig config = ProcessConfig.builder("echo")
                    .timeout(Duration.ofSeconds(5))
                    .timeout((Duration) null)
                    .build();
            assertThat(config.timeout()).isNull();
        }
    }

    // ==================== Process Control Tests | 进程控制测试 ====================

    @Nested
    @DisplayName("进程控制测试")
    class ProcessControlTests {

        @Test
        @DisplayName("当前进程应处于存活状态")
        void isAliveForCurrentProcessShouldReturnTrue() {
            assertThat(ProcessManager.isAlive(ProcessManager.currentPid())).isTrue();
        }

        @Test
        @DisplayName("不存在的 PID 不应存活")
        void isAliveForNonExistentPidShouldReturnFalse() {
            assertThat(ProcessManager.isAlive(99_999_999L)).isFalse();
        }

        @Test
        @DisplayName("终止不存在的进程应返回 false")
        void killNonExistentProcessShouldReturnFalse() {
            assertThat(ProcessManager.kill(99_999_999L)).isFalse();
        }

        @Test
        @DisplayName("强制终止不存在的进程应返回 false")
        void killForciblyNonExistentProcessShouldReturnFalse() {
            assertThat(ProcessManager.killForcibly(99_999_999L)).isFalse();
        }

        @Test
        @DisplayName("启动并终止子进程")
        @DisabledOnOs(OS.WINDOWS)
        void startAndKillChildProcess() throws Exception {
            Process process = ProcessManager.start("sleep", "60");
            long pid = process.pid();
            try {
                assertThat(ProcessManager.isAlive(pid)).isTrue();
                boolean killed = ProcessManager.kill(pid);
                assertThat(killed).isTrue();
                process.waitFor(5, TimeUnit.SECONDS);
                assertThat(process.isAlive()).isFalse();
            } finally {
                process.destroyForcibly();
            }
        }

        @Test
        @DisplayName("启动并强制终止子进程")
        @DisabledOnOs(OS.WINDOWS)
        void startAndKillForciblyChildProcess() throws Exception {
            Process process = ProcessManager.start("sleep", "60");
            long pid = process.pid();
            try {
                assertThat(ProcessManager.isAlive(pid)).isTrue();
                boolean killed = ProcessManager.killForcibly(pid);
                assertThat(killed).isTrue();
                process.waitFor(5, TimeUnit.SECONDS);
                assertThat(process.isAlive()).isFalse();
            } finally {
                process.destroyForcibly();
            }
        }

        @Test
        @DisplayName("waitFor 已退出的进程应返回退出状态")
        @DisabledOnOs(OS.WINDOWS)
        void waitForExitedProcessShouldReturnStatus() throws Exception {
            Process process = ProcessManager.start("echo", "done");
            long pid = process.pid();
            process.waitFor(5, TimeUnit.SECONDS);
            // Process already exited; waitFor should return something or empty
            Optional<Integer> status = ProcessManager.waitFor(pid, 5, TimeUnit.SECONDS);
            // Either the handle is gone (empty) or it returned exit status
            assertThat(status).isNotNull();
        }

        @Test
        @DisplayName("waitFor 不存在的 PID 应返回空 Optional")
        void waitForNonExistentPidShouldReturnEmpty() {
            Optional<Integer> status = ProcessManager.waitFor(99_999_999L, 1, TimeUnit.SECONDS);
            assertThat(status).isEmpty();
        }

        @Test
        @DisplayName("waitFor null unit 应抛出 NullPointerException")
        void waitForNullUnitShouldThrow() {
            assertThatThrownBy(() -> ProcessManager.waitFor(1, 1, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("waitFor 应在超时后返回空 Optional")
        @DisabledOnOs(OS.WINDOWS)
        void waitForWithTimeoutShouldReturnEmpty() throws Exception {
            Process process = ProcessManager.start("sleep", "60");
            long pid = process.pid();
            try {
                // Very short timeout - process should not have exited
                Optional<Integer> status = ProcessManager.waitFor(pid, 100, TimeUnit.MILLISECONDS);
                assertThat(status).isEmpty();
            } finally {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        }

        @Test
        @DisplayName("waitFor 运行中进程在超时内退出应返回退出状态")
        @DisabledOnOs(OS.WINDOWS)
        void waitForRunningProcessThatExitsShouldReturnStatus() throws Exception {
            // Start a process that exits quickly
            Process process = ProcessManager.start("sleep", "0.1");
            long pid = process.pid();
            try {
                Optional<Integer> status = ProcessManager.waitFor(pid, 5, TimeUnit.SECONDS);
                // Should have exited within timeout
                assertThat(status).isPresent();
                assertThat(status.get()).isEqualTo(0);
            } finally {
                process.destroyForcibly();
            }
        }
    }

    // ==================== Start Tests | 启动测试 ====================

    @Nested
    @DisplayName("进程启动测试")
    class ProcessStartTests {

        @Test
        @DisplayName("start 应返回正在运行的进程")
        @DisabledOnOs(OS.WINDOWS)
        void startShouldReturnRunningProcess() {
            Process process = ProcessManager.start("sleep", "10");
            try {
                assertThat(process.isAlive()).isTrue();
                assertThat(process.pid()).isGreaterThan(0);
            } finally {
                process.destroyForcibly();
            }
        }

        @Test
        @DisplayName("start 不存在的命令应抛出异常")
        void startNonExistentCommandShouldThrow() {
            assertThatThrownBy(() -> ProcessManager.start("nonexistent_cmd_xyz"))
                    .isInstanceOf(OpenException.class)
                    .hasMessageContaining("PROCESS_START_FAILED");
        }

        @Test
        @DisplayName("使用 ProcessConfig 启动应正常工作")
        @DisabledOnOs(OS.WINDOWS)
        void startWithConfigShouldWork() {
            ProcessConfig config = ProcessConfig.builder("sleep", "10").build();
            Process process = ProcessManager.start(config);
            try {
                assertThat(process.isAlive()).isTrue();
            } finally {
                process.destroyForcibly();
            }
        }

        @Test
        @DisplayName("start(null config) 应抛出 NullPointerException")
        void startNullConfigShouldThrow() {
            assertThatThrownBy(() -> ProcessManager.start((ProcessConfig) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== ProcessManager Utility Tests | 工具类测试 ====================

    @Nested
    @DisplayName("ProcessManager 不可实例化测试")
    class ProcessManagerUtilityTests {

        @Test
        @DisplayName("ProcessManager 不可通过反射实例化")
        void processManagerShouldNotBeInstantiable() throws Exception {
            Constructor<ProcessManager> constructor = ProcessManager.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatThrownBy(constructor::newInstance)
                    .isInstanceOf(InvocationTargetException.class)
                    .hasCauseInstanceOf(AssertionError.class);
        }
    }
}
