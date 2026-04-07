package cloud.opencode.base.test.data;

import java.util.Random;

/**
 * Repeatable Random - Seeded random generator for reproducible tests
 * 可重复随机 - 用于可复现测试的种子随机生成器
 *
 * <p>Provides deterministic random values based on a seed, useful for creating
 * reproducible test data.</p>
 * <p>基于种子提供确定性随机值，用于创建可复现的测试数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deterministic random sequences from seed - 基于种子的确定性随机序列</li>
 *   <li>Reproducible test data generation - 可复现的测试数据生成</li>
 *   <li>Support for int, long, double, boolean, string generation - 支持int、long、double、boolean、字符串生成</li>
 *   <li>Reset capability for re-running sequences - 可重置以重新运行序列</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Same seed = same sequence
 * RepeatableRandom r1 = new RepeatableRandom(12345L);
 * RepeatableRandom r2 = new RepeatableRandom(12345L);
 *
 * r1.nextInt(100) == r2.nextInt(100); // true
 *
 * // For debugging, print the seed
 * RepeatableRandom r = RepeatableRandom.withRandomSeed();
 * System.out.println("Seed: " + r.getSeed());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (uses non-thread-safe java.util.Random) - 线程安全: 否（使用非线程安全的java.util.Random）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class RepeatableRandom {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final long seed;
    private final Random random;

    /**
     * Creates a repeatable random with the specified seed.
     * 使用指定种子创建可重复随机。
     *
     * @param seed the seed | 种子
     */
    public RepeatableRandom(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    /**
     * Creates a repeatable random with a random seed.
     * 使用随机种子创建可重复随机。
     *
     * @return the repeatable random | 可重复随机
     */
    public static RepeatableRandom withRandomSeed() {
        return new RepeatableRandom(System.nanoTime());
    }

    /**
     * Creates a repeatable random with specified seed.
     * 使用指定种子创建可重复随机。
     *
     * @param seed the seed | 种子
     * @return the repeatable random | 可重复随机
     */
    public static RepeatableRandom withSeed(long seed) {
        return new RepeatableRandom(seed);
    }

    /**
     * Gets the seed.
     * 获取种子。
     *
     * @return the seed | 种子
     */
    public long getSeed() {
        return seed;
    }

    // ==================== Integer Generation | 整数生成 ====================

    /**
     * Returns a random int.
     * 返回随机整数。
     *
     * @return random int | 随机整数
     */
    public int nextInt() {
        return random.nextInt();
    }

    /**
     * Returns a random int between 0 (inclusive) and bound (exclusive).
     * 返回0（包含）到边界（不包含）之间的随机整数。
     *
     * @param bound the upper bound | 上界
     * @return random int | 随机整数
     */
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    /**
     * Returns a random int between min (inclusive) and max (inclusive).
     * 返回最小值（包含）到最大值（包含）之间的随机整数。
     *
     * @param min the minimum value | 最小值
     * @param max the maximum value | 最大值
     * @return random int | 随机整数
     */
    public int nextInt(int min, int max) {
        if (min == max) {
            return min;
        }
        // Use long arithmetic to avoid overflow when range spans large int ranges
        long range = (long) max - (long) min + 1;
        if (range <= Integer.MAX_VALUE) {
            return min + random.nextInt((int) range);
        }
        // For very large ranges (e.g., MIN_VALUE to MAX_VALUE), use long modulo
        long r = random.nextLong() >>> 1;
        return (int) (min + (r % range));
    }

    // ==================== Long Generation | 长整数生成 ====================

    /**
     * Returns a random long.
     * 返回随机长整数。
     *
     * @return random long | 随机长整数
     */
    public long nextLong() {
        return random.nextLong();
    }

    /**
     * Returns a random long between 0 and bound.
     * 返回0到边界之间的随机长整数。
     *
     * @param bound the upper bound | 上界
     * @return random long | 随机长整数
     */
    public long nextLong(long bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        // Use unsigned shift to avoid Math.abs(Long.MIN_VALUE) returning negative
        long r = random.nextLong() >>> 1;
        return r % bound;
    }

    // ==================== Double Generation | 双精度生成 ====================

    /**
     * Returns a random double between 0.0 and 1.0.
     * 返回0.0到1.0之间的随机双精度数。
     *
     * @return random double | 随机双精度数
     */
    public double nextDouble() {
        return random.nextDouble();
    }

    /**
     * Returns a random double between min and max.
     * 返回最小值到最大值之间的随机双精度数。
     *
     * @param min the minimum value | 最小值
     * @param max the maximum value | 最大值
     * @return random double | 随机双精度数
     */
    public double nextDouble(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    // ==================== Boolean Generation | 布尔生成 ====================

    /**
     * Returns a random boolean.
     * 返回随机布尔值。
     *
     * @return random boolean | 随机布尔值
     */
    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    /**
     * Returns a random boolean with specified true probability.
     * 返回带指定真概率的随机布尔值。
     *
     * @param trueProbability the probability of true (0.0 - 1.0) | 真的概率
     * @return random boolean | 随机布尔值
     */
    public boolean nextBoolean(double trueProbability) {
        return random.nextDouble() < trueProbability;
    }

    // ==================== String Generation | 字符串生成 ====================

    /**
     * Returns a random string of specified length.
     * 返回指定长度的随机字符串。
     *
     * @param length the length | 长度
     * @return random string | 随机字符串
     */
    public String nextString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Returns a random string of digits.
     * 返回随机数字字符串。
     *
     * @param length the length | 长度
     * @return random digits | 随机数字
     */
    public String nextDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    // ==================== Array Selection | 数组选择 ====================

    /**
     * Returns a random element from the array.
     * 从数组返回随机元素。
     *
     * @param array the array | 数组
     * @param <T>   the element type | 元素类型
     * @return random element | 随机元素
     */
    public <T> T nextElement(T[] array) {
        return array[random.nextInt(array.length)];
    }

    /**
     * Resets the random generator to the initial state.
     * 重置随机生成器到初始状态。
     *
     * @return a new repeatable random with the same seed | 相同种子的新可重复随机
     */
    public RepeatableRandom reset() {
        return new RepeatableRandom(seed);
    }
}
