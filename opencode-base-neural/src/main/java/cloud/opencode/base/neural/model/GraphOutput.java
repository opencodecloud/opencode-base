package cloud.opencode.base.neural.model;

import java.util.Objects;

/**
 * Graph Output Descriptor
 * 计算图输出描述符
 *
 * <p>Immutable record describing an output from the computation graph.
 * Each output references a specific node by index and an output slot
 * within that node's result list.</p>
 * <p>描述计算图输出的不可变记录。
 * 每个输出通过索引引用特定节点，并指定该节点结果列表中的输出槽位。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param name       output name | 输出名称
 * @param nodeIndex  index of the source node in the graph | 源节点在计算图中的索引
 * @param outputSlot output slot index within the node's results | 节点结果中的输出槽位索引
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Graph
 * @see GraphNode
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public record GraphOutput(String name, int nodeIndex, int outputSlot) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造器
     */
    public GraphOutput {
        Objects.requireNonNull(name, "name must not be null");
        if (nodeIndex < 0) {
            throw new IllegalArgumentException("nodeIndex must not be negative");
        }
        if (outputSlot < 0) {
            throw new IllegalArgumentException("outputSlot must not be negative");
        }
    }
}
