package cloud.opencode.base.string.format;

/**
 * File Size Format Utility - Provides file size formatting methods.
 * 文件大小格式化工具 - 提供文件大小格式化方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bytes to human-readable format (B, KB, MB, GB, TB, PB) - 字节到可读格式</li>
 *   <li>Human-readable format to bytes parsing - 可读格式到字节解析</li>
 *   <li>Configurable decimal scale - 可配置小数位数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String size = OpenFileSize.format(1536);        // "1.50 KB"
 * long bytes = OpenFileSize.parse("2.5 MB");      // 2621440
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (parse returns 0 for null) - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenFileSize {
    private OpenFileSize() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB", "PB"};

    public static String format(long bytes) {
        return format(bytes, 2);
    }

    public static String format(long bytes, int scale) {
        if (bytes < 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < UNITS.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        String format = "%." + scale + "f %s";
        return String.format(format, size, UNITS[unitIndex]);
    }

    public static long parse(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) return 0;

        sizeStr = sizeStr.trim().toUpperCase();
        long multiplier = 1;

        for (int i = UNITS.length - 1; i >= 0; i--) {
            if (sizeStr.endsWith(UNITS[i])) {
                // Use integer shift instead of Math.pow to avoid double precision loss
                multiplier = 1L << (10 * i);
                sizeStr = sizeStr.substring(0, sizeStr.length() - UNITS[i].length()).trim();
                break;
            }
        }

        try {
            double value = Double.parseDouble(sizeStr);
            return (long) (value * multiplier);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
