package cloud.opencode.base.core.system;

/**
 * Immutable snapshot of memory information.
 * 内存信息的不可变快照。
 *
 * <p>Represents a point-in-time view of memory usage, applicable to both
 * JVM heap/non-heap memory and physical system memory.</p>
 * <p>表示内存使用情况的时间点视图，适用于 JVM 堆/非堆内存和物理系统内存。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MemoryInfo heap = SystemInfo.heapMemory();
 * System.out.println("Heap used: " + heap.usedDisplay());
 * System.out.println("Usage: " + heap.usagePercent() + "%");
 *
 * MemoryInfo physical = SystemInfo.memory();
 * System.out.println("Physical total: " + physical.totalDisplay());
 * }</pre>
 *
 * @param total total memory in bytes - 总内存（字节）
 * @param used  used memory in bytes - 已用内存（字节）
 * @param free  free memory in bytes - 空闲内存（字节）
 * @param max   maximum memory in bytes, -1 if undefined - 最大内存（字节），未定义时为 -1
 * @author Leon Soo
 * @see SystemInfo
 * @since JDK 25, opencode-base-core V1.0.3
 */
public record MemoryInfo(
        long total,
        long used,
        long free,
        long max
) {

    /**
     * Compact canonical constructor with validation.
     * 带验证的紧凑规范构造器。
     */
    public MemoryInfo {
        if (total < 0) {
            throw new IllegalArgumentException("total must be non-negative, got: " + total);
        }
        if (used < 0) {
            throw new IllegalArgumentException("used must be non-negative, got: " + used);
        }
        if (free < 0) {
            throw new IllegalArgumentException("free must be non-negative, got: " + free);
        }
        if (max < -1) {
            throw new IllegalArgumentException("max must be >= -1, got: " + max);
        }
    }

    /**
     * Creates a MemoryInfo with the given values.
     * 使用给定值创建 MemoryInfo。
     *
     * @param total total bytes - 总字节数
     * @param used  used bytes - 已用字节数
     * @param free  free bytes - 空闲字节数
     * @param max   max bytes (-1 if undefined) - 最大字节数（未定义时为 -1）
     * @return a new MemoryInfo instance
     */
    public static MemoryInfo of(long total, long used, long free, long max) {
        return new MemoryInfo(total, used, free, max);
    }

    /**
     * Creates a MemoryInfo for physical memory where max equals total.
     * 为物理内存创建 MemoryInfo，其中 max 等于 total。
     *
     * @param total total physical memory bytes - 总物理内存字节数
     * @param free  free physical memory bytes - 空闲物理内存字节数
     * @return a new MemoryInfo instance
     */
    public static MemoryInfo ofPhysical(long total, long free) {
        long used = total - free;
        if (used < 0) {
            used = 0;
        }
        return new MemoryInfo(total, used, free, total);
    }

    /**
     * Returns the memory usage percentage (used/total * 100).
     * 返回内存使用百分比（used/total * 100）。
     *
     * @return usage percentage in range [0.0, 100.0], or 0.0 if total is 0
     */
    public double usagePercent() {
        if (total == 0) {
            return 0.0;
        }
        return (double) used / total * 100.0;
    }

    /**
     * Returns a human-readable representation of total memory.
     * 返回总内存的可读表示。
     *
     * @return formatted string e.g. "16.0 GB"
     */
    public String totalDisplay() {
        return formatBytes(total);
    }

    /**
     * Returns a human-readable representation of used memory.
     * 返回已用内存的可读表示。
     *
     * @return formatted string e.g. "8.5 GB"
     */
    public String usedDisplay() {
        return formatBytes(used);
    }

    /**
     * Returns a human-readable representation of free memory.
     * 返回空闲内存的可读表示。
     *
     * @return formatted string e.g. "7.5 GB"
     */
    public String freeDisplay() {
        return formatBytes(free);
    }

    /**
     * Formats a byte count into a human-readable string with 1 decimal place.
     * 将字节数格式化为带 1 位小数的可读字符串。
     *
     * @param bytes the byte count - 字节数
     * @return formatted string e.g. "1.5 GB", "256.0 MB"
     */
    static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "N/A";
        }
        if (bytes < 1024L) {
            return bytes + " B";
        }
        if (bytes < 1024L * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        if (bytes < 1024L * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        }
        if (bytes < 1024L * 1024 * 1024 * 1024) {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
        return String.format("%.1f TB", bytes / (1024.0 * 1024 * 1024 * 1024));
    }
}
