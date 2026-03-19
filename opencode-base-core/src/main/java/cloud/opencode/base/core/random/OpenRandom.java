package cloud.opencode.base.core.random;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random Utility Class - Comprehensive random generation utilities
 * 随机工具类 - 全面的随机生成工具
 *
 * <p>Provides random number, string, UUID generation with ThreadLocalRandom and SecureRandom.</p>
 * <p>提供基于 ThreadLocalRandom 和 SecureRandom 的随机数、字符串、UUID 生成。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Random numbers (int, long, double, boolean) - 随机数生成</li>
 *   <li>Random strings (alphanumeric, numeric, alphabetic) - 随机字符串</li>
 *   <li>Secure random (cryptographically strong) - 安全随机数</li>
 *   <li>UUID generation (standard and simple) - UUID 生成</li>
 *   <li>Collection random (element selection, shuffle) - 集合随机操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * int num = OpenRandom.randomInt(100);
 * String code = OpenRandom.randomAlphanumeric(8);
 * String uuid = OpenRandom.simpleUUID();
 * String element = OpenRandom.randomElement(list);
 * OpenRandom.shuffle(array);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ThreadLocalRandom) - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenRandom {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String NUMERIC = "0123456789";
    private static final String ALPHABETIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String ALPHABETIC_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHABETIC_LOWER = "abcdefghijklmnopqrstuvwxyz";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private OpenRandom() {
    }

    // ==================== 基本随机数 ====================

    /**
     * Generates a random integer [0, bound)
     * 生成随机整数 [0, bound)
     */
    public static int randomInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    /**
     * Generates a random integer [origin, bound)
     * 生成随机整数 [origin, bound)
     */
    public static int randomInt(int origin, int bound) {
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }

    /**
     * Generates a random long [0, bound)
     * 生成随机长整数 [0, bound)
     */
    public static long randomLong(long bound) {
        return ThreadLocalRandom.current().nextLong(bound);
    }

    /**
     * Generates a random long [origin, bound)
     * 生成随机长整数 [origin, bound)
     */
    public static long randomLong(long origin, long bound) {
        return ThreadLocalRandom.current().nextLong(origin, bound);
    }

    /**
     * Generates a random double [0, 1)
     * 生成随机浮点数 [0, 1)
     */
    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * Generates a random double [0, bound)
     * 生成随机浮点数 [0, bound)
     */
    public static double randomDouble(double bound) {
        return ThreadLocalRandom.current().nextDouble(bound);
    }

    /**
     * Generates a random double [origin, bound)
     * 生成随机浮点数 [origin, bound)
     */
    public static double randomDouble(double origin, double bound) {
        return ThreadLocalRandom.current().nextDouble(origin, bound);
    }

    /**
     * Generates a random boolean
     * 生成随机布尔值
     */
    public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * Generates a random byte array
     * 生成随机字节数组
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }

    // ==================== 安全随机 ====================

    /**
     * Generates a secure random byte array
     * 生成安全随机字节数组
     */
    public static byte[] secureBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    /**
     * Generates a secure random integer
     * 生成安全随机整数
     */
    public static int secureInt(int bound) {
        return SECURE_RANDOM.nextInt(bound);
    }

    /**
     * Generates a secure random long
     * 生成安全随机长整数
     */
    public static long secureLong() {
        return SECURE_RANDOM.nextLong();
    }

    // ==================== 随机字符串 ====================

    /**
     * Generates a random alphanumeric string
     * 生成随机字母数字字符串
     */
    public static String randomAlphanumeric(int length) {
        return randomString(length, ALPHANUMERIC);
    }

    /**
     * Generates a secure random alphanumeric string
     * 生成安全随机字母数字字符串
     */
    public static String secureAlphanumeric(int length) {
        return secureString(length, ALPHANUMERIC);
    }

    /**
     * Generates a random numeric string
     * 生成随机数字字符串
     */
    public static String randomNumeric(int length) {
        return randomString(length, NUMERIC);
    }

    /**
     * Generates a random alphabetic string
     * 生成随机字母字符串
     */
    public static String randomAlphabetic(int length) {
        return randomString(length, ALPHABETIC);
    }

    /**
     * Generates a random uppercase string
     * 生成随机大写字母字符串
     */
    public static String randomUpperCase(int length) {
        return randomString(length, ALPHABETIC_UPPER);
    }

    /**
     * Generates a random lowercase string
     * 生成随机小写字母字符串
     */
    public static String randomLowerCase(int length) {
        return randomString(length, ALPHABETIC_LOWER);
    }

    /**
     * Generates a random string (custom character set)
     * 生成随机字符串（自定义字符集）
     */
    public static String randomString(int length, String chars) {
        if (length <= 0 || chars == null || chars.isEmpty()) {
            return "";
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            result[i] = chars.charAt(random.nextInt(chars.length()));
        }
        return new String(result);
    }

    /**
     * Generates a secure random string (custom character set)
     * 生成安全随机字符串（自定义字符集）
     */
    public static String secureString(int length, String chars) {
        if (length <= 0 || chars == null || chars.isEmpty()) {
            return "";
        }
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            result[i] = chars.charAt(SECURE_RANDOM.nextInt(chars.length()));
        }
        return new String(result);
    }

    // ==================== UUID ====================

    /**
     * Generates a UUID
     * 生成 UUID
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a simple UUID (without hyphens)
     * 生成简单 UUID（无横线）
     */
    public static String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generates a secure UUID (based on SecureRandom)
     * 生成安全 UUID（基于 SecureRandom）
     */
    public static String secureUUID() {
        byte[] randomBytes = secureBytes(16);
        randomBytes[6] &= 0x0f;
        randomBytes[6] |= 0x40;
        randomBytes[8] &= 0x3f;
        randomBytes[8] |= 0x80;
        return formatUUID(randomBytes);
    }

    // ==================== 集合随机 ====================

    /**
     * Selects a random element from a list
     * 从列表随机选择元素
     */
    public static <T> T randomElement(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(randomInt(list.size()));
    }

    /**
     * Selects a random element from an array
     * 从数组随机选择元素
     */
    @SafeVarargs
    public static <T> T randomElement(T... array) {
        if (array == null || array.length == 0) return null;
        return array[randomInt(array.length)];
    }

    /**
     * Selects multiple random elements from a collection
     * 从集合随机选择多个元素
     */
    public static <T> List<T> randomElements(List<T> list, int count) {
        if (list == null || list.isEmpty() || count <= 0) {
            return new ArrayList<>();
        }
        if (count >= list.size()) {
            return new ArrayList<>(list);
        }
        List<T> copy = new ArrayList<>(list);
        shuffle(copy);
        return copy.subList(0, count);
    }

    /**
     * Shuffles a list
     * 打乱列表顺序
     */
    public static <T> void shuffle(List<T> list) {
        Collections.shuffle(list, ThreadLocalRandom.current());
    }

    /**
     * Shuffles an array
     * 打乱数组顺序
     */
    public static <T> void shuffle(T[] array) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    // ==================== 日期随机 ====================

    /**
     * Generates a random date [startYear, endYear)
     * 生成随机日期 [startYear, endYear)
     */
    public static LocalDate randomDate(int startYear, int endYear) {
        long startEpochDay = LocalDate.of(startYear, 1, 1).toEpochDay();
        long endEpochDay = LocalDate.of(endYear, 1, 1).toEpochDay();
        long randomDay = randomLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    /**
     * Generates a random date (specified range)
     * 生成随机日期（指定范围）
     */
    public static LocalDate randomDate(LocalDate start, LocalDate end) {
        long randomDay = randomLong(start.toEpochDay(), end.toEpochDay());
        return LocalDate.ofEpochDay(randomDay);
    }

    // ==================== 辅助方法 ====================

    private static String formatUUID(byte[] bytes) {
        StringBuilder sb = new StringBuilder(36);
        for (int i = 0; i < 16; i++) {
            sb.append(HEX.toHexDigits(bytes[i]));
            if (i == 3 || i == 5 || i == 7 || i == 9) {
                sb.append('-');
            }
        }
        return sb.toString();
    }
}
