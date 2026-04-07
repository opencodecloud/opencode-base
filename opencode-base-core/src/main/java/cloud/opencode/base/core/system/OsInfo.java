package cloud.opencode.base.core.system;

import java.util.Objects;

/**
 * Immutable snapshot of operating system information.
 * 操作系统信息的不可变快照。
 *
 * <p>Captures OS name, version, architecture, hostname, and basic hardware
 * metrics at the time of creation.</p>
 * <p>捕获创建时的操作系统名称、版本、架构、主机名和基本硬件指标。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * OsInfo os = SystemInfo.os();
 * System.out.println("OS: " + os.name() + " " + os.version());
 * System.out.println("Host: " + os.hostname());
 * }</pre>
 *
 * @param name                operating system name - 操作系统名称
 * @param version             operating system version - 操作系统版本
 * @param arch                processor architecture - 处理器架构
 * @param hostname            machine hostname - 主机名
 * @param availableProcessors number of available processors - 可用处理器数量
 * @param physicalMemoryTotal total physical memory in bytes - 总物理内存（字节）
 * @param swapTotal           total swap space in bytes - 总交换空间（字节）
 * @author Leon Soo
 * @see SystemInfo
 * @since JDK 25, opencode-base-core V1.0.3
 */
public record OsInfo(
        String name,
        String version,
        String arch,
        String hostname,
        int availableProcessors,
        long physicalMemoryTotal,
        long swapTotal
) {

    /**
     * Compact canonical constructor with validation.
     * 带验证的紧凑规范构造器。
     */
    public OsInfo {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(arch, "arch must not be null");
        Objects.requireNonNull(hostname, "hostname must not be null");
        if (availableProcessors < 1) {
            throw new IllegalArgumentException(
                    "availableProcessors must be >= 1, got: " + availableProcessors);
        }
    }
}
