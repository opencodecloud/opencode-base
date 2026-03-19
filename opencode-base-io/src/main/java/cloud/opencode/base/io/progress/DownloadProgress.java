package cloud.opencode.base.io.progress;

import java.util.Objects;

/**
 * Download Progress - Download Progress Information
 * 下载进度 - 下载进度信息
 *
 * <p>This class represents the current progress of a download operation,
 * including bytes transferred, speed, and estimated time remaining.</p>
 * <p>此类表示下载操作的当前进度，包括已传输字节、速度和预计剩余时间。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * DownloadProgress progress = DownloadProgress.of(1000000, 500000, 100000);
 * System.out.println(progress.formattedPercentage()); // "50.00%"
 * System.out.println(progress.formattedSpeed());      // "100.00 KB/s"
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Download progress tracking with bytes/speed/percentage - 下载进度跟踪（字节/速度/百分比）</li>
 *   <li>Formatted output for percentage and speed - 格式化输出百分比和速度</li>
 *   <li>Estimated remaining time calculation - 估算剩余时间</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DownloadProgress progress = DownloadProgress.of(1000000, 500000, 100000);
 * System.out.println(progress.formattedPercentage());  // "50.00%"
 * System.out.println(progress.formattedSpeed());       // "100.00 KB/s"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No, mutable fields - 线程安全: 否，可变字段</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class DownloadProgress {

    private static final long KB = 1024;
    private static final long MB = 1024 * KB;
    private static final long GB = 1024 * MB;

    private final long totalBytes;
    private final long downloadedBytes;
    private final double percentage;
    private final long speed;           // bytes per second
    private final long remainingTime;   // seconds

    // ==================== Constructors ====================

    /**
     * Creates a download progress.
     * 创建下载进度。
     *
     * @param totalBytes      total bytes to download - 总字节数
     * @param downloadedBytes downloaded bytes - 已下载字节
     * @param percentage      completion percentage - 完成百分比
     * @param speed           download speed in bytes/sec - 下载速度（字节/秒）
     * @param remainingTime   remaining time in seconds - 剩余时间（秒）
     */
    public DownloadProgress(long totalBytes, long downloadedBytes, double percentage,
                            long speed, long remainingTime) {
        this.totalBytes = totalBytes;
        this.downloadedBytes = downloadedBytes;
        this.percentage = percentage;
        this.speed = speed;
        this.remainingTime = remainingTime;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a progress instance.
     * 创建进度实例。
     *
     * @param totalBytes      total bytes - 总字节数
     * @param downloadedBytes downloaded bytes - 已下载字节
     * @param speed           speed in bytes/sec - 速度（字节/秒）
     * @return the progress - 进度
     */
    public static DownloadProgress of(long totalBytes, long downloadedBytes, long speed) {
        double percentage = totalBytes > 0 ? (downloadedBytes * 100.0 / totalBytes) : 0;
        long remaining = speed > 0 ? (totalBytes - downloadedBytes) / speed : 0;
        return new DownloadProgress(totalBytes, downloadedBytes, percentage, speed, remaining);
    }

    /**
     * Creates a completed progress.
     * 创建完成的进度。
     *
     * @param totalBytes total bytes - 总字节数
     * @return the progress - 进度
     */
    public static DownloadProgress completed(long totalBytes) {
        return new DownloadProgress(totalBytes, totalBytes, 100.0, 0, 0);
    }

    /**
     * Creates an unknown progress.
     * 创建未知的进度。
     *
     * @param downloadedBytes downloaded bytes - 已下载字节
     * @param speed           speed in bytes/sec - 速度（字节/秒）
     * @return the progress - 进度
     */
    public static DownloadProgress unknown(long downloadedBytes, long speed) {
        return new DownloadProgress(-1, downloadedBytes, -1, speed, -1);
    }

    // ==================== Getters ====================

    /**
     * Gets the total bytes.
     * 获取总字节数。
     *
     * @return total bytes or -1 if unknown - 总字节数或 -1（如果未知）
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * Gets the downloaded bytes.
     * 获取已下载字节。
     *
     * @return downloaded bytes - 已下载字节
     */
    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    /**
     * Gets the completion percentage.
     * 获取完成百分比。
     *
     * @return percentage (0-100) or -1 if unknown - 百分比（0-100）或 -1（如果未知）
     */
    public double getPercentage() {
        return percentage;
    }

    /**
     * Gets the download speed.
     * 获取下载速度。
     *
     * @return speed in bytes per second - 速度（字节/秒）
     */
    public long getSpeed() {
        return speed;
    }

    /**
     * Gets the remaining time.
     * 获取剩余时间。
     *
     * @return remaining time in seconds or -1 if unknown - 剩余时间（秒）或 -1（如果未知）
     */
    public long getRemainingTime() {
        return remainingTime;
    }

    // ==================== Derived Values ====================

    /**
     * Checks if download is complete.
     * 检查下载是否完成。
     *
     * @return true if complete - 如果完成返回 true
     */
    public boolean isComplete() {
        return totalBytes > 0 && downloadedBytes >= totalBytes;
    }

    /**
     * Checks if total size is known.
     * 检查总大小是否已知。
     *
     * @return true if known - 如果已知返回 true
     */
    public boolean isTotalKnown() {
        return totalBytes >= 0;
    }

    // ==================== Formatted Values ====================

    /**
     * Gets formatted percentage.
     * 获取格式化的百分比。
     *
     * @return formatted percentage like "50.00%" - 格式化的百分比
     */
    public String formattedPercentage() {
        if (percentage < 0) {
            return "Unknown";
        }
        return String.format("%.2f%%", percentage);
    }

    /**
     * Gets formatted speed.
     * 获取格式化的速度。
     *
     * @return formatted speed like "1.50 MB/s" - 格式化的速度
     */
    public String formattedSpeed() {
        return formatBytes(speed) + "/s";
    }

    /**
     * Gets formatted total bytes.
     * 获取格式化的总字节数。
     *
     * @return formatted total like "1.50 GB" - 格式化的总数
     */
    public String formattedTotalBytes() {
        if (totalBytes < 0) {
            return "Unknown";
        }
        return formatBytes(totalBytes);
    }

    /**
     * Gets formatted downloaded bytes.
     * 获取格式化的已下载字节。
     *
     * @return formatted downloaded like "500.00 MB" - 格式化的已下载
     */
    public String formattedDownloadedBytes() {
        return formatBytes(downloadedBytes);
    }

    /**
     * Gets formatted remaining time.
     * 获取格式化的剩余时间。
     *
     * @return formatted time like "2h 30m 15s" - 格式化的时间
     */
    public String formattedRemainingTime() {
        if (remainingTime < 0) {
            return "Unknown";
        }
        if (remainingTime == 0) {
            return "0s";
        }

        long hours = remainingTime / 3600;
        long minutes = (remainingTime % 3600) / 60;
        long seconds = remainingTime % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    private static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "Unknown";
        }
        if (bytes >= GB) {
            return String.format("%.2f GB", bytes / (double) GB);
        }
        if (bytes >= MB) {
            return String.format("%.2f MB", bytes / (double) MB);
        }
        if (bytes >= KB) {
            return String.format("%.2f KB", bytes / (double) KB);
        }
        return bytes + " B";
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DownloadProgress that)) return false;
        return totalBytes == that.totalBytes &&
                downloadedBytes == that.downloadedBytes &&
                Double.compare(that.percentage, percentage) == 0 &&
                speed == that.speed &&
                remainingTime == that.remainingTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalBytes, downloadedBytes, percentage, speed, remainingTime);
    }

    @Override
    public String toString() {
        return "DownloadProgress[" + formattedPercentage() + ", " +
                formattedSpeed() + ", ETA: " + formattedRemainingTime() + "]";
    }
}
