package cloud.opencode.base.core.system;

import java.util.Objects;

/**
 * Immutable snapshot of disk/file-store information.
 * 磁盘/文件存储信息的不可变快照。
 *
 * <p>Captures capacity and usage metrics for a single {@link java.nio.file.FileStore}
 * at the time of creation.</p>
 * <p>捕获创建时单个 {@link java.nio.file.FileStore} 的容量和使用指标。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<DiskInfo> disks = SystemInfo.disks();
 * for (DiskInfo disk : disks) {
 *     System.out.println(disk.name() + ": " + disk.usagePercent() + "% used");
 * }
 *
 * DiskInfo current = SystemInfo.disk(Path.of("."));
 * System.out.println("Usable: " + current.usableDisplay());
 * }</pre>
 *
 * @param name             file store name - 文件存储名称
 * @param type             file store type (e.g. "ext4", "apfs", "ntfs") - 文件存储类型
 * @param totalSpace       total space in bytes - 总空间（字节）
 * @param usableSpace      usable space in bytes - 可用空间（字节）
 * @param unallocatedSpace unallocated space in bytes - 未分配空间（字节）
 * @param readOnly         whether the file store is read-only - 是否只读
 * @author Leon Soo
 * @see SystemInfo
 * @since JDK 25, opencode-base-core V1.0.3
 */
public record DiskInfo(
        String name,
        String type,
        long totalSpace,
        long usableSpace,
        long unallocatedSpace,
        boolean readOnly
) {

    /**
     * Compact canonical constructor with validation.
     * 带验证的紧凑规范构造器。
     */
    public DiskInfo {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (totalSpace < 0) {
            throw new IllegalArgumentException("totalSpace must be non-negative, got: " + totalSpace);
        }
        if (usableSpace < 0) {
            throw new IllegalArgumentException("usableSpace must be non-negative, got: " + usableSpace);
        }
        if (unallocatedSpace < 0) {
            throw new IllegalArgumentException(
                    "unallocatedSpace must be non-negative, got: " + unallocatedSpace);
        }
    }

    /**
     * Returns the used space in bytes (total - unallocated).
     * 返回已用空间（字节）（总量 - 未分配）。
     *
     * @return used space in bytes
     */
    public long usedSpace() {
        long used = totalSpace - unallocatedSpace;
        return Math.max(used, 0);
    }

    /**
     * Returns the disk usage percentage (usedSpace / totalSpace * 100).
     * 返回磁盘使用百分比（usedSpace / totalSpace * 100）。
     *
     * @return usage percentage in range [0.0, 100.0], or 0.0 if totalSpace is 0
     */
    public double usagePercent() {
        if (totalSpace == 0) {
            return 0.0;
        }
        return (double) usedSpace() / totalSpace * 100.0;
    }

    /**
     * Returns a human-readable representation of total space.
     * 返回总空间的可读表示。
     *
     * @return formatted string e.g. "500.0 GB"
     */
    public String totalDisplay() {
        return MemoryInfo.formatBytes(totalSpace);
    }

    /**
     * Returns a human-readable representation of usable space.
     * 返回可用空间的可读表示。
     *
     * @return formatted string e.g. "250.0 GB"
     */
    public String usableDisplay() {
        return MemoryInfo.formatBytes(usableSpace);
    }
}
