package cloud.opencode.base.id.snowflake;

/**
 * Fixed Worker ID Assigner - Explicitly configured, deterministic worker and datacenter IDs
 * 固定工作节点ID分配器 - 显式配置的确定性工作节点和数据中心ID
 *
 * <p>Provides statically configured worker and datacenter IDs for the Snowflake generator.
 * Prefer this class over {@link RandomAssigner#of(long, long)} when you need explicit,
 * deterministic ID assignment — the class name clearly communicates its intent.</p>
 * <p>为雪花ID生成器提供静态配置的工作节点和数据中心ID。
 * 当需要显式、确定性的ID分配时，优先使用此类而非{@link RandomAssigner#of(long, long)}——
 * 类名清晰地传达了意图。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Explicit, deterministic assignment - 显式、确定性分配</li>
 *   <li>Suitable for static/on-premise deployments - 适用于静态/本地部署</li>
 *   <li>Clear naming vs. RandomAssigner.of() - 比RandomAssigner.of()命名更清晰</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Node 3 in datacenter 1
 * WorkerIdAssigner assigner = FixedWorkerIdAssigner.of(3, 1);
 *
 * SnowflakeGenerator gen = SnowflakeGenerator.builder()
 *     .workerIdAssigner(assigner)
 *     .build();
 *
 * long id = gen.generate();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.3
 */
public final class FixedWorkerIdAssigner implements WorkerIdAssigner {

    private final long workerId;
    private final long datacenterId;

    /**
     * Creates a fixed assigner with specified worker and datacenter IDs
     * 使用指定的工作节点和数据中心ID创建固定分配器
     *
     * @param workerId     the worker node ID (must be &gt;= 0) | 工作节点ID（必须&gt;=0）
     * @param datacenterId the datacenter ID (must be &gt;= 0) | 数据中心ID（必须&gt;=0）
     * @throws IllegalArgumentException if either ID is negative | 任一ID为负数时抛出
     */
    private FixedWorkerIdAssigner(long workerId, long datacenterId) {
        if (workerId < 0) {
            throw new IllegalArgumentException("workerId must be >= 0, got: " + workerId);
        }
        if (datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId must be >= 0, got: " + datacenterId);
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * Creates a FixedWorkerIdAssigner with specified worker and datacenter IDs
     * 创建具有指定工作节点和数据中心ID的FixedWorkerIdAssigner
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * FixedWorkerIdAssigner.of(0, 0)   // both default to 0
     * FixedWorkerIdAssigner.of(7, 2)   // worker=7, datacenter=2
     * </pre>
     *
     * @param workerId     the worker node ID (0-31 for default bit config) | 工作节点ID（默认位配置下0-31）
     * @param datacenterId the datacenter ID (0-31 for default bit config) | 数据中心ID（默认位配置下0-31）
     * @return assigner instance | 分配器实例
     * @throws IllegalArgumentException if either ID is negative | 任一ID为负数时抛出
     */
    public static FixedWorkerIdAssigner of(long workerId, long datacenterId) {
        return new FixedWorkerIdAssigner(workerId, datacenterId);
    }

    /**
     * Returns the configured worker ID
     * 返回配置的工作节点ID
     *
     * @return worker ID | 工作节点ID
     */
    @Override
    public long assignWorkerId() {
        return workerId;
    }

    /**
     * Returns the configured datacenter ID
     * 返回配置的数据中心ID
     *
     * @return datacenter ID | 数据中心ID
     */
    @Override
    public long assignDatacenterId() {
        return datacenterId;
    }

    /**
     * Returns a descriptive strategy name including the configured IDs
     * 返回包含已配置ID的描述性策略名称
     *
     * @return strategy name | 策略名称
     */
    @Override
    public String getStrategyName() {
        return "fixed[workerId=" + workerId + ", datacenterId=" + datacenterId + "]";
    }
}
