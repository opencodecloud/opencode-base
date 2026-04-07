package cloud.opencode.base.core.process;

import cloud.opencode.base.core.exception.OpenException;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * ProcessResult - Immutable result of a process execution
 * ProcessResult - 进程执行的不可变结果
 *
 * <p>Captures the exit code, standard output, standard error, wall-clock duration,
 * and the command that was executed.</p>
 * <p>捕获退出码、标准输出、标准错误、挂钟时间以及执行的命令。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ProcessResult result = ProcessManager.execute("ls", "-la");
 * if (result.isSuccess()) {
 *     System.out.println(result.stdout());
 * }
 *
 * // Or throw on failure
 * ProcessManager.execute("git", "status").orThrow();
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong> This record is immutable and thread-safe.
 * 此记录是不可变的，线程安全。</p>
 *
 * @param exitCode the process exit code (0 typically means success) - 进程退出码（0 通常表示成功）
 * @param stdout   captured standard output - 捕获的标准输出
 * @param stderr   captured standard error - 捕获的标准错误
 * @param duration wall-clock execution time - 挂钟执行时间
 * @param command  the command that was executed - 执行的命令
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ProcessManager
 * @since JDK 25, opencode-base-core V1.0.3
 */
public record ProcessResult(
        int exitCode,
        String stdout,
        String stderr,
        Duration duration,
        List<String> command
) {

    /**
     * Compact constructor that enforces non-null invariants and defensive copies.
     * 紧凑构造函数，确保非 null 不变量和防御性复制。
     */
    public ProcessResult {
        Objects.requireNonNull(stdout, "stdout must not be null");
        Objects.requireNonNull(stderr, "stderr must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        Objects.requireNonNull(command, "command must not be null");
        command = List.copyOf(command);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a new {@code ProcessResult}.
     * 创建新的 {@code ProcessResult}。
     *
     * @param exitCode the exit code - 退出码
     * @param stdout   standard output - 标准输出
     * @param stderr   standard error - 标准错误
     * @param duration execution duration - 执行时间
     * @param command  the command - 命令
     * @return a new ProcessResult - 新的 ProcessResult
     */
    public static ProcessResult of(int exitCode, String stdout, String stderr,
                                   Duration duration, List<String> command) {
        return new ProcessResult(exitCode, stdout, stderr, duration, command);
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Returns {@code true} if the process exited with code 0.
     * 如果进程以退出码 0 退出，返回 {@code true}。
     *
     * @return true if exit code is 0 - 退出码为 0 时返回 true
     */
    public boolean isSuccess() {
        return exitCode == 0;
    }

    /**
     * Alias for {@link #stdout()}.
     * {@link #stdout()} 的别名。
     *
     * @return the standard output - 标准输出
     */
    public String output() {
        return stdout;
    }

    /**
     * Returns this result if successful, otherwise throws an {@link OpenException}.
     * 如果成功则返回此结果，否则抛出 {@link OpenException}。
     *
     * <p>The exception message includes the exit code, command, and a truncated stderr snippet.
     * Callers should be cautious about exposing this exception to end users, as stderr
     * may contain sensitive system information.</p>
     * <p>异常消息包含退出码、命令和截断的 stderr 片段。
     * 调用者应注意不要将此异常暴露给最终用户，因为 stderr 可能包含敏感系统信息。</p>
     *
     * @return this result if exit code is 0 - 退出码为 0 时返回此结果
     * @throws OpenException if exit code is not 0 - 退出码不为 0 时抛出
     */
    public ProcessResult orThrow() {
        if (!isSuccess()) {
            String stderrSnippet = stderr.length() > 200
                    ? stderr.substring(0, 200) + "...(truncated)"
                    : stderr;
            throw new OpenException("Process", "PROCESS_EXIT_" + exitCode,
                    "Process exited with code " + exitCode
                            + " | command=" + String.join(" ", command)
                            + " | stderr=" + stderrSnippet);
        }
        return this;
    }
}
