package cloud.opencode.base.money.format;

import java.math.BigDecimal;

/**
 * Chinese Util
 * 中文大写工具类
 *
 * <p>Utility for converting money amounts to Chinese uppercase.</p>
 * <p>将金额转换为中文大写的工具类。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String result = ChineseUtil.toUpperCase(new BigDecimal("1234.56"));
 * // 壹仟贰佰叁拾肆元伍角陆分
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Convert monetary amounts to Chinese uppercase (e.g. 壹佰元) - 将金额转换为中文大写</li>
 *   <li>Convert integers to Chinese characters - 将整数转换为中文字符</li>
 *   <li>Proper handling of zero, jiao, fen units - 正确处理零、角、分</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No, amount must not be null - 空值安全: 否，金额不可为null</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(d) where d is the number of digits in the amount - 时间复杂度: O(d)，d 为金额的位数</li>
 *   <li>Space complexity: O(d) for result string buffer - 空间复杂度: O(d) 结果字符串缓冲区</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public final class ChineseUtil {

    /**
     * Chinese uppercase numbers | 中文大写数字
     */
    private static final String[] CN_UPPER_NUMBERS = {
        "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"
    };

    /**
     * Chinese uppercase units | 中文大写单位
     */
    private static final String[] CN_UPPER_UNITS = {
        "", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "万"
    };

    /**
     * Yuan | 元
     */
    private static final String CN_YUAN = "元";

    /**
     * Jiao | 角
     */
    private static final String CN_JIAO = "角";

    /**
     * Fen | 分
     */
    private static final String CN_FEN = "分";

    /**
     * Zheng (whole) | 整
     */
    private static final String CN_ZHENG = "整";

    /**
     * Zero | 零
     */
    private static final String CN_ZERO = "零";

    /**
     * Negative | 负
     */
    private static final String CN_NEGATIVE = "负";

    private ChineseUtil() {
        // Utility class
    }

    /**
     * Convert amount to Chinese uppercase
     * 将金额转换为中文大写
     *
     * @param amount the amount | 金额
     * @return the Chinese uppercase string | 中文大写字符串
     */
    public static String toUpperCase(BigDecimal amount) {
        if (amount == null) {
            return "";
        }

        // Handle zero
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return CN_ZERO + CN_YUAN + CN_ZHENG;
        }

        StringBuilder sb = new StringBuilder();

        // Handle negative
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            sb.append(CN_NEGATIVE);
            amount = amount.abs();
        }

        // Extract yuan, jiao, fen
        // Use BigDecimal arithmetic to avoid int/long overflow for large amounts
        BigDecimal integerPart = amount.setScale(0, java.math.RoundingMode.DOWN);
        if (integerPart.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0) {
            throw new ArithmeticException("Amount too large for Chinese conversion: " + amount);
        }
        long yuan = integerPart.longValue();
        BigDecimal fraction = amount.subtract(integerPart).movePointRight(2)
                .setScale(0, java.math.RoundingMode.HALF_UP);
        int fracInt = Math.min(fraction.abs().intValue(), 99);
        int jiao = fracInt / 10;
        int fen = fracInt % 10;

        // Integer part (yuan)
        if (yuan > 0) {
            sb.append(convertInteger(yuan));
            sb.append(CN_YUAN);
        }

        // Decimal part (jiao, fen)
        if (jiao == 0 && fen == 0) {
            sb.append(CN_ZHENG);
        } else {
            if (jiao > 0) {
                sb.append(CN_UPPER_NUMBERS[jiao]).append(CN_JIAO);
            } else if (yuan > 0) {
                sb.append(CN_ZERO);
            }
            if (fen > 0) {
                sb.append(CN_UPPER_NUMBERS[fen]).append(CN_FEN);
            }
        }

        return sb.toString();
    }

    /**
     * Convert integer to Chinese uppercase
     * 将整数转换为中文大写
     *
     * @param num the integer | 整数
     * @return the Chinese uppercase string | 中文大写字符串
     */
    private static String convertInteger(long num) {
        if (num == 0) {
            return CN_ZERO;
        }

        StringBuilder sb = new StringBuilder();
        String numStr = String.valueOf(num);
        int len = numStr.length();

        boolean lastZero = false;

        for (int i = 0; i < len; i++) {
            int digit = numStr.charAt(i) - '0';
            int pos = len - i - 1;

            if (digit != 0) {
                if (lastZero) {
                    sb.append(CN_ZERO);
                }
                sb.append(CN_UPPER_NUMBERS[digit]);
                sb.append(CN_UPPER_UNITS[pos % 13]);
                lastZero = false;
            } else {
                // Handle special positions (万, 亿)
                if (pos == 4 && !endsWithUnit(sb, "万", "亿")) {
                    sb.append("万");
                    lastZero = false;
                } else if (pos == 8) {
                    sb.append("亿");
                    lastZero = false;
                } else {
                    lastZero = true;
                }
            }
        }

        // Clean up trailing units
        return cleanUp(sb.toString());
    }

    /**
     * Check if string ends with any of the given units
     * 检查字符串是否以指定单位结尾
     */
    private static boolean endsWithUnit(StringBuilder sb, String... units) {
        String str = sb.toString();
        for (String unit : units) {
            if (str.endsWith(unit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clean up the result string
     * 清理结果字符串
     */
    private static String cleanUp(String str) {
        // Remove duplicate zeros
        while (str.contains("零零")) {
            str = str.replace("零零", "零");
        }
        // Remove zero before unit
        str = str.replace("零万", "万");
        str = str.replace("零亿", "亿");
        // Remove trailing zero
        if (str.endsWith("零")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * Convert to simplified Chinese number
     * 转换为简体中文数字
     *
     * @param amount the amount | 金额
     * @return the simplified Chinese number | 简体中文数字
     */
    public static String toSimplified(BigDecimal amount) {
        if (amount == null) {
            return "";
        }

        String[] simplifiedNumbers = {"〇", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        String[] simplifiedUnits = {"", "十", "百", "千", "万", "十", "百", "千", "亿"};

        StringBuilder sb = new StringBuilder();
        String numStr = amount.toPlainString();

        // Handle integer part
        int dotIndex = numStr.indexOf('.');
        String intPart = dotIndex == -1 ? numStr : numStr.substring(0, dotIndex);

        for (int i = 0; i < intPart.length(); i++) {
            char c = intPart.charAt(i);
            if (c == '-') {
                sb.append("负");
            } else {
                int digit = c - '0';
                int pos = intPart.length() - i - 1;
                if (digit != 0) {
                    sb.append(simplifiedNumbers[digit]);
                    if (pos < simplifiedUnits.length) {
                        sb.append(simplifiedUnits[pos]);
                    }
                } else if (sb.length() > 0 && !sb.toString().endsWith("〇")) {
                    sb.append("〇");
                }
            }
        }

        // Handle decimal part
        if (dotIndex != -1) {
            sb.append("点");
            String decPart = numStr.substring(dotIndex + 1);
            for (char c : decPart.toCharArray()) {
                sb.append(simplifiedNumbers[c - '0']);
            }
        }

        return sb.toString();
    }
}
