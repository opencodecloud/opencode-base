package cloud.opencode.base.string.regex;

/**
 * Verification Utility - Provides checksum-based verification algorithms
 * 校验工具类 - 提供基于校验位的验证算法
 *
 * <p>Unlike {@link RegexPattern} which only checks format via regex,
 * this class performs full algorithmic verification including checksum validation.</p>
 * <p>不同于 {@link RegexPattern} 仅通过正则检查格式，
 * 此类执行完整的算法校验，包括校验位验证。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Luhn algorithm (bank cards, IMEI) - Luhn 算法（银行卡、IMEI）</li>
 *   <li>China 18-digit ID card checksum - 中国18位身份证校验位</li>
 *   <li>China 15-digit ID card validation - 中国15位身份证校验</li>
 *   <li>USCI (Unified Social Credit Identifier) checksum - 统一社会信用代码校验位</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean validCard = OpenVerify.isLuhn("4111111111111111");
 * boolean validId = OpenVerify.isValidIdCard18("110101199003077735");
 * boolean validUsci = OpenVerify.isValidUSCI("91110000MA001ABCX5");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns false for null) - 空值安全: 是（null返回false）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenVerify {

    private static final int[] ID_CARD_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] ID_CARD_CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    private static final String USCI_CHARS = "0123456789ABCDEFGHJKLMNPQRTUWXY";
    private static final int[] USCI_WEIGHTS = {1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28};

    private OpenVerify() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Validates a number using the Luhn algorithm (MOD 10).
     * 使用 Luhn 算法（MOD 10）校验数字。
     *
     * <p>Applicable to credit/debit card numbers, IMEI numbers, etc.</p>
     * <p>适用于银行卡号、IMEI 号等。</p>
     *
     * @param digits the digit string to validate
     * @return true if valid according to Luhn algorithm
     */
    public static boolean isLuhn(String digits) {
        if (digits == null || digits.isEmpty()) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            char c = digits.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            int n = c - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    /**
     * Validates a credit/debit card number using the Luhn algorithm.
     * 使用 Luhn 算法校验银行卡号。
     *
     * <p>Strips non-digit characters before validation. Valid card numbers are 13-19 digits.</p>
     * <p>校验前去除非数字字符。有效卡号为 13-19 位。</p>
     *
     * @param cardNumber the card number (may contain spaces/dashes)
     * @return true if valid card number
     */
    public static boolean isCreditCard(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }
        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.length() < 13 || digits.length() > 19) {
            return false;
        }
        return isLuhn(digits);
    }

    /**
     * Validates a China 18-digit ID card number with checksum verification.
     * 校验中国18位身份证号（含校验位验证）。
     *
     * <p>Performs weighted sum modulo 11 checksum calculation.</p>
     * <p>执行加权求和模 11 校验位计算。</p>
     *
     * @param idCard the 18-digit ID card number
     * @return true if valid (format + checksum)
     */
    public static boolean isIdCard18(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return false;
        }
        idCard = idCard.toUpperCase();
        for (int i = 0; i < 17; i++) {
            if (!Character.isDigit(idCard.charAt(i))) {
                return false;
            }
        }
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard.charAt(i) - '0') * ID_CARD_WEIGHTS[i];
        }
        char expectedCheck = ID_CARD_CHECK_CODES[sum % 11];
        return idCard.charAt(17) == expectedCheck;
    }

    /**
     * Validates a China 15-digit ID card number.
     * 校验中国15位身份证号。
     *
     * @param idCard the 15-digit ID card number
     * @return true if valid (all digits, correct length)
     */
    public static boolean isIdCard15(String idCard) {
        if (idCard == null || idCard.length() != 15) {
            return false;
        }
        for (int i = 0; i < idCard.length(); i++) {
            if (!Character.isDigit(idCard.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates a Unified Social Credit Identifier (USCI / 统一社会信用代码).
     * 校验统一社会信用代码。
     *
     * <p>Performs weighted sum modulo 31 checksum calculation
     * using the character set: 0-9, A-H, J-N, P, Q, R, T, U, W, X, Y.</p>
     * <p>使用字符集 0-9, A-H, J-N, P, Q, R, T, U, W, X, Y
     * 执行加权求和模 31 校验位计算。</p>
     *
     * @param usci the 18-character USCI code
     * @return true if valid (format + checksum)
     */
    public static boolean isUSCI(String usci) {
        if (usci == null || usci.length() != 18) {
            return false;
        }
        usci = usci.toUpperCase();
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            int index = USCI_CHARS.indexOf(usci.charAt(i));
            if (index < 0) {
                return false;
            }
            sum += index * USCI_WEIGHTS[i];
        }
        int checkIndex = 31 - (sum % 31);
        if (checkIndex == 31) {
            checkIndex = 0;
        }
        char expectedCheck = USCI_CHARS.charAt(checkIndex);
        return usci.charAt(17) == expectedCheck;
    }
}
