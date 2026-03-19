package cloud.opencode.base.hash.consistent;

import java.util.Objects;

/**
 * Hash ring node representation
 * 哈希环节点表示
 *
 * <p>Represents a physical node in a consistent hash ring with an identifier,
 * associated data, and weight for load balancing.</p>
 * <p>表示一致性哈希环中的物理节点，包含标识符、关联数据和用于负载均衡的权重。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unique identifier - 唯一标识符</li>
 *   <li>Associated data - 关联数据</li>
 *   <li>Weight for load balancing - 用于负载均衡的权重</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a node with default weight
 * HashNode<String> node = HashNode.of("server1", "192.168.1.1");
 *
 * // Create a weighted node
 * HashNode<String> weighted = HashNode.of("server2", "192.168.1.2", 2);
 * }</pre>
 *
 * @param id     unique node identifier | 唯一节点标识符
 * @param data   node data | 节点数据
 * @param weight node weight (affects virtual node count) | 节点权重（影响虚拟节点数量）
 * @param <T>    data type | 数据类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public record HashNode<T>(
        String id,
        T data,
        int weight
) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造函数
     */
    public HashNode {
        Objects.requireNonNull(id, "Node id cannot be null");
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a node with default weight (1)
     * 使用默认权重（1）创建节点
     *
     * @param id   node id | 节点ID
     * @param data node data | 节点数据
     * @param <T>  data type | 数据类型
     * @return hash node | 哈希节点
     */
    public static <T> HashNode<T> of(String id, T data) {
        return new HashNode<>(id, data, 1);
    }

    /**
     * Creates a weighted node
     * 创建带权重的节点
     *
     * @param id     node id | 节点ID
     * @param data   node data | 节点数据
     * @param weight node weight | 节点权重
     * @param <T>    data type | 数据类型
     * @return hash node | 哈希节点
     */
    public static <T> HashNode<T> of(String id, T data, int weight) {
        return new HashNode<>(id, data, weight);
    }

    @Override
    public String toString() {
        return "HashNode{id='" + id + "', data=" + data + ", weight=" + weight + "}";
    }
}
