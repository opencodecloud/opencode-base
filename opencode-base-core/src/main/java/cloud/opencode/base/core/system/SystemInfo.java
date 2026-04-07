package cloud.opencode.base.core.system;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SystemInfo - Comprehensive system information facade
 * SystemInfo - 全面的系统信息门面
 *
 * <p>Provides detailed CPU, memory, disk, OS, and JVM runtime information
 * beyond what {@link cloud.opencode.base.core.Environment} offers.
 * All snapshot methods return immutable records that are safe to cache and share
 * across threads.</p>
 * <p>提供比 {@link cloud.opencode.base.core.Environment} 更详细的 CPU、内存、磁盘、
 * 操作系统和 JVM 运行时信息。所有快照方法返回不可变记录，可安全缓存和跨线程共享。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>CPU load and processor info - CPU 负载和处理器信息</li>
 *   <li>JVM heap/non-heap and physical memory metrics - JVM 堆/非堆及物理内存指标</li>
 *   <li>Disk/file-store capacity and usage - 磁盘/文件存储容量和使用情况</li>
 *   <li>OS identification and hostname - 操作系统识别和主机名</li>
 *   <li>JVM runtime details (version, uptime, PID, arguments) - JVM 运行时详情</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // CPU information
 * CpuInfo cpu = SystemInfo.cpu();
 * double load = SystemInfo.cpuLoad();
 *
 * // Memory information
 * MemoryInfo heap = SystemInfo.heapMemory();
 * long physMem = SystemInfo.physicalMemoryTotal();
 *
 * // Disk information
 * List<DiskInfo> disks = SystemInfo.disks();
 * DiskInfo root = SystemInfo.disk(Path.of("/"));
 *
 * // OS and runtime
 * String host = SystemInfo.hostname();
 * RuntimeInfo rt = SystemInfo.runtime();
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>All methods are stateless and thread-safe. Returned records are immutable.</p>
 * <p>所有方法都是无状态且线程安全的。返回的记录是不可变的。</p>
 *
 * @author Leon Soo
 * @see CpuInfo
 * @see MemoryInfo
 * @see DiskInfo
 * @see OsInfo
 * @see RuntimeInfo
 * @since JDK 25, opencode-base-core V1.0.3
 */
public final class SystemInfo {

    private SystemInfo() {
        throw new AssertionError("No SystemInfo instances for you!");
    }

    // ==================== CPU | CPU 信息 ====================

    /**
     * Returns a snapshot of current CPU information.
     * 返回当前 CPU 信息的快照。
     *
     * @return CPU info snapshot
     */
    public static CpuInfo cpu() {
        return new CpuInfo(
                Runtime.getRuntime().availableProcessors(),
                System.getProperty("os.arch", "unknown"),
                systemCpuLoadSafe(),
                processCpuLoadSafe(),
                loadAverageSafe()
        );
    }

    /**
     * Returns the current system-wide CPU load as a value in [0.0, 1.0],
     * or -1.0 if not available.
     * 返回当前系统级 CPU 负载，值在 [0.0, 1.0] 范围内，不可用时返回 -1.0。
     *
     * @return system CPU load, or -1.0 if unavailable
     */
    public static double cpuLoad() {
        return systemCpuLoadSafe();
    }

    /**
     * Returns the current JVM process CPU load as a value in [0.0, 1.0],
     * or -1.0 if not available.
     * 返回当前 JVM 进程 CPU 负载，值在 [0.0, 1.0] 范围内，不可用时返回 -1.0。
     *
     * @return process CPU load, or -1.0 if unavailable
     */
    public static double processCpuLoad() {
        return processCpuLoadSafe();
    }

    /**
     * Returns the system load averages for 1, 5, and 15 minutes.
     * 返回 1、5、15 分钟的系统平均负载。
     *
     * <p>On Linux, attempts to read {@code /proc/loadavg} for all three values.
     * On other Unix-like systems, returns the 1-minute average from
     * {@link OperatingSystemMXBean#getSystemLoadAverage()}.
     * On Windows, returns an empty array.</p>
     * <p>在 Linux 上尝试从 {@code /proc/loadavg} 读取全部三个值。
     * 在其他类 Unix 系统上，返回 {@link OperatingSystemMXBean#getSystemLoadAverage()}
     * 的 1 分钟平均值。在 Windows 上返回空数组。</p>
     *
     * @return load average array (may be empty on unsupported platforms)
     */
    public static double[] loadAverage() {
        return loadAverageSafe();
    }

    // ==================== Memory | 内存信息 ====================

    /**
     * Returns a snapshot of physical memory information.
     * 返回物理内存信息的快照。
     *
     * @return physical memory info snapshot
     */
    public static MemoryInfo memory() {
        long total = physicalMemoryTotal();
        long free = physicalMemoryFree();
        return MemoryInfo.ofPhysical(total, free);
    }

    /**
     * Returns a snapshot of JVM heap memory information.
     * 返回 JVM 堆内存信息的快照。
     *
     * @return heap memory info snapshot
     */
    public static MemoryInfo heapMemory() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        return MemoryInfo.of(
                heap.getCommitted(),
                heap.getUsed(),
                heap.getCommitted() - heap.getUsed(),
                heap.getMax()
        );
    }

    /**
     * Returns a snapshot of JVM non-heap memory information.
     * 返回 JVM 非堆内存信息的快照。
     *
     * @return non-heap memory info snapshot
     */
    public static MemoryInfo nonHeapMemory() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
        long committed = nonHeap.getCommitted();
        long used = nonHeap.getUsed();
        long free = committed - used;
        if (free < 0) {
            free = 0;
        }
        return MemoryInfo.of(committed, used, free, nonHeap.getMax());
    }

    /**
     * Returns the total physical memory in bytes, or -1 if unavailable.
     * 返回总物理内存（字节），不可用时返回 -1。
     *
     * @return total physical memory bytes, or -1
     */
    public static long physicalMemoryTotal() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                return sunBean.getTotalMemorySize();
            }
        } catch (Exception _) {
            // com.sun.management not available
        }
        return -1;
    }

    /**
     * Returns the free physical memory in bytes, or -1 if unavailable.
     * 返回空闲物理内存（字节），不可用时返回 -1。
     *
     * @return free physical memory bytes, or -1
     */
    public static long physicalMemoryFree() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                return sunBean.getFreeMemorySize();
            }
        } catch (Exception _) {
            // com.sun.management not available
        }
        return -1;
    }

    /**
     * Returns the total swap space in bytes, or -1 if unavailable.
     * 返回总交换空间（字节），不可用时返回 -1。
     *
     * @return total swap bytes, or -1
     */
    public static long swapTotal() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                return sunBean.getTotalSwapSpaceSize();
            }
        } catch (Exception _) {
            // com.sun.management not available
        }
        return -1;
    }

    /**
     * Returns the free swap space in bytes, or -1 if unavailable.
     * 返回空闲交换空间（字节），不可用时返回 -1。
     *
     * @return free swap bytes, or -1
     */
    public static long swapFree() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                return sunBean.getFreeSwapSpaceSize();
            }
        } catch (Exception _) {
            // com.sun.management not available
        }
        return -1;
    }

    // ==================== Disk | 磁盘信息 ====================

    /**
     * Returns information for all file stores (disks/partitions).
     * 返回所有文件存储（磁盘/分区）的信息。
     *
     * <p>File stores that throw exceptions during query are silently skipped.</p>
     * <p>查询时抛出异常的文件存储将被静默跳过。</p>
     *
     * @return unmodifiable list of disk info snapshots
     */
    public static List<DiskInfo> disks() {
        List<DiskInfo> result = new ArrayList<>();
        for (FileStore store : FileSystems.getDefault().getFileStores()) {
            try {
                result.add(toDiskInfo(store));
            } catch (IOException _) {
                // skip inaccessible file stores
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns disk information for the file store containing the given path.
     * 返回包含给定路径的文件存储的磁盘信息。
     *
     * @param path the path to query - 要查询的路径
     * @return disk info for the path's file store
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if path is null
     */
    public static DiskInfo disk(Path path) throws IOException {
        java.util.Objects.requireNonNull(path, "path must not be null");
        FileStore store = Files.getFileStore(path);
        return toDiskInfo(store);
    }

    /**
     * Returns the total disk space for the given path, in bytes.
     * 返回给定路径的总磁盘空间（字节）。
     *
     * @param path the path to query - 要查询的路径
     * @return total disk space in bytes, or -1 if an error occurs
     */
    public static long diskTotal(Path path) {
        try {
            return Files.getFileStore(path).getTotalSpace();
        } catch (IOException _) {
            return -1;
        }
    }

    /**
     * Returns the free (unallocated) disk space for the given path, in bytes.
     * 返回给定路径的空闲（未分配）磁盘空间（字节）。
     *
     * @param path the path to query - 要查询的路径
     * @return free disk space in bytes, or -1 if an error occurs
     */
    public static long diskFree(Path path) {
        try {
            return Files.getFileStore(path).getUnallocatedSpace();
        } catch (IOException _) {
            return -1;
        }
    }

    /**
     * Returns the usable disk space for the given path, in bytes.
     * 返回给定路径的可用磁盘空间（字节）。
     *
     * <p>Usable space takes into account OS-level restrictions and may be
     * less than unallocated space.</p>
     * <p>可用空间考虑了操作系统级别的限制，可能小于未分配空间。</p>
     *
     * @param path the path to query - 要查询的路径
     * @return usable disk space in bytes, or -1 if an error occurs
     */
    public static long diskUsable(Path path) {
        try {
            return Files.getFileStore(path).getUsableSpace();
        } catch (IOException _) {
            return -1;
        }
    }

    // ==================== OS | 操作系统信息 ====================

    /**
     * Returns a snapshot of operating system information.
     * 返回操作系统信息的快照。
     *
     * <p>This method performs several system calls (hostname resolution, MXBean queries).
     * Cache the returned {@link OsInfo} if you need repeated access.</p>
     * <p>此方法执行多个系统调用（主机名解析、MXBean 查询）。
     * 如果需要反复访问，请缓存返回的 {@link OsInfo}。</p>
     *
     * @return OS info snapshot
     */
    public static OsInfo os() {
        return new OsInfo(
                System.getProperty("os.name", "unknown"),
                System.getProperty("os.version", "unknown"),
                System.getProperty("os.arch", "unknown"),
                hostname(),
                Runtime.getRuntime().availableProcessors(),
                physicalMemoryTotal(),
                swapTotal()
        );
    }

    /**
     * Returns the machine hostname.
     * 返回机器主机名。
     *
     * <p>Falls back to "unknown" if hostname resolution fails.</p>
     * <p>如果主机名解析失败则回退为 "unknown"。</p>
     *
     * @return the hostname, or "unknown" on failure
     */
    public static String hostname() {
        // Try environment variables first (fast, no DNS)
        String host = System.getenv("HOSTNAME");
        if (host != null && !host.isBlank()) {
            return host;
        }
        host = System.getenv("COMPUTERNAME");
        if (host != null && !host.isBlank()) {
            return host;
        }
        // Fallback to DNS resolution (may block if misconfigured)
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception _) {
            return "unknown";
        }
    }

    /**
     * Returns the JVM uptime in milliseconds.
     * 返回 JVM 运行时间（毫秒）。
     *
     * @return uptime in milliseconds
     */
    public static long uptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    /**
     * Returns an unmodifiable view of the system environment variables.
     * 返回系统环境变量的不可修改视图。
     *
     * <p><strong>Security Warning | 安全警告:</strong> Environment variables may contain
     * sensitive credentials (API keys, database passwords, tokens). Never log, serialize,
     * or expose the returned map to untrusted parties.</p>
     * <p><strong>安全警告:</strong> 环境变量可能包含敏感凭据（API 密钥、数据库密码、令牌）。
     * 切勿将返回的 Map 记录日志、序列化或暴露给不可信方。</p>
     *
     * @return unmodifiable map of environment variables
     */
    public static Map<String, String> environmentVariables() {
        return System.getenv(); // already unmodifiable
    }

    // ==================== Runtime | JVM 运行时信息 ====================

    /**
     * Returns a snapshot of JVM runtime information.
     * 返回 JVM 运行时信息的快照。
     *
     * @return runtime info snapshot
     */
    public static RuntimeInfo runtime() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return new RuntimeInfo(
                Runtime.version().toString(),
                System.getProperty("java.vendor", "unknown"),
                System.getProperty("java.home", "unknown"),
                runtimeBean.getVmName(),
                runtimeBean.getVmVersion(),
                runtimeBean.getUptime(),
                runtimeBean.getStartTime(),
                ProcessHandle.current().pid(),
                runtimeBean.getInputArguments()
        );
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    /**
     * Safely retrieves system CPU load, returning -1.0 on failure.
     */
    private static double systemCpuLoadSafe() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                return sunBean.getCpuLoad();
            }
        } catch (Exception _) {
            // com.sun.management not available
        }
        return -1.0;
    }

    /**
     * Safely retrieves process CPU load, returning -1.0 on failure.
     */
    private static double processCpuLoadSafe() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                return sunBean.getProcessCpuLoad();
            }
        } catch (Exception _) {
            // com.sun.management not available
        }
        return -1.0;
    }

    /**
     * Safely retrieves load averages. On Linux reads /proc/loadavg for 1/5/15 min values.
     * On other Unix-like systems uses OperatingSystemMXBean for 1-min average.
     * Returns empty array on Windows or on failure.
     */
    private static double[] loadAverageSafe() {
        String osName = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);

        // Windows does not support load average
        if (osName.contains("win")) {
            return new double[0];
        }

        // On Linux, try to read /proc/loadavg for all three values
        if (osName.contains("nux")) {
            try {
                String content = Files.readString(Path.of("/proc/loadavg"));
                String[] parts = content.trim().split("\\s+");
                if (parts.length >= 3) {
                    return new double[]{
                            Double.parseDouble(parts[0]),
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2])
                    };
                }
            } catch (IOException | NumberFormatException | SecurityException _) {
                // fall through to MXBean
            }
        }

        // Fallback: use OperatingSystemMXBean for 1-minute average
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double loadAvg = osBean.getSystemLoadAverage();
        if (loadAvg >= 0) {
            return new double[]{loadAvg};
        }
        return new double[0];
    }

    /**
     * Converts a FileStore to a DiskInfo record.
     */
    private static DiskInfo toDiskInfo(FileStore store) throws IOException {
        return new DiskInfo(
                store.name(),
                store.type(),
                store.getTotalSpace(),
                store.getUsableSpace(),
                store.getUnallocatedSpace(),
                store.isReadOnly()
        );
    }
}
