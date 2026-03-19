package cloud.opencode.base.test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

/**
 * Test Data Generation Entry Class - Provides test data generation capabilities
 * 测试数据生成入口类 - 提供测试数据生成能力
 *
 * <p>Zero-dependency test data generation library with support for
 * random data, fake data, and repeatable random generation.</p>
 * <p>零依赖测试数据生成库，支持随机数据、假数据和可重复随机生成。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Random primitive data - 随机原始数据</li>
 *   <li>Fake personal data (Chinese/English) - 假个人数据（中/英文）</li>
 *   <li>Random strings and UUIDs - 随机字符串和UUID</li>
 *   <li>Random dates and times - 随机日期时间</li>
 *   <li>Repeatable random with seed - 可重复的带种子随机</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Random data
 * int age = OpenData.randomInt(18, 65);
 * String name = OpenData.randomString(10);
 *
 * // Fake data
 * String chineseName = OpenData.chineseName();
 * String email = OpenData.email();
 * String phone = OpenData.phone();
 *
 * // Repeatable random
 * OpenData.withSeed(12345, () -> {
 *     int value = OpenData.randomInt(100); // Always same value with same seed
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Partially (seeded random uses ThreadLocal) - 线程安全: 部分（种子随机使用ThreadLocal）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class OpenData {

    private static final Random RANDOM = new Random();
    private static final ThreadLocal<Random> SEEDED_RANDOM = new ThreadLocal<>();

    // Character sets for random string generation
    private static final String ALPHA_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHA_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMERIC = "0123456789";
    private static final String ALPHA_NUMERIC = ALPHA_LOWER + ALPHA_UPPER + NUMERIC;

    // Chinese data
    private static final String[] CHINESE_SURNAMES = {
            "王", "李", "张", "刘", "陈", "杨", "黄", "赵", "吴", "周",
            "徐", "孙", "马", "朱", "胡", "郭", "何", "林", "罗", "高"
    };
    private static final String[] CHINESE_GIVEN_NAMES = {
            "伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军",
            "洋", "勇", "艳", "杰", "涛", "明", "超", "秀兰", "霞", "平"
    };

    // English data
    private static final String[] ENGLISH_FIRST_NAMES = {
            "James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph",
            "Mary", "Patricia", "Jennifer", "Linda", "Barbara", "Elizabeth", "Susan", "Jessica"
    };
    private static final String[] ENGLISH_LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Anderson", "Taylor", "Thomas", "Jackson", "White", "Harris"
    };

    private static final String[] EMAIL_DOMAINS = {
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "qq.com", "163.com"
    };

    private static final String[] CITIES = {
            "Beijing", "Shanghai", "Guangzhou", "Shenzhen", "Hangzhou", "Chengdu",
            "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Seattle"
    };

    private OpenData() {
    }

    // ==================== Random Primitives | 随机原始类型 ====================

    /**
     * Gets random generator (seeded or default)
     * 获取随机生成器（种子的或默认的）
     */
    private static Random random() {
        Random seeded = SEEDED_RANDOM.get();
        return seeded != null ? seeded : RANDOM;
    }

    /**
     * Generates random int
     * 生成随机整数
     *
     * @return random int | 随机整数
     */
    public static int randomInt() {
        return random().nextInt();
    }

    /**
     * Generates random int up to bound (exclusive)
     * 生成随机整数（不包含上界）
     *
     * @param bound the upper bound | 上界
     * @return random int | 随机整数
     */
    public static int randomInt(int bound) {
        return random().nextInt(bound);
    }

    /**
     * Generates random int in range
     * 生成范围内随机整数
     *
     * @param min the minimum (inclusive) | 最小值（包含）
     * @param max the maximum (inclusive) | 最大值（包含）
     * @return random int | 随机整数
     */
    public static int randomInt(int min, int max) {
        return random().nextInt(min, max + 1);
    }

    /**
     * Generates random long
     * 生成随机长整数
     *
     * @return random long | 随机长整数
     */
    public static long randomLong() {
        return random().nextLong();
    }

    /**
     * Generates random long in range
     * 生成范围内随机长整数
     *
     * @param min the minimum | 最小值
     * @param max the maximum | 最大值
     * @return random long | 随机长整数
     */
    public static long randomLong(long min, long max) {
        return random().nextLong(min, max + 1);
    }

    /**
     * Generates random double (0.0 to 1.0)
     * 生成随机双精度数（0.0到1.0）
     *
     * @return random double | 随机双精度数
     */
    public static double randomDouble() {
        return random().nextDouble();
    }

    /**
     * Generates random double in range
     * 生成范围内随机双精度数
     *
     * @param min the minimum | 最小值
     * @param max the maximum | 最大值
     * @return random double | 随机双精度数
     */
    public static double randomDouble(double min, double max) {
        return random().nextDouble(min, max);
    }

    /**
     * Generates random boolean
     * 生成随机布尔值
     *
     * @return random boolean | 随机布尔值
     */
    public static boolean randomBoolean() {
        return random().nextBoolean();
    }

    // ==================== Random Strings | 随机字符串 ====================

    /**
     * Generates random alphanumeric string
     * 生成随机字母数字字符串
     *
     * @param length the length | 长度
     * @return random string | 随机字符串
     */
    public static String randomString(int length) {
        return randomString(length, ALPHA_NUMERIC);
    }

    /**
     * Generates random string from character set
     * 从字符集生成随机字符串
     *
     * @param length     the length | 长度
     * @param characters the character set | 字符集
     * @return random string | 随机字符串
     */
    public static String randomString(int length, String characters) {
        StringBuilder sb = new StringBuilder(length);
        Random rnd = random();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(rnd.nextInt(characters.length())));
        }
        return sb.toString();
    }

    /**
     * Generates random alphabetic string (letters only)
     * 生成随机字母字符串（仅字母）
     *
     * @param length the length | 长度
     * @return random alphabetic string | 随机字母字符串
     */
    public static String randomAlphabetic(int length) {
        return randomString(length, ALPHA_LOWER + ALPHA_UPPER);
    }

    /**
     * Generates random numeric string (digits only)
     * 生成随机数字字符串（仅数字）
     *
     * @param length the length | 长度
     * @return random numeric string | 随机数字字符串
     */
    public static String randomNumeric(int length) {
        return randomString(length, NUMERIC);
    }

    /**
     * Generates random UUID
     * 生成随机UUID
     *
     * @return random UUID string | 随机UUID字符串
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates random float (0.0 to 1.0)
     * 生成随机浮点数（0.0到1.0）
     *
     * @return random float | 随机浮点数
     */
    public static float randomFloat() {
        return random().nextFloat();
    }

    /**
     * Generates random float in range
     * 生成范围内随机浮点数
     *
     * @param min the minimum | 最小值
     * @param max the maximum | 最大值
     * @return random float | 随机浮点数
     */
    public static float randomFloat(float min, float max) {
        return min + random().nextFloat() * (max - min);
    }

    /**
     * Generates random bytes
     * 生成随机字节数组
     *
     * @param length the length | 长度
     * @return random bytes | 随机字节数组
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        random().nextBytes(bytes);
        return bytes;
    }

    /**
     * Generates random hex string
     * 生成随机十六进制字符串
     *
     * @param length the number of hex characters | 十六进制字符数量
     * @return random hex string | 随机十六进制字符串
     */
    public static String randomHex(int length) {
        return randomString(length, "0123456789abcdef");
    }

    // ==================== Fake Personal Data | 假个人数据 ====================

    /**
     * Generates random Chinese name
     * 生成随机中文姓名
     *
     * @return Chinese name | 中文姓名
     */
    public static String chineseName() {
        Random rnd = random();
        String surname = CHINESE_SURNAMES[rnd.nextInt(CHINESE_SURNAMES.length)];
        String given = CHINESE_GIVEN_NAMES[rnd.nextInt(CHINESE_GIVEN_NAMES.length)];
        return surname + given;
    }

    /**
     * Generates random English name
     * 生成随机英文姓名
     *
     * @return English name | 英文姓名
     */
    public static String englishName() {
        Random rnd = random();
        String first = ENGLISH_FIRST_NAMES[rnd.nextInt(ENGLISH_FIRST_NAMES.length)];
        String last = ENGLISH_LAST_NAMES[rnd.nextInt(ENGLISH_LAST_NAMES.length)];
        return first + " " + last;
    }

    /**
     * Generates random email address
     * 生成随机电子邮件地址
     *
     * @return email address | 电子邮件地址
     */
    public static String email() {
        Random rnd = random();
        String username = randomString(8, ALPHA_LOWER).toLowerCase();
        String domain = EMAIL_DOMAINS[rnd.nextInt(EMAIL_DOMAINS.length)];
        return username + "@" + domain;
    }

    /**
     * Generates random Chinese phone number
     * 生成随机中国手机号
     *
     * @return phone number | 手机号
     */
    public static String phone() {
        String[] prefixes = {"130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
                "150", "151", "152", "153", "155", "156", "157", "158", "159",
                "180", "181", "182", "183", "184", "185", "186", "187", "188", "189"};
        Random rnd = random();
        String prefix = prefixes[rnd.nextInt(prefixes.length)];
        return prefix + randomNumeric(8);
    }

    /**
     * Generates random city name
     * 生成随机城市名
     *
     * @return city name | 城市名
     */
    public static String city() {
        return CITIES[random().nextInt(CITIES.length)];
    }

    /**
     * Generates random age in range
     * 生成范围内随机年龄
     *
     * @param min minimum age | 最小年龄
     * @param max maximum age | 最大年龄
     * @return age | 年龄
     */
    public static int age(int min, int max) {
        return randomInt(min, max);
    }

    // ==================== Random Dates | 随机日期 ====================

    /**
     * Generates random date in past
     * 生成过去的随机日期
     *
     * @param daysBack max days back | 最多回溯天数
     * @return random date | 随机日期
     */
    public static LocalDate pastDate(int daysBack) {
        return LocalDate.now().minusDays(randomInt(1, daysBack));
    }

    /**
     * Generates random date in future
     * 生成未来的随机日期
     *
     * @param daysForward max days forward | 最多向前天数
     * @return random date | 随机日期
     */
    public static LocalDate futureDate(int daysForward) {
        return LocalDate.now().plusDays(randomInt(1, daysForward));
    }

    /**
     * Generates random datetime in past
     * 生成过去的随机日期时间
     *
     * @param hoursBack max hours back | 最多回溯小时数
     * @return random datetime | 随机日期时间
     */
    public static LocalDateTime pastDateTime(int hoursBack) {
        return LocalDateTime.now().minusHours(randomInt(1, hoursBack));
    }

    /**
     * Generates random birthday for age range
     * 为年龄范围生成随机生日
     *
     * @param minAge minimum age | 最小年龄
     * @param maxAge maximum age | 最大年龄
     * @return birthday | 生日
     */
    public static LocalDate birthday(int minAge, int maxAge) {
        int age = randomInt(minAge, maxAge);
        return LocalDate.now().minusYears(age).minusDays(randomInt(0, 364));
    }

    /**
     * Generates random date between start and end (inclusive)
     * 生成开始和结束日期之间的随机日期（包含）
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return random date | 随机日期
     */
    public static LocalDate randomDate(LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        return start.plusDays(randomLong(0, days));
    }

    /**
     * Generates random datetime
     * 生成随机日期时间
     *
     * @return random datetime | 随机日期时间
     */
    public static LocalDateTime randomDateTime() {
        return pastDateTime(24 * 365); // Random within past year
    }

    /**
     * Generates random datetime between start and end
     * 生成开始和结束日期时间之间的随机日期时间
     *
     * @param start the start datetime | 开始日期时间
     * @param end   the end datetime | 结束日期时间
     * @return random datetime | 随机日期时间
     */
    public static LocalDateTime randomDateTime(LocalDateTime start, LocalDateTime end) {
        long seconds = ChronoUnit.SECONDS.between(start, end);
        return start.plusSeconds(randomLong(0, seconds));
    }

    // ==================== Random Money | 随机金额 ====================

    /**
     * Generates random money amount (0.00 to 10000.00)
     * 生成随机金额（0.00到10000.00）
     *
     * @return random money | 随机金额
     */
    public static BigDecimal randomMoney() {
        return randomMoney(0, 10000);
    }

    /**
     * Generates random money amount in range
     * 生成范围内的随机金额
     *
     * @param min minimum amount | 最小金额
     * @param max maximum amount | 最大金额
     * @return random money | 随机金额
     */
    public static BigDecimal randomMoney(double min, double max) {
        double value = randomDouble(min, max);
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Generates random price (formatted as X.99 or X.00)
     * 生成随机价格（格式为X.99或X.00）
     *
     * @param min minimum price | 最小价格
     * @param max maximum price | 最大价格
     * @return random price | 随机价格
     */
    public static BigDecimal randomPrice(int min, int max) {
        int base = randomInt(min, max);
        int cents = randomBoolean() ? 99 : 0;
        return BigDecimal.valueOf(base * 100L + cents, 2);
    }

    // ==================== Collection Utilities | 集合工具 ====================

    /**
     * Picks random element from array
     * 从数组中随机选择元素
     *
     * @param array the array | 数组
     * @param <T>   the element type | 元素类型
     * @return random element | 随机元素
     */
    public static <T> T pick(T[] array) {
        return array[random().nextInt(array.length)];
    }

    /**
     * Picks random element from list
     * 从列表中随机选择元素
     *
     * @param list the list | 列表
     * @param <T>  the element type | 元素类型
     * @return random element | 随机元素
     */
    public static <T> T pick(List<T> list) {
        return list.get(random().nextInt(list.size()));
    }

    /**
     * Picks random element from collection
     * 从集合中随机选择元素
     *
     * @param collection the collection | 集合
     * @param <T>        the element type | 元素类型
     * @return random element | 随机元素
     */
    public static <T> T pick(Collection<T> collection) {
        int index = random().nextInt(collection.size());
        int i = 0;
        for (T element : collection) {
            if (i++ == index) {
                return element;
            }
        }
        throw new IllegalStateException("Should not reach here");
    }

    /**
     * Picks random multiple elements from list
     * 从列表中随机选择多个元素
     *
     * @param list  the list | 列表
     * @param count the number of elements | 元素数量
     * @param <T>   the element type | 元素类型
     * @return random elements | 随机元素列表
     */
    public static <T> List<T> pickMany(List<T> list, int count) {
        if (count >= list.size()) {
            return new ArrayList<>(list);
        }
        List<T> copy = new ArrayList<>(list);
        Collections.shuffle(copy, random());
        return copy.subList(0, count);
    }

    /**
     * Shuffles list randomly
     * 随机打乱列表
     *
     * @param list the list | 列表
     * @param <T>  the element type | 元素类型
     * @return shuffled list | 打乱后的列表
     */
    public static <T> List<T> shuffle(List<T> list) {
        List<T> copy = new ArrayList<>(list);
        Collections.shuffle(copy, random());
        return copy;
    }

    /**
     * Generates list with random data
     * 生成包含随机数据的列表
     *
     * @param count    the count | 数量
     * @param supplier the data supplier | 数据供应者
     * @param <T>      the element type | 元素类型
     * @return list of random data | 随机数据列表
     */
    public static <T> List<T> listOf(int count, Supplier<T> supplier) {
        List<T> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(supplier.get());
        }
        return list;
    }

    // ==================== Repeatable Random | 可重复随机 ====================

    /**
     * Executes with seeded random for reproducibility
     * 使用种子随机执行以实现可重复性
     *
     * @param seed   the random seed | 随机种子
     * @param action the action to execute | 要执行的操作
     */
    public static void withSeed(long seed, Runnable action) {
        SEEDED_RANDOM.set(new Random(seed));
        try {
            action.run();
        } finally {
            SEEDED_RANDOM.remove();
        }
    }

    /**
     * Executes with seeded random and returns result
     * 使用种子随机执行并返回结果
     *
     * @param seed     the random seed | 随机种子
     * @param supplier the supplier | 供应者
     * @param <T>      the result type | 结果类型
     * @return the result | 结果
     */
    public static <T> T withSeed(long seed, Supplier<T> supplier) {
        SEEDED_RANDOM.set(new Random(seed));
        try {
            return supplier.get();
        } finally {
            SEEDED_RANDOM.remove();
        }
    }
}
