package cloud.opencode.base.hash;

/**
 * Zero-allocation hash code combining utility
 * 零分配哈希码组合工具
 *
 * <p>Provides high-quality hash code combining using MurmurHash3's fmix32
 * mixing function. Unlike {@code Objects.hash()}, this utility avoids
 * varargs array allocation and produces better distribution through
 * proper mixing at each step.</p>
 * <p>使用MurmurHash3的fmix32混合函数提供高质量的哈希码组合。
 * 与 {@code Objects.hash()} 不同，此工具避免了可变参数数组分配，
 * 并通过每一步的正确混合产生更好的分布。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Zero-allocation fixed-arity combine methods (2-8 params) - 零分配固定参数组合方法(2-8个参数)</li>
 *   <li>Chainable Combiner builder for dynamic combining - 可链式调用的Combiner构建器用于动态组合</li>
 *   <li>MurmurHash3 fmix32 mixing for excellent avalanche - MurmurHash3 fmix32混合实现优秀的雪崩效应</li>
 *   <li>Order-sensitive: combine(a,b) != combine(b,a) - 顺序敏感: combine(a,b) != combine(b,a)</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Fixed-arity combining (zero allocation)
 * // 固定参数组合（零分配）
 * int hash = HashCodes.combine(name.hashCode(), age);
 * int hash3 = HashCodes.combine(a.hashCode(), b.hashCode(), c.hashCode());
 *
 * // Chainable builder for dynamic combining
 * // 可链式调用的构建器用于动态组合
 * int hash = HashCodes.start()
 *     .add(name.hashCode())
 *     .add(age)
 *     .add(active)
 *     .add(address)
 *     .result();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility methods) - 线程安全: 是（无状态工具方法）</li>
 *   <li>Combiner instances are NOT thread-safe - Combiner实例非线程安全</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = number of values - O(n), n为值的数量</li>
 *   <li>Space complexity: O(1) - zero heap allocation for combine() - O(1) - combine()零堆分配</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.3
 */
public final class HashCodes {

    /**
     * Initial seed value (golden ratio constant)
     * 初始种子值（黄金比例常数）
     */
    private static final int SEED = 0x9e3779b9;

    private HashCodes() {
        // Utility class, prevent instantiation
        // 工具类，防止实例化
    }

    // ==================== Mixing Function | 混合函数 ====================

    /**
     * MurmurHash3 fmix32 mixing function
     * MurmurHash3 fmix32 混合函数
     *
     * <p>Provides excellent avalanche behavior: a single bit change in the
     * input causes approximately half the output bits to change.</p>
     * <p>提供优秀的雪崩行为：输入中的单个位变化导致大约一半的输出位发生变化。</p>
     *
     * @param h input value | 输入值
     * @return mixed value | 混合后的值
     */
    private static int mix(int h) {
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    // ==================== Fixed-Arity Combine Methods | 固定参数组合方法 ====================

    /**
     * Combines two hash codes into one
     * 将两个哈希码组合为一个
     *
     * @param a first hash code | 第一个哈希码
     * @param b second hash code | 第二个哈希码
     * @return combined hash code | 组合后的哈希码
     */
    public static int combine(int a, int b) {
        return mix(mix(SEED ^ a) ^ b);
    }

    /**
     * Combines three hash codes into one
     * 将三个哈希码组合为一个
     *
     * @param a first hash code | 第一个哈希码
     * @param b second hash code | 第二个哈希码
     * @param c third hash code | 第三个哈希码
     * @return combined hash code | 组合后的哈希码
     */
    public static int combine(int a, int b, int c) {
        return mix(mix(mix(SEED ^ a) ^ b) ^ c);
    }

    /**
     * Combines four hash codes into one
     * 将四个哈希码组合为一个
     *
     * @param a first hash code | 第一个哈希码
     * @param b second hash code | 第二个哈希码
     * @param c third hash code | 第三个哈希码
     * @param d fourth hash code | 第四个哈希码
     * @return combined hash code | 组合后的哈希码
     */
    public static int combine(int a, int b, int c, int d) {
        return mix(mix(mix(mix(SEED ^ a) ^ b) ^ c) ^ d);
    }

    /**
     * Combines five hash codes into one
     * 将五个哈希码组合为一个
     *
     * @param a first hash code | 第一个哈希码
     * @param b second hash code | 第二个哈希码
     * @param c third hash code | 第三个哈希码
     * @param d fourth hash code | 第四个哈希码
     * @param e fifth hash code | 第五个哈希码
     * @return combined hash code | 组合后的哈希码
     */
    public static int combine(int a, int b, int c, int d, int e) {
        return mix(mix(mix(mix(mix(SEED ^ a) ^ b) ^ c) ^ d) ^ e);
    }

    /**
     * Combines six hash codes into one
     * 将六个哈希码组合为一个
     *
     * @param a first hash code | 第一个哈希码
     * @param b second hash code | 第二个哈希码
     * @param c third hash code | 第三个哈希码
     * @param d fourth hash code | 第四个哈希码
     * @param e fifth hash code | 第五个哈希码
     * @param f sixth hash code | 第六个哈希码
     * @return combined hash code | 组合后的哈希码
     */
    public static int combine(int a, int b, int c, int d, int e, int f) {
        return mix(mix(mix(mix(mix(mix(SEED ^ a) ^ b) ^ c) ^ d) ^ e) ^ f);
    }

    /**
     * Combines seven hash codes into one
     * 将七个哈希码组合为一个
     *
     * @param a first hash code | 第一个哈希码
     * @param b second hash code | 第二个哈希码
     * @param c third hash code | 第三个哈希码
     * @param d fourth hash code | 第四个哈希码
     * @param e fifth hash code | 第五个哈希码
     * @param f sixth hash code | 第六个哈希码
     * @param g seventh hash code | 第七个哈希码
     * @return combined hash code | 组合后的哈希码
     */
    public static int combine(int a, int b, int c, int d, int e, int f, int g) {
        return mix(mix(mix(mix(mix(mix(mix(SEED ^ a) ^ b) ^ c) ^ d) ^ e) ^ f) ^ g);
    }

    /**
     * Combines eight hash codes into one
     * 将八个哈希码组合为一个
     *
     * @param a first hash code | 第一个哈希码
     * @param b second hash code | 第二个哈希码
     * @param c third hash code | 第三个哈希码
     * @param d fourth hash code | 第四个哈希码
     * @param e fifth hash code | 第五个哈希码
     * @param f sixth hash code | 第六个哈希码
     * @param g seventh hash code | 第七个哈希码
     * @param h eighth hash code | 第八个哈希码
     * @return combined hash code | 组合后的哈希码
     */
    public static int combine(int a, int b, int c, int d, int e, int f, int g, int h) {
        return mix(mix(mix(mix(mix(mix(mix(mix(SEED ^ a) ^ b) ^ c) ^ d) ^ e) ^ f) ^ g) ^ h);
    }

    // ==================== Combiner Builder | 组合器构建器 ====================

    /**
     * Creates a new Combiner with the default seed
     * 使用默认种子创建新的Combiner
     *
     * @return a new Combiner instance | 新的Combiner实例
     */
    public static Combiner start() {
        return new Combiner(SEED);
    }

    /**
     * Creates a new Combiner with an initial value mixed into the seed
     * 使用混入种子的初始值创建新的Combiner
     *
     * @param initial initial hash value | 初始哈希值
     * @return a new Combiner instance | 新的Combiner实例
     */
    public static Combiner start(int initial) {
        return new Combiner(mix(SEED ^ initial));
    }

    /**
     * Chainable hash code combiner
     * 可链式调用的哈希码组合器
     *
     * <p>Accumulates hash values incrementally and produces a final
     * combined hash code. Each value is mixed in using the fmix32 function.</p>
     * <p>增量累积哈希值并产生最终的组合哈希码。
     * 每个值都使用fmix32函数混合进来。</p>
     *
     * <p><strong>Usage Examples | 使用示例:</strong></p>
     * <pre>{@code
     * int hash = HashCodes.start()
     *     .add(name.hashCode())
     *     .add(42L)
     *     .add(true)
     *     .add(someObject)
     *     .result();
     * }</pre>
     *
     * <p><strong>Security | 安全性:</strong></p>
     * <ul>
     *   <li>Thread-safe: No (mutable state) - 线程安全: 否（可变状态）</li>
     * </ul>
     */
    public static final class Combiner {

        private int hash;

        Combiner(int hash) {
            this.hash = hash;
        }

        /**
         * Adds an int value to the hash combination
         * 向哈希组合中添加int值
         *
         * @param value int value | int值
         * @return this combiner | 此组合器
         */
        public Combiner add(int value) {
            hash = mix(hash ^ value);
            return this;
        }

        /**
         * Adds a long value to the hash combination (splits into two ints)
         * 向哈希组合中添加long值（拆分为两个int）
         *
         * @param value long value | long值
         * @return this combiner | 此组合器
         */
        public Combiner add(long value) {
            hash = mix(hash ^ (int) value);
            hash = mix(hash ^ (int) (value >>> 32));
            return this;
        }

        /**
         * Adds a boolean value to the hash combination
         * 向哈希组合中添加boolean值
         *
         * @param value boolean value | boolean值
         * @return this combiner | 此组合器
         */
        public Combiner add(boolean value) {
            hash = mix(hash ^ (value ? 1 : 0));
            return this;
        }

        /**
         * Adds an object's hash code to the hash combination (0 for null)
         * 向哈希组合中添加对象的哈希码（null为0）
         *
         * @param obj object | 对象
         * @return this combiner | 此组合器
         */
        public Combiner add(Object obj) {
            hash = mix(hash ^ (obj == null ? 0 : obj.hashCode()));
            return this;
        }

        /**
         * Returns the final combined hash code
         * 返回最终的组合哈希码
         *
         * @return combined hash code | 组合后的哈希码
         */
        public int result() {
            return hash;
        }
    }
}
