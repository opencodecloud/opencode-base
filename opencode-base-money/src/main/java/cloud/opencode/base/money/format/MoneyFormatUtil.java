package cloud.opencode.base.money.format;

import cloud.opencode.base.money.Currency;
import cloud.opencode.base.money.Money;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Money Format Util
 * 金额格式化工具类
 *
 * <p>Utility for formatting money amounts in various formats.</p>
 * <p>以各种格式格式化金额的工具类。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Money m = Money.of("1234567.89");
 * System.out.println(MoneyFormatUtil.format(m));  // ¥1,234,567.89
 * System.out.println(MoneyFormatUtil.formatWithCode(m));  // CNY 1,234,567.89
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Format money with currency symbol or code - 使用货币符号或代码格式化金额</li>
 *   <li>Thousands separator formatting - 千位分隔符格式化</li>
 *   <li>Configurable decimal scale - 可配置的小数精度</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes, null returns empty string - 空值安全: 是，null返回空字符串</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(d) where d is the number of digits in the amount - 时间复杂度: O(d)，d 为金额的位数</li>
 *   <li>Space complexity: O(d) for formatted string - 空间复杂度: O(d) 格式化字符串</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public final class MoneyFormatUtil {

    private MoneyFormatUtil() {
        // Utility class
    }

    /**
     * Format money with default settings
     * 使用默认设置格式化金额
     *
     * @param money the money | 金额
     * @return the formatted string | 格式化字符串
     */
    public static String format(Money money) {
        if (money == null) {
            return "";
        }
        return money.currency().getSymbol() + formatNumber(money.amount(), money.currency().getScale());
    }

    /**
     * Format money number only
     * 仅格式化金额数字
     *
     * @param money the money | 金额
     * @return the formatted number | 格式化的数字
     */
    public static String formatNumber(Money money) {
        if (money == null) {
            return "";
        }
        return formatNumber(money.amount(), money.currency().getScale());
    }

    /**
     * Format number with scale
     * 按精度格式化数字
     *
     * @param amount the amount | 金额
     * @param scale the decimal scale | 小数位数
     * @return the formatted number | 格式化的数字
     */
    public static String formatNumber(BigDecimal amount, int scale) {
        if (amount == null) {
            return "";
        }
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(scale);
        nf.setMaximumFractionDigits(scale);
        return nf.format(amount);
    }

    /**
     * Format with currency code
     * 带货币代码格式化
     *
     * @param money the money | 金额
     * @return the formatted string | 格式化字符串
     */
    public static String formatWithCode(Money money) {
        if (money == null) {
            return "";
        }
        return money.currency().getCode() + " " + formatNumber(money);
    }

    /**
     * Format with Chinese name
     * 带中文名称格式化
     *
     * @param money the money | 金额
     * @return the formatted string | 格式化字符串
     */
    public static String formatWithNameZh(Money money) {
        if (money == null) {
            return "";
        }
        return money.currency().getNameZh() + " " + formatNumber(money);
    }

    /**
     * Format as accounting style (negative in parentheses)
     * 会计格式（负数用括号）
     *
     * @param money the money | 金额
     * @return the formatted string | 格式化字符串
     */
    public static String formatAccounting(Money money) {
        if (money == null) {
            return "";
        }
        if (money.isNegative()) {
            return money.currency().getSymbol() + "(" + formatNumber(money.abs()) + ")";
        }
        return format(money);
    }

    /**
     * Format with sign (always show + or -)
     * 带符号格式化（总是显示+或-）
     *
     * @param money the money | 金额
     * @return the formatted string | 格式化字符串
     */
    public static String formatWithSign(Money money) {
        if (money == null) {
            return "";
        }
        String sign = money.isPositive() ? "+" : (money.isNegative() ? "-" : "");
        return sign + money.currency().getSymbol() + formatNumber(money.abs());
    }

    /**
     * Format without grouping separator
     * 不带千位分隔符格式化
     *
     * @param money the money | 金额
     * @return the formatted string | 格式化字符串
     */
    public static String formatNoGrouping(Money money) {
        if (money == null) {
            return "";
        }
        return money.currency().getSymbol() + money.amount().setScale(
            money.currency().getScale(), java.math.RoundingMode.HALF_UP
        ).toPlainString();
    }

    /**
     * Format with locale
     * 按地区格式化
     *
     * @param money the money | 金额
     * @param locale the locale | 地区
     * @return the formatted string | 格式化字符串
     */
    public static String format(Money money, Locale locale) {
        if (money == null) {
            return "";
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
        return nf.format(money.amount());
    }

    /**
     * Format with custom pattern
     * 使用自定义模式格式化
     *
     * @param money the money | 金额
     * @param pattern the pattern | 模式
     * @return the formatted string | 格式化字符串
     */
    public static String format(Money money, String pattern) {
        if (money == null) {
            return "";
        }
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(money.amount());
    }

    /**
     * Format for display (compact format for large numbers)
     * 显示格式化（大数字使用紧凑格式）
     *
     * @param money the money | 金额
     * @return the formatted string | 格式化字符串
     */
    public static String formatCompact(Money money) {
        if (money == null) {
            return "";
        }

        BigDecimal amount = money.amount().abs();
        String sign = money.isNegative() ? "-" : "";
        String symbol = money.currency().getSymbol();

        if (amount.compareTo(new BigDecimal("100000000")) >= 0) {
            // 亿
            return sign + symbol + amount.divide(new BigDecimal("100000000"), 2,
                java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "亿";
        } else if (amount.compareTo(new BigDecimal("10000")) >= 0) {
            // 万
            return sign + symbol + amount.divide(new BigDecimal("10000"), 2,
                java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "万";
        }
        return sign + format(money.abs());
    }

    /**
     * Format percentage
     * 格式化百分比
     *
     * @param rate the rate (e.g., 0.05 for 5%) | 比率（如0.05表示5%）
     * @param scale the decimal scale | 小数位数
     * @return the formatted percentage | 格式化的百分比
     */
    public static String formatPercent(BigDecimal rate, int scale) {
        if (rate == null) {
            return "";
        }
        NumberFormat pf = NumberFormat.getPercentInstance();
        pf.setMinimumFractionDigits(scale);
        pf.setMaximumFractionDigits(scale);
        return pf.format(rate);
    }
}
