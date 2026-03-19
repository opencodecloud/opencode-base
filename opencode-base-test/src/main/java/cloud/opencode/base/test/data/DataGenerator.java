package cloud.opencode.base.test.data;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Data Generator - Factory for creating test data
 * 数据生成器 - 创建测试数据的工厂
 *
 * <p>Provides methods to generate various types of test data.</p>
 * <p>提供生成各种类型测试数据的方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Random string, number, boolean generation - 随机字符串、数字、布尔值生成</li>
 *   <li>Date/time range generation - 日期时间范围生成</li>
 *   <li>Collection and map generation - 集合和映射生成</li>
 *   <li>Automatic record instantiation with random values - 自动用随机值实例化记录</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Generate single values
 * String name = DataGenerator.string(10);
 * int age = DataGenerator.intBetween(18, 65);
 *
 * // Generate lists
 * List<String> names = DataGenerator.list(10, () -> DataGenerator.string(5));
 *
 * // Generate records/beans
 * User user = DataGenerator.record(User.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ThreadLocalRandom) - 线程安全: 是（使用ThreadLocalRandom）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class DataGenerator {

    private static final String ALPHA_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC_CHARS = "0123456789";
    private static final String ALPHANUMERIC_CHARS = ALPHA_CHARS + NUMERIC_CHARS;

    private DataGenerator() {
    }

    // ============ String Generation | 字符串生成 ============

    /**
     * Generates random alphanumeric string.
     * 生成随机字母数字字符串。
     *
     * @param length the length | 长度
     * @return the string | 字符串
     */
    public static String string(int length) {
        return randomString(length, ALPHANUMERIC_CHARS);
    }

    /**
     * Generates random alphabetic string.
     * 生成随机字母字符串。
     *
     * @param length the length | 长度
     * @return the string | 字符串
     */
    public static String alpha(int length) {
        return randomString(length, ALPHA_CHARS);
    }

    /**
     * Generates random numeric string.
     * 生成随机数字字符串。
     *
     * @param length the length | 长度
     * @return the string | 字符串
     */
    public static String numeric(int length) {
        return randomString(length, NUMERIC_CHARS);
    }

    /**
     * Generates random string from character set.
     * 从字符集生成随机字符串。
     *
     * @param length the length | 长度
     * @param chars  the characters to use | 使用的字符
     * @return the string | 字符串
     */
    public static String randomString(int length, String chars) {
        if (length <= 0) return "";
        var random = ThreadLocalRandom.current();
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ============ Number Generation | 数值生成 ============

    /**
     * Generates random int between bounds.
     * 生成边界内的随机整数。
     *
     * @param min minimum (inclusive) | 最小值（包含）
     * @param max maximum (exclusive) | 最大值（不包含）
     * @return the int | 整数
     */
    public static int intBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    /**
     * Generates random long between bounds.
     * 生成边界内的随机长整数。
     *
     * @param min minimum (inclusive) | 最小值（包含）
     * @param max maximum (exclusive) | 最大值（不包含）
     * @return the long | 长整数
     */
    public static long longBetween(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max);
    }

    /**
     * Generates random double between bounds.
     * 生成边界内的随机双精度数。
     *
     * @param min minimum (inclusive) | 最小值（包含）
     * @param max maximum (exclusive) | 最大值（不包含）
     * @return the double | 双精度数
     */
    public static double doubleBetween(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Generates random BigDecimal for monetary values.
     * 生成用于货币值的随机BigDecimal。
     *
     * @param min   minimum | 最小值
     * @param max   maximum | 最大值
     * @param scale decimal places | 小数位数
     * @return the BigDecimal | BigDecimal值
     */
    public static BigDecimal decimal(double min, double max, int scale) {
        double value = doubleBetween(min, max);
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * Generates random boolean.
     * 生成随机布尔值。
     *
     * @return the boolean | 布尔值
     */
    public static boolean bool() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * Generates random boolean with probability.
     * 按概率生成随机布尔值。
     *
     * @param trueProbability probability of true (0.0 to 1.0) | true的概率
     * @return the boolean | 布尔值
     */
    public static boolean bool(double trueProbability) {
        return ThreadLocalRandom.current().nextDouble() < trueProbability;
    }

    // ============ Date/Time Generation | 日期时间生成 ============

    /**
     * Generates random LocalDate between bounds.
     * 生成边界内的随机日期。
     *
     * @param start start date | 开始日期
     * @param end   end date | 结束日期
     * @return the date | 日期
     */
    public static LocalDate dateBetween(LocalDate start, LocalDate end) {
        long startDay = start.toEpochDay();
        long endDay = end.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(startDay, endDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    /**
     * Generates random LocalDateTime between bounds.
     * 生成边界内的随机日期时间。
     *
     * @param start start datetime | 开始日期时间
     * @param end   end datetime | 结束日期时间
     * @return the datetime | 日期时间
     */
    public static LocalDateTime dateTimeBetween(LocalDateTime start, LocalDateTime end) {
        long startNanos = start.toLocalDate().toEpochDay() * 86400_000_000_000L
            + start.toLocalTime().toNanoOfDay();
        long endNanos = end.toLocalDate().toEpochDay() * 86400_000_000_000L
            + end.toLocalTime().toNanoOfDay();
        long randomNanos = ThreadLocalRandom.current().nextLong(startNanos, endNanos);
        long days = randomNanos / 86400_000_000_000L;
        long nanoOfDay = randomNanos % 86400_000_000_000L;
        return LocalDateTime.of(LocalDate.ofEpochDay(days), LocalTime.ofNanoOfDay(nanoOfDay));
    }

    /**
     * Generates random Instant between bounds.
     * 生成边界内的随机时刻。
     *
     * @param start start instant | 开始时刻
     * @param end   end instant | 结束时刻
     * @return the instant | 时刻
     */
    public static Instant instantBetween(Instant start, Instant end) {
        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        long randomMillis = ThreadLocalRandom.current().nextLong(startMillis, endMillis);
        return Instant.ofEpochMilli(randomMillis);
    }

    // ============ Collection Generation | 集合生成 ============

    /**
     * Generates list of items.
     * 生成项目列表。
     *
     * @param size     the size | 大小
     * @param supplier the item supplier | 项目供应器
     * @param <T>      the item type | 项目类型
     * @return the list | 列表
     */
    public static <T> List<T> list(int size, Supplier<T> supplier) {
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(supplier.get());
        }
        return list;
    }

    /**
     * Generates map with keys and values.
     * 生成带键值的映射。
     *
     * @param size          the size | 大小
     * @param keySupplier   the key supplier | 键供应器
     * @param valueSupplier the value supplier | 值供应器
     * @param <K>           the key type | 键类型
     * @param <V>           the value type | 值类型
     * @return the map | 映射
     */
    public static <K, V> Map<K, V> map(int size, Supplier<K> keySupplier, Supplier<V> valueSupplier) {
        Map<K, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(keySupplier.get(), valueSupplier.get());
        }
        return map;
    }

    /**
     * Generates array of bytes.
     * 生成字节数组。
     *
     * @param length the length | 长度
     * @return the bytes | 字节数组
     */
    public static byte[] bytes(int length) {
        byte[] bytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }

    // ============ Selection | 选择 ============

    /**
     * Selects random element from array.
     * 从数组中随机选择元素。
     *
     * @param elements the elements | 元素
     * @param <T>      the element type | 元素类型
     * @return the selected element | 选中的元素
     */
    @SafeVarargs
    public static <T> T oneOf(T... elements) {
        Objects.requireNonNull(elements, "elements cannot be null");
        if (elements.length == 0) {
            throw new IllegalArgumentException("elements cannot be empty");
        }
        return elements[ThreadLocalRandom.current().nextInt(elements.length)];
    }

    /**
     * Selects random element from list.
     * 从列表中随机选择元素。
     *
     * @param elements the elements | 元素
     * @param <T>      the element type | 元素类型
     * @return the selected element | 选中的元素
     */
    public static <T> T oneOf(List<T> elements) {
        Objects.requireNonNull(elements, "elements cannot be null");
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("elements cannot be empty");
        }
        return elements.get(ThreadLocalRandom.current().nextInt(elements.size()));
    }

    // ============ Record/Bean Generation | 记录/Bean生成 ============

    /**
     * Generates random record with default values.
     * 使用默认值生成随机记录。
     *
     * @param recordClass the record class | 记录类
     * @param <T>         the record type | 记录类型
     * @return the record | 记录
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record> T record(Class<T> recordClass) {
        Objects.requireNonNull(recordClass, "recordClass cannot be null");
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException("Class must be a record: " + recordClass.getName());
        }

        try {
            RecordComponent[] components = recordClass.getRecordComponents();
            Class<?>[] types = new Class<?>[components.length];
            Object[] args = new Object[components.length];

            for (int i = 0; i < components.length; i++) {
                types[i] = components[i].getType();
                args[i] = generateDefaultValue(types[i]);
            }

            return recordClass.getDeclaredConstructor(types).newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create record: " + recordClass.getName(), e);
        }
    }

    private static Object generateDefaultValue(Class<?> type) {
        if (type == String.class) return string(10);
        if (type == int.class || type == Integer.class) return intBetween(1, 1000);
        if (type == long.class || type == Long.class) return longBetween(1L, 10000L);
        if (type == double.class || type == Double.class) return doubleBetween(0.0, 100.0);
        if (type == float.class || type == Float.class) return (float) doubleBetween(0.0, 100.0);
        if (type == boolean.class || type == Boolean.class) return bool();
        if (type == byte.class || type == Byte.class) return (byte) intBetween(0, 128);
        if (type == short.class || type == Short.class) return (short) intBetween(0, 1000);
        if (type == char.class || type == Character.class) return alpha(1).charAt(0);
        if (type == LocalDate.class) return LocalDate.now();
        if (type == LocalDateTime.class) return LocalDateTime.now();
        if (type == Instant.class) return Instant.now();
        if (type == BigDecimal.class) return decimal(0.0, 1000.0, 2);
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants.length > 0 ? constants[0] : null;
        }
        return null;
    }
}
