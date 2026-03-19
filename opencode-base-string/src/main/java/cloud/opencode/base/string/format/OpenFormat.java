package cloud.opencode.base.string.format;

import java.math.BigDecimal;

/**
 * String Format Facade - Unified entry point for string formatting operations.
 * 字符串格式化门面 - 字符串格式化操作的统一入口。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Number formatting (decimal, percent, currency) - 数字格式化</li>
 *   <li>Chinese number and money formatting - 中文数字和金额格式化</li>
 *   <li>File size formatting - 文件大小格式化</li>
 *   <li>Duration formatting - 时长格式化</li>
 *   <li>Mobile/ID card/bank card formatting - 手机号/身份证/银行卡格式化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String num = OpenFormat.formatNumber(1234567.89);    // "1,234,567.89"
 * String pct = OpenFormat.formatPercent(0.8567);       // "85.67%"
 * String size = OpenFormat.formatFileSize(1048576);    // "1.00 MB"
 * String dur = OpenFormat.formatDuration(90000);       // "1m 30s"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenFormat {
    private OpenFormat() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // Number formatting
    public static String formatNumber(Number number) { return OpenNumberFormat.formatNumber(number); }
    public static String formatNumber(Number number, int scale) { return OpenNumberFormat.formatNumber(number, scale); }
    public static String formatPercent(double number) { return OpenNumberFormat.formatPercent(number); }
    public static String formatPercent(double number, int scale) { return OpenNumberFormat.formatPercent(number, scale); }
    public static String formatCurrency(BigDecimal amount) { return OpenNumberFormat.formatCurrency(amount); }
    public static String formatCurrency(BigDecimal amount, String symbol) { return OpenNumberFormat.formatCurrency(amount, symbol); }
    public static String toChineseNumber(long number) { return OpenNumberFormat.toChineseNumber(number); }
    public static String toChineseMoney(BigDecimal amount) { return OpenNumberFormat.toChineseMoney(amount); }

    // File size formatting
    public static String formatFileSize(long bytes) { return OpenFileSize.format(bytes); }
    public static String formatFileSize(long bytes, int scale) { return OpenFileSize.format(bytes, scale); }
    public static long parseFileSize(String sizeStr) { return OpenFileSize.parse(sizeStr); }

    // Duration formatting
    public static String formatDuration(long millis) { return OpenDuration.format(millis); }
    public static String formatTime(long seconds) { return OpenDuration.formatTime(seconds); }
    public static String formatRelativeTime(long timestamp) { return OpenDuration.formatRelativeTime(timestamp); }

    // Simple formatting
    public static String formatMobile(String mobile) {
        if (mobile == null || mobile.length() != 11) return mobile;
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    public static String formatIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) return idCard;
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }

    public static String formatBankCard(String cardNo) {
        if (cardNo == null || cardNo.length() < 16) return cardNo;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cardNo.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(' ');
            sb.append(cardNo.charAt(i));
        }
        return sb.toString();
    }
}
