package cloud.opencode.base.id.snowflake;

import java.security.SecureRandom;

/**
 * Random Worker ID Assigner
 * 随机工作节点ID分配器
 *
 * <p>Randomly assigns worker ID and datacenter ID.
 * Simple and suitable for testing or low-collision scenarios.</p>
 * <p>随机分配工作节点ID和数据中心ID。
 * 简单，适用于测试或低碰撞场景。</p>
 *
 * <p><strong>Note | 注意:</strong></p>
 * <ul>
 *   <li>IDs are generated once at creation time - ID在创建时生成一次</li>
 *   <li>Collision is possible in large clusters - 在大型集群中可能发生碰撞</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * WorkerIdAssigner assigner = RandomAssigner.create();
 * long workerId = assigner.assignWorkerId();
 * long datacenterId = assigner.assignDatacenterId();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Assigns worker ID using secure random generation - 使用安全随机生成分配工作ID</li>
 *   <li>Suitable for environments without stable network identity - 适用于无稳定网络标识的环境</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.1.0
 */
public final class RandomAssigner implements WorkerIdAssigner {

    private static final int MAX_WORKER_ID = 31;
    private static final int MAX_DATACENTER_ID = 31;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final long workerId;
    private final long datacenterId;

    private RandomAssigner() {
        this.workerId = RANDOM.nextInt(MAX_WORKER_ID + 1);
        this.datacenterId = RANDOM.nextInt(MAX_DATACENTER_ID + 1);
    }

    private RandomAssigner(long workerId, long datacenterId) {
        this.workerId = workerId % (MAX_WORKER_ID + 1);
        this.datacenterId = datacenterId % (MAX_DATACENTER_ID + 1);
    }

    /**
     * Creates a random assigner
     * 创建随机分配器
     *
     * @return assigner | 分配器
     */
    public static RandomAssigner create() {
        return new RandomAssigner();
    }

    /**
     * Creates an assigner with fixed IDs
     * 创建使用固定ID的分配器
     *
     * @param workerId     the worker ID | 工作节点ID
     * @param datacenterId the datacenter ID | 数据中心ID
     * @return assigner | 分配器
     */
    public static RandomAssigner of(long workerId, long datacenterId) {
        return new RandomAssigner(workerId, datacenterId);
    }

    @Override
    public long assignWorkerId() {
        return workerId;
    }

    @Override
    public long assignDatacenterId() {
        return datacenterId;
    }

    @Override
    public String getStrategyName() {
        return "Random";
    }
}
