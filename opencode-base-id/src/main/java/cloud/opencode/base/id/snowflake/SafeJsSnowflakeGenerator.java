package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.IdGenerator;
import cloud.opencode.base.id.exception.OpenIdGenerationException;

import java.util.concurrent.locks.ReentrantLock;

/**
 * JavaScript-Safe Snowflake ID Generator - IDs guaranteed to fit in JS Number.MAX_SAFE_INTEGER
 * JavaScript安全雪花ID生成器 - ID保证在JS Number.MAX_SAFE_INTEGER范围内
 *
 * <p>Generates time-ordered 64-bit IDs whose value is always ≤ 2^53−1 (9,007,199,254,740,991),
 * the maximum integer that JavaScript can represent exactly as a {@code Number}. This avoids
 * the well-known silent rounding issue when large Snowflake IDs are serialized as JSON numbers
 * and consumed by JavaScript frontends.</p>
 * <p>生成时间有序的64位ID，其值始终≤ 2^53−1（9,007,199,254,740,991），
 * 即JavaScript能精确表示为{@code Number}的最大整数。
 * 避免大Snowflake ID序列化为JSON数字被JavaScript前端消费时的静默四舍五入问题。</p>
 *
 * <p><strong>Bit Layout | 位布局 (53 bits total):</strong></p>
 * <pre>
 * [ 41-bit timestamp | 6-bit workerId | 6-bit sequence ]
 *   ms since epoch     0-63             0-63/ms
 * </pre>
 *
 * <p><strong>Capacity | 容量:</strong></p>
 * <ul>
 *   <li>Timestamp: ~69 years from epoch (same as standard Snowflake) | 时间戳: 约69年</li>
 *   <li>Worker nodes: 64 (0-63) | 工作节点: 64个（0-63）</li>
 *   <li>Throughput: 64 IDs/ms per node = ~64,000/s | 吞吐量: 每节点每毫秒64个</li>
 *   <li>Max value: 9,007,199,254,740,991 ≤ Number.MAX_SAFE_INTEGER | 最大值在JS安全整数范围内</li>
 * </ul>
 *
 * <p><strong>Trade-offs vs. Standard Snowflake | 与标准雪花ID的取舍:</strong></p>
 * <ul>
 *   <li>Fewer worker nodes (64 vs 1024) - 工作节点较少（64 vs 1024）</li>
 *   <li>Lower throughput (64 vs 4096/ms) - 吞吐量较低（64 vs 4096/ms）</li>
 *   <li>No datacenter ID bits - 无数据中心ID位</li>
 *   <li>JSON-safe without string serialization workarounds - 无需字符串序列化绕过即可JSON安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default (workerId=0)
 * SafeJsSnowflakeGenerator gen = SafeJsSnowflakeGenerator.create();
 * long id = gen.generate();
 * assert SafeJsSnowflakeGenerator.isJsSafe(id); // always true
 *
 * // With workerId
 * SafeJsSnowflakeGenerator gen2 = SafeJsSnowflakeGenerator.create(7);
 * long id2 = gen2.generate(); // always <= 2^53-1
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ReentrantLock) - 线程安全: 是（可重入锁）</li>
 *   <li>Clock rollback: throws OpenIdGenerationException - 时钟回拨: 抛出异常</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.3
 */
public final class SafeJsSnowflakeGenerator implements IdGenerator<Long> {

    /** JavaScript Number.MAX_SAFE_INTEGER = 2^53 - 1 | JavaScript最大安全整数 */
    public static final long JS_MAX_SAFE_INT = 9007199254740991L;

    /** Default epoch: 2021-01-01 00:00:00 UTC | 默认起始时间: 2021-01-01 UTC */
    private static final long DEFAULT_EPOCH = 1609459200000L;

    // Bit layout: 41 timestamp + 6 workerId + 6 sequence = 53 bits
    private static final int SEQUENCE_BITS   = 6;
    private static final int WORKER_ID_BITS  = 6;
    private static final long MAX_WORKER_ID  = (1L << WORKER_ID_BITS) - 1;   // 63
    private static final long MAX_SEQUENCE   = (1L << SEQUENCE_BITS) - 1;    // 63
    private static final int WORKER_ID_SHIFT = SEQUENCE_BITS;                // 6
    private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS; // 12

    private final long workerId;
    private final long epoch;
    private final ReentrantLock lock = new ReentrantLock();

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    private SafeJsSnowflakeGenerator(long workerId, long epoch) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw OpenIdGenerationException.invalidParameter(
                    "workerId", workerId, "[0, " + MAX_WORKER_ID + "]");
        }
        this.workerId = workerId;
        this.epoch = epoch;
    }

    /**
     * Creates a generator with workerId=0 and the default epoch
     * 使用workerId=0和默认起始时间创建生成器
     *
     * @return generator | 生成器
     */
    public static SafeJsSnowflakeGenerator create() {
        return new SafeJsSnowflakeGenerator(0, DEFAULT_EPOCH);
    }

    /**
     * Creates a generator with the specified workerId
     * 使用指定workerId创建生成器
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * SafeJsSnowflakeGenerator.create(0)   // node 0 (default)
     * SafeJsSnowflakeGenerator.create(63)  // node 63 (max)
     * </pre>
     *
     * @param workerId the worker node ID (0-63) | 工作节点ID（0-63）
     * @return generator | 生成器
     * @throws OpenIdGenerationException if workerId is out of range | workerId越界时抛出
     */
    public static SafeJsSnowflakeGenerator create(long workerId) {
        return new SafeJsSnowflakeGenerator(workerId, DEFAULT_EPOCH);
    }

    /**
     * Creates a generator with specified workerId and epoch
     * 使用指定workerId和起始时间创建生成器
     *
     * @param workerId  the worker node ID (0-63) | 工作节点ID（0-63）
     * @param epochMillis the epoch in milliseconds | 起始时间（毫秒）
     * @return generator | 生成器
     * @throws OpenIdGenerationException if workerId is out of range | workerId越界时抛出
     */
    public static SafeJsSnowflakeGenerator create(long workerId, long epochMillis) {
        return new SafeJsSnowflakeGenerator(workerId, epochMillis);
    }

    /**
     * Generates a JavaScript-safe Snowflake ID
     * 生成JavaScript安全的雪花ID
     *
     * <p><strong>Performance | 性能:</strong></p>
     * <p>Time: O(1) amortized, Space: O(1)</p>
     * <p>时间: O(1) 均摊, 空间: O(1)</p>
     *
     * @return ID ≤ 2^53-1 | ID值 ≤ 2^53-1
     * @throws OpenIdGenerationException on clock backward | 时钟回拨时抛出
     */
    @Override
    public Long generate() {
        lock.lock();
        try {
            long timestamp = System.currentTimeMillis() - epoch;

            if (timestamp < lastTimestamp) {
                throw OpenIdGenerationException.clockBackward(lastTimestamp + epoch, timestamp + epoch);
            }

            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & MAX_SEQUENCE;
                if (sequence == 0) {
                    // Sequence exhausted for this ms — wait for next
                    long current = System.currentTimeMillis() - epoch;
                    while (current <= lastTimestamp) {
                        Thread.onSpinWait();
                        current = System.currentTimeMillis() - epoch;
                    }
                    timestamp = current;
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            long id = (timestamp << TIMESTAMP_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
            if (id < 0 || id > JS_MAX_SAFE_INT) {
                throw OpenIdGenerationException.invalidParameter(
                        "generatedId", id, "[0, " + JS_MAX_SAFE_INT + "] — check epoch configuration");
            }
            return id;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Generates the ID as a decimal string (always safe to pass to JavaScript as-is)
     * 生成ID的十进制字符串（可直接安全传递给JavaScript）
     *
     * @return decimal ID string | 十进制ID字符串
     */
    public String generateStr() {
        return String.valueOf(generate());
    }

    /**
     * Returns the worker node ID configured for this generator
     * 返回此生成器配置的工作节点ID
     *
     * @return worker ID | 工作节点ID
     */
    public long getWorkerId() {
        return workerId;
    }

    /**
     * Returns the epoch used by this generator
     * 返回此生成器使用的起始时间
     *
     * @return epoch in milliseconds | 起始时间（毫秒）
     */
    public long getEpoch() {
        return epoch;
    }

    /**
     * Checks whether a long value is within JavaScript's safe integer range
     * 检查long值是否在JavaScript安全整数范围内
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * isJsSafe(9007199254740991L) = true   // JS_MAX_SAFE_INT
     * isJsSafe(9007199254740992L) = false  // JS_MAX_SAFE_INT + 1
     * isJsSafe(-1L)               = false  // negative
     * </pre>
     *
     * @param id the ID to check | 要检查的ID
     * @return true if safe for JavaScript | 对JavaScript安全则返回true
     */
    public static boolean isJsSafe(long id) {
        return id >= 0 && id <= JS_MAX_SAFE_INT;
    }

    @Override
    public String getType() {
        return "SafeJsSnowflake";
    }
}
