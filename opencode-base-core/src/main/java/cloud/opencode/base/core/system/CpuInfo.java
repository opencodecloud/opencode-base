package cloud.opencode.base.core.system;

import java.util.Objects;

/**
 * Immutable snapshot of CPU information.
 * CPU 信息的不可变快照。
 *
 * <p>Captures processor count, architecture, and current load metrics at the
 * time of creation. Load values may be {@code -1} if the underlying OS does
 * not expose them.</p>
 * <p>捕获创建时的处理器数量、架构和当前负载指标。如果底层操作系统不公开负载值，
 * 则可能为 {@code -1}。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CpuInfo cpu = SystemInfo.cpu();
 * System.out.println("Processors: " + cpu.availableProcessors());
 * System.out.println("Arch: " + cpu.archDisplay());
 * System.out.println("System load: " + cpu.systemCpuLoad());
 * }</pre>
 *
 * @param availableProcessors number of available processors - 可用处理器数量
 * @param arch                processor architecture (e.g. "amd64", "aarch64") - 处理器架构
 * @param systemCpuLoad       system-wide CPU load [0.0, 1.0], or -1 if unavailable - 系统 CPU 负载
 * @param processCpuLoad      JVM process CPU load [0.0, 1.0], or -1 if unavailable - 进程 CPU 负载
 * @param loadAverage         1/5/15 minute load averages; empty array on unsupported platforms - 1/5/15 分钟平均负载
 * @author Leon Soo
 * @see SystemInfo
 * @since JDK 25, opencode-base-core V1.0.3
 */
public record CpuInfo(
        int availableProcessors,
        String arch,
        double systemCpuLoad,
        double processCpuLoad,
        double[] loadAverage
) {

    /**
     * Compact canonical constructor with validation and defensive copy.
     * 带验证和防御性复制的紧凑规范构造器。
     */
    public CpuInfo {
        if (availableProcessors < 1) {
            throw new IllegalArgumentException(
                    "availableProcessors must be >= 1, got: " + availableProcessors);
        }
        Objects.requireNonNull(arch, "arch must not be null");
        Objects.requireNonNull(loadAverage, "loadAverage must not be null");
        loadAverage = loadAverage.clone(); // defensive copy
    }

    /**
     * Returns a defensive copy of the load average array.
     * 返回平均负载数组的防御性副本。
     *
     * @return copy of load average values
     */
    @Override
    public double[] loadAverage() {
        return loadAverage.clone();
    }

    /**
     * Checks whether load average data is available.
     * 检查平均负载数据是否可用。
     *
     * @return {@code true} if load average contains at least one value
     */
    public boolean isLoadAvailable() {
        return loadAverage.length > 0 && loadAverage[0] >= 0;
    }

    /**
     * Returns a display-friendly architecture name.
     * 返回用户友好的架构名称。
     *
     * <p>Normalizes common architecture names:</p>
     * <ul>
     *   <li>"amd64" → "x86_64"</li>
     *   <li>"x86" → "x86_32"</li>
     * </ul>
     *
     * @return the display architecture name
     */
    public String archDisplay() {
        return switch (arch) {
            case "amd64" -> "x86_64";
            case "x86" -> "x86_32";
            default -> arch;
        };
    }
}
