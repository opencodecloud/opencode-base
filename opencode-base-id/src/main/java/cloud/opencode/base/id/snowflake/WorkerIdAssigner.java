package cloud.opencode.base.id.snowflake;

/**
 * Worker ID Assigner Interface
 * 工作节点ID分配器接口
 *
 * <p>Provides automatic assignment of worker ID and datacenter ID for Snowflake generator.
 * This is especially useful in containerized or Kubernetes environments.</p>
 * <p>为雪花ID生成器提供工作节点ID和数据中心ID的自动分配。
 * 这在容器化或Kubernetes环境中特别有用。</p>
 *
 * <p><strong>Built-in Implementations | 内置实现:</strong></p>
 * <ul>
 *   <li>{@link IpBasedAssigner} - Based on IP address | 基于IP地址</li>
 *   <li>{@link MacBasedAssigner} - Based on MAC address | 基于MAC地址</li>
 *   <li>{@link RandomAssigner} - Random assignment | 随机分配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SnowflakeGenerator gen = new SnowflakeBuilder()
 *     .workerIdAssigner(IpBasedAssigner.create())
 *     .build();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Strategy interface for assigning Snowflake worker IDs - 分配Snowflake工作ID的策略接口</li>
 *   <li>Pluggable worker identification mechanism - 可插拔的工作节点标识机制</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.1.0
 */
public interface WorkerIdAssigner {

    /**
     * Assigns a worker ID
     * 分配工作节点ID
     *
     * @return worker ID within valid range | 有效范围内的工作节点ID
     */
    long assignWorkerId();

    /**
     * Assigns a datacenter ID
     * 分配数据中心ID
     *
     * @return datacenter ID within valid range | 有效范围内的数据中心ID
     */
    long assignDatacenterId();

    /**
     * Gets a description of the assignment strategy
     * 获取分配策略描述
     *
     * @return strategy description | 策略描述
     */
    default String getStrategyName() {
        return getClass().getSimpleName();
    }
}
