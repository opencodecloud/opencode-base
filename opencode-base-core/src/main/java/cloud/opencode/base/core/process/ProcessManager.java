package cloud.opencode.base.core.process;

import cloud.opencode.base.core.exception.OpenException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ProcessManager - Process management utility facade
 * ProcessManager - 进程管理工具门面
 *
 * <p>Provides a comprehensive set of static methods for process discovery,
 * execution, and control using the JDK {@link ProcessHandle} and {@link ProcessBuilder} APIs.</p>
 * <p>使用 JDK {@link ProcessHandle} 和 {@link ProcessBuilder} API 提供一整套进程发现、
 * 执行和控制的静态方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Process discovery: list, find by PID, name, or command - 进程发现：列表、按 PID/名称/命令查找</li>
 *   <li>Process tree: children and descendants - 进程树：子进程和后代进程</li>
 *   <li>Process execution: run commands with timeout, capture output - 进程执行：运行命令、超时、捕获输出</li>
 *   <li>Process control: kill, force kill, wait, alive check - 进程控制：终止、强制终止、等待、存活检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Execute a command and capture output
 * ProcessResult result = ProcessManager.execute("ls", "-la");
 * System.out.println(result.stdout());
 *
 * // Find processes by name
 * List<ProcessInfo> javaProcesses = ProcessManager.findByName("java");
 *
 * // Execute with configuration
 * ProcessConfig config = ProcessConfig.builder("git", "status")
 *         .workingDirectory(Path.of("/my/repo"))
 *         .timeout(Duration.ofSeconds(10))
 *         .build();
 * ProcessResult gitResult = ProcessManager.execute(config);
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong> All methods are stateless and thread-safe.
 * 所有方法都是无状态的，线程安全。</p>
 *
 * <p><strong>Output Capture Limit | 输出捕获限制:</strong> stdout and stderr are each limited
 * to 10 MB to prevent out-of-memory errors. Excess output is truncated.</p>
 * <p>stdout 和 stderr 各限制为 10 MB 以防止内存溢出。超出部分将被截断。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ProcessInfo
 * @see ProcessResult
 * @see ProcessConfig
 * @since JDK 25, opencode-base-core V1.0.3
 */
public final class ProcessManager {

    /**
     * Maximum captured output size per stream (10 MB).
     * 每个流的最大捕获输出大小（10 MB）。
     */
    private static final int MAX_OUTPUT_SIZE = 10 * 1024 * 1024;

    private ProcessManager() {
        throw new AssertionError("No ProcessManager instances for you!");
    }

    // ==================== Process Discovery | 进程发现 ====================

    /**
     * Returns information about the current JVM process.
     * 返回当前 JVM 进程的信息。
     *
     * @return process info for the current process - 当前进程的进程信息
     */
    public static ProcessInfo current() {
        return ProcessInfo.fromCurrent();
    }

    /**
     * Finds a process by its PID.
     * 通过 PID 查找进程。
     *
     * @param pid the process ID to find - 要查找的进程 ID
     * @return an Optional containing the process info, or empty if not found - 包含进程信息的 Optional，未找到时为空
     */
    public static Optional<ProcessInfo> find(long pid) {
        return ProcessHandle.of(pid).map(ProcessInfo::from);
    }

    /**
     * Lists all visible processes on the system.
     * 列出系统上所有可见的进程。
     *
     * <p>Note: The returned list is a snapshot and may not reflect real-time state.
     * Visibility depends on OS permissions. This method scans all OS processes and
     * may be expensive on systems with many active processes — avoid calling in tight loops.</p>
     * <p>注意：返回的列表是快照，可能不反映实时状态。可见性取决于操作系统权限。
     * 此方法扫描所有系统进程，在进程数量多的系统上开销较大——避免在紧密循环中调用。</p>
     *
     * <p><strong>Security Warning | 安全警告:</strong> Process command lines may contain
     * sensitive credentials passed as arguments. Do not log or expose the returned list
     * to untrusted parties.</p>
     * <p><strong>安全警告:</strong> 进程命令行可能包含作为参数传递的敏感凭据。
     * 切勿将返回的列表记录日志或暴露给不可信方。</p>
     *
     * @return list of all visible processes - 所有可见进程的列表
     */
    public static List<ProcessInfo> listAll() {
        return ProcessHandle.allProcesses()
                .map(ProcessInfo::from)
                .toList();
    }

    /**
     * Finds processes whose command name contains the given string (case-insensitive).
     * 查找命令名称包含给定字符串的进程（不区分大小写）。
     *
     * @param name the name to search for (contains match) - 要搜索的名称（包含匹配）
     * @return list of matching processes - 匹配进程的列表
     * @throws NullPointerException if name is null - 若 name 为 null
     */
    public static List<ProcessInfo> findByName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        String lowerName = name.toLowerCase(java.util.Locale.ROOT);
        return ProcessHandle.allProcesses()
                .filter(h -> h.info().command()
                        .map(cmd -> cmd.toLowerCase(java.util.Locale.ROOT).contains(lowerName))
                        .orElse(false))
                .map(ProcessInfo::from)
                .toList();
    }

    /**
     * Finds processes whose full command line contains the given string (case-insensitive).
     * 查找完整命令行包含给定字符串的进程（不区分大小写）。
     *
     * @param command the command string to search for (contains match) - 要搜索的命令字符串（包含匹配）
     * @return list of matching processes - 匹配进程的列表
     * @throws NullPointerException if command is null - 若 command 为 null
     */
    public static List<ProcessInfo> findByCommand(String command) {
        Objects.requireNonNull(command, "command must not be null");
        String lowerCmd = command.toLowerCase(java.util.Locale.ROOT);
        return ProcessHandle.allProcesses()
                .filter(h -> h.info().commandLine()
                        .map(cl -> cl.toLowerCase(java.util.Locale.ROOT).contains(lowerCmd))
                        .orElse(false))
                .map(ProcessInfo::from)
                .toList();
    }

    /**
     * Returns the PID of the current JVM process.
     * 返回当前 JVM 进程的 PID。
     *
     * @return the current process PID - 当前进程 PID
     */
    public static long currentPid() {
        return ProcessHandle.current().pid();
    }

    /**
     * Returns information about the parent process of the current JVM.
     * 返回当前 JVM 父进程的信息。
     *
     * @return an Optional containing parent process info, or empty if no parent - 包含父进程信息的 Optional，无父进程时为空
     */
    public static Optional<ProcessInfo> parent() {
        return ProcessHandle.current().parent().map(ProcessInfo::from);
    }

    // ==================== Process Tree | 进程树 ====================

    /**
     * Returns direct children of the current JVM process.
     * 返回当前 JVM 进程的直接子进程。
     *
     * @return list of direct child processes - 直接子进程列表
     */
    public static List<ProcessInfo> children() {
        return ProcessHandle.current().children()
                .map(ProcessInfo::from)
                .toList();
    }

    /**
     * Returns direct children of the process with the given PID.
     * 返回给定 PID 进程的直接子进程。
     *
     * @param pid the parent process PID - 父进程 PID
     * @return list of direct child processes, or empty if PID not found - 直接子进程列表，若 PID 未找到则为空
     */
    public static List<ProcessInfo> children(long pid) {
        return ProcessHandle.of(pid)
                .map(h -> h.children().map(ProcessInfo::from).toList())
                .orElse(List.of());
    }

    /**
     * Returns all descendants of the current JVM process.
     * 返回当前 JVM 进程的所有后代进程。
     *
     * @return list of all descendant processes - 所有后代进程列表
     */
    public static List<ProcessInfo> descendants() {
        return ProcessHandle.current().descendants()
                .map(ProcessInfo::from)
                .toList();
    }

    /**
     * Returns all descendants of the process with the given PID.
     * 返回给定 PID 进程的所有后代进程。
     *
     * @param pid the ancestor process PID - 祖先进程 PID
     * @return list of all descendant processes, or empty if PID not found - 所有后代进程列表，若 PID 未找到则为空
     */
    public static List<ProcessInfo> descendants(long pid) {
        return ProcessHandle.of(pid)
                .map(h -> h.descendants().map(ProcessInfo::from).toList())
                .orElse(List.of());
    }

    // ==================== Process Execution | 进程执行 ====================

    /**
     * Executes a command and waits for completion, capturing stdout and stderr.
     * 执行命令并等待完成，捕获 stdout 和 stderr。
     *
     * <p><strong>Security Warning | 安全警告:</strong> Never pass untrusted user input directly
     * as command arguments. Validate and sanitize all inputs before execution.
     * Avoid invoking shell interpreters (sh -c, cmd /c) with concatenated arguments.</p>
     * <p><strong>安全警告:</strong> 切勿将不可信的用户输入直接作为命令参数传入。
     * 执行前请验证和清理所有输入。避免使用 shell 解释器拼接参数。</p>
     *
     * @param command the command and arguments - 命令和参数
     * @return the execution result - 执行结果
     * @throws OpenException if the process cannot be started or is interrupted - 若进程无法启动或被中断
     */
    public static ProcessResult execute(String... command) {
        return execute(ProcessConfig.builder(command).build());
    }

    /**
     * Executes a command and waits for completion, capturing stdout and stderr.
     * 执行命令并等待完成，捕获 stdout 和 stderr。
     *
     * @param command the command and arguments - 命令和参数
     * @return the execution result - 执行结果
     * @throws OpenException if the process cannot be started or is interrupted - 若进程无法启动或被中断
     */
    public static ProcessResult execute(List<String> command) {
        return execute(ProcessConfig.builder(command).build());
    }

    /**
     * Executes a command with the given configuration and waits for completion.
     * 使用给定配置执行命令并等待完成。
     *
     * <p>stdout and stderr are captured in parallel threads to avoid deadlock
     * when the process output exceeds the OS pipe buffer size.
     * Each stream is limited to {@value #MAX_OUTPUT_SIZE} bytes.</p>
     * <p>stdout 和 stderr 在并行线程中捕获，以避免当进程输出超过操作系统管道缓冲区大小时的死锁。
     * 每个流限制为 {@value #MAX_OUTPUT_SIZE} 字节。</p>
     *
     * @param config the process configuration - 进程配置
     * @return the execution result - 执行结果
     * @throws OpenException if the process cannot be started, times out, or is interrupted -
     *                       若进程无法启动、超时或被中断
     */
    public static ProcessResult execute(ProcessConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        Instant startTime = Instant.now();

        ProcessBuilder pb = buildProcessBuilder(config);

        try {
            Process process = pb.start();

            if (config.inheritIO()) {
                return waitForProcess(process, config, startTime, "", "");
            }

            // Capture stdout and stderr in parallel virtual threads to avoid deadlock
            StringBuilder stdoutBuilder = new StringBuilder();
            StringBuilder stderrBuilder = new StringBuilder();

            Thread stdoutThread = Thread.ofVirtual().name("process-stdout-reader").start(
                    () -> readStream(process.getInputStream(), stdoutBuilder));

            Thread stderrThread;
            if (config.redirectErrorStream()) {
                stderrThread = null;
            } else {
                stderrThread = Thread.ofVirtual().name("process-stderr-reader").start(
                        () -> readStream(process.getErrorStream(), stderrBuilder));
            }

            ProcessResult result = waitForProcess(process, config, startTime,
                    null, null);

            // Wait for reader threads to finish (process already exited, streams will close)
            stdoutThread.join();
            if (stderrThread != null) {
                stderrThread.join();
            }

            return new ProcessResult(
                    result.exitCode(),
                    stdoutBuilder.toString(),
                    stderrBuilder.toString(),
                    result.duration(),
                    config.command()
            );

        } catch (IOException e) {
            throw new OpenException("Process", "PROCESS_START_FAILED",
                    "Failed to start process: " + String.join(" ", config.command()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenException("Process", "PROCESS_INTERRUPTED",
                    "Process execution interrupted: " + String.join(" ", config.command()), e);
        }
    }

    /**
     * Starts a process without waiting for completion.
     * 启动进程但不等待完成。
     *
     * <p><strong>Security Warning | 安全警告:</strong> Never pass untrusted user input directly
     * as command arguments. See {@link #execute(String...)} for details.</p>
     * <p><strong>安全警告:</strong> 切勿将不可信的用户输入直接作为命令参数传入。</p>
     *
     * @param command the command and arguments - 命令和参数
     * @return the started {@link Process} - 启动的进程
     * @throws OpenException if the process cannot be started - 若进程无法启动
     */
    public static Process start(String... command) {
        return start(ProcessConfig.builder(command).build());
    }

    /**
     * Starts a process with the given configuration without waiting for completion.
     * 使用给定配置启动进程但不等待完成。
     *
     * @param config the process configuration - 进程配置
     * @return the started {@link Process} - 启动的进程
     * @throws OpenException if the process cannot be started - 若进程无法启动
     */
    public static Process start(ProcessConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        ProcessBuilder pb = buildProcessBuilder(config);
        try {
            return pb.start();
        } catch (IOException e) {
            throw new OpenException("Process", "PROCESS_START_FAILED",
                    "Failed to start process: " + String.join(" ", config.command()), e);
        }
    }

    // ==================== Process Control | 进程控制 ====================

    /**
     * Sends a normal termination request to the process with the given PID.
     * 向给定 PID 的进程发送正常终止请求。
     *
     * @param pid the process ID - 进程 ID
     * @return {@code true} if the process was found and the request was sent - 若进程被找到且请求已发送则为 true
     */
    public static boolean kill(long pid) {
        return ProcessHandle.of(pid)
                .map(ProcessHandle::destroy)
                .orElse(false);
    }

    /**
     * Forcibly terminates the process with the given PID.
     * 强制终止给定 PID 的进程。
     *
     * @param pid the process ID - 进程 ID
     * @return {@code true} if the process was found and forcibly terminated - 若进程被找到且已被强制终止则为 true
     */
    public static boolean killForcibly(long pid) {
        return ProcessHandle.of(pid)
                .map(ProcessHandle::destroyForcibly)
                .orElse(false);
    }

    /**
     * Checks whether the process with the given PID is alive.
     * 检查给定 PID 的进程是否存活。
     *
     * @param pid the process ID - 进程 ID
     * @return {@code true} if the process exists and is alive - 若进程存在且存活则为 true
     */
    public static boolean isAlive(long pid) {
        return ProcessHandle.of(pid)
                .map(ProcessHandle::isAlive)
                .orElse(false);
    }

    /**
     * Waits for the process with the given PID to exit, with a timeout.
     * 等待给定 PID 的进程退出，带超时。
     *
     * @param pid     the process ID - 进程 ID
     * @param timeout the maximum time to wait - 最大等待时间
     * @param unit    the time unit - 时间单位
     * @return an Optional containing the exit status, or empty if the process was not found
     *         or did not exit within the timeout - 包含退出状态的 Optional，若进程未找到或未在超时内退出则为空
     * @throws NullPointerException if unit is null - 若 unit 为 null
     */
    public static Optional<Integer> waitFor(long pid, long timeout, TimeUnit unit) {
        Objects.requireNonNull(unit, "unit must not be null");
        return ProcessHandle.of(pid).flatMap(handle -> {
            try {
                boolean exited = handle.onExit()
                        .orTimeout(timeout, unit)
                        .thenApply(_ -> true)
                        .exceptionally(_ -> false)
                        .join();
                if (exited && !handle.isAlive()) {
                    // ProcessHandle does not expose exit code; return 0 as sentinel for "exited"
                    return Optional.of(0);
                }
                return Optional.empty();
            } catch (Exception e) {
                return Optional.empty();
            }
        });
    }

    // ==================== Internal | 内部实现 ====================

    /**
     * Builds a ProcessBuilder from a ProcessConfig.
     * 从 ProcessConfig 构建 ProcessBuilder。
     */
    private static ProcessBuilder buildProcessBuilder(ProcessConfig config) {
        ProcessBuilder pb = new ProcessBuilder(config.command());

        if (config.workingDirectory() != null) {
            pb.directory(config.workingDirectory().toFile());
        }

        if (!config.environment().isEmpty()) {
            pb.environment().putAll(config.environment());
        }

        if (config.redirectErrorStream()) {
            pb.redirectErrorStream(true);
        }

        if (config.inheritIO()) {
            pb.inheritIO();
        } else {
            if (config.stdoutFile() != null) {
                pb.redirectOutput(config.stdoutFile().toFile());
            }
            if (config.stderrFile() != null) {
                pb.redirectError(config.stderrFile().toFile());
            }
        }

        return pb;
    }

    /**
     * Waits for a process to exit, applying timeout if configured.
     * 等待进程退出，如果配置了超时则应用超时。
     */
    private static ProcessResult waitForProcess(Process process, ProcessConfig config,
                                                Instant startTime,
                                                String stdout, String stderr) throws InterruptedException {
        int exitCode;
        if (config.timeout() != null) {
            boolean finished = process.waitFor(config.timeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
                throw new OpenException("Process", "PROCESS_TIMEOUT",
                        "Process timed out after " + config.timeout()
                                + " | command=" + String.join(" ", config.command()));
            }
            exitCode = process.exitValue();
        } else {
            exitCode = process.waitFor();
        }

        Duration duration = Duration.between(startTime, Instant.now());
        return new ProcessResult(
                exitCode,
                stdout != null ? stdout : "",
                stderr != null ? stderr : "",
                duration,
                config.command()
        );
    }

    /**
     * Reads an InputStream into a StringBuilder, limiting to MAX_OUTPUT_SIZE.
     * 将 InputStream 读入 StringBuilder，限制为 MAX_OUTPUT_SIZE。
     */
    private static void readStream(InputStream is, StringBuilder sb) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            char[] buffer = new char[8192];
            int totalRead = 0;
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                int remaining = MAX_OUTPUT_SIZE - totalRead;
                if (remaining <= 0) {
                    // Drain the rest to avoid blocking the process
                    while (reader.read(buffer) != -1) {
                        // discard
                    }
                    break;
                }
                int toAppend = Math.min(charsRead, remaining);
                sb.append(buffer, 0, toAppend);
                totalRead += toAppend;
            }
        } catch (IOException _) {
            // Stream closed or read error — best effort
        }
    }
}
