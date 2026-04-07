package cloud.opencode.base.core.process;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * ProcessInfo - Immutable snapshot of process information
 * ProcessInfo - 进程信息的不可变快照
 *
 * <p>Captures key attributes of an operating system process at a point in time.
 * Built from {@link ProcessHandle} via the {@link #from(ProcessHandle)} factory method.</p>
 * <p>在某一时刻捕获操作系统进程的关键属性。
 * 通过 {@link #from(ProcessHandle)} 工厂方法从 {@link ProcessHandle} 构建。</p>
 *
 * <p><strong>Fields | 字段:</strong></p>
 * <ul>
 *   <li>{@code pid} - Process ID - 进程 ID</li>
 *   <li>{@code command} - Executable name only (may be empty) - 可执行文件名（可能为空）</li>
 *   <li>{@code commandLine} - Full command line (may be empty for security) - 完整命令行（出于安全可能为空）</li>
 *   <li>{@code user} - Process owner (may be empty) - 进程所有者（可能为空）</li>
 *   <li>{@code startTime} - When process started (may be null) - 进程启动时间（可能为 null）</li>
 *   <li>{@code cpuDuration} - Total CPU time used (may be null) - 已使用的 CPU 总时间（可能为 null）</li>
 *   <li>{@code alive} - Whether process is currently alive - 进程是否存活</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong> This record is immutable and thread-safe.
 * 此记录是不可变的，线程安全。</p>
 *
 * @param pid         the process ID - 进程 ID
 * @param command     the executable name (may be empty) - 可执行文件名（可能为空）
 * @param commandLine the full command line (may be empty) - 完整命令行（可能为空）
 * @param user        the process owner (may be empty) - 进程所有者（可能为空）
 * @param startTime   when the process started, or {@code null} if unknown - 进程启动时间，未知时为 null
 * @param cpuDuration total CPU time used, or {@code null} if unknown - 已使用的 CPU 总时间，未知时为 null
 * @param alive       whether the process is currently alive - 进程是否存活
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ProcessManager
 * @see ProcessHandle
 * @since JDK 25, opencode-base-core V1.0.3
 */
public record ProcessInfo(
        long pid,
        String command,
        String commandLine,
        String user,
        Instant startTime,
        Duration cpuDuration,
        boolean alive
) {

    /**
     * Compact constructor that enforces non-null for String fields.
     * 紧凑构造函数，确保字符串字段非 null。
     */
    public ProcessInfo {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(commandLine, "commandLine must not be null");
        Objects.requireNonNull(user, "user must not be null");
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a {@code ProcessInfo} from a {@link ProcessHandle}.
     * 从 {@link ProcessHandle} 创建 {@code ProcessInfo}。
     *
     * <p>All optional fields from {@link ProcessHandle.Info} are handled gracefully
     * with sensible defaults (empty strings, null for temporal values).</p>
     * <p>{@link ProcessHandle.Info} 中的所有可选字段均以合理默认值优雅处理
     * （空字符串、时间值为 null）。</p>
     *
     * @param handle the process handle - 进程句柄
     * @return a new ProcessInfo snapshot - 新的 ProcessInfo 快照
     * @throws NullPointerException if handle is null - 若 handle 为 null
     */
    public static ProcessInfo from(ProcessHandle handle) {
        Objects.requireNonNull(handle, "handle must not be null");
        ProcessHandle.Info info = handle.info();
        return new ProcessInfo(
                handle.pid(),
                info.command().map(ProcessInfo::extractCommandName).orElse(""),
                info.commandLine().orElse(""),
                info.user().orElse(""),
                info.startInstant().orElse(null),
                info.totalCpuDuration().orElse(null),
                handle.isAlive()
        );
    }

    /**
     * Creates a {@code ProcessInfo} for the current JVM process.
     * 为当前 JVM 进程创建 {@code ProcessInfo}。
     *
     * @return process info for the current process - 当前进程的进程信息
     */
    public static ProcessInfo fromCurrent() {
        return from(ProcessHandle.current());
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Returns the start time as an {@link Optional}.
     * 以 {@link Optional} 形式返回启动时间。
     *
     * @return optional start instant - 可选的启动时间
     */
    public Optional<Instant> startInstant() {
        return Optional.ofNullable(startTime);
    }

    /**
     * Returns the CPU time as an {@link Optional}.
     * 以 {@link Optional} 形式返回 CPU 时间。
     *
     * @return optional CPU duration - 可选的 CPU 时间
     */
    public Optional<Duration> cpuTime() {
        return Optional.ofNullable(cpuDuration);
    }

    /**
     * Returns the uptime in milliseconds since the process started, or {@code -1} if unknown.
     * 返回进程启动以来的毫秒数，未知时返回 {@code -1}。
     *
     * @return uptime in milliseconds, or -1 if start time is unknown - 运行时间（毫秒），未知时为 -1
     */
    public long uptimeMillis() {
        if (startTime == null) {
            return -1L;
        }
        Duration uptime = Duration.between(startTime, Instant.now());
        return uptime.isNegative() ? 0L : uptime.toMillis();
    }

    // ==================== Internal | 内部实现 ====================

    /**
     * Extracts the executable name from a full path.
     * 从完整路径中提取可执行文件名。
     */
    private static String extractCommandName(String fullPath) {
        if (fullPath == null || fullPath.isEmpty()) {
            return "";
        }
        // Handle both Unix (/) and Windows (\) separators
        int lastSlash = fullPath.lastIndexOf('/');
        int lastBackslash = fullPath.lastIndexOf('\\');
        int lastSep = Math.max(lastSlash, lastBackslash);
        return lastSep >= 0 ? fullPath.substring(lastSep + 1) : fullPath;
    }
}
