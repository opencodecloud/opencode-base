package cloud.opencode.base.io.progress;

/**
 * Upload Progress - Snapshot of an upload operation's current progress
 * 上传进度 - 上传操作当前进度的快照
 *
 * <p>Passed to the progress listener callback at regular intervals during an upload.
 * All fields are immutable since this is a point-in-time snapshot.</p>
 * <p>上传期间定期传递给进度监听器回调。所有字段不可变，因为这是某一时刻的快照。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Upload progress snapshot with file name and bytes - 上传进度快照（文件名和字节数）</li>
 *   <li>Throughput calculation in bytes per second - 每秒字节数吞吐量计算</li>
 *   <li>Formatted percentage output - 格式化百分比输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * UploadProgress progress = new UploadProgress("data.zip", 500000, 1000000, 5000);
 * System.out.println(progress.formattedPercentage());  // "50.0%"
 * System.out.println(progress.throughputBytesPerSec()); // 100000.0
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No, fileName must not be null - 空值安全: 否，fileName不可为null</li>
 * </ul>
 *
 * @param fileName      the file name being uploaded - 正在上传的文件名
 * @param bytesUploaded bytes uploaded so far - 迄今已上传的字节数
 * @param totalBytes    total bytes to upload (may be 0 if unknown) - 要上传的总字节数（未知时为 0）
 * @param elapsedMs     elapsed time in milliseconds since the upload started - 上传开始以来的耗时（毫秒）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public record UploadProgress(String fileName, long bytesUploaded, long totalBytes, long elapsedMs) {

    /**
     * Returns the upload percentage as a formatted string (e.g., "42.5%").
     * 以格式化字符串形式返回上传百分比（例如"42.5%"）。
     *
     * @return formatted percentage string - 格式化的百分比字符串
     */
    public String formattedPercentage() {
        if (totalBytes <= 0) return "?%";
        double pct = (double) bytesUploaded / totalBytes * 100.0;
        return String.format("%.1f%%", pct);
    }

    /**
     * Returns the current upload throughput in bytes per second.
     * Returns 0 if no time has elapsed.
     * 以字节每秒返回当前上传吞吐量。如果没有经过时间则返回 0。
     *
     * @return throughput in bytes per second - 每秒字节数吞吐量
     */
    public double throughputBytesPerSec() {
        if (elapsedMs <= 0) return 0.0;
        return (double) bytesUploaded / elapsedMs * 1000.0;
    }

    /**
     * Returns the upload percentage as a double (0.0 to 100.0).
     * 以双精度浮点数（0.0 到 100.0）返回上传百分比。
     *
     * @return percentage - 百分比
     */
    public double percentage() {
        if (totalBytes <= 0) return 0.0;
        return (double) bytesUploaded / totalBytes * 100.0;
    }

    @Override
    public String toString() {
        return "UploadProgress{file=" + fileName +
                ", " + bytesUploaded + "/" + totalBytes +
                " (" + formattedPercentage() + ")" +
                ", throughput=" + String.format("%.0f", throughputBytesPerSec()) + " B/s" +
                "}";
    }
}
