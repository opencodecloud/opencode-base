package cloud.opencode.base.string.format;

/**
 * Duration Format Utility - Provides duration formatting methods.
 * 时长格式化工具 - 提供时长格式化方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Human-readable duration formatting - 人类可读时长格式化</li>
 *   <li>Time formatting (HH:mm:ss) - 时间格式化</li>
 *   <li>Relative time (e.g., "3 hours ago") - 相对时间</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String duration = OpenDuration.format(3661000); // "1h 1m 1s"
 * String time = OpenDuration.formatTime(3661);    // "01:01:01"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: N/A (primitive parameters) - 空值安全: 不适用（基本类型参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenDuration {
    private OpenDuration() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String format(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    public static String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public static String formatRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        if (diff < 0) return "future";
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = days / 365;
        
        if (years > 0) return years + "年前";
        if (months > 0) return months + "月前";
        if (days > 0) return days + "天前";
        if (hours > 0) return hours + "小时前";
        if (minutes > 0) return minutes + "分钟前";
        return "刚刚";
    }
}
