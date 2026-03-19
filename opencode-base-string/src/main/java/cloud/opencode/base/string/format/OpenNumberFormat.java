package cloud.opencode.base.string.format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Number Format Utility - Provides number formatting methods.
 * 数字格式化工具 - 提供数字格式化方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Decimal number formatting with grouping - 分组小数格式化</li>
 *   <li>Percentage formatting - 百分比格式化</li>
 *   <li>Currency formatting - 货币格式化</li>
 *   <li>Chinese number conversion - 中文数字转换</li>
 *   <li>Chinese money conversion - 中文金额转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String num = OpenNumberFormat.formatNumber(1234.5, 2);  // "1,234.50"
 * String pct = OpenNumberFormat.formatPercent(0.85);      // "85.00%"
 * String cn = OpenNumberFormat.toChineseNumber(123);      // "壹佰贰拾叁"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility, new DecimalFormat per call) - 线程安全: 是（无状态，每次调用创建新DecimalFormat）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenNumberFormat {
    private OpenNumberFormat() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String formatNumber(Number number) {
        return formatNumber(number, 2);
    }

    public static String formatNumber(Number number, int scale) {
        if (number == null) return "0";
        DecimalFormat df = new DecimalFormat("#,##0." + "0".repeat(scale));
        return df.format(number);
    }

    public static String formatPercent(double number) {
        return formatPercent(number, 2);
    }

    public static String formatPercent(double number, int scale) {
        BigDecimal percent = BigDecimal.valueOf(number * 100);
        percent = percent.setScale(scale, RoundingMode.HALF_UP);
        return percent.toString() + "%";
    }

    public static String formatCurrency(BigDecimal amount) {
        return formatCurrency(amount, "¥");
    }

    public static String formatCurrency(BigDecimal amount, String symbol) {
        if (amount == null) return symbol + "0.00";
        DecimalFormat df = new DecimalFormat(symbol + "#,##0.00");
        return df.format(amount);
    }

    private static final String[] CN_NUMBERS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    private static final String[] CN_UNITS = {"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿"};

    public static String toChineseNumber(long number) {
        if (number == 0) return CN_NUMBERS[0];
        
        StringBuilder result = new StringBuilder();
        String numStr = String.valueOf(number);
        int len = numStr.length();
        
        for (int i = 0; i < len; i++) {
            int digit = numStr.charAt(i) - '0';
            int unitIndex = len - i - 1;
            
            if (digit != 0) {
                result.append(CN_NUMBERS[digit]);
                if (unitIndex < CN_UNITS.length) {
                    result.append(CN_UNITS[unitIndex]);
                }
            } else if (i > 0 && numStr.charAt(i - 1) != '0') {
                result.append(CN_NUMBERS[0]);
            }
        }
        
        return result.toString();
    }

    public static String toChineseMoney(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "零元整";
        }

        // Use abs for fractional extraction; handle negative prefix separately
        BigDecimal abs = amount.abs();
        long yuan = abs.setScale(0, RoundingMode.DOWN).longValue();
        // Extract jiao and fen using BigDecimal arithmetic to avoid int overflow
        BigDecimal fraction = abs.subtract(BigDecimal.valueOf(yuan)).movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP);
        int fracInt = fraction.abs().intValue();
        int jiao = fracInt / 10;
        int fen = fracInt % 10;

        StringBuilder result = new StringBuilder();
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            result.append("负");
        }
        result.append(toChineseNumber(yuan)).append("元");

        if (jiao > 0) {
            result.append(CN_NUMBERS[jiao]).append("角");
        }
        if (fen > 0) {
            result.append(CN_NUMBERS[fen]).append("分");
        }
        if (jiao == 0 && fen == 0) {
            result.append("整");
        }

        return result.toString();
    }
}
