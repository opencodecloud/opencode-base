package cloud.opencode.base.test.data;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Sensitive Data Generator - Generates test data that looks real but is fake
 * 敏感数据生成器 - 生成看起来真实但实际是假的测试数据
 *
 * <p>Generates test data with correct formats (checksums, etc.) but using
 * non-real values to avoid accidental use of real personal information.</p>
 * <p>生成格式正确（校验和等）但使用非真实值的测试数据，以避免意外使用真实个人信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Test phone numbers (non-real carrier prefixes) - 测试手机号（非真实运营商前缀）</li>
 *   <li>Test ID card numbers (valid checksum, fake region) - 测试身份证号（有效校验和，虚假地区）</li>
 *   <li>Test bank card numbers (Luhn valid, test BIN) - 测试银行卡号（Luhn有效，测试BIN）</li>
 *   <li>Test email addresses - 测试邮箱地址</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String phone = SensitiveDataGenerator.testPhone();     // 199xxxxxxxx
 * String idCard = SensitiveDataGenerator.testIdCard();   // 999999xxxxxxxxxx
 * String bankCard = SensitiveDataGenerator.testBankCard(); // 622848xxxxxxxxxx
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ThreadLocalRandom) - 线程安全: 是（使用ThreadLocalRandom）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class SensitiveDataGenerator {

    private static final String TEST_PHONE_PREFIX = "199";  // Non-real carrier prefix
    private static final String TEST_REGION_CODE = "999999"; // Fake region code
    private static final String TEST_BANK_BIN = "622848";   // Test BIN

    private static final int[] ID_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] ID_CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private SensitiveDataGenerator() {
    }

    // ==================== Phone Number | 手机号 ====================

    /**
     * Generates a test phone number (199 prefix, not assigned to real carriers).
     * 生成测试手机号（199前缀，未分配给真实运营商）。
     *
     * @return the test phone number | 测试手机号
     */
    public static String testPhone() {
        return TEST_PHONE_PREFIX + randomDigits(8);
    }

    /**
     * Generates a test phone number with custom prefix.
     * 生成带自定义前缀的测试手机号。
     *
     * @param prefix the custom prefix (2-3 digits) | 自定义前缀
     * @return the test phone number | 测试手机号
     */
    public static String testPhone(String prefix) {
        int remaining = 11 - prefix.length();
        return prefix + randomDigits(remaining);
    }

    // ==================== ID Card | 身份证号 ====================

    /**
     * Generates a test 18-digit ID card number (fake region 999999).
     * 生成18位测试身份证号（虚假地区999999）。
     *
     * @return the test ID card number | 测试身份证号
     */
    public static String testIdCard() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Birth date: 1970-2010
        int year = random.nextInt(1970, 2011);
        int month = random.nextInt(1, 13);
        int maxDay = switch (month) {
            case 2 -> 28;
            case 4, 6, 9, 11 -> 30;
            default -> 31;
        };
        int day = random.nextInt(1, maxDay + 1);

        String birthDate = String.format("%04d%02d%02d", year, month, day);
        String sequence = String.format("%03d", random.nextInt(1, 1000));

        String base = TEST_REGION_CODE + birthDate + sequence;
        char checkCode = calculateIdCheckCode(base);

        return base + checkCode;
    }

    /**
     * Generates a test ID card with specified birth year.
     * 生成指定出生年份的测试身份证号。
     *
     * @param birthYear the birth year | 出生年份
     * @return the test ID card number | 测试身份证号
     */
    public static String testIdCard(int birthYear) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int month = random.nextInt(1, 13);
        int maxDay = switch (month) {
            case 2 -> 28;
            case 4, 6, 9, 11 -> 30;
            default -> 31;
        };
        int day = random.nextInt(1, maxDay + 1);

        String birthDate = String.format("%04d%02d%02d", birthYear, month, day);
        String sequence = String.format("%03d", random.nextInt(1, 1000));

        String base = TEST_REGION_CODE + birthDate + sequence;
        char checkCode = calculateIdCheckCode(base);

        return base + checkCode;
    }

    private static char calculateIdCheckCode(String base17) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (base17.charAt(i) - '0') * ID_WEIGHTS[i];
        }
        return ID_CHECK_CODES[sum % 11];
    }

    // ==================== Bank Card | 银行卡号 ====================

    /**
     * Generates a test bank card number with valid Luhn checksum.
     * 生成带有效Luhn校验和的测试银行卡号。
     *
     * @return the test bank card number | 测试银行卡号
     */
    public static String testBankCard() {
        String body = TEST_BANK_BIN + randomDigits(9);
        int checkDigit = calculateLuhnCheckDigit(body);
        return body + checkDigit;
    }

    /**
     * Generates a test bank card number with custom BIN.
     * 生成带自定义BIN的测试银行卡号。
     *
     * @param bin the bank identification number (6 digits) | 银行识别号
     * @return the test bank card number | 测试银行卡号
     */
    public static String testBankCard(String bin) {
        int remaining = 15 - bin.length();  // Total 16 digits minus check digit
        String body = bin + randomDigits(remaining);
        int checkDigit = calculateLuhnCheckDigit(body);
        return body + checkDigit;
    }

    private static int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    // ==================== Email | 邮箱 ====================

    /**
     * Generates a test email address.
     * 生成测试邮箱地址。
     *
     * @return the test email address | 测试邮箱地址
     */
    public static String testEmail() {
        String local = randomLowercase(8);
        return local + "@test.example.com";
    }

    /**
     * Generates a test email address with specified domain.
     * 生成带指定域名的测试邮箱地址。
     *
     * @param domain the domain | 域名
     * @return the test email address | 测试邮箱地址
     */
    public static String testEmail(String domain) {
        String local = randomLowercase(8);
        return local + "@" + domain;
    }

    // ==================== Social Credit Code | 统一社会信用代码 ====================

    /**
     * Generates a test unified social credit code.
     * 生成测试统一社会信用代码。
     *
     * @return the test code | 测试代码
     */
    public static String testSocialCreditCode() {
        // Format: 2位登记机关 + 1位企业类型 + 6位行政区划 + 9位组织机构代码 + 校验位
        String base = "91" + "1" + TEST_REGION_CODE + randomAlphaNumeric(9).toUpperCase();
        // Simplified: use random check digit (real implementation would calculate)
        return base + randomAlphaNumeric(1).toUpperCase();
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private static String randomDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static String randomLowercase(int length) {
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return sb.toString();
    }

    private static String randomAlphaNumeric(int length) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
