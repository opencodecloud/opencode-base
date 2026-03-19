package cloud.opencode.base.test.data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Test Data Generator
 * 测试数据生成器
 *
 * <p>Generates random test data.</p>
 * <p>生成随机测试数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Random test data generation - 随机测试数据生成</li>
 *   <li>Names, emails, dates, numbers - 姓名、邮件、日期、数字</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String name = TestDataGenerator.randomFullName();
 * String email = TestDataGenerator.randomEmail();
 * int num = TestDataGenerator.randomInt(1, 100);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class TestDataGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String[] FIRST_NAMES = {"John", "Jane", "Alice", "Bob", "Charlie", "Diana", "Eva", "Frank"};
    private static final String[] LAST_NAMES = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller"};
    private static final String[] DOMAINS = {"gmail.com", "yahoo.com", "outlook.com", "example.com"};

    private TestDataGenerator() {
        // Utility class
    }

    // === String generators ===

    public static String randomString(int length) {
        Random random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public static String randomAlpha(int length) {
        Random random = ThreadLocalRandom.current();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alpha.charAt(random.nextInt(alpha.length())));
        }
        return sb.toString();
    }

    public static String randomNumeric(int length) {
        Random random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    // === Name generators ===

    public static String randomFirstName() {
        return FIRST_NAMES[ThreadLocalRandom.current().nextInt(FIRST_NAMES.length)];
    }

    public static String randomLastName() {
        return LAST_NAMES[ThreadLocalRandom.current().nextInt(LAST_NAMES.length)];
    }

    public static String randomFullName() {
        return randomFirstName() + " " + randomLastName();
    }

    public static String randomEmail() {
        String domain = DOMAINS[ThreadLocalRandom.current().nextInt(DOMAINS.length)];
        return randomFirstName().toLowerCase() + "." + randomLastName().toLowerCase() + "@" + domain;
    }

    public static String randomPhone() {
        return "1" + randomNumeric(10);
    }

    // === Number generators ===

    public static int randomInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    public static int randomInt(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }

    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public static long randomLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    public static long randomLong(long max) {
        return ThreadLocalRandom.current().nextLong(max);
    }

    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public static double randomDouble(double max) {
        return ThreadLocalRandom.current().nextDouble(max);
    }

    public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    // === Date/Time generators ===

    public static Instant randomInstant() {
        long start = Instant.parse("2020-01-01T00:00:00Z").toEpochMilli();
        long end = Instant.now().toEpochMilli();
        return Instant.ofEpochMilli(ThreadLocalRandom.current().nextLong(start, end));
    }

    public static LocalDate randomDate() {
        long minDay = LocalDate.of(2020, 1, 1).toEpochDay();
        long maxDay = LocalDate.now().toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    public static LocalDateTime randomDateTime() {
        return LocalDateTime.of(
            randomInt(2020, 2025),
            randomInt(1, 13),
            randomInt(1, 29),
            randomInt(0, 24),
            randomInt(0, 60)
        );
    }

    // === Collection generators ===

    public static List<String> randomStrings(int count, int length) {
        return IntStream.range(0, count)
            .mapToObj(i -> randomString(length))
            .toList();
    }

    public static List<Integer> randomInts(int count, int max) {
        return IntStream.range(0, count)
            .map(i -> randomInt(max))
            .boxed()
            .toList();
    }

    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }

    // === Choice methods ===

    @SafeVarargs
    public static <T> T oneOf(T... options) {
        return options[ThreadLocalRandom.current().nextInt(options.length)];
    }

    public static <T> T oneOf(List<T> options) {
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }
}
